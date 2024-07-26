//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_THROW_ON_ERROR_LOC_HPP
#define BOOST_MYSQL_DETAIL_THROW_ON_ERROR_LOC_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/error_with_diagnostics.hpp>

#include <boost/assert/source_location.hpp>

namespace boost {
namespace mysql {
namespace detail {

inline void throw_on_error_loc(error_code err, const diagnostics& diag, const boost::source_location& loc)
{
    if (err)
    {
        ::boost::throw_exception(error_with_diagnostics(err, diag), loc);
    }
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
