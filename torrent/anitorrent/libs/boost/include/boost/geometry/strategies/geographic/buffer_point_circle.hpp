// Boost.Geometry

// Copyright (c) 2018-2022 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2020-2021.
// Modifications copyright (c) 2020-2021 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_POINT_CIRCLE_HPP
#define BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_POINT_CIRCLE_HPP

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
\brief Create a circular buffer around a point, on the Earth
\ingroup strategies
\details This strategy can be used as PointStrategy for the buffer algorithm.
    It creates a circular buffer around a point, on the Earth. It can be applied
    for points and multi_points.

\qbk{
[heading Example]
[buffer_geographic_point_circle]
[buffer_geographic_point_circle_output]
[heading See also]
\* [link geometry.reference.algorithms.buffer.buffer_7_with_strategies buffer (with strategies)]
\* [link geometry.reference.strategies.strategy_buffer_point_circle point_circle]
\* [link geometry.reference.strategies.strategy_buffer_point_square point_square]
}
 */
template
<
    typename FormulaPolicy = strategy::andoyer,
    typename Spheroid = srs::spheroid<double>,
    typename CalculationType = void
>
class geographic_point_circle
{
public :

    //! \brief Constructs the strategy with a spheroid
    //! \param spheroid The spheroid to be used
    //! \param count Number of points (minimum 3) for the created circle
    explicit inline geographic_point_circle(Spheroid const& spheroid,
                                            std::size_t count = default_points_per_circle)
        : m_spheroid(spheroid)
        , m_count(get_point_count_for_circle(count))
    {}

    //! \brief Constructs the strategy
    //! \param count Number of points (minimum 3) for the created circle
    explicit inline geographic_point_circle(std::size_t count = default_points_per_circle)
        : m_count(get_point_count_for_circle(count))
    {}

#ifndef DOXYGEN_SHOULD_SKIP_THIS
    //! Fills range_out with a circle around point using distance_strategy
    template
    <
        typename Point,
        typename DistanceStrategy,
        typename RangeOut
    >
    inline void apply(Point const& point,
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

        calc_t const lon_rad = get_as_radian<0>(point);
        calc_t const lat_rad = get_as_radian<1>(point);

        calc_t const buffer_distance = distance_strategy.apply(point,
            point, strategy::buffer::buffer_side_left);

        calc_t const two_pi = geometry::math::two_pi<calc_t>();
        calc_t const pi = geometry::math::pi<calc_t>();

        calc_t const diff = two_pi / calc_t(m_count);
        calc_t angle = -pi;

        for (std::size_t i = 0; i < m_count; i++, angle += diff)
        {
            // If angle is zero, shift angle a tiny bit to avoid spikes.
            calc_t const eps = angle == 0 ? 1.0e-10 : 0.0;
            helper::append_point(lon_rad, lat_rad, buffer_distance, angle + eps, m_spheroid, range_out);
        }

        {
            // Close the range
            auto const p = range_out.front();
            range_out.push_back(p);
        }
    }
#endif // DOXYGEN_SHOULD_SKIP_THIS

private :
    Spheroid m_spheroid;
    std::size_t m_count;
};

}} // namespace strategy::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_GEOGRAPHIC_BUFFER_POINT_CIRCLE_HPP
