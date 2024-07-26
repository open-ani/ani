//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_ALGO_PARAMS_HPP
#define BOOST_MYSQL_DETAIL_ALGO_PARAMS_HPP

#include <boost/mysql/character_set.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/handshake_params.hpp>
#include <boost/mysql/rows_view.hpp>
#include <boost/mysql/statement.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/any_execution_request.hpp>
#include <boost/mysql/detail/execution_processor/execution_processor.hpp>
#include <boost/mysql/detail/execution_processor/execution_state_impl.hpp>

#include <cstddef>
#include <cstdint>

namespace boost {
namespace mysql {

namespace detail {

struct connect_algo_params
{
    diagnostics* diag;
    handshake_params hparams;
    bool secure_channel;  // Are we using UNIX sockets or any other secure channel?

    using result_type = void;
};

struct handshake_algo_params
{
    diagnostics* diag;
    handshake_params hparams;
    bool secure_channel;  // Are we using UNIX sockets or any other secure channel?

    using result_type = void;
};

struct execute_algo_params
{
    diagnostics* diag;
    any_execution_request req;
    execution_processor* proc;

    using result_type = void;
};

struct start_execution_algo_params
{
    diagnostics* diag;
    any_execution_request req;
    execution_processor* proc;

    using result_type = void;
};

struct read_resultset_head_algo_params
{
    diagnostics* diag;
    execution_processor* proc;

    using result_type = void;
};

struct read_some_rows_algo_params
{
    diagnostics* diag;
    execution_processor* proc;
    output_ref output;

    using result_type = std::size_t;
};

struct read_some_rows_dynamic_algo_params
{
    diagnostics* diag;
    execution_state_impl* exec_st;

    using result_type = rows_view;
};

struct prepare_statement_algo_params
{
    diagnostics* diag;
    string_view stmt_sql;

    using result_type = statement;
};

struct close_statement_algo_params
{
    diagnostics* diag;
    std::uint32_t stmt_id;

    using result_type = void;
};

struct ping_algo_params
{
    diagnostics* diag;

    using result_type = void;
};

struct reset_connection_algo_params
{
    diagnostics* diag;
    character_set charset;  // set a non-empty character set to pipeline a SET NAMES with the reset request

    using result_type = void;
};

struct set_character_set_algo_params
{
    diagnostics* diag;
    character_set charset;

    using result_type = void;
};

struct quit_connection_algo_params
{
    diagnostics* diag;

    using result_type = void;
};

struct close_connection_algo_params
{
    diagnostics* diag;

    using result_type = void;
};

template <class AlgoParams>
constexpr bool has_void_result() noexcept
{
    return std::is_same<typename AlgoParams::result_type, void>::value;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
