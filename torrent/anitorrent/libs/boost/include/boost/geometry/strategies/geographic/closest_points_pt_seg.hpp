// Boost.Geometry

// Copyright (c) 2023 Adam Wulkiewicz, Lodz, Poland.

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_CLOSEST_POINTS_CROSS_TRACK_HPP
#define BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_CLOSEST_POINTS_CROSS_TRACK_HPP

#include <boost/geometry/core/coordinate_dimension.hpp>
#include <boost/geometry/core/coordinate_promotion.hpp>
#include <boost/geometry/core/coordinate_system.hpp>
#include <boost/geometry/core/radian_access.hpp>
#include <boost/geometry/geometries/point.hpp>
#include <boost/geometry/srs/spheroid.hpp>
#include <boost/geometry/strategies/geographic/distance_cross_track.hpp>
#include <boost/geometry/util/select_calculation_type.hpp>

namespace boost { namespace geometry
{

namespace strategy { namespace closest_points
{

template
<
    typename FormulaPolicy = geometry::strategy::andoyer,
    typename Spheroid = srs::spheroid<double>,
    typename CalculationType = void
>
class geographic_cross_track
    : public distance::detail::geographic_cross_track
        <
            FormulaPolicy,
            Spheroid,
            CalculationType,
            false,
            true
        >
{
    using base_t = distance::detail::geographic_cross_track
        <
            FormulaPolicy,
            Spheroid,
            CalculationType,
            false,
            true
        >;

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

public :
    explicit geographic_cross_track(Spheroid const& spheroid = Spheroid())
        : base_t(spheroid)
        {}

        template <typename Point, typename PointOfSegment>
        auto apply(Point const& p,
                   PointOfSegment const& sp1,
                   PointOfSegment const& sp2) const
        {
            auto result = base_t::apply(get_as_radian<0>(sp1), get_as_radian<1>(sp1),
                                        get_as_radian<0>(sp2), get_as_radian<1>(sp2),
                                        get_as_radian<0>(p), get_as_radian<1>(p),
                                        base_t::m_spheroid);

            model::point
                <
                    typename calculation_type<Point, PointOfSegment>::type,
                    dimension<PointOfSegment>::value,
                    typename coordinate_system<PointOfSegment>::type
                > cp;

            geometry::set_from_radian<0>(cp, result.lon);
            geometry::set_from_radian<1>(cp, result.lat);

            return cp;
        }
};

}} // namespace strategy::closest_points

}} // namespace boost::geometry
#endif // BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_CLOSEST_POINTS_CROSS_TRACK_HPP
