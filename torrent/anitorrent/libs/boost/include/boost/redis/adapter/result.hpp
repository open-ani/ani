
/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_ADAPTER_RESULT_HPP
#define BOOST_REDIS_ADAPTER_RESULT_HPP

#include <boost/redis/resp3/type.hpp>
#include <boost/redis/error.hpp>
#include <boost/system/result.hpp>
#include <string>

namespace boost::redis::adapter
{

/** @brief Stores any resp3 error
 *  @ingroup high-level-api
 */
struct error {
   /// RESP3 error data type.
   resp3::type data_type = resp3::type::invalid;

   /// Diagnostic error message sent by Redis.
   std::string diagnostic;
};

/** @brief Compares two error objects for equality
 *  @relates error
 *
 *  @param a Left hand side error object.
 *  @param b Right hand side error object.
 */
inline bool operator==(error const& a, error const& b)
{
   return a.data_type == b.data_type && a.diagnostic == b.diagnostic;
}

/** @brief Compares two error objects for difference
 *  @relates error
 *
 *  @param a Left hand side error object.
 *  @param b Right hand side error object.
 */
inline bool operator!=(error const& a, error const& b)
{
   return !(a == b);
}

/** @brief Stores response to individual Redis commands
 *  @ingroup high-level-api
 */
template <class Value>
using result = system::result<Value, error>;

BOOST_NORETURN inline void
throw_exception_from_error(error const & e, boost::source_location const &)
{
   system::error_code ec;
   switch (e.data_type) {
      case resp3::type::simple_error:
         ec = redis::error::resp3_simple_error;
         break;
      case resp3::type::blob_error:
         ec = redis::error::resp3_blob_error;
         break;
      case resp3::type::null:
         ec = redis::error::resp3_null;
         break;
      default:
         BOOST_ASSERT_MSG(false, "Unexpected data type.");
   }

   throw system::system_error(ec, e.diagnostic);
}

} // boost::redis::adapter

#endif // BOOST_REDIS_ADAPTER_RESULT_HPP
