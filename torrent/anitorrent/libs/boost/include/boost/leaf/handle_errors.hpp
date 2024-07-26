#ifndef BOOST_LEAF_HANDLE_ERRORS_HPP_INCLUDED
#define BOOST_LEAF_HANDLE_ERRORS_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/leaf/config.hpp>
#include <boost/leaf/context.hpp>

namespace boost { namespace leaf {

template <class T>
class BOOST_LEAF_SYMBOL_VISIBLE result;

////////////////////////////////////////

class BOOST_LEAF_SYMBOL_VISIBLE error_info
{
    error_info & operator=( error_info const & ) = delete;

#ifndef BOOST_LEAF_NO_EXCEPTIONS
    static error_id unpack_error_id( std::exception const * ex ) noexcept
    {
        if( error_id const * err_id = dynamic_cast<error_id const *>(ex) )
            return *err_id;
        return current_error();
    }

    std::exception * const ex_;
#endif

    error_id const err_id_;

protected:

    error_info( error_info const & ) noexcept = default;

public:

    BOOST_LEAF_CONSTEXPR explicit error_info( error_id id ) noexcept:
#ifndef BOOST_LEAF_NO_EXCEPTIONS
        ex_(nullptr),
#endif
        err_id_(id)
    {
    }

#ifndef BOOST_LEAF_NO_EXCEPTIONS
    explicit error_info( std::exception * ex ) noexcept:
        ex_(ex),
        err_id_(unpack_error_id(ex_))
    {
    }
#endif

    BOOST_LEAF_CONSTEXPR error_id error() const noexcept
    {
        return err_id_;
    }

    BOOST_LEAF_CONSTEXPR std::exception * exception() const noexcept
    {
#ifdef BOOST_LEAF_NO_EXCEPTIONS
        return nullptr;
#else
        return ex_;
#endif
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, error_info const & x )
    {
        os << "Error ID: " << x.err_id_.value();
#ifndef BOOST_LEAF_NO_EXCEPTIONS
        if( x.ex_ )
        {
            os <<
                "\nException dynamic type: " << leaf_detail::demangle(typeid(*x.ex_).name()) <<
                "\nstd::exception::what(): " << x.ex_->what();
        }
#endif
        return os << '\n';
    }
};

namespace leaf_detail
{
    template <>
    struct handler_argument_traits<error_info const &>: handler_argument_always_available<void>
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR static error_info const & get( Tup const &, error_info const & ei ) noexcept
        {
            return ei;
        }
    };
}

////////////////////////////////////////

#if BOOST_LEAF_CFG_DIAGNOSTICS

class diagnostic_info: public error_info
{
    void const * tup_;
    void (*print_context_content_)( std::ostream &, void const * tup, int err_id_to_print );

protected:

    diagnostic_info( diagnostic_info const & ) noexcept = default;

    template <class Tup>
    BOOST_LEAF_CONSTEXPR diagnostic_info( error_info const & ei, Tup const & tup ) noexcept:
        error_info(ei),
        tup_(&tup),
        print_context_content_(&leaf_detail::tuple_for_each<std::tuple_size<Tup>::value, Tup>::print)
    {
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, diagnostic_info const & x )
    {
        os << static_cast<error_info const &>(x);
        x.print_context_content_(os, x.tup_, x.error().value());
        return os;
    }
};

namespace leaf_detail
{
    struct diagnostic_info_: diagnostic_info
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR diagnostic_info_( error_info const & ei, Tup const & tup ) noexcept:
            diagnostic_info(ei, tup)
        {
        }
    };

    template <>
    struct handler_argument_traits<diagnostic_info const &>: handler_argument_always_available<void>
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR static diagnostic_info_ get( Tup const & tup, error_info const & ei ) noexcept
        {
            return diagnostic_info_(ei, tup);
        }
    };
}

#else

class diagnostic_info: public error_info
{
protected:

    diagnostic_info( diagnostic_info const & ) noexcept = default;

    BOOST_LEAF_CONSTEXPR diagnostic_info( error_info const & ei ) noexcept:
        error_info(ei)
    {
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, diagnostic_info const & x )
    {
        return os << "diagnostic_info not available due to BOOST_LEAF_CFG_DIAGNOSTICS=0. Basic error_info follows.\n" << static_cast<error_info const &>(x);
    }
};

