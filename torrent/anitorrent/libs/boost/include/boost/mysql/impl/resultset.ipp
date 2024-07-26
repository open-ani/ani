//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_RESULTSET_IPP
#define BOOST_MYSQL_IMPL_RESULTSET_IPP

#pragma once

#include <boost/mysql/resultset.hpp>

void boost::mysql::resultset::assign(resultset_view v)
{
    has_value_ = v.has_value();
    if (has_value_)
    {
        meta_.assign(v.meta().begin(), v.meta().end());
        rws_ = v.rows();
        affected_rows_ = v.affected_rows();
        last_insert_id_ = v.last_insert_id();
        warnings_ = static_cast<std::uint16_t>(v.warning_count());
        info_.assign(v.info().begin(), v.info().end());
        is_out_params_ = v.is_out_params();
    }
    else
    {
        meta_.clear();
        rws_ = ::boost::mysql::rows();
        affected_rows_ = 0;
        last_insert_id_ = 0;
        warnings_ = 0;
        info_.clear();
        is_out_params_ = false;
    }
}

#endif
