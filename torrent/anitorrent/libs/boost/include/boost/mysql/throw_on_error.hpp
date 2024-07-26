//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_THROW_ON_ERROR_HPP
#define BOOST_MYSQL_THROW_ON_ERROR_HPP

#include <boost/mysql/detail/throw_on_error_loc.hpp>

namespace boost {
namespace mysql {

/**
 * \brief Throws an exception in case of error, including diagnostic information.
 * \details If err indicates a failure (`err.failed() == true`), throws an exception that
 * derives from \ref error_with_diagnostics. The exception will make
 * `diag` available in \ref error_with_diagnostics::get_diagnostics.
 */
inline void throw_on_error(error_code err, const diagnostics& diag = {})
{
    detail::throw_on_error_loc(err, diag, boost::source_location{});
}

}  // namespace mysql
}  // namespace boost

#endif
