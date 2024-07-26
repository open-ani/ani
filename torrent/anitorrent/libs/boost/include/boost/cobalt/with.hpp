//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_WITH_HPP
#define BOOST_COBALT_WITH_HPP

#include <exception>
#include <utility>
#include <boost/cobalt/detail/util.hpp>
#include <boost/cobalt/detail/await_result_helper.hpp>
#include <boost/cobalt/detail/with.hpp>


namespace boost::cobalt
{

namespace detail
{

template<typename T>
auto invoke_await_exit(T && t, std::exception_ptr & e)
{
  return std::forward<T>(t).await_exit(e);
}

}


template<typename Arg, typename Func, typename Teardown>
    requires (requires (Func func, Arg & arg, Teardown & teardown, std::exception_ptr ep)
    {
        {std::move(func)(arg)} -> awaitable<detail::with_impl<void>::promise_type>;
        {std::move(teardown)(std::move(arg), ep)} -> awaitable<detail::with_impl<void>::promise_type>;
        {std::declval<detail::co_await_result_t<decltype(std::move(func)(arg))>>()} -> std::same_as<void>;
    })
auto with(Arg arg, Func func, Teardown teardown) -> detail::with_impl<void>
{
    std::exception_ptr e;
    try
    {
        co_await std::move(func)(arg);
    }
    catch (...)
    {
        e = std::current_exception();
    }

    try
    {
        co_await std::move(teardown)(std::move(arg), e);
    }
    catch (...)
    {
        if (!e)
            e = std::current_exception();
    }
    if (e)
        std::rethrow_exception(e);
}


template<typename Arg, typename Func, typename Teardown>
    requires (requires (Func func, Arg & arg, Teardown & teardown, std::exception_ptr e)
    {
        {std::move(teardown)(std::move(arg), e)} -> awaitable<detail::with_impl<void>::promise_type>;
        {std::move(func)(arg)} -> std::same_as<void>;
    }
    && (!requires (Func func, Arg & arg)
    {
        {std::move(func)(arg)} -> awaitable<detail::with_impl<void>::promise_type>;
    }))
auto with(Arg arg, Func func, Teardown teardown) -> detail::with_impl<void>
{
    std::exception_ptr e;
    try
    {
        std::move(func)(arg);
    }
    catch (...)
    {
        e = std::current_exception();
    }

    try
    {
        co_await std::move(teardown)(arg, e);
    }
    catch (...)
    {
        if (!e)
            e = std::current_exception();
    }
    if (e)
        std::rethrow_exception(e);
}


template<typename Arg, typename Func, typename Teardown>
    requires (requires (Func func, Arg & arg, Teardown & teardown, std::exception_ptr ep)
    {
        {std::move(func)(arg)} -> awaitable<detail::with_impl<void>::promise_type>;
        {std::move(teardown)(std::move(arg), ep)} -> awaitable<detail::with_impl<void>::promise_type>;
        {std::declval<detail::co_await_result_t<decltype(std::move(func)(arg))>>()} -> std::move_constructible;
    })
auto with(Arg arg, Func func, Teardown teardown)
    -> detail::with_impl<detail::co_await_result_t<decltype(std::move(func)(arg))>>
{
    std::exception_ptr e;
    std::optional<detail::co_await_result_t<decltype(std::move(func)(arg))>> res;

    try
    {
        res = co_await std::move(func)(arg);
    }
    catch (...)
    {
        e = std::current_exception();
    }

    try
    {
        co_await std::move(teardown)(std::move(arg), e);
    }
    catch (...)
    {
        if (!e)
            e = std::current_exception();
    }
    if (e)
        std::rethrow_exception(e);
    co_return std::move(res);
}


template<typename Arg, typename Func, typename Teardown>
    requires (requires (Func func, Arg & arg, Teardown & teardown, std::exception_ptr e)
    {
        {std::move(teardown)(std::move(arg), e)} -> awaitable<detail::with_impl<void>::promise_type>;
        {std::move(func)(arg)} -> std::move_constructible;
    }
    && (!requires (Func func, Arg & arg)
    {
        {std::move(func)(arg)} -> awaitable<detail::with_impl<void>::promise_type>;
    }))
auto with(Arg arg, Func func, Teardown teardown) -> detail::with_impl<decltype(std::move(func)(arg))>
{
    std::exception_ptr e;
    std::optional<decltype(std::move(func)(arg))> res;
    try
    {
      res = std::move(func)(arg);
    }
    catch (...)
    {
        e = std::current_exception();
    }

    try
    {
        co_await std::move(teardown)(arg, e);
    }
    catch (...)
    {
        if (!e)
            e = std::current_exception();
    }
    if (e)
        std::rethrow_exception(e);

    co_return std::move(res);
}



template<typename Arg, typename Func>
  requires requires  (Arg args, std::exception_ptr ep)
  {
    {std::move(args).await_exit(ep)} -> awaitable<detail::with_impl<void>::promise_type>;
  }
auto with(Arg && arg, Func && func)
{
  return with(std::forward<Arg>(arg), std::forward<Func>(func), &detail::invoke_await_exit<Arg>);
}

}


#endif //BOOST_COBALT_WITH_HPP