namespace leaf_detail
{
    struct diagnostic_info_: diagnostic_info
    {
        BOOST_LEAF_CONSTEXPR diagnostic_info_( error_info const & ei ) noexcept:
            diagnostic_info(ei)
        {
        }
    };

    template <>
    struct handler_argument_traits<diagnostic_info const &>: handler_argument_always_available<void>
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR static diagnostic_info_ get( Tup const &, error_info const & ei ) noexcept
        {
            return diagnostic_info_(ei);
        }
    };
}

#endif

////////////////////////////////////////

#if BOOST_LEAF_CFG_DIAGNOSTICS

#if BOOST_LEAF_CFG_CAPTURE

class verbose_diagnostic_info: public diagnostic_info
{
    leaf_detail::dynamic_allocator const * const da_;

protected:

    verbose_diagnostic_info( verbose_diagnostic_info const & ) noexcept = default;

    template <class Tup>
    BOOST_LEAF_CONSTEXPR verbose_diagnostic_info( error_info const & ei, Tup const & tup, leaf_detail::dynamic_allocator const * da ) noexcept:
        diagnostic_info(ei, tup),
        da_(da)
    {
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, verbose_diagnostic_info const & x )
    {
        os << static_cast<diagnostic_info const &>(x);
        if( x.da_ )
            x.da_->print(os, "Unhandled error objects:\n", x.error().value());
        return os;
    }
};

namespace leaf_detail
{
    struct verbose_diagnostic_info_: verbose_diagnostic_info
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR verbose_diagnostic_info_( error_info const & ei, Tup const & tup, dynamic_allocator const * da ) noexcept:
            verbose_diagnostic_info(ei, tup, da)
        {
        }
    };

    template <>
    struct handler_argument_traits<verbose_diagnostic_info const &>: handler_argument_always_available<dynamic_allocator>
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR static verbose_diagnostic_info_ get( Tup const & tup, error_info const & ei ) noexcept
        {
            return verbose_diagnostic_info_(ei, tup, handler_argument_traits_defaults<dynamic_allocator>::check(tup, ei));
        }
    };
}

#else

class verbose_diagnostic_info: public diagnostic_info
{
protected:

    verbose_diagnostic_info( verbose_diagnostic_info const & ) noexcept = default;

    template <class Tup>
    BOOST_LEAF_CONSTEXPR verbose_diagnostic_info( error_info const & ei, Tup const & tup ) noexcept:
        diagnostic_info(ei, tup)
    {
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, verbose_diagnostic_info const & x )
    {
        return os << "verbose_diagnostic_info not available due to BOOST_LEAF_CFG_CAPTURE=0. Basic diagnostic_info follows.\n" << static_cast<diagnostic_info const &>(x);
    }
};

namespace leaf_detail
{
    struct verbose_diagnostic_info_: verbose_diagnostic_info
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR verbose_diagnostic_info_( error_info const & ei, Tup const & tup ) noexcept:
            verbose_diagnostic_info(ei, tup)
        {
        }
    };

    template <>
    struct handler_argument_traits<verbose_diagnostic_info const &>: handler_argument_always_available<void>
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR static verbose_diagnostic_info_ get( Tup const & tup, error_info const & ei ) noexcept
        {
            return verbose_diagnostic_info_(ei, tup);
        }
    };
}

#endif

#else

class verbose_diagnostic_info: public diagnostic_info
{
protected:

    verbose_diagnostic_info( verbose_diagnostic_info const & ) noexcept = default;

    BOOST_LEAF_CONSTEXPR verbose_diagnostic_info( error_info const & ei ) noexcept:
        diagnostic_info(ei)
    {
    }

    template <class CharT, class Traits>
    friend std::ostream & operator<<( std::basic_ostream<CharT, Traits> & os, verbose_diagnostic_info const & x )
    {
        return os << "verbose_diagnostic_info not available due to BOOST_LEAF_CFG_DIAGNOSTICS=0. Basic error_info follows.\n" << static_cast<error_info const &>(x);
    }
};

