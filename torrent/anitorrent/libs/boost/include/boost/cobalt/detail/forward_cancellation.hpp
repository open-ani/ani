//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_FORWARD_CANCELLATION_HPP
#define BOOST_COBALT_DETAIL_FORWARD_CANCELLATION_HPP

#include <boost/cobalt/config.hpp>
#include <boost/asio/cancellation_signal.hpp>
#include <boost/asio/dispatch.hpp>

namespace boost::cobalt
{

// Requests cancellation where a successful cancellation results
// in no apparent side effects and where the op can re-awaited.
template<typename Awaitable>
concept interruptible =
       ( std::is_rvalue_reference_v<Awaitable> && requires (Awaitable && t) {std::move(t).interrupt_await();})
    || (!std::is_rvalue_reference_v<Awaitable> && requires (Awaitable t) {t.interrupt_await();});


}

namespace boost::cobalt::detail
{

struct forward_cancellation
{
  asio::cancellation_signal &cancel_signal;

  forward_cancellation(asio::cancellation_signal &cancel_signal) : cancel_signal(cancel_signal) {}
  void operator()(asio::cancellation_type ct) const
  {
    cancel_signal.emit(ct);
  }
};


}

#endif //BOOST_COBALT_DETAIL_FORWARD_CANCELLATION_HPP
