//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_BYTE_TO_HEX_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_BYTE_TO_HEX_HPP

namespace boost {
namespace mysql {
namespace detail {

// We implement the translation to hex ourselves, since it's easy enough.
// We use a table to look up characters
constexpr char byte_to_hex_table[16] =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

// it should point to a buffer of size 2, at least
inline char* byte_to_hex(unsigned char byte, char* it) noexcept
{
    *it++ = byte_to_hex_table[(byte & ~15) >> 4];
    *it++ = byte_to_hex_table[byte & 15];
    return it;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