namespace leaf_detail
{
    struct verbose_diagnostic_info_: verbose_diagnostic_info
    {
        BOOST_LEAF_CONSTEXPR verbose_diagnostic_info_( error_info const & ei ) noexcept:
            verbose_diagnostic_info(ei)
        {
        }
    };

    template <>
    struct handler_argument_traits<verbose_diagnostic_info const &>: handler_argument_always_available<void>
    {
        template <class Tup>
        BOOST_LEAF_CONSTEXPR static verbose_diagnostic_info_ get( Tup const &, error_info const & ei ) noexcept
        {
            return verbose_diagnostic_info_(ei);
        }
    };
}

#endif

////////////////////////////////////////

namespace leaf_detail
{
    template <class T, class... List>
    struct type_index;

    template <class T, class... Cdr>
    struct type_index<T, T, Cdr...>
    {
        constexpr static int value = 0;
    };

    template <class T, class Car, class... Cdr>
    struct type_index<T, Car, Cdr...>
    {
        constexpr static int value = 1 + type_index<T,Cdr...>::value;
    };

    template <class T, class Tuple>
    struct tuple_type_index;

    template <class T, class... TupleTypes>
    struct tuple_type_index<T,std::tuple<TupleTypes...>>
    {
        constexpr static int value = type_index<T,TupleTypes...>::value;
    };

#ifndef BOOST_LEAF_NO_EXCEPTIONS

    template <class E, bool = std::is_class<E>::value>
    struct peek_exception;

    template <>
    struct peek_exception<std::exception const, true>
    {
        BOOST_LEAF_CONSTEXPR static std::exception const * peek( error_info const & ei ) noexcept
        {
            return ei.exception();
        }
    };

    template <>
    struct peek_exception<std::exception, true>
    {
        BOOST_LEAF_CONSTEXPR static std::exception * peek( error_info const & ei ) noexcept
        {
            return ei.exception();
        }
    };

#if BOOST_LEAF_CFG_STD_SYSTEM_ERROR
    template <>
    struct peek_exception<std::error_code const, true>
    {
        static std::error_code const * peek( error_info const & ei ) noexcept
        {
            auto const ex = ei.exception();
            if( std::system_error * se = dynamic_cast<std::system_error *>(ex) )
                return &se->code();
            else if( std::error_code * ec = dynamic_cast<std::error_code *>(ex) )
                return ec;
            else
                return nullptr;
        }
    };

    template <>
    struct peek_exception<std::error_code, true>
    {
        static std::error_code * peek( error_info const & ei ) noexcept
        {
            auto const ex = ei.exception();
            if( std::system_error * se = dynamic_cast<std::system_error *>(ex) )
                return const_cast<std::error_code *>(&se->code());
            else if( std::error_code * ec = dynamic_cast<std::error_code *>(ex) )
                return ec;
            else
                return nullptr;
        }
    };
#endif

    template <class E>
    struct peek_exception<E, true>
    {
        static E * peek( error_info const & ei ) noexcept
        {
            return dynamic_cast<E *>(ei.exception());
        }
    };

    template <class E>
    struct peek_exception<E, false>
    {
        BOOST_LEAF_CONSTEXPR static E * peek( error_info const & ) noexcept
        {
            return nullptr;
        }
    };

#endif

    template <class E, bool = does_not_participate_in_context_deduction<E>::value>
    struct peek_tuple;

    template <class E>
    struct peek_tuple<E, true>
    {
        template <class SlotsTuple>
        BOOST_LEAF_CONSTEXPR static E const * peek( SlotsTuple const &, error_id const & ) noexcept
        {
            return nullptr;
        }
        
        template <class SlotsTuple>
        BOOST_LEAF_CONSTEXPR static E * peek( SlotsTuple &, error_id const & ) noexcept
        {
            return nullptr;
        }
    };

    template <class E>
    struct peek_tuple<E, false>
    {
        template <class SlotsTuple>
        BOOST_LEAF_CONSTEXPR static E const * peek( SlotsTuple const & tup, error_id const & err ) noexcept
        {
            return std::get<tuple_type_index<slot<E>,SlotsTuple>::value>(tup).has_value(err.value());
        }

