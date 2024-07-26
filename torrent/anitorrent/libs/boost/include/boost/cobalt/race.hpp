//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_RACE_HPP
#define BOOST_COBALT_RACE_HPP

#include <boost/cobalt/concepts.hpp>
#include <boost/cobalt/detail/race.hpp>
#include <boost/cobalt/detail/wrapper.hpp>
#include <random>

namespace boost::cobalt
{

namespace detail
{

inline std::default_random_engine &prng()
{
  thread_local static std::default_random_engine g(std::random_device{}());
  return g;
}

}
// tag::concept[]
template<typename G>
  concept uniform_random_bit_generator =
    requires ( G & g)
    {
      {typename std::decay_t<G>::result_type() } -> std::unsigned_integral; // is an unsigned integer type
      // T	Returns the smallest value that G's operator() may return. The value is strictly less than G::max(). The function must be constexpr.
      {std::decay_t<G>::min()} -> std::same_as<typename std::decay_t<G>::result_type>;
      // T	Returns the largest value that G's operator() may return. The value is strictly greater than G::min(). The function must be constexpr.
      {std::decay_t<G>::max()} -> std::same_as<typename std::decay_t<G>::result_type>;
      {g()} -> std::same_as<typename std::decay_t<G>::result_type>;
    } && (std::decay_t<G>::max() > std::decay_t<G>::min());

// end::concept[]


template<asio::cancellation_type Ct = asio::cancellation_type::all,
    uniform_random_bit_generator URBG,
    awaitable<detail::fork::promise_type> ... Promise>
auto race(URBG && g, Promise && ... p) -> detail::race_variadic_impl<Ct, URBG, Promise ...>
{
  return detail::race_variadic_impl<Ct, URBG, Promise ...>(std::forward<URBG>(g), static_cast<Promise&&>(p)...);
}


template<asio::cancellation_type Ct = asio::cancellation_type::all,
    uniform_random_bit_generator URBG,
    typename PromiseRange>
requires awaitable<std::decay_t<decltype(*std::declval<PromiseRange>().begin())>,
    detail::fork::promise_type>
auto race(URBG && g, PromiseRange && p) -> detail::race_ranged_impl<Ct, URBG, PromiseRange>
{
  if (std::empty(p))
    throw_exception(std::invalid_argument("empty range raceed"));

  return detail::race_ranged_impl<Ct, URBG, PromiseRange>{std::forward<URBG>(g), static_cast<PromiseRange&&>(p)};
}

template<asio::cancellation_type Ct = asio::cancellation_type::all,
         awaitable<detail::fork::promise_type> ... Promise>
auto race(Promise && ... p) -> detail::race_variadic_impl<Ct, std::default_random_engine&, Promise ...>
{
  return race<Ct>(detail::prng(), static_cast<Promise&&>(p)...);
}


template<asio::cancellation_type Ct = asio::cancellation_type::all, typename PromiseRange>
  requires awaitable<std::decay_t<decltype(*std::declval<PromiseRange>().begin())>,
      detail::fork::promise_type>
auto race(PromiseRange && p) -> detail::race_ranged_impl<Ct, std::default_random_engine&, PromiseRange>
{
  if (std::empty(p))
    throw_exception(std::invalid_argument("empty range raceed"));

  return race<Ct>(detail::prng(), static_cast<PromiseRange&&>(p));
}

template<asio::cancellation_type Ct = asio::cancellation_type::all,
    awaitable<detail::fork::promise_type> ... Promise>
auto left_race(Promise && ... p) -> detail::race_variadic_impl<Ct, detail::left_race_tag, Promise ...>
{
  return detail::race_variadic_impl<Ct, detail::left_race_tag, Promise ...>(
      detail::left_race_tag{}, static_cast<Promise&&>(p)...);
}


template<asio::cancellation_type Ct = asio::cancellation_type::all, typename PromiseRange>
requires awaitable<std::decay_t<decltype(*std::declval<PromiseRange>().begin())>,
    detail::fork::promise_type>
auto left_race(PromiseRange && p)  -> detail::race_ranged_impl<Ct, detail::left_race_tag, PromiseRange>
{
  if (std::empty(p))
    throw_exception(std::invalid_argument("empty range left_raceed"));

  return detail::race_ranged_impl<Ct, detail::left_race_tag, PromiseRange>{
      detail::left_race_tag{}, static_cast<PromiseRange&&>(p)};
}




}

#endif //BOOST_COBALT_RACE_HPP
