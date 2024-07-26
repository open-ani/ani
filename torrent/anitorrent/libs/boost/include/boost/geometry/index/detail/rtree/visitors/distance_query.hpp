// Boost.Geometry Index
//
// R-tree distance (knn, path, etc. ) query visitor implementation
//
// Copyright (c) 2011-2014 Adam Wulkiewicz, Lodz, Poland.
//
// This file was modified by Oracle on 2019-2023.
// Modifications copyright (c) 2019-2023 Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle
//
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_INDEX_DETAIL_RTREE_VISITORS_DISTANCE_QUERY_HPP
#define BOOST_GEOMETRY_INDEX_DETAIL_RTREE_VISITORS_DISTANCE_QUERY_HPP

#include <queue>

#include <boost/geometry/index/detail/distance_predicates.hpp>
#include <boost/geometry/index/detail/predicates.hpp>
#include <boost/geometry/index/detail/priority_dequeue.hpp>
#include <boost/geometry/index/detail/rtree/node/weak_visitor.hpp>
#include <boost/geometry/index/detail/rtree/node/node_elements.hpp>
#include <boost/geometry/index/detail/translator.hpp>
#include <boost/geometry/index/parameters.hpp>

namespace boost { namespace geometry { namespace index {

namespace detail { namespace rtree { namespace visitors {


struct pair_first_less
{
    template <typename First, typename Second>
    inline bool operator()(std::pair<First, Second> const& p1,
                           std::pair<First, Second> const& p2) const
    {
        return p1.first < p2.first;
    }
};

struct pair_first_greater
{
    template <typename First, typename Second>
    inline bool operator()(std::pair<First, Second> const& p1,
                           std::pair<First, Second> const& p2) const
    {
        return p1.first > p2.first;
    }
};

template <typename T, typename Comp>
struct priority_dequeue : index::detail::priority_dequeue<T, std::vector<T>, Comp>
{
    priority_dequeue() = default;
    //void reserve(typename std::vector<T>::size_type n)
    //{
    //    this->c.reserve(n);
    //}
    //void clear()
    //{
    //    this->c.clear();
    //}
};

template <typename T, typename Comp>
struct priority_queue : std::priority_queue<T, std::vector<T>, Comp>
{
    priority_queue() = default;
    //void reserve(typename std::vector<T>::size_type n)
    //{
    //    this->c.reserve(n);
    //}
    void clear()
    {
        this->c.clear();
    }
};

struct branch_data_comp
{
    template <typename BranchData>
    bool operator()(BranchData const& b1, BranchData const& b2) const
    {
        return b1.distance > b2.distance || (b1.distance == b2.distance && b1.reverse_level > b2.reverse_level);
    }
};

template <typename DistanceType, typename Value>
class distance_query_result
{
    using neighbor_data = std::pair<DistanceType, const Value *>;
    using neighbors_type = std::vector<neighbor_data>;
    using size_type = typename neighbors_type::size_type;

public:
    inline distance_query_result(size_type k)
        : m_count(k)
    {
        m_neighbors.reserve(m_count);
    }

    // NOTE: Do not call if max_count() == 0
    inline void store(DistanceType const& distance, const Value * value_ptr)
    {
        if (m_neighbors.size() < m_count)
        {
            m_neighbors.push_back(std::make_pair(distance, value_ptr));

            if (m_neighbors.size() == m_count)
            {
                std::make_heap(m_neighbors.begin(), m_neighbors.end(), pair_first_less());
            }
        }
        else if (distance < m_neighbors.front().first)
        {
            std::pop_heap(m_neighbors.begin(), m_neighbors.end(), pair_first_less());
            m_neighbors.back().first = distance;
            m_neighbors.back().second = value_ptr;
            std::push_heap(m_neighbors.begin(), m_neighbors.end(), pair_first_less());
        }
    }

    // NOTE: Do not call if max_count() == 0
    inline bool ignore_branch(DistanceType const& distance) const
    {
        return m_neighbors.size() == m_count
            && m_neighbors.front().first <= distance;
    }

    template <typename OutIt>
    inline size_type finish(OutIt out_it) const
    {
        for (auto const& p : m_neighbors)
        {
            *out_it = *(p.second);
            ++out_it;
        }

        return m_neighbors.size();
    }