        template <class SlotsTuple>
        BOOST_LEAF_CONSTEXPR static E * peek( SlotsTuple & tup, error_id const & err ) noexcept
        {
            return std::get<tuple_type_index<slot<E>,SlotsTuple>::value>(tup).has_value(err.value());
        }
    };

    template <class E, class SlotsTuple>
    BOOST_LEAF_CONSTEXPR inline
    E const *
    peek( SlotsTuple const & tup, error_info const & ei ) noexcept
    {
        if( error_id err = ei.error() )
        {
            if( E const * e = peek_tuple<E>::peek(tup, err) )
                return e;
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            else
                return peek_exception<E const>::peek(ei);
#endif
        }
        return nullptr;
    }

    template <class E, class SlotsTuple>
    BOOST_LEAF_CONSTEXPR inline
    E *
    peek( SlotsTuple & tup, error_info const & ei ) noexcept
    {
        if( error_id err = ei.error() )
        {
            if( E * e = peek_tuple<E>::peek(tup, err) )
                return e;
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            else
                return peek_exception<E>::peek(ei);
#endif
        }
        return nullptr;
    }
}

////////////////////////////////////////

namespace leaf_detail
{
    template <class A>
    template <class Tup>
    BOOST_LEAF_CONSTEXPR inline
    typename handler_argument_traits_defaults<A, false>::error_type const *
    handler_argument_traits_defaults<A, false>::
    check( Tup const & tup, error_info const & ei ) noexcept
    {
        return peek<typename std::decay<A>::type>(tup, ei);
    }

    template <class A>
    template <class Tup>
    BOOST_LEAF_CONSTEXPR inline
    typename handler_argument_traits_defaults<A, false>::error_type *
    handler_argument_traits_defaults<A, false>::
    check( Tup & tup, error_info const & ei ) noexcept
    {
        return peek<typename std::decay<A>::type>(tup, ei);
    }

    template <class Tup>
    BOOST_LEAF_CONSTEXPR inline
    std::exception const *
    handler_argument_traits<void>::
    check( Tup const &, error_info const & ei ) noexcept
    {
        return ei.exception();
    }

    template <class Tup, class... List>
    struct check_arguments;

    template <class Tup>
    struct check_arguments<Tup>
    {
        BOOST_LEAF_CONSTEXPR static bool check( Tup const &, error_info const & )
        {
            return true;
        }
    };

    template <class Tup, class Car, class... Cdr>
    struct check_arguments<Tup, Car, Cdr...>
    {
        BOOST_LEAF_CONSTEXPR static bool check( Tup & tup, error_info const & ei ) noexcept
        {
            return handler_argument_traits<Car>::check(tup, ei) && check_arguments<Tup, Cdr...>::check(tup, ei);
        }
    };
}

////////////////////////////////////////

namespace leaf_detail
{
    template <class>
    struct handler_matches_any_error: std::false_type
    {
    };

    template <template<class...> class L>
    struct handler_matches_any_error<L<>>: std::true_type
    {
    };

    template <template<class...> class L, class Car, class... Cdr>
    struct handler_matches_any_error<L<Car, Cdr...>>
    {
        constexpr static bool value = handler_argument_traits<Car>::always_available && handler_matches_any_error<L<Cdr...>>::value;
    };
}

////////////////////////////////////////

namespace leaf_detail
{
    template <class Tup, class... A>
    BOOST_LEAF_CONSTEXPR inline bool check_handler_( Tup & tup, error_info const & ei, leaf_detail_mp11::mp_list<A...> ) noexcept
    {
        return check_arguments<Tup, A...>::check(tup, ei);
    }

    template <class R, class F, bool IsResult = is_result_type<R>::value, class FReturnType = fn_return_type<F>>
    struct handler_caller
    {
        template <class Tup, class... A>
        BOOST_LEAF_CONSTEXPR static R call( Tup & tup, error_info const & ei, F && f, leaf_detail_mp11::mp_list<A...> )
        {
            return std::forward<F>(f)( handler_argument_traits<A>::get(tup, ei)... );
        }
    };

    template <template <class...> class Result, class... E, class F>
    struct handler_caller<Result<void, E...>, F, true, void>
    {
        using R = Result<void, E...>;

