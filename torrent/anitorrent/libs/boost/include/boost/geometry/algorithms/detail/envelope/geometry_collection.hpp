// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Distributed under the Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_GEOMETRY_COLLECTION_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_GEOMETRY_COLLECTION_HPP


#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/algorithms/dispatch/envelope.hpp>
#include <boost/geometry/algorithms/is_empty.hpp>

#include <boost/geometry/core/tags.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template <typename Collection>
struct envelope<Collection, geometry_collection_tag>
{
    template <typename Geometry, typename Box, typename Strategies>
    static inline void apply(Geometry const& geometry,
                             Box& mbr,
                             Strategies const& strategies)
    {
        using strategy_t = decltype(strategies.envelope(geometry, mbr));

        typename strategy_t::template state<Box> state;
        detail::visit_breadth_first([&](auto const& g)
        {
            if (! geometry::is_empty(g))
            {
                Box b;
                envelope<util::remove_cref_t<decltype(g)>>::apply(g, b, strategies);
                strategy_t::apply(state, b);
            }
            return true;
        }, geometry);
        strategy_t::result(state, mbr);
    }
};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_GEOMETRY_COLLECTION_HPP
