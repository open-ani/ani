// Boost.Geometry

// Copyright (c) 2022, Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_INTERSECTION_GC_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_INTERSECTION_GC_HPP


#include <tuple>

#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/detail/gc_make_rtree.hpp>
#include <boost/geometry/algorithms/detail/intersection/interface.hpp>
#include <boost/geometry/views/detail/geometry_collection_view.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace intersection
{


template <typename GC, typename Multi>
struct gc_can_move_element
{
    template <typename G>
    using is_same_as_single = std::is_same<G, typename boost::range_value<Multi>::type>;
    using gc_types = typename traits::geometry_types<GC>::type;
    using found_type = typename util::sequence_find_if<gc_types, is_same_as_single>::type;
    static const bool value = ! std::is_void<found_type>::value;
};

template <typename GC, typename Multi>
struct gc_can_convert_element
{
    template <typename G>
    using has_same_tag_as_single = std::is_same
        <
            typename geometry::tag<G>::type,
            typename geometry::tag<typename boost::range_value<Multi>::type>::type
        >;
    using gc_types = typename traits::geometry_types<GC>::type;
    using found_type = typename util::sequence_find_if<gc_types, has_same_tag_as_single>::type;
    static const bool value = ! std::is_void<found_type>::value;
};

template
<
    typename GC, typename Multi,
    std::enable_if_t<gc_can_move_element<GC, Multi>::value, int> = 0
>
inline void gc_move_one_elem_multi_back(GC& gc, Multi&& multi)
{
    range::emplace_back(gc, std::move(*boost::begin(multi)));
}

template
<
    typename GC, typename Multi,
    std::enable_if_t<! gc_can_move_element<GC, Multi>::value && gc_can_convert_element<GC, Multi>::value, int> = 0
>
inline void gc_move_one_elem_multi_back(GC& gc, Multi&& multi)
{
    typename gc_can_convert_element<GC, Multi>::found_type single_out;
    geometry::convert(*boost::begin(multi), single_out);
    range::emplace_back(gc, std::move(single_out));
}

template
<
    typename GC, typename Multi,
    std::enable_if_t<! gc_can_move_element<GC, Multi>::value && ! gc_can_convert_element<GC, Multi>::value, int> = 0
>
inline void gc_move_one_elem_multi_back(GC& gc, Multi&& multi)
{
    range::emplace_back(gc, std::move(multi));
}

template <typename GC, typename Multi>
inline void gc_move_multi_back(GC& gc, Multi&& multi)
{
    if (! boost::empty(multi))
    {
        if (boost::size(multi) == 1)
        {
            gc_move_one_elem_multi_back(gc, std::move(multi));
        }
        else
        {
            range::emplace_back(gc, std::move(multi));
        }
    }
}


}} // namespace detail::intersection
#endif // DOXYGEN_NO_DETAIL


namespace resolve_collection
{


template
<
    typename Geometry1, typename Geometry2, typename GeometryOut
>
struct intersection
    <
        Geometry1, Geometry2, GeometryOut,
        geometry_collection_tag, geometry_collection_tag, geometry_collection_tag
    >
{
    // NOTE: for now require all of the possible output types
    //       technically only a subset could be needed.
    using multi_point_t = typename util::sequence_find_if
        <
            typename traits::geometry_types<GeometryOut>::type,
            util::is_multi_point
        >::type;
    using multi_linestring_t = typename util::sequence_find_if
        <
            typename traits::geometry_types<GeometryOut>::type,
            util::is_multi_linestring
        >::type;
    using multi_polygon_t = typename util::sequence_find_if
        <
            typename traits::geometry_types<GeometryOut>::type,
            util::is_multi_polygon
        >::type;
    using tuple_out_t = boost::tuple<multi_point_t, multi_linestring_t, multi_polygon_t>;

    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             GeometryOut& geometry_out,
                             Strategy const& strategy)
    {
        bool result = false;
        tuple_out_t out;

        auto const rtree2 = detail::gc_make_rtree_iterators(geometry2, strategy);
        detail::visit_breadth_first([&](auto const& g1)
        {
            bool r = g1_prod_gc2(g1, rtree2, out, strategy);
            result = result || r;
            return true;
        }, geometry1);

        detail::intersection::gc_move_multi_back(geometry_out, boost::get<0>(out));
        detail::intersection::gc_move_multi_back(geometry_out, boost::get<1>(out));
        detail::intersection::gc_move_multi_back(geometry_out, boost::get<2>(out));

        return result;
    }

private:
    // Implemented as separate function because msvc is unable to do nested lambda capture
    template <typename G1, typename Rtree2, typename TupleOut, typename Strategy>
    static bool g1_prod_gc2(G1 const& g1, Rtree2 const& rtree2, TupleOut& out, Strategy const& strategy)
    {
        bool result = false;

        using box1_t = detail::gc_make_rtree_box_t<G1>;
        box1_t b1 = geometry::return_envelope<box1_t>(g1, strategy);
        detail::expand_by_epsilon(b1);

        for (auto qit = rtree2.qbegin(index::intersects(b1)); qit != rtree2.qend(); ++qit)
        {
            traits::iter_visit<Geometry2>::apply([&](auto const& g2)
            {
                TupleOut inters_result;
                using g2_t = util::remove_cref_t<decltype(g2)>;
                intersection<G1, g2_t, TupleOut>::apply(g1, g2, inters_result, strategy);

                // TODO: If possible merge based on adjacency lists, i.e. merge
                //       only the intersections of elements that intersect each other
                //       as subgroups. So the result could contain merged intersections
                //       of several groups, not only one.
                // TODO: It'd probably be better to gather all of the parts first
                //       and then merge them with merge_elements.
                // NOTE: template explicitly called because gcc-6 doesn't compile it
                //       otherwise.
                bool const r0 = intersection::template merge_result<0>(inters_result, out, strategy);
                bool const r1 = intersection::template merge_result<1>(inters_result, out, strategy);
                bool const r2 = intersection::template merge_result<2>(inters_result, out, strategy);
                result = result || r0 || r1 || r2;
            }, qit->second);
        }

        return result;
    }

