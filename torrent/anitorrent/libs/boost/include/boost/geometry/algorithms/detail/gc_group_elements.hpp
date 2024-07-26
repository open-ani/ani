// Boost.Geometry

// Copyright (c) 2022, Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_GROUP_ELEMENTS_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_GROUP_ELEMENTS_HPP


#include <array>
#include <deque>
#include <map>
#include <set>
#include <vector>

#include <boost/range/begin.hpp>
#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/detail/gc_make_rtree.hpp>
#include <boost/geometry/algorithms/detail/visit.hpp>
#include <boost/geometry/algorithms/is_empty.hpp>
#include <boost/geometry/core/topological_dimension.hpp>
#include <boost/geometry/views/detail/random_access_view.hpp>



namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail
{


struct gc_element_id
{
    gc_element_id(unsigned int source_id_, std::size_t gc_id_)
        : source_id(source_id_), gc_id(gc_id_)
    {}

    unsigned int source_id;
    std::size_t gc_id;

    friend bool operator<(gc_element_id const& left, gc_element_id const& right)
    {
        return left.source_id < right.source_id
            || (left.source_id == right.source_id && left.gc_id < right.gc_id);
    }
};

template <typename GC1View, typename GC2View, typename Strategy, typename IntersectingFun, typename DisjointFun>
inline void gc_group_elements(GC1View const& gc1_view, GC2View const& gc2_view, Strategy const& strategy,
                              IntersectingFun&& intersecting_fun,
                              DisjointFun&& disjoint_fun,
                              bool const group_self = false)
{
    // NOTE: gc_make_rtree_indexes() already checks for random-access,
    //   non-recursive geometry collections.

    // NOTE: could be replaced with unordered_map and unordered_set
    std::map<gc_element_id, std::set<gc_element_id>> adjacent;

    // Create adjacency list based on intersecting envelopes of GC elements
    auto const rtree2 = gc_make_rtree_indexes(gc2_view, strategy);
    if (! group_self) // group only elements from the other GC?
    {
        for (std::size_t i = 0; i < boost::size(gc1_view); ++i)
        {
            traits::iter_visit<GC1View>::apply([&](auto const& g1)
            {
                using g1_t = util::remove_cref_t<decltype(g1)>;
                using box1_t = gc_make_rtree_box_t<g1_t>;
                box1_t b1 = geometry::return_envelope<box1_t>(g1, strategy);
                detail::expand_by_epsilon(b1);

                gc_element_id id1 = {0, i};
                for (auto qit = rtree2.qbegin(index::intersects(b1)); qit != rtree2.qend(); ++qit)
                {
                    gc_element_id id2 = {1, qit->second};
                    adjacent[id1].insert(id2);
                    adjacent[id2].insert(id1);
                }
            }, boost::begin(gc1_view) + i);
        }
    }
    else // group elements from the same GCs and the other GC
    {
        auto const rtree1 = gc_make_rtree_indexes(gc1_view, strategy);
        for (auto it1 = rtree1.begin() ; it1 != rtree1.end() ; ++it1)
        {
            auto const b1 = it1->first;
            gc_element_id id1 = {0, it1->second};
            for (auto qit2 = rtree2.qbegin(index::intersects(b1)); qit2 != rtree2.qend(); ++qit2)
            {
                gc_element_id id2 = {1, qit2->second};
                adjacent[id1].insert(id2);
                adjacent[id2].insert(id1);
            }
            for (auto qit1 = rtree1.qbegin(index::intersects(b1)); qit1 != rtree1.qend(); ++qit1)
            {
                if (id1.gc_id != qit1->second)
                {
                    gc_element_id id11 = {0, qit1->second};
                    adjacent[id1].insert(id11);
                    adjacent[id11].insert(id1);
                }
            }
        }
        for (auto it2 = rtree2.begin(); it2 != rtree2.end(); ++it2)
        {
            auto const b2 = it2->first;
            gc_element_id id2 = {1, it2->second};
            for (auto qit2 = rtree2.qbegin(index::intersects(b2)); qit2 != rtree2.qend(); ++qit2)
            {
                if (id2.gc_id != qit2->second)
                {
                    gc_element_id id22 = {1, qit2->second};
                    adjacent[id2].insert(id22);
                    adjacent[id22].insert(id2);
                }
            }
        }
    }

    // Traverse the graph and build connected groups i.e. groups of intersecting envelopes
    std::deque<gc_element_id> queue;
    std::array<std::vector<bool>, 2> visited = {
        std::vector<bool>(boost::size(gc1_view), false),
        std::vector<bool>(boost::size(gc2_view), false)
    };
    for (auto const& elem : adjacent)
    {
        std::vector<gc_element_id> group;
        if (! visited[elem.first.source_id][elem.first.gc_id])
        {
            queue.push_back(elem.first);
            visited[elem.first.source_id][elem.first.gc_id] = true;
            group.push_back(elem.first);
            while (! queue.empty())
            {
                gc_element_id e = queue.front();
                queue.pop_front();
                for (auto const& n : adjacent[e])
                {
                    if (! visited[n.source_id][n.gc_id])
                    {
                        queue.push_back(n);
                        visited[n.source_id][n.gc_id] = true;
                        group.push_back(n);
                    }
                }
            }
        }
        if (! group.empty())
        {
            if (! intersecting_fun(group))
            {
                return;
            }
        }
    }

    {
        std::vector<gc_element_id> group;
        for (std::size_t i = 0; i < visited[0].size(); ++i)
        {
            if (! visited[0][i])
            {
                group.emplace_back(0, i);
            }
        }
        for (std::size_t i = 0; i < visited[1].size(); ++i)
        {
            if (! visited[1][i])
            {
                group.emplace_back(1, i);
            }
        }
        if (! group.empty())
        {
            disjoint_fun(group);
        }
    }
}


} // namespace detail
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_GC_GROUP_ELEMENTS_HPP
