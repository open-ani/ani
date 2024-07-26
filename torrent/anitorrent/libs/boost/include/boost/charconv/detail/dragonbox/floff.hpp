// Copyright 2020-2022 Junekey Jeon
//
// The contents of this file may be used under the terms of
// the Apache License v2.0 with LLVM Exceptions.
//
//    (See accompanying file LICENSE-Apache or copy at
//     https://llvm.org/foundation/relicensing/LICENSE.txt)
//
// Alternatively, the contents of this file may be used under the terms of
// the Boost Software License, Version 1.0.
//    (See accompanying file LICENSE-Boost or copy at
//     https://www.boost.org/LICENSE_1_0.txt)
//
// Unless required by applicable law or agreed to in writing, this software
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.
//
// Some parts are copied from Dragonbox project.
//
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_FLOFF
#define BOOST_CHARCONV_DETAIL_FLOFF

#include <boost/charconv/detail/config.hpp>
#include <boost/charconv/detail/bit_layouts.hpp>
#include <boost/charconv/detail/emulated128.hpp>
#include <boost/charconv/detail/dragonbox/dragonbox_common.hpp>
#include <boost/charconv/detail/to_chars_result.hpp>
#include <boost/charconv/chars_format.hpp>
#include <boost/core/bit.hpp>
#include <type_traits>
#include <limits>
#include <cstdint>
#include <cstring>
#include <cstddef>
#include <climits>

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4127) // Extensive use of BOOST_IF_CONSTEXPR emits warnings under C++11 and 14
# pragma warning(disable: 4554) // parentheses are used be warning is still emitted
#endif

namespace boost { namespace charconv { namespace detail {

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4702) // use of BOOST_IF_CONSTEXPR can result in unreachable code if max_blocks is 3
                                // Other older compilers will emit warnings if the unreachable code is wrapped
                                // in an else block (e.g. no return statment)
#endif

template <std::size_t max_blocks>
struct fixed_point_calculator 
{
    static_assert(1 < max_blocks, "Max blocks must be greater than 1");

    // Multiply multiplier to the fractional blocks and take the resulting integer part.
    // The fractional blocks are updated.
    template <typename MultiplierType>
    BOOST_FORCEINLINE static MultiplierType generate(MultiplierType multiplier,
                                                     std::uint64_t* blocks_ptr,
                                                     std::size_t number_of_blocks) noexcept
    {
        BOOST_CHARCONV_ASSERT(0 < number_of_blocks && number_of_blocks <= max_blocks);

        BOOST_IF_CONSTEXPR (max_blocks == 3)
        {
            uint128 mul_result;
            std::uint64_t carry = 0;

            switch (number_of_blocks) 
            {
            case 3:
                mul_result = umul128(blocks_ptr[2], multiplier);
                blocks_ptr[2] = mul_result.low;
                carry = mul_result.high;
                BOOST_FALLTHROUGH;

            case 2:
                mul_result = umul128(blocks_ptr[1], multiplier);
                mul_result += carry;
                blocks_ptr[1] = mul_result.low;
                carry = mul_result.high;
                BOOST_FALLTHROUGH;

            case 1:
                mul_result = umul128(blocks_ptr[0], multiplier);
                mul_result += carry;
                blocks_ptr[0] = mul_result.low;
                return mul_result.high;

            default:
                BOOST_UNREACHABLE_RETURN(carry); // NOLINT : Macro for unreachable can expand to be empty
            }
        }

        auto mul_result = umul128(blocks_ptr[number_of_blocks - 1], multiplier);
        blocks_ptr[number_of_blocks - 1] = mul_result.low;
        auto carry = mul_result.high;
        for (std::size_t i = 1; i < number_of_blocks; ++i) 
        {
            mul_result = umul128(blocks_ptr[number_of_blocks - i - 1], multiplier);
            mul_result += carry;
            blocks_ptr[number_of_blocks - i - 1] = mul_result.low;
            carry = mul_result.high;
        }

        return MultiplierType(carry);
    }

    // Multiply multiplier to the fractional blocks and discard the resulting integer part.
    // The fractional blocks are updated.
    template <typename MultiplierType>
    BOOST_FORCEINLINE static void discard_upper(MultiplierType multiplier,
                                                std::uint64_t* blocks_ptr,
                                                std::size_t number_of_blocks) noexcept 
    {
        BOOST_CHARCONV_ASSERT(0 < number_of_blocks && number_of_blocks <= max_blocks);

        blocks_ptr[0] *= multiplier;
        if (number_of_blocks > 1) 
        {
            BOOST_IF_CONSTEXPR (max_blocks == 3) 
            {
                uint128 mul_result;
                std::uint64_t carry = 0;

                if (number_of_blocks > 2)
                {
                    mul_result = umul128(multiplier, blocks_ptr[2]);
                    blocks_ptr[2] = mul_result.low;
                    carry = mul_result.high;
                }

                mul_result = umul128(multiplier, blocks_ptr[1]);
                mul_result += carry;
                blocks_ptr[1] = mul_result.low;
                blocks_ptr[0] += mul_result.high;
            }
            else 
            {
                auto mul_result = umul128(multiplier, blocks_ptr[number_of_blocks - 1]);
                blocks_ptr[number_of_blocks - 1] = mul_result.low;
                auto carry = mul_result.high;

                for (std::size_t i = 2; i < number_of_blocks; ++i)
                {
                    mul_result = umul128(multiplier, blocks_ptr[number_of_blocks - i]);
                    mul_result += carry;
                    blocks_ptr[number_of_blocks - i] = mul_result.low;
                    carry = mul_result.high;
                }

                blocks_ptr[0] += carry;
            }
        }
    }

    // Multiply multiplier to the fractional blocks and take the resulting integer part.
    // Don't care about what happens to the fractional blocks.
    template <typename MultiplierType>
    BOOST_FORCEINLINE static MultiplierType
    generate_and_discard_lower(MultiplierType multiplier, std::uint64_t* blocks_ptr,
                                std::size_t number_of_blocks) noexcept 
    {
        BOOST_CHARCONV_ASSERT(0 < number_of_blocks && number_of_blocks <= max_blocks);

        BOOST_IF_CONSTEXPR (max_blocks == 3) 
        {
            uint128 mul_result;
            std::uint64_t carry = 0;

            switch (number_of_blocks) 
            {
            case 3:
                mul_result = umul128(blocks_ptr[2], static_cast<std::uint64_t>(multiplier));
                carry = mul_result.high;
                BOOST_FALLTHROUGH;

            case 2:
                mul_result = umul128(blocks_ptr[1], static_cast<std::uint64_t>(multiplier));
                mul_result += carry;
                carry = mul_result.high;
                BOOST_FALLTHROUGH;

            case 1:
                mul_result = umul128(blocks_ptr[0], static_cast<std::uint64_t>(multiplier));
                mul_result += carry;
                return static_cast<MultiplierType>(mul_result.high);

            default:
                BOOST_UNREACHABLE_RETURN(carry); // NOLINT
            }
        }

        auto mul_result = umul128(blocks_ptr[number_of_blocks - 1], static_cast<std::uint64_t>(multiplier));
        auto carry = mul_result.high;
        for (std::size_t i = 1; i < number_of_blocks; ++i)
        {
            mul_result = umul128(blocks_ptr[number_of_blocks - i - 1], static_cast<std::uint64_t>(multiplier));
            mul_result += carry;
            carry = mul_result.high;
        }

        return static_cast<MultiplierType>(carry);
    }
};

#ifdef BOOST_MSVC
# pragma warning(pop)
#endif

template <bool b>
struct additional_static_data_holder_impl
{
    static constexpr char radix_100_table[] = {
        '0', '0', '0', '1', '0', '2', '0', '3', '0', '4', //
        '0', '5', '0', '6', '0', '7', '0', '8', '0', '9', //
        '1', '0', '1', '1', '1', '2', '1', '3', '1', '4', //
        '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', //
        '2', '0', '2', '1', '2', '2', '2', '3', '2', '4', //
        '2', '5', '2', '6', '2', '7', '2', '8', '2', '9', //
        '3', '0', '3', '1', '3', '2', '3', '3', '3', '4', //
        '3', '5', '3', '6', '3', '7', '3', '8', '3', '9', //
        '4', '0', '4', '1', '4', '2', '4', '3', '4', '4', //
        '4', '5', '4', '6', '4', '7', '4', '8', '4', '9', //
        '5', '0', '5', '1', '5', '2', '5', '3', '5', '4', //
        '5', '5', '5', '6', '5', '7', '5', '8', '5', '9', //
        '6', '0', '6', '1', '6', '2', '6', '3', '6', '4', //
        '6', '5', '6', '6', '6', '7', '6', '8', '6', '9', //
        '7', '0', '7', '1', '7', '2', '7', '3', '7', '4', //
        '7', '5', '7', '6', '7', '7', '7', '8', '7', '9', //
        '8', '0', '8', '1', '8', '2', '8', '3', '8', '4', //
        '8', '5', '8', '6', '8', '7', '8', '8', '8', '9', //
        '9', '0', '9', '1', '9', '2', '9', '3', '9', '4', //
        '9', '5', '9', '6', '9', '7', '9', '8', '9', '9'  //
    };

    static constexpr std::uint32_t fractional_part_rounding_thresholds32[] = {
        UINT32_C(2576980378), UINT32_C(2190433321), UINT32_C(2151778616), UINT32_C(2147913145),
        UINT32_C(2147526598), UINT32_C(2147487943), UINT32_C(2147484078), UINT32_C(2147483691)
    };

    static constexpr std::uint64_t fractional_part_rounding_thresholds64[] = {
        UINT64_C(11068046444225730970), UINT64_C(9407839477591871325), UINT64_C(9241818780928485360),
        UINT64_C(9225216711262146764),  UINT64_C(9223556504295512904), UINT64_C(9223390483598849518),
        UINT64_C(9223373881529183179),  UINT64_C(9223372221322216546), UINT64_C(9223372055301519882),
        UINT64_C(9223372038699450216),  UINT64_C(9223372037039243249), UINT64_C(9223372036873222553),
        UINT64_C(9223372036856620483),  UINT64_C(9223372036854960276), UINT64_C(9223372036854794255),
        UINT64_C(9223372036854777653),  UINT64_C(9223372036854775993), UINT64_C(9223372036854775827)
    };
};

#if defined(BOOST_NO_CXX17_INLINE_VARIABLES) && (!defined(BOOST_MSVC) || BOOST_MSVC != 1900)

template <bool b> constexpr char additional_static_data_holder_impl<b>::radix_100_table[];
template <bool b> constexpr std::uint32_t additional_static_data_holder_impl<b>::fractional_part_rounding_thresholds32[];
template <bool b> constexpr std::uint64_t additional_static_data_holder_impl<b>::fractional_part_rounding_thresholds64[];

#endif

using additional_static_data_holder = additional_static_data_holder_impl<true>;

struct compute_mul_result 
{
    std::uint64_t result;
    bool is_integer;
};

// Load the necessary bits into blocks_ptr and then return the number of cache blocks
// loaded. The most significant block is loaded into blocks_ptr[0].
template <typename ExtendedCache, bool zero_out, 
          typename CacheBlockType = typename std::decay<decltype(ExtendedCache::cache[0])>::type,
          typename std::enable_if<(ExtendedCache::constant_block_count), bool>::type = true>
inline std::uint8_t cache_block_count_helper(CacheBlockType*, int, int, std::uint32_t) noexcept 
{
    return static_cast<std::uint8_t>(ExtendedCache::max_cache_blocks);
}

template <typename ExtendedCache, bool zero_out,
          typename CacheBlockType = typename std::decay<decltype(ExtendedCache::cache[0])>::type,
          typename std::enable_if<!(ExtendedCache::constant_block_count), bool>::type = true>
inline std::uint8_t cache_block_count_helper(CacheBlockType*, int e, int, std::uint32_t multiplier_index) noexcept 
{
    const auto mul_info = ExtendedCache::multiplier_index_info_table[multiplier_index];

    const auto cache_block_count_index =
                mul_info.cache_block_count_index_offset +
                static_cast<std::uint32_t>(e - ExtendedCache::e_min) / ExtendedCache::collapse_factor -
                ExtendedCache::cache_block_count_offset_base;

    BOOST_IF_CONSTEXPR (ExtendedCache::max_cache_blocks < 3)
    {
        // 1-bit packing.
        return static_cast<std::uint8_t>(
                    (ExtendedCache::cache_block_counts[cache_block_count_index /
                                                        8] >>
                    (cache_block_count_index % 8)) &
                    0x1) +
                1;
    }
    else BOOST_IF_CONSTEXPR (ExtendedCache::max_cache_blocks < 4)
    {
        // 2-bit packing.
        return static_cast<std::uint8_t>(
            (ExtendedCache::cache_block_counts[cache_block_count_index / 4] >>
                (2 * (cache_block_count_index % 4))) &
            0x3);
    }
    else 
    {
        // 4-bit packing.
        return std::uint8_t(
            (ExtendedCache::cache_block_counts[cache_block_count_index / 2] >>
                (4 * (cache_block_count_index % 2))) &
            0xf);
    }
}

template <typename ExtendedCache, bool zero_out,
          typename CacheBlockType = typename std::decay<decltype(ExtendedCache::cache[0])>::type>
BOOST_FORCEINLINE std::uint8_t load_extended_cache(CacheBlockType* blocks_ptr, int e, int k,
                                                   std::uint32_t multiplier_index) noexcept 
{
    BOOST_IF_CONSTEXPR (zero_out)
    {
        std::memset(blocks_ptr, 0, sizeof(CacheBlockType) * ExtendedCache::max_cache_blocks);
    }

    const auto mul_info = ExtendedCache::multiplier_index_info_table[multiplier_index];

    std::uint32_t number_of_leading_zero_blocks;
    std::uint32_t first_cache_block_index;
    std::uint32_t bit_offset;
    std::uint32_t excessive_bits_to_left;
    std::uint32_t excessive_bits_to_right;
    std::uint8_t  cache_block_count = cache_block_count_helper<ExtendedCache, zero_out, CacheBlockType>(blocks_ptr, e, k, multiplier_index);

    // The request window starting/ending positions.
    auto start_bit_index = static_cast<int>(mul_info.cache_bit_index_offset) + e - ExtendedCache::cache_bit_index_offset_base;
    auto end_bit_index = start_bit_index + cache_block_count * static_cast<int>(ExtendedCache::cache_bits_unit);

    // The source window starting/ending positions.
    const auto src_start_bit_index = static_cast<int>(mul_info.first_cache_bit_index);
    const auto src_end_bit_index = static_cast<int>(ExtendedCache::multiplier_index_info_table[multiplier_index + 1].first_cache_bit_index);

    // If the request window goes further than the left boundary of the source window,
    if (start_bit_index < src_start_bit_index)
    {
        number_of_leading_zero_blocks =
            static_cast<std::uint32_t>(src_start_bit_index - start_bit_index) /
            static_cast<std::uint32_t>(ExtendedCache::cache_bits_unit);
        excessive_bits_to_left = static_cast<std::uint32_t>(src_start_bit_index - start_bit_index) %
                                    static_cast<std::uint32_t>(ExtendedCache::cache_bits_unit);

        BOOST_IF_CONSTEXPR (!zero_out)
        {
            std::memset(blocks_ptr, 0, number_of_leading_zero_blocks * sizeof(CacheBlockType));
        }

        start_bit_index += static_cast<int>(number_of_leading_zero_blocks * ExtendedCache::cache_bits_unit);

        const auto src_start_block_index =
            static_cast<int>(static_cast<std::uint32_t>(src_start_bit_index) /
                static_cast<std::uint32_t>(ExtendedCache::cache_bits_unit));
        
        const auto src_start_block_bit_index =
            src_start_block_index * static_cast<int>(ExtendedCache::cache_bits_unit);

        first_cache_block_index = static_cast<std::uint32_t>(src_start_block_index);

        if (start_bit_index < src_start_block_bit_index)
        {
            auto shift_amount = src_start_block_bit_index - start_bit_index;
            BOOST_CHARCONV_ASSERT(shift_amount >= 0 && shift_amount < static_cast<int>(ExtendedCache::cache_bits_unit));

            blocks_ptr[number_of_leading_zero_blocks] =
                ((ExtendedCache::cache[src_start_block_index] >> shift_amount) &
                    (CacheBlockType(CacheBlockType(0) - CacheBlockType(1)) >>
                    excessive_bits_to_left));

            ++number_of_leading_zero_blocks;
            bit_offset = static_cast<std::uint32_t>(static_cast<int>(ExtendedCache::cache_bits_unit) - shift_amount);
            excessive_bits_to_left = 0;
        }
        else 
        {
            bit_offset = static_cast<std::uint32_t>(start_bit_index - src_start_block_bit_index);
        }
    }
    else 
    {
        number_of_leading_zero_blocks = 0;
        first_cache_block_index =
            static_cast<std::uint32_t>(start_bit_index) / static_cast<std::uint32_t>(ExtendedCache::cache_bits_unit);
        bit_offset =
            static_cast<std::uint32_t>(start_bit_index) % static_cast<std::uint32_t>(ExtendedCache::cache_bits_unit);
        excessive_bits_to_left = 0;
    }

    // If the request window goes further than the right boundary of the source window,
    if (end_bit_index > src_end_bit_index)
    {
        const std::uint8_t number_of_trailing_zero_blocks =
            static_cast<std::uint8_t>(end_bit_index - src_end_bit_index) / ExtendedCache::cache_bits_unit;
        excessive_bits_to_right = static_cast<std::uint32_t>(end_bit_index - src_end_bit_index) %
                                    static_cast<std::uint32_t>(ExtendedCache::cache_bits_unit);

        cache_block_count -= number_of_trailing_zero_blocks;
    }
    else
    {
        excessive_bits_to_right = 0;
    }

    // Load blocks.
    const auto number_of_blocks_to_load = cache_block_count - number_of_leading_zero_blocks;
    auto* const dst_ptr = blocks_ptr + number_of_leading_zero_blocks;
    if (bit_offset == 0)
    {
        BOOST_IF_CONSTEXPR (ExtendedCache::max_cache_blocks == 3)
        {
            switch (number_of_blocks_to_load)
            {
            case 3:
                std::memcpy(dst_ptr, ExtendedCache::cache + first_cache_block_index, 3 * sizeof(CacheBlockType));
                break;
            case 2:
                std::memcpy(dst_ptr, ExtendedCache::cache + first_cache_block_index, 2 * sizeof(CacheBlockType));
                break;
            case 1:
                std::memcpy(dst_ptr, ExtendedCache::cache + first_cache_block_index, 1 * sizeof(CacheBlockType));
                break;
            case 0:
                break;
            default:
                BOOST_UNREACHABLE_RETURN(dst_ptr); // NOLINT
            }
        }
        else 
        {
            std::memcpy(dst_ptr, ExtendedCache::cache + first_cache_block_index, number_of_blocks_to_load * sizeof(CacheBlockType));
        }
    }
    else 
    {
        BOOST_IF_CONSTEXPR (ExtendedCache::max_cache_blocks == 3)
        {
            switch (number_of_blocks_to_load)
            {
            case 3:
                *(dst_ptr + 2) =
                    (ExtendedCache::cache[first_cache_block_index + 2] << bit_offset) |
                    (ExtendedCache::cache[first_cache_block_index + 3] >>
                        (ExtendedCache::cache_bits_unit - bit_offset));
                BOOST_FALLTHROUGH;
            case 2:
                *(dst_ptr + 1) =
                    (ExtendedCache::cache[first_cache_block_index + 1] << bit_offset) |
                    (ExtendedCache::cache[first_cache_block_index + 2] >>
                        (ExtendedCache::cache_bits_unit - bit_offset));
                BOOST_FALLTHROUGH;
            case 1:
                *dst_ptr = (ExtendedCache::cache[first_cache_block_index] << bit_offset) |
                            (ExtendedCache::cache[first_cache_block_index + 1] >>
                            (ExtendedCache::cache_bits_unit - bit_offset));
            case 0:
                break;
            default:
                BOOST_UNREACHABLE_RETURN(dst_ptr); // NOLINT
            }
        }
        else 
        {
            for (std::uint8_t i = 0; i < number_of_blocks_to_load; ++i)
            {
                *(dst_ptr + i) =
                    (ExtendedCache::cache[first_cache_block_index + i] << bit_offset) |
                    (ExtendedCache::cache[first_cache_block_index + i + 1] >>
                        (ExtendedCache::cache_bits_unit - bit_offset));
            }
        }
    }
    
    // Remove possible flooding bits from adjacent entries.
    *dst_ptr &= (CacheBlockType(CacheBlockType(0) - CacheBlockType(1)) >> excessive_bits_to_left);

    blocks_ptr[cache_block_count - 1] &= (CacheBlockType(CacheBlockType(0) - CacheBlockType(1)) << excessive_bits_to_right);

    // To compute ceil(2^Q * x / D), we need to check if
    // 2^Q * x / D = 2^(Q + e + k - eta - 1) * 5^(k - eta) is an integer or not.
    if (k < ExtendedCache::segment_length ||
        e + k + static_cast<int>(cache_block_count * ExtendedCache::cache_bits_unit) -
                static_cast<int>(excessive_bits_to_right) <
            ExtendedCache::segment_length + 1) {
        blocks_ptr[cache_block_count - 1] += (CacheBlockType(1) << excessive_bits_to_right);
        BOOST_CHARCONV_ASSERT(blocks_ptr[cache_block_count - 1] != 0);
    }

    return cache_block_count;
}

template <bool constant_block_count, std::uint8_t max_cache_blocks>
struct cache_block_count_t;

template <std::uint8_t max_cache_blocks>
struct cache_block_count_t<false, max_cache_blocks>
{
    std::uint8_t value;
    
