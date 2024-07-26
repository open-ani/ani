// Copyright 2020-2023 Daniel Lemire
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt
//
// Derivative of: https://github.com/fastfloat/fast_float

#ifndef BOOST_CHARCONV_DETAIL_FASTFLOAT_ASCII_NUMBER_HPP
#define BOOST_CHARCONV_DETAIL_FASTFLOAT_ASCII_NUMBER_HPP

#include <boost/charconv/detail/fast_float/float_common.hpp>
#include <cctype>
#include <cstdint>
#include <cstring>
#include <iterator>

namespace boost { namespace charconv { namespace detail { namespace fast_float {

// Next function can be micro-optimized, but compilers are entirely
// able to optimize it well.
template <typename UC>
BOOST_FORCEINLINE constexpr bool is_integer(UC c) noexcept {
  return !(c > UC('9') || c < UC('0'));
}

BOOST_FORCEINLINE constexpr uint64_t byteswap(uint64_t val) {
  return (val & 0xFF00000000000000) >> 56
    | (val & 0x00FF000000000000) >> 40
    | (val & 0x0000FF0000000000) >> 24
    | (val & 0x000000FF00000000) >> 8
    | (val & 0x00000000FF000000) << 8
    | (val & 0x0000000000FF0000) << 24
    | (val & 0x000000000000FF00) << 40
    | (val & 0x00000000000000FF) << 56;
}

BOOST_FORCEINLINE BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20
uint64_t read_u64(const char *chars) {
  if (cpp20_and_in_constexpr()) {
    uint64_t val = 0;
    for(int i = 0; i < 8; ++i) {
      val |= uint64_t(*chars) << (i*8);
      ++chars;
    }
    return val;
  }
  uint64_t val;
  ::memcpy(&val, chars, sizeof(uint64_t));
#if BOOST_CHARCONV_FASTFLOAT_IS_BIG_ENDIAN == 1
  // Need to read as-if the number was in little-endian order.
  val = byteswap(val);
#endif
  return val;
}

BOOST_FORCEINLINE BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20
void write_u64(uint8_t *chars, uint64_t val) {
  if (cpp20_and_in_constexpr()) {
    for(int i = 0; i < 8; ++i) {
      *chars = uint8_t(val);
      val >>= 8;
      ++chars;
    }
    return;
  }
#if BOOST_CHARCONV_FASTFLOAT_IS_BIG_ENDIAN == 1
  // Need to read as-if the number was in little-endian order.
  val = byteswap(val);
#endif
  ::memcpy(chars, &val, sizeof(uint64_t));
}

// credit  @aqrit
BOOST_FORCEINLINE BOOST_CHARCONV_FASTFLOAT_CONSTEXPR14
uint32_t parse_eight_digits_unrolled(uint64_t val) {
  constexpr uint64_t mask = 0x000000FF000000FF;
  constexpr uint64_t mul1 = 0x000F424000000064; // 100 + (1000000ULL << 32)
  constexpr uint64_t mul2 = 0x0000271000000001; // 1 + (10000ULL << 32)
  val -= 0x3030303030303030;
  val = (val * 10) + (val >> 8); // val = (val * 2561) >> 8;
  val = (((val & mask) * mul1) + (((val >> 16) & mask) * mul2)) >> 32;
  return uint32_t(val);
}

BOOST_FORCEINLINE constexpr
uint32_t parse_eight_digits_unrolled(const char16_t *)  noexcept  {
  return 0;
}

BOOST_FORCEINLINE constexpr
uint32_t parse_eight_digits_unrolled(const char32_t *)  noexcept  {
  return 0;
}

BOOST_FORCEINLINE BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20
uint32_t parse_eight_digits_unrolled(const char *chars)  noexcept  {
  return parse_eight_digits_unrolled(read_u64(chars));
}

// credit @aqrit
BOOST_FORCEINLINE constexpr bool is_made_of_eight_digits_fast(uint64_t val)  noexcept  {
  return !((((val + 0x4646464646464646) | (val - 0x3030303030303030)) & 0x8080808080808080));
}

BOOST_FORCEINLINE constexpr
bool is_made_of_eight_digits_fast(const char16_t *)  noexcept  {
  return false;
}

BOOST_FORCEINLINE constexpr
bool is_made_of_eight_digits_fast(const char32_t *)  noexcept  {
  return false;
}

BOOST_FORCEINLINE BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20
bool is_made_of_eight_digits_fast(const char *chars)  noexcept  {
  return is_made_of_eight_digits_fast(read_u64(chars));
}

template <typename UC>
struct parsed_number_string_t {
  int64_t exponent{0};
  uint64_t mantissa{0};
  UC const * lastmatch{nullptr};
  bool negative{false};
  bool valid{false};
  bool too_many_digits{false};
  // contains the range of the significant digits
  span<const UC> integer{};  // non-nullable
  span<const UC> fraction{}; // nullable
};
using byte_span = span<char>;
using parsed_number_string = parsed_number_string_t<char>;
// Assuming that you use no more than 19 digits, this will
// parse an ASCII string.
template <typename UC>
BOOST_FORCEINLINE BOOST_CHARCONV_FASTFLOAT_CONSTEXPR20
parsed_number_string_t<UC> parse_number_string(UC const *p, UC const * pend, parse_options_t<UC> options) noexcept {
  chars_format const fmt = options.format;
  UC const decimal_point = options.decimal_point;

  parsed_number_string_t<UC> answer;
  answer.valid = false;
  answer.too_many_digits = false;
  answer.negative = (*p == UC('-'));
#ifdef BOOST_CHARCONV_FASTFLOAT_ALLOWS_LEADING_PLUS // disabled by default
  if ((*p == UC('-')) || (*p == UC('+')))
#else
  if (*p == UC('-')) // C++17 20.19.3.(7.1) explicitly forbids '+' sign here
#endif
  {
    ++p;
    if (p == pend) {
      return answer;
    }
    if (!is_integer(*p) && (*p != decimal_point)) { // a sign must be followed by an integer or the dot
      return answer;
    }
  }
  UC const * const start_digits = p;

  uint64_t i = 0; // an unsigned int avoids signed overflows (which are bad)

  while ((p != pend) && is_integer(*p)) {
    // a multiplication by 10 is cheaper than an arbitrary integer
    // multiplication
    i = 10 * i +
        uint64_t(*p - UC('0')); // might overflow, we will handle the overflow later
    ++p;
  }
  UC const * const end_of_integer_part = p;
  int64_t digit_count = int64_t(end_of_integer_part - start_digits);
  answer.integer = span<const UC>(start_digits, size_t(digit_count));
  int64_t exponent = 0;
  if ((p != pend) && (*p == decimal_point)) {
    ++p;
    UC const * before = p;
    // can occur at most twice without overflowing, but let it occur more, since
    // for integers with many digits, digit parsing is the primary bottleneck.
    if (std::is_same<UC,char>::value) {
      while ((std::distance(p, pend) >= 8) && is_made_of_eight_digits_fast(p)) {
        i = i * 100000000 + parse_eight_digits_unrolled(p); // in rare cases, this will overflow, but that's ok
        p += 8;
      }
    }
    while ((p != pend) && is_integer(*p)) {
      uint8_t digit = uint8_t(*p - UC('0'));
      ++p;
      i = i * 10 + digit; // in rare cases, this will overflow, but that's ok
    }
    exponent = before - p;
    answer.fraction = span<const UC>(before, size_t(p - before));
    digit_count -= exponent;
  }
  // we must have encountered at least one integer!
  if (digit_count == 0) {
    return answer;
  }
  int64_t exp_number = 0;            // explicit exponential part
  if ((static_cast<unsigned>(fmt) & static_cast<unsigned>(chars_format::scientific)) && (p != pend) && ((UC('e') == *p) || (UC('E') == *p))) {
    UC const * location_of_e = p;
    ++p;
    bool neg_exp = false;
    if ((p != pend) && (UC('-') == *p)) {
      neg_exp = true;
      ++p;
    } else if ((p != pend) && (UC('+') == *p)) { // '+' on exponent is allowed by C++17 20.19.3.(7.1)
      ++p;
    }
    if ((p == pend) || !is_integer(*p)) {
      if(!(static_cast<unsigned>(fmt) & static_cast<unsigned>(chars_format::fixed))) {
        // We are in error.
        return answer;
      }
      // Otherwise, we will be ignoring the 'e'.
      p = location_of_e;
    } else {
      while ((p != pend) && is_integer(*p)) {
        uint8_t digit = uint8_t(*p - UC('0'));
        if (exp_number < 0x10000000) {
          exp_number = 10 * exp_number + digit;
        }
        ++p;
      }
      if(neg_exp) { exp_number = - exp_number; }
      exponent += exp_number;
    }
  } else {
    // If it scientific and not fixed, we have to bail out.
    if((static_cast<unsigned>(fmt) & static_cast<unsigned>(chars_format::scientific)) &&
       !(static_cast<unsigned>(fmt) & static_cast<unsigned>(chars_format::fixed)))
    {
        return answer;
    }
  }
  answer.lastmatch = p;
  answer.valid = true;

  // If we frequently had to deal with long strings of digits,
  // we could extend our code by using a 128-bit integer instead
  // of a 64-bit integer. However, this is uncommon.
  //
  // We can deal with up to 19 digits.
  if (digit_count > 19) { // this is uncommon
    // It is possible that the integer had an overflow.
    // We have to handle the case where we have 0.0000somenumber.
    // We need to be mindful of the case where we only have zeroes...
    // E.g., 0.000000000...000.
    UC const * start = start_digits;
    while ((start != pend) && (*start == UC('0') || *start == decimal_point)) {
      if(*start == UC('0')) { digit_count --; }
      start++;
    }
    if (digit_count > 19) {
      answer.too_many_digits = true;
      // Let us start again, this time, avoiding overflows.
      // We don't need to check if is_integer, since we use the
      // pre-tokenized spans from above.
      i = 0;
      p = answer.integer.ptr;
      UC const * int_end = p + answer.integer.len();
      constexpr uint64_t minimal_nineteen_digit_integer{1000000000000000000};
      while((i < minimal_nineteen_digit_integer) && (p != int_end)) {
        i = i * 10 + uint64_t(*p - UC('0'));
        ++p;
      }
      if (i >= minimal_nineteen_digit_integer) { // We have a big integers
        exponent = end_of_integer_part - p + exp_number;
      } else { // We have a value with a fractional component.
          p = answer.fraction.ptr;
          UC const * frac_end = p + answer.fraction.len();
          while((i < minimal_nineteen_digit_integer) && (p != frac_end)) {
            i = i * 10 + uint64_t(*p - UC('0'));
            ++p;
          }
          exponent = answer.fraction.ptr - p + exp_number;
      }
      // We have now corrected both exponent and i, to a truncated value
    }
  }
  answer.exponent = exponent;
  answer.mantissa = i;
  return answer;
}

}}}} // namespace s

#endif
