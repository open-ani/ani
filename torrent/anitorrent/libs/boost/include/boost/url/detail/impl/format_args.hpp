//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_IMPL_FORMAT_ARGS_HPP
#define BOOST_URL_DETAIL_IMPL_FORMAT_ARGS_HPP

namespace boost {
namespace urls {
namespace detail {

template<
    class A,
    typename std::enable_if<
        !std::is_integral<
            typename std::decay<A>::type>::value,
                int>::type = 0>
std::size_t
get_uvalue( A&& )
{
    return 0;
}

template<
    class A,
    typename std::enable_if<
        std::is_integral<
            typename std::decay<A>::type>::value &&
        std::is_signed<
            typename std::decay<A>::type>::value,
        int>::type = 0>
std::size_t
get_uvalue( A&& a )
{
    if (a > 0)
        return static_cast<std::size_t>(a);
    return 0;
}

template<
    class A,
    typename std::enable_if<
        std::is_integral<
            typename std::decay<A>::type>::value &&
            std::is_unsigned<
                typename std::decay<A>::type>::value,
        int>::type = 0>
std::size_t
get_uvalue( A&& a )
{
    return static_cast<std::size_t>(a);
}

BOOST_URL_DECL
std::size_t
get_uvalue( core::string_view a );

BOOST_URL_DECL
std::size_t
get_uvalue( char a );

template<class A>
format_arg::
format_arg( A&& a )
    : arg_( &a )
    , measure_( &measure_impl<A> )
    , fmt_( &format_impl<A> )
    , value_( get_uvalue(std::forward<A>(a) ))
    , ignore_( std::is_same<A, ignore_format>::value )
{}

template<class A>
format_arg::
format_arg( named_arg<A>&& a )
    : arg_( &a.value )
    , measure_( &measure_impl<A> )
    , fmt_( &format_impl<A> )
    , name_( a.name )
    , value_( get_uvalue(a.value))
{}

template<class A>
format_arg::
format_arg( core::string_view name, A&& a )
    : arg_( &a )
    , measure_( &measure_impl<A> )
    , fmt_( &format_impl<A> )
    , name_( name )
    , value_( get_uvalue(a) )
{}

// define the type-erased implementations that
// depends on everything: the context types,
// formatters, and type erased args
template <class A>
void
format_arg::
measure_impl(
    format_parse_context& pctx,
    measure_context& mctx,
    grammar::lut_chars const& cs,
    void const* a )
{
    using ref_t = typename std::remove_cv<
        typename std::remove_reference<A>::type>::type;
    A const& ref = *static_cast<ref_t*>(
        const_cast<void*>( a ) );
    formatter<ref_t> f;
    pctx.advance_to( f.parse(pctx) );
    mctx.advance_to( f.measure( ref, mctx, cs ) );
}

template <class A>
void
format_arg::
format_impl(
    format_parse_context& pctx,
    format_context& fctx,
    grammar::lut_chars const& cs,
    void const* a )
{
    using ref_t = typename std::remove_cv<
        typename std::remove_reference<A>::type>::type;
    A const& ref = *static_cast<ref_t*>(
            const_cast<void*>( a ) );
    formatter<ref_t> f;
    pctx.advance_to( f.parse(pctx) );
    fctx.advance_to( f.format( ref, fctx, cs ) );
}

// We point to formatter<ignore_format> where
// the format_arg variant would store monostate
template <>
struct formatter<ignore_format>
{
public:
    char const*
    parse(format_parse_context& ctx) const
    {
        return parse_empty_spec(
            ctx.begin(), ctx.end());
    }

    std::size_t
    measure(
        ignore_format,
        measure_context& ctx,
        grammar::lut_chars const&) const
    {
        return ctx.out();
    }

    char*
    format(
        ignore_format,
        format_context& ctx,
        grammar::lut_chars const&) const
    {
        return ctx.out();
    }

