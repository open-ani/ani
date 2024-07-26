//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_SPAWN_HPP
#define BOOST_COBALT_DETAIL_SPAWN_HPP

#include <boost/cobalt/task.hpp>
#include <boost/asio/dispatch.hpp>

#include <boost/smart_ptr/allocate_unique.hpp>

namespace boost::cobalt
{
template<typename T>
struct task;
}

namespace boost::cobalt::detail
{

struct async_initiate_spawn
{
  template<typename Handler, typename T>
  void operator()(Handler && h, task<T> a, executor exec)
  {
    auto & rec = a.receiver_;
    if (rec.done)
      return asio::dispatch(
          asio::get_associated_immediate_executor(h, exec),
          asio::append(std::forward<Handler>(h), rec.exception, rec.exception ? T() : *rec.get_result()));

#if !defined(BOOST_COBALT_NO_PMR)
    auto dalloc = pmr::polymorphic_allocator<void>{boost::cobalt::this_thread::get_default_resource()};
    auto alloc = asio::get_associated_allocator(h, dalloc);
#else
    auto alloc = asio::get_associated_allocator(h);
#endif
    auto recs = std::allocate_shared<detail::task_receiver<T>>(alloc, std::move(rec));

    auto sl = asio::get_associated_cancellation_slot(h);
    if (sl.is_connected())
      sl.assign(
          [exec, recs](asio::cancellation_type ct)
          {
            asio::dispatch(exec, [recs, ct] {recs->cancel(ct);});
          });

    auto p = recs.get();

    p->promise->exec.emplace(exec);
    p->promise->exec_ = exec;

    struct completion_handler
    {
      using allocator_type = std::decay_t<decltype(alloc)>;

      allocator_type get_allocator() const { return alloc_; }
      allocator_type alloc_;

      using executor_type = std::decay_t<decltype(asio::get_associated_executor(h, exec))>;
      const executor_type &get_executor() const { return exec_; }
      executor_type exec_;

      decltype(recs) r;
      Handler handler;

      void operator()()
      {
        auto ex = r->exception;
        T rr{};
        if (r->result)
          rr = std::move(*r->result);
        r.reset();
        std::move(handler)(ex, std::move(rr));
      }
    };

    p->awaited_from.reset(detail::post_coroutine(
        completion_handler{
            alloc, asio::get_associated_executor(h, exec), std::move(recs), std::move(h)
        }).address());

    asio::dispatch(exec, std::coroutine_handle<detail::task_promise<T>>::from_promise(*p->promise));
  }

  template<typename Handler>
  void operator()(Handler && h, task<void> a, executor exec)
  {
    if (a.receiver_.done)
      return asio::dispatch(
          asio::get_associated_immediate_executor(h, exec),
          asio::append(std::forward<Handler>(h), a.receiver_.exception));


#if !defined(BOOST_COBALT_NO_PMR)
    auto alloc = asio::get_associated_allocator(h, pmr::polymorphic_allocator<void>{boost::cobalt::this_thread::get_default_resource()});
#else
    auto alloc = asio::get_associated_allocator(h);
#endif
    auto recs = std::allocate_shared<detail::task_receiver<void>>(alloc, std::move(a.receiver_));

    if (recs->done)
      return asio::dispatch(asio::get_associated_immediate_executor(h, exec),
                            asio::append(std::forward<Handler>(h), recs->exception));

    auto sl = asio::get_associated_cancellation_slot(h);
    if (sl.is_connected())
      sl.assign(
          [exec, recs](asio::cancellation_type ct)
          {
            asio::dispatch(exec, [recs, ct] {recs->cancel(ct);});
          });

    auto p = recs.get();

    p->promise->exec.emplace(exec);
    p->promise->exec_ = exec;

    struct completion_handler
    {
      using allocator_type = std::decay_t<decltype(alloc)>;

      const allocator_type &get_allocator() const { return alloc_; }

      allocator_type alloc_;

      using executor_type = std::decay_t<decltype(asio::get_associated_executor(h, exec))>;
      const executor_type &get_executor() const { return exec_; }

      executor_type exec_;
      decltype(recs) r;
      Handler handler;

      void operator()()
      {
        auto ex = r->exception;
        r.reset();
        std::move(handler)(ex);
      }
    };

    p->awaited_from.reset(detail::post_coroutine(completion_handler{
        alloc, asio::get_associated_executor(h, exec), std::move(recs), std::forward<Handler>(h)
      }).address());

    asio::dispatch(exec, std::coroutine_handle<detail::task_promise<void>>::from_promise(*p->promise));
  }
};

}

#endif //BOOST_COBALT_DETAIL_SPAWN_HPP
