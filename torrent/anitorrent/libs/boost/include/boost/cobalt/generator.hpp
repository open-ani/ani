//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_GENERATOR_HPP
#define BOOST_COBALT_GENERATOR_HPP

#include <boost/cobalt/detail/generator.hpp>


namespace boost::cobalt
{

// tag::outline[]
template<typename Yield, typename Push = void>
struct [[nodiscard]] generator
// end::outline[]
     : detail::generator_base<Yield, Push>
// tag::outline[]
{
  // Movable

  generator(generator &&lhs) noexcept = default;
  generator& operator=(generator &&) noexcept = default;

  // True until it co_returns & is co_awaited after <1>
  explicit operator bool() const;

  // Cancel the generator. <3>
  void cancel(asio::cancellation_type ct = asio::cancellation_type::all);

  // Check if a value is available
  bool ready() const;

  // Get the returned value. If !ready() this function has undefined behaviour.
  Yield get();

  // Cancel & detach the generator.
  ~generator();

  // end::outline[]
  using promise_type = detail::generator_promise<Yield, Push>;

  generator(const generator &) = delete;
  generator& operator=(const generator &) = delete;


 private:
  template<typename, typename>
  friend struct detail::generator_base;
  template<typename, typename>
  friend struct detail::generator_promise;

  generator(detail::generator_promise<Yield, Push> * generator) : receiver_(generator->receiver, generator->signal)
  {
  }
  detail::generator_receiver<Yield, Push> receiver_;

  /* tag::outline[]
  // an awaitable that results in value of `Yield`.
  using __generator_awaitable__ = __unspecified__;

  // Present when `Push` != `void`
  __generator_awaitable__ operator()(      Push && push);
  __generator_awaitable__ operator()(const Push &  push);

  // Present when `Push` == `void`, i.e. can `co_await` the generator directly.
  __generator_awaitable__ operator co_await (); // <2>
   end::outline[]
   */


// tag::outline[]

};
// end::outline[]



template<typename Yield, typename Push >
inline generator<Yield, Push>::operator bool() const
{
  return !receiver_.done || receiver_.result || receiver_.exception;
}

template<typename Yield, typename Push >
inline void generator<Yield, Push>::cancel(asio::cancellation_type ct)
{
  if (!receiver_.done && receiver_.reference == &receiver_)
    receiver_.cancel_signal.emit(ct);
}

template<typename Yield, typename Push >
inline bool generator<Yield, Push>::ready() const  { return receiver_.result || receiver_.exception; }

template<typename Yield, typename Push >
inline Yield generator<Yield, Push>::get()
{
  BOOST_ASSERT(ready());
  receiver_.rethrow_if();
  return receiver_.get_result();
}

template<typename Yield, typename Push >
inline generator<Yield, Push>::~generator() { cancel(); }


}

#endif //BOOST_COBALT_GENERATOR_HPP
