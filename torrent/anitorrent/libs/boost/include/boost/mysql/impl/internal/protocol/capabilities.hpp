//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_CAPABILITIES_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_CAPABILITIES_HPP

#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

// Server/client capabilities
// clang-format off
constexpr std::uint32_t CLIENT_LONG_PASSWORD = 1; // Use the improved version of Old Password Authentication
constexpr std::uint32_t CLIENT_FOUND_ROWS = 2; // Send found rows instead of affected rows in EOF_Packet
constexpr std::uint32_t CLIENT_LONG_FLAG = 4; // Get all column flags
constexpr std::uint32_t CLIENT_CONNECT_WITH_DB = 8; // Database (schema) name can be specified on connect in Handshake Response Packet
constexpr std::uint32_t CLIENT_NO_SCHEMA = 16; // Don't allow database.table.column
constexpr std::uint32_t CLIENT_COMPRESS = 32; // Compression protocol supported
constexpr std::uint32_t CLIENT_ODBC = 64; // Special handling of ODBC behavior
constexpr std::uint32_t CLIENT_LOCAL_FILES = 128; // Can use LOAD DATA LOCAL
constexpr std::uint32_t CLIENT_IGNORE_SPACE = 256; // Ignore spaces before '('
constexpr std::uint32_t CLIENT_PROTOCOL_41 = 512; // New 4.1 protocol
constexpr std::uint32_t CLIENT_INTERACTIVE = 1024; // This is an interactive client
constexpr std::uint32_t CLIENT_SSL = 2048; // Use SSL encryption for the session
constexpr std::uint32_t CLIENT_IGNORE_SIGPIPE = 4096; // Client only flag
constexpr std::uint32_t CLIENT_TRANSACTIONS = 8192; // Client knows about transactions
constexpr std::uint32_t CLIENT_RESERVED = 16384; // DEPRECATED: Old flag for 4.1 protocol
constexpr std::uint32_t CLIENT_SECURE_CONNECTION = 32768; // DEPRECATED: Old flag for 4.1 authentication, required by MariaDB
constexpr std::uint32_t CLIENT_MULTI_STATEMENTS = (1UL << 16); // Enable/disable multi-stmt support
constexpr std::uint32_t CLIENT_MULTI_RESULTS = (1UL << 17); // Enable/disable multi-results
constexpr std::uint32_t CLIENT_PS_MULTI_RESULTS = (1UL << 18); // Multi-results and OUT parameters in PS-protocol
constexpr std::uint32_t CLIENT_PLUGIN_AUTH = (1UL << 19); // Client supports plugin authentication
constexpr std::uint32_t CLIENT_CONNECT_ATTRS = (1UL << 20); // Client supports connection attributes
constexpr std::uint32_t CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = (1UL << 21); // Enable authentication response packet to be larger than 255 bytes
constexpr std::uint32_t CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS = (1UL << 22); // Don't close the connection for a user account with expired password
constexpr std::uint32_t CLIENT_SESSION_TRACK = (1UL << 23); // Capable of handling server state change information
constexpr std::uint32_t CLIENT_DEPRECATE_EOF = (1UL << 24); // Client no longer needs EOF_Packet and will use OK_Packet instead
constexpr std::uint32_t CLIENT_SSL_VERIFY_SERVER_CERT = (1UL << 30); // Verify server certificate
constexpr std::uint32_t CLIENT_OPTIONAL_RESULTSET_METADATA = (1UL << 25); // The client can handle optional metadata information in the resultset
constexpr std::uint32_t CLIENT_REMEMBER_OPTIONS = (1UL << 31); // Don't reset the options after an unsuccessful connect
// clang-format on

