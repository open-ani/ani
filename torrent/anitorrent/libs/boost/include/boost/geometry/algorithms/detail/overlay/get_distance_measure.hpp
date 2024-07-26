// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2019-2021 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2022.
// Modifications copyright (c) 2022 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_GET_DISTANCE_MEASURE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_GET_DISTANCE_MEASURE_HPP

#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/coordinate_system.hpp>
#include <boost/geometry/core/coordinate_type.hpp>
#include <boost/geometry/arithmetic/infinite_line_functions.hpp>
#include <boost/geometry/algorithms/detail/make/make.hpp>
#include <boost/geometry/algorithms/not_implemented.hpp>
#include <boost/geometry/util/select_coordinate_type.hpp>

#include <cmath>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail
{

template <typename T>
struct distance_measure
{
    T measure;

    distance_measure()
        : measure(T())
    {}

    // Returns true if the distance measure is absolutely zero
    bool is_zero() const
    {
      return ! is_positive() && ! is_negative();
    }

    // Returns true if the distance measure is positive. Distance measure
    // algorithm returns positive value if it is located on the left side.
    bool is_positive() const { return measure > T(0); }

    // Returns true if the distance measure is negative. Distance measure
    // algorithm returns negative value if it is located on the right side.
    bool is_negative() const { return measure < T(0); }
};

} // detail


namespace detail_dispatch
{

// TODO: this is effectively a strategy, but for internal usage.
// It might be moved to the strategies folder.

template <typename CalculationType, typename CsTag>
struct get_distance_measure
    : not_implemented<CsTag>
{};

template <typename CalculationType>
struct get_distance_measure<CalculationType, spherical_tag>
{
    // By default the distance measure is zero, no side difference
    using result_type = detail::distance_measure<CalculationType>;

    template <typename SegmentPoint, typename Point>
    static result_type apply(SegmentPoint const& , SegmentPoint const& ,
                             Point const& )
    {
        result_type const result;
        return result;
    }
};

template <typename CalculationType>
struct get_distance_measure<CalculationType, geographic_tag>
    : get_distance_measure<CalculationType, spherical_tag>
{};

template <typename CalculationType>
struct get_distance_measure<CalculationType, cartesian_tag>
{
    using result_type = detail::distance_measure<CalculationType>;

    template <typename SegmentPoint, typename Point>
    static result_type apply(SegmentPoint const& p1, SegmentPoint const& p2,
                             Point const& p)
    {
        // Get the distance measure / side value
        // It is not a real distance and purpose is
        // to detect small differences in collinearity
        auto const line = detail::make::make_infinite_line<CalculationType>(p1, p2);
        result_type result;
        result.measure = arithmetic::side_value(line, p);
        return result;
    }
};

} // namespace detail_dispatch

namespace detail
{

// Returns a (often very tiny) value to indicate its side, and distance,
// 0 (absolutely 0, not even an epsilon) means collinear. Like side,
// a negative means that p is to the right of p1-p2. And a positive value
// means that p is to the left of p1-p2.
template <typename SegmentPoint, typename Point, typename Strategies>
inline auto get_distance_measure(SegmentPoint const& p1, SegmentPoint const& p2, Point const& p,
                                 Strategies const&)
{
    using calc_t = typename select_coordinate_type<SegmentPoint, Point>::type;

    // Verify equality, without using a tolerance
    // (so don't use equals or equals_point_point)
    // because it is about very tiny differences.
    auto identical = [](auto const& point1, auto const& point2)
    {
        return geometry::get<0>(point1) == geometry::get<0>(point2)
            && geometry::get<1>(point1) == geometry::get<1>(point2);
    };

    if (identical(p1, p) || identical(p2, p))
    {
        detail::distance_measure<calc_t> const result;
        return result;
    }

    return detail_dispatch::get_distance_measure
        <
            calc_t,
            typename Strategies::cs_tag
        >::apply(p1, p2, p);
}

} // namespace detail
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_GET_DISTANCE_MEASURE_HPP
