// Copyright 2020-2023 Daniel Lemire
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_COMPUTE_FLOAT64_HPP
#define BOOST_CHARCONV_DETAIL_COMPUTE_FLOAT64_HPP

#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/significand_tables.hpp>
#include <boost/charconv/detail/emulated128.hpp>
#include <boost/core/bit.hpp>
#include <cstdint>
#include <cfloat>
#include <cstring>
#include <cmath>

namespace boost { namespace charconv { namespace detail { 

static constexpr double powers_of_ten[] = {
    1e0,  1e1,  1e2,  1e3,  1e4,  1e5,  1e6,  1e7,  1e8,  1e9,  1e10, 1e11,
    1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18, 1e19, 1e20, 1e21, 1e22
};

// Attempts to compute i * 10^(power) exactly; and if "negative" is true, negate the result.
// 
// This function will only work in some cases, when it does not work, success is
// set to false. This should work *most of the time* (like 99% of the time).
// We assume that power is in the [-325, 308] interval.
inline double compute_float64(std::int64_t power, std::uint64_t i, bool negative, bool& success) noexcept
{
    static constexpr auto smallest_power = -325;
    static constexpr auto largest_power = 308;

    // We start with a fast path
    // It was described in Clinger WD. 
    // How to read floating point numbers accurately.
    // ACM SIGPLAN Notices. 1990
#if (FLT_EVAL_METHOD != 1) && (FLT_EVAL_METHOD != 0)
    if (0 <= power && power <= 22 && i <= UINT64_C(9007199254740991))
#else
    if (-22 <= power && power <= 22 && i <= UINT64_C(9007199254740991))
#endif
    {
        // The general idea is as follows.
        // If 0 <= s < 2^53 and if 10^0 <= p <= 10^22 then
        // 1) Both s and p can be represented exactly as 64-bit floating-point
        // values
        // (binary64).
        // 2) Because s and p can be represented exactly as floating-point values,
        // then s * p
        // and s / p will produce correctly rounded values.
        
        auto d = static_cast<double>(i);
        
        if (power < 0) 
        {
            d = d / powers_of_ten[-power];
        } 
        else 
        {
            d = d * powers_of_ten[power];
        }
        
        if (negative) 
        {
            d = -d;
        }

        success = true;
        return d;
    }

    // When 22 < power && power <  22 + 16, we could
    // hope for another, secondary fast path.  It was
    // described by David M. Gay in  "Correctly rounded
    // binary-decimal and decimal-binary conversions." (1990)
    // If you need to compute i * 10^(22 + x) for x < 16,
    // first compute i * 10^x, if you know that result is exact
    // (e.g., when i * 10^x < 2^53),
    // then you can still proceed and do (i * 10^x) * 10^22.
    // Is this worth your time?
    // You need  22 < power *and* power <  22 + 16 *and* (i * 10^(x-22) < 2^53)
    // for this second fast path to work.
    // If you have 22 < power *and* power <  22 + 16, and then you
    // optimistically compute "i * 10^(x-22)", there is still a chance that you
    // have wasted your time if i * 10^(x-22) >= 2^53. It makes the use cases of
    // this optimization maybe less common than we would like. Source:
    // http://www.exploringbinary.com/fast-path-decimal-to-floating-point-conversion/
    // also used in RapidJSON: https://rapidjson.org/strtod_8h_source.html

    if (i == 0 || power < smallest_power)
    {
        return negative ? -0.0 : 0.0;
    }
    else if (power > largest_power)
    {
        return negative ? -HUGE_VAL : HUGE_VAL;
    }

    const std::uint64_t factor_significand = significands_table::significand_64[power - smallest_power];
    const std::int64_t exponent = (((152170 + 65536) * power) >> 16) + 1024 + 63;
    int leading_zeros = boost::core::countl_zero(i);
    i <<= static_cast<std::uint64_t>(leading_zeros);

    uint128 product = umul128(i, factor_significand);
    std::uint64_t low = product.low;
    std::uint64_t high = product.high;

    // We know that upper has at most one leading zero because
    // both i and  factor_mantissa have a leading one. This means
    // that the result is at least as large as ((1<<63)*(1<<63))/(1<<64).
    // 
    // As long as the first 9 bits of "upper" are not "1", then we
    // know that we have an exact computed value for the leading
    // 55 bits because any imprecision would play out as a +1, in the worst case.
    // Having 55 bits is necessary because we need 53 bits for the mantissa,
    // but we have to have one rounding bit and, we can waste a bit if the most 
    // significant bit of the product is zero.
    // 
    // We expect this next branch to be rarely taken (say 1% of the time).
    // When (upper & 0x1FF) == 0x1FF, it can be common for
    // lower + i < lower to be true (proba. much higher than 1%).
    if (BOOST_UNLIKELY((high & 0x1FF) == 0x1FF) && (low + i < low))
    {
        const std::uint64_t factor_significand_low = significands_table::significand_128[power - smallest_power];
        product = umul128(i, factor_significand_low);
        //const std::uint64_t product_low = product.low;
        const std::uint64_t product_middle2 = product.high;
        const std::uint64_t product_middle1 = low;
        std::uint64_t product_high = high;
        const std::uint64_t product_middle = product_middle1 + product_middle2;

        if (product_middle < product_middle1)
        {
            product_high++;
        }

        // Commented out because possibly unneeded
        // See: https://arxiv.org/pdf/2212.06644.pdf
        /*
        // we want to check whether mantissa *i + i would affect our result
        // This does happen, e.g. with 7.3177701707893310e+15
        if (((product_middle + 1 == 0) && ((product_high & 0x1FF) == 0x1FF) && (product_low + i < product_low))) 
        {
            success = false;
            return 0;
        }
        */

        low = product_middle;
        high = product_high;
    }

    // The final significand should be 53 bits with a leading 1
    // We shift it so that it occupies 54 bits with a leading 1
    const std::uint64_t upper_bit = high >> 63;
    std::uint64_t significand = high >> (upper_bit + 9);
    leading_zeros += static_cast<int>(1 ^ upper_bit);

    // If we have lots of trailing zeros we may fall between two values
    if (BOOST_UNLIKELY((low == 0) && ((high & 0x1FF) == 0) && ((significand & 3) == 1)))
    {
        // if significand & 1 == 1 we might need to round up
        success = false;
        return 0;
    }

    significand += significand & 1;
    significand >>= 1;

    // Here the significand < (1<<53), unless there is an overflow
    if (significand >= (UINT64_C(1) << 53))
    {
        significand = (UINT64_C(1) << 52);
        leading_zeros--;
    }

    significand &= ~(UINT64_C(1) << 52);
    const auto real_exponent = static_cast<std::uint64_t>(exponent - leading_zeros);

    // We have to check that real_exponent is in range, otherwise fail
    if (BOOST_UNLIKELY((real_exponent < 1) || (real_exponent > 2046)))
    {
        success = false;
        return 0;
    }

    significand |= real_exponent << 52;
    significand |= ((static_cast<std::uint64_t>(negative) << 63));
    
    double d;
    std::memcpy(&d, &significand, sizeof(d));

    success = true;
    return d;
}

}}} // Namespaces

#endif // BOOST_CHARCONV_DETAIL_COMPUTE_FLOAT64_HPP
