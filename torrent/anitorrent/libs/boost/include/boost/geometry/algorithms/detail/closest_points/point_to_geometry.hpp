// Boost.Geometry

// Copyright (c) 2021-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_POINT_TO_GEOMETRY_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_POINT_TO_GEOMETRY_HPP

#include <iterator>
#include <type_traits>

#include <boost/core/ignore_unused.hpp>
#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>
#include <boost/range/size.hpp>
#include <boost/range/value_type.hpp>

#include <boost/geometry/algorithms/assign.hpp>
#include <boost/geometry/algorithms/detail/closest_feature/geometry_to_range.hpp>
#include <boost/geometry/algorithms/detail/closest_feature/point_to_range.hpp>
#include <boost/geometry/algorithms/detail/closest_points/utilities.hpp>
#include <boost/geometry/algorithms/detail/distance/is_comparable.hpp>
#include <boost/geometry/algorithms/detail/distance/iterator_selector.hpp>
#include <boost/geometry/algorithms/detail/distance/strategy_utils.hpp>
#include <boost/geometry/algorithms/detail/within/point_in_geometry.hpp>
#include <boost/geometry/algorithms/dispatch/closest_points.hpp>
#include <boost/geometry/algorithms/dispatch/distance.hpp>

#include <boost/geometry/core/closure.hpp>
#include <boost/geometry/core/point_type.hpp>
#include <boost/geometry/core/exterior_ring.hpp>
#include <boost/geometry/core/interior_rings.hpp>
#include <boost/geometry/core/radian_access.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>

#include <boost/geometry/strategies/distance.hpp>
#include <boost/geometry/strategies/relate/services.hpp>
#include <boost/geometry/strategies/tags.hpp>

#include <boost/geometry/util/math.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace closest_points
{

struct point_to_point
{
    template <typename P1, typename P2, typename Segment, typename Strategies>
    static inline void apply(P1 const& p1, P2 const& p2,
                             Segment& shortest_seg, Strategies const&)
    {
        set_segment_from_points::apply(p1, p2, shortest_seg);
    }
};

struct point_to_segment
{
    template <typename Point, typename Segment, typename OutputSegment, typename Strategies>
    static inline void apply(Point const& point, Segment const& segment,
                             OutputSegment& shortest_seg, Strategies const& strategies)
    {
        typename point_type<Segment>::type p[2];
        geometry::detail::assign_point_from_index<0>(segment, p[0]);
        geometry::detail::assign_point_from_index<1>(segment, p[1]);

        boost::ignore_unused(strategies);

        auto closest_point = strategies.closest_points(point, segment)
            .apply(point, p[0], p[1]);

        set_segment_from_points::apply(point, closest_point, shortest_seg);
    }
};

/*
struct point_to_box
{
    template<typename Point, typename Box, typename Strategies>
static inline auto apply(Point const& point, Box const& box,
                             Strategies const& strategies)
    {
        boost::ignore_unused(strategies);
        return strategies.closest_points(point, box).apply(point, box);
    }
};
*/

template <closure_selector Closure>
class point_to_range
{
public:

    template <typename Point, typename Range, typename Segment, typename Strategies>
    static inline void apply(Point const& point, Range const& range,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        using point_to_point_range = detail::closest_feature::point_to_point_range
            <
                Point, Range, Closure
            >;

        if (boost::size(range) == 0)
        {
            set_segment_from_points::apply(point, point, shortest_seg);
            return;
        }

        closest_points::creturn_t<Point, Range, Strategies> cd_min;

        auto comparable_distance = strategy::distance::services::get_comparable
            <
                decltype(strategies.distance(point, range))
            >::apply(strategies.distance(point, range));

        auto closest_segment = point_to_point_range::apply(point,
                                                           boost::begin(range),
                                                           boost::end(range),
                                                           comparable_distance,
                                                           cd_min);

        auto closest_point = strategies.closest_points(point, range)
            .apply(point, *closest_segment.first, *closest_segment.second);

        set_segment_from_points::apply(point, closest_point, shortest_seg);
    }
};


template<closure_selector Closure>
struct point_to_ring
{
    template <typename Point, typename Ring, typename Segment, typename Strategies>
    static inline auto apply(Point const& point,
                             Ring const& ring,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        if (within::within_point_geometry(point, ring, strategies))
        {
            set_segment_from_points::apply(point, point, shortest_seg);
        }
        else
        {
            point_to_range
            <
                closure<Ring>::value
            >::apply(point, ring, shortest_seg, strategies);
        }

    }
};


template <closure_selector Closure>
class point_to_polygon
{
    template <typename Polygon>
    struct distance_to_interior_rings
    {
        template
        <
            typename Point,
            typename InteriorRingIterator,
            typename Segment,
            typename Strategies
        >
        static inline void apply(Point const& point,
                                 InteriorRingIterator first,
                                 InteriorRingIterator last,
                                 Segment& shortest_seg,
                                 Strategies const& strategies)
        {
            using per_ring = point_to_range<Closure>;

            for (InteriorRingIterator it = first; it != last; ++it)
            {
                if (within::within_point_geometry(point, *it, strategies))
                {
                    // the point is inside a polygon hole, so its distance
                    // to the polygon is its distance to the polygon's
                    // hole boundary
                    per_ring::apply(point, *it, shortest_seg, strategies);
                    return;
                }
            }
            set_segment_from_points::apply(point, point, shortest_seg);
        }

        template
        <
            typename Point,
            typename InteriorRings,
            typename Segment,
            typename Strategies
        >
        static inline void apply(Point const& point, InteriorRings const& interior_rings,
                                 Segment& shortest_seg, Strategies const& strategies)
        {
            apply(point,
                  boost::begin(interior_rings),
                  boost::end(interior_rings),
                  shortest_seg,
                  strategies);
        }
    };


public:
    template
    <
        typename Point,
        typename Polygon,
        typename Segment,
        typename Strategies
    >
    static inline void apply(Point const& point,
                             Polygon const& polygon,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        using per_ring = point_to_range<Closure>;

        if (! within::covered_by_point_geometry(point, exterior_ring(polygon),
                                                strategies))
        {
            // the point is outside the exterior ring, so its distance
            // to the polygon is its distance to the polygon's exterior ring
            per_ring::apply(point, exterior_ring(polygon), shortest_seg, strategies);
            return;
        }

        // Check interior rings
        distance_to_interior_rings<Polygon>::apply(point,
                                                   interior_rings(polygon),
                                                   shortest_seg,
                                                   strategies);
    }
};


template
<
    typename MultiGeometry,
    bool CheckCoveredBy = std::is_same
        <
            typename tag<MultiGeometry>::type, multi_polygon_tag
        >::value
>
class point_to_multigeometry
{
private:
    using geometry_to_range = detail::closest_feature::geometry_to_range;

public:

    template
    <
        typename Point,
        typename Segment,
        typename Strategies
    >
    static inline void apply(Point const& point,
                             MultiGeometry const& multigeometry,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        using selector_type = distance::iterator_selector<MultiGeometry const>;

        closest_points::creturn_t<Point, MultiGeometry, Strategies> cd;

        auto comparable_distance = strategy::distance::services::get_comparable
            <
                decltype(strategies.distance(point, multigeometry))
            >::apply(strategies.distance(point, multigeometry));

        typename selector_type::iterator_type it_min
            = geometry_to_range::apply(point,
                                       selector_type::begin(multigeometry),
                                       selector_type::end(multigeometry),
                                       comparable_distance,
                                       cd);

        dispatch::closest_points
            <
                Point,
                typename std::iterator_traits
                    <
                        typename selector_type::iterator_type
                    >::value_type
            >::apply(point, *it_min, shortest_seg, strategies);
    }
};


// this is called only for multipolygons, hence the change in the
// template parameter name MultiGeometry to MultiPolygon
template <typename MultiPolygon>
struct point_to_multigeometry<MultiPolygon, true>
{
    template
    <
        typename Point,
        typename Segment,
        typename Strategies
    >
    static inline void apply(Point const& point,
                             MultiPolygon const& multipolygon,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        if (within::covered_by_point_geometry(point, multipolygon, strategies))
        {
            set_segment_from_points::apply(point, point, shortest_seg);
            return;
        }

        return point_to_multigeometry
            <
                MultiPolygon, false
            >::apply(point, multipolygon, shortest_seg, strategies);
    }
};


}} // namespace detail::closest_points
#endif // DOXYGEN_NO_DETAIL




