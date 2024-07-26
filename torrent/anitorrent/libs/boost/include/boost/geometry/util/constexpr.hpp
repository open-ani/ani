// Boost.Geometry
 
// Copyright (c) 2023 Adam Wulkiewicz, Lodz, Poland.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_UTIL_CONSTEXPR_HPP
#define BOOST_GEOMETRY_UTIL_CONSTEXPR_HPP


#include <boost/geometry/util/condition.hpp>


#ifndef BOOST_NO_CXX17_IF_CONSTEXPR

#define BOOST_GEOMETRY_CONSTEXPR(CONDITION) constexpr (CONDITION)

#else

#define BOOST_GEOMETRY_CONSTEXPR(CONDITION) (BOOST_GEOMETRY_CONDITION(CONDITION))

#endif


#endif // BOOST_GEOMETRY_UTIL_CONSTEXPR_HPP
