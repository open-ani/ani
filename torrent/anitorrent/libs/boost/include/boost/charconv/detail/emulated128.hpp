// Copyright 2020-2023 Daniel Lemire
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt
//
// If the architecture (e.g. Apple ARM) does not have __int128 we need to emulate it

#ifndef BOOST_CHARCONV_DETAIL_EMULATED128_HPP
#define BOOST_CHARCONV_DETAIL_EMULATED128_HPP

#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/config.hpp>
#include <boost/core/bit.hpp>
#include <type_traits>
#include <limits>
#include <cstdint>
#include <cassert>
#include <cmath>

namespace boost { namespace charconv { namespace detail {

// Compilers might support built-in 128-bit integer types. However, it seems that
// emulating them with a pair of 64-bit integers actually produces a better code,
// so we avoid using those built-ins. That said, they are still useful for
// implementing 64-bit x 64-bit -> 128-bit multiplication.

// Memcpy-able temp class for uint128
struct trivial_uint128
{
    #if BOOST_CHARCONV_ENDIAN_LITTLE_BYTE
    std::uint64_t low;
    std::uint64_t high;
    #else
    std::uint64_t high;
    std::uint64_t low;
    #endif
};

// Macro replacement lists can not be enclosed in parentheses
struct uint128
{
    std::uint64_t high;
    std::uint64_t low;

    // Constructors
    constexpr uint128() noexcept : high {}, low {} {}

    constexpr uint128(const uint128& v) noexcept = default;

    constexpr uint128(uint128&& v) noexcept = default;

    constexpr uint128(std::uint64_t high_, std::uint64_t low_) noexcept : high {high_}, low {low_} {}

    constexpr uint128(const trivial_uint128& v) noexcept : high {v.high}, low {v.low} {} // NOLINT

    constexpr uint128(trivial_uint128&& v) noexcept : high {v.high}, low {v.low} {} // NOLINT

    #define SIGNED_CONSTRUCTOR(expr) constexpr uint128(expr v) noexcept : high {v < 0 ? UINT64_MAX : UINT64_C(0)}, low {static_cast<std::uint64_t>(v)} {} // NOLINT
    #define UNSIGNED_CONSTRUCTOR(expr) constexpr uint128(expr v) noexcept : high {}, low {static_cast<std::uint64_t>(v)} {} // NOLINT

    SIGNED_CONSTRUCTOR(char)                    // NOLINT
    SIGNED_CONSTRUCTOR(signed char)             // NOLINT
    SIGNED_CONSTRUCTOR(short)                   // NOLINT
    SIGNED_CONSTRUCTOR(int)                     // NOLINT
    SIGNED_CONSTRUCTOR(long)                    // NOLINT
    SIGNED_CONSTRUCTOR(long long)               // NOLINT

    UNSIGNED_CONSTRUCTOR(unsigned char)         // NOLINT
    UNSIGNED_CONSTRUCTOR(unsigned short)        // NOLINT
    UNSIGNED_CONSTRUCTOR(unsigned)              // NOLINT
    UNSIGNED_CONSTRUCTOR(unsigned long)         // NOLINT
    UNSIGNED_CONSTRUCTOR(unsigned long long)    // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    constexpr uint128(boost::int128_type v) noexcept :  // NOLINT : Allow implicit conversions
        high {static_cast<std::uint64_t>(v >> 64)},
         low {static_cast<std::uint64_t>(static_cast<boost::uint128_type>(v) & ~UINT64_C(0))} {}

    constexpr uint128(boost::uint128_type v) noexcept : // NOLINT : Allow implicit conversions
        high {static_cast<std::uint64_t>(v >> 64)},
         low {static_cast<std::uint64_t>(v & ~UINT64_C(0))} {}
    #endif

    #undef SIGNED_CONSTRUCTOR
    #undef UNSIGNED_CONSTRUCTOR

    // Assignment Operators
    #define SIGNED_ASSIGNMENT_OPERATOR(expr) BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator=(const expr& v) noexcept { high = v < 0 ? UINT64_MAX : UINT64_C(0); low = static_cast<std::uint64_t>(v); return *this; } // NOLINT
    #define UNSIGNED_ASSIGNMENT_OPERATOR(expr) BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator=(const expr& v) noexcept { high = 0U; low = static_cast<std::uint64_t>(v); return *this; } // NOLINT

    SIGNED_ASSIGNMENT_OPERATOR(char)                    // NOLINT
    SIGNED_ASSIGNMENT_OPERATOR(signed char)             // NOLINT
    SIGNED_ASSIGNMENT_OPERATOR(short)                   // NOLINT
    SIGNED_ASSIGNMENT_OPERATOR(int)                     // NOLINT
    SIGNED_ASSIGNMENT_OPERATOR(long)                    // NOLINT
    SIGNED_ASSIGNMENT_OPERATOR(long long)               // NOLINT

