//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_PROMISE_HPP
#define BOOST_COBALT_PROMISE_HPP

#include <boost/cobalt/detail/promise.hpp>

namespace boost::cobalt
{

// tag::outline[]
template<typename Return>
struct [[nodiscard]] promise
{
    promise(promise &&lhs) noexcept;
    promise& operator=(promise && lhs) noexcept;

    // enable `co_await`. <1>
    auto operator co_await ();

    // Ignore the return value, i.e. detach it. <2>
    void operator +() &&;

    // Cancel the promise.
    void cancel(asio::cancellation_type ct = asio::cancellation_type::all);

    // Check if the result is ready
    bool ready() const;
    // Check if the promise can be awaited.
    explicit operator bool () const; // <3>

    // Detach or attach
    bool attached() const;
    void detach();
    void attach();
    // end::outline[]

    /* tag::outline[]
    // Get the return value. If !ready() this function has undefined behaviour.
    Return get();
    end::outline[] */

    Return get(const boost::source_location & loc = BOOST_CURRENT_LOCATION)
    {
      BOOST_ASSERT(ready());
      return receiver_.get_result().value(loc);
    }

    using promise_type = detail::cobalt_promise<Return>;
    promise(const promise &) = delete;
    promise& operator=(const promise &) = delete;

    ~promise()
    {
      if (attached_)
        cancel();
    }
  private:
    template<typename>
    friend struct detail::cobalt_promise;

    promise(detail::cobalt_promise<Return> * promise) : receiver_(promise->receiver, promise->signal), attached_{true}
    {
    }

    detail::promise_receiver<Return> receiver_;
    bool attached_;

    friend struct detached;
    //tag::outline[]
};
// end::outline[]

template<typename T>
inline
promise<T>::promise(promise &&lhs) noexcept
    : receiver_(std::move(lhs.receiver_)), attached_(std::exchange(lhs.attached_, false))
{
}

template<typename T>
inline
promise<T>& promise<T>::operator=(promise && lhs) noexcept
{
    if (attached_)
    cancel();
  receiver_ = std::move(lhs.receiver_);
  attached_ = std::exchange(lhs.attached_, false);
}

template<typename T>
inline
auto promise<T>::operator co_await () {return receiver_.get_awaitable();}

// Ignore the returns value
template<typename T>
inline
void promise<T>::operator +() && {detach();}

template<typename T>
inline
void promise<T>::cancel(asio::cancellation_type ct)
{
  if (!receiver_.done && receiver_.reference == &receiver_)
    receiver_.cancel_signal.emit(ct);
}

template<typename T>
inline
bool promise<T>::ready() const  { return receiver_.done; }

template<typename T>
inline
promise<T>::operator bool () const
{
  return !receiver_.done || !receiver_.result_taken;
}

template<typename T>
inline
bool promise<T>::attached() const {return attached_;}

template<typename T>
inline
void promise<T>::detach() {attached_ = false;}
template<typename T>
inline
void promise<T>::attach() {attached_ = true;}


}

#endif //BOOST_COBALT_PROMISE_HPP
