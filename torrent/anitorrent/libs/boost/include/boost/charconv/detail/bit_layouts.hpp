// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_BIT_LAYOUTS_HPP
#define BOOST_CHARCONV_DETAIL_BIT_LAYOUTS_HPP

#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/emulated128.hpp>
#include <cstdint>
#include <cfloat>

// Layouts of floating point types as specified by IEEE 754
// See page 23 of IEEE 754-2008

namespace boost { namespace charconv { namespace detail {

struct ieee754_binary32 
{
    static constexpr int significand_bits = 23;
    static constexpr int exponent_bits = 8;
    static constexpr int min_exponent = -126;
    static constexpr int max_exponent = 127;
    static constexpr int exponent_bias = -127;
    static constexpr int decimal_digits = 9;
};

struct ieee754_binary64 
{
    static constexpr int significand_bits = 52;
    static constexpr int exponent_bits = 11;
    static constexpr int min_exponent = -1022;
    static constexpr int max_exponent = 1023;
    static constexpr int exponent_bias = -1023;
    static constexpr int decimal_digits = 17;
};

// 80 bit long double (e.g. x86-64)
#if LDBL_MANT_DIG == 64 && LDBL_MAX_EXP == 16384

struct IEEEl2bits
{
#if BOOST_CHARCONV_ENDIAN_LITTLE_BYTE
    std::uint64_t mantissa_l : 64;
    std::uint32_t exponent : 15;
    std::uint32_t sign : 1;
    std::uint64_t pad : 48;
#else // Big endian
    std::uint64_t pad : 48;
    std::uint32_t sign : 1;
    std::uint32_t exponent : 15;
    std::uint64_t mantissa_h : 64;
#endif
};

struct ieee754_binary80
{
    static constexpr int significand_bits = 64; // Fraction is 63 and 1 integer bit
    static constexpr int exponent_bits = 15;
    static constexpr int min_exponent = -16382;
    static constexpr int max_exponent = 16383;
    static constexpr int exponent_bias = -16383;
    static constexpr int decimal_digits = 18;
};

#define BOOST_CHARCONV_LDBL_BITS 80

// 128 bit long double (e.g. s390x, ppcle64)
#elif LDBL_MANT_DIG == 113 && LDBL_MAX_EXP == 16384

struct IEEEl2bits
{
#if BOOST_CHARCONV_ENDIAN_LITTLE_BYTE
    std::uint64_t mantissa_l : 64;
    std::uint64_t mantissa_h : 48;
    std::uint32_t exponent : 15;
    std::uint32_t sign : 1;
#else // Big endian
    std::uint32_t sign : 1;
    std::uint32_t exponent : 15;
    std::uint64_t mantissa_h : 48;
    std::uint64_t mantissa_l : 64;
#endif
};

#define BOOST_CHARCONV_LDBL_BITS 128

// 64 bit long double (double == long double on ARM)
#elif LDBL_MANT_DIG == 53 && LDBL_MAX_EXP == 1024

struct IEEEl2bits
{
#if BOOST_CHARCONV_ENDIAN_LITTLE_BYTE
    std::uint32_t mantissa_l : 32;
    std::uint32_t mantissa_h : 20;
    std::uint32_t exponent : 11;
    std::uint32_t sign : 1;
#else // Big endian
    std::uint32_t sign : 1;
    std::uint32_t exponent : 11;
    std::uint32_t mantissa_h : 20;
    std::uint32_t mantissa_l : 32;
#endif
};

#define BOOST_CHARCONV_LDBL_BITS 64

#else // Unsupported long double representation
#  define BOOST_MATH_UNSUPPORTED_LONG_DOUBLE
#endif

struct IEEEbinary128
{
#if BOOST_CHARCONV_ENDIAN_LITTLE_BYTE
    std::uint64_t mantissa_l : 64;
    std::uint64_t mantissa_h : 48;
    std::uint32_t exponent : 15;
    std::uint32_t sign : 1;
#else // Big endian
    std::uint32_t sign : 1;
    std::uint32_t exponent : 15;
    std::uint64_t mantissa_h : 48;
    std::uint64_t mantissa_l : 64;
#endif
};

struct ieee754_binary128
{
    static constexpr int significand_bits = 112;
    static constexpr int exponent_bits = 15;
    static constexpr int min_exponent = -16382;
    static constexpr int max_exponent = 16383;
    static constexpr int exponent_bias = 16383;
    static constexpr int decimal_digits = 33;
};

}}} // Namespaces

#endif // BOOST_CHARCONV_DETAIL_BIT_LAYOUTS_HPP
