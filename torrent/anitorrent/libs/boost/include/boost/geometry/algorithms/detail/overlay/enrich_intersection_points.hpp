// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2017 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2017-2021.
// Modifications copyright (c) 2017-2020 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_ENRICH_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_ENRICH_HPP

#include <cstddef>
#include <algorithm>
#include <map>
#include <set>
#include <vector>

#ifdef BOOST_GEOMETRY_DEBUG_ENRICH
#  include <iostream>
#  include <boost/geometry/algorithms/detail/overlay/debug_turn_info.hpp>
#  include <boost/geometry/io/wkt/wkt.hpp>
#  if ! defined(BOOST_GEOMETRY_DEBUG_IDENTIFIER)
#    define BOOST_GEOMETRY_DEBUG_IDENTIFIER
  #endif
#endif

#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>
#include <boost/range/value_type.hpp>

#include <boost/geometry/algorithms/detail/ring_identifier.hpp>
#include <boost/geometry/algorithms/detail/overlay/discard_duplicate_turns.hpp>
#include <boost/geometry/algorithms/detail/overlay/handle_colocations.hpp>
#include <boost/geometry/algorithms/detail/overlay/handle_self_turns.hpp>
#include <boost/geometry/algorithms/detail/overlay/is_self_turn.hpp>
#include <boost/geometry/algorithms/detail/overlay/less_by_segment_ratio.hpp>
#include <boost/geometry/algorithms/detail/overlay/overlay_type.hpp>
#include <boost/geometry/policies/robustness/robust_type.hpp>
#include <boost/geometry/util/constexpr.hpp>
#include <boost/geometry/util/for_each_with_index.hpp>

