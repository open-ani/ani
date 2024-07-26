// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2015 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2015 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2015-2021.
// Modifications copyright (c) 2015-2021, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Distributed under the Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_INTERFACE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_INTERFACE_HPP


#include <boost/geometry/algorithms/dispatch/envelope.hpp>

#include <boost/geometry/core/coordinate_system.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>

#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/geometries/concepts/check.hpp>

#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/strategies/envelope/services.hpp>

#include <boost/geometry/util/select_most_precise.hpp>
#include <boost/geometry/util/type_traits_std.hpp>


namespace boost { namespace geometry
{

namespace resolve_strategy
{

template
<
    typename Strategy,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategy>::value
>
struct envelope
{
    template <typename Geometry, typename Box>
    static inline void apply(Geometry const& geometry,
                             Box& box,
                             Strategy const& strategy)
    {
        dispatch::envelope<Geometry>::apply(geometry, box, strategy);
    }
};

template <typename Strategy>
struct envelope<Strategy, false>
{
    template <typename Geometry, typename Box>
    static inline void apply(Geometry const& geometry,
                             Box& box,
                             Strategy const& strategy)
    {
        using strategies::envelope::services::strategy_converter;
        return dispatch::envelope
            <
                Geometry
            >::apply(geometry, box, strategy_converter<Strategy>::get(strategy));
    }
};

template <>
struct envelope<default_strategy, false>
{
    template <typename Geometry, typename Box>
    static inline void apply(Geometry const& geometry,
                             Box& box,
                             default_strategy)
    {
        typedef typename strategies::envelope::services::default_strategy
            <
                Geometry, Box
            >::type strategy_type;

        dispatch::envelope<Geometry>::apply(geometry, box, strategy_type());
    }
};

} // namespace resolve_strategy

namespace resolve_dynamic
{

template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct envelope
{
    template <typename Box, typename Strategy>
    static inline void apply(Geometry const& geometry,
                             Box& box,
                             Strategy const& strategy)
    {
        concepts::check<Geometry const>();
        concepts::check<Box>();

        resolve_strategy::envelope<Strategy>::apply(geometry, box, strategy);
    }
};


template <typename Geometry>
struct envelope<Geometry, dynamic_geometry_tag>
{
    template <typename Box, typename Strategy>
    static inline void apply(Geometry const& geometry,
                             Box& box,
                             Strategy const& strategy)
    {
        traits::visit<Geometry>::apply([&](auto const& g)
        {
            envelope<util::remove_cref_t<decltype(g)>>::apply(g, box, strategy);
        }, geometry);
    }
};

} // namespace resolve_dynamic


/*!
\brief \brief_calc{envelope (with strategy)}
\ingroup envelope
\details \details_calc{envelope,\det_envelope}.
\tparam Geometry \tparam_geometry
\tparam Box \tparam_box
\tparam Strategy \tparam_strategy{Envelope}
\param geometry \param_geometry
\param mbr \param_box \param_set{envelope}
\param strategy \param_strategy{envelope}

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/envelope.qbk]}
\qbk{
[heading Example]
[envelope] [envelope_output]
}
*/
template<typename Geometry, typename Box, typename Strategy>
inline void envelope(Geometry const& geometry, Box& mbr, Strategy const& strategy)
{
    resolve_dynamic::envelope<Geometry>::apply(geometry, mbr, strategy);
}

/*!
\brief \brief_calc{envelope}
\ingroup envelope
\details \details_calc{envelope,\det_envelope}.
\tparam Geometry \tparam_geometry
\tparam Box \tparam_box
\param geometry \param_geometry
\param mbr \param_box \param_set{envelope}

\qbk{[include reference/algorithms/envelope.qbk]}
\qbk{
[heading Example]
[envelope] [envelope_output]
}
*/
template<typename Geometry, typename Box>
inline void envelope(Geometry const& geometry, Box& mbr)
{
    resolve_dynamic::envelope<Geometry>::apply(geometry, mbr, default_strategy());
}


/*!
\brief \brief_calc{envelope}
\ingroup envelope
\details \details_calc{return_envelope,\det_envelope}. \details_return{envelope}
\tparam Box \tparam_box
\tparam Geometry \tparam_geometry
\tparam Strategy \tparam_strategy{Envelope}
\param geometry \param_geometry
\param strategy \param_strategy{envelope}
\return \return_calc{envelope}

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/envelope.qbk]}
\qbk{
[heading Example]
[return_envelope] [return_envelope_output]
}
*/
template<typename Box, typename Geometry, typename Strategy>
inline Box return_envelope(Geometry const& geometry, Strategy const& strategy)
{
    Box mbr;
    resolve_dynamic::envelope<Geometry>::apply(geometry, mbr, strategy);
    return mbr;
}

/*!
\brief \brief_calc{envelope}
\ingroup envelope
\details \details_calc{return_envelope,\det_envelope}. \details_return{envelope}
\tparam Box \tparam_box
\tparam Geometry \tparam_geometry
\param geometry \param_geometry
\return \return_calc{envelope}

\qbk{[include reference/algorithms/envelope.qbk]}
\qbk{
[heading Example]
[return_envelope] [return_envelope_output]
}
*/
template<typename Box, typename Geometry>
inline Box return_envelope(Geometry const& geometry)
{
    Box mbr;
    resolve_dynamic::envelope<Geometry>::apply(geometry, mbr, default_strategy());
    return mbr;
}

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_ENVELOPE_INTERFACE_HPP
