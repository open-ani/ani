//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_AUTH_AUTH_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_AUTH_AUTH_HPP

#include <boost/mysql/error_code.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/core/span.hpp>

#include <vector>

namespace boost {
namespace mysql {
namespace detail {

struct auth_response
{
    std::vector<std::uint8_t> data;
    string_view plugin_name;
};

BOOST_ATTRIBUTE_NODISCARD
BOOST_MYSQL_DECL
error_code compute_auth_response(
    string_view plugin_name,
    string_view password,
    span<const std::uint8_t> challenge,
    bool secure_channel,
    auth_response& output
);

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/internal/auth/auth.ipp>
#endif

#endif
