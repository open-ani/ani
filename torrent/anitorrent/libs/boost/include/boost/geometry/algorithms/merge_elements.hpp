// Boost.Geometry

// Copyright (c) 2022-2023, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_MERGE_ELEMENTS_HPP
#define BOOST_GEOMETRY_ALGORITHMS_MERGE_ELEMENTS_HPP


#include <vector>

#include <boost/geometry/algorithms/detail/point_on_border.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/algorithms/difference.hpp>
#include <boost/geometry/algorithms/union.hpp>
#include <boost/geometry/core/coordinate_system.hpp>
#include <boost/geometry/core/coordinate_type.hpp>
#include <boost/geometry/core/point_type.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>
#include <boost/geometry/geometries/point.hpp>
#include <boost/geometry/policies/compare.hpp>
#include <boost/geometry/util/range.hpp>
#include <boost/geometry/strategies/relate/cartesian.hpp>
#include <boost/geometry/strategies/relate/geographic.hpp>
#include <boost/geometry/strategies/relate/spherical.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace merge_elements
{


template <typename T>
using is_pla = util::bool_constant<util::is_pointlike<T>::value || util::is_linear<T>::value || util::is_areal<T>::value>;
template <typename T, typename ...Ts>
struct are_areal : util::bool_constant<util::is_areal<T>::value && are_areal<Ts...>::value> {};
template <typename T>
struct are_areal<T> : util::is_areal<T> {};
template <typename T, typename ...Ts>
struct are_linear : util::bool_constant<util::is_linear<T>::value && are_linear<Ts...>::value> {};
template <typename T>
struct are_linear<T> : util::is_linear<T> {};
template <typename T, typename ...Ts>
struct are_pointlike : util::bool_constant<util::is_pointlike<T>::value && are_pointlike<Ts...>::value> {};
template <typename T>
struct are_pointlike<T> : util::is_pointlike<T> {};
template <typename ...Ts>
using are_same_kind = util::bool_constant<are_areal<Ts...>::value || are_linear<Ts...>::value || are_pointlike<Ts...>::value>;


template
<
    typename Geometry, typename It, typename PointLike, typename Linear, typename Areal,
    std::enable_if_t<util::is_areal<Geometry>::value, int> = 0
>
inline void distribute_element(Geometry const& geometry, It it, PointLike& , Linear&, Areal& areal)
{
    typename geometry::point_type<Geometry>::type point;
    if (geometry::point_on_border(point, geometry))
    {
        using point_t = typename Areal::value_type::first_type;
        areal.emplace_back(point_t(geometry::get<0>(point), geometry::get<1>(point)), it);
    }
}

template
<
    typename Geometry, typename It, typename PointLike, typename Linear, typename Areal,
    std::enable_if_t<util::is_linear<Geometry>::value, int> = 0
>
inline void distribute_element(Geometry const& geometry, It it, PointLike& , Linear& linear, Areal& )
{
    typename geometry::point_type<Geometry>::type point;
    if (geometry::point_on_border(point, geometry))
    {
        using point_t = typename Linear::value_type::first_type;
        linear.emplace_back(point_t(geometry::get<0>(point), geometry::get<1>(point)), it);
    }
}

template
<
    typename Geometry, typename It, typename PointLike, typename Linear, typename Areal,
    std::enable_if_t<util::is_pointlike<Geometry>::value, int> = 0
>
inline void distribute_element(Geometry const& geometry, It it, PointLike& pointlike, Linear& , Areal& )
{
    typename geometry::point_type<Geometry>::type point;
    if (geometry::point_on_border(point, geometry))
    {
        using point_t = typename Linear::value_type::first_type;
        pointlike.emplace_back(point_t(geometry::get<0>(point), geometry::get<1>(point)), it);
    }
}

template
<
    typename Geometry, typename It, typename PointLike, typename Linear, typename Areal,
    std::enable_if_t<! is_pla<Geometry>::value, int> = 0
>
inline void distribute_element(Geometry const& , It const&, PointLike const& , Linear const&, Areal const&)
{}


template
<
    typename Geometry, typename MultiGeometry,
    std::enable_if_t<are_same_kind<Geometry, MultiGeometry>::value, int> = 0
>
inline void convert(Geometry const& geometry, MultiGeometry& result)
{
    geometry::convert(geometry, result);
}

template
<
    typename Geometry, typename MultiGeometry,
    std::enable_if_t<! are_same_kind<Geometry, MultiGeometry>::value, int> = 0
>
inline void convert(Geometry const& , MultiGeometry const& )
{}


template
<
    typename Geometry1, typename Geometry2, typename MultiGeometry, typename Strategy,
    std::enable_if_t<are_same_kind<Geometry1, Geometry2, MultiGeometry>::value, int> = 0
>
inline void union_(Geometry1 const& geometry1, Geometry2 const& geometry2, MultiGeometry& result, Strategy const& strategy)
{
    geometry::union_(geometry1, geometry2, result, strategy);
}

template
<
    typename Geometry1, typename Geometry2, typename MultiGeometry, typename Strategy,
    std::enable_if_t<! are_same_kind<Geometry1, Geometry2, MultiGeometry>::value, int> = 0
>
inline void union_(Geometry1 const& , Geometry2 const& , MultiGeometry const& , Strategy const&)
{}


template <typename It>
struct merge_data
{
    merge_data(It first_, It last_)
        : first(first_), last(last_)
    {}
    It first, last;
    bool merge_results = false;
};

template <typename GeometryCollection, typename RandomIt, typename MultiGeometry, typename Strategy>
inline void merge(RandomIt const first, RandomIt const last, MultiGeometry& out, Strategy const& strategy)
{
    auto const size = last - first;
    if (size <= 0)
    {
        return;
    }

    auto const less = [](auto const& l, auto const& r)
    {
        return geometry::less<void, -1, Strategy>()(l.first, r.first);
    };

    std::vector<merge_data<RandomIt>> stack_in;
    std::vector<MultiGeometry> stack_out;
    stack_in.reserve(size / 2 + 1);
    stack_out.reserve(size / 2 + 1);

    stack_in.emplace_back(first, last);

    while (! stack_in.empty())
    {
        auto & b = stack_in.back();
        if (! b.merge_results)
        {
            auto const s = b.last - b.first;
            if (s > 2)
            {
                RandomIt const mid = b.first + s / 2;
                std::nth_element(b.first, mid, b.last, less);
                RandomIt const fir = b.first;
                RandomIt const las = b.last;
                b.merge_results = true;
                stack_in.emplace_back(fir, mid);
                stack_in.emplace_back(mid, las);
            }
            else if (s == 2)
            {
                MultiGeometry result;
                // VERSION 1
//                traits::iter_visit<GeometryCollection>::apply([&](auto const& g1)
//                {
//                    traits::iter_visit<GeometryCollection>::apply([&](auto const& g2)
//                    {
//                        merge_elements::union_(g1, g2, result, strategy);
//                    }, (b.first + 1)->second);
//                }, b.first->second);
                // VERSION 2
                // calling iter_visit non-recursively seems to decrease compilation time
                // greately with GCC
                MultiGeometry temp1, temp2;
                traits::iter_visit<GeometryCollection>::apply([&](auto const& g1)
                {
                    merge_elements::convert(g1, temp1);
                }, b.first->second);
                traits::iter_visit<GeometryCollection>::apply([&](auto const& g2)
                {
                    merge_elements::convert(g2, temp2);
                }, (b.first + 1)->second);
                geometry::union_(temp1, temp2, result, strategy);

                stack_out.push_back(std::move(result));
                stack_in.pop_back();
            }
            else if (s == 1)
            {
                MultiGeometry result;
                traits::iter_visit<GeometryCollection>::apply([&](auto const& g)
                {
                    merge_elements::convert(g, result);
                }, b.first->second);
                stack_out.push_back(std::move(result));
                stack_in.pop_back();
            }
        }
        else if (b.merge_results)
        {
            MultiGeometry m2 = std::move(stack_out.back());
            stack_out.pop_back();
            MultiGeometry m1 = std::move(stack_out.back());
            stack_out.pop_back();
            MultiGeometry result;
            geometry::union_(m1, m2, result, strategy);
            stack_out.push_back(std::move(result));
            stack_in.pop_back();
        }
    }

    out = std::move(stack_out.back());
}

template <typename MultiGeometry, typename Geometry, typename Strategy>
inline void subtract(MultiGeometry & multi, Geometry const& geometry, Strategy const& strategy)
{
    MultiGeometry temp;
    geometry::difference(multi, geometry, temp, strategy);
    multi = std::move(temp);
}

struct merge_gc
{
    template <typename GeometryCollection, typename Strategy>
    static void apply(GeometryCollection const& geometry_collection,
                      GeometryCollection & out,
                      Strategy const& strategy)
    {
        using original_point_t = typename geometry::point_type<GeometryCollection>::type;
        using iterator_t = typename boost::range_iterator<GeometryCollection const>::type;
        using coordinate_t = typename geometry::coordinate_type<original_point_t>::type;
        using cs_t = typename geometry::coordinate_system<original_point_t>::type;
        using point_t = model::point<coordinate_t, 2, cs_t>;

        using multi_point_t = typename util::sequence_find_if
            <
                typename traits::geometry_types<std::remove_const_t<GeometryCollection>>::type,
                util::is_multi_point
            >::type;
        using multi_linestring_t = typename util::sequence_find_if
            <
                typename traits::geometry_types<std::remove_const_t<GeometryCollection>>::type,
                util::is_multi_linestring
            >::type;
        using multi_polygon_t = typename util::sequence_find_if
            <
                typename traits::geometry_types<std::remove_const_t<GeometryCollection>>::type,
                util::is_multi_polygon
            >::type;

        // NOTE: Right now GC containing all of the above is required but technically
        //       we could allow only some combinations and the algorithm below could
        //       normalize GC accordingly.

        multi_point_t multi_point;
        multi_linestring_t multi_linestring;
        multi_polygon_t multi_polygon;

        std::vector<std::pair<point_t, iterator_t>> pointlike;
        std::vector<std::pair<point_t, iterator_t>> linear;
        std::vector<std::pair<point_t, iterator_t>> areal;

        detail::visit_breadth_first_impl<true>::apply([&](auto const& g, auto it)
        {
            merge_elements::distribute_element(g, it, pointlike, linear, areal);
            return true;
        }, geometry_collection);

        // TODO: make this optional?
        // TODO: merge linear at the end? (difference can break linear rings, their parts would be joined)
        merge<GeometryCollection>(pointlike.begin(), pointlike.end(), multi_point, strategy);
        merge<GeometryCollection>(linear.begin(), linear.end(), multi_linestring, strategy);
        merge<GeometryCollection>(areal.begin(), areal.end(), multi_polygon, strategy);

        // L \ A
        subtract(multi_linestring, multi_polygon, strategy);
        // P \ A
        subtract(multi_point, multi_polygon, strategy);
        // P \ L
        subtract(multi_point, multi_linestring, strategy);

        if (! geometry::is_empty(multi_point))
        {
            range::emplace_back(out, std::move(multi_point));
        }
        if (! geometry::is_empty(multi_linestring))
        {
            range::emplace_back(out, std::move(multi_linestring));
        }
        if (! geometry::is_empty(multi_polygon))
        {
            range::emplace_back(out, std::move(multi_polygon));
        }
    }
};


}} // namespace detail::merge_elements
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct merge_elements
    : not_implemented<Geometry, Tag>
{};

