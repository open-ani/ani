//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_FIELD_KIND_IPP
#define BOOST_MYSQL_IMPL_FIELD_KIND_IPP

#pragma once

#include <boost/mysql/field_kind.hpp>

#include <ostream>

std::ostream& boost::mysql::operator<<(std::ostream& os, boost::mysql::field_kind v)
{
    switch (v)
    {
    case field_kind::null: return os << "null";
    case field_kind::int64: return os << "int64";
    case field_kind::uint64: return os << "uint64";
    case field_kind::string: return os << "string";
    case field_kind::float_: return os << "float_";
    case field_kind::double_: return os << "double_";
    case field_kind::date: return os << "date";
    case field_kind::datetime: return os << "datetime";
    case field_kind::time: return os << "time";
    default: return os << "<invalid>";
    }
}

#endif