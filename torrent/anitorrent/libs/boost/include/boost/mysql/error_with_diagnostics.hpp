//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_ERROR_WITH_DIAGNOSTICS_HPP
#define BOOST_MYSQL_ERROR_WITH_DIAGNOSTICS_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>

#include <boost/system/system_error.hpp>

namespace boost {
namespace mysql {

/**
 * \brief A system_error with an embedded diagnostics object.
 * \details
 * Like `boost::system::system_error`, but adds a \ref diagnostics member
 * containing additional information.
 */
class error_with_diagnostics : public boost::system::system_error
{
    diagnostics diag_;

    static boost::system::system_error create_base(const error_code& err, const diagnostics& diag)
    {
        return diag.client_message().empty() ? boost::system::system_error(err)
                                             : boost::system::system_error(err, diag.client_message());
    }

public:
    /// Initializing constructor.
    error_with_diagnostics(const error_code& err, const diagnostics& diag)
        : boost::system::system_error(create_base(err, diag)), diag_(diag)
    {
    }

    /**
     * \brief Retrieves the server diagnostics embedded in this object.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` is alive.
     */
    const diagnostics& get_diagnostics() const noexcept { return diag_; }
};

}  // namespace mysql
}  // namespace boost

#endif
