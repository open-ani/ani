//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_EXECUTE_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_EXECUTE_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>

#include <boost/mysql/detail/algo_params.hpp>
#include <boost/mysql/detail/any_execution_request.hpp>
#include <boost/mysql/detail/execution_processor/execution_processor.hpp>

#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/read_resultset_head.hpp>
#include <boost/mysql/impl/internal/sansio/read_some_rows.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>
#include <boost/mysql/impl/internal/sansio/start_execution.hpp>

#include <boost/asio/coroutine.hpp>

#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

class execute_algo : public sansio_algorithm, asio::coroutine
{
    start_execution_algo start_execution_st_;
    read_resultset_head_algo read_head_st_;
    read_some_rows_algo read_some_rows_st_;

    diagnostics& diag() noexcept { return *read_head_st_.params().diag; }
    execution_processor& processor() noexcept { return *read_head_st_.params().proc; }

public:
    execute_algo(connection_state_data& st, execute_algo_params params) noexcept
        : sansio_algorithm(st),
          start_execution_st_(st, start_execution_algo_params{params.diag, params.req, params.proc}),
          read_head_st_(st, read_resultset_head_algo_params{params.diag, params.proc}),
          read_some_rows_st_(st, read_some_rows_algo_params{params.diag, params.proc, output_ref()})
    {
    }

    next_action resume(error_code ec)
    {
        next_action act;

        BOOST_ASIO_CORO_REENTER(*this)
        {
            // Send request and read the first response
            while (!(act = start_execution_st_.resume(ec)).is_done())
                BOOST_ASIO_CORO_YIELD return act;
            if (act.error())
                return act;

            // Read anything else
            while (!processor().is_complete())
            {
                if (processor().is_reading_head())
                {
                    read_head_st_ = read_resultset_head_algo(*st_, read_head_st_.params());
                    while (!(act = read_head_st_.resume(ec)).is_done())
                        BOOST_ASIO_CORO_YIELD return act;
                    if (act.error())
                        return act;
                }
                else if (processor().is_reading_rows())
                {
                    read_some_rows_st_ = read_some_rows_algo(*st_, read_some_rows_st_.params());
                    while (!(act = read_some_rows_st_.resume(ec)).is_done())
                        BOOST_ASIO_CORO_YIELD return act;
                    if (act.error())
                        return act;
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
