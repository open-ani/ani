//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_PROMISE_HPP
#define BOOST_COBALT_DETAIL_PROMISE_HPP

#include <boost/cobalt/detail/exception.hpp>
#include <boost/cobalt/detail/forward_cancellation.hpp>
#include <boost/cobalt/detail/wrapper.hpp>
#include <boost/cobalt/detail/this_thread.hpp>
#include <boost/cobalt/unique_handle.hpp>

#include <boost/asio/cancellation_signal.hpp>




#include <boost/core/exchange.hpp>

#include <coroutine>
#include <optional>
#include <utility>
#include <boost/asio/bind_allocator.hpp>

namespace boost::cobalt
{

struct as_tuple_tag;
struct as_result_tag;

template<typename Return>
struct promise;

namespace detail
{

template<typename T>
struct promise_receiver;

template<typename T>
struct promise_value_holder
{
  std::optional<T> result;
  bool result_taken = false;

  system::result<T, std::exception_ptr> get_result_value()
  {
    result_taken = true;
    BOOST_ASSERT(result);
    return {system::in_place_value, std::move(*result)};
  }

  void return_value(T && ret)
  {
    result.emplace(std::move(ret));
    static_cast<promise_receiver<T>*>(this)->set_done();
  }

  void return_value(const T & ret)
  {
    result.emplace(ret);
    static_cast<promise_receiver<T>*>(this)->set_done();
  }

};

template<>
struct promise_value_holder<void>
{
  bool result_taken = false;
  system::result<void, std::exception_ptr> get_result_value()
  {
    result_taken = true;
    return {system::in_place_value};
  }

  inline void return_void();
};



template<typename T>
struct promise_receiver : promise_value_holder<T>
{
  std::exception_ptr exception;
  system::result<T, std::exception_ptr> get_result()
  {
    if (exception && !done) // detached error
      return {system::in_place_error, std::exchange(exception, nullptr)};
    else if (exception)
    {
      this->result_taken = true;
      return {system::in_place_error, exception};
    }
    return this->get_result_value();
  }
  void unhandled_exception()
  {
    exception = std::current_exception();
    set_done();
  }

  bool done = false;
  unique_handle<void>  awaited_from{nullptr};

  void set_done()
  {
    done = true;
  }

  promise_receiver() = default;
  promise_receiver(promise_receiver && lhs) noexcept
      : promise_value_holder<T>(std::move(lhs)),
        exception(std::move(lhs.exception)), done(lhs.done), awaited_from(std::move(lhs.awaited_from)),
        reference(lhs.reference), cancel_signal(lhs.cancel_signal)
  {
    if (!done && !exception)
    {
      reference = this;
      lhs.exception = moved_from_exception();
    }

    lhs.done = true;

  }

  ~promise_receiver()
  {
    if (!done && reference == this)
      reference = nullptr;
  }

  promise_receiver(promise_receiver * &reference, asio::cancellation_signal & cancel_signal)
      : reference(reference), cancel_signal(cancel_signal)
  {
    reference = this;
  }

  struct awaitable
  {
    promise_receiver * self;
    std::exception_ptr ex;
    asio::cancellation_slot cl;

    awaitable(promise_receiver * self) : self(self)
    {
    }

    awaitable(awaitable && aw) : self(aw.self)
    {
    }

    ~awaitable ()
    {
    }
    bool await_ready() const { return self->done; }

    template<typename Promise>
    bool await_suspend(std::coroutine_handle<Promise> h)
    {
      if (self->done) // ok, so we're actually done already, so noop
        return false;

      if (ex)
        return false;

      if (self->awaited_from != nullptr) // we're already being awaited, that's an error!
      {
        ex = already_awaited();
        return false;
      }

      if constexpr (requires (Promise p) {p.get_cancellation_slot();})
        if ((cl = h.promise().get_cancellation_slot()).is_connected())
          cl.emplace<forward_cancellation>(self->cancel_signal);

      self->awaited_from.reset(h.address());
      return true;
    }

    T await_resume(const boost::source_location & loc = BOOST_CURRENT_LOCATION)
    {
      if (cl.is_connected())
        cl.clear();
      if (ex)
        std::rethrow_exception(ex);
      return self->get_result().value(loc);
    }

    system::result<T, std::exception_ptr> await_resume(const as_result_tag &)
    {
      if (cl.is_connected())
        cl.clear();
      if (ex)
        return {system::in_place_error, std::move(ex)};
      return self->get_result();
    }

