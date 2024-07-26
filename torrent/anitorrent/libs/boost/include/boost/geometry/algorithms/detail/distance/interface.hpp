// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2014 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2014 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2014 Mateusz Loskot, London, UK.
// Copyright (c) 2013-2014 Adam Wulkiewicz, Lodz, Poland.
// Copyright (c) 2014 Samuel Debionne, Grenoble, France.

// This file was modified by Oracle on 2014-2021.
// Modifications copyright (c) 2014-2021, Oracle and/or its affiliates.
// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_DISTANCE_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_DISTANCE_INTERFACE_HPP

#include <boost/concept_check.hpp>

#include <boost/geometry/algorithms/detail/throw_on_empty_input.hpp>
#include <boost/geometry/algorithms/dispatch/distance.hpp>

#include <boost/geometry/core/point_type.hpp>
#include <boost/geometry/core/visit.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/geometries/concepts/check.hpp>

// TODO: move these to algorithms
#include <boost/geometry/strategies/default_distance_result.hpp>
#include <boost/geometry/strategies/distance_result.hpp>

#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/distance/services.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


// If reversal is needed, perform it
template
<
    typename Geometry1, typename Geometry2, typename Strategy,
    typename Tag1, typename Tag2, typename StrategyTag
>
struct distance
<
    Geometry1, Geometry2, Strategy,
    Tag1, Tag2, StrategyTag,
    true
>
    : distance<Geometry2, Geometry1, Strategy, Tag2, Tag1, StrategyTag, false>
{
    static inline auto apply(Geometry1 const& g1, Geometry2 const& g2,
                             Strategy const& strategy)
    {
        return distance
            <
                Geometry2, Geometry1, Strategy,
                Tag2, Tag1, StrategyTag,
                false
            >::apply(g2, g1, strategy);
    }
};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_strategy
{

template
<
    typename Strategy,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategy>::value
>
struct distance
{
    template <typename Geometry1, typename Geometry2>
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        return dispatch::distance
            <
                Geometry1, Geometry2, Strategy
            >::apply(geometry1, geometry2, strategy);
    }
};

template <typename Strategy>
struct is_strategy_converter_specialized
{
    typedef strategies::distance::services::strategy_converter<Strategy> converter;
    static const bool value = ! std::is_same
        <
            decltype(converter::get(std::declval<Strategy>())),
            strategies::detail::not_implemented
        >::value;
};

template <typename Strategy>
struct distance<Strategy, false>
{
    template
    <
        typename Geometry1, typename Geometry2, typename S,
        std::enable_if_t<is_strategy_converter_specialized<S>::value, int> = 0
    >
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             S const& strategy)
    {
        typedef strategies::distance::services::strategy_converter<Strategy> converter;
        typedef decltype(converter::get(strategy)) strategy_type;

        return dispatch::distance
            <
                Geometry1, Geometry2, strategy_type
            >::apply(geometry1, geometry2, converter::get(strategy));
    }

    template
    <
        typename Geometry1, typename Geometry2, typename S,
        std::enable_if_t<! is_strategy_converter_specialized<S>::value, int> = 0
    >
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             S const& strategy)
    {
        typedef strategies::distance::services::custom_strategy_converter
            <
                Geometry1, Geometry2, Strategy
            > converter;
        typedef decltype(converter::get(strategy)) strategy_type;

        return dispatch::distance
            <
                Geometry1, Geometry2, strategy_type
            >::apply(geometry1, geometry2, converter::get(strategy));
    }
};

template <>
struct distance<default_strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             default_strategy)
    {
        typedef typename strategies::distance::services::default_strategy
            <
                Geometry1, Geometry2
            >::type strategy_type;

        return dispatch::distance
            <
                Geometry1, Geometry2, strategy_type
            >::apply(geometry1, geometry2, strategy_type());
    }
};

} // namespace resolve_strategy


namespace resolve_dynamic
{


template
<
    typename Geometry1, typename Geometry2,
    typename Tag1 = typename geometry::tag<Geometry1>::type,
    typename Tag2 = typename geometry::tag<Geometry2>::type
>
struct distance
{
    template <typename Strategy>
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        return resolve_strategy::distance
            <
                Strategy
            >::apply(geometry1, geometry2, strategy);
    }
};


