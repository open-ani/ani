//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_BLOB_VIEW_HPP
#define BOOST_MYSQL_BLOB_VIEW_HPP

#include <boost/core/span.hpp>

namespace boost {
namespace mysql {

/// Non-owning type used to represent binary blobs.
using blob_view = boost::span<const unsigned char>;

}  // namespace mysql
}  // namespace boost

#endif
