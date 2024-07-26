#ifndef BOOST_LEAF_CONTEXT_HPP_INCLUDED
#define BOOST_LEAF_CONTEXT_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/leaf/config.hpp>
#include <boost/leaf/error.hpp>

#if !defined(BOOST_LEAF_NO_THREADS) && !defined(NDEBUG)
#   include <thread>
#endif

namespace boost { namespace leaf {

class error_info;
class diagnostic_info;
class verbose_diagnostic_info;

template <class>
struct is_predicate: std::false_type
{
};

namespace leaf_detail
{
    template <class T>
    struct is_exception: std::is_base_of<std::exception, typename std::decay<T>::type>
    {
    };

    template <class E>
    struct handler_argument_traits;

    template <class E, bool IsPredicate = is_predicate<E>::value>
    struct handler_argument_traits_defaults;

    template <class E>
    struct handler_argument_traits_defaults<E, false>
    {
        using error_type = typename std::decay<E>::type;
        constexpr static bool always_available = false;

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static error_type const * check( Tup const &, error_info const & ) noexcept;

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static error_type * check( Tup &, error_info const & ) noexcept;

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static E get( Tup & tup, error_info const & ei ) noexcept
        {
            return *check(tup, ei);
        }

        static_assert(!is_predicate<error_type>::value, "Handlers must take predicate arguments by value");
        static_assert(!std::is_same<E, error_info>::value, "Handlers must take leaf::error_info arguments by const &");
        static_assert(!std::is_same<E, diagnostic_info>::value, "Handlers must take leaf::diagnostic_info arguments by const &");
        static_assert(!std::is_same<E, verbose_diagnostic_info>::value, "Handlers must take leaf::verbose_diagnostic_info arguments by const &");
    };

    template <class Pred>
    struct handler_argument_traits_defaults<Pred, true>: handler_argument_traits<typename Pred::error_type>
    {
        using base = handler_argument_traits<typename Pred::error_type>;
        static_assert(!base::always_available, "Predicates can't use types that are always_available");

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static bool check( Tup const & tup, error_info const & ei ) noexcept
        {
            auto e = base::check(tup, ei);
            return e && Pred::evaluate(*e);
        }

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static Pred get( Tup const & tup, error_info const & ei ) noexcept
        {
            return Pred{*base::check(tup, ei)};
        }
    };

    template <class E>
    struct handler_argument_always_available
    {
        using error_type = E;
        constexpr static bool always_available = true;

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static bool check( Tup &, error_info const & ) noexcept
        {
            return true;
        }
    };

    template <class E>
    struct handler_argument_traits: handler_argument_traits_defaults<E>
    {
    };

    template <>
    struct handler_argument_traits<void>
    {
        using error_type = void;
        constexpr static bool always_available = false;

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static std::exception const * check( Tup const &, error_info const & ) noexcept;
    };

    template <class E>
    struct handler_argument_traits<E &&>
    {
        static_assert(sizeof(E) == 0, "Error handlers may not take rvalue ref arguments");
    };

    template <class E>
    struct handler_argument_traits<E *>: handler_argument_always_available<typename std::remove_const<E>::type>
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR static E * get( Tup & tup, error_info const & ei) noexcept
        {
            return handler_argument_traits_defaults<E>::check(tup, ei);
        }
    };

    template <class E>
    struct handler_argument_traits_require_by_value
    {
        static_assert(sizeof(E) == 0, "Error handlers must take this type by value");
    };
}

////////////////////////////////////////

namespace leaf_detail
{
    template <int I, class Tuple>
    struct tuple_for_each
    {
        BOOST_LEAF_CONSTEXPR static void activate( Tuple & tup ) noexcept
        {
            static_assert(!std::is_same<error_info, typename std::decay<decltype(std::get<I-1>(tup))>::type>::value, "Bug in LEAF: context type deduction");
            tuple_for_each<I-1,Tuple>::activate(tup);
            std::get<I-1>(tup).activate();
        }

