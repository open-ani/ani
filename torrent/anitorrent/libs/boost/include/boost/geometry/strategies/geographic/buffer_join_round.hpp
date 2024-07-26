// Boost.Geometry

// Copyright (c) 2022 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2023 Adam Wulkiewicz, Lodz, Poland.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_JOIN_ROUND_HPP
#define BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_JOIN_ROUND_HPP

#include <boost/range/value_type.hpp>

#include <boost/geometry/core/radian_access.hpp>

#include <boost/geometry/srs/spheroid.hpp>
#include <boost/geometry/strategies/buffer.hpp>
#include <boost/geometry/strategies/geographic/buffer_helper.hpp>
#include <boost/geometry/strategies/geographic/parameters.hpp>
#include <boost/geometry/util/math.hpp>
#include <boost/geometry/util/select_calculation_type.hpp>


namespace boost { namespace geometry
{

namespace strategy { namespace buffer
{

template
<
    typename FormulaPolicy = strategy::andoyer,
    typename Spheroid = srs::spheroid<double>,
    typename CalculationType = void
>
class geographic_join_round
{
public :

    //! \brief Constructs the strategy with a spheroid
    //! \param spheroid The spheroid to be used
    //! \param points_per_circle Number of points (minimum 4) that would be used for a full circle
    explicit inline geographic_join_round(Spheroid const& spheroid,
                                          std::size_t points_per_circle = default_points_per_circle)
        : m_spheroid(spheroid)
        , m_points_per_circle(get_point_count_for_join(points_per_circle))
    {}

    //! \brief Constructs the strategy
    //! \param points_per_circle Number of points (minimum 4) that would be used for a full circle
    explicit inline geographic_join_round(std::size_t points_per_circle = default_points_per_circle)
        : m_points_per_circle(get_point_count_for_join(points_per_circle))
    {}

#ifndef DOXYGEN_SHOULD_SKIP_THIS
    //! Fills output_range with a rounded shape around a vertex
    template <typename Point, typename DistanceType, typename RangeOut>
    inline bool apply(Point const& /*ip*/, Point const& vertex,
                      Point const& perp1, Point const& perp2,
                      DistanceType const& buffer_distance,
                      RangeOut& range_out) const
    {
        using calc_t = typename select_calculation_type
            <
                Point,
                typename boost::range_value<RangeOut>::type,
                CalculationType
            >::type;

        using helper = geographic_buffer_helper<FormulaPolicy, calc_t>;

        calc_t const lon_rad = get_as_radian<0>(vertex);
        calc_t const lat_rad = get_as_radian<1>(vertex);

        calc_t first_azimuth;
        calc_t angle_diff;
        if (! helper::calculate_angles(lon_rad, lat_rad, perp1, perp2, m_spheroid,
                                       angle_diff, first_azimuth))
        {
            return false;
        }

        static calc_t const two_pi = geometry::math::two_pi<calc_t>();
        calc_t const circle_fraction = angle_diff / two_pi;
        std::size_t const n = (std::max)(static_cast<std::size_t>(
            std::ceil(m_points_per_circle * circle_fraction)), std::size_t(1));

        calc_t const diff = angle_diff / static_cast<calc_t>(n);
        calc_t azi = math::wrap_azimuth_in_radian(first_azimuth + diff);

        range_out.push_back(perp1);

        // Generate points between 0 and n, not including them
        // because perp1 and perp2 are inserted before and after this range.
        for (std::size_t i = 1; i < n; i++)
        {
            helper::append_point(lon_rad, lat_rad, buffer_distance, azi, m_spheroid, range_out);
            azi = math::wrap_azimuth_in_radian(azi + diff);
        }

        range_out.push_back(perp2);
        return true;
    }

    template <typename NumericType>
    static inline NumericType max_distance(NumericType const& distance)
    {
        return distance;
    }

#endif // DOXYGEN_SHOULD_SKIP_THIS

private :
    Spheroid m_spheroid;
    std::size_t m_points_per_circle;
};

}} // namespace strategy::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_JOIN_ROUND_HPP
