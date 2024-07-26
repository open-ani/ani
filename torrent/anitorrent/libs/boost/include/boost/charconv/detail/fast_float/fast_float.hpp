// Copyright 2020-2023 Daniel Lemire
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt
//
// Derivative of: https://github.com/fastfloat/fast_float

#ifndef BOOST_CHARCONV_DETAIL_FASTFLOAT_FAST_FLOAT_HPP
#define BOOST_CHARCONV_DETAIL_FASTFLOAT_FAST_FLOAT_HPP

#include <boost/charconv/detail/fast_float/float_common.hpp>

namespace boost { namespace charconv { namespace detail { namespace fast_float {
/**
 * This function parses the character sequence [first,last) for a number. It parses floating-point numbers expecting
 * a locale-independent format equivalent to what is used by std::strtod in the default ("C") locale.
 * The resulting floating-point value is the closest floating-point values (using either float or double),
 * using the "round to even" convention for values that would otherwise fall right in-between two values.
 * That is, we provide exact parsing according to the IEEE standard.
 *
 * Given a successful parse, the pointer (`ptr`) in the returned value is set to point right after the
 * parsed number, and the `value` referenced is set to the parsed value. In case of error, the returned
 * `ec` contains a representative error, otherwise the default (`std::errc()`) value is stored.
 *
 * The implementation does not throw and does not allocate memory (e.g., with `new` or `malloc`).
 *
 * Like the C++17 standard, the `fast_float::from_chars` functions take an optional last argument of
 * the type `fast_float::chars_format`. It is a bitset value: we check whether
 * `fmt & fast_float::chars_format::fixed` and `fmt & fast_float::chars_format::scientific` are set
 * to determine whether we allow the fixed point and scientific notation respectively.
 * The default is  `fast_float::chars_format::general` which allows both `fixed` and `scientific`.
 */
template<typename T, typename UC = char>
BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20
from_chars_result_t<UC> from_chars(UC const * first, UC const * last,
                             T &value, chars_format fmt = chars_format::general)  noexcept;

/**
 * Like from_chars, but accepts an `options` argument to govern number parsing.
 */
template<typename T, typename UC = char>
BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20
from_chars_result_t<UC> from_chars_advanced(UC const * first, UC const * last,
                                      T &value, parse_options_t<UC> options)  noexcept;

}}}} // namespace fast_float
#include <boost/charconv/detail/fast_float/parse_number.hpp>
#endif // BOOST_CHARCONV_FASTFLOAT_FAST_FLOAT_H
