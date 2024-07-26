//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_DB_FLAVOR_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_DB_FLAVOR_HPP

namespace boost {
namespace mysql {
namespace detail {

enum class db_flavor
{
    mysql,
    mariadb
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
