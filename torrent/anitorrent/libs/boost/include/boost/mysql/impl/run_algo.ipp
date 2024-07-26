//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_RUN_ALGO_IPP
#define BOOST_MYSQL_IMPL_RUN_ALGO_IPP

#pragma once

#include <boost/mysql/detail/algo_params.hpp>
#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/run_algo.hpp>

#include <boost/mysql/impl/internal/network_algorithms/run_algo_impl.hpp>
#include <boost/mysql/impl/internal/sansio/connection_state.hpp>

#include <boost/asio/any_completion_handler.hpp>

namespace boost {
namespace mysql {
namespace detail {

template <class AlgoParams>
class generic_algo_handler
{
public:
    static_assert(!has_void_result<AlgoParams>(), "AlgoParams::result_type should be non-void");

    using result_t = typename AlgoParams::result_type;

    using final_handler_t = asio::any_completion_handler<void(error_code, result_t)>;
    generic_algo_handler(final_handler_t&& h, connection_state& st) : final_handler_(std::move(h)), st_(&st)
    {
    }

    using allocator_type = typename final_handler_t::allocator_type;
    using cancellation_slot_type = typename final_handler_t::cancellation_slot_type;

    allocator_type get_allocator() const noexcept { return final_handler_.get_allocator(); }
    cancellation_slot_type get_cancellation_slot() const noexcept
    {
        return final_handler_.get_cancellation_slot();
    }

    const final_handler_t& handler() const noexcept { return final_handler_; }

    void operator()(error_code ec)
    {
        std::move(final_handler_)(ec, ec ? result_t{} : st_->result<AlgoParams>());
    }

private:
    final_handler_t final_handler_;
    connection_state* st_;
};

template <class OpParam>
asio::any_completion_handler<void(error_code)> make_handler(
    completion_handler_t<OpParam>&& final_handler,
    connection_state& st,
    typename std::enable_if<!has_void_result<OpParam>()>::type* = nullptr
)
{
    return generic_algo_handler<OpParam>(std::move(final_handler), st);
}

template <class OpParam>
asio::any_completion_handler<void(error_code)> make_handler(
    asio::any_completion_handler<void(error_code)>&& final_handler,
    connection_state&,
    typename std::enable_if<has_void_result<OpParam>()>::type* = nullptr
)
{
    return std::move(final_handler);
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

namespace boost {
namespace asio {

template <class OpParam, class Candidate>
struct associated_executor<mysql::detail::generic_algo_handler<OpParam>, Candidate>
{
    using type = any_completion_executor;

    static type get(
        const mysql::detail::generic_algo_handler<OpParam>& handler,
        const Candidate& candidate = Candidate()
    ) noexcept
    {
        return asio::get_associated_executor(handler.handler(), candidate);
    }
};

template <class OpParam, class Candidate>
struct associated_immediate_executor<mysql::detail::generic_algo_handler<OpParam>, Candidate>
{
    using type = any_completion_executor;

    static type get(
        const mysql::detail::generic_algo_handler<OpParam>& handler,
        const Candidate& candidate = Candidate()
    ) BOOST_ASIO_NOEXCEPT
    {
        return asio::get_associated_immediate_executor(handler.handler(), candidate);
    }
};

}  // namespace asio
}  // namespace boost

template <class AlgoParams>
typename AlgoParams::result_type boost::mysql::detail::run_algo(
    any_stream& stream,
    connection_state& st,
    AlgoParams params,
    error_code& ec
)
{
    auto algo = st.setup(params);
    run_algo_impl(stream, algo, ec);
    return st.result<AlgoParams>();
}

template <class AlgoParams>
void boost::mysql::detail::async_run_algo(
    any_stream& stream,
    connection_state& st,
    AlgoParams params,
    completion_handler_t<AlgoParams> final_handler
)
{
    auto handler = make_handler<AlgoParams>(std::move(final_handler), st);
    auto algo = st.setup(params);
    async_run_algo_impl(stream, algo, std::move(handler));
}

#ifdef BOOST_MYSQL_SEPARATE_COMPILATION

#define BOOST_MYSQL_INSTANTIATE_ALGO(op_params_type)                                       \
    template op_params_type::result_type                                                   \
    run_algo<op_params_type>(any_stream&, connection_state&, op_params_type, error_code&); \
    template void async_run_algo<                                                          \
        op_params_type>(any_stream&, connection_state&, op_params_type, completion_handler_t<op_params_type>);

namespace boost {
namespace mysql {
namespace detail {

BOOST_MYSQL_INSTANTIATE_ALGO(connect_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(handshake_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(execute_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(start_execution_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(read_resultset_head_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(read_some_rows_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(read_some_rows_dynamic_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(prepare_statement_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(close_statement_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(set_character_set_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(ping_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(reset_connection_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(quit_connection_algo_params)
BOOST_MYSQL_INSTANTIATE_ALGO(close_connection_algo_params)

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif

#endif
