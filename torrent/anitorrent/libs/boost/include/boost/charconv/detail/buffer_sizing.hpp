// Copyright 2024 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_BUFFER_SIZING_HPP
#define BOOST_CHARCONV_DETAIL_BUFFER_SIZING_HPP

#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/integer_search_trees.hpp>
#include <type_traits>

namespace boost {
namespace charconv {
namespace detail {

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4127) // Conditional expression for BOOST_IF_CONSTEXPR will be constant in not C++17
#endif

template <typename Real>
inline int get_real_precision(int precision = -1) noexcept
{
    // If the user did not specify a precision than we use the maximum representable amount
    // and remove trailing zeros at the end

    int real_precision;
    BOOST_IF_CONSTEXPR (std::is_same<Real, float>::value || std::is_same<Real, double>::value)
    {
        real_precision = precision == -1 ? std::numeric_limits<Real>::max_digits10 : precision;
    }
    else
    {
        #ifdef BOOST_CHARCONV_HAS_FLOAT128
        BOOST_CHARCONV_IF_CONSTEXPR (std::is_same<Real, __float128>::value)
        {
            real_precision = 33;
        }
        else
        #endif
        {
            #if BOOST_CHARCONV_LDBL_BITS == 128
            real_precision = 33;
            #else
            real_precision = 18;
            #endif
        }
    }

    return real_precision;
}

template <typename Int>
inline int total_buffer_length(int real_precision, Int exp, bool signed_value)
{
    // Sign + integer part + '.' + precision of fraction part + e+/e- or p+/p- + exponent digits
    return static_cast<int>(signed_value) + 1 + real_precision + 2 + num_digits(exp);
}

#ifdef BOOST_MSVC
# pragma warning(pop)
#endif

} //namespace detail
} //namespace charconv
} //namespace boost

#endif //BOOST_CHARCONV_DETAIL_BUFFER_SIZING_HPP
