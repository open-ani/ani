// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_LINEAR_TO_LINEAR_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_LINEAR_TO_LINEAR_HPP

#include <boost/geometry/algorithms/detail/closest_points/range_to_geometry_rtree.hpp>
#include <boost/geometry/algorithms/detail/closest_points/utilities.hpp>

#include <boost/geometry/algorithms/num_points.hpp>
#include <boost/geometry/algorithms/num_segments.hpp>

#include <boost/geometry/core/point_type.hpp>

#include <boost/geometry/iterators/point_iterator.hpp>
#include <boost/geometry/iterators/segment_iterator.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace closest_points
{


struct linear_to_linear
{
    template <typename Linear1, typename Linear2, typename Segment, typename Strategies>
    static inline void apply(Linear1 const& linear1,
                             Linear2 const& linear2,
                             Segment& shortest_seg,
                             Strategies const& strategies,
                             bool = false)
    {
        if (geometry::num_points(linear1) == 1)
        {
            dispatch::closest_points
                <
                    typename point_type<Linear1>::type,
                    Linear2
                >::apply(*points_begin(linear1), linear2, shortest_seg, strategies);
            return;
        }

        if (geometry::num_points(linear2) == 1)
        {
            dispatch::closest_points
                <
                    typename point_type<Linear2>::type,
                    Linear1
                >::apply(*points_begin(linear2), linear1, shortest_seg, strategies);
            detail::closest_points::swap_segment_points::apply(shortest_seg);
            return;
        }

        if (geometry::num_segments(linear1) < geometry::num_segments(linear2))
        {
            point_or_segment_range_to_geometry_rtree::apply(
                geometry::segments_begin(linear2),
                geometry::segments_end(linear2),
                linear1,
                shortest_seg,
                strategies);
            detail::closest_points::swap_segment_points::apply(shortest_seg);
            return;
        }

        point_or_segment_range_to_geometry_rtree::apply(
            geometry::segments_begin(linear1),
            geometry::segments_end(linear1),
            linear2,
            shortest_seg,
            strategies);
    }
};

struct segment_to_linear
{
    template <typename Segment, typename Linear, typename OutSegment, typename Strategies>
    static inline void apply(Segment const& segment,
                             Linear const& linear,
                             OutSegment& shortest_seg,
                             Strategies const& strategies,
                             bool = false)
    {
        using linestring_type = geometry::model::linestring
            <typename point_type<Segment>::type>;
        linestring_type linestring;
        convert(segment, linestring);
        linear_to_linear::apply(linestring, linear, shortest_seg, strategies);
    }
};

struct linear_to_segment
{
    template <typename Linear, typename Segment, typename OutSegment, typename Strategies>
    static inline void apply(Linear const& linear,
                             Segment const& segment,
                             OutSegment& shortest_seg,
                             Strategies const& strategies,
                             bool = false)
    {
        segment_to_linear::apply(segment, linear, shortest_seg, strategies);
        detail::closest_points::swap_segment_points::apply(shortest_seg);
    }
};

}} // namespace detail::closest_points
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{

template <typename Linear1, typename Linear2>
struct closest_points
    <
        Linear1, Linear2,
        linear_tag, linear_tag,
        false
    > : detail::closest_points::linear_to_linear
{};

template <typename Segment, typename Linear>
struct closest_points
    <
        Segment, Linear,
        segment_tag, linear_tag,
        false
    > : detail::closest_points::segment_to_linear
{};

template <typename Linear, typename Segment>
struct closest_points
    <
        Linear, Segment,
        linear_tag, segment_tag,
        false
    > : detail::closest_points::linear_to_segment
{};

} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_LINEAR_TO_LINEAR_HPP
