// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2015 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2015 Mateusz Loskot, London, UK.
// Copyright (c) 2023 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2014-2021.
// Modifications copyright (c) 2014-2021 Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_CONVEX_HULL_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_CONVEX_HULL_INTERFACE_HPP

#include <array>

#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/detail/assign_box_corners.hpp>
#include <boost/geometry/algorithms/detail/convex_hull/graham_andrew.hpp>
#include <boost/geometry/algorithms/detail/equals/point_point.hpp>
#include <boost/geometry/algorithms/detail/for_each_range.hpp>
#include <boost/geometry/algorithms/detail/select_geometry_type.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/algorithms/is_empty.hpp>

#include <boost/geometry/core/closure.hpp>
#include <boost/geometry/core/cs.hpp>
#include <boost/geometry/core/exterior_ring.hpp>
#include <boost/geometry/core/geometry_types.hpp>
#include <boost/geometry/core/point_order.hpp>
#include <boost/geometry/core/ring_type.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/geometries/concepts/check.hpp>
#include <boost/geometry/geometries/ring.hpp>

#include <boost/geometry/strategies/convex_hull/cartesian.hpp>
#include <boost/geometry/strategies/convex_hull/geographic.hpp>
#include <boost/geometry/strategies/convex_hull/spherical.hpp>
#include <boost/geometry/strategies/default_strategy.hpp>

#include <boost/geometry/util/constexpr.hpp>
#include <boost/geometry/util/range.hpp>
#include <boost/geometry/util/sequence.hpp>
#include <boost/geometry/util/type_traits.hpp>


namespace boost { namespace geometry
{

// TODO: This file is named interface.hpp but the code below is not the interface.
//       It's the implementation of the algorithm.

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace convex_hull
{

// Abstraction representing ranges/rings of a geometry
template <typename Geometry>
struct input_geometry_proxy
{
    input_geometry_proxy(Geometry const& geometry)
        : m_geometry(geometry)
    {}

    template <typename UnaryFunction>
    inline void for_each_range(UnaryFunction fun) const
    {
        geometry::detail::for_each_range(m_geometry, fun);
    }

    Geometry const& m_geometry;
};

// Abstraction representing ranges/rings of subgeometries of geometry collection
// with boxes converted to rings
template <typename Geometry, typename BoxRings>
struct input_geometry_collection_proxy
{
    input_geometry_collection_proxy(Geometry const& geometry, BoxRings const& box_rings)
        : m_geometry(geometry)
        , m_box_rings(box_rings)
    {}

    template <typename UnaryFunction>
    inline void for_each_range(UnaryFunction fun) const
    {
        detail::visit_breadth_first([&](auto const& g)
        {
            input_geometry_collection_proxy::call_for_non_boxes(g, fun);
            return true;
        }, m_geometry);

        for (auto const& r : m_box_rings)
        {
            geometry::detail::for_each_range(r, fun);
        }
    }

private:
    template <typename G, typename F, std::enable_if_t<! util::is_box<G>::value, int> = 0>
    static inline void call_for_non_boxes(G const& g, F & f)
    {
        geometry::detail::for_each_range(g, f);
    }
    template <typename G, typename F, std::enable_if_t<util::is_box<G>::value, int> = 0>
    static inline void call_for_non_boxes(G const&, F &)
    {}

    Geometry const& m_geometry;
    BoxRings const& m_box_rings;
};


// TODO: Or just implement point_type<> for GeometryCollection
//   and enforce the same point_type used in the whole sequence in check().
template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct default_strategy
{
    using type = typename strategies::convex_hull::services::default_strategy
        <
            Geometry
        >::type;
};

template <typename Geometry>
struct default_strategy<Geometry, geometry_collection_tag>
    : default_strategy<typename detail::first_geometry_type<Geometry>::type>
{};


// Utilities for output GC and DG
template <typename G1, typename G2>
struct output_polygonal_less
{
    template <typename G>
    using priority = std::integral_constant
        <
            int,
            (util::is_ring<G>::value ? 0 :
             util::is_polygon<G>::value ? 1 :
             util::is_multi_polygon<G>::value ? 2 : 3)
        >;

    static const bool value = priority<G1>::value < priority<G2>::value;
};

template <typename G1, typename G2>
struct output_linear_less
{
    template <typename G>
    using priority = std::integral_constant
        <
            int,
            (util::is_segment<G>::value ? 0 :
             util::is_linestring<G>::value ? 1 :
             util::is_multi_linestring<G>::value ? 2 : 3)
        >;

    static const bool value = priority<G1>::value < priority<G2>::value;
};

template <typename G1, typename G2>
struct output_pointlike_less
{
    template <typename G>
    using priority = std::integral_constant
        <
            int,
            (util::is_point<G>::value ? 0 :
             util::is_multi_point<G>::value ? 1 : 2)
        >;

    static const bool value = priority<G1>::value < priority<G2>::value;
};


}} // namespace detail::convex_hull
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template
<
    typename Geometry,
    typename Tag = typename tag<Geometry>::type
>
struct convex_hull
{
    template <typename OutputGeometry, typename Strategy>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             Strategy const& strategy)
    {
        detail::convex_hull::input_geometry_proxy<Geometry> in_proxy(geometry);
        detail::convex_hull::graham_andrew
            <
                typename point_type<Geometry>::type
            >::apply(in_proxy, out, strategy);
    }
};


