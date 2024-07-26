//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_DESERIALIZE_BINARY_FIELD_IPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_DESERIALIZE_BINARY_FIELD_IPP

#pragma once

#include <boost/mysql/field_kind.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/datetime.hpp>

#include <boost/mysql/impl/internal/protocol/bit_deserialization.hpp>
#include <boost/mysql/impl/internal/protocol/constants.hpp>
#include <boost/mysql/impl/internal/protocol/deserialize_binary_field.hpp>
#include <boost/mysql/impl/internal/protocol/serialization.hpp>

#include <cmath>
#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

// strings
BOOST_MYSQL_STATIC_OR_INLINE
deserialize_errc deserialize_binary_field_string(
    deserialization_context& ctx,
    field_view& output,
    bool is_blob
) noexcept
{
    string_lenenc deser;
    auto err = deserialize(ctx, deser);
    if (err != deserialize_errc::ok)
        return err;
    if (is_blob)
    {
        output = field_view(
            blob_view(reinterpret_cast<const unsigned char*>(deser.value.data()), deser.value.size())
        );
    }
    else
    {
        output = field_view(deser.value);
    }
    return deserialize_errc::ok;
}

// ints
template <class TargetType, class DeserializableType>
BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_binary_field_int_impl(deserialization_context& ctx, field_view& output) noexcept
{
    DeserializableType deser;
    auto err = deserialize(ctx, deser);
    if (err != deserialize_errc::ok)
        return err;
    output = field_view(static_cast<TargetType>(deser));
    return deserialize_errc::ok;
}

template <class DeserializableTypeUnsigned, class DeserializableTypeSigned>
BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_binary_field_int(const metadata& meta, deserialization_context& ctx, field_view& output) noexcept
{
    return meta.is_unsigned()
               ? deserialize_binary_field_int_impl<std::uint64_t, DeserializableTypeUnsigned>(ctx, output)
               : deserialize_binary_field_int_impl<std::int64_t, DeserializableTypeSigned>(ctx, output);
}

// Bits. These come as a binary value between 1 and 8 bytes,
// packed in a string
BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_binary_field_bit(deserialization_context& ctx, field_view& output) noexcept
{
    string_lenenc buffer;
    auto err = deserialize(ctx, buffer);
    if (err != deserialize_errc::ok)
        return err;
    return boost::mysql::detail::deserialize_bit(buffer.value, output);
}

// Floats
template <class T>
BOOST_MYSQL_STATIC_OR_INLINE deserialize_errc
deserialize_binary_field_float(deserialization_context& ctx, field_view& output) noexcept
{
    // Size check
    if (!ctx.enough_size(sizeof(T)))
        return deserialize_errc::incomplete_message;

    // Endianness conversion. Boost.Endian support for floats start at 1.71
    T v = boost::endian::endian_load<T, sizeof(T), boost::endian::order::little>(ctx.first());

    // Nans and infs not allowed in SQL
    if (std::isnan(v) || std::isinf(v))
        return deserialize_errc::protocol_value_error;

    // Done
    ctx.advance(sizeof(T));
    output = field_view(v);
    return deserialize_errc::ok;
}

