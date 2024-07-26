//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_CLOSE_STATEMENT_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_CLOSE_STATEMENT_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/statement.hpp>

#include <boost/mysql/detail/algo_params.hpp>

#include <boost/mysql/impl/internal/protocol/protocol.hpp>
#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/next_action.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>

namespace boost {
namespace mysql {
namespace detail {

class close_statement_algo : public sansio_algorithm, asio::coroutine
{
    diagnostics* diag_;
    std::uint32_t stmt_id_;
    std::uint8_t close_seqnum_{0};
    std::uint8_t ping_seqnum_{0};

public:
    close_statement_algo(connection_state_data& st, close_statement_algo_params params) noexcept
        : sansio_algorithm(st), diag_(params.diag), stmt_id_(params.stmt_id)
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

            // Compose the requests. We pipeline a ping with the close statement
            // to force the server send a response. Otherwise, the client ends up waiting
            // for the next TCP ACK, which takes some milliseconds to be sent
            // (see https://github.com/boostorg/mysql/issues/181)
            st_->writer.prepare_pipelined_write(
                close_stmt_command{stmt_id_},
                close_seqnum_,
                ping_command{},
                ping_seqnum_
            );
            BOOST_ASIO_CORO_YIELD return next_action::write({});

            // Read ping response
            BOOST_ASIO_CORO_YIELD return read(ping_seqnum_);

            // Process the OK packet
            return st_->deserialize_ok(*diag_);
        }

        return next_action();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif /* INCLUDE_BOOST_MYSQL_DETAIL_NETWORK_ALGORITHMS_CLOSE_STATEMENT_HPP_ */
