/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#include <boost/redis/response.hpp>
#include <boost/redis/error.hpp>
#include <boost/assert.hpp>

namespace boost::redis
{

void consume_one(generic_response& r, system::error_code& ec)
{
   if (r.has_error())
      return; // Nothing to consume.

   if (std::empty(r.value()))
      return; // Nothing to consume.

   auto const depth = r.value().front().depth;

   // To simplify we will refuse to consume any data-type that is not
   // a root node. I think there is no use for that and it is complex
   // since it requires updating parent nodes.
   if (depth != 0) {
      ec = error::incompatible_node_depth;
      return;
   }

   auto f = [depth](auto const& e)
      { return e.depth == depth; };

   auto match = std::find_if(std::next(std::cbegin(r.value())), std::cend(r.value()), f);

   r.value().erase(std::cbegin(r.value()), match);
}

void consume_one(generic_response& r)
{
   system::error_code ec;
   consume_one(r, ec);
   if (ec)
      throw system::system_error(ec);
}

} // boost::redis::resp3
