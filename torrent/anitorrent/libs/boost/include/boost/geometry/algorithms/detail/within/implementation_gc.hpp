// Boost.Geometry

// Copyright (c) 2022 Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_WITHIN_IMPLEMENTATION_GC_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_WITHIN_IMPLEMENTATION_GC_HPP


#include <boost/geometry/algorithms/detail/relate/implementation_gc.hpp>
#include <boost/geometry/algorithms/detail/within/implementation.hpp>


namespace boost { namespace geometry {


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch {

template <typename Geometry1, typename Geometry2>
struct within<Geometry1, Geometry2, geometry_collection_tag, geometry_collection_tag>
    : detail::within::use_relate
{};

template <typename Geometry1, typename Geometry2, typename Tag1>
struct within<Geometry1, Geometry2, Tag1, geometry_collection_tag>
    : detail::within::use_relate
{};

template <typename Geometry1, typename Geometry2, typename Tag2>
struct within<Geometry1, Geometry2, geometry_collection_tag, Tag2>
    : detail::within::use_relate
{};

} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_WITHIN_IMPLEMENTATION_GC_HPP
