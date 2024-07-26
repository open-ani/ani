//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_CLOSE_CONNECTION_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_CLOSE_CONNECTION_HPP

#include <boost/mysql/detail/algo_params.hpp>

#include <boost/mysql/impl/internal/sansio/next_action.hpp>
#include <boost/mysql/impl/internal/sansio/quit_connection.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>

namespace boost {
namespace mysql {
namespace detail {

class close_connection_algo : public sansio_algorithm, asio::coroutine
{
    quit_connection_algo quit_;
    error_code stored_ec_;

public:
    close_connection_algo(connection_state_data& st, close_connection_algo_params params) noexcept
        : sansio_algorithm(st), quit_(st, {params.diag})
    {
    }

    next_action resume(error_code ec)
    {
        next_action act;

        BOOST_ASIO_CORO_REENTER(*this)
        {
            // Clear diagnostics
            quit_.diag().clear();

            // If we're not connected, we're done
            if (!st_->is_connected)
                return next_action();

            // Attempt quit
            while (!(act = quit_.resume(ec)).is_done())
                BOOST_ASIO_CORO_YIELD return act;
            stored_ec_ = act.error();

            // Close the transport
            BOOST_ASIO_CORO_YIELD return next_action::close();

            // If quit resulted in an error, keep that error.
            // Otherwise, return any error derived from close
            return stored_ec_ ? stored_ec_ : ec;
        }

        return next_action();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
