// Copyright 2024 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_GENERATE_NAN_HPP
#define BOOST_GENERATE_NAN_HPP

#include <cstdint>
#include <cstring>

#ifdef BOOST_CHARCONV_HAS_FLOAT128

namespace boost {
namespace charconv {
namespace detail {

struct words
{
#if BOOST_CHARCONV_ENDIAN_LITTLE_BYTE
    std::uint64_t lo;
    std::uint64_t hi;
#else
    std::uint64_t hi;
    std::uint64_t lo;
#endif
};

inline __float128 nans BOOST_PREVENT_MACRO_SUBSTITUTION () noexcept
{
    words bits;
    bits.hi = UINT64_C(0x7FFF400000000000);
    bits.lo = UINT64_C(0);

    __float128 return_val;
    std::memcpy(&return_val, &bits, sizeof(__float128));
    return return_val;
}

inline __float128 nanq BOOST_PREVENT_MACRO_SUBSTITUTION () noexcept
{
    words bits;
    bits.hi = UINT64_C(0x7FFF800000000000);
    bits.lo = UINT64_C(0);

    __float128 return_val;
    std::memcpy(&return_val, &bits, sizeof(__float128));
    return return_val;
}

} //namespace detail
} //namespace charconv
} //namespace boost

#endif

#endif //BOOST_GENERATE_NAN_HPP