#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template <typename P1, typename P2>
struct closest_points
    <
        P1, P2, point_tag, point_tag, false
    > : detail::closest_points::point_to_point
{};


template <typename Point, typename Linestring>
struct closest_points
    <
        Point, Linestring, point_tag, linestring_tag, false
    > : detail::closest_points::point_to_range<closed>
{};


template <typename Point, typename Ring>
struct closest_points
    <
        Point, Ring, point_tag, ring_tag, false
    > : detail::closest_points::point_to_ring
        <
            closure<Ring>::value
        >
{};


template <typename Point, typename Polygon>
struct closest_points
    <
        Point, Polygon, point_tag, polygon_tag, false
    > : detail::closest_points::point_to_polygon
        <
            closure<Polygon>::value
        >
{};


template <typename Point, typename Segment>
struct closest_points
    <
        Point, Segment, point_tag, segment_tag, false
    > : detail::closest_points::point_to_segment
{};

/*
template <typename Point, typename Box>
struct closest_points
    <
         Point, Box, point_tag, box_tag,
         strategy_tag_distance_point_box, false
    > : detail::closest_points::point_to_box<Point, Box>
{};
*/

template<typename Point, typename MultiPoint>
struct closest_points
    <
        Point, MultiPoint, point_tag, multi_point_tag, false
    > : detail::closest_points::point_to_multigeometry<MultiPoint>
{};


template<typename Point, typename MultiLinestring>
struct closest_points
    <
        Point, MultiLinestring, point_tag, multi_linestring_tag, false
    > : detail::closest_points::point_to_multigeometry<MultiLinestring>
{};


template<typename Point, typename MultiPolygon>
struct closest_points
    <
        Point, MultiPolygon, point_tag, multi_polygon_tag, false
    > : detail::closest_points::point_to_multigeometry<MultiPolygon>
{};


template <typename Point, typename Linear>
struct closest_points
    <
         Point, Linear, point_tag, linear_tag, false
    > : closest_points
        <
            Point, Linear,
            point_tag, typename tag<Linear>::type, false
        >
{};


template <typename Point, typename Areal>
struct closest_points
    <
         Point, Areal, point_tag, areal_tag, false
    > : closest_points
        <
            Point, Areal,
            point_tag, typename tag<Areal>::type, false
        >
{};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_POINT_TO_GEOMETRY_HPP
