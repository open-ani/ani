// Copyright 2020-2023 Daniel Lemire
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

// If the architecture (e.g. ARM) does not have __int128 we need to emulate it

#ifndef BOOST_JSON_DETAIL_CHARCONV_DETAIL_EMULATED128_HPP
#define BOOST_JSON_DETAIL_CHARCONV_DETAIL_EMULATED128_HPP

#include <boost/json/detail/charconv/detail/config.hpp>
#include <cstdint>
#include <cassert>

namespace boost { namespace json { namespace detail { namespace charconv { namespace detail {

// Compilers might support built-in 128-bit integer types. However, it seems that
// emulating them with a pair of 64-bit integers actually produces a better code,
// so we avoid using those built-ins. That said, they are still useful for
// implementing 64-bit x 64-bit -> 128-bit multiplication.

struct uint128
{
    std::uint64_t high;
    std::uint64_t low;

    uint128& operator+=(std::uint64_t n) & noexcept
    {
        #if BOOST_JSON_HAS_BUILTIN(__builtin_addcll)

        unsigned long long carry;
        low = __builtin_addcll(low, n, 0, &carry);
        high = __builtin_addcll(high, 0, carry, &carry);

        #elif BOOST_JSON_HAS_BUILTIN(__builtin_ia32_addcarryx_u64)

        unsigned long long result;
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
};

static inline std::uint64_t umul64(std::uint32_t x, std::uint32_t y) noexcept
{
    #if defined(BOOST_JSON_HAS_MSVC_32BIT_INTRINSICS) && !defined(_M_ARM)

    return __emulu(x, y);

    #else

    return x * static_cast<std::uint64_t>(y);
    
    #endif
}

// Get 128-bit result of multiplication of two 64-bit unsigned integers.
BOOST_JSON_SAFEBUFFERS inline uint128 umul128(std::uint64_t x, std::uint64_t y) noexcept
{
    #if defined(BOOST_HAS_INT128)

    auto result = static_cast<boost::uint128_type>(x) * static_cast<boost::uint128_type>(y);
    return {static_cast<std::uint64_t>(result >> 64), static_cast<std::uint64_t>(result)};

    #elif defined(BOOST_JSON_HAS_MSVC_64BIT_INTRINSICS) && !defined(_M_ARM64)

    std::uint64_t high;
    std::uint64_t low = _umul128(x, y, &high);
    return {high, low};

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

BOOST_JSON_SAFEBUFFERS inline std::uint64_t umul128_upper64(std::uint64_t x, std::uint64_t y) noexcept
{
    #if defined(BOOST_HAS_INT128)

    auto result = static_cast<boost::uint128_type>(x) * static_cast<boost::uint128_type>(y);
    return static_cast<std::uint64_t>(result >> 64);

    #elif defined(BOOST_JSON_HAS_MSVC_64BIT_INTRINSICS)

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
BOOST_JSON_SAFEBUFFERS inline uint128 umul192_upper128(std::uint64_t x, uint128 y) noexcept
{
    auto r = umul128(x, y.high);
    r += umul128_upper64(x, y.low);
    return r;
}

// Get upper 64-bits of multiplication of a 32-bit unsigned integer and a 64-bit
// unsigned integer.
inline std::uint64_t umul96_upper64(std::uint32_t x, std::uint64_t y) noexcept
{
    #if defined(BOOST_HAS_INT128) || defined(BOOST_JSON_HAS_MSVC_64BIT_INTRINSICS)

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
BOOST_JSON_SAFEBUFFERS inline uint128 umul192_lower128(std::uint64_t x, uint128 y) noexcept
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

}}}}} // Namespaces

#endif // BOOST_JSON_DETAIL_CHARCONV_DETAIL_EMULATED128_HPP
