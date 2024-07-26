//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_CONNECTION_STATE_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_CONNECTION_STATE_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/handshake_params.hpp>
#include <boost/mysql/statement.hpp>

#include <boost/mysql/detail/algo_params.hpp>

#include <boost/mysql/impl/internal/sansio/algo_runner.hpp>
#include <boost/mysql/impl/internal/sansio/close_connection.hpp>
#include <boost/mysql/impl/internal/sansio/close_statement.hpp>
#include <boost/mysql/impl/internal/sansio/connect.hpp>
#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/execute.hpp>
#include <boost/mysql/impl/internal/sansio/handshake.hpp>
#include <boost/mysql/impl/internal/sansio/ping.hpp>
#include <boost/mysql/impl/internal/sansio/prepare_statement.hpp>
#include <boost/mysql/impl/internal/sansio/quit_connection.hpp>
#include <boost/mysql/impl/internal/sansio/read_resultset_head.hpp>
#include <boost/mysql/impl/internal/sansio/read_some_rows.hpp>
#include <boost/mysql/impl/internal/sansio/read_some_rows_dynamic.hpp>
#include <boost/mysql/impl/internal/sansio/reset_connection.hpp>
#include <boost/mysql/impl/internal/sansio/set_character_set.hpp>
#include <boost/mysql/impl/internal/sansio/start_execution.hpp>

#include <boost/asio/coroutine.hpp>
#include <boost/variant2/variant.hpp>

#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

// clang-format off
template <class AlgoParams> struct get_algo;
template <> struct get_algo<connect_algo_params> { using type = connect_algo; };
template <> struct get_algo<handshake_algo_params> { using type = handshake_algo; };
template <> struct get_algo<execute_algo_params> { using type = execute_algo; };
template <> struct get_algo<start_execution_algo_params> { using type = start_execution_algo; };
template <> struct get_algo<read_resultset_head_algo_params> { using type = read_resultset_head_algo; };
template <> struct get_algo<read_some_rows_algo_params> { using type = read_some_rows_algo; };
template <> struct get_algo<read_some_rows_dynamic_algo_params> { using type = read_some_rows_dynamic_algo; };
template <> struct get_algo<prepare_statement_algo_params> { using type = prepare_statement_algo; };
template <> struct get_algo<close_statement_algo_params> { using type = close_statement_algo; };
template <> struct get_algo<set_character_set_algo_params> { using type = set_character_set_algo; };
template <> struct get_algo<ping_algo_params> { using type = ping_algo; };
template <> struct get_algo<reset_connection_algo_params> { using type = reset_connection_algo; };
template <> struct get_algo<quit_connection_algo_params> { using type = quit_connection_algo; };
template <> struct get_algo<close_connection_algo_params> { using type = close_connection_algo; };
template <class AlgoParams> using get_algo_t = typename get_algo<AlgoParams>::type;
// clang-format on

class connection_state
{
    using any_algo = variant2::variant<
        connect_algo,
        handshake_algo,
        execute_algo,
        start_execution_algo,
        read_resultset_head_algo,
        read_some_rows_algo,
        read_some_rows_dynamic_algo,
        prepare_statement_algo,
        close_statement_algo,
        set_character_set_algo,
        ping_algo,
        reset_connection_algo,
        quit_connection_algo,
        close_connection_algo>;

    connection_state_data st_data_;
    any_algo algo_;

public:
    // We initialize the algo state with a dummy value. This will be overwritten
    // by setup() before the first algorithm starts running. Doing this avoids
    // the need for a special null algo
    connection_state(std::size_t read_buffer_size, bool transport_supports_ssl)
        : st_data_(read_buffer_size, transport_supports_ssl),
          algo_(ping_algo(st_data_, {&st_data_.shared_diag}))
    {
    }

    const connection_state_data& data() const noexcept { return st_data_; }
    connection_state_data& data() noexcept { return st_data_; }

    template <class AlgoParams>
    any_algo_ref setup(AlgoParams params)
    {
        return algo_.emplace<get_algo_t<AlgoParams>>(st_data_, params);
    }

    template <typename AlgoParams>
    void result(typename std::enable_if<has_void_result<AlgoParams>()>::type* = nullptr) const noexcept
    {
    }

    template <typename AlgoParams>
    typename AlgoParams::result_type result(
        typename std::enable_if<!has_void_result<AlgoParams>()>::type* = nullptr
    ) const
    {
        return variant2::get<get_algo_t<AlgoParams>>(algo_).result();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
