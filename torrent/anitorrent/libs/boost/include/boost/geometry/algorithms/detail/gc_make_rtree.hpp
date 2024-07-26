// Boost.Geometry

// Copyright (c) 2022, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_MAKE_RTREE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_MAKE_RTREE_HPP

#include <vector>

#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>
#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/detail/expand_by_epsilon.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/algorithms/envelope.hpp>
#include <boost/geometry/index/rtree.hpp>
#include <boost/geometry/strategies/index/services.hpp>
#include <boost/geometry/views/detail/random_access_view.hpp>

namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail
{


template <typename GC>
using gc_make_rtree_box_t = geometry::model::box
    <
        geometry::model::point
            <
                typename geometry::coordinate_type<GC>::type,
                geometry::dimension<GC>::value,
                typename geometry::coordinate_system<GC>::type
            >
    >;


template <typename GC, typename Strategy>
inline auto gc_make_rtree_iterators(GC& gc, Strategy const& strategy)
{
    using box_t = gc_make_rtree_box_t<GC>;
    using iter_t = typename boost::range_iterator<GC>::type;

    using rtree_param_t = index::rstar<4>;
    using rtree_parameters_t = index::parameters<rtree_param_t, Strategy>;
    using value_t = std::pair<box_t, iter_t>;
    using rtree_t = index::rtree<value_t, rtree_parameters_t>;

    // TODO: get rid of the temporary vector
    auto const size = boost::size(gc);
    std::vector<value_t> values;
    values.reserve(size);

    visit_breadth_first_impl<true>::apply([&](auto const& g, auto iter)
    {
        box_t b = geometry::return_envelope<box_t>(g, strategy);
        detail::expand_by_epsilon(b);
        values.emplace_back(b, iter);
        return true;
    }, gc);

    return rtree_t(values.begin(), values.end(), rtree_parameters_t(rtree_param_t(), strategy));
}


template <typename GCView, typename Strategy>
inline auto gc_make_rtree_indexes(GCView const& gc, Strategy const& strategy)
{
    // Alternatively only take random_access_view<GC>
    static const bool is_random_access = is_random_access_range<GCView>::value;
    static const bool is_not_recursive = ! is_geometry_collection_recursive<GCView>::value;
    BOOST_GEOMETRY_STATIC_ASSERT((is_random_access && is_not_recursive),
                                 "This algorithm requires random-access, non-recursive geometry collection or view.",
                                 GCView);

    using box_t = gc_make_rtree_box_t<GCView>;

    using rtree_param_t = index::rstar<4>;
    using rtree_parameters_t = index::parameters<rtree_param_t, Strategy>;
    using value_t = std::pair<box_t, std::size_t>;
    using rtree_t = index::rtree<value_t, rtree_parameters_t>;

    // TODO: get rid of the temporary vector
    std::size_t const size = boost::size(gc);
    std::vector<value_t> values;
    values.reserve(size);

    auto const begin = boost::begin(gc);
    for (std::size_t i = 0; i < size; ++i)
    {
        traits::iter_visit<GCView>::apply([&](auto const& g)
        {
            box_t b = geometry::return_envelope<box_t>(g, strategy);
            detail::expand_by_epsilon(b);
            values.emplace_back(b, i);
        }, begin + i);
    }

    return rtree_t(values.begin(), values.end(), rtree_parameters_t(rtree_param_t(), strategy));
}


} // namespace detail
#endif // DOXYGEN_NO_DETAIL


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_MAKE_RTREE_HPP
