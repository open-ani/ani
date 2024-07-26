// Boost.Geometry Index
//
// R-tree nodes based on Boost.Variant, storing dynamic-size containers
//
// Copyright (c) 2011-2023 Adam Wulkiewicz, Lodz, Poland.
//
// This file was modified by Oracle on 2021-2023.
// Modifications copyright (c) 2021-2023 Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle
//
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_INDEX_DETAIL_RTREE_NODE_VARIANT_DYNAMIC_HPP
#define BOOST_GEOMETRY_INDEX_DETAIL_RTREE_NODE_VARIANT_DYNAMIC_HPP

#include <utility>
#include <boost/container/allocator_traits.hpp>
#include <boost/container/vector.hpp>
#include <boost/core/invoke_swap.hpp>
#include <boost/core/pointer_traits.hpp>
#include <boost/variant/static_visitor.hpp>
#include <boost/variant/variant.hpp>

#include <boost/geometry/index/detail/rtree/options.hpp>
#include <boost/geometry/index/detail/rtree/node/concept.hpp>
#include <boost/geometry/index/detail/rtree/node/pairs.hpp>
#include <boost/geometry/index/detail/rtree/node/scoped_deallocator.hpp>

namespace boost { namespace geometry { namespace index {

namespace detail { namespace rtree {

// nodes default types

template <typename Value, typename Parameters, typename Box, typename Allocators, typename Tag>
struct variant_internal_node
{
    typedef rtree::ptr_pair<Box, typename Allocators::node_pointer> element_type;
    typedef typename boost::container::allocator_traits
        <
            typename Allocators::node_allocator_type
        >::template rebind_alloc<element_type> allocator_type;

    typedef boost::container::vector<element_type, allocator_type> elements_type;

    template <typename Al>
    inline variant_internal_node(Al const& al)
        : elements(allocator_type(al))
    {}

    elements_type elements;
};

template <typename Value, typename Parameters, typename Box, typename Allocators, typename Tag>
struct variant_leaf
{
    typedef typename boost::container::allocator_traits
        <
            typename Allocators::node_allocator_type
        >::template rebind_alloc<Value> allocator_type;

    typedef boost::container::vector<Value, allocator_type> elements_type;

    template <typename Al>
    inline variant_leaf(Al const& al)
        : elements(allocator_type(al))
    {}

    elements_type elements;
};

// nodes traits

template <typename Value, typename Parameters, typename Box, typename Allocators>
struct node<Value, Parameters, Box, Allocators, node_variant_dynamic_tag>
{
    typedef boost::variant<
        variant_leaf<Value, Parameters, Box, Allocators, node_variant_dynamic_tag>,
        variant_internal_node<Value, Parameters, Box, Allocators, node_variant_dynamic_tag>
    > type;
};

template <typename Value, typename Parameters, typename Box, typename Allocators>
struct internal_node<Value, Parameters, Box, Allocators, node_variant_dynamic_tag>
{
    typedef variant_internal_node<Value, Parameters, Box, Allocators, node_variant_dynamic_tag> type;
};

template <typename Value, typename Parameters, typename Box, typename Allocators>
struct leaf<Value, Parameters, Box, Allocators, node_variant_dynamic_tag>
{
    typedef variant_leaf<Value, Parameters, Box, Allocators, node_variant_dynamic_tag> type;
};

// visitor traits

template <typename Value, typename Parameters, typename Box, typename Allocators, bool IsVisitableConst>
struct visitor<Value, Parameters, Box, Allocators, node_variant_dynamic_tag, IsVisitableConst>
{
    typedef static_visitor<> type;
};

// allocators

template <typename Allocator, typename Value, typename Parameters, typename Box, typename Tag>
struct node_alloc
{
    typedef typename node
        <
            Value, Parameters, Box,
            allocators<Allocator, Value, Parameters, Box, Tag>,
            Tag
        >::type node_type;

    typedef typename boost::container::allocator_traits
        <
            Allocator
        >::template rebind_alloc<node_type> type;

