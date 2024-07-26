// Copyright (c) 2016-2024 Antony Polukhin
// Copyright (c) 2022 Denis Mikhailov
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_PFR_DETAIL_CONFIG_HPP
#define BOOST_PFR_DETAIL_CONFIG_HPP
#pragma once

#include <boost/pfr/config.hpp>

#if !BOOST_PFR_ENABLED

#error Boost.PFR library is not supported in your environment.             \
       Try one of the possible solutions:                                  \
       1. try to take away an '-DBOOST_PFR_ENABLED=0', if it exists        \
       2. enable C++14;                                                    \
       3. enable C++17;                                                    \
       4. update your compiler;                                            \
       or disable this error by '-DBOOST_PFR_ENABLED=1' if you really know what are you doing.

#endif // !BOOST_PFR_ENABLED

#endif // BOOST_PFR_DETAIL_CONFIG_HPP

