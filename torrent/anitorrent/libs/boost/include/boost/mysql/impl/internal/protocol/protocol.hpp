//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_PROTOCOL_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_PROTOCOL_HPP

#include <boost/mysql/column_type.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/coldef_view.hpp>
#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/ok_view.hpp>
#include <boost/mysql/detail/resultset_encoding.hpp>

#include <boost/mysql/impl/internal/protocol/capabilities.hpp>
#include <boost/mysql/impl/internal/protocol/constants.hpp>
#include <boost/mysql/impl/internal/protocol/db_flavor.hpp>
#include <boost/mysql/impl/internal/protocol/static_buffer.hpp>

#include <boost/config.hpp>
#include <boost/core/span.hpp>

#include <cstddef>
#include <cstdint>
#include <type_traits>

namespace boost {
namespace mysql {
namespace detail {

// Frame header
constexpr std::size_t frame_header_size = 4;

struct frame_header
{
    std::uint32_t size;
    std::uint8_t sequence_number;
};

BOOST_MYSQL_DECL
void serialize_frame_header(frame_header, span<std::uint8_t, frame_header_size> buffer) noexcept;

BOOST_MYSQL_DECL
frame_header deserialize_frame_header(span<const std::uint8_t, frame_header_size> buffer) noexcept;

// OK packets (views because strings are non-owning)
BOOST_MYSQL_DECL
error_code deserialize_ok_packet(span<const std::uint8_t> msg, ok_view& output) noexcept;  // for testing

// Error packets (exposed for testing)
struct err_view
{
    std::uint16_t error_code;
    string_view error_message;
};
BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code deserialize_error_packet(
    span<const std::uint8_t> message,
    err_view& pack,
    bool has_sql_state = true
) noexcept;

BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code process_error_packet(
    span<const std::uint8_t> message,
    db_flavor flavor,
    diagnostics& diag,
    bool has_sql_state = true
);

// Column definition
BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code
deserialize_column_definition(span<const std::uint8_t> input, coldef_view& output) noexcept;

// Quit
struct quit_command
{
    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

// Ping
struct ping_command
{
    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

// Reset connection
struct reset_connection_command
{
    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

// Deserializes a response that may be an OK or an error packet.
// Applicable for commands like ping and reset connection.
// If the response is an OK packet, sets backslash_escapes according to the
// OK packet's server status flags
BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code deserialize_ok_response(
    span<const std::uint8_t> message,
    db_flavor flavor,
    diagnostics& diag,
    bool& backslash_escapes
);

// Query
struct query_command
{
    string_view query;

    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

// Prepare statement
struct prepare_stmt_command
{
    string_view stmt;

    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};
struct prepare_stmt_response
{
    std::uint32_t id;
    std::uint16_t num_columns;
    std::uint16_t num_params;
};
BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code deserialize_prepare_stmt_response_impl(
    span<const std::uint8_t> message,
    prepare_stmt_response& output
) noexcept;  // exposed for testing, doesn't take header into account

BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code deserialize_prepare_stmt_response(
    span<const std::uint8_t> message,
    db_flavor flavor,
    prepare_stmt_response& output,
    diagnostics& diag
);

// Execute statement
struct execute_stmt_command
{
    std::uint32_t statement_id;
    span<const field_view> params;

    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

// Close statement
struct close_stmt_command
{
    std::uint32_t statement_id{};

    constexpr close_stmt_command() = default;
    constexpr close_stmt_command(std::uint32_t statement_id) noexcept : statement_id(statement_id) {}

    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

// Execution messages
static_assert(std::is_trivially_destructible<error_code>::value, "");
struct execute_response
{
    enum class type_t
    {
        num_fields,
        ok_packet,
        error
    } type;
    union data_t
    {
        std::size_t num_fields;
        ok_view ok_pack;
        error_code err;

        data_t(size_t v) noexcept : num_fields(v) {}
        data_t(const ok_view& v) noexcept : ok_pack(v) {}
        data_t(error_code v) noexcept : err(v) {}
    } data;

