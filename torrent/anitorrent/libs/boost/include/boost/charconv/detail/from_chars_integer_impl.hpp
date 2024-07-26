// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_FROM_CHARS_INTEGER_IMPL_HPP
#define BOOST_CHARCONV_DETAIL_FROM_CHARS_INTEGER_IMPL_HPP

#include <boost/charconv/detail/apply_sign.hpp>
#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/from_chars_result.hpp>
#include <boost/charconv/detail/emulated128.hpp>
#include <boost/charconv/detail/type_traits.hpp>
#include <boost/charconv/config.hpp>
#include <boost/config.hpp>
#include <system_error>
#include <type_traits>
#include <limits>
#include <cstdlib>
#include <cerrno>
#include <cstddef>
#include <cstdint>

namespace boost { namespace charconv { namespace detail {

static constexpr unsigned char uchar_values[] =
     {255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
        0,   1,   2,   3,   4,   5,   6,   7,   8,   9, 255, 255, 255, 255, 255, 255,
      255,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,
       25,  26,  27,  28,  29,  30,  31,  32,  33,  34,  35, 255, 255, 255, 255, 255,
      255,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,
       25,  26,  27,  28,  29,  30,  31,  32,  33,  34,  35, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
      255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};

static_assert(sizeof(uchar_values) == 256, "uchar_values should represent all 256 values of unsigned char");

// Convert characters for 0-9, A-Z, a-z to 0-35. Anything else is 255
constexpr unsigned char digit_from_char(char val) noexcept
{
    return uchar_values[static_cast<unsigned char>(val)];
}

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4146) // unary minus operator applied to unsigned type, result still unsigned
# pragma warning(disable: 4189) // 'is_negative': local variable is initialized but not referenced

#elif defined(__clang__)
# pragma clang diagnostic push
# pragma clang diagnostic ignored "-Wconstant-conversion"

#elif defined(__GNUC__) && (__GNUC__ < 7)
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Woverflow"
# pragma GCC diagnostic ignored "-Wconversion"
# pragma GCC diagnostic ignored "-Wsign-conversion"

#elif defined(__GNUC__) && (__GNUC__ >= 7)
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Wmaybe-uninitialized"
# pragma GCC diagnostic ignored "-Wconversion"

#endif

template <typename Integer, typename Unsigned_Integer>
BOOST_CXX14_CONSTEXPR from_chars_result from_chars_integer_impl(const char* first, const char* last, Integer& value, int base) noexcept
{
    Unsigned_Integer result = 0;
    Unsigned_Integer overflow_value = 0;
    Unsigned_Integer max_digit = 0;
    
    // Check pre-conditions
    if (!((first <= last) && (base >= 2 && base <= 36)))
    {
        return {first, std::errc::invalid_argument};
    }

    const auto unsigned_base = static_cast<Unsigned_Integer>(base);

    // Strip sign if the type is signed
    // Negative sign will be appended at the end of parsing
    BOOST_ATTRIBUTE_UNUSED bool is_negative = false;
    auto next = first;

    BOOST_CHARCONV_IF_CONSTEXPR (is_signed<Integer>::value)
    {
        if (next != last)
        {
            if (*next == '-')
            {
                is_negative = true;
                ++next;
            }
            else if (*next == '+' || *next == ' ')
            {
                return {next, std::errc::invalid_argument};
            }
        }

        #ifdef BOOST_CHARCONV_HAS_INT128
        BOOST_IF_CONSTEXPR (std::is_same<Integer, boost::int128_type>::value)
        {
            overflow_value = BOOST_CHARCONV_INT128_MAX;
            max_digit = BOOST_CHARCONV_INT128_MAX;
        }
        else
        #endif
        {
            overflow_value = (std::numeric_limits<Integer>::max)();
            max_digit = (std::numeric_limits<Integer>::max)();
        }

        if (is_negative)
        {
            ++overflow_value;
            ++max_digit;
        }
    }
    else
    {
        if (next != last && (*next == '-' || *next == '+' || *next == ' '))
        {
            return {first, std::errc::invalid_argument};
        }
        
        #ifdef BOOST_CHARCONV_HAS_INT128
        BOOST_IF_CONSTEXPR (std::is_same<Integer, boost::uint128_type>::value)
        {
            overflow_value = BOOST_CHARCONV_UINT128_MAX;
            max_digit = BOOST_CHARCONV_UINT128_MAX;
        }
        else
        #endif
        {
            overflow_value = (std::numeric_limits<Unsigned_Integer>::max)();
            max_digit = (std::numeric_limits<Unsigned_Integer>::max)();
        }
    }

    #ifdef BOOST_CHARCONV_HAS_INT128
    BOOST_IF_CONSTEXPR (std::is_same<Integer, boost::int128_type>::value)
    {
        overflow_value /= unsigned_base;
        max_digit %= unsigned_base;
        #ifndef __GLIBCXX_TYPE_INT_N_0
        if (base != 10)
        {
            // Overflow value would cause INT128_MIN in non-base10 to fail
            overflow_value *= static_cast<Unsigned_Integer>(2);
        }
        #endif
    }
    else
    #endif
    {
        overflow_value /= unsigned_base;
        max_digit %= unsigned_base;
    }

    // If the only character was a sign abort now
    if (next == last)
    {
        return {first, std::errc::invalid_argument};
    }

    bool overflowed = false;

    const std::ptrdiff_t nc = last - next;

    // In non-GNU mode on GCC numeric limits may not be specialized
    #if defined(BOOST_CHARCONV_HAS_INT128) && !defined(__GLIBCXX_TYPE_INT_N_0)
    constexpr std::ptrdiff_t nd = std::is_same<Integer, boost::int128_type>::value ? 38 :
                                  std::is_same<Integer, boost::uint128_type>::value ? 38 :
                                  std::numeric_limits<Integer>::digits10;
    #else
    constexpr std::ptrdiff_t nd = std::numeric_limits<Integer>::digits10;
    #endif

    {
        std::ptrdiff_t i = 0;

        for( ; i < nd && i < nc; ++i )
        {
            // overflow is not possible in the first nd characters

            const unsigned char current_digit = digit_from_char(*next);

            if (current_digit >= unsigned_base)
            {
                break;
            }

            result = static_cast<Unsigned_Integer>(result * unsigned_base + current_digit);
            ++next;
        }

        for( ; i < nc; ++i )
        {
            const unsigned char current_digit = digit_from_char(*next);

            if (current_digit >= unsigned_base)
            {
                break;
            }

            if (result < overflow_value || (result == overflow_value && current_digit <= max_digit))
            {
                result = static_cast<Unsigned_Integer>(result * unsigned_base + current_digit);
            }
            else
            {
                // Required to keep updating the value of next, but the result is garbage
                overflowed = true;
            }

            ++next;
        }
    }

    // Return the parsed value, adding the sign back if applicable
    // If we have overflowed then we do not return the result 
    if (overflowed)
    {
        return {next, std::errc::result_out_of_range};
    }

    value = static_cast<Integer>(result);

    BOOST_IF_CONSTEXPR (is_signed<Integer>::value)
    {
        if (is_negative)
        {
            value = static_cast<Integer>(-(static_cast<Unsigned_Integer>(value)));
        }
    }

    return {next, std::errc()};
}

#ifdef BOOST_MSVC
# pragma warning(pop)
#elif defined(__clang__)
# pragma clang diagnostic pop
#elif defined(__GNUC__)
# pragma GCC diagnostic pop
#endif

// Only from_chars for integer types is constexpr (as of C++23)
template <typename Integer>
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, Integer& value, int base = 10) noexcept
{
    using Unsigned_Integer = typename std::make_unsigned<Integer>::type;
    return detail::from_chars_integer_impl<Integer, Unsigned_Integer>(first, last, value, base);
}

#ifdef BOOST_CHARCONV_HAS_INT128
template <typename Integer>
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars128(const char* first, const char* last, Integer& value, int base = 10) noexcept
{
    using Unsigned_Integer = boost::uint128_type;
    return detail::from_chars_integer_impl<Integer, Unsigned_Integer>(first, last, value, base);
}
#endif

BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars128(const char* first, const char* last, uint128& value, int base = 10) noexcept
{
    return from_chars_integer_impl<uint128, uint128>(first, last, value, base);
}

}}} // Namespaces

#endif // BOOST_CHARCONV_DETAIL_FROM_CHARS_INTEGER_IMPL_HPP
