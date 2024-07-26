//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_UNIX_SSL_HPP
#define BOOST_MYSQL_UNIX_SSL_HPP

#include <boost/mysql/connection.hpp>

#include <boost/asio/local/stream_protocol.hpp>
#include <boost/asio/ssl/stream.hpp>

namespace boost {
namespace mysql {

#if defined(BOOST_ASIO_HAS_LOCAL_SOCKETS) || defined(BOOST_MYSQL_DOXYGEN)

/// A connection to MySQL over a UNIX domain socket over TLS.
using unix_ssl_connection = connection<boost::asio::ssl::stream<boost::asio::local::stream_protocol::socket>>;

#endif

}  // namespace mysql
}  // namespace boost

#endif