    size_type max_count() const
    {
        return m_count;
    }

private:
    size_type m_count;
    neighbors_type m_neighbors;
};

template <typename MembersHolder, typename Predicates>
class distance_query
{
    typedef typename MembersHolder::value_type value_type;
    typedef typename MembersHolder::box_type box_type;
    typedef typename MembersHolder::parameters_type parameters_type;
    typedef typename MembersHolder::translator_type translator_type;

    typedef typename index::detail::strategy_type<parameters_type>::type strategy_type;

    typedef typename MembersHolder::node node;
    typedef typename MembersHolder::internal_node internal_node;
    typedef typename MembersHolder::leaf leaf;

    typedef index::detail::predicates_element
        <
            index::detail::predicates_find_distance<Predicates>::value, Predicates
        > nearest_predicate_access;
    typedef typename nearest_predicate_access::type nearest_predicate_type;
    typedef typename indexable_type<translator_type>::type indexable_type;

    typedef index::detail::calculate_distance<nearest_predicate_type, indexable_type, strategy_type, value_tag> calculate_value_distance;
    typedef index::detail::calculate_distance<nearest_predicate_type, box_type, strategy_type, bounds_tag> calculate_node_distance;
    typedef typename calculate_value_distance::result_type value_distance_type;
    typedef typename calculate_node_distance::result_type node_distance_type;

    typedef typename MembersHolder::size_type size_type;
    typedef typename MembersHolder::node_pointer node_pointer;

    using neighbor_data = std::pair<value_distance_type, const value_type *>;
    using neighbors_type = std::vector<neighbor_data>;

    struct branch_data
    {
        branch_data(node_distance_type d, size_type rl, node_pointer p)
            : distance(d), reverse_level(rl), ptr(p)
        {}

        node_distance_type distance;
        size_type reverse_level;
        node_pointer ptr;
    };
    using branches_type = priority_queue<branch_data, branch_data_comp>;

public:
    distance_query(MembersHolder const& members, Predicates const& pred)
        : m_tr(members.translator())
        , m_strategy(index::detail::get_strategy(members.parameters()))
        , m_pred(pred)
    {
        m_neighbors.reserve((std::min)(members.values_count, size_type(max_count())));
        //m_branches.reserve(members.parameters().get_min_elements() * members.leafs_level); ?
        // min, max or average?
    }

    template <typename OutIter>
    size_type apply(MembersHolder const& members, OutIter out_it)
    {
        return apply(members.root, members.leafs_level, out_it);
    }

private:
    template <typename OutIter>
    size_type apply(node_pointer ptr, size_type reverse_level, OutIter out_it)
    {
        namespace id = index::detail;

        if (max_count() <= 0)
        {
            return 0;
        }

        for (;;)
        {
            if (reverse_level > 0)
            {
                internal_node& n = rtree::get<internal_node>(*ptr);
                // fill array of nodes meeting predicates
                for (auto const& p : rtree::elements(n))
                {
                    node_distance_type node_distance; // for distance predicate

                    // if current node meets predicates (0 is dummy value)
                    if (id::predicates_check<id::bounds_tag>(m_pred, 0, p.first, m_strategy)
                        // and if distance is ok
                        && calculate_node_distance::apply(predicate(), p.first, m_strategy, node_distance)
                        // and if current node is closer than the furthest neighbor
                        && ! ignore_branch(node_distance))
                    {
                        // add current node's data into the list
                        m_branches.push(branch_data(node_distance, reverse_level - 1, p.second));
                    }
                }
            }
            else
            {
                leaf& n = rtree::get<leaf>(*ptr);
                // search leaf for closest value meeting predicates
                for (auto const& v : rtree::elements(n))
                {
                    value_distance_type value_distance; // for distance predicate

                    // if value meets predicates
                    if (id::predicates_check<id::value_tag>(m_pred, v, m_tr(v), m_strategy)
                        // and if distance is ok
                        && calculate_value_distance::apply(predicate(), m_tr(v), m_strategy, value_distance))
                    {
                        // store value
                        store_value(value_distance, boost::addressof(v));
                    }
                }
            }

            if (m_branches.empty()
                || ignore_branch(m_branches.top().distance))
            {
                break;
            }

            ptr = m_branches.top().ptr;
            reverse_level = m_branches.top().reverse_level;
            m_branches.pop();
        }

        for (auto const& p : m_neighbors)
        {
            *out_it = *(p.second);
            ++out_it;
        }

        return m_neighbors.size();
    }

    bool ignore_branch(node_distance_type const& node_distance) const
    {
        return m_neighbors.size() == max_count()
            && m_neighbors.front().first <= node_distance;
    }