// Time types
BOOST_MYSQL_STATIC_OR_INLINE
deserialize_errc deserialize_binary_ymd(deserialization_context& ctx, boost::mysql::date& output)
{
    using namespace boost::mysql::detail;

    std::uint16_t year;
    std::uint8_t month;
    std::uint8_t day;

    // Deserialize
    auto err = deserialize(ctx, year, month, day);
    if (err != deserialize_errc::ok)
        return err;

    // Range check
    if (year > max_year || month > max_month || day > max_day)
    {
        return deserialize_errc::protocol_value_error;
    }

    output = boost::mysql::date(year, month, day);

    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE
deserialize_errc deserialize_binary_field_date(deserialization_context& ctx, field_view& output) noexcept
{
    using namespace boost::mysql::detail::binc;

    // Deserialize length
    std::uint8_t length;
    auto err = deserialize(ctx, length);
    if (err != deserialize_errc::ok)
        return err;

    // Check for zero dates
    if (length < date_sz)
    {
        output = field_view(boost::mysql::date());
        return deserialize_errc::ok;
    }

    // Deserialize rest of fields
    boost::mysql::date d;
    err = deserialize_binary_ymd(ctx, d);
    if (err != deserialize_errc::ok)
        return err;
    output = field_view(d);
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE
deserialize_errc deserialize_binary_field_datetime(deserialization_context& ctx, field_view& output) noexcept
{
    using namespace binc;

    // Deserialize length
    std::uint8_t length;
    auto err = deserialize(ctx, length);
    if (err != deserialize_errc::ok)
        return err;

    // If the DATETIME does not contain some of the values below,
    // they are supposed to be zero
    boost::mysql::date d{};
    std::uint8_t hours = 0;
    std::uint8_t minutes = 0;
    std::uint8_t seconds = 0;
    std::uint32_t micros = 0;

    // Date part
    if (length >= datetime_d_sz)
    {
        err = deserialize_binary_ymd(ctx, d);
        if (err != deserialize_errc::ok)
            return err;
    }

    // Hours, minutes, seconds
    if (length >= datetime_dhms_sz)
    {
        err = deserialize(ctx, hours, minutes, seconds);
        if (err != deserialize_errc::ok)
            return err;
    }

    // Microseconds
    if (length >= datetime_dhmsu_sz)
    {
        err = deserialize(ctx, micros);
        if (err != deserialize_errc::ok)
            return err;
    }

    // Validity check. deserialize_binary_ymd already does it for date
    if (hours > max_hour || minutes > max_min || seconds > max_sec || micros > max_micro)
    {
        return deserialize_errc::protocol_value_error;
    }

    // Compose the final datetime
    boost::mysql::datetime dt(d.year(), d.month(), d.day(), hours, minutes, seconds, micros);
    output = field_view(dt);
    return deserialize_errc::ok;
}

BOOST_MYSQL_STATIC_OR_INLINE
deserialize_errc deserialize_binary_field_time(deserialization_context& ctx, field_view& output) noexcept
{
    using namespace boost::mysql::detail;
    using namespace boost::mysql::detail::binc;

    // Deserialize length
    std::uint8_t length;
    auto err = deserialize(ctx, length);
    if (err != deserialize_errc::ok)
        return err;

    // If the TIME contains no value for these fields, they are zero
    std::uint8_t is_negative = 0;
    std::uint32_t num_days = 0;
    std::uint8_t hours = 0;
    std::uint8_t minutes = 0;
    std::uint8_t seconds = 0;
    std::uint32_t microseconds = 0;

    // Sign, days, hours, minutes, seconds
    if (length >= time_dhms_sz)
    {
        err = deserialize(ctx, is_negative, num_days, hours, minutes, seconds);
        if (err != deserialize_errc::ok)
            return err;
    }

    // Microseconds
    if (length >= time_dhmsu_sz)
    {
        err = deserialize(ctx, microseconds);
        if (err != deserialize_errc::ok)
            return err;
    }

    // Range check
    if (num_days > time_max_days || hours > max_hour || minutes > max_min || seconds > max_sec ||
        microseconds > max_micro)
    {
        return deserialize_errc::protocol_value_error;
    }

    // Compose the final time
    output = field_view(boost::mysql::time(
        (is_negative ? -1 : 1) *
        (boost::mysql::days(num_days) + std::chrono::hours(hours) + std::chrono::minutes(minutes) +
         std::chrono::seconds(seconds) + std::chrono::microseconds(microseconds))
    ));
    return deserialize_errc::ok;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

boost::mysql::detail::deserialize_errc boost::mysql::detail::deserialize_binary_field(
    deserialization_context& ctx,
    const metadata& meta,
    field_view& output
)
{
    switch (meta.type())
    {
    case column_type::tinyint:
        return deserialize_binary_field_int<std::uint8_t, std::int8_t>(meta, ctx, output);
    case column_type::smallint:
    case column_type::year:
        return deserialize_binary_field_int<std::uint16_t, std::int16_t>(meta, ctx, output);
    case column_type::mediumint:
    case column_type::int_:
        return deserialize_binary_field_int<std::uint32_t, std::int32_t>(meta, ctx, output);
    case column_type::bigint:
        return deserialize_binary_field_int<std::uint64_t, std::int64_t>(meta, ctx, output);
    case column_type::bit: return deserialize_binary_field_bit(ctx, output);
    case column_type::float_: return deserialize_binary_field_float<float>(ctx, output);
    case column_type::double_: return deserialize_binary_field_float<double>(ctx, output);
    case column_type::timestamp:
    case column_type::datetime: return deserialize_binary_field_datetime(ctx, output);
    case column_type::date: return deserialize_binary_field_date(ctx, output);
    case column_type::time: return deserialize_binary_field_time(ctx, output);
    // True string types
    case column_type::char_:
    case column_type::varchar:
    case column_type::text:
    case column_type::enum_:
    case column_type::set:
    case column_type::decimal:
    case column_type::json: return deserialize_binary_field_string(ctx, output, false);
    // Blobs and anything else
    case column_type::binary:
    case column_type::varbinary:
    case column_type::blob:
    case column_type::geometry:
    default: return deserialize_binary_field_string(ctx, output, true);
    }
}

#endif