//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_START_EXECUTION_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_START_EXECUTION_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>

#include <boost/mysql/detail/algo_params.hpp>
#include <boost/mysql/detail/any_execution_request.hpp>
#include <boost/mysql/detail/execution_processor/execution_processor.hpp>

#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/read_resultset_head.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>

namespace boost {
namespace mysql {
namespace detail {

inline error_code check_client_errors(const any_execution_request& req)
{
    if (req.is_query)
        return error_code();
    return req.data.stmt.stmt.num_params() == req.data.stmt.params.size() ? error_code()
                                                                          : client_errc::wrong_num_params;
}

inline resultset_encoding get_encoding(const any_execution_request& req)
{
    return req.is_query ? resultset_encoding::text : resultset_encoding::binary;
}

class start_execution_algo : public sansio_algorithm, asio::coroutine
{
    read_resultset_head_algo read_head_st_;
    any_execution_request req_;

    std::uint8_t& seqnum() noexcept { return processor().sequence_number(); }
    execution_processor& processor() noexcept { return *read_head_st_.params().proc; }
    diagnostics& diag() noexcept { return *read_head_st_.params().diag; }

public:
    start_execution_algo(connection_state_data& st, start_execution_algo_params params) noexcept
        : sansio_algorithm(st),
          read_head_st_(st, read_resultset_head_algo_params{params.diag, params.proc}),
          req_(params.req)
    {
    }

    next_action resume(error_code ec)
    {
        next_action act;

        BOOST_ASIO_CORO_REENTER(*this)
        {
            // Clear diagnostics
            diag().clear();

            // Check for errors
            ec = check_client_errors(req_);
            if (ec)
                return ec;

            // Reset the processor
            processor().reset(get_encoding(req_), st_->meta_mode);

            // Send the execution request
            if (req_.is_query)
            {
                BOOST_ASIO_CORO_YIELD return write(query_command{req_.data.query}, seqnum());
            }
            else
            {
                BOOST_ASIO_CORO_YIELD return write(
                    execute_stmt_command{req_.data.stmt.stmt.id(), req_.data.stmt.params},
                    seqnum()
                );
            }

            if (ec)
                return ec;

            // Read the first resultset's head and return its result
            while (!(act = read_head_st_.resume(ec)).is_done())
                BOOST_ASIO_CORO_YIELD return act;
            return act;
        }

        return next_action();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif /* INCLUDE_MYSQL_IMPL_NETWORK_ALGORITHMS_READ_RESULTSET_HEAD_HPP_ */
