//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_STRING_VIEW_HPP
#define BOOST_MYSQL_STRING_VIEW_HPP

#include <boost/core/detail/string_view.hpp>

namespace boost {
namespace mysql {

/// Type used to represent read-only string references, similar to `std::string_view`.
using string_view = boost::core::string_view;

}  // namespace mysql
}  // namespace boost

#endif
