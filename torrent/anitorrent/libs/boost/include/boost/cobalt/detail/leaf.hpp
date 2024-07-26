// Copyright (c) 2023 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_DETAIL_LEAF_HPP
#define BOOST_COBALT_DETAIL_LEAF_HPP

#include <boost/cobalt/detail/await_result_helper.hpp>
#include <boost/leaf/config.hpp>
#include <boost/leaf/handle_errors.hpp>

namespace boost::cobalt::detail
{

template<typename Awaitable, typename ... H>
struct [[nodiscard]] try_catch_awaitable
{
  Awaitable aw;
  std::tuple<H...> handler;

  bool await_ready() {return aw.await_ready(); }
  template<typename Promise>
  auto await_suspend(std::coroutine_handle<Promise> h) {return aw.await_suspend(h);}

  auto await_resume()
  {
    return std::apply(
        [this](auto && ... h)
        {
          return leaf::try_catch(
              [this]{return std::move(aw).await_resume();},
              std::move(h)...);
        }, std::move(handler));
  }
};

template<typename Awaitable, typename ... H>
struct [[nodiscard]] try_handle_all_awaitable
{
  Awaitable aw;
  std::tuple<H...> handler;

  bool await_ready() {return aw.await_ready(); }
  template<typename Promise>
  auto await_suspend(std::coroutine_handle<Promise> h) {return aw.await_suspend(h);}

  auto await_resume()
  {
    return std::apply(
        [this](auto && ... h)
        {
          return leaf::try_handle_all(
              [this]{return std::move(aw).await_resume();},
              std::move(h)...);
        }, std::move(handler));
  }
};



template<typename Awaitable, typename ... H>
struct [[nodiscard]] try_handle_some_awaitable
{
  Awaitable aw;
  std::tuple<H...> handler;

  bool await_ready() {return aw.await_ready(); }
  template<typename Promise>
  auto await_suspend(std::coroutine_handle<Promise> h) {return aw.await_suspend(h);}

  auto await_resume()
  {
    return std::apply(
        [this](auto && ... h)
        {
          return leaf::try_handle_some(
              [this]{return std::move(aw).await_resume();},
              std::move(h)...);
        }, std::move(handler));
  }
};

}

#endif //BOOST_COBALT_DETAIL_LEAF_HPP
