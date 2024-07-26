// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2014-2022, Oracle and/or its affiliates.

// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html


#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_TURNS_REMOVE_DUPLICATE_TURNS_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_TURNS_REMOVE_DUPLICATE_TURNS_HPP

#include <algorithm>
#include <boost/geometry/algorithms/detail/equals/point_point.hpp>

namespace boost { namespace geometry
{

namespace detail { namespace turns
{

template <typename Turns, bool Enable>
struct remove_duplicate_turns
{
    template <typename Strategy>
    static inline void apply(Turns const&, Strategy const&) {}
};



template <typename Turns>
struct remove_duplicate_turns<Turns, true>
{
    template <typename Strategy>
    static inline void apply(Turns& turns, Strategy const& strategy)
    {
        turns.erase(
            std::unique(turns.begin(), turns.end(),
                [&](auto const& t1, auto const& t2)
                {
                    return detail::equals::equals_point_point(t1.point, t2.point, strategy)
                        && t1.operations[0].seg_id == t2.operations[0].seg_id
                        && t1.operations[1].seg_id == t2.operations[1].seg_id;
                }),
            turns.end());
    }
};



}} // namespace detail::turns

}} // namespect boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_TURNS_REMOVE_DUPLICATE_TURNS_HPP
