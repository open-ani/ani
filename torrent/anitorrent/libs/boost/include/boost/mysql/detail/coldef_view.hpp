//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_COLDEF_VIEW_HPP
#define BOOST_MYSQL_DETAIL_COLDEF_VIEW_HPP

#include <boost/mysql/column_type.hpp>
#include <boost/mysql/string_view.hpp>

#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

struct coldef_view
{
    string_view database;
    string_view table;
    string_view org_table;
    string_view name;
    string_view org_name;
    std::uint16_t collation_id;
    std::uint32_t column_length;  // maximum length of the field
    column_type type;
    std::uint16_t flags;
    std::uint8_t decimals;  // max shown decimal digits. 0x00 for int/static strings; 0x1f for
                            // dynamic strings, double, float
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