        template <class Tup, class... A>
        BOOST_LEAF_CONSTEXPR static R call( Tup & tup, error_info const & ei, F && f, leaf_detail_mp11::mp_list<A...> )
        {
            std::forward<F>(f)( handler_argument_traits<A>::get(tup, ei)... );
            return { };
        }
    };

    template <class T>
    struct is_tuple: std::false_type { };

    template <class... T>
    struct is_tuple<std::tuple<T...>>: std::true_type { };

    template <class... T>
    struct is_tuple<std::tuple<T...> &>: std::true_type { };

    template <class R, class Tup, class H>
    BOOST_LEAF_CONSTEXPR inline
    typename std::enable_if<!is_tuple<typename std::decay<H>::type>::value, R>::type
    handle_error_( Tup & tup, error_info const & ei, H && h )
    {
        static_assert( handler_matches_any_error<fn_mp_args<H>>::value, "The last handler passed to handle_all must match any error." );
        return handler_caller<R, H>::call( tup, ei, std::forward<H>(h), fn_mp_args<H>{ } );
    }

    template <class R, class Tup, class Car, class... Cdr>
    BOOST_LEAF_CONSTEXPR inline
    typename std::enable_if<!is_tuple<typename std::decay<Car>::type>::value, R>::type
    handle_error_( Tup & tup, error_info const & ei, Car && car, Cdr && ... cdr )
    {
        if( handler_matches_any_error<fn_mp_args<Car>>::value || check_handler_( tup, ei, fn_mp_args<Car>{ } ) )
            return handler_caller<R, Car>::call( tup, ei, std::forward<Car>(car), fn_mp_args<Car>{ } );
        else
            return handle_error_<R>( tup, ei, std::forward<Cdr>(cdr)...);
    }

    template <class R, class Tup, class HTup, size_t ... I>
    BOOST_LEAF_CONSTEXPR inline
    R
    handle_error_tuple_( Tup & tup, error_info const & ei, leaf_detail_mp11::index_sequence<I...>, HTup && htup )
    {
        return handle_error_<R>(tup, ei, std::get<I>(std::forward<HTup>(htup))...);
    }

    template <class R, class Tup, class HTup, class... Cdr, size_t ... I>
    BOOST_LEAF_CONSTEXPR inline
    R
    handle_error_tuple_( Tup & tup, error_info const & ei, leaf_detail_mp11::index_sequence<I...>, HTup && htup, Cdr && ... cdr )
    {
        return handle_error_<R>(tup, ei, std::get<I>(std::forward<HTup>(htup))..., std::forward<Cdr>(cdr)...);
    }

    template <class R, class Tup, class H>
    BOOST_LEAF_CONSTEXPR inline
    typename std::enable_if<is_tuple<typename std::decay<H>::type>::value, R>::type
    handle_error_( Tup & tup, error_info const & ei, H && h )
    {
        return handle_error_tuple_<R>(
            tup,
            ei,
            leaf_detail_mp11::make_index_sequence<std::tuple_size<typename std::decay<H>::type>::value>(),
            std::forward<H>(h));
    }

    template <class R, class Tup, class Car, class... Cdr>
    BOOST_LEAF_CONSTEXPR inline
    typename std::enable_if<is_tuple<typename std::decay<Car>::type>::value, R>::type
    handle_error_( Tup & tup, error_info const & ei, Car && car, Cdr && ... cdr )
    {
        return handle_error_tuple_<R>(
            tup,
            ei,
            leaf_detail_mp11::make_index_sequence<std::tuple_size<typename std::decay<Car>::type>::value>(),
            std::forward<Car>(car),
            std::forward<Cdr>(cdr)...);
    }
}

////////////////////////////////////////

template <class... E>
template <class R, class... H>
BOOST_LEAF_CONSTEXPR BOOST_LEAF_ALWAYS_INLINE
R
context<E...>::
handle_error( error_id id, H && ... h ) const
{
    BOOST_LEAF_ASSERT(!is_active());
    return leaf_detail::handle_error_<R>(tup(), error_info(id), std::forward<H>(h)...);
}

