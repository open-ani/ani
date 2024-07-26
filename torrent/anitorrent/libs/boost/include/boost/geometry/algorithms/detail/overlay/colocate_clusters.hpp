// Boost.Geometry

// Copyright (c) 2023 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_COLOCATE_CLUSTERS_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_COLOCATE_CLUSTERS_HPP

#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/cs.hpp>
#include <boost/geometry/core/coordinate_type.hpp>
#include <boost/geometry/core/tags.hpp>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace overlay
{

// Default implementation, using the first point for all turns in the cluster.
template
<  
    typename Point,
    typename CoordinateType = typename geometry::coordinate_type<Point>::type,
    typename CsTag = typename geometry::cs_tag<Point>::type,
    bool IsIntegral = std::is_integral<CoordinateType>::value
>            
struct cluster_colocator 
{
    template <typename TurnIndices, typename Turns>
    static inline void apply(TurnIndices const& indices, Turns& turns)
    {
        // This approach works for all but one testcase (rt_p13)
        // The problem is fill_sbs, which uses sides and these sides might change slightly
        // depending on the exact location of the cluster.
        // Using the centroid is, on the average, a safer choice for sides.
        // Alternatively fill_sbs could be revised, but that requires a lot of work
        // and is outside current scope.
        // Integer coordinates are always colocated already and do not need centroid calculation.
        // Geographic/spherical coordinates might (in extremely rare cases) cross the date line
        // and therefore the first point is taken for them as well.
        auto it = indices.begin();
        auto const& first_point = turns[*it].point;
        for (++it; it != indices.end(); ++it)
        {
            turns[*it].point = first_point;
        }
    }
};

// Specialization for non-integral cartesian coordinates, calculating
// the centroid of the points of the turns in the cluster.
template <typename Point, typename CoordinateType>
struct cluster_colocator<Point, CoordinateType, geometry::cartesian_tag, false>
{
    template <typename TurnIndices, typename Turns>
    static inline void apply(TurnIndices const& indices, Turns& turns)
    {
        CoordinateType centroid_0 = 0;
        CoordinateType centroid_1 = 0;
        for (auto const& index : indices)
        {
            centroid_0 += geometry::get<0>(turns[index].point);
            centroid_1 += geometry::get<1>(turns[index].point);
        }
        centroid_0 /= indices.size();
        centroid_1 /= indices.size();
        for (auto const& index : indices)
        {
            geometry::set<0>(turns[index].point, centroid_0);
            geometry::set<1>(turns[index].point, centroid_1);
        }
    }
};

// Moves intersection points per cluster such that they are identical.
// Because clusters are intersection close together, and
// handled as one location. Then they should also have one location.
// It is necessary to avoid artefacts and invalidities.
template <typename Clusters, typename Turns>
inline void colocate_clusters(Clusters const& clusters, Turns& turns)
{
    for (auto const& pair : clusters)
    {
        auto const& turn_indices = pair.second.turn_indices;
        if (turn_indices.size() < 2)
        {
            // Defensive check
            continue;
        }
        using point_t = decltype(turns[*turn_indices.begin()].point);
        cluster_colocator<point_t>::apply(turn_indices, turns);
    }
}


}} // namespace detail::overlay
#endif //DOXYGEN_NO_DETAIL


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_OVERLAY_COLOCATE_CLUSTERS_HPP
