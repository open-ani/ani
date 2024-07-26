// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2013-2022.
// Modifications copyright (c) 2013-2022 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_WITHIN_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_WITHIN_INTERFACE_HPP


#include <boost/concept_check.hpp>

#include <boost/geometry/algorithms/not_implemented.hpp>

#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tag_cast.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp>
#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/strategies/concepts/within_concept.hpp>
#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/relate/services.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{

template
<
    typename Geometry1,
    typename Geometry2,
    typename Tag1 = typename tag<Geometry1>::type,
    typename Tag2 = typename tag<Geometry2>::type
>
struct within
    : not_implemented<Tag1, Tag2>
{};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_strategy
{

template
<
    typename Strategy,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategy>::value
>
struct within
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        concepts::within::check<Geometry1, Geometry2, Strategy>();

        return dispatch::within
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, strategy);
    }
};

template <typename Strategy>
struct within<Strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using strategies::relate::services::strategy_converter;

        return within
            <
                decltype(strategy_converter<Strategy>::get(strategy))
            >::apply(geometry1, geometry2,
                     strategy_converter<Strategy>::get(strategy));
    }
};

template <>
struct within<default_strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             default_strategy)
    {
        typedef typename strategies::relate::services::default_strategy
            <
                Geometry1,
                Geometry2
            >::type strategy_type;

        return within
            <
                strategy_type
            >::apply(geometry1, geometry2, strategy_type());
    }
};

} // namespace resolve_strategy


namespace resolve_dynamic
{

template
<
    typename Geometry1, typename Geometry2,
    typename Tag1 = typename geometry::tag<Geometry1>::type,
    typename Tag2 = typename geometry::tag<Geometry2>::type
>
struct within
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        concepts::check<Geometry1 const>();
        concepts::check<Geometry2 const>();
        assert_dimension_equal<Geometry1, Geometry2>();

        return resolve_strategy::within
            <
                Strategy
            >::apply(geometry1, geometry2, strategy);
    }
};

template <typename Geometry1, typename Geometry2, typename Tag2>
struct within<Geometry1, Geometry2, dynamic_geometry_tag, Tag2>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry1>::apply([&](auto const& g1)
        {
            result = within
                <
                    util::remove_cref_t<decltype(g1)>,
                    Geometry2
                >::apply(g1, geometry2, strategy);
        }, geometry1);
        return result;
    }
};

template <typename Geometry1, typename Geometry2, typename Tag1>
struct within<Geometry1, Geometry2, Tag1, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry2>::apply([&](auto const& g2)
        {
            result = within
                <
                    Geometry1,
                    util::remove_cref_t<decltype(g2)>
                >::apply(geometry1, g2, strategy);
        }, geometry2);
        return result;
    }
};

template <typename Geometry1, typename Geometry2>
struct within<Geometry1, Geometry2, dynamic_geometry_tag, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry1, Geometry2>::apply([&](auto const& g1, auto const& g2)
        {
            result = within
                <
                    util::remove_cref_t<decltype(g1)>,
                    util::remove_cref_t<decltype(g2)>
                >::apply(g1, g2, strategy);
        }, geometry1, geometry2);
        return result;
    }
};

}


/*!
\brief \brief_check12{is completely inside}
\ingroup within
\details \details_check12{within, is completely inside}.
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\param geometry1 \param_geometry which might be within the second geometry
\param geometry2 \param_geometry which might contain the first geometry
\return true if geometry1 is completely contained within geometry2,
    else false
\note The default strategy is used for within detection


\qbk{[include reference/algorithms/within.qbk]}

\qbk{
[heading Example]
[within]
[within_output]
}
 */
template<typename Geometry1, typename Geometry2>
inline bool within(Geometry1 const& geometry1, Geometry2 const& geometry2)
{
    return resolve_dynamic::within
        <
            Geometry1,
            Geometry2
        >::apply(geometry1, geometry2, default_strategy());
}

/*!
\brief \brief_check12{is completely inside} \brief_strategy
\ingroup within
\details \details_check12{within, is completely inside}, \brief_strategy. \details_strategy_reasons
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\param geometry1 \param_geometry which might be within the second geometry
\param geometry2 \param_geometry which might contain the first geometry
\param strategy strategy to be used
\return true if geometry1 is completely contained within geometry2,
    else false

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/within.qbk]}
\qbk{
[heading Available Strategies]
\* [link geometry.reference.strategies.strategy_within_winding Winding (coordinate system agnostic)]
\* [link geometry.reference.strategies.strategy_within_franklin Franklin (cartesian)]
\* [link geometry.reference.strategies.strategy_within_crossings_multiply Crossings Multiply (cartesian)]

[heading Example]
[within_strategy]
[within_strategy_output]

}
*/
template<typename Geometry1, typename Geometry2, typename Strategy>
inline bool within(Geometry1 const& geometry1,
                   Geometry2 const& geometry2,
                   Strategy const& strategy)
{
    return resolve_dynamic::within
        <
            Geometry1,
            Geometry2
        >::apply(geometry1, geometry2, strategy);
}

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_WITHIN_INTERFACE_HPP
