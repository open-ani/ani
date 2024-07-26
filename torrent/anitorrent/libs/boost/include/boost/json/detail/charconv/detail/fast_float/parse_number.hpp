// Copyright 2020-2023 Daniel Lemire
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt
//
// Derivative of: https://github.com/fastfloat/fast_float

#ifndef BOOST_JSON_DETAIL_CHARCONV_DETAIL_FASTFLOAT_PARSE_NUMBER_HPP
#define BOOST_JSON_DETAIL_CHARCONV_DETAIL_FASTFLOAT_PARSE_NUMBER_HPP

#include <boost/json/detail/charconv/detail/fast_float/ascii_number.hpp>
#include <boost/json/detail/charconv/detail/fast_float/decimal_to_binary.hpp>
#include <boost/json/detail/charconv/detail/fast_float/digit_comparison.hpp>
#include <boost/json/detail/charconv/detail/fast_float/float_common.hpp>

#include <cmath>
#include <cstring>
#include <limits>
#include <system_error>

namespace boost { namespace json { namespace detail { namespace charconv { namespace detail { namespace fast_float {


namespace detail {
/**
 * Special case +inf, -inf, nan, infinity, -infinity.
 * The case comparisons could be made much faster given that we know that the
 * strings a null-free and fixed.
 **/

#if defined(__GNUC__) && __GNUC__ < 5 && !defined(__clang__)
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

template <typename T, typename UC>
from_chars_result_t<UC> BOOST_JSON_CXX14_CONSTEXPR
parse_infnan(UC const * first, UC const * last, T &value)  noexcept  {
  from_chars_result_t<UC> answer{};
  answer.ptr = first;
  answer.ec = std::errc(); // be optimistic
  bool minusSign = false;
  if (*first == UC('-')) { // assume first < last, so dereference without checks; C++17 20.19.3.(7.1) explicitly forbids '+' here
      minusSign = true;
      ++first;
  }
  if (last - first >= 3) {
    if (fastfloat_strncasecmp(first, str_const_nan<UC>(), 3)) {
      answer.ptr = (first += 3);
      value = minusSign ? -std::numeric_limits<T>::quiet_NaN() : std::numeric_limits<T>::quiet_NaN();
      // Check for possible nan(n-char-seq-opt), C++17 20.19.3.7, C11 7.20.1.3.3. At least MSVC produces nan(ind) and nan(snan).
      if(first != last && *first == UC('(')) {
        for(UC const * ptr = first + 1; ptr != last; ++ptr) {
          if (*ptr == UC(')')) {
            answer.ptr = ptr + 1; // valid nan(n-char-seq-opt)
            break;
          }
          else if(!((UC('a') <= *ptr && *ptr <= UC('z')) || (UC('A') <= *ptr && *ptr <= UC('Z')) || (UC('0') <= *ptr && *ptr <= UC('9')) || *ptr == UC('_')))
            break; // forbidden char, not nan(n-char-seq-opt)
        }
      }
      return answer;
    }
    if (fastfloat_strncasecmp(first, str_const_inf<UC>(), 3)) {
      if ((last - first >= 8) && fastfloat_strncasecmp(first + 3, str_const_inf<UC>() + 3, 5)) {
        answer.ptr = first + 8;
      } else {
        answer.ptr = first + 3;
      }
      value = minusSign ? -std::numeric_limits<T>::infinity() : std::numeric_limits<T>::infinity();
      return answer;
    }
  }
  answer.ec = std::errc::invalid_argument;
  return answer;
}

#if defined(__GNUC__) && __GNUC__ < 5 && !defined(__clang__)
# pragma GCC diagnostic pop
#endif

/**
 * Returns true if the floating-pointing rounding mode is to 'nearest'.
 * It is the default on most system. This function is meant to be inexpensive.
 * Credit : @mwalcott3
 */
BOOST_FORCEINLINE bool rounds_to_nearest() noexcept {
  // https://lemire.me/blog/2020/06/26/gcc-not-nearest/
#if (FLT_EVAL_METHOD != 1) && (FLT_EVAL_METHOD != 0)
  return false;
#endif
  // See
  // A fast function to check your floating-point rounding mode
  // https://lemire.me/blog/2022/11/16/a-fast-function-to-check-your-floating-point-rounding-mode/
  //
  // This function is meant to be equivalent to :
  // prior: #include <cfenv>
  //  return fegetround() == FE_TONEAREST;
  // However, it is expected to be much faster than the fegetround()
  // function call.
  //
  // The volatile keywoard prevents the compiler from computing the function
  // at compile-time.
  // There might be other ways to prevent compile-time optimizations (e.g., asm).
  // The value does not need to be std::numeric_limits<float>::min(), any small
  // value so that 1 + x should round to 1 would do (after accounting for excess
  // precision, as in 387 instructions).
  static volatile float fmin = (std::numeric_limits<float>::min)();
  float fmini = fmin; // we copy it so that it gets loaded at most once.
  //
  // Explanation:
  // Only when fegetround() == FE_TONEAREST do we have that
  // fmin + 1.0f == 1.0f - fmin.
  //
  // FE_UPWARD:
  //  fmin + 1.0f > 1
  //  1.0f - fmin == 1
  //
  // FE_DOWNWARD or  FE_TOWARDZERO:
  //  fmin + 1.0f == 1
  //  1.0f - fmin < 1
  //
  // Note: This may fail to be accurate if fast-math has been
  // enabled, as rounding conventions may not apply.
  #ifdef BOOST_JSON_FASTFLOAT_VISUAL_STUDIO
  #   pragma warning(push)
  //  todo: is there a VS warning?
  //  see https://stackoverflow.com/questions/46079446/is-there-a-warning-for-floating-point-equality-checking-in-visual-studio-2013
  #elif defined(__clang__)
  #   pragma clang diagnostic push
  #   pragma clang diagnostic ignored "-Wfloat-equal"
  #elif defined(__GNUC__)
  #   pragma GCC diagnostic push
  #   pragma GCC diagnostic ignored "-Wfloat-equal"
  #endif
  return (fmini + 1.0f == 1.0f - fmini);
  #ifdef BOOST_JSON_FASTFLOAT_VISUAL_STUDIO
  #   pragma warning(pop)
  #elif defined(__clang__)
  #   pragma clang diagnostic pop
  #elif defined(__GNUC__)
  #   pragma GCC diagnostic pop
  #endif
}

} // namespace detail

template<typename T, typename UC>
BOOST_JSON_FASTFLOAT_CONSTEXPR20
from_chars_result_t<UC> from_chars(UC const * first, UC const * last,
                             T &value, chars_format fmt /*= chars_format::general*/)  noexcept  {
  return from_chars_advanced(first, last, value, parse_options_t<UC>{fmt});
}

template<typename T, typename UC>
BOOST_JSON_FASTFLOAT_CONSTEXPR20
from_chars_result_t<UC> from_chars_advanced(UC const * first, UC const * last,
                                      T &value, parse_options_t<UC> options)  noexcept  {

  static_assert (std::is_same<T, double>::value || std::is_same<T, float>::value, "only float and double are supported");
  static_assert (std::is_same<UC, char>::value ||
                 std::is_same<UC, wchar_t>::value ||
                 std::is_same<UC, char16_t>::value ||
                 std::is_same<UC, char32_t>::value , "only char, wchar_t, char16_t and char32_t are supported");

  from_chars_result_t<UC> answer;
  if (first == last) {
    answer.ec = std::errc::invalid_argument;
    answer.ptr = first;
    return answer;
  }
  parsed_number_string_t<UC> pns = parse_number_string<UC>(first, last, options);
  if (!pns.valid) {
    return detail::parse_infnan(first, last, value);
  }
  answer.ec = std::errc(); // be optimistic
  answer.ptr = pns.lastmatch;
  // The implementation of the Clinger's fast path is convoluted because
  // we want round-to-nearest in all cases, irrespective of the rounding mode
  // selected on the thread.
  // We proceed optimistically, assuming that detail::rounds_to_nearest() returns
  // true.
  if (binary_format<T>::min_exponent_fast_path() <= pns.exponent && pns.exponent <= binary_format<T>::max_exponent_fast_path() && !pns.too_many_digits) {
    // Unfortunately, the conventional Clinger's fast path is only possible
    // when the system rounds to the nearest float.
    //
    // We expect the next branch to almost always be selected.
    // We could check it first (before the previous branch), but
    // there might be performance advantages at having the check
    // be last.
    if(!cpp20_and_in_constexpr() && detail::rounds_to_nearest())  {
      // We have that fegetround() == FE_TONEAREST.
      // Next is Clinger's fast path.
      if (pns.mantissa <=binary_format<T>::max_mantissa_fast_path()) {
        value = T(pns.mantissa);
        if (pns.exponent < 0) { value = value / binary_format<T>::exact_power_of_ten(-pns.exponent); }
        else { value = value * binary_format<T>::exact_power_of_ten(pns.exponent); }
        if (pns.negative) { value = -value; }
        return answer;
      }
    } else {
      // We do not have that fegetround() == FE_TONEAREST.
      // Next is a modified Clinger's fast path, inspired by Jakub Jelínek's proposal
      if (pns.exponent >= 0 && pns.mantissa <=binary_format<T>::max_mantissa_fast_path(pns.exponent)) {
#if defined(__clang__)
        // Clang may map 0 to -0.0 when fegetround() == FE_DOWNWARD
        if(pns.mantissa == 0) {
          value = pns.negative ? -0. : 0.;
          return answer;
        }
#endif
        value = T(pns.mantissa) * binary_format<T>::exact_power_of_ten(pns.exponent);
        if (pns.negative) { value = -value; }
        return answer;
      }
    }
  }
  adjusted_mantissa am = compute_float<binary_format<T>>(pns.exponent, pns.mantissa);
  if(pns.too_many_digits && am.power2 >= 0) {
    if(am != compute_float<binary_format<T>>(pns.exponent, pns.mantissa + 1)) {
      am = compute_error<binary_format<T>>(pns.exponent, pns.mantissa);
    }
  }
  // If we called compute_float<binary_format<T>>(pns.exponent, pns.mantissa) and we have an invalid power (am.power2 < 0),
  // then we need to go the long way around again. This is very uncommon.
  if(am.power2 < 0) { am = digit_comp<T>(pns, am); }
  to_float(pns.negative, am, value);
  // Test for over/underflow.
  if ((pns.mantissa != 0 && am.mantissa == 0 && am.power2 == 0) || am.power2 == binary_format<T>::infinite_power()) {
    answer.ec = std::errc::result_out_of_range;
  }
  return answer;
}

}}}}}} // namespace fast_float

#endif
