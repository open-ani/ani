// Boost.Geometry

// Copyright (c) 2023 Adam Wulkiewicz, Lodz, Poland.

// Copyright (c) 2017-2023, Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DENSIFY_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DENSIFY_HPP


#include <boost/range/size.hpp>
#include <boost/range/value_type.hpp>
#include <boost/throw_exception.hpp>

#include <boost/geometry/algorithms/clear.hpp>
#include <boost/geometry/algorithms/convert.hpp>
#include <boost/geometry/algorithms/detail/convert_point_to_point.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/algorithms/not_implemented.hpp>
#include <boost/geometry/core/closure.hpp>
#include <boost/geometry/core/exception.hpp>
#include <boost/geometry/core/tag.hpp>
#include <boost/geometry/core/tags.hpp>
#include <boost/geometry/core/visit.hpp>
#include <boost/geometry/geometries/adapted/boost_variant.hpp> // For backward compatibility
#include <boost/geometry/strategies/default_strategy.hpp>
#include <boost/geometry/strategies/densify/cartesian.hpp>
#include <boost/geometry/strategies/densify/geographic.hpp>
#include <boost/geometry/strategies/densify/spherical.hpp>
#include <boost/geometry/strategies/detail.hpp>
#include <boost/geometry/util/constexpr.hpp>
#include <boost/geometry/util/range.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace densify
{

template <typename Range>
struct push_back_policy
{
    typedef typename boost::range_value<Range>::type point_type;

    inline explicit push_back_policy(Range & rng)
        : m_rng(rng)
    {}

    inline void apply(point_type const& p)
    {
        range::push_back(m_rng, p);
    }

private:
    Range & m_rng;
};

template <typename Range, typename Point>
inline void convert_and_push_back(Range & range, Point const& p)
{
    typename boost::range_value<Range>::type p2;
    geometry::detail::conversion::convert_point_to_point(p, p2);
    range::push_back(range, p2);
}

template <bool AppendLastPoint = true>
struct densify_range
{
    template <typename FwdRng, typename MutRng, typename T, typename Strategies>
    static inline void apply(FwdRng const& rng, MutRng & rng_out,
                             T const& len, Strategies const& strategies)
    {
        typedef typename boost::range_value<FwdRng>::type point_t;

        auto it = boost::begin(rng);
        auto const end = boost::end(rng);

        if (it == end) // empty(rng)
        {
            return;
        }

        auto strategy = strategies.densify(rng);
        push_back_policy<MutRng> policy(rng_out);

        auto prev = it;
        for ( ++it ; it != end ; prev = it++)
        {
            point_t const& p0 = *prev;
            point_t const& p1 = *it;

            convert_and_push_back(rng_out, p0);

            strategy.apply(p0, p1, policy, len);
        }

        if BOOST_GEOMETRY_CONSTEXPR (AppendLastPoint)
        {
            convert_and_push_back(rng_out, *prev); // back(rng)
        }
    }
};

template <bool IsClosed1, bool IsClosed2> // false, X
struct densify_ring
{
    template <typename Geometry, typename GeometryOut, typename T, typename Strategies>
    static inline void apply(Geometry const& ring, GeometryOut & ring_out,
                             T const& len, Strategies const& strategies)
    {
        geometry::detail::densify::densify_range<true>
            ::apply(ring, ring_out, len, strategies);

        if (boost::size(ring) <= 1)
            return;

        auto const& p0 = range::back(ring);
        auto const& p1 = range::front(ring);

        auto strategy = strategies.densify(ring);
        push_back_policy<GeometryOut> policy(ring_out);

        strategy.apply(p0, p1, policy, len);

        if BOOST_GEOMETRY_CONSTEXPR (IsClosed2)
        {
            convert_and_push_back(ring_out, p1);
        }
    }
};

template <>
struct densify_ring<true, true>
    : densify_range<true>
{};

template <>
struct densify_ring<true, false>
    : densify_range<false>
{};

struct densify_convert
{
    template <typename GeometryIn, typename GeometryOut, typename T, typename Strategy>
    static void apply(GeometryIn const& in, GeometryOut &out,
                      T const& , Strategy const& )
    {
        geometry::convert(in, out);
    }
};

}} // namespace detail::densify
#endif // DOXYGEN_NO_DETAIL


