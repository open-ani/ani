// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2015 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2013-2022.
// Modifications copyright (c) 2013-2022 Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATION_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATION_INTERFACE_HPP


#include <boost/geometry/algorithms/detail/relate/interface.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace relate
{

template <typename Geometry1, typename Geometry2>
struct result_handler_type<Geometry1, Geometry2, geometry::de9im::matrix>
{
    typedef matrix_handler<geometry::de9im::matrix> type;
};


}} // namespace detail::relate
#endif // DOXYGEN_NO_DETAIL

namespace resolve_dynamic
{

template
<
    typename Geometry1, typename Geometry2,
    typename Tag1 = typename geometry::tag<Geometry1>::type,
    typename Tag2 = typename geometry::tag<Geometry2>::type
>
struct relation
{
    template <typename Matrix, typename Strategy>
    static inline Matrix apply(Geometry1 const& geometry1,
                               Geometry2 const& geometry2,
                               Strategy const& strategy)
    {
        concepts::check<Geometry1 const>();
        concepts::check<Geometry2 const>();
        assert_dimension_equal<Geometry1, Geometry2>();

        typename detail::relate::result_handler_type
            <
                Geometry1,
                Geometry2,
                Matrix
            >::type handler;

        resolve_strategy::relate
            <
                Strategy
            >::apply(geometry1, geometry2, handler, strategy);

        return handler.result();
    }
};

template <typename Geometry1, typename Geometry2, typename Tag2>
struct relation<Geometry1, Geometry2, dynamic_geometry_tag, Tag2>
{
    template <typename Matrix, typename Strategy>
    static inline Matrix apply(Geometry1 const& geometry1,
                               Geometry2 const& geometry2,
                               Strategy const& strategy)
    {
        Matrix result;
        traits::visit<Geometry1>::apply([&](auto const& g1)
        {
            result = relation
                <
                    util::remove_cref_t<decltype(g1)>,
                    Geometry2
                >::template apply<Matrix>(g1, geometry2, strategy);
        }, geometry1);
        return result;
    }
};

template <typename Geometry1, typename Geometry2, typename Tag1>
struct relation<Geometry1, Geometry2, Tag1, dynamic_geometry_tag>
{
    template <typename Matrix, typename Strategy>
    static inline Matrix apply(Geometry1 const& geometry1,
                               Geometry2 const& geometry2,
                               Strategy const& strategy)
    {
        Matrix result;
        traits::visit<Geometry2>::apply([&](auto const& g2)
        {
            result = relation
                <
                    Geometry1,
                    util::remove_cref_t<decltype(g2)>
                >::template apply<Matrix>(geometry1, g2, strategy);
        }, geometry2);
        return result;
    }
};

template <typename Geometry1, typename Geometry2>
struct relation<Geometry1, Geometry2, dynamic_geometry_tag, dynamic_geometry_tag>
{
    template <typename Matrix, typename Strategy>
    static inline Matrix apply(Geometry1 const& geometry1,
                               Geometry2 const& geometry2,
                               Strategy const& strategy)
    {
        Matrix result;
        traits::visit<Geometry1, Geometry2>::apply([&](auto const& g1, auto const& g2)
        {
            result = relation
                <
                    util::remove_cref_t<decltype(g1)>,
                    util::remove_cref_t<decltype(g2)>
                >::template apply<Matrix>(g1, g2, strategy);
        }, geometry1, geometry2);
        return result;
    }
};

} // namespace resolve_dynamic


/*!
\brief Calculates the relation between a pair of geometries as defined in DE-9IM.
\ingroup relation
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam Strategy \tparam_strategy{Relation}
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param strategy \param_strategy{relation}
\return The DE-9IM matrix expressing the relation between geometries.

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/relation.qbk]}
 */
template <typename Geometry1, typename Geometry2, typename Strategy>
inline de9im::matrix relation(Geometry1 const& geometry1,
                              Geometry2 const& geometry2,
                              Strategy const& strategy)
{
    return resolve_dynamic::relation
        <
            Geometry1,
            Geometry2
        >::template apply<de9im::matrix>(geometry1, geometry2, strategy);
}


/*!
\brief Calculates the relation between a pair of geometries as defined in DE-9IM.
\ingroup relation
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\return The DE-9IM matrix expressing the relation between geometries.

\qbk{[include reference/algorithms/relation.qbk]}
 */
template <typename Geometry1, typename Geometry2>
inline de9im::matrix relation(Geometry1 const& geometry1,
                              Geometry2 const& geometry2)
{
    return resolve_dynamic::relation
        <
            Geometry1,
            Geometry2
        >::template apply<de9im::matrix>(geometry1, geometry2, default_strategy());
}


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_INTERFACE_HPP