    execute_response(std::size_t v) noexcept : type(type_t::num_fields), data(v) {}
    execute_response(const ok_view& v) noexcept : type(type_t::ok_packet), data(v) {}
    execute_response(error_code v) noexcept : type(type_t::error), data(v) {}
};
BOOST_MYSQL_DECL
execute_response deserialize_execute_response(
    span<const std::uint8_t> msg,
    db_flavor flavor,
    diagnostics& diag
) noexcept;

struct row_message
{
    enum class type_t
    {
        row,
        ok_packet,
        error
    } type;
    union data_t
    {
        span<const std::uint8_t> row;
        ok_view ok_pack;
        error_code err;

        data_t(span<const std::uint8_t> row) noexcept : row(row) {}
        data_t(const ok_view& ok_pack) noexcept : ok_pack(ok_pack) {}
        data_t(error_code err) noexcept : err(err) {}
    } data;

    row_message(span<const std::uint8_t> row) noexcept : type(type_t::row), data(row) {}
    row_message(const ok_view& ok_pack) noexcept : type(type_t::ok_packet), data(ok_pack) {}
    row_message(error_code v) noexcept : type(type_t::error), data(v) {}
};
BOOST_MYSQL_DECL
row_message deserialize_row_message(span<const std::uint8_t> msg, db_flavor flavor, diagnostics& diag);

BOOST_MYSQL_DECL
error_code deserialize_row(
    resultset_encoding encoding,
    span<const std::uint8_t> message,
    metadata_collection_view meta,
    span<field_view> output  // Should point to meta.size() field_view objects
);

// Server hello
struct server_hello
{
    using auth_buffer_type = static_buffer<8 + 0xff>;
    db_flavor server;
    auth_buffer_type auth_plugin_data;
    capabilities server_capabilities{};
    string_view auth_plugin_name;
};
BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code deserialize_server_hello_impl(
    span<const std::uint8_t> msg,
    server_hello& output
);  // exposed for testing, doesn't take message header into account

BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code
deserialize_server_hello(span<const std::uint8_t> msg, server_hello& output, diagnostics& diag);

// Login & ssl requests
struct login_request
{
    capabilities negotiated_capabilities;  // capabilities
    std::uint32_t max_packet_size;
    std::uint32_t collation_id;
    string_view username;
    span<const std::uint8_t> auth_response;
    string_view database;
    string_view auth_plugin_name;

    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

struct ssl_request
{
    capabilities negotiated_capabilities;
    std::uint32_t max_packet_size;
    std::uint32_t collation_id;

    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

// Auth switch
struct auth_switch
{
    string_view plugin_name;
    span<const std::uint8_t> auth_data;
};

BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code deserialize_auth_switch(
    span<const std::uint8_t> msg,
    auth_switch& output
) noexcept;  // exposed for testing

struct handhake_server_response
{
    struct ok_follows_t
    {
    };

    enum class type_t
    {
        ok,
        error,
        ok_follows,
        auth_switch,
        auth_more_data
    } type;

    union data_t
    {
        ok_view ok;
        error_code err;
        ok_follows_t ok_follows;
        auth_switch auth_sw;
        span<const std::uint8_t> more_data;

        data_t(const ok_view& ok) noexcept : ok(ok) {}
        data_t(error_code err) noexcept : err(err) {}
        data_t(ok_follows_t) noexcept : ok_follows({}) {}
        data_t(auth_switch msg) noexcept : auth_sw(msg) {}
        data_t(span<const std::uint8_t> more_data) noexcept : more_data(more_data) {}
    } data;

    handhake_server_response(const ok_view& ok) noexcept : type(type_t::ok), data(ok) {}
    handhake_server_response(error_code err) noexcept : type(type_t::error), data(err) {}
    handhake_server_response(ok_follows_t) noexcept : type(type_t::ok_follows), data(ok_follows_t{}) {}
    handhake_server_response(auth_switch auth_switch) noexcept : type(type_t::auth_switch), data(auth_switch)
    {
    }
    handhake_server_response(span<const std::uint8_t> more_data) noexcept
        : type(type_t::auth_more_data), data(more_data)
    {
    }
};
BOOST_MYSQL_DECL
handhake_server_response deserialize_handshake_server_response(
    span<const std::uint8_t> buff,
    db_flavor flavor,
    diagnostics& diag
);

struct auth_switch_response
{
    span<const std::uint8_t> auth_plugin_data;

    BOOST_MYSQL_DECL std::size_t get_size() const noexcept;
    BOOST_MYSQL_DECL void serialize(span<std::uint8_t> buffer) const noexcept;
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/internal/protocol/protocol.ipp>
#endif

#endif
