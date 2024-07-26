// Copyright 2020-2023 Daniel Lemire
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt
//
// Derivative of: https://github.com/fastfloat/fast_float

#ifndef BOOST_CHARCONV_DETAIL_FASTFLOAT_CONSTEXPR_FEATURE_DETECT_HPP
#define BOOST_CHARCONV_DETAIL_FASTFLOAT_CONSTEXPR_FEATURE_DETECT_HPP

#ifdef __has_include
#if __has_include(<version>)
#include <version>
#endif
#endif

// Testing for https://wg21.link/N3652, adopted in C++14
#if __cpp_constexpr >= 201304
#define BOOST_CHARCONV_FASTFLOAT_CONSTEXPR14 constexpr
#else
#define BOOST_CHARCONV_FASTFLOAT_CONSTEXPR14
#endif

#if defined(__cpp_lib_bit_cast) && __cpp_lib_bit_cast >= 201806L
#define BOOST_CHARCONV_FASTFLOAT_HAS_BIT_CAST 1
#else
#define BOOST_CHARCONV_FASTFLOAT_HAS_BIT_CAST 0
#endif

#if defined(__cpp_lib_is_constant_evaluated) && __cpp_lib_is_constant_evaluated >= 201811L
#define BOOST_CHARCONV_FASTFLOAT_HAS_IS_CONSTANT_EVALUATED 1
#else
#define BOOST_CHARCONV_FASTFLOAT_HAS_IS_CONSTANT_EVALUATED 0
#endif

// Testing for relevant C++20 constexpr library features
#if BOOST_CHARCONV_FASTFLOAT_HAS_IS_CONSTANT_EVALUATED \
    && BOOST_CHARCONV_FASTFLOAT_HAS_BIT_CAST \
    && __cpp_lib_constexpr_algorithms >= 201806L /*For std::copy and std::fill*/
#define BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20 constexpr
#define BOOST_CHARCONV_FASTFLOAT_IS_CONSTEXPR 1
#else
#define BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20
#define BOOST_CHARCONV_FASTFLOAT_IS_CONSTEXPR 0
#endif

#endif // BOOST_CHARCONV_FASTFLOAT_CONSTEXPR_FEATURE_DETECT_H
