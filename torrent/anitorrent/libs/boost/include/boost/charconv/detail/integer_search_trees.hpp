// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_INTEGER_SEARCH_TREES_HPP
#define BOOST_CHARCONV_DETAIL_INTEGER_SEARCH_TREES_HPP

// https://stackoverflow.com/questions/1489830/efficient-way-to-determine-number-of-digits-in-an-integer?page=1&tab=scoredesc#tab-top
// https://graphics.stanford.edu/~seander/bithacks.html

#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/emulated128.hpp>
#include <limits>
#include <array>
#include <cstdint>

namespace boost { namespace charconv { namespace detail {

// Generic solution
template <typename T>
BOOST_CHARCONV_CXX14_CONSTEXPR int num_digits(T x) noexcept
{
    int digits = 0;

    while (x)
    {
        x /= 10;
        ++digits;
    }

    return digits;
}

template <>
BOOST_CHARCONV_CXX14_CONSTEXPR int num_digits(std::uint32_t x) noexcept
{
    if (x >= UINT32_C(10000))
    {
        if (x >= UINT32_C(10000000))
        {
            if (x >= UINT32_C(100000000))
            {
                if (x >= UINT32_C(1000000000))
                {
                    return 10;
                }
                return 9;
            }
            return 8;
        }

        else if (x >= UINT32_C(100000))
        {
            if (x >= UINT32_C(1000000))
            {
                return 7;
            }
            return 6;
        }
        return 5;
    }
    else if (x >= UINT32_C(100))
    {
        if (x >= UINT32_C(1000))
        {
            return 4;
        }
        return 3;
    }
    else if (x >= UINT32_C(10))
    {
        return 2;
    }

    return 1;
}

template <>
BOOST_CHARCONV_CXX14_CONSTEXPR int num_digits(std::uint64_t x) noexcept
{
    if (x >= UINT64_C(10000000000))
    {
        if (x >= UINT64_C(100000000000000))
        {
            if (x >= UINT64_C(10000000000000000))
            {
                if (x >= UINT64_C(100000000000000000)) 
                {
                    if (x >= UINT64_C(1000000000000000000))
                    {
                        if (x >= UINT64_C(10000000000000000000))
                        {
                            return 20;
                        }
                        return 19;
                    }
                    return 18;
                }
                return 17;
            }
            else if (x >= UINT64_C(1000000000000000))
            {
                return 16;
            }
            return 15;
        } 
        if (x >= UINT64_C(1000000000000))
        {
            if (x >= UINT64_C(10000000000000))
            {
                return 14;
            }
            return 13;
        }
        if (x >= UINT64_C(100000000000))
        {
            return 12;
        }
        return 11;
    }
    else if (x >= UINT64_C(100000))
    {
        if (x >= UINT64_C(10000000))
        {
            if (x >= UINT64_C(100000000))
            {
                if (x >= UINT64_C(1000000000))
                {
                    return 10;
                }
                return 9;
            }
            return 8;
        }
        if (x >= UINT64_C(1000000))
        {
            return 7;
        }
        return 6;
    }
    if (x >= UINT64_C(100))
    {
        if (x >= UINT64_C(1000))
        {
            if (x >= UINT64_C(10000))
            {
                return 5;
            }
            return 4;
        }
        return 3;
    }
    if (x >= UINT64_C(10))
    {
        return 2;
    }
    return 1;
}

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4307) // MSVC 14.1 warns of intergral constant overflow
#endif

BOOST_CHARCONV_CXX14_CONSTEXPR int num_digits(uint128 x) noexcept
{
    if (x.high == 0)
    {
        return num_digits(x.low);
    }

    BOOST_CHARCONV_CXX14_CONSTEXPR_NO_INLINE uint128 digits_39 = static_cast<uint128>(UINT64_C(10000000000000000000)) *
                                                                 static_cast<uint128>(UINT64_C(10000000000000000000));
    uint128 current_power_of_10 = digits_39;

    for (int i = 39; i > 0; --i)
    {
        if (x >= current_power_of_10)
        {
            return i;
        }

        current_power_of_10 /= 10U;
    }

    return 1;
}

#ifdef BOOST_MSVC
# pragma warning(pop)
#endif

#ifdef BOOST_CHARCONV_HAS_INT128
static constexpr std::array<std::uint64_t, 20> powers_of_10 =
{{
    UINT64_C(1), UINT64_C(10), UINT64_C(100), UINT64_C(1000), UINT64_C(10000), UINT64_C(100000), UINT64_C(1000000), 
    UINT64_C(10000000), UINT64_C(100000000), UINT64_C(1000000000), UINT64_C(10000000000), UINT64_C(100000000000), 
    UINT64_C(1000000000000), UINT64_C(10000000000000), UINT64_C(100000000000000), UINT64_C(1000000000000000), 
    UINT64_C(10000000000000000), UINT64_C(100000000000000000), UINT64_C(1000000000000000000), UINT64_C(10000000000000000000)
}};

// Assume that if someone is using 128 bit ints they are favoring the top end of the range
// Max value is 340,282,366,920,938,463,463,374,607,431,768,211,455 (39 digits)
BOOST_CHARCONV_CXX14_CONSTEXPR int num_digits(boost::uint128_type x) noexcept
{
    // There is no literal for boost::uint128_type, so we need to calculate them using the max value of the
    // std::uint64_t powers of 10
    constexpr boost::uint128_type digits_39 = static_cast<boost::uint128_type>(UINT64_C(10000000000000000000)) * 
                                              static_cast<boost::uint128_type>(UINT64_C(10000000000000000000));

    constexpr boost::uint128_type digits_38 = digits_39 / 10;
    constexpr boost::uint128_type digits_37 = digits_38 / 10;
    constexpr boost::uint128_type digits_36 = digits_37 / 10;
    constexpr boost::uint128_type digits_35 = digits_36 / 10;
    constexpr boost::uint128_type digits_34 = digits_35 / 10;
    constexpr boost::uint128_type digits_33 = digits_34 / 10;
    constexpr boost::uint128_type digits_32 = digits_33 / 10;
    constexpr boost::uint128_type digits_31 = digits_32 / 10;
    constexpr boost::uint128_type digits_30 = digits_31 / 10;
    constexpr boost::uint128_type digits_29 = digits_30 / 10;
    constexpr boost::uint128_type digits_28 = digits_29 / 10;
    constexpr boost::uint128_type digits_27 = digits_28 / 10;
    constexpr boost::uint128_type digits_26 = digits_27 / 10;
    constexpr boost::uint128_type digits_25 = digits_26 / 10;
    constexpr boost::uint128_type digits_24 = digits_25 / 10;
    constexpr boost::uint128_type digits_23 = digits_24 / 10;
    constexpr boost::uint128_type digits_22 = digits_23 / 10;
    constexpr boost::uint128_type digits_21 = digits_22 / 10;

    return (x >= digits_39) ? 39 :
           (x >= digits_38) ? 38 :
           (x >= digits_37) ? 37 :
           (x >= digits_36) ? 36 :
           (x >= digits_35) ? 35 :
           (x >= digits_34) ? 34 :
           (x >= digits_33) ? 33 :
           (x >= digits_32) ? 32 :
           (x >= digits_31) ? 31 :
           (x >= digits_30) ? 30 :
           (x >= digits_29) ? 29 :
           (x >= digits_28) ? 28 :
           (x >= digits_27) ? 27 :
           (x >= digits_26) ? 26 :
           (x >= digits_25) ? 25 :
           (x >= digits_24) ? 24 :
           (x >= digits_23) ? 23 :
           (x >= digits_22) ? 22 :
           (x >= digits_21) ? 21 :
           (x >= powers_of_10[19]) ? 20 :
           (x >= powers_of_10[18]) ? 19 :
           (x >= powers_of_10[17]) ? 18 :
           (x >= powers_of_10[16]) ? 17 :
           (x >= powers_of_10[15]) ? 16 :
           (x >= powers_of_10[14]) ? 15 :
           (x >= powers_of_10[13]) ? 14 :
           (x >= powers_of_10[12]) ? 13 :
           (x >= powers_of_10[11]) ? 12 :
           (x >= powers_of_10[10]) ? 11 :
           (x >= powers_of_10[9])  ? 10 :
           (x >= powers_of_10[8])  ?  9 :
           (x >= powers_of_10[7])  ?  8 :
           (x >= powers_of_10[6])  ?  7 :
           (x >= powers_of_10[5])  ?  6 :
           (x >= powers_of_10[4])  ?  5 :
           (x >= powers_of_10[3])  ?  4 :
           (x >= powers_of_10[2])  ?  3 :
           (x >= powers_of_10[1])  ?  2 :
           (x >= powers_of_10[0])  ?  1 : 0;
}
#endif

}}} // Namespace boost::charconv::detail

#endif // BOOST_CHARCONV_DETAIL_INTEGER_SEARCH_TREES_HPP
