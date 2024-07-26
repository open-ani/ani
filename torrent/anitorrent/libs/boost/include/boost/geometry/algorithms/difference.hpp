// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2017-2023.
// Modifications copyright (c) 2017-2023, Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DIFFERENCE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DIFFERENCE_HPP


#include <boost/geometry/algorithms/detail/gc_make_rtree.hpp>
#include <boost/geometry/algorithms/detail/intersection/gc.hpp>
#include <boost/geometry/algorithms/detail/intersection/multi.hpp>
#include <boost/geometry/algorithms/detail/overlay/intersection_insert.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/core/geometry_types.hpp>
#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/policies/robustness/get_rescale_policy.hpp>
#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/relate/cartesian.hpp>
#include <boost/geometry/strategies/relate/geographic.hpp>
#include <boost/geometry/strategies/relate/spherical.hpp>
#include <boost/geometry/util/sequence.hpp>
#include <boost/geometry/views/detail/geometry_collection_view.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace difference
{


// True if the result of difference can be different than Geometry1
template <typename Geometry1, typename Geometry2>
using is_subtractable_t = util::bool_constant
    <
        (geometry::topological_dimension<Geometry1>::value
            <= geometry::topological_dimension<Geometry2>::value)
    >;


template
<
    typename Geometry1,
    typename Geometry2,
    typename SingleOut,
    typename OutTag = typename detail::setop_insert_output_tag<SingleOut>::type,
    bool ReturnGeometry1 = (! is_subtractable_t<Geometry1, Geometry2>::value)
>
struct call_intersection_insert
{
    template
    <
        typename OutputIterator,
        typename RobustPolicy,
        typename Strategy
    >
    static inline OutputIterator apply(Geometry1 const& geometry1,
                                       Geometry2 const& geometry2,
                                       RobustPolicy const& robust_policy,
                                       OutputIterator out,
                                       Strategy const& strategy)
    {
        return geometry::dispatch::intersection_insert
            <
                Geometry1, Geometry2,
                SingleOut,
                overlay_difference,
                geometry::detail::overlay::do_reverse<geometry::point_order<Geometry1>::value>::value,
                geometry::detail::overlay::do_reverse<geometry::point_order<Geometry2>::value, true>::value
            >::apply(geometry1, geometry2, robust_policy, out, strategy);
    }
};


template <typename Geometry1, typename Geometry2, typename SingleOut, typename OutTag>
struct call_intersection_insert<Geometry1, Geometry2, SingleOut, OutTag, true>
{
    template <typename OutputIterator, typename RobustPolicy, typename Strategy>
    static inline OutputIterator apply(Geometry1 const& geometry1,
                                       Geometry2 const& ,
                                       RobustPolicy const& ,
                                       OutputIterator out,
                                       Strategy const& )
    {
        return geometry::detail::convert_to_output
            <
                Geometry1,
                SingleOut
            >::apply(geometry1, out);
    }
};


template
<
    typename Geometry1,
    typename Geometry2,
    typename SingleOut
>
struct call_intersection_insert_tupled_base
{
    typedef typename geometry::detail::single_tag_from_base_tag
        <
            typename geometry::tag_cast
                <
                    typename geometry::tag<Geometry1>::type,
                    pointlike_tag, linear_tag, areal_tag
                >::type
        >::type single_tag;

    typedef detail::expect_output
        <
            Geometry1, Geometry2, SingleOut, single_tag
        > expect_check;

    typedef typename geometry::detail::output_geometry_access
        <
            SingleOut, single_tag, single_tag
        > access;
};

template
<
    typename Geometry1,
    typename Geometry2,
    typename SingleOut
>
struct call_intersection_insert
    <
        Geometry1, Geometry2, SingleOut,
        detail::tupled_output_tag,
        false
    >
    : call_intersection_insert_tupled_base<Geometry1, Geometry2, SingleOut>
{
    typedef call_intersection_insert_tupled_base<Geometry1, Geometry2, SingleOut> base_t;

    template
    <
        typename OutputIterator,
        typename RobustPolicy,
        typename Strategy
    >
    static inline OutputIterator apply(Geometry1 const& geometry1,
                                       Geometry2 const& geometry2,
                                       RobustPolicy const& robust_policy,
                                       OutputIterator out,
                                       Strategy const& strategy)
    {
        base_t::access::get(out) = call_intersection_insert
            <
                Geometry1, Geometry2,
                typename base_t::access::type
            >::apply(geometry1, geometry2, robust_policy,
                     base_t::access::get(out), strategy);

        return out;
    }
};

template
<
    typename Geometry1,
    typename Geometry2,
    typename SingleOut
>
struct call_intersection_insert
    <
        Geometry1, Geometry2, SingleOut,
        detail::tupled_output_tag,
        true
    >
    : call_intersection_insert_tupled_base<Geometry1, Geometry2, SingleOut>
{
    typedef call_intersection_insert_tupled_base<Geometry1, Geometry2, SingleOut> base_t;

    template
    <
        typename OutputIterator,
        typename RobustPolicy,
        typename Strategy
    >
    static inline OutputIterator apply(Geometry1 const& geometry1,
                                       Geometry2 const& ,
                                       RobustPolicy const& ,
                                       OutputIterator out,
                                       Strategy const& )
    {
        base_t::access::get(out) = geometry::detail::convert_to_output
            <
                Geometry1,
                typename base_t::access::type
            >::apply(geometry1, base_t::access::get(out));

        return out;
    }
};


/*!
\brief_calc2{difference} \brief_strategy
\ingroup difference
\details \details_calc2{difference_insert, spatial set theoretic difference}
    \brief_strategy. \details_inserter{difference}
\tparam GeometryOut output geometry type, must be specified
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam OutputIterator output iterator
\tparam Strategy \tparam_strategy_overlay
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param out \param_out{difference}
\param strategy \param_strategy{difference}
\return \return_out

\qbk{distinguish,with strategy}
*/
template
<
    typename GeometryOut,
    typename Geometry1,
    typename Geometry2,
    typename OutputIterator,
    typename Strategy
>
inline OutputIterator difference_insert(Geometry1 const& geometry1,
                                        Geometry2 const& geometry2,
                                        OutputIterator out,
                                        Strategy const& strategy)
{
    concepts::check<Geometry1 const>();
    concepts::check<Geometry2 const>();
    //concepts::check<GeometryOut>();
    geometry::detail::output_geometry_concept_check<GeometryOut>::apply();

    typedef typename geometry::rescale_overlay_policy_type
        <
            Geometry1,
            Geometry2,
            typename Strategy::cs_tag
        >::type rescale_policy_type;

    rescale_policy_type robust_policy
        = geometry::get_rescale_policy<rescale_policy_type>(
            geometry1, geometry2, strategy);

    return geometry::detail::difference::call_intersection_insert
        <
            Geometry1, Geometry2, GeometryOut
        >::apply(geometry1, geometry2, robust_policy, out, strategy);
}

/*!
\brief_calc2{difference}
\ingroup difference
\details \details_calc2{difference_insert, spatial set theoretic difference}.
    \details_insert{difference}
\tparam GeometryOut output geometry type, must be specified
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam OutputIterator output iterator
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param out \param_out{difference}
\return \return_out

\qbk{[include reference/algorithms/difference_insert.qbk]}
*/
template
<
    typename GeometryOut,
    typename Geometry1,
    typename Geometry2,
    typename OutputIterator
>
inline OutputIterator difference_insert(Geometry1 const& geometry1,
                                        Geometry2 const& geometry2,
                                        OutputIterator out)
{
    typedef typename strategies::relate::services::default_strategy
        <
            Geometry1,
            Geometry2
        >::type strategy_type;

    return difference_insert<GeometryOut>(geometry1, geometry2, out,
                                          strategy_type());
}


template
<
    typename Geometry, typename Collection,
    typename CastedTag = typename geometry::tag_cast
        <
            typename geometry::tag<Geometry>::type,
            pointlike_tag, linear_tag, areal_tag
        >::type
>
struct multi_output_type
{
    BOOST_GEOMETRY_STATIC_ASSERT_FALSE(
        "Not implemented this Geometry type.",
        Geometry, CastedTag);
};

template <typename Geometry, typename Collection>
struct multi_output_type<Geometry, Collection, pointlike_tag>
{
    using type = typename util::sequence_find_if
        <
            typename traits::geometry_types<Collection>::type,
            util::is_multi_point
        >::type;
};

template <typename Geometry, typename Collection>
struct multi_output_type<Geometry, Collection, linear_tag>
{
    using type = typename util::sequence_find_if
        <
            typename traits::geometry_types<Collection>::type,
            util::is_multi_linestring
        >::type;
};

template <typename Geometry, typename Collection>
struct multi_output_type<Geometry, Collection, areal_tag>
{
    using type = typename util::sequence_find_if
        <
            typename traits::geometry_types<Collection>::type,
            util::is_multi_polygon
        >::type;
};


}} // namespace detail::difference
#endif // DOXYGEN_NO_DETAIL


