// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2015 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2015 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2014-2022.
// Modifications copyright (c) 2014-2022 Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAPS_IMPLEMENTATION_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAPS_IMPLEMENTATION_HPP


#include <cstddef>

#include <boost/geometry/core/access.hpp>

#include <boost/geometry/algorithms/detail/gc_topological_dimension.hpp>
#include <boost/geometry/algorithms/detail/overlaps/interface.hpp>
#include <boost/geometry/algorithms/detail/relate/implementation.hpp>
#include <boost/geometry/algorithms/detail/relate/implementation_gc.hpp>
#include <boost/geometry/algorithms/not_implemented.hpp>

#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/strategies/relate/cartesian.hpp>
#include <boost/geometry/strategies/relate/geographic.hpp>
#include <boost/geometry/strategies/relate/spherical.hpp>

#include <boost/geometry/views/detail/geometry_collection_view.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace overlaps
{

template
<
    std::size_t Dimension,
    std::size_t DimensionCount
>
struct box_box_loop
{
    template <typename Box1, typename Box2>
    static inline void apply(Box1 const& b1, Box2 const& b2,
            bool& overlaps, bool& one_in_two, bool& two_in_one)
    {
        assert_dimension_equal<Box1, Box2>();

        typedef typename coordinate_type<Box1>::type coordinate_type1;
        typedef typename coordinate_type<Box2>::type coordinate_type2;

        coordinate_type1 const& min1 = get<min_corner, Dimension>(b1);
        coordinate_type1 const& max1 = get<max_corner, Dimension>(b1);
        coordinate_type2 const& min2 = get<min_corner, Dimension>(b2);
        coordinate_type2 const& max2 = get<max_corner, Dimension>(b2);

        // We might use the (not yet accepted) Boost.Interval
        // submission in the future

        // If:
        // B1: |-------|
        // B2:           |------|
        // in any dimension -> no overlap
        if (max1 <= min2 || min1 >= max2)
        {
            overlaps = false;
            return;
        }

        // If:
        // B1: |--------------------|
        // B2:   |-------------|
        // in all dimensions -> within, then no overlap
        // B1: |--------------------|
        // B2: |-------------|
        // this is "within-touch" -> then no overlap. So use < and >
        if (min1 < min2 || max1 > max2)
        {
            one_in_two = false;
        }

        // Same other way round
        if (min2 < min1 || max2 > max1)
        {
            two_in_one = false;
        }

        box_box_loop
            <
                Dimension + 1,
                DimensionCount
            >::apply(b1, b2, overlaps, one_in_two, two_in_one);
    }
};

template
<
    std::size_t DimensionCount
>
struct box_box_loop<DimensionCount, DimensionCount>
{
    template <typename Box1, typename Box2>
    static inline void apply(Box1 const& , Box2 const&, bool&, bool&, bool&)
    {
    }
};

struct box_box
{
    template <typename Box1, typename Box2, typename Strategy>
    static inline bool apply(Box1 const& b1, Box2 const& b2, Strategy const& /*strategy*/)
    {
        bool overlaps = true;
        bool within1 = true;
        bool within2 = true;
        box_box_loop
            <
                0,
                dimension<Box1>::type::value
            >::apply(b1, b2, overlaps, within1, within2);

        /*
        \see http://docs.codehaus.org/display/GEOTDOC/02+Geometry+Relationships#02GeometryRelationships-Overlaps
        where is stated that "inside" is not an "overlap",
        this is true and is implemented as such.
        */
        return overlaps && ! within1 && ! within2;
    }
};

}} // namespace detail::overlaps
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{

template <typename Box1, typename Box2>
struct overlaps<Box1, Box2, box_tag, box_tag>
    : detail::overlaps::box_box
{};


template <typename Geometry1, typename Geometry2>
struct overlaps<Geometry1, Geometry2, geometry_collection_tag, geometry_collection_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        int dimension1 = detail::gc_topological_dimension(geometry1);
        int dimension2 = detail::gc_topological_dimension(geometry2);

        if (dimension1 >= 0 && dimension2 >= 0)
        {
            if (dimension1 == 1 && dimension2 == 1)
            {
                return detail::relate::relate_impl
                    <
                        detail::de9im::static_mask_overlaps_d1_1_d2_1_type,
                        Geometry1,
                        Geometry2
                    >::apply(geometry1, geometry2, strategy);
            }
            else if (dimension1 == dimension2)
            {
                return detail::relate::relate_impl
                    <
                        detail::de9im::static_mask_overlaps_d1_eq_d2_type,
                        Geometry1,
                        Geometry2
                    >::apply(geometry1, geometry2, strategy);
            }
        }

        return false;
    }
};

template <typename Geometry1, typename Geometry2, typename Tag1>
struct overlaps<Geometry1, Geometry2, Tag1, geometry_collection_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using gc1_view_t = detail::geometry_collection_view<Geometry1>;
        return overlaps
            <
                gc1_view_t, Geometry2
            >::apply(gc1_view_t(geometry1), geometry2, strategy);
    }
};

template <typename Geometry1, typename Geometry2, typename Tag2>
struct overlaps<Geometry1, Geometry2, geometry_collection_tag, Tag2>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using gc2_view_t = detail::geometry_collection_view<Geometry2>;
        return overlaps
            <
                Geometry1, gc2_view_t
            >::apply(geometry1, gc2_view_t(geometry2), strategy);
    }
};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAPS_IMPLEMENTATION_HPP
