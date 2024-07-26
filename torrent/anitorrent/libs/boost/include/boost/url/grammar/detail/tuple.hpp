//
// Copyright (c) 2016-2019 Damian Jarek (damian dot jarek93 at gmail dot com)
// Copyright (c) 2022 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_URL_GRAMMAR_DETAIL_TUPLE_HPP
#define BOOST_URL_GRAMMAR_DETAIL_TUPLE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/core/empty_value.hpp>
#include <boost/mp11/algorithm.hpp>
#include <boost/mp11/function.hpp>
#include <boost/mp11/integer_sequence.hpp>
#include <boost/type_traits/remove_cv.hpp>
#include <boost/type_traits/copy_cv.hpp>
#include <cstdlib>
#include <utility>

#ifndef BOOST_URL_TUPLE_EBO
// VFALCO No idea what causes it or how to fix it
// https://devblogs.microsoft.com/cppblog/optimizing-the-layout-of-empty-base-classes-in-vs2015-update-2-3/
#ifdef BOOST_MSVC
#define BOOST_URL_TUPLE_EBO 0
#else
#define BOOST_URL_TUPLE_EBO 1
#endif
#endif

namespace boost {
namespace urls {
namespace grammar {
namespace detail {

#if BOOST_URL_TUPLE_EBO
template<std::size_t I, class T>
struct tuple_element_impl
    : empty_value<T>
{
    constexpr
    tuple_element_impl(T const& t)
        : empty_value<T>(
            empty_init, t)
    {
    }

    constexpr
    tuple_element_impl(T&& t)
        : empty_value<T>(
            empty_init,
                std::move(t))
    {
    }
};
#else
template<std::size_t I, class T>
struct tuple_element_impl
{
    T t_;

    constexpr
    tuple_element_impl(T const& t)
        : t_(t)
    {
    }

    constexpr
    tuple_element_impl(T&& t)
        : t_(std::move(t))
    {
    }

    constexpr
    T&
    get() noexcept
    {
        return t_;
    }

    constexpr
    T const&
    get() const noexcept
    {
        return t_;
    }
};
#endif

template<std::size_t I, class T>
struct tuple_element_impl<I, T&>
{
    T& t;

    constexpr
    tuple_element_impl(T& t_)
        : t(t_)
    {
    }

    T&
    get() const noexcept
    {
        return t;
    }
};

template<class... Ts>
struct tuple_impl;

template<class... Ts, std::size_t... Is>
struct tuple_impl<
    mp11::index_sequence<Is...>, Ts...>
  : tuple_element_impl<Is, Ts>...
{
    template<class... Us>
    constexpr
    explicit
    tuple_impl(Us&&... us)
        : tuple_element_impl<Is, Ts>(
            std::forward<Us>(us))...
    {
    }
};

template<class... Ts>
struct tuple
    : tuple_impl<
        mp11::index_sequence_for<Ts...>, Ts...>
{
    template<class... Us,
        typename std::enable_if<
            mp11::mp_bool<
                mp11::mp_all<std::is_constructible<
                    Ts, Us>...>::value &&
                ! mp11::mp_all<std::is_convertible<
                    Us, Ts>...>::value>::value,
            int>::type = 0
    >
    constexpr
    explicit
    tuple(Us&&... us) noexcept
      : tuple_impl<mp11::index_sequence_for<
            Ts...>, Ts...>{std::forward<Us>(us)...}
    {
    }

    template<class... Us,
        typename std::enable_if<
            mp11::mp_all<std::is_convertible<
                Us, Ts>...>::value,
            int>::type = 0
    >
    constexpr
    tuple(Us&&... us) noexcept
      : tuple_impl<mp11::index_sequence_for<
            Ts...>, Ts...>{std::forward<Us>(us)...}
    {
    }
};

//------------------------------------------------

template<std::size_t I, class T>
constexpr
T&
get(tuple_element_impl<I, T>& te)
{
    return te.get();
}

template<std::size_t I, class T>
constexpr
T const&
get(tuple_element_impl<I, T> const& te)
{
    return te.get();
}

template<std::size_t I, class T>
constexpr
T&&
get(tuple_element_impl<I, T>&& te)
{
    return std::move(te.get());
}

template<std::size_t I, class T>
constexpr
T&
get(tuple_element_impl<I, T&>&& te)
{
    return te.get();
}

template<std::size_t I, class T>
using tuple_element =
    typename boost::copy_cv<
        mp11::mp_at_c<typename
            remove_cv<T>::type,
            I>, T>::type;

} // detail
} // grammar
} // urls
} // boost

#endif
