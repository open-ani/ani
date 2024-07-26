// Boost.Geometry

// Copyright (c) 2022-2023, Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_VIEWS_DETAIL_RANDOM_ACCESS_VIEW_HPP
#define BOOST_GEOMETRY_VIEWS_DETAIL_RANDOM_ACCESS_VIEW_HPP

#include <vector>

#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>
#include <boost/range/iterator.hpp>
#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/core/geometry_types.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/util/sequence.hpp>
#include <boost/geometry/util/type_traits.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail
{


template <typename Range>
struct is_random_access_range
    : std::is_convertible
        <
            typename boost::iterator_traversal
                <
                    typename boost::range_iterator<Range>::type
                >::type,
            boost::random_access_traversal_tag
        >
{};

template <typename Geometry>
struct is_geometry_collection_recursive
    : util::bool_constant
        <
            util::is_geometry_collection
                <
                    typename util::sequence_find_if
                        <
                            typename traits::geometry_types<std::remove_const_t<Geometry>>::type,
                            util::is_geometry_collection
                        >::type
                >::value
        >
{};

template
<
    typename GeometryCollection,
    bool IsRandomAccess = is_random_access_range<GeometryCollection>::value,
    bool IsRecursive = is_geometry_collection_recursive<GeometryCollection>::value
>
class random_access_view
    : public std::vector<typename boost::range_iterator<GeometryCollection>::type>
{
    // NOTE: An alternative would be to implement iterator holding base iterators
    //   to geometry collections of lower levels to process after the current level
    //   of bfs traversal is finished.

    using base_t = std::vector<typename boost::range_iterator<GeometryCollection>::type>;

public:
    random_access_view(GeometryCollection & geometry)
    {
        this->reserve(boost::size(geometry));
        detail::visit_breadth_first_impl<true>::apply([&](auto&&, auto iter)
        {
            this->push_back(iter);
            return true;
        }, geometry);
    }
};

template <typename GeometryCollection>
class random_access_view<GeometryCollection, true, false>
{
public:
    using iterator = typename boost::range_iterator<GeometryCollection>::type;
    using const_iterator = typename boost::range_const_iterator<GeometryCollection>::type;

    random_access_view(GeometryCollection & geometry)
        : m_begin(boost::begin(geometry))
        , m_end(boost::end(geometry))
    {}

    iterator begin() { return m_begin; }
    iterator end() { return m_end; }
    const_iterator begin() const { return m_begin; }
    const_iterator end() const { return m_end; }

private:
    iterator m_begin, m_end;
};


template <typename GeometryCollection>
struct random_access_view_iter_visit
{
    template <typename Function, typename Iterator>
    static void apply(Function && function, Iterator iterator)
    {
        geometry::traits::iter_visit
            <
                std::remove_const_t<GeometryCollection>
            >::apply(std::forward<Function>(function), *iterator);
    }
};


template <typename ...Ts>
struct remove_geometry_collections_pack
{
    using type = util::type_sequence<>;
};

template <typename T, typename ...Ts>
struct remove_geometry_collections_pack<T, Ts...>
{
    using next_sequence = typename remove_geometry_collections_pack<Ts...>::type;
    using type = std::conditional_t
        <
            util::is_geometry_collection<T>::value,
            next_sequence,
            typename util::sequence_merge<util::type_sequence<T>, next_sequence>::type
        >;
};

template <typename Types>
struct remove_geometry_collections;

template <typename ...Ts>
struct remove_geometry_collections<util::type_sequence<Ts...>>
    : remove_geometry_collections_pack<Ts...>
{};


} // namespace detail
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_TRAITS_SPECIALIZATIONS
namespace traits
{

template<typename GeometryCollection, bool IsRandomAccess, bool IsRecursive>
struct tag<geometry::detail::random_access_view<GeometryCollection, IsRandomAccess, IsRecursive>>
{
    using type = geometry_collection_tag;
};


template <typename GeometryCollection>
struct iter_visit<geometry::detail::random_access_view<GeometryCollection, false, false>>
    : geometry::detail::random_access_view_iter_visit<GeometryCollection>
{};

template <typename GeometryCollection>
struct iter_visit<geometry::detail::random_access_view<GeometryCollection, true, true>>
    : geometry::detail::random_access_view_iter_visit<GeometryCollection>
{};

template <typename GeometryCollection>
struct iter_visit<geometry::detail::random_access_view<GeometryCollection, false, true>>
    : geometry::detail::random_access_view_iter_visit<GeometryCollection>
{};

template <typename GeometryCollection>
struct iter_visit<geometry::detail::random_access_view<GeometryCollection, true, false>>
{
    template <typename Function, typename Iterator>
    static void apply(Function && function, Iterator iterator)
    {
        geometry::traits::iter_visit
            <
                std::remove_const_t<GeometryCollection>
            >::apply(std::forward<Function>(function), iterator);
    }
};


template <typename GeometryCollection, bool IsRandomAccess>
struct geometry_types<geometry::detail::random_access_view<GeometryCollection, IsRandomAccess, false>>
    : traits::geometry_types
        <
            std::remove_const_t<GeometryCollection>
        >
{};

template <typename GeometryCollection, bool IsRandomAccess>
struct geometry_types<geometry::detail::random_access_view<GeometryCollection, IsRandomAccess, true>>
    : geometry::detail::remove_geometry_collections
        <
            typename traits::geometry_types
                <
                    std::remove_const_t<GeometryCollection>
                >::type
        >
{};

} // namespace traits
#endif // DOXYGEN_NO_TRAITS_SPECIALIZATIONS


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_VIEWS_DETAIL_RANDOM_ACCESS_VIEW_HPP
