// Boost.Geometry Index
//
// R-tree spatial query visitor implementation
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

#ifndef BOOST_GEOMETRY_INDEX_DETAIL_RTREE_VISITORS_SPATIAL_QUERY_HPP
#define BOOST_GEOMETRY_INDEX_DETAIL_RTREE_VISITORS_SPATIAL_QUERY_HPP

#include <boost/geometry/index/detail/rtree/node/node_elements.hpp>
#include <boost/geometry/index/detail/rtree/node/weak_visitor.hpp>
#include <boost/geometry/index/detail/predicates.hpp>
#include <boost/geometry/index/parameters.hpp>

namespace boost { namespace geometry { namespace index {

namespace detail { namespace rtree { namespace visitors {

template <typename MembersHolder, typename Predicates, typename OutIter>
struct spatial_query
{
    typedef typename MembersHolder::parameters_type parameters_type;
    typedef typename MembersHolder::translator_type translator_type;
    typedef typename MembersHolder::allocators_type allocators_type;

    typedef typename index::detail::strategy_type<parameters_type>::type strategy_type;

    typedef typename MembersHolder::node node;
    typedef typename MembersHolder::internal_node internal_node;
    typedef typename MembersHolder::leaf leaf;

    typedef typename allocators_type::node_pointer node_pointer;
    typedef typename allocators_type::size_type size_type;

    spatial_query(MembersHolder const& members, Predicates const& p, OutIter out_it)
        : m_tr(members.translator())
        , m_strategy(index::detail::get_strategy(members.parameters()))
        , m_pred(p)
        , m_out_iter(out_it)
        , m_found_count(0)
    {}

    size_type apply(node_pointer ptr, size_type reverse_level)
    {
        namespace id = index::detail;
        if (reverse_level > 0)
        {
            internal_node& n = rtree::get<internal_node>(*ptr);
            // traverse nodes meeting predicates
            for (auto const& p : rtree::elements(n))
            {
                // if node meets predicates (0 is dummy value)
                if (id::predicates_check<id::bounds_tag>(m_pred, 0, p.first, m_strategy))
                {
                    apply(p.second, reverse_level - 1);
                }
            }
        }
        else
        {
            leaf& n = rtree::get<leaf>(*ptr);
            // get all values meeting predicates
            for (auto const& v : rtree::elements(n))
            {
                // if value meets predicates
                if (id::predicates_check<id::value_tag>(m_pred, v, m_tr(v), m_strategy))
                {
                    *m_out_iter = v;
                    ++m_out_iter;
                    ++m_found_count;
                }
            }
        }

        return m_found_count;
    }

    size_type apply(MembersHolder const& members)
    {
        return apply(members.root, members.leafs_level);
    }

private:
    translator_type const& m_tr;
    strategy_type m_strategy;

    Predicates const& m_pred;
    OutIter m_out_iter;

    size_type m_found_count;
};

template <typename MembersHolder, typename Predicates>
class spatial_query_incremental
{
    typedef typename MembersHolder::value_type value_type;
    typedef typename MembersHolder::parameters_type parameters_type;
    typedef typename MembersHolder::translator_type translator_type;
    typedef typename MembersHolder::allocators_type allocators_type;

    typedef typename index::detail::strategy_type<parameters_type>::type strategy_type;

    typedef typename MembersHolder::node node;
    typedef typename MembersHolder::internal_node internal_node;
    typedef typename MembersHolder::leaf leaf;

    typedef typename allocators_type::size_type size_type;
    typedef typename allocators_type::const_reference const_reference;
    typedef typename allocators_type::node_pointer node_pointer;

    typedef typename rtree::elements_type<internal_node>::type::const_iterator internal_iterator;
    typedef typename rtree::elements_type<leaf>::type leaf_elements;
    typedef typename rtree::elements_type<leaf>::type::const_iterator leaf_iterator;

    struct internal_data
    {
        internal_data(internal_iterator f, internal_iterator l, size_type rl)
            : first(f), last(l), reverse_level(rl)
        {}
        internal_iterator first;
        internal_iterator last;
        size_type reverse_level;
    };

public:
    spatial_query_incremental()
        : m_translator(nullptr)
//        , m_strategy()
//        , m_pred()
        , m_values(nullptr)
        , m_current()
    {}

    spatial_query_incremental(Predicates const& p)
        : m_translator(nullptr)
//        , m_strategy()
        , m_pred(p)
        , m_values(nullptr)
        , m_current()
    {}

    spatial_query_incremental(MembersHolder const& members, Predicates const& p)
        : m_translator(::boost::addressof(members.translator()))
        , m_strategy(index::detail::get_strategy(members.parameters()))
        , m_pred(p)
        , m_values(nullptr)
        , m_current()
    {}

    const_reference dereference() const
    {
        BOOST_GEOMETRY_INDEX_ASSERT(m_values, "not dereferencable");
        return *m_current;
    }

    void initialize(MembersHolder const& members)
    {
        apply(members.root, members.leafs_level);
        search_value();
    }

    void increment()
    {
        ++m_current;
        search_value();
    }

    bool is_end() const
    {
        return 0 == m_values;
    }

    friend bool operator==(spatial_query_incremental const& l, spatial_query_incremental const& r)
    {
        return (l.m_values == r.m_values) && (0 == l.m_values || l.m_current == r.m_current);
    }

private:
    void apply(node_pointer ptr, size_type reverse_level)
    {
        namespace id = index::detail;

        if (reverse_level > 0)
        {
            internal_node& n = rtree::get<internal_node>(*ptr);
            auto const& elements = rtree::elements(n);
            m_internal_stack.push_back(internal_data(elements.begin(), elements.end(), reverse_level - 1));
        }
        else
        {
            leaf& n = rtree::get<leaf>(*ptr);
            m_values = ::boost::addressof(rtree::elements(n));
            m_current = rtree::elements(n).begin();
        }
    }

    void search_value()
    {
        namespace id = index::detail;
        for (;;)
        {
            // if leaf is choosen, move to the next value in leaf
            if ( m_values )
            {
                if ( m_current != m_values->end() )
                {
                    // return if next value is found
                    value_type const& v = *m_current;
                    if (id::predicates_check<id::value_tag>(m_pred, v, (*m_translator)(v), m_strategy))
                    {
                        return;
                    }

                    ++m_current;
                }
                // no more values, clear current leaf
                else
                {
                    m_values = 0;
                }
            }
            // if leaf isn't choosen, move to the next leaf
            else
            {
                // return if there is no more nodes to traverse
                if (m_internal_stack.empty())
                {
                    return;
                }

                internal_data& current_data = m_internal_stack.back();

                // no more children in current node, remove it from stack
                if (current_data.first == current_data.last)
                {
                    m_internal_stack.pop_back();
                    continue;
                }

                internal_iterator it = current_data.first;
                ++current_data.first;

                // next node is found, push it to the stack
                if (id::predicates_check<id::bounds_tag>(m_pred, 0, it->first, m_strategy))
                {
                    apply(it->second, current_data.reverse_level);
                }
            }
        }
    }

    const translator_type * m_translator;
    strategy_type m_strategy;

    Predicates m_pred;

    std::vector<internal_data> m_internal_stack;
    const leaf_elements * m_values;
    leaf_iterator m_current;
};

}}} // namespace detail::rtree::visitors

}}} // namespace boost::geometry::index

#endif // BOOST_GEOMETRY_INDEX_DETAIL_RTREE_VISITORS_SPATIAL_QUERY_HPP
