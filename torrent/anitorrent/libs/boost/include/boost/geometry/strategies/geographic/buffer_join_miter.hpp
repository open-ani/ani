// Boost.Geometry

// Copyright (c) 2022 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_JOIN_MITER_HPP
#define BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_JOIN_MITER_HPP

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
class geographic_join_miter
{
public :

    //! \brief Constructs the strategy with a spheroid
    //! \param spheroid The spheroid to be used
    //! \param miter_limit The miter limit, to avoid excessively long miters around sharp corners
    explicit inline geographic_join_miter(Spheroid const& spheroid,
                                          double miter_limit = 5.0)
        : m_spheroid(spheroid)
        , m_miter_limit(valid_limit(miter_limit))
    {}

    //! \brief Constructs the strategy
    //! \param miter_limit The miter limit, to avoid excessively long miters around sharp corners
    explicit inline geographic_join_miter(double miter_limit = 5.0)
        : m_miter_limit(valid_limit(miter_limit))
    {}

#ifndef DOXYGEN_SHOULD_SKIP_THIS
    //! Fills output_range with a sharp shape around a vertex
    template <typename Point, typename DistanceType, typename RangeOut>
    inline bool apply(Point const& , Point const& vertex,
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

        calc_t const half = 0.5;
        calc_t const half_angle_diff = half * angle_diff;
        calc_t const azi = math::wrap_azimuth_in_radian(first_azimuth + half_angle_diff);

        calc_t const cos_angle = std::cos(half_angle_diff);

        if (cos_angle == 0)
        {
            // It is opposite, perp1==perp2, do not generate a miter cap
            return false;
        }

        // If it is sharp (angle close to 0), the distance will become too high and will be capped.
        calc_t const max_distance = m_miter_limit * geometry::math::abs(buffer_distance);
        calc_t const distance = (std::min)(max_distance, buffer_distance / cos_angle);

        range_out.push_back(perp1);
        helper::append_point(lon_rad, lat_rad, distance, azi, m_spheroid, range_out);
        range_out.push_back(perp2);
        return true;
    }

    template <typename NumericType>
    inline NumericType max_distance(NumericType const& distance) const
    {
        return distance * m_miter_limit;
    }

#endif // DOXYGEN_SHOULD_SKIP_THIS

private :
    double valid_limit(double miter_limit) const
    {
        if (miter_limit < 1.0)
        {
            // It should always exceed the buffer distance
            miter_limit = 1.0;
        }
        return miter_limit;
    }

    Spheroid m_spheroid;
    double m_miter_limit;
};

}} // namespace strategy::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_JOIN_MITER_HPP
