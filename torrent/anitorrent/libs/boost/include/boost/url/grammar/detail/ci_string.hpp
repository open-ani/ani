//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_DETAIL_CI_STRING_HPP
#define BOOST_URL_GRAMMAR_DETAIL_CI_STRING_HPP

#include <boost/core/detail/string_view.hpp>
#include <boost/assert.hpp>
#include <cstdint>
#include <iterator>
#include <type_traits>

namespace boost {
namespace urls {
namespace grammar {
namespace detail {

template<class T, class = void>
struct is_char_iter : std::false_type {};

template<class T>
struct is_char_iter<T, void_t<
    decltype(std::declval<char&>() =
        *std::declval<T const&>()),
    decltype(std::declval<T&>() =
        ++std::declval<T&>()),
    decltype(std::declval<bool&>() =
        std::declval<T const&>() ==
        std::declval<T const&>())
    > > : std::integral_constant<bool,
        std::is_copy_constructible<T>::value>
{
};

template<class T, class = void>
struct is_char_range : std::false_type {};

template<class T>
struct is_char_range<T, void_t<
    decltype(std::declval<T const&>().begin()),
    decltype(std::declval<T const&>().end())
    > > : std::integral_constant<bool,
        is_char_iter<decltype(
            std::declval<T const&>(
                ).begin())>::value &&
        is_char_iter<decltype(
            std::declval<T const&>(
                ).end())>::value>
{
};

template<class T>
struct type_id_impl
{
    static
    constexpr
    char cid = 0;
};

template<class T>
constexpr
char
type_id_impl<T>::cid;

template<class T>
constexpr
std::uintptr_t
type_id() noexcept
{
    return std::uintptr_t(
        &type_id_impl<T>::cid);
}

//------------------------------------------------

constexpr
char
to_lower(char c) noexcept
{
    return
      (c >= 'A' &&
       c <= 'Z')
        ? c + 'a' - 'A'
        : c;
}

constexpr
char
to_upper(char c) noexcept
{
    return
      (c >= 'a' &&
       c <= 'z')
        ? c - ('a' - 'A')
        : c;
}

//------------------------------------------------

template<class S0, class S1>
auto
ci_is_equal(
    S0 const& s0,
    S1 const& s1) ->
        typename std::enable_if<
            ! std::is_convertible<
                S0, core::string_view>::value ||
            ! std::is_convertible<
                S1, core::string_view>::value,
        bool>::type
{
/*  If you get a compile error here, it
    means that a range you passed does
    not meet the requirements stated
    in the documentation.
*/
    static_assert(
        is_char_range<S0>::value,
        "Type requirements not met");
    static_assert(
        is_char_range<S1>::value,
        "Type requirements not met");

    // Arguments are sorted by type to
    // reduce the number of function
    // template instantiations. This
    // works because:
    //
    // ci_is_equal(s0,s1) == ci_is_equal(s1,s0)
    //
    BOOST_ASSERT(
        detail::type_id<S0>() <=
        detail::type_id<S1>());

    auto it0 = s0.begin();
    auto it1 = s1.begin();
    auto const end0 = s0.end();
    auto const end1 = s1.end();
    for(;;)
    {
        if(it0 == end0)
            return it1 == end1;
        if(it1 == end1)
            return false;
        if( to_lower(*it0) !=
            to_lower(*it1))
            return false;
        ++it0;
        ++it1;
    }
}

//------------------------------------------------

BOOST_URL_DECL
bool
ci_is_equal(
    core::string_view s0,
    core::string_view s1) noexcept;

BOOST_URL_DECL
bool
ci_is_less(
    core::string_view s0,
    core::string_view s1) noexcept;

} // detail
} // grammar
} // urls
} // boost

#endif
