// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_SPHERICAL_CLOSEST_POINTS_CROSS_TRACK_HPP
#define BOOST_GEOMETRY_STRATEGIES_SPHERICAL_CLOSEST_POINTS_CROSS_TRACK_HPP

#include <algorithm>
#include <type_traits>

#include <boost/config.hpp>
#include <boost/concept_check.hpp>

#include <boost/geometry/core/cs.hpp>
#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/coordinate_promotion.hpp>
#include <boost/geometry/core/radian_access.hpp>
#include <boost/geometry/core/tags.hpp>

#include <boost/geometry/formulas/spherical.hpp>

#include <boost/geometry/strategies/distance.hpp>
#include <boost/geometry/strategies/concepts/distance_concept.hpp>
#include <boost/geometry/strategies/spherical/distance_haversine.hpp>
#include <boost/geometry/strategies/spherical/distance_cross_track.hpp>
#include <boost/geometry/strategies/spherical/point_in_point.hpp>
#include <boost/geometry/strategies/spherical/intersection.hpp>

#include <boost/geometry/util/math.hpp>
#include <boost/geometry/util/select_calculation_type.hpp>

#ifdef BOOST_GEOMETRY_DEBUG_CROSS_TRACK
#  include <boost/geometry/io/dsv/write.hpp>
#endif


namespace boost { namespace geometry
{

namespace strategy { namespace closest_points
{

template
<
    typename CalculationType = void,
    typename Strategy = distance::comparable::haversine<double, CalculationType>
>
class cross_track
{
public:
    template <typename Point, typename PointOfSegment>
    struct calculation_type
        : promote_floating_point
          <
              typename select_calculation_type
                  <
                      Point,
                      PointOfSegment,
                      CalculationType
                  >::type
          >
    {};

    using radius_type = typename Strategy::radius_type;

    cross_track() = default;

    explicit inline cross_track(typename Strategy::radius_type const& r)
        : m_strategy(r)
    {}

    inline cross_track(Strategy const& s)
        : m_strategy(s)
    {}

    template <typename Point, typename PointOfSegment>
    inline auto apply(Point const& p,
                      PointOfSegment const& sp1,
                      PointOfSegment const& sp2) const
    {
        using CT = typename calculation_type<Point, PointOfSegment>::type;

        // http://williams.best.vwh.net/avform.htm#XTE
        CT d3 = m_strategy.apply(sp1, sp2);

        if (geometry::math::equals(d3, 0.0))
        {
            // "Degenerate" segment, return either d1 or d2
            return sp1;
        }

        CT d1 = m_strategy.apply(sp1, p);
        CT d2 = m_strategy.apply(sp2, p);

        auto d_crs_pair = distance::detail::compute_cross_track_pair<CT>::apply(
            p, sp1, sp2);

        // d1, d2, d3 are in principle not needed, only the sign matters
        CT projection1 = cos(d_crs_pair.first) * d1 / d3;
        CT projection2 = cos(d_crs_pair.second) * d2 / d3;

#ifdef BOOST_GEOMETRY_DEBUG_CROSS_TRACK
        std::cout << "Course " << dsv(sp1) << " to " << dsv(p) << " "
                  << crs_AD * geometry::math::r2d<CT>() << std::endl;
        std::cout << "Course " << dsv(sp1) << " to " << dsv(sp2) << " "
                  << crs_AB * geometry::math::r2d<CT>() << std::endl;
        std::cout << "Course " << dsv(sp2) << " to " << dsv(sp1) << " "
                  << crs_BA * geometry::math::r2d<CT>() << std::endl;
        std::cout << "Course " << dsv(sp2) << " to " << dsv(p) << " "
                  << crs_BD * geometry::math::r2d<CT>() << std::endl;
        std::cout << "Projection AD-AB " << projection1 << " : "
                  << d_crs1 * geometry::math::r2d<CT>() << std::endl;
        std::cout << "Projection BD-BA " << projection2 << " : "
                  << d_crs2 * geometry::math::r2d<CT>() << std::endl;
        std::cout << " d1: " << (d1 )
                  << " d2: " << (d2 )
                  << std::endl;
#endif

        if (projection1 > 0.0 && projection2 > 0.0)
        {
#ifdef BOOST_GEOMETRY_DEBUG_CROSS_TRACK
            CT XTD = radius() * geometry::math::abs( asin( sin( d1 ) * sin( d_crs1 ) ));

            std::cout << "Projection ON the segment" << std::endl;
            std::cout << "XTD: " << XTD
                      << " d1: " << (d1 * radius())
                      << " d2: " << (d2 * radius())
                      << std::endl;
#endif
            auto distance = distance::detail::compute_cross_track_distance::apply(
                d_crs_pair.first, d1);

            CT lon1 = geometry::get_as_radian<0>(sp1);
            CT lat1 = geometry::get_as_radian<1>(sp1);
            CT lon2 = geometry::get_as_radian<0>(sp2);
            CT lat2 = geometry::get_as_radian<1>(sp2);

            CT dist = CT(2) * asin(math::sqrt(distance)) * m_strategy.radius();
            CT dist_d1 = CT(2) * asin(math::sqrt(d1)) * m_strategy.radius();

            // Note: this is similar to spherical computation in geographic
            // point_segment_distance formula
            CT earth_radius = m_strategy.radius();
            CT cos_frac = cos(dist_d1 / earth_radius) / cos(dist / earth_radius);
            CT s14_sph = cos_frac >= 1
                ? CT(0) : cos_frac <= -1 ? math::pi<CT>() * earth_radius
                                         : acos(cos_frac) * earth_radius;

            CT a12 = geometry::formula::spherical_azimuth<>(lon1, lat1, lon2, lat2);
            auto res_direct = geometry::formula::spherical_direct
                <
                    true,
                    false
                >(lon1, lat1, s14_sph, a12, srs::sphere<CT>(earth_radius));

            model::point
                <
                    CT,
                    dimension<PointOfSegment>::value,
                    typename coordinate_system<PointOfSegment>::type
                > cp;

            geometry::set_from_radian<0>(cp, res_direct.lon2);
            geometry::set_from_radian<1>(cp, res_direct.lat2);

            return cp;
        }
        else
        {
#ifdef BOOST_GEOMETRY_DEBUG_CROSS_TRACK
            std::cout << "Projection OUTSIDE the segment" << std::endl;
#endif
            return d1 < d2 ? sp1 : sp2;
        }
    }

    template <typename T1, typename T2>
    inline radius_type vertical_or_meridian(T1 lat1, T2 lat2) const
    {
        return m_strategy.radius() * (lat1 - lat2);
    }

    inline typename Strategy::radius_type radius() const
    { return m_strategy.radius(); }

private :
    Strategy m_strategy;
};

}} // namespace strategy::closest_points

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_SPHERICAL_CLOSEST_POINTS_CROSS_TRACK_HPP
