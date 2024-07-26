//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_PROTOCOL_IPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_PROTOCOL_IPP

#pragma once

#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/common_server_errc.hpp>
#include <boost/mysql/error_categories.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_kind.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/make_string_view.hpp>

#include <boost/mysql/impl/internal/error/server_error_to_string.hpp>
#include <boost/mysql/impl/internal/protocol/basic_types.hpp>
#include <boost/mysql/impl/internal/protocol/binary_serialization.hpp>
#include <boost/mysql/impl/internal/protocol/capabilities.hpp>
#include <boost/mysql/impl/internal/protocol/constants.hpp>
#include <boost/mysql/impl/internal/protocol/deserialize_binary_field.hpp>
#include <boost/mysql/impl/internal/protocol/deserialize_text_field.hpp>
#include <boost/mysql/impl/internal/protocol/null_bitmap_traits.hpp>
#include <boost/mysql/impl/internal/protocol/protocol.hpp>
#include <boost/mysql/impl/internal/protocol/serialization.hpp>

#include <boost/core/ignore_unused.hpp>
#include <boost/core/span.hpp>

#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

// Constants
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::uint8_t handshake_protocol_version_9 = 9;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::uint8_t handshake_protocol_version_10 = 10;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::uint8_t error_packet_header = 0xff;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::uint8_t ok_packet_header = 0x00;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::uint8_t eof_packet_header = 0xfe;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::uint8_t auth_switch_request_header = 0xfe;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::uint8_t auth_more_data_header = 0x01;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr string_view fast_auth_complete_challenge = make_string_view("\3");

// Helpers
BOOST_MYSQL_STATIC_OR_INLINE
void serialize_command_id(span<std::uint8_t> buff, std::uint8_t command_id) noexcept
{
    BOOST_ASSERT(buff.size() >= 1u);
    buff[0] = command_id;
}

struct frame_header_packet
{
    int3 packet_size;
    std::uint8_t sequence_number;
};

