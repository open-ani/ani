//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_SOCKET_STREAM_HPP
#define BOOST_MYSQL_DETAIL_SOCKET_STREAM_HPP

#include <boost/asio/basic_socket.hpp>
#include <boost/asio/basic_stream_socket.hpp>

#include <type_traits>

namespace boost {
namespace mysql {
namespace detail {

template <class T>
struct is_socket : std::false_type
{
};

// typename basic_stream_socket::lowest_layer_type is basic_socket, so we accept basic_socket and
// basic_stream_socket here
template <class Protocol, class Executor>
struct is_socket<asio::basic_socket<Protocol, Executor>> : std::true_type
{
};

template <class Protocol, class Executor>
struct is_socket<asio::basic_stream_socket<Protocol, Executor>> : std::true_type
{
};

template <class T, class = void>
struct is_socket_stream : std::false_type
{
};

template <class T>
struct is_socket_stream<T, typename std::enable_if<is_socket<typename T::lowest_layer_type>::value>::type>
    : std::true_type
{
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
