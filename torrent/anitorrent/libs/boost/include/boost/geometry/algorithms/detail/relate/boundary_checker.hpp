// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2014-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_BOUNDARY_CHECKER_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_BOUNDARY_CHECKER_HPP

#include <boost/core/ignore_unused.hpp>
#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/detail/equals/point_point.hpp>
#include <boost/geometry/algorithms/detail/sub_range.hpp>
#include <boost/geometry/algorithms/num_points.hpp>

#include <boost/geometry/geometries/helper_geometry.hpp>

#include <boost/geometry/policies/compare.hpp>

#include <boost/geometry/strategies/relate/cartesian.hpp>
#include <boost/geometry/strategies/relate/geographic.hpp>
#include <boost/geometry/strategies/relate/spherical.hpp>

#include <boost/geometry/util/has_nan_coordinate.hpp>
#include <boost/geometry/util/range.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace relate
{


template
<
    typename Geometry,
    typename Strategy,
    typename Tag = typename geometry::tag<Geometry>::type
>
class boundary_checker {};

template <typename Geometry, typename Strategy>
class boundary_checker<Geometry, Strategy, linestring_tag>
{
public:
    boundary_checker(Geometry const& g, Strategy const& s)
        : m_has_boundary(
            boost::size(g) >= 2
            && ! detail::equals::equals_point_point(range::front(g), range::back(g), s))
#ifdef BOOST_GEOMETRY_DEBUG_RELATE_BOUNDARY_CHECKER
        , m_geometry(g)
#endif
        , m_strategy(s)
    {}

    template <typename Point>
    bool is_endpoint_boundary(Point const& pt) const
    {
        boost::ignore_unused(pt);
#ifdef BOOST_GEOMETRY_DEBUG_RELATE_BOUNDARY_CHECKER
        // may give false positives for INT
        BOOST_GEOMETRY_ASSERT(
            detail::equals::equals_point_point(pt, range::front(m_geometry), m_strategy)
         || detail::equals::equals_point_point(pt, range::back(m_geometry), m_strategy));
#endif
        return m_has_boundary;
    }

    Strategy const& strategy() const
    {
        return m_strategy;
    }

private:
    bool m_has_boundary;
#ifdef BOOST_GEOMETRY_DEBUG_RELATE_BOUNDARY_CHECKER
    Geometry const& m_geometry;
#endif
    Strategy const& m_strategy;
};


template <typename Point, typename Strategy, typename Out>
inline void copy_boundary_points(Point const& front_pt, Point const& back_pt,
                                 Strategy const& strategy, Out & boundary_points)
{
    using mutable_point_type = typename Out::value_type;
    // linear ring or point - no boundary
    if (! equals::equals_point_point(front_pt, back_pt, strategy))
    {
        // do not add points containing NaN coordinates
        // because they cannot be reasonably compared, e.g. with MSVC
        // an assertion failure is reported in std::equal_range()
        if (! geometry::has_nan_coordinate(front_pt))
        {
            mutable_point_type pt;
            geometry::convert(front_pt, pt);
            boundary_points.push_back(pt);
        }
        if (! geometry::has_nan_coordinate(back_pt))
        {
            mutable_point_type pt;
            geometry::convert(back_pt, pt);
            boundary_points.push_back(pt);
        }
    }
}

template <typename Segment, typename Strategy, typename Out>
inline void copy_boundary_points_of_seg(Segment const& seg, Strategy const& strategy,
                                        Out & boundary_points)
{
    typename Out::value_type front_pt, back_pt;
    assign_point_from_index<0>(seg, front_pt);
    assign_point_from_index<1>(seg, back_pt);
    copy_boundary_points(front_pt, back_pt, strategy, boundary_points);
}

template <typename Linestring, typename Strategy, typename Out>
inline void copy_boundary_points_of_ls(Linestring const& ls, Strategy const& strategy,
                                       Out & boundary_points)
{
    // empty or point - no boundary
    if (boost::size(ls) >= 2)
    {
        auto const& front_pt = range::front(ls);
        auto const& back_pt = range::back(ls);
        copy_boundary_points(front_pt, back_pt, strategy, boundary_points);
    }
}

template <typename MultiLinestring, typename Strategy, typename Out>
inline void copy_boundary_points_of_mls(MultiLinestring const& mls, Strategy const& strategy,
                                        Out & boundary_points)
{
    for (auto it = boost::begin(mls); it != boost::end(mls); ++it)
    {
        copy_boundary_points_of_ls(*it, strategy, boundary_points);
    }
}


template <typename Geometry, typename Strategy>
class boundary_checker<Geometry, Strategy, multi_linestring_tag>
{
    using point_type = typename point_type<Geometry>::type;
    using mutable_point_type = typename helper_geometry<point_type>::type;

public:
    boundary_checker(Geometry const& g, Strategy const& s)
        : m_is_filled(false), m_geometry(g), m_strategy(s)
    {}

    // First call O(NlogN)
    // Each next call O(logN)
    template <typename Point>
    bool is_endpoint_boundary(Point const& pt) const
    {
        using less_type = geometry::less<mutable_point_type, -1, Strategy>;

        auto const multi_count = boost::size(m_geometry);

        if (multi_count < 1)
        {
            return false;
        }

        if (! m_is_filled)
        {
            //boundary_points.clear();
            m_boundary_points.reserve(multi_count * 2);

            copy_boundary_points_of_mls(m_geometry, m_strategy, m_boundary_points);

            std::sort(m_boundary_points.begin(), m_boundary_points.end(), less_type());

            m_is_filled = true;
        }

        mutable_point_type mpt;
        geometry::convert(pt, mpt);
        auto const equal_range = std::equal_range(m_boundary_points.begin(),
                                                  m_boundary_points.end(),
                                                  mpt,
                                                  less_type());

        std::size_t const equal_points_count = boost::size(equal_range);
        return equal_points_count % 2 != 0;// && equal_points_count > 0; // the number is odd and > 0
    }

    Strategy const& strategy() const
    {
        return m_strategy;
    }

private:
    mutable bool m_is_filled;
    // TODO: store references/pointers instead of converted points?
    mutable std::vector<mutable_point_type> m_boundary_points;

    Geometry const& m_geometry;
    Strategy const& m_strategy;
};


}} // namespace detail::relate
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_BOUNDARY_CHECKER_HPP