#ifdef BOOST_GEOMETRY_DEBUG_ENRICH
#  include <boost/geometry/algorithms/detail/overlay/check_enrich.hpp>
#endif


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace overlay
{

template <typename Turns>
struct discarded_indexed_turn
{
    discarded_indexed_turn(Turns const& turns)
        : m_turns(turns)
    {}

    template <typename IndexedTurn>
    inline bool operator()(IndexedTurn const& indexed) const
    {
        return m_turns[indexed.turn_index].discarded;
    }

    Turns const& m_turns;
};

// Sorts IP-s of this ring on segment-identifier, and if on same segment,
//  on distance.
// Then assigns for each IP which is the next IP on this segment,
// plus the vertex-index to travel to, plus the next IP
// (might be on another segment)
template
<
    bool Reverse1, bool Reverse2,
    typename Operations,
    typename Turns,
    typename Geometry1, typename Geometry2,
    typename RobustPolicy,
    typename Strategy
>
inline void enrich_sort(Operations& operations,
            Turns const& turns,
            Geometry1 const& geometry1,
            Geometry2 const& geometry2,
            RobustPolicy const& robust_policy,
            Strategy const& strategy)
{
    std::sort(std::begin(operations),
              std::end(operations),
              less_by_segment_ratio
                <
                    Turns,
                    typename boost::range_value<Operations>::type,
                    Geometry1, Geometry2,
                    RobustPolicy,
                    Strategy,
                    Reverse1, Reverse2
                >(turns, geometry1, geometry2, robust_policy, strategy));
}


// Assign travel-to-vertex/ip index for each turn.
template <typename Operations, typename Turns>
inline void enrich_assign(Operations& operations, Turns& turns,
                          bool check_consecutive_turns)
{
    for_each_with_index(operations, [&](std::size_t index, auto const& indexed)
    {
        auto& turn = turns[indexed.turn_index];
        auto& op = turn.operations[indexed.operation_index];

        std::size_t next_index = index + 1 < operations.size() ? index + 1 : 0;
        auto advance = [&operations](auto index)
        {
            std::size_t const result = index + 1;
            return result >= operations.size() ? 0 : result;
        };

        auto next_turn = [&operations, &turns, &next_index]()
        {
            return turns[operations[next_index].turn_index];
        };
        auto next_operation = [&operations, &turns, &next_index]()
        {
            auto const& next_turn = turns[operations[next_index].turn_index];
            return next_turn.operations[operations[next_index].operation_index];
        };

        if (check_consecutive_turns
            && indexed.turn_index == operations[next_index].turn_index
            && op.seg_id == next_operation().seg_id)
        {
            // If the two operations on the same turn are ordered consecutively,
            // and they are on the same segment, then the turn where to travel to should
            // be considered one further. Therefore next is increased.
            //
            // It often happens in buffer, in these configurations:
            // +---->--+
            // |       |
            // |   +->-*---->
            // |   |   |
            // ^   +-<-+
            // If the next index is not corrected, the small rectangle
            // will be kept in the output.

            // This is a normal situation and occurs, for example, in every concave bend.
            // In general it should always travel from turn to next turn.
            // Only in some circumstances traveling to the same turn is necessary, for example
            // if there is only one turn in the outer ring.
            //
            // (For dissolve this is not done, turn_index is often
            // the same for two consecutive operations - but the conditions are changed
            // and this should be verified again)
            next_index = advance(next_index);
        }

        // Cluster behaviour: next should point after cluster, unless
        // their seg_ids are not the same
        // (For dissolve, this is still to be examined - TODO)
        while (turn.is_clustered()
                && turn.cluster_id == next_turn().cluster_id
                && op.seg_id == next_operation().seg_id
                && indexed.turn_index != operations[next_index].turn_index)
        {
            next_index = advance(next_index);
        }

        op.enriched.travels_to_ip_index
                = static_cast<signed_size_type>(operations[next_index].turn_index);
        op.enriched.travels_to_vertex_index
                = operations[next_index].subject->seg_id.segment_index;

        auto const& next_op = next_operation();
        if (op.seg_id.segment_index == next_op.seg_id.segment_index
            && op.fraction < next_op.fraction)
        {
            // Next turn is located further on same segment: assign next_ip_index
            op.enriched.next_ip_index = static_cast<signed_size_type>(operations[next_index].turn_index);
        }
    });

#ifdef BOOST_GEOMETRY_DEBUG_ENRICH
    for (auto const& indexed_op : operations)
    {
        auto const& op = turns[indexed_op.turn_index].operations[indexed_op.operation_index];

        std::cout << indexed_op.turn_index
            << " cl=" << turns[indexed_op.turn_index].cluster_id
            << " meth=" << method_char(turns[indexed_op.turn_index].method)
            << " seg=" << op.seg_id
            << " dst=" << op.fraction // needs define
            << " op=" << operation_char(turns[indexed_op.turn_index].operations[0].operation)
            << operation_char(turns[indexed_op.turn_index].operations[1].operation)
            << " (" << operation_char(op.operation) << ")"
            << " nxt=" << op.enriched.next_ip_index
            << " / " << op.enriched.travels_to_ip_index
            << " [vx " << op.enriched.travels_to_vertex_index << "]"
            << (turns[indexed_op.turn_index].discarded ? " [discarded]" : "")
            << (op.enriched.startable ? "" : " [not startable]")
            << std::endl;
    }
#endif
}

template <typename Operations, typename Turns>
inline void enrich_adapt(Operations& operations, Turns& turns)
{
    // Operations is a vector of indexed_turn_operation<>
    // If it is empty, or contains one or two items, it makes no sense
    if (operations.size() < 3)
    {
        return;
    }

    bool next_phase = false;
    std::size_t previous_index = operations.size() - 1;

    for_each_with_index(operations, [&](std::size_t index, auto const& indexed)
    {
        auto& turn = turns[indexed.turn_index];
        auto& op = turn.operations[indexed.operation_index];

        std::size_t const next_index = index + 1 < operations.size() ? index + 1 : 0;
        auto const& next_turn = turns[operations[next_index].turn_index];
        auto const& next_op = next_turn.operations[operations[next_index].operation_index];

        if (op.seg_id.segment_index == next_op.seg_id.segment_index)
        {
            auto const& prev_turn = turns[operations[previous_index].turn_index];
            auto const& prev_op = prev_turn.operations[operations[previous_index].operation_index];
            if (op.seg_id.segment_index == prev_op.seg_id.segment_index)
            {
                op.enriched.startable = false;
                next_phase = true;
            }
        }
        previous_index = index;
    });

    if (! next_phase)
    {
        return;
    }

    // Discard turns which are both non-startable
    next_phase = false;
    for (auto& turn : turns)
    {
        if (! turn.operations[0].enriched.startable
            && ! turn.operations[1].enriched.startable)
        {
            turn.discarded = true;
            next_phase = true;
        }
    }

    if (! next_phase)
    {
        return;
    }

    // Remove discarded turns from operations to avoid having them as next turn
    discarded_indexed_turn<Turns> const predicate(turns);
    operations.erase(std::remove_if(std::begin(operations),
        std::end(operations), predicate), std::end(operations));
}

struct enriched_map_default_include_policy
{
    template <typename Operation>
    static inline bool include(Operation const& )
    {
        // By default include all operations
        return true;
    }
};

// Add all (non discarded) operations on this ring
// Blocked operations or uu on clusters (for intersection)
// should be included, to block potential paths in clusters
template <typename Turns, typename MappedVector, typename IncludePolicy>
inline void create_map(Turns const& turns, MappedVector& mapped_vector,
                       IncludePolicy const& include_policy)
{
    for_each_with_index(turns, [&](std::size_t index, auto const& turn)
    {
        if (! turn.discarded)
        {
            for_each_with_index(turn.operations, [&](std::size_t op_index, auto const& op)
            {
                if (include_policy.include(op.operation))
                {
                    ring_identifier const ring_id
                        (
                            op.seg_id.source_index,
                            op.seg_id.multi_index,
                            op.seg_id.ring_index
                        );
                    mapped_vector[ring_id].emplace_back
                        (
                            index, op_index, op, turn.operations[1 - op_index].seg_id
                        );
                }
            });
        }
    });
}

template <typename Point1, typename Point2>
inline typename geometry::coordinate_type<Point1>::type
        distance_measure(Point1 const& a, Point2 const& b)
{
    // TODO: use comparable distance for point-point instead - but that
    // causes currently cycling include problems
    using ctype = typename geometry::coordinate_type<Point1>::type;
    ctype const dx = get<0>(a) - get<0>(b);
    ctype const dy = get<1>(a) - get<1>(b);
    return dx * dx + dy * dy;
}

template <typename Turns>
inline void calculate_remaining_distance(Turns& turns)
{
    for (auto& turn : turns)
    {
        auto& op0 = turn.operations[0];
        auto& op1 = turn.operations[1];

        static decltype(op0.remaining_distance) const zero_distance = 0;

        if (op0.remaining_distance != zero_distance
            || op1.remaining_distance != zero_distance)
        {
            continue;
        }

        auto const to_index0 = op0.enriched.get_next_turn_index();
        auto const to_index1 = op1.enriched.get_next_turn_index();
        if (to_index0 >= 0
            && to_index1 >= 0
            && to_index0 != to_index1)
        {
            op0.remaining_distance = distance_measure(turn.point, turns[to_index0].point);
            op1.remaining_distance = distance_measure(turn.point, turns[to_index1].point);
        }
    }
}


}} // namespace detail::overlay
#endif //DOXYGEN_NO_DETAIL



