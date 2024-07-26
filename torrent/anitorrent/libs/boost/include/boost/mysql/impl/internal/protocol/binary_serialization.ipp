//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_BINARY_SERIALIZATION_IPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_BINARY_SERIALIZATION_IPP

#pragma once

#include <boost/mysql/days.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/mysql/impl/internal/protocol/binary_serialization.hpp>
#include <boost/mysql/impl/internal/protocol/constants.hpp>
#include <boost/mysql/impl/internal/protocol/serialization.hpp>

#include <chrono>

namespace boost {
namespace mysql {
namespace detail {

// Binary serialization
template <class T>
BOOST_MYSQL_STATIC_OR_INLINE void serialize_binary_float(serialization_context& ctx, T input)
{
    boost::endian::endian_store<T, sizeof(T), boost::endian::order::little>(ctx.first(), input);
    ctx.advance(sizeof(T));
}

BOOST_MYSQL_STATIC_OR_INLINE
void serialize_binary_date(serialization_context& ctx, const date& input)
{
    using namespace binc;
    serialize(ctx, static_cast<std::uint8_t>(date_sz), input.year(), input.month(), input.day());
}

BOOST_MYSQL_STATIC_OR_INLINE
void serialize_binary_datetime(serialization_context& ctx, const datetime& input)
{
    using namespace binc;

    // Serialize
    serialize(
        ctx,
        static_cast<std::uint8_t>(datetime_dhmsu_sz),
        input.year(),
        input.month(),
        input.day(),
        input.hour(),
        input.minute(),
        input.second(),
        input.microsecond()
    );
}

BOOST_MYSQL_STATIC_OR_INLINE
void serialize_binary_time(serialization_context& ctx, const boost::mysql::time& input)
{
    using namespace binc;
    using boost::mysql::days;
    using std::chrono::duration_cast;
    using std::chrono::hours;
    using std::chrono::microseconds;
    using std::chrono::minutes;
    using std::chrono::seconds;

    // Break time
    auto num_micros = duration_cast<microseconds>(input % seconds(1));
    auto num_secs = duration_cast<seconds>(input % minutes(1) - num_micros);
    auto num_mins = duration_cast<minutes>(input % hours(1) - num_secs);
    auto num_hours = duration_cast<hours>(input % days(1) - num_mins);
    auto num_days = duration_cast<days>(input - num_hours);
    std::uint8_t is_negative = (input.count() < 0) ? 1 : 0;

    // Serialize
    serialize(
        ctx,
        static_cast<std::uint8_t>(time_dhmsu_sz),
        is_negative,
        static_cast<std::uint32_t>(std::abs(num_days.count())),
        static_cast<std::uint8_t>(std::abs(num_hours.count())),
        static_cast<std::uint8_t>(std::abs(num_mins.count())),
        static_cast<std::uint8_t>(std::abs(num_secs.count())),
        static_cast<std::uint32_t>(std::abs(num_micros.count()))
    );
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

std::size_t boost::mysql::detail::get_size(field_view input) noexcept
{
    switch (input.kind())
    {
    case field_kind::null: return 0;
    case field_kind::int64: return 8;
    case field_kind::uint64: return 8;
    case field_kind::string: return get_size(string_lenenc{input.get_string()});
    case field_kind::blob: return get_size(string_lenenc{to_string(input.get_blob())});
    case field_kind::float_: return 4;
    case field_kind::double_: return 8;
    case field_kind::date: return binc::date_sz + binc::length_sz;
    case field_kind::datetime: return binc::datetime_dhmsu_sz + binc::length_sz;
    case field_kind::time: return binc::time_dhmsu_sz + binc::length_sz;
    default: BOOST_ASSERT(false); return 0;
    }
}

void boost::mysql::detail::serialize(serialization_context& ctx, field_view input) noexcept
{
    switch (input.kind())
    {
    case field_kind::null: break;
    case field_kind::int64: serialize(ctx, input.get_int64()); break;
    case field_kind::uint64: serialize(ctx, input.get_uint64()); break;
    case field_kind::string: serialize(ctx, string_lenenc{input.get_string()}); break;
    case field_kind::blob: serialize(ctx, string_lenenc{to_string(input.get_blob())}); break;
    case field_kind::float_: serialize_binary_float(ctx, input.get_float()); break;
    case field_kind::double_: serialize_binary_float(ctx, input.get_double()); break;
    case field_kind::date: serialize_binary_date(ctx, input.get_date()); break;
    case field_kind::datetime: serialize_binary_datetime(ctx, input.get_datetime()); break;
    case field_kind::time: serialize_binary_time(ctx, input.get_time()); break;
    default: BOOST_ASSERT(false); break;
    }
}

#endif