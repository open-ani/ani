//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_HANDSHAKE_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_HANDSHAKE_HPP

#include <boost/mysql/character_set.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/handshake_params.hpp>
#include <boost/mysql/mysql_collations.hpp>

#include <boost/mysql/detail/algo_params.hpp>
#include <boost/mysql/detail/ok_view.hpp>

#include <boost/mysql/impl/internal/auth/auth.hpp>
#include <boost/mysql/impl/internal/protocol/capabilities.hpp>
#include <boost/mysql/impl/internal/protocol/protocol.hpp>
#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/next_action.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>

#include <cstddef>
#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

inline capabilities conditional_capability(bool condition, std::uint32_t cap)
{
    return capabilities(condition ? cap : 0);
}

inline error_code process_capabilities(
    const handshake_params& params,
    const server_hello& hello,
    capabilities& negotiated_caps
)
{
    auto ssl = params.ssl();
    capabilities server_caps = hello.server_capabilities;
    capabilities required_caps = mandatory_capabilities |
                                 conditional_capability(!params.database().empty(), CLIENT_CONNECT_WITH_DB) |
                                 conditional_capability(params.multi_queries(), CLIENT_MULTI_STATEMENTS) |
                                 conditional_capability(ssl == ssl_mode::require, CLIENT_SSL);
    if (required_caps.has(CLIENT_SSL) && !server_caps.has(CLIENT_SSL))
    {
        // This happens if the server doesn't have SSL configured. This special
        // error code helps users diagnosing their problem a lot (server_unsupported doesn't).
        return make_error_code(client_errc::server_doesnt_support_ssl);
    }
    else if (!server_caps.has_all(required_caps))
    {
        return make_error_code(client_errc::server_unsupported);
    }
    negotiated_caps = server_caps & (required_caps | optional_capabilities |
                                     conditional_capability(ssl == ssl_mode::enable, CLIENT_SSL));
    return error_code();
}

class handshake_algo : public sansio_algorithm, asio::coroutine
{
    diagnostics* diag_;
    handshake_params hparams_;
    auth_response auth_resp_;
    std::uint8_t sequence_number_{0};
    bool secure_channel_{false};

    // Attempts to map the collection_id to a character set. We try to be conservative
    // here, since servers will happily accept unknown collation IDs, silently defaulting
    // to the server's default character set (often latin1, which is not Unicode).
    static character_set collation_id_to_charset(std::uint16_t collation_id) noexcept
    {
        switch (collation_id)
        {
        case mysql_collations::utf8mb4_bin:
        case mysql_collations::utf8mb4_general_ci: return utf8mb4_charset;
        case mysql_collations::ascii_general_ci:
        case mysql_collations::ascii_bin: return ascii_charset;
        default: return character_set{};
        }
    }

    // Once the handshake is processed, the capabilities are stored in the connection state
    bool use_ssl() const noexcept { return st_->current_capabilities.has(CLIENT_SSL); }

    error_code process_handshake(span<const std::uint8_t> buffer)
    {
        // Deserialize server hello
        server_hello hello{};
        auto err = deserialize_server_hello(buffer, hello, *diag_);
        if (err)
            return err;

        // Check capabilities
        capabilities negotiated_caps;
        err = process_capabilities(hparams_, hello, negotiated_caps);
        if (err)
            return err;

        // Set capabilities & db flavor
        st_->current_capabilities = negotiated_caps;
        st_->flavor = hello.server;

        // If we're using SSL, mark the channel as secure
        secure_channel_ = secure_channel_ || use_ssl();

        // Compute auth response
        return compute_auth_response(
            hello.auth_plugin_name,
            hparams_.password(),
            hello.auth_plugin_data.to_span(),
            secure_channel_,
            auth_resp_
        );
    }

    // Response to that initial greeting
    ssl_request compose_ssl_request()
    {
        return ssl_request{
            st_->current_capabilities,
            static_cast<std::uint32_t>(MAX_PACKET_SIZE),
            hparams_.connection_collation(),
        };
    }

    login_request compose_login_request()
    {
        return login_request{
            st_->current_capabilities,
            static_cast<std::uint32_t>(MAX_PACKET_SIZE),
            hparams_.connection_collation(),
            hparams_.username(),
            auth_resp_.data,
            hparams_.database(),
            auth_resp_.plugin_name,
        };
    }