    UNSIGNED_ASSIGNMENT_OPERATOR(unsigned char)         // NOLINT
    UNSIGNED_ASSIGNMENT_OPERATOR(unsigned short)        // NOLINT
    UNSIGNED_ASSIGNMENT_OPERATOR(unsigned)              // NOLINT
    UNSIGNED_ASSIGNMENT_OPERATOR(unsigned long)         // NOLINT
    UNSIGNED_ASSIGNMENT_OPERATOR(unsigned long long)    // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator=(const boost::int128_type&  v) noexcept { *this = uint128(v); return *this; }
    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator=(const boost::uint128_type& v) noexcept { *this = uint128(v); return *this; }
    #endif

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator=(const trivial_uint128& v) noexcept { this->low = v.low; this->high = v.high; return *this; }

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator=(const uint128&) noexcept;

    #undef SIGNED_ASSIGNMENT_OPERATOR
    #undef UNSIGNED_ASSIGNMENT_OPERATOR

    // Conversion Operators
    #define INTEGER_CONVERSION_OPERATOR(expr) explicit constexpr operator expr() const noexcept { return static_cast<expr>(low); } // NOLINT
    #define FLOAT_CONVERSION_OPERATOR(expr) explicit operator expr() const noexcept { return std::ldexp(static_cast<expr>(high), 64) + static_cast<expr>(low); } // NOLINT

    INTEGER_CONVERSION_OPERATOR(char)                   // NOLINT
    INTEGER_CONVERSION_OPERATOR(signed char)            // NOLINT
    INTEGER_CONVERSION_OPERATOR(short)                  // NOLINT
    INTEGER_CONVERSION_OPERATOR(int)                    // NOLINT
    INTEGER_CONVERSION_OPERATOR(long)                   // NOLINT
    INTEGER_CONVERSION_OPERATOR(long long)              // NOLINT
    INTEGER_CONVERSION_OPERATOR(unsigned char)          // NOLINT
    INTEGER_CONVERSION_OPERATOR(unsigned short)         // NOLINT
    INTEGER_CONVERSION_OPERATOR(unsigned)               // NOLINT
    INTEGER_CONVERSION_OPERATOR(unsigned long)          // NOLINT
    INTEGER_CONVERSION_OPERATOR(unsigned long long)     // NOLINT

    explicit constexpr operator bool() const noexcept { return high || low; }

    #ifdef BOOST_CHARCONV_HAS_INT128
    explicit constexpr operator boost::int128_type()  const noexcept { return (static_cast<boost::int128_type>(high) << 64) + low; }
    explicit constexpr operator boost::uint128_type() const noexcept { return (static_cast<boost::uint128_type>(high) << 64) + low; }
    #endif

    #ifdef BOOST_CHARCONV_HAS_FLOAT128
    explicit operator __float128() const noexcept { return ldexpq(static_cast<__float128>(high), 64) + static_cast<__float128>(low); }
    #endif

    FLOAT_CONVERSION_OPERATOR(float)        // NOLINT
    FLOAT_CONVERSION_OPERATOR(double)       // NOLINT
    FLOAT_CONVERSION_OPERATOR(long double)  // NOLINT

    #undef INTEGER_CONVERSION_OPERATOR
    #undef FLOAT_CONVERSION_OPERATOR

    // Unary Operators
    constexpr friend uint128 operator-(uint128 val) noexcept;
    constexpr friend uint128 operator+(uint128 val) noexcept;

    // Comparison Operators

    // Equality
    #define INTEGER_OPERATOR_EQUAL(expr) constexpr friend bool operator==(uint128 lhs, expr rhs) noexcept { return lhs.high == 0 && rhs >= 0 && lhs.low == static_cast<std::uint64_t>(rhs); } // NOLINT
    #define UNSIGNED_INTEGER_OPERATOR_EQUAL(expr) constexpr friend bool operator==(uint128 lhs, expr rhs) noexcept { return lhs.high == 0 && lhs.low == static_cast<std::uint64_t>(rhs); } // NOLINT

    INTEGER_OPERATOR_EQUAL(char)                        // NOLINT
    INTEGER_OPERATOR_EQUAL(signed char)                 // NOLINT
    INTEGER_OPERATOR_EQUAL(short)                       // NOLINT
    INTEGER_OPERATOR_EQUAL(int)                         // NOLINT
    INTEGER_OPERATOR_EQUAL(long)                        // NOLINT
    INTEGER_OPERATOR_EQUAL(long long)                   // NOLINT
    UNSIGNED_INTEGER_OPERATOR_EQUAL(unsigned char)      // NOLINT
    UNSIGNED_INTEGER_OPERATOR_EQUAL(unsigned short)     // NOLINT
    UNSIGNED_INTEGER_OPERATOR_EQUAL(unsigned)           // NOLINT
    UNSIGNED_INTEGER_OPERATOR_EQUAL(unsigned long)      // NOLINT
    UNSIGNED_INTEGER_OPERATOR_EQUAL(unsigned long long) // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    constexpr friend bool operator==(uint128 lhs, boost::int128_type  rhs) noexcept { return lhs == uint128(rhs); }
    constexpr friend bool operator==(uint128 lhs, boost::uint128_type rhs) noexcept { return lhs == uint128(rhs); }
    #endif

    constexpr friend bool operator==(uint128 lhs, uint128 rhs) noexcept;

    #undef INTEGER_OPERATOR_EQUAL
    #undef UNSIGNED_INTEGER_OPERATOR_EQUAL

