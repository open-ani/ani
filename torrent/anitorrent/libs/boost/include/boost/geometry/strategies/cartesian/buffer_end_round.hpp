// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2012-2015 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2015-2023.
// Modifications copyright (c) 2015-2023, Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGIES_CARTESIAN_BUFFER_END_ROUND_HPP
#define BOOST_GEOMETRY_STRATEGIES_CARTESIAN_BUFFER_END_ROUND_HPP

#include <boost/core/ignore_unused.hpp>

#include <boost/geometry/arithmetic/arithmetic.hpp>
#include <boost/geometry/core/cs.hpp>
#include <boost/geometry/core/access.hpp>
#include <boost/geometry/strategies/tags.hpp>
#include <boost/geometry/util/math.hpp>
#include <boost/geometry/util/select_most_precise.hpp>

#include <boost/geometry/strategies/buffer.hpp>


namespace boost { namespace geometry
{


namespace strategy { namespace buffer
{


/*!
\brief Let the buffer create rounded ends
\ingroup strategies
\details This strategy can be used as EndStrategy for the buffer algorithm.
    It creates a rounded end for each linestring-end. It can be applied
    for (multi)linestrings. Also it is applicable for spikes in (multi)polygons.
    This strategy is only applicable for Cartesian coordinate systems.

\qbk{
[heading Example]
[buffer_end_round]
[heading Output]
[$img/strategies/buffer_end_round.png]
[heading See also]
\* [link geometry.reference.algorithms.buffer.buffer_7_with_strategies buffer (with strategies)]
\* [link geometry.reference.strategies.strategy_buffer_end_flat end_flat]
}
 */
class end_round
{
private :
    std::size_t m_points_per_circle;

    template
    <
        typename Point,
        typename PromotedType,
        typename DistanceType,
        typename RangeOut
    >
    inline void generate_points(Point const& point,
                PromotedType alpha, // by value
                DistanceType const& buffer_distance,
                RangeOut& range_out) const
    {
        PromotedType const two_pi = geometry::math::two_pi<PromotedType>();

        std::size_t point_buffer_count = m_points_per_circle;

        PromotedType const diff = two_pi / PromotedType(point_buffer_count);

        // For half circle:
        point_buffer_count /= 2;
        point_buffer_count++;

        for (std::size_t i = 0; i < point_buffer_count; i++, alpha -= diff)
        {
            typename boost::range_value<RangeOut>::type p;
            geometry::set<0>(p, geometry::get<0>(point) + buffer_distance * cos(alpha));
            geometry::set<1>(p, geometry::get<1>(point) + buffer_distance * sin(alpha));
            range_out.push_back(p);
        }
    }

    template <typename T, typename P1, typename P2>
    static inline T calculate_angle(P1 const& from_point, P2 const& to_point)
    {
        typedef P1 vector_type;
        vector_type v = from_point;
        geometry::subtract_point(v, to_point);
        return atan2(geometry::get<1>(v), geometry::get<0>(v));
    }

public :

    //! \brief Constructs the strategy
    //! \param points_per_circle Number of points (minimum 4) that would be used for a full circle
    explicit inline end_round(std::size_t points_per_circle = default_points_per_circle)
        : m_points_per_circle(get_point_count_for_end(points_per_circle))
    {}

#ifndef DOXYGEN_SHOULD_SKIP_THIS

    //! Fills output_range with a round end
    template <typename Point, typename DistanceStrategy, typename RangeOut>
    inline void apply(Point const& penultimate_point,
                Point const& perp_left_point,
                Point const& ultimate_point,
                Point const& perp_right_point,
                buffer_side_selector side,
                DistanceStrategy const& distance,
                RangeOut& range_out) const
    {
        boost::ignore_unused(perp_left_point);
        typedef typename coordinate_type<Point>::type coordinate_type;

        typedef typename geometry::select_most_precise
        <
            coordinate_type,
            double
        >::type promoted_type;

        promoted_type const dist_left = distance.apply(penultimate_point, ultimate_point, buffer_side_left);
        promoted_type const dist_right = distance.apply(penultimate_point, ultimate_point, buffer_side_right);
        promoted_type const alpha
                = calculate_angle<promoted_type>(penultimate_point, ultimate_point)
                    - geometry::math::half_pi<promoted_type>();

        if (geometry::math::equals(dist_left, dist_right))
        {
            generate_points(ultimate_point, alpha, dist_left, range_out);
        }
        else
        {
            static promoted_type const two = 2.0;
            promoted_type const dist_average = (dist_left + dist_right) / two;
            promoted_type const dist_half
                    = (side == buffer_side_right
                    ? (dist_right - dist_left)
                    : (dist_left - dist_right)) / two;

            Point shifted_point;
            geometry::set<0>(shifted_point, geometry::get<0>(ultimate_point) + dist_half * cos(alpha));
            geometry::set<1>(shifted_point, geometry::get<1>(ultimate_point) + dist_half * sin(alpha));
            generate_points(shifted_point, alpha, dist_average, range_out);
        }

        if (m_points_per_circle % 2 == 1)
        {
            // For a half circle, if the number of points is not even,
            // we should insert the end point too, to generate a full cap
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
};


}} // namespace strategy::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_CARTESIAN_BUFFER_END_ROUND_HPP
