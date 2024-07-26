/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_RESPONSE_HPP
#define BOOST_REDIS_RESPONSE_HPP

#include <boost/redis/resp3/node.hpp>
#include <boost/redis/adapter/result.hpp>
#include <boost/system.hpp>

#include <vector>
#include <string>
#include <tuple>

namespace boost::redis
{

/** @brief Response with compile-time size.
 *  @ingroup high-level-api
 */
template <class... Ts>
using response = std::tuple<adapter::result<Ts>...>;

/** @brief A generic response to a request
 *  @ingroup high-level-api
 *
 *  This response type can store any type of RESP3 data structure.  It
 *  contains the
 *  [pre-order](https://en.wikipedia.org/wiki/Tree_traversal#Pre-order,_NLR)
 *  view of the response tree.
 */
using generic_response = adapter::result<std::vector<resp3::node>>;

/** @brief Consume on response from a generic response
 *
 *  This function rotates the elements so that the start of the next
 *  response becomes the new front element. For example the output of
 *  the following code
 *
 * @code
 * request req;
 * req.push("PING", "one");
 * req.push("PING", "two");
 * req.push("PING", "three");
 *
 * generic_response resp;
 * co_await conn->async_exec(req, resp, asio::deferred);
 *
 * std::cout << "PING: " << resp.value().front().value << std::endl;
 * consume_one(resp);
 * std::cout << "PING: " << resp.value().front().value << std::endl;
 * consume_one(resp);
 * std::cout << "PING: " << resp.value().front().value << std::endl;
 * @endcode
 *
 * is
 *
 * @code
 * PING: one
 * PING: two
 * PING: three
 * @endcode
 *
 * Given that this function rotates elements, it won't be very
 * efficient for responses with a large number of elements. It was
 * introduced mainly to deal with buffers server pushes as shown in
 * the cpp20_subscriber.cpp example. In the future queue-like
 * responses might be introduced to consume in O(1) operations. 
 */
void consume_one(generic_response& r, system::error_code& ec);

/// Throwing overload of `consume_one`.
void consume_one(generic_response& r);

} // boost::redis

#endif // BOOST_REDIS_RESPONSE_HPP
