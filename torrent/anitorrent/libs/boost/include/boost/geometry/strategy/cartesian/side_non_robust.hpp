// Boost.Geometry

// Copyright (c) 2020-2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_NON_ROBUST_HPP
#define BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_NON_ROBUST_HPP

#include <boost/geometry/util/select_most_precise.hpp>
#include <boost/geometry/util/select_calculation_type.hpp>
#include <boost/geometry/util/precise_math.hpp>

#include <boost/geometry/arithmetic/determinant.hpp>

namespace boost { namespace geometry
{

namespace strategy { namespace side
{

/*!
\brief Predicate to check at which side of a segment a point lies:
    left of segment (>0), right of segment (< 0), on segment (0).
\ingroup strategies
\tparam CalculationType \tparam_calculation
\details This predicate determines at which side of a segment a point lies
*/
template
<
    typename CalculationType = void
>
struct side_non_robust
{
public:
    //! \brief Computes double the signed area of the CCW triangle p1, p2, p
    template
    <
        typename P1,
        typename P2,
        typename P
    >
    static inline int apply(P1 const& p1, P2 const& p2, P const& p)
    {
        typedef typename select_calculation_type_alt
            <
                CalculationType,
                P1,
                P2,
                P
            >::type CoordinateType;
        typedef typename select_most_precise
            <
                CoordinateType,
                double
            >::type PromotedType;

        CoordinateType const x = get<0>(p);
        CoordinateType const y = get<1>(p);

        CoordinateType const sx1 = get<0>(p1);
        CoordinateType const sy1 = get<1>(p1);
        CoordinateType const sx2 = get<0>(p2);
        CoordinateType const sy2 = get<1>(p2);

        //non-robust 1
        //the following is 2x slower in some generic cases when compiled with g++
        //(tested versions 9 and 10)
        //
        //auto detleft = (sx1 - x) * (sy2 - y);
        //auto detright = (sy1 - y) * (sx2 - x);
        //return detleft > detright ? 1 : (detleft < detright ? -1 : 0 );

        //non-robust 2
        PromotedType const dx = sx2 - sx1;
        PromotedType const dy = sy2 - sy1;
        PromotedType const dpx = x - sx1;
        PromotedType const dpy = y - sy1;

        PromotedType sv = geometry::detail::determinant<PromotedType>
                (
                    dx, dy,
                    dpx, dpy
                );
        PromotedType const zero = PromotedType();

        return sv == zero ? 0 : sv > zero ? 1 : -1;
    }

};

}} // namespace strategy::side

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_NON_ROBUST_HPP
