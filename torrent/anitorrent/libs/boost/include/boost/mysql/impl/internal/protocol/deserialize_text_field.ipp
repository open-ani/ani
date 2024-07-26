//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_DESERIALIZE_TEXT_FIELD_IPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_DESERIALIZE_TEXT_FIELD_IPP

#pragma once

#include <boost/mysql/blob_view.hpp>
#include <boost/mysql/datetime.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/datetime.hpp>

#include <boost/mysql/impl/internal/protocol/bit_deserialization.hpp>
#include <boost/mysql/impl/internal/protocol/constants.hpp>
#include <boost/mysql/impl/internal/protocol/deserialize_text_field.hpp>
#include <boost/mysql/impl/internal/protocol/serialization.hpp>

#include <boost/assert.hpp>
#include <boost/charconv/from_chars.hpp>

#include <cmath>
#include <cstddef>
#include <cstdlib>
#include <cstring>
#include <system_error>

namespace boost {
namespace mysql {
namespace detail {

// Constants
BOOST_MYSQL_STATIC_IF_COMPILED constexpr unsigned max_decimals = 6u;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr unsigned time_max_hour = 838;

// Integers
template <class T>
BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_text_value_int_impl(string_view from, field_view& to) noexcept
{
    // Iterators
    const char* begin = from.data();
    const char* end = begin + from.size();

    // Convert
    T v;
    auto res = charconv::from_chars(from.data(), from.data() + from.size(), v);

    // Check
    if (res.ec != std::errc() || res.ptr != end)
        return deserialize_errc::protocol_value_error;

    // Done
    to = field_view(v);
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_text_value_int(string_view from, field_view& to, const metadata& meta) noexcept
{
    return meta.is_unsigned() ? deserialize_text_value_int_impl<std::uint64_t>(from, to)
                              : deserialize_text_value_int_impl<std::int64_t>(from, to);
}

// Floating points
template <class T>
BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_text_value_float(string_view from, field_view& to) noexcept
{
    // Iterators
    const char* begin = from.data();
    const char* end = begin + from.size();

    // Convert
    T val;
    auto res = charconv::from_chars(begin, end, val);

    // Check. SQL std forbids nan and inf
    if (res.ec != std::errc() || res.ptr != end || std::isnan(val) || std::isinf(val))
        return deserialize_errc::protocol_value_error;

    // Done
    to = field_view(val);
    return deserialize_errc::ok;
}

// Strings
BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_text_value_string(string_view from, field_view& to) noexcept
{
    to = field_view(from);
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_text_value_blob(string_view from, field_view& to) noexcept
{
    to = field_view(to_span(from));
    return deserialize_errc::ok;
}

// Date/time types
template <class IntType>
BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_fixed_width_number(const char*& it, const char* end, IntType& to, std::size_t size) noexcept
{
    auto res = charconv::from_chars(it, end, to);
    if (res.ec != std::errc() || res.ptr != it + size)
        return deserialize_errc::protocol_value_error;
    it = res.ptr;
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
check_separator(const char*& it, const char* end, char sep) noexcept
{
    if (it == end || *it++ != sep)
        return deserialize_errc::protocol_value_error;
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc deserialize_text_ymd(const char*& it, const char* end, date& to)
{
    // Year
    std::uint16_t year = 0;
    auto err = deserialize_fixed_width_number(it, end, year, 4);
    if (err != deserialize_errc::ok)
        return err;

    // Separator
    err = check_separator(it, end, '-');
    if (err != deserialize_errc::ok)
        return err;

    // Month
    std::uint8_t month = 0;
    err = deserialize_fixed_width_number(it, end, month, 2);
    if (err != deserialize_errc::ok)
        return err;

    // Separator
    err = check_separator(it, end, '-');
    if (err != deserialize_errc::ok)
        return err;

    // Day
    std::uint8_t day = 0;
    err = deserialize_fixed_width_number(it, end, day, 2);
    if (err != deserialize_errc::ok)
        return err;

    // Range check for individual components. MySQL doesn't allow invidiual components
    // to be out of range, although they may be zero or representing an invalid date
    if (year > max_year || month > max_month || day > max_day)
        return deserialize_errc::protocol_value_error;

    to = date(year, month, day);
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_microsecond(const char*& it, const char* end, std::uint32_t& output, unsigned decimals) noexcept
{
    // Sanitize decimals
    decimals = (std::min)(decimals, max_decimals);

    if (decimals)
    {
        // Microsecond separator
        auto err = check_separator(it, end, '.');
        if (err != deserialize_errc::ok)
            return err;

        // Microseconds. Depending on decimals, this has a variable width.
        // For instance, with decimals = 2, a value could be '.92', meaning 920000 microseconds.
        // Max decimals is 6 (guaranteed by sanitize_decimals).
        // Right pad the input string with zeros and convert

        // Size check
        if (it + decimals > end)
            return deserialize_errc::protocol_value_error;

        // Right pad with zeros
        char micros_buff[6] = {'0', '0', '0', '0', '0', '0'};
        std::memcpy(micros_buff, it, decimals);

        // Parse
        const char* buff_begin = micros_buff;
        const char* buff_end = micros_buff + sizeof(micros_buff);
        err = deserialize_fixed_width_number(buff_begin, buff_end, output, 6);
        if (err != deserialize_errc::ok)
            return err;

        // Update iterator
        it += decimals;
    }

    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_text_value_date(string_view from, field_view& to) noexcept
{
    // Iterators
    const char* it = from.data();
    const char* end = it + from.size();

    // Deserialize
    date d;
    auto err = deserialize_text_ymd(it, end, d);
    if (err != deserialize_errc::ok)
        return err;

    // Size check
    if (it != end)
        return deserialize_errc::protocol_value_error;

    // Done
    to = field_view(d);
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_text_value_datetime(string_view from, field_view& to, const metadata& meta) noexcept
{
    // Iterators
    const char* it = from.data();
    const char* end = it + from.size();

    // Deserialize date part
    date d;
    auto err = deserialize_text_ymd(it, end, d);
    if (err != deserialize_errc::ok)
        return err;

    // Separator
    err = check_separator(it, end, ' ');
    if (err != deserialize_errc::ok)
        return err;

    // Hour
    std::uint8_t hour = 0;
    err = deserialize_fixed_width_number(it, end, hour, 2);
    if (err != deserialize_errc::ok)
        return err;

    // Separator
    err = check_separator(it, end, ':');
    if (err != deserialize_errc::ok)
        return err;

    // Minute
    std::uint8_t minute = 0;
    err = deserialize_fixed_width_number(it, end, minute, 2);
    if (err != deserialize_errc::ok)
        return err;

    // Separator
    err = check_separator(it, end, ':');
    if (err != deserialize_errc::ok)
        return err;

    // Second
    std::uint8_t second = 0;
    err = deserialize_fixed_width_number(it, end, second, 2);
    if (err != deserialize_errc::ok)
        return err;

    // Microsecond
    std::uint32_t microsecond = 0;
    err = deserialize_microsecond(it, end, microsecond, meta.decimals());
    if (err != deserialize_errc::ok)
        return err;

    // Size check
    if (it != end)
        return deserialize_errc::protocol_value_error;

    // Validity check. Although MySQL allows invalid and zero datetimes, it doesn't allow
    // individual components to be out of range.
    if (hour > max_hour || minute > max_min || second > max_sec || microsecond > max_micro)
    {
        return deserialize_errc::protocol_value_error;
    }

    to = field_view(datetime(d.year(), d.month(), d.day(), hour, minute, second, microsecond));
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_text_value_time(string_view from, field_view& to, const metadata& meta) noexcept
{
    // Iterators
    const char* it = from.data();
    const char* end = it + from.size();

    // Sign
    if (it == end)
        return deserialize_errc::protocol_value_error;
    bool is_negative = *it == '-';
    if (is_negative)
        ++it;

    // Hours
    std::uint16_t hours = 0;
    auto res = charconv::from_chars(it, end, hours);
    if (res.ec != std::errc())
        return deserialize_errc::protocol_value_error;
    auto hour_num_chars = res.ptr - it;
    if (hour_num_chars != 2 && hour_num_chars != 3)  // may take between 2 and 3 chars
        return deserialize_errc::protocol_value_error;
    it = res.ptr;

    // Separator
    auto err = check_separator(it, end, ':');
    if (err != deserialize_errc::ok)
        return err;

    // Minute
    std::uint8_t minute = 0;
    err = deserialize_fixed_width_number(it, end, minute, 2);
    if (err != deserialize_errc::ok)
        return err;

    // Separator
    err = check_separator(it, end, ':');
    if (err != deserialize_errc::ok)
        return err;

    // Second
    std::uint8_t second = 0;
    err = deserialize_fixed_width_number(it, end, second, 2);
    if (err != deserialize_errc::ok)
        return err;

    // Microsecond
    std::uint32_t microsecond = 0;
    err = deserialize_microsecond(it, end, microsecond, meta.decimals());
    if (err != deserialize_errc::ok)
        return err;

    // Size check
    if (it != end)
        return deserialize_errc::protocol_value_error;

    // Range check
    if (hours > time_max_hour || minute > max_min || second > max_sec || microsecond > max_micro)
        return deserialize_errc::protocol_value_error;

    // Sum it
    auto t = std::chrono::hours(hours) + std::chrono::minutes(minute) + std::chrono::seconds(second) +
             std::chrono::microseconds(microsecond);
    if (is_negative)
    {
        t = -t;
    }

    // Done
    to = field_view(t);
    return deserialize_errc::ok;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

boost::mysql::detail::deserialize_errc boost::mysql::detail::deserialize_text_field(
    string_view from,
    const metadata& meta,
    field_view& output
)
{
    switch (meta.type())
    {
    case column_type::tinyint:
    case column_type::smallint:
    case column_type::mediumint:
    case column_type::int_:
    case column_type::bigint:
    case column_type::year: return deserialize_text_value_int(from, output, meta);
    case column_type::bit: return deserialize_bit(from, output);
    case column_type::float_: return deserialize_text_value_float<float>(from, output);
    case column_type::double_: return deserialize_text_value_float<double>(from, output);
    case column_type::timestamp:
    case column_type::datetime: return deserialize_text_value_datetime(from, output, meta);
    case column_type::date: return deserialize_text_value_date(from, output);
    case column_type::time: return deserialize_text_value_time(from, output, meta);
    // True string types
    case column_type::char_:
    case column_type::varchar:
    case column_type::text:
    case column_type::enum_:
    case column_type::set:
    case column_type::decimal:
    case column_type::json: return deserialize_text_value_string(from, output);
    // Blobs and anything else
    case column_type::binary:
    case column_type::varbinary:
    case column_type::blob:
    case column_type::geometry:
    default: return deserialize_text_value_blob(from, output);
    }
}

#endif
