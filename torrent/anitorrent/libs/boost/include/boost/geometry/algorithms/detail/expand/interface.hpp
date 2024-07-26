// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2015 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2015 Mateusz Loskot, London, UK.
// Copyright (c) 2014-2015 Samuel Debionne, Grenoble, France.

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

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_EXPAND_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_EXPAND_INTERFACE_HPP


#include <boost/geometry/algorithms/dispatch/expand.hpp>

#include <boost/geometry/core/coordinate_system.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/expand/services.hpp>

#include <boost/geometry/util/type_traits_std.hpp>


namespace boost { namespace geometry
{

namespace resolve_strategy
{

template
<
    typename Strategy,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategy>::value
>
struct expand
{
    template <typename Box, typename Geometry>
    static inline void apply(Box& box,
                             Geometry const& geometry,
                             Strategy const& strategy)
    {
        dispatch::expand<Box, Geometry>::apply(box, geometry, strategy);
    }
};

template <typename Strategy>
struct expand<Strategy, false>
{
    template <typename Box, typename Geometry>
    static inline void apply(Box& box,
                             Geometry const& geometry,
                             Strategy const& strategy)
    {
        using strategies::expand::services::strategy_converter;
        dispatch::expand
            <
                Box, Geometry
            >::apply(box, geometry, strategy_converter<Strategy>::get(strategy));
    }
};

template <>
struct expand<default_strategy, false>
{
    template <typename Box, typename Geometry>
    static inline void apply(Box& box,
                             Geometry const& geometry,
                             default_strategy)
    {
        typedef typename strategies::expand::services::default_strategy
            <
                Box, Geometry
            >::type strategy_type;

        dispatch::expand<Box, Geometry>::apply(box, geometry, strategy_type());
    }
};

} //namespace resolve_strategy


namespace resolve_dynamic
{

template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct expand
{
    template <typename Box, typename Strategy>
    static inline void apply(Box& box,
                             Geometry const& geometry,
                             Strategy const& strategy)
    {
        concepts::check<Box>();
        concepts::check<Geometry const>();
        concepts::check_concepts_and_equal_dimensions<Box, Geometry const>();

        resolve_strategy::expand<Strategy>::apply(box, geometry, strategy);
    }
};

template <typename Geometry>
struct expand<Geometry, dynamic_geometry_tag>
{
    template <class Box, typename Strategy>
    static inline void apply(Box& box,
                             Geometry const& geometry,
                             Strategy const& strategy)
    {
        traits::visit<Geometry>::apply([&](auto const& g)
        {
            expand<util::remove_cref_t<decltype(g)>>::apply(box, g, strategy);
        }, geometry);
    }
};

} // namespace resolve_dynamic


/*!
\brief Expands (with strategy)
\ingroup expand
\tparam Box type of the box
\tparam Geometry \tparam_geometry
\tparam Strategy \tparam_strategy{expand}
\param box box to be expanded using another geometry, mutable
\param geometry \param_geometry geometry which envelope (bounding box)
\param strategy \param_strategy{expand}
will be added to the box

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/expand.qbk]}
 */
template <typename Box, typename Geometry, typename Strategy>
inline void expand(Box& box, Geometry const& geometry, Strategy const& strategy)
{
    resolve_dynamic::expand<Geometry>::apply(box, geometry, strategy);
}

/*!
\brief Expands a box using the bounding box (envelope) of another geometry
(box, point)
\ingroup expand
\tparam Box type of the box
\tparam Geometry \tparam_geometry
\param box box to be expanded using another geometry, mutable
\param geometry \param_geometry geometry which envelope (bounding box) will be
added to the box

\qbk{[include reference/algorithms/expand.qbk]}
 */
template <typename Box, typename Geometry>
inline void expand(Box& box, Geometry const& geometry)
{
    resolve_dynamic::expand<Geometry>::apply(box, geometry, default_strategy());
}

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_EXPAND_INTERFACE_HPP
