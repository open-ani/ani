// Copyright (C) 2022 T. Zachary Laine
//
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_STL_INTERFACES_VIEW_ADAPTOR_HPP
#define BOOST_STL_INTERFACES_VIEW_ADAPTOR_HPP

#include <boost/stl_interfaces/config.hpp>
#include <boost/stl_interfaces/detail/view_closure.hpp>

#include <boost/type_traits/is_detected.hpp>

#include <tuple>
#include <type_traits>


#if !defined(BOOST_STL_INTERFACES_DOXYGEN)

#if defined(__cpp_lib_ranges) && 202202L <= __cpp_lib_ranges
#define BOOST_STL_INTERFACES_USE_CPP23_STD_RANGE_ADAPTOR_CLOSURE 1
#else
#define BOOST_STL_INTERFACES_USE_CPP23_STD_RANGE_ADAPTOR_CLOSURE 0
#endif

#if !BOOST_STL_INTERFACES_USE_CPP23_STD_RANGE_ADAPTOR_CLOSURE &&               \
    BOOST_STL_INTERFACES_USE_CONCEPTS && defined(__GNUC__) && 12 <= __GNUC__
#define BOOST_STL_INTERFACES_USE_LIBSTDCPP_GCC12_RANGE_ADAPTOR_CLOSURE 1
#else
#define BOOST_STL_INTERFACES_USE_LIBSTDCPP_GCC12_RANGE_ADAPTOR_CLOSURE 0
#endif

#if !BOOST_STL_INTERFACES_USE_CPP23_STD_RANGE_ADAPTOR_CLOSURE &&               \
    defined(_MSC_VER) && _MSC_VER <= 1929
#define BOOST_STL_INTERFACES_NEED_VS_COMPATIBLE_RANGE_ADAPTOR_CLOSURE 1
#else
#define BOOST_STL_INTERFACES_NEED_VS_COMPATIBLE_RANGE_ADAPTOR_CLOSURE 0
#endif

#if !BOOST_STL_INTERFACES_USE_CPP23_STD_RANGE_ADAPTOR_CLOSURE &&               \
    !BOOST_STL_INTERFACES_USE_LIBSTDCPP_GCC12_RANGE_ADAPTOR_CLOSURE &&         \
    !BOOST_STL_INTERFACES_NEED_VS_COMPATIBLE_RANGE_ADAPTOR_CLOSURE
#define BOOST_STL_INTERFACES_DEFINE_CUSTOM_RANGE_ADAPTOR_CLOSURE 1
#else
#define BOOST_STL_INTERFACES_DEFINE_CUSTOM_RANGE_ADAPTOR_CLOSURE 0
#endif

#endif


namespace boost { namespace stl_interfaces {
    namespace detail {
        template<typename F, typename... Args>
        using invocable_expr =
            decltype(std::declval<F>()(std::declval<Args>()...));
        template<typename F, typename... Args>
        constexpr bool is_invocable_v =
            is_detected_v<invocable_expr, F, Args...>;

        template<typename Func, typename... CapturedArgs>
        struct bind_back_t
        {
            static_assert(std::is_move_constructible<Func>::value, "");
#if defined(__cpp_fold_expressions)
            static_assert(
                (std::is_move_constructible<CapturedArgs>::value && ...), "");
#endif

            template<typename F, typename... Args>
            explicit constexpr bind_back_t(int, F && f, Args &&... args) :
                f_((F &&) f), bound_args_((Args &&) args...)
            {
                static_assert(sizeof...(Args) == sizeof...(CapturedArgs), "");
            }

            template<typename... Args>
            constexpr decltype(auto) operator()(Args &&... args) &
            {
                return call_impl(*this, indices(), (Args &&) args...);
            }

            template<typename... Args>
            constexpr decltype(auto) operator()(Args &&... args) const &
            {
                return call_impl(*this, indices(), (Args &&) args...);
            }

            template<typename... Args>
            constexpr decltype(auto) operator()(Args &&... args) &&
            {
                return call_impl(
                    std::move(*this), indices(), (Args &&) args...);
            }

            template<typename... Args>
            constexpr decltype(auto) operator()(Args &&... args) const &&
            {
                return call_impl(
                    std::move(*this), indices(), (Args &&) args...);
            }

        private:
            using indices = std::index_sequence_for<CapturedArgs...>;

            template<typename T, size_t... I, typename... Args>
            static constexpr decltype(auto)
            call_impl(T && this_, std::index_sequence<I...>, Args &&... args)
            {
                return ((T &&) this_)
                    .f_((Args &&) args...,
                        std::get<I>(((T &&) this_).bound_args_)...);
            }

            Func f_;
            std::tuple<CapturedArgs...> bound_args_;
        };

        template<typename Func, typename... Args>
        using bind_back_result =
            bind_back_t<std::decay_t<Func>, std::decay_t<Args>...>;
    }

    /** An implementation of `std::bind_back()` from C++23. */
    template<typename Func, typename... Args>
    constexpr auto bind_back(Func && f, Args &&... args)
    {
        return detail::bind_back_result<Func, Args...>(
            0, (Func &&) f, (Args &&) args...);
    }

#if BOOST_STL_INTERFACES_DEFINE_CUSTOM_RANGE_ADAPTOR_CLOSURE ||                \
    defined(BOOST_STL_INTERFACES_DOXYGEN)