namespace resolve_collection
{


template
<
    typename Geometry1, typename Geometry2, typename Collection,
    typename Tag1 = typename geometry::tag<Geometry1>::type,
    typename Tag2 = typename geometry::tag<Geometry2>::type,
    typename CollectionTag = typename geometry::tag<Collection>::type
>
struct difference
{
    template <typename Strategy>
    static void apply(Geometry1 const& geometry1,
                      Geometry2 const& geometry2,
                      Collection & output_collection,
                      Strategy const& strategy)
    {
        using single_out = typename geometry::detail::output_geometry_value
            <
                Collection
            >::type;

        detail::difference::difference_insert<single_out>(
            geometry1, geometry2,
            geometry::detail::output_geometry_back_inserter(output_collection),
            strategy);
    }
};

template <typename Geometry1, typename Geometry2, typename Collection>
struct difference
    <
        Geometry1, Geometry2, Collection,
        geometry_collection_tag, geometry_collection_tag, geometry_collection_tag
    >
{
    template <typename Strategy>
    static void apply(Geometry1 const& geometry1,
                      Geometry2 const& geometry2,
                      Collection& output_collection,
                      Strategy const& strategy)
    {
        auto const rtree2 = detail::gc_make_rtree_iterators(geometry2, strategy);
        detail::visit_breadth_first([&](auto const& g1)
        {
            // multi-point, multi-linestring or multi_polygon
            typename detail::difference::multi_output_type
                <
                    util::remove_cref_t<decltype(g1)>, Collection
                >::type out;

            g1_minus_gc2(g1, rtree2, out, strategy);

            detail::intersection::gc_move_multi_back(output_collection, out);

            return true;
        }, geometry1);
    }

private:
    // Implemented as separate function because msvc is unable to do nested lambda capture
    template <typename G1, typename Rtree2, typename MultiOut, typename Strategy>
    static void g1_minus_gc2(G1 const& g1, Rtree2 const& rtree2, MultiOut& out, Strategy const& strategy)
    {
        {
            using single_out_t = typename geometry::detail::output_geometry_value<MultiOut>::type;
            auto out_it = geometry::detail::output_geometry_back_inserter(out);
            geometry::detail::convert_to_output<G1, single_out_t>::apply(g1, out_it);
        }

        using box1_t = detail::gc_make_rtree_box_t<G1>;
        box1_t b1 = geometry::return_envelope<box1_t>(g1, strategy);
        detail::expand_by_epsilon(b1);

        for (auto qit = rtree2.qbegin(index::intersects(b1)); qit != rtree2.qend(); ++qit)
        {
            traits::iter_visit<Geometry2>::apply([&](auto const& g2)
            {
                multi_out_minus_g2(out, g2, strategy);
            }, qit->second);

            if (boost::empty(out))
            {
                return;
            }
        }
    }

    template
    <
        typename MultiOut, typename G2, typename Strategy,
        std::enable_if_t<detail::difference::is_subtractable_t<MultiOut, G2>::value, int> = 0
    >
    static void multi_out_minus_g2(MultiOut& out, G2 const& g2, Strategy const& strategy)
    {
        MultiOut result;
        difference<MultiOut, G2, MultiOut>::apply(out, g2, result, strategy);
        out = std::move(result);
    }

    template
    <
        typename MultiOut, typename G2, typename Strategy,
        std::enable_if_t<(! detail::difference::is_subtractable_t<MultiOut, G2>::value), int> = 0
    >
    static void multi_out_minus_g2(MultiOut& , G2 const& , Strategy const& )
    {}
};


template <typename Geometry1, typename Geometry2, typename Collection, typename Tag1>
struct difference
    <
        Geometry1, Geometry2, Collection,
        Tag1, geometry_collection_tag, geometry_collection_tag
    >
{
    template <typename Strategy>
    static void apply(Geometry1 const& geometry1,
                      Geometry2 const& geometry2,
                      Collection & output_collection,
                      Strategy const& strategy)
    {
        using gc_view_t = geometry::detail::geometry_collection_view<Geometry1>;
        difference
            <
                gc_view_t, Geometry2, Collection
            >::apply(gc_view_t(geometry1), geometry2, output_collection, strategy);
    }
};

template <typename Geometry1, typename Geometry2, typename Collection, typename Tag2>
struct difference
    <
        Geometry1, Geometry2, Collection,
        geometry_collection_tag, Tag2, geometry_collection_tag
    >
{
    template <typename Strategy>
    static void apply(Geometry1 const& geometry1,
                      Geometry2 const& geometry2,
                      Collection & output_collection,
                      Strategy const& strategy)
    {
        using gc_view_t = geometry::detail::geometry_collection_view<Geometry2>;
        difference
            <
                Geometry1, gc_view_t, Collection
            >::apply(geometry1, gc_view_t(geometry2), output_collection, strategy);
    }
};

template <typename Geometry1, typename Geometry2, typename Collection, typename Tag1, typename Tag2>
struct difference
    <
        Geometry1, Geometry2, Collection,
        Tag1, Tag2, geometry_collection_tag
    >
{
    template <typename Strategy>
    static void apply(Geometry1 const& geometry1,
                      Geometry2 const& geometry2,
                      Collection & output_collection,
                      Strategy const& strategy)
    {
        using gc1_view_t = geometry::detail::geometry_collection_view<Geometry1>;
        using gc2_view_t = geometry::detail::geometry_collection_view<Geometry2>;
        difference
            <
                gc1_view_t, gc2_view_t, Collection
            >::apply(gc1_view_t(geometry1), gc2_view_t(geometry2), output_collection, strategy);
    }
};


} // namespace resolve_collection


