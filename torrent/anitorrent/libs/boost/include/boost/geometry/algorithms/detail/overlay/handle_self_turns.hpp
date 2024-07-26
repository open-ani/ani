// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2017 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2017-2023 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2019-2022.
// Modifications copyright (c) 2019-2022 Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_HANDLE_SELF_TURNS_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_HANDLE_SELF_TURNS_HPP

#include <boost/range/begin.hpp>
#include <boost/range/end.hpp>
#include <boost/range/value_type.hpp>

#include <boost/geometry/algorithms/detail/covered_by/implementation.hpp>
#include <boost/geometry/algorithms/detail/overlay/cluster_info.hpp>
#include <boost/geometry/algorithms/detail/overlay/is_self_turn.hpp>
#include <boost/geometry/algorithms/detail/overlay/overlay_type.hpp>
#include <boost/geometry/algorithms/detail/within/implementation.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace overlay
{

template <overlay_type OverlayType>
struct check_within
{
    template
    <
        typename Turn, typename Geometry0, typename Geometry1,
        typename UmbrellaStrategy
    >
    static inline
    bool apply(Turn const& turn, Geometry0 const& geometry0,
               Geometry1 const& geometry1, UmbrellaStrategy const& strategy)
    {
        // Operations 0 and 1 have the same source index in self-turns
        return turn.operations[0].seg_id.source_index == 0
            ? geometry::within(turn.point, geometry1, strategy)
            : geometry::within(turn.point, geometry0, strategy);
    }

};

template <>
struct check_within<overlay_difference>
{
    template
    <
        typename Turn, typename Geometry0, typename Geometry1,
        typename UmbrellaStrategy
    >
    static inline
    bool apply(Turn const& turn, Geometry0 const& geometry0,
               Geometry1 const& geometry1, UmbrellaStrategy const& strategy)
    {
        // difference = intersection(a, reverse(b))
        // therefore we should reverse the meaning of within for geometry1
        return turn.operations[0].seg_id.source_index == 0
            ? ! geometry::covered_by(turn.point, geometry1, strategy)
            : geometry::within(turn.point, geometry0, strategy);
    }
};

struct discard_turns
{
    template
    <
        typename Turns, typename Clusters,
        typename Geometry0, typename Geometry1,
        typename Strategy
    >
    static inline
    void apply(Turns& , Clusters const& ,
               Geometry0 const& , Geometry1 const& ,
               Strategy const& )
    {}
};

template <overlay_type OverlayType, operation_type OperationType>
struct discard_closed_turns : discard_turns {};

// It is only implemented for operation_union, not in buffer
template <>
struct discard_closed_turns<overlay_union, operation_union>
{
    // Point in Geometry Strategy
    template
    <
        typename Turns, typename Clusters,
        typename Geometry0, typename Geometry1,
        typename Strategy
    >
    static inline
    void apply(Turns& turns, Clusters const& /*clusters*/,
               Geometry0 const& geometry0, Geometry1 const& geometry1,
               Strategy const& strategy)
    {
        for (auto& turn : turns)
        {
            if (! turn.discarded
                && is_self_turn<overlay_union>(turn)
                && check_within<overlay_union>::apply(turn, geometry0,
                                                      geometry1, strategy))
            {
                // Turn is in the interior of other geometry
                turn.discarded = true;
            }
        }
    }
};

template <overlay_type OverlayType>
struct discard_self_intersection_turns
{
private :

    template <typename Turns, typename Clusters>
    static inline
    bool is_self_cluster(signed_size_type cluster_id,
            Turns const& turns, Clusters const& clusters)
    {
        auto cit = clusters.find(cluster_id);
        if (cit == clusters.end())
        {
            return false;
        }

        cluster_info const& cinfo = cit->second;
        for (auto index : cinfo.turn_indices)
        {
            if (! is_self_turn<OverlayType>(turns[index]))
            {
                return false;
            }
        }

        return true;
    }

    template <typename Turns, typename Clusters,
              typename Geometry0, typename Geometry1, typename Strategy>
    static inline
    void discard_clusters(Turns& turns, Clusters const& clusters,
            Geometry0 const& geometry0, Geometry1 const& geometry1,
            Strategy const& strategy)
    {
        for (auto const& pair : clusters)
        {
            signed_size_type const cluster_id = pair.first;
            cluster_info const& cinfo = pair.second;

            // If there are only self-turns in the cluster, the cluster should
            // be located within the other geometry, for intersection
            if (! cinfo.turn_indices.empty()
                && is_self_cluster(cluster_id, turns, clusters))
            {
                signed_size_type const first_index = *cinfo.turn_indices.begin();
                if (! check_within<OverlayType>::apply(turns[first_index],
                                                       geometry0, geometry1,
                                                       strategy))
                {
                    // Discard all turns in cluster
                    for (auto index : cinfo.turn_indices)
                    {
                        turns[index].discarded = true;
                    }
                }
            }
        }
    }

public :

    template <typename Turns, typename Clusters,
              typename Geometry0, typename Geometry1, typename Strategy>
    static inline
    void apply(Turns& turns, Clusters const& clusters,
            Geometry0 const& geometry0, Geometry1 const& geometry1,
            Strategy const& strategy)
    {
        discard_clusters(turns, clusters, geometry0, geometry1, strategy);

        for (auto& turn : turns)
        {
            // It is a ii self-turn
            // Check if it is within the other geometry
            if (! turn.discarded
                && is_self_turn<overlay_intersection>(turn)
                && ! check_within<OverlayType>::apply(turn, geometry0,
                                                      geometry1, strategy))
            {
                // It is not within another geometry, set it as non startable.
                // It still might be traveled (#case_recursive_boxes_70)
                turn.operations[0].enriched.startable = false;
                turn.operations[1].enriched.startable = false;
            }
        }
    }
};


template <overlay_type OverlayType, operation_type OperationType>
struct discard_open_turns : discard_turns {};

// Handler for intersection
template <>
struct discard_open_turns<overlay_intersection, operation_intersection>
        : discard_self_intersection_turns<overlay_intersection> {};

// Handler for difference, with different meaning of 'within'
template <>
struct discard_open_turns<overlay_difference, operation_intersection>
        : discard_self_intersection_turns<overlay_difference> {};

}} // namespace detail::overlay
#endif //DOXYGEN_NO_DETAIL


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_HANDLE_SELF_TURNS_HPP