    /** A backwards-compatible implementation of C++23's
        `std::ranges::range_adaptor_closure`.  `range_adaptor_closure` may be
        a struct template or may be an alias, as required to maintain
        compatability with the standard library's view adaptors. */
#if BOOST_STL_INTERFACES_USE_CONCEPTS
    template<typename D>
    requires std::is_class_v<D> && std::same_as<D, std::remove_cv_t<D>>
#else
    template<
        typename D,
        typename Enable = std::enable_if_t<
            std::is_class<D>::value &&
            std::is_same<D, std::remove_cv_t<D>>::value>>
#endif
    struct range_adaptor_closure;

    namespace detail {
#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<typename T>
        concept range_adaptor_closure_ = std::derived_from<
            std::remove_cvref_t<T>,
            range_adaptor_closure<std::remove_cvref_t<T>>>;
#else
        template<typename T>
        using range_adaptor_closure_tag_expr = typename range_adaptor_closure<
            T>::inheritance_tag_with_an_unlikely_name_;
        template<typename T>
        constexpr bool range_adaptor_closure_ =
            is_detected_v<range_adaptor_closure_tag_expr, remove_cvref_t<T>>;
#endif
    }

#endif

#if BOOST_STL_INTERFACES_USE_CPP23_STD_RANGE_ADAPTOR_CLOSURE

    template<typename D>
    using range_adaptor_closure = std::ranges::range_adaptor_closure<D>;

#elif BOOST_STL_INTERFACES_USE_LIBSTDCPP_GCC12_RANGE_ADAPTOR_CLOSURE

    template<typename D>
    using range_adaptor_closure = std::views::__adaptor::_RangeAdaptorClosure;

#elif BOOST_STL_INTERFACES_NEED_VS_COMPATIBLE_RANGE_ADAPTOR_CLOSURE

    template<typename D>
    using range_adaptor_closure = detail::pipeable<D>;

#else

#if BOOST_STL_INTERFACES_USE_CONCEPTS
    template<typename D>
    requires std::is_class_v<D> && std::same_as<D, std::remove_cv_t<D>>
#else
    template<typename D, typename>
#endif
    struct range_adaptor_closure
    {
#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<typename T>
        requires std::invocable<D, T>
#else
        template<
            typename T,
            typename Enable = std::enable_if_t<detail::is_invocable_v<D, T>>>
#endif
        [[nodiscard]] friend constexpr decltype(auto) operator|(T && t, D && d)
        {
            return std::move(d)((T &&) t);
        }

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<typename T>
        requires std::invocable<D const &, T>
#else
        template<
            typename T,
            typename Enable =
                std::enable_if_t<detail::is_invocable_v<D const &, T>>>
#endif
        [[nodiscard]] friend constexpr decltype(auto)
        operator|(T && t, D const & d)
        {
            return d((T &&) t);
        }

        using inheritance_tag_with_an_unlikely_name_ = int;
    };

#endif

    //[closure_defn
    /** An invocable consisting of a contained invocable `f`.  Calling
        `operator()` with some argument `t` calls `f(t)` and returns the
        result.  This type is typically used to capture a the result of a call
        to `bind_back()`. */
    template<typename F>
    struct closure : range_adaptor_closure<closure<F>>
    {
        constexpr closure(F f) : f_(f) {}

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<typename T>
        requires std::invocable<F const &, T>
#else
        template<
            typename T,
            typename Enable =
                std::enable_if_t<detail::is_invocable_v<F const &, T>>>
#endif
        constexpr decltype(auto) operator()(T && t) const &
        {
            return f_((T &&) t);
        }

#if BOOST_STL_INTERFACES_USE_CONCEPTS
        template<typename T>
        requires std::invocable<F &&, T>
#else
        template<
            typename T,
            typename Enable = std::enable_if_t<detail::is_invocable_v<F &&, T>>>
#endif
        constexpr decltype(auto) operator()(T && t) &&
        {
            return std::move(f_)((T &&) t);
        }

    private:
        F f_;
    };
    //]

    namespace detail {
#if !BOOST_STL_INTERFACES_USE_CONCEPTS
        template<typename F, bool Invocable, typename... Args>
        struct adaptor_impl
        {
            static constexpr decltype(auto) call(F const & f, Args &&... args)
            {
                return f((Args &&) args...);
            }
        };

        template<typename F, typename... Args>
        struct adaptor_impl<F, false, Args...>
        {
            static constexpr auto call(F const & f, Args &&... args)
            {
                using closure_func =
                    std::decay_t<decltype(stl_interfaces::bind_back(
                        f, (Args &&) args...))>;
                return closure<closure_func>(
                    stl_interfaces::bind_back(f, (Args &&) args...));
            }
        };
#endif
    }

    //[adaptor_defn
    /** Adapts an invocable `f` as a view adaptor.  Calling
        `operator(args...)` will either: call `f(args...)` and return the
        result, if `f(args...)` is well-formed; or return
        `closure(stl_interfaces::bind_back(f, args...))` otherwise. */
    template<typename F>
    struct adaptor
    {
        constexpr adaptor(F f) : f_(f) {}

        // clang-format off
        template<typename... Args>
        constexpr auto operator()(Args &&... args) const
        // clang-format on
        {
#if BOOST_STL_INTERFACES_USE_CONCEPTS
            if constexpr (std::is_invocable_v<F const &, Args...>) {
                return f_((Args &&) args...);
            } else {
                return closure(
                    stl_interfaces::bind_back(f_, (Args &&) args...));
            }
#else
            return detail::adaptor_impl<
                F const &,
                detail::is_invocable_v<F const &, Args...>,
                Args...>::call(f_, (Args &&) args...);
#endif
        }

    private:
        F f_;
    };
    //]

}}

#endif
