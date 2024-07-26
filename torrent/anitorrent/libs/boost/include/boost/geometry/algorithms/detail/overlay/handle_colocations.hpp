// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2017-2023 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2017-2020.
// Modifications copyright (c) 2017-2020 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_HANDLE_COLOCATIONS_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_HANDLE_COLOCATIONS_HPP

#include <cstddef>
#include <algorithm>
#include <map>
#include <vector>

#include <boost/core/ignore_unused.hpp>
#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>
#include <boost/range/value_type.hpp>

#include <boost/geometry/core/assert.hpp>
#include <boost/geometry/core/point_order.hpp>
#include <boost/geometry/algorithms/detail/overlay/cluster_info.hpp>
#include <boost/geometry/algorithms/detail/overlay/do_reverse.hpp>
#include <boost/geometry/algorithms/detail/overlay/colocate_clusters.hpp>
#include <boost/geometry/algorithms/detail/overlay/get_clusters.hpp>
#include <boost/geometry/algorithms/detail/overlay/get_ring.hpp>
#include <boost/geometry/algorithms/detail/overlay/is_self_turn.hpp>
#include <boost/geometry/algorithms/detail/overlay/overlay_type.hpp>
#include <boost/geometry/algorithms/detail/overlay/sort_by_side.hpp>
#include <boost/geometry/algorithms/detail/overlay/turn_info.hpp>
#include <boost/geometry/algorithms/detail/overlay/segment_identifier.hpp>
#include <boost/geometry/util/constexpr.hpp>

