//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_DETAIL_CHARSET_HPP
#define BOOST_URL_GRAMMAR_DETAIL_CHARSET_HPP

#include <boost/core/bit.hpp>
#include <type_traits>

#ifdef BOOST_URL_USE_SSE2
# include <emmintrin.h>
# include <xmmintrin.h>
# ifdef _MSC_VER
#  include <intrin.h>
# endif
#endif

#ifdef _MSC_VER
#pragma warning(push)
#pragma warning(disable: 4127) // conditional expression is constant
#endif

namespace boost {
namespace urls {
namespace grammar {
namespace detail {

template<class T, class = void>
struct has_find_if : std::false_type {};

template<class T>
struct has_find_if<T, void_t<
    decltype(
    std::declval<char const*&>() =
        std::declval<T const&>().find_if(
            std::declval<char const*>(),
            std::declval<char const*>())
            )>> : std::true_type
{
};

template<class T, class = void>
struct has_find_if_not : std::false_type {};

template<class T>
struct has_find_if_not<T, void_t<
    decltype(
    std::declval<char const*&>() =
        std::declval<T const&>().find_if_not(
            std::declval<char const*>(),
            std::declval<char const*>())
            )>> : std::true_type
{
};

template<class Pred>
char const*
find_if(
    char const* first,
    char const* const last,
    Pred const& pred,
    std::false_type) noexcept
{
    while(first != last)
    {
        if(pred(*first))
            break;
        ++first;
    }
    return first;
}

template<class Pred>
char const*
find_if(
    char const* first,
    char const* const last,
    Pred const& pred,
    std::true_type) noexcept
{
    return pred.find_if(
        first, last);
}

template<class Pred>
char const*
find_if_not(
    char const* first,
    char const* const last,
    Pred const& pred,
    std::false_type) noexcept
{
    while(first != last)
    {
        if(! pred(*first))
            break;
        ++first;
    }
    return first;
}

template<class Pred>
char const*
find_if_not(
    char const* first,
    char const* const last,
    Pred const& pred,
    std::true_type) noexcept
{
    return pred.find_if_not(
        first, last);
}

#ifdef BOOST_URL_USE_SSE2

// by Peter Dimov
template<class Pred>
char const*
find_if_pred(
    Pred const& pred,
    char const* first,
    char const* last ) noexcept
{
    while( last - first >= 16 )
    {
        unsigned char r[ 16 ] = {};
        for( int i = 0; i < 16; ++i )
            r[ i ] = pred( first[ i ] )? 0xFF: 0x00;
        __m128i r2 = _mm_loadu_si128( (__m128i const*)r );
        unsigned r3 = _mm_movemask_epi8( r2 );
        if( r3 )
            return first + boost::core::countr_zero( r3 );
        first += 16;
    }
    while(
        first != last &&
        ! pred(*first))
    {
        ++first;
    }
    return first;
}

// by Peter Dimov
template<class Pred>
char const*
find_if_not_pred(
    Pred const& pred,
    char const* first,
    char const* last ) noexcept
{
    while( last - first >= 16 )
    {
        unsigned char r[ 16 ] = {};
        for( int i = 0; i < 16; ++i )
            r[ i ] = pred( first[ i ] )? 0x00: 0xFF;
        __m128i r2 = _mm_loadu_si128( (__m128i const*)r );
        unsigned r3 = _mm_movemask_epi8( r2 );
        if( r3 )
            return first + boost::core::countr_zero( r3 );
        first += 16;
    }
    while(
        first != last &&
        pred(*first))
    {
        ++first;
    }
    return first;
}

#endif

} // detail
} // grammar
} // urls
} // boost

#ifdef _MSC_VER
#pragma warning(pop)
#endif

#endif
