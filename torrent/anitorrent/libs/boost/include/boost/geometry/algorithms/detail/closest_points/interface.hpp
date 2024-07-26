// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_INTERFACE_HPP

#include <boost/concept_check.hpp>

#include <boost/geometry/algorithms/detail/throw_on_empty_input.hpp>
#include <boost/geometry/algorithms/detail/closest_points/utilities.hpp>

#include <boost/geometry/algorithms/dispatch/closest_points.hpp>

#include <boost/geometry/algorithms/detail/distance/interface.hpp>

#include <boost/geometry/core/point_type.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/closest_points/services.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


// If reversal is needed, perform it

template
<
    typename Geometry1,
    typename Geometry2,
    typename Tag1,
    typename Tag2
>
struct closest_points
<
    Geometry1, Geometry2,
    Tag1, Tag2, true
>
    : closest_points<Geometry2, Geometry1, Tag2, Tag1, false>
{
    template <typename Segment, typename Strategy>
    static inline void apply(Geometry1 const& g1, Geometry2 const& g2,
                             Segment& shortest_seg, Strategy const& strategy)
    {
        closest_points
            <
                Geometry2, Geometry1, Tag2, Tag1, false
            >::apply(g2, g1, shortest_seg, strategy);

        detail::closest_points::swap_segment_points::apply(shortest_seg);
    }
};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_strategy
{

template<typename Strategy>
struct closest_points
{
    template <typename Geometry1, typename Geometry2, typename Segment>
    static inline void apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Segment& shortest_seg,
                             Strategy const& strategy)
    {
        dispatch::closest_points
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, shortest_seg, strategy);
    }
};

template <>
struct closest_points<default_strategy>
{
    template <typename Geometry1, typename Geometry2, typename Segment>
    static inline void
    apply(Geometry1 const& geometry1,
          Geometry2 const& geometry2,
          Segment& shortest_seg,
          default_strategy)
    {
        using strategy_type = typename strategies::closest_points::services::default_strategy
            <
                Geometry1, Geometry2
            >::type;

        dispatch::closest_points
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, shortest_seg, strategy_type());
    }
};

} // namespace resolve_strategy


namespace resolve_variant
{


template <typename Geometry1, typename Geometry2>
struct closest_points
{
    template <typename Segment, typename Strategy>
    static inline void apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Segment& shortest_seg,
                             Strategy const& strategy)
    {
        resolve_strategy::closest_points
            <
                Strategy
            >::apply(geometry1, geometry2, shortest_seg, strategy);
    }
};

//TODO: Add support for DG/GC

} // namespace resolve_variant


/*!
\brief Calculate the closest points between two geometries \brief_strategy
\ingroup closest_points
\details
\details The free function closest_points calculates the distance between two geometries \brief_strategy. \details_strategy_reasons
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam Segment Any type fulfilling a Segment Concept
\tparam Strategy \tparam_strategy{Closest Points}
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param shortest_seg Output segment containing the closest points
\param strategy \param_strategy{closest_points}
\note The strategy can be a point-point strategy. In case of distance point-line/point-polygon
    it may also be a point-segment strategy.

\qbk{distinguish,with strategy}

\qbk{

[heading Example]
[closest_points_strategy]
[closest_points_strategy_output]

[heading See also]
\* [link geometry.reference.algorithms.distance distance]
}
*/

template <typename Geometry1, typename Geometry2, typename Segment, typename Strategy>
inline void closest_points(Geometry1 const& geometry1,
                           Geometry2 const& geometry2,
                           Segment& shortest_seg,
                           Strategy const& strategy)
{
    concepts::check<Geometry1 const>();
    concepts::check<Geometry2 const>();

    detail::throw_on_empty_input(geometry1);
    detail::throw_on_empty_input(geometry2);

    resolve_variant::closest_points
        <
            Geometry1,
            Geometry2
        >::apply(geometry1, geometry2, shortest_seg, strategy);
}


/*!
\brief Compute the closest points between two geometries.
\ingroup closest_points
\details The free function closest_points calculates the closest points between two geometries. \details_default_strategy
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam Segment Any type fulfilling a Segment Concept
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param shortest_seg Output segment containing the closest points

\qbk{

[heading Example]
[closest_points]
[closest_points_output]

[heading See also]
\* [link geometry.reference.algorithms.distance distance]
}
*/

template <typename Geometry1, typename Geometry2, typename Segment>
inline void closest_points(Geometry1 const& geometry1,
                           Geometry2 const& geometry2,
                           Segment& shortest_seg)
{
    closest_points(geometry1, geometry2, shortest_seg, default_strategy());
}

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_CLOSEST_POINTS_INTERFACE_HPP
