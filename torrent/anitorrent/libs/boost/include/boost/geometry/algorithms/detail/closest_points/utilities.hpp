// Boost.Geometry

// Copyright (c) 2021-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_UTILITIES_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_UTILITIES_HPP

#include <boost/geometry/algorithms/detail/assign_indexed_point.hpp>
#include <boost/geometry/util/algorithm.hpp>

#include <boost/geometry/strategies/distance.hpp>

namespace boost { namespace geometry
{

namespace detail { namespace closest_points
{

struct set_segment_from_points
{
    template <typename Point1, typename Point2, typename Segment>
    static inline void apply(Point1 const& p1, Point2 const& p2, Segment& segment)
    {
        assign_point_to_index<0>(p1, segment);
        assign_point_to_index<1>(p2, segment);
    }
};


struct swap_segment_points
{
    template <typename Segment>
    static inline void apply(Segment& segment)
    {
        geometry::detail::for_each_dimension<Segment>([&](auto index)
        {
            auto temp = get<0,index>(segment);
            set<0,index>(segment, get<1,index>(segment));
            set<1,index>(segment, temp);
        });
    }
};

template <typename Geometry1, typename Geometry2, typename Strategies>
using distance_strategy_t = decltype(
    std::declval<Strategies>().distance(std::declval<Geometry1>(), std::declval<Geometry2>()));

template <typename Geometry1, typename Geometry2, typename Strategies>
using creturn_t = typename strategy::distance::services::return_type
    <
        typename strategy::distance::services::comparable_type
            <
                distance_strategy_t<Geometry1, Geometry2, Strategies>
            >::type,
        typename point_type<Geometry1>::type,
        typename point_type<Geometry2>::type
    >::type;


}} // namespace detail::closest_points

}} // namespace boost::geometry

#endif //BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_UTILITIES_HPP
