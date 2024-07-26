// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2015 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2015 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2015-2021.
// Modifications copyright (c) 2015-2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Distributed under the Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_RANGE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_RANGE_HPP

#include <iterator>
#include <vector>

#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>

#include <boost/geometry/algorithms/is_empty.hpp>
#include <boost/geometry/algorithms/detail/dummy_geometries.hpp>
#include <boost/geometry/algorithms/detail/envelope/initialize.hpp>
#include <boost/geometry/algorithms/detail/expand/box.hpp>
#include <boost/geometry/algorithms/detail/expand/point.hpp>
#include <boost/geometry/algorithms/detail/expand/segment.hpp>

#include <boost/geometry/core/coordinate_dimension.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace envelope
{


// implementation for simple ranges
struct envelope_range
{
    template <typename Range, typename Box, typename Strategies>
    static inline void apply(Range const& range, Box& mbr, Strategies const& strategies)
    {
        strategies.envelope(range, mbr).apply(range, mbr);
    }
};


// implementation for multi-ranges
template <typename EnvelopePolicy>
struct envelope_multi_range
{
    template <typename MultiRange, typename Box, typename Strategies>
    static inline void apply(MultiRange const& multirange,
                             Box& mbr,
                             Strategies const& strategies)
    {
        using strategy_t = decltype(strategies.envelope(multirange, mbr));
        apply<strategy_t>(multirange, mbr, strategies);
    }

    template <typename Strategy, typename MultiRange, typename Box, typename Strategies>
    static inline void apply(MultiRange const& multirange,
                             Box& mbr,
                             Strategies const& strategies)
    {
        typename Strategy::template state<Box> state;
        auto const end = boost::end(multirange);
        for (auto it = boost::begin(multirange); it != end; ++it)
        {
            if (! geometry::is_empty(*it))
            {
                Box helper_mbr;
                EnvelopePolicy::apply(*it, helper_mbr, strategies);
                Strategy::apply(state, helper_mbr);
            }
        }
        Strategy::result(state, mbr);
    }
};


}} // namespace detail::envelope
#endif // DOXYGEN_NO_DETAIL


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_RANGE_HPP
