//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_CALL_NEXT_CHAR_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_CALL_NEXT_CHAR_HPP

#include <boost/mysql/character_set.hpp>

#include <boost/assert.hpp>

#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

inline std::size_t call_next_char(const character_set& charset, const char* first, const char* last) noexcept
{
    // Range must be non-empty
    BOOST_ASSERT(last > first);

    // ASCII characters are always 1 byte (UTF-16 and friends are not supported)
    auto* data = reinterpret_cast<const unsigned char*>(first);
    if (*data < 0x80)
        return 1u;

    // May be a multi-byte character. Call the relevant function
    return charset.next_char({data, static_cast<std::size_t>(last - first)});
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
