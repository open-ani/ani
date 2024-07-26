// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_COMPUTE_FLOAT32_HPP
#define BOOST_CHARCONV_DETAIL_COMPUTE_FLOAT32_HPP

#include <boost/charconv/detail/compute_float64.hpp>
#include <limits>
#include <cstdint>
#include <cmath>

namespace boost { namespace charconv { namespace detail {

inline float compute_float32(std::int64_t power, std::uint64_t i, bool negative, bool& success) noexcept
{
    const double d = compute_float64(power, i, negative, success);
    float return_val;

    if (success)
    {
        // Some compilers (e.g. Intel) will optimize std::isinf to always false depending on compiler flags
        //
        // From Intel(R) oneAPI DPC++/C++ Compiler 2023.0.0 (2023.0.0.20221201)
        // warning: comparison with infinity always evaluates to false in fast floating point modes [-Wtautological-constant-compare]
        // if (std::isinf(return_val))
        if (d > static_cast<double>((std::numeric_limits<float>::max)()) ||
            d < static_cast<double>((std::numeric_limits<float>::lowest)()))
        {
            return_val = negative ? -HUGE_VALF : HUGE_VALF;
            success = false;
        }
        else
        {
            return_val = static_cast<float>(d);
        }
    }
    else
    {
        if (power > 38)
        {
            return_val = negative ? -HUGE_VALF : HUGE_VALF;
        }
        else
        {
            return_val = negative ? -0.0F : 0.0F;
        }
    }

    return return_val;
}

}}} // Namespaces

#endif // BOOST_CHARCONV_DETAIL_COMPUTE_FLOAT32_HPP