        BOOST_LEAF_CONSTEXPR static void deactivate( Tuple & tup ) noexcept
        {
            static_assert(!std::is_same<error_info, typename std::decay<decltype(std::get<I-1>(tup))>::type>::value, "Bug in LEAF: context type deduction");
            std::get<I-1>(tup).deactivate();
            tuple_for_each<I-1,Tuple>::deactivate(tup);
        }

        BOOST_LEAF_CONSTEXPR static void unload( Tuple & tup, int err_id ) noexcept
        {
            static_assert(!std::is_same<error_info, typename std::decay<decltype(std::get<I-1>(tup))>::type>::value, "Bug in LEAF: context type deduction");
            BOOST_LEAF_ASSERT(err_id != 0);
            auto & sl = std::get<I-1>(tup);
            sl.unload(err_id);
            tuple_for_each<I-1,Tuple>::unload(tup, err_id);
        }

        template <class CharT, class Traits>
        static void print( std::basic_ostream<CharT, Traits> & os, void const * tup, int err_id_to_print )
        {
            BOOST_LEAF_ASSERT(tup != nullptr);
            tuple_for_each<I-1,Tuple>::print(os, tup, err_id_to_print);
            std::get<I-1>(*static_cast<Tuple const *>(tup)).print(os, err_id_to_print);
        }
    };

    template <class Tuple>
    struct tuple_for_each<0, Tuple>
    {
        BOOST_LEAF_CONSTEXPR static void activate( Tuple & ) noexcept { }
        BOOST_LEAF_CONSTEXPR static void deactivate( Tuple & ) noexcept { }
        BOOST_LEAF_CONSTEXPR static void unload( Tuple &, int ) noexcept { }
        template <class CharT, class Traits>
        BOOST_LEAF_CONSTEXPR static void print( std::basic_ostream<CharT, Traits> &, void const *, int ) { }
    };
}

////////////////////////////////////////////

namespace leaf_detail
{
    template <class T> struct does_not_participate_in_context_deduction: std::is_abstract<T> { };
    template <> struct does_not_participate_in_context_deduction<void>: std::true_type { };
    template <> struct does_not_participate_in_context_deduction<error_id>: std::true_type { };

    template <class L>
    struct deduce_e_type_list;

    template <template<class...> class L, class... T>
    struct deduce_e_type_list<L<T...>>
    {
        using type =
            leaf_detail_mp11::mp_remove_if<
                leaf_detail_mp11::mp_unique<
                    leaf_detail_mp11::mp_list<typename handler_argument_traits<T>::error_type...>
                >,
                does_not_participate_in_context_deduction
            >;
    };

    template <class L>
    struct deduce_e_tuple_impl;

    template <template <class...> class L, class... E>
    struct deduce_e_tuple_impl<L<E...>>
    {
        using type = std::tuple<slot<E>...>;
    };

    template <class... E>
    using deduce_e_tuple = typename deduce_e_tuple_impl<typename deduce_e_type_list<leaf_detail_mp11::mp_list<E...>>::type>::type;
}

////////////////////////////////////////////

template <class... E>
class context
{
    context( context const & ) = delete;
    context & operator=( context const & ) = delete;

    using Tup = leaf_detail::deduce_e_tuple<E...>;
    Tup tup_;
    bool is_active_;

#if !defined(BOOST_LEAF_NO_THREADS) && !defined(NDEBUG)
    std::thread::id thread_id_;
#endif

public:

    BOOST_LEAF_CONSTEXPR context( context && x ) noexcept:
        tup_(std::move(x.tup_)),
        is_active_(false)
    {
        BOOST_LEAF_ASSERT(!x.is_active());
    }

    BOOST_LEAF_CONSTEXPR context() noexcept:
        is_active_(false)
    {
    }

    ~context() noexcept
    {
        BOOST_LEAF_ASSERT(!is_active());
    }

    BOOST_LEAF_CONSTEXPR Tup const & tup() const noexcept
    {
        return tup_;
    }

    BOOST_LEAF_CONSTEXPR Tup & tup() noexcept
    {
        return tup_;
    }

    BOOST_LEAF_CONSTEXPR void activate() noexcept
    {
        using namespace leaf_detail;
        BOOST_LEAF_ASSERT(!is_active());
        tuple_for_each<std::tuple_size<Tup>::value,Tup>::activate(tup_);
#if !defined(BOOST_LEAF_NO_THREADS) && !defined(NDEBUG)
        thread_id_ = std::this_thread::get_id();
#endif
        is_active_ = true;
    }

