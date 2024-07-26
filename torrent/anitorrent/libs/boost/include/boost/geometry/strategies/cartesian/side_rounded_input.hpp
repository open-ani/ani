// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2021 Tinko Bartels, Berlin, Germany.

// This file was modified by Oracle on 2023.
// Modifications copyright (c) 2023 Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_ROUNDED_INPUT_HPP
#define BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_ROUNDED_INPUT_HPP

#include <limits>

#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/config.hpp>

#include <boost/geometry/util/math.hpp>

#include <boost/geometry/strategies/side.hpp>

#include <boost/geometry/util/select_calculation_type.hpp>

namespace boost { namespace geometry
{

namespace strategy { namespace side
{

template <typename CalculationType = void, int Coeff1 = 5, int Coeff2 = 32>
struct side_rounded_input
{
    using cs_tag = cartesian_tag;

    template <typename P1, typename P2, typename P>
    static inline int apply(P1 const& p1, P2 const& p2, P const& p)
    {
        using coor_t = typename select_calculation_type_alt<CalculationType, P1, P2, P>::type;

        coor_t const p1_x = geometry::get<0>(p1);
        coor_t const p1_y = geometry::get<1>(p1);
        coor_t const p2_x = geometry::get<0>(p2);
        coor_t const p2_y = geometry::get<1>(p2);
        coor_t const p_x = geometry::get<0>(p);
        coor_t const p_y = geometry::get<1>(p);

        static coor_t const eps = std::numeric_limits<coor_t>::epsilon() / 2;
        coor_t const det = (p1_x - p_x) * (p2_y - p_y) - (p1_y - p_y) * (p2_x - p_x);
        coor_t const err_bound = (Coeff1 * eps + Coeff2 * eps * eps) *
            (  (geometry::math::abs(p1_x) + geometry::math::abs(p_x))
             * (geometry::math::abs(p2_y) + geometry::math::abs(p_y))
             + (geometry::math::abs(p2_x) + geometry::math::abs(p_x))
             * (geometry::math::abs(p1_y) + geometry::math::abs(p_y)));
        return (det > err_bound) - (det < -err_bound);
    }
};

}} // namespace strategy::side

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_ROUNDED_INPUT_HPP