namespace resolve_strategy {

template
<
    typename Strategy,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategy>::value
>
struct difference
{
    template <typename Geometry1, typename Geometry2, typename Collection>
    static inline void apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Collection & output_collection,
                             Strategy const& strategy)
    {
        resolve_collection::difference
            <
                Geometry1, Geometry2, Collection
            >::apply(geometry1, geometry2, output_collection, strategy);
    }
};

template <typename Strategy>
struct difference<Strategy, false>
{
    template <typename Geometry1, typename Geometry2, typename Collection>
    static inline void apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Collection & output_collection,
                             Strategy const& strategy)
    {
        using strategies::relate::services::strategy_converter;

        difference
            <
                decltype(strategy_converter<Strategy>::get(strategy))
            >::apply(geometry1, geometry2, output_collection,
                     strategy_converter<Strategy>::get(strategy));
    }
};

template <>
struct difference<default_strategy, false>
{
    template <typename Geometry1, typename Geometry2, typename Collection>
    static inline void apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Collection & output_collection,
                             default_strategy)
    {
        typedef typename strategies::relate::services::default_strategy
            <
                Geometry1,
                Geometry2
            >::type strategy_type;

        difference
            <
                strategy_type
            >::apply(geometry1, geometry2, output_collection, strategy_type());
    }
};

} // resolve_strategy


