// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.
// Copyright (c) 2014 Samuel Debionne, Grenoble, France.

// This file was modified by Oracle on 2014-2022.
// Modifications copyright (c) 2014-2022 Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_CROSSES_HPP
#define BOOST_GEOMETRY_ALGORITHMS_CROSSES_HPP

#include <cstddef>

#include <boost/geometry/algorithms/detail/gc_topological_dimension.hpp>
#include <boost/geometry/algorithms/detail/relate/relate_impl.hpp>
#include <boost/geometry/algorithms/relate.hpp>
#include <boost/geometry/core/access.hpp>
#include <boost/geometry/geometries/adapted/boost_variant.hpp>
#include <boost/geometry/geometries/concepts/check.hpp>
#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/relate/cartesian.hpp>
#include <boost/geometry/strategies/relate/geographic.hpp>
#include <boost/geometry/strategies/relate/spherical.hpp>
#include <boost/geometry/views/detail/geometry_collection_view.hpp>


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
struct crosses
    : detail::relate::relate_impl
        <
            detail::de9im::static_mask_crosses_type,
            Geometry1,
            Geometry2
        >
{};


template <typename Geometry1, typename Geometry2>
struct crosses<Geometry1, Geometry2, geometry_collection_tag, geometry_collection_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        int const dimension1 = detail::gc_topological_dimension(geometry1);
        int const dimension2 = detail::gc_topological_dimension(geometry2);

        if (dimension1 >= 0 && dimension2 >= 0)
        {
            if (dimension1 < dimension2)
            {
                return detail::relate::relate_impl
                    <
                        detail::de9im::static_mask_crosses_d1_le_d2_type,
                        Geometry1,
                        Geometry2
                    >::apply(geometry1, geometry2, strategy);
            }
            else if (dimension1 > dimension2)
            {
                return detail::relate::relate_impl
                    <
                        detail::de9im::static_mask_crosses_d2_le_d1_type,
                        Geometry1,
                        Geometry2
                    >::apply(geometry1, geometry2, strategy);
            }
            else if (dimension1 == 1 && dimension2 == 1)
            {
                return detail::relate::relate_impl
                    <
                        detail::de9im::static_mask_crosses_d1_1_d2_1_type,
                        Geometry1,
                        Geometry2
                    >::apply(geometry1, geometry2, strategy);
            }
        }

        return false;
    }
};

template <typename Geometry1, typename Geometry2, typename Tag1>
struct crosses<Geometry1, Geometry2, Tag1, geometry_collection_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using gc1_view_t = detail::geometry_collection_view<Geometry1>;
        return crosses
            <
                gc1_view_t, Geometry2
            >::apply(gc1_view_t(geometry1), geometry2, strategy);
    }
};

template <typename Geometry1, typename Geometry2, typename Tag2>
struct crosses<Geometry1, Geometry2, geometry_collection_tag, Tag2>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1, Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        using gc2_view_t = detail::geometry_collection_view<Geometry2>;
        return crosses
            <
                Geometry1, gc2_view_t
            >::apply(geometry1, gc2_view_t(geometry2), strategy);
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
struct crosses
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        concepts::check<Geometry1 const>();
        concepts::check<Geometry2 const>();

        return dispatch::crosses
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, strategy);
    }
};

template <typename Strategy>
struct crosses<Strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        //using strategies::crosses::services::strategy_converter;
        using strategies::relate::services::strategy_converter;
        return crosses
            <
                decltype(strategy_converter<Strategy>::get(strategy))
            >::apply(geometry1, geometry2,
                     strategy_converter<Strategy>::get(strategy));
    }
};

template <>
struct crosses<default_strategy, false>
{
    template <typename Geometry1, typename Geometry2>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             default_strategy)
    {
        //typedef typename strategies::crosses::services::default_strategy
        typedef typename strategies::relate::services::default_strategy
            <
                Geometry1,
                Geometry2
            >::type strategy_type;

        return crosses
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
struct crosses
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        return resolve_strategy::crosses
            <
                Strategy
            >::apply(geometry1, geometry2, strategy);
    }
};


template <typename DynamicGeometry1, typename Geometry2, typename Tag2>
struct crosses<DynamicGeometry1, Geometry2, dynamic_geometry_tag, Tag2>
{
    template <typename Strategy>
    static inline bool apply(DynamicGeometry1 const& geometry1,
                             Geometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<DynamicGeometry1>::apply([&](auto const& g1)
        {
            result = resolve_strategy::crosses
                <
                    Strategy
                >::apply(g1, geometry2, strategy);
        }, geometry1);
        return result;
    }
};


template <typename Geometry1, typename DynamicGeometry2, typename Tag1>
struct crosses<Geometry1, DynamicGeometry2, Tag1, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline bool apply(Geometry1 const& geometry1,
                             DynamicGeometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<DynamicGeometry2>::apply([&](auto const& g2)
        {
            result = resolve_strategy::crosses
                <
                    Strategy
                >::apply(geometry1, g2, strategy);
        }, geometry2);
        return result;
    }
};


template <typename DynamicGeometry1, typename DynamicGeometry2>
struct crosses<DynamicGeometry1, DynamicGeometry2, dynamic_geometry_tag, dynamic_geometry_tag>
{
    template <typename Strategy>
    static inline bool apply(DynamicGeometry1 const& geometry1,
                             DynamicGeometry2 const& geometry2,
                             Strategy const& strategy)
    {
        bool result = false;
        traits::visit<DynamicGeometry1, DynamicGeometry2>::apply([&](auto const& g1, auto const& g2)
        {
            result = resolve_strategy::crosses
                <
                    Strategy
                >::apply(g1, g2, strategy);
        }, geometry1, geometry2);
        return result;
    }
};


} // namespace resolve_dynamic


/*!
\brief \brief_check2{crosses}
\ingroup crosses
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam Strategy \tparam_strategy{Crosses}
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param strategy \param_strategy{crosses}
\return \return_check2{crosses}

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/crosses.qbk]}
*/
template <typename Geometry1, typename Geometry2, typename Strategy>
inline bool crosses(Geometry1 const& geometry1,
                    Geometry2 const& geometry2,
                    Strategy const& strategy)
{
    return resolve_dynamic::crosses
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, strategy);
}

/*!
\brief \brief_check2{crosses}
\ingroup crosses
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\return \return_check2{crosses}

\qbk{[include reference/algorithms/crosses.qbk]}
\qbk{
[heading Examples]
[crosses]
[crosses_output]
}
*/
template <typename Geometry1, typename Geometry2>
inline bool crosses(Geometry1 const& geometry1, Geometry2 const& geometry2)
{
    return resolve_dynamic::crosses
            <
                Geometry1, Geometry2
            >::apply(geometry1, geometry2, default_strategy());
}

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_CROSSES_HPP
