//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_BLOB_HPP
#define BOOST_MYSQL_BLOB_HPP

#include <vector>

namespace boost {
namespace mysql {

/// Owning type used to represent binary blobs.
using blob = std::vector<unsigned char>;

}  // namespace mysql
}  // namespace boost

#endif
