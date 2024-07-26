// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DISPATCH_CLOSEST_POINTS_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DISPATCH_CLOSEST_POINTS_HPP


#include <boost/geometry/algorithms/not_implemented.hpp>

#include <boost/geometry/core/reverse_dispatch.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tag_cast.hpp>
#include <boost/geometry/core/tags.hpp>

#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/closest_points/services.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template
<
    typename Geometry1, typename Geometry2,
    typename Tag1 = typename tag_cast
        <
            typename tag<Geometry1>::type,
            segment_tag,
            box_tag,
            linear_tag,
            areal_tag
        >::type,
    typename Tag2 = typename tag_cast
        <
            typename tag<Geometry2>::type,
            segment_tag,
            box_tag,
            linear_tag,
            areal_tag
        >::type,
    bool Reverse = reverse_dispatch<Geometry1, Geometry2>::type::value
>
struct closest_points : not_implemented<Tag1, Tag2>
{};



} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_DISPATCH_CLOSEST_POINTS_HPP