// A hull for boxes is trivial. Any strategy is (currently) skipped.
// TODO: This is not correct in spherical and geographic CS.
template <typename Box>
struct convex_hull<Box, box_tag>
{
    template <typename OutputGeometry, typename Strategy>
    static inline void apply(Box const& box,
                             OutputGeometry& out,
                             Strategy const& )
    {
        static bool const Close
            = geometry::closure<OutputGeometry>::value == closed;
        static bool const Reverse
            = geometry::point_order<OutputGeometry>::value == counterclockwise;

        std::array<typename point_type<OutputGeometry>::type, 4> arr;
        // TODO: This assigns only 2d cooridnates!
        //       And it is also used in box_view<>!
        geometry::detail::assign_box_corners_oriented<Reverse>(box, arr);

        std::move(arr.begin(), arr.end(), range::back_inserter(out));
        if BOOST_GEOMETRY_CONSTEXPR (Close)
        {
            range::push_back(out, range::front(out));
        }
    }
};


template <typename GeometryCollection>
struct convex_hull<GeometryCollection, geometry_collection_tag>
{
    template <typename OutputGeometry, typename Strategy>
    static inline void apply(GeometryCollection const& geometry,
                             OutputGeometry& out,
                             Strategy const& strategy)
    {
        // Assuming that single point_type is used by the GeometryCollection
        using subgeometry_type = typename detail::first_geometry_type<GeometryCollection>::type;
        using point_type = typename geometry::point_type<subgeometry_type>::type;
        using ring_type = model::ring<point_type, true, false>;

        // Calculate box rings once
        std::vector<ring_type> box_rings;
        detail::visit_breadth_first([&](auto const& g)
        {
            convex_hull::add_ring_for_box(box_rings, g, strategy);
            return true;
        }, geometry);

        detail::convex_hull::input_geometry_collection_proxy
            <
                GeometryCollection, std::vector<ring_type>
            > in_proxy(geometry, box_rings);

        detail::convex_hull::graham_andrew
            <
                point_type
            >::apply(in_proxy, out, strategy);
    }

private:
    template
    <
        typename Ring, typename SubGeometry, typename Strategy,
        std::enable_if_t<util::is_box<SubGeometry>::value, int> = 0
    >
    static inline void add_ring_for_box(std::vector<Ring> & rings, SubGeometry const& box,
                                        Strategy const& strategy)
    {
        Ring ring;
        convex_hull<SubGeometry>::apply(box, ring, strategy);
        rings.push_back(std::move(ring));
    }
    template
    <
        typename Ring, typename SubGeometry, typename Strategy,
        std::enable_if_t<! util::is_box<SubGeometry>::value, int> = 0
    >
    static inline void add_ring_for_box(std::vector<Ring> & , SubGeometry const& ,
                                        Strategy const& )
    {}
};


template <typename OutputGeometry, typename Tag = typename tag<OutputGeometry>::type>
struct convex_hull_out
{
    BOOST_GEOMETRY_STATIC_ASSERT_FALSE("This OutputGeometry is not supported.", OutputGeometry, Tag);
};

template <typename OutputGeometry>
struct convex_hull_out<OutputGeometry, ring_tag>
{
    template <typename Geometry, typename Strategies>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             Strategies const& strategies)
    {
        dispatch::convex_hull<Geometry>::apply(geometry, out, strategies);
    }
};

template <typename OutputGeometry>
struct convex_hull_out<OutputGeometry, polygon_tag>
{
    template <typename Geometry, typename Strategies>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             Strategies const& strategies)
    {
        auto&& ring = exterior_ring(out);
        dispatch::convex_hull<Geometry>::apply(geometry, ring, strategies);
    }
};

template <typename OutputGeometry>
struct convex_hull_out<OutputGeometry, multi_polygon_tag>
{
    template <typename Geometry, typename Strategies>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             Strategies const& strategies)
    {
        typename boost::range_value<OutputGeometry>::type polygon;
        auto&& ring = exterior_ring(polygon);
        dispatch::convex_hull<Geometry>::apply(geometry, ring, strategies);
        // Empty input is checked so the output shouldn't be empty
        range::push_back(out, std::move(polygon));
    }
};

template <typename OutputGeometry>
struct convex_hull_out<OutputGeometry, geometry_collection_tag>
{
    using polygonal_t = typename util::sequence_min_element
        <
            typename traits::geometry_types<OutputGeometry>::type,
            detail::convex_hull::output_polygonal_less
        >::type;
    using linear_t = typename util::sequence_min_element
        <
            typename traits::geometry_types<OutputGeometry>::type,
            detail::convex_hull::output_linear_less
        >::type;
    using pointlike_t = typename util::sequence_min_element
        <
            typename traits::geometry_types<OutputGeometry>::type,
            detail::convex_hull::output_pointlike_less
        >::type;

    // select_element may define different kind of geometry than the one that is desired
    BOOST_GEOMETRY_STATIC_ASSERT(util::is_polygonal<polygonal_t>::value,
        "It must be possible to store polygonal geometry in OutputGeometry.", polygonal_t);
    BOOST_GEOMETRY_STATIC_ASSERT(util::is_linear<linear_t>::value,
        "It must be possible to store linear geometry in OutputGeometry.", linear_t);
    BOOST_GEOMETRY_STATIC_ASSERT(util::is_pointlike<pointlike_t>::value,
        "It must be possible to store pointlike geometry in OutputGeometry.", pointlike_t);

    template <typename Geometry, typename Strategies>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             Strategies const& strategies)
    {
        polygonal_t polygonal;
        convex_hull_out<polygonal_t>::apply(geometry, polygonal, strategies);
        // Empty input is checked so the output shouldn't be empty
        auto&& out_ring = ring(polygonal);

        if (boost::size(out_ring) == detail::minimum_ring_size<polygonal_t>::value)
        {
            using detail::equals::equals_point_point;
            if (equals_point_point(range::front(out_ring), range::at(out_ring, 1), strategies))
            {
                pointlike_t pointlike;
                move_to_pointlike(out_ring, pointlike);
                move_to_out(pointlike, out);
                return;
            }
            if (equals_point_point(range::front(out_ring), range::at(out_ring, 2), strategies))
            {
                linear_t linear;
                move_to_linear(out_ring, linear);
                move_to_out(linear, out);
                return;
            }
        }

        move_to_out(polygonal, out);
    }

private:
    template <typename Polygonal, util::enable_if_ring_t<Polygonal, int> = 0>
    static decltype(auto) ring(Polygonal const& polygonal)
    {
        return polygonal;
    }
    template <typename Polygonal, util::enable_if_polygon_t<Polygonal, int> = 0>
    static decltype(auto) ring(Polygonal const& polygonal)
    {
        return exterior_ring(polygonal);
    }
    template <typename Polygonal, util::enable_if_multi_polygon_t<Polygonal, int> = 0>
    static decltype(auto) ring(Polygonal const& polygonal)
    {
        return exterior_ring(range::front(polygonal));
    }

    template <typename Range, typename Linear, util::enable_if_segment_t<Linear, int> = 0>
    static void move_to_linear(Range & out_range, Linear & seg)
    {
        detail::assign_point_to_index<0>(range::front(out_range), seg);
        detail::assign_point_to_index<1>(range::at(out_range, 1), seg);
    }
    template <typename Range, typename Linear, util::enable_if_linestring_t<Linear, int> = 0>
    static void move_to_linear(Range & out_range, Linear & ls)
    {
        std::move(boost::begin(out_range), boost::begin(out_range) + 2, range::back_inserter(ls));
    }
    template <typename Range, typename Linear, util::enable_if_multi_linestring_t<Linear, int> = 0>
    static void move_to_linear(Range & out_range, Linear & mls)
    {
        typename boost::range_value<Linear>::type ls;
        std::move(boost::begin(out_range), boost::begin(out_range) + 2, range::back_inserter(ls));
        range::push_back(mls, std::move(ls));
    }

    template <typename Range, typename PointLike, util::enable_if_point_t<PointLike, int> = 0>
    static void move_to_pointlike(Range & out_range, PointLike & pt)
    {
        pt = range::front(out_range);
    }
    template <typename Range, typename PointLike, util::enable_if_multi_point_t<PointLike, int> = 0>
    static void move_to_pointlike(Range & out_range, PointLike & mpt)
    {
        range::push_back(mpt, std::move(range::front(out_range)));
    }

    template
    <
        typename Geometry, typename OutputGeometry_,
        util::enable_if_geometry_collection_t<OutputGeometry_, int> = 0
    >
    static void move_to_out(Geometry & g, OutputGeometry_ & out)
    {
        range::emplace_back(out, std::move(g));
    }
    template
    <
        typename Geometry, typename OutputGeometry_,
        util::enable_if_dynamic_geometry_t<OutputGeometry_, int> = 0
    >
    static void move_to_out(Geometry & g, OutputGeometry_ & out)
    {
        out = std::move(g);
    }
};

template <typename OutputGeometry>
struct convex_hull_out<OutputGeometry, dynamic_geometry_tag>
    : convex_hull_out<OutputGeometry, geometry_collection_tag>
{};


// For backward compatibility
template <typename OutputGeometry>
struct convex_hull_out<OutputGeometry, linestring_tag>
    : convex_hull_out<OutputGeometry, ring_tag>
{};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_strategy {

template <typename Strategies>
struct convex_hull
{
    template <typename Geometry, typename OutputGeometry>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             Strategies const& strategies)
    {
        dispatch::convex_hull_out<OutputGeometry>::apply(geometry, out, strategies);
    }
};

template <>
struct convex_hull<default_strategy>
{
    template <typename Geometry, typename OutputGeometry>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             default_strategy const&)
    {
        using strategy_type = typename detail::convex_hull::default_strategy
            <
                Geometry
            >::type;

        dispatch::convex_hull_out<OutputGeometry>::apply(geometry, out, strategy_type());
    }
};


} // namespace resolve_strategy


namespace resolve_dynamic {

template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct convex_hull
{
    template <typename OutputGeometry, typename Strategy>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             Strategy const& strategy)
    {
        concepts::check_concepts_and_equal_dimensions<
            const Geometry,
            OutputGeometry
        >();

        resolve_strategy::convex_hull<Strategy>::apply(geometry, out, strategy);
    }
};

template <typename Geometry>
struct convex_hull<Geometry, dynamic_geometry_tag>
{
    template <typename OutputGeometry, typename Strategy>
    static inline void apply(Geometry const& geometry,
                             OutputGeometry& out,
                             Strategy const& strategy)
    {
        traits::visit<Geometry>::apply([&](auto const& g)
        {
            convex_hull<util::remove_cref_t<decltype(g)>>::apply(g, out, strategy);
        }, geometry);
    }
};


} // namespace resolve_dynamic


/*!
\brief \brief_calc{convex hull} \brief_strategy
\ingroup convex_hull
\details \details_calc{convex_hull,convex hull} \brief_strategy.
\tparam Geometry the input geometry type
\tparam OutputGeometry the output geometry type
\tparam Strategy the strategy type
\param geometry \param_geometry,  input geometry
\param out \param_geometry \param_set{convex hull}
\param strategy \param_strategy{area}

\qbk{distinguish,with strategy}

\qbk{[include reference/algorithms/convex_hull.qbk]}
 */
template<typename Geometry, typename OutputGeometry, typename Strategy>
inline void convex_hull(Geometry const& geometry, OutputGeometry& out, Strategy const& strategy)
{
    if (geometry::is_empty(geometry))
    {
        // Leave output empty
        return;
    }

    resolve_dynamic::convex_hull<Geometry>::apply(geometry, out, strategy);
}


/*!
\brief \brief_calc{convex hull}
\ingroup convex_hull
\details \details_calc{convex_hull,convex hull}.
\tparam Geometry the input geometry type
\tparam OutputGeometry the output geometry type
\param geometry \param_geometry,  input geometry
\param hull \param_geometry \param_set{convex hull}

\qbk{[include reference/algorithms/convex_hull.qbk]}
 */
template<typename Geometry, typename OutputGeometry>
inline void convex_hull(Geometry const& geometry, OutputGeometry& hull)
{
    geometry::convex_hull(geometry, hull, default_strategy());
}


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_CONVEX_HULL_INTERFACE_HPP
