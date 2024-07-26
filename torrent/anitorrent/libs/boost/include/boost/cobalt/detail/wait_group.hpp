//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_WAIT_GROUP_HPP
#define BOOST_COBALT_DETAIL_WAIT_GROUP_HPP

#include <boost/cobalt/promise.hpp>
#include <boost/cobalt/race.hpp>
#include <boost/cobalt/gather.hpp>

#include <list>


namespace boost::cobalt::detail
{

struct race_wrapper
{
  using impl_type = decltype(race(std::declval<std::list<promise<void>> &>()));
  std::list<promise<void>> &waitables_;

  race_wrapper(std::list<promise<void>> &waitables) : waitables_(waitables)
  {
  }

  struct awaitable_type
  {

    bool await_ready()
    {
      if (waitables_.empty())
        return true;
      else
        return impl_->await_ready();
    }

    template<typename Promise>
    auto await_suspend(std::coroutine_handle<Promise> h)
    {
      return impl_->await_suspend(h);
    }

    void await_resume()
    {
      if (waitables_.empty())
        return;
      auto idx = impl_->await_resume();
      if (idx != std::numeric_limits<std::size_t>::max())
        waitables_.erase(std::next(waitables_.begin(), idx));
    }

    awaitable_type(std::list<promise<void>> &waitables) : waitables_(waitables)
    {
        if (!waitables_.empty())
          impl_.emplace(waitables_, random_);
    }

   private:
    std::optional<impl_type::awaitable> impl_;
    std::list<promise<void>> &waitables_;
    std::default_random_engine &random_{detail::prng()};

  };
  awaitable_type operator co_await() &&
  {
    return awaitable_type(waitables_);
  }
};

struct gather_wrapper
{
  using impl_type = decltype(gather(std::declval<std::list<promise<void>> &>()));
  std::list<promise<void>> &waitables_;

  gather_wrapper(std::list<promise<void>> &waitables) : waitables_(waitables)
  {
  }

  struct awaitable_type
  {
    bool await_ready()
    {
      if (waitables_.empty())
        return true;
      else
        return impl_->await_ready();
    }

    template<typename Promise>
    auto await_suspend(std::coroutine_handle<Promise> h)
    {
      return impl_->await_suspend(h);
    }

    void await_resume()
    {
      if (waitables_.empty())
        return;
      BOOST_ASSERT(impl_);
      impl_->await_resume();
      waitables_.clear();
    }

    awaitable_type(std::list<promise<void>> &waitables) : waitables_(waitables)
    {
      if (!waitables_.empty())
        impl_.emplace(waitables_);
    }
   private:
    std::list<promise<void>> &waitables_;
    std::optional<decltype(gather(waitables_).operator co_await())> impl_;
  };

  awaitable_type operator co_await()
  {
    return awaitable_type(waitables_);
  }

};

}

#endif //BOOST_COBALT_DETAIL_WAIT_GROUP_HPP
