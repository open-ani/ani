// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2015-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_IS_EMPTY_HPP
#define BOOST_GEOMETRY_ALGORITHMS_IS_EMPTY_HPP

#include <boost/range/begin.hpp>
#include <boost/range/empty.hpp>
#include <boost/range/end.hpp>

#include <boost/geometry/algorithms/not_implemented.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>

#include <boost/geometry/core/exterior_ring.hpp>
#include <boost/geometry/core/interior_rings.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/util/type_traits_std.hpp>

namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace is_empty
{

struct always_not_empty
{
    template <typename Geometry>
    static inline bool apply(Geometry const&)
    {
        return false;
    }
};

struct range_is_empty
{
    template <typename Range>
    static inline bool apply(Range const& range)
    {
        return boost::empty(range);
    }
};

class polygon_is_empty
{
    template <typename InteriorRings>
    static inline bool check_interior_rings(InteriorRings const& interior_rings)
    {
        return std::all_of(boost::begin(interior_rings), boost::end(interior_rings),
                           []( auto const& range ){ return boost::empty(range); });
    }

public:
    template <typename Polygon>
    static inline bool apply(Polygon const& polygon)
    {
        return boost::empty(exterior_ring(polygon))
            && check_interior_rings(interior_rings(polygon));
    }
};

template <typename Policy = range_is_empty>
struct multi_is_empty
{
    template <typename MultiGeometry>
    static inline bool apply(MultiGeometry const& multigeometry)
    {
        return std::all_of(boost::begin(multigeometry),
                           boost::end(multigeometry),
                           []( auto const& range ){ return Policy::apply(range); });
    }
};

}} // namespace detail::is_empty
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{

template <typename Geometry, typename Tag =  typename tag<Geometry>::type>
struct is_empty : not_implemented<Tag>
{};

template <typename Geometry>
struct is_empty<Geometry, point_tag>
    : detail::is_empty::always_not_empty
{};

template <typename Geometry>
struct is_empty<Geometry, box_tag>
    : detail::is_empty::always_not_empty
{};

template <typename Geometry>
struct is_empty<Geometry, segment_tag>
    : detail::is_empty::always_not_empty
{};

template <typename Geometry>
struct is_empty<Geometry, linestring_tag>
    : detail::is_empty::range_is_empty
{};

template <typename Geometry>
struct is_empty<Geometry, ring_tag>
    : detail::is_empty::range_is_empty
{};

template <typename Geometry>
struct is_empty<Geometry, polygon_tag>
    : detail::is_empty::polygon_is_empty
{};

template <typename Geometry>
struct is_empty<Geometry, multi_point_tag>
    : detail::is_empty::range_is_empty
{};

template <typename Geometry>
struct is_empty<Geometry, multi_linestring_tag>
    : detail::is_empty::multi_is_empty<>
{};

template <typename Geometry>
struct is_empty<Geometry, multi_polygon_tag>
    : detail::is_empty::multi_is_empty<detail::is_empty::polygon_is_empty>
{};

} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_dynamic
{

template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct is_empty
{
    static inline bool apply(Geometry const& geometry)
    {
        concepts::check<Geometry const>();

        return dispatch::is_empty<Geometry>::apply(geometry);
    }
};

template <typename Geometry>
struct is_empty<Geometry, dynamic_geometry_tag>
{
    static inline bool apply(Geometry const& geometry)
    {
        bool result = true;
        traits::visit<Geometry>::apply([&](auto const& g)
        {
            result = is_empty<util::remove_cref_t<decltype(g)>>::apply(g);
        }, geometry);
        return result;
    }
};

template <typename Geometry>
struct is_empty<Geometry, geometry_collection_tag>
{
    static inline bool apply(Geometry const& geometry)
    {
        bool result = true;
        detail::visit_breadth_first([&](auto const& g)
        {
            result = is_empty<util::remove_cref_t<decltype(g)>>::apply(g);
            return result;
        }, geometry);
        return result;
    }
};

} // namespace resolve_dynamic


/*!
\brief \brief_check{is the empty set}
\ingroup is_empty
\tparam Geometry \tparam_geometry
\param geometry \param_geometry
\return \return_check{is the empty set}

\qbk{[include reference/algorithms/is_empty.qbk]}
*/
template <typename Geometry>
inline bool is_empty(Geometry const& geometry)
{
    return resolve_dynamic::is_empty<Geometry>::apply(geometry);
}


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_IS_EMPTY_HPP
