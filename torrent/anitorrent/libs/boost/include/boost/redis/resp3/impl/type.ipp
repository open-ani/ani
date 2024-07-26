/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#include <boost/redis/resp3/type.hpp>
#include <boost/assert.hpp>

namespace boost::redis::resp3 {

auto to_string(type t) noexcept -> char const*
{
   switch (t) {
      case type::array: return "array";
      case type::push: return "push";
      case type::set: return "set";
      case type::map: return "map";
      case type::attribute: return "attribute";
      case type::simple_string: return "simple_string";
      case type::simple_error: return "simple_error";
      case type::number: return "number";
      case type::doublean: return "doublean";
      case type::boolean: return "boolean";
      case type::big_number: return "big_number";
      case type::null: return "null";
      case type::blob_error: return "blob_error";
      case type::verbatim_string: return "verbatim_string";
      case type::blob_string: return "blob_string";
      case type::streamed_string: return "streamed_string";
      case type::streamed_string_part: return "streamed_string_part";
      default: return "invalid";
   }
}

auto operator<<(std::ostream& os, type t) -> std::ostream&
{
   os << to_string(t);
   return os;
}

} // boost::redis::resp3
