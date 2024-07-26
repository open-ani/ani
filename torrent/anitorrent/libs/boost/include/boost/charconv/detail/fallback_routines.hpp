// Copyright 2024 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_FALLBACK_ROUTINES_HPP
#define BOOST_FALLBACK_ROUTINES_HPP

#include <boost/charconv/detail/to_chars_integer_impl.hpp>
#include <boost/charconv/detail/dragonbox/floff.hpp>
#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/from_chars_result.hpp>
#include <boost/charconv/chars_format.hpp>
#include <system_error>
#include <type_traits>
#include <locale>
#include <clocale>
#include <cstring>
#include <cstdio>

namespace boost {
namespace charconv {
namespace detail {

#ifdef BOOST_CHARCONV_HAS_FLOAT128
inline int print_val(char* first, std::size_t size, char* format, __float128 value) noexcept
{
    return quadmath_snprintf(first, size, format, value);
}
#endif

template <typename T>
inline int print_val(char* first, std::size_t size, char* format, T value) noexcept
{
    return std::snprintf(first, size, format, value);
}

template <typename T>
to_chars_result to_chars_printf_impl(char* first, char* last, T value, chars_format fmt, int precision)
{
    // v % + . + num_digits(INT_MAX) + specifier + null terminator
    // 1 + 1 + 10 + 1 + 1
    char format[14] {};
    std::memcpy(format, "%", 1); // NOLINT : No null terminator is purposeful
    std::size_t pos = 1;

    // precision of -1 is unspecified
    if (precision != -1 && fmt != chars_format::fixed)
    {
        format[pos] = '.';
        ++pos;
        const auto unsigned_precision = static_cast<std::uint32_t>(precision);
        if (unsigned_precision < 10)
        {
            boost::charconv::detail::print_1_digit(unsigned_precision, format + pos);
            ++pos;
        }
        else if (unsigned_precision < 100)
        {
            boost::charconv::detail::print_2_digits(unsigned_precision, format + pos);
            pos += 2;
        }
        else
        {
            boost::charconv::detail::to_chars_int(format + pos, format + sizeof(format), precision);
            pos = std::strlen(format);
        }
    }
    else if (fmt == chars_format::fixed)
    {
        // Force 0 decimal places
        std::memcpy(format + pos, ".0", 2); // NOLINT : No null terminator is purposeful
        pos += 2;
    }

    // Add the type identifier
    #ifdef BOOST_CHARCONV_HAS_FLOAT128
    BOOST_CHARCONV_IF_CONSTEXPR (std::is_same<T, __float128>::value || std::is_same<T, long double>::value)
    {
        format[pos] = std::is_same<T, __float128>::value ? 'Q' : 'L';
        ++pos;
    }
    #else
    BOOST_CHARCONV_IF_CONSTEXPR (std::is_same<T, long double>::value)
    {
        format[pos] = 'L';
        ++pos;
    }
    #endif

    // Add the format character
    switch (fmt)
    {
        case boost::charconv::chars_format::general:
            format[pos] = 'g';
            break;

        case boost::charconv::chars_format::scientific:
            format[pos] = 'e';
            break;

        case boost::charconv::chars_format::fixed:
            format[pos] = 'f';
            break;

        case boost::charconv::chars_format::hex:
            format[pos] = 'a';
            break;
    }

    const auto rv = print_val(first, static_cast<std::size_t>(last - first), format, value);

    if (rv <= 0)
    {
        return {last, static_cast<std::errc>(errno)};
    }

    return {first + rv, std::errc()};
}

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4244) // Implict converion when BOOST_IF_CONSTEXPR expands to if
#elif defined(__GNUC__) && __GNUC__ >= 5
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Wmissing-field-initializers"
# pragma GCC diagnostic ignored "-Wfloat-conversion"
#elif defined(__clang__) && __clang_major__ > 7
# pragma clang diagnostic push
# pragma clang diagnostic ignored "-Wimplicit-float-conversion"
#elif defined(__clang__) && __clang_major__ <= 7
# pragma clang diagnostic push
# pragma clang diagnostic ignored "-Wconversion"
#endif

// We know that the string is in the "C" locale because it would have previously passed through our parser.
// Convert the string into the current locale so that the strto* family of functions
// works correctly for the given locale.
//
// We are operating on our own copy of the buffer, so we are free to modify it.
inline void convert_string_locale(char* buffer) noexcept
{
    const auto locale_decimal_point = *std::localeconv()->decimal_point;
    if (locale_decimal_point != '.')
    {
        auto p = std::strchr(buffer, '.');
        if (p != nullptr)
        {
            *p = locale_decimal_point;
        }
    }
}

template <typename T>
from_chars_result from_chars_strtod_impl(const char* first, const char* last, T& value, char* buffer) noexcept
{
    // For strto(f/d)
    // Floating point value corresponding to the contents of str on success.
    // If the converted value falls out of range of corresponding return type, range error occurs and HUGE_VAL, HUGE_VALF or HUGE_VALL is returned.
    // If no conversion can be performed, 0 is returned and *str_end is set to str.

    std::memcpy(buffer, first, static_cast<std::size_t>(last - first));
    buffer[last - first] = '\0';
    convert_string_locale(buffer);

    char* str_end;
    T return_value {};
    from_chars_result r {nullptr, std::errc()};

    BOOST_IF_CONSTEXPR (std::is_same<T, float>::value)
    {
        return_value = std::strtof(buffer, &str_end);

        #ifndef __INTEL_LLVM_COMPILER
        if (return_value == HUGE_VALF)
                #else
            if (return_value >= std::numeric_limits<T>::max())
                #endif
        {
            r = {last, std::errc::result_out_of_range};
        }
    }
    else BOOST_IF_CONSTEXPR (std::is_same<T, double>::value)
    {
        return_value = std::strtod(buffer, &str_end);

        #ifndef __INTEL_LLVM_COMPILER
        if (return_value == HUGE_VAL)
                #else
            if (return_value >= std::numeric_limits<T>::max())
                #endif
        {
            r = {last, std::errc::result_out_of_range};
        }
    }
    else BOOST_IF_CONSTEXPR (std::is_same<T, long double>::value)
    {
        return_value = std::strtold(buffer, &str_end);

        #ifndef __INTEL_LLVM_COMPILER
        if (return_value == HUGE_VALL)
                #else
            if (return_value >= std::numeric_limits<T>::max())
                #endif
        {
            r = {last, std::errc::result_out_of_range};
        }
    }
    #ifdef BOOST_CHARCONV_HAS_FLOAT128
    else
    {
        return_value = strtoflt128(buffer, &str_end);

        if (return_value == HUGE_VALQ)
        {
            r = {last, std::errc::result_out_of_range};
        }
    }
    #endif

    // Since this is a fallback routine we are safe to check for 0
    if (return_value == 0 && str_end == last)
    {
        r = {first, std::errc::result_out_of_range};
    }

    if (r)
    {
        value = return_value;
        r = {first + (str_end - buffer), std::errc()};
    }

    return r;
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

    char* buffer = static_cast<char*>(std::malloc(static_cast<std::size_t>(last - first + 1)));
    if (buffer == nullptr)
    {
        return {first, std::errc::not_enough_memory};
    }

    auto r = from_chars_strtod_impl(first, last, value, buffer);
    std::free(buffer);

    return r;
}

#ifdef BOOST_MSVC
# pragma warning(pop)
#elif defined(__GNUC__) && __GNUC__ >= 5
# pragma GCC diagnostic pop
#elif defined(__clang__)
# pragma clang diagnostic pop
#endif

} //namespace detail
} //namespace charconv
} //namespace boost

#endif //BOOST_FALLBACK_ROUTINES_HPP
