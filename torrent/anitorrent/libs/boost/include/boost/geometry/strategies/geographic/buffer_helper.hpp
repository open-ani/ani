// Boost.Geometry

// Copyright (c) 2022 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_HELPER_HPP
#define BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_HELPER_HPP

#include <boost/geometry/core/assert.hpp>
#include <boost/geometry/core/radian_access.hpp>
#include <boost/geometry/strategies/geographic/parameters.hpp>
#include <boost/geometry/util/math.hpp>


namespace boost { namespace geometry
{

namespace strategy { namespace buffer
{

#ifndef DOXYGEN_SHOULD_SKIP_THIS
template <typename FormulaPolicy, typename CalculationType>
struct geographic_buffer_helper
{
    static bool const enable_azimuth = true;
    static bool const enable_coordinates = true;

    using inverse = typename FormulaPolicy::template inverse
        <
            CalculationType, false, enable_azimuth, false, false, false
        >;

    using direct = typename FormulaPolicy::template direct
        <
            CalculationType, enable_coordinates, false, false, false
        >;

    // Calculates the azimuth using the inverse formula, where the first point
    // is specified by lon/lat (for pragmatic reasons) and the second point as a point.
    template <typename T, typename Point, typename Spheroid>
    static inline CalculationType azimuth(T const& lon_rad, T const& lat_rad,
                                          Point const& p, Spheroid const& spheroid)
    {
        return inverse::apply(lon_rad, lat_rad, get_as_radian<0>(p), get_as_radian<1>(p), spheroid).azimuth;
    }

    // Using specified points, distance and azimuth it calculates a new point
    // and appends it to the range
    template <typename T, typename Spheroid, typename RangeOut>
    static inline void append_point(T const& lon_rad, T const& lat_rad,
                                    T const& distance, T const& angle,
                                    Spheroid const& spheroid, RangeOut& range_out)
    {
        using point_t = typename boost::range_value<RangeOut>::type;
        point_t point;
        auto const d = direct::apply(lon_rad, lat_rad, distance, angle, spheroid);
        set_from_radian<0>(point, d.lon2);
        set_from_radian<1>(point, d.lat2);
        range_out.emplace_back(point);
    }

    // Calculates the angle diff and azimuth of a point (specified as lon/lat)
    // and two points, perpendicular in the buffer context.
    template <typename T, typename Point, typename Spheroid>
    static inline bool calculate_angles(T const& lon_rad, T const& lat_rad, Point const& perp1,
                                        Point const& perp2, Spheroid const& spheroid,
                                        T& angle_diff, T& first_azimuth)
    {
        T const inv1 = azimuth(lon_rad, lat_rad, perp1, spheroid);
        T const inv2 = azimuth(lon_rad, lat_rad, perp2, spheroid);

        static CalculationType const two_pi = geometry::math::two_pi<CalculationType>();
        static CalculationType const pi = geometry::math::pi<CalculationType>();

        // For a sharp corner, perpendicular points are nearly opposite and the
        // angle between the two azimuths can be nearly 180, but not more.
        angle_diff = inv2 < inv1 ? (two_pi + inv2) - inv1 : inv2 - inv1;

        if (angle_diff < 0 || angle_diff > pi)
        {
            // Defensive check with asserts
            BOOST_GEOMETRY_ASSERT(angle_diff >= 0);
            BOOST_GEOMETRY_ASSERT(angle_diff <= pi);
            return false;
        }

        first_azimuth = inv1;

        return true;
    }
};
#endif // DOXYGEN_SHOULD_SKIP_THIS

}} // namespace strategy::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_HELPER_HPP