    // Inequality
    #define INTEGER_OPERATOR_NOTEQUAL(expr) constexpr friend bool operator!=(uint128 lhs, expr rhs) noexcept { return !(lhs == rhs); } // NOLINT

    INTEGER_OPERATOR_NOTEQUAL(char)                 // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(signed char)          // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(short)                // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(int)                  // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(long)                 // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(long long)            // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(unsigned char)        // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(unsigned short)       // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(unsigned)             // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(unsigned long)        // NOLINT
    INTEGER_OPERATOR_NOTEQUAL(unsigned long long)   // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    constexpr friend bool operator!=(uint128 lhs, boost::int128_type  rhs) noexcept { return !(lhs == rhs); }
    constexpr friend bool operator!=(uint128 lhs, boost::uint128_type rhs) noexcept { return !(lhs == rhs); }
    #endif

    constexpr friend bool operator!=(uint128 lhs, uint128 rhs) noexcept;

    #undef INTEGER_OPERATOR_NOTEQUAL

    // Less than
    #define INTEGER_OPERATOR_LESS_THAN(expr) constexpr friend bool operator<(uint128 lhs, expr rhs) noexcept { return lhs.high == 0U && rhs > 0 && lhs.low < static_cast<std::uint64_t>(rhs); } // NOLINT
    #define UNSIGNED_INTEGER_OPERATOR_LESS_THAN(expr) constexpr friend bool operator<(uint128 lhs, expr rhs) noexcept { return lhs.high == 0U && lhs.low < static_cast<std::uint64_t>(rhs); } // NOLINT

    INTEGER_OPERATOR_LESS_THAN(char)                            // NOLINT
    INTEGER_OPERATOR_LESS_THAN(signed char)                     // NOLINT
    INTEGER_OPERATOR_LESS_THAN(short)                           // NOLINT
    INTEGER_OPERATOR_LESS_THAN(int)                             // NOLINT
    INTEGER_OPERATOR_LESS_THAN(long)                            // NOLINT
    INTEGER_OPERATOR_LESS_THAN(long long)                       // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN(unsigned char)          // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN(unsigned short)         // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN(unsigned)               // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN(unsigned long)          // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN(unsigned long long)     // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator<(uint128 lhs, boost::int128_type  rhs) noexcept { return lhs < uint128(rhs); }
    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator<(uint128 lhs, boost::uint128_type rhs) noexcept { return lhs < uint128(rhs); }
    #endif

    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator<(uint128 lhs, uint128 rhs) noexcept;

    #undef INTEGER_OPERATOR_LESS_THAN
    #undef UNSIGNED_INTEGER_OPERATOR_LESS_THAN

    // Less than or equal to
    #define INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(expr) constexpr friend bool operator<=(uint128 lhs, expr rhs) noexcept { return lhs.high == 0U && rhs >= 0 && lhs.low <= static_cast<std::uint64_t>(rhs); } // NOLINT
    #define UNSIGNED_INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(expr) constexpr friend bool operator<=(uint128 lhs, expr rhs) noexcept { return lhs.high == 0U && lhs.low <= static_cast<std::uint64_t>(rhs); } // NOLINT

    INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(char)                            // NOLINT
    INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(signed char)                     // NOLINT
    INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(short)                           // NOLINT
    INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(int)                             // NOLINT
    INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(long)                            // NOLINT
    INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(long long)                       // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(unsigned char)          // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(unsigned short)         // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(unsigned)               // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(unsigned long)          // NOLINT
    UNSIGNED_INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO(unsigned long long)     // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator<=(uint128 lhs, boost::int128_type  rhs) noexcept { return lhs <= uint128(rhs); }
    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator<=(uint128 lhs, boost::uint128_type rhs) noexcept { return lhs <= uint128(rhs); }
    #endif

    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator<=(uint128 lhs, uint128 rhs) noexcept;

    #undef INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO
    #undef UNSIGNED_INTEGER_OPERATOR_LESS_THAN_OR_EQUAL_TO

    // Greater than
    #define INTEGER_OPERATOR_GREATER_THAN(expr) constexpr friend bool operator>(uint128 lhs, expr rhs) noexcept { return lhs.high > 0U || rhs < 0 || lhs.low > static_cast<std::uint64_t>(rhs); } // NOLINT
    #define UNSIGNED_INTEGER_OPERATOR_GREATER_THAN(expr) constexpr friend bool operator>(uint128 lhs, expr rhs) noexcept { return lhs.high > 0U || lhs.low > static_cast<std::uint64_t>(rhs); } // NOLINT

    INTEGER_OPERATOR_GREATER_THAN(char)                             // NOLINT
    INTEGER_OPERATOR_GREATER_THAN(signed char)                      // NOLINT
    INTEGER_OPERATOR_GREATER_THAN(short)                            // NOLINT
    INTEGER_OPERATOR_GREATER_THAN(int)                              // NOLINT
    INTEGER_OPERATOR_GREATER_THAN(long)                             // NOLINT
    INTEGER_OPERATOR_GREATER_THAN(long long)                        // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN(unsigned char)           // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN(unsigned short)          // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN(unsigned)                // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN(unsigned long)           // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN(unsigned long long)      // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator>(uint128 lhs, boost::int128_type  rhs) noexcept { return lhs > uint128(rhs); }
    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator>(uint128 lhs, boost::uint128_type rhs) noexcept { return lhs > uint128(rhs); }
    #endif

    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator>(uint128 lhs, uint128 rhs) noexcept;