    typedef boost::container::allocator_traits<type> traits;
};


template <typename Allocator, typename Value, typename Parameters, typename Box>
class allocators<Allocator, Value, Parameters, Box, node_variant_dynamic_tag>
    : public detail::rtree::node_alloc
        <
            Allocator, Value, Parameters, Box, node_variant_dynamic_tag
        >::type
{
    typedef detail::rtree::node_alloc
        <
            Allocator, Value, Parameters, Box, node_variant_dynamic_tag
        > node_alloc;

public:
    typedef typename node_alloc::type node_allocator_type;
    typedef typename node_alloc::traits::pointer node_pointer;

private:
    typedef typename boost::container::allocator_traits
        <
            node_allocator_type // node_allocator_type for consistency with variant_leaf
        >::template rebind_alloc<Value> value_allocator_type;
    typedef boost::container::allocator_traits<value_allocator_type> value_allocator_traits;


public:
    typedef Allocator allocator_type;

    typedef Value value_type;
    typedef typename value_allocator_traits::reference reference;
    typedef typename value_allocator_traits::const_reference const_reference;
    typedef typename value_allocator_traits::size_type size_type;
    typedef typename value_allocator_traits::difference_type difference_type;
    typedef typename value_allocator_traits::pointer pointer;
    typedef typename value_allocator_traits::const_pointer const_pointer;

    inline allocators()
        : node_allocator_type()
    {}

    template <typename Alloc>
    inline explicit allocators(Alloc const& alloc)
        : node_allocator_type(alloc)
    {}

    inline allocators(allocators&& a)
        : node_allocator_type(std::move(a.node_allocator()))
    {}

    inline allocators & operator=(allocators&& a)
    {
        node_allocator() = std::move(a.node_allocator());
        return *this;
    }

    inline allocators & operator=(allocators const& a)
    {
        node_allocator() = a.node_allocator();
        return *this;
    }

    void swap(allocators & a)
    {
        boost::core::invoke_swap(node_allocator(), a.node_allocator());
    }

    bool operator==(allocators const& a) const { return node_allocator() == a.node_allocator(); }
    template <typename Alloc>
    bool operator==(Alloc const& a) const { return node_allocator() == node_allocator_type(a); }

    Allocator allocator() const { return Allocator(node_allocator()); }

    node_allocator_type & node_allocator() { return *this; }
    node_allocator_type const& node_allocator() const { return *this; }
};

// create_node_variant

template <typename VariantPtr, typename Node>
struct create_variant_node
{
    template <typename AllocNode>
    static inline VariantPtr apply(AllocNode & alloc_node)
    {
        typedef boost::container::allocator_traits<AllocNode> Al;
        typedef typename Al::pointer P;

        P p = Al::allocate(alloc_node, 1);

        if ( 0 == p )
            throw_runtime_error("boost::geometry::index::rtree node creation failed");

        scoped_deallocator<AllocNode> deallocator(p, alloc_node);

        Al::construct(alloc_node, boost::to_address(p), Node(alloc_node)); // implicit cast to Variant

        deallocator.release();
        return p;
    }
};

// destroy_node_variant

template <typename Node>
struct destroy_variant_node
{
    template <typename AllocNode, typename VariantPtr>
    static inline void apply(AllocNode & alloc_node, VariantPtr n)
    {
        typedef boost::container::allocator_traits<AllocNode> Al;

        Al::destroy(alloc_node, boost::addressof(*n));
        Al::deallocate(alloc_node, n, 1);
    }
};

// create_node

template <typename Allocators, typename Value, typename Parameters, typename Box, typename Tag>
struct create_node<
    Allocators,
    variant_internal_node<Value, Parameters, Box, Allocators, Tag>
>
{
    static inline typename Allocators::node_pointer
    apply(Allocators & allocators)
    {
        return create_variant_node<
            typename Allocators::node_pointer,
            variant_internal_node<Value, Parameters, Box, Allocators, Tag>
        >::apply(allocators.node_allocator());
    }
};

template <typename Allocators, typename Value, typename Parameters, typename Box, typename Tag>
struct create_node<
    Allocators,
    variant_leaf<Value, Parameters, Box, Allocators, Tag>
>
{
    static inline typename Allocators::node_pointer
    apply(Allocators & allocators)
    {
        return create_variant_node<
            typename Allocators::node_pointer,
            variant_leaf<Value, Parameters, Box, Allocators, Tag>
        >::apply(allocators.node_allocator());
    }
};

// destroy_node

template <typename Allocators, typename Value, typename Parameters, typename Box, typename Tag>
struct destroy_node<
    Allocators,
    variant_internal_node<Value, Parameters, Box, Allocators, Tag>
>
{
    static inline void apply(Allocators & allocators, typename Allocators::node_pointer n)
    {
        destroy_variant_node<
            variant_internal_node<Value, Parameters, Box, Allocators, Tag>
        >::apply(allocators.node_allocator(), n);
    }
};

template <typename Allocators, typename Value, typename Parameters, typename Box, typename Tag>
struct destroy_node<
    Allocators,
    variant_leaf<Value, Parameters, Box, Allocators, Tag>
>
{
    static inline void apply(Allocators & allocators, typename Allocators::node_pointer n)
    {
        destroy_variant_node<
            variant_leaf<Value, Parameters, Box, Allocators, Tag>
        >::apply(allocators.node_allocator(), n);
    }
};

}} // namespace detail::rtree

}}} // namespace boost::geometry::index

#endif // BOOST_GEOMETRY_INDEX_DETAIL_RTREE_NODE_VARIANT_DYNAMIC_HPP