    // We ignore the modifiers in all replacements
    // for now
    static
    char const*
    parse_empty_spec(
        char const* it,
        char const* end)
    {
        // [it, end] -> "} suffix"
        BOOST_ASSERT(it != end);
        ignore_unused(end);
        // Should be always empty/valid as an
        // implementation detail
        BOOST_ASSERT(*it == '}');
        /*
        if (*it != '}')
            urls::detail::throw_invalid_argument();
        */
        return it;
    }
};

inline
std::size_t
measure_one(
    char c,
    grammar::lut_chars const& unreserved)
{
    // '%' must be reserved
    BOOST_ASSERT(! unreserved('%'));
    return 1 + !unreserved(c) * 2;
}

inline
void
encode_one(
    char*& out,
    char c,
    grammar::lut_chars const& unreserved)
{
    // '%' must be reserved
    BOOST_ASSERT(! unreserved('%'));
    if(unreserved(c))
    {
        *out++ = c;
        return;
    }
    *out++ = '%';
    *out++ = urls::detail::hexdigs[0][c>>4];
    *out++ = urls::detail::hexdigs[0][c&0xf];
}

// get an unsigned value from format_args
BOOST_URL_DECL
void
get_width_from_args(
    std::size_t arg_idx,
    core::string_view arg_name,
    format_args args,
    std::size_t& w);

// formatter for string view
template <>
struct formatter<core::string_view>
{
private:
    char fill = ' ';
    char align = '\0';
    std::size_t width = 0;
    std::size_t width_idx = std::size_t(-1);
    core::string_view width_name;

public:
    BOOST_URL_DECL
    char const*
    parse(format_parse_context& ctx);

    BOOST_URL_DECL
    std::size_t
    measure(
        core::string_view str,
        measure_context& ctx,
        grammar::lut_chars const& cs) const;

    BOOST_URL_DECL
    char*
    format(
        core::string_view str,
        format_context& ctx,
        grammar::lut_chars const& cs) const;
};

// formatter for anything convertible to a
// string view
template <class T>
struct formatter<
    T, typename std::enable_if<
        std::is_convertible<
            T, core::string_view>::value>::type>
{
    formatter<core::string_view> impl_;

public:
    char const*
    parse(format_parse_context& ctx)
    {
        return impl_.parse(ctx);
    }

    std::size_t
    measure(
        core::string_view str,
        measure_context& ctx,
        grammar::lut_chars const& cs) const
    {
        return impl_.measure(str, ctx, cs);
    }

    char*
    format(core::string_view str, format_context& ctx, grammar::lut_chars const& cs) const
    {
        return impl_.format(str, ctx, cs);
    }
};

template <>
struct formatter<char>
{
    formatter<core::string_view> impl_;

public:
    char const*
    parse(format_parse_context& ctx)
    {
        return impl_.parse(ctx);
    }

    std::size_t
    measure(
        char c,
        measure_context& ctx,
        grammar::lut_chars const& cs) const
    {
        return impl_.measure({&c, 1}, ctx, cs);
    }

    char*
    format(
        char c,
        format_context& ctx,
        grammar::lut_chars const& cs) const
    {
        return impl_.format({&c, 1}, ctx, cs);
    }
};

// formatters for a single integer
class integer_formatter_impl
{
    char fill = ' ';
    char align = '\0';
    char sign = '-';
    bool zeros = false;
    std::size_t width = 0;
    std::size_t width_idx = std::size_t(-1);
    core::string_view width_name;

public:
    BOOST_URL_DECL
    char const*
    parse(format_parse_context& ctx);

    BOOST_URL_DECL
    std::size_t
    measure(
        unsigned long long int v,
        measure_context& ctx,
        grammar::lut_chars const& cs) const;

    BOOST_URL_DECL
    std::size_t
    measure(
        long long int v,
        measure_context& ctx,
        grammar::lut_chars const& cs) const;

    BOOST_URL_DECL
    char*
    format(
        unsigned long long int v,
        format_context& ctx,
        grammar::lut_chars const& cs) const;

    BOOST_URL_DECL
    char*
    format(
        long long int v,
        format_context& ctx,
        grammar::lut_chars const& cs) const;
};

template <class T>
struct formatter<
    T, typename std::enable_if<
        mp11::mp_contains<mp11::mp_list<
            short int,
            int,
            long int,
            long long int,
            unsigned short int,
            unsigned int,
            unsigned long int,
            unsigned long long int>, T>::value>::type>
{
private:
    integer_formatter_impl impl_;
    using base_value_type = typename std::conditional<
        std::is_unsigned<T>::value,
        unsigned long long int,
        long long int
        >::type;

public:
    char const*
    parse(format_parse_context& ctx)
    {
        return impl_.parse(ctx);
    }

    std::size_t
    measure(
        T v,
        measure_context& ctx,
        grammar::lut_chars const& cs) const
    {
        return impl_.measure(
            static_cast<base_value_type>(v), ctx, cs);
    }

    char*
    format(T v, format_context& ctx, grammar::lut_chars const& cs) const
    {
        return impl_.format(
            static_cast<base_value_type>(v), ctx, cs);
    }
};

} // detail
} // url
} // boost

#endif
