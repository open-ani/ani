//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_HANDSHAKE_PARAMS_HPP
#define BOOST_MYSQL_HANDSHAKE_PARAMS_HPP

#include <boost/mysql/buffer_params.hpp>
#include <boost/mysql/ssl_mode.hpp>
#include <boost/mysql/string_view.hpp>

#include <cstdint>

namespace boost {
namespace mysql {

/**
 * \brief Parameters defining how to perform the handshake with a MySQL server.
 * \par Object lifetimes
 * This object stores references to strings (like username and password), performing
 * no copy of these values. Users are resposible for keeping them alive until required.
 */
class handshake_params
{
    string_view username_;
    string_view password_;
    string_view database_;
    std::uint16_t connection_collation_;
    ssl_mode ssl_;
    bool multi_queries_;

public:
    /// The default collation to use with the connection (`utf8mb4_general_ci` on both MySQL and MariaDB).
    static constexpr std::uint16_t default_collation = 45;

    /**
     * \brief Initializing constructor.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \param username User name to authenticate as.
     * \param password Password for that username, possibly empty.
     * \param db Database name to use, or empty string for no database (this is the default).
     * \param connection_col The ID of the collation to use for the connection.
     * Impacts how text queries and prepared statements are interpreted. Defaults to
     * `utf8mb4_general_ci` (see \ref default_collation), which is compatible with MySQL 5.x, 8.x and MariaDB.
     * \param mode The \ref ssl_mode to use with this connection; ignored if
     * the connection's `Stream` does not support SSL.
     * \param multi_queries Whether to enable support for executing semicolon-separated
     * queries using \ref connection::execute and \ref connection::start_execution. Disabled by default.
     */
    handshake_params(
        string_view username,
        string_view password,
        string_view db = "",
        std::uint16_t connection_col = default_collation,
        ssl_mode mode = ssl_mode::require,
        bool multi_queries = false
    )
        : username_(username),
          password_(password),
          database_(db),
          connection_collation_(connection_col),
          ssl_(mode),
          multi_queries_(multi_queries)
    {
    }

    /**
     * \brief Retrieves the username.
     * \par Exception safety
     * No-throw guarantee.
     */
    string_view username() const noexcept { return username_; }

    /**
     * \brief Sets the username.
     * \par Exception safety
     * No-throw guarantee.
     */
    void set_username(string_view value) noexcept { username_ = value; }

    /**
     * \brief Retrieves the password.
     * \par Exception safety
     * No-throw guarantee.
     */
    string_view password() const noexcept { return password_; }

    /**
     * \brief Sets the password.
     * \par Exception safety
     * No-throw guarantee.
     */
    void set_password(string_view value) noexcept { password_ = value; }

    /**
     * \brief Retrieves the database name to use when connecting.
     * \par Exception safety
     * No-throw guarantee.
     */
    string_view database() const noexcept { return database_; }

    /**
     * \brief Sets the database name to use when connecting.
     * \par Exception safety
     * No-throw guarantee.
     */
    void set_database(string_view value) noexcept { database_ = value; }

    /**
     * \brief Retrieves the connection collation.
     * \par Exception safety
     * No-throw guarantee.
     */
    std::uint16_t connection_collation() const noexcept { return connection_collation_; }

    /**
     * \brief Sets the connection collation.
     * \par Exception safety
     * No-throw guarantee.
     */
    void set_connection_collation(std::uint16_t value) noexcept { connection_collation_ = value; }

    /**
     * \brief Retrieves the SSL mode.
     * \par Exception safety
     * No-throw guarantee.
     */
    ssl_mode ssl() const noexcept { return ssl_; }

    /**
     * \brief Sets the SSL mode.
     * \par Exception safety
     * No-throw guarantee.
     */
    void set_ssl(ssl_mode value) noexcept { ssl_ = value; }

    /**
     * \brief Retrieves whether multi-query support is enabled.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool multi_queries() const noexcept { return multi_queries_; }

    /**
     * \brief Enables or disables support for the multi-query feature.
     * \par Exception safety
     * No-throw guarantee.
     */
    void set_multi_queries(bool v) noexcept { multi_queries_ = v; }
};

}  // namespace mysql
}  // namespace boost

#endif
