// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2015 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2015 Mateusz Loskot, London, UK.
// Copyright (c) 2013-2015 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2013-2022.
// Modifications copyright (c) 2013-2022, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_TOUCHES_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_TOUCHES_INTERFACE_HPP


#include <deque>

#include <boost/geometry/core/reverse_dispatch.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tag_cast.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp>
#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/relate/services.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch {

// TODO: Since CastedTags are used is Reverse needed?

template
<
    typename Geometry1,
    typename Geometry2,
    typename Tag1 = typename tag<Geometry1>::type,
    typename Tag2 = typename tag<Geometry2>::type,
    typename CastedTag1 = typename tag_cast<Tag1, pointlike_tag, linear_tag, areal_tag>::type,
    typename CastedTag2 = typename tag_cast<Tag2, pointlike_tag, linear_tag, areal_tag>::type,
    bool Reverse = reverse_dispatch<Geometry1, Geometry2>::type::value
>
struct touches
    : not_implemented<Tag1, Tag2>
{};

// If reversal is needed, perform it
template
<
    typename Geometry1, typename Geometry2,
    typename Tag1, typename Tag2,
    typename CastedTag1, typename CastedTag2
>
struct touches<Geometry1, Geometry2, Tag1, Tag2, CastedTag1, CastedTag2, true>
    : touches<Geometry2, Geometry1, Tag2, Tag1, CastedTag2, CastedTag1, false>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& g1, Geometry2 const& g2, Strategy const& strategy)
    {
        return touches<Geometry2, Geometry1>::apply(g2, g1, strategy);
    }
};

} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_strategy
{

template
<
    typename Strategy,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategy>::value
>
struct touches
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        return dispatch::touches
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, strategy);
    }
};

template <typename Strategy>
struct touches<Strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using strategies::relate::services::strategy_converter;

        return dispatch::touches
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2,
                     strategy_converter<Strategy>::get(strategy));
    }
};

template <>
struct touches<default_strategy, false>
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

        return dispatch::touches
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, strategy_type());
    }
};

} // namespace resolve_strategy


namespace resolve_dynamic {

template
<
    typename Geometry1, typename Geometry2,
    typename Tag1 = typename geometry::tag<Geometry1>::type,
    typename Tag2 = typename geometry::tag<Geometry2>::type
>
struct touches
{
    template <typename Strategy>
    static bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                      Strategy const& strategy)
    {
        concepts::check<Geometry1 const>();
        concepts::check<Geometry2 const>();

        return resolve_strategy::touches
                <
                    Strategy
                >::apply(geometry1, geometry2, strategy);
    }
};

template <typename Geometry1, typename Geometry2, typename Tag2>
struct touches<Geometry1, Geometry2, dynamic_geometry_tag, Tag2>
{
    template <typename Strategy>
    static bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                      Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry1>::apply([&](auto const& g1)
        {
            result = touches
                <
                    util::remove_cref_t<decltype(g1)>,
                    Geometry2
                >::apply(g1, geometry2, strategy);
        }, geometry1);
        return result;
    }
};

template <typename Geometry1, typename Geometry2, typename Tag1>
struct touches<Geometry1, Geometry2, Tag1, dynamic_geometry_tag>
{
    template <typename Strategy>
    static bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                      Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry2>::apply([&](auto const& g2)
        {
            result = touches
                <
                    Geometry1,
                    util::remove_cref_t<decltype(g2)>
                >::apply(geometry1, g2, strategy);
        }, geometry2);
        return result;
    }
};

template <typename Geometry1, typename Geometry2>
struct touches<Geometry1, Geometry2, dynamic_geometry_tag, dynamic_geometry_tag>
{
    template <typename Strategy>
    static bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                      Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry1, Geometry2>::apply([&](auto const& g1, auto const& g2)
        {
            result = touches
                <
                    util::remove_cref_t<decltype(g1)>,
                    util::remove_cref_t<decltype(g2)>
                >::apply(g1, g2, strategy);
        }, geometry1, geometry2);
        return result;
    }
};

template <typename Geometry, typename Tag = typename geometry::tag<Geometry>::type>
struct self_touches;

template <typename Geometry>
struct self_touches<Geometry, dynamic_geometry_tag>
{
    static bool apply(Geometry const& geometry)
    {
        bool result = false;
        traits::visit<Geometry>::apply([&](auto const& g)
        {
            result = self_touches
                <
                    util::remove_cref_t<decltype(g)>
                >::apply(g);
        }, geometry);
        return result;
    }
};

} // namespace resolve_dynamic


/*!
\brief \brief_check{has at least one touching point (self-tangency)}
\note This function can be called for one geometry (self-tangency) and
    also for two geometries (touch)
\ingroup touches
\tparam Geometry \tparam_geometry
\param geometry \param_geometry
\return \return_check{is self-touching}

\qbk{distinguish,one geometry}
\qbk{[def __one_parameter__]}
\qbk{[include reference/algorithms/touches.qbk]}
\qbk{
[heading Examples]
[touches_one_geometry]
[touches_one_geometry_output]
}
*/
template <typename Geometry>
inline bool touches(Geometry const& geometry)
{
    return resolve_dynamic::self_touches<Geometry>::apply(geometry);
}


/*!
\brief \brief_check2{have at least one touching point (tangent - non overlapping)}
\ingroup touches
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\return \return_check2{touch each other}

\qbk{distinguish,two geometries}
\qbk{[include reference/algorithms/touches.qbk]}
\qbk{
[heading Examples]
[touches_two_geometries]
[touches_two_geometries_output]
}
 */
template <typename Geometry1, typename Geometry2>
inline bool touches(Geometry1 const& geometry1, Geometry2 const& geometry2)
{
    return resolve_dynamic::touches
        <
            Geometry1, Geometry2
        >::apply(geometry1, geometry2, default_strategy());
}

/*!
\brief \brief_check2{have at least one touching point (tangent - non overlapping)}
\ingroup touches
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam Strategy \tparam_strategy{Touches}
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param strategy \param_strategy{touches}
\return \return_check2{touch each other}

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/touches.qbk]}
 */
template <typename Geometry1, typename Geometry2, typename Strategy>
inline bool touches(Geometry1 const& geometry1,
                    Geometry2 const& geometry2,
                    Strategy const& strategy)
{
    return resolve_dynamic::touches
        <
            Geometry1, Geometry2
        >::apply(geometry1, geometry2, strategy);
}


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_TOUCHES_INTERFACE_HPP
