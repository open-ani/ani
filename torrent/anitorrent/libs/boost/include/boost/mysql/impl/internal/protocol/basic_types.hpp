//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_BASIC_TYPES_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_BASIC_TYPES_HPP

#include <boost/mysql/string_view.hpp>

#include <array>
#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

struct int3
{
    std::uint32_t value;
};

struct int_lenenc
{
    std::uint64_t value;
};

struct string_null
{
    string_view value;
};

struct string_eof
{
    string_view value;
};

struct string_lenenc
{
    string_view value;
};

template <std::size_t N>
struct string_fixed
{
    std::array<char, N> value;
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
