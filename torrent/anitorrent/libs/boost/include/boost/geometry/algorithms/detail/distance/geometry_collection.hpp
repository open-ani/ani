// Boost.Geometry

// Copyright (c) 2021 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_DISTANCE_GEOMETRY_COLLECTION_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_DISTANCE_GEOMETRY_COLLECTION_HPP


#include <vector>

#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/dispatch/distance.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/core/assert.hpp>
#include <boost/geometry/core/point_type.hpp>
#include <boost/geometry/index/rtree.hpp>
#include <boost/geometry/strategies/distance.hpp>
#include <boost/geometry/strategies/distance_result.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace distance
{


template <typename Geometry, typename GeometryCollection, typename Strategies>
inline auto geometry_to_collection(Geometry const& geometry,
                                   GeometryCollection const& collection,
                                   Strategies const& strategies)
{
    using result_t = typename geometry::distance_result<Geometry, GeometryCollection, Strategies>::type;
    result_t result = 0;
    bool is_first = true;
    detail::visit_breadth_first([&](auto const& g)
    {
        result_t r = dispatch::distance
                        <
                            Geometry, util::remove_cref_t<decltype(g)>, Strategies
                        >::apply(geometry, g, strategies);
        if (is_first)
        {
            result = r;
            is_first = false;
        }
        else if (r < result)
        {
            result = r;
        }
        return result > result_t(0);
    }, collection);

    return result;
}

template <typename GeometryCollection1, typename GeometryCollection2, typename Strategies>
inline auto collection_to_collection(GeometryCollection1 const& collection1,
                                     GeometryCollection2 const& collection2,
                                     Strategies const& strategies)
{
    using result_t = typename geometry::distance_result<GeometryCollection1, GeometryCollection2, Strategies>::type;

    using point1_t = typename geometry::point_type<GeometryCollection1>::type;
    using box1_t = model::box<point1_t>;
    using point2_t = typename geometry::point_type<GeometryCollection2>::type;
    using box2_t = model::box<point2_t>;

    using rtree_value_t = std::pair<box1_t, typename boost::range_iterator<GeometryCollection1 const>::type>;
    using rtree_params_t = index::parameters<index::rstar<4>, Strategies>;
    using rtree_t = index::rtree<rtree_value_t, rtree_params_t>;

    rtree_params_t rtree_params(index::rstar<4>(), strategies);
    rtree_t rtree(rtree_params);

    // Build rtree of boxes and iterators of elements of GC1
    // TODO: replace this with visit_breadth_first_iterator to avoid creating an unnecessary container?
    {
        std::vector<rtree_value_t> values;
        visit_breadth_first_impl<true>::apply([&](auto & g1, auto it)
        {
            box1_t b1 = geometry::return_envelope<box1_t>(g1, strategies);
            geometry::detail::expand_by_epsilon(b1);
            values.emplace_back(b1, it);
            return true;
        }, collection1);
        rtree_t rt(values.begin(), values.end(), rtree_params);
        rtree = std::move(rt);
    }

    result_t const zero = 0;
    auto const rtree_qend = rtree.qend();

    result_t result = 0;
    bool is_first = true;
    visit_breadth_first([&](auto const& g2)
    {
        box2_t b2 = geometry::return_envelope<box2_t>(g2, strategies);
        geometry::detail::expand_by_epsilon(b2);

        for (auto it = rtree.qbegin(index::nearest(b2, rtree.size())) ; it != rtree_qend ; ++it)
        {
            // If the distance between boxes is greater than or equal to previously found
            // distance between geometries then stop processing the current b2 because no
            // closer b1 will be found
            if (! is_first)
            {
                result_t const bd = dispatch::distance
                    <
                        box1_t, box2_t, Strategies
                    >::apply(it->first, b2, strategies);
                if (bd >= result)
                {
                    break;
                }
            }

            // Boxes are closer than the previously found distance (or it's the first time),
            // calculate the new distance between geometries and check if it's closer (or assign it).
            traits::iter_visit<GeometryCollection1>::apply([&](auto const& g1)
            {
                result_t const d = dispatch::distance
                    <
                        util::remove_cref_t<decltype(g1)>, util::remove_cref_t<decltype(g2)>,
                        Strategies
                    >::apply(g1, g2, strategies);
                if (is_first)
                {
                    result = d;
                    is_first = false;
                }
                else if (d < result)
                {
                    result = d;
                }
            }, it->second);

            // The smallest possible distance found, end searching.
            if (! is_first && result <= zero)
            {
                return false;
            }
        }

        // Just in case
        return is_first || result > zero;
    }, collection2);

    return result;
}


}} // namespace detail::distance
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template
<
    typename Geometry, typename GeometryCollection, typename Strategies, typename Tag1
>
struct distance
    <
        Geometry, GeometryCollection, Strategies,
        Tag1, geometry_collection_tag, void, false
    >
{
    static inline auto apply(Geometry const& geometry,
                             GeometryCollection const& collection,
                             Strategies const& strategies)
    {
        assert_dimension_equal<Geometry, GeometryCollection>();

        return detail::distance::geometry_to_collection(geometry, collection, strategies);
    }
};

template
<
    typename GeometryCollection, typename Geometry, typename Strategies, typename Tag2
>
struct distance
    <
        GeometryCollection, Geometry, Strategies,
        geometry_collection_tag, Tag2, void, false
    >
{
    static inline auto apply(GeometryCollection const& collection,
                             Geometry const& geometry,
                             Strategies const& strategies)
    {
        assert_dimension_equal<Geometry, GeometryCollection>();

        return detail::distance::geometry_to_collection(geometry, collection, strategies);
    }
};

template
<
    typename GeometryCollection1, typename GeometryCollection2, typename Strategies
>
struct distance
    <
        GeometryCollection1, GeometryCollection2, Strategies,
        geometry_collection_tag, geometry_collection_tag, void, false
    >
{
    static inline auto apply(GeometryCollection1 const& collection1,
                             GeometryCollection2 const& collection2,
                             Strategies const& strategies)
    {
        assert_dimension_equal<GeometryCollection1, GeometryCollection2>();

        // Build the rtree for the smaller GC (ignoring recursive GCs)
        return boost::size(collection1) <= boost::size(collection2)
             ? detail::distance::collection_to_collection(collection1, collection2, strategies)
             : detail::distance::collection_to_collection(collection2, collection1, strategies);
    }
};

} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_DISTANCE_SEGMENT_TO_BOX_HPP
