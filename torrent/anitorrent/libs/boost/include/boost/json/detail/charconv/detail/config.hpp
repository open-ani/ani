// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_JSON_DETAIL_CHARCONV_DETAIL_CONFIG_HPP
#define BOOST_JSON_DETAIL_CHARCONV_DETAIL_CONFIG_HPP

#include <boost/config.hpp>
#include <type_traits>
#include <cfloat>

// Use 128 bit integers and supress warnings for using extensions
#if defined(BOOST_HAS_INT128)
#  define BOOST_JSON_INT128_MAX  (boost::int128_type)(((boost::uint128_type) 1 << 127) - 1)
#  define BOOST_JSON_UINT128_MAX ((2 * (boost::uint128_type) BOOST_JSON_INT128_MAX) + 1)
#endif

#ifndef BOOST_NO_CXX14_CONSTEXPR
#  define BOOST_JSON_CXX14_CONSTEXPR BOOST_CXX14_CONSTEXPR
#  define BOOST_JSON_CXX14_CONSTEXPR_NO_INLINE BOOST_CXX14_CONSTEXPR
#else
#  define BOOST_JSON_CXX14_CONSTEXPR inline
#  define BOOST_JSON_CXX14_CONSTEXPR_NO_INLINE
#endif

#if defined(__GNUC__) && __GNUC__ == 5
#  define BOOST_JSON_GCC5_CONSTEXPR inline
#else
#  define BOOST_JSON_GCC5_CONSTEXPR BOOST_JSON_CXX14_CONSTEXPR
#endif

// Inclue intrinsics if available
#if defined(BOOST_MSVC)
# include <intrin.h>
# if defined(_WIN64)
#   define BOOST_JSON_HAS_MSVC_64BIT_INTRINSICS
# else
#   define BOOST_JSON_HAS_MSVC_32BIT_INTRINSICS
# endif
#endif

// Suppress additional buffer overrun check.
// I have no idea why MSVC thinks some functions here are vulnerable to the buffer overrun
// attacks. No, they aren't.
#if defined(__GNUC__) || defined(__clang__)
    #define BOOST_JSON_SAFEBUFFERS
#elif defined(_MSC_VER)
    #define BOOST_JSON_SAFEBUFFERS __declspec(safebuffers)
#else
    #define BOOST_JSON_SAFEBUFFERS
#endif

#if defined(__has_builtin)
    #define BOOST_JSON_HAS_BUILTIN(x) __has_builtin(x)
#else
    #define BOOST_JSON_HAS_BUILTIN(x) false
#endif

#endif // BOOST_JSON_DETAIL_CHARCONV_DETAIL_CONFIG_HPP
