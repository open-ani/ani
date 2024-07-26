/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_RESP3_NODE_HPP
#define BOOST_REDIS_RESP3_NODE_HPP

#include <boost/redis/resp3/type.hpp>

namespace boost::redis::resp3 {

/** \brief A node in the response tree.
 *  \ingroup high-level-api
 *
 *  RESP3 can contain recursive data structures: A map of sets of
 *  vector of etc. As it is parsed each element is passed to user
 *  callbacks (push parser). The signature of this
 *  callback is `f(resp3::node<std::string_view)`. This class is called a node
 *  because it can be seen as the element of the response tree. It
 *  is a template so that users can use it with owing strings e.g.
 *  `std::string` or `boost::static_string` etc.
 *
 *  @tparam String A `std::string`-like type.
 */
template <class String>
struct basic_node {
   /// The RESP3 type of the data in this node.
   type data_type = type::invalid;

   /// The number of elements of an aggregate.
   std::size_t aggregate_size{};

   /// The depth of this node in the response tree.
   std::size_t depth{};

   /// The actual data. For aggregate types this is usually empty.
   String value{};
};

/** @brief Compares a node for equality.
 *  @relates basic_node
 *
 *  @param a Left hand side node object.
 *  @param b Right hand side node object.
 */
template <class String>
auto operator==(basic_node<String> const& a, basic_node<String> const& b)
{
   return a.aggregate_size == b.aggregate_size
       && a.depth == b.depth
       && a.data_type == b.data_type
       && a.value == b.value;
};

/** @brief A node in the response tree.
 *  @ingroup high-level-api
 */
using node = basic_node<std::string>;

} // boost::redis::resp3

#endif // BOOST_REDIS_RESP3_NODE_HPP
