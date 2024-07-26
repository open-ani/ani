//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_PROTOCOL_FIELD_TYPE_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_PROTOCOL_FIELD_TYPE_HPP

#include <boost/mysql/column_type.hpp>

#include <boost/mysql/detail/config.hpp>

#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

enum class protocol_field_type : std::uint8_t
{
    decimal = 0x00,      // Apparently not sent
    tiny = 0x01,         // TINYINT
    short_ = 0x02,       // SMALLINT
    long_ = 0x03,        // INT
    float_ = 0x04,       // FLOAT
    double_ = 0x05,      // DOUBLE
    null = 0x06,         // Apparently not sent
    timestamp = 0x07,    // TIMESTAMP
    longlong = 0x08,     // BIGINT
    int24 = 0x09,        // MEDIUMINT
    date = 0x0a,         // DATE
    time = 0x0b,         // TIME
    datetime = 0x0c,     // DATETIME
    year = 0x0d,         // YEAR
    varchar = 0x0f,      // Apparently not sent
    bit = 0x10,          // BIT
    json = 0xf5,         // JSON
    newdecimal = 0xf6,   // DECIMAL
    enum_ = 0xf7,        // Apparently not sent
    set = 0xf8,          // Apperently not sent
    tiny_blob = 0xf9,    // Apparently not sent
    medium_blob = 0xfa,  // Apparently not sent
    long_blob = 0xfb,    // Apparently not sent
    blob = 0xfc,         // Used for all TEXT and BLOB types
    var_string = 0xfd,   // Used for VARCHAR and VARBINARY
    string = 0xfe,       // Used for CHAR and BINARY, ENUM (enum flag set), SET (set flag set)
    geometry = 0xff      // GEOMETRY
};

BOOST_MYSQL_DECL
column_type compute_column_type(
    protocol_field_type protocol_type,
    std::uint16_t flags,
    std::uint16_t collation
) noexcept;

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/internal/protocol/protocol_field_type.ipp>
#endif

#endif