    operator std::uint8_t() const noexcept { return value; } // NOLINT : implicit conversions are ok for block count
    cache_block_count_t& operator=(std::uint8_t new_value) noexcept
    {
        value = new_value;
        return *this;
    }
};

template <std::uint8_t max_cache_blocks>
struct cache_block_count_t<true, max_cache_blocks>
{
    static constexpr std::uint8_t value = max_cache_blocks;
    operator std::uint8_t() const noexcept { return value; } // NOLINT : implicit conversions are ok for block count
    cache_block_count_t& operator=(std::uint8_t) noexcept
    {
        // Don't do anything.
        return *this;
    }
};

template <unsigned n>
struct uconst
{
    constexpr uconst() {}; // NOLINT : Clang 3.x does not support = default
    static constexpr unsigned value = n;
};

BOOST_INLINE_VARIABLE constexpr uconst<0>  uconst0;
BOOST_INLINE_VARIABLE constexpr uconst<1>  uconst1;
BOOST_INLINE_VARIABLE constexpr uconst<6>  uconst6;
BOOST_INLINE_VARIABLE constexpr uconst<9>  uconst9;
BOOST_INLINE_VARIABLE constexpr uconst<14> uconst14;
BOOST_INLINE_VARIABLE constexpr uconst<16> uconst16;

#ifdef __clang__
#  pragma clang diagnostic push
#  pragma clang diagnostic ignored "-Wsign-conversion"
#elif defined(__GNUC__)
#  pragma GCC diagnostic push
#  pragma GCC diagnostic ignored "-Wsign-conversion"
#elif defined(BOOST_MSVC)
#  pragma warning(push)
#  pragma warning(disable: 4365 4267)
#endif

template <unsigned digits, bool dummy = (digits <= 9)>
struct uint_with_known_number_of_digits;

template <unsigned digits_>
struct uint_with_known_number_of_digits<digits_, true> 
{
    static constexpr auto digits = digits_;
    std::uint32_t value;
};

template <unsigned digits_>
struct uint_with_known_number_of_digits<digits_, false>
{
    static constexpr auto digits = digits_;
    std::uint64_t value;
};

template <typename HasFurtherDigits, typename... Args, typename std::enable_if<std::is_same<HasFurtherDigits, bool>::value, bool>::type = true>
static BOOST_FORCEINLINE bool check_rounding_condition_inside_subsegment(
    std::uint32_t current_digits, std::uint32_t fractional_part,
    int remaining_digits_in_the_current_subsegment, HasFurtherDigits has_further_digits,
    Args...) noexcept 
{
    if (fractional_part >= additional_static_data_holder::fractional_part_rounding_thresholds32[remaining_digits_in_the_current_subsegment - 1])
    {
        return true;
    }

    return ((fractional_part >> 31) & ((current_digits & 1) | has_further_digits)) != 0;
}

template <typename HasFurtherDigits, typename... Args,
          typename std::enable_if<!std::is_same<HasFurtherDigits, bool>::value, bool>::type = true>
static BOOST_FORCEINLINE bool check_rounding_condition_inside_subsegment(
    std::uint32_t current_digits, std::uint32_t fractional_part,
    int remaining_digits_in_the_current_subsegment, HasFurtherDigits has_further_digits,
    Args... args) noexcept 
{
    if (fractional_part >= additional_static_data_holder::fractional_part_rounding_thresholds32[remaining_digits_in_the_current_subsegment - 1]) 
    {
        return true;
    }
    
    return fractional_part >= 0x80000000 && ((current_digits & 1) != 0 || has_further_digits(args...));
}

template <typename HasFurtherDigits, typename... Args,
          typename std::enable_if<std::is_same<HasFurtherDigits, bool>::value, bool>::type = true>
static BOOST_FORCEINLINE bool check_rounding_condition_with_next_bit(std::uint32_t current_digits, bool next_bit,
                                                                     HasFurtherDigits has_further_digits, Args...) noexcept 
{
    if (!next_bit) 
    {
        return false;
    }

    return ((current_digits & 1) | has_further_digits) != 0;
}

template <typename HasFurtherDigits, typename... Args,
          typename std::enable_if<!std::is_same<HasFurtherDigits, bool>::value, bool>::type = true>
static BOOST_FORCEINLINE bool check_rounding_condition_with_next_bit(std::uint32_t current_digits, bool next_bit,
                                                                     HasFurtherDigits has_further_digits, Args... args) noexcept 
{
    if (!next_bit) 
    {
        return false;
    }

    return (current_digits & 1) != 0 || has_further_digits(args...);
}

template <typename UintWithKnownDigits, typename HasFurtherDigits, typename... Args, 
          typename std::enable_if<std::is_same<HasFurtherDigits, bool>::value, bool>::type = true>
static BOOST_FORCEINLINE bool check_rounding_condition_subsegment_boundary_with_next_subsegment(
    std::uint32_t current_digits, UintWithKnownDigits next_subsegment,
    HasFurtherDigits has_further_digits, Args...) noexcept 
{
    if (next_subsegment.value > power_of_10[decltype(next_subsegment)::digits] / 2)
    {
        return true;
    }

    return next_subsegment.value == power_of_10[decltype(next_subsegment)::digits] / 2 && 
                                    ((current_digits & 1) | has_further_digits) != 0;
}

template <typename UintWithKnownDigits, typename HasFurtherDigits, typename... Args, 
          typename std::enable_if<!std::is_same<HasFurtherDigits, bool>::value, bool>::type = true>
static BOOST_FORCEINLINE bool check_rounding_condition_subsegment_boundary_with_next_subsegment(
    std::uint32_t current_digits, UintWithKnownDigits next_subsegment,
    HasFurtherDigits has_further_digits, Args... args) noexcept 
{
    if (next_subsegment.value > power_of_10[decltype(next_subsegment)::digits] / 2) 
    {
        return true;
    }

    return next_subsegment.value == power_of_10[decltype(next_subsegment)::digits] / 2 &&
                                    ((current_digits & 1) != 0 || has_further_digits(args...));
}

#ifdef __clang__
#  pragma clang diagnostic pop
#elif defined(__GNUC__)
#  pragma GCC diagnostic pop
#elif defined(BOOST_MSVC)
#  pragma warning(pop)
#endif

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4307) // MSVC 14.1 emits warnings for uint64_t constants
#endif

namespace has_further_digits_impl {
template <int k_right_threshold, int additional_neg_exp_of_2>
bool no_neg_k_can_be_integer(int k, int exp2_base) noexcept 
{
    return k < k_right_threshold || exp2_base + k < additional_neg_exp_of_2;
}

template <int k_left_threshold, int k_right_threshold, int additional_neg_exp_of_2, int min_neg_exp_of_5, typename SignificandType>
bool only_one_neg_k_can_be_integer(int k, int exp2_base, SignificandType significand) noexcept
{
    // Supposed to be k - additional_neg_exp_of_5_v < -min_neg_exp_of_5 || ...
    if (k < k_left_threshold || exp2_base + k < additional_neg_exp_of_2) 
    {
        return true;
    }
    // Supposed to be k - additional_neg_exp_of_5_v >= 0.
    if (k >= k_right_threshold) 
    {
        return false;
    }

    BOOST_CXX14_CONSTEXPR std::uint64_t mod_inv = compute_power(UINT64_C(0xcccccccccccccccd), static_cast<unsigned>(min_neg_exp_of_5));
    BOOST_CXX14_CONSTEXPR std::uint64_t max_quot = UINT64_C(0xffffffffffffffff) / compute_power(UINT64_C(5), static_cast<unsigned>(min_neg_exp_of_5));

    return (significand * mod_inv) > max_quot;
}

template <int k_left_threshold, int k_middle_threshold, int k_right_threshold,
            int additional_neg_exp_of_2, int min_neg_exp_of_5, int segment_length,
            typename SignificandType>
bool only_two_neg_k_can_be_integer(int k, int exp2_base,
                                    SignificandType significand) noexcept {
    // Supposed to be k - additional_neg_exp_of_5_v < -min_neg_exp_of_5 - segment_length
    // || ...
    if (k < k_left_threshold || exp2_base + k < additional_neg_exp_of_2) {
        return true;
    }
    // Supposed to be k - additional_neg_exp_of_5_v >= 0.
    if (k >= k_right_threshold) {
        return false;
    }

    if (k >= k_middle_threshold) {
        BOOST_CXX14_CONSTEXPR std::uint64_t mod_inv =
            compute_power(UINT64_C(0xcccccccccccccccd), static_cast<unsigned>(min_neg_exp_of_5));
        BOOST_CXX14_CONSTEXPR std::uint64_t max_quot =
            UINT64_C(0xffffffffffffffff) /
            compute_power(UINT64_C(5), static_cast<unsigned>(min_neg_exp_of_5));

        return (significand * mod_inv) > max_quot;
    }
    else {
        BOOST_CXX14_CONSTEXPR std::uint64_t mod_inv = compute_power(
            UINT64_C(0xcccccccccccccccd), static_cast<unsigned>(min_neg_exp_of_5 + segment_length));
        BOOST_CXX14_CONSTEXPR std::uint64_t max_quot =
            UINT64_C(0xffffffffffffffff) /
            compute_power(UINT64_C(5),
                            static_cast<unsigned>(min_neg_exp_of_5 + segment_length));

        return (significand * mod_inv) > max_quot;
    }
}
} // Namespace has_further_digits_impl

#ifdef BOOST_MSVC
#pragma warning(pop)
#endif

inline void print_1_digit(std::uint32_t n, char* buffer) noexcept 
{
    *buffer = char('0' + n);
}

inline void print_2_digits(std::uint32_t n, char* buffer) noexcept
{
    std::memcpy(buffer, additional_static_data_holder::radix_100_table + n * 2, 2);
}

inline void print_6_digits(std::uint32_t n, char* buffer) noexcept 
{
    // 429497 = ceil(2^32/10^4)
    auto prod = (n * UINT64_C(429497)) + 1;
    print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
    
    for (int i = 0; i < 2; ++i) 
    {
        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
        print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer + 2 + i * 2);
    }
}

inline void print_7_digits(std::uint32_t n, char* buffer) noexcept 
{
    // 17592187 = ceil(2^(32+12)/10^6)
    auto prod = ((n * UINT64_C(17592187)) >> 12) + 1;
    print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
    
    for (int i = 0; i < 3; ++i)
    {
        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
        print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer + 1 + i * 2);
    }
}

inline void print_8_digits(std::uint32_t n, char* buffer) noexcept
{
    // 140737489 = ceil(2^(32+15)/10^6)
    auto prod = ((n * UINT64_C(140737489)) >> 15) + 1;
    print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
    
    for (int i = 0; i < 3; ++i) 
    {
        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
        print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer + 2 + i * 2);
    }
}

inline void print_9_digits(std::uint32_t n, char* buffer) noexcept
{
    // 1441151881 = ceil(2^(32+25)/10^8)
    auto prod = ((n * UINT64_C(1441151881)) >> 25) + 1;
    print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
    
    for (int i = 0; i < 4; ++i) 
    {
        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
        print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer + 1 + i * 2);
    }
}

struct main_cache_full 
{
    template <typename FloatFormat>
    static constexpr typename main_cache_holder::cache_entry_type get_cache(int k) noexcept
    {
        return main_cache_holder::cache[std::size_t(k - main_cache_holder::min_k)];
    }
};

struct main_cache_compressed 
{
    template <typename FloatFormat>
    static BOOST_CHARCONV_CXX14_CONSTEXPR typename main_cache_holder::cache_entry_type get_cache(int k) noexcept
    {
        BOOST_CHARCONV_ASSERT(k >= main_cache_holder::min_k && k <= main_cache_holder::max_k);

        BOOST_IF_CONSTEXPR (std::is_same<FloatFormat, ieee754_binary64>::value) 
        {
            // Compute the base index.
            const auto cache_index =
                static_cast<int>(static_cast<std::uint32_t>(k - main_cache_holder::min_k) /
                    compressed_cache_detail::compression_ratio);

            const auto kb = cache_index * compressed_cache_detail::compression_ratio +
                            main_cache_holder::min_k;

            const auto offset = k - kb;

            // Get the base cache.
            const auto base_cache = compressed_cache_detail::cache_holder_t::table[cache_index];

            if (offset == 0)
            {
                return base_cache;
            }
            else 
            {

                // Compute the required amount of bit-shift.
                const auto alpha = log::floor_log2_pow10(kb + offset) - log::floor_log2_pow10(kb) - offset;
                BOOST_CHARCONV_ASSERT(alpha > 0 && alpha < 64);

                // Try to recover the real cache.
                const auto pow5 = compressed_cache_detail::pow5_holder_t::table[offset];
                auto recovered_cache = umul128(base_cache.high, pow5);
                const auto middle_low = umul128(base_cache.low, pow5);

                recovered_cache += middle_low.high;

                const auto high_to_middle = recovered_cache.high << (64 - alpha);
                const auto middle_to_low = recovered_cache.low << (64 - alpha);

                recovered_cache = uint128{(recovered_cache.low >> alpha) | high_to_middle, ((middle_low.low >> alpha) | middle_to_low)};

                BOOST_CHARCONV_ASSERT(recovered_cache.low + 1 != 0);
                recovered_cache = uint128(recovered_cache.high, recovered_cache.low + 1);

                return recovered_cache;
            }
        }
        else
        {
            // Just use the full cache for anything other than binary64
            return main_cache_holder::cache[std::size_t(k - main_cache_holder::min_k)];
        }
    }
};

