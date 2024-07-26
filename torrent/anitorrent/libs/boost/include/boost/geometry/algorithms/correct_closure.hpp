// Boost.Geometry

// Copyright (c) 2017 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2020-2023.
// Modifications copyright (c) 2020-2023 Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_CORRECT_CLOSURE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_CORRECT_CLOSURE_HPP

#include <boost/geometry/algorithms/detail/multi_modify.hpp>
#include <boost/geometry/algorithms/disjoint.hpp>

#include <boost/geometry/core/closure.hpp>
#include <boost/geometry/core/exterior_ring.hpp>
#include <boost/geometry/core/interior_rings.hpp>
#include <boost/geometry/core/tags.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp>
#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/util/range.hpp>
#include <boost/range/size.hpp>

namespace boost { namespace geometry
{

// Silence warning C4127: conditional expression is constant
#if defined(_MSC_VER)
#pragma warning(push)
#pragma warning(disable : 4127)
#endif

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace correct_closure
{

struct nop
{
    template <typename Geometry>
    static inline void apply(Geometry& )
    {}
};

// Close a ring, if not closed, or open it
struct close_or_open_ring
{
    template <typename Ring>
    static inline void apply(Ring& r)
    {
        auto size = boost::size(r);
        if (size <= 2)
        {
            return;
        }

        // TODO: This requires relate(pt, pt) strategy
        bool const disjoint = geometry::disjoint(*boost::begin(r), *(boost::end(r) - 1));
        closure_selector const closure = geometry::closure<Ring>::value;

        if (disjoint && closure == closed)
        {
            // Close it by adding first point
            geometry::append(r, *boost::begin(r));
        }
        else if (! disjoint && closure == open)
        {
            // Open it by removing last point
            range::resize(r, size - 1);
        }
    }
};

// Close/open exterior ring and all its interior rings
struct close_or_open_polygon
{
    template <typename Polygon>
    static inline void apply(Polygon& poly)
    {
        close_or_open_ring::apply(exterior_ring(poly));

        auto&& rings = interior_rings(poly);
        auto const end = boost::end(rings);
        for (auto it = boost::begin(rings); it != end; ++it)
        {
            close_or_open_ring::apply(*it);
        }
    }
};

}} // namespace detail::correct_closure
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{

template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct correct_closure: not_implemented<Tag>
{};

template <typename Point>
struct correct_closure<Point, point_tag>
    : detail::correct_closure::nop
{};

template <typename LineString>
struct correct_closure<LineString, linestring_tag>
    : detail::correct_closure::nop
{};

template <typename Segment>
struct correct_closure<Segment, segment_tag>
    : detail::correct_closure::nop
{};


template <typename Box>
struct correct_closure<Box, box_tag>
    : detail::correct_closure::nop
{};

template <typename Ring>
struct correct_closure<Ring, ring_tag>
    : detail::correct_closure::close_or_open_ring
{};

template <typename Polygon>
struct correct_closure<Polygon, polygon_tag>
    : detail::correct_closure::close_or_open_polygon
{};


template <typename MultiPoint>
struct correct_closure<MultiPoint, multi_point_tag>
    : detail::correct_closure::nop
{};


template <typename MultiLineString>
struct correct_closure<MultiLineString, multi_linestring_tag>
    : detail::correct_closure::nop
{};


template <typename Geometry>
struct correct_closure<Geometry, multi_polygon_tag>
    : detail::multi_modify
        <
            detail::correct_closure::close_or_open_polygon
        >
{};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_variant
{

template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct correct_closure
{
    static inline void apply(Geometry& geometry)
    {
        concepts::check<Geometry const>();
        dispatch::correct_closure<Geometry>::apply(geometry);
    }
};

template <typename Geometry>
struct correct_closure<Geometry, dynamic_geometry_tag>
{
    static void apply(Geometry& geometry)
    {
        traits::visit<Geometry>::apply([](auto & g)
        {
            correct_closure<util::remove_cref_t<decltype(g)>>::apply(g);
        }, geometry);
    }
};

template <typename Geometry>
struct correct_closure<Geometry, geometry_collection_tag>
{
    static void apply(Geometry& geometry)
    {
        detail::visit_breadth_first([](auto & g)
        {
            correct_closure<util::remove_cref_t<decltype(g)>>::apply(g);
            return true;
        }, geometry);
    }
};

} // namespace resolve_variant


// TODO: This algorithm should use relate(pt, pt) strategy


/*!
\brief Closes or opens a geometry, according to its type
\details Corrects a geometry w.r.t. closure points to all rings which do not
    have a closing point and are typed as they should have one, the first point
    is appended.
\ingroup correct_closure
\tparam Geometry \tparam_geometry
\param geometry \param_geometry which will be corrected if necessary
*/
template <typename Geometry>
inline void correct_closure(Geometry& geometry)
{
    resolve_variant::correct_closure<Geometry>::apply(geometry);
}


#if defined(_MSC_VER)
#pragma warning(pop)
#endif

}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_CORRECT_CLOSURE_HPP
