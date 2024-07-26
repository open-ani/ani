//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_ANY_STREAM_HPP
#define BOOST_MYSQL_DETAIL_ANY_STREAM_HPP

#include <boost/mysql/error_code.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/asio/any_completion_handler.hpp>
#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/buffer.hpp>

#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

class any_stream
{
    bool supports_ssl_;

public:
    using executor_type = asio::any_io_executor;

    any_stream(bool supports_ssl) noexcept : supports_ssl_(supports_ssl) {}

    bool supports_ssl() const noexcept { return supports_ssl_; }

    virtual ~any_stream() {}
    virtual executor_type get_executor() = 0;

    // SSL
    virtual void handshake(error_code& ec) = 0;
    virtual void async_handshake(asio::any_completion_handler<void(error_code)>) = 0;
    virtual void shutdown(error_code& ec) = 0;
    virtual void async_shutdown(asio::any_completion_handler<void(error_code)>) = 0;

    // Reading
    virtual std::size_t read_some(asio::mutable_buffer, bool use_ssl, error_code& ec) = 0;
    virtual void async_read_some(asio::mutable_buffer, bool use_ssl, asio::any_completion_handler<void(error_code, std::size_t)>) = 0;

    // Writing
    virtual std::size_t write_some(asio::const_buffer, bool use_ssl, error_code& ec) = 0;
    virtual void async_write_some(asio::const_buffer, bool use_ssl, asio::any_completion_handler<void(error_code, std::size_t)>) = 0;

    // Connect and close
    virtual void set_endpoint(const void*) = 0;
    virtual void connect(error_code& ec) = 0;
    virtual void async_connect(asio::any_completion_handler<void(error_code)>) = 0;
    virtual void close(error_code& ec) = 0;
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
