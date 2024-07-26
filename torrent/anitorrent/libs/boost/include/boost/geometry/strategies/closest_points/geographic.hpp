// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_GEOGRAPHIC_HPP
#define BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_GEOGRAPHIC_HPP

#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/distance/detail.hpp>
#include <boost/geometry/strategies/closest_points/services.hpp>
#include <boost/geometry/strategies/geographic/closest_points_pt_seg.hpp>
#include <boost/geometry/strategies/geographic/parameters.hpp>

#include <boost/geometry/strategies/distance/geographic.hpp>

#include <boost/geometry/util/type_traits.hpp>


namespace boost { namespace geometry
{

namespace strategies { namespace closest_points
{

template
<
    typename FormulaPolicy = geometry::strategy::andoyer,
    typename Spheroid = srs::spheroid<double>,
    typename CalculationType = void
>
class geographic
    : public strategies::distance::geographic<FormulaPolicy, Spheroid, CalculationType>
{
    using base_t = strategies::distance::geographic<FormulaPolicy, Spheroid, CalculationType>;

public:

    geographic() = default;

    explicit geographic(Spheroid const& spheroid)
        : base_t(spheroid)
    {}

    template <typename Geometry1, typename Geometry2>
    auto closest_points(Geometry1 const&, Geometry2 const&,
                        distance::detail::enable_if_ps_t<Geometry1, Geometry2> * = nullptr) const
    {
        return strategy::closest_points::geographic_cross_track
            <
                FormulaPolicy,
                Spheroid,
                CalculationType
            >(base_t::m_spheroid);
    }
};


namespace services
{

template <typename Geometry1, typename Geometry2>
struct default_strategy<Geometry1, Geometry2, geographic_tag, geographic_tag>
{
    using type = strategies::closest_points::geographic<>;
};

} // namespace services

}} // namespace strategies::closest_points

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_GEOGRAPHIC_HPP
