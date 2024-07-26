// Copyright (c) 2023 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_LEAF_HPP
#define BOOST_COBALT_LEAF_HPP

#include <boost/cobalt/detail/leaf.hpp>

#include <boost/leaf/config.hpp>
#include <boost/leaf/handle_errors.hpp>

namespace boost::cobalt
{

template<awaitable TryAwaitable, typename ... H >
auto try_catch(TryAwaitable && try_coro, H && ... h )
{
  return detail::try_catch_awaitable<
        std::decay_t<decltype(detail::get_awaitable_type(std::forward<TryAwaitable>(try_coro)))>, H...>
        {
          detail::get_awaitable_type(std::forward<TryAwaitable>(try_coro)),
          {std::forward<H>(h)...}
        };
}

template<awaitable TryAwaitable, typename ... H >
auto try_handle_all(TryAwaitable && try_coro, H && ... h )
{
  return detail::try_handle_all_awaitable<
      std::decay_t<decltype(detail::get_awaitable_type(std::forward<TryAwaitable>(try_coro)))>, H...>
      {
          detail::get_awaitable_type(std::forward<TryAwaitable>(try_coro)),
          {std::forward<H>(h)...}
      };
}

template<awaitable TryAwaitable, typename ... H >
auto try_handle_some(TryAwaitable && try_coro, H && ... h )
{
  return detail::try_handle_some_awaitable<
      std::decay_t<decltype(detail::get_awaitable_type(std::forward<TryAwaitable>(try_coro)))>, H...>
      {
          detail::get_awaitable_type(std::forward<TryAwaitable>(try_coro)),
          {std::forward<H>(h)...}
      };
}

}

#endif //BOOST_COBALT_LEAF_HPP
