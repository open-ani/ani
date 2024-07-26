// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_WRAPPER_HPP
#define BOOST_COBALT_WRAPPER_HPP

#include <boost/cobalt/this_coro.hpp>
#include <boost/cobalt/concepts.hpp>
#include <boost/cobalt/detail/util.hpp>

#include <boost/asio/bind_executor.hpp>
#include <boost/asio/executor.hpp>
#include <boost/asio/post.hpp>
#include <boost/config.hpp>

#include <coroutine>
#include <utility>
#if BOOST_COBALT_NO_SELF_DELETE
#include <boost/asio/consign.hpp>
#endif
namespace boost::cobalt::detail
{

template<typename Allocator>
struct partial_promise_base
{
    template<typename CompletionToken>
    void * operator new(const std::size_t size, CompletionToken & token)
    {
      // gcc: 168 40
      // clang: 144 40
      return allocate_coroutine(size, asio::get_associated_allocator(token));
    }

    template<typename Executor, typename CompletionToken>
    void * operator new(const std::size_t size, Executor &, CompletionToken & token)
    {
      // gcc: 120 8 16
      // clang: 96 8 16
      return allocate_coroutine(size, asio::get_associated_allocator(token));
    }

    void operator delete(void * raw, const std::size_t size)
    {
      deallocate_coroutine<Allocator>(raw, size);
    }

};

template<>
struct partial_promise_base<std::allocator<void>> {};


template<>           struct partial_promise_base<void> {};
template<typename T> struct partial_promise_base<std::allocator<T>> {};

// alloc options are two: allocator or aligned storage
template<typename Allocator = void>
struct partial_promise : partial_promise_base<Allocator>
{
    auto initial_suspend() noexcept
    {
        return std::suspend_always();
    }

    auto final_suspend() noexcept
    {
        return std::suspend_never();
    }

    void return_void() {}
};

template<typename Allocator = void>
struct post_coroutine_promise : partial_promise<Allocator>
{
    template<typename CompletionToken>
    auto yield_value(CompletionToken cpl)
    {
        struct awaitable_t
        {
            CompletionToken cpl;
            constexpr bool await_ready() noexcept { return false; }
            BOOST_NOINLINE
            auto await_suspend(std::coroutine_handle<void> h) noexcept
            {
                auto c = std::move(cpl);
                if (this_thread::has_executor())
                  detail::self_destroy(h, asio::get_associated_executor(c, this_thread::get_executor()));
                else
                  detail::self_destroy(h, asio::get_associated_executor(c));

                asio::post(std::move(c));
            }

            constexpr void await_resume() noexcept {}
        };
        return awaitable_t{std::move(cpl)};
    }

    std::coroutine_handle<post_coroutine_promise<Allocator>> get_return_object()
    {
        return std::coroutine_handle<post_coroutine_promise<Allocator>>::from_promise(*this);
    }

    void unhandled_exception()
    {
        detail::self_destroy(std::coroutine_handle<post_coroutine_promise<Allocator>>::from_promise(*this));
        throw;
    }
};


}

namespace std
{

template <typename T, typename ... Args>
struct coroutine_traits<coroutine_handle<boost::cobalt::detail::post_coroutine_promise<T>>, Args...>
{
    using promise_type = boost::cobalt::detail::post_coroutine_promise<T>;
};

} // namespace std


namespace boost::cobalt::detail
{


template <typename CompletionToken>
auto post_coroutine(CompletionToken token)
    -> std::coroutine_handle<post_coroutine_promise<asio::associated_allocator_t<CompletionToken>>>
{
    co_yield std::move(token);
}

template <asio::execution::executor Executor, typename CompletionToken>
auto post_coroutine(Executor exec, CompletionToken token)
    -> std::coroutine_handle<post_coroutine_promise<asio::associated_allocator_t<CompletionToken>>>
{
    co_yield asio::bind_executor(exec, std::move(token));
}

template <with_get_executor Context, typename CompletionToken>
auto post_coroutine(Context &ctx, CompletionToken token)
    -> std::coroutine_handle<post_coroutine_promise<asio::associated_allocator_t<CompletionToken>>>
{
    co_yield asio::bind_executor(ctx.get_executor(), std::move(token));
}

}

#endif //BOOST_COBALT_WRAPPER_HPP
