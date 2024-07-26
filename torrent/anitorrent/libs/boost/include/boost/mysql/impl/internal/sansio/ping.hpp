//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_PING_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_PING_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>

#include <boost/mysql/detail/algo_params.hpp>

#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>

#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

class ping_algo : public sansio_algorithm, asio::coroutine
{
    diagnostics* diag_;
    std::uint8_t seqnum_{0};

public:
    ping_algo(connection_state_data& st, ping_algo_params params) noexcept
        : sansio_algorithm(st), diag_(params.diag)
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
            BOOST_ASIO_CORO_YIELD return write(ping_command(), seqnum_);

            // Read the response
            BOOST_ASIO_CORO_YIELD return read(seqnum_);

            // Process the OK packet
            return st_->deserialize_ok(*diag_);
        }

        return next_action();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
