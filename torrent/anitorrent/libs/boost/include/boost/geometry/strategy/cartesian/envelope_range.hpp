// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGY_CARTESIAN_ENVELOPE_RANGE_HPP
#define BOOST_GEOMETRY_STRATEGY_CARTESIAN_ENVELOPE_RANGE_HPP

#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>

#include <boost/geometry/algorithms/detail/envelope/initialize.hpp>
#include <boost/geometry/strategy/cartesian/envelope_point.hpp>
#include <boost/geometry/strategy/cartesian/expand_point.hpp>

namespace boost { namespace geometry
{

namespace strategy { namespace envelope
{

class cartesian_range
{
public:
    template <typename Range, typename Box>
    static inline void apply(Range const& range, Box& mbr)
    {
        auto it = boost::begin(range);
        auto const end = boost::end(range);
        if (it == end)
        {
            // initialize box (assign inverse)
            geometry::detail::envelope::initialize<Box>::apply(mbr);
            return;
        }

        // initialize box with the first point
        envelope::cartesian_point::apply(*it, mbr);

        // consider now the remaining points in the range (if any)
        for (++it; it != end; ++it)
        {
            expand::cartesian_point::apply(mbr, *it);
        }
    }
};

}} // namespace strategy::envelope

}} //namepsace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGY_CARTESIAN_ENVELOPE_RANGE_HPP
