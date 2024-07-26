/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_ERROR_HPP
#define BOOST_REDIS_ERROR_HPP

#include <boost/system/error_code.hpp>

namespace boost::redis {

/** \brief Generic errors.
 *  \ingroup high-level-api
 */
enum class error
{
   /// Invalid RESP3 type.
   invalid_data_type = 1,

   /// Can't parse the string as a number.
   not_a_number,

   /// The maximum depth of a nested response was exceeded.
   exceeeds_max_nested_depth,

   /// Got non boolean value.
   unexpected_bool_value,

   /// Expected field value is empty.
   empty_field,

   /// Expects a simple RESP3 type but got an aggregate.
   expects_resp3_simple_type,

   /// Expects aggregate.
   expects_resp3_aggregate,

   /// Expects a map but got other aggregate.
   expects_resp3_map,

   /// Expects a set aggregate but got something else.
   expects_resp3_set,

   /// Nested response not supported.
   nested_aggregate_not_supported,

   /// Got RESP3 simple error.
   resp3_simple_error,

   /// Got RESP3 blob_error.
   resp3_blob_error,

   /// Aggregate container has incompatible size.
   incompatible_size,

   /// Not a double
   not_a_double,

   /// Got RESP3 null.
   resp3_null,

   /// There is no stablished connection.
   not_connected,

   /// Resolve timeout
   resolve_timeout,

   /// Connect timeout
   connect_timeout,

   /// Connect timeout
   pong_timeout,

   /// SSL handshake timeout
   ssl_handshake_timeout,

   /// Can't receive push synchronously without blocking
   sync_receive_push_failed,

   /// Incompatible node depth.
   incompatible_node_depth,
};

/** \internal
 *  \brief Creates a error_code object from an error.
 *  \param e Error code.
 *  \ingroup any
 */
auto make_error_code(error e) -> system::error_code;

} // boost::redis

namespace std {

template<>
struct is_error_code_enum<::boost::redis::error> : std::true_type {};

} // std

#endif // BOOST_REDIS_ERROR_HPP
