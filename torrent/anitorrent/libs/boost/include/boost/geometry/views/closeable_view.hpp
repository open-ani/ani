// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2020-2023.
// Modifications copyright (c) 2020-2023 Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_VIEWS_CLOSEABLE_VIEW_HPP
#define BOOST_GEOMETRY_VIEWS_CLOSEABLE_VIEW_HPP

#include <boost/geometry/core/closure.hpp>
#include <boost/geometry/core/point_order.hpp>
#include <boost/geometry/core/ring_type.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/iterators/closing_iterator.hpp>

#include <boost/geometry/views/identity_view.hpp>

namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail
{

template <typename Range>
struct closing_view
{
    using iterator = closing_iterator<Range const>;
    using const_iterator = closing_iterator<Range const>;

    // Keep this explicit, important for nested views/ranges
    explicit inline closing_view(Range const& r)
        : m_begin(r)
        , m_end(r, true)
    {}

    inline const_iterator begin() const { return m_begin; }
    inline const_iterator end() const { return m_end; }

private:
    const_iterator m_begin;
    const_iterator m_end;
};


template
<
    typename Range,
    closure_selector Close = geometry::closure<Range>::value
>
struct closed_view
    : identity_view<Range>
{
    explicit inline closed_view(Range const& r)
        : identity_view<Range const>(r)
    {}
};

template <typename Range>
struct closed_view<Range, open>
    : closing_view<Range>
{
    explicit inline closed_view(Range const& r)
        : closing_view<Range const>(r)
    {}
};


} // namespace detail
#endif // DOXYGEN_NO_DETAIL


/*!
\brief View on a range, either closing it or leaving it as it is
\details The closeable_view is used internally by the library to handle all rings,
    either closed or open, the same way. The default method is closed, all
    algorithms process rings as if they are closed. Therefore, if they are opened,
    a view is created which closes them.
    The closeable_view might be used by library users, but its main purpose is
    internally.
\tparam Range Original range
\tparam Close Specifies if it the range is closed, if so, nothing will happen.
    If it is open, it will iterate the first point after the last point.
\ingroup views
*/
template <typename Range, closure_selector Close>
struct closeable_view {};


#ifndef DOXYGEN_NO_SPECIALIZATIONS

template <typename Range>
struct closeable_view<Range, closed>
{
    using type = identity_view<Range>;
};


template <typename Range>
struct closeable_view<Range, open>
{
    using type = detail::closing_view<Range>;
};

#endif // DOXYGEN_NO_SPECIALIZATIONS


#ifndef DOXYGEN_NO_TRAITS_SPECIALIZATIONS
namespace traits
{


template <typename Range, closure_selector Close>
struct tag<geometry::detail::closed_view<Range, Close> >
    : geometry::tag<Range>
{};

template <typename Range, closure_selector Close>
struct point_order<geometry::detail::closed_view<Range, Close> >
    : geometry::point_order<Range>
{};

template <typename Range, closure_selector Close>
struct closure<geometry::detail::closed_view<Range, Close> >
{
    static const closure_selector value = closed;
};


} // namespace traits
#endif // DOXYGEN_NO_TRAITS_SPECIALIZATIONS


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_VIEWS_CLOSEABLE_VIEW_HPP
