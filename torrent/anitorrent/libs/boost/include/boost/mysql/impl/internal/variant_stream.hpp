//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_VARIANT_STREAM_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_VARIANT_STREAM_HPP

#include <boost/mysql/error_code.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/any_stream.hpp>
#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/connect_params_helpers.hpp>

#include <boost/mysql/impl/internal/ssl_context_with_default.hpp>

#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/compose.hpp>
#include <boost/asio/connect.hpp>
#include <boost/asio/coroutine.hpp>
#include <boost/asio/error.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/local/stream_protocol.hpp>
#include <boost/asio/post.hpp>
#include <boost/asio/ssl/context.hpp>
#include <boost/asio/ssl/stream.hpp>
#include <boost/optional/optional.hpp>
#include <boost/variant2/variant.hpp>

#include <string>

namespace boost {
namespace mysql {
namespace detail {

// Asio defines a "string view parameter" to be either const std::string&,
// std::experimental::string_view or std::string_view. Casting from the Boost
// version doesn't work for std::experimental::string_view
#if defined(BOOST_ASIO_HAS_STD_STRING_VIEW)
inline std::string_view cast_asio_sv_param(string_view input) noexcept { return input; }
#elif defined(BOOST_ASIO_HAS_STD_EXPERIMENTAL_STRING_VIEW)
inline std::experimental::string_view cast_asio_sv_param(string_view input) noexcept
{
    return {input.data(), input.size()};
}
#else
inline std::string cast_asio_sv_param(string_view input) { return input; }
#endif

class variant_stream final : public any_stream
{
    any_address_view address_;

public:
    variant_stream(asio::any_io_executor ex, asio::ssl::context* ctx)
        : any_stream(true), ex_(std::move(ex)), ssl_ctx_(ctx)
    {
    }

    void set_endpoint(const void* value) override final
    {
        address_ = *static_cast<const any_address_view*>(value);
    }

    // Executor
    executor_type get_executor() override final { return ex_; }

    // SSL
    void handshake(error_code& ec) override final
    {
        create_ssl_stream();
        ssl_->handshake(asio::ssl::stream_base::client, ec);
    }

    void async_handshake(asio::any_completion_handler<void(error_code)> handler) override final
    {
        create_ssl_stream();
        ssl_->async_handshake(asio::ssl::stream_base::client, std::move(handler));
    }

    void shutdown(error_code& ec) override final
    {
        BOOST_ASSERT(ssl_.has_value());
        ssl_->shutdown(ec);
    }

    void async_shutdown(asio::any_completion_handler<void(error_code)> handler) override final
    {
        BOOST_ASSERT(ssl_.has_value());
        ssl_->async_shutdown(std::move(handler));
    }

    // Reading
    std::size_t read_some(asio::mutable_buffer buff, bool use_ssl, error_code& ec) override final
    {
        if (use_ssl)
        {
            BOOST_ASSERT(ssl_.has_value());
            return ssl_->read_some(buff, ec);
        }
        else if (auto* tcp_sock = variant2::get_if<socket_and_resolver>(&sock_))
        {
            return tcp_sock->sock.read_some(buff, ec);
        }
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
        else if (auto* unix_sock = variant2::get_if<unix_socket>(&sock_))
        {
            return unix_sock->read_some(buff, ec);
        }
#endif
        else
        {
            BOOST_ASSERT(false);
            return 0u;
        }
    }

    void async_read_some(
        asio::mutable_buffer buff,
        bool use_ssl,
        asio::any_completion_handler<void(error_code, std::size_t)> handler
    ) override final
    {
        if (use_ssl)
        {
            BOOST_ASSERT(ssl_.has_value());
            ssl_->async_read_some(buff, std::move(handler));
        }
        else if (auto* tcp_sock = variant2::get_if<socket_and_resolver>(&sock_))
        {
            tcp_sock->sock.async_read_some(buff, std::move(handler));
        }
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
        else if (auto* unix_sock = variant2::get_if<unix_socket>(&sock_))
        {
            unix_sock->async_read_some(buff, std::move(handler));
        }
#endif
        else
        {
            BOOST_ASSERT(false);
        }
    }

    // Writing
    std::size_t write_some(boost::asio::const_buffer buff, bool use_ssl, error_code& ec) override final
    {
        if (use_ssl)
        {
            BOOST_ASSERT(ssl_.has_value());
            return ssl_->write_some(buff, ec);
        }
        else if (auto* tcp_sock = variant2::get_if<socket_and_resolver>(&sock_))
        {
            return tcp_sock->sock.write_some(buff, ec);
        }
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
        else if (auto* unix_sock = variant2::get_if<unix_socket>(&sock_))
        {
            return unix_sock->write_some(buff, ec);
        }
#endif
        else
        {
            BOOST_ASSERT(false);
            return 0u;
        }
    }

    void async_write_some(
        boost::asio::const_buffer buff,
        bool use_ssl,
        asio::any_completion_handler<void(error_code, std::size_t)> handler
    ) override final
    {
        if (use_ssl)
        {
            BOOST_ASSERT(ssl_.has_value());
            return ssl_->async_write_some(buff, std::move(handler));
        }
        else if (auto* tcp_sock = variant2::get_if<socket_and_resolver>(&sock_))
        {
            return tcp_sock->sock.async_write_some(buff, std::move(handler));
        }
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
        else if (auto* unix_sock = variant2::get_if<unix_socket>(&sock_))
        {
            return unix_sock->async_write_some(buff, std::move(handler));
        }
#endif
        else
        {
            BOOST_ASSERT(false);
        }
    }