template <class... E>
template <class R, class... H>
BOOST_LEAF_CONSTEXPR BOOST_LEAF_ALWAYS_INLINE
R
context<E...>::
handle_error( error_id id, H && ... h )
{
    BOOST_LEAF_ASSERT(!is_active());
    return leaf_detail::handle_error_<R>(tup(), error_info(id), std::forward<H>(h)...);
}

////////////////////////////////////////

namespace leaf_detail
{
    template <class T>
    void unload_result( result<T> * r )
    {
        (void) r->unload();
    }

    inline void unload_result( void * )
    {
    }
}

////////////////////////////////////////

#ifdef BOOST_LEAF_NO_EXCEPTIONS

template <class TryBlock, class... H>
BOOST_LEAF_CONSTEXPR inline
typename std::decay<decltype(std::declval<TryBlock>()().value())>::type
try_handle_all( TryBlock && try_block, H && ... h ) noexcept
{
    static_assert(is_result_type<decltype(std::declval<TryBlock>()())>::value, "The return type of the try_block passed to a try_handle_all function must be registered with leaf::is_result_type");
    context_type_from_handlers<H...> ctx;
    auto active_context = activate_context(ctx);
    if( auto r = std::forward<TryBlock>(try_block)() )
        return std::move(r).value();
    else
    {
        leaf_detail::unload_result(&r);
        error_id id = r.error();
        ctx.deactivate();
        using R = typename std::decay<decltype(std::declval<TryBlock>()().value())>::type;
        return ctx.template handle_error<R>(std::move(id), std::forward<H>(h)...);
    }
}

template <class TryBlock, class... H>
BOOST_LEAF_ATTRIBUTE_NODISCARD BOOST_LEAF_CONSTEXPR inline
typename std::decay<decltype(std::declval<TryBlock>()())>::type
try_handle_some( TryBlock && try_block, H && ... h ) noexcept
{
    static_assert(is_result_type<decltype(std::declval<TryBlock>()())>::value, "The return type of the try_block passed to a try_handle_some function must be registered with leaf::is_result_type");
    context_type_from_handlers<H...> ctx;
    auto active_context = activate_context(ctx);
    if( auto r = std::forward<TryBlock>(try_block)() )
        return r;
    else
    {
        leaf_detail::unload_result(&r);
        error_id id = r.error();
        ctx.deactivate();
        using R = typename std::decay<decltype(std::declval<TryBlock>()())>::type;
        auto rr = ctx.template handle_error<R>(std::move(id), std::forward<H>(h)..., [&r]()->R { return std::move(r); });
        if( !rr )
            ctx.unload(rr.error());
        return rr;
    }
}

template <class TryBlock, class... H>
BOOST_LEAF_CONSTEXPR inline
decltype(std::declval<TryBlock>()())
try_catch( TryBlock && try_block, H && ... ) noexcept
{
    static_assert(sizeof(context_type_from_handlers<H...>) > 0,
        "When exceptions are disabled, try_catch can't fail and has no use for the handlers, but this ensures that the supplied H... types are compatible.");
    return std::forward<TryBlock>(try_block)();
}

#else

namespace leaf_detail
{
    template <class Ctx, class TryBlock, class... H>
    decltype(std::declval<TryBlock>()())
    try_catch_( Ctx & ctx, TryBlock && try_block, H && ... h )
    {
        using namespace leaf_detail;
        BOOST_LEAF_ASSERT(ctx.is_active());
        using R = decltype(std::declval<TryBlock>()());
        try
        {
            auto r = std::forward<TryBlock>(try_block)();
            unload_result(&r);
            return std::move(r);
        }
        catch( std::exception & ex )
        {
            ctx.deactivate();
            error_info e(&ex);
            return handle_error_<R>(ctx.tup(), e, std::forward<H>(h)...,
                [&]() -> R
                {
                    ctx.unload(e.error());
                    throw;
                } );
        }
        catch(...)
        {
            ctx.deactivate();
            error_info e(nullptr);
            return handle_error_<R>(ctx.tup(), e, std::forward<H>(h)...,
                [&]() -> R
                {
                    ctx.unload(e.error());
                    throw;
                } );
        }
    }
}

