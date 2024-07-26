// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2017-2023.
// Modifications copyright (c) 2017-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)


#ifndef BOOST_GEOMETRY_STRATEGIES_COMPARE_HPP
#define BOOST_GEOMETRY_STRATEGIES_COMPARE_HPP


#include <algorithm>
#include <cstddef>
#include <functional>

#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/cs.hpp>
#include <boost/geometry/core/coordinate_type.hpp>
#include <boost/geometry/core/coordinate_dimension.hpp>
#include <boost/geometry/core/static_assert.hpp>

#include <boost/geometry/util/math.hpp>


namespace boost { namespace geometry
{


namespace strategy { namespace compare
{


struct less
{
    template <typename T1, typename T2>
    static inline bool apply(T1 const& l, T2 const& r)
    {
        return l < r;
    }
};

struct greater
{
    template <typename T1, typename T2>
    static inline bool apply(T1 const& l, T2 const& r)
    {
        return l > r;
    }
};

struct equal_to
{
    template <typename T1, typename T2>
    static inline bool apply(T1 const& , T2 const& )
    {
        return false;
    }
};

struct equals_epsilon
{
    template <typename T1, typename T2>
    static inline bool apply(T1 const& l, T2 const& r)
    {
        return math::equals(l, r);
    }
};

struct equals_exact
{
    template <typename T1, typename T2>
    static inline bool apply(T1 const& l, T2 const& r)
    {
        return l == r;
    }
};


#ifndef DOXYGEN_NO_DETAIL
namespace detail
{


template
<
    typename ComparePolicy,
    typename EqualsPolicy,
    std::size_t Dimension,
    std::size_t DimensionCount
>
struct compare_loop
{
    template <typename Point1, typename Point2>
    static inline bool apply(Point1 const& left, Point2 const& right)
    {
        typename geometry::coordinate_type<Point1>::type const&
            cleft = geometry::get<Dimension>(left);
        typename geometry::coordinate_type<Point2>::type const&
            cright = geometry::get<Dimension>(right);

        if (EqualsPolicy::apply(cleft, cright))
        {
            return compare_loop
                <
                    ComparePolicy,
                    EqualsPolicy,
                    Dimension + 1, DimensionCount
                >::apply(left, right);
        }
        else
        {
            return ComparePolicy::apply(cleft, cright);
        }
    }
};

template
<
    typename ComparePolicy,
    typename EqualsPolicy,
    std::size_t DimensionCount
>
struct compare_loop<ComparePolicy, EqualsPolicy, DimensionCount, DimensionCount>
{
    template <typename Point1, typename Point2>
    static inline bool apply(Point1 const& , Point2 const& )
    {
        // On coming here, points are equal.
        // Return false for less/greater.
        return false;
    }
};

template
<
    typename EqualsPolicy,
    std::size_t DimensionCount
>
struct compare_loop<strategy::compare::equal_to, EqualsPolicy, DimensionCount, DimensionCount>
{
    template <typename Point1, typename Point2>
    static inline bool apply(Point1 const& , Point2 const& )
    {
        // On coming here, points are equal.
        // Return true for equal_to.
        return true;
    }
};

} // namespace detail
#endif // DOXYGEN_NO_DETAIL


template
<
    typename ComparePolicy,
    typename EqualsPolicy,
    int Dimension = -1
>
struct cartesian
{
    template <typename Point1, typename Point2>
    static inline bool apply(Point1 const& left, Point2 const& right)
    {
        return compare::detail::compare_loop
            <
                ComparePolicy, EqualsPolicy, Dimension, Dimension + 1
            >::apply(left, right);
    }
};

#ifndef DOXYGEN_NO_STRATEGY_SPECIALIZATIONS
template
<
    typename ComparePolicy,
    typename EqualsPolicy
>
struct cartesian<ComparePolicy, EqualsPolicy, -1>
{
    template <typename Point1, typename Point2>
    static inline bool apply(Point1 const& left, Point2 const& right)
    {
        return compare::detail::compare_loop
            <
                ComparePolicy,
                EqualsPolicy,
                0,
                ((std::min)(geometry::dimension<Point1>::value,
                            geometry::dimension<Point2>::value))
            >::apply(left, right);
    }
};
#endif // DOXYGEN_NO_STRATEGY_SPECIALIZATIONS

namespace services
{


template
<
    typename ComparePolicy,
    typename EqualsPolicy,
    typename Point1,
    typename Point2 = Point1,
    int Dimension = -1,
    typename CSTag1 = typename cs_tag<Point1>::type,
    typename CSTag2 = typename cs_tag<Point2>::type
>
struct default_strategy
{
    BOOST_GEOMETRY_STATIC_ASSERT_FALSE(
        "Not implemented for these types.",
        CSTag1, CSTag2);
};


template
<
    typename ComparePolicy,
    typename EqualsPolicy,
    typename Point1,
    typename Point2,
    int Dimension
>
struct default_strategy<ComparePolicy, EqualsPolicy, Point1, Point2, Dimension, cartesian_tag, cartesian_tag>
{
    typedef compare::cartesian<ComparePolicy, EqualsPolicy, Dimension> type;
};


} // namespace services


}} // namespace strategy compare


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_STRATEGIES_COMPARE_HPP
