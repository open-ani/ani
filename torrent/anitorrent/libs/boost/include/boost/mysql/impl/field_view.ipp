//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_FIELD_VIEW_IPP
#define BOOST_MYSQL_IMPL_FIELD_VIEW_IPP

#pragma once

#include <boost/mysql/field_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/impl/internal/byte_to_hex.hpp>
#include <boost/mysql/impl/internal/dt_to_string.hpp>

#include <boost/assert.hpp>

#include <cstddef>
#include <ostream>

namespace boost {
namespace mysql {
namespace detail {

inline std::ostream& print_blob(std::ostream& os, blob_view value)
{
    if (value.empty())
        return os << "{}";

    char buffer[16]{'0', 'x'};

    os << "{ ";
    for (std::size_t i = 0; i < value.size(); ++i)
    {
        // Separating comma
        if (i != 0)
            os << ", ";

        // Convert to hex
        byte_to_hex(value[i], buffer + 2);

        // Insert
        os << string_view(buffer, 4);
    }
    os << " }";
    return os;
}

inline std::ostream& print_time(std::ostream& os, const boost::mysql::time& value)
{
    char buffer[64]{};
    std::size_t sz = detail::time_to_string(value, buffer);
    os << string_view(buffer, sz);
    return os;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

std::ostream& boost::mysql::operator<<(std::ostream& os, const field_view& value)
{
    // Make operator<< work for detail::string_view_offset types
    if (value.impl_.is_string_offset() || value.impl_.is_blob_offset())
    {
        return os << "<sv_offset>";
    }

    switch (value.kind())
    {
    case field_kind::null: return os << "<NULL>";
    case field_kind::int64: return os << value.get_int64();
    case field_kind::uint64: return os << value.get_uint64();
    case field_kind::string: return os << value.get_string();
    case field_kind::blob: return detail::print_blob(os, value.get_blob());
    case field_kind::float_: return os << value.get_float();
    case field_kind::double_: return os << value.get_double();
    case field_kind::date: return os << value.get_date();
    case field_kind::datetime: return os << value.get_datetime();
    case field_kind::time: return detail::print_time(os, value.get_time());
    default: BOOST_ASSERT(false); return os;
    }
}

#endif