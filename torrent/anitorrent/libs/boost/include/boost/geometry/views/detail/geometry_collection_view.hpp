// Boost.Geometry

// Copyright (c) 2022, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_VIEWS_GEOMETRY_COLLECTION_VIEW_HPP
#define BOOST_GEOMETRY_VIEWS_GEOMETRY_COLLECTION_VIEW_HPP

#include <boost/core/addressof.hpp>

#include <boost/geometry/core/geometry_types.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>
#include <boost/geometry/util/sequence.hpp>

namespace boost { namespace geometry
{

namespace detail
{


template <typename Geometry>
class geometry_collection_view
{
public:
    using iterator = Geometry const*;
    using const_iterator = Geometry const*;

    explicit geometry_collection_view(Geometry const& geometry)
        : m_geometry(geometry)
    {}

    const_iterator begin() const { return boost::addressof(m_geometry); }
    const_iterator end() const { return boost::addressof(m_geometry) + 1; }

private:
    Geometry const& m_geometry;
};

} // namespace detail


#ifndef DOXYGEN_NO_TRAITS_SPECIALIZATIONS
namespace traits
{

template <typename Geometry>
struct tag<geometry::detail::geometry_collection_view<Geometry>>
{
    using type = geometry_collection_tag;
};


template <typename Geometry>
struct geometry_types<geometry::detail::geometry_collection_view<Geometry>>
{
    using type = util::type_sequence<Geometry>;
};


template <typename Geometry>
struct iter_visit<geometry::detail::geometry_collection_view<Geometry>>
{
    template <typename Function, typename Iterator>
    static void apply(Function && function, Iterator iterator)
    {
        function(*iterator);
    }
};


} // namespace traits
#endif // DOXYGEN_NO_TRAITS_SPECIALIZATIONS


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_VIEWS_GEOMETRY_COLLECTION_VIEW_HPP
