// Boost.Geometry

// Copyright (c) 2024 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_UTIL_BOUNDS_HPP
#define BOOST_GEOMETRY_UTIL_BOUNDS_HPP

#include <boost/numeric/conversion/bounds.hpp>

namespace boost { namespace geometry { namespace util
{

// Define a boost::geometry::util::bounds
// It might be specialized for other numeric types, for example Boost.Rational
template<class CT>
struct bounds
{
  static CT lowest  () { return boost::numeric::bounds<CT>::lowest(); }
  static CT highest () { return boost::numeric::bounds<CT>::highest(); }
};

}}} // namespace boost::geometry::util

#endif // BOOST_GEOMETRY_UTIL_BOUNDS_HPP