// Maps from an actual value to a protocol_field_type (for execute statement). Only value's type is used
static protocol_field_type get_protocol_field_type(field_view input) noexcept
{
    switch (input.kind())
    {
    case field_kind::null: return protocol_field_type::null;
    case field_kind::int64: return protocol_field_type::longlong;
    case field_kind::uint64: return protocol_field_type::longlong;
    case field_kind::string: return protocol_field_type::string;
    case field_kind::blob: return protocol_field_type::blob;
    case field_kind::float_: return protocol_field_type::float_;
    case field_kind::double_: return protocol_field_type::double_;
    case field_kind::date: return protocol_field_type::date;
    case field_kind::datetime: return protocol_field_type::datetime;
    case field_kind::time: return protocol_field_type::time;
    default: BOOST_ASSERT(false); return protocol_field_type::null;
    }
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

// Frame header
void boost::mysql::detail::serialize_frame_header(
    frame_header msg,
    span<std::uint8_t, frame_header_size> buffer
) noexcept
{
    BOOST_ASSERT(msg.size <= 0xffffff);  // range check
    serialization_context ctx(buffer.data());
    frame_header_packet pack{int3{msg.size}, msg.sequence_number};
    serialize(ctx, pack.packet_size, pack.sequence_number);
}

boost::mysql::detail::frame_header boost::mysql::detail::deserialize_frame_header(
    span<const std::uint8_t, frame_header_size> buffer
) noexcept
{
    frame_header_packet pack{};
    deserialization_context ctx(buffer.data(), buffer.size());
    auto err = deserialize(ctx, pack.packet_size, pack.sequence_number);
    BOOST_ASSERT(err == deserialize_errc::ok);
    boost::ignore_unused(err);
    return frame_header{pack.packet_size.value, pack.sequence_number};
}

// OK packets
boost::mysql::error_code boost::mysql::detail::deserialize_ok_packet(
    span<const std::uint8_t> msg,
    ok_view& output
) noexcept
{
    struct ok_packet
    {
        // header: int<1>     header     0x00 or 0xFE the OK packet header
        int_lenenc affected_rows;
        int_lenenc last_insert_id;
        std::uint16_t status_flags;  // server_status_flags
        std::uint16_t warnings;
        // CLIENT_SESSION_TRACK: not implemented
        string_lenenc info;
    } pack{};

    deserialization_context ctx(msg);
    auto err = deserialize(ctx, pack.affected_rows, pack.last_insert_id, pack.status_flags, pack.warnings);
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    if (ctx.enough_size(1))  // message is optional, may be omitted
    {
        err = deserialize(ctx, pack.info);
        if (err != deserialize_errc::ok)
            return to_error_code(err);
    }

    output = {
        pack.affected_rows.value,
        pack.last_insert_id.value,
        pack.status_flags,
        pack.warnings,
        pack.info.value,
    };

    return ctx.check_extra_bytes();
}

// Error packets
boost::mysql::error_code boost::mysql::detail::deserialize_error_packet(
    span<const std::uint8_t> msg,
    err_view& output,
    bool has_sql_state
) noexcept
{
    struct err_packet
    {
        // int<1>     header     0xFF ERR packet header
        std::uint16_t error_code;
        // if capabilities & CLIENT_PROTOCOL_41 {  (modeled here as has_sql_state)
        string_fixed<1> sql_state_marker;
        string_fixed<5> sql_state;
        // }
        string_eof error_message;
    } pack{};

    deserialization_context ctx(msg);
    auto err = has_sql_state ? deserialize(
                                   ctx,
                                   pack.error_code,
                                   pack.sql_state_marker,
                                   pack.sql_state,
                                   pack.error_message
                               )
                             : deserialize(ctx, pack.error_code, pack.error_message);
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    output = err_view{
        pack.error_code,
        pack.error_message.value,
    };

    return ctx.check_extra_bytes();
}

boost::mysql::error_code boost::mysql::detail::process_error_packet(
    span<const std::uint8_t> msg,
    db_flavor flavor,
    diagnostics& diag,
    bool has_sql_state
)
{
    err_view error_packet{};
    auto err = deserialize_error_packet(msg, error_packet, has_sql_state);
    if (err)
        return err;

    // Error message
    access::get_impl(diag).assign_server(error_packet.error_message);

    // Error code
    if (common_error_to_string(error_packet.error_code))
    {
        // This is an error shared between MySQL and MariaDB, represented as a common_server_errc.
        // get_common_error_message will check that the code has a common_server_errc representation
        // (the common error range has "holes" because of removed error codes)
        return static_cast<common_server_errc>(error_packet.error_code);
    }
    else
    {
        // This is a MySQL or MariaDB specific code. There is no fixed list of error codes,
        // as they both keep adding more codes, so no validation happens.
        const auto& cat = flavor == db_flavor::mysql ? get_mysql_server_category()
                                                     : get_mariadb_server_category();
        return error_code(error_packet.error_code, cat);
    }
}

// Column definition
boost::mysql::error_code boost::mysql::detail::deserialize_column_definition(
    span<const std::uint8_t> input,
    coldef_view& output
) noexcept
{
    deserialization_context ctx(input);

    struct column_definition_packet
    {
        string_lenenc catalog;    // always "def"
        string_lenenc schema;     // database
        string_lenenc table;      // virtual table
        string_lenenc org_table;  // physical table
        string_lenenc name;       // virtual column name
        string_lenenc org_name;   // physical column name
        string_lenenc fixed_fields;
    } pack{};

    // pack.fixed_fields itself is a structure like this.
    // The proto allows for extensibility here - adding fields just increasing fixed_fields.length
    struct fixed_fields_pack
    {
        std::uint16_t character_set;  // collation id, somehow named character_set in the protocol docs
        std::uint32_t column_length;  // maximum length of the field
        protocol_field_type type;     // type of the column as defined in enum_field_types
        std::uint16_t flags;          // Flags as defined in Column Definition Flags
        std::uint8_t decimals;        // max shown decimal digits. 0x00 for int/static strings; 0x1f for
                                      // dynamic strings, double, float
    } fixed_fields{};

    // Deserialize the main structure
    auto err = deserialize(
        ctx,
        pack.catalog,
        pack.schema,
        pack.table,
        pack.org_table,
        pack.name,
        pack.org_name,
        pack.fixed_fields
    );
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    // Deserialize the fixed_fields structure.
    // Intentionally not checking for extra bytes here, since there may be unknown fields that should just get
    // ignored
    deserialization_context subctx(
        reinterpret_cast<const std::uint8_t*>(pack.fixed_fields.value.data()),
        pack.fixed_fields.value.size()
    );
    err = deserialize(
        subctx,
        fixed_fields.character_set,
        fixed_fields.column_length,
        fixed_fields.type,
        fixed_fields.flags,
        fixed_fields.decimals
    );
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    // Compose output
    output = coldef_view{
        pack.schema.value,
        pack.table.value,
        pack.org_table.value,
        pack.name.value,
        pack.org_name.value,
        fixed_fields.character_set,
        fixed_fields.column_length,
        compute_column_type(fixed_fields.type, fixed_fields.flags, fixed_fields.character_set),
        fixed_fields.flags,
        fixed_fields.decimals,
    };

    return ctx.check_extra_bytes();
}

// quit
std::size_t boost::mysql::detail::quit_command::get_size() const noexcept { return 1u; }
void boost::mysql::detail::quit_command::serialize(span<std::uint8_t> buff) const noexcept
{
    serialize_command_id(buff, 0x01);
}

// ping
std::size_t boost::mysql::detail::ping_command::get_size() const noexcept { return 1u; }
void boost::mysql::detail::ping_command::serialize(span<std::uint8_t> buff) const noexcept
{
    serialize_command_id(buff, 0x0e);
}

// reset connection
std::size_t boost::mysql::detail::reset_connection_command::get_size() const noexcept { return 1u; }
void boost::mysql::detail::reset_connection_command::serialize(span<std::uint8_t> buff) const noexcept
{
    serialize_command_id(buff, 0x1f);
}

boost::mysql::error_code boost::mysql::detail::deserialize_ok_response(
    span<const std::uint8_t> message,
    db_flavor flavor,
    diagnostics& diag,
    bool& backslash_escapes
)
{
    // Header
    std::uint8_t header{};
    deserialization_context ctx(message);
    auto err = to_error_code(deserialize(ctx, header));
    if (err)
        return err;

    if (header == ok_packet_header)
    {
        // Verify that the ok_packet is correct
        ok_view ok{};
        err = deserialize_ok_packet(ctx.to_span(), ok);
        if (err)
            return err;
        backslash_escapes = ok.backslash_escapes();
        return error_code();
    }
    else if (header == error_packet_header)
    {
        // Theoretically, the server can answer with an error packet, too
        return process_error_packet(ctx.to_span(), flavor, diag);
    }
    else
    {
        // Invalid message
        return client_errc::protocol_value_error;
    }
}

// query
std::size_t boost::mysql::detail::query_command::get_size() const noexcept
{
    return ::boost::mysql::detail::get_size(string_eof{query}) + 1;  // command ID
}
void boost::mysql::detail::query_command::serialize(span<std::uint8_t> buff) const noexcept
{
    constexpr std::uint8_t command_id = 0x03;

    BOOST_ASSERT(buff.size() >= get_size());
    serialization_context ctx(buff.data());
    ::boost::mysql::detail::serialize(ctx, command_id, string_eof{query});
}

// prepare statement
std::size_t boost::mysql::detail::prepare_stmt_command::get_size() const noexcept
{
    return ::boost::mysql::detail::get_size(string_eof{stmt}) + 1;  // command ID
}
void boost::mysql::detail::prepare_stmt_command::serialize(span<std::uint8_t> buff) const noexcept
{
    constexpr std::uint8_t command_id = 0x16;

    BOOST_ASSERT(buff.size() >= get_size());
    serialization_context ctx(buff.data());
    ::boost::mysql::detail::serialize(ctx, command_id, string_eof{stmt});
}

boost::mysql::error_code boost::mysql::detail::deserialize_prepare_stmt_response_impl(
    span<const std::uint8_t> message,
    prepare_stmt_response& output
) noexcept
{
    struct com_stmt_prepare_ok_packet
    {
        // std::uint8_t status: must be 0
        std::uint32_t statement_id;
        std::uint16_t num_columns;
        std::uint16_t num_params;
        std::uint8_t reserved_1;  // must be 0
        std::uint16_t warning_count;
        // std::uint8_t metadata_follows when CLIENT_OPTIONAL_RESULTSET_METADATA: not implemented
    } pack{};

    deserialization_context ctx(message);

    auto err = deserialize(
        ctx,
        pack.statement_id,
        pack.num_columns,
        pack.num_params,
        pack.reserved_1,
        pack.warning_count
    );
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    output = prepare_stmt_response{
        pack.statement_id,
        pack.num_columns,
        pack.num_params,
    };

    return ctx.check_extra_bytes();
}

boost::mysql::error_code boost::mysql::detail::deserialize_prepare_stmt_response(
    span<const std::uint8_t> message,
    db_flavor flavor,
    prepare_stmt_response& output,
    diagnostics& diag
)
{
    deserialization_context ctx(message);
    std::uint8_t msg_type = 0;
    auto err = to_error_code(deserialize(ctx, msg_type));
    if (err)
        return err;

    if (msg_type == error_packet_header)
    {
        return process_error_packet(ctx.to_span(), flavor, diag);
    }
    else if (msg_type != 0)
    {
        return client_errc::protocol_value_error;
    }
    else
    {
        return deserialize_prepare_stmt_response_impl(ctx.to_span(), output);
    }
}

// execute statement
// The wire layout is as follows:
//  command ID
//  std::uint32_t statement_id;
//  std::uint8_t flags;
//  std::uint32_t iteration_count;
//  if num_params > 0:
//      NULL bitmap
//      std::uint8_t new_params_bind_flag;
//      array<meta_packet, num_params> meta;
//          protocol_field_type type;
//          std::uint8_t unsigned_flag;
//      array<field_view, num_params> params;
std::size_t boost::mysql::detail::execute_stmt_command::get_size() const noexcept
{
    constexpr std::size_t param_meta_packet_size = 2;           // type + unsigned flag
    constexpr std::size_t stmt_execute_packet_head_size = 1     // command ID
                                                          + 4   // statement_id
                                                          + 1   // flags
                                                          + 4;  // iteration_count
    std::size_t res = stmt_execute_packet_head_size;
    auto num_params = params.size();
    if (num_params > 0u)
    {
        res += null_bitmap_traits(stmt_execute_null_bitmap_offset, num_params).byte_count();
        res += 1;  // new_params_bind_flag
        res += param_meta_packet_size * num_params;
        for (field_view param : params)
        {
            res += ::boost::mysql::detail::get_size(param);
        }
    }

    return res;
}

void boost::mysql::detail::execute_stmt_command::serialize(span<std::uint8_t> buff) const noexcept
{
    constexpr std::uint8_t command_id = 0x17;

    serialization_context ctx(buff.data());
    BOOST_ASSERT(buff.size() >= get_size());

    std::uint8_t flags = 0;
    std::uint32_t iteration_count = 1;
    std::uint8_t new_params_bind_flag = 1;

    ::boost::mysql::detail::serialize(ctx, command_id, this->statement_id, flags, iteration_count);

    // Number of parameters
    auto num_params = params.size();

    if (num_params > 0)
    {
        // NULL bitmap
        null_bitmap_traits traits(stmt_execute_null_bitmap_offset, num_params);
        std::memset(ctx.first(), 0, traits.byte_count());  // Initialize to zeroes
        for (std::size_t i = 0; i < num_params; ++i)
        {
            if (params[i].is_null())
            {
                traits.set_null(ctx.first(), i);
            }
        }
        ctx.advance(traits.byte_count());

        // new parameters bind flag
        ::boost::mysql::detail::serialize(ctx, new_params_bind_flag);

        // value metadata
        for (field_view param : params)
        {
            protocol_field_type type = get_protocol_field_type(param);
            std::uint8_t unsigned_flag = param.is_uint64() ? std::uint8_t(0x80) : std::uint8_t(0);
            ::boost::mysql::detail::serialize(ctx, type, unsigned_flag);
        }

        // actual values
        for (field_view param : params)
        {
            ::boost::mysql::detail::serialize(ctx, param);
        }
    }
}

// close statement
std::size_t boost::mysql::detail::close_stmt_command::get_size() const noexcept { return 5u; }

void boost::mysql::detail::close_stmt_command::serialize(span<std::uint8_t> buff) const noexcept
{
    constexpr std::uint8_t command_id = 0x19;

    serialization_context ctx(buff.data());
    BOOST_ASSERT(buff.size() >= get_size());

    ::boost::mysql::detail::serialize(ctx, command_id, statement_id);
}

// execute response
boost::mysql::detail::execute_response boost::mysql::detail::deserialize_execute_response(
    span<const std::uint8_t> msg,
    db_flavor flavor,
    diagnostics& diag
) noexcept
{
    // Response may be: ok_packet, err_packet, local infile request (not implemented)
    // If it is none of this, then the message type itself is the beginning of
    // a length-encoded int containing the field count
    deserialization_context ctx(msg);
    std::uint8_t msg_type = 0;
    auto err = to_error_code(deserialize(ctx, msg_type));
    if (err)
        return err;

    if (msg_type == ok_packet_header)
    {
        ok_view ok{};
        err = deserialize_ok_packet(ctx.to_span(), ok);
        if (err)
            return err;
        return ok;
    }
    else if (msg_type == error_packet_header)
    {
        return process_error_packet(ctx.to_span(), flavor, diag);
    }
    else
    {
        // Resultset with metadata. First packet is an int_lenenc with
        // the number of field definitions to expect. Message type is part
        // of this packet, so we must rewind the context
        ctx.rewind(1);
        int_lenenc num_fields{};
        err = to_error_code(deserialize(ctx, num_fields));
        if (err)
            return err;
        err = ctx.check_extra_bytes();
        if (err)
            return err;

        // We should have at least one field.
        // The max number of fields is some value around 1024. For simplicity/extensibility,
        // we accept anything less than 0xffff
        if (num_fields.value == 0 || num_fields.value > 0xffffu)
        {
            return make_error_code(client_errc::protocol_value_error);
        }

        return static_cast<std::size_t>(num_fields.value);
    }
}

boost::mysql::detail::row_message boost::mysql::detail::deserialize_row_message(
    span<const std::uint8_t> msg,
    db_flavor flavor,
    diagnostics& diag
)
{
    // Message type: row, error or eof?
    std::uint8_t msg_type = 0;
    deserialization_context ctx(msg);
    auto deser_errc = deserialize(ctx, msg_type);
    if (deser_errc != deserialize_errc::ok)
    {
        return to_error_code(deser_errc);
    }

    if (msg_type == eof_packet_header)
    {
        // end of resultset => this is a ok_packet, not a row
        ok_view ok{};
        auto err = deserialize_ok_packet(ctx.to_span(), ok);
        if (err)
            return err;
        return ok;
    }
    else if (msg_type == error_packet_header)
    {
        // An error occurred during the generation of the rows
        return process_error_packet(ctx.to_span(), flavor, diag);
    }
    else
    {
        // An actual row
        ctx.rewind(1);  // keep the 'message type' byte, as it is part of the actual message
        return span<const std::uint8_t>(ctx.first(), ctx.size());
    }
}

// Deserialize row
namespace boost {
namespace mysql {
namespace detail {

BOOST_MYSQL_STATIC_OR_INLINE
bool is_next_field_null(const deserialization_context& ctx) noexcept
{
    if (!ctx.enough_size(1))
        return false;
    return *ctx.first() == 0xfb;
}

BOOST_MYSQL_STATIC_OR_INLINE
error_code deserialize_text_row(
    deserialization_context& ctx,
    metadata_collection_view meta,
    field_view* output
)
{
    for (std::vector<field_view>::size_type i = 0; i < meta.size(); ++i)
    {
        if (is_next_field_null(ctx))
        {
            ctx.advance(1);
            output[i] = field_view(nullptr);
        }
        else
        {
            string_lenenc value_str;
            auto err = deserialize(ctx, value_str);
            if (err != deserialize_errc::ok)
                return to_error_code(err);
            err = deserialize_text_field(value_str.value, meta[i], output[i]);
            if (err != deserialize_errc::ok)
                return to_error_code(err);
        }
    }
    if (!ctx.empty())
        return client_errc::extra_bytes;
    return error_code();
}

BOOST_MYSQL_STATIC_OR_INLINE
error_code deserialize_binary_row(
    deserialization_context& ctx,
    metadata_collection_view meta,
    field_view* output
)
{
    // Skip packet header (it is not part of the message in the binary
    // protocol but it is in the text protocol, so we include it for homogeneity)
    if (!ctx.enough_size(1))
        return client_errc::incomplete_message;
    ctx.advance(1);

    // Number of fields
    std::size_t num_fields = meta.size();

    // Null bitmap
    null_bitmap_traits null_bitmap(binary_row_null_bitmap_offset, num_fields);
    const std::uint8_t* null_bitmap_begin = ctx.first();
    if (!ctx.enough_size(null_bitmap.byte_count()))
        return client_errc::incomplete_message;
    ctx.advance(null_bitmap.byte_count());

    // Actual values
    for (std::vector<field_view>::size_type i = 0; i < num_fields; ++i)
    {
        if (null_bitmap.is_null(null_bitmap_begin, i))
        {
            output[i] = field_view(nullptr);
        }
        else
        {
            auto err = deserialize_binary_field(ctx, meta[i], output[i]);
            if (err != deserialize_errc::ok)
                return to_error_code(err);
        }
    }

    // Check for remaining bytes
    if (!ctx.empty())
        return make_error_code(client_errc::extra_bytes);

    return error_code();
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

boost::mysql::error_code boost::mysql::detail::deserialize_row(
    resultset_encoding encoding,
    span<const std::uint8_t> buff,
    metadata_collection_view meta,
    span<field_view> output
)
{
    BOOST_ASSERT(meta.size() == output.size());
    deserialization_context ctx(buff);
    return encoding == detail::resultset_encoding::text ? deserialize_text_row(ctx, meta, output.data())
                                                        : deserialize_binary_row(ctx, meta, output.data());
}

// Server hello
namespace boost {
namespace mysql {
namespace detail {

BOOST_MYSQL_STATIC_OR_INLINE
capabilities compose_capabilities(string_fixed<2> low, string_fixed<2> high) noexcept
{
    std::uint32_t res = 0;
    auto capabilities_begin = reinterpret_cast<std::uint8_t*>(&res);
    memcpy(capabilities_begin, low.value.data(), 2);
    memcpy(capabilities_begin + 2, high.value.data(), 2);
    return capabilities(boost::endian::little_to_native(res));
}

BOOST_MYSQL_STATIC_OR_INLINE
db_flavor parse_db_version(string_view version_string) noexcept
{
    return version_string.find("MariaDB") != string_view::npos ? db_flavor::mariadb : db_flavor::mysql;
}

BOOST_MYSQL_STATIC_IF_COMPILED
constexpr std::uint8_t server_hello_auth1_length = 8;

}  // namespace detail
}  // namespace mysql
}  // namespace boost

boost::mysql::error_code boost::mysql::detail::deserialize_server_hello_impl(
    span<const std::uint8_t> msg,
    server_hello& output
)
{
    struct server_hello_packet
    {
        // int<1>     protocol version     Always 10
        string_null server_version;
        std::uint32_t connection_id;
        string_fixed<server_hello_auth1_length> auth_plugin_data_part_1;
        std::uint8_t filler;  // should be 0
        string_fixed<2> capability_flags_low;
        std::uint8_t character_set;  // default server a_protocol_character_set, only the lower 8-bits
        std::uint16_t status_flags;  // server_status_flags
        string_fixed<2> capability_flags_high;
        std::uint8_t auth_plugin_data_len;
        string_fixed<10> reserved;
        // auth plugin data, 2nd part. This has a weird representation that doesn't fit any defined type
        string_null auth_plugin_name;
    } pack{};

    deserialization_context ctx(msg);

    auto err = deserialize(
        ctx,
        pack.server_version,
        pack.connection_id,
        pack.auth_plugin_data_part_1,
        pack.filler,
        pack.capability_flags_low,
        pack.character_set,
        pack.status_flags,
        pack.capability_flags_high
    );
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    // Compose capabilities
    auto cap = compose_capabilities(pack.capability_flags_low, pack.capability_flags_high);

    // Check minimum server capabilities to deserialize this frame
    if (!cap.has(CLIENT_PLUGIN_AUTH))
        return client_errc::server_unsupported;

    // Deserialize next fields
    err = deserialize(ctx, pack.auth_plugin_data_len, pack.reserved);
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    // Auth plugin data, second part
    auto auth2_length = static_cast<std::uint8_t>(
        (std::max)(13, pack.auth_plugin_data_len - server_hello_auth1_length)
    );
    const void* auth2_data = ctx.first();
    if (!ctx.enough_size(auth2_length))
        return client_errc::incomplete_message;
    ctx.advance(auth2_length);

    // Auth plugin name
    err = deserialize(ctx, pack.auth_plugin_name);
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    // Compose output
    output.server = parse_db_version(pack.server_version.value);
    output.server_capabilities = cap;
    output.auth_plugin_name = pack.auth_plugin_name.value;

    // Compose auth_plugin_data
    output.auth_plugin_data.clear();
    output.auth_plugin_data.append(pack.auth_plugin_data_part_1.value.data(), server_hello_auth1_length);
    output.auth_plugin_data.append(auth2_data,
                                   auth2_length - 1);  // discard an extra trailing NULL byte

    return ctx.check_extra_bytes();
}

boost::mysql::error_code boost::mysql::detail::deserialize_server_hello(
    span<const std::uint8_t> msg,
    server_hello& output,
    diagnostics& diag
)
{
    deserialization_context ctx(msg);

    // Message type
    std::uint8_t msg_type = 0;
    auto err = to_error_code(deserialize(ctx, msg_type));
    if (err)
        return err;
    if (msg_type == handshake_protocol_version_9)
    {
        return make_error_code(client_errc::server_unsupported);
    }
    else if (msg_type == error_packet_header)
    {
        // We don't know which DB is yet. The server has no knowledge of our capabilities
        // yet, so it will assume we don't support the 4.1 protocol and send an error
        // packet without SQL state
        return process_error_packet(ctx.to_span(), db_flavor::mysql, diag, false);
    }
    else if (msg_type != handshake_protocol_version_10)
    {
        return make_error_code(client_errc::protocol_value_error);
    }
    else
    {
        return deserialize_server_hello_impl(ctx.to_span(), output);
    }
}

// Login request
namespace boost {
namespace mysql {
namespace detail {

BOOST_MYSQL_STATIC_OR_INLINE
std::uint8_t get_collation_first_byte(std::uint32_t collation_id) noexcept
{
    return static_cast<std::uint8_t>(collation_id % 0xff);
}

struct login_request_packet
{
    std::uint32_t client_flag;  // capabilities
    std::uint32_t max_packet_size;
    std::uint8_t character_set;  // collation ID first byte
    string_fixed<23> filler;     //     All 0s.
    string_null username;
    string_lenenc auth_response;     // we require CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA
    string_null database;            // only to be serialized if CLIENT_CONNECT_WITH_DB
    string_null client_plugin_name;  // we require CLIENT_PLUGIN_AUTH
    // CLIENT_CONNECT_ATTRS: not implemented
};

BOOST_MYSQL_STATIC_OR_INLINE
login_request_packet to_packet(const login_request& req) noexcept
{
    return {
        req.negotiated_capabilities.get(),
        req.max_packet_size,
        get_collation_first_byte(req.collation_id),
        {},
        string_null{req.username},
        string_lenenc{to_string(req.auth_response)},
        string_null{req.database},
        string_null{req.auth_plugin_name},
    };
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

std::size_t boost::mysql::detail::login_request::get_size() const noexcept
{
    auto pack = to_packet(*this);
    return ::boost::mysql::detail::get_size(
               pack.client_flag,
               pack.max_packet_size,
               pack.character_set,
               pack.filler,
               pack.username,
               pack.auth_response
           ) +
           (negotiated_capabilities.has(CLIENT_CONNECT_WITH_DB)
                ? ::boost::mysql::detail::get_size(pack.database)
                : 0) +
           ::boost::mysql::detail::get_size(pack.client_plugin_name);
}

void boost::mysql::detail::login_request::serialize(span<std::uint8_t> buff) const noexcept
{
    BOOST_ASSERT(buff.size() >= get_size());
    serialization_context ctx(buff.data());

    auto pack = to_packet(*this);
    ::boost::mysql::detail::serialize(
        ctx,
        pack.client_flag,
        pack.max_packet_size,
        pack.character_set,
        pack.filler,
        pack.username,
        pack.auth_response
    );
    if (negotiated_capabilities.has(CLIENT_CONNECT_WITH_DB))
    {
        ::boost::mysql::detail::serialize(ctx, pack.database);
    }
    ::boost::mysql::detail::serialize(ctx, pack.client_plugin_name);
}

// ssl_request
std::size_t boost::mysql::detail::ssl_request::get_size() const noexcept { return 4 + 4 + 1 + 23; }

void boost::mysql::detail::ssl_request::serialize(span<std::uint8_t> buff) const noexcept
{
    BOOST_ASSERT(buff.size() >= get_size());
    serialization_context ctx(buff.data());

    struct ssl_request_packet
    {
        std::uint32_t client_flag;
        std::uint32_t max_packet_size;
        std::uint8_t character_set;
        string_fixed<23> filler;
    } pack{
        negotiated_capabilities.get(),
        max_packet_size,
        get_collation_first_byte(collation_id),
        {},
    };

    ::boost::mysql::detail::serialize(
        ctx,
        pack.client_flag,
        pack.max_packet_size,
        pack.character_set,
        pack.filler
    );
}

// auth_switch
BOOST_ATTRIBUTE_NODISCARD
boost::mysql::error_code boost::mysql::detail::deserialize_auth_switch(
    span<const std::uint8_t> msg,
    auth_switch& output
) noexcept
{
    struct auth_switch_request_packet
    {
        string_null plugin_name;
        string_eof auth_plugin_data;
    } pack{};

    deserialization_context ctx(msg);

    auto err = deserialize(ctx, pack.plugin_name, pack.auth_plugin_data);
    if (err != deserialize_errc::ok)
        return to_error_code(err);

    // Discard an additional NULL at the end of auth data
    string_view auth_data = pack.auth_plugin_data.value;
    if (!auth_data.empty() && auth_data.back() == 0)
    {
        auth_data = auth_data.substr(0, auth_data.size() - 1);
    }

    output = {
        pack.plugin_name.value,
        to_span(auth_data),
    };

    return ctx.check_extra_bytes();
}

boost::mysql::detail::handhake_server_response boost::mysql::detail::deserialize_handshake_server_response(
    span<const std::uint8_t> buff,
    db_flavor flavor,
    diagnostics& diag
)
{
    deserialization_context ctx(buff);
    std::uint8_t msg_type = 0;
    auto err = to_error_code(deserialize(ctx, msg_type));
    if (err)
        return err;

    if (msg_type == ok_packet_header)
    {
        ok_view ok{};
        err = deserialize_ok_packet(ctx.to_span(), ok);
        if (err)
            return err;
        return ok;
    }
    else if (msg_type == error_packet_header)
    {
        return process_error_packet(ctx.to_span(), flavor, diag);
    }
    else if (msg_type == auth_switch_request_header)
    {
        // We have received an auth switch request. Deserialize it
        auth_switch auth_sw{};
        err = deserialize_auth_switch(ctx.to_span(), auth_sw);
        if (err)
            return err;
        return auth_sw;
    }
    else if (msg_type == auth_more_data_header)
    {
        // We have received an auth more data request. Deserialize it.
        // Note that string_eof never fails deserialization (by definition)
        string_eof auth_more_data;
        auto ec = deserialize(ctx, auth_more_data);
        BOOST_ASSERT(ec == deserialize_errc::ok);
        boost::ignore_unused(ec);

        // If the special value fast_auth_complete_challenge
        // is received as auth data, it means that the auth is complete
        // but we must wait for another OK message. We consider this
        // a special type of message
        string_view challenge = auth_more_data.value;
        if (challenge == fast_auth_complete_challenge)
        {
            return handhake_server_response::ok_follows_t();
        }

        // Otherwise, just return the normal data
        return handhake_server_response(to_span(challenge));
    }
    else
    {
        // Unknown message type
        return make_error_code(client_errc::protocol_value_error);
    }
}

std::size_t boost::mysql::detail::auth_switch_response::get_size() const noexcept
{
    return ::boost::mysql::detail::get_size(string_eof{to_string(auth_plugin_data)});
}

void boost::mysql::detail::auth_switch_response::serialize(span<std::uint8_t> buff) const noexcept
{
    BOOST_ASSERT(buff.size() >= get_size());
    serialization_context ctx(buff.data());
    ::boost::mysql::detail::serialize(ctx, string_eof{to_string(auth_plugin_data)});
}

#endif
