//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_AUTH_AUTH_IPP
#define BOOST_MYSQL_IMPL_INTERNAL_AUTH_AUTH_IPP

#pragma once

#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/make_string_view.hpp>

#include <boost/mysql/impl/internal/auth/auth.hpp>

#include <algorithm>
#include <cstring>
#include <openssl/sha.h>

namespace boost {
namespace mysql {
namespace detail {

// mysql_native_password
// Authorization for this plugin is always challenge (nonce) -> response
// (hashed password).

BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::size_t mnp_challenge_length = 20;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::size_t mnp_response_length = 20;

// challenge must point to challenge_length bytes of data
// output must point to response_length bytes of data
// SHA1( password ) XOR SHA1( "20-bytes random data from server" <concat> SHA1( SHA1( password ) ) )
BOOST_MYSQL_STATIC_OR_INLINE
void mnp_compute_auth_string(string_view password, const void* challenge, void* output)
{
    // SHA1 (password)
    using sha1_buffer = unsigned char[SHA_DIGEST_LENGTH];
    sha1_buffer password_sha1;
    SHA1(reinterpret_cast<const unsigned char*>(password.data()), password.size(), password_sha1);

    // Add server challenge (salt)
    unsigned char salted_buffer[mnp_challenge_length + SHA_DIGEST_LENGTH];
    memcpy(salted_buffer, challenge, mnp_challenge_length);
    SHA1(password_sha1, sizeof(password_sha1), salted_buffer + 20);
    sha1_buffer salted_sha1;
    SHA1(salted_buffer, sizeof(salted_buffer), salted_sha1);

    // XOR
    static_assert(mnp_response_length == SHA_DIGEST_LENGTH, "Buffer size mismatch");
    for (std::size_t i = 0; i < SHA_DIGEST_LENGTH; ++i)
    {
        static_cast<std::uint8_t*>(output)[i] = password_sha1[i] ^ salted_sha1[i];
    }
}

BOOST_MYSQL_STATIC_OR_INLINE
error_code mnp_compute_response(
    string_view password,
    boost::span<const std::uint8_t> challenge,
    bool,  // secure_channel
    std::vector<std::uint8_t>& output
)
{
    // Check challenge size
    if (challenge.size() != mnp_challenge_length)
    {
        return make_error_code(client_errc::protocol_value_error);
    }

    // Do the calculation
    output.resize(mnp_response_length);
    mnp_compute_auth_string(password, challenge.data(), output.data());
    return error_code();
}

// caching_sha2_password
// Authorization for this plugin may be cleartext password or challenge/response.
// The server has a cache that uses when employing challenge/response. When
// the server sends a challenge of challenge_length bytes, we should send
// the password hashed with the challenge. The server may send a challenge
// equals to perform_full_auth, meaning it could not use the cache to
// complete the auth. In this case, we should just send the cleartext password.
// Doing the latter requires a SSL connection. It is possible to perform full
// auth without an SSL connection, but that requires the server public key,
// and we do not implement that.

BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::size_t csha2p_challenge_length = 20;
BOOST_MYSQL_STATIC_IF_COMPILED constexpr std::size_t csha2p_response_length = 32;

// challenge must point to challenge_length bytes of data
// output must point to response_length bytes of data
BOOST_MYSQL_STATIC_OR_INLINE
void csha2p_compute_auth_string(string_view password, const void* challenge, void* output)
{
    static_assert(csha2p_response_length == SHA256_DIGEST_LENGTH, "Buffer size mismatch");

    // SHA(SHA(password_sha) concat challenge) XOR password_sha
    // hash1 = SHA(pass)
    using sha_buffer = std::uint8_t[csha2p_response_length];
    sha_buffer password_sha;
    SHA256(reinterpret_cast<const unsigned char*>(password.data()), password.size(), password_sha);

    // SHA(password_sha) concat challenge = buffer
    std::uint8_t buffer[csha2p_response_length + csha2p_challenge_length];
    SHA256(password_sha, csha2p_response_length, buffer);
    std::memcpy(buffer + csha2p_response_length, challenge, csha2p_challenge_length);

    // SHA(SHA(password_sha) concat challenge) = SHA(buffer) = salted_password
    sha_buffer salted_password;
    SHA256(buffer, sizeof(buffer), salted_password);

    // salted_password XOR password_sha
    for (unsigned i = 0; i < csha2p_response_length; ++i)
    {
        static_cast<std::uint8_t*>(output)[i] = salted_password[i] ^ password_sha[i];
    }
}

BOOST_MYSQL_STATIC_OR_INLINE
bool should_perform_full_auth(boost::span<const std::uint8_t> challenge) noexcept
{
    // A challenge of "\4" means "perform full auth"
    return challenge.size() == 1u && challenge[0] == 4;
}

BOOST_MYSQL_STATIC_OR_INLINE
error_code csha2p_compute_response(
    string_view password,
    boost::span<const std::uint8_t> challenge,
    bool secure_channel,
    std::vector<std::uint8_t>& output
)
{
    if (should_perform_full_auth(challenge))
    {
        if (!secure_channel)
        {
            return make_error_code(client_errc::auth_plugin_requires_ssl);
        }
        output.assign(password.begin(), password.end());
        output.push_back(0);
        return error_code();
    }
    else
    {
        // Check challenge size
        if (challenge.size() != csha2p_challenge_length)
        {
            return make_error_code(client_errc::protocol_value_error);
        }

        // Do the calculation
        output.resize(csha2p_response_length);
        csha2p_compute_auth_string(password, challenge.data(), output.data());
        return error_code();
    }
}

// top-level API
struct authentication_plugin
{
    using calculator_signature = error_code (*)(
        string_view password,
        boost::span<const std::uint8_t> challenge,
        bool secure_channel,
        std::vector<std::uint8_t>& output
    );

    string_view name;
    calculator_signature calculator;
};

BOOST_MYSQL_STATIC_IF_COMPILED
constexpr authentication_plugin all_authentication_plugins[] = {
    {
     make_string_view("mysql_native_password"),
     &mnp_compute_response,
     },
    {
     make_string_view("caching_sha2_password"),
     &csha2p_compute_response,
     },
};

BOOST_MYSQL_STATIC_OR_INLINE
const authentication_plugin* find_plugin(string_view name)
{
    auto it = std::find_if(
        std::begin(all_authentication_plugins),
        std::end(all_authentication_plugins),
        [name](const authentication_plugin& plugin) { return plugin.name == name; }
    );
    return it == std::end(all_authentication_plugins) ? nullptr : it;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

boost::mysql::error_code boost::mysql::detail::compute_auth_response(
    string_view plugin_name,
    string_view password,
    span<const std::uint8_t> challenge,
    bool secure_channel,
    auth_response& output
)
{
    const auto* plugin = find_plugin(plugin_name);
    if (plugin)
    {
        output.plugin_name = plugin->name;

        if (password.empty())
        {
            // Blank password: we should just return an empty auth string
            output.data.clear();
            return error_code();
        }
        else
        {
            return plugin->calculator(password, challenge, secure_channel, output.data);
        }
    }
    else
    {
        return client_errc::unknown_auth_plugin;
    }
}

#endif