    // Processes auth_switch and auth_more_data messages, and leaves the result in auth_resp_
    error_code process_auth_switch(auth_switch msg)
    {
        return compute_auth_response(
            msg.plugin_name,
            hparams_.password(),
            msg.auth_data,
            secure_channel_,
            auth_resp_
        );
    }

    error_code process_auth_more_data(span<const std::uint8_t> data)
    {
        return compute_auth_response(
            auth_resp_.plugin_name,
            hparams_.password(),
            data,
            secure_channel_,
            auth_resp_
        );
    }

    // Composes an auth_switch_response message with the contents of auth_resp_
    auth_switch_response compose_auth_switch_response() const noexcept
    {
        return auth_switch_response{auth_resp_.data};
    }

    void on_success(const ok_view& ok)
    {
        st_->is_connected = true;
        st_->backslash_escapes = ok.backslash_escapes();
        st_->current_charset = collation_id_to_charset(hparams_.connection_collation());
    }

    error_code process_ok()
    {
        ok_view res{};
        auto ec = deserialize_ok_packet(st_->reader.message(), res);
        if (ec)
            return ec;
        on_success(res);
        return error_code();
    }

    static handshake_params fix_ssl_mode(const handshake_params& input, bool transport_supports_ssl) noexcept
    {
        handshake_params res(input);
        if (!transport_supports_ssl)
            res.set_ssl(ssl_mode::disable);
        return res;
    }

public:
    handshake_algo(connection_state_data& st, handshake_algo_params params) noexcept
        : sansio_algorithm(st),
          diag_(params.diag),
          hparams_(fix_ssl_mode(params.hparams, st.supports_ssl())),
          secure_channel_(params.secure_channel)
    {
    }

    diagnostics& diag() noexcept { return *diag_; }

    next_action resume(error_code ec)
    {
        if (ec)
            return ec;

        handhake_server_response resp(error_code{});

        BOOST_ASIO_CORO_REENTER(*this)
        {
            // Setup
            diag_->clear();
            st_->reset();

            // Read server greeting
            BOOST_ASIO_CORO_YIELD return read(sequence_number_);

            // Process server greeting
            ec = process_handshake(st_->reader.message());
            if (ec)
                return ec;

            // SSL
            if (use_ssl())
            {
                // Send SSL request
                BOOST_ASIO_CORO_YIELD return write(compose_ssl_request(), sequence_number_);

                // SSL handshake
                BOOST_ASIO_CORO_YIELD return next_action::ssl_handshake();

                // Mark the connection as using ssl
                st_->ssl = ssl_state::active;
            }

            // Compose and send handshake response
            BOOST_ASIO_CORO_YIELD return write(compose_login_request(), sequence_number_);

            // Auth message exchange
            while (true)
            {
                // Receive response
                BOOST_ASIO_CORO_YIELD return read(sequence_number_);

                // Process it
                resp = deserialize_handshake_server_response(st_->reader.message(), st_->flavor, *diag_);
                if (resp.type == handhake_server_response::type_t::ok)
                {
                    // Auth success, quit
                    on_success(resp.data.ok);
                    return next_action();
                }
                else if (resp.type == handhake_server_response::type_t::error)
                {
                    // Error, quit
                    return resp.data.err;
                }
                else if (resp.type == handhake_server_response::type_t::auth_switch)
                {
                    // Compute response
                    ec = process_auth_switch(resp.data.auth_sw);
                    if (ec)
                        return ec;

                    BOOST_ASIO_CORO_YIELD return write(compose_auth_switch_response(), sequence_number_);
                }
                else if (resp.type == handhake_server_response::type_t::ok_follows)
                {
                    // The next packet must be an OK packet. Read it
                    BOOST_ASIO_CORO_YIELD return read(sequence_number_);

                    // Process it
                    // Regardless of whether we succeeded or not, we're done
                    return process_ok();
                }
                else
                {
                    BOOST_ASSERT(resp.type == handhake_server_response::type_t::auth_more_data);

                    // Compute response
                    ec = process_auth_more_data(resp.data.more_data);
                    if (ec)
                        return ec;

                    // Write response
                    BOOST_ASIO_CORO_YIELD return write(compose_auth_switch_response(), sequence_number_);
                }
            }
        }

        return next_action();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
