//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_FORMAT_ARGS_HPP
#define BOOST_URL_DETAIL_FORMAT_ARGS_HPP

#include <boost/url/detail/encode.hpp>
#include <boost/url/grammar/lut_chars.hpp>

#include <boost/core/ignore_unused.hpp>
#include <array>

// This file implements functions and classes to
// type-erase format arguments.

namespace boost {
namespace urls {
namespace detail {

// state of the format string. It basically keeps
// track of where we are in the format string.
class format_parse_context
{
    char const* begin_;
    char const* end_;
    std::size_t arg_id_ = 0;

public:
    constexpr
    format_parse_context(
        char const* first,
        char const* last,
        std::size_t arg_id = 0)
        : begin_( first )
        , end_( last )
        , arg_id_( arg_id )
    {}

    constexpr
    format_parse_context(
        core::string_view fmt,
        std::size_t arg_id = 0)
        : format_parse_context(
            fmt.data(),
            fmt.data() + fmt.size(),
            arg_id )
    {}

    constexpr
    char const*
    begin() const noexcept
    {
        return begin_;
    }

    constexpr
    char const*
    end() const noexcept
    {
        return end_;
    }

    BOOST_CXX14_CONSTEXPR
    void
    advance_to( char const* it )
    {
        begin_ = it;
    }

    std::size_t
    next_arg_id()
    {
        return arg_id_++;
    }
};

// State of the destination string
class format_context;
class measure_context;
struct ignore_format {};

template <class T>
struct named_arg
{
    core::string_view name;
    T const& value;

    named_arg(core::string_view n, T const& v)
        : name(n)
        , value(v)
    {}
};

// A type erased format argument
class format_arg
{
    void const* arg_;
    void (*measure_)(
        format_parse_context&,
        measure_context&,
        grammar::lut_chars const&,
        void const* );
    void (*fmt_)(
        format_parse_context&,
        format_context&,
        grammar::lut_chars const&,
        void const* );
    core::string_view name_;
    std::size_t value_ = 0;
    bool ignore_ = false;

    template <class A>
    static
    void
    measure_impl(
        format_parse_context& pctx,
        measure_context& mctx,
        grammar::lut_chars const& cs,
        void const* a );

    template <class A>
    static
    void
    format_impl(
        format_parse_context& pctx,
        format_context& fctx,
        grammar::lut_chars const& cs,
        void const* a );

public:
    template<class A>
    format_arg( A&& a );

    template<class A>
    format_arg( named_arg<A>&& a );

    template<class A>
    format_arg( core::string_view name, A&& a );

    format_arg()
        : format_arg(ignore_format{})
    {}

    explicit
    operator bool() const noexcept
    {
        return !ignore_;
    }

    void
    measure(
        format_parse_context& pctx,
        measure_context& mctx,
        grammar::lut_chars const& cs)
    {
        measure_( pctx, mctx, cs, arg_ );
    }

    void
    format(
        format_parse_context& pctx,
        format_context& fctx,
        grammar::lut_chars const& cs )
    {
        fmt_( pctx, fctx, cs, arg_ );
    }

    core::string_view
    name() const
    {
        return name_;
    }

    std::size_t
    value() const
    {
        return value_;
    }
};

// create temp stack storage for type erased args
template< class... Args >
std::array<format_arg, sizeof...(Args)>
make_format_args( Args&&... args )
{
    return {{ std::forward<Args>(args)... }};
}

// reference to an array of format_args
class format_args
{
    format_arg const* p_{nullptr};
    std::size_t n_{0};

public:
    format_args(
        detail::format_arg const* first,
        detail::format_arg const* last ) noexcept
        : p_(first)
        , n_(static_cast<std::size_t>(last - first))
    {}

    template < std::size_t N >
    format_args( std::array<format_arg, N> const& store ) noexcept
        : p_(store.data())
        , n_(store.size())
    {}

    format_arg
    get( std::size_t i ) const noexcept
    {
        if (i < n_)
            return p_[i];
        return {};
    }

    format_arg
    get( core::string_view name ) const noexcept
    {
        for (std::size_t i = 0; i < n_; ++i)
        {
            if (p_[i].name() == name)
                return p_[i];
        }
        return {};
    }
};

// define the format_context after format_args
class format_context
{
    format_args args_;
    char* out_;

public:
    format_context(
        char* out,
        format_args args )
        : args_( args )
        , out_( out )
    {}

    format_args
    args() const noexcept
    {
        return args_;
    }

    format_arg
    arg( std::size_t id ) const noexcept
    {
        return args_.get( id );
    }

    format_arg
    arg( core::string_view name ) const noexcept
    {
        return args_.get( name );
    }

    char*
    out()
    {
        return out_;
    }

    void
    advance_to( char* it )
    {
        out_ = it;
    }
};

// define the measure_context after format_args
class measure_context
{
    format_args args_;
    std::size_t out_;

public:
    measure_context(
        format_args args )
        : measure_context(0, args)
    {}

    measure_context(
        std::size_t out,
        format_args args )
        : args_( args )
        , out_( out )
    {}

    format_args
    args() const noexcept
    {
        return args_;
    }

    format_arg
    arg( std::size_t id ) const noexcept
    {
        return args_.get( id );
    }

    format_arg
    arg( core::string_view name ) const noexcept
    {
        return args_.get( name );
    }

    std::size_t
    out()
    {
        return out_;
    }

    void
    advance_to( std::size_t n )
    {
        out_ = n;
    }
};

// fwd declare the formatter
template <class T, class = void>
struct formatter;

} // detail
} // url
} // boost

#include <boost/url/detail/impl/format_args.hpp>

#endif