namespace resolve_dynamic
{

template
<
    typename Geometry1, typename Geometry2,
    typename Tag1 = typename geometry::tag<Geometry1>::type,
    typename Tag2 = typename geometry::tag<Geometry2>::type
>
struct difference
{
    template <typename Collection, typename Strategy>
    static void apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                      Collection& output_collection, Strategy const& strategy)
    {
        resolve_strategy::difference
            <
                Strategy
            >::apply(geometry1, geometry2, output_collection, strategy);
    }
};

template <typename DynamicGeometry1, typename Geometry2, typename Tag2>
struct difference<DynamicGeometry1, Geometry2, dynamic_geometry_tag, Tag2>
{
    template <typename Collection, typename Strategy>
    static void apply(DynamicGeometry1 const& geometry1, Geometry2 const& geometry2,
                      Collection& output_collection, Strategy const& strategy)
    {
        traits::visit<DynamicGeometry1>::apply([&](auto const& g1)
        {
            resolve_strategy::difference
                <
                    Strategy
                >::apply(g1, geometry2, output_collection, strategy);
        }, geometry1);
    }
};

template <typename Geometry1, typename DynamicGeometry2, typename Tag1>
struct difference<Geometry1, DynamicGeometry2, Tag1, dynamic_geometry_tag>
{
    template <typename Collection, typename Strategy>
    static void apply(Geometry1 const& geometry1, DynamicGeometry2 const& geometry2,
                      Collection& output_collection, Strategy const& strategy)
    {
        traits::visit<DynamicGeometry2>::apply([&](auto const& g2)
        {
            resolve_strategy::difference
                <
                    Strategy
                >::apply(geometry1, g2, output_collection, strategy);
        }, geometry2);
    }
};

