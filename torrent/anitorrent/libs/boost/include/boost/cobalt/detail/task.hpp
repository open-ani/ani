//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_TASK_HPP
#define BOOST_COBALT_DETAIL_TASK_HPP

#include <boost/cobalt/detail/exception.hpp>
#include <boost/cobalt/detail/forward_cancellation.hpp>
#include <boost/cobalt/detail/wrapper.hpp>
#include <boost/cobalt/detail/this_thread.hpp>

#include <boost/asio/bind_allocator.hpp>
#include <boost/asio/cancellation_signal.hpp>


#include <coroutine>
#include <optional>
#include <utility>

namespace boost::cobalt
{

struct as_tuple_tag;
struct as_result_tag;

template<typename Return>
struct task;

namespace detail
{

template<typename T>
struct task_receiver;

template<typename T>
struct task_value_holder
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
    static_cast<task_receiver<T>*>(this)->set_done();
  }
  void return_value(const T & ret)
  {
    result.emplace(ret);
    static_cast<task_receiver<T>*>(this)->set_done();
  }
};

template<>
struct task_value_holder<void>
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
struct task_promise;

template<typename T>
struct task_receiver : task_value_holder<T>
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
  unique_handle<void> awaited_from{nullptr};

  void set_done()
  {
    done = true;
  }

  void cancel(asio::cancellation_type ct) const
  {
    if (!done)
      promise->signal.emit(ct);
  }

  task_receiver() = default;
  task_receiver(task_receiver && lhs)
      : task_value_holder<T>(std::move(lhs)),
        exception(std::move(lhs.exception)), done(lhs.done), awaited_from(std::move(lhs.awaited_from)),
        promise(lhs.promise)
  {
    if (!done && !exception)
    {
      promise->receiver = this;
      lhs.exception = moved_from_exception();
    }

    lhs.done = true;
  }

  ~task_receiver()
  {
    if (!done && promise && promise->receiver == this)
    {
      promise->receiver = nullptr;
      if (!promise->started)
        std::coroutine_handle<task_promise<T>>::from_promise(*promise).destroy();
    }
  }

  task_receiver(task_promise<T> * promise)
      : promise(promise)
  {
      promise->receiver = this;
  }

  struct awaitable
  {
    task_receiver * self;
    asio::cancellation_slot cl;
    awaitable(task_receiver * self) : self(self)
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
    BOOST_NOINLINE std::coroutine_handle<void> await_suspend(std::coroutine_handle<Promise> h)
    {
      if (self->done) // ok, so we're actually done already, so noop
        return std::coroutine_handle<void>::from_address(h.address());

      if constexpr (requires (Promise p) {p.get_cancellation_slot();})
        if ((cl = h.promise().get_cancellation_slot()).is_connected())
          cl.emplace<forward_cancellation>(self->promise->signal);


      if constexpr (requires (Promise p) {p.get_executor();})
        self->promise->exec.emplace(h.promise().get_executor());
      else
        self->promise->exec.emplace(this_thread::get_executor());
      self->promise->exec_ = self->promise->exec->get_executor();
      self->awaited_from.reset(h.address());

      return std::coroutine_handle<task_promise<T>>::from_promise(*self->promise);
    }

    T await_resume(const boost::source_location & loc = BOOST_CURRENT_LOCATION)
    {
      if (cl.is_connected())
        cl.clear();

      return self->get_result().value(loc);
    }

    system::result<T, std::exception_ptr> await_resume(const as_result_tag &)
    {
      if (cl.is_connected())
        cl.clear();
      return self->get_result();
    }

    auto await_resume(const as_tuple_tag &)
    {
      if (cl.is_connected())
        cl.clear();
      auto res = self->get_result();
      if constexpr (std::is_void_v<T>)
        return res.error();
      else
      {
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
      self->exception = detached_exception();
      if (self->awaited_from)
        self->awaited_from.release().resume();
    }
  };

  task_promise<T>  * promise;

  awaitable get_awaitable() {return awaitable{this};}


  void interrupt_await() &
  {
    exception = detached_exception();
    awaited_from.release().resume();
  }
};

inline void task_value_holder<void>::return_void()
{
  static_cast<task_receiver<void>*>(this)->set_done();
}

template<typename Return>
struct task_promise_result
{
  task_receiver<Return>* receiver{nullptr};
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
struct task_promise_result<void>
{
  task_receiver<void>* receiver{nullptr};
  void return_void()
  {
    if(receiver)
      receiver->return_void();
  }
};

struct async_initiate_spawn;

template<typename Return>
struct task_promise
    : promise_memory_resource_base,
      promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>,
      promise_throw_if_cancelled_base,
      enable_awaitables<task_promise<Return>>,
      enable_await_allocator<task_promise<Return>>,
      enable_await_executor<task_promise<Return>>,
      task_promise_result<Return>
{
  using promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>::await_transform;
  using promise_throw_if_cancelled_base::await_transform;
  using enable_awaitables<task_promise<Return>>::await_transform;
  using enable_await_allocator<task_promise<Return>>::await_transform;
  using enable_await_executor<task_promise<Return>>::await_transform;

  [[nodiscard]] task<Return> get_return_object()
  {
    return task<Return>{this};
  }

  mutable asio::cancellation_signal signal;

  using executor_type = executor;
  std::optional<asio::executor_work_guard<executor_type>> exec;
  std::optional<executor_type> exec_;
  const executor_type & get_executor() const
  {
      if (!exec)
          throw_exception(asio::bad_executor());
      BOOST_ASSERT(exec_);
      return *exec_;
  }

  template<typename ... Args>
  task_promise(Args & ...args)
#if !defined(BOOST_COBALT_NO_PMR)
    : promise_memory_resource_base(detail::get_memory_resource_from_args_global(args...))
#endif
  {
    this->reset_cancellation_source(signal.slot());
  }

  struct initial_awaitable
  {
    task_promise * promise;

    bool await_ready() const noexcept {return false;}
    void await_suspend(std::coroutine_handle<>) {}

    void await_resume()
    {
      promise->started = true;
    }
  };

  auto initial_suspend()
  {

    return initial_awaitable{this};
  }

  struct final_awaitable
  {
    task_promise * promise;
    bool await_ready() const noexcept
    {
      return promise->receiver && promise->receiver->awaited_from.get() == nullptr;
    }

    BOOST_NOINLINE
    auto await_suspend(std::coroutine_handle<task_promise> h) noexcept
    {
      std::coroutine_handle<void> res = std::noop_coroutine();
      if (promise->receiver && promise->receiver->awaited_from.get() != nullptr)
        res = promise->receiver->awaited_from.release();


      if (auto & rec = h.promise().receiver; rec != nullptr)
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

  ~task_promise()
  {
    if (this->receiver)
    {
      if (!this->receiver->done && !this->receiver->exception)
        this->receiver->exception = completed_unexpected();
      this->receiver->set_done();
      this->receiver->awaited_from.reset(nullptr);
    }
  }
  bool started = false;
  friend struct async_initiate;
};

}

}

#endif //BOOST_COBALT_DETAIL_TASK_HPP
