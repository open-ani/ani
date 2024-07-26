// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_JSON_DETAIL_CHARCONV_LIMITS_HPP
#define BOOST_JSON_DETAIL_CHARCONV_LIMITS_HPP

#include <boost/config.hpp>
#include <limits>
#include <type_traits>

namespace boost { namespace json { namespace detail { namespace charconv {

// limits<T>::max_chars10: the minimum size of the buffer that needs to be
//   passed to to_chars to guarantee successful conversion for all values of
//   type T, when either no base is passed, or base 10 is passed
//
// limits<T>::max_chars: the minimum size of the buffer that needs to be
//   passed to to_chars to guarantee successful conversion for all values of
//   type T, for any value of base

namespace detail
{

constexpr int exp_digits( int exp )
{
    return exp < 100? 2: exp < 1000? 3: exp < 10000? 4: 5;
}

#if defined(BOOST_HAS_INT128)

template<class T> struct is_int128: std::is_same<T, boost::int128_type> {};
template<class T> struct is_uint128: std::is_same<T, boost::int128_type> {};

#else

template<class T> struct is_int128: std::false_type {};
template<class T> struct is_uint128: std::false_type {};

#endif

} // namespace detail

template<typename T> struct limits
{
    static constexpr int max_chars10 =

        // int128_t
        detail::is_int128<T>::value? 38+2: // digits10 + 1 + sign

        // uint128_t
        detail::is_uint128<T>::value? 38+1: // digits10 + 1

        // integral
        std::numeric_limits<T>::is_integer? std::numeric_limits<T>::digits10 + 1 + std::numeric_limits<T>::is_signed:

        // floating point
        std::numeric_limits<T>::max_digits10 + 3 + 2 + detail::exp_digits( std::numeric_limits<T>::max_exponent10 ); // -1.(max_digits10)e+(max_exp)

    static constexpr int max_chars =

        // int128_t
        detail::is_int128<T>::value? 127+2: // digits + 1 + sign

        // uint128_t
        detail::is_uint128<T>::value? 128+1: // digits + 1

        // integral
        std::numeric_limits<T>::is_integer? std::numeric_limits<T>::digits + 1 + std::numeric_limits<T>::is_signed:

        // floating point
        std::numeric_limits<T>::max_digits10 + 3 + 2 + detail::exp_digits( std::numeric_limits<T>::max_exponent10 ); // as above
};

#if defined(BOOST_NO_CXX17_INLINE_VARIABLES)

// Definitions of in-class constexpr members are allowed but deprecated in C++17

template<typename T> constexpr int limits<T>::max_chars10;
template<typename T> constexpr int limits<T>::max_chars;

#endif // defined(BOOST_NO_CXX17_INLINE_VARIABLES)

}}}} // namespace boost::charconv

#endif // BOOST_JSON_DETAIL_CHARCONV_LIMITS_HPP
