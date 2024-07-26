//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_THREAD_HPP
#define BOOST_COBALT_THREAD_HPP

#include <boost/cobalt/detail/thread.hpp>
#include <boost/cobalt/detail/await_result_helper.hpp>



namespace boost::cobalt
{

// tag::outline[]
struct thread
{
  // Send a cancellation signal
  void cancel(asio::cancellation_type type = asio::cancellation_type::all);


  // Allow the thread to be awaited. NOOP if the thread is invalid.
  auto operator co_await() &-> detail::thread_awaitable; //<1>
  auto operator co_await() && -> detail::thread_awaitable; //<2>

  // Stops the io_context & joins the executor
  ~thread();
  /// Move constructible
  thread(thread &&) noexcept = default;

  using executor_type = executor;

  using id = std::thread::id;
  id get_id() const noexcept;

  // end::outline[]
  /* tag::outline[]
  // Add the functions similar to `std::thread`
  void join();
  bool joinable() const;
  void detach();

  executor_type get_executor() const;
  end::outline[] */


  BOOST_COBALT_DECL void join();
  BOOST_COBALT_DECL bool joinable() const;
  BOOST_COBALT_DECL void detach();

  executor_type get_executor(const boost::source_location & loc = BOOST_CURRENT_LOCATION) const
  {
    auto st = state_;
    if (!st || st->done)
      boost::throw_exception(asio::execution::bad_executor(), loc);

    return st ->ctx.get_executor();
  }


  using promise_type = detail::thread_promise;

 private:
  thread(std::thread thr, std::shared_ptr<detail::thread_state> state)
      : thread_(std::move(thr)), state_(std::move(state))
  {
  }

  std::thread thread_;
  std::shared_ptr<detail::thread_state> state_;
  friend struct detail::thread_promise;
  // tag::outline[]
};
// end::outline[]


inline
void thread::cancel(asio::cancellation_type type)
{
  if (auto st = state_)
    asio::post(state_->ctx,
               [s= state_, type]
               {
                 BOOST_ASIO_HANDLER_LOCATION((__FILE__, __LINE__, __func__));
                 s->signal.emit(type);
               });
}


inline
auto thread::operator co_await() &-> detail::thread_awaitable
{
  return detail::thread_awaitable{std::move(state_)};
}
inline
auto thread::operator co_await() && -> detail::thread_awaitable
{
  return detail::thread_awaitable{std::move(thread_), std::move(state_)};
}
inline
thread::~thread()
{
  if (state_)
  {
    state_->ctx.stop();
    state_.reset();
  }

  if (thread_.joinable())
    thread_.join();
}


inline
thread::id thread::get_id() const noexcept {return thread_.get_id();}

}

#endif //BOOST_COBALT_THREAD_HPP
