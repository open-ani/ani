//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_PROTOCOL_FIELD_TYPE_IPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_PROTOCOL_FIELD_TYPE_IPP

#pragma once

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/flags.hpp>

#include <boost/mysql/impl/internal/protocol/constants.hpp>
#include <boost/mysql/impl/internal/protocol/protocol_field_type.hpp>

namespace boost {
namespace mysql {
namespace detail {

BOOST_MYSQL_STATIC_OR_INLINE
column_type compute_field_type_string(std::uint16_t flags, std::uint16_t collation) noexcept
{
    if (flags & column_flags::set)
        return column_type::set;
    else if (flags & column_flags::enum_)
        return column_type::enum_;
    else if (collation == binary_collation)
        return column_type::binary;
    else
        return column_type::char_;
}

BOOST_MYSQL_STATIC_OR_INLINE
column_type compute_field_type_var_string(std::uint16_t collation) noexcept
{
    return collation == binary_collation ? column_type::varbinary : column_type::varchar;
}

BOOST_MYSQL_STATIC_OR_INLINE
column_type compute_field_type_blob(std::uint16_t collation) noexcept
{
    return collation == binary_collation ? column_type::blob : column_type::text;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

boost::mysql::column_type boost::mysql::detail::compute_column_type(
    protocol_field_type protocol_type,
    std::uint16_t flags,
    std::uint16_t collation
) noexcept
{
    // Some protocol_field_types seem to not be sent by the server. We've found instances
    // where some servers, with certain SQL statements, send some of the "apparently not sent"
    // types (e.g. MariaDB was sending medium_blob only if you SELECT TEXT variables - but not with TEXT
    // columns). So we've taken a defensive approach here
    switch (protocol_type)
    {
    case protocol_field_type::decimal:
    case protocol_field_type::newdecimal: return column_type::decimal;
    case protocol_field_type::geometry: return column_type::geometry;
    case protocol_field_type::tiny: return column_type::tinyint;
    case protocol_field_type::short_: return column_type::smallint;
    case protocol_field_type::int24: return column_type::mediumint;
    case protocol_field_type::long_: return column_type::int_;
    case protocol_field_type::longlong: return column_type::bigint;
    case protocol_field_type::float_: return column_type::float_;
    case protocol_field_type::double_: return column_type::double_;
    case protocol_field_type::bit: return column_type::bit;
    case protocol_field_type::date: return column_type::date;
    case protocol_field_type::datetime: return column_type::datetime;
    case protocol_field_type::timestamp: return column_type::timestamp;
    case protocol_field_type::time: return column_type::time;
    case protocol_field_type::year: return column_type::year;
    case protocol_field_type::json: return column_type::json;
    case protocol_field_type::enum_: return column_type::enum_;  // in theory not set
    case protocol_field_type::set: return column_type::set;      // in theory not set
    case protocol_field_type::string: return compute_field_type_string(flags, collation);
    case protocol_field_type::varchar:  // in theory not sent
    case protocol_field_type::var_string: return compute_field_type_var_string(collation);
    case protocol_field_type::tiny_blob:    // in theory not sent
    case protocol_field_type::medium_blob:  // in theory not sent
    case protocol_field_type::long_blob:    // in theory not sent
    case protocol_field_type::blob: return compute_field_type_blob(collation);
    default: return column_type::unknown;
    }
}

#endif