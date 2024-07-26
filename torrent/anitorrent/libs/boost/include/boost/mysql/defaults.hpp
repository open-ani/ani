//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DEFAULTS_HPP
#define BOOST_MYSQL_DEFAULTS_HPP

#include <cstddef>

namespace boost {
namespace mysql {

/// The default TCP port for the MySQL protocol.
constexpr unsigned short default_port = 3306;

/// The default TCP port for the MySQL protocol, as a string. Useful for hostname resolution.
constexpr const char* default_port_string = "3306";

/// The default initial size of the connection's internal read buffer, in bytes.
constexpr std::size_t default_initial_read_buffer_size = 1024;

}  // namespace mysql
}  // namespace boost

#endif
