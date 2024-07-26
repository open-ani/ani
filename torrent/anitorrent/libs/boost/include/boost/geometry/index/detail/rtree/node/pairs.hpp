// Boost.Geometry Index
//
// Pairs intended to be used internally in nodes.
//
// Copyright (c) 2011-2023 Adam Wulkiewicz, Lodz, Poland.
//
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_INDEX_DETAIL_RTREE_NODE_PAIRS_HPP
#define BOOST_GEOMETRY_INDEX_DETAIL_RTREE_NODE_PAIRS_HPP

namespace boost { namespace geometry { namespace index {

namespace detail { namespace rtree {

template <typename First, typename Pointer>
class ptr_pair
{
public:
    typedef First first_type;
    typedef Pointer second_type;
    ptr_pair(First const& f, Pointer s) : first(f), second(s) {}

    first_type first;
    second_type second;
};

template <typename First, typename Pointer> inline
ptr_pair<First, Pointer> make_ptr_pair(First const& f, Pointer s)
{
    return ptr_pair<First, Pointer>(f, s);
}

}} // namespace detail::rtree

}}} // namespace boost::geometry::index

#endif // BOOST_GEOMETRY_INDEX_DETAIL_RTREE_NODE_PAIRS_HPP
