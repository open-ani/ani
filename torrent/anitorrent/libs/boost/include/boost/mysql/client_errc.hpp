//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_CLIENT_ERRC_HPP
#define BOOST_MYSQL_CLIENT_ERRC_HPP

#include <boost/mysql/error_code.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/system/error_category.hpp>

namespace boost {
namespace mysql {

/**
 * \brief MySQL client-defined error codes.
 * \details These errors are produced by the client itself, rather than the server.
 */
enum class client_errc : int
{
    /// An incomplete message was received from the server (indicates a deserialization error or
    /// packet mismatch).
    incomplete_message = 1,

    /// An unexpected value was found in a server-received message (indicates a deserialization
    /// error or packet mismatch).
    protocol_value_error,

    /// The server does not support the minimum required capabilities to establish the connection.
    server_unsupported,

    /// Unexpected extra bytes at the end of a message were received (indicates a deserialization
    /// error or packet mismatch).
    extra_bytes,

    /// Mismatched sequence numbers (usually caused by a packet mismatch).
    sequence_number_mismatch,

    /// The user employs an authentication plugin not known to this library.
    unknown_auth_plugin,

    /// The authentication plugin requires the connection to use SSL.
    auth_plugin_requires_ssl,

    /// The number of parameters passed to the prepared statement does not match the number of
    /// actual parameters.
    wrong_num_params,

    /// The connection mandatory SSL, but the server doesn't accept SSL connections.
    server_doesnt_support_ssl,

    /// The static interface detected a mismatch between your C++ type definitions and what the server
    /// returned in the query.
    metadata_check_failed,

    /// The static interface detected a mismatch between the number of row types passed to `static_results`
    /// or `static_execution_state` and the number of resultsets returned by your query.
    num_resultsets_mismatch,

    /// The StaticRow type passed to read_some_rows does not correspond to the resultset type being read.
    row_type_mismatch,

    /// The static interface encountered an error when parsing a field into a C++ data structure.
    static_row_parsing_error,

    /// (EXPERIMENTAL) An operation controlled by Boost.MySQL timed out.
    timeout,

    /// (EXPERIMENTAL) An operation controlled by Boost.MySQL was cancelled.
    cancelled,

    /// (EXPERIMENTAL) Getting a connection from a connection_pool failed because the
    /// pool is not running. Ensure that you're calling connection_pool::async_run.
    pool_not_running,

    /// (EXPERIMENTAL) An invalid byte sequence was found while trying to decode a string.
    invalid_encoding,

    /// (EXPERIMENTAL) A formatting operation could not format one of its arguments.
    unformattable_value,

    /// (EXPERIMENTAL) An format string with invalid syntax was provided to a SQL formatting function.
    format_string_invalid_syntax,

    /// (EXPERIMENTAL) A format string with an invalid byte sequence was provided to a SQL formatting
    /// function.
    format_string_invalid_encoding,

    /// (EXPERIMENTAL) A format string mixes manual (e.g. {0}) and automatic (e.g. {}) indexing.
    format_string_manual_auto_mix,

    /// (EXPERIMENTAL) A format argument referenced by a format string was not found. Check the number
    /// of format arguments passed and their names.
    format_arg_not_found,

    /// (EXPERIMENTAL) The character set used by the connection is not known by the client. Use
    /// set_character_set or async_set_character_set before invoking operations that require a known charset.
    unknown_character_set,
};

BOOST_MYSQL_DECL
const boost::system::error_category& get_client_category() noexcept;

/// Creates an \ref error_code from a \ref client_errc.
inline error_code make_error_code(client_errc error)
{
    return error_code(static_cast<int>(error), get_client_category());
}

}  // namespace mysql

#ifndef BOOST_MYSQL_DOXYGEN
namespace system {

template <>
struct is_error_code_enum<::boost::mysql::client_errc>
{
    static constexpr bool value = true;
};
}  // namespace system
#endif

}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/error_categories.ipp>
#endif

#endif
