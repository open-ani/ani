//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_OK_VIEW_HPP
#define BOOST_MYSQL_DETAIL_OK_VIEW_HPP

#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/flags.hpp>

#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

struct ok_view
{
    std::uint64_t affected_rows;
    std::uint64_t last_insert_id;
    std::uint16_t status_flags;
    std::uint16_t warnings;
    string_view info;

    bool more_results() const noexcept { return status_flags & status_flags::more_results; }
    bool backslash_escapes() const noexcept { return !(status_flags & status_flags::no_backslash_escapes); }
    bool is_out_params() const noexcept { return status_flags & status_flags::out_params; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