#if defined(BOOST_GEOMETRY_DEBUG_HANDLE_COLOCATIONS)
#  include <iostream>
#  include <boost/geometry/algorithms/detail/overlay/debug_turn_info.hpp>
#  include <boost/geometry/io/wkt/wkt.hpp>
#  define BOOST_GEOMETRY_DEBUG_IDENTIFIER
#endif

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace overlay
{

// Removes clusters which have only one point left, or are empty.
template <typename Turns, typename Clusters>
inline void remove_clusters(Turns& turns, Clusters& clusters)
{
    auto it = clusters.begin();
    while (it != clusters.end())
    {
        // Hold iterator and increase. We can erase cit, this keeps the
        // iterator valid (cf The standard associative-container erase idiom)
        auto current_it = it;
        ++it;

        auto const& turn_indices = current_it->second.turn_indices;
        if (turn_indices.size() == 1)
        {
            auto const turn_index = *turn_indices.begin();
            turns[turn_index].cluster_id = -1;
            clusters.erase(current_it);
        }
    }
}

template <typename Turns, typename Clusters>
inline void cleanup_clusters(Turns& turns, Clusters& clusters)
{
    // Removes discarded turns from clusters
    for (auto& pair : clusters)
    {
        auto& cinfo = pair.second;
        auto& indices = cinfo.turn_indices;
        for (auto sit = indices.begin(); sit != indices.end(); /* no increment */)
        {
            auto current_it = sit;
            ++sit;

            auto const turn_index = *current_it;
            if (turns[turn_index].discarded)
            {
                indices.erase(current_it);
            }
        }
    }

    remove_clusters(turns, clusters);
}

template <typename Turn, typename IndexSet>
inline void discard_colocated_turn(Turn& turn, IndexSet& indices, signed_size_type index)
{
    turn.discarded = true;
    // Set cluster id to -1, but don't clear colocated flags
    turn.cluster_id = -1;
    // To remove it later from clusters
    indices.insert(index);
}

template <bool Reverse>
inline bool is_interior(segment_identifier const& seg_id)
{
    return Reverse ? seg_id.ring_index == -1 : seg_id.ring_index >= 0;
}

template <bool Reverse0, bool Reverse1>
inline bool is_ie_turn(segment_identifier const& ext_seg_0,
                       segment_identifier const& ext_seg_1,
                       segment_identifier const& int_seg_0,
                       segment_identifier const& other_seg_1)
{
    if (ext_seg_0.source_index == ext_seg_1.source_index)
    {
        // External turn is a self-turn, dont discard internal turn for this
        return false;
    }


    // Compares two segment identifiers from two turns (external / one internal)

    // From first turn [0], both are from same polygon (multi_index),
    // one is exterior (-1), the other is interior (>= 0),
    // and the second turn [1] handles the same ring

    // For difference, where the rings are processed in reversal, all interior
    // rings become exterior and vice versa. But also the multi property changes:
    // rings originally from the same multi should now be considered as from
    // different multi polygons.
    // But this is not always the case, and at this point hard to figure out
    // (not yet implemented, TODO)

    bool const same_multi0 = ! Reverse0
                             && ext_seg_0.multi_index == int_seg_0.multi_index;

    bool const same_multi1 = ! Reverse1
                             && ext_seg_1.multi_index == other_seg_1.multi_index;

    boost::ignore_unused(same_multi1);

    return same_multi0
            && same_multi1
            && ! is_interior<Reverse0>(ext_seg_0)
            && is_interior<Reverse0>(int_seg_0)
            && ext_seg_1.ring_index == other_seg_1.ring_index;

    // The other way round is tested in another call
}

template
<
    bool Reverse0, bool Reverse1, // Reverse interpretation interior/exterior
    typename Turns,
    typename Clusters
>
inline void discard_interior_exterior_turns(Turns& turns, Clusters& clusters)
{
    std::set<signed_size_type> indices_to_remove;

    for (auto& pair : clusters)
    {
        cluster_info& cinfo = pair.second;

        indices_to_remove.clear();

        for (auto index : cinfo.turn_indices)
        {
            auto& turn = turns[index];
            segment_identifier const& seg_0 = turn.operations[0].seg_id;
            segment_identifier const& seg_1 = turn.operations[1].seg_id;

            if (! (turn.both(operation_union)
                   || turn.combination(operation_union, operation_blocked)))
            {
                // Not a uu/ux, so cannot be colocated with a iu turn
                continue;
            }

            for (auto interior_index : cinfo.turn_indices)
            {
                if (index == interior_index)
                {
                    continue;
                }

                // Turn with, possibly, an interior ring involved
                auto& interior_turn = turns[interior_index];
                segment_identifier const& int_seg_0 = interior_turn.operations[0].seg_id;
                segment_identifier const& int_seg_1 = interior_turn.operations[1].seg_id;

                if (is_ie_turn<Reverse0, Reverse1>(seg_0, seg_1, int_seg_0, int_seg_1))
                {
                    discard_colocated_turn(interior_turn, indices_to_remove, interior_index);
                }
                if (is_ie_turn<Reverse1, Reverse0>(seg_1, seg_0, int_seg_1, int_seg_0))
                {
                    discard_colocated_turn(interior_turn, indices_to_remove, interior_index);
                }
            }
        }

        // Erase from the indices (which cannot be done above)
        for (auto index : indices_to_remove)
        {
            cinfo.turn_indices.erase(index);
        }
    }
}

template
<
    overlay_type OverlayType,
    typename Turns,
    typename Clusters
>
inline void set_colocation(Turns& turns, Clusters const& clusters)
{
    for (auto const& pair : clusters)
    {
        cluster_info const& cinfo = pair.second;

        bool both_target = false;
        for (auto index : cinfo.turn_indices)
        {
            auto const& turn = turns[index];
            if (turn.both(operation_from_overlay<OverlayType>::value))
            {
                both_target = true;
                break;
            }
        }

        if (both_target)
        {
            for (auto index : cinfo.turn_indices)
            {
                auto& turn = turns[index];
                turn.has_colocated_both = true;
            }
        }
    }
}

template
<
    typename Turns,
    typename Clusters
>
inline void check_colocation(bool& has_blocked,
        signed_size_type cluster_id, Turns const& turns, Clusters const& clusters)
{
    typedef typename boost::range_value<Turns>::type turn_type;

    has_blocked = false;

    auto mit = clusters.find(cluster_id);
    if (mit == clusters.end())
    {
        return;
    }

    cluster_info const& cinfo = mit->second;

    for (auto index : cinfo.turn_indices)
    {
        turn_type const& turn = turns[index];
        if (turn.any_blocked())
        {
            has_blocked = true;
        }
    }
}

template
<
    typename Turns,
    typename Clusters
>
inline void assign_cluster_ids(Turns& turns, Clusters const& clusters)
{
    for (auto& turn : turns)
    {
        turn.cluster_id = -1;
    }
    for (auto const& kv : clusters)
    {
        for (auto const& index : kv.second.turn_indices)
        {
            turns[index].cluster_id = kv.first;
        }
    }
}

// Checks colocated turns and flags combinations of uu/other, possibly a
// combination of a ring touching another geometry's interior ring which is
// tangential to the exterior ring

// This function can be extended to replace handle_tangencies: at each
// colocation incoming and outgoing vectors should be inspected

template
<
    bool Reverse1, bool Reverse2,
    overlay_type OverlayType,
    typename Geometry0,
    typename Geometry1,
    typename Turns,
    typename Clusters,
    typename RobustPolicy
>
inline bool handle_colocations(Turns& turns, Clusters& clusters,
                               RobustPolicy const& robust_policy)
{
    static const detail::overlay::operation_type target_operation
            = detail::overlay::operation_from_overlay<OverlayType>::value;

    get_clusters(turns, clusters, robust_policy);

    if (clusters.empty())
    {
        return false;
    }

    assign_cluster_ids(turns, clusters);

    // Get colocated information here, and not later, to keep information
    // on turns which are discarded afterwards
    set_colocation<OverlayType>(turns, clusters);

    if BOOST_GEOMETRY_CONSTEXPR (target_operation == operation_intersection)
    {
        discard_interior_exterior_turns
            <
                do_reverse<geometry::point_order<Geometry0>::value>::value != Reverse1,
                do_reverse<geometry::point_order<Geometry1>::value>::value != Reverse2
            >(turns, clusters);
    }

    // There might be clusters having only one turn, if the rest is discarded
    // This is cleaned up later, after gathering the properties.

#if defined(BOOST_GEOMETRY_DEBUG_HANDLE_COLOCATIONS)
    std::cout << "*** Colocations " << map.size() << std::endl;
    for (auto const& kv : map)
    {
        std::cout << kv.first << std::endl;
        for (auto const& toi : kv.second)
        {
            detail::debug::debug_print_turn(turns[toi.turn_index]);
            std::cout << std::endl;
        }
    }
#endif

    return true;
}


struct is_turn_index
{
    is_turn_index(signed_size_type index)
        : m_index(index)
    {}

    template <typename Indexed>
    inline bool operator()(Indexed const& indexed) const
    {
        // Indexed is a indexed_turn_operation<Operation>
        return indexed.turn_index == m_index;
    }

    signed_size_type m_index;
};

template
<
    typename Sbs,
    typename Point,
    typename Turns,
    typename Geometry1,
    typename Geometry2
>
inline bool fill_sbs(Sbs& sbs, Point& turn_point,
                     cluster_info const& cinfo,
                     Turns const& turns,
                     Geometry1 const& geometry1, Geometry2 const& geometry2)
{
    if (cinfo.turn_indices.empty())
    {
        return false;
    }

    bool first = true;
    for (auto turn_index : cinfo.turn_indices)
    {
        auto const& turn = turns[turn_index];
        if (first)
        {
            turn_point = turn.point;
        }
        for (int i = 0; i < 2; i++)
        {
            sbs.add(turn, turn.operations[i], turn_index, i, geometry1, geometry2, first);
            first = false;
        }
    }
    return true;
}

template
<
    bool Reverse1, bool Reverse2,
    overlay_type OverlayType,
    typename Turns,
    typename Clusters,
    typename Geometry1,
    typename Geometry2,
    typename Strategy
>
inline void gather_cluster_properties(Clusters& clusters, Turns& turns,
        operation_type for_operation,
        Geometry1 const& geometry1, Geometry2 const& geometry2,
        Strategy const& strategy)
{
    typedef typename boost::range_value<Turns>::type turn_type;
    typedef typename turn_type::point_type point_type;
    typedef typename turn_type::turn_operation_type turn_operation_type;

    // Define sorter, sorting counter-clockwise such that polygons are on the
    // right side
    typedef sort_by_side::side_sorter
        <
            Reverse1, Reverse2, OverlayType, point_type, Strategy, std::less<int>
        > sbs_type;

    for (auto& pair : clusters)
    {
        cluster_info& cinfo = pair.second;

        sbs_type sbs(strategy);
        point_type turn_point; // should be all the same for all turns in cluster
        if (! fill_sbs(sbs, turn_point, cinfo, turns, geometry1, geometry2))
        {
            continue;
        }

        sbs.apply(turn_point);

        sbs.find_open();
        sbs.assign_zones(for_operation);

        cinfo.open_count = sbs.open_count(for_operation);

        // Determine spikes
        cinfo.spike_count = 0;
        for (std::size_t i = 0; i + 1 < sbs.m_ranked_points.size(); i++)
        {
            auto const& current = sbs.m_ranked_points[i];
            auto const& next = sbs.m_ranked_points[i + 1];
            if (current.rank == next.rank
                && current.direction == detail::overlay::sort_by_side::dir_from
                && next.direction == detail::overlay::sort_by_side::dir_to)
            {
                // It leaves, from cluster point, and immediately returns.
                cinfo.spike_count += 1;
            }
        }

        bool const set_startable = OverlayType != overlay_dissolve;

        // Unset the startable flag for all 'closed' zones. This does not
        // apply for self-turns, because those counts are not from both
        // polygons
        for (std::size_t i = 0; i < sbs.m_ranked_points.size(); i++)
        {
            typename sbs_type::rp const& ranked = sbs.m_ranked_points[i];
            turn_type& turn = turns[ranked.turn_index];
            turn_operation_type& op = turn.operations[ranked.operation_index];

            if (set_startable
                && for_operation == operation_union
                && cinfo.open_count == 0)
            {
                op.enriched.startable = false;
            }

            if (ranked.direction != sort_by_side::dir_to)
            {
                continue;
            }

            op.enriched.count_left = ranked.count_left;
            op.enriched.count_right = ranked.count_right;
            op.enriched.rank = ranked.rank;
            op.enriched.zone = ranked.zone;

            if (! set_startable)
            {
                continue;
            }

            if BOOST_GEOMETRY_CONSTEXPR (OverlayType == overlay_difference)
            {
                if (is_self_turn<OverlayType>(turn))
                {
                    // TODO: investigate
                    continue;
                }
            }

            if ((for_operation == operation_union
                    && ranked.count_left != 0)
             || (for_operation == operation_intersection
                    && ranked.count_right != 2))
            {
                op.enriched.startable = false;
            }
        }

    }
}


}} // namespace detail::overlay
#endif //DOXYGEN_NO_DETAIL


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_HANDLE_COLOCATIONS_HPP
