// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_SELECT_GEOMETRY_TYPE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_SELECT_GEOMETRY_TYPE_HPP

#include <boost/geometry/core/geometry_types.hpp>
#include <boost/geometry/core/static_assert.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/util/sequence.hpp>
#include <boost/geometry/util/type_traits.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail
{


template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct first_geometry_type
{
    using type = Geometry;
};

template <typename Geometry>
struct first_geometry_type<Geometry, geometry_collection_tag>
{
    template <typename T>
    using pred = util::bool_constant
        <
            ! util::is_dynamic_geometry<T>::value
            && ! util::is_geometry_collection<T>::value
        >;

    using type = typename util::sequence_find_if
        <
            typename traits::geometry_types<Geometry>::type,
            pred
        >::type;
};

template <typename Geometry>
struct first_geometry_type<Geometry, dynamic_geometry_tag>
    : first_geometry_type<Geometry, geometry_collection_tag>
{};


template
<
    typename Geometry,
    bool IsDynamicOrCollection = util::is_dynamic_geometry<Geometry>::value
                              || util::is_geometry_collection<Geometry>::value
>
struct geometry_types
{
    using type = util::type_sequence<std::remove_const_t<Geometry>>;
};

template <typename Geometry>
struct geometry_types<Geometry, true>
{
    using type = typename traits::geometry_types<std::remove_const_t<Geometry>>::type;
};


template
<
    typename Geometry,
    template <typename, typename> class LessPred,
    bool IsDynamicOrCollection = util::is_dynamic_geometry<Geometry>::value
                              || util::is_geometry_collection<Geometry>::value
>
struct select_geometry_type
{
    using type = Geometry;
};

template
<
    typename Geometry,
    template <typename, typename> class LessPred
>
struct select_geometry_type<Geometry, LessPred, true>
    : util::sequence_min_element
        <
            typename traits::geometry_types<std::remove_const_t<Geometry>>::type,
            LessPred
        >
{};


template
<
    typename Geometry1, typename Geometry2,
    template <typename, typename> class LessPred,
    bool IsDynamicOrCollection = util::is_dynamic_geometry<Geometry1>::value
                              || util::is_dynamic_geometry<Geometry2>::value
                              || util::is_geometry_collection<Geometry1>::value
                              || util::is_geometry_collection<Geometry2>::value
>
struct select_geometry_types
{
    using type = util::type_sequence
        <
            std::remove_const_t<Geometry1>,
            std::remove_const_t<Geometry2>
        >;
};

template
<
    typename Geometry1, typename Geometry2,
    template <typename, typename> class LessPred
>
struct select_geometry_types<Geometry1, Geometry2, LessPred, true>
    : util::sequence_min_element
        <
            typename util::sequence_combine
                <
                    typename geometry_types<Geometry1>::type,
                    typename geometry_types<Geometry2>::type
                >::type,
            LessPred
        >
{};


} // namespace detail
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_SELECT_GEOMETRY_TYPE_HPP
