// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_CONCEPTS_HPP
#define BOOST_COBALT_CONCEPTS_HPP

#include <coroutine>
#include <concepts>
#include <utility>

#include <boost/asio/error.hpp>
#include <boost/asio/is_executor.hpp>
#include <boost/asio/execution/executor.hpp>
#include <boost/system/system_error.hpp>
#include <boost/throw_exception.hpp>

namespace boost::cobalt
{

// tag::outline[]
template<typename Awaitable, typename Promise = void>
concept awaitable_type = requires (Awaitable aw, std::coroutine_handle<Promise> h)
{
    {aw.await_ready()} -> std::convertible_to<bool>;
    {aw.await_suspend(h)};
    {aw.await_resume()};
};

template<typename Awaitable, typename Promise = void>
concept awaitable =
        awaitable_type<Awaitable, Promise>
    || requires (Awaitable && aw) { {std::forward<Awaitable>(aw).operator co_await()} -> awaitable_type<Promise>;}
    || requires (Awaitable && aw) { {operator co_await(std::forward<Awaitable>(aw))} -> awaitable_type<Promise>;};
//end::outline[]

struct promise_throw_if_cancelled_base;
template<typename Promise = void>
struct enable_awaitables
{
    template<awaitable<Promise> Aw>
    Aw && await_transform(Aw && aw,
                          const boost::source_location & loc = BOOST_CURRENT_LOCATION)
    {
        if constexpr (std::derived_from<Promise, promise_throw_if_cancelled_base>)
        {
          auto p = static_cast<Promise*>(this);
          // a promise inheriting promise_throw_if_cancelled_base needs to also have a .cancelled() function
          if (!!p->cancelled() && p->throw_if_cancelled())
          {
            constexpr boost::source_location here{BOOST_CURRENT_LOCATION};
            boost::throw_exception(system::system_error(
                {asio::error::operation_aborted, &here},
                "throw_if_cancelled"), loc);
          }

        }
        return static_cast<Aw&&>(aw);
    }
};

template <typename T>
concept with_get_executor = requires (T& t)
{
  {t.get_executor()} -> asio::execution::executor;
};


}

#endif //BOOST_COBALT_CONCEPTS_HPP
