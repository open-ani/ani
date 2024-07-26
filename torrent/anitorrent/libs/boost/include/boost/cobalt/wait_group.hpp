// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_WAIT_GROUP_HPP
#define BOOST_COBALT_WAIT_GROUP_HPP

#include <boost/cobalt/detail/wait_group.hpp>

namespace boost::cobalt
{
// tag::outline[]
struct wait_group
{
    // create a wait_group
    explicit
    wait_group(asio::cancellation_type normal_cancel = asio::cancellation_type::none,
               asio::cancellation_type exception_cancel = asio::cancellation_type::all);

    // insert a task into the group
    void push_back(promise<void> p);

    // the number of tasks in the group
    std::size_t size() const;
    // remove completed tasks without waiting (i.e. zombie tasks)
    std::size_t reap();
    // cancel all tasks
    void cancel(asio::cancellation_type ct = asio::cancellation_type::all);
    // end::outline[]

    /* tag::outline[]
    // wait for one task to complete.
    __wait_one_op__ wait_one();
    // wait for all tasks to complete
    __wait_op__ wait();
    // wait for all tasks to complete
    __wait_op__ operator co_await ();
    // when used with `with` , this will receive the exception
    // and wait for the completion
    // if `ep` is set, this will use the `exception_cancel` level,
    // otherwise the `normal_cancel` to cancel all promises.
    __wait_op__ await_exit(std::exception_ptr ep);
     end::outline[] */

    auto wait_one() -> detail::race_wrapper
    {
        return  detail::race_wrapper(waitables_);
    }

    detail::gather_wrapper wait()
    {
        return detail::gather_wrapper(waitables_);

    }
    detail::gather_wrapper::awaitable_type operator co_await ()
    {
      return detail::gather_wrapper(waitables_).operator co_await();
    }
    // swallow the exception here.
    detail::gather_wrapper await_exit(std::exception_ptr ep)
    {
        auto ct = ep ? ct_except_ : ct_normal_;
        if (ct != asio::cancellation_type::none)
            for (auto & w : waitables_)
                w.cancel(ct);
        return detail::gather_wrapper(waitables_);
    }


  private:
    std::list<promise<void>> waitables_;
    asio::cancellation_type ct_normal_, ct_except_;
  // tag::outline[]
};
// end::outline[]

inline wait_group::wait_group(
    asio::cancellation_type normal_cancel,
    asio::cancellation_type exception_cancel)
: ct_normal_(normal_cancel), ct_except_(exception_cancel) {}

inline
std::size_t wait_group::size() const {return waitables_.size();}

inline
std::size_t wait_group::reap()
{
  return erase_if(waitables_, [](promise<void> & p) { return p.ready() && p;});
}

inline
void wait_group::cancel(asio::cancellation_type ct)
{
  for (auto & w : waitables_)
    w.cancel(ct);
}

inline
void wait_group::push_back(promise<void> p) { waitables_.push_back(std::move(p));}


}

#endif //BOOST_COBALT_WAIT_GROUP_HPP