template <bool b>
struct extended_cache_long_impl
{
    static constexpr std::size_t max_cache_blocks = 3;
    static constexpr std::size_t cache_bits_unit = 64;
    static constexpr int segment_length = 22;
    static constexpr bool constant_block_count = true;
    static constexpr int e_min = -1074;
    static constexpr int k_min = -272;
    static constexpr int cache_bit_index_offset_base = 977;
    static constexpr std::uint64_t cache[] = {
        0xa37fce126597973c, 0xe50ff107bab528a0, 0x8f1ba3f17395a391, 0xd56bdc876cdb4648,
        0x6ca000bdd9e33bd4, 0x23cf34bbf983f78b, 0x8737d87296e93f5d, 0xa2824ba6d9df301d,
        0x8ce3eccf7cfb42ab, 0xe5ecdc0b78109f00, 0xa620c9995c9c5c3a, 0xa0f79c97ac210943,
        0x64dfb5636985915f, 0xc12f542e4c7ea6ee, 0x34de81232784ea17, 0xd0cbde7fac4643f2,
        0x5d9400de8fef7552, 0x81214f68696d9af2, 0xb7d0e0a2ccaccf20, 0x5c4ed9243f16193d,
        0xf71838486e60b926, 0x48892047ec1a8bf4, 0x14ff2faa9c32befa, 0x666fbaa24ddbb8e9,
        0x436682c807652a58, 0xed98ddaee19068c7, 0x63badd624dd9b095, 0x72dbb637d5b77493,
        0xd01998fb8d9e8861, 0xacb39418dce017b9, 0x8db8f2f13eed81cf, 0xfd699fbb7d0a737a,
        0x011cd67160923d91, 0x9a66fd7732c14d98, 0x235857d065a52d18, 0x895288951dab0d8e,
        0x59041cb66e4f0e68, 0x5e7c68240249e750, 0x8881a2a6ab00987b, 0x5fc8c32c863aaeac,
        0x3bafbe662a7f81a8, 0xd47692705ae76b64, 0xeb1cc7d99143fb53, 0xcf8be24f7b0fc499,
        0x6a276e8f0fbf33eb, 0x63b2d61966fa7243, 0x0970327d2cc58011, 0x43ff09410ec24aae,
        0x0bdb6f345ea1851d, 0x409c37132c5836ff, 0xf3150f74a6190324, 0x5c358d6c07453d23,
        0x7207012ad7846ba7, 0x61ad5d0772604733, 0x19a20a6e21c2018d, 0x5f568fd497ef18b2,
        0xeda5815eed00749f, 0x029531461bc483d8, 0xb8789d7784875911, 0x6fc40572236f2ba5,
        0x9c2a50a76ace3168, 0xbf4815c2bea56741, 0xf84e8f2fe9b211f5, 0x689033182d2ea7ed,
        0x5bcb3a3230a68f47, 0xa848403d116805ef, 0xfaeaa73623b79604, 0x31d76828d2181b64,
        0x7c4eabddc7dd634b, 0xc2b13231eeff6fda, 0x8094743db32bf251, 0x2df07391bde052d2,
        0xffd9bdbf321ad8ae, 0x06b2c6d1cf6cf742, 0xf32a54ce1598fe8f, 0x1cc2e3082d28897e,
        0x0485f2e46b488584, 0xe3f6965b145a49cb, 0x406eaa1217aefe69, 0x0777373638de456b,
        0xcde91853b592212b, 0x3faf7b46d7f79c18, 0x558d83afb7127381, 0x5f490259c7957aeb,
        0x76e6540e246d73cc, 0x5098a935a866dc75, 0xc50d9c29002d9e73, 0xcc8f8252faac0b7f,
        0xb759afb688f8251d, 0x6a2934d3036c85d3, 0x570eb3ce4c86407f, 0x036f2b68794754af,
        0x57661a5d6993fe2c, 0x6d07b7fabe546a80, 0x38efe4029259743c, 0x548f417ebaa61c6c,
        0xb0c31fa64a3fcc9e, 0x7dab825964fb7100, 0xd0c92ae8207d6f22, 0xf1e38a8a9c541144,
        0x2139951c68d0385b, 0x9d9e22c42f139287, 0x4fea4d670876b800, 0x35f293a9a62252d4,
        0x4b606b26f1922c5c, 0x8e5660b37505cb11, 0x868138391855da81, 0x6e95f6c9b45c7aa2,
        0x425ff75e14fc31a1, 0x258379a94d028d18, 0xdf2ccd1fe00a03b6, 0x398471c1ff970f83,
        0x8c36b2214a3db8e7, 0x431dd42c3fe7f4fb, 0xb09bcf0fffb5b849, 0xc47dd13da60fb5a1,
        0x8fdad56516fe9d75, 0xc317e1025a7e1c63, 0x9ddcb98cbb384fda, 0x80adccda993bf70e,
        0x667f1622e4052ae4, 0xa41598d58f777363, 0x704b93d675808501, 0xaf046d3fd448aaf3,
        0x1dc4611873bf3f70, 0x834acdae9f0f4f53, 0x4f5d60585a5f1c1a, 0x3ced1b4be0d415c1,
        0x5d57f4de8ec12376, 0x51c0e7e72f799542, 0x46f7604940e6a510, 0x1a546a0f9345ed75,
        0x0df4097cab773ca2, 0x72b122774e4029e6, 0xae4a55b99aebd424, 0x04163a291bad2fa3,
        0x86ad58be322a49aa, 0x98f051614696e839, 0x64d08f241fc4ec58, 0xae41f23dca90dd5d,
        0x68bbd62f5af3107a, 0x7025f39ef241c56c, 0xd2e7c72fa9be33ac, 0x0aece66fd3e29a7d,
        0xd91241cebf3bd47c, 0x3ed7bfdee19ba2f6, 0x4bdf483194c7444e, 0xc99d83c931e8ab87,
        0x1732f416dbf7381f, 0x2ac88e244de13b96, 0x2cab688bd86c8bf8, 0x9f209787bb47d6b8,
        0x4c0678c5dbd23a49, 0xa0612c3c5ce15e55, 0x4dccc6ca29b3e9df, 0x0dc079c918022212,
        0x26be55a64c249495, 0x4da2c9789dd268b0, 0xe975528c76435158, 0xa6cb8a4d2356f9cf,
        0xdcafd2279c77d987, 0xaa9aff7904228690, 0xfb44d2f05d0842fb, 0x118fc9c217a1d2b2,
        0x04b3d9686f55b572, 0xbd9cb3625ef1cfc3, 0x2eba0e25e938e6c3, 0x1f48eaf234ad3a21,
        0xf2dc02fad2890f79, 0xace340325d4a7f9b, 0xe9e051f540b239dc, 0x221091f05abb8687,
        0x7e08deb014db8afe, 0x4711e1e9d9a094cc, 0x0b2d79bd90a9ef61, 0xb93d19bd45b82515,
        0x45e9e31d63c1afe1, 0x2c5f0a596005c216, 0xe687cc2331b14a12, 0x51963a2412b6f60c,
        0x91aeb77c8fe68eaa, 0xd6e18e8cc6841d68, 0x9391085cc2c933d9, 0x6e184be07e68df49,
        0x4fe4e52edb0dce60, 0x6cda31e8617f0ca2, 0xf8b9374fda7e7c95, 0x8032c603725e774d,
        0x222b6aa27e007612, 0xf7b7f47cf096afad, 0xe6a9fbafee77e77a, 0x3776ee406e63fbaa,
        0xde147932fcf78be6, 0x2ab9e031ffaa071e, 0x2169ad0e8a9b1256, 0xe33358135938b76a,
        0xcaec07e7a5373835, 0xef2863090a97c3ec, 0x6ccfb95f69c3adcc, 0x173e00da427cee4b,
        0x20f4ed58fcfb3040, 0x16f6fb326a60c32c, 0x2968fa04270ed545, 0x70673adfac0eabc4,
        0x6ff3c9364ff4e873, 0xde09ed35f13325d3, 0x2396e863b18c500f, 0xe22d253cc031e3ff,
        0x756d97a61247798d, 0xc9fc8d937e43c880, 0x0759ba59c08e14c7, 0xcd7aad86a4a45810,
        0x9f91c21c571dbe84, 0xd52d936f44abe8a3, 0xd5b48c100959d9d0, 0xb6cc856b3adc93b6,
        0x7aea8f8e067d2c8d, 0x04bc177f7b4287a6, 0xe3fcda36fa3b3342, 0xeaeb442e15d45095,
        0x2f4dd1ca5e89b18b, 0x602368385bb19cb1, 0x4bdfc434d3028181, 0x0b5a92cb80ac8150,
        0xb95953a97b1578ab, 0x46e6a18b01781b92, 0xdfd31585f38d7433, 0x0b1084b96009370b,
        0x9a81808e52462ba3, 0xff83368ace4af235, 0xb4e5d8a647e05e95, 0xf848cfc90df4b231,
        0x9919c68cf3576038, 0x1e89dad8a6790435, 0x7ac9361379139511, 0x7b5f9b6b937a7760,
        0x6e42e395fde0c1f7, 0x430cef1679799f8f, 0x0ad21cc1b4828074, 0x8982577d0ea42349,
        0xb1aca6185a7d0d0d, 0x4085c6db106c3d74, 0xba6f7a86e728a418, 0x0325a28758a974d2,
        0x57ea317f731817ed, 0xbd1e8e00b215a6eb, 0xb39f323742948e87, 0x9f9b0f873784cef4,
        0xa8c83d26585c5377, 0x837ba337bfcf893c, 0x0a7eeca62a23b805, 0xba4925a9e7f7346f,
        0xa574eebb90c8da6d, 0x5db7ff0e8d0b8d2d, 0x1562834c52c048d8, 0x0b2e577a853bcafc,
        0xdecef97a3524ff97, 0xeec053c8fd537066, 0xeaf2b1df83d600e4, 0x5be8b9ab7717eccf,
        0x05905b91ecbba038, 0xabacba5b373029ed, 0x22fb2283c0ee1267, 0x9c32b2ec3634c580,
        0x5186c586b6e5611c, 0x71eb0de5e91bb0a0, 0x89e969b42975ef08, 0x2ba0958bc44e322f,
        0x626d033cb828ba7d, 0xe5fbb65c7776509d, 0xb1403ae51ae9bc82, 0x5d773f0d9753a966,
        0x4a06feadd4ec8585, 0xda58a710fccd7b76, 0x6061ba4cd3d80d59, 0xf4824f5cfa2ba71c,
        0xfce622bba0ece756, 0x7d9c738486bc6842, 0x5f629d33c99db969, 0x855ff7c9b79362e6,
        0x892188a87c7de231, 0x85fea7caf30e2b5e, 0xbefeb221543782c5, 0x769ca33d280842f6,
        0x3974ebaf71353e52, 0xed0577283980f0cb, 0x7c37d689ab6b0662, 0x5037aeffcd3db52d,
        0x11bb0a5f64fbdcb5, 0xf5fd5aa5f2b7e974, 0xe1aa07ba7074367b, 0x4b5c14aa1c6a0d28,
        0xe9fc8c9c36f73953, 0x2609ad2cd0f99b76, 0x8d4f1d6bb589844f, 0xde09f066714fa909,
        0xe004c5d7adad3747, 0xd5ac81a94dfdefe3, 0xfd3e0083658a13c2, 0xf5512f25dd6e39a7,
        0xeb7204042ffa181d, 0x046d9254242d06e3, 0x91a5ca94f8706fab, 0xf5c58cc57af63c98,
        0x04e7ff1e23474908, 0xe4a9bec5c5818324, 0x1edfb105cc3084dd, 0x82431ec76e72a87a,
        0xe0b215be32c51083, 0x0d9942e3b5245098, 0xa49f1aad5723fd7e, 0xad45edba25a4bde8,
        0x241f0adc0cd56771, 0xf09bf2de59df3274, 0x090db856bbc020f2, 0x6aa4efb2d2ecb9bb,
        0xc6be4224ba04c233, 0x557a1760bde90850, 0x23090117938cb921, 0xcbec34da23f3e9c2,
        0xdfe2d55daad85c54, 0xa7932be700067f48, 0xfb7874535e2d76a4, 0x5161ba088056e74f,
        0xc275a8435be6cdb2, 0x05fcb771cab5aa15, 0x7f18a4382c9565a8, 0x4244c2cb833d6710,
        0x884e2b7a4a3db4d0, 0x08ded459d3edf2c2, 0x1616df531fee90cd, 0x9531c65800a97aaa,
        0x881ba77ab7e5d63a, 0x606d27428df4edd3, 0x294063ed78e305c7, 0x7de2b12f8a8cceb5,
        0xe6b01cc54a494437, 0x0cdecbe5ac90907c, 0xb88496c657d3e644, 0xf3eecf996f9c6b13,
        0x24aad7949edcde03, 0x304ca88ebfeaa534, 0x7b68a7bd3ef1916b, 0x3cc307a784d9060c,
        0x5dca03f19b213efd, 0xa380539c235f80c3, 0xf39756fc01d75bd7, 0x39ac6c7281739adb,
        0x4b606dc4aa036fda, 0x97126cd02a23b97c, 0x98c1e6906230aead, 0xe12d0f696a6bbc36,
        0x657a202bb6a89a33, 0x6421a07bda47e13d, 0x8d9d21b3c6b1dbee, 0x1f110f3744f13e0d,
        0x04d86fccb6e77ee8, 0x8c92852d9c9c14b3, 0x56be3cef19b19446, 0x57ceef0e2ebcbcf7,
        0x230a9328be0144bf, 0x3c1949b98a92aebc, 0x7ed2db80a62003f2, 0x84e609d13c7594f4,
        0xf8e81b9a9f35b4e8, 0xc2982fde1a087e4b, 0x84b0713cb3b18147, 0x3582530578d1ff08,
        0x0e5b6538cd61fce4, 0x46867abf4b6e72bc, 0x4fe9652832325e89, 0x7d141d065654745f,
        0x9bd5c0479188a53d, 0x4ccd47925108c00b, 0xfd3f6c8d961d47e3, 0x9c5c18a96093d2ad,
        0xa7d91bf008a358c3, 0x3ea3e5629f977d55, 0x80f0fed6a5f06003, 0x21f390e377ee4d68,
        0x73ed055ec082526b, 0x28482600c10f6ce2, 0x2bff1aaf94c11fe9, 0xde29cb7a943801b8,
        0x045b0493dd35af0e, 0xaeae25ff7a431c16, 0x78c9d3348f5364b7, 0xf973d1af84bc2476,
        0x4d2303e11baf18f3, 0xacebdb3fe5efbc7b, 0xd274a5cf5be50678, 0x2d60c40fdf53ac67,
        0x109592b606139855, 0x612f472a9c09925f, 0x701a035ccd4e7ab0, 0xac881f0db121a709,
        0xe1ed47438368366d, 0xde2faff8eeb2810a, 0x8eb2188044342ef9, 0x0e3c1aa7b6851548,
        0x7ce94a6ba4fd843f, 0x0da503676ee5ebb2, 0xf3bc7bb2cb8669e8, 0xd4b9e44de392fe64,
        0x81e470ebf207fdea, 0xdd53b09d49a0e5b5, 0xf78e23167a350d5a, 0x706470fc2d84423b,
        0x816ee82b19a29476, 0x35a9d218ba7cd4a1, 0xf590f12fb09b3fe3, 0x5e574140b302f8b7,
        0x6cb237a2021f77c3, 0x30a29037231a861e, 0xff4bb07af553a606, 0x831412ee2690d92c,
        0xf6d2d725ef14ff67, 0x2f79f810928a40ff, 0x2857d91ea9b04f71, 0xd063066f0ed78f3c,
        0xbf4b8dbc8a34017d, 0x6230f319f8b1f9c4, 0x061b0e25d8899834, 0x4071de32ef7ff0bf,
        0xbc546a0793fcfcd3, 0xd5881f5d968cf898, 0x0e21c0674cdda190, 0x0000000000000000};

    struct multiplier_index_info 
    {
        std::uint16_t first_cache_bit_index;
        std::uint16_t cache_bit_index_offset;
    };

    static constexpr multiplier_index_info multiplier_index_info_table[] = {
        {0, 0},         {171, 244},     {419, 565},     {740, 959},     {1135, 1427},
        {1604, 1969},   {2141, 2579},   {2750, 3261},   {3434, 4019},   {4191, 4849},
        {5019, 5750},   {5924, 6728},   {6904, 7781},   {7922, 8872},   {8993, 10016},
        {9026, 10122},  {9110, 10279},  {9245, 10487},  {9431, 10746},  {9668, 11056},
        {9956, 11418},  {10296, 11831}, {10687, 12295}, {11129, 12810}, {11622, 13376},
        {12166, 13993}, {12761, 14661}, {13407, 15380}, {14104, 16150}, {14852, 16902},
        {15582, 17627}, {16285, 18332}, {16968, 19019}, {17633, 19683}, {18275, 20326},
        {18896, 20947}, {19495, 21546}, {20072, 22122}, {20626, 22669}, {21151, 23202},
        {21662, 23713}, {22151, 24202}, {22618, 24669}, {23063, 25114}, {23486, 25535},
        {23885, 25936}, {24264, 26313}, {24619, 26670}, {24954, 27004}, {25266, 27316},
        {25556, 27603}, {25821, 27870}, {26066, 28117}, {26291, 28340}, {26492, 28543},
        {26673, 28723}, {26831, 28881}, {26967, 29018}, {27082, 29133}, {27175, 29225},
        {27245, 29296}, {27294, 29344}, {27320, 29370}, {27324, 0}};
};

#if defined(BOOST_NO_CXX17_INLINE_VARIABLES) && (!defined(BOOST_MSVC) || BOOST_MSVC != 1900)

template <bool b> constexpr std::size_t extended_cache_long_impl<b>::max_cache_blocks;
template <bool b> constexpr std::size_t extended_cache_long_impl<b>::cache_bits_unit;
template <bool b> constexpr int extended_cache_long_impl<b>::segment_length;
template <bool b> constexpr bool extended_cache_long_impl<b>::constant_block_count;
template <bool b> constexpr int extended_cache_long_impl<b>::e_min;
template <bool b> constexpr int extended_cache_long_impl<b>::k_min;
template <bool b> constexpr int extended_cache_long_impl<b>::cache_bit_index_offset_base;
template <bool b> constexpr std::uint64_t extended_cache_long_impl<b>::cache[];
template <bool b> constexpr typename extended_cache_long_impl<b>::multiplier_index_info extended_cache_long_impl<b>::multiplier_index_info_table[];

#endif

using extended_cache_long = extended_cache_long_impl<true>;

struct extended_cache_compact 
{
    static constexpr std::size_t max_cache_blocks = 6;
    static constexpr std::size_t cache_bits_unit = 64;
    static constexpr int segment_length = 80;
    static constexpr bool constant_block_count = false;
    static constexpr int collapse_factor = 64;
    static constexpr int e_min = -1074;
    static constexpr int k_min = -211;
    static constexpr int cache_bit_index_offset_base = 967;
    static constexpr int cache_block_count_offset_base = 27;

    static constexpr std::uint64_t cache[] = {
        0x9faacf3df73609b1, 0x77b191618c54e9ac, 0xcbc0fe19cae9528c, 0x8164d034592c3d4e,
        0x04c42d46c9d7a229, 0x7ee39007a5bc8cc3, 0x5469cf7bb8b25e57, 0x2effce010198cb81,
        0x642eb5bc0d8169e0, 0x91356aed1f5cd514, 0xe1c8f30156868b8c, 0xd1201a2b857f5cc5,
        0x15c07ee55715eff8, 0x8530360cd386f94f, 0xeb706c10ea02c329, 0x3cb22680f921f59e,
        0x3231912d5bf60e61, 0x0e1fff697ed6c695, 0xa8bed97c2f3b63fc, 0xda96e93c07538a6d,
        0xc1c4e34ccd6fdbc5, 0x85c09fd1d0f79834, 0x485f3a5d03622bba, 0xe640b09cca5b9d50,
        0x19a80913a40927a9, 0x4d82d751a5cf886d, 0x325c9cd793b9977b, 0x4896c18501fb9e0c,
        0xa9993bfdf3ea7275, 0xcb7d257a3ee7c9d8, 0xcbf8fdb78849a5f9, 0x6de98520472bdd03,
        0x36efd14b69b311de, 0x694fa387dcf3e78f, 0xdccfbfc61d1662ef, 0xbe3a4d4104fb75a2,
        0x289ccaebae5c6d2d, 0x436915952987fa63, 0x830446728505ab75, 0x3ad8772923e4e0c0,
        0xca946600436f3894, 0x0faae7895e3885f0, 0xadf6b773b1ebf8e0, 0x52473dd5e8218647,
        0x5e6b5121ca3b747c, 0x217399923cd80bc0, 0x0a56ced144bb2f9f, 0xb856e82eea863c1f,
        0x5cdae42f9562104d, 0x3fa421962c8c4241, 0x63451ff73769a3d2, 0xb0895649e11affd6,
        0xe5dd7be415e5d3ef, 0x282a242e818f1668, 0xc8a86da5faf0b5cc, 0xf5176ecc7cbb19db,
        0x2a9a282e49b4da0e, 0x59e22f9ed2cb3a4b, 0xc010afa26505a7e7, 0xee47b3ab83a99c3e,
        0xc7eafae5fa385ec2, 0x3ec747e06293a148, 0x4b8a8260baf424a7, 0x63079a1ac7709a4e,
        0x7fd0cd567aa4a0fa, 0x6909d0e0cfc6ce8d, 0xe0c965770d1491dd, 0xa6d4449e3a3e13ea,
        0x73e06d2253c6b584, 0x9f95a4b69679998d, 0x0cc8cc76a8234060, 0xd3da311bb4fc0aae,
        0x670614382f45f33c, 0x21f68425f4189fbf, 0x557ce28d58d9a8bd, 0x1f16d908907d0a0e,
        0x929415f993b9a2c2, 0x95e0878748988052, 0xc4a104701f794a31, 0xe7d2d2b0c3c31b19,
        0x1e6a68d5574b3d9d, 0x5727ec70c7681154, 0xe4b2adae8ac5259e, 0x1cefff5ed639205f,
        0xf9410ba5daeb3af5, 0x21b0ad30acb4b8d2, 0xd324604028bf6fac, 0x349a5d2dc4bdc6e0,
        0xc77223714aff22d9, 0x5b18ce4aabb5b369, 0xb8a6d609b15ecab7, 0x2111dbce86023643,
        0x2a5717a571b96b6c, 0x8039783af28427bf, 0x5bbadd6a1a3fb931, 0xe8564a7a3e3ff2dc,
        0xd0868939e541158e, 0xc57d0b8a8af06dde, 0xf1706d329def96c1, 0xbe74f435713bb7d5,
        0x8dcdaef5bfb0242c, 0x73b5a1c8c8ec33c7, 0x4ab726d9dac95550, 0x210cf3b3ddfa00ae,
        0x559d5e65eefbfa04, 0xe5d1f67c5f9de0ec, 0x6ad4699ea2d0efd6, 0x9590c0f05024f29a,
        0x917d5715e6e20913, 0xb13124a40bffe5ba, 0x5248ce22e40406e5, 0xb844b16596551ded,
        0xad4c4c5140496c58, 0x458562ae335689b6, 0x269441e13a195ad3, 0x7a5e32a8baf53ea8,
        0x6d1469edb474b5f6, 0xe87b554829f6ee5b, 0xbf824a42bae3bdef, 0xed12ec6937744feb,
        0x2ca544e624e048f9, 0x1bab8d5ee0c61285, 0x8863eaef018d32d9, 0x98f37ac46669f7ea,
        0xa9a0573cb5501b2b, 0xf25c3a8e08a5694d, 0x42355a8000000000, 0x0000000000000000};