    void store_value(value_distance_type value_distance, const value_type * value_ptr)
    {
        if (m_neighbors.size() < max_count())
        {
            m_neighbors.push_back(std::make_pair(value_distance, value_ptr));

            if (m_neighbors.size() == max_count())
            {
                std::make_heap(m_neighbors.begin(), m_neighbors.end(), pair_first_less());
            }
        }
        else if (value_distance < m_neighbors.front().first)
        {
            std::pop_heap(m_neighbors.begin(), m_neighbors.end(), pair_first_less());
            m_neighbors.back() = std::make_pair(value_distance, value_ptr);
            std::push_heap(m_neighbors.begin(), m_neighbors.end(), pair_first_less());
        }
    }

    std::size_t max_count() const
    {
        return nearest_predicate_access::get(m_pred).count;
    }

    nearest_predicate_type const& predicate() const
    {
        return nearest_predicate_access::get(m_pred);
    }

    translator_type const& m_tr;
    strategy_type m_strategy;

    Predicates const& m_pred;

    branches_type m_branches;
    neighbors_type m_neighbors;
};

template <typename MembersHolder, typename Predicates>
class distance_query_incremental
{
    typedef typename MembersHolder::value_type value_type;
    typedef typename MembersHolder::box_type box_type;
    typedef typename MembersHolder::parameters_type parameters_type;
    typedef typename MembersHolder::translator_type translator_type;
    typedef typename MembersHolder::allocators_type allocators_type;

    typedef typename index::detail::strategy_type<parameters_type>::type strategy_type;

    typedef typename MembersHolder::node node;
    typedef typename MembersHolder::internal_node internal_node;
    typedef typename MembersHolder::leaf leaf;

    typedef index::detail::predicates_element
        <
            index::detail::predicates_find_distance<Predicates>::value, Predicates
        > nearest_predicate_access;
    typedef typename nearest_predicate_access::type nearest_predicate_type;
    typedef typename indexable_type<translator_type>::type indexable_type;

    typedef index::detail::calculate_distance<nearest_predicate_type, indexable_type, strategy_type, value_tag> calculate_value_distance;
    typedef index::detail::calculate_distance<nearest_predicate_type, box_type, strategy_type, bounds_tag> calculate_node_distance;
    typedef typename calculate_value_distance::result_type value_distance_type;
    typedef typename calculate_node_distance::result_type node_distance_type;

    typedef typename allocators_type::size_type size_type;
    typedef typename allocators_type::const_reference const_reference;
    typedef typename allocators_type::node_pointer node_pointer;

    typedef typename rtree::elements_type<internal_node>::type internal_elements;
    typedef typename internal_elements::const_iterator internal_iterator;
    typedef typename rtree::elements_type<leaf>::type leaf_elements;

    using neighbor_data = std::pair<value_distance_type, const value_type *>;
    using neighbors_type = priority_dequeue<neighbor_data, pair_first_greater>;

    struct branch_data
    {
        branch_data(node_distance_type d, size_type rl, node_pointer p)
            : distance(d), reverse_level(rl), ptr(p)
        {}

        node_distance_type distance;
        size_type reverse_level;
        node_pointer ptr;
    };
    using branches_type = priority_queue<branch_data, branch_data_comp>;

public:
    inline distance_query_incremental()
        : m_tr(nullptr)
//        , m_strategy()
//        , m_pred()
        , m_neighbors_count(0)
        , m_neighbor_ptr(nullptr)
    {}

    inline distance_query_incremental(Predicates const& pred)
        : m_tr(nullptr)
//        , m_strategy()
        , m_pred(pred)
        , m_neighbors_count(0)
        , m_neighbor_ptr(nullptr)
    {}

    inline distance_query_incremental(MembersHolder const& members, Predicates const& pred)
        : m_tr(::boost::addressof(members.translator()))
        , m_strategy(index::detail::get_strategy(members.parameters()))
        , m_pred(pred)
        , m_neighbors_count(0)
        , m_neighbor_ptr(nullptr)
    {}

    const_reference dereference() const
    {
        return *m_neighbor_ptr;
    }

    void initialize(MembersHolder const& members)
    {
        if (0 < max_count())
        {
            apply(members.root, members.leafs_level);
            increment();
        }
    }

