//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_MAKE_STRING_VIEW_HPP
#define BOOST_MYSQL_DETAIL_MAKE_STRING_VIEW_HPP

#include <boost/mysql/string_view.hpp>

namespace boost {
namespace mysql {
namespace detail {

template <std::size_t N>
constexpr string_view make_string_view(const char (&buff)[N]) noexcept
{
    static_assert(N >= 1, "Expected a C-array literal");
    return string_view(buff, N - 1);  // discard null terminator
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