template <class TryBlock, class... H>
inline
typename std::decay<decltype(std::declval<TryBlock>()().value())>::type
try_handle_all( TryBlock && try_block, H && ... h )
{
    static_assert(is_result_type<decltype(std::declval<TryBlock>()())>::value, "The return type of the try_block passed to try_handle_all must be registered with leaf::is_result_type");
    context_type_from_handlers<H...> ctx;
    auto active_context = activate_context(ctx);
    if( auto r = leaf_detail::try_catch_(ctx, std::forward<TryBlock>(try_block), std::forward<H>(h)...) )
        return std::move(r).value();
    else
    {
        BOOST_LEAF_ASSERT(ctx.is_active());
        leaf_detail::unload_result(&r);
        error_id id = r.error();
        ctx.deactivate();
        using R = typename std::decay<decltype(std::declval<TryBlock>()().value())>::type;
        return ctx.template handle_error<R>(std::move(id), std::forward<H>(h)...);
    }
}

template <class TryBlock, class... H>
BOOST_LEAF_ATTRIBUTE_NODISCARD inline
typename std::decay<decltype(std::declval<TryBlock>()())>::type
try_handle_some( TryBlock && try_block, H && ... h )
{
    static_assert(is_result_type<decltype(std::declval<TryBlock>()())>::value, "The return type of the try_block passed to try_handle_some must be registered with leaf::is_result_type");
    context_type_from_handlers<H...> ctx;
    auto active_context = activate_context(ctx);
    if( auto r = leaf_detail::try_catch_(ctx, std::forward<TryBlock>(try_block), std::forward<H>(h)...) )
        return r;
    else if( ctx.is_active() )
    {
        leaf_detail::unload_result(&r);
        error_id id = r.error();
        ctx.deactivate();
        using R = typename std::decay<decltype(std::declval<TryBlock>()())>::type;
        auto rr = ctx.template handle_error<R>(std::move(id), std::forward<H>(h)...,
            [&r]()->R
            {
                return std::move(r);
            });
        if( !rr )
            ctx.unload(rr.error());
        return rr;
    }
    else
    {
        ctx.unload(r.error());
        return r;
    }
}

template <class TryBlock, class... H>
inline
decltype(std::declval<TryBlock>()())
try_catch( TryBlock && try_block, H && ... h )
{
    context_type_from_handlers<H...> ctx;
    auto active_context = activate_context(ctx);
    using R = decltype(std::declval<TryBlock>()());
    try
    {
        return std::forward<TryBlock>(try_block)();
    }
    catch( std::exception & ex )
    {
        ctx.deactivate();
        error_info e(&ex);
        return leaf_detail::handle_error_<R>(ctx.tup(), e, std::forward<H>(h)...,
            [&]() -> R
            {
                ctx.unload(e.error());
                throw;
            } );
    }
    catch(...)
    {
        ctx.deactivate();
        error_info e(nullptr);
        return leaf_detail::handle_error_<R>(ctx.tup(), e, std::forward<H>(h)...,
            [&]() -> R
            {
                ctx.unload(e.error());
                throw;
            } );
    }
}

#endif

#if BOOST_LEAF_CFG_CAPTURE

namespace leaf_detail
{
    template <class LeafResult>
    struct try_capture_all_dispatch_non_void
    {
        using leaf_result = LeafResult;

        template <class TryBlock>
        inline
        static
        leaf_result
        try_capture_all_( TryBlock && try_block ) noexcept
        {
            leaf_detail::slot<leaf_detail::dynamic_allocator> sl;
            sl.activate();
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            try
#endif
            {
                if( leaf_result r = std::forward<TryBlock>(try_block)() )
                {
                    sl.deactivate();
                    return r;
                }
                else
                {
                    sl.deactivate();
                    return leaf_result(sl.value(error_id(r.error()).value()).template extract_capture_list<leaf_result>());
                }
            }
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            catch( std::exception & ex )
            {
                sl.deactivate();
                return sl.value(error_info(&ex).error().value()).template extract_capture_list<leaf_result>();
            }
            catch(...)
            {
                sl.deactivate();
                return sl.value(error_info(nullptr).error().value()).template extract_capture_list<leaf_result>();
            }
#endif
        }
    };