#ifndef DOXYGEN_NO_DISPATCH
namespace dispatch
{


template
<
    typename Geometry,
    typename GeometryOut,
    typename Tag1 = typename tag<Geometry>::type,
    typename Tag2 = typename tag<GeometryOut>::type
>
struct densify
    : not_implemented<Tag1, Tag2>
{};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, point_tag, point_tag>
    : geometry::detail::densify::densify_convert
{};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, segment_tag, segment_tag>
    : geometry::detail::densify::densify_convert
{};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, box_tag, box_tag>
    : geometry::detail::densify::densify_convert
{};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, multi_point_tag, multi_point_tag>
    : geometry::detail::densify::densify_convert
{};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, linestring_tag, linestring_tag>
    : geometry::detail::densify::densify_range<>
{};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, multi_linestring_tag, multi_linestring_tag>
{
    template <typename T, typename Strategy>
    static void apply(Geometry const& mls, GeometryOut & mls_out,
                      T const& len, Strategy const& strategy)
    {
        std::size_t count = boost::size(mls);
        range::resize(mls_out, count);

        for (std::size_t i = 0 ; i < count ; ++i)
        {
            geometry::detail::densify::densify_range<>
                ::apply(range::at(mls, i), range::at(mls_out, i),
                        len, strategy);
        }
    }
};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, ring_tag, ring_tag>
    : geometry::detail::densify::densify_ring
        <
            geometry::closure<Geometry>::value != geometry::open,
            geometry::closure<GeometryOut>::value != geometry::open
        >
{};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, polygon_tag, polygon_tag>
{
    template <typename T, typename Strategy>
    static void apply(Geometry const& poly, GeometryOut & poly_out,
                      T const& len, Strategy const& strategy)
    {
        apply_ring(exterior_ring(poly), exterior_ring(poly_out),
                   len, strategy);

        std::size_t count = boost::size(interior_rings(poly));
        range::resize(interior_rings(poly_out), count);

        for (std::size_t i = 0 ; i < count ; ++i)
        {
            apply_ring(range::at(interior_rings(poly), i),
                       range::at(interior_rings(poly_out), i),
                       len, strategy);
        }
    }

    template <typename Ring, typename RingOut, typename T, typename Strategy>
    static void apply_ring(Ring const& ring, RingOut & ring_out,
                           T const& len, Strategy const& strategy)
    {
        densify<Ring, RingOut, ring_tag, ring_tag>
            ::apply(ring, ring_out, len, strategy);
    }
};

template <typename Geometry, typename GeometryOut>
struct densify<Geometry, GeometryOut, multi_polygon_tag, multi_polygon_tag>
{
    template <typename T, typename Strategy>
    static void apply(Geometry const& mpoly, GeometryOut & mpoly_out,
                      T const& len, Strategy const& strategy)
    {
        std::size_t count = boost::size(mpoly);
        range::resize(mpoly_out, count);

        for (std::size_t i = 0 ; i < count ; ++i)
        {
            apply_poly(range::at(mpoly, i),
                       range::at(mpoly_out, i),
                       len, strategy);
        }
    }

    template <typename Poly, typename PolyOut, typename T, typename Strategy>
    static void apply_poly(Poly const& poly, PolyOut & poly_out,
                           T const& len, Strategy const& strategy)
    {
        densify<Poly, PolyOut, polygon_tag, polygon_tag>::
            apply(poly, poly_out, len, strategy);
    }
};


} // namespace dispatch
#endif // DOXYGEN_NO_DISPATCH


namespace resolve_strategy
{

template
<
    typename Strategies,
    bool IsUmbrella = strategies::detail::is_umbrella_strategy<Strategies>::value
>
struct densify
{
    template <typename Geometry, typename Distance>
    static inline void apply(Geometry const& geometry,
                             Geometry& out,
                             Distance const& max_distance,
                             Strategies const& strategies)
    {
        dispatch::densify
            <
                Geometry, Geometry
            >::apply(geometry, out, max_distance, strategies);
    }
};

template <typename Strategy>
struct densify<Strategy, false>
{
    template <typename Geometry, typename Distance>
    static inline void apply(Geometry const& geometry,
                             Geometry& out,
                             Distance const& max_distance,
                             Strategy const& strategy)
    {
        using strategies::densify::services::strategy_converter;

        dispatch::densify
            <
                Geometry, Geometry
            >::apply(geometry, out, max_distance,
                     strategy_converter<Strategy>::get(strategy));
    }
};

template <>
struct densify<default_strategy, false>
{
    template <typename Geometry, typename Distance>
    static inline void apply(Geometry const& geometry,
                             Geometry& out,
                             Distance const& max_distance,
                             default_strategy const&)
    {
        typedef typename strategies::densify::services::default_strategy
            <
                Geometry
            >::type strategies_type;

        dispatch::densify
            <
                Geometry, Geometry
            >::apply(geometry, out, max_distance, strategies_type());
    }
};

} // namespace resolve_strategy


