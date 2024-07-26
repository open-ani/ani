// Copyright (C) 2022 T. Zachary Laine
//
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_STL_INTERFACES_DETAIL_PIPEABLE_VIEW_HPP
#define BOOST_STL_INTERFACES_DETAIL_PIPEABLE_VIEW_HPP

#include <boost/stl_interfaces/config.hpp>

#include <type_traits>


namespace boost { namespace stl_interfaces { namespace detail {

    template<typename T>
    using remove_cvref_t = std::remove_cv_t<std::remove_reference_t<T>>;

    struct pipeable_base;

#if BOOST_STL_INTERFACES_USE_CONCEPTS
    template<typename T>
    concept pipeable_ = std::derived_from<T, pipeable_base> &&
        std::is_object_v<T> && std::copy_constructible<T>;
#else
    template<typename T>
    constexpr bool pipeable_ = std::is_base_of<pipeable_base, T>::value &&
        std::is_object<T>::value && std::is_copy_constructible<T>::value;
#endif

#if BOOST_STL_INTERFACES_USE_CONCEPTS
    template<pipeable_ T, pipeable_ U>
#else
    template<
        typename T,
        typename U,
        typename Enable = std::enable_if_t<pipeable_<T> && pipeable_<U>>>
#endif
    struct view_pipeline;

    struct pipeable_base
    {
#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<pipeable_ T, pipeable_ U>
        requires std::constructible_from<std::remove_cvref_t<T>, T> &&
            std::constructible_from<std::remove_cvref_t<U>, U>
#else
        template<
            typename T,
            typename U,
            typename Enable = std::enable_if_t<
                pipeable_<T> && pipeable_<U> &&
                std::is_constructible<remove_cvref_t<T>, T>::value &&
                std::is_constructible<remove_cvref_t<U>, U>::value>>
#endif
        friend constexpr auto operator|(T && t, U && u)
        {
            return view_pipeline<T, U>{(T &&) t, (U &&) u};
        }
    };

    template<typename Derived>
    struct pipeable : pipeable_base
    {
        template<typename R>
        friend constexpr auto operator|(R && r, Derived & d)
            -> decltype(((Derived &&) d)((R &&) r))
        {
            return ((Derived &&) d)((R &&) r);
        }

        template<typename R>
        friend constexpr auto operator|(R && r, Derived const & d)
            -> decltype(((Derived &&) d)((R &&) r))
        {
            return ((Derived &&) d)((R &&) r);
        }

        template<typename R>
        friend constexpr auto operator|(R && r, Derived && d)
            -> decltype(((Derived &&) d)((R &&) r))
        {
            return ((Derived &&) d)((R &&) r);
        }
    };

#if BOOST_STL_INTERFACES_USE_CONCEPTS
    template<pipeable_ T, pipeable_ U>
#else
    template<typename T, typename U, typename>
#endif
    struct view_pipeline : pipeable<view_pipeline<T, U>>
    {
        view_pipeline() = default;

        constexpr view_pipeline(T && t, U && u) :
            left_(std::move(t)), right_(std::move(u))
        {}

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<std::ranges::viewable_range R>
        requires std::invocable<T &, R> &&
            std::invocable<U &, std::invoke_result_t<T &, R>>
        constexpr decltype(auto) operator()(R && r) &
#else
        template<typename R>
        constexpr auto
        operator()(R && r) & -> decltype(this->right_(this->left_((R &&) r)))
#endif
        {
            return right_(left_((R &&) r));
        }

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<std::ranges::viewable_range R>
        requires std::invocable<T const &, R> &&
            std::invocable<U const &, std::invoke_result_t<T const &, R>>
        constexpr decltype(auto) operator()(R && r) const &
#else
        template<typename R>
        constexpr auto operator()(
            R && r) const & -> decltype(this->right_(this->left_((R &&) r)))
#endif
        {
            return right_(left_((R &&) r));
        }

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<std::ranges::viewable_range R>
        requires std::invocable<T, R> &&
            std::invocable<U, std::invoke_result_t<T, R>>
        constexpr decltype(auto) operator()(R && r) &&
#else
        template<typename R>
        constexpr auto operator()(R && r) && -> decltype(std::move(
            this->right_)(std::move(this->left_)((R &&) r)))
#endif
        {
            return std::move(right_)(std::move(left_)((R &&) r));
        }

        T left_;
        U right_;
    };

}}}

#endif