    template <class R, bool IsVoid = std::is_same<void, R>::value, bool IsResultType = is_result_type<R>::value>
    struct try_capture_all_dispatch;

    template <class R>
    struct try_capture_all_dispatch<R, false, true>:
        try_capture_all_dispatch_non_void<::boost::leaf::result<typename std::decay<decltype(std::declval<R>().value())>::type>>
    {
    };

    template <class R>
    struct try_capture_all_dispatch<R, false, false>:
        try_capture_all_dispatch_non_void<::boost::leaf::result<typename std::remove_reference<R>::type>>
    {
    };

    template <class R>
    struct try_capture_all_dispatch<R, true, false>
    {
        using leaf_result = ::boost::leaf::result<R>;

        template <class TryBlock>
        inline
        static
        leaf_result
        try_capture_all_( TryBlock && try_block ) noexcept
        {
            leaf_detail::slot<leaf_detail::dynamic_allocator> sl;
            sl.activate();
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            try
#endif
            {
                std::forward<TryBlock>(try_block)();
                return {};
            }
#ifndef BOOST_LEAF_NO_EXCEPTIONS
            catch( std::exception & ex )
            {
                sl.deactivate();
                return sl.value(error_info(&ex).error().value()).template extract_capture_list<leaf_result>();
            }
            catch(...)
            {
                sl.deactivate();
                return sl.value(error_info(nullptr).error().value()).template extract_capture_list<leaf_result>();
            }
#endif
        }
    };
}

template <class TryBlock>
inline
typename leaf_detail::try_capture_all_dispatch<decltype(std::declval<TryBlock>()())>::leaf_result
try_capture_all( TryBlock && try_block ) noexcept
{
    return leaf_detail::try_capture_all_dispatch<decltype(std::declval<TryBlock>()())>::try_capture_all_(std::forward<TryBlock>(try_block));
}
#endif

} }

// Boost Exception Integration

namespace boost { class exception; }
namespace boost { template <class Tag,class T> class error_info; }
namespace boost { namespace exception_detail { template <class ErrorInfo> struct get_info; } }

namespace boost { namespace leaf {

namespace leaf_detail
{
    template <class T>
    struct match_enum_type;

    template <class Tag, class T>
    struct match_enum_type<boost::error_info<Tag, T>>
    {
        using type = T;
    };

    template <class Ex>
    BOOST_LEAF_CONSTEXPR inline Ex * get_exception( error_info const & ei )
    {
        return dynamic_cast<Ex *>(ei.exception());
    }

    template <class, class T>
    struct dependent_type { using type = T; };

    template <class Dep, class T>
    using dependent_type_t = typename dependent_type<Dep, T>::type;

    template <class Tag, class T>
    struct handler_argument_traits<boost::error_info<Tag, T>>
    {
        using error_type = void;
        constexpr static bool always_available = false;

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static T * check( Tup &, error_info const & ei ) noexcept
        {
            using boost_exception = dependent_type_t<T, boost::exception>;
            if( auto * be = get_exception<boost_exception>(ei) )
                return exception_detail::get_info<boost::error_info<Tag, T>>::get(*be);
            else
                return nullptr;
        }

        template <class Tup>
        BOOST_LEAF_CONSTEXPR static boost::error_info<Tag, T> get( Tup const & tup, error_info const & ei ) noexcept
        {
            return boost::error_info<Tag, T>(*check(tup, ei));
        }
    };

    template <class Tag, class T> struct handler_argument_traits<boost::error_info<Tag, T> const &>: handler_argument_traits_require_by_value<boost::error_info<Tag, T>> { };
    template <class Tag, class T> struct handler_argument_traits<boost::error_info<Tag, T> const *>: handler_argument_traits_require_by_value<boost::error_info<Tag, T>> { };
    template <class Tag, class T> struct handler_argument_traits<boost::error_info<Tag, T> &>: handler_argument_traits_require_by_value<boost::error_info<Tag, T>> { };
    template <class Tag, class T> struct handler_argument_traits<boost::error_info<Tag, T> *>: handler_argument_traits_require_by_value<boost::error_info<Tag, T>> { };
}

} }

#endif
