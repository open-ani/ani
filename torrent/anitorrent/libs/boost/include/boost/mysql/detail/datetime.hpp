//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_DATETIME_HPP
#define BOOST_MYSQL_DETAIL_DATETIME_HPP

// All these algorithms have been taken from:
// http://howardhinnant.github.io/date_algorithms.html

#include <boost/assert.hpp>
#include <boost/config.hpp>

#include <cstdint>
#include <limits>

namespace boost {
namespace mysql {
namespace detail {

// Helpers
constexpr unsigned char last_month_day_arr[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

constexpr bool is_leap(std::uint16_t y) noexcept { return y % 4 == 0 && (y % 100 != 0 || y % 400 == 0); }

constexpr inline std::uint8_t last_month_day(std::uint16_t y, std::uint8_t m) noexcept
{
    return m != 2 || !is_leap(y) ? last_month_day_arr[m - 1] : 29u;
}

// Interface
constexpr std::uint16_t max_year = 9999;
constexpr std::uint8_t max_month = 12;
constexpr std::uint8_t max_day = 31;
constexpr std::uint8_t max_hour = 23;
constexpr std::uint8_t max_min = 59;
constexpr std::uint8_t max_sec = 59;
constexpr std::uint32_t max_micro = 999999;

constexpr inline bool is_valid(std::uint16_t years, std::uint8_t month, std::uint8_t day) noexcept
{
    return years <= max_year && month > 0 && month <= max_month && day > 0 &&
           day <= last_month_day(years, month);
}

BOOST_CXX14_CONSTEXPR inline int ymd_to_days(
    std::uint16_t years,
    std::uint8_t month,
    std::uint8_t day
) noexcept
{
    BOOST_ASSERT(is_valid(years, month, day));
    int y = years;
    const int m = month;
    const int d = day;
    y -= m <= 2;
    const int era = (y >= 0 ? y : y - 399) / 400;
    const unsigned yoe = static_cast<unsigned>(y - era * 400);            // [0, 399]
    const unsigned doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;  // [0, 365]
    const unsigned doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;           // [0, 146096]
    return era * 146097 + static_cast<int>(doe) - 719468;
}

BOOST_CXX14_CONSTEXPR inline bool days_to_ymd(
    int num_days,
    std::uint16_t& years,
    std::uint8_t& month,
    std::uint8_t& day
) noexcept
{
    // Prevent overflow
    constexpr int days_magic = 719468;
    if (num_days > (std::numeric_limits<int>::max)() - days_magic)
        return false;

    num_days += days_magic;
    const int era = (num_days >= 0 ? num_days : num_days - 146096) / 146097;
    const unsigned doe = static_cast<unsigned>(num_days - era * 146097);         // [0, 146096]
    const unsigned yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365;  // [0, 399]
    const int y = static_cast<int>(yoe) + era * 400;
    const unsigned doy = doe - (365 * yoe + yoe / 4 - yoe / 100);  // [0, 365]
    const unsigned mp = (5 * doy + 2) / 153;                       // [0, 11]
    const unsigned d = doy - (153 * mp + 2) / 5 + 1;               // [1, 31]
    const unsigned m = mp + (mp < 10 ? 3 : -9);                    // [1, 12]
    const int final_year = y + (m <= 2);
    if (final_year < 0 || final_year > static_cast<int>(max_year))
        return false;
    else
    {
        years = static_cast<std::uint16_t>(final_year);
        month = static_cast<std::uint8_t>(m);
        day = static_cast<std::uint8_t>(d);
        return true;
    }
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
