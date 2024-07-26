/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_ADAPTER_ADAPT_HPP
#define BOOST_REDIS_ADAPTER_ADAPT_HPP

#include <boost/redis/resp3/node.hpp>
#include <boost/redis/response.hpp>
#include <boost/redis/adapter/detail/result_traits.hpp>
#include <boost/redis/adapter/detail/response_traits.hpp>
#include <boost/mp11.hpp>
#include <boost/system.hpp>

#include <tuple>
#include <limits>
#include <string_view>
#include <variant>

namespace boost::redis::adapter
{

/** @brief Adapts a type to be used as a response.
 *
 *  The type T must be either
 *
 *  1. a response<T1, T2, T3, ...> or
 *  2. std::vector<node<String>>
 *
 *  The types T1, T2, etc can be any STL container, any integer type
 *  and `std::string`.
 *
 *  @param t Tuple containing the responses.
 */
template<class T>
auto boost_redis_adapt(T& t) noexcept
{
   return detail::response_traits<T>::adapt(t);
}

/** @brief Adapts user data to read operations.
 *  @ingroup low-level-api
 *
 *  STL containers, \c resp3::response and built-in types are supported and
 *  can be used in conjunction with \c std::optional<T>.
 *
 *  Example usage:
 *
 *  @code
 *  std::unordered_map<std::string, std::string> cont;
 *  co_await async_read(socket, buffer, adapt(cont));
 *  @endcode
 * 
 *  For a transaction
 *
 *  @code
 *  sr.push(command::multi);
 *  sr.push(command::ping, ...);
 *  sr.push(command::incr, ...);
 *  sr.push_range(command::rpush, ...);
 *  sr.push(command::lrange, ...);
 *  sr.push(command::incr, ...);
 *  sr.push(command::exec);
 *
 *  co_await async_write(socket, buffer(request));
 *
 *  // Reads the response to a transaction
 *  resp3::response<std::string, int, int, std::vector<std::string>, int> execs;
 *  co_await resp3::async_read(socket, dynamic_buffer(buffer), adapt(execs));
 *  @endcode
 */
template<class T>
auto adapt2(T& t = redis::ignore) noexcept
   { return detail::result_traits<T>::adapt(t); }

} // boost::redis::adapter

#endif // BOOST_REDIS_ADAPTER_ADAPT_HPP