    auto await_resume(const as_tuple_tag &)
    {
      if (cl.is_connected())
        cl.clear();

      if constexpr (std::is_void_v<T>)
      {
        if (ex)
          return std::move(ex);
        return self->get_result().error();
      }
      else
      {
        if (ex)
          return std::make_tuple(std::move(ex), T{});
        auto res = self->get_result();
        if (res.has_error())
          return std::make_tuple(res.error(), T{});
        else
          return std::make_tuple(std::exception_ptr(), std::move(*res));
      }
    }
    void interrupt_await() &
    {
      if (!self)
        return ;
      ex = detached_exception();
      if (self->awaited_from)
        self->awaited_from.release().resume();
    }
  };

  promise_receiver  * &reference;
  asio::cancellation_signal & cancel_signal;

  awaitable get_awaitable() {return awaitable{this};}


  void interrupt_await() &
  {
    exception = detached_exception();
    awaited_from.release().resume();
  }
};

inline void promise_value_holder<void>::return_void()
{
  static_cast<promise_receiver<void>*>(this)->set_done();
}

template<typename Return>
struct cobalt_promise_result
{
  promise_receiver<Return>* receiver{nullptr};
  void return_value(Return && ret)
  {
    if(receiver)
      receiver->return_value(std::move(ret));
  }

  void return_value(const Return & ret)
  {
    if(receiver)
      receiver->return_value(ret);
  }

};

template<>
struct cobalt_promise_result<void>
{
  promise_receiver<void>* receiver{nullptr};
  void return_void()
  {
    if(receiver)
      receiver->return_void();
  }
};

template<typename Return>
struct cobalt_promise
    : promise_memory_resource_base,
      promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>,
      promise_throw_if_cancelled_base,
      enable_awaitables<cobalt_promise<Return>>,
      enable_await_allocator<cobalt_promise<Return>>,
      enable_await_executor<cobalt_promise<Return>>,
      cobalt_promise_result<Return>
{
  using promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>::await_transform;
  using promise_throw_if_cancelled_base::await_transform;
  using enable_awaitables<cobalt_promise<Return>>::await_transform;
  using enable_await_allocator<cobalt_promise<Return>>::await_transform;
  using enable_await_executor<cobalt_promise<Return>>::await_transform;

  [[nodiscard]] promise<Return> get_return_object()
  {
    return promise<Return>{this};
  }

  mutable asio::cancellation_signal signal;

  using executor_type = executor;
  executor_type exec;
  const executor_type & get_executor() const {return exec;}

  template<typename ... Args>
  cobalt_promise(Args & ...args)
      :
#if !defined(BOOST_COBALT_NO_PMR)
  promise_memory_resource_base(detail::get_memory_resource_from_args(args...)),
#endif
        exec{detail::get_executor_from_args(args...)}
  {
    this->reset_cancellation_source(signal.slot());
  }

  std::suspend_never initial_suspend()        {return {};}
  auto final_suspend() noexcept
  {
    return final_awaitable{this};
  }

  void unhandled_exception()
  {
    if (this->receiver)
      this->receiver->unhandled_exception();
    else
      throw ;
  }

  ~cobalt_promise()
  {
    if (this->receiver)
    {
      if (!this->receiver->done && !this->receiver->exception)
        this->receiver->exception = completed_unexpected();
      this->receiver->set_done();
      this->receiver->awaited_from.reset(nullptr);
    }

  }
 private:
  struct final_awaitable
  {
    cobalt_promise * promise;
    bool await_ready() const noexcept
    {
      return promise->receiver && promise->receiver->awaited_from.get() == nullptr;
    }

    std::coroutine_handle<void> await_suspend(std::coroutine_handle<cobalt_promise> h) noexcept
    {
      std::coroutine_handle<void> res = std::noop_coroutine();
      if (promise->receiver && promise->receiver->awaited_from.get() != nullptr)
        res = promise->receiver->awaited_from.release();


      if (auto &rec = h.promise().receiver; rec != nullptr)
      {
        if (!rec->done && !rec->exception)
          rec->exception = completed_unexpected();
        rec->set_done();
        rec->awaited_from.reset(nullptr);
        rec = nullptr;
      }
      detail::self_destroy(h);
      return res;
    }

    void await_resume() noexcept
    {
    }
  };


};

}

}

#endif //BOOST_COBALT_DETAIL_PROMISE_HPP
