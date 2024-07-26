//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_STRING_VIEW_OFFSET_HPP
#define BOOST_MYSQL_DETAIL_STRING_VIEW_OFFSET_HPP

#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

// Represents a string_view using offsets into a buffer.
// Useful during deserialization, for buffers that may reallocate.
struct string_view_offset
{
    std::size_t offset;
    std::size_t size;

    constexpr bool operator==(string_view_offset rhs) const noexcept
    {
        return offset == rhs.offset && size == rhs.size;
    }
    constexpr bool operator!=(string_view_offset rhs) const noexcept { return !(*this == rhs); }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
