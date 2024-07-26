// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_ISSIGNALING_HPP
#define BOOST_CHARCONV_DETAIL_ISSIGNALING_HPP

#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/bit_layouts.hpp>
#include <cstdint>
#include <cstring>

namespace boost { namespace charconv { namespace detail {

template <typename T>
inline bool issignaling BOOST_PREVENT_MACRO_SUBSTITUTION (T x) noexcept;

#if BOOST_CHARCONV_LDBL_BITS == 128 || defined(BOOST_CHARCONV_HAS_FLOAT128)

struct words128
{
#if BOOST_CHARCONV_ENDIAN_LITTLE_BYTE
    std::uint64_t lo;
    std::uint64_t hi;
#else
    std::uint64_t hi;
    std::uint64_t lo;
#endif
};

template <typename T>
inline bool issignaling BOOST_PREVENT_MACRO_SUBSTITUTION (T x) noexcept
{
    words128 bits;
    std::memcpy(&bits, &x, sizeof(T));

    std::uint64_t hi_word = bits.hi;
    std::uint64_t lo_word = bits.lo;

    hi_word ^= UINT64_C(0x0000800000000000);
    hi_word |= (lo_word | -lo_word) >> 63;
    return ((hi_word & INT64_MAX) > UINT64_C(0x7FFF800000000000));
}

#endif

}}} // Namespaces

#endif // BOOST_CHARCONV_DETAIL_ISSIGNALING_HPP
