// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.
// Copyright (c) 2014 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2020-2023.
// Modifications copyright (c) 2020-2023 Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_REVERSE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_REVERSE_HPP

#include <algorithm>

#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>

#include <boost/geometry/algorithms/detail/multi_modify.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/core/interior_rings.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>
#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/geometries/concepts/check.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace reverse
{


struct range_reverse
{
    template <typename Range>
    static inline void apply(Range& range)
    {
        std::reverse(boost::begin(range), boost::end(range));
    }
};


struct polygon_reverse: private range_reverse
{
    template <typename Polygon>
    static inline void apply(Polygon& polygon)
    {
        range_reverse::apply(exterior_ring(polygon));

        auto&& rings = interior_rings(polygon);
        auto const end = boost::end(rings);
        for (auto it = boost::begin(rings); it != end; ++it)
        {
            range_reverse::apply(*it);
        }
    }
};


}} // namespace detail::reverse
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct reverse
{
    static inline void apply(Geometry&)
    {}
};


template <typename Ring>
struct reverse<Ring, ring_tag>
    : detail::reverse::range_reverse
{};


template <typename LineString>
struct reverse<LineString, linestring_tag>
    : detail::reverse::range_reverse
{};


template <typename Polygon>
struct reverse<Polygon, polygon_tag>
    : detail::reverse::polygon_reverse
{};


template <typename Geometry>
struct reverse<Geometry, multi_linestring_tag>
    : detail::multi_modify<detail::reverse::range_reverse>
{};


template <typename Geometry>
struct reverse<Geometry, multi_polygon_tag>
    : detail::multi_modify<detail::reverse::polygon_reverse>
{};



} // namespace dispatch
#endif


namespace resolve_dynamic
{

template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct reverse
{
    static void apply(Geometry& geometry)
    {
        concepts::check<Geometry>();
        dispatch::reverse<Geometry>::apply(geometry);
    }
};

template <typename Geometry>
struct reverse<Geometry, dynamic_geometry_tag>
{
    static void apply(Geometry& geometry)
    {
        traits::visit<Geometry>::apply([](auto & g)
        {
            reverse<util::remove_cref_t<decltype(g)>>::apply(g);
        }, geometry);
    }
};

template <typename Geometry>
struct reverse<Geometry, geometry_collection_tag>
{
    static void apply(Geometry& geometry)
    {
        detail::visit_breadth_first([](auto & g)
        {
            reverse<util::remove_cref_t<decltype(g)>>::apply(g);
            return true;
        }, geometry);
    }
};

} // namespace resolve_dynamic


/*!
\brief Reverses the points within a geometry
\details Generic function to reverse a geometry. It resembles the std::reverse
   functionality, but it takes the geometry type into account. Only for a ring
   or for a linestring it is the same as the std::reverse.
\ingroup reverse
\tparam Geometry \tparam_geometry
\param geometry \param_geometry which will be reversed

\qbk{[include reference/algorithms/reverse.qbk]}
*/
template <typename Geometry>
inline void reverse(Geometry& geometry)
{
    resolve_dynamic::reverse<Geometry>::apply(geometry);
}

}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_REVERSE_HPP