/*!
\brief All intersection points are enriched with successor information
\ingroup overlay
\tparam Turns type of intersection container
            (e.g. vector of "intersection/turn point"'s)
\tparam Clusters type of cluster container
\tparam Geometry1 \tparam_geometry
\tparam Geometry2 \tparam_geometry
\tparam PointInGeometryStrategy point in geometry strategy type
\param turns container containing intersection points
\param clusters container containing clusters
\param geometry1 \param_geometry
\param geometry2 \param_geometry
\param robust_policy policy to handle robustness issues
\param strategy point in geometry strategy
 */
template
<
    bool Reverse1, bool Reverse2,
    overlay_type OverlayType,
    typename Turns,
    typename Clusters,
    typename Geometry1, typename Geometry2,
    typename RobustPolicy,
    typename IntersectionStrategy
>
inline void enrich_intersection_points(Turns& turns,
    Clusters& clusters,
    Geometry1 const& geometry1, Geometry2 const& geometry2,
    RobustPolicy const& robust_policy,
    IntersectionStrategy const& strategy)
{
    constexpr detail::overlay::operation_type target_operation
            = detail::overlay::operation_from_overlay<OverlayType>::value;
    constexpr detail::overlay::operation_type opposite_operation
            = target_operation == detail::overlay::operation_union
            ? detail::overlay::operation_intersection
            : detail::overlay::operation_union;
    constexpr bool is_dissolve = OverlayType == overlay_dissolve;
    constexpr bool is_buffer = OverlayType == overlay_buffer;

    using turn_type = typename boost::range_value<Turns>::type;
    using indexed_turn_operation = detail::overlay::indexed_turn_operation
        <
            typename turn_type::turn_operation_type
        > ;

    using mapped_vector_type = std::map
        <
            ring_identifier,
            std::vector<indexed_turn_operation>
        >;

    // Turns are often used by index (in clusters, next_index, etc)
    // and turns may therefore NOT be DELETED - they may only be flagged as discarded

    bool has_cc = false;
    bool has_colocations = false;

    if BOOST_GEOMETRY_CONSTEXPR (! is_buffer)
    {
        // Handle colocations, gathering clusters and (below) their properties.
        has_colocations = detail::overlay::handle_colocations
                    <
                        Reverse1, Reverse2, OverlayType, Geometry1, Geometry2
                    >(turns, clusters, robust_policy);
        // Gather cluster properties (using even clusters with
        // discarded turns - for open turns)
        detail::overlay::gather_cluster_properties
            <
                Reverse1,
                Reverse2,
                OverlayType
            >(clusters, turns, target_operation,
            geometry1, geometry2, strategy);
    }
    else
    {
        // For buffer, this was already done before calling enrich_intersection_points.
        has_colocations = ! clusters.empty();
    }

    discard_duplicate_start_turns(turns, geometry1, geometry2);

    // Discard turns not part of target overlay
    for (auto& turn : turns)
    {
        if (turn.both(detail::overlay::operation_none)
            || turn.both(opposite_operation)
            || turn.both(detail::overlay::operation_blocked)
            || (detail::overlay::is_self_turn<OverlayType>(turn)
                && ! turn.is_clustered()
                && ! turn.both(target_operation)))
        {
            // For all operations, discard xx and none/none
            // For intersections, remove uu to avoid the need to travel
            // a union (during intersection) in uu/cc clusters (e.g. #31,#32,#33)
            // The ux is necessary to indicate impossible paths
            // (especially if rescaling is removed)

            // Similarly, for union, discard ii and ix

            // For self-turns, only keep uu / ii

            turn.discarded = true;
            turn.cluster_id = -1;
            continue;
        }

        if (! turn.discarded
            && turn.both(detail::overlay::operation_continue))
        {
            has_cc = true;
        }
    }

    if (! is_dissolve)
    {
        detail::overlay::discard_closed_turns
            <
                OverlayType,
                target_operation
            >::apply(turns, clusters, geometry1, geometry2,
                     strategy);
        detail::overlay::discard_open_turns
            <
                OverlayType,
                target_operation
            >::apply(turns, clusters, geometry1, geometry2,
                     strategy);
    }

    // Create a map of vectors of indexed operation-types to be able
    // to sort intersection points PER RING
    mapped_vector_type mapped_vector;

    detail::overlay::create_map(turns, mapped_vector,
                                detail::overlay::enriched_map_default_include_policy());

    for (auto& pair : mapped_vector)
    {
        detail::overlay::enrich_sort<Reverse1, Reverse2>(
                    pair.second, turns,
                    geometry1, geometry2,
                    robust_policy, strategy);
    }

    if (has_colocations)
    {
        detail::overlay::cleanup_clusters(turns, clusters);
        detail::overlay::colocate_clusters(clusters, turns);
    }

    // After cleaning up clusters assign the next turns

    for (auto& pair : mapped_vector)
    {
#ifdef BOOST_GEOMETRY_DEBUG_ENRICH
    std::cout << "ENRICH-assign Ring " << pair.first << std::endl;
#endif
        if (is_dissolve)
        {
            detail::overlay::enrich_adapt(pair.second, turns);
        }

        detail::overlay::enrich_assign(pair.second, turns, ! is_dissolve);
    }

    if (has_cc)
    {
        detail::overlay::calculate_remaining_distance(turns);
    }

#ifdef BOOST_GEOMETRY_DEBUG_ENRICH
    //detail::overlay::check_graph(turns, for_operation);
#endif

}

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_ENRICH_HPP
