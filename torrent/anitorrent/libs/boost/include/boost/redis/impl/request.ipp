/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#include <boost/redis/request.hpp>

#include <string_view>

namespace boost::redis::detail {

auto has_response(std::string_view cmd) -> bool
{
   if (cmd == "SUBSCRIBE") return true;
   if (cmd == "PSUBSCRIBE") return true;
   if (cmd == "UNSUBSCRIBE") return true;
   return false;
}

} // boost:redis::detail
