//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_THREAD_HPP
#define BOOST_COBALT_DETAIL_THREAD_HPP

#include <boost/cobalt/config.hpp>
#include <boost/cobalt/detail/forward_cancellation.hpp>
#include <boost/cobalt/detail/handler.hpp>
#include <boost/cobalt/concepts.hpp>
#include <boost/cobalt/this_coro.hpp>

#include <boost/asio/cancellation_signal.hpp>

#include <thread>

namespace boost::cobalt
{

struct as_tuple_tag;
struct as_result_tag;

namespace detail
{
struct thread_promise;
}

struct thread;

namespace detail
{


struct signal_helper_2
{
  asio::cancellation_signal signal;
};


struct thread_state
{
  asio::io_context ctx{1u};
  asio::cancellation_signal signal;
  std::mutex mtx;
  std::optional<completion_handler<std::exception_ptr>> waitor;
  std::atomic<bool> done = false;
};

struct thread_promise : signal_helper_2,
                        promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>,
                        promise_throw_if_cancelled_base,
                        enable_awaitables<thread_promise>,
                        enable_await_allocator<thread_promise>,
                        enable_await_executor<thread_promise>
{
  BOOST_COBALT_DECL thread_promise();

  struct initial_awaitable
  {
    bool await_ready() const {return false;}
    void await_suspend(std::coroutine_handle<thread_promise> h)
    {
      h.promise().mtx.unlock();
    }

    void await_resume() {}
  };

  auto initial_suspend() noexcept
  {
    return initial_awaitable{};
  }
  std::suspend_never final_suspend() noexcept
  {
    wexec_.reset();
    return {};
  }

  void unhandled_exception() { throw; }
  void return_void() { }

  using executor_type = typename cobalt::executor;
  const executor_type & get_executor() const {return *exec_;}

#if !defined(BOOST_COBALT_NO_PMR)
  using allocator_type = pmr::polymorphic_allocator<void>;
  using resource_type  = pmr::unsynchronized_pool_resource;

  resource_type * resource;
  allocator_type  get_allocator() const { return allocator_type(resource); }
#endif

  using promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>::await_transform;
  using promise_throw_if_cancelled_base::await_transform;
  using enable_awaitables<thread_promise>::await_transform;
  using enable_await_allocator<thread_promise>::await_transform;
  using enable_await_executor<thread_promise>::await_transform;

  BOOST_COBALT_DECL
  boost::cobalt::thread get_return_object();

  void set_executor(asio::io_context::executor_type exec)
  {
    wexec_.emplace(exec);
    exec_.emplace(exec);
  }

  std::mutex mtx;
 private:

  std::optional<asio::executor_work_guard<asio::io_context::executor_type>> wexec_;
  std::optional<cobalt::executor> exec_;
};

struct thread_awaitable
{
  asio::cancellation_slot cl;
  std::optional<std::tuple<std::exception_ptr>> res;
  bool await_ready(const boost::source_location & loc = BOOST_CURRENT_LOCATION) const
  {
    if (state_ == nullptr)
      boost::throw_exception(std::invalid_argument("Thread expired"), loc);
    std::lock_guard<std::mutex> lock{state_->mtx};
    return state_->done;
  }

  template<typename Promise>
  bool await_suspend(std::coroutine_handle<Promise> h)
  {
    BOOST_ASSERT(state_);

    std::lock_guard<std::mutex> lock{state_->mtx};
    if (state_->done)
      return false;

    if constexpr (requires (Promise p) {p.get_cancellation_slot();})
      if ((cl = h.promise().get_cancellation_slot()).is_connected())
      {
        cl.assign(
            [st = state_](asio::cancellation_type type)
            {
              std::lock_guard<std::mutex> lock{st->mtx};
              asio::post(st->ctx,
                         [st, type]
                         {
                            BOOST_ASIO_HANDLER_LOCATION((__FILE__, __LINE__, __func__));
                            st->signal.emit(type);
                         });
            });

      }

    state_->waitor.emplace(h, res);
    return true;
  }

  void await_resume()
  {
    if (cl.is_connected())
      cl.clear();
    if (thread_)
      thread_->join();
    if (!res) // await_ready
      return;
    if (auto ee = std::get<0>(*res))
      std::rethrow_exception(ee);
  }

  system::result<void, std::exception_ptr> await_resume(const as_result_tag &)
  {
    if (cl.is_connected())
      cl.clear();
    if (thread_)
      thread_->join();
    if (!res) // await_ready
      return {system::in_place_value};
    if (auto ee = std::get<0>(*res))
      return {system::in_place_error, std::move(ee)};

    return {system::in_place_value};
  }

  std::tuple<std::exception_ptr> await_resume(const as_tuple_tag &)
  {
    if (cl.is_connected())
      cl.clear();
    if (thread_)
      thread_->join();

    return std::get<0>(*res);
  }

  explicit thread_awaitable(std::shared_ptr<detail::thread_state> state)
      : state_(std::move(state)) {}

  explicit thread_awaitable(std::thread thread,
                            std::shared_ptr<detail::thread_state> state)
      : thread_(std::move(thread)), state_(std::move(state)) {}
 private:
  std::optional<std::thread> thread_;
  std::shared_ptr<detail::thread_state> state_;
};
}

}

#endif //BOOST_COBALT_DETAIL_THREAD_HPP
