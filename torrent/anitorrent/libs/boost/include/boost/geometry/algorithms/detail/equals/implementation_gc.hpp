// Boost.Geometry

// Copyright (c) 2022 Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_EQUALS_IMPLEMENTATION_GC_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_EQUALS_IMPLEMENTATION_GC_HPP


#include <boost/geometry/algorithms/detail/equals/implementation.hpp>
#include <boost/geometry/algorithms/detail/relate/implementation_gc.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template <typename Geometry1, typename Geometry2, std::size_t DimensionCount>
struct equals
    <
        Geometry1, Geometry2,
        geometry_collection_tag, geometry_collection_tag,
        geometry_collection_tag, geometry_collection_tag,
        DimensionCount, false
    >
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        return detail::relate::relate_impl
            <
                detail::de9im::static_mask_equals_type,
                Geometry1,
                Geometry2
            >::apply(geometry1, geometry2, strategy);
    }
};


template
<
    typename Geometry1, typename Geometry2,
    typename Tag1, typename CastedTag1,
    std::size_t DimensionCount
>
struct equals
    <
        Geometry1, Geometry2,
        Tag1, geometry_collection_tag,
        CastedTag1, geometry_collection_tag,
        DimensionCount, false
    >
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using gc1_view_t = detail::geometry_collection_view<Geometry1>;
        return equals
            <
                gc1_view_t, Geometry2
            >::apply(gc1_view_t(geometry1), geometry2, strategy);
    }
};


template
<
    typename Geometry1, typename Geometry2,
    typename Tag2, typename CastedTag2,
    std::size_t DimensionCount
>
struct equals
    <
        Geometry1, Geometry2,
        geometry_collection_tag, Tag2,
        geometry_collection_tag, CastedTag2,
        DimensionCount, false
    >
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using gc2_view_t = detail::geometry_collection_view<Geometry2>;
        return equals
            <
                Geometry1, gc2_view_t
            >::apply(geometry1, gc2_view_t(geometry2), strategy);
    }
};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_EQUALS_IMPLEMENTATION_GC_HPP