    struct multiplier_index_info 
    {
        std::uint16_t first_cache_bit_index;
        std::uint16_t cache_bit_index_offset;
        std::uint16_t cache_block_count_index_offset;
    };

    static constexpr multiplier_index_info multiplier_index_info_table[] = {
        {0, 0, 0},          {377, 643, 9},      {1020, 1551, 22},  {1924, 2721, 39},
        {3046, 4109, 60},   {3114, 4443, 70},   {3368, 4962, 84},  {3807, 5667, 98},
        {4432, 6473, 111},  {5158, 7199, 123},  {5804, 7845, 134}, {6370, 8411, 143},
        {6856, 8896, 151},  {7261, 9302, 158},  {7587, 9628, 164}, {7833, 9874, 168},
        {7999, 10039, 171}, {8084, 10124, 173}, {8089, 0, 0}};

    static constexpr std::uint8_t cache_block_counts[] = {
        0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66,
        0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x56, 0x34, 0x12, 0x66,
        0x66, 0x45, 0x23, 0x61, 0x66, 0x66, 0x66, 0x45, 0x23, 0x61, 0x66, 0x66, 0x66,
        0x56, 0x34, 0x12, 0x66, 0x66, 0x66, 0x56, 0x34, 0x12, 0x66, 0x66, 0x66, 0x45,
        0x23, 0x61, 0x66, 0x56, 0x34, 0x12, 0x66, 0x56, 0x34, 0x12, 0x66, 0x45, 0x23,
        0x61, 0x45, 0x23, 0x41, 0x23, 0x31, 0x12, 0x12, 0x01};
};

#ifdef BOOST_CXX17_INLINE_VARIABLES

constexpr std::size_t extended_cache_compact::max_cache_blocks;
constexpr std::size_t extended_cache_compact::cache_bits_unit;
constexpr int extended_cache_compact::segment_length;
constexpr bool extended_cache_compact::constant_block_count;
constexpr int extended_cache_compact::collapse_factor;
constexpr int extended_cache_compact::e_min;
constexpr int extended_cache_compact::k_min;
constexpr int extended_cache_compact::cache_bit_index_offset_base;
constexpr int extended_cache_compact::cache_block_count_offset_base;
constexpr extended_cache_compact::multiplier_index_info extended_cache_compact::multiplier_index_info_table[];
constexpr std::uint8_t extended_cache_compact::cache_block_counts[];

#endif

struct extended_cache_super_compact 
{
    static constexpr std::size_t max_cache_blocks = 15;
    static constexpr std::size_t cache_bits_unit = 64;
    static constexpr int segment_length = 252;
    static constexpr bool constant_block_count = false;
    static constexpr int collapse_factor = 128;
    static constexpr int e_min = -1074;
    static constexpr int k_min = -65;
    static constexpr int cache_bit_index_offset_base = 1054;
    static constexpr int cache_block_count_offset_base = 10;

    static constexpr std::uint64_t cache[] = {
        0xf712b443bbd52b7b, 0xa5e9ec7501d523e4, 0x6f99ee8b281c132a, 0x1c7262e905287f33,
        0xbf4f71a69f411989, 0xe95fb0bf35d5c518, 0x00d875ffe81c1457, 0x31f0fcb03c200323,
        0x6f64d6af592895a0, 0x45c073ee14c78fb0, 0x8744404cbdba226c, 0x8dbe2386885f0c74,
        0x279b6693e94ab813, 0x6df0a4a86ccbb52e, 0xa94baea98e947129, 0xfc2b4e9bb4cbe9a4,
        0x73bbc273e753c4ad, 0xc70c8ff8c19c1059, 0xb7da754b6db8b578, 0x5214cf7f2274988c,
        0x39b5c4db3b36b321, 0xda6f355441d9f234, 0x01ab018d850bd7e2, 0x36517c3f140b3bcf,
        0xd0e52375d8d125a7, 0xaf9709f49f3b8404, 0x022dd12dd219aa3f, 0x46e2ecebe43f459e,
        0xa428ebddeecd6636, 0x3a7d11bff7e2a722, 0xd35d40e9d3b97c7d, 0x60ef65c4478901f1,
        0x945301feb0da841a, 0x2028c054ab187f51, 0xbe94b1f686a8b684, 0x09c13fdc1c4868c9,
        0xf2325ac2bf88a4ce, 0x92980d8fa53b6888, 0x8f6e17c7572a3359, 0x2964c5bfdd7761f2,
        0xf60269fc4910b562, 0x3ca164c4a2183ab0, 0x13f4f9e5a06a95c9, 0xf75022e39380598a,
        0x0d3f3c870002ab76, 0x24a4beb4780b78ef, 0x17a59a8f5696d625, 0x0ad76de884cb489d,
        0x559d3d0681553d6a, 0x813dcf205788af76, 0xf42f9c3ad707bf72, 0x770d63ceb129026c,
        0xa604d413fc14c7c2, 0x3cfc19e01239c784, 0xec7ef19965cedd56, 0x7303dcb3b300b6fd,
        0x118059e1139c0f3c, 0x97097186308c91f7, 0x2ad91d77379dce42, 0xad396c61acbe15ec,
        0x728518461b5722b6, 0xb85c5bb1ed805ecd, 0x816abc04592a4974, 0x1866b17c7cfbd0d0,
        0x0000000000000000};

    struct multiplier_index_info 
    {
        std::uint16_t first_cache_bit_index;
        std::uint16_t cache_bit_index_offset;
        std::uint16_t cache_block_count_index_offset;
    };

    static constexpr multiplier_index_info multiplier_index_info_table[] = {
        {0, 0, 0},        {860, 1698, 13},  {2506, 4181, 29}, {2941, 5069, 36},
        {3577, 5705, 41}, {3961, 6088, 44}, {4092, 0, 0}};

    static constexpr std::uint8_t cache_block_counts[] = {0xff, 0xff, 0xff, 0xff, 0xff, 0xee,
                                                            0xee, 0xee, 0xee, 0xee, 0xac, 0x68,
                                                            0x24, 0x8a, 0x46, 0x62, 0x24, 0x13};
};

#ifdef BOOST_CXX17_INLINE_VARIABLES

constexpr std::size_t extended_cache_super_compact::max_cache_blocks;
constexpr std::size_t extended_cache_super_compact::cache_bits_unit;
constexpr int extended_cache_super_compact::segment_length;
constexpr bool extended_cache_super_compact::constant_block_count;
constexpr int extended_cache_super_compact::collapse_factor;
constexpr int extended_cache_super_compact::e_min;
constexpr int extended_cache_super_compact::k_min;
constexpr int extended_cache_super_compact::cache_bit_index_offset_base;
constexpr int extended_cache_super_compact::cache_block_count_offset_base;
constexpr std::uint64_t extended_cache_super_compact::cache[];
constexpr extended_cache_super_compact::multiplier_index_info extended_cache_super_compact::multiplier_index_info_table[];
constexpr std::uint8_t extended_cache_super_compact::cache_block_counts[];

#endif

#ifdef BOOST_MSVC
# pragma warning(push)
# pragma warning(disable: 4100) // MSVC 14.0 warning of unused formal parameter is incorrect
#endif

template <unsigned v1, unsigned v2, typename ExtendedCache>
bool has_further_digits(std::uint64_t significand, int exp2_base, int& k, boost::charconv::detail::uconst<v1> additional_neg_exp_of_2_c, boost::charconv::detail::uconst<v2> additional_neg_exp_of_10_c) noexcept
{
    constexpr auto additional_neg_exp_of_2_v = static_cast<int>(decltype(additional_neg_exp_of_2_c)::value +
                                               decltype(additional_neg_exp_of_10_c)::value);
    
    constexpr auto additional_neg_exp_of_5_v = static_cast<int>(decltype(additional_neg_exp_of_10_c)::value);

    constexpr auto min_neg_exp_of_5 = (-ExtendedCache::k_min + additional_neg_exp_of_5_v) % ExtendedCache::segment_length;

    // k >= k_right_threshold iff k - k1 >= 0.
    static_assert(additional_neg_exp_of_5_v + ExtendedCache::segment_length >= 1 + ExtendedCache::k_min, 
    "additional_neg_exp_of_5_v + ExtendedCache::segment_length >= 1 + ExtendedCache::k_min");

    constexpr auto k_right_threshold = ExtendedCache::k_min + 
    ((additional_neg_exp_of_5_v + ExtendedCache::segment_length - 1 -
            ExtendedCache::k_min) /
            ExtendedCache::segment_length) *
            ExtendedCache::segment_length;

    // When the smallest absolute value of negative exponent for 5 is too big,
    // so whenever the exponent for 5 is negative, the result cannot be an
    // integer.
    BOOST_IF_CONSTEXPR (min_neg_exp_of_5 > 23)
    {
        return boost::charconv::detail::has_further_digits_impl::no_neg_k_can_be_integer<
            k_right_threshold, additional_neg_exp_of_2_v>(k, exp2_base);
    }
    // When the smallest absolute value of negative exponent for 5 is big enough, so
    // the only negative exponent for 5 that allows the result to be an integer is the
    // smallest one.
    else BOOST_IF_CONSTEXPR (min_neg_exp_of_5 + ExtendedCache::segment_length > 23)
    {
        // k < k_left_threshold iff k - k1 < -min_neg_exp_of_5.
        static_assert(additional_neg_exp_of_5_v + ExtendedCache::segment_length >= min_neg_exp_of_5 + 1 + ExtendedCache::k_min,
        "additional_neg_exp_of_5_v + ExtendedCache::segment_length >= min_neg_exp_of_5 + 1 + ExtendedCache::k_min");

        constexpr auto k_left_threshold =
            ExtendedCache::k_min +
            ((additional_neg_exp_of_5_v - min_neg_exp_of_5 +
                ExtendedCache::segment_length - 1 - ExtendedCache::k_min) /
                ExtendedCache::segment_length) *
                ExtendedCache::segment_length;

        return boost::charconv::detail::has_further_digits_impl::only_one_neg_k_can_be_integer<
            k_left_threshold, k_right_threshold, additional_neg_exp_of_2_v,
            min_neg_exp_of_5>(k, exp2_base, significand);
    }
    // When the smallest absolute value of negative exponent for 5 is big enough, so
    // the only negative exponents for 5 that allows the result to be an integer are the
    // smallest one and the next smallest one.
    else
    {
        static_assert(min_neg_exp_of_5 + 2 * ExtendedCache::segment_length > 23,
                        "min_neg_exp_of_5 + 2 * ExtendedCache::segment_length > 23");

        constexpr auto k_left_threshold =
            ExtendedCache::k_min +
            ((additional_neg_exp_of_5_v - min_neg_exp_of_5 - 1 - ExtendedCache::k_min) /
                ExtendedCache::segment_length) *
                ExtendedCache::segment_length;

        constexpr auto k_middle_threshold =
            ExtendedCache::k_min +
            ((additional_neg_exp_of_5_v - min_neg_exp_of_5 +
                ExtendedCache::segment_length - 1 - ExtendedCache::k_min) /
                ExtendedCache::segment_length) *
                ExtendedCache::segment_length;

        return boost::charconv::detail::has_further_digits_impl::only_two_neg_k_can_be_integer<
            k_left_threshold, k_middle_threshold, k_right_threshold,
            additional_neg_exp_of_2_v, min_neg_exp_of_5, ExtendedCache::segment_length>(
            k, exp2_base, significand);
    }
}

template <unsigned v1, unsigned v2, typename ExtendedCache>
inline bool has_further_digits(std::uint64_t significand, int exp2_base, int& k)
{
    boost::charconv::detail::uconst<v1> additional_neg_exp_of_2_c;
    boost::charconv::detail::uconst<v2> additional_neg_exp_of_10_c;

    return has_further_digits<v1, v2, ExtendedCache>(significand, exp2_base, k, additional_neg_exp_of_2_c, additional_neg_exp_of_10_c);
}

template <unsigned additional_neg_exp_of_2, unsigned additional_neg_exp_of_10, typename ExtendedCache>
bool compute_has_further_digits(unsigned remaining_subsegment_pairs, std::uint64_t significand, int exp2_base, int& k) noexcept
{
    #define BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(n)                                                        \
    case n:                                                                                            \
        return has_further_digits<additional_neg_exp_of_2, additional_neg_exp_of_10 + (n - 1) * 18, ExtendedCache>(significand, exp2_base, k)                                             
                                    switch (remaining_subsegment_pairs) {
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(1);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(2);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(3);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(4);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(5);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(6);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(7);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(8);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(9);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(10);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(11);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(12);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(13);
                                        BOOST_CHARCONV_252_HAS_FURTHER_DIGITS(14);

                                    default:
                                        BOOST_UNREACHABLE_RETURN(remaining_subsegment_pairs); // NOLINT
                                    }
    #undef BOOST_CHARCONV_252_HAS_FURTHER_DIGITS

    BOOST_UNREACHABLE_RETURN(false); // NOLINT
}

#ifdef BOOST_MSVC
# pragma warning(pop)
#endif

// Print 0.000...0 where precision is the number of 0's after the decimal dot.
inline to_chars_result print_zero_fixed(char* buffer, std::size_t buffer_size, const int precision) noexcept
{
    // No trailing decimal dot.
    if (precision == 0)
    {
        *buffer = '0';
        return {buffer + 1, std::errc()};
    }

    if (buffer_size < static_cast<std::size_t>(precision) + 2U)
    {
        return {buffer + buffer_size, std::errc::value_too_large};
    }

    std::memcpy(buffer, "0.", 2); // NOLINT : Specifically not null-terminating
    std::memset(buffer + 2, '0', static_cast<std::size_t>(precision)); // NOLINT : Specifically not null-terminating
    return {buffer + 2 + precision, std::errc()};
}