    BOOST_LEAF_CONSTEXPR void deactivate() noexcept
    {
        using namespace leaf_detail;
        BOOST_LEAF_ASSERT(is_active());
        is_active_ = false;
#if !defined(BOOST_LEAF_NO_THREADS) && !defined(NDEBUG)
        BOOST_LEAF_ASSERT(std::this_thread::get_id() == thread_id_);
        thread_id_ = std::thread::id();
#endif
        tuple_for_each<std::tuple_size<Tup>::value,Tup>::deactivate(tup_);
    }

    BOOST_LEAF_CONSTEXPR void unload(error_id id) noexcept
    {
        BOOST_LEAF_ASSERT(!is_active());
        leaf_detail::tuple_for_each<std::tuple_size<Tup>::value,Tup>::unload(tup_, id.value());
    }

    BOOST_LEAF_CONSTEXPR bool is_active() const noexcept
    {
        return is_active_;
    }

    template <class CharT, class Traits>
    void print( std::basic_ostream<CharT, Traits> & os ) const
    {
        leaf_detail::tuple_for_each<std::tuple_size<Tup>::value,Tup>::print(os, &tup_, 0);
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, context const & ctx )
    {
        ctx.print(os);
        return os;
    }

    template <class R, class... H>
    BOOST_LEAF_CONSTEXPR R handle_error( error_id, H && ... ) const;

    template <class R, class... H>
    BOOST_LEAF_CONSTEXPR R handle_error( error_id, H && ... );
};

template <class Ctx>
BOOST_LEAF_CONSTEXPR BOOST_LEAF_ALWAYS_INLINE context_activator<Ctx> activate_context(Ctx & ctx) noexcept
{
    return context_activator<Ctx>(ctx);
}

////////////////////////////////////////

namespace leaf_detail
{
    template <class TypeList>
    struct deduce_context_impl;

    template <template <class...> class L, class... E>
    struct deduce_context_impl<L<E...>>
    {
        using type = context<E...>;
    };

    template <class TypeList>
    using deduce_context = typename deduce_context_impl<TypeList>::type;

    template <class H>
    struct fn_mp_args_fwd
    {
        using type = fn_mp_args<H>;
    };

    template <class... H>
    struct fn_mp_args_fwd<std::tuple<H...> &>: fn_mp_args_fwd<std::tuple<H...>> { };

    template <class... H>
    struct fn_mp_args_fwd<std::tuple<H...> const &>: fn_mp_args_fwd<std::tuple<H...>> { };

    template <class... H>
    struct fn_mp_args_fwd<std::tuple<H...>>
    {
        using type = leaf_detail_mp11::mp_append<typename fn_mp_args_fwd<H>::type...>;
    };

    template <class... H>
    struct context_type_from_handlers_impl
    {
        using type = deduce_context<leaf_detail_mp11::mp_append<typename fn_mp_args_fwd<H>::type...>>;
    };
}

template <class... H>
using context_type_from_handlers = typename leaf_detail::context_type_from_handlers_impl<H...>::type;

////////////////////////////////////////////

template <class...  H>
BOOST_LEAF_CONSTEXPR inline context_type_from_handlers<H...> make_context() noexcept
{
    return { };
}

template <class...  H>
BOOST_LEAF_CONSTEXPR inline context_type_from_handlers<H...> make_context( H && ... ) noexcept
{
    return { };
}

////////////////////////////////////////////

#if BOOST_LEAF_CFG_CAPTURE

template <class...>
BOOST_LEAF_DEPRECATED("Please use try_capture_all instead of make_shared_context/capture.")
inline context_ptr make_shared_context() noexcept
{
    return std::make_shared<polymorphic_context>();
}

template <class... H>
BOOST_LEAF_DEPRECATED("Please use try_capture_all instead of make_shared_context/capture.")
inline context_ptr make_shared_context( H && ... ) noexcept
{
    return std::make_shared<polymorphic_context>();
}

#endif

} }

#endif
