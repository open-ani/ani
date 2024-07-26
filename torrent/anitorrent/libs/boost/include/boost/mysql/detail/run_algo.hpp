//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_RUN_ALGO_HPP
#define BOOST_MYSQL_DETAIL_RUN_ALGO_HPP

#include <boost/mysql/error_code.hpp>

#include <boost/mysql/detail/algo_params.hpp>

#include <boost/asio/any_completion_handler.hpp>
#include <boost/asio/async_result.hpp>

#include <type_traits>

namespace boost {
namespace mysql {
namespace detail {

class any_stream;
class connection_state;

template <class AlgoParams, bool is_void>
struct completion_signature_impl;

template <class AlgoParams>
struct completion_signature_impl<AlgoParams, true>
{
    // Using typedef to workaround a msvc 14.1 bug
    typedef void(type)(error_code);
};

template <class AlgoParams>
struct completion_signature_impl<AlgoParams, false>
{
    // Using typedef to workaround a msvc 14.1 bug
    typedef void(type)(error_code, typename AlgoParams::result_type);
};

template <class AlgoParams>
using completion_signature_t = typename completion_signature_impl<AlgoParams, has_void_result<AlgoParams>()>::
    type;

template <class AlgoParams>
using completion_handler_t = asio::any_completion_handler<completion_signature_t<AlgoParams>>;

template <class AlgoParams>
typename AlgoParams::result_type run_algo(
    any_stream& stream,
    connection_state& st,
    AlgoParams params,
    error_code& ec
);

template <class AlgoParams>
void async_run_algo(
    any_stream& stream,
    connection_state& st,
    AlgoParams params,
    completion_handler_t<AlgoParams> handler
);

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/run_algo.ipp>
#endif

#endif
