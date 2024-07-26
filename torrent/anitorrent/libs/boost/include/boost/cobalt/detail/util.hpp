// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_UTIL_HPP
#define BOOST_COBALT_UTIL_HPP

#include <boost/cobalt/config.hpp>
#include <boost/cobalt/this_thread.hpp>

#include <boost/system/result.hpp>
#include <boost/variant2/variant.hpp>

#include <limits>
#include <type_traits>
#include <coroutine>

namespace boost::variant2
{
struct monostate;
}

namespace boost::cobalt::detail
{

template<typename T>
constexpr std::size_t variadic_first(std::size_t = 0u)
{
    return std::numeric_limits<std::size_t>::max();
}


template<typename T, typename First, typename ... Args>
constexpr std::size_t variadic_first(std::size_t pos = 0u)
{
    if constexpr (std::is_same_v<std::decay_t<First>, T>)
        return pos;
    else
        return variadic_first<T, Args...>(pos+1);
}


template<typename T, typename ... Args>
constexpr bool variadic_has = variadic_first<T, Args...>() < sizeof...(Args);

template<std::size_t Idx, typename First, typename ... Args>
    requires (Idx <= sizeof...(Args))
constexpr decltype(auto) get_variadic(First && first, Args  && ... args)
{
    if constexpr (Idx == 0u)
        return static_cast<First>(first);
    else
        return get_variadic<Idx-1u>(static_cast<Args>(args)...);
}

template<std::size_t Idx, typename ... Args>
struct variadic_element;

template<std::size_t Idx, typename First, typename ...Tail>
struct variadic_element<Idx, First, Tail...>
{
    using type = typename variadic_element<Idx-1, Tail...>::type;
};

template<typename First, typename ...Tail>
struct variadic_element<0u, First, Tail...>
{
    using type = First;
};

template<std::size_t Idx, typename ... Args>
using variadic_element_t = typename variadic_element<Idx, Args...>::type;


template<typename ... Args>
struct variadic_last
{
    using type = variadic_element_t<sizeof...(Args) - 1, Args...>;
};

template<>
struct variadic_last<>
{
    using type = void;
};

template<typename ... Args>
using variadic_last_t = typename variadic_last<Args...>::type;


template<typename First>
constexpr decltype(auto) get_last_variadic(First && first)
{
    return first;
}

template<typename First, typename ... Args>
constexpr decltype(auto) get_last_variadic(First &&, Args  && ... args)
{
    return get_last_variadic(static_cast<Args>(args)...);
}

template<typename Awaitable>
auto get_resume_result(Awaitable & aw) -> system::result<decltype(aw.await_resume()), std::exception_ptr>
{
  using type = decltype(aw.await_resume());
  try
  {
    if constexpr (std::is_void_v<type>)
    {
      aw.await_resume();
      return {};
    }
    else
      return aw.await_resume();
  }
  catch(...)
  {
    return std::current_exception();
  }
}

#if BOOST_COBALT_NO_SELF_DELETE

BOOST_COBALT_DECL
void self_destroy(std::coroutine_handle<void> h, const cobalt::executor & exec) noexcept;

template<typename T>
inline void self_destroy(std::coroutine_handle<T> h) noexcept
{
  if constexpr (requires {h.promise().get_executor();})
    self_destroy(h, h.promise().get_executor());
  else
    self_destroy(h, this_thread::get_executor());
}

#else

template<typename T>
inline void self_destroy(std::coroutine_handle<T> h) noexcept
{
  h.destroy();
}

template<typename T, typename Executor>
inline void self_destroy(std::coroutine_handle<T> h, const Executor &) noexcept
{
  h.destroy();
}

#endif

template<typename T>
using void_as_monostate = std::conditional_t<std::is_void_v<T>, variant2::monostate, T>;

template<typename T>
using monostate_as_void = std::conditional_t<std::is_same_v<T, variant2::monostate>, void, T>;


}

#endif //BOOST_COBALT_UTIL_HPP
