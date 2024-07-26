// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_MULTIPOINT_TO_GEOMETRY_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_MULTIPOINT_TO_GEOMETRY_HPP

#include <iterator>

#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/covered_by.hpp>
#include <boost/geometry/algorithms/detail/closest_points/range_to_geometry_rtree.hpp>
#include <boost/geometry/algorithms/detail/closest_points/utilities.hpp>
#include <boost/geometry/algorithms/dispatch/closest_points.hpp>

#include <boost/geometry/core/point_type.hpp>
#include <boost/geometry/core/tags.hpp>

#include <boost/geometry/geometries/linestring.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace closest_points
{


struct multipoint_to_multipoint
{
    template
    <
        typename MultiPoint1,
        typename MultiPoint2,
        typename Segment,
        typename Strategies
    >
    static inline void apply(MultiPoint1 const& multipoint1,
                             MultiPoint2 const& multipoint2,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        if (boost::size(multipoint1) < boost::size(multipoint2))
        {
            point_or_segment_range_to_geometry_rtree::apply(
                boost::begin(multipoint2),
                boost::end(multipoint2),
                multipoint1,
                shortest_seg,
                strategies);
            detail::closest_points::swap_segment_points::apply(shortest_seg);
            return;
        }
        point_or_segment_range_to_geometry_rtree::apply(
            boost::begin(multipoint1),
            boost::end(multipoint1),
            multipoint2,
            shortest_seg,
            strategies);
    }
};

struct multipoint_to_linear
{
    template
    <
        typename MultiPoint,
        typename Linear,
        typename Segment,
        typename Strategies
    >
    static inline void apply(MultiPoint const& multipoint,
                             Linear const& linear,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        point_or_segment_range_to_geometry_rtree::apply(
            boost::begin(multipoint),
            boost::end(multipoint),
            linear,
            shortest_seg,
            strategies);
    }
};

struct linear_to_multipoint
{
    template
    <
        typename Linear,
        typename MultiPoint,
        typename Segment,
        typename Strategies
    >
    static inline void apply(Linear const& linear,
                             MultiPoint const& multipoint,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        multipoint_to_linear::apply(multipoint, linear, shortest_seg, strategies);
        detail::closest_points::swap_segment_points::apply(shortest_seg);
    }
};

struct segment_to_multipoint
{
    template
    <
        typename Segment,
        typename MultiPoint,
        typename OutSegment,
        typename Strategies
    >
    static inline void apply(Segment const& segment,
                             MultiPoint const& multipoint,
                             OutSegment& shortest_seg,
                             Strategies const& strategies)
    {
        using linestring_type = geometry::model::linestring
            <
                typename point_type<Segment>::type
            >;
        linestring_type linestring;
        convert(segment, linestring);
        multipoint_to_linear::apply(multipoint, linestring, shortest_seg, strategies);
        detail::closest_points::swap_segment_points::apply(shortest_seg);
    }
};

struct multipoint_to_segment
{
    template
    <
        typename MultiPoint,
        typename Segment,
        typename OutSegment,
        typename Strategies
    >
    static inline void apply(MultiPoint const& multipoint,
                             Segment const& segment,
                             OutSegment& shortest_seg,
                             Strategies const& strategies)
    {
        using linestring_type = geometry::model::linestring
            <
                typename point_type<Segment>::type
            >;
        linestring_type linestring;
        convert(segment, linestring);
        multipoint_to_linear::apply(multipoint, linestring, shortest_seg,
            strategies);
    }
};


struct multipoint_to_areal
{

private:

    template <typename Areal, typename Strategies>
    struct covered_by_areal
    {
        covered_by_areal(Areal const& areal, Strategies const& strategy)
            : m_areal(areal), m_strategy(strategy)
        {}

        template <typename Point>
        inline bool operator()(Point const& point) const
        {
            return geometry::covered_by(point, m_areal, m_strategy);
        }

        Areal const& m_areal;
        Strategies const& m_strategy;
    };

public:

    template
    <
        typename MultiPoint,
        typename Areal,
        typename Segment,
        typename Strategies
    >
    static inline void apply(MultiPoint const& multipoint,
                             Areal const& areal,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        covered_by_areal<Areal, Strategies> predicate(areal, strategies);

        auto it = std::find_if(
                boost::begin(multipoint),
                boost::end(multipoint),
                predicate);

        if (it != boost::end(multipoint))
        {
            return set_segment_from_points::apply(*it, *it, shortest_seg);

        }

        point_or_segment_range_to_geometry_rtree::apply(
            boost::begin(multipoint),
            boost::end(multipoint),
            areal,
            shortest_seg,
            strategies);
    }
};

struct areal_to_multipoint
{
    template
    <
        typename Areal,
        typename MultiPoint,
        typename Segment,
        typename Strategies
    >
    static inline void apply(Areal const& areal,
                             MultiPoint const& multipoint,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        multipoint_to_areal::apply(multipoint, areal, shortest_seg, strategies);
        detail::closest_points::swap_segment_points::apply(shortest_seg);
    }
};

}} // namespace detail::closest_points
#endif // DOXYGEN_NO_DETAIL



#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template <typename MultiPoint1, typename MultiPoint2>
struct closest_points
    <
        MultiPoint1, MultiPoint2,
        multi_point_tag, multi_point_tag,
        false
    > : detail::closest_points::multipoint_to_multipoint
{};


template <typename MultiPoint, typename Linear>
struct closest_points
    <
        MultiPoint, Linear,
        multi_point_tag, linear_tag,
        false
    > : detail::closest_points::multipoint_to_linear
{};


template <typename Linear, typename MultiPoint>
struct closest_points
    <
         Linear, MultiPoint,
         linear_tag, multi_point_tag,
         false
    > : detail::closest_points::linear_to_multipoint
{};


template <typename MultiPoint, typename Segment>
struct closest_points
    <
        MultiPoint, Segment,
        multi_point_tag, segment_tag,
        false
    > : detail::closest_points::multipoint_to_segment
{};


template <typename Segment, typename MultiPoint>
struct closest_points
    <
         Segment, MultiPoint,
         segment_tag, multi_point_tag,
         false
    > : detail::closest_points::segment_to_multipoint
{};


template <typename MultiPoint, typename Areal>
struct closest_points
    <
        MultiPoint, Areal,
        multi_point_tag, areal_tag,
        false
    > : detail::closest_points::multipoint_to_areal
{};


template <typename Areal, typename MultiPoint>
struct closest_points
    <
        Areal, MultiPoint,
        areal_tag, multi_point_tag,
        false
    > : detail::closest_points::areal_to_multipoint
{};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_MULTIPOINT_TO_GEOMETRY_HPP
