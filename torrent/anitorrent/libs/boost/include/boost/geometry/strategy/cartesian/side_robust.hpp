// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2019 Tinko Bartels, Berlin, Germany.

// Contributed and/or modified by Tinko Bartels,
//   as part of Google Summer of Code 2019 program.

// This file was modified by Oracle on 2021.
// Modifications copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_ROBUST_HPP
#define BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_ROBUST_HPP

#include <boost/geometry/core/config.hpp>
#include <boost/geometry/strategy/cartesian/side_non_robust.hpp>

#include <boost/geometry/strategies/side.hpp>

#include <boost/geometry/util/select_most_precise.hpp>
#include <boost/geometry/util/select_calculation_type.hpp>
#include <boost/geometry/util/precise_math.hpp>
#include <boost/geometry/util/math.hpp>

namespace boost { namespace geometry
{

namespace strategy { namespace side
{

struct epsilon_equals_policy
{
public:
    template <typename Policy, typename T1, typename T2>
    static bool apply(T1 const& a, T2 const& b, Policy const& policy)
    {
        return boost::geometry::math::detail::equals_by_policy(a, b, policy);
    }
};

struct fp_equals_policy
{
public:
    template <typename Policy, typename T1, typename T2>
    static bool apply(T1 const& a, T2 const& b, Policy const&)
    {
        return a == b;
    }
};


/*!
\brief Adaptive precision predicate to check at which side of a segment a point lies:
    left of segment (>0), right of segment (< 0), on segment (0).
\ingroup strategies
\tparam CalculationType \tparam_calculation (numeric_limits<ct>::epsilon() and numeric_limits<ct>::digits must be supported for calculation type ct)
\tparam Robustness std::size_t value from 0 (fastest) to 3 (default, guarantees correct results).
\details This predicate determines at which side of a segment a point lies using an algorithm that is adapted from orient2d as described in "Adaptive Precision Floating-Point Arithmetic and Fast Robust Geometric Predicates" by Jonathan Richard Shewchuk ( https://dl.acm.org/citation.cfm?doid=237218.237337 ). More information and copies of the paper can also be found at https://www.cs.cmu.edu/~quake/robust.html . It is designed to be adaptive in the sense that it should be fast for inputs that lead to correct results with plain float operations but robust for inputs that require higher precision arithmetics.
 */
template
<
    typename CalculationType = void,
    typename EqualsPolicy = epsilon_equals_policy,
    std::size_t Robustness = 3
>
struct side_robust
{

    template <typename CT>
    struct epsilon_policy
    {
        using Policy = boost::geometry::math::detail::equals_factor_policy<CT>;

        epsilon_policy() {}

        template <typename Type>
        epsilon_policy(Type const& a, Type const& b, Type const& c, Type const& d)
            : m_policy(a, b, c, d)
        {}
        Policy m_policy;

    public:

        template <typename T1, typename T2>
        bool apply(T1 a, T2 b) const
        {
            return EqualsPolicy::apply(a, b, m_policy);
        }
    };

public:

    typedef cartesian_tag cs_tag;

    //! \brief Computes the sign of the CCW triangle p1, p2, p
    template
    <
        typename PromotedType,
        typename P1,
        typename P2,
        typename P,
        typename EpsPolicyInternal,
        std::enable_if_t<std::is_fundamental<PromotedType>::value, int> = 0
    >
    static inline PromotedType side_value(P1 const& p1,
                                          P2 const& p2,
                                          P const& p,
                                          EpsPolicyInternal& eps_policy)
    {
        using vec2d = ::boost::geometry::detail::precise_math::vec2d<PromotedType>;
        vec2d pa;
        pa.x = get<0>(p1);
        pa.y = get<1>(p1);
        vec2d pb;
        pb.x = get<0>(p2);
        pb.y = get<1>(p2);
        vec2d pc;
        pc.x = get<0>(p);
        pc.y = get<1>(p);
        return ::boost::geometry::detail::precise_math::orient2d
            <PromotedType, Robustness>(pa, pb, pc, eps_policy);
    }

    template
    <
        typename PromotedType,
        typename P1,
        typename P2,
        typename P,
        typename EpsPolicyInternal,
        std::enable_if_t<!std::is_fundamental<PromotedType>::value, int> = 0
    >
    static inline auto side_value(P1 const& p1, P2 const& p2, P const& p,
                                  EpsPolicyInternal&)
    {
        return side_non_robust<>::apply(p1, p2, p);
    }

#ifndef DOXYGEN_SHOULD_SKIP_THIS
    template
    <
        typename P1,
        typename P2,
        typename P
    >
    static inline int apply(P1 const& p1, P2 const& p2, P const& p)
    {
        using coordinate_type = typename select_calculation_type_alt
            <
                CalculationType,
                P1,
                P2,
                P
            >::type;

        using promoted_type = typename select_most_precise
            <
                coordinate_type,
                double
            >::type;

        epsilon_policy<promoted_type> epsp;
        promoted_type sv = side_value<promoted_type>(p1, p2, p, epsp);
        promoted_type const zero = promoted_type();

        return epsp.apply(sv, zero) ? 0
            : sv > zero ? 1
            : -1;
    }

#endif

};

}} // namespace strategy::side

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGY_CARTESIAN_SIDE_ROBUST_HPP
