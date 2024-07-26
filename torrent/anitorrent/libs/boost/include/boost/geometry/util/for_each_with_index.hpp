// Boost.Geometry

// Copyright (c) 2023 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_UTIL_FOR_EACH_WITH_INDEX_HPP
#define BOOST_GEOMETRY_UTIL_FOR_EACH_WITH_INDEX_HPP

#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>
#include <boost/range/size_type.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail
{

// Utility function to implement a Kotlin like range based for loop
template <typename Container, typename Function>
inline void for_each_with_index(Container const& container, Function func)
{
    typename boost::range_size<Container>::type index = 0;
    for (auto it = boost::begin(container); it != boost::end(container); ++it, ++index)
    {
        func(index, *it);
    }
}

template <typename Container, typename Function>
inline void for_each_with_index(Container& container, Function func)
{
    typename boost::range_size<Container>::type index = 0;
    for (auto it = boost::begin(container); it != boost::end(container); ++it, ++index)
    {
        func(index, *it);
    }
}

} // namespace detail
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_UTIL_FOR_EACH_WITH_INDEX_HPP
