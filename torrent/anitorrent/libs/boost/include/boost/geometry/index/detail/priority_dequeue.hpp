// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_INDEX_DETAIL_PRIORITY_DEQUEUE_HPP
#define BOOST_GEOMETRY_INDEX_DETAIL_PRIORITY_DEQUEUE_HPP

#include <vector>

#include <boost/geometry/index/detail/maxmin_heap.hpp>

namespace boost { namespace geometry { namespace index { namespace detail
{

template
<
    typename T,
    typename Container = std::vector<T>,
    typename Compare = std::less<typename Container::value_type>
>
class priority_dequeue
{
public:
    using container_type = Container;
    using value_compare = Compare;
    using value_type = typename Container::value_type;
    using size_type = typename Container::size_type;
    using reference = typename Container::reference;
    using const_reference = typename Container::const_reference;

    priority_dequeue()
        : c(), comp()
    {}

    explicit priority_dequeue(Compare const& compare)
        : c(), comp(compare)
    {}

    priority_dequeue(Compare const& compare, Container const& cont)
        : c(cont), comp(compare)
    {
        make_maxmin_heap(c.begin(), c.end(), comp);
    }

    priority_dequeue(Compare const& compare, Container&& cont)
        : c(std::move(cont)), comp(compare)
    {
        make_maxmin_heap(c.begin(), c.end(), comp);
    }

    template <typename It>
    priority_dequeue(It first, It last)
        : c(first, last), comp()
    {
        make_maxmin_heap(c.begin(), c.end(), comp);
    }

    template <typename It>
    priority_dequeue(It first, It last, Compare const& compare)
        : c(first, last), comp(compare)
    {
        make_maxmin_heap(c.begin(), c.end(), comp);
    }

    template <typename It>
    priority_dequeue(It first, It last, Compare const& compare, Container const& cont)
        : c(cont), comp(compare)
    {
        c.insert(first, last);
        make_maxmin_heap(c.begin(), c.end(), comp);
    }

    template <typename It>
    priority_dequeue(It first, It last, Compare const& compare, Container&& cont)
        : c(std::move(cont)), comp(compare)
    {
        c.insert(first, last);
        make_maxmin_heap(c.begin(), c.end(), comp);
    }

    const_reference top() const
    {
        return *c.begin();
    }

    const_reference bottom() const
    {
        return bottom_maxmin_heap(c.begin(), c.end(), comp);
    }

    void push(const value_type& value)
    {
        c.push_back(value);
        push_maxmin_heap(c.begin(), c.end(), comp);
    }

    void push(value_type&& value)
    {
        c.push_back(std::move(value));
        push_maxmin_heap(c.begin(), c.end(), comp);
    }

    template <typename ...Args>
    void emplace(Args&& ...args)
    {
        c.emplace_back(std::forward<Args>(args)...);
        push_maxmin_heap(c.begin(), c.end(), comp);
    }

    void pop_top()
    {
        pop_top_maxmin_heap(c.begin(), c.end(), comp);
        c.pop_back();
    }

    void pop_bottom()
    {
        pop_bottom_maxmin_heap(c.begin(), c.end(), comp);
        c.pop_back();
    }

    bool empty() const
    {
        return c.empty();
    }

    size_t size() const
    {
        return c.size();
    }

    void swap(priority_dequeue& other)
    {
        using std::swap;
        std::swap(c, other.c);
        std::swap(comp, other.comp);
    }

protected:
    Container c;
    Compare comp;
};

}}}} // namespace boost::geometry::index::detail

#endif // BOOST_GEOMETRY_INDEX_DETAIL_PRIORITY_DEQUEUE_HPP
