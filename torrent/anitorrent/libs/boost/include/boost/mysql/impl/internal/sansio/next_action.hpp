//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_NEXT_ACTION_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_NEXT_ACTION_HPP

#include <boost/mysql/error_code.hpp>

#include <boost/mysql/impl/internal/sansio/read_buffer.hpp>

#include <cstddef>
#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

class next_action
{
public:
    enum class type_t
    {
        none,
        write,
        read,
        ssl_handshake,
        ssl_shutdown,
        connect,
        close,
    };

    struct read_args_t
    {
        span<std::uint8_t> buffer;
        bool use_ssl;
    };

    struct write_args_t
    {
        span<const std::uint8_t> buffer;
        bool use_ssl;
    };

    next_action(error_code ec = {}) noexcept : type_(type_t::none), data_(ec) {}

    // Type
    type_t type() const noexcept { return type_; }
    bool is_done() const noexcept { return type_ == type_t::none; }
    bool success() const noexcept { return is_done() && !data_.ec; }

    // Arguments
    error_code error() const noexcept
    {
        BOOST_ASSERT(is_done());
        return data_.ec;
    }
    read_args_t read_args() const noexcept
    {
        BOOST_ASSERT(type_ == type_t::read);
        return data_.read_args;
    }
    write_args_t write_args() const noexcept
    {
        BOOST_ASSERT(type_ == type_t::write);
        return data_.write_args;
    }

    static next_action connect() noexcept { return next_action(type_t::connect, data_t()); }
    static next_action read(read_args_t args) noexcept { return next_action(type_t::read, args); }
    static next_action write(write_args_t args) noexcept { return next_action(type_t::write, args); }
    static next_action ssl_handshake() noexcept { return next_action(type_t::ssl_handshake, data_t()); }
    static next_action ssl_shutdown() noexcept { return next_action(type_t::ssl_shutdown, data_t()); }
    static next_action close() noexcept { return next_action(type_t::close, data_t()); }

private:
    type_t type_{type_t::none};
    union data_t
    {
        error_code ec;
        read_args_t read_args;
        write_args_t write_args;

        data_t() noexcept : ec(error_code()) {}
        data_t(error_code ec) noexcept : ec(ec) {}
        data_t(read_args_t args) noexcept : read_args(args) {}
        data_t(write_args_t args) noexcept : write_args(args) {}
    } data_;

    next_action(type_t t, data_t data) noexcept : type_(t), data_(data) {}
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
