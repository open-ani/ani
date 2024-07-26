//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SSL_CONTEXT_WITH_DEFAULT_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SSL_CONTEXT_WITH_DEFAULT_HPP

#include <boost/asio/ssl/context.hpp>
#include <boost/variant2/variant.hpp>

namespace boost {
namespace mysql {
namespace detail {

inline asio::ssl::context create_default_ssl_context()
{
    // As of MySQL 5.7.35, support for previous TLS versions is deprecated,
    // so this is a secure default. User can override it if they want
    asio::ssl::context ctx(asio::ssl::context::tlsv12_client);
    return ctx;
}

inline asio::ssl::context& default_ssl_context()
{
    static asio::ssl::context ctx = create_default_ssl_context();
    return ctx;
}

class ssl_context_with_default
{
    asio::ssl::context* impl_;

public:
    ssl_context_with_default(asio::ssl::context* ctx) noexcept : impl_(ctx) {}

    asio::ssl::context& get()
    {
        if (impl_ == nullptr)
            impl_ = &default_ssl_context();
        return *impl_;
    }

    // Exposed for the sake of testing
    const asio::ssl::context* get_ptr() const noexcept { return impl_; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
