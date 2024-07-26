/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_RESP3_TYPE_HPP
#define BOOST_REDIS_RESP3_TYPE_HPP

#include <boost/assert.hpp>
#include <ostream>
#include <vector>
#include <string>

namespace boost::redis::resp3 {

/** \brief RESP3 data types.
    \ingroup high-level-api
  
    The RESP3 specification can be found at https://github.com/redis/redis-specifications/blob/master/protocol/RESP3.md.
 */
enum class type
{ /// Aggregate
  array,
  /// Aaggregate
  push,
  /// Aggregate
  set,
  /// Aggregate
  map,
  /// Aggregate
  attribute,
  /// Simple
  simple_string,
  /// Simple
  simple_error,
  /// Simple
  number,
  /// Simple
  doublean,
  /// Simple
  boolean,
  /// Simple
  big_number,
  /// Simple
  null,
  /// Simple
  blob_error,
  /// Simple
  verbatim_string,
  /// Simple
  blob_string,
  /// Simple
  streamed_string,
  /// Simple
  streamed_string_part,
  /// Invalid
  invalid
};

/** \brief Converts the data type to a string.
 *  \ingroup high-level-api
 *  \param t RESP3 type.
 */
auto to_string(type t) noexcept -> char const*;

/** \brief Writes the type to the output stream.
 *  \ingroup high-level-api
 *  \param os Output stream.
 *  \param t RESP3 type.
 */
auto operator<<(std::ostream& os, type t) -> std::ostream&;

/* Checks whether the data type is an aggregate.
 */
constexpr auto is_aggregate(type t) noexcept -> bool
{
   switch (t) {
      case type::array:
      case type::push:
      case type::set:
      case type::map:
      case type::attribute: return true;
      default: return false;
   }
}

// For map and attribute data types this function returns 2.  All
// other types have value 1.
constexpr auto element_multiplicity(type t) noexcept -> std::size_t
{
   switch (t) {
      case type::map:
      case type::attribute: return 2ULL;
      default: return 1ULL;
   }
}

// Returns the wire code of a given type.
constexpr auto to_code(type t) noexcept -> char
{
   switch (t) {
      case type::blob_error:           return '!';
      case type::verbatim_string:      return '=';
      case type::blob_string:          return '$';
      case type::streamed_string_part: return ';';
      case type::simple_error:         return '-';
      case type::number:               return ':';
      case type::doublean:             return ',';
      case type::boolean:              return '#';
      case type::big_number:           return '(';
      case type::simple_string:        return '+';
      case type::null:                 return '_';
      case type::push:                 return '>';
      case type::set:                  return '~';
      case type::array:                return '*';
      case type::attribute:            return '|';
      case type::map:                  return '%';

      default: BOOST_ASSERT(false); return ' ';
   }
}

// Converts a wire-format RESP3 type (char) to a resp3 type.
constexpr auto to_type(char c) noexcept -> type
{
   switch (c) {
      case '!': return type::blob_error;
      case '=': return type::verbatim_string;
      case '$': return type::blob_string;
      case ';': return type::streamed_string_part;
      case '-': return type::simple_error;
      case ':': return type::number;
      case ',': return type::doublean;
      case '#': return type::boolean;
      case '(': return type::big_number;
      case '+': return type::simple_string;
      case '_': return type::null;
      case '>': return type::push;
      case '~': return type::set;
      case '*': return type::array;
      case '|': return type::attribute;
      case '%': return type::map;
      default: return type::invalid;
   }
}

} // boost::redis::resp3

#endif // BOOST_REDIS_RESP3_TYPE_HPP