    void increment()
    {
        for (;;)
        {
            if (m_branches.empty())
            {
                // there exists a next closest neighbor so we can increment
                if (! m_neighbors.empty())
                {
                    m_neighbor_ptr = m_neighbors.top().second;
                    ++m_neighbors_count;
                    m_neighbors.pop_top();
                }
                else
                {
                    // there aren't any neighbors left, end
                    m_neighbor_ptr = nullptr;
                    m_neighbors_count = max_count();
                }

                return;
            }
            else
            {
                branch_data const& closest_branch = m_branches.top();

                // if next neighbor is closer or as close as the closest branch, set next neighbor
                if (! m_neighbors.empty() && m_neighbors.top().first <= closest_branch.distance )
                {
                    m_neighbor_ptr = m_neighbors.top().second;
                    ++m_neighbors_count;
                    m_neighbors.pop_top();
                    return;
                }

                BOOST_GEOMETRY_INDEX_ASSERT(m_neighbors_count + m_neighbors.size() <= max_count(), "unexpected neighbors count");

                // if there is enough neighbors and there is no closer branch
                if (ignore_branch_or_value(closest_branch.distance))
                {
                    m_branches.clear();
                    continue;
                }
                else
                {
                    node_pointer ptr = closest_branch.ptr;
                    size_type reverse_level = closest_branch.reverse_level;
                    m_branches.pop();

                    apply(ptr, reverse_level);
                }
            }
        }
    }

    bool is_end() const
    {
        return m_neighbor_ptr == nullptr;
    }

    friend bool operator==(distance_query_incremental const& l, distance_query_incremental const& r)
    {
        return l.m_neighbors_count == r.m_neighbors_count;
    }

private:
    void apply(node_pointer ptr, size_type reverse_level)
    {
        namespace id = index::detail;
        // Put node's elements into the list of active branches if those elements meets predicates
        // and distance predicates(currently not used)
        // and aren't further than found neighbours (if there is enough neighbours)
        if (reverse_level > 0)
        {
            internal_node& n = rtree::get<internal_node>(*ptr);
            // fill active branch list array of nodes meeting predicates
            for (auto const& p : rtree::elements(n))
            {
                node_distance_type node_distance; // for distance predicate

                // if current node meets predicates (0 is dummy value)
                if (id::predicates_check<id::bounds_tag>(m_pred, 0, p.first, m_strategy)
                    // and if distance is ok
                    && calculate_node_distance::apply(predicate(), p.first, m_strategy, node_distance)
                    // and if current node is closer than the furthest neighbor
                    && ! ignore_branch_or_value(node_distance))
                {
                    // add current node into the queue
                    m_branches.push(branch_data(node_distance, reverse_level - 1, p.second));
                }
            }
        }
        // Put values into the list of neighbours if those values meets predicates
        // and distance predicates(currently not used)
        // and aren't further than already found neighbours (if there is enough neighbours)
        else
        {
            leaf& n = rtree::get<leaf>(*ptr);
            // search leaf for closest value meeting predicates
            for (auto const& v : rtree::elements(n))
            {
                value_distance_type value_distance; // for distance predicate

                // if value meets predicates
                if (id::predicates_check<id::value_tag>(m_pred, v, (*m_tr)(v), m_strategy)
                    // and if distance is ok
                    && calculate_value_distance::apply(predicate(), (*m_tr)(v), m_strategy, value_distance)
                    // and if current value is closer than the furthest neighbor
                    && ! ignore_branch_or_value(value_distance))
                {
                    // add current value into the queue
                    m_neighbors.push(std::make_pair(value_distance, boost::addressof(v)));

                    // remove unneeded value
                    if (m_neighbors_count + m_neighbors.size() > max_count())
                    {
                        m_neighbors.pop_bottom();
                    }
                }
            }
        }
    }

    template <typename Distance>
    bool ignore_branch_or_value(Distance const& distance)
    {
        return m_neighbors_count + m_neighbors.size() == max_count()
            && (m_neighbors.empty() || m_neighbors.bottom().first <= distance);
    }

    std::size_t max_count() const
    {
        return nearest_predicate_access::get(m_pred).count;
    }

    nearest_predicate_type const& predicate() const
    {
        return nearest_predicate_access::get(m_pred);
    }

    const translator_type * m_tr;
    strategy_type m_strategy;

    Predicates m_pred;

    branches_type m_branches;
    neighbors_type m_neighbors;
    size_type m_neighbors_count;
    const value_type * m_neighbor_ptr;
};

}}} // namespace detail::rtree::visitors

}}} // namespace boost::geometry::index

#endif // BOOST_GEOMETRY_INDEX_DETAIL_RTREE_VISITORS_DISTANCE_QUERY_HPP
