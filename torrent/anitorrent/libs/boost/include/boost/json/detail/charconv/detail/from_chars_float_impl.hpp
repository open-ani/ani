// Copyright 2022 Peter Dimov
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_JSON_DETAIL_CHARCONV_DETAIL_FROM_CHARS_FLOAT_IMPL_HPP
#define BOOST_JSON_DETAIL_CHARCONV_DETAIL_FROM_CHARS_FLOAT_IMPL_HPP

#include <boost/json/detail/charconv/detail/config.hpp>
#include <boost/json/detail/charconv/detail/from_chars_result.hpp>
#include <boost/json/detail/charconv/detail/parser.hpp>
#include <boost/json/detail/charconv/detail/compute_float64.hpp>
#include <boost/json/detail/charconv/chars_format.hpp>
#include <system_error>
#include <cstdlib>
#include <cmath>

namespace boost { namespace json { namespace detail { namespace charconv { namespace detail {

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4244) // Implict converion when BOOST_IF_CONSTEXPR expands to if
#elif defined(__GNUC__) && __GNUC__ < 5 && !defined(__clang__)
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

template <typename T>
from_chars_result from_chars_strtod_impl(const char* first, const char* last, T& value, char* buffer) noexcept
{
    // For strto(f/d)
    // Floating point value corresponding to the contents of str on success.
    // If the converted value falls out of range of corresponding return type, range error occurs and HUGE_VAL, HUGE_VALF or HUGE_VALL is returned.
    // If no conversion can be performed, 0 is returned and *str_end is set to str.

    std::memcpy(buffer, first, static_cast<std::size_t>(last - first));
    buffer[last - first] = '\0';

    char* str_end;
    T return_value {};
    BOOST_IF_CONSTEXPR (std::is_same<T, float>::value)
    {
        return_value = std::strtof(buffer, &str_end);
        if (return_value == HUGE_VALF)
        {
            return {last, std::errc::result_out_of_range};
        }
    }
    else BOOST_IF_CONSTEXPR (std::is_same<T, double>::value)
    {
        return_value = std::strtod(buffer, &str_end);
        if (return_value == HUGE_VAL)
        {
            return {last, std::errc::result_out_of_range};
        }
    }
    else
    {
        return_value = std::strtold(buffer, &str_end);
        if (return_value == HUGE_VALL)
        {
            return {last, std::errc::result_out_of_range};
        }
    }

    // Since this is a fallback routine we are safe to check for 0
    if (return_value == 0 && str_end == last)
    {
        return {first, std::errc::result_out_of_range};
    }

    value = return_value;
    return {first + (str_end - buffer), std::errc()};
}

template <typename T>
inline from_chars_result from_chars_strtod(const char* first, const char* last, T& value) noexcept
{
    if (last - first < 1024)
    {
        char buffer[1024];
        return from_chars_strtod_impl(first, last, value, buffer);
    }

    // If the string to be parsed does not fit into the 1024 byte static buffer than we have to allocate a buffer.
    // malloc is used here because it does not throw on allocation failure.

    char* buffer = static_cast<char*>(std::malloc(last - first + 1));
    if (buffer == nullptr)
    {
        return {first, std::errc::not_enough_memory};
    }

    auto r = from_chars_strtod_impl(first, last, value, buffer);
    std::free(buffer);

    return r;
}

template <typename T>
from_chars_result from_chars_float_impl(const char* first, const char* last, T& value, chars_format fmt) noexcept
{
    bool sign {};
    std::uint64_t significand {};
    std::int64_t  exponent {};

    auto r = charconv::detail::parser(first, last, sign, significand, exponent, fmt);
    if (r.ec != std::errc())
    {
        return r;
    }
    else if (significand == 0)
    {
        value = sign ? static_cast<T>(-0.0L) : static_cast<T>(0.0L);
        return r;
    }
    else if (exponent == -1)
    {
        // A full length significand e.g. -1985444280612224 with a power of -1 sometimes
        // fails in compute_float64 but is trivial to calculate
        // Found investigating GitHub issue #47
        value = (sign ? -static_cast<T>(significand) : static_cast<T>(significand)) / 10;
    }

    bool success {};
    T return_val {};
    return_val = compute_float64(exponent, significand, sign, success);

    if (!success)
    {
        if (significand == 1 && exponent == 0)
        {
            value = 1;
            r.ptr = last;
            r.ec = std::errc();
        }
        else
        {
            if (return_val == HUGE_VAL || return_val == -HUGE_VAL)
            {
                value = return_val;
                r.ec = std::errc::result_out_of_range;
            }
            else if (exponent < -342)
            {
                value = sign ? -0.0 : 0.0;
                r.ec = std::errc::result_out_of_range;
            }
            else
            {
                r = from_chars_strtod(first, r.ptr, value);
            }
        }
    }
    else
    {
        value = return_val;
    }

    return r;
}


#ifdef BOOST_MSVC
# pragma warning(pop)
#elif defined(__GNUC__) && __GNUC__ < 5 && !defined(__clang__)
# pragma GCC diagnostic pop
#endif

}}}}} // Namespace boost::charconv::detail

#endif // BOOST_JSON_DETAIL_CHARCONV_DETAIL_FROM_CHARS_FLOAT_IMPL_HPP
