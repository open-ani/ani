//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_COLUMN_TYPE_IPP
#define BOOST_MYSQL_IMPL_COLUMN_TYPE_IPP

#pragma once

#include <boost/mysql/column_type.hpp>

#include <ostream>

std::ostream& boost::mysql::operator<<(std::ostream& os, column_type t)
{
    switch (t)
    {
    case column_type::tinyint: return os << "tinyint";
    case column_type::smallint: return os << "smallint";
    case column_type::mediumint: return os << "mediumint";
    case column_type::int_: return os << "int_";
    case column_type::bigint: return os << "bigint";
    case column_type::float_: return os << "float_";
    case column_type::double_: return os << "double_";
    case column_type::decimal: return os << "decimal";
    case column_type::bit: return os << "bit";
    case column_type::year: return os << "year";
    case column_type::time: return os << "time";
    case column_type::date: return os << "date";
    case column_type::datetime: return os << "datetime";
    case column_type::timestamp: return os << "timestamp";
    case column_type::char_: return os << "char_";
    case column_type::varchar: return os << "varchar";
    case column_type::binary: return os << "binary";
    case column_type::varbinary: return os << "varbinary";
    case column_type::text: return os << "text";
    case column_type::blob: return os << "blob";
    case column_type::enum_: return os << "enum_";
    case column_type::set: return os << "set";
    case column_type::json: return os << "json";
    case column_type::geometry: return os << "geometry";
    default: return os << "<unknown column type>";
    }
}

#endif