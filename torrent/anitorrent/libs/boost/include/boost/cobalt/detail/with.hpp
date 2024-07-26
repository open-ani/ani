// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_DETAIL_WITH_HPP
#define BOOST_COBALT_DETAIL_WITH_HPP

#include <boost/cobalt/concepts.hpp>
#include <boost/cobalt/this_coro.hpp>

#include <boost/asio/cancellation_signal.hpp>

namespace boost::cobalt::detail
{

template<typename T>
struct [[nodiscard]] with_impl
{
    struct promise_type;

    bool await_ready() { return false;}

    template<typename Promise>
    BOOST_NOINLINE auto await_suspend(std::coroutine_handle<Promise> h) -> std::coroutine_handle<promise_type>;
    inline T await_resume();

  private:
    with_impl(promise_type & promise) : promise(promise) {}
    promise_type & promise;
};

template<typename T>
struct with_promise_value
{
  std::optional<T> result;

  void return_value(std::optional<T> && value)
  {
    if (value) // so non-move-assign types work
      result.emplace(std::move(*value));
  }

  T get_result()
  {
    return std::move(result).value();
  }
};

template<>
struct with_promise_value<void>
{
  void return_void() {}
  void get_result() {}
};


template<typename T>
struct with_impl<T>::promise_type
        : with_promise_value<T>,
          enable_awaitables<promise_type>,
          enable_await_allocator<promise_type>
{
  using enable_awaitables<promise_type>::await_transform;
  using enable_await_allocator<promise_type>::await_transform;


  using executor_type = executor;
  const executor_type & get_executor() const {return *exec;}
  std::optional<executor_type> exec;

  with_impl get_return_object()
  {
      return with_impl{*this};
  }

  std::exception_ptr e;
  void unhandled_exception()
  {
    e = std::current_exception();
  }

  std::suspend_always initial_suspend() {return {};}

  struct final_awaitable
  {
    promise_type *promise;

    bool await_ready() const noexcept
    {
      return false;
    }
    BOOST_NOINLINE
    auto await_suspend(std::coroutine_handle<promise_type> h) noexcept -> std::coroutine_handle<void>
    {
      return std::coroutine_handle<void>::from_address(h.promise().awaited_from.address());
    }

    void await_resume() noexcept
    {
    }
  };

  auto final_suspend() noexcept
  {
    return final_awaitable{this};
  }
  using cancellation_slot_type = asio::cancellation_slot;
  cancellation_slot_type get_cancellation_slot() const {return slot_;}
  asio::cancellation_slot slot_;

  std::coroutine_handle<void> awaited_from{nullptr};

};

template<typename T>
T with_impl<T>::await_resume()
{
    auto e = promise.e;
    auto res = std::move(promise.get_result());
    std::coroutine_handle<promise_type>::from_promise(promise).destroy();
    if (e)
        std::rethrow_exception(e);

    return std::move(res);
}

template<>
inline void with_impl<void>::await_resume()
{
  auto e = promise.e;
  std::coroutine_handle<promise_type>::from_promise(promise).destroy();
  if (e)
    std::rethrow_exception(e);
}

template<typename T>
template<typename Promise>
auto with_impl<T>::await_suspend(std::coroutine_handle<Promise> h) -> std::coroutine_handle<promise_type>
{
    if constexpr (requires (Promise p) {p.get_executor();})
        promise.exec.emplace(h.promise().get_executor());
    else
        promise.exec.emplace(this_thread::get_executor());

    if constexpr (requires (Promise p) {p.get_cancellation_slot();})
        promise.slot_ = h.promise().get_cancellation_slot();

    promise.awaited_from = h;
    return std::coroutine_handle<promise_type>::from_promise(promise);
}

}

#endif //BOOST_COBALT_DETAIL_WITH_HPP
