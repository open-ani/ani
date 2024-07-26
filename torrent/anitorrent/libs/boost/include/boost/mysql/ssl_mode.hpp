//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_SSL_MODE_HPP
#define BOOST_MYSQL_SSL_MODE_HPP

namespace boost {
namespace mysql {

/// Determines how to perform SSL negotiation with the server.
enum class ssl_mode
{
    /// Never use TLS
    disable,

    /// Use TLS if the server supports it, fall back to non-encrypted connection if it does not.
    enable,

    /// Always use TLS; abort the connection if the server does not support it.
    /// Ignored by connections with `Stream` types not supporting SSL.
    require
};

}  // namespace mysql
}  // namespace boost

#endif
