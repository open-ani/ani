//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_ANY_EXECUTION_REQUEST_HPP
#define BOOST_MYSQL_DETAIL_ANY_EXECUTION_REQUEST_HPP

#include <boost/mysql/field_view.hpp>
#include <boost/mysql/statement.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/core/span.hpp>

namespace boost {
namespace mysql {
namespace detail {

struct any_execution_request
{
    union data_t
    {
        string_view query;
        struct
        {
            statement stmt;
            span<const field_view> params;
        } stmt;

        data_t(string_view q) noexcept : query(q) {}
        data_t(statement s, span<const field_view> params) noexcept : stmt{s, params} {}
    } data;
    bool is_query;

    any_execution_request(string_view q) noexcept : data(q), is_query(true) {}
    any_execution_request(statement s, span<const field_view> params) noexcept
        : data(s, params), is_query(false)
    {
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