    #undef INTEGER_OPERATOR_GREATER_THAN
    #undef UNSIGNED_INTEGER_OPERATOR_GREATER_THAN

    // Greater than or equal to
    #define INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(expr) constexpr friend bool operator>=(uint128 lhs, expr rhs) noexcept { return lhs.high > 0U || rhs < 0 || lhs.low >= static_cast<std::uint64_t>(rhs); } // NOLINT
    #define UNSIGNED_INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(expr) constexpr friend bool operator>=(uint128 lhs, expr rhs) noexcept { return lhs.high > 0U || lhs.low >= static_cast<std::uint64_t>(rhs); } // NOLINT

    INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(char)                             // NOLINT
    INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(signed char)                      // NOLINT
    INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(short)                            // NOLINT
    INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(int)                              // NOLINT
    INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(long)                             // NOLINT
    INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(long long)                        // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(unsigned char)           // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(unsigned short)          // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(unsigned)                // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(unsigned long)           // NOLINT
    UNSIGNED_INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO(unsigned long long)      // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator>=(uint128 lhs, boost::int128_type  rhs) noexcept { return lhs >= uint128(rhs); }
    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator>=(uint128 lhs, boost::uint128_type rhs) noexcept { return lhs >= uint128(rhs); }
    #endif

    BOOST_CHARCONV_CXX14_CONSTEXPR friend bool operator>=(uint128 lhs, uint128 rhs) noexcept;

    #undef INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO
    #undef UNSIGNED_INTEGER_OPERATOR_GREATER_THAN_OR_EQUAL_TO

    // Binary Operators

    // Not
    constexpr friend uint128 operator~(uint128 v) noexcept;

    // Or
    #define INTEGER_BINARY_OPERATOR_OR(expr) constexpr friend uint128 operator|(uint128 lhs, expr rhs) noexcept { return {lhs.high, lhs.low | static_cast<std::uint64_t>(rhs)}; } // NOLINT

    INTEGER_BINARY_OPERATOR_OR(char)                // NOLINT
    INTEGER_BINARY_OPERATOR_OR(signed char)         // NOLINT
    INTEGER_BINARY_OPERATOR_OR(short)               // NOLINT
    INTEGER_BINARY_OPERATOR_OR(int)                 // NOLINT
    INTEGER_BINARY_OPERATOR_OR(long)                // NOLINT
    INTEGER_BINARY_OPERATOR_OR(long long)           // NOLINT
    INTEGER_BINARY_OPERATOR_OR(unsigned char)       // NOLINT
    INTEGER_BINARY_OPERATOR_OR(unsigned short)      // NOLINT
    INTEGER_BINARY_OPERATOR_OR(unsigned)            // NOLINT
    INTEGER_BINARY_OPERATOR_OR(unsigned long)       // NOLINT
    INTEGER_BINARY_OPERATOR_OR(unsigned long long)  // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    constexpr friend uint128 operator|(uint128 lhs, boost::int128_type  rhs) noexcept { return lhs | uint128(rhs); }
    constexpr friend uint128 operator|(uint128 lhs, boost::uint128_type rhs) noexcept { return lhs | uint128(rhs); }
    #endif

    constexpr friend uint128 operator|(uint128 lhs, uint128 rhs) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator|=(uint128 v) noexcept;

    #undef INTEGER_BINARY_OPERATOR_OR

    // And
    #define INTEGER_BINARY_OPERATOR_AND(expr) constexpr friend uint128 operator&(uint128 lhs, expr rhs) noexcept { return {lhs.high, lhs.low & static_cast<std::uint64_t>(rhs)}; } // NOLINT

    INTEGER_BINARY_OPERATOR_AND(char)                   // NOLINT
    INTEGER_BINARY_OPERATOR_AND(signed char)            // NOLINT
    INTEGER_BINARY_OPERATOR_AND(short)                  // NOLINT
    INTEGER_BINARY_OPERATOR_AND(int)                    // NOLINT
    INTEGER_BINARY_OPERATOR_AND(long)                   // NOLINT
    INTEGER_BINARY_OPERATOR_AND(long long)              // NOLINT
    INTEGER_BINARY_OPERATOR_AND(unsigned char)          // NOLINT
    INTEGER_BINARY_OPERATOR_AND(unsigned short)         // NOLINT
    INTEGER_BINARY_OPERATOR_AND(unsigned)               // NOLINT
    INTEGER_BINARY_OPERATOR_AND(unsigned long)          // NOLINT
    INTEGER_BINARY_OPERATOR_AND(unsigned long long)     // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    constexpr friend uint128 operator&(uint128 lhs, boost::int128_type  rhs) noexcept { return lhs & uint128(rhs); }
    constexpr friend uint128 operator&(uint128 lhs, boost::uint128_type rhs) noexcept { return lhs & uint128(rhs); }
    #endif