// precision means the number of decimal significand digits minus 1.
// Assumes round-to-nearest, tie-to-even rounding.
template <typename MainCache = main_cache_full, typename ExtendedCache>
BOOST_CHARCONV_SAFEBUFFERS to_chars_result floff(const double x, int precision, char* first, char* last,
                                                 boost::charconv::chars_format fmt) noexcept
{
    if (first >= last)
    {
        return {last, std::errc::value_too_large};
    }

    auto buffer_size = static_cast<std::size_t>(last - first);
    auto buffer = first;

    BOOST_CHARCONV_ASSERT(precision >= 0);
    using namespace detail;

    std::uint64_t br = default_float_traits<double>::float_to_carrier(x);
    bool is_negative = ((br >> 63) != 0);
    br <<= 1;
    int e = static_cast<int>(br >> (ieee754_binary64::significand_bits + 1));
    auto significand = (br & ((UINT64_C(1) << (ieee754_binary64::significand_bits + 1)) - 1)); // shifted by 1-bit.

    if (is_negative)
    {
        *buffer = '-';
        ++buffer;
        --buffer_size;

        if (buffer_size == 0)
        {
            return {buffer, std::errc::value_too_large};
        }
    }

    // Infinities or NaN
    if (e == ((UINT32_C(1) << ieee754_binary64::exponent_bits) - 1)) 
    {
        if (significand == 0)
        {
            constexpr std::size_t inf_chars = 3;

            if (buffer_size < inf_chars)
            {
                return {last, std::errc::value_too_large};
            }

            std::memcpy(buffer, "inf", inf_chars); // NOLINT : Specifically not null-terminating
            return {buffer + inf_chars, std::errc()};
        }
        else 
        {
            // Significand values for NaN by type
            // qNaN = 4503599627370496
            // sNaN = 2251799813685248
            //
            if (significand == UINT64_C(4503599627370496))
            {
                if (!is_negative)
                {
                    constexpr std::size_t nan_chars = 3;

                    if (buffer_size < nan_chars)
                    {
                        return {last, std::errc::value_too_large};
                    }

                    std::memcpy(buffer, "nan", nan_chars); // NOLINT : Specifically not null-terminating
                    return {buffer + nan_chars, std::errc()};
                }
                else
                {
                    constexpr std::size_t neg_nan_chars = 8;

                    if (buffer_size < neg_nan_chars)
                    {
                        return {last, std::errc::value_too_large};
                    }

                    std::memcpy(buffer, "nan(ind)", neg_nan_chars); // NOLINT : Specifically not null-terminating
                    return {buffer + neg_nan_chars, std::errc()};
                }
            }
            else
            {
                constexpr std::size_t snan_chars = 9;

                if (buffer_size < snan_chars)
                {
                    return {last, std::errc::value_too_large};
                }

                std::memcpy(buffer, "nan(snan)", snan_chars); // NOLINT : Specifically not null-terminating
                return {buffer + snan_chars, std::errc()};
            }
        }
    }
    else
    {
        // Normal numbers.
        if (e != 0)
        {
            significand |= (decltype(significand)(1) << (ieee754_binary64::significand_bits + 1));
            e += (ieee754_binary64::exponent_bias - ieee754_binary64::significand_bits);
        }
        // Subnormal numbers.
        else 
        {
            // Zero
            if (significand == 0) 
            {
                if (fmt == boost::charconv::chars_format::general)
                {
                    // For the case of chars_format::general, 0 is always printed as 0.
                    *buffer = '0';
                    return {buffer + 1, std::errc()};
                }
                else if (fmt == boost::charconv::chars_format::fixed)
                {
                    return print_zero_fixed(buffer, buffer_size, precision);
                }
                // For the case of chars_format::scientific, print as many 0's as requested after the decimal dot, and then print e+00.
                if (precision == 0) 
                {
                    constexpr std::size_t zero_chars = 5;

                    if (buffer_size < zero_chars)
                    {
                        return {last, std::errc::value_too_large};
                    }

                    std::memcpy(buffer, "0e+00", zero_chars);
                    return {buffer + zero_chars, std::errc()};
                }
                else 
                {
                    if (buffer_size < static_cast<std::size_t>(precision) + 6U)
                    {
                        return {last, std::errc::value_too_large};
                    }

                    std::memcpy(buffer, "0.", 2); // NOLINT : Specifically not null-terminating
                    std::memset(buffer + 2, '0', static_cast<std::size_t>(precision)); // NOLINT : Specifically not null-terminating
                    std::memcpy(buffer + 2 + precision, "e+00", 4); // NOLINT : Specifically not null-terminating
                    return {buffer + precision + 6, std::errc()};
                }
            }
            // Nonzero
            e = ieee754_binary64::min_exponent - ieee754_binary64::significand_bits;
        }
    }

    constexpr int kappa = 2;
    int k = kappa - log::floor_log10_pow2(e);
    std::uint32_t current_digits {};
    char* const buffer_starting_pos = buffer;
    char* decimal_dot_pos = buffer; // decimal_dot_pos == buffer_starting_pos indicates that there should be no decimal dot.
    int decimal_exponent_normalized {};

    // Number of digits to be printed.
    int remaining_digits {};

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /// Phase 1 - Print the first digit segment computed with the Dragonbox table.
    /////////////////////////////////////////////////////////////////////////////////////////////////

    {
        // Compute the first digit segment.
        const auto main_cache = MainCache::template get_cache<ieee754_binary64>(k);
        const int beta = e + log::floor_log2_pow10(k);

        // Integer check is okay for binary64.
        //auto [first_segment, has_more_segments] 
        compute_mul_result segments = [&] {
            const auto r = umul192_upper128(significand << beta, main_cache);
            return compute_mul_result{r.high, r.low == 0};
        }();

        auto first_segment = segments.result;
        auto has_more_segments = !segments.is_integer;

        // The first segment can be up to 19 digits. It is in fact always of either 18 or 19
        // digits except when the input is a subnormal number. For subnormal numbers, the
        // smallest possible value of the first segment is 10^kappa, so it is of at least
        // kappa+1 digits (i.e., 3 in this case).

        int first_segment_length = 19;
        auto first_segment_aligned = first_segment; // Aligned to have 19 digits.
        while (first_segment_aligned < UINT64_C(10000000000000000))
        {
            first_segment_aligned *= 100;
            first_segment_length -= 2;
        }
        if (first_segment_aligned < UINT64_C(1000000000000000000))
        {
            first_segment_aligned *= 10;
            first_segment_length -= 1;
        }
        // The decimal exponent when written as X.XXXX.... x 10^XX.
        decimal_exponent_normalized = first_segment_length - k - 1;

        // Figure out the correct value of remaining_digits.
        if (fmt == boost::charconv::chars_format::scientific)
        {
            remaining_digits = precision + 1;
            int exponent_print_length =
                decimal_exponent_normalized >= 100 ? 5 :
                decimal_exponent_normalized <= -100 ? 6 :
                decimal_exponent_normalized >= 0 ? 4 : 5;

            // No trailing decimal dot.
            auto minimum_required_buffer_size =
                static_cast<std::size_t>(remaining_digits + exponent_print_length + (precision != 0 ? 1 : 0));
            if (buffer_size < minimum_required_buffer_size)
            {
                return {last, std::errc::value_too_large};
            }

            if (precision != 0)
            {
                // Reserve a place for the decimal dot.
                *buffer = '0';
                ++buffer;
                ++decimal_dot_pos;
            }
        }
        else if (fmt == boost::charconv::chars_format::fixed)
        {
            if (decimal_exponent_normalized >= 0)
            {
                remaining_digits = precision + decimal_exponent_normalized + 1;
                
                // No trailing decimal dot.
                auto minimum_required_buffer_size =
                    static_cast<std::size_t>(remaining_digits + (precision != 0 ? 1 : 0));

                // We need one more space if the rounding changes the exponent,
                // but since we don't know at this point if that will actually happen, handle such a case later.

                if (buffer_size < minimum_required_buffer_size)
                {
                    return {last, std::errc::value_too_large};
                }

                if (precision != 0)
                {
                    // Reserve a place for the decimal dot.
                    *buffer = '0';
                    ++buffer;
                    decimal_dot_pos += decimal_exponent_normalized + 1;
                }
            }
            else
            {
                int number_of_leading_zeros = -decimal_exponent_normalized - 1;

                // When there are more than precision number of leading zeros,
                // all the digits we need to print are 0.
                if (number_of_leading_zeros > precision)
                {
                    return print_zero_fixed(buffer, buffer_size, precision);
                }
                // When the number of leading zeros is exactly precision,
                // then we might need to print 1 at the last digit due to rounding.
                if (number_of_leading_zeros == precision)
                {
                    // Since the last digit before rounding is 0,
                    // according to the "round-to-nearest, tie-to-even" rule, we round-up
                    // if and only if the input is strictly larger than the midpoint.
                    bool round_up = (first_segment_aligned + (has_more_segments ? 1 : 0)) > UINT64_C(5000000000000000000);
                    if (!round_up)
                    {
                        return print_zero_fixed(buffer, buffer_size, precision);
                    }

                    // No trailing decimal dot.
                    if (precision == 0)
                    {
                        *buffer = '1';
                        return {buffer + 1, std::errc()};
                    }

                    if (buffer_size < static_cast<std::size_t>(precision) + 2U)
                    {
                        return {buffer + buffer_size, std::errc::value_too_large};
                    }

                    std::memcpy(buffer, "0.", 2); // NOLINT : Specifically not null-terminating
                    std::memset(buffer + 2, '0', static_cast<std::size_t>(precision - 1)); // NOLINT : Specifically not null-terminating
                    buffer[1 + precision] = '1';
                    return {buffer + 2 + precision, std::errc()};
                }

                remaining_digits = precision - number_of_leading_zeros;
                
                // Always have decimal dot.
                BOOST_CHARCONV_ASSERT(precision > 0);
                auto minimum_required_buffer_size = static_cast<std::size_t>(precision + 2);
                if (buffer_size < minimum_required_buffer_size)
                {
                    return {last, std::errc::value_too_large};
                }

                // Print leading zeros.
                std::memset(buffer, '0', static_cast<std::size_t>(number_of_leading_zeros + 2));
                buffer += number_of_leading_zeros + 2;
                ++decimal_dot_pos;
            }
        }
        else
        {
            // fmt == boost::charconv::chars_format::general
            if (precision == 0)
            {
                // For general format, precision = 0 is interpreted as precision = 1.
                precision = 1;
            }
            remaining_digits = precision;

            // Use scientific format if decimal_exponent_normalized <= -6 or decimal_exponent_normalized >= precision.
            // Use fixed format if -4 <= decimal_exponent_normalized <= precision - 2.
            // If decimal_exponent_normalized == -5, use fixed format if and only if the rounding increases the exponent.
            // If decimal_exponent_normalized == precision - 1, use scientific format if and only if the rounding increases the exponent.
            // Since we cannot reliably decide which format to use, necessary corrections will be made in the last phase.

            // We may end up not printing the decimal dot if fixed format is chosen, but reserve a place anyway.
            *buffer = '0';
            ++buffer;
            decimal_dot_pos += (0 < decimal_exponent_normalized && decimal_exponent_normalized < precision)
                                ? decimal_exponent_normalized + 1 : 1;
        }

        if (remaining_digits <= 2) 
        {
            uint128 prod;
            std::uint64_t fractional_part64;
            std::uint64_t fractional_part_rounding_threshold64;

            // Convert to fixed-point form with 64/32-bit boundary for the fractional part.

            if (remaining_digits == 1)
            {
                prod = umul128(first_segment_aligned, UINT64_C(1329227995784915873));
                // ceil(2^63 + 2^64/10^18)
                fractional_part_rounding_threshold64 = additional_static_data_holder::fractional_part_rounding_thresholds64[17];
            }
            else
            {
                prod = umul128(first_segment_aligned, UINT64_C(13292279957849158730));
                // ceil(2^63 + 2^64/10^17)
                fractional_part_rounding_threshold64 = additional_static_data_holder::
                    fractional_part_rounding_thresholds64[16];
            }
            fractional_part64 = (prod.low >> 56) | (prod.high << 8);
            current_digits = static_cast<std::uint32_t>(prod.high >> 56);

            // Perform rounding, print the digit, and return.
            if (remaining_digits == 1)
            {
                if (fractional_part64 >= fractional_part_rounding_threshold64 ||
                    ((fractional_part64 >> 63) & (has_more_segments | (current_digits & 1))) != 0) 
                {
                    goto round_up_one_digit;
                }

                print_1_digit(current_digits, buffer);
                ++buffer;
            }
            else 
            {
                if (fractional_part64 >= fractional_part_rounding_threshold64 ||
                    ((fractional_part64 >> 63) & (has_more_segments | (current_digits & 1))) != 0)
                {
                    goto round_up_two_digits;
                }

                print_2_digits(current_digits, buffer);
                buffer += 2;
            }

            goto insert_decimal_dot;
        } // remaining_digits <= 2

        // At this point, there are at least 3 digits to print.
        // We split the segment into three chunks, each consisting of 9 digits, 8 digits,
        // and 2 digits.

        // MSVC doesn't know how to do Grandlund-Montgomery for large 64-bit integers.
        // 7922816251426433760 = ceil(2^96/10^10) = floor(2^96*(10^9/(10^19 - 1)))
        const auto first_subsegment =
            static_cast<std::uint32_t>(umul128_upper64(first_segment, UINT64_C(7922816251426433760)) >> 32);

        const auto second_third_subsegments =
            first_segment - first_subsegment * UINT64_C(10000000000);

        BOOST_CHARCONV_ASSERT(first_subsegment < UINT64_C(1000000000));
        BOOST_CHARCONV_ASSERT(second_third_subsegments < UINT64_C(10000000000));

        int remaining_digits_in_the_current_subsegment;
        std::uint64_t prod; // holds intermediate values for digit generation.

        // Print the first subsegment.
        if (first_subsegment != 0)
        {
            // 9 digits (19 digits in total).
            if (first_subsegment >= 100000000) 
            {
                // 1441151882 = ceil(2^57 / 10^8) + 1
                prod = first_subsegment * UINT64_C(1441151882);
                prod >>= 25;
                remaining_digits_in_the_current_subsegment = 8;
            }
            // 7 or 8 digits (17 or 18 digits in total).
            else if (first_subsegment >= 1000000)
            {
                // 281474978 = ceil(2^48 / 10^6) + 1
                prod = first_subsegment * UINT64_C(281474978);
                prod >>= 16;
                remaining_digits_in_the_current_subsegment = 6;
            }
            // 5 or 6 digits (15 or 16 digits in total).
            else if (first_subsegment >= 10000)
            {
                // 429497 = ceil(2^32 / 10^4)
                prod = first_subsegment * UINT64_C(429497);
                remaining_digits_in_the_current_subsegment = 4;
            }
            // 3 or 4 digits (13 or 14 digits in total).
            else if (first_subsegment >= 100)
            {
                // 42949673 = ceil(2^32 / 10^2)
                prod = first_subsegment * UINT64_C(42949673);
                remaining_digits_in_the_current_subsegment = 2;
            }
            // 1 or 2 digits (11 or 12 digits in total).
            else
            {
                prod = std::uint64_t(first_subsegment) << 32;
                remaining_digits_in_the_current_subsegment = 0;
            }

            const auto initial_digits = static_cast<std::uint32_t>(prod >> 32);

            buffer -= (initial_digits < 10 ? 1 : 0);
            remaining_digits -= (2 - (initial_digits < 10 ? 1 : 0));
            print_2_digits(initial_digits, buffer);
            buffer += 2;

            if (remaining_digits > remaining_digits_in_the_current_subsegment) 
            {
                remaining_digits -= remaining_digits_in_the_current_subsegment;
                
                for (; remaining_digits_in_the_current_subsegment > 0; remaining_digits_in_the_current_subsegment -= 2) 
                {
                    // Write next two digits.
                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                    buffer += 2;
                }
            }
            else 
            {
                for (int i = 0; i < (remaining_digits - 1) / 2; ++i) 
                {
                    // Write next two digits.
                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                    buffer += 2;
                }

                // Distinguish two cases of rounding.
                if (remaining_digits_in_the_current_subsegment > remaining_digits)
                {
                    if ((remaining_digits & 1) != 0) 
                    {
                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                    }
                    else 
                    {
                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    }
                    
                    current_digits = static_cast<std::uint32_t>(prod >> 32);

                    if (check_rounding_condition_inside_subsegment(
                            current_digits, static_cast<std::uint32_t>(prod),
                            remaining_digits_in_the_current_subsegment - remaining_digits,
                            second_third_subsegments != 0 || has_more_segments)) 
                    {
                        goto round_up;
                    }

                    goto print_last_digits;
                }
                else 
                {
                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    current_digits = static_cast<std::uint32_t>(prod >> 32);

                    if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                            current_digits,
                            uint_with_known_number_of_digits<10>{second_third_subsegments},
                            has_more_segments)) 
                    {
                        goto round_up_two_digits;
                    }

                    goto print_last_two_digits;
                }
            }
        }

        // Print the second subsegment.
        // The second subsegment cannot be zero even for subnormal numbers.

        if (remaining_digits <= 2) 
        {
            // In this case the first subsegment must be nonzero.

            if (remaining_digits == 1) 
            {
                const auto prod128 = umul128(second_third_subsegments, UINT64_C(18446744074));

                current_digits = static_cast<std::uint32_t>(prod128.high);
                const auto fractional_part64 = prod128.low + 1;
                // 18446744074 is even, so prod.low cannot be equal to 2^64 - 1.
                BOOST_CHARCONV_ASSERT(fractional_part64 != 0);

                if (fractional_part64 >= additional_static_data_holder::fractional_part_rounding_thresholds64[8] ||
                    ((fractional_part64 >> 63) & (has_more_segments | (current_digits & 1))) != 0) 
                {
                    goto round_up_one_digit;
                }

                goto print_last_one_digit;
            } // remaining_digits == 1
            else 
            {
                const auto prod128 = umul128(second_third_subsegments, UINT64_C(184467440738));

                current_digits = static_cast<std::uint32_t>(prod128.high);
                const auto fractional_part64 = prod128.low + 1;
                // 184467440738 is even, so prod.low cannot be equal to 2^64 - 1.
                BOOST_CHARCONV_ASSERT(fractional_part64 != 0);

                if (fractional_part64 >= additional_static_data_holder::fractional_part_rounding_thresholds64[7] ||
                    ((fractional_part64 >> 63) & (has_more_segments | (current_digits & 1))) != 0) 
                {
                    goto round_up_two_digits;
                }

                goto print_last_two_digits;
            }
        } // remaining_digits <= 2

        // Compilers are not aware of how to leverage the maximum value of
        // second_third_subsegments to find out a better magic number which allows us to
        // eliminate an additional shift.
        // 184467440737095517 = ceil(2^64/100) < floor(2^64*(10^8/(10^10 - 1))).
        const auto second_subsegment = static_cast<std::uint32_t>(
            umul128_upper64(second_third_subsegments, UINT64_C(184467440737095517)));

        // Since the final result is of 2 digits, we can do the computation in 32-bits.
        const auto third_subsegment =
            static_cast<std::uint32_t>(second_third_subsegments) - second_subsegment * 100;

        BOOST_CHARCONV_ASSERT(second_subsegment < 100000000);
        BOOST_CHARCONV_ASSERT(third_subsegment < 100);

        {
            std::uint32_t initial_digits;
            if (first_subsegment != 0) 
            {
                prod = ((second_subsegment * UINT64_C(281474977)) >> 16) + 1;
                remaining_digits_in_the_current_subsegment = 6;

                initial_digits = static_cast<std::uint32_t>(prod >> 32);
                remaining_digits -= 2;
            }
            else 
            {
                // 7 or 8 digits (9 or 10 digits in total).
                if (second_subsegment >= 1000000) 
                {
                    prod = (second_subsegment * UINT64_C(281474978)) >> 16;
                    remaining_digits_in_the_current_subsegment = 6;
                }
                // 5 or 6 digits (7 or 8 digits in total).
                else if (second_subsegment >= 10000)
                {
                    prod = second_subsegment * UINT64_C(429497);
                    remaining_digits_in_the_current_subsegment = 4;
                }
                // 3 or 4 digits (5 or 6 digits in total).
                else if (second_subsegment >= 100)
                {
                    prod = second_subsegment * UINT64_C(42949673);
                    remaining_digits_in_the_current_subsegment = 2;
                }
                // 1 or 2 digits (3 or 4 digits in total).
                else
                {
                    prod = std::uint64_t(second_subsegment) << 32;
                    remaining_digits_in_the_current_subsegment = 0;
                }

                initial_digits = static_cast<std::uint32_t>(prod >> 32);
                buffer -= (initial_digits < 10 ? 1 : 0);
                remaining_digits -= (2 - (initial_digits < 10 ? 1 : 0));
            }

            print_2_digits(initial_digits, buffer);
            buffer += 2;

            if (remaining_digits > remaining_digits_in_the_current_subsegment)
            {
                remaining_digits -= remaining_digits_in_the_current_subsegment;
                for (; remaining_digits_in_the_current_subsegment > 0; remaining_digits_in_the_current_subsegment -= 2) 
                {
                    // Write next two digits.
                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                    buffer += 2;
                }
            }
            else 
            {
                for (int i = 0; i < (remaining_digits - 1) / 2; ++i) 
                {
                    // Write next two digits.
                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                    buffer += 2;
                }

                // Distinguish two cases of rounding.
                if (remaining_digits_in_the_current_subsegment > remaining_digits) 
                {
                    if ((remaining_digits & 1) != 0) 
                    {
                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                    }
                    else 
                    {
                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    }
                    current_digits = static_cast<std::uint32_t>(prod >> 32);

                    if (check_rounding_condition_inside_subsegment(
                            current_digits, static_cast<std::uint32_t>(prod),
                            remaining_digits_in_the_current_subsegment - remaining_digits,
                            third_subsegment != 0 || has_more_segments)) 
                    {
                        goto round_up;
                    }

                    goto print_last_digits;
                }
                else 
                {
                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    current_digits = static_cast<std::uint32_t>(prod >> 32);

                    if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                            current_digits,
                            uint_with_known_number_of_digits<2>{third_subsegment},
                            has_more_segments)) 
                    {
                        goto round_up_two_digits;
                    }

                    goto print_last_two_digits;
                }
            }
        }

        // Print the third subsegment.
        {
            if (remaining_digits > 2) 
            {
                print_2_digits(third_subsegment, buffer);
                buffer += 2;
                remaining_digits -= 2;

                // If there is no more segment, then fill remaining digits with 0's and return.
                if (!has_more_segments) 
                {
                    goto fill_remaining_digits_with_0s;
                }
            }
            else if (remaining_digits == 1) 
            {
                prod = third_subsegment * UINT64_C(429496730);
                current_digits = static_cast<std::uint32_t>(prod >> 32);

                if (check_rounding_condition_inside_subsegment(
                        current_digits, static_cast<std::uint32_t>(prod), 1, has_more_segments)) 
                {
                    goto round_up_one_digit;
                }

                goto print_last_one_digit;
            }
            else 
            {
                // remaining_digits == 2.
                // If there is no more segment, then print the current two digits and return.
                if (!has_more_segments)
                {
                    print_2_digits(third_subsegment, buffer);
                    buffer += 2;
                    goto insert_decimal_dot;
                }

                // Otherwise, for performing the rounding, we have to wait until the next
                // segment becomes available. This state can be detected afterward by
                // inspecting if remaining_digits == 0.
                remaining_digits = 0;
                current_digits = third_subsegment;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /// Phase 2 - Print further digit segments computed with the extended cache table.
    /////////////////////////////////////////////////////////////////////////////////////////////////

    {
        auto multiplier_index =
            static_cast<std::uint32_t>(k + ExtendedCache::segment_length - ExtendedCache::k_min) /
            static_cast<std::uint32_t>(ExtendedCache::segment_length);

        int digits_in_the_second_segment;
        {
            const auto new_k =
                ExtendedCache::k_min + static_cast<int>(multiplier_index) * ExtendedCache::segment_length;
            digits_in_the_second_segment = new_k - k;
            k = new_k;
        }

        const auto exp2_base = e + boost::core::countr_zero(significand);

        using cache_block_type = typename std::decay<decltype(ExtendedCache::cache[0])>::type;
        
        cache_block_type blocks[ExtendedCache::max_cache_blocks];
        cache_block_count_t<ExtendedCache::constant_block_count, ExtendedCache::max_cache_blocks> cache_block_count;

        // Deal with the second segment. The second segment is special because it can have
        // overlapping digits with the first segment. Note that we cannot just move the buffer
        // pointer backward and print the whole segment from there, because it may contain
        // leading zeros.
        {
            cache_block_count =
                load_extended_cache<ExtendedCache, ExtendedCache::constant_block_count>(
                    blocks, e, k, multiplier_index);

            // Compute nm mod 2^Q.
            fixed_point_calculator<ExtendedCache::max_cache_blocks>::discard_upper(significand, blocks, cache_block_count);

            BOOST_CHARCONV_IF_CONSTEXPR (ExtendedCache::segment_length == 22)
            {
                // No rounding, continue.
                if (remaining_digits > digits_in_the_second_segment)
                {
                    remaining_digits -= digits_in_the_second_segment;

                    if (digits_in_the_second_segment <= 2)
                    {
                        BOOST_CHARCONV_ASSERT(digits_in_the_second_segment != 0);

                        fixed_point_calculator<ExtendedCache::max_cache_blocks>::discard_upper(
                            power_of_10[19], blocks, cache_block_count);

                        auto subsegment =
                            fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                                generate_and_discard_lower(power_of_10[3], blocks,
                                                            cache_block_count);

                        if (digits_in_the_second_segment == 1)
                        {
                            auto prod = subsegment * UINT64_C(429496730);
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                            print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                            ++buffer;
                        }
                        else 
                        {
                            auto prod = subsegment * UINT64_C(42949673);
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                        }
                    } // digits_in_the_second_segment <= 2
                    else if (digits_in_the_second_segment <= 16)
                    {
                        BOOST_CHARCONV_ASSERT(22 - digits_in_the_second_segment <= 19);
                        
                        fixed_point_calculator<ExtendedCache::max_cache_blocks>::discard_upper(
                            compute_power(UINT64_C(10), 22 - digits_in_the_second_segment),
                            blocks, cache_block_count);

                        // When there are at most 9 digits, we can store them in 32-bits.
                        if (digits_in_the_second_segment <= 9) 
                        {
                            // The number of overlapping digits is in the range 13 ~ 19.
                            const auto subsegment =
                                fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                                    generate_and_discard_lower(power_of_10[9], blocks,
                                                                cache_block_count);

                            std::uint64_t prod;
                            if ((digits_in_the_second_segment & 1) != 0)
                            {
                                prod = ((subsegment * UINT64_C(720575941)) >> 24) + 1;
                                print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                                ++buffer;
                            }
                            else
                            {
                                prod = ((subsegment * UINT64_C(450359963)) >> 20) + 1;
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }

                            for (; digits_in_the_second_segment > 2; digits_in_the_second_segment -= 2)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }
                        } // digits_in_the_second_segment <= 9
                        else 
                        {
                            // The number of digits in the segment is in the range 10 ~ 16.
                            const auto first_second_subsegments =
                                fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                                    generate_and_discard_lower(power_of_10[16], blocks,
                                                                cache_block_count);

                            // The first segment is of 8 digits, and the second segment is of
                            // 2 ~ 8 digits.
                            // ceil(2^(64+14)/10^8) = 3022314549036573
                            // = floor(2^(64+14)*(10^8/(10^16 - 1)))
                            const auto first_subsegment =
                                static_cast<std::uint32_t>(umul128_upper64(first_second_subsegments,
                                                                        UINT64_C(3022314549036573)) >>
                                                14);
                            const auto second_subsegment =
                                static_cast<std::uint32_t>(first_second_subsegments) -
                                UINT32_C(100000000) * first_subsegment;

                            // Print the first subsegment.
                            print_8_digits(first_subsegment, buffer);
                            buffer += 8;

                            // Print the second subsegment.
                            // There are at least 2 digits in the second subsegment.
                            auto prod = ((second_subsegment * UINT64_C(140737489)) >> 15) + 1;
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                            digits_in_the_second_segment -= 10;

                            for (; digits_in_the_second_segment > 1; digits_in_the_second_segment -= 2)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }

                            if (digits_in_the_second_segment != 0)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                                print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                                ++buffer;
                            }
                        }
                    } // digits_in_the_second_segment <= 16
                    else 
                    {
                        // The number of digits in the segment is in the range 17 ~ 22.
                        const auto first_subsegment =
                            fixed_point_calculator<ExtendedCache::max_cache_blocks>::generate(
                                power_of_10[6], blocks, cache_block_count);

                        const auto second_third_subsegments =
                            fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                                generate_and_discard_lower(power_of_10[16], blocks,
                                                            cache_block_count);

                        // ceil(2^(64+14)/10^8) = 3022314549036573
                        // = floor(2^(64+14)*(10^8/(10^16 - 1)))
                        const auto second_subsegment =
                            static_cast<std::uint32_t>(umul128_upper64(second_third_subsegments,
                                                                    UINT64_C(3022314549036573)) >>
                                            14);
                        const auto third_subsegment = static_cast<std::uint32_t>(second_third_subsegments) -
                                                        UINT32_C(100000000) * second_subsegment;

                        // Print the first subsegment (1 ~ 6 digits).
                        std::uint64_t prod {};
                        auto remaining_digits_in_the_current_subsegment =
                            digits_in_the_second_segment - 16;

                        switch (remaining_digits_in_the_current_subsegment)
                        {
                        case 1:
                            prod = first_subsegment * UINT64_C(429496730);
                            goto second_segment22_more_than_16_digits_first_subsegment_no_rounding_odd_remaining;

                        case 2:
                            prod = first_subsegment * UINT64_C(42949673);
                            goto second_segment22_more_than_16_digits_first_subsegment_no_rounding_even_remaining;

                        case 3:
                            prod = first_subsegment * UINT64_C(4294968);
                            goto second_segment22_more_than_16_digits_first_subsegment_no_rounding_odd_remaining;

                        case 4:
                            prod = first_subsegment * UINT64_C(429497);
                            goto second_segment22_more_than_16_digits_first_subsegment_no_rounding_even_remaining;

                        case 5:
                            prod = ((first_subsegment * UINT64_C(687195)) >> 4) + 1;
                            goto second_segment22_more_than_16_digits_first_subsegment_no_rounding_odd_remaining;

                        case 6:
                            prod = first_subsegment * UINT64_C(429497);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                            remaining_digits_in_the_current_subsegment = 4;
                            goto second_segment22_more_than_16_digits_first_subsegment_no_rounding_even_remaining;

                        default:
                            BOOST_UNREACHABLE_RETURN(prod); // NOLINT
                        }

                    second_segment22_more_than_16_digits_first_subsegment_no_rounding_odd_remaining
                        :
                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                        print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                        ++buffer;

                    second_segment22_more_than_16_digits_first_subsegment_no_rounding_even_remaining
                        :
                        for (; remaining_digits_in_the_current_subsegment > 1;
                                remaining_digits_in_the_current_subsegment -= 2)
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                        }

                        // Print the second and third subsegments (8 digits each).
                        print_8_digits(second_subsegment, buffer);
                        print_8_digits(third_subsegment, buffer + 8);
                        buffer += 16;
                    }
                } // remaining_digits > digits_in_the_second_segment

                // Perform rounding and return.
                else
                {
                    if (digits_in_the_second_segment <= 2)
                    {
                        fixed_point_calculator<ExtendedCache::max_cache_blocks>::discard_upper(
                            power_of_10[19], blocks, cache_block_count);

                        // Get one more bit for potential rounding on the segment boundary.
                        auto subsegment =
                            fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                                generate_and_discard_lower(2000, blocks, cache_block_count);

                        bool segment_boundary_rounding_bit = ((subsegment & 1) != 0);
                        subsegment >>= 1;

                        if (digits_in_the_second_segment == 2)
                        {
                            // Convert subsegment into fixed-point fractional form where the
                            // integer part is of one digit. The integer part is ignored.
                            // 42949673 = ceil(2^32/10^2)
                            auto prod = static_cast<std::uint64_t>(subsegment) * UINT64_C(42949673);

                            if (remaining_digits == 1)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                                current_digits = static_cast<std::uint32_t>(prod >> 32);
                                const bool has_further_digits_v = has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0);
                                if (check_rounding_condition_inside_subsegment(current_digits, static_cast<std::uint32_t>(prod), 1, has_further_digits_v))
                                {
                                    goto round_up_one_digit;
                                }
                                goto print_last_one_digit;
                            }

                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            const auto next_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 0)
                            {
                                if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                        current_digits,
                                        uint_with_known_number_of_digits<2>{next_digits},
                                        has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0))) 
                                {
                                    goto round_up_two_digits;
                                }
                                goto print_last_two_digits;
                            }

                            current_digits = next_digits;
                            BOOST_CHARCONV_ASSERT(remaining_digits == 2);
                        }
                        else
                        {
                            BOOST_CHARCONV_ASSERT(digits_in_the_second_segment == 1);
                            // Convert subsegment into fixed-point fractional form where the
                            // integer part is of two digits. The integer part is ignored.
                            // 429496730 = ceil(2^32/10^1)
                            auto prod = static_cast<std::uint64_t>(subsegment) * UINT64_C(429496730);
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                            const auto next_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 0) 
                            {
                                if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                        current_digits,
                                        uint_with_known_number_of_digits<1>{next_digits},
                                        has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0)))
                                {
                                    goto round_up_two_digits;
                                }
                                goto print_last_two_digits;
                            }

                            current_digits = next_digits;
                            BOOST_CHARCONV_ASSERT(remaining_digits == 1);
                        }

                        if (check_rounding_condition_with_next_bit(
                                current_digits, segment_boundary_rounding_bit,
                                has_further_digits<0, 0, ExtendedCache>(significand, exp2_base, k, uconst0, uconst0))) 
                        {
                            goto round_up;
                        }

                        goto print_last_digits;
                    } // digits_in_the_second_segment <= 2

                    // When there are at most 9 digits in the segment.
                    if (digits_in_the_second_segment <= 9)
                    {
                        // Throw away all overlapping digits.
                        BOOST_CHARCONV_ASSERT(22 - digits_in_the_second_segment <= 19);

                        fixed_point_calculator<ExtendedCache::max_cache_blocks>::discard_upper(
                            compute_power(UINT64_C(10), 22 - digits_in_the_second_segment),
                            blocks, cache_block_count);

                        // Get one more bit for potential rounding on the segment boundary.
                        auto segment = fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                            generate_and_discard_lower(power_of_10[9] << 1, blocks,
                                                        cache_block_count);

                        std::uint64_t prod;
                        digits_in_the_second_segment -= remaining_digits;

                        if ((remaining_digits & 1) != 0)
                        {
                            prod = ((segment * UINT64_C(1441151881)) >> 26) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 1)
                            {
                                goto second_segment22_at_most_9_digits_rounding;
                            }

                            print_1_digit(current_digits, buffer);
                            ++buffer;
                        }
                        else 
                        {
                            prod = ((segment * UINT64_C(1801439851)) >> 23) + 1;
                            const auto next_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 0)
                            {
                                if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                        current_digits,
                                        uint_with_known_number_of_digits<2>{next_digits}, [&] {
                                            return static_cast<std::uint32_t>(prod) >=
                                                        (additional_static_data_holder::
                                                            fractional_part_rounding_thresholds32[digits_in_the_second_segment - 1] & 0x7fffffff)
                                                    || has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0);
                                        })) 
                                {
                                    goto round_up_two_digits;
                                }
                                goto print_last_two_digits;
                            }
                            else if (remaining_digits == 2)
                            {
                                current_digits = next_digits;
                                goto second_segment22_at_most_9_digits_rounding;
                            }

                            print_2_digits(next_digits, buffer);
                            buffer += 2;
                        }

                        BOOST_CHARCONV_ASSERT(remaining_digits >= 3);

                        for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                        }

                        if (digits_in_the_second_segment != 0) 
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            current_digits = static_cast<std::uint32_t>(prod >> 32);
                            remaining_digits = 0;

                        second_segment22_at_most_9_digits_rounding:
                            if (check_rounding_condition_inside_subsegment(
                                    current_digits, static_cast<std::uint32_t>(prod),
                                    digits_in_the_second_segment, has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1,
                                    uconst0))) 
                            {
                                goto round_up;
                            }

                            goto print_last_digits;
                        }
                        else
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(200);
                            current_digits = static_cast<std::uint32_t>(prod >> 32);
                            const auto segment_boundary_rounding_bit = (current_digits & 1) != 0;
                            current_digits >>= 1;

                            if (check_rounding_condition_with_next_bit(
                                    current_digits, segment_boundary_rounding_bit,
                                    has_further_digits<0, 1, ExtendedCache>(significand, exp2_base, k, uconst0, uconst1)))
                            {
                                goto round_up_two_digits;
                            }
                            goto print_last_two_digits;
                        }
                    } // digits_in_the_second_segment <= 9

                    // first_second_subsegments is of 1 ~ 13 digits, and third_subsegment is
                    // of 9 digits.
                    // Get one more bit for potential rounding condition check.
                    auto first_second_subsegments =
                        fixed_point_calculator<ExtendedCache::max_cache_blocks>::generate(
                            power_of_10[13] << 1, blocks, cache_block_count);
                    
                    bool first_bit_of_third_subsegment = ((first_second_subsegments & 1) != 0);
                    first_second_subsegments >>= 1;

                    // Compilers are not aware of how to leverage the maximum value of
                    // first_second_subsegments to find out a better magic number which
                    // allows us to eliminate an additional shift.
                    // 1844674407371 = ceil(2^64/10^7) = floor(2^64*(10^6/(10^13 - 1))).
                    const auto first_subsegment =
                        static_cast<std::uint32_t>(boost::charconv::detail::umul128_upper64(
                            first_second_subsegments, 1844674407371));

                    const auto second_subsegment =
                        static_cast<std::uint32_t>(first_second_subsegments) - 10000000 * first_subsegment;

                    int digits_in_the_second_subsegment;

                    // Print the first subsegment (0 ~ 6 digits) if exists.
                    if (digits_in_the_second_segment > 16)
                    {
                        std::uint64_t prod;
                        int remaining_digits_in_the_current_subsegment = digits_in_the_second_segment - 16;

                        // No rounding, continue.
                        if (remaining_digits > remaining_digits_in_the_current_subsegment)
                        {
                            remaining_digits -= remaining_digits_in_the_current_subsegment;

                            // There is no overlap in the second subsegment.
                            digits_in_the_second_subsegment = 7;

                            // When there is no overlapping digit.
                            if (remaining_digits_in_the_current_subsegment == 6)
                            {
                                prod = (first_subsegment * UINT64_C(429497)) + 1;
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                                remaining_digits_in_the_current_subsegment -= 2;
                            }
                            // If there are overlapping digits, move all overlapping digits
                            // into the integer part.
                            else 
                            {
                                prod = ((first_subsegment * UINT64_C(687195)) >> 4) + 1;
                                prod *= compute_power(UINT64_C(10), 5 - remaining_digits_in_the_current_subsegment);

                                if ((remaining_digits_in_the_current_subsegment & 1) != 0) 
                                {
                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                                    print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                                    ++buffer;
                                }
                            }

                            for (; remaining_digits_in_the_current_subsegment > 1; remaining_digits_in_the_current_subsegment -= 2) 
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }
                        }
                        // The first subsegment is the last subsegment to print.
                        else
                        {
                            if ((remaining_digits & 1) != 0)
                            {
                                prod = ((first_subsegment * UINT64_C(687195)) >> 4) + 1;

                                // If there are overlapping digits, move all overlapping digits
                                // into the integer part and then get the next digit.
                                if (remaining_digits_in_the_current_subsegment < 6)
                                {
                                    prod *= compute_power(UINT64_C(10), 5 - remaining_digits_in_the_current_subsegment);
                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                                }
                                current_digits = static_cast<std::uint32_t>(prod >> 32);
                                remaining_digits_in_the_current_subsegment -= remaining_digits;

                                if (remaining_digits == 1) 
                                {
                                    goto second_segment22_more_than_9_digits_first_subsegment_rounding;
                                }

                                print_1_digit(current_digits, buffer);
                                ++buffer;
                            }
                            else 
                            {
                                // When there is no overlapping digit.
                                if (remaining_digits_in_the_current_subsegment == 6) 
                                {
                                    if (remaining_digits == 0)
                                    {
                                        if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                                current_digits,
                                                uint_with_known_number_of_digits<6>{
                                                    first_subsegment},
                                                has_further_digits<1, 16, ExtendedCache>(significand, exp2_base, k, uconst1, uconst16)))
                                        {
                                            goto round_up_two_digits;
                                        }
                                        goto print_last_two_digits;
                                    }

                                    prod = (first_subsegment * UINT64_C(429497)) + 1;
                                }
                                // Otherwise, convert the subsegment into a fixed-point
                                // fraction form, move all overlapping digits into the
                                // integer part, and then extract the next two digits.
                                else 
                                {
                                    prod = ((first_subsegment * UINT64_C(687195)) >> 4) + 1;
                                    prod *= compute_power(UINT64_C(10), 5 - remaining_digits_in_the_current_subsegment);

                                    if (remaining_digits == 0)
                                    {
                                        goto second_segment22_more_than_9_digits_first_subsegment_rounding_inside_subsegment;
                                    }

                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                }
                                current_digits = static_cast<std::uint32_t>(prod >> 32);
                                remaining_digits_in_the_current_subsegment -= remaining_digits;

                                if (remaining_digits == 2)
                                {
                                    goto second_segment22_more_than_9_digits_first_subsegment_rounding;
                                }

                                print_2_digits(current_digits, buffer);
                                buffer += 2;
                            }

                            BOOST_CHARCONV_ASSERT(remaining_digits >= 3);

                            if (remaining_digits > 4)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }

                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            current_digits = static_cast<std::uint32_t>(prod >> 32);
                            remaining_digits = 0;

                        second_segment22_more_than_9_digits_first_subsegment_rounding:
                            if (remaining_digits_in_the_current_subsegment == 0) 
                            {
                                if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                        current_digits,
                                        uint_with_known_number_of_digits<7>{second_subsegment},
                                        has_further_digits<1, 9, ExtendedCache>(significand, exp2_base, k, uconst1, uconst9))) 
                                {
                                    goto round_up;
                                }
                            }
                            else 
                            {
                            second_segment22_more_than_9_digits_first_subsegment_rounding_inside_subsegment
                                :
                                if (check_rounding_condition_inside_subsegment(
                                        current_digits, static_cast<std::uint32_t>(prod),
                                        remaining_digits_in_the_current_subsegment,
                                        has_further_digits<1, 16, ExtendedCache>(significand, exp2_base, k, uconst1, uconst16)))
                                {
                                    goto round_up;
                                }
                            }
                            goto print_last_digits;
                        }
                    }
                    else
                    {
                        digits_in_the_second_subsegment = digits_in_the_second_segment - 9;
                    }

                    // Print the second subsegment (1 ~ 7 digits).
                    {
                        // No rounding, continue.
                        if (remaining_digits > digits_in_the_second_subsegment) 
                        {
                            auto prod = ((second_subsegment * UINT64_C(17592187)) >> 12) + 1;
                            remaining_digits -= digits_in_the_second_subsegment;

                            // When there is no overlapping digit.
                            if (digits_in_the_second_subsegment == 7)
                            {
                                print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                                ++buffer;
                            }
                            // If there are overlapping digits, move all overlapping digits
                            // into the integer part.
                            else
                            {
                                prod *= compute_power(UINT64_C(10),
                                                        6 - digits_in_the_second_subsegment);

                                if ((digits_in_the_second_subsegment & 1) != 0)
                                {
                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                                    print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                                    ++buffer;
                                }
                            }

                            for (; digits_in_the_second_subsegment > 1; digits_in_the_second_subsegment -= 2)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }
                        }
                        // The second subsegment is the last subsegment to print.
                        else
                        {
                            std::uint64_t prod;

                            if ((remaining_digits & 1) != 0)
                            {
                                prod = ((second_subsegment * UINT64_C(17592187)) >> 12) + 1;

                                // If there are overlapping digits, move all overlapping digits
                                // into the integer part and then get the next digit.
                                if (digits_in_the_second_subsegment < 7) 
                                {
                                    prod *= compute_power(UINT64_C(10), 6 - digits_in_the_second_subsegment);
                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                                }
                                current_digits = static_cast<std::uint32_t>(prod >> 32);
                                digits_in_the_second_subsegment -= remaining_digits;

                                if (remaining_digits == 1)
                                {
                                    goto second_segment22_more_than_9_digits_second_subsegment_rounding;
                                }

                                print_1_digit(current_digits, buffer);
                                ++buffer;
                            }
                            else
                            {
                                // When there is no overlapping digit.
                                if (digits_in_the_second_subsegment == 7)
                                {
                                    if (remaining_digits == 0)
                                    {
                                        if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                                current_digits,
                                                uint_with_known_number_of_digits<7>{
                                                    second_subsegment},
                                                has_further_digits<1, 9, ExtendedCache>(significand, exp2_base, k, uconst1, uconst9)))
                                        {
                                            goto round_up_two_digits;
                                        }
                                        goto print_last_two_digits;
                                    }

                                    prod = ((second_subsegment * UINT64_C(10995117)) >> 8) + 1;
                                }
                                // Otherwise, convert the subsegment into a fixed-point
                                // fraction form, move all overlapping digits into the
                                // integer part, and then extract the next two digits.
                                else 
                                {
                                    prod = ((second_subsegment * UINT64_C(17592187)) >> 12) + 1;
                                    prod *= compute_power(UINT64_C(10), 6 - digits_in_the_second_subsegment);

                                    if (remaining_digits == 0)
                                    {
                                        goto second_segment22_more_than_9_digits_second_subsegment_rounding_inside_subsegment;
                                    }

                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                }
                                current_digits = static_cast<std::uint32_t>(prod >> 32);
                                digits_in_the_second_subsegment -= remaining_digits;

                                if (remaining_digits == 2)
                                {
                                    goto second_segment22_more_than_9_digits_second_subsegment_rounding;
                                }

                                print_2_digits(current_digits, buffer);
                                buffer += 2;
                            }

                            BOOST_CHARCONV_ASSERT(remaining_digits >= 3);

                            if (remaining_digits > 4)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }

                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            current_digits = static_cast<std::uint32_t>(prod >> 32);
                            remaining_digits = 0;

                        second_segment22_more_than_9_digits_second_subsegment_rounding:
                            if (digits_in_the_second_subsegment == 0)
                            {
                                if (check_rounding_condition_with_next_bit(
                                        current_digits, first_bit_of_third_subsegment,
                                        has_further_digits<0, 9, ExtendedCache>(significand, exp2_base, k, uconst0, uconst9)))
                                {
                                    goto round_up;
                                }
                            }
                            else
                            {
                            second_segment22_more_than_9_digits_second_subsegment_rounding_inside_subsegment
                                :
                                if (check_rounding_condition_inside_subsegment(
                                        current_digits, static_cast<std::uint32_t>(prod),
                                        digits_in_the_second_subsegment, has_further_digits<1, 9, ExtendedCache>(significand, exp2_base, k,
                                        uconst1, uconst9)))
                                {
                                    goto round_up;
                                }
                            }
                            goto print_last_digits;
                        }
                    }

                    // Print the third subsegment (9 digits).
                    {
                        // Get one more bit if we need to check rounding conditions on
                        // the segment boundary. We already have shifted by 1-bit in the
                        // computation of first & second subsegments, so here we don't
                        // shift the multiplier.
                        auto third_subsegment =
                            fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                                generate_and_discard_lower(power_of_10[9], blocks,
                                                            cache_block_count);

                        bool segment_boundary_rounding_bit = ((third_subsegment & 1) != 0);
                        third_subsegment >>= 1;
                        third_subsegment += (first_bit_of_third_subsegment ? 500000000 : 0);

                        std::uint64_t prod;
                        if ((remaining_digits & 1) != 0)
                        {
                            prod = ((third_subsegment * UINT64_C(720575941)) >> 24) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 1) 
                            {
                                if (check_rounding_condition_inside_subsegment(
                                        current_digits, static_cast<std::uint32_t>(prod), 8,
                                        has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0))) 
                                {
                                    goto round_up_one_digit;
                                }
                                goto print_last_one_digit;
                            }

                            print_1_digit(current_digits, buffer);
                            ++buffer;
                        }
                        else 
                        {
                            prod = ((third_subsegment * UINT64_C(450359963)) >> 20) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 2) 
                            {
                                goto second_segment22_more_than_9_digits_third_subsegment_rounding;
                            }

                            print_2_digits(current_digits, buffer);
                            buffer += 2;
                        }

                        for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                        }

                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                        current_digits = static_cast<std::uint32_t>(prod >> 32);

                        if (remaining_digits < 9)
                        {
                        second_segment22_more_than_9_digits_third_subsegment_rounding:
                            if (check_rounding_condition_inside_subsegment(
                                    current_digits, static_cast<std::uint32_t>(prod), 9 - remaining_digits,
                                    has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0)))
                            {
                                goto round_up_two_digits;
                            }
                        }
                        else 
                        {
                            if (check_rounding_condition_with_next_bit(
                                    current_digits, segment_boundary_rounding_bit,
                                    has_further_digits<0, 0, ExtendedCache>(significand, exp2_base, k, uconst0, uconst0))) 
                            {
                                goto round_up_two_digits;
                            }
                        }
                        goto print_last_two_digits;
                    }
                }
            } // ExtendedCache::segment_length == 22

            else BOOST_CHARCONV_IF_CONSTEXPR (ExtendedCache::segment_length == 252)
            {
                int overlapping_digits = 252 - digits_in_the_second_segment;
                int remaining_subsegment_pairs = 14;

                while (overlapping_digits >= 18)
                {
                    fixed_point_calculator<ExtendedCache::max_cache_blocks>::discard_upper(
                        power_of_10[18], blocks, cache_block_count);
                    --remaining_subsegment_pairs;
                    overlapping_digits -= 18;
                }

                auto subsegment_pair = fixed_point_calculator<ExtendedCache::max_cache_blocks>::generate(power_of_10[18] << 1, blocks, cache_block_count);
                auto subsegment_boundary_rounding_bit = (subsegment_pair & 1) != 0;
                subsegment_pair >>= 1;

                // Deal with the first subsegment pair.
                {
                    // Divide it into two 9-digits subsegments.
                    const auto first_part = static_cast<std::uint32_t>(subsegment_pair / power_of_10[9]);
                    const auto second_part = static_cast<std::uint32_t>(subsegment_pair - power_of_10[9] * first_part);

                    auto print_subsegment = [&](std::uint32_t subsegment, int digits_in_the_subsegment)
                    {
                        remaining_digits -= digits_in_the_subsegment;

                        // Move all overlapping digits into the integer part.
                        auto prod = ((subsegment * UINT64_C(720575941)) >> 24) + 1;
                        if (digits_in_the_subsegment < 9) 
                        {
                            prod *= compute_power(UINT32_C(10), 8 - digits_in_the_subsegment);

                            if ((digits_in_the_subsegment & 1) != 0) 
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                                print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                                ++buffer;
                            }
                        }
                        else 
                        {
                            print_1_digit(static_cast<std::uint32_t>(prod >> 32), buffer);
                            ++buffer;
                        }

                        for (; digits_in_the_subsegment > 1; digits_in_the_subsegment -= 2) 
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                        }
                    };

                    // When the first part is not completely overlapping with the first segment.
                    int digits_in_the_second_part;
                    if (overlapping_digits < 9) 
                    {
                        int digits_in_the_first_part = 9 - overlapping_digits;

                        // No rounding, continue.
                        if (remaining_digits > digits_in_the_first_part) 
                        {
                            digits_in_the_second_part = 9;
                            print_subsegment(first_part, digits_in_the_first_part);
                        }
                        // Perform rounding and return.
                        else 
                        {
                            // When there is no overlapping digit.
                            std::uint64_t prod;
                            if (digits_in_the_first_part == 9)
                            {
                                if ((remaining_digits & 1) != 0) 
                                {
                                    prod = ((first_part * UINT64_C(720575941)) >> 24) + 1;
                                }
                                else 
                                {
                                    if (remaining_digits == 0) 
                                    {
                                        if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                                current_digits,
                                                uint_with_known_number_of_digits<9>{first_part},
                                                compute_has_further_digits<1, 9, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                                        {
                                            goto round_up_two_digits;
                                        }
                                        goto print_last_two_digits;
                                    }

                                    prod = ((first_part * UINT64_C(450359963)) >> 20) + 1;
                                }
                            }
                            else 
                            {
                                prod = ((first_part * UINT64_C(720575941)) >> 24) + 1;
                                prod *= compute_power(UINT32_C(10), 8 - digits_in_the_first_part);

                                if ((remaining_digits & 1) != 0) 
                                {
                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                                }
                                else 
                                {
                                    if (remaining_digits == 0) 
                                    {
                                        goto second_segment252_first_subsegment_rounding_inside_subsegment;
                                    }

                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                }
                            }
                            digits_in_the_first_part -= remaining_digits;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits > 2) 
                            {
                                if ((remaining_digits & 1) != 0) 
                                {
                                    print_1_digit(current_digits, buffer);
                                    ++buffer;
                                }
                                else 
                                {
                                    print_2_digits(current_digits, buffer);
                                    buffer += 2;
                                }

                                for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                                {
                                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                    print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                    buffer += 2;
                                }

                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                current_digits = static_cast<std::uint32_t>(prod >> 32);
                                remaining_digits = 0;
                            }

                            if (digits_in_the_first_part != 0)
                            {
                            second_segment252_first_subsegment_rounding_inside_subsegment:
                                if (check_rounding_condition_inside_subsegment(
                                        current_digits, static_cast<std::uint32_t>(prod),
                                        digits_in_the_first_part, compute_has_further_digits<1, 9, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k)) 
                                {
                                    goto round_up;
                                }
                            }
                            else 
                            {
                                if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                        current_digits,
                                        uint_with_known_number_of_digits<9>{static_cast<std::uint32_t>(second_part)},
                                        compute_has_further_digits<1, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                                {
                                    goto round_up;
                                }
                            }
                            goto print_last_digits;
                        }
                    }
                    else
                    {
                        digits_in_the_second_part = 18 - overlapping_digits;
                    }

                    // Print the second part.
                    // No rounding, continue.
                    if (remaining_digits > digits_in_the_second_part)
                    {
                        print_subsegment(second_part, digits_in_the_second_part);
                    }
                    // Perform rounding and return.
                    else
                    {
                        // When there is no overlapping digit.
                        std::uint64_t prod;
                        if (digits_in_the_second_part == 9)
                        {
                            if ((remaining_digits & 1) != 0)
                            {
                                prod = ((second_part * UINT64_C(720575941)) >> 24) + 1;
                            }
                            else 
                            {
                                if (remaining_digits == 0)
                                {
                                    if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                            current_digits,
                                            uint_with_known_number_of_digits<9>{static_cast<std::uint32_t>(second_part)},
                                            compute_has_further_digits<1, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k)) 
                                    {
                                        goto round_up_two_digits;
                                    }
                                    goto print_last_two_digits;
                                }

                                prod = ((second_part * UINT64_C(450359963)) >> 20) + 1;
                            }
                        }
                        else 
                        {
                            prod = ((second_part * UINT64_C(720575941)) >> 24) + 1;
                            prod *= compute_power(UINT32_C(10), 8 - digits_in_the_second_part);

                            if ((remaining_digits & 1) != 0)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(10);
                            }
                            else 
                            {
                                if (remaining_digits == 0) 
                                {
                                    goto second_segment252_second_subsegment_rounding_inside_subsegment;
                                }

                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            }
                        }
                        digits_in_the_second_part -= remaining_digits;
                        current_digits = static_cast<std::uint32_t>(prod >> 32);

                        if (remaining_digits > 2)
                        {
                            if ((remaining_digits & 1) != 0) 
                            {
                                print_1_digit(current_digits, buffer);
                                ++buffer;
                            }
                            else 
                            {
                                print_2_digits(current_digits, buffer);
                                buffer += 2;
                            }

                            for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }

                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            current_digits = static_cast<std::uint32_t>(prod >> 32);
                            remaining_digits = 0;
                        }

                        if (digits_in_the_second_part != 0)
                        {
                        second_segment252_second_subsegment_rounding_inside_subsegment:
                            if (check_rounding_condition_inside_subsegment(
                                    current_digits, static_cast<std::uint32_t>(prod),
                                    digits_in_the_second_part, compute_has_further_digits<1, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                            {
                                goto round_up;
                            }
                        }
                        else 
                        {
                            if (check_rounding_condition_with_next_bit(
                                    current_digits, subsegment_boundary_rounding_bit,
                                    compute_has_further_digits<0, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                            {
                                goto round_up;
                            }
                        }
                        goto print_last_digits;
                    }
                }

                // Remaining subsegment pairs do not have overlapping digits.
                --remaining_subsegment_pairs;
                for (; remaining_subsegment_pairs > 0; --remaining_subsegment_pairs) 
                {
                    subsegment_pair = fixed_point_calculator<ExtendedCache::max_cache_blocks>::generate(power_of_10[18], blocks, cache_block_count);

                    subsegment_pair += (subsegment_boundary_rounding_bit ? power_of_10[18] : 0);
                    subsegment_boundary_rounding_bit = (subsegment_pair & 1) != 0;
                    subsegment_pair >>= 1;

                    const auto first_part = static_cast<std::uint32_t>(subsegment_pair / power_of_10[9]);
                    const auto second_part = static_cast<std::uint32_t>(subsegment_pair - power_of_10[9] * first_part);

                    // The first part can be printed without rounding.
                    if (remaining_digits > 9)
                    {
                        print_9_digits(first_part, buffer);

                        // The second part also can be printed without rounding.
                        if (remaining_digits > 18) 
                        {
                            print_9_digits(second_part, buffer + 9);
                        }
                        // Otherwise, perform rounding and return.
                        else
                        {
                            buffer += 9;
                            remaining_digits -= 9;

                            std::uint64_t prod;
                            int remaining_digits_in_the_current_subsegment = 9 - remaining_digits;

                            if ((remaining_digits & 1) != 0)
                            {
                                prod = ((second_part * UINT64_C(720575941)) >> 24) + 1;
                                current_digits = static_cast<std::uint32_t>(prod >> 32);

                                if (remaining_digits == 1)
                                {
                                    goto second_segment252_loop_second_subsegment_rounding;
                                }

                                print_1_digit(current_digits, buffer);
                                ++buffer;
                            }
                            else
                            {
                                prod = ((second_part * UINT64_C(450359963)) >> 20) + 1;
                                current_digits = static_cast<std::uint32_t>(prod >> 32);

                                if (remaining_digits == 2)
                                {
                                    goto second_segment252_loop_second_subsegment_rounding;
                                }

                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }

                            for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }

                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            current_digits = static_cast<std::uint32_t>(prod >> 32);
                            remaining_digits = 0;

                            if (remaining_digits_in_the_current_subsegment != 0) 
                            {
                            second_segment252_loop_second_subsegment_rounding:
                                if (check_rounding_condition_inside_subsegment(
                                        current_digits, static_cast<std::uint32_t>(prod),
                                        remaining_digits_in_the_current_subsegment,
                                        compute_has_further_digits<1, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                                {
                                    goto round_up;
                                }
                                goto print_last_digits;
                            }
                            else 
                            {
                                if (check_rounding_condition_with_next_bit(
                                        current_digits, subsegment_boundary_rounding_bit,
                                        compute_has_further_digits<0, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                                {
                                    goto round_up_two_digits;
                                }
                                goto print_last_two_digits;
                            }
                        }
                    }
                    // Otherwise, perform rounding and return.
                    else
                    {
                        std::uint64_t prod;
                        int remaining_digits_in_the_current_subsegment = 9 - remaining_digits;
                        if ((remaining_digits & 1) != 0) 
                        {
                            prod = ((first_part * UINT64_C(720575941)) >> 24) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 1) 
                            {
                                goto second_segment252_loop_first_subsegment_rounding;
                            }

                            print_1_digit(current_digits, buffer);
                            ++buffer;
                        }
                        else
                        {
                            prod = ((first_part * UINT64_C(450359963)) >> 20) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 2)
                            {
                                goto second_segment252_loop_first_subsegment_rounding;
                            }

                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                        }

                        for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                        }

                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                        current_digits = static_cast<std::uint32_t>(prod >> 32);
                        remaining_digits = 0;

                        if (remaining_digits_in_the_current_subsegment != 0)
                        {
                        second_segment252_loop_first_subsegment_rounding:
                            if (check_rounding_condition_inside_subsegment(
                                    current_digits, static_cast<std::uint32_t>(prod),
                                    remaining_digits_in_the_current_subsegment,
                                    compute_has_further_digits<1, 9, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                            {
                                goto round_up;
                            }
                            goto print_last_digits;
                        }
                        else
                        {
                            if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                    current_digits,
                                    uint_with_known_number_of_digits<9>{static_cast<std::uint32_t>(second_part)},
                                    compute_has_further_digits<1, 9, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                            {
                                goto round_up_two_digits;
                            }
                            goto print_last_two_digits;
                        }
                    }

                    buffer += 18;
                    remaining_digits -= 18;
                }
            } // ExtendedCache::segment_length == 252
        }

        // Print all remaining segments.
        while (has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0))
        {
            // Get new segment.
            ++multiplier_index;
            k += ExtendedCache::segment_length;

            cache_block_count = load_extended_cache<ExtendedCache, ExtendedCache::constant_block_count>(blocks, e, k, multiplier_index);

            // Compute nm mod 2^Q.
            fixed_point_calculator<ExtendedCache::max_cache_blocks>::discard_upper(significand, blocks, cache_block_count);

            BOOST_CHARCONV_IF_CONSTEXPR (ExtendedCache::segment_length == 22)
            {
                // When at least two subsegments left.
                if (remaining_digits > 16) 
                {
                    std::uint64_t first_second_subsegments = fixed_point_calculator<ExtendedCache::max_cache_blocks>::generate(power_of_10[16], blocks, cache_block_count);

                    const auto first_subsegment =
                        static_cast<std::uint32_t>(boost::charconv::detail::umul128_upper64(first_second_subsegments, UINT64_C(3022314549036573)) >> 14);
                    
                    const std::uint32_t second_subsegment = static_cast<std::uint32_t>(first_second_subsegments) - UINT32_C(100000000) * first_subsegment;

                    print_8_digits(first_subsegment, buffer);
                    print_8_digits(second_subsegment, buffer + 8);

                    // When more segments left.
                    if (remaining_digits > 22)
                    {
                        const auto third_subsegment = static_cast<std::uint32_t>(
                            fixed_point_calculator<ExtendedCache::max_cache_blocks>::generate_and_discard_lower(power_of_10[6], blocks,cache_block_count));

                        print_6_digits(third_subsegment, buffer + 16);
                        buffer += 22;
                        remaining_digits -= 22;
                    }
                    // When this is the last segment.
                    else
                    {
                        buffer += 16;
                        remaining_digits -= 16;

                        auto third_subsegment = fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                            generate_and_discard_lower(power_of_10[6] << 1, blocks, cache_block_count);

                        bool segment_boundary_rounding_bit = ((third_subsegment & 1) != 0);
                        third_subsegment >>= 1;

                        std::uint64_t prod;
                        if ((remaining_digits & 1) != 0)
                        {
                            prod = ((third_subsegment * UINT64_C(687195)) >> 4) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 1)
                            {
                                if (check_rounding_condition_inside_subsegment(
                                        current_digits, static_cast<std::uint32_t>(prod), 5,
                                        has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0))) 
                                {
                                    goto round_up_one_digit;
                                }
                                goto print_last_one_digit;
                            }

                            print_1_digit(current_digits, buffer);
                            ++buffer;
                        }
                        else
                        {
                            prod = (third_subsegment * UINT64_C(429497)) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 2)
                            {
                                goto segment_loop22_more_than_16_digits_rounding;
                            }

                            print_2_digits(current_digits, buffer);
                            buffer += 2;
                        }

                        if (remaining_digits > 4)
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;

                            if (remaining_digits == 6)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                current_digits = static_cast<std::uint32_t>(prod >> 32);

                                if (check_rounding_condition_with_next_bit(
                                        current_digits, segment_boundary_rounding_bit,
                                        has_further_digits<0, 0, ExtendedCache>(significand, exp2_base, k, uconst0, uconst0)))
                                {
                                    goto round_up_two_digits;
                                }
                                goto print_last_two_digits;
                            }
                        }

                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                        current_digits = static_cast<std::uint32_t>(prod >> 32);

                    segment_loop22_more_than_16_digits_rounding:
                        if (check_rounding_condition_inside_subsegment(
                                current_digits, static_cast<std::uint32_t>(prod), 6 - remaining_digits,
                                has_further_digits<1, 0, ExtendedCache>(significand, exp2_base, k, uconst1, uconst0)))
                        {
                            goto round_up_two_digits;
                        }
                        goto print_last_two_digits;
                    }
                }
                // When two subsegments left.
                else if (remaining_digits > 8)
                {
                    // Get one more bit for potential rounding conditions check.
                    auto first_second_subsegments =
                        fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                            generate_and_discard_lower(power_of_10[16] << 1, blocks, cache_block_count);

                    bool first_bit_of_third_subsegment = ((first_second_subsegments & 1) != 0);
                    first_second_subsegments >>= 1;

                    // 3022314549036573 = ceil(2^78/10^8) = floor(2^78*(10^8/(10^16 -
                    // 1))).
                    const auto first_subsegment =
                        static_cast<std::uint32_t>(boost::charconv::detail::umul128_upper64(first_second_subsegments, UINT64_C(3022314549036573)) >> 14);

                    const auto second_subsegment = static_cast<std::uint32_t>(first_second_subsegments) - UINT32_C(100000000) * first_subsegment;

                    print_8_digits(first_subsegment, buffer);
                    buffer += 8;
                    remaining_digits -= 8;

                    // Second subsegment (8 digits).
                    std::uint64_t prod;
                    if ((remaining_digits & 1) != 0)
                    {
                        prod = ((second_subsegment * UINT64_C(112589991)) >> 18) + 1;
                        current_digits = static_cast<std::uint32_t>(prod >> 32);

                        if (remaining_digits == 1)
                        {
                            if (check_rounding_condition_inside_subsegment(
                                    current_digits, static_cast<std::uint32_t>(prod), 7, has_further_digits<1, 6, ExtendedCache>(significand, exp2_base, k,
                                    uconst1, uconst6)))
                            {
                                goto round_up_one_digit;
                            }
                            goto print_last_one_digit;
                        }

                        print_1_digit(current_digits, buffer);
                        ++buffer;
                    }
                    else
                    {
                        prod = ((second_subsegment * UINT64_C(140737489)) >> 15) + 1;
                        current_digits = static_cast<std::uint32_t>(prod >> 32);

                        if (remaining_digits == 2)
                        {
                            goto segment_loop22_more_than_8_digits_rounding;
                        }

                        print_2_digits(current_digits, buffer);
                        buffer += 2;
                    }

                    for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                    {
                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                        print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                        buffer += 2;
                    }

                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    current_digits = static_cast<std::uint32_t>(prod >> 32);

                    if (remaining_digits < 8)
                    {
                    segment_loop22_more_than_8_digits_rounding:
                        if (check_rounding_condition_inside_subsegment(
                                current_digits, static_cast<std::uint32_t>(prod), 8 - remaining_digits,
                                has_further_digits<1, 6, ExtendedCache>(significand, exp2_base, k, uconst1, uconst6)))
                        {
                            goto round_up_two_digits;
                        }
                    }
                    else {
                        if (check_rounding_condition_with_next_bit(
                                current_digits, first_bit_of_third_subsegment,
                                has_further_digits<0, 6, ExtendedCache>(significand, exp2_base, k, uconst0, uconst6)))
                        {
                            goto round_up_two_digits;
                        }
                    }
                    goto print_last_two_digits;
                }
                // remaining_digits is at most 8.
                else
                {
                    // Get one more bit for potential rounding conditions check.
                    auto first_subsegment =
                        fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                            generate_and_discard_lower(power_of_10[8] << 1, blocks, cache_block_count);

                    bool first_bit_of_second_subsegment = ((first_subsegment & 1) != 0);
                    first_subsegment >>= 1;

                    std::uint64_t prod;
                    if ((remaining_digits & 1) != 0)
                    {
                        prod = ((first_subsegment * UINT64_C(112589991)) >> 18) + 1;
                        current_digits = static_cast<std::uint32_t>(prod >> 32);

                        if (remaining_digits == 1)
                        {
                            if (check_rounding_condition_inside_subsegment(
                                    current_digits, static_cast<std::uint32_t>(prod), 7, has_further_digits<1, 14, ExtendedCache>(significand, exp2_base, k,
                                    uconst1, uconst14)))
                            {
                                goto round_up_one_digit;
                            }
                            goto print_last_one_digit;
                        }

                        print_1_digit(current_digits, buffer);
                        ++buffer;
                    }
                    else
                    {
                        prod = ((first_subsegment * UINT64_C(140737489)) >> 15) + 1;
                        current_digits = static_cast<std::uint32_t>(prod >> 32);

                        if (remaining_digits == 2)
                        {
                            goto segment_loop22_at_most_8_digits_rounding;
                        }

                        print_2_digits(current_digits, buffer);
                        buffer += 2;
                    }

                    for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                    {
                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                        print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                        buffer += 2;
                    }

                    prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                    current_digits = static_cast<std::uint32_t>(prod >> 32);

                    if (remaining_digits < 8)
                    {
                    segment_loop22_at_most_8_digits_rounding:
                        if (check_rounding_condition_inside_subsegment(
                                current_digits, static_cast<std::uint32_t>(prod), 8 - remaining_digits,
                                has_further_digits<1, 14, ExtendedCache>(significand, exp2_base, k, uconst1, uconst14)))
                        {
                            goto round_up_two_digits;
                        }
                    }
                    else
                    {
                        if (check_rounding_condition_with_next_bit(
                                current_digits, first_bit_of_second_subsegment,
                                has_further_digits<0, 14, ExtendedCache>(significand, exp2_base, k, uconst0, uconst14)))
                        {
                            goto round_up_two_digits;
                        }
                    }
                    goto print_last_two_digits;
                }
            } // ExtendedCache::segment_length == 22
            else if (ExtendedCache::segment_length == 252)
            {
                // Print as many 18-digits subsegment pairs as possible.
                for (int remaining_subsegment_pairs = 14; remaining_subsegment_pairs > 0;
                        --remaining_subsegment_pairs)
                {
                    // No rounding, continue.
                    if (remaining_digits > 18)
                    {
                        const auto subsegment_pair =
                            fixed_point_calculator<ExtendedCache::max_cache_blocks>::generate(power_of_10[18], blocks, cache_block_count);

                        const auto first_part = static_cast<std::uint32_t>(subsegment_pair / power_of_10[9]);
                        const auto second_part = static_cast<std::uint32_t>(subsegment_pair - power_of_10[9] * first_part);

                        print_9_digits(first_part, buffer);
                        print_9_digits(second_part, buffer + 9);
                        buffer += 18;
                        remaining_digits -= 18;
                    }
                    // Final subsegment pair.
                    else
                    {
                        auto last_subsegment_pair =
                            fixed_point_calculator<ExtendedCache::max_cache_blocks>::
                                generate_and_discard_lower(power_of_10[18] << 1, blocks, cache_block_count);

                        const bool subsegment_boundary_rounding_bit = ((last_subsegment_pair & 1) != 0);
                        last_subsegment_pair >>= 1;

                        const auto first_part = static_cast<std::uint32_t>(last_subsegment_pair / power_of_10[9]);
                        const auto second_part = static_cast<std::uint32_t>(last_subsegment_pair) - power_of_10[9] * first_part;

                        if (remaining_digits <= 9)
                        {
                            std::uint64_t prod;

                            if ((remaining_digits & 1) != 0)
                            {
                                prod = ((first_part * UINT64_C(1441151881)) >> 25) + 1;
                                current_digits = static_cast<std::uint32_t>(prod >> 32);

                                if (remaining_digits == 1)
                                {
                                    if (check_rounding_condition_inside_subsegment(
                                            current_digits, static_cast<std::uint32_t>(prod), 8,
                                            compute_has_further_digits<1, 9, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                                    {
                                        goto round_up_one_digit;
                                    }
                                    goto print_last_one_digit;
                                }

                                print_1_digit(current_digits, buffer);
                                ++buffer;
                            }
                            else
                            {
                                prod = ((first_part * UINT64_C(450359963)) >> 20) + 1;
                                current_digits = static_cast<std::uint32_t>(prod >> 32);

                                if (remaining_digits == 2)
                                {
                                    goto segment_loop252_final18_first_part_rounding;
                                }

                                print_2_digits(current_digits, buffer);
                                buffer += 2;
                            }

                            for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                            {
                                prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                                print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                                buffer += 2;
                            }

                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits < 9)
                            {
                            segment_loop252_final18_first_part_rounding:
                                if (check_rounding_condition_inside_subsegment(
                                        current_digits, static_cast<std::uint32_t>(prod),
                                        9 - remaining_digits, compute_has_further_digits<1, 9, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                                {
                                    goto round_up_two_digits;
                                }
                            }
                            else
                            {
                                if (check_rounding_condition_subsegment_boundary_with_next_subsegment(
                                        current_digits,
                                        uint_with_known_number_of_digits<9>{static_cast<std::uint32_t>(second_part)},
                                        compute_has_further_digits<1, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                                {
                                    goto round_up_two_digits;
                                }
                            }
                            goto print_last_two_digits;
                        } // remaining_digits <= 9

                        print_9_digits(first_part, buffer);
                        buffer += 9;
                        remaining_digits -= 9;

                        std::uint64_t prod;

                        if ((remaining_digits & 1) != 0)
                        {
                            prod = ((second_part * UINT64_C(1441151881)) >> 25) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 1)
                            {
                                if (check_rounding_condition_inside_subsegment(
                                        current_digits, static_cast<std::uint32_t>(prod), 8,
                                        compute_has_further_digits<1, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                                {
                                    goto round_up_one_digit;
                                }
                                goto print_last_one_digit;
                            }

                            print_1_digit(current_digits, buffer);
                            ++buffer;
                        }
                        else
                        {
                            prod = ((second_part * UINT64_C(450359963)) >> 20) + 1;
                            current_digits = static_cast<std::uint32_t>(prod >> 32);

                            if (remaining_digits == 2)
                            {
                                goto segment_loop252_final18_second_part_rounding;
                            }

                            print_2_digits(current_digits, buffer);
                            buffer += 2;
                        }

                        for (int i = 0; i < (remaining_digits - 3) / 2; ++i)
                        {
                            prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                            print_2_digits(static_cast<std::uint32_t>(prod >> 32), buffer);
                            buffer += 2;
                        }

                        prod = static_cast<std::uint32_t>(prod) * UINT64_C(100);
                        current_digits = static_cast<std::uint32_t>(prod >> 32);

                        if (remaining_digits < 9)
                        {
                        segment_loop252_final18_second_part_rounding:
                            if (check_rounding_condition_inside_subsegment(
                                    current_digits, static_cast<std::uint32_t>(prod), 9 - remaining_digits,
                                    compute_has_further_digits<1, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                            {
                                goto round_up_two_digits;
                            }
                        }
                        else
                        {
                            if (check_rounding_condition_with_next_bit(
                                    current_digits, subsegment_boundary_rounding_bit,
                                    compute_has_further_digits<0, 0, ExtendedCache>, remaining_subsegment_pairs, significand, exp2_base, k))
                            {
                                goto round_up_two_digits;
                            }
                        }
                        goto print_last_two_digits;
                    }
                }
            } // if (ExtendedCache::segment_length == 252)
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    /// Phase 3 - Fill remaining digits with 0's, insert decimal dot, print exponent, and
    /// return.
    /////////////////////////////////////////////////////////////////////////////////////////////////

fill_remaining_digits_with_0s:
    // This is probably not needed for the general format, but currently I am not 100% sure.
    // (When fixed format is eventually choosed, we do not remove trailing zeros in the integer part.
    // I am not sure if those trailing zeros are guaranteed to be already printed or not.)
    std::memset(buffer, '0', static_cast<std::size_t>(remaining_digits));
    buffer += remaining_digits;

insert_decimal_dot:
    if (fmt == chars_format::general)
    {
        // Decide between fixed vs scientific.
        if (-4 <= decimal_exponent_normalized && decimal_exponent_normalized < precision)
        {
            // Fixed.
            if (decimal_exponent_normalized >= 0)
            {
                // Insert decimal dot.
                decimal_dot_pos = buffer_starting_pos + decimal_exponent_normalized + 1;
                std::memmove(buffer_starting_pos, buffer_starting_pos + 1,
                             static_cast<std::size_t>(decimal_dot_pos - buffer_starting_pos));
                *decimal_dot_pos = '.';
            }
            else
            {
                // Print leading zeros and insert decimal dot.
                int number_of_leading_zeros = -decimal_exponent_normalized - 1;
                std::memmove(buffer_starting_pos + number_of_leading_zeros + 2, buffer_starting_pos + 1,
                             static_cast<std::size_t>(buffer - buffer_starting_pos - 1));
                std::memcpy(buffer_starting_pos, "0.", 2);
                std::memset(buffer_starting_pos + 2, '0', static_cast<std::size_t>(number_of_leading_zeros));
                buffer += number_of_leading_zeros + 1;
            }
            // Don't print exponent.
            fmt = chars_format::fixed;
        }
        else
        {
            // Scientific.
            // Insert decimal dot.
            *buffer_starting_pos = *(buffer_starting_pos + 1);
            *(buffer_starting_pos + 1) = '.';
        }

        // Remove trailing zeros.
        while (true)
        {
            auto prev = buffer - 1;

            // Remove decimal dot as well if there is no fractional digits.
            if (*prev == '.')
            {
                buffer = prev;
                break;
            }
            else if (*prev != '0')
            {
                break;
            }
            buffer = prev;
        }
    }
    else if (decimal_dot_pos != buffer_starting_pos)
    {
        std::memmove(buffer_starting_pos, buffer_starting_pos + 1,
                     static_cast<std::size_t>(decimal_dot_pos - buffer_starting_pos));
        *decimal_dot_pos = '.';
    }

    if (fmt != chars_format::fixed)
    {
        if (decimal_exponent_normalized >= 0)
        {
            std::memcpy(buffer, "e+", 2); // NOLINT : Specifically not null-terminating
        }
        else
        {
            std::memcpy(buffer, "e-", 2); // NOLINT : Specifically not null-terminating
            decimal_exponent_normalized = -decimal_exponent_normalized;
        }

        buffer += 2;
        if (decimal_exponent_normalized >= 100)
        {
            // d1 = decimal_exponent / 10; d2 = decimal_exponent % 10;
            // 6554 = ceil(2^16 / 10)
            auto prod = static_cast<std::uint32_t>(decimal_exponent_normalized) * UINT32_C(6554);
            auto d1 = prod >> 16;
            prod = static_cast<std::uint16_t>(prod) * UINT16_C(5); // * 10
            auto d2 = prod >> 15;                                  // >> 16
            print_2_digits(d1, buffer);
            print_1_digit(d2, buffer + 2);
            buffer += 3;
        }
        else
        {
            print_2_digits(static_cast<std::uint32_t>(decimal_exponent_normalized), buffer);
            buffer += 2;
        }
    }    

    return {buffer, std::errc()};

round_up:
    if ((remaining_digits & 1) != 0)
    {
    round_up_one_digit:
        if (++current_digits == 10)
        {
            goto round_up_all_9s;
        }

        goto print_last_one_digit;
    }
    else
    {
    round_up_two_digits:
        if (++current_digits == 100)
        {
            goto round_up_all_9s;
        }

        goto print_last_two_digits;
    }

print_last_digits:
    if ((remaining_digits & 1) != 0) 
    {
    print_last_one_digit:
        print_1_digit(current_digits, buffer);
        ++buffer;
    }
    else
    {
    print_last_two_digits:
        print_2_digits(current_digits, buffer);
        buffer += 2;
    }

    goto insert_decimal_dot;

round_up_all_9s:
    char* first_9_pos = buffer;
    buffer += (2 - (remaining_digits & 1));
    
    // Find the starting position of printed digits.
    char* digit_starting_pos = [&] {
        // For negative exponent & fixed format, we already printed leading zeros.
        if (fmt == chars_format::fixed && decimal_exponent_normalized < 0)
        {
            return buffer_starting_pos - decimal_exponent_normalized + 1;
        }
        // We reserved one slot for decimal dot, so the starting position of printed digits
        // is buffer_starting_pos + 1 if we need to print decimal dot.
        return buffer_starting_pos == decimal_dot_pos ? buffer_starting_pos
            : buffer_starting_pos + 1;
    }();
    // Find all preceding 9's.
    if ((first_9_pos - digit_starting_pos) % 2 != 0)
    {
        if (*(first_9_pos - 1) != '9')
        {
            ++*(first_9_pos - 1);
            if ((remaining_digits & 1) != 0)
            {
                *first_9_pos = '0';
            }
            else
            {
                std::memcpy(first_9_pos, "00", 2);
            }
            goto insert_decimal_dot;
        }
        --first_9_pos;
    }
    while (first_9_pos != digit_starting_pos)
    {
        if (std::memcmp(first_9_pos - 2, "99", 2) != 0)
        {
            if (*(first_9_pos - 1) != '9')
            {
                ++*(first_9_pos - 1);
            }
            else
            {
                ++*(first_9_pos - 2);
                *(first_9_pos - 1) = '0';
            }
            std::memset(first_9_pos, '0', static_cast<std::size_t>(buffer - first_9_pos));
            goto insert_decimal_dot;
        }
        first_9_pos -= 2;
    }

    // Every digit we wrote so far are all 9's. In this case, we have to shift the whole thing by 1.
    ++decimal_exponent_normalized;

    if (fmt == chars_format::fixed)
    {
        if (decimal_exponent_normalized > 0)
        {
            // We need to print one more character.
            if (buffer == last)
            {
                return {last, std::errc::value_too_large};
            }
            ++buffer;
            // If we were to print the decimal dot, we have to shift it to right
            // since we now have one more digit in the integer part.
            if (buffer_starting_pos != decimal_dot_pos)
            {
                ++decimal_dot_pos;
            }
        }
        else if (decimal_exponent_normalized == 0)
        {
            // For the case 0.99...9 -> 1.00...0, the rounded digit is one before the first digit written.
            // Note: decimal_exponent_normalized was negative before the increment (++decimal_exponent_normalized),
            //       so we already have printed "00" onto the buffer.
            //       Hence, --digit_starting_pos doesn't go more than the starting position of the buffer.
            --digit_starting_pos;
        }
    }

    // Nolint is applied to the following two calls since we know they are not supposed to be null terminated
    *digit_starting_pos = '1';
    std::memset(digit_starting_pos + 1, '0', static_cast<std::size_t>(buffer - digit_starting_pos - 1)); // NOLINT

    goto insert_decimal_dot;
}

}}} // Namespaces 

#ifdef BOOST_MSVC
# pragma warning(pop)
#endif

#endif // BOOST_CHARCONV_DETAIL_FLOFF
