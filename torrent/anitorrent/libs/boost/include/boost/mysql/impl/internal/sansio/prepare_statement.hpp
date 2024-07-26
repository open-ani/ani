//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_PREPARE_STATEMENT_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_PREPARE_STATEMENT_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/statement.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/algo_params.hpp>

#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>

#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

class prepare_statement_algo : public sansio_algorithm, asio::coroutine
{
    diagnostics* diag_;
    string_view stmt_sql_;
    std::uint8_t sequence_number_{0};
    unsigned remaining_meta_{0};
    statement res_;

    error_code process_response()
    {
        prepare_stmt_response response{};
        auto err = deserialize_prepare_stmt_response(st_->reader.message(), st_->flavor, response, *diag_);
        if (err)
            return err;
        res_ = access::construct<statement>(response.id, response.num_params);
        remaining_meta_ = response.num_columns + response.num_params;
        return error_code();
    }

public:
    prepare_statement_algo(connection_state_data& st, prepare_statement_algo_params params) noexcept
        : sansio_algorithm(st), diag_(params.diag), stmt_sql_(params.stmt_sql)
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

            // Send request
            BOOST_ASIO_CORO_YIELD return write(prepare_stmt_command{stmt_sql_}, sequence_number_);

            // Read response
            BOOST_ASIO_CORO_YIELD return read(sequence_number_);

            // Process response
            ec = process_response();
            if (ec)
                return ec;

            // Server sends now one packet per parameter and field.
            // We ignore these for now.
            for (; remaining_meta_ > 0u; --remaining_meta_)
                BOOST_ASIO_CORO_YIELD return read(sequence_number_);
        }

        return next_action();
    }

    statement result() const noexcept { return res_; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif /* INCLUDE_BOOST_MYSQL_DETAIL_NETWORK_ALGORITHMS_PREPARE_STATEMENT_HPP_ */