    constexpr friend uint128 operator&(uint128 lhs, uint128 rhs) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator&=(uint128 v) noexcept;

    #undef INTEGER_BINARY_OPERATOR_AND

    // Xor
    #define INTEGER_BINARY_OPERATOR_XOR(expr) constexpr friend uint128 operator^(uint128 lhs, expr rhs) noexcept { return {lhs.high, lhs.low ^ static_cast<std::uint64_t>(rhs)}; } // NOLINT

    INTEGER_BINARY_OPERATOR_XOR(char)                   // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(signed char)            // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(short)                  // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(int)                    // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(long)                   // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(long long)              // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(unsigned char)          // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(unsigned short)         // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(unsigned)               // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(unsigned long)          // NOLINT
    INTEGER_BINARY_OPERATOR_XOR(unsigned long long)     // NOLINT

    #ifdef BOOST_CHARCONV_HAS_INT128
    constexpr friend uint128 operator^(uint128 lhs, boost::int128_type  rhs) noexcept { return lhs ^ uint128(rhs); }
    constexpr friend uint128 operator^(uint128 lhs, boost::uint128_type rhs) noexcept { return lhs ^ uint128(rhs); }
    #endif

    constexpr friend uint128 operator^(uint128 lhs, uint128 rhs) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator^=(uint128 v) noexcept;

    #undef INTEGER_BINARY_OPERATOR_XOR

    // Left shift
    #define INTEGER_BINARY_OPERATOR_LEFT_SHIFT(expr)                                            \
    BOOST_CHARCONV_CXX14_CONSTEXPR friend uint128 operator<<(uint128 lhs, expr rhs) noexcept    \
    {                                                                                           \
        if (rhs >= 64)                                                                          \
        {                                                                                       \
            return {lhs.low << (rhs - 64), 0};                                                  \
        }                                                                                       \
        else if (rhs == 0)                                                                      \
        {                                                                                       \
            return lhs;                                                                         \
        }                                                                                       \
                                                                                                \
        return {(lhs.high << rhs) | (lhs.low >> (64 - rhs)), lhs.low << rhs};                   \
    } // NOLINT

    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(char)                    // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(signed char)             // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(short)                   // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(int)                     // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(long)                    // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(long long)               // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(unsigned char)           // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(unsigned short)          // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(unsigned)                // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(unsigned long)           // NOLINT
    INTEGER_BINARY_OPERATOR_LEFT_SHIFT(unsigned long long)      // NOLINT

    #define INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(expr)                     \
    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator<<=(expr amount) noexcept   \
    {                                                                           \
        *this = *this << amount;                                                \
        return *this;                                                           \
    } // NOLINT

    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(char)                     // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(signed char)              // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(short)                    // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(int)                      // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(long)                     // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(long long)                // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(unsigned char)            // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(unsigned short)           // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(unsigned)                 // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(unsigned long)            // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT(unsigned long long)       // NOLINT

    #undef INTEGER_BINARY_OPERATOR_LEFT_SHIFT
    #undef INTEGER_BINARY_OPERATOR_EQUALS_LEFT_SHIFT

    // Right Shift
    #define INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(expr)                                               \
    BOOST_CHARCONV_CXX14_CONSTEXPR friend uint128 operator>>(uint128 lhs, expr amount) noexcept     \
    {                                                                                               \
        if (amount >= 64)                                                                           \
        {                                                                                           \
            return {0, lhs.high >> (amount - 64)};                                                  \
        }                                                                                           \
        else if (amount == 0)                                                                       \
        {                                                                                           \
            return lhs;                                                                             \
        }                                                                                           \
                                                                                                    \
        return {lhs.high >> amount, (lhs.low >> amount) | (lhs.high << (64 - amount))};             \
    } // NOLINT

    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(char)                   // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(signed char)            // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(short)                  // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(int)                    // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(long)                   // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(long long)              // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(unsigned char)          // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(unsigned short)         // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(unsigned)               // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(unsigned long)          // NOLINT
    INTEGER_BINARY_OPERATOR_RIGHT_SHIFT(unsigned long long)     // NOLINT

    #define INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(expr)                        \
    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator>>=(expr amount) noexcept       \
    {                                                                               \
        *this = *this >> amount;                                                    \
        return *this;                                                               \
    } // NOLINT

    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(char)                    // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(signed char)             // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(short)                   // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(int)                     // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(long)                    // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(long long)               // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(unsigned char)           // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(unsigned short)          // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(unsigned)                // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(unsigned long)           // NOLINT
    INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT(unsigned long long)      // NOLINT

    #undef INTEGER_BINARY_OPERATOR_RIGHT_SHIFT
    #undef INTEGER_BINARY_OPERATOR_EQUALS_RIGHT_SHIFT

    // Arithmetic operators (Add, sub, mul, div, mod)
    inline uint128 &operator+=(std::uint64_t n) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR friend uint128 operator+(uint128 lhs, uint128 rhs) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator+=(uint128 v) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator++() noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR const uint128 operator++(int) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR friend uint128 operator-(uint128 lhs, uint128 rhs) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator-=(uint128 v) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator--() noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR const uint128 operator--(int) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR friend uint128 operator*(uint128 lhs, uint128 rhs) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator*=(uint128 v) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR friend uint128 operator/(uint128 lhs, uint128 rhs) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator/=(uint128 v) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR friend uint128 operator%(uint128 lhs, uint128 rhs) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &operator%=(uint128 v) noexcept;

private:
    BOOST_CHARCONV_CXX14_CONSTEXPR friend int high_bit(uint128 v) noexcept;

