// Boost.Geometry

// Copyright (c) 2020-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_CONVEX_HULL_CARTESIAN_HPP
#define BOOST_GEOMETRY_STRATEGIES_CONVEX_HULL_CARTESIAN_HPP

#include <boost/geometry/strategies/cartesian/point_in_point.hpp>
#include <boost/geometry/strategies/convex_hull/services.hpp>
#include <boost/geometry/strategies/compare.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/side.hpp>

#include <boost/geometry/strategy/cartesian/side_robust.hpp>

#include <boost/geometry/util/type_traits.hpp>


namespace boost { namespace geometry
{

namespace strategies { namespace convex_hull
{

template <typename CalculationType = void>
class cartesian : public strategies::detail::cartesian_base
{
public:
    template <typename Geometry1, typename Geometry2>
    static auto relate(Geometry1 const&, Geometry2 const&,
                       std::enable_if_t
                            <
                                util::is_pointlike<Geometry1>::value
                             && util::is_pointlike<Geometry2>::value
                            > * = nullptr)
    {
        return strategy::within::cartesian_point_point();
    }

    static auto side()
    {
        using side_strategy_type
            = strategy::side::side_robust<CalculationType, strategy::side::fp_equals_policy>;
        return side_strategy_type();
    }

    template <typename ComparePolicy, typename EqualsPolicy>
    using compare_type = typename strategy::compare::cartesian
        <
            ComparePolicy,
            EqualsPolicy,
            -1
        >;
};

namespace services
{

template <typename Geometry>
struct default_strategy<Geometry, cartesian_tag>
{
    using type = strategies::convex_hull::cartesian<>;
};

} // namespace services

}} // namespace strategies::convex_hull

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_CONVEX_HULL_CARTESIAN_HPP
