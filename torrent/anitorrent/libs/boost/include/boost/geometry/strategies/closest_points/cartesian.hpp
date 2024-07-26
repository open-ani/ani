// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_CARTESIAN_HPP
#define BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_CARTESIAN_HPP


//#include <boost/geometry/strategies/cartesian/azimuth.hpp>

//#include <boost/geometry/strategies/cartesian/distance_projected_point.hpp>
//#include <boost/geometry/strategies/cartesian/distance_pythagoras.hpp>
#include <boost/geometry/strategies/cartesian/distance_pythagoras_box_box.hpp>
#include <boost/geometry/strategies/cartesian/distance_pythagoras_point_box.hpp>
#include <boost/geometry/strategies/cartesian/distance_segment_box.hpp>

#include <boost/geometry/strategies/cartesian/closest_points_pt_seg.hpp>

#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/distance/detail.hpp>
#include <boost/geometry/strategies/closest_points/services.hpp>

//#include <boost/geometry/strategies/normalize.hpp>
#include <boost/geometry/strategies/distance/cartesian.hpp>

#include <boost/geometry/util/type_traits.hpp>


namespace boost { namespace geometry
{

namespace strategies { namespace closest_points
{

template <typename CalculationType = void>
struct cartesian
    : public strategies::distance::cartesian<CalculationType>
{
    template <typename Geometry1, typename Geometry2>
    static auto closest_points(Geometry1 const&, Geometry2 const&,
                               distance::detail::enable_if_ps_t<Geometry1, Geometry2> * = nullptr)
    {
        return strategy::closest_points::projected_point<CalculationType>();
    }
};


namespace services
{

template <typename Geometry1, typename Geometry2>
struct default_strategy<Geometry1, Geometry2, cartesian_tag, cartesian_tag>
{
    using type = strategies::closest_points::cartesian<>;
};

} // namespace services

}} // namespace strategies::closest_points

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_CARTESIAN_HPP