    BOOST_CHARCONV_CXX14_CONSTEXPR friend void
    div_impl(uint128 lhs, uint128 rhs, uint128 &quotient, uint128 &remainder) noexcept;
};

constexpr uint128 operator-(uint128 val) noexcept
{
    return {~val.high + static_cast<std::uint64_t>(val.low == 0), ~val.low + 1};
}

constexpr uint128 operator+(uint128 val) noexcept
{
    return val;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator=(const uint128& v) noexcept // NOLINT : User defined for older compilers
{
    low = v.low;
    high = v.high;
    return *this;
}

constexpr bool operator==(uint128 lhs, uint128 rhs) noexcept
{
    return lhs.high == rhs.high && lhs.low == rhs.low;
}

constexpr bool operator!=(uint128 lhs, uint128 rhs) noexcept
{
    return !(lhs == rhs);
}

BOOST_CHARCONV_CXX14_CONSTEXPR bool operator<(uint128 lhs, uint128 rhs) noexcept
{
    if (lhs.high == rhs.high)
    {
        return lhs.low < rhs.low;
    }

    return lhs.high < rhs.high;
}

BOOST_CHARCONV_CXX14_CONSTEXPR bool operator<=(uint128 lhs, uint128 rhs) noexcept
{
    return !(rhs < lhs);
}

BOOST_CHARCONV_CXX14_CONSTEXPR bool operator>(uint128 lhs, uint128 rhs) noexcept
{
    return rhs < lhs;
}

BOOST_CHARCONV_CXX14_CONSTEXPR bool operator>=(uint128 lhs, uint128 rhs) noexcept
{
    return !(lhs < rhs);
}

constexpr uint128 operator~(uint128 v) noexcept
{
    return {~v.high, ~v.low};
}

constexpr uint128 operator|(uint128 lhs, uint128 rhs) noexcept
{
    return {lhs.high | rhs.high, lhs.low | rhs.low};
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator|=(uint128 v) noexcept
{
    *this = *this | v;
    return *this;
}

constexpr uint128 operator&(uint128 lhs, uint128 rhs) noexcept
{
    return {lhs.high & rhs.high, lhs.low & rhs.low};
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator&=(uint128 v) noexcept
{
    *this = *this & v;
    return *this;
}

constexpr uint128 operator^(uint128 lhs, uint128 rhs) noexcept
{
    return {lhs.high ^ rhs.high, lhs.low ^ rhs.low};
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator^=(uint128 v) noexcept
{
    *this = *this ^ v;
    return *this;
}

inline uint128 &uint128::operator+=(std::uint64_t n) noexcept
{
    #if BOOST_CHARCONV_HAS_BUILTIN(__builtin_addcll)

    unsigned long long carry {};
        low = __builtin_addcll(low, n, 0, &carry);
        high = __builtin_addcll(high, 0, carry, &carry);

    #elif BOOST_CHARCONV_HAS_BUILTIN(__builtin_ia32_addcarryx_u64)

    unsigned long long result {};
        auto carry = __builtin_ia32_addcarryx_u64(0, low, n, &result);
        low = result;
        __builtin_ia32_addcarryx_u64(carry, high, 0, &result);
        high = result;

    #elif defined(BOOST_MSVC) && defined(_M_X64)

    auto carry = _addcarry_u64(0, low, n, &low);
        _addcarry_u64(carry, high, 0, &high);

    #else

    auto sum = low + n;
    high += (sum < low ? 1 : 0);
    low = sum;

    #endif
    return *this;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 operator+(uint128 lhs, uint128 rhs) noexcept
{
    const uint128 temp = {lhs.high + rhs.high, lhs.low + rhs.low};

    // Need to carry a bit into rhs
    if (temp.low < lhs.low)
    {
        return {temp.high + 1, temp.low};
    }

    return temp;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator+=(uint128 v) noexcept
{
    *this = *this + v;
    return *this;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator++() noexcept
{
    if (this->low == UINT64_MAX)
    {
        this->low = 0;
        ++this->high;
    }
    else
    {
        ++this->low;
    }

    return *this;
}

BOOST_CHARCONV_CXX14_CONSTEXPR const uint128 uint128::operator++(int) noexcept
{
    return ++(*this);
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 operator-(uint128 lhs, uint128 rhs) noexcept
{
    const uint128 temp {lhs.high - rhs.high, lhs.low - rhs.low};

    // Check for carry
    if (lhs.low < rhs.low)
    {
        return {temp.high - 1, temp.low};
    }

    return temp;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator-=(uint128 v) noexcept
{
    *this = *this - v;
    return *this;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator--() noexcept
{
	if (this->low == 0)
	{
        this->low = UINT64_MAX;
        --this->high;
	}
    else // NOLINT
    {
        --this->low;
    }

    return *this;
}

BOOST_CHARCONV_CXX14_CONSTEXPR const uint128 uint128::operator--(int) noexcept
{
    return --(*this);
}
BOOST_CHARCONV_CXX14_CONSTEXPR uint128 operator*(uint128 lhs, uint128 rhs) noexcept
{
    const auto a = static_cast<std::uint64_t>(lhs.low >> 32);
    const auto b = static_cast<std::uint64_t>(lhs.low & UINT32_MAX);
    const auto c = static_cast<std::uint64_t>(rhs.low >> 32);
    const auto d = static_cast<std::uint64_t>(rhs.low & UINT32_MAX);

    uint128 result { lhs.high * rhs.low + lhs.low * rhs.high + a * c, b * d };
    result += uint128(a * d) << 32;
    result += uint128(b * c) << 32;
    return result;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator*=(uint128 v) noexcept
{
    *this = *this * v;
    return *this;
}

BOOST_CHARCONV_CXX14_CONSTEXPR int high_bit(uint128 v) noexcept
{
    if (v.high != 0)
    {
        return 127 - boost::core::countl_zero(v.high);
    }
    else if (v.low != 0)
    {
        return 63 - boost::core::countl_zero(v.low);
    }

    return 0;
}

// See: https://stackoverflow.com/questions/5386377/division-without-using
BOOST_CHARCONV_CXX14_CONSTEXPR void div_impl(uint128 lhs, uint128 rhs, uint128& quotient, uint128& remainder) noexcept
{
    constexpr uint128 one {0, 1};

    if (rhs > lhs)
    {
        quotient = 0U;
        remainder = 0U;
    }
    else if (lhs == rhs)
    {
        quotient = 1U;
        remainder = 0U;
    }

    uint128 denom = rhs;
    quotient = 0U;

    std::int32_t shift = high_bit(lhs) - high_bit(rhs);
    if (shift < 0)
    {
        shift = 32 - shift;
    }
    denom <<= shift;

    for (int i = 0; i <= shift; ++i)
    {
        quotient <<= 1;
        if (lhs >= denom)
        {
            lhs -= denom;
            quotient |= one;
        }
        denom >>= 1;
    }

    remainder = lhs;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 operator/(uint128 lhs, uint128 rhs) noexcept
{
    uint128 quotient {0, 0};
    uint128 remainder {0, 0};
    div_impl(lhs, rhs, quotient, remainder);

    return quotient;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator/=(uint128 v) noexcept
{
    *this = *this / v;
    return *this;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 operator%(uint128 lhs, uint128 rhs) noexcept
{
    uint128 quotient {0, 0};
    uint128 remainder {0, 0};
    div_impl(lhs, rhs, quotient, remainder);

    return remainder;
}

BOOST_CHARCONV_CXX14_CONSTEXPR uint128 &uint128::operator%=(uint128 v) noexcept
{
    *this = *this % v;
    return *this;
}

static inline std::uint64_t umul64(std::uint32_t x, std::uint32_t y) noexcept
{
    // __emulu is not available on ARM https://learn.microsoft.com/en-us/cpp/intrinsics/emul-emulu?view=msvc-170
    #if defined(BOOST_CHARCONV_HAS_MSVC_32BIT_INTRINSICS) && !defined(_M_ARM)

    return __emulu(x, y);

    #else

    return x * static_cast<std::uint64_t>(y);

    #endif
}

// Get 128-bit result of multiplication of two 64-bit unsigned integers.
BOOST_CHARCONV_SAFEBUFFERS inline uint128 umul128(std::uint64_t x, std::uint64_t y) noexcept 
{
    #if defined(BOOST_CHARCONV_HAS_INT128)
    
    auto result = static_cast<boost::uint128_type>(x) * static_cast<boost::uint128_type>(y);
    return {static_cast<std::uint64_t>(result >> 64), static_cast<std::uint64_t>(result)};

    // _umul128 is x64 only https://learn.microsoft.com/en-us/cpp/intrinsics/umul128?view=msvc-170
    #elif defined(BOOST_CHARCONV_HAS_MSVC_64BIT_INTRINSICS) && !defined(_M_ARM64)
    
    unsigned long long high;
    std::uint64_t low = _umul128(x, y, &high);
    return {static_cast<std::uint64_t>(high), low};
    
    // https://developer.arm.com/documentation/dui0802/a/A64-General-Instructions/UMULH
    #elif defined(_M_ARM64) && !defined(__MINGW32__)

    std::uint64_t high = __umulh(x, y);
    std::uint64_t low = x * y;
    return {high, low};

    #else
    
    auto a = static_cast<std::uint32_t>(x >> 32);
    auto b = static_cast<std::uint32_t>(x);
    auto c = static_cast<std::uint32_t>(y >> 32);
    auto d = static_cast<std::uint32_t>(y);

    auto ac = umul64(a, c);
    auto bc = umul64(b, c);
    auto ad = umul64(a, d);
    auto bd = umul64(b, d);

    auto intermediate = (bd >> 32) + static_cast<std::uint32_t>(ad) + static_cast<std::uint32_t>(bc);

    return {ac + (intermediate >> 32) + (ad >> 32) + (bc >> 32),
            (intermediate << 32) + static_cast<std::uint32_t>(bd)};
    
    #endif
}

BOOST_CHARCONV_SAFEBUFFERS inline std::uint64_t umul128_upper64(std::uint64_t x, std::uint64_t y) noexcept
{
    #if defined(BOOST_CHARCONV_HAS_INT128)
    
    auto result = static_cast<boost::uint128_type>(x) * static_cast<boost::uint128_type>(y);
    return static_cast<std::uint64_t>(result >> 64);
    
    #elif defined(BOOST_CHARCONV_HAS_MSVC_64BIT_INTRINSICS)
    
    return __umulh(x, y);
    
    #else
    
    auto a = static_cast<std::uint32_t>(x >> 32);
    auto b = static_cast<std::uint32_t>(x);
    auto c = static_cast<std::uint32_t>(y >> 32);
    auto d = static_cast<std::uint32_t>(y);

    auto ac = umul64(a, c);
    auto bc = umul64(b, c);
    auto ad = umul64(a, d);
    auto bd = umul64(b, d);

    auto intermediate = (bd >> 32) + static_cast<std::uint32_t>(ad) + static_cast<std::uint32_t>(bc);

    return ac + (intermediate >> 32) + (ad >> 32) + (bc >> 32);
    
    #endif
}

// Get upper 128-bits of multiplication of a 64-bit unsigned integer and a 128-bit
// unsigned integer.
BOOST_CHARCONV_SAFEBUFFERS inline uint128 umul192_upper128(std::uint64_t x, uint128 y) noexcept
{
    auto r = umul128(x, y.high);
    r += umul128_upper64(x, y.low);
    return r;
}

// Get upper 64-bits of multiplication of a 32-bit unsigned integer and a 64-bit
// unsigned integer.
inline std::uint64_t umul96_upper64(std::uint32_t x, std::uint64_t y) noexcept 
{
    #if defined(BOOST_CHARCONV_HAS_INT128) || defined(BOOST_CHARCONV_HAS_MSVC_64BIT_INTRINSICS)
    
    return umul128_upper64(static_cast<std::uint64_t>(x) << 32, y);
    
    #else
    
    auto yh = static_cast<std::uint32_t>(y >> 32);
    auto yl = static_cast<std::uint32_t>(y);

    auto xyh = umul64(x, yh);
    auto xyl = umul64(x, yl);

    return xyh + (xyl >> 32);

    #endif
}

// Get lower 128-bits of multiplication of a 64-bit unsigned integer and a 128-bit
// unsigned integer.
BOOST_CHARCONV_SAFEBUFFERS inline uint128 umul192_lower128(std::uint64_t x, uint128 y) noexcept
{
    auto high = x * y.high;
    auto highlow = umul128(x, y.low);
    return {high + highlow.high, highlow.low};
}

// Get lower 64-bits of multiplication of a 32-bit unsigned integer and a 64-bit
// unsigned integer.
inline std::uint64_t umul96_lower64(std::uint32_t x, std::uint64_t y) noexcept 
{
    return x * y;
}

}}} // Namespaces

// Non-standard libraries may add specializations for library-provided types
namespace std {

template <>
struct numeric_limits<boost::charconv::detail::uint128>
{
    // Member constants
    BOOST_ATTRIBUTE_UNUSED static constexpr bool is_specialized = true;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool is_signed = false;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool is_integer = true;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool is_exact = true;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool has_infinity = false;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool has_quiet_NaN = false;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool has_signaling_NaN = false;
    BOOST_ATTRIBUTE_UNUSED static constexpr std::float_round_style round_style = std::round_toward_zero;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool is_iec559 = false;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool is_bounded = true;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool is_modulo = true;
    BOOST_ATTRIBUTE_UNUSED static constexpr int digits = 128;
    BOOST_ATTRIBUTE_UNUSED static constexpr int digits10 = 38;
    BOOST_ATTRIBUTE_UNUSED static constexpr int max_digits10 = 0;
    BOOST_ATTRIBUTE_UNUSED static constexpr int radix = 2;
    BOOST_ATTRIBUTE_UNUSED static constexpr int min_exponent = 0;
    BOOST_ATTRIBUTE_UNUSED static constexpr int min_exponent10 = 0;
    BOOST_ATTRIBUTE_UNUSED static constexpr int max_exponent = 0;
    BOOST_ATTRIBUTE_UNUSED static constexpr int max_exponent10 = 0;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool traps = std::numeric_limits<std::uint64_t>::traps;
    BOOST_ATTRIBUTE_UNUSED static constexpr bool tinyness_before = false;

    // Member functions
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 (min)() { return 0; }
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 lowest() { return 0; }
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 (max)() { return {UINT64_MAX, UINT64_MAX}; }
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 epsilon() { return 0; }
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 round_error() { return 0; }
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 infinity() { return 0; }
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 quiet_NaN() { return 0; }
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 signaling_NaN() { return 0; }
    BOOST_ATTRIBUTE_UNUSED static constexpr boost::charconv::detail::uint128 denorm_min() { return 0; }
};

} // Namespace std

#endif // BOOST_CHARCONV_DETAIL_EMULATED128_HPP