template <typename DynamicGeometry1, typename Geometry2, typename Tag2>
struct distance<DynamicGeometry1, Geometry2, dynamic_geometry_tag, Tag2>
{
    template <typename Strategy>
    static inline auto apply(DynamicGeometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using result_t = typename geometry::distance_result<DynamicGeometry1, Geometry2, Strategy>::type;
        result_t result = 0;
        traits::visit<DynamicGeometry1>::apply([&](auto const& g1)
        {
            result = resolve_strategy::distance
                        <
                            Strategy
                        >::apply(g1, geometry2, strategy);
        }, geometry1);
        return result;
    }
};


template <typename Geometry1, typename DynamicGeometry2, typename Tag1>
struct distance<Geometry1, DynamicGeometry2, Tag1, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline auto apply(Geometry1 const& geometry1,
                             DynamicGeometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using result_t = typename geometry::distance_result<Geometry1, DynamicGeometry2, Strategy>::type;
        result_t result = 0;
        traits::visit<DynamicGeometry2>::apply([&](auto const& g2)
        {
            result = resolve_strategy::distance
                        <
                            Strategy
                        >::apply(geometry1, g2, strategy);
        }, geometry2);
        return result;
    }
};


template <typename DynamicGeometry1, typename DynamicGeometry2>
struct distance<DynamicGeometry1, DynamicGeometry2, dynamic_geometry_tag, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline auto apply(DynamicGeometry1 const& geometry1,
                             DynamicGeometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using result_t = typename geometry::distance_result<DynamicGeometry1, DynamicGeometry2, Strategy>::type;
        result_t result = 0;
        traits::visit<DynamicGeometry1, DynamicGeometry2>::apply([&](auto const& g1, auto const& g2)
        {
            result = resolve_strategy::distance
                        <
                            Strategy
                        >::apply(g1, g2, strategy);
        }, geometry1, geometry2);
        return result;
    }
};

} // namespace resolve_dynamic


/*!
\brief Calculate the distance between two geometries \brief_strategy
\ingroup distance
\details
\details The free function distance calculates the distance between two geometries \brief_strategy. \details_strategy_reasons

\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam Strategy \tparam_strategy{Distance}
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param strategy \param_strategy{distance}
\return \return_calc{distance}
\note The strategy can be a point-point strategy. In case of distance point-line/point-polygon
    it may also be a point-segment strategy.

\qbk{distinguish,with strategy}

\qbk{
[heading Available Strategies]
\* [link geometry.reference.strategies.strategy_distance_pythagoras Pythagoras (cartesian)]
\* [link geometry.reference.strategies.strategy_distance_haversine Haversine (spherical)]
\* [link geometry.reference.strategies.strategy_distance_cross_track Cross track (spherical\, point-to-segment)]
\* [link geometry.reference.strategies.strategy_distance_projected_point Projected point (cartesian\, point-to-segment)]
\* more (currently extensions): Vincenty\, Andoyer (geographic)
}
 */

/*
Note, in case of a Compilation Error:
if you get:
 - "Failed to specialize function template ..."
 - "error: no matching function for call to ..."
for distance, it is probably so that there is no specialization
for return_type<...> for your strategy.
*/
template <typename Geometry1, typename Geometry2, typename Strategy>
inline auto distance(Geometry1 const& geometry1,
                     Geometry2 const& geometry2,
                     Strategy const& strategy)
{
    concepts::check<Geometry1 const>();
    concepts::check<Geometry2 const>();

    detail::throw_on_empty_input(geometry1);
    detail::throw_on_empty_input(geometry2);

    return resolve_dynamic::distance
               <
                   Geometry1,
                   Geometry2
               >::apply(geometry1, geometry2, strategy);
}


/*!
\brief Calculate the distance between two geometries.
\ingroup distance
\details The free function distance calculates the distance between two geometries. \details_default_strategy

\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\return \return_calc{distance}

\qbk{[include reference/algorithms/distance.qbk]}
 */
template <typename Geometry1, typename Geometry2>
inline auto distance(Geometry1 const& geometry1,
                     Geometry2 const& geometry2)
{
    return geometry::distance(geometry1, geometry2, default_strategy());
}

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_DISTANCE_INTERFACE_HPP
