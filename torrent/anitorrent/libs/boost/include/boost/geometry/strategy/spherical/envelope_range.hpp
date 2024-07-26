// Boost.Geometry

// Copyright (c) 2021-2022, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGY_SPHERICAL_ENVELOPE_RANGE_HPP
#define BOOST_GEOMETRY_STRATEGY_SPHERICAL_ENVELOPE_RANGE_HPP

#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/assign.hpp>
#include <boost/geometry/algorithms/detail/envelope/initialize.hpp>
#include <boost/geometry/geometries/segment.hpp>
#include <boost/geometry/strategy/spherical/envelope_point.hpp>
#include <boost/geometry/strategy/spherical/envelope_segment.hpp>
#include <boost/geometry/strategy/spherical/expand_segment.hpp>
#include <boost/geometry/views/closeable_view.hpp>

// Get rid of this dependency?
#include <boost/geometry/strategies/spherical/point_in_poly_winding.hpp>


namespace boost { namespace geometry
{

namespace strategy { namespace envelope
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail
{

template <typename Range, typename Box, typename EnvelopeStrategy, typename ExpandStrategy>
inline void spheroidal_linestring(Range const& range, Box& mbr,
                                  EnvelopeStrategy const& envelope_strategy,
                                  ExpandStrategy const& expand_strategy)
{
    auto it = boost::begin(range);
    auto const end = boost::end(range);
    if (it == end)
    {
        // initialize box (assign inverse)
        geometry::detail::envelope::initialize<Box>::apply(mbr);
        return;
    }

    auto prev = it;
    ++it;
    if (it == end)
    {
        // initialize box with the first point
        envelope::spherical_point::apply(*prev, mbr);
        return;
    }

    // initialize box with the first segment
    envelope_strategy.apply(*prev, *it, mbr);

    // consider now the remaining segments in the range (if any)
    prev = it;
    ++it;
    while (it != end)
    {
        using point_t = typename boost::range_value<Range>::type;
        geometry::model::referring_segment<point_t const> const seg(*prev, *it);
        expand_strategy.apply(mbr, seg);
        prev = it;
        ++it;
    }
}


// This strategy is intended to be used together with winding strategy to check
// if ring/polygon has a pole in its interior or exterior. It is not intended
// for checking if the pole is on the boundary.
template <typename CalculationType = void>
struct side_of_pole
{
    typedef spherical_tag cs_tag;

    template <typename P>
    static inline int apply(P const& p1, P const& p2, P const& pole)
    {
        using calc_t = typename promote_floating_point
            <
                typename select_calculation_type_alt
                    <
                        CalculationType, P
                    >::type
            >::type;

        using units_t = typename geometry::detail::cs_angular_units<P>::type;
        using constants = math::detail::constants_on_spheroid<calc_t, units_t>;

        calc_t const c0 = 0;
        calc_t const pi = constants::half_period();

        calc_t const lon1 = get<0>(p1);
        calc_t const lat1 = get<1>(p1);
        calc_t const lon2 = get<0>(p2);
        calc_t const lat2 = get<1>(p2);
        calc_t const lat_pole = get<1>(pole);

        calc_t const s_lon_diff = math::longitude_distance_signed<units_t>(lon1, lon2);
        bool const s_vertical = math::equals(s_lon_diff, c0)
                             || math::equals(s_lon_diff, pi);
        // Side of vertical segment is 0 for both poles.
        if (s_vertical)
        {
            return 0;
        }

        // This strategy shouldn't be called in this case but just in case
        // check if segment starts at a pole
        if (math::equals(lat_pole, lat1) || math::equals(lat_pole, lat2))
        {
            return 0;
        }

        // -1 is rhs
        //  1 is lhs
        if (lat_pole >= c0) // north pole
        {
            return s_lon_diff < c0 ? -1 : 1;
        }
        else // south pole
        {
            return s_lon_diff > c0 ? -1 : 1;
        }
    }
};


template <typename Point, typename Range, typename Strategy>
inline int point_in_range(Point const& point, Range const& range, Strategy const& strategy)
{
    typename Strategy::state_type state;

    auto it = boost::begin(range);
    auto const end = boost::end(range);
    for (auto previous = it++ ; it != end ; ++previous, ++it )
    {
        if (! strategy.apply(point, *previous, *it, state))
        {
            break;
        }
    }

    return strategy.result(state);
}


template <typename T, typename Ring, typename PoleWithinStrategy>
inline bool pole_within(T const& lat_pole, Ring const& ring,
                        PoleWithinStrategy const& pole_within_strategy)
{
    if (boost::size(ring) < core_detail::closure::minimum_ring_size
                                <
                                    geometry::closure<Ring>::value
                                >::value)
    {
        return false;
    }

    using point_t = typename geometry::point_type<Ring>::type;
    point_t point;
    geometry::assign_zero(point);
    geometry::set<1>(point, lat_pole);
    geometry::detail::closed_clockwise_view<Ring const> view(ring);
    return point_in_range(point, view, pole_within_strategy) > 0;
}

template
<
    typename Range,
    typename Box,
    typename EnvelopeStrategy,
    typename ExpandStrategy,
    typename PoleWithinStrategy
>
inline void spheroidal_ring(Range const& range, Box& mbr,
                            EnvelopeStrategy const& envelope_strategy,
                            ExpandStrategy const& expand_strategy,
                            PoleWithinStrategy const& pole_within_strategy)
{
    geometry::detail::closed_view<Range const> closed_range(range);

    spheroidal_linestring(closed_range, mbr, envelope_strategy, expand_strategy);

    using coord_t = typename geometry::coordinate_type<Box>::type;
    using point_t = typename geometry::point_type<Box>::type;
    using units_t = typename geometry::detail::cs_angular_units<point_t>::type;
    using constants_t = math::detail::constants_on_spheroid<coord_t, units_t>;
    coord_t const two_pi = constants_t::period();
    coord_t const lon_min = geometry::get<0, 0>(mbr);
    coord_t const lon_max = geometry::get<1, 0>(mbr);
    // If box covers the whole longitude range it is possible that the ring contains
    // one of the poles.
    // Technically it is possible that a reversed ring may cover more than
    // half of the globe and mbr of it's linear ring may be small and not cover the
    // longitude range. We currently don't support such rings.
    if (lon_max - lon_min >= two_pi)
    {
        coord_t const lat_n_pole = constants_t::max_latitude();
        coord_t const lat_s_pole = constants_t::min_latitude();
        coord_t lat_min = geometry::get<0, 1>(mbr);
        coord_t lat_max = geometry::get<1, 1>(mbr);
        // Normalize box latitudes, just in case
        if (math::equals(lat_min, lat_s_pole))
        {
            lat_min = lat_s_pole;
        }
        if (math::equals(lat_max, lat_n_pole))
        {
            lat_max = lat_n_pole;
        }

        if (lat_max < lat_n_pole)
        {
            if (pole_within(lat_n_pole, range, pole_within_strategy))
            {
                lat_max = lat_n_pole;
            }
        }
        if (lat_min > lat_s_pole)
        {
            if (pole_within(lat_s_pole, range, pole_within_strategy))
            {
                lat_min = lat_s_pole;
            }
        }

        geometry::set<0, 1>(mbr, lat_min);
        geometry::set<1, 1>(mbr, lat_max);
    }
}

} // namespace detail
#endif // DOXYGEN_NO_DETAIL

template <typename CalculationType = void>
class spherical_linestring
{
public:
    template <typename Range, typename Box>
    static inline void apply(Range const& range, Box& mbr)
    {
        detail::spheroidal_linestring(range, mbr,
                                      envelope::spherical_segment<CalculationType>(),
                                      expand::spherical_segment<CalculationType>());
    }
};

template <typename CalculationType = void>
class spherical_ring
{
public:
    template <typename Range, typename Box>
    static inline void apply(Range const& range, Box& mbr)
    {
        detail::spheroidal_ring(range, mbr,
                                envelope::spherical_segment<CalculationType>(),
                                expand::spherical_segment<CalculationType>(),
                                within::detail::spherical_winding_base
                                    <
                                        envelope::detail::side_of_pole<CalculationType>,
                                        CalculationType
                                    >());
    }
};

}} // namespace strategy::envelope

}} //namepsace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGY_SPHERICAL_ENVELOPE_RANGE_HPP
