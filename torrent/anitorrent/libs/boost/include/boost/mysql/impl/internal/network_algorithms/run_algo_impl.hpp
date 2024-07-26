//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_NETWORK_ALGORITHMS_RUN_ALGO_IMPL_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_NETWORK_ALGORITHMS_RUN_ALGO_IMPL_HPP

#include <boost/mysql/error_code.hpp>

#include <boost/mysql/detail/any_stream.hpp>

#include <boost/mysql/impl/internal/sansio/algo_runner.hpp>
#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>

#include <boost/asio/any_completion_handler.hpp>
#include <boost/asio/async_result.hpp>
#include <boost/asio/buffer.hpp>
#include <boost/asio/compose.hpp>
#include <boost/asio/post.hpp>

#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

inline asio::mutable_buffer to_buffer(span<std::uint8_t> buff) noexcept
{
    return asio::mutable_buffer(buff.data(), buff.size());
}

struct run_algo_op : boost::asio::coroutine
{
    any_stream& stream_;
    algo_runner runner_;
    bool has_done_io_{false};
    error_code stored_ec_;

    run_algo_op(any_stream& stream, any_algo_ref algo) noexcept : stream_(stream), runner_(algo) {}

    template <class Self>
    void operator()(Self& self, error_code io_ec = {}, std::size_t bytes_transferred = 0)
    {
        next_action act;

        BOOST_ASIO_CORO_REENTER(*this)
        {
            while (true)
            {
                // Run the op
                act = runner_.resume(io_ec, bytes_transferred);
                if (act.is_done())
                {
                    stored_ec_ = act.error();
                    if (!has_done_io_)
                    {
                        BOOST_ASIO_CORO_YIELD asio::post(stream_.get_executor(), std::move(self));
                    }
                    self.complete(stored_ec_);
                    BOOST_ASIO_CORO_YIELD break;
                }
                else if (act.type() == next_action::type_t::read)
                {
                    BOOST_ASIO_CORO_YIELD stream_.async_read_some(
                        to_buffer(act.read_args().buffer),
                        act.read_args().use_ssl,
                        std::move(self)
                    );
                    has_done_io_ = true;
                }
                else if (act.type() == next_action::type_t::write)
                {
                    BOOST_ASIO_CORO_YIELD stream_.async_write_some(
                        asio::buffer(act.write_args().buffer),
                        act.write_args().use_ssl,
                        std::move(self)
                    );
                    has_done_io_ = true;
                }
                else if (act.type() == next_action::type_t::ssl_handshake)
                {
                    BOOST_ASIO_CORO_YIELD stream_.async_handshake(std::move(self));
                    has_done_io_ = true;
                }
                else if (act.type() == next_action::type_t::ssl_shutdown)
                {
                    BOOST_ASIO_CORO_YIELD stream_.async_shutdown(std::move(self));
                    has_done_io_ = true;
                }
                else if (act.type() == next_action::type_t::connect)
                {
                    BOOST_ASIO_CORO_YIELD stream_.async_connect(std::move(self));
                    has_done_io_ = true;
                }
                else
                {
                    BOOST_ASSERT(act.type() == next_action::type_t::close);
                    stream_.close(io_ec);
                }
            }
        }
    }
};

inline void run_algo_impl(any_stream& stream, any_algo_ref algo, error_code& ec)
{
    ec.clear();
    error_code io_ec;
    std::size_t bytes_transferred = 0;
    algo_runner runner(algo);

    while (true)
    {
        // Run the op
        auto act = runner.resume(io_ec, bytes_transferred);

        // Apply the next action
        bytes_transferred = 0;
        if (act.is_done())
        {
            ec = act.error();
            return;
        }
        else if (act.type() == next_action::type_t::read)
        {
            bytes_transferred = stream.read_some(
                to_buffer(act.read_args().buffer),
                act.read_args().use_ssl,
                io_ec
            );
        }
        else if (act.type() == next_action::type_t::write)
        {
            bytes_transferred = stream.write_some(
                asio::buffer(act.write_args().buffer),
                act.write_args().use_ssl,
                io_ec
            );
        }
        else if (act.type() == next_action::type_t::ssl_handshake)
        {
            stream.handshake(io_ec);
        }
        else if (act.type() == next_action::type_t::ssl_shutdown)
        {
            stream.shutdown(io_ec);
        }
        else if (act.type() == next_action::type_t::connect)
        {
            stream.connect(io_ec);
        }
        else
        {
            BOOST_ASSERT(act.type() == next_action::type_t::close);
            stream.close(io_ec);
        }
    }
}

template <class CompletionToken>
BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code))
async_run_algo_impl(any_stream& stream, any_algo_ref algo, CompletionToken&& token)
{
    return asio::async_compose<CompletionToken, void(error_code)>(run_algo_op(stream, algo), token, stream);
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