    // Connect and close
    void connect(error_code& ec) override final
    {
        ec = setup_stream();
        if (ec)
            return;

        if (address_.type == address_type::host_and_port)
        {
            // Resolve endpoints
            auto& tcp_sock = variant2::unsafe_get<1>(sock_);
            auto endpoints = tcp_sock.resolv.resolve(
                cast_asio_sv_param(address_.address),
                std::to_string(address_.port),
                ec
            );
            if (ec)
                return;

            // Connect stream
            asio::connect(tcp_sock.sock, std::move(endpoints), ec);
        }
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
        else
        {
            BOOST_ASSERT(address_.type == address_type::unix_path);

            // Just connect the stream
            auto& unix_sock = variant2::unsafe_get<2>(sock_);
            unix_sock.connect(cast_asio_sv_param(address_.address), ec);
        }
#endif
    }

    void async_connect(asio::any_completion_handler<void(error_code)> handler) override final
    {
        asio::async_compose<asio::any_completion_handler<void(error_code)>, void(error_code)>(
            connect_op(*this),
            handler,
            ex_
        );
    }

    void close(error_code& ec) override final
    {
        if (auto* tcp_sock = variant2::get_if<socket_and_resolver>(&sock_))
        {
            tcp_sock->sock.close(ec);
        }
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
        else if (auto* unix_sock = variant2::get_if<unix_socket>(&sock_))
        {
            unix_sock->close(ec);
        }
#endif
    }

private:
    struct socket_and_resolver
    {
        asio::ip::tcp::socket sock;
        asio::ip::tcp::resolver resolv;

        socket_and_resolver(asio::any_io_executor ex) : sock(ex), resolv(std::move(ex)) {}
    };

#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
    using unix_socket = asio::local::stream_protocol::socket;
#endif

    asio::any_io_executor ex_;
    variant2::variant<
        variant2::monostate,
        socket_and_resolver
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
        ,
        unix_socket
#endif
        >
        sock_;
    ssl_context_with_default ssl_ctx_;
    boost::optional<asio::ssl::stream<asio::ip::tcp::socket&>> ssl_;

    error_code setup_stream()
    {
        if (address_.type == address_type::host_and_port)
        {
            // Clean up any previous state
            sock_.emplace<socket_and_resolver>(ex_);
        }

        else if (address_.type == address_type::unix_path)
        {
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
            // Clean up any previous state
            sock_.emplace<unix_socket>(ex_);
#else
            return asio::error::operation_not_supported;
#endif
        }

        return error_code();
    }

    void create_ssl_stream()
    {
        // The stream object must be re-created even if it already exists, since
        // once used for a connection (anytime after ssl::stream::handshake is called),
        // it can't be re-used for any subsequent connections
        BOOST_ASSERT(variant2::holds_alternative<socket_and_resolver>(sock_));
        ssl_.emplace(variant2::unsafe_get<1>(sock_).sock, ssl_ctx_.get());
    }

    struct connect_op : boost::asio::coroutine
    {
        variant_stream& this_obj_;
        error_code stored_ec_;

        connect_op(variant_stream& this_obj) noexcept : this_obj_(this_obj) {}

        template <class Self>
        void operator()(Self& self, error_code ec = {}, asio::ip::tcp::resolver::results_type endpoints = {})
        {
            if (ec)
            {
                self.complete(ec);
                return;
            }

            BOOST_ASIO_CORO_REENTER(*this)
            {
                // Setup stream
                stored_ec_ = this_obj_.setup_stream();
                if (stored_ec_)
                {
                    BOOST_ASIO_CORO_YIELD asio::post(this_obj_.ex_, std::move(self));
                    self.complete(stored_ec_);
                    return;
                }

                if (this_obj_.address_.type == address_type::host_and_port)
                {
                    // Resolve endpoints
                    BOOST_ASIO_CORO_YIELD
                    variant2::unsafe_get<1>(this_obj_.sock_)
                        .resolv.async_resolve(
                            cast_asio_sv_param(this_obj_.address_.address),
                            std::to_string(this_obj_.address_.port),
                            std::move(self)
                        );

                    // Connect stream
                    BOOST_ASIO_CORO_YIELD
                    asio::async_connect(
                        variant2::unsafe_get<1>(this_obj_.sock_).sock,
                        std::move(endpoints),
                        std::move(self)
                    );

                    // The final handler requires a void(error_code, tcp::endpoint signature),
                    // which this function can't implement. See operator() overload below.
                }
#ifdef BOOST_ASIO_HAS_LOCAL_SOCKETS
                else
                {
                    BOOST_ASSERT(this_obj_.address_.type == address_type::unix_path);

                    // Just connect the stream
                    BOOST_ASIO_CORO_YIELD
                    variant2::unsafe_get<2>(this_obj_.sock_)
                        .async_connect(cast_asio_sv_param(this_obj_.address_.address), std::move(self));

                    self.complete(error_code());
                }
#endif
            }
        }

        template <class Self>
        void operator()(Self& self, error_code ec, asio::ip::tcp::endpoint)
        {
            self.complete(ec);
        }
    };
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
