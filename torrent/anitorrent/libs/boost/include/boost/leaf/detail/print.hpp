#ifndef BOOST_LEAF_DETAIL_PRINT_HPP_INCLUDED
#define BOOST_LEAF_DETAIL_PRINT_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/leaf/config.hpp>
#include <boost/leaf/detail/demangle.hpp>

#include <type_traits>
#include <exception>
#include <iosfwd>
#include <cstring>

namespace boost { namespace leaf {

namespace leaf_detail
{
    template <class T, class E = void>
    struct is_printable: std::false_type
    {
    };

    template <class T>
    struct is_printable<T, decltype(std::declval<std::ostream&>()<<std::declval<T const &>(), void())>: std::true_type
    {
    };

    ////////////////////////////////////////

    template <class T, class E = void>
    struct has_printable_member_value: std::false_type
    {
    };

    template <class T>
    struct has_printable_member_value<T, decltype(std::declval<std::ostream&>()<<std::declval<T const &>().value, void())>: std::true_type
    {
    };

    ////////////////////////////////////////

    template <
        class Wrapper,
        bool WrapperPrintable = is_printable<Wrapper>::value,
        bool ValuePrintable = has_printable_member_value<Wrapper>::value,
        bool IsException = std::is_base_of<std::exception,Wrapper>::value,
        bool IsEnum = std::is_enum<Wrapper>::value>
    struct diagnostic;

    template <class Wrapper, bool ValuePrintable, bool IsException, bool IsEnum>
    struct diagnostic<Wrapper, true, ValuePrintable, IsException, IsEnum>
    {
        static constexpr bool is_invisible = false;

        template <class CharT, class Traits>
        static void print( std::basic_ostream<CharT, Traits> & os, Wrapper const & x )
        {
            os << x;
        }
    };

    template <class Wrapper, bool IsException, bool IsEnum>
    struct diagnostic<Wrapper, false, true, IsException, IsEnum>
    {
        static constexpr bool is_invisible = false;

        template <class CharT, class Traits>
        static void print( std::basic_ostream<CharT, Traits> & os, Wrapper const & x )
        {
            os << type<Wrapper>() << ": " << x.value;
        }
    };

    template <class Wrapper, bool IsEnum>
    struct diagnostic<Wrapper, false, false, true, IsEnum>
    {
        static constexpr bool is_invisible = false;

        template <class CharT, class Traits>
        static void print( std::basic_ostream<CharT, Traits> & os, Wrapper const & ex )
        {
            os << type<Wrapper>() << ": std::exception::what(): " << ex.what();
        }
    };

    template <class Wrapper>
    struct diagnostic<Wrapper, false, false, false, false>
    {
        static constexpr bool is_invisible = false;

        template <class CharT, class Traits>
        static void print( std::basic_ostream<CharT, Traits> & os, Wrapper const & )
        {
            os << type<Wrapper>() << ": {not printable}";
        }
    };

    template <class Wrapper>
    struct diagnostic<Wrapper, false, false, false, true>
    {
        static constexpr bool is_invisible = false;

        template <class CharT, class Traits>
        static void print( std::basic_ostream<CharT, Traits> & os, Wrapper const & w )
        {
            os << type<Wrapper>() << ": " << static_cast<typename std::underlying_type<Wrapper>::type>(w);
        }
    };

    template <>
    struct diagnostic<std::exception_ptr, false, false, false>
    {
        static constexpr bool is_invisible = true;

        template <class CharT, class Traits>
        BOOST_LEAF_CONSTEXPR static void print( std::basic_ostream<CharT, Traits> &, std::exception_ptr const & )
        {
        }
    };
}

} }

#endif
