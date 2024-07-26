//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_FIELD_KIND_HPP
#define BOOST_MYSQL_FIELD_KIND_HPP

#include <boost/mysql/detail/config.hpp>

#include <iosfwd>

namespace boost {
namespace mysql {

/**
 * \brief Represents the possible C++ types a `field` or `field_view` may have.
 */
enum class field_kind
{
    // Order here is important

    /// Any of the below when the value is NULL
    null = 0,

    /// The field contains a `std::int64_t`.
    int64,

    /// The field contains a `std::uint64_t`.
    uint64,

    /// The field contains a string (`std::string` for `field` and `string_view` for
    /// `field_view`).
    string,

    /// The field contains a binary string (\ref blob for `field` and \ref blob_view for
    /// `field_view`).
    blob,

    /// The field contains a `float`.
    float_,

    /// The field contains a `double`.
    double_,

    /// The field contains a \ref date.
    date,

    /// The field contains a \ref datetime.
    datetime,

    /// The field contains a \ref time.
    time
};

/**
 * \brief Streams a field_kind.
 */
BOOST_MYSQL_DECL
std::ostream& operator<<(std::ostream& os, field_kind v);

}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/field_kind.ipp>
#endif

#endif
