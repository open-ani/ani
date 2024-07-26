// Boost.Geometry

// Copyright (c) 2022 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_SIDE_STRAIGHT_HPP
#define BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_SIDE_STRAIGHT_HPP

#include <cstddef>

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

/*!
\brief Create a straight buffer along a side, on the Earth
\ingroup strategies
\details This strategy can be used as SideStrategy for the buffer algorithm.
 */
template
<
    typename FormulaPolicy = strategy::andoyer,
    typename Spheroid = srs::spheroid<double>,
    typename CalculationType = void
>
class geographic_side_straight
{
public :
    //! \brief Constructs the strategy with a spheroid
    //! \param spheroid The spheroid to be used
    explicit inline geographic_side_straight(Spheroid const& spheroid)
        : m_spheroid(spheroid)
    {}

    //! \brief Constructs the strategy
    inline geographic_side_straight()
    {}

#ifndef DOXYGEN_SHOULD_SKIP_THIS
    // Returns true if the buffer distance is always the same
    static inline bool equidistant()
    {
        return true;
    }


    template
    <
        typename Point,
        typename DistanceStrategy,
        typename RangeOut
    >
    inline result_code apply(Point const& input_p1, Point const& input_p2,
                             buffer_side_selector side,
                             DistanceStrategy const& distance_strategy,
                             RangeOut& range_out) const
    {
        using calc_t = typename select_calculation_type
            <
                Point,
                typename boost::range_value<RangeOut>::type,
                CalculationType
            >::type;

        using helper = geographic_buffer_helper<FormulaPolicy, calc_t>;

        calc_t const lon1_rad = get_as_radian<0>(input_p1);
        calc_t const lat1_rad = get_as_radian<1>(input_p1);
        calc_t const lon2_rad = get_as_radian<0>(input_p2);
        calc_t const lat2_rad = get_as_radian<1>(input_p2);
        if (lon1_rad == lon2_rad && lat1_rad == lat2_rad)
        {
            // Coordinates are simplified and therefore most often not equal.
            // But if simplify is skipped, or for lines with two
            // equal points, length is 0 and we cannot generate output.
            return result_no_output;
        }


        // Measure the angle from p1 to p2 with the Inverse transformation,
        // and subtract pi/2 to make it perpendicular.
        auto const inv = helper::azimuth(lon1_rad, lat1_rad, input_p2, m_spheroid);
        auto const angle = math::wrap_azimuth_in_radian(inv - geometry::math::half_pi<calc_t>());

        // Calculate the distance and generate two points at that distance
        auto const distance = distance_strategy.apply(input_p1, input_p2, side);
        helper::append_point(lon1_rad, lat1_rad, distance, angle, m_spheroid, range_out);
        helper::append_point(lon2_rad, lat2_rad, distance, angle, m_spheroid, range_out);

        return result_normal;
    }
#endif // DOXYGEN_SHOULD_SKIP_THIS

private :
    Spheroid m_spheroid;
};

}} // namespace strategy::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_SIDE_STRAIGHT_HPP
