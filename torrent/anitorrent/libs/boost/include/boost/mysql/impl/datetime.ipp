//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_DATETIME_IPP
#define BOOST_MYSQL_IMPL_DATETIME_IPP

#pragma once

#include <boost/mysql/datetime.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/impl/internal/dt_to_string.hpp>

#include <cstddef>
#include <ostream>

std::ostream& boost::mysql::operator<<(std::ostream& os, const datetime& value)
{
    char buffer[64]{};
    std::size_t sz = detail::datetime_to_string(
        value.year(),
        value.month(),
        value.day(),
        value.hour(),
        value.minute(),
        value.second(),
        value.microsecond(),
        buffer
    );
    os << string_view(buffer, sz);
    return os;
}

#endif