template <typename DynamicGeometry1, typename DynamicGeometry2>
struct difference<DynamicGeometry1, DynamicGeometry2, dynamic_geometry_tag, dynamic_geometry_tag>
{
    template <typename Collection, typename Strategy>
    static void apply(DynamicGeometry1 const& geometry1, DynamicGeometry2 const& geometry2,
                      Collection& output_collection, Strategy const& strategy)
    {
        traits::visit<DynamicGeometry1, DynamicGeometry2>::apply([&](auto const& g1, auto const& g2)
        {
            resolve_strategy::difference
                <
                    Strategy
                >::apply(g1, g2, output_collection, strategy);
        }, geometry1, geometry2);
    }
};

} // namespace resolve_dynamic


/*!
\brief_calc2{difference}
\ingroup difference
\details \details_calc2{difference, spatial set theoretic difference}.
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam Collection \tparam_output_collection
\tparam Strategy \tparam_strategy{Difference}
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param output_collection the output collection
\param strategy \param_strategy{difference}

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/difference.qbk]}
*/
template
<
    typename Geometry1,
    typename Geometry2,
    typename Collection,
    typename Strategy
>
inline void difference(Geometry1 const& geometry1,
                       Geometry2 const& geometry2,
                       Collection& output_collection,
                       Strategy const& strategy)
{
    resolve_dynamic::difference
        <
            Geometry1,
            Geometry2
        >::apply(geometry1, geometry2, output_collection, strategy);
}


/*!
\brief_calc2{difference}
\ingroup difference
\details \details_calc2{difference, spatial set theoretic difference}.
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam Collection \tparam_output_collection
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param output_collection the output collection

\qbk{[include reference/algorithms/difference.qbk]}
*/
template
<
    typename Geometry1,
    typename Geometry2,
    typename Collection
>
inline void difference(Geometry1 const& geometry1,
                       Geometry2 const& geometry2,
                       Collection& output_collection)
{
    resolve_dynamic::difference
        <
            Geometry1,
            Geometry2
        >::apply(geometry1, geometry2, output_collection, default_strategy());
}


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_DIFFERENCE_HPP
