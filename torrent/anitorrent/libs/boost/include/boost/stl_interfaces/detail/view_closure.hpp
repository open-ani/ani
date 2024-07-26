// Copyright (C) 2022 T. Zachary Laine
//
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_STL_INTERFACES_DETAIL_VIEW_CLOSURE_HPP
#define BOOST_STL_INTERFACES_DETAIL_VIEW_CLOSURE_HPP

#include <boost/stl_interfaces/detail/pipeable_view.hpp>

#include <utility>


namespace boost { namespace stl_interfaces { namespace detail {

    template<std::size_t I, typename T>
    struct box
    {
        T value_;
    };

    template<typename Indices, typename Func, typename... T>
    struct view_closure_impl;

    template<std::size_t... I, typename Func, typename... T>
    struct view_closure_impl<std::index_sequence<I...>, Func, T...>
        : box<I, T>...
    {
        view_closure_impl() = default;
        constexpr explicit view_closure_impl(Func, T &&... x) :
            box<I, T>{std::move(x)}...
        {}

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<std::ranges::input_range R>
        requires std::ranges::viewable_range<R> &&
            std::invocable<Func, R, T &...> &&
            std::ranges::view<std::invoke_result_t<Func, R, T &...>>
        constexpr auto operator()(R && r) &
#else
        template<typename R>
        constexpr auto operator()(R && r) & -> decltype(
            Func{}((R &&) r, std::declval<box<I, T> &>().value_...))
#endif
        {
            return Func{}((R &&) r, static_cast<box<I, T> &>(*this).value_...);
        }

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<std::ranges::input_range R>
        requires std::ranges::viewable_range<R> &&
            std::invocable<Func, R, T const &...> &&
            std::ranges::view<std::invoke_result_t<Func, R, T const &...>>
        constexpr auto operator()(R && r) const &
#else
        template<typename R>
        constexpr auto operator()(R && r) const & -> decltype(
            Func{}((R &&) r, std::declval<box<I, T> const &>().value_...))
#endif
        {
            return Func{}(
                (R &&) r, static_cast<box<I, T> const &>(*this).value_...);
        }

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<std::ranges::input_range R>
        requires std::ranges::viewable_range<R> &&
            std::invocable<Func, R, T...> &&
            std::ranges::view<std::invoke_result_t<Func, R, T...>>
        constexpr auto operator()(R && r) &&
#else
        template<typename R>
        constexpr auto operator()(R && r) && -> decltype(
            Func{}((R &&) r, std::declval<box<I, T> &&>().value_...))
#endif
        {
            return Func{}((R &&) r, static_cast<box<I, T> &&>(*this).value_...);
        }
    };

#if BOOST_STL_INTERFACES_USE_CONCEPTS
    template<std::semiregular Func, std::copy_constructible... T>
#else
    template<typename Func, typename... T>
#endif
    struct view_closure
        : pipeable<view_closure<Func, T...>>,
          view_closure_impl<std::index_sequence_for<T...>, Func, T...>
    {
        using base_type =
            view_closure_impl<std::index_sequence_for<T...>, Func, T...>;

        view_closure() = default;

        constexpr explicit view_closure(Func func, T &&... x) :
            base_type{func, std::move(x)...}
        {}
    };

#if defined(__cpp_deduction_guides)
    template<typename Func, typename... T>
    view_closure(Func, T...) -> view_closure<Func, T...>;
#endif

}}}

#endif
