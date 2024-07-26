//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_CONNECT_PARAMS_HELPERS_HPP
#define BOOST_MYSQL_DETAIL_CONNECT_PARAMS_HELPERS_HPP

#include <boost/mysql/any_address.hpp>
#include <boost/mysql/connect_params.hpp>
#include <boost/mysql/handshake_params.hpp>
#include <boost/mysql/ssl_mode.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/config.hpp>

#include <memory>

namespace boost {
namespace mysql {
namespace detail {

struct any_address_view
{
    address_type type;
    string_view address;
    unsigned short port;

    any_address_view(
        address_type t = address_type::host_and_port,
        string_view addr = {},
        unsigned short port = 0
    ) noexcept
        : type(t), address(addr), port(port)
    {
    }
};

inline any_address_view make_view(const any_address& input) noexcept
{
    const auto& impl = access::get_impl(input);
    return any_address_view(impl.type, impl.address, impl.port);
}

inline ssl_mode adjust_ssl_mode(ssl_mode input, address_type addr_type) noexcept
{
    return addr_type == address_type::host_and_port ? input : ssl_mode::disable;
}

inline handshake_params make_hparams(const connect_params& input) noexcept
{
    return handshake_params(
        input.username,
        input.password,
        input.database,
        input.connection_collation,
        adjust_ssl_mode(input.ssl, input.server_address.type()),
        input.multi_queries
    );
}

struct stable_connect_params
{
    any_address_view address;
    handshake_params hparams;
    std::unique_ptr<char[]> string_buffer;
};

BOOST_MYSQL_DECL
stable_connect_params make_stable(const connect_params& input);

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/connect_params_helpers.ipp>
#endif

#endif
