// Boost.Geometry

// Copyright (c) 2021-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_RANGE_TO_GEOMETRY_RTREE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_RANGE_TO_GEOMETRY_RTREE_HPP

#include <iterator>
#include <utility>

#include <boost/geometry/algorithms/detail/closest_feature/range_to_range.hpp>
#include <boost/geometry/algorithms/detail/closest_points/utilities.hpp>
#include <boost/geometry/algorithms/detail/distance/is_comparable.hpp>
#include <boost/geometry/algorithms/detail/distance/iterator_selector.hpp>
#include <boost/geometry/algorithms/detail/distance/strategy_utils.hpp>
#include <boost/geometry/algorithms/dispatch/closest_points.hpp>

#include <boost/geometry/core/assert.hpp>
#include <boost/geometry/core/point_type.hpp>

#include <boost/geometry/iterators/detail/has_one_element.hpp>

#include <boost/geometry/strategies/distance.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace closest_points
{

class point_or_segment_range_to_geometry_rtree
{
public:

    template
    <
        typename PointOrSegmentIterator,
        typename Geometry,
        typename Segment,
        typename Strategies
    >
    static inline void apply(PointOrSegmentIterator first,
                             PointOrSegmentIterator last,
                             Geometry const& geometry,
                             Segment& shortest_seg,
                             Strategies const& strategies)
    {
        typedef typename std::iterator_traits
        <
            PointOrSegmentIterator
        >::value_type point_or_segment_type;

        typedef distance::iterator_selector<Geometry const> selector_type;

        typedef detail::closest_feature::range_to_range_rtree range_to_range;

        BOOST_GEOMETRY_ASSERT( first != last );

        //TODO: Is this special case needed?
        //if ( detail::has_one_element(first, last) )
        //{
        //    dispatch::closest_points
        //        <
        //            point_or_segment_type, Geometry
        //        >::apply(*first, geometry, shortest_seg, strategies);
        //}

        closest_points::creturn_t<point_or_segment_type, Geometry, Strategies> cd;

        std::pair
            <
                point_or_segment_type,
                typename selector_type::iterator_type
            > closest_features
            = range_to_range::apply(first,
                                    last,
                                    selector_type::begin(geometry),
                                    selector_type::end(geometry),
                                    strategies,
                                    cd);
        dispatch::closest_points
                <
                    point_or_segment_type,
                    typename std::iterator_traits
                        <
                            typename selector_type::iterator_type
                        >::value_type
                >::apply(closest_features.first,
                         *closest_features.second,
                         shortest_seg,
                         strategies);
    }
};


}} // namespace detail::closest_points
#endif // DOXYGEN_NO_DETAIL


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_RANGE_TO_GEOMETRY_RTREE_HPP
