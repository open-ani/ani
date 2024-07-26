// Boost.Geometry

// Copyright (c) 2022, Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_TOPOLOGICAL_DIMENSION_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_TOPOLOGICAL_DIMENSION_HPP


#include <algorithm>

#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/algorithms/is_empty.hpp>
#include <boost/geometry/core/topological_dimension.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail
{


template <typename GeometryCollection>
inline int gc_topological_dimension(GeometryCollection const& geometry)
{
    int result = -1;
    detail::visit_breadth_first([&](auto const& g)
    {
        if (! geometry::is_empty(g))
        {
            static const int d = geometry::topological_dimension<decltype(g)>::value;
            result = (std::max)(result, d);
        }
        return result >= 2;
    }, geometry);
    return result;
}


} // namespace detail
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_TOPOLOGICAL_DIMENSION_HPP
