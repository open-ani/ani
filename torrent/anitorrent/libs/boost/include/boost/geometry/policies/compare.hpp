// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2017-2023.
// Modifications copyright (c) 2017-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_POLICIES_COMPARE_HPP
#define BOOST_GEOMETRY_POLICIES_COMPARE_HPP


#include <cstddef>

#include <boost/geometry/strategies/compare.hpp>
#include <boost/geometry/strategies/spherical/compare.hpp>
#include <boost/geometry/util/math.hpp>


namespace boost { namespace geometry
{


/*!
\brief Less functor, to sort points in ascending order.
\ingroup compare
\details This functor compares points and orders them on x,
    then on y, then on z coordinate.
\tparam Point the geometry
\tparam Dimension the dimension to sort on, defaults to -1,
    indicating ALL dimensions. That's to say, first on x,
    on equal x-es then on y, etc.
    If a dimension is specified, only that dimension is considered
*/

template
<
    typename Point = void,
    int Dimension = -1,
    typename StrategyOrTag = void
>
struct less_exact
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    inline bool operator()(Point const& left, Point const& right) const
    {
        return StrategyOrTag::template compare_type
            <
                strategy::compare::less,
                strategy::compare::equals_exact
            >::apply(left, right);
    }
};

template
<
    typename Point = void,
    int Dimension = -1,
    typename StrategyOrTag = void
>
struct less
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    inline bool operator()(Point const& left, Point const& right) const
    {
        return StrategyOrTag::template compare_type
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon
            >::apply(left, right);
    }
};


template <typename Point, int Dimension>
struct less<Point, Dimension, void>
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    inline bool operator()(Point const& left, Point const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon,
                Point, Point,
                Dimension
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <int Dimension, typename Strategy>
struct less<void, Dimension, Strategy>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    inline bool operator()(Point1 const& left, Point2 const& right) const
    {
        return Strategy::template compare_type
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon
            >::apply(left, right);
    }
};

// for backward compatibility

template <typename Point, int Dimension>
struct less<Point, Dimension, boost::geometry::cartesian_tag>
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    inline bool operator()(Point const& left, Point const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon,
                Point, Point,
                Dimension,
                boost::geometry::cartesian_tag, boost::geometry::cartesian_tag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <typename Point, int Dimension>
struct less<Point, Dimension, boost::geometry::spherical_tag>
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    inline bool operator()(Point const& left, Point const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon,
                Point, Point,
                Dimension,
                boost::geometry::spherical_tag, boost::geometry::spherical_tag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <typename Point, int Dimension>
struct less<Point, Dimension, boost::geometry::geographic_tag>
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    inline bool operator()(Point const& left, Point const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon,
                Point, Point,
                Dimension,
                boost::geometry::geographic_tag, boost::geometry::geographic_tag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <int Dimension>
struct less<void, Dimension, boost::geometry::cartesian_tag>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    inline bool operator()(Point1 const& left, Point2 const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon,
                Point1, Point2,
                Dimension,
                boost::geometry::cartesian_tag, boost::geometry::cartesian_tag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <int Dimension>
struct less<void, Dimension, boost::geometry::spherical_tag>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    inline bool operator()(Point1 const& left, Point2 const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon,
                Point1, Point2,
                Dimension,
                boost::geometry::spherical_tag, boost::geometry::spherical_tag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <int Dimension>
struct less<void, Dimension, boost::geometry::geographic_tag>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    inline bool operator()(Point1 const& left, Point2 const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon,
                Point1, Point2,
                Dimension,
                boost::geometry::geographic_tag, boost::geometry::geographic_tag
            >::type;

        return strategy_type::apply(left, right);
    }
};


template <int Dimension>
struct less<void, Dimension, void>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    inline bool operator()(Point1 const& left, Point2 const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::less,
                strategy::compare::equals_epsilon,
                Point1, Point2,
                Dimension
            >::type;

        return strategy_type::apply(left, right);
    }
};




/*!
\brief Greater functor
\ingroup compare
\details Can be used to sort points in reverse order
\see Less functor
*/
template
<
    typename Point = void,
    int Dimension = -1,
    typename CSTag = void
>
struct greater
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    bool operator()(Point const& left, Point const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::greater,
                strategy::compare::equals_epsilon,
                Point, Point,
                Dimension,
                CSTag, CSTag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <int Dimension, typename CSTag>
struct greater<void, Dimension, CSTag>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    bool operator()(Point1 const& left, Point2 const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::greater,
                strategy::compare::equals_epsilon,
                Point1, Point2,
                Dimension,
                CSTag, CSTag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <typename Point, int Dimension>
struct greater<Point, Dimension, void>
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    bool operator()(Point const& left, Point const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::greater,
                strategy::compare::equals_epsilon,
                Point, Point,
                Dimension
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <int Dimension>
struct greater<void, Dimension, void>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    bool operator()(Point1 const& left, Point2 const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::greater,
                strategy::compare::equals_epsilon,
                Point1, Point2,
                Dimension
            >::type;

        return strategy_type::apply(left, right);
    }
};


/*!
\brief Equal To functor, to compare if points are equal
\ingroup compare
\tparam Geometry the geometry
\tparam Dimension the dimension to compare on, defaults to -1,
    indicating ALL dimensions.
    If a dimension is specified, only that dimension is considered
*/
template
<
    typename Point,
    int Dimension = -1,
    typename CSTag = void
>
struct equal_to
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    bool operator()(Point const& left, Point const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::equal_to,
                strategy::compare::equals_epsilon,
                Point, Point,
                Dimension,
                CSTag, CSTag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <int Dimension, typename CSTag>
struct equal_to<void, Dimension, CSTag>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    bool operator()(Point1 const& left, Point2 const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::equal_to,
                strategy::compare::equals_epsilon,
                Point1, Point2,
                Dimension,
                CSTag, CSTag
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <typename Point, int Dimension>
struct equal_to<Point, Dimension, void>
{
    using first_argument_type = Point;
    using second_argument_type = Point;
    using result_type = bool;

    bool operator()(Point const& left, Point const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::equal_to,
                strategy::compare::equals_epsilon,
                Point, Point,
                Dimension
            >::type;

        return strategy_type::apply(left, right);
    }
};

template <int Dimension>
struct equal_to<void, Dimension, void>
{
    using result_type = bool;

    template <typename Point1, typename Point2>
    bool operator()(Point1 const& left, Point2 const& right) const
    {
        using strategy_type = typename strategy::compare::services::default_strategy
            <
                strategy::compare::equal_to,
                strategy::compare::equals_epsilon,
                Point1, Point2,
                Dimension
            >::type;

        return strategy_type::apply(left, right);
    }
};


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_POLICIES_COMPARE_HPP
