//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_RESET_CONNECTION_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_RESET_CONNECTION_HPP

#include <boost/mysql/character_set.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>

#include <boost/mysql/detail/algo_params.hpp>

#include <boost/mysql/impl/internal/protocol/protocol.hpp>
#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>
#include <boost/mysql/impl/internal/sansio/set_character_set.hpp>

#include <boost/asio/coroutine.hpp>

namespace boost {
namespace mysql {
namespace detail {

class reset_connection_algo : public sansio_algorithm, asio::coroutine
{
    diagnostics* diag_;
    character_set charset_;
    std::uint8_t reset_seqnum_{0};
    std::uint8_t set_names_seqnum_{0};
    error_code stored_ec_;

    // true if we need to pipeline a SET NAMES with the reset request
    bool has_charset() const noexcept { return !charset_.name.empty(); }

    next_action compose_request()
    {
        if (has_charset())
        {
            // Compose the SET NAMES statement
            auto query = compose_set_names(charset_);
            if (query.has_error())
                return query.error();

            // Compose the pipeline
            st_->writer.prepare_pipelined_write(
                reset_connection_command{},
                reset_seqnum_,
                query_command{query.value()},
                set_names_seqnum_
            );

            // Success
            return next_action::write({});
        }
        else
        {
            // Just compose the reset connection request
            return write(reset_connection_command{}, reset_seqnum_);
        }
    }

public:
    reset_connection_algo(connection_state_data& st, reset_connection_algo_params params) noexcept
        : sansio_algorithm(st), diag_(params.diag), charset_(params.charset)
    {
    }

    next_action resume(error_code ec)
    {
        if (ec)
            return ec;

        BOOST_ASIO_CORO_REENTER(*this)
        {
            // Clear diagnostics
            diag_->clear();

            // Send the request
            BOOST_ASIO_CORO_YIELD return compose_request();

            // Read the reset response
            BOOST_ASIO_CORO_YIELD return read(reset_seqnum_);

            // Verify it's what we expected
            stored_ec_ = st_->deserialize_ok(*diag_);

            if (!stored_ec_)
            {
                // Reset was successful. Resetting changes the connection's character set
                // to the server's default, which is an unknown value that doesn't have to match
                // what was specified in handshake. As a safety measure, clear the current charset
                st_->current_charset = character_set{};
            }

            if (has_charset())
            {
                // We issued a SET NAMES too, read its response
                BOOST_ASIO_CORO_YIELD return read(set_names_seqnum_);

                // Verify it's what we expected. Don't overwrite diagnostics if reset failed
                ec = st_->deserialize_ok(stored_ec_ ? st_->shared_diag : *diag_);
                if (!ec)
                {
                    // Set the character set to the new known value
                    st_->current_charset = charset_;
                }

                // Set the return value if there is no error code already stored
                if (!stored_ec_)
                    stored_ec_ = ec;
            }

            return stored_ec_;
        }

        return next_action();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif /* INCLUDE_BOOST_MYSQL_DETAIL_NETWORK_ALGORITHMS_IMPL_CLOSE_STATEMENT_HPP_ */
