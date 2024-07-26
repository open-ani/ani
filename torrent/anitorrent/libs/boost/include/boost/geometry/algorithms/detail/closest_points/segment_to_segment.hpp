// Boost.Geometry

// Copyright (c) 2021-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_SEGMENT_TO_SEGMENT_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_SEGMENT_TO_SEGMENT_HPP

#include <algorithm>
#include <iterator>

#include <boost/core/addressof.hpp>

#include <boost/geometry/algorithms/assign.hpp>
#include <boost/geometry/algorithms/detail/closest_points/utilities.hpp>
#include <boost/geometry/algorithms/detail/distance/is_comparable.hpp>
#include <boost/geometry/algorithms/detail/distance/strategy_utils.hpp>
#include <boost/geometry/algorithms/dispatch/closest_points.hpp>
#include <boost/geometry/algorithms/dispatch/distance.hpp>
#include <boost/geometry/algorithms/intersects.hpp>

#include <boost/geometry/core/point_type.hpp>
#include <boost/geometry/core/tags.hpp>

#include <boost/geometry/strategies/distance.hpp>
#include <boost/geometry/strategies/tags.hpp>

#include <boost/geometry/util/condition.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace closest_points
{



// compute segment-segment closest-points
class segment_to_segment
{
public:

    template <typename Segment1, typename Segment2, typename OutputSegment, typename Strategies>
    static inline void apply(Segment1 const& segment1, Segment2 const& segment2,
                             OutputSegment& shortest_seg,
                             Strategies const& strategies)
    {
        using intersection_return_type = segment_intersection_points
            <
                typename point_type<Segment1>::type
            >;

        using intersection_policy = policies::relate::segments_intersection_points
            <
                intersection_return_type
            >;

        detail::segment_as_subrange<Segment1> sub_range1(segment1);
        detail::segment_as_subrange<Segment2> sub_range2(segment2);
        auto is = strategies.relate().apply(sub_range1, sub_range2,
                                            intersection_policy());
        if (is.count > 0)
        {
            set_segment_from_points::apply(is.intersections[0],
                                           is.intersections[0],
                                           shortest_seg);
            return;
        }

        typename point_type<Segment1>::type p[2];
        detail::assign_point_from_index<0>(segment1, p[0]);
        detail::assign_point_from_index<1>(segment1, p[1]);

        typename point_type<Segment2>::type q[2];
        detail::assign_point_from_index<0>(segment2, q[0]);
        detail::assign_point_from_index<1>(segment2, q[1]);

        auto cp0 = strategies.closest_points(q[0], segment1).apply(q[0], p[0], p[1]);
        auto cp1 = strategies.closest_points(q[1], segment1).apply(q[1], p[0], p[1]);
        auto cp2 = strategies.closest_points(p[0], segment2).apply(p[0], q[0], q[1]);
        auto cp3 = strategies.closest_points(p[1], segment2).apply(p[1], q[0], q[1]);

        closest_points::creturn_t<Segment1, Segment2, Strategies> d[4];

        auto const cds = strategies::distance::detail::make_comparable(strategies)
            .distance(detail::dummy_point(), detail::dummy_point());

        d[0] = cds.apply(cp0, q[0]);
        d[1] = cds.apply(cp1, q[1]);
        d[2] = cds.apply(p[0], cp2);
        d[3] = cds.apply(p[1], cp3);

        std::size_t imin = std::distance(boost::addressof(d[0]), std::min_element(d, d + 4));

        switch (imin)
        {
        case 0:
            set_segment_from_points::apply(cp0, q[0], shortest_seg);
            return;
        case 1:
            set_segment_from_points::apply(cp1, q[1], shortest_seg);
            return;
        case 2:
            set_segment_from_points::apply(p[0], cp2, shortest_seg);
            return;
        default:
            set_segment_from_points::apply(p[1], cp3, shortest_seg);
            return;
        }
    }
};


}} // namespace detail::closest_points
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


// segment-segment
template <typename Segment1, typename Segment2>
struct closest_points
    <
        Segment1, Segment2, segment_tag, segment_tag, false
    > : detail::closest_points::segment_to_segment
{};

} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_SEGMENT_TO_SEGMENT_HPP