    template <std::size_t Index, typename Out, typename Strategy>
    static bool merge_result(Out const& inters_result, Out& out, Strategy const& strategy)
    {
        auto const& multi_result = boost::get<Index>(inters_result);
        auto& multi_out = boost::get<Index>(out);
        if (! boost::empty(multi_result))
        {
            std::remove_reference_t<decltype(multi_out)> temp_result;
            merge_two(multi_out, multi_result, temp_result, strategy);
            multi_out = std::move(temp_result);
            return true;
        }
        return false;
    }

    template <typename Out, typename Strategy, std::enable_if_t<! util::is_pointlike<Out>::value, int> = 0>
    static void merge_two(Out const& g1, Out const& g2, Out& out, Strategy const& strategy)
    {
        using rescale_policy_type = typename geometry::rescale_overlay_policy_type
            <
                Out, Out, typename Strategy::cs_tag
            >::type;

        rescale_policy_type robust_policy
            = geometry::get_rescale_policy<rescale_policy_type>(
                    g1, g2, strategy);

        geometry::dispatch::intersection_insert
            <
                Out, Out, typename boost::range_value<Out>::type,
                overlay_union
            >::apply(g1,
                     g2,
                     robust_policy,
                     geometry::range::back_inserter(out),
                     strategy);
    }

    template <typename Out, typename Strategy, std::enable_if_t<util::is_pointlike<Out>::value, int> = 0>
    static void merge_two(Out const& g1, Out const& g2, Out& out, Strategy const& strategy)
    {
        detail::overlay::union_pointlike_pointlike_point
            <
                Out, Out, typename boost::range_value<Out>::type
            >::apply(g1,
                     g2,
                     0, // dummy robust policy
                     geometry::range::back_inserter(out),
                     strategy);
    }
};

template
<
    typename Geometry1, typename Geometry2, typename GeometryOut, typename Tag1
>
struct intersection
    <
        Geometry1, Geometry2, GeometryOut,
        Tag1, geometry_collection_tag, geometry_collection_tag
    >
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             GeometryOut& geometry_out,
                             Strategy const& strategy)
    {
        using gc_view_t = geometry::detail::geometry_collection_view<Geometry1>;
        return intersection
            <
                gc_view_t, Geometry2, GeometryOut
            >::apply(gc_view_t(geometry1), geometry2, geometry_out, strategy);
    }
};

template
<
    typename Geometry1, typename Geometry2, typename GeometryOut, typename Tag2
>
struct intersection
    <
        Geometry1, Geometry2, GeometryOut,
        geometry_collection_tag, Tag2, geometry_collection_tag
    >
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             GeometryOut& geometry_out,
                             Strategy const& strategy)
    {
        using gc_view_t = geometry::detail::geometry_collection_view<Geometry2>;
        return intersection
            <
                Geometry1, gc_view_t, GeometryOut
            >::apply(geometry1, gc_view_t(geometry2), geometry_out, strategy);
    }
};

template
<
    typename Geometry1, typename Geometry2, typename GeometryOut, typename Tag1, typename Tag2
>
struct intersection
    <
        Geometry1, Geometry2, GeometryOut,
        Tag1, Tag2, geometry_collection_tag
    >
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             GeometryOut& geometry_out,
                             Strategy const& strategy)
    {
        using gc1_view_t = geometry::detail::geometry_collection_view<Geometry1>;
        using gc2_view_t = geometry::detail::geometry_collection_view<Geometry2>;
        return intersection
            <
                gc1_view_t, gc2_view_t, GeometryOut
            >::apply(gc1_view_t(geometry1), gc2_view_t(geometry2), geometry_out, strategy);
    }
};


} // namespace resolve_collection

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_INTERSECTION_GC_HPP
