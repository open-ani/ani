// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2017-2022.
// Modifications copyright (c) 2017-2022 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_BUFFER_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_BUFFER_INTERFACE_HPP

#include <boost/geometry/algorithms/clear.hpp>
#include <boost/geometry/algorithms/not_implemented.hpp>
#include <boost/geometry/core/visit.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/geometries/adapted/boost_variant.hpp>
#include <boost/geometry/geometries/concepts/check.hpp>
#include <boost/geometry/strategies/buffer/services.hpp>
#include <boost/geometry/util/type_traits_std.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{

template
<
    typename Input,
    typename Output,
    typename TagIn = typename tag<Input>::type,
    typename TagOut = typename tag<Output>::type
>
struct buffer_dc : not_implemented<TagIn, TagOut>
{};

template
<
    typename Input,
    typename Output,
    typename TagIn = typename tag<Input>::type,
    typename TagOut = typename tag<Output>::type
>
struct buffer_all : not_implemented<TagIn, TagOut>
{};

} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_dynamic
{

template
<
    typename Input,
    typename TagIn = typename geometry::tag<Input>::type
>
struct buffer_dc
{
    template <typename Output, typename Distance>
    static inline void apply(Input const& geometry_in,
                             Output& geometry_out,
                             Distance const& distance,
                             Distance const& chord_length)
    {
        dispatch::buffer_dc<Input, Output>::apply(geometry_in, geometry_out, distance, chord_length);
    }
};

template <typename Input>
struct buffer_dc<Input, dynamic_geometry_tag>
{
    template <typename Output, typename Distance>
    static inline void apply(Input const& geometry_in,
                             Output& geometry_out,
                             Distance const& distance,
                             Distance const& chord_length)
    {
        traits::visit<Input>::apply([&](auto const& g)
        {
            dispatch::buffer_dc
                <
                    util::remove_cref_t<decltype(g)>, Output
                >::apply(g, geometry_out, distance, chord_length);
        }, geometry_in);
    }
};


template
<
    typename Input,
    typename TagIn = typename geometry::tag<Input>::type
>
struct buffer_all
{
    template
    <
        typename Output,
        typename DistanceStrategy,
        typename SideStrategy,
        typename JoinStrategy,
        typename EndStrategy,
        typename PointStrategy
    >
    static inline void apply(Input const& geometry_in,
                             Output& geometry_out,
                             DistanceStrategy const& distance_strategy,
                             SideStrategy const& side_strategy,
                             JoinStrategy const& join_strategy,
                             EndStrategy const& end_strategy,
                             PointStrategy const& point_strategy)
    {
        typename strategies::buffer::services::default_strategy
            <
                Input
            >::type strategies;

        dispatch::buffer_all
            <
                Input, Output
            >::apply(geometry_in, geometry_out, distance_strategy, side_strategy,
                     join_strategy, end_strategy, point_strategy, strategies);
    }
};

template <typename Input>
struct buffer_all<Input, dynamic_geometry_tag>
{
    template
    <
        typename Output,
        typename DistanceStrategy,
        typename SideStrategy,
        typename JoinStrategy,
        typename EndStrategy,
        typename PointStrategy
    >
    static inline void apply(Input const& geometry_in,
                             Output& geometry_out,
                             DistanceStrategy const& distance_strategy,
                             SideStrategy const& side_strategy,
                             JoinStrategy const& join_strategy,
                             EndStrategy const& end_strategy,
                             PointStrategy const& point_strategy)
    {
        traits::visit<Input>::apply([&](auto const& g)
        {
            buffer_all
                <
                    util::remove_cref_t<decltype(g)>
                >::apply(g, geometry_out, distance_strategy, side_strategy,
                         join_strategy, end_strategy, point_strategy);
        }, geometry_in);
    }
};

} // namespace resolve_dynamic


/*!
\brief \brief_calc{buffer}
\ingroup buffer
\details \details_calc{buffer, \det_buffer}.
\tparam Input \tparam_geometry
\tparam Output \tparam_geometry
\tparam Distance \tparam_numeric
\param geometry_in \param_geometry
\param geometry_out \param_geometry
\param distance The distance to be used for the buffer
\param chord_length (optional) The length of the chord's in the generated arcs around points or bends

\qbk{[include reference/algorithms/buffer.qbk]}
 */
template <typename Input, typename Output, typename Distance>
inline void buffer(Input const& geometry_in, Output& geometry_out,
                   Distance const& distance, Distance const& chord_length = -1)
{
    concepts::check<Input const>();
    concepts::check<Output>();

    resolve_dynamic::buffer_dc<Input>::apply(geometry_in, geometry_out, distance, chord_length);
}

/*!
\brief \brief_calc{buffer}
\ingroup buffer
\details \details_calc{return_buffer, \det_buffer}. \details_return{buffer}.
\tparam Input \tparam_geometry
\tparam Output \tparam_geometry
\tparam Distance \tparam_numeric
\param geometry \param_geometry
\param distance The distance to be used for the buffer
\param chord_length (optional) The length of the chord's in the generated arcs
    around points or bends (RESERVED, NOT YET USED)
\return \return_calc{buffer}
 */
template <typename Output, typename Input, typename Distance>
inline Output return_buffer(Input const& geometry, Distance const& distance,
                            Distance const& chord_length = -1)
{
    concepts::check<Input const>();
    concepts::check<Output>();

    Output geometry_out;

    resolve_dynamic::buffer_dc<Input>::apply(geometry, geometry_out, distance, chord_length);

    return geometry_out;
}

/*!
\brief \brief_calc{buffer}
\ingroup buffer
\details \details_calc{buffer, \det_buffer}.
\tparam GeometryIn \tparam_geometry
\tparam GeometryOut \tparam_geometry{GeometryOut}
\tparam DistanceStrategy A strategy defining distance (or radius)
\tparam SideStrategy A strategy defining creation along sides
\tparam JoinStrategy A strategy defining creation around convex corners
\tparam EndStrategy A strategy defining creation at linestring ends
\tparam PointStrategy A strategy defining creation around points
\param geometry_in \param_geometry
\param geometry_out output geometry, e.g. multi polygon,
       will contain a buffered version of the input geometry
\param distance_strategy The distance strategy to be used
\param side_strategy The side strategy to be used
\param join_strategy The join strategy to be used
\param end_strategy The end strategy to be used
\param point_strategy The point strategy to be used

\qbk{distinguish,with strategies}
\qbk{[include reference/algorithms/buffer_with_strategies.qbk]}
 */
template
<
    typename GeometryIn,
    typename GeometryOut,
    typename DistanceStrategy,
    typename SideStrategy,
    typename JoinStrategy,
    typename EndStrategy,
    typename PointStrategy
>
inline void buffer(GeometryIn const& geometry_in,
                   GeometryOut& geometry_out,
                   DistanceStrategy const& distance_strategy,
                   SideStrategy const& side_strategy,
                   JoinStrategy const& join_strategy,
                   EndStrategy const& end_strategy,
                   PointStrategy const& point_strategy)
{
    concepts::check<GeometryIn const>();
    concepts::check<GeometryOut>();

    geometry::clear(geometry_out);

    resolve_dynamic::buffer_all
        <
            GeometryIn, GeometryOut
        >::apply(geometry_in, geometry_out, distance_strategy, side_strategy,
                 join_strategy, end_strategy, point_strategy);
}


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_BUFFER_INTERFACE_HPP
