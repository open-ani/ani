//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_JOIN_HPP
#define BOOST_COBALT_JOIN_HPP

#include <boost/cobalt/concepts.hpp>
#include <boost/cobalt/detail/join.hpp>

namespace boost::cobalt
{


template<awaitable ... Promise>
auto join(Promise && ... p)
{
  return detail::join_variadic_impl<Promise ...>(
      static_cast<Promise&&>(p)...);
}


template<typename PromiseRange>
  requires awaitable<std::decay_t<decltype(*std::declval<PromiseRange>().begin())>>
auto join(PromiseRange && p)
{
  return detail::join_ranged_impl<PromiseRange>{static_cast<PromiseRange&&>(p)};
}



}


#endif //BOOST_COBALT_JOIN_HPP
