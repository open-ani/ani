// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_LINEAR_OR_AREAL_TO_AREAL_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_LINEAR_OR_AREAL_TO_AREAL_HPP

#include <boost/geometry/algorithms/detail/closest_points/linear_to_linear.hpp>
#include <boost/geometry/algorithms/detail/closest_points/utilities.hpp>

#include <boost/geometry/algorithms/intersection.hpp>

#include <boost/geometry/core/point_type.hpp>

#include <boost/geometry/geometries/geometries.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace closest_points
{

struct linear_to_areal
{
    template <typename Linear, typename Areal, typename Segment, typename Strategies>
    static inline void apply(Linear const& linear,
                             Areal const& areal,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        using most_precise_type = typename select_coordinate_type<Linear, Areal>::type;

        using point_type = typename std::conditional
            <
                std::is_same<typename coordinate_type<Linear>::type, most_precise_type>::value,
                typename point_type<Linear>::type,
                typename point_type<Areal>::type
            >::type;

        using linestring_type = geometry::model::linestring<point_type>;

        /* TODO: currently intersection does not support some cases of tupled input
         *       such as linestring - multipolygon
         *       this could be implemented directly with dynamic geometries
        using polygon_type = geometry::model::polygon<point_type>;
        std::tuple
        <
            geometry::model::multi_point<point_type>,
            geometry::model::multi_linestring<linestring_type>,
            geometry::model::multi_polygon<polygon_type>
        > tp;
        bool intersect_tp = geometry::intersection(linear, areal, tp, strategies);
        */

        geometry::model::multi_point<point_type> mp_out;
        geometry::intersection(linear, areal, mp_out, strategies);

        if (! boost::empty(mp_out))
        {
            set_segment_from_points::apply(*boost::begin(mp_out),
                                           *boost::begin(mp_out),
                                           shortest_seg);
            return;
        }

        // if there are no intersection points then check if the linear geometry
        // (or part of it) is inside the areal and return any point of this part
        geometry::model::multi_linestring<linestring_type> ln_out;
        geometry::intersection(linear, areal, ln_out, strategies);

        if (! boost::empty(ln_out))
        {
            set_segment_from_points::apply(*boost::begin(*boost::begin(ln_out)),
                                           *boost::begin(*boost::begin(ln_out)),
                                           shortest_seg);
            return;
        }

        linear_to_linear::apply(linear, areal, shortest_seg, strategies, false);
    }
};

struct areal_to_linear
{
    template <typename Linear, typename Areal, typename Segment, typename Strategies>
    static inline void apply(Areal const& areal,
                             Linear const& linear,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        linear_to_areal::apply(linear, areal, shortest_seg, strategies);
        detail::closest_points::swap_segment_points::apply(shortest_seg);
    }
};

struct segment_to_areal
{
    template <typename Segment, typename Areal, typename OutSegment, typename Strategies>
    static inline void apply(Segment const& segment,
                             Areal const& areal,
                             OutSegment& shortest_seg,
                             Strategies const& strategies,
                             bool = false)
    {
        using linestring_type = geometry::model::linestring<typename point_type<Segment>::type>;
        linestring_type linestring;
        convert(segment, linestring);
        linear_to_areal::apply(linestring, areal, shortest_seg, strategies);
    }
};

struct areal_to_segment
{
    template <typename Areal, typename Segment, typename OutSegment, typename Strategies>
    static inline void apply(Areal const& areal,
                             Segment const& segment,
                             OutSegment& shortest_seg,
                             Strategies const& strategies,
                             bool = false)
    {
        segment_to_areal::apply(segment, areal, shortest_seg, strategies);
        detail::closest_points::swap_segment_points::apply(shortest_seg);
    }
};

struct areal_to_areal
{
    template <typename Areal1, typename Areal2, typename Segment, typename Strategies>
    static inline void apply(Areal1 const& areal1,
                             Areal2 const& areal2,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        using most_precise_type = typename select_coordinate_type<Areal1, Areal2>::type;

        using point_type = typename std::conditional
            <
                std::is_same<typename coordinate_type<Areal1>::type, most_precise_type>::value,
                typename point_type<Areal1>::type,
                typename point_type<Areal2>::type
            >::type;

        using linestring_type = geometry::model::linestring<point_type>;
        using polygon_type = geometry::model::polygon<point_type>;

        /* TODO: currently intersection does not support tupled input
         *       this should be implemented directly with dynamic geometries
        */

        geometry::model::multi_point<point_type> mp_out;
        geometry::intersection(areal1, areal2, mp_out, strategies);

        if (! boost::empty(mp_out))
        {
            set_segment_from_points::apply(*boost::begin(mp_out),
                                           *boost::begin(mp_out),
                                           shortest_seg);
            return;
        }

        // if there are no intersection points then the linear geometry (or part of it)
        // is inside the areal; return any point of this part
        geometry::model::multi_linestring<linestring_type> ln_out;
        geometry::intersection(areal1, areal2, ln_out, strategies);

        if (! boost::empty(ln_out))
        {
            set_segment_from_points::apply(*boost::begin(*boost::begin(ln_out)),
                                           *boost::begin(*boost::begin(ln_out)),
                                           shortest_seg);
            return;
        }

        geometry::model::multi_polygon<polygon_type> pl_out;
        geometry::intersection(areal1, areal2, pl_out, strategies);

        if (! boost::empty(pl_out))
        {
            set_segment_from_points::apply(
                *boost::begin(boost::geometry::exterior_ring(*boost::begin(pl_out))),
                *boost::begin(boost::geometry::exterior_ring(*boost::begin(pl_out))),
                shortest_seg);
            return;
        }

        linear_to_linear::apply(areal1, areal2, shortest_seg, strategies, false);
    }
};


}} // namespace detail::closest_points
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{

template <typename Linear, typename Areal>
struct closest_points
    <
        Linear, Areal,
        linear_tag, areal_tag,
        false
    >
    : detail::closest_points::linear_to_areal
{};

template <typename Areal, typename Linear>
struct closest_points
    <
        Areal, Linear,
        areal_tag, linear_tag,
        false
    >
    : detail::closest_points::areal_to_linear
{};

template <typename Segment, typename Areal>
struct closest_points
    <
        Segment, Areal,
        segment_tag, areal_tag,
        false
    >
    : detail::closest_points::segment_to_areal
{};

template <typename Areal, typename Segment>
struct closest_points
    <
        Areal, Segment,
        areal_tag, segment_tag,
        false
    >
    : detail::closest_points::areal_to_segment
{};

template <typename Areal1, typename Areal2>
struct closest_points
    <
        Areal1, Areal2,
        areal_tag, areal_tag,
        false
    >
    : detail::closest_points::areal_to_areal
{};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_LINEAR_OR_AREAL_TO_AREAL_HPP
