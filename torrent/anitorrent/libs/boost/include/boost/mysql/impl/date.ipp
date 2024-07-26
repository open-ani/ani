//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_DATE_IPP
#define BOOST_MYSQL_IMPL_DATE_IPP

#pragma once

#include <boost/mysql/date.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/impl/internal/dt_to_string.hpp>

#include <cstddef>
#include <ostream>

std::ostream& boost::mysql::operator<<(std::ostream& os, const date& value)
{
    char buffer[32]{};
    std::size_t sz = detail::date_to_string(value.year(), value.month(), value.day(), buffer);
    return os << string_view(buffer, sz);
}

#endif