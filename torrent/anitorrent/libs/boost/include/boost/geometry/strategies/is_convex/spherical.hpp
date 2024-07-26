// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_IS_CONVEX_SPHERICAL_HPP
#define BOOST_GEOMETRY_STRATEGIES_IS_CONVEX_SPHERICAL_HPP


#include <boost/geometry/strategies/convex_hull/spherical.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/is_convex/services.hpp>


namespace boost { namespace geometry
{

namespace strategies { namespace is_convex
{


template <typename CalculationType = void>
using spherical = strategies::convex_hull::spherical<CalculationType>;


namespace services
{

template <typename Geometry>
struct default_strategy<Geometry, spherical_equatorial_tag>
{
    using type = strategies::is_convex::spherical<>;
};

template <typename CT>
struct strategy_converter<strategy::side::spherical_side_formula<CT>>
{
    static auto get(strategy::side::spherical_side_formula<CT> const& )
    {
        return strategies::is_convex::spherical<CT>();
    }
};

} // namespace services

}} // namespace strategies::is_convex

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_IS_CONVEX_SPHERICAL_HPP
