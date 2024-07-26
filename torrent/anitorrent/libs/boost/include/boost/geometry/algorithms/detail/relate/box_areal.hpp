// Boost.Geometry

// Copyright (c) 2022, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_BOX_AREAL_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_BOX_AREAL_HPP

#include <boost/geometry/algorithms/detail/relate/areal_areal.hpp>
#include <boost/geometry/views/box_view.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace relate {


// The implementation of an algorithm calculating relate() for B/A
template <typename Box, typename Areal>
struct box_areal
{
    static const bool interruption_enabled = true;

    template <typename Result, typename Strategy>
    static inline void apply(Box const& box, Areal const& areal,
                             Result& result,
                             Strategy const& strategy)
    {
        using is_cartesian = std::is_same
            <
                typename Strategy::cs_tag,
                cartesian_tag
            >;
        apply(box, areal, result, strategy, is_cartesian());
    }

    template <typename Result, typename Strategy>
    static inline void apply(Box const& box, Areal const& areal,
                             Result& result,
                             Strategy const& strategy,
                             std::true_type /*is_cartesian*/)
    {
        using box_view = boost::geometry::box_view<Box>;
        box_view view(box);
        areal_areal<box_view, Areal>::apply(view, areal, result, strategy);
    }

    template <typename Result, typename Strategy>
    static inline void apply(Box const& /* box */, Areal const& /* areal */,
                             Result& /* result */,
                             Strategy const& /* strategy */,
                             std::false_type /*is_cartesian*/)
    {
        BOOST_GEOMETRY_STATIC_ASSERT_FALSE(
            "Not implemented for this coordinate system.",
            typename Strategy::cs_tag());
    }
};

}} // namespace detail::relate
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_BOX_AREAL_HPP
