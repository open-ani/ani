//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_ANY_ADDRESS_HPP
#define BOOST_MYSQL_ANY_ADDRESS_HPP

#include <boost/mysql/defaults.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>

#include <string>

namespace boost {
namespace mysql {

/// (EXPERIMENTAL) The type of an address identifying a MySQL server.
enum class address_type
{
    /// An Internet hostname and a TCP port.
    host_and_port,

    /// A UNIX domain socket path.
    unix_path
};

/**
 * \brief (EXPERIMENTAL) A host and port identifying how to connect to a MySQL server.
 * \details
 * This is an owning type with value semantics.
 * \see any_address
 *
 * \par Experimental
 * This part of the API is experimental, and may change in successive
 * releases without previous notice.
 */
struct host_and_port
{
    /**
     * \brief The hostname where the MySQL server is expected to be listening.
     * \details
     * An empty string is equivalent to `localhost`. This is the default.
     * This is an owning field
     */
    std::string host;

    /// The port where the MySQL server is expected to be listening.
    unsigned short port{default_port};
};

/**
 * \brief (EXPERIMENTAL) Contains a UNIX-socket domain path.
 * \details
 * This type is defined in all systems, regardless of their UNIX socket support.
 * \n
 * This is an owning type with value semantics.
 * \see any_address
 */
struct unix_path
{
    /**
     * \brief The UNIX domain socket path where the MySQL server is listening.
     * \details Defaults to the empty string. This is an owning field.
     */
    std::string path;
};

/**
 * \brief (EXPERIMENTAL) A server address, identifying how to physically connect to a MySQL server.
 * \details
 * A variant-like type that can represent the network address of a MySQL server,
 * regardless of the transport type being used. It can contain either a host
 * and port (to connect using TCP) or a UNIX path (to connect using UNIX domain sockets).
 * \n
 * This class may be extended in the future to accomodate Windows named pipes.
 * \n
 * This type has value semantics: it is owning and regular.
 */
class any_address
{
#ifndef BOOST_MYSQL_DOXYGEN
    struct
    {
        address_type type;
        std::string address;
        unsigned short port;
    } impl_;

    any_address(address_type t, std::string&& addr, unsigned short port) noexcept
        : impl_{t, std::move(addr), port}
    {
    }
    friend struct detail::access;
#endif

public:
    /**
     * \brief Constructs an empty address.
     * \details Results in an address with `this->type() == address_type::host_and_port`,
     * `this->hostname() == ""` and `this->port() == default_port`, which identifies
     * a server running on `localhost` using the default port.
     * \par Exception safety
     * No-throw guarantee.
     */
    any_address() noexcept : any_address(address_type::host_and_port, std::string(), default_port) {}

    /**
     * \brief Copy constructor.
     * \par Exception safety
     * Strong guarantee. Exceptions may be thrown by memory allocations.
     * \par Object lifetimes
     * `*this` and `other` will have independent lifetimes (regular value semantics).
     */
    any_address(const any_address& other) = default;

    /**
     * \brief Move constructor.
     * \details Leaves `other` in a valid but unspecified state.
     * \par Exception safety
     * No-throw guarantee.
     */
    any_address(any_address&& other) = default;

    /**
     * \brief Copy assignment.
     * \par Exception safety
     * Basic guarantee. Exceptions may be thrown by memory allocations.
     * \par Object lifetimes
     * `*this` and `other` will have independent lifetimes (regular value semantics).
     */
    any_address& operator=(const any_address& other) = default;

    /**
     * \brief Move assignment.
     * \details Leaves `other` in a valid but unspecified state.
     * \par Exception safety
     * No-throw guarantee.
     */
    any_address& operator=(any_address&& other) = default;

    /// Destructor.
    ~any_address() = default;

