
/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_IGNORE_HPP
#define BOOST_REDIS_IGNORE_HPP

#include <boost/system/result.hpp>

#include <tuple>
#include <type_traits>

namespace boost::redis
{

/** @brief Type used to ignore responses.
 *  @ingroup high-level-api
 *
 *  For example
 *
 *  @code
 *  response<ignore_t, std::string, ignore_t> resp;
 *  @endcode
 *
 *  will ignore the first and third responses. RESP3 errors won't be
 *  ignore but will cause `async_exec` to complete with an error.
 */
using ignore_t = std::decay_t<decltype(std::ignore)>;

/** @brief Global ignore object.
 *  @ingroup high-level-api
 *
 *  Can be used to ignore responses to a request
 *
 *  @code
 *  conn->async_exec(req, ignore, ...);
 *  @endcode
 *
 *  RESP3 errors won't be ignore but will cause `async_exec` to
 *  complete with an error.
 */
extern ignore_t ignore;

} // boost::redis

#endif // BOOST_REDIS_IGNORE_HPP
