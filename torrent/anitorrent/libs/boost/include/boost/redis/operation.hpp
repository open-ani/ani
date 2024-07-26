/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_OPERATION_HPP
#define BOOST_REDIS_OPERATION_HPP

namespace boost::redis {

/** @brief Connection operations that can be cancelled.
 *  @ingroup high-level-api
 *  
 *  The operations listed below can be passed to the
 *  `boost::redis::connection::cancel` member function.
 */
enum class operation {
   /// Resolve operation.
   resolve,
   /// Connect operation.
   connect,
   /// SSL handshake operation.
   ssl_handshake,
   /// Refers to `connection::async_exec` operations.
   exec,
   /// Refers to `connection::async_run` operations.
   run,
   /// Refers to `connection::async_receive` operations.
   receive,
   /// Cancels reconnection.
   reconnection,
   /// Health check operation.
   health_check,
   /// Refers to all operations.
   all,
};

} // boost::redis

#endif // BOOST_REDIS_OPERATION_HPP
