/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_HELPER_HPP
#define BOOST_REDIS_HELPER_HPP

#include <boost/asio/cancellation_type.hpp>

namespace boost::redis::detail
{

template <class T>
auto is_cancelled(T const& self)
{
   return self.get_cancellation_state().cancelled() != asio::cancellation_type_t::none;
}

#define BOOST_REDIS_CHECK_OP0(X)\
   if (ec || redis::detail::is_cancelled(self)) {\
      X\
      self.complete(!!ec ? ec : asio::error::operation_aborted);\
      return;\
   }

#define BOOST_REDIS_CHECK_OP1(X)\
   if (ec || redis::detail::is_cancelled(self)) {\
      X\
      self.complete(!!ec ? ec : asio::error::operation_aborted, {});\
      return;\
   }

} // boost::redis::detail

#endif // BOOST_REDIS_HELPER_HPP
