// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_IS_CONVEX_GEOGRAPHIC_HPP
#define BOOST_GEOMETRY_STRATEGIES_IS_CONVEX_GEOGRAPHIC_HPP


#include <boost/geometry/strategies/convex_hull/geographic.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/is_convex/services.hpp>


namespace boost { namespace geometry
{

namespace strategies { namespace is_convex
{


template
<
    typename FormulaPolicy = strategy::andoyer,
    typename Spheroid = srs::spheroid<double>,
    typename CalculationType = void
>
using geographic = strategies::convex_hull::geographic<FormulaPolicy, Spheroid, CalculationType>;


namespace services
{

template <typename Geometry>
struct default_strategy<Geometry, geographic_tag>
{
    using type = strategies::convex_hull::geographic<>;
};

template <typename FP, typename S, typename CT>
struct strategy_converter<strategy::side::geographic<FP, S, CT>>
{
    static auto get(strategy::side::geographic<FP, S, CT> const& s)
    {
        return strategies::is_convex::geographic<FP, S, CT>(s.model());
    }
};

} // namespace services

}} // namespace strategies::is_convex

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_IS_CONVEX_GEOGRAPHIC_HPP
