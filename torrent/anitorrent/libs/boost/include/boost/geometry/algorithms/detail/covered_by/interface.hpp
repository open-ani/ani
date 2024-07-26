// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2013-2021.
// Modifications copyright (c) 2013-2021 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_COVERED_BY_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_COVERED_BY_INTERFACE_HPP


#include <boost/geometry/algorithms/detail/within/interface.hpp>
#include <boost/geometry/algorithms/not_implemented.hpp>

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
struct covered_by
    : not_implemented<Tag1, Tag2>
{};

} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_strategy {

template
<
    typename Strategy,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategy>::value
>
struct covered_by
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        concepts::within::check<Geometry1, Geometry2, Strategy>();
        concepts::check<Geometry1 const>();
        concepts::check<Geometry2 const>();
        assert_dimension_equal<Geometry1, Geometry2>();

        return dispatch::covered_by
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, strategy);
    }
};

template <typename Strategy>
struct covered_by<Strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using strategies::relate::services::strategy_converter;

        return covered_by
            <
                decltype(strategy_converter<Strategy>::get(strategy))
            >::apply(geometry1, geometry2,
                        strategy_converter<Strategy>::get(strategy));
    }
};

template <>
struct covered_by<default_strategy, false>
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

        return covered_by
            <
                strategy_type
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
struct covered_by
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        return resolve_strategy::covered_by
            <
                Strategy
            >::apply(geometry1, geometry2, strategy);
    }
};

template <typename Geometry1, typename Geometry2, typename Tag2>
struct covered_by<Geometry1, Geometry2, dynamic_geometry_tag, Tag2>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry1>::apply([&](auto const& g1)
        {
            result = resolve_strategy::covered_by
                <
                    Strategy
                >::apply(g1, geometry2, strategy);
        }, geometry1);
        return result;
    }
};

template <typename Geometry1, typename Geometry2, typename Tag1>
struct covered_by<Geometry1, Geometry2, Tag1, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry2>::apply([&](auto const& g2)
        {
            result = resolve_strategy::covered_by
                <
                    Strategy
                >::apply(geometry1, g2, strategy);
        }, geometry2);
        return result;
    }
};

template <typename Geometry1, typename Geometry2>
struct covered_by<Geometry1, Geometry2, dynamic_geometry_tag, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<Geometry1, Geometry2>::apply([&](auto const& g1, auto const& g2)
        {
            result = resolve_strategy::covered_by
                <
                    Strategy
                >::apply(g1, g2, strategy);
        }, geometry1, geometry2);
        return result;
    }
};

} // namespace resolve_dynamic


/*!
\brief \brief_check12{is inside or on border}
\ingroup covered_by
\details \details_check12{covered_by, is inside or on border}.
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\param geometry1 \param_geometry which might be inside or on the border of the second geometry
\param geometry2 \param_geometry which might cover the first geometry
\return true if geometry1 is inside of or on the border of geometry2,
    else false
\note The default strategy is used for covered_by detection

\qbk{[include reference/algorithms/covered_by.qbk]}
\qbk{
[heading Examples]
[covered_by]
[covered_by_output]
}
 */
template<typename Geometry1, typename Geometry2>
inline bool covered_by(Geometry1 const& geometry1, Geometry2 const& geometry2)
{
    return resolve_dynamic::covered_by
        <
            Geometry1, Geometry2
        >::apply(geometry1, geometry2, default_strategy());
}

/*!
\brief \brief_check12{is inside or on border} \brief_strategy
\ingroup covered_by
\details \details_check12{covered_by, is inside or on border}, \brief_strategy. \details_strategy_reasons
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\param geometry1 \param_geometry which might be inside or on the border of the second geometry
\param geometry2 \param_geometry which might cover the first geometry
\param strategy strategy to be used
\return true if geometry1 is inside of or on the border of geometry2,
    else false

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/covered_by.qbk]}

*/
template<typename Geometry1, typename Geometry2, typename Strategy>
inline bool covered_by(Geometry1 const& geometry1, Geometry2 const& geometry2,
        Strategy const& strategy)
{
    return resolve_dynamic::covered_by
        <
            Geometry1, Geometry2
        >::apply(geometry1, geometry2, strategy);
}

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_COVERED_BY_INTERFACE_HPP
