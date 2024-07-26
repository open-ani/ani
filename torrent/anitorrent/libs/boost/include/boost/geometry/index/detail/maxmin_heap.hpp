// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_INDEX_DETAIL_MAXMIN_HEAP_HPP
#define BOOST_GEOMETRY_INDEX_DETAIL_MAXMIN_HEAP_HPP

#include <boost/geometry/index/detail/minmax_heap.hpp>

namespace boost { namespace geometry { namespace index { namespace detail
{

template <typename It, typename Compare>
inline void push_maxmin_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    minmax_heap_detail::push_heap<max_call, min_call>(first, last, comp);
}

template <typename It>
inline void push_maxmin_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    minmax_heap_detail::push_heap<max_call, min_call>(first, last, std::less<>());
}

template <typename It, typename Compare>
inline void pop_top_maxmin_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    pop_heap<max_call, min_call>(first, first, last, comp);
}

template <typename It>
inline void pop_top_maxmin_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    pop_heap<max_call, min_call>(first, first, last, std::less<>());
}

template <typename It, typename Compare>
inline void pop_bottom_maxmin_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    It bottom = minmax_heap_detail::bottom_heap<max_call>(first, last, comp);
    pop_heap<max_call, min_call>(first, bottom, last, comp);
}

template <typename It>
inline void pop_bottom_maxmin_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    auto&& comp = std::less<>();
    It bottom = minmax_heap_detail::bottom_heap<max_call>(first, last, comp);
    pop_heap<max_call, min_call>(first, bottom, last, comp);
}

template <typename It, typename Compare>
inline void make_maxmin_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    return minmax_heap_detail::make_heap<max_call, min_call>(first, last, comp);
}

template <typename It>
inline void make_maxmin_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    return minmax_heap_detail::make_heap<max_call, min_call>(first, last, std::less<>());
}

template <typename It, typename Compare>
inline bool is_maxmin_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    return minmax_heap_detail::is_heap<max_call>(first, last, comp);
}

template <typename It>
inline bool is_maxmin_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    return minmax_heap_detail::is_heap<max_call>(first, last, std::less<>());
}

template <typename It, typename Compare>
inline decltype(auto) bottom_maxmin_heap(It first, It last, Compare comp)
{
    using namespace minmax_heap_detail;
    return *minmax_heap_detail::bottom_heap<max_call>(first, last, comp);
}

template <typename It>
inline decltype(auto) bottom_maxmin_heap(It first, It last)
{
    using namespace minmax_heap_detail;
    return *minmax_heap_detail::bottom_heap<max_call>(first, last, std::less<>());
}


}}}} // namespace boost::geometry::index::detail

#endif // BOOST_GEOMETRY_INDEX_DETAIL_MAXMIN_HEAP_HPP
