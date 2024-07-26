// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2014 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2014 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2014 Mateusz Loskot, London, UK.
// Copyright (c) 2023 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2014-2021.
// Modifications copyright (c) 2014-2021, Oracle and/or its affiliates.

// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_COMPARABLE_DISTANCE_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_COMPARABLE_DISTANCE_INTERFACE_HPP


#include <boost/geometry/algorithms/detail/distance/interface.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/strategies/comparable_distance_result.hpp>
#include <boost/geometry/strategies/default_comparable_distance_result.hpp>
#include <boost/geometry/strategies/distance.hpp>

#include <boost/geometry/strategies/distance/comparable.hpp>
#include <boost/geometry/strategies/distance/services.hpp>


namespace boost { namespace geometry
{


namespace resolve_strategy
{


template
<
    typename Strategies,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategies>::value
>
struct comparable_distance
{
    template <typename Geometry1, typename Geometry2>
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategies const& strategies)
    {
        return dispatch::distance
            <
                Geometry1, Geometry2,
                strategies::distance::detail::comparable<Strategies>
            >::apply(geometry1,
                     geometry2,
                     strategies::distance::detail::comparable<Strategies>(strategies));
    }
};

template <typename Strategy>
struct comparable_distance<Strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using strategies::distance::services::strategy_converter;

        using comparable_strategies_type = strategies::distance::detail::comparable
            <
                decltype(strategy_converter<Strategy>::get(strategy))
            >;

        return dispatch::distance
            <
                Geometry1, Geometry2,
                comparable_strategies_type
            >::apply(geometry1,
                     geometry2,
                     comparable_strategies_type(
                         strategy_converter<Strategy>::get(strategy)));
    }
};

template <>
struct comparable_distance<default_strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             default_strategy)
    {
        using comparable_strategy_type = strategies::distance::detail::comparable
            <
                typename strategies::distance::services::default_strategy
                    <
                        Geometry1, Geometry2
                    >::type
            >;

        return dispatch::distance
            <
                Geometry1, Geometry2, comparable_strategy_type
            >::apply(geometry1, geometry2, comparable_strategy_type());
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
struct comparable_distance
{
    template <typename Strategy>
    static inline auto apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        return resolve_strategy::comparable_distance
            <
                Strategy
            >::apply(geometry1, geometry2, strategy);
    }
};


template <typename DynamicGeometry1, typename Geometry2, typename Tag2>
struct comparable_distance<DynamicGeometry1, Geometry2, dynamic_geometry_tag, Tag2>
{
    template <typename Strategy>
    static inline auto apply(DynamicGeometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using result_t = typename geometry::comparable_distance_result
            <
                DynamicGeometry1, Geometry2, Strategy
            >::type;
        result_t result = 0;
        traits::visit<DynamicGeometry1>::apply([&](auto const& g1)
        {
            result = resolve_strategy::comparable_distance
                        <
                            Strategy
                        >::apply(g1, geometry2, strategy);
        }, geometry1);
        return result;
    }
};


template <typename Geometry1, typename DynamicGeometry2, typename Tag1>
struct comparable_distance<Geometry1, DynamicGeometry2, Tag1, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline auto apply(Geometry1 const& geometry1,
                             DynamicGeometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using result_t = typename geometry::comparable_distance_result
            <
                Geometry1, DynamicGeometry2, Strategy
            >::type;
        result_t result = 0;
        traits::visit<DynamicGeometry2>::apply([&](auto const& g2)
        {
            result = resolve_strategy::comparable_distance
                        <
                            Strategy
                        >::apply(geometry1, g2, strategy);
        }, geometry2);
        return result;
    }
};


template <typename DynamicGeometry1, typename DynamicGeometry2>
struct comparable_distance
    <
        DynamicGeometry1, DynamicGeometry2,
        dynamic_geometry_tag, dynamic_geometry_tag
    >
{
    template <typename Strategy>
    static inline auto apply(DynamicGeometry1 const& geometry1,
                             DynamicGeometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using result_t = typename geometry::comparable_distance_result
            <
                DynamicGeometry1, DynamicGeometry2, Strategy
            >::type;
        result_t result = 0;
        traits::visit<DynamicGeometry1, DynamicGeometry2>::apply([&](auto const& g1, auto const& g2)
        {
            result = resolve_strategy::comparable_distance
                        <
                            Strategy
                        >::apply(g1, g2, strategy);
        }, geometry1, geometry2);
        return result;
    }
};

} // namespace resolve_dynamic



/*!
\brief \brief_calc2{comparable distance measurement} \brief_strategy
\ingroup distance
\details The free function comparable_distance does not necessarily calculate the distance,
    but it calculates a distance measure such that two distances are comparable to each other.
    For example: for the Cartesian coordinate system, Pythagoras is used but the square root
    is not taken, which makes it faster and the results of two point pairs can still be
    compared to each other.
\tparam Geometry1 first geometry type
\tparam Geometry2 second geometry type
\tparam Strategy \tparam_strategy{Distance}
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param strategy \param_strategy{distance}
\return \return_calc{comparable distance}

\qbk{distinguish,with strategy}
 */
template <typename Geometry1, typename Geometry2, typename Strategy>
inline auto comparable_distance(Geometry1 const& geometry1,
                                Geometry2 const& geometry2,
                                Strategy const& strategy)
{
    concepts::check<Geometry1 const>();
    concepts::check<Geometry2 const>();

    return resolve_dynamic::comparable_distance
        <
            Geometry1,
            Geometry2
        >::apply(geometry1, geometry2, strategy);
}



/*!
\brief \brief_calc2{comparable distance measurement}
\ingroup distance
\details The free function comparable_distance does not necessarily calculate the distance,
    but it calculates a distance measure such that two distances are comparable to each other.
    For example: for the Cartesian coordinate system, Pythagoras is used but the square root
    is not taken, which makes it faster and the results of two point pairs can still be
    compared to each other.
\tparam Geometry1 first geometry type
\tparam Geometry2 second geometry type
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\return \return_calc{comparable distance}

\qbk{[include reference/algorithms/comparable_distance.qbk]}
 */
template <typename Geometry1, typename Geometry2>
inline auto comparable_distance(Geometry1 const& geometry1,
                                Geometry2 const& geometry2)
{
    return geometry::comparable_distance(geometry1, geometry2, default_strategy());
}


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_COMPARABLE_DISTANCE_INTERFACE_HPP
