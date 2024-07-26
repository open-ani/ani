//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_DT_TO_STRING_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_DT_TO_STRING_HPP

#include <boost/mysql/time.hpp>

#include <boost/assert.hpp>
#include <boost/charconv/to_chars.hpp>
#include <boost/core/span.hpp>

#include <cstddef>
#include <cstdint>
#include <cstdlib>
#include <system_error>
#include <type_traits>

// gcc-11+ issues spurious warnings about to_chars
#if BOOST_GCC >= 70000
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wstringop-overflow"
#endif

namespace boost {
namespace mysql {
namespace detail {

// Helpers
template <class IntType>
inline char* call_to_chars(char* begin, char* end, IntType value) noexcept
{
    auto r = charconv::to_chars(begin, end, value);
    BOOST_ASSERT(r.ec == std::errc());
    return r.ptr;
}

template <class UnsignedInt>
inline char* write_pad2(char* begin, char* end, UnsignedInt value) noexcept
{
    static_assert(std::is_unsigned<UnsignedInt>::value, "");
    if (value < 10u)
        *begin++ = '0';
    return call_to_chars(begin, end, value);
}

inline char* write_pad4(char* begin, char* end, unsigned long value) noexcept
{
    for (auto l : {1000u, 100u, 10u})
    {
        if (value < l)
            *begin++ = '0';
    }
    return call_to_chars(begin, end, value);
}

inline char* write_pad6(char* begin, char* end, unsigned long value) noexcept
{
    for (auto l : {100000u, 10000u, 1000u, 100u, 10u})
    {
        if (value < l)
            *begin++ = '0';
    }
    return call_to_chars(begin, end, value);
}

inline std::size_t date_to_string(
    std::uint16_t year,
    std::uint8_t month,
    std::uint8_t day,
    span<char, 32> output
) noexcept
{
    // Worst-case output is 14 chars, extra space just in case

    // Iterators
    char* it = output.data();
    char* end = it + output.size();

    // Year
    it = write_pad4(it, end, year);

    // Month
    *it++ = '-';
    it = write_pad2(it, end, static_cast<unsigned long>(month));

    // Day
    *it++ = '-';
    it = write_pad2(it, end, static_cast<unsigned long>(day));

    // Done
    return it - output.data();
}

inline std::size_t datetime_to_string(
    std::uint16_t year,
    std::uint8_t month,
    std::uint8_t day,
    std::uint8_t hour,
    std::uint8_t minute,
    std::uint8_t second,
    std::uint32_t microsecond,
    span<char, 64> output
) noexcept
{
    // Worst-case output is 37 chars, extra space just in case

    // Iterators
    char* it = output.data();
    char* end = it + output.size();

    // Date
    it += date_to_string(year, month, day, span<char, 32>(it, 32));

    // Hour
    *it++ = ' ';
    it = write_pad2(it, end, static_cast<unsigned long>(hour));

    // Minutes
    *it++ = ':';
    it = write_pad2(it, end, static_cast<unsigned long>(minute));

    // Seconds
    *it++ = ':';
    it = write_pad2(it, end, static_cast<unsigned long>(second));

    // Microseconds
    *it++ = '.';
    it = write_pad6(it, end, microsecond);

    // Done
    return it - output.data();
}

inline std::size_t time_to_string(::boost::mysql::time value, span<char, 64> output) noexcept
{
    // Worst-case output is 34 chars, extra space just in case

    // Values. Note that std::abs(time::min()) invokes UB because of
    // signed integer overflow
    constexpr auto min_val = (::boost::mysql::time::min)();
    using unsigned_t = typename std::make_unsigned<typename ::boost::mysql::time::rep>::type;
    auto total_count = value == min_val ? static_cast<unsigned_t>(min_val.count())
                                        : static_cast<unsigned_t>(std::abs(value.count()));

    auto num_micros = total_count % 1000000u;
    total_count /= 1000000u;

    auto num_secs = total_count % 60u;
    total_count /= 60u;

    auto num_mins = total_count % 60u;
    total_count /= 60u;

    auto num_hours = total_count;

    // Iterators
    char* it = output.data();
    char* end = it + output.size();

    // Sign
    if (value.count() < 0)
        *it++ = '-';

    // Hours
    it = write_pad2(it, end, num_hours);  // type is unspecified

    // Minutes
    *it++ = ':';
    it = write_pad2(it, end, static_cast<unsigned long>(num_mins));

    // Seconds
    *it++ = ':';
    it = write_pad2(it, end, static_cast<unsigned long>(num_secs));

    // Microseconds
    *it++ = '.';
    it = write_pad6(it, end, static_cast<unsigned long>(num_micros));

    // Done
    return it - output.data();
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#if BOOST_GCC >= 110000
#pragma GCC diagnostic pop
#endif

#endif
