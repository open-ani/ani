// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_SPHERICAL_HPP
#define BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_SPHERICAL_HPP

#include <boost/geometry/strategies/spherical/closest_points_pt_seg.hpp>

#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/distance/detail.hpp>
#include <boost/geometry/strategies/closest_points/services.hpp>

#include <boost/geometry/strategies/distance/spherical.hpp>

#include <boost/geometry/util/type_traits.hpp>


namespace boost { namespace geometry
{

namespace strategies { namespace closest_points
{

template
<
    typename RadiusTypeOrSphere = double,
    typename CalculationType = void
>
class spherical
    : public strategies::distance::spherical<RadiusTypeOrSphere, CalculationType>
{
    using base_t = strategies::distance::spherical<RadiusTypeOrSphere, CalculationType>;

public:
    spherical() = default;

    template <typename RadiusOrSphere>
    explicit spherical(RadiusOrSphere const& radius_or_sphere)
        : base_t(radius_or_sphere)
    {}

    template <typename Geometry1, typename Geometry2>
    auto closest_points(Geometry1 const&, Geometry2 const&,
                        distance::detail::enable_if_ps_t<Geometry1, Geometry2> * = nullptr) const
    {
        return strategy::closest_points::cross_track<CalculationType>(base_t::radius());
    }
};


namespace services
{

template <typename Geometry1, typename Geometry2>
struct default_strategy
<
    Geometry1,
    Geometry2,
    spherical_equatorial_tag,
    spherical_equatorial_tag
>
{
    using type = strategies::closest_points::spherical<>;
};

} // namespace services

}} // namespace strategies::closest_points

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_CLOSEST_POINTS_SPHERICAL_HPP
