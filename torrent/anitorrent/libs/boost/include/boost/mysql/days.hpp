//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DAYS_HPP
#define BOOST_MYSQL_DAYS_HPP

#include <chrono>

namespace boost {
namespace mysql {

/**
 * \brief Duration representing a day (24 hours).
 * \details Suitable to represent the range of dates MySQL offers.
 * May differ in representation from `std::chrono::days` in C++20.
 */
using days = std::chrono::duration<int, std::ratio<3600 * 24>>;

}  // namespace mysql
}  // namespace boost

#endif