namespace resolve_dynamic {

template <typename Geometry, typename Tag = typename tag<Geometry>::type>
struct densify
{
    template <typename Distance, typename Strategy>
    static inline void apply(Geometry const& geometry,
                             Geometry& out,
                             Distance const& max_distance,
                             Strategy const& strategy)
    {
        resolve_strategy::densify
            <
                Strategy
            >::apply(geometry, out, max_distance, strategy);
    }
};

template <typename Geometry>
struct densify<Geometry, dynamic_geometry_tag>
{
    template <typename Distance, typename Strategy>
    static inline void
    apply(Geometry const& geometry,
          Geometry& out,
          Distance const& max_distance,
          Strategy const& strategy)
    {
        traits::visit<Geometry>::apply([&](auto const& g)
        {
            using geom_t = util::remove_cref_t<decltype(g)>;
            geom_t o;
            densify<geom_t>::apply(g, o, max_distance, strategy);
            out = std::move(o);
        }, geometry);
    }
};

template <typename Geometry>
struct densify<Geometry, geometry_collection_tag>
{
    template <typename Distance, typename Strategy>
    static inline void
    apply(Geometry const& geometry,
          Geometry& out,
          Distance const& max_distance,
          Strategy const& strategy)
    {
        detail::visit_breadth_first([&](auto const& g)
        {
            using geom_t = util::remove_cref_t<decltype(g)>;
            geom_t o;
            densify<geom_t>::apply(g, o, max_distance, strategy);
            traits::emplace_back<Geometry>::apply(out, std::move(o));
            return true;
        }, geometry);
    }
};

} // namespace resolve_dynamic


/*!
\brief Densify a geometry using a specified strategy
\ingroup densify
\tparam Geometry \tparam_geometry
\tparam Distance A numerical distance measure
\tparam Strategy A type fulfilling a DensifyStrategy concept
\param geometry Input geometry, to be densified
\param out Output geometry, densified version of the input geometry
\param max_distance Distance threshold (in units depending on strategy)
\param strategy Densify strategy to be used for densification

\qbk{distinguish,with strategy}
\qbk{[include reference/algorithms/densify.qbk]}

\qbk{
[heading Available Strategies]
\* [link geometry.reference.strategies.strategy_densify_cartesian Cartesian]
\* [link geometry.reference.strategies.strategy_densify_spherical Spherical]
\* [link geometry.reference.strategies.strategy_densify_geographic Geographic]

[heading Example]
[densify_strategy]
[densify_strategy_output]

[heading See also]
\* [link geometry.reference.algorithms.line_interpolate line_interpolate]
}
*/
template <typename Geometry, typename Distance, typename Strategy>
inline void densify(Geometry const& geometry,
                    Geometry& out,
                    Distance const& max_distance,
                    Strategy const& strategy)
{
    concepts::check<Geometry>();

    if (max_distance <= Distance(0))
    {
        BOOST_THROW_EXCEPTION(geometry::invalid_input_exception());
    }

    geometry::clear(out);

    resolve_dynamic::densify
        <
            Geometry
        >::apply(geometry, out, max_distance, strategy);
}


/*!
\brief Densify a geometry
\ingroup densify
\tparam Geometry \tparam_geometry
\tparam Distance A numerical distance measure
\param geometry Input geometry, to be densified
\param out Output geometry, densified version of the input geometry
\param max_distance Distance threshold (in units depending on coordinate system)

\qbk{[include reference/algorithms/densify.qbk]}

\qbk{
[heading Example]
[densify]
[densify_output]

[heading See also]
\* [link geometry.reference.algorithms.line_interpolate line_interpolate]
}
*/
template <typename Geometry, typename Distance>
inline void densify(Geometry const& geometry,
                    Geometry& out,
                    Distance const& max_distance)
{
    densify(geometry, out, max_distance, default_strategy());
}


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DENSIFY_HPP
