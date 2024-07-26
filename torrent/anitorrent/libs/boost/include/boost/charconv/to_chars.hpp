// Copyright 2022 Peter Dimov
// Copyright 2023 Matt Borland
// Copyright 2023 Junekey Jeon
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_TO_CHARS_HPP_INCLUDED
#define BOOST_CHARCONV_TO_CHARS_HPP_INCLUDED

#include <boost/charconv/detail/to_chars_integer_impl.hpp>
#include <boost/charconv/detail/to_chars_result.hpp>
#include <boost/charconv/config.hpp>
#include <boost/charconv/chars_format.hpp>

namespace boost {
namespace charconv {

// integer overloads
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, bool value, int base) noexcept = delete;
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, char value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, signed char value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, unsigned char value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, short value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, unsigned short value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, int value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, unsigned int value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, long value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, unsigned long value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, long long value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, unsigned long long value, int base = 10) noexcept
{
    return detail::to_chars_int(first, last, value, base);
}

#ifdef BOOST_CHARCONV_HAS_INT128
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, boost::int128_type value, int base = 10) noexcept
{
    return detail::to_chars128(first, last, value, base);
}
BOOST_CHARCONV_CONSTEXPR to_chars_result to_chars(char* first, char* last, boost::uint128_type value, int base = 10) noexcept
{
    return detail::to_chars128(first, last, value, base);
}
#endif

//----------------------------------------------------------------------------------------------------------------------
// Floating Point
//----------------------------------------------------------------------------------------------------------------------

BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, float value,
                                             chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, double value,
                                             chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, long double value,
                                             chars_format fmt = chars_format::general) noexcept;

BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, float value,
                                             chars_format fmt, int precision) noexcept;
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, double value, 
                                             chars_format fmt, int precision) noexcept;
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, long double value,
                                             chars_format fmt, int precision) noexcept;

#ifdef BOOST_CHARCONV_HAS_FLOAT128
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, __float128 value,
                                             chars_format fmt = chars_format::general) noexcept;

BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, __float128 value,
                                             chars_format fmt, int precision) noexcept;
#endif

#ifdef BOOST_CHARCONV_HAS_FLOAT16
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::float16_t value,
                                             chars_format fmt = chars_format::general) noexcept;

BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::float16_t value, 
                                             chars_format fmt, int precision) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT32
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::float32_t value,
                                             chars_format fmt = chars_format::general) noexcept;

BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::float32_t value, 
                                             chars_format fmt, int precision) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT64
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::float64_t value,
                                             chars_format fmt = chars_format::general) noexcept;

BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::float64_t value, 
                                             chars_format fmt, int precision) noexcept;
#endif
#if defined(BOOST_CHARCONV_HAS_STDFLOAT128) && defined(BOOST_CHARCONV_HAS_FLOAT128)
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::float128_t value,
                                             chars_format fmt = chars_format::general) noexcept;

BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::float128_t value,
                                             chars_format fmt, int precision) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_BRAINFLOAT16
BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::bfloat16_t value,
                                             chars_format fmt = chars_format::general) noexcept;

BOOST_CHARCONV_DECL to_chars_result to_chars(char* first, char* last, std::bfloat16_t value, 
                                             chars_format fmt, int precision) noexcept;
#endif

} // namespace charconv
} // namespace boost

#endif // #ifndef BOOST_CHARCONV_TO_CHARS_HPP_INCLUDED
