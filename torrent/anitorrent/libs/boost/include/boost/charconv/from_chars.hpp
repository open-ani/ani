// Copyright 2022 Peter Dimov
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_FROM_CHARS_HPP_INCLUDED
#define BOOST_CHARCONV_FROM_CHARS_HPP_INCLUDED

#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/from_chars_result.hpp>
#include <boost/charconv/detail/from_chars_integer_impl.hpp>
#include <boost/charconv/detail/bit_layouts.hpp>
#include <boost/charconv/config.hpp>
#include <boost/charconv/chars_format.hpp>
#include <boost/core/detail/string_view.hpp>
#include <system_error>

namespace boost { namespace charconv {

// integer overloads

BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, bool& value, int base = 10) noexcept = delete;
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, char& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, signed char& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, unsigned char& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, short& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, unsigned short& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, int& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, unsigned int& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, long& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, unsigned long& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, long long& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, unsigned long long& value, int base = 10) noexcept
{
    return detail::from_chars(first, last, value, base);
}

#ifdef BOOST_CHARCONV_HAS_INT128
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, boost::int128_type& value, int base = 10) noexcept
{
    return detail::from_chars128(first, last, value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(const char* first, const char* last, boost::uint128_type& value, int base = 10) noexcept
{
    return detail::from_chars128(first, last, value, base);
}
#endif

BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, bool& value, int base = 10) noexcept = delete;
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, char& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, signed char& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, unsigned char& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, short& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, unsigned short& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, int& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, unsigned int& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, long& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, unsigned long& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, long long& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, unsigned long long& value, int base = 10) noexcept
{
    return detail::from_chars(sv.data(), sv.data() + sv.size(), value, base);
}

#ifdef BOOST_CHARCONV_HAS_INT128
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, boost::int128_type& value, int base = 10) noexcept
{
    return detail::from_chars128(sv.data(), sv.data() + sv.size(), value, base);
}
BOOST_CHARCONV_GCC5_CONSTEXPR from_chars_result from_chars(boost::core::string_view sv, boost::uint128_type& value, int base = 10) noexcept
{
    return detail::from_chars128(sv.data(), sv.data() + sv.size(), value, base);
}
#endif

//----------------------------------------------------------------------------------------------------------------------
// Floating Point
//----------------------------------------------------------------------------------------------------------------------

BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, float& value, chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, double& value, chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, long double& value, chars_format fmt = chars_format::general) noexcept;

#ifdef BOOST_CHARCONV_HAS_FLOAT128
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, __float128& value, chars_format fmt = chars_format::general) noexcept;
#endif

// <stdfloat> types
#ifdef BOOST_CHARCONV_HAS_FLOAT16
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, std::float16_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT32
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, std::float32_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT64
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, std::float64_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#if defined(BOOST_CHARCONV_HAS_STDFLOAT128) && defined(BOOST_CHARCONV_HAS_FLOAT128)
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, std::float128_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_BRAINFLOAT16
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(const char* first, const char* last, std::bfloat16_t& value, chars_format fmt = chars_format::general) noexcept;
#endif

BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, float& value, chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, double& value, chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, long double& value, chars_format fmt = chars_format::general) noexcept;

#ifdef BOOST_CHARCONV_HAS_FLOAT128
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, __float128& value, chars_format fmt = chars_format::general) noexcept;
#endif

// <stdfloat> types
#ifdef BOOST_CHARCONV_HAS_FLOAT16
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, std::float16_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT32
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, std::float32_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT64
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, std::float64_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#if defined(BOOST_CHARCONV_HAS_STDFLOAT128) && defined(BOOST_CHARCONV_HAS_FLOAT128)
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, std::float128_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_BRAINFLOAT16
BOOST_CHARCONV_DECL from_chars_result from_chars_erange(boost::core::string_view sv, std::bfloat16_t& value, chars_format fmt = chars_format::general) noexcept;
#endif

// The following adhere to the standard library definition with std::errc::result_out_of_range
// Returns value unmodified
// See: https://github.com/cppalliance/charconv/issues/110

BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, float& value, chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, double& value, chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, long double& value, chars_format fmt = chars_format::general) noexcept;

#ifdef BOOST_CHARCONV_HAS_FLOAT128
BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, __float128& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT16
BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, std::float16_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT32
BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, std::float32_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT64
BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, std::float64_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#if defined(BOOST_CHARCONV_HAS_STDFLOAT128) && defined(BOOST_CHARCONV_HAS_FLOAT128)
BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, std::float128_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_BRAINFLOAT16
BOOST_CHARCONV_DECL from_chars_result from_chars(const char* first, const char* last, std::bfloat16_t& value, chars_format fmt = chars_format::general) noexcept;
#endif

BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, float& value, chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, double& value, chars_format fmt = chars_format::general) noexcept;
BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, long double& value, chars_format fmt = chars_format::general) noexcept;

#ifdef BOOST_CHARCONV_HAS_FLOAT128
BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, __float128& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT16
BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, std::float16_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT32
BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, std::float32_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_FLOAT64
BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, std::float64_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#if defined(BOOST_CHARCONV_HAS_STDFLOAT128) && defined(BOOST_CHARCONV_HAS_FLOAT128)
BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, std::float128_t& value, chars_format fmt = chars_format::general) noexcept;
#endif
#ifdef BOOST_CHARCONV_HAS_BRAINFLOAT16
BOOST_CHARCONV_DECL from_chars_result from_chars(boost::core::string_view sv, std::bfloat16_t& value, chars_format fmt = chars_format::general) noexcept;
#endif

} // namespace charconv
} // namespace boost

#endif // #ifndef BOOST_CHARCONV_FROM_CHARS_HPP_INCLUDED