class capabilities
{
    std::uint32_t value_;

public:
    constexpr explicit capabilities(std::uint32_t value = 0) noexcept : value_(value){};
    constexpr std::uint32_t get() const noexcept { return value_; }
    void set(std::uint32_t value) noexcept { value_ = value; }
    constexpr bool has(std::uint32_t cap) const noexcept { return value_ & cap; }
    constexpr bool has_all(capabilities other) const noexcept
    {
        return (value_ & other.get()) == other.get();
    }
    constexpr capabilities operator|(capabilities rhs) const noexcept
    {
        return capabilities(value_ | rhs.value_);
    }
    constexpr capabilities operator&(capabilities rhs) const noexcept
    {
        return capabilities(value_ & rhs.value_);
    }
    constexpr bool operator==(const capabilities& rhs) const noexcept { return value_ == rhs.value_; }
    constexpr bool operator!=(const capabilities& rhs) const noexcept { return value_ != rhs.value_; }
};

/*
 * CLIENT_LONG_PASSWORD: unset //  Use the improved version of Old Password Authentication
 * CLIENT_FOUND_ROWS: unset //  Send found rows instead of affected rows in EOF_Packet
 * CLIENT_LONG_FLAG: unset //  Get all column flags
 * CLIENT_CONNECT_WITH_DB: optional //  Database (schema) name can be specified on connect in
 * Handshake Response Packet CLIENT_NO_SCHEMA: unset //  Don't allow database.table.column
 * CLIENT_COMPRESS: unset //  Compression protocol supported
 * CLIENT_ODBC: unset //  Special handling of ODBC behavior
 * CLIENT_LOCAL_FILES: unset //  Can use LOAD DATA LOCAL
 * CLIENT_IGNORE_SPACE: unset //  Ignore spaces before '('
 * CLIENT_PROTOCOL_41: mandatory //  New 4.1 protocol
 * CLIENT_INTERACTIVE: unset //  This is an interactive client
 * CLIENT_SSL: unset //  Use SSL encryption for the session
 * CLIENT_IGNORE_SIGPIPE: unset //  Client only flag
 * CLIENT_TRANSACTIONS: unset //  Client knows about transactions
 * CLIENT_RESERVED: unset //  DEPRECATED: Old flag for 4.1 protocol
 * CLIENT_RESERVED2: unset //  DEPRECATED: Old flag for 4.1 authentication
 * \ CLIENT_SECURE_CONNECTION CLIENT_MULTI_STATEMENTS: unset //  Enable/disable multi-stmt support
 * CLIENT_MULTI_RESULTS: unset //  Enable/disable multi-results
 * CLIENT_PS_MULTI_RESULTS: unset //  Multi-results and OUT parameters in PS-protocol
 * CLIENT_PLUGIN_AUTH: mandatory //  Client supports plugin authentication
 * CLIENT_CONNECT_ATTRS: unset //  Client supports connection attributes
 * CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA: mandatory //  Enable authentication response packet to be
 * larger than 255 bytes CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS: unset //  Don't close the connection
 * for a user account with expired password CLIENT_SESSION_TRACK: unset //  Capable of handling
 * server state change information CLIENT_DEPRECATE_EOF: mandatory //  Client no longer needs
 * EOF_Packet and will use OK_Packet instead CLIENT_SSL_VERIFY_SERVER_CERT: unset //  Verify server
 * certificate CLIENT_OPTIONAL_RESULTSET_METADATA: unset //  The client can handle optional metadata
 * information in the resultset CLIENT_REMEMBER_OPTIONS: unset //  Don't reset the options after an
 * unsuccessful connect
 *
 * We pay attention to:
 * CLIENT_CONNECT_WITH_DB: optional //  Database (schema) name can be specified on connect in
 * Handshake Response Packet CLIENT_PROTOCOL_41: mandatory //  New 4.1 protocol CLIENT_PLUGIN_AUTH:
 * mandatory //  Client supports plugin authentication CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA:
 * mandatory //  Enable authentication response packet to be larger than 255 bytes
 * CLIENT_DEPRECATE_EOF: mandatory //  Client no longer needs EOF_Packet and will use OK_Packet
 * instead
 */

// clang-format off
constexpr capabilities mandatory_capabilities{
    CLIENT_PROTOCOL_41 |
    CLIENT_PLUGIN_AUTH |
    CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA |
    CLIENT_DEPRECATE_EOF |
    CLIENT_SECURE_CONNECTION
};
// clang-format on

constexpr capabilities optional_capabilities{CLIENT_MULTI_RESULTS | CLIENT_PS_MULTI_RESULTS};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
