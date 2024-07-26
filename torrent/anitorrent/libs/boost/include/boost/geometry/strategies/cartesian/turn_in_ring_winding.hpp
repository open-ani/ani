// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2020 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2023 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2023.
// Modifications copyright (c) 2023 Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGIES_CARTESIAN_TURN_IN_RING_WINDING_HPP
#define BOOST_GEOMETRY_STRATEGIES_CARTESIAN_TURN_IN_RING_WINDING_HPP

#include <boost/geometry/arithmetic/infinite_line_functions.hpp>
#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/config.hpp>
#include <boost/geometry/algorithms/detail/make/make.hpp>
#include <boost/geometry/util/math.hpp>

#include <boost/geometry/strategies/cartesian/side_rounded_input.hpp>

namespace boost { namespace geometry
{

namespace strategy { namespace buffer
{

#ifndef DOXYGEN_NO_DETAIL

enum place_on_ring_type
{
    // +----offsetted----> (offsetted is considered as outside)
    // |                 |
    // |                 |
    // left              right (first point outside, rest inside)
    // |                 |
    // |                 |
    // <-----original----+ (original is considered as inside)
    place_on_ring_offsetted,
    place_on_ring_original,
    place_on_ring_to_offsetted,
    place_on_ring_from_offsetted,
};

template <typename CalculationType>
class turn_in_ring_winding
{

    // Implements the winding rule.
    // Basic calculations (on a clockwise ring of 5 segments)
    // (as everywhere in BG, -1 = right, 0 = on segment, +1 = left)
    // +--------2--------+  // P : For 1/3, nothing happens, it returns
    // |                 |  //     For 2, side is right (-1), multiplier=2, -2
    // |        P        |  //     For 4, side is right (-1), multiplier=1, -1
    // 1                 3  //     For 5, side is right (-1), multiplier=1, -1, total -4
    // |             Q   |  // Q : For 2: -2, for 4: -2, total -4
    // |                 |  // R : For 2: -2, for 5: +2, total 0
    // +----5---*----4---+  // S : For 2: -1, 3: nothing, 4: +1, total 0
    //
    //     R             S
    //


public:

    struct counter
    {
        //! Counter, is increased if point is left of a segment (outside),
        //! and decreased if point is right of a segment (inside)
        int count{0};

        int count_on_offsetted{0};
        int count_on_origin{0};
        int count_on_edge{0};

        CalculationType edge_min_fraction{(std::numeric_limits<CalculationType>::max)()};
#if defined(BOOST_GEOMETRY_USE_RESCALING)
        CalculationType inside_min_measure{(std::numeric_limits<CalculationType>::max)()};
#endif

        inline bool is_inside() const
        {
            return count < 0 || count_on_origin > 0;
        }

        inline bool is_on_boundary() const
        {
            return count_on_origin == 0
                && (count_on_offsetted > 0
                || (count_on_edge > 0 && edge_min_fraction < 1.0e-3)
#if defined(BOOST_GEOMETRY_USE_RESCALING)
                || (count < 0 && inside_min_measure < 1.0e-5)
#endif
                );
        }

    };

    using state_type = counter;

    template <typename Point, typename PointOfSegment>
    static inline bool is_in_vertical_range(Point const& point,
                             PointOfSegment const& s1,
                             PointOfSegment const& s2)
    {
        CalculationType const py = get<1>(point);
        CalculationType const s1y = get<1>(s1);
        CalculationType const s2y = get<1>(s2);

        return s1y < s2y ? (py >= s1y && py <= s2y) : (py >= s2y && py <= s1y);
    }

    template <typename Point, typename PointOfSegment>
    static inline void apply_on_boundary(Point const& point,
                             PointOfSegment const& s1,
                             PointOfSegment const& s2,
                             place_on_ring_type place_on_ring,
                             counter& the_state)
    {
        if (place_on_ring == place_on_ring_offsetted)
        {
            the_state.count_on_offsetted++;
        }
        else if (place_on_ring == place_on_ring_to_offsetted
            || place_on_ring == place_on_ring_from_offsetted)
        {
            the_state.count_on_edge++;

            auto const line1 = detail::make::make_perpendicular_line<CalculationType>(s1, s2, s1);
            auto const line2 = detail::make::make_perpendicular_line<CalculationType>(s2, s1, s2);

            auto const value1 = arithmetic::side_value(line1, point);
            auto const value2 = arithmetic::side_value(line2, point);
            if (value1 >= 0 && value2 >= 0)
            {
                auto const length_value = value1 + value2;
                if (length_value > 0)
                {
                    // If it is to the utmost point s1 or s2, it is "outside"
                    auto const fraction = (place_on_ring == place_on_ring_to_offsetted ? value2 : value1) / length_value;
                    if (fraction < the_state.edge_min_fraction)
                    {
                        the_state.edge_min_fraction = fraction;
                    }
                }
            }
        }
        else
        {
            the_state.count_on_origin++;
        }
    }

    template <typename Point, typename PointOfSegment>
    static inline bool apply(Point const& point,
                             PointOfSegment const& s1,
                             PointOfSegment const& s2,
                             place_on_ring_type place_on_ring,
                             bool is_convex,
                             counter& the_state)
    {
        int const side = strategy::side::side_rounded_input<CalculationType>::apply(s1, s2, point);

        if (is_convex && side > 0)
        {
            // If the point is left of this segment of a convex piece, it can never be inside.
            // Stop further processing
            the_state.count = 1;
            return false;
        }

        CalculationType const px = get<0>(point);
        CalculationType const s1x = get<0>(s1);
        CalculationType const s2x = get<0>(s2);

        bool const in_horizontal_range = s1x < s2x ? (px >= s1x && px <= s2x) : (px >= s2x && px <= s1x);

        bool const vertical = s1x == s2x;

        if (in_horizontal_range || (vertical && is_in_vertical_range(point, s1, s2)))
        {
            if (side == 0)
            {
                apply_on_boundary(point, s1, s2, place_on_ring, the_state);
            }
#if defined(BOOST_GEOMETRY_USE_RESCALING)
            else if (side == -1)
            {
                auto const line = detail::make::make_infinite_line<CalculationType>(s1, s2);
                auto const value = -arithmetic::side_value(line, point);
                if (value > 0 && value < the_state.inside_min_measure) { the_state.inside_min_measure = value; }

            }
#endif
        }

        if (in_horizontal_range)
        {
            auto const on_boundary = the_state.count_on_offsetted + the_state.count_on_edge + the_state.count_on_origin;
            if (on_boundary == 0)
            {
                // Use only absolute comparisons, because the ring is continuous -
                // what was missed is there earlier or later, and turns should
                // not be counted twice (which can happen if an epsilon is used).
                bool const eq1 = s1x == px;
                bool const eq2 = s2x == px;

                // Account for  1 or  2 for left side (outside)
                //     and for -1 or -2 for right side (inside)
                int const multiplier = eq1 || eq2 ? 1 : 2;

                the_state.count += side * multiplier;
            }
        }

        return true;
    }
};

#endif // DOXYGEN_NO_DETAIL

}} // namespace strategy::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_CARTESIAN_TURN_IN_RING_WINDING_HPP

