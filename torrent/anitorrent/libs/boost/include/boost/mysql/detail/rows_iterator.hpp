//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_ROWS_ITERATOR_HPP
#define BOOST_MYSQL_DETAIL_ROWS_ITERATOR_HPP

#include <boost/mysql/field_view.hpp>
#include <boost/mysql/row.hpp>
#include <boost/mysql/row_view.hpp>

#include <boost/mysql/detail/access.hpp>

#include <cstddef>
#include <cstdint>
#include <iterator>

namespace boost {
namespace mysql {
namespace detail {

inline row_view row_slice(const field_view* fields, std::size_t num_columns, std::size_t offset) noexcept
{
    return access::construct<row_view>(fields + num_columns * offset, num_columns);
}

class rows_iterator
{
    const field_view* fields_{nullptr};
    std::size_t num_columns_{0};
    std::size_t row_num_{0};

public:
    using value_type = row;
    using reference = row_view;
    using pointer = row_view;
    using difference_type = std::ptrdiff_t;
    using iterator_category = std::random_access_iterator_tag;

    rows_iterator() = default;
    rows_iterator(const field_view* fields, std::size_t num_columns, std::size_t rownum) noexcept
        : fields_(fields), num_columns_(num_columns), row_num_(rownum)
    {
    }

    rows_iterator& operator++() noexcept
    {
        ++row_num_;
        return *this;
    }
    rows_iterator operator++(int) noexcept
    {
        auto res = *this;
        ++(*this);
        return res;
    }
    rows_iterator& operator--() noexcept
    {
        --row_num_;
        return *this;
    }
    rows_iterator operator--(int) noexcept
    {
        auto res = *this;
        --(*this);
        return res;
    }
    rows_iterator& operator+=(std::ptrdiff_t n) noexcept
    {
        row_num_ += n;
        return *this;
    }
    rows_iterator& operator-=(std::ptrdiff_t n) noexcept
    {
        row_num_ -= n;
        return *this;
    }
    rows_iterator operator+(std::ptrdiff_t n) const noexcept
    {
        return rows_iterator(fields_, num_columns_, row_num_ + n);
    }
    rows_iterator operator-(std::ptrdiff_t n) const noexcept
    {
        return rows_iterator(fields_, num_columns_, row_num_ - n);
    }
    std::ptrdiff_t operator-(rows_iterator rhs) const noexcept { return row_num_ - rhs.row_num_; }

    pointer operator->() const noexcept { return **this; }
    reference operator*() const noexcept { return (*this)[0]; }
    reference operator[](std::ptrdiff_t i) const noexcept
    {
        return row_slice(fields_, num_columns_, row_num_ + i);
    }

    bool operator==(rows_iterator rhs) const noexcept { return row_num_ == rhs.row_num_; }
    bool operator!=(rows_iterator rhs) const noexcept { return !(*this == rhs); }
    bool operator<(rows_iterator rhs) const noexcept { return row_num_ < rhs.row_num_; }
    bool operator<=(rows_iterator rhs) const noexcept { return row_num_ <= rhs.row_num_; }
    bool operator>(rows_iterator rhs) const noexcept { return row_num_ > rhs.row_num_; }
    bool operator>=(rows_iterator rhs) const noexcept { return row_num_ >= rhs.row_num_; }
};

inline rows_iterator operator+(std::ptrdiff_t n, rows_iterator it) noexcept { return it + n; }

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
