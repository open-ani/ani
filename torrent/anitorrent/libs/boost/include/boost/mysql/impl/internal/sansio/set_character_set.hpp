//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_SET_CHARACTER_SET_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_SET_CHARACTER_SET_HPP

#include <boost/mysql/character_set.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/format_sql.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/algo_params.hpp>

#include <boost/mysql/impl/internal/protocol/protocol.hpp>
#include <boost/mysql/impl/internal/sansio/next_action.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>
#include <boost/system/result.hpp>

#include <string>

namespace boost {
namespace mysql {
namespace detail {

// Securely compose a SET NAMES statement. This function
// is also used by the pipelined version of reset_connection
inline system::result<std::string> compose_set_names(const character_set& charset)
{
    // The character set should have a non-empty name
    BOOST_ASSERT(!charset.name.empty());

    // For security, if the character set has non-ascii characters in it name, reject it.
    format_context ctx(format_options{ascii_charset, true});
    ctx.append_raw("SET NAMES ").append_value(charset.name);
    return std::move(ctx).get();
}

class set_character_set_algo : public sansio_algorithm, asio::coroutine
{
    diagnostics* diag_;
    character_set charset_;
    std::uint8_t seqnum_{0};

    next_action compose_request()
    {
        auto q = compose_set_names(charset_);
        if (q.has_error())
            return q.error();
        return write(query_command{q.value()}, seqnum_);
    }

public:
    set_character_set_algo(connection_state_data& st, set_character_set_algo_params params) noexcept
        : sansio_algorithm(st), diag_(params.diag), charset_(params.charset)
    {
    }

    next_action resume(error_code ec)
    {
        if (ec)
            return ec;

        // SET NAMES never returns rows. Using execute requires us to allocate
        // a results object, which we can avoid by simply sending the query and reading the OK response.
        BOOST_ASIO_CORO_REENTER(*this)
        {
            // Setup
            diag_->clear();

            // Send the execution request
            BOOST_ASIO_CORO_YIELD return compose_request();

            // Read the response
            BOOST_ASIO_CORO_YIELD return read(seqnum_);

            // Verify it's what we expected
            ec = st_->deserialize_ok(*diag_);
            if (ec)
                return ec;

            // If we were successful, update the character set
            st_->current_charset = charset_;
        }

        return next_action();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
