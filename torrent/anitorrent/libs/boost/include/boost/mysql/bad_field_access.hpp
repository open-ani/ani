//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_BAD_FIELD_ACCESS_HPP
#define BOOST_MYSQL_BAD_FIELD_ACCESS_HPP

#include <exception>

namespace boost {
namespace mysql {

/// Exception type thrown when trying to access a \ref field
/// or \ref field_view with an incorrect type.
class bad_field_access : public std::exception
{
public:
    /// Returns the error message.
    const char* what() const noexcept override { return "bad_value_access"; }
};

}  // namespace mysql
}  // namespace boost

#endif
