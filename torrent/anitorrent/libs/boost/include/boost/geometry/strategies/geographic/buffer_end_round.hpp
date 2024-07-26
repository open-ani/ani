// Boost.Geometry

// Copyright (c) 2022 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_END_ROUND_HPP
#define BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_END_ROUND_HPP

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
class geographic_end_round
{
public :

    //! \brief Constructs the strategy with a spheroid
    //! \param spheroid The spheroid to be used
    //! \param points_per_circle Number of points (minimum 4) that would be used for a full circle
    explicit inline geographic_end_round(Spheroid const& spheroid,
                                         std::size_t points_per_circle = default_points_per_circle)
        : m_spheroid(spheroid)
        , m_points_per_circle(get_point_count_for_end(points_per_circle))
    {}

    //! \brief Constructs the strategy
    //! \param points_per_circle Number of points (minimum 4) that would be used for a full circle
    explicit inline geographic_end_round(std::size_t points_per_circle = default_points_per_circle)
        : m_points_per_circle(get_point_count_for_end(points_per_circle))
    {}

#ifndef DOXYGEN_SHOULD_SKIP_THIS
    template <typename T, typename RangeOut>
    inline void generate(T lon_rad, T lat_rad, T distance, T azimuth, RangeOut& range_out) const
    {
        using helper = geographic_buffer_helper<FormulaPolicy, T>;
        std::size_t const n = m_points_per_circle / 2;
        T const angle_diff = geometry::math::pi<T>() / n;
        T azi = math::wrap_azimuth_in_radian(azimuth + angle_diff);

        // Generate points between 0 and n, not including them
        // because left and right are inserted before and after this range.
        for (std::size_t i = 1; i < n; i++)
        {
            helper::append_point(lon_rad, lat_rad, distance, azi, m_spheroid, range_out);
            azi = math::wrap_azimuth_in_radian(azi + angle_diff);
        }
    }

    //! Fills output_range with a round end
    template <typename Point, typename DistanceStrategy, typename RangeOut>
    inline void apply(Point const& penultimate_point, Point const& perp_left_point,
                      Point const& ultimate_point, Point const& perp_right_point,
                      buffer_side_selector side, DistanceStrategy const& distance,
                      RangeOut& range_out) const
    {
        using calc_t = typename select_calculation_type
            <
                Point,
                typename boost::range_value<RangeOut>::type,
                CalculationType
            >::type;

        using helper = geographic_buffer_helper<FormulaPolicy, calc_t>;

        calc_t const lon_rad = get_as_radian<0>(ultimate_point);
        calc_t const lat_rad = get_as_radian<1>(ultimate_point);

        auto const azimuth = helper::azimuth(lon_rad, lat_rad, perp_left_point, m_spheroid);

        calc_t const dist_left = distance.apply(penultimate_point, ultimate_point, buffer_side_left);
        calc_t const dist_right = distance.apply(penultimate_point, ultimate_point, buffer_side_right);

        bool const reversed = (side == buffer_side_left && dist_right < 0 && -dist_right > dist_left)
                    || (side == buffer_side_right && dist_left < 0 && -dist_left > dist_right)
                    ;

        if (reversed)
        {
            range_out.push_back(perp_right_point);
            // generate
            range_out.push_back(perp_left_point);
        }
        else
        {
            range_out.push_back(perp_left_point);

            if (geometry::math::equals(dist_left, dist_right))
            {
                generate(lon_rad, lat_rad, dist_left, azimuth, range_out);
            }
            else
            {
                static calc_t const two = 2.0;
                calc_t const dist_average = (dist_left + dist_right) / two;
                calc_t const dist_half
                        = (side == buffer_side_right
                        ? (dist_right - dist_left)
                        : (dist_left - dist_right)) / two;
                auto const shifted = helper::direct::apply(lon_rad, lat_rad, dist_half, azimuth, m_spheroid);
                generate(shifted.lon2, shifted.lat2, dist_average, azimuth, range_out);
            }

            range_out.push_back(perp_right_point);
        }
    }

    template <typename NumericType>
    static inline NumericType max_distance(NumericType const& distance)
    {
        return distance;
    }

    //! Returns the piece_type (round end)
    static inline piece_type get_piece_type()
    {
        return buffered_round_end;
    }
#endif // DOXYGEN_SHOULD_SKIP_THIS

private :
    Spheroid m_spheroid;
    std::size_t m_points_per_circle;
};

}} // namespace strategy::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_END_ROUND_HPP