template <typename GeometryCollection>
struct merge_elements<GeometryCollection, geometry_collection_tag>
    : geometry::detail::merge_elements::merge_gc
{};


} // namespace dispatch
#endif


namespace resolve_strategy
{

template <typename Strategy>
struct merge_elements
{
    template <typename Geometry>
    static void apply(Geometry const& geometry, Geometry & out, Strategy const& strategy)
    {
        dispatch::merge_elements
            <
                Geometry
            >::apply(geometry, out, strategy);
    }
};

template <>
struct merge_elements<default_strategy>
{
    template <typename Geometry>
    static void apply(Geometry const& geometry, Geometry & out, default_strategy)
    {
        using strategy_type = typename strategies::relate::services::default_strategy
            <
                Geometry, Geometry
            >::type;

        dispatch::merge_elements
            <
                Geometry
            >::apply(geometry, out, strategy_type());
    }
};

} // namespace resolve_strategy


template <typename Geometry, typename Strategy>
inline void merge_elements(Geometry const& geometry, Geometry & out, Strategy const& strategy)
{
    resolve_strategy::merge_elements
        <
            Strategy
        >::apply(geometry, out, strategy);
}


template <typename Geometry>
inline void merge_elements(Geometry const& geometry, Geometry & out)
{
    resolve_strategy::merge_elements
        <
            default_strategy
        >::apply(geometry, out, default_strategy());
}


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_MERGE_ELEMENTS_HPP
