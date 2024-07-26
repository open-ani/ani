//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_STATEMENT_HPP
#define BOOST_MYSQL_IMPL_STATEMENT_HPP

#pragma once

#include <boost/mysql/statement.hpp>

#include <boost/mysql/detail/access.hpp>

#include <boost/assert.hpp>

template <BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple>
class boost::mysql::bound_statement_tuple
{
    friend class statement;
    friend struct detail::access;

    struct impl
    {
        statement stmt;
        WritableFieldTuple params;
    } impl_;

    template <typename TupleType>
    bound_statement_tuple(const statement& stmt, TupleType&& t) : impl_{stmt, std::forward<TupleType>(t)}
    {
    }
};

template <BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR FieldViewFwdIterator>
class boost::mysql::bound_statement_iterator_range
{
    friend class statement;
    friend struct detail::access;

    struct impl
    {
        statement stmt;
        FieldViewFwdIterator first;
        FieldViewFwdIterator last;
    } impl_;

    bound_statement_iterator_range(
        const statement& stmt,
        FieldViewFwdIterator first,
        FieldViewFwdIterator last
    )
        : impl_{stmt, first, last}
    {
    }
};

template <BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple, typename EnableIf>
boost::mysql::bound_statement_tuple<typename std::decay<WritableFieldTuple>::type> boost::mysql::statement::
    bind(WritableFieldTuple&& args) const

{
    BOOST_ASSERT(valid());
    return bound_statement_tuple<typename std::decay<WritableFieldTuple>::type>(
        *this,
        std::forward<WritableFieldTuple>(args)
    );
}

template <BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR FieldViewFwdIterator, typename EnableIf>
boost::mysql::bound_statement_iterator_range<FieldViewFwdIterator> boost::mysql::statement::bind(
    FieldViewFwdIterator first,
    FieldViewFwdIterator last
) const
{
    BOOST_ASSERT(valid());
    return bound_statement_iterator_range<FieldViewFwdIterator>(*this, first, last);
}

#endif
