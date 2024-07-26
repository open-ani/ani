// Copyright 2018 - 2023 Ulf Adams
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_RYU_RYU_GENERIC_128_HPP
#define BOOST_CHARCONV_DETAIL_RYU_RYU_GENERIC_128_HPP

#include <boost/charconv/detail/ryu/generic_128.hpp>
#include <boost/charconv/detail/integer_search_trees.hpp>
#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/bit_layouts.hpp>
#include <boost/charconv/to_chars.hpp>
#include <cinttypes>
#include <cstdio>
#include <cstdint>

#ifdef BOOST_CHARCONV_DEBUG
#  include <iostream>
#endif

namespace boost { namespace charconv { namespace detail { namespace ryu {

static constexpr int32_t fd128_exceptional_exponent = 0x7FFFFFFF;
static constexpr unsigned_128_type one = 1;

struct floating_decimal_128
{
    unsigned_128_type mantissa;
    int32_t exponent;
    bool sign;
};

#ifdef BOOST_CHARCONV_DEBUG
static char* s(unsigned_128_type v) {
  int len = num_digits(v);
  char* b = static_cast<char*>(malloc((len + 1) * sizeof(char)));
  for (int i = 0; i < len; i++) {
    const uint32_t c = static_cast<uint32_t>(v % 10);
    v /= 10;
    b[len - 1 - i] = static_cast<char>('0' + c);
  }
  b[len] = 0;
  return b;
}
#endif

static inline struct floating_decimal_128 generic_binary_to_decimal(
        const unsigned_128_type bits,
        const uint32_t mantissaBits, const uint32_t exponentBits, const bool explicitLeadingBit) noexcept
{
    #ifdef BOOST_CHARCONV_DEBUG
    printf("IN=");
    for (int32_t bit = 127; bit >= 0; --bit)
    {
        printf("%u", static_cast<uint32_t>((bits >> bit) & 1));
    }
    printf("\n");
    #endif

    const uint32_t bias = (1u << (exponentBits - 1)) - 1;
    const bool ieeeSign = ((bits >> (mantissaBits + exponentBits)) & 1) != 0;
    const unsigned_128_type ieeeMantissa = bits & ((one << mantissaBits) - 1);
    const uint32_t ieeeExponent = static_cast<uint32_t>((bits >> mantissaBits) & ((one << exponentBits) - 1u));

    if (ieeeExponent == 0 && ieeeMantissa == 0)
    {
        struct floating_decimal_128 fd {0, 0, ieeeSign};
        return fd;
    }
    if (ieeeExponent == ((1u << exponentBits) - 1u))
    {
        struct floating_decimal_128 fd;
        fd.mantissa = explicitLeadingBit ? ieeeMantissa & ((one << (mantissaBits - 1)) - 1) : ieeeMantissa;
        fd.exponent = fd128_exceptional_exponent;
        fd.sign = ieeeSign;
        return fd;
    }

    int32_t e2;
    unsigned_128_type m2;
    // We subtract 2 in all cases so that the bounds computation has 2 additional bits.
    if (explicitLeadingBit)
    {
        // mantissaBits includes the explicit leading bit, so we need to correct for that here.
        if (ieeeExponent == 0)
        {
            e2 = static_cast<int32_t>(1 - bias - mantissaBits + 1 - 2);
        }
        else
        {
            e2 = static_cast<int32_t>(ieeeExponent - bias - mantissaBits + 1 - 2);
        }
        m2 = ieeeMantissa;
    }
    else
    {
        if (ieeeExponent == 0)
        {
            e2 = static_cast<int32_t>(1 - bias - mantissaBits - 2);
            m2 = ieeeMantissa;
        } else
        {
            e2 = static_cast<int32_t>(ieeeExponent - bias - mantissaBits - 2U);
            m2 = (one << mantissaBits) | ieeeMantissa;
        }
    }
    const bool even = (m2 & 1) == 0;
    const bool acceptBounds = even;

    #ifdef BOOST_CHARCONV_DEBUG
    printf("-> %s %s * 2^%d\n", ieeeSign ? "-" : "+", s(m2), e2 + 2);
    #endif

    // Step 2: Determine the interval of legal decimal representations.
    const unsigned_128_type mv = 4 * m2;
    // Implicit bool -> int conversion. True is 1, false is 0.
    const uint32_t mmShift =
            (ieeeMantissa != (explicitLeadingBit ? one << (mantissaBits - 1) : 0))
            || (ieeeExponent == 0);

    // Step 3: Convert to a decimal power base using 128-bit arithmetic.
    unsigned_128_type vr;
    unsigned_128_type vp;
    unsigned_128_type vm;
    int32_t e10;
    bool vmIsTrailingZeros = false;
    bool vrIsTrailingZeros = false;
    if (e2 >= 0)
    {
        // I tried special-casing q == 0, but there was no effect on performance.
        // This expression is slightly faster than max(0, log10Pow2(e2) - 1).
        const uint32_t q = log10Pow2(e2) - (e2 > 3);
        e10 = static_cast<int32_t>(q);
        const int32_t k = BOOST_CHARCONV_POW5_INV_BITCOUNT + static_cast<int32_t>(pow5bits(q)) - 1;
        const int32_t i = -e2 + static_cast<int32_t>(q) + k;
        uint64_t pow5[4];
        generic_computeInvPow5(q, pow5);
        vr = mulShift(4 * m2, pow5, i);
        vp = mulShift(4 * m2 + 2, pow5, i);
        vm = mulShift(4 * m2 - 1 - mmShift, pow5, i);

        #ifdef BOOST_CHARCONV_DEBUG
        printf("%s * 2^%d / 10^%d\n", s(mv), e2, q);
        printf("V+=%s\nV =%s\nV-=%s\n", s(vp), s(vr), s(vm));
        #endif

        // floor(log_5(2^128)) = 55, this is very conservative
        if (q <= 55)
        {
            // Only one of mp, mv, and mm can be a multiple of 5, if any.
            if (mv % 5 == 0)
            {
                vrIsTrailingZeros = multipleOfPowerOf5(mv, q - 1);
            }
            else if (acceptBounds)
            {
                // Same as min(e2 + (~mm & 1), pow5Factor(mm)) >= q
                // <=> e2 + (~mm & 1) >= q && pow5Factor(mm) >= q
                // <=> true && pow5Factor(mm) >= q, since e2 >= q.
                vmIsTrailingZeros = multipleOfPowerOf5(mv - 1 - mmShift, q);
            }
            else
            {
                // Same as min(e2 + 1, pow5Factor(mp)) >= q.
                vp -= multipleOfPowerOf5(mv + 2, q);
            }
        }
    }
    else
    {
        // This expression is slightly faster than max(0, log10Pow5(-e2) - 1).
        const uint32_t q = log10Pow5(-e2) - static_cast<uint32_t>(-e2 > 1);
        e10 = static_cast<int32_t>(q) + e2;
        const int32_t i = -e2 - static_cast<int32_t>(q);
        const int32_t k = static_cast<int32_t>(pow5bits(static_cast<uint32_t>(i))) - BOOST_CHARCONV_POW5_BITCOUNT;
        const int32_t j = static_cast<int32_t>(q) - k;
        uint64_t pow5[4];
        generic_computePow5(static_cast<uint32_t>(i), pow5);
        vr = mulShift(4 * m2, pow5, j);
        vp = mulShift(4 * m2 + 2, pow5, j);
        vm = mulShift(4 * m2 - 1 - mmShift, pow5, j);

        #ifdef BOOST_CHARCONV_DEBUG
        printf("%s * 5^%d / 10^%d\n", s(mv), -e2, q);
        printf("%d %d %d %d\n", q, i, k, j);
        printf("V+=%s\nV =%s\nV-=%s\n", s(vp), s(vr), s(vm));
        #endif

        if (q <= 1)
        {
            // {vr,vp,vm} is trailing zeros if {mv,mp,mm} has at least q trailing 0 bits.
            // mv = 4 m2, so it always has at least two trailing 0 bits.
            vrIsTrailingZeros = true;
            if (acceptBounds)
            {
                // mm = mv - 1 - mmShift, so it has 1 trailing 0 bit iff mmShift == 1.
                vmIsTrailingZeros = mmShift == 1;
            }
            else
            {
                // mp = mv + 2, so it always has at least one trailing 0 bit.
                --vp;
            }
        }
        else if (q < 127)
        {
            // We need to compute min(ntz(mv), pow5Factor(mv) - e2) >= q-1
            // <=> ntz(mv) >= q-1  &&  pow5Factor(mv) - e2 >= q-1
            // <=> ntz(mv) >= q-1    (e2 is negative and -e2 >= q)
            // <=> (mv & ((1 << (q-1)) - 1)) == 0
            // We also need to make sure that the left shift does not overflow.
            vrIsTrailingZeros = multipleOfPowerOf2(mv, q - 1);

            #ifdef BOOST_CHARCONV_DEBUG
            printf("vr is trailing zeros=%s\n", vrIsTrailingZeros ? "true" : "false");
            #endif
        }
    }

    #ifdef BOOST_CHARCONV_DEBUG
    printf("e10=%d\n", e10);
    printf("V+=%s\nV =%s\nV-=%s\n", s(vp), s(vr), s(vm));
    printf("vm is trailing zeros=%s\n", vmIsTrailingZeros ? "true" : "false");
    printf("vr is trailing zeros=%s\n", vrIsTrailingZeros ? "true" : "false");
    #endif

    // Step 4: Find the shortest decimal representation in the interval of legal representations.
    uint32_t removed = 0;
    uint8_t lastRemovedDigit = 0;
    unsigned_128_type output;

    while (vp / 10 > vm / 10)
    {
        vmIsTrailingZeros &= vm % 10 == 0;
        vrIsTrailingZeros &= lastRemovedDigit == 0;
        lastRemovedDigit = static_cast<uint8_t>(vr % 10);
        vr /= 10;
        vp /= 10;
        vm /= 10;
        ++removed;
    }

    #ifdef BOOST_CHARCONV_DEBUG
    printf("V+=%s\nV =%s\nV-=%s\n", s(vp), s(vr), s(vm));
    printf("d-10=%s\n", vmIsTrailingZeros ? "true" : "false");
    #endif

    if (vmIsTrailingZeros)
    {
        while (vm % 10 == 0)
        {
            vrIsTrailingZeros &= lastRemovedDigit == 0;
            lastRemovedDigit = static_cast<uint8_t>(vr % 10);
            vr /= 10;
            vp /= 10;
            vm /= 10;
            ++removed;
        }
    }

    #ifdef BOOST_CHARCONV_DEBUG
    printf("%s %d\n", s(vr), lastRemovedDigit);
    printf("vr is trailing zeros=%s\n", vrIsTrailingZeros ? "true" : "false");
    #endif

    if (vrIsTrailingZeros && (lastRemovedDigit == 5) && (vr % 2 == 0))
    {
        // Round even if the exact numbers is .....50..0.
        lastRemovedDigit = 4;
    }
    // We need to take vr+1 if vr is outside bounds, or we need to round up.
    output = vr + static_cast<unsigned_128_type>((vr == vm && (!acceptBounds || !vmIsTrailingZeros)) || (lastRemovedDigit >= 5));
    const int32_t exp = e10 + static_cast<int32_t>(removed);

    #ifdef BOOST_CHARCONV_DEBUG
    printf("V+=%s\nV =%s\nV-=%s\n", s(vp), s(vr), s(vm));
    printf("O=%s\n", s(output));
    printf("EXP=%d\n", exp);
    #endif

    return {output, exp, ieeeSign};
}

static inline int copy_special_str(char* result, const std::ptrdiff_t result_size, const struct floating_decimal_128 fd) noexcept
{
    if (fd.sign)
    {
        *result = '-';
        ++result;
    }

    if (fd.mantissa)
    {
        if (fd.sign)
        {
            if (fd.mantissa == static_cast<unsigned_128_type>(2305843009213693952) ||
                fd.mantissa == static_cast<unsigned_128_type>(6917529027641081856) ||
                fd.mantissa == static_cast<unsigned_128_type>(1) << 110) // 2^110
            {
                if (result_size >= 10)
                {
                    std::memcpy(result, "nan(snan)", 9);
                    return 10;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                if (result_size >= 9)
                {
                    std::memcpy(result, "nan(ind)", 8);
                    return 9;
                }
                else
                {
                    return -1;
                }
            }
        }
        else
        {
            if (fd.mantissa == static_cast<unsigned_128_type>(2305843009213693952) ||
                fd.mantissa == static_cast<unsigned_128_type>(6917529027641081856) ||
                fd.mantissa == static_cast<unsigned_128_type>(1) << 110) // 2^110
            {
                if (result_size >= 9)
                {
                    std::memcpy(result, "nan(snan)", 9);
                    return 9;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                if (result_size >= 3)
                {
                    std::memcpy(result, "nan", 3);
                    return 3;
                }
                else
                {
                    return -1;
                }
            }
        }
    }

    if (result_size >= 3 + static_cast<std::ptrdiff_t>(fd.sign))
    {
        memcpy(result, "inf", 3);
        return static_cast<int>(fd.sign) + 3;
    }

    return -1;
}

static inline int generic_to_chars_fixed(const struct floating_decimal_128 v, char* result, const ptrdiff_t result_size, int precision) noexcept
{
    if (v.exponent == fd128_exceptional_exponent)
    {
        return copy_special_str(result, result_size, v);
    }

    // Step 5: Print the decimal representation.
    if (v.sign)
    {
        *result++ = '-';
    }

    unsigned_128_type output = v.mantissa;
    const auto r = to_chars_128integer_impl(result, result + result_size, output);
    if (r.ec != std::errc())
    {
        return -static_cast<int>(r.ec);
    }

    auto current_len = static_cast<int>(r.ptr - result);

    #ifdef BOOST_CHARCONV_DEBUG
    char* man_print = s(v.mantissa);
    std::cerr << "Exp: " << v.exponent
              << "\nMantissa: " << man_print
              << "\nMan len: " << current_len << std::endl;
    free(man_print);
    #endif

    if (v.exponent == 0)
    {
        // Option 1: We need to do nothing
        return current_len + static_cast<int>(v.sign);
    }
    else if (v.exponent > 0)
    {
        // Option 2: Append 0s to the end of the number until we get the proper significand value
        // Then we need precison worth of zeros after the decimal point as applicable
        if (current_len + v.exponent > result_size)
        {
            return -static_cast<int>(std::errc::value_too_large);
        }

        result = r.ptr;
        memset(result, '0', static_cast<std::size_t>(v.exponent));
        result += static_cast<std::size_t>(v.exponent);
        current_len += v.exponent;
        *result++ = '.';
        ++precision;
    }
    else if ((-v.exponent) < current_len)
    {
        // Option 3: Insert a decimal point into the middle of the existing number
        if (current_len + v.exponent + 1 > result_size)
        {
            return -static_cast<int>(std::errc::result_out_of_range);
        }

        memmove(result + current_len + v.exponent + 1, result + current_len + v.exponent, static_cast<std::size_t>(-v.exponent));
        memcpy(result + current_len + v.exponent, ".", 1U);
        ++current_len;
        precision -= current_len + v.exponent;
        result += current_len + v.exponent + 1;
    }
    else
    {
        // Option 4: Leading 0s
        if (-v.exponent + 2 > result_size)
        {
            return -static_cast<int>(std::errc::value_too_large);
        }

        memmove(result - v.exponent - current_len + 2, result, static_cast<std::size_t>(current_len));
        memcpy(result, "0.", 2U);
        memset(result + 2, '0', static_cast<std::size_t>(0 - v.exponent - current_len));
        current_len = -v.exponent + 2;
        precision -= current_len - 2;
        result += current_len;
    }

    if (precision > 0)
    {
        if (current_len + precision > result_size)
        {
            return -static_cast<int>(std::errc::result_out_of_range);
        }

        memset(result, '0', static_cast<std::size_t>(precision));
        current_len += precision;
    }

    return current_len + static_cast<int>(v.sign);
}

// Converts the given decimal floating point number to a string, writing to result, and returning
// the number characters written. Does not terminate the buffer with a 0. In the worst case, this
// function can write up to 53 characters.
//
// Maximal char buffer requirement:
// sign + mantissa digits + decimal dot + 'E' + exponent sign + exponent digits
// = 1 + 39 + 1 + 1 + 1 + 10 = 53
static inline int generic_to_chars(const struct floating_decimal_128 v, char* result, const ptrdiff_t result_size, 
                                   chars_format fmt = chars_format::general, int precision = -1) noexcept
{
    if (v.exponent == fd128_exceptional_exponent)
    {
        return copy_special_str(result, result_size, v);
    }

    unsigned_128_type output = v.mantissa;
    const uint32_t olength = static_cast<uint32_t>(num_digits(output));

    #ifdef BOOST_CHARCONV_DEBUG
    printf("DIGITS=%s\n", s(v.mantissa));
    printf("OLEN=%u\n", olength);
    printf("EXP=%u\n", v.exponent + olength);
    #endif

    // See: https://github.com/cppalliance/charconv/issues/64
    if (fmt == chars_format::general)
    {
        const int64_t exp = v.exponent + static_cast<int64_t>(olength);
        if (std::abs(exp) <= olength)
        {
            return generic_to_chars_fixed(v, result, result_size, precision);
        }
    }

    // Step 5: Print the decimal representation.
    size_t index = 0;
    if (v.sign)
    {
        result[index++] = '-';
    }

    if (index + olength > static_cast<size_t>(result_size))
    {
        return -static_cast<int>(std::errc::value_too_large);
    }
    else if (olength == 0)
    {
        return -2; // Something has gone horribly wrong
    }

    for (uint32_t i = 0; i < olength - 1; ++i)
    {
        const auto c = static_cast<uint32_t>(output % 10);
        output /= 10;
        result[index + olength - i] = static_cast<char>('0' + c);
    }
    BOOST_CHARCONV_ASSERT(output < 10);
    result[index] = static_cast<char>('0' + static_cast<uint32_t>(output % 10)); // output should be < 10 by now.

    // Print decimal point if needed.
    if (olength > 1)
    {
        result[index + 1] = '.';
        index += olength + 1;
    }
    else
    {
        ++index;
    }

    // Reset the index to where the required precision should be
    if (precision != -1)
    {
        if (static_cast<size_t>(precision) < index)
        {
            if (fmt != chars_format::scientific)
            {
                index = static_cast<size_t>(precision) + 1 + static_cast<size_t>(v.sign); // Precision is number of characters not just the decimal portion
            }
            else
            {
                index = static_cast<size_t>(precision) + 2 + static_cast<size_t>(v.sign); // In scientific format the precision is just the decimal places
            }

            // Now we need to see if we need to round
            if (result[index] >= '5' && index < olength + 1 + static_cast<size_t>(v.sign))
            {
                bool continue_rounding = false;
                auto current_index = index;
                do
                {
                    --current_index;
                    if (result[current_index] == '9')
                    {
                        continue_rounding = true;
                        result[current_index] = '0';
                    }
                    else
                    {
                        continue_rounding = false;
                        result[current_index] = static_cast<char>(result[current_index] + static_cast<char>(1));
                    }
                } while (continue_rounding && current_index > 2);
            }

            // If the last digit is a zero than overwrite that as well, but not in scientific formatting
            if (fmt != chars_format::scientific)
            {
                while (result[index - 1] == '0')
                {
                    --index;
                }
            }
            else
            {
                // In scientific formatting we may need a final 0 to achieve the correct precision
                if (precision + 1 > static_cast<int>(olength))
                {
                    result[index - 1] = '0';
                }
            }
        }
        else if (static_cast<size_t>(precision) > index)
        {
            // Use our fallback routine that will capture more of the precision
            return -1;
        }
    }

    // Print the exponent.
    result[index++] = 'e';
    int32_t exp = v.exponent + static_cast<int32_t>(olength) - 1;
    if (exp < 0)
    {
        result[index++] = '-';
        exp = -exp;
    }
    else
    {
        result[index++] = '+';
    }

    uint32_t elength = static_cast<uint32_t>(num_digits(exp));
    for (uint32_t i = 0; i < elength; ++i)
    {
        // Always print a minimum of 2 characters in the exponent field
        if (elength == 1)
        {
            result[index + elength - 1 - i] = '0';
            ++index;
        }

        const uint32_t c = static_cast<uint32_t>(exp % 10);
        exp /= 10;
        result[index + elength - 1 - i] = static_cast<char>('0' + c);
    }
    if (elength == 0)
    {
        result[index++] = '0';
        result[index++] = '0';
    }
    
    index += elength;
    return static_cast<int>(index);
}

static inline struct floating_decimal_128 float_to_fd128(float f) noexcept
{
    static_assert(sizeof(float) == sizeof(uint32_t), "Float is not 32 bits");
    uint32_t bits = 0;
    std::memcpy(&bits, &f, sizeof(float));
    return generic_binary_to_decimal(bits, 23, 8, false);
}

static inline struct floating_decimal_128 double_to_fd128(double d) noexcept
{
    static_assert(sizeof(double) == sizeof(uint64_t), "Float is not 64 bits");
    uint64_t bits = 0;
    std::memcpy(&bits, &d, sizeof(double));
    return generic_binary_to_decimal(bits, 52, 11, false);
}

#if BOOST_CHARCONV_LDBL_BITS == 80

static inline struct floating_decimal_128 long_double_to_fd128(long double d) noexcept
{
    #ifdef BOOST_CHARCONV_HAS_INT128
    unsigned_128_type bits = 0;
    std::memcpy(&bits, &d, sizeof(long double));
    #else
    trivial_uint128 trivial_bits;
    std::memcpy(&trivial_bits, &d, sizeof(long double));
    unsigned_128_type bits {trivial_bits};
    #endif

    #ifdef BOOST_CHARCONV_DEBUG
    // For some odd reason, this ends up with noise in the top 48 bits. We can
    // clear out those bits with the following line; this is not required, the
    // conversion routine should ignore those bits, but the debug output can be
    // confusing if they aren't 0s.
    bits &= (one << 80) - 1;
    #endif

    return generic_binary_to_decimal(bits, 64, 15, true);
}

#else

static inline struct floating_decimal_128 long_double_to_fd128(long double d) noexcept
{
    unsigned_128_type bits = 0;
    std::memcpy(&bits, &d, sizeof(long double));

    #if LDBL_MANT_DIG == 113 // binary128 (e.g. ARM, S390X, PPC64LE)
    # ifdef __PPC64__
        return generic_binary_to_decimal(bits, 112, 15, false);
    # else
        return generic_binary_to_decimal(bits, 112, 15, true);
    # endif
    #elif LDBL_MANT_DIG == 106 // ibm128 (e.g. PowerPC)
    return generic_binary_to_decimal(bits, 105, 11, true);
    #endif
}

#endif

#ifdef BOOST_HAS_FLOAT128

static inline struct floating_decimal_128 float128_to_fd128(__float128 d) noexcept
{
    #ifdef BOOST_CHARCONV_HAS_INT128
    unsigned_128_type bits = 0;
    std::memcpy(&bits, &d, sizeof(__float128));
    #else
    trivial_uint128 trivial_bits;
    std::memcpy(&trivial_bits, &d, sizeof(__float128));
    unsigned_128_type bits {trivial_bits};
    #endif

    return generic_binary_to_decimal(bits, 112, 15, false);
}

#endif

#ifdef BOOST_CHARCONV_HAS_STDFLOAT128

static inline struct floating_decimal_128 stdfloat128_to_fd128(std::float128_t d) noexcept
{
    #ifdef BOOST_CHARCONV_HAS_INT128
    unsigned_128_type bits = 0;
    std::memcpy(&bits, &d, sizeof(std::float128_t));
    #else
    trivial_uint128 trivial_bits;
    std::memcpy(&trivial_bits, &d, sizeof(std::float128_t));
    unsigned_128_type bits {trivial_bits};
    #endif

    return generic_binary_to_decimal(bits, 112, 15, false);
}

#endif

}}}} // Namespaces

#endif //BOOST_RYU_GENERIC_128_HPP
