//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_RESULTS_ITERATOR_HPP
#define BOOST_MYSQL_DETAIL_RESULTS_ITERATOR_HPP

#include <boost/mysql/resultset.hpp>
#include <boost/mysql/resultset_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/execution_processor/results_impl.hpp>

namespace boost {
namespace mysql {
namespace detail {

class results_iterator
{
    const results_impl* self_{};
    std::size_t index_{};

public:
    using value_type = resultset;
    using reference = resultset_view;
    using pointer = resultset_view;
    using difference_type = std::ptrdiff_t;
    using iterator_category = std::random_access_iterator_tag;

    results_iterator() = default;
    results_iterator(const results_impl* self, std::size_t index) noexcept : self_(self), index_(index) {}

    results_iterator& operator++() noexcept
    {
        ++index_;
        return *this;
    }
    results_iterator operator++(int) noexcept
    {
        auto res = *this;
        ++(*this);
        return res;
    }
    results_iterator& operator--() noexcept
    {
        --index_;
        return *this;
    }
    results_iterator operator--(int) noexcept
    {
        auto res = *this;
        --(*this);
        return res;
    }
    results_iterator& operator+=(std::ptrdiff_t n) noexcept
    {
        index_ += n;
        return *this;
    }
    results_iterator& operator-=(std::ptrdiff_t n) noexcept
    {
        index_ -= n;
        return *this;
    }
    results_iterator operator+(std::ptrdiff_t n) const noexcept
    {
        return results_iterator(self_, index_ + n);
    }
    results_iterator operator-(std::ptrdiff_t n) const noexcept { return *this + (-n); }
    std::ptrdiff_t operator-(results_iterator rhs) const noexcept { return index_ - rhs.index_; }

    pointer operator->() const noexcept { return **this; }
    reference operator*() const noexcept { return (*this)[0]; }
    reference operator[](std::ptrdiff_t i) const noexcept
    {
        return access::construct<resultset_view>(*self_, index_ + i);
    }

    bool operator==(results_iterator rhs) const noexcept { return index_ == rhs.index_; }
    bool operator!=(results_iterator rhs) const noexcept { return !(*this == rhs); }
    bool operator<(results_iterator rhs) const noexcept { return index_ < rhs.index_; }
    bool operator<=(results_iterator rhs) const noexcept { return index_ <= rhs.index_; }
    bool operator>(results_iterator rhs) const noexcept { return index_ > rhs.index_; }
    bool operator>=(results_iterator rhs) const noexcept { return index_ >= rhs.index_; }

    std::size_t index() const noexcept { return index_; }
    const results_impl* obj() const noexcept { return self_; }
};

inline results_iterator operator+(std::ptrdiff_t n, results_iterator it) noexcept { return it + n; }

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
