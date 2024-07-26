//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_CONNECTION_STATE_DATA_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_CONNECTION_STATE_DATA_HPP

#include <boost/mysql/character_set.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata_mode.hpp>

#include <boost/mysql/impl/internal/protocol/capabilities.hpp>
#include <boost/mysql/impl/internal/protocol/db_flavor.hpp>
#include <boost/mysql/impl/internal/protocol/protocol.hpp>
#include <boost/mysql/impl/internal/sansio/message_reader.hpp>
#include <boost/mysql/impl/internal/sansio/message_writer.hpp>

#include <cstddef>
#include <vector>

namespace boost {
namespace mysql {
namespace detail {

enum class ssl_state
{
    unsupported,
    inactive,
    active,
    torn_down,
};

struct connection_state_data
{
    // Is the connection actually connected? Set by handshake
    bool is_connected{false};

    // Are we talking to MySQL or MariaDB?
    db_flavor flavor{db_flavor::mysql};

    // What are the connection's capabilities?
    capabilities current_capabilities;

    // Used by async ops without output diagnostics params, to avoid allocations
    diagnostics shared_diag;

    // Temporary field storage, re-used by several ops
    std::vector<field_view> shared_fields;

    // Do we want to retain metadata strings or not? Used to save allocations
    metadata_mode meta_mode{metadata_mode::minimal};

    // Is SSL supported/enabled for the current connection?
    ssl_state ssl;

    // Do backslashes represent escape sequences? By default they do, but they can
    // be disabled using a variable. OK packets include a flag with this info.
    bool backslash_escapes{true};

    // The current character set, or a default-constructed character set (will all nullptrs) if unknown
    character_set current_charset{};

    // Reader and writer
    message_reader reader;
    message_writer writer;

    bool ssl_active() const noexcept { return ssl == ssl_state::active; }
    bool supports_ssl() const noexcept { return ssl != ssl_state::unsupported; }

    const character_set* charset_ptr() const noexcept
    {
        return current_charset.name.empty() ? nullptr : &current_charset;
    }

    connection_state_data(std::size_t read_buffer_size, bool transport_supports_ssl = false)
        : ssl(transport_supports_ssl ? ssl_state::inactive : ssl_state::unsupported), reader(read_buffer_size)
    {
    }

    void reset()
    {
        is_connected = false;
        flavor = db_flavor::mysql;
        current_capabilities = capabilities();
        // Metadata mode does not get reset on handshake
        reader.reset();
        // Writer does not need reset, since every write clears previous state
        if (supports_ssl())
            ssl = ssl_state::inactive;
        backslash_escapes = true;
        current_charset = character_set{};
    }

    // Reads an OK packet from the reader. This operation is repeated in several places.
    error_code deserialize_ok(diagnostics& diag) noexcept
    {
        return deserialize_ok_response(reader.message(), flavor, diag, backslash_escapes);
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
