//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DIAGNOSTICS_HPP
#define BOOST_MYSQL_DIAGNOSTICS_HPP

#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>

#include <string>

namespace boost {
namespace mysql {

/**
 * \brief Contains additional information about errors.
 * \details
 * This class is a container for additional diagnostics about an operation that
 * failed. It can contain server-generated messages (\ref server_message) or client-side messages
 * (\ref client_message). More members may be added in the future.
 */
class diagnostics
{
public:
    /**
     * \brief Constructs a diagnostics object with empty error messages.
     * \par Exception safety
     * No-throw guarantee.
     */
    diagnostics() = default;

    /**
     * \brief Gets the client-generated error message.
     * \details
     * Contrary to \ref server_message, the client message never contains any string data
     * returned by the server, and is always ASCII-encoded. If you're using the static interface,
     * it may contain C++ type identifiers, too.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned view is valid as long as `*this` is alive, hasn't been assigned-to
     * or moved-from, and \ref clear hasn't been called. Moving `*this` invalidates the view.
     */
    string_view client_message() const noexcept
    {
        return impl_.is_server ? string_view() : string_view(impl_.msg);
    }

    /**
     * \brief Gets the server-generated error message.
     * \details
     * It's encoded according to `character_set_results` character set, which
     * usually matches the connection's character set. It may potentially contain user input.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned view is valid as long as `*this` is alive, hasn't been assigned-to
     * or moved-from, and \ref clear hasn't been called. Moving `*this` invalidates the view.
     */
    string_view server_message() const noexcept
    {
        return impl_.is_server ? string_view(impl_.msg) : string_view();
    }

    /**
     * \brief Clears the error messages.
     * \par Exception safety
     * No-throw guarantee.
     */
    void clear() noexcept
    {
        impl_.is_server = false;
        impl_.msg.clear();
    }

private:
#ifndef BOOST_MYSQL_DOXYGEN
    struct
    {
        bool is_server{};
        std::string msg;

        void assign_client(std::string from)
        {
            msg = std::move(from);
            is_server = false;
        }

        void assign_server(std::string from)
        {
            msg = std::move(from);
            is_server = true;
        }
    } impl_;

    friend bool operator==(const diagnostics& lhs, const diagnostics& rhs) noexcept;
    friend struct detail::access;
#endif
};

/**
 * \relates diagnostics
 * \brief Compares two diagnostics objects.
 * \par Exception safety
 * No-throw guarantee.
 */
inline bool operator==(const diagnostics& lhs, const diagnostics& rhs) noexcept
{
    return lhs.impl_.is_server == rhs.impl_.is_server && lhs.impl_.msg == rhs.impl_.msg;
}

/**
 * \relates diagnostics
 * \brief Compares two diagnostics objects.
 * \par Exception safety
 * No-throw guarantee.
 */
inline bool operator!=(const diagnostics& lhs, const diagnostics& rhs) noexcept { return !(lhs == rhs); }

}  // namespace mysql
}  // namespace boost

#endif