    /**
     * \brief Constructs an address containing a host and a port.
     * \details Results in an address with `this->type() == address_type::host_and_port`,
     * `this->hostname() == value.hostname()` and `this->port() == value.port()`.
     *
     * \par Object lifetimes
     * `*this` and `value` will have independent lifetimes (regular value semantics).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    any_address(host_and_port value) noexcept
        : impl_{address_type::host_and_port, std::move(value.host), value.port}
    {
    }

    /**
     * \brief Constructs an address containing a UNIX socket path.
     * \details Results in an address with `this->type() == address_type::unix_path`,
     * `this->unix_socket_path() == value.path()`.
     *
     * \par Object lifetimes
     * `*this` and `value` will have independent lifetimes (regular value semantics).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    any_address(unix_path value) noexcept : impl_{address_type::unix_path, std::move(value.path), 0} {}

    /**
     * \brief Retrieves the type of address that this object contains.
     * \par Exception safety
     * No-throw guarantee.
     */
    address_type type() const noexcept { return impl_.type; }

    /**
     * \brief Retrieves the hostname that this object contains.
     * \par Preconditions
     * `this->type() == address_type::host_and_port`
     *
     * \par Object lifetimes
     * The returned view points into `*this`, and is valid as long as `*this`
     * is alive and hasn't been assigned to or moved from.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    string_view hostname() const noexcept
    {
        BOOST_ASSERT(type() == address_type::host_and_port);
        return impl_.address;
    }

    /**
     * \brief Retrieves the port that this object contains.
     * \par Preconditions
     * `this->type() == address_type::host_and_port`
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    unsigned short port() const noexcept
    {
        BOOST_ASSERT(type() == address_type::host_and_port);
        return impl_.port;
    }

    /**
     * \brief Retrieves the UNIX socket path that this object contains.
     * \par Preconditions
     * `this->type() == address_type::unix_path`
     *
     * \par Object lifetimes
     * The returned view points into `*this`, and is valid as long as `*this`
     * is alive and hasn't been assigned to or moved from.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    string_view unix_socket_path() const noexcept
    {
        BOOST_ASSERT(type() == address_type::unix_path);
        return impl_.address;
    }

    /**
     * \brief Replaces the current object with a host and port.
     * \details
     * Destroys the current contained object and constructs a new
     * host and port from the passed components. This function can
     * change the underlying type of object held by `*this`.
     * \n
     * The constructed object has `this->type() == address_type::host_and_port`,
     * `this->hostname() == hostname` and `this->port() == port`.
     * \n
     * An empty hostname is equivalent to `localhost`.
     * \n
     * \par Exception safety
     * Basic guarantee. Memory allocations may throw.
     * \par Object lifetimes
     * Invalidates views pointing into `*this`.
     */
    void emplace_host_and_port(std::string hostname, unsigned short port = default_port)
    {
        impl_.type = address_type::host_and_port;
        impl_.address = std::move(hostname);
        impl_.port = port;
    }

    /**
     * \brief Replaces the current object with a UNIX socket path.
     * \details
     * Destroys the current contained object and constructs a new
     * UNIX socket path from the passed value. This function can
     * change the underlying type of object held by `*this`.
     * \n
     * The constructed object has `this->type() == address_type::unix_path` and
     * `this->unix_socket_path() == path`.
     * \n
     * \par Exception safety
     * Basic guarantee. Memory allocations may throw.
     * \par Object lifetimes
     * Invalidates views pointing into `*this`.
     */
    void emplace_unix_path(std::string path)
    {
        impl_.type = address_type::unix_path;
        impl_.address = std::move(path);
        impl_.port = 0;
    }

    /**
     * \brief Tests for equality.
     * \details Two addresses are equal if they have the same type and individual components.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool operator==(const any_address& rhs) const noexcept
    {
        return impl_.type == rhs.impl_.type && impl_.address == rhs.impl_.address &&
               impl_.port == rhs.impl_.port;
    }

    /**
     * \brief Tests for inequality.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool operator!=(const any_address& rhs) const noexcept { return !(*this == rhs); }
};

}  // namespace mysql
}  // namespace boost

#endif
