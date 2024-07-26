//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_ROW_HPP
#define BOOST_MYSQL_ROW_HPP

#include <boost/mysql/field.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/row_view.hpp>

#include <boost/mysql/detail/row_impl.hpp>

#include <cstddef>
#include <vector>

namespace boost {
namespace mysql {

/**
 * \brief An owning, read-only sequence of fields.
 * \details
 * Although owning, `row` is read-only. It's optimized for memory re-use. If you need to mutate
 * fields, use a `std::vector<field>` instead (see \ref row_view::as_vector and \ref
 * row::as_vector).
 *
 * \par Object lifetimes
 * A `row` object owns a chunk of memory in which it stores its elements. On element access (using
 * iterators, \ref row::at or \ref row::operator[]) it returns \ref field_view's pointing into the
 * `row`'s internal storage. These views behave like references, and are valid as long as pointers,
 * iterators and references into the `row` remain valid.
 */
class row
{
    detail::row_impl impl_;

public:
#ifdef BOOST_MYSQL_DOXYGEN
    /**
     * \brief A random access iterator to an element.
     * \details The exact type of the iterator is unspecified.
     */
    using iterator = __see_below__;
#else
    using iterator = const field_view*;
#endif

    /// \copydoc iterator
    using const_iterator = iterator;

    /// A type that can hold elements in this collection with value semantics.
    using value_type = field;

    /// The reference type.
    using reference = field_view;

    /// \copydoc reference
    using const_reference = field_view;

    /// An unsigned integer type to represent sizes.
    using size_type = std::size_t;

    /// A signed integer type used to represent differences.
    using difference_type = std::ptrdiff_t;

    /**
     * \brief Constructs an empty row.
     * \par Exception safety
     * No-throw guarantee.
     */
    row() = default;

    /**
     * \brief Copy constructor.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `other`'s.
     *
     * \par Complexity
     * Linear on `other.size()`.
     */
    row(const row& other) = default;

    /**
     * \brief Move constructor.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Iterators and references (including \ref row_view's and \ref field_view's) to
     * elements in `other` remain valid.
     *
     * \par Complexity
     * Constant.
     */
    row(row&& other) = default;

    /**
     * \brief Copy assignment.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `other`'s. Iterators and references
     * (including \ref row_view's and \ref field_view's) to elements in `*this` are invalidated.
     *
     * \par Complexity
     * Linear on `this->size()` and `other.size()`.
     */
    row& operator=(const row& other) = default;

    /**
     * \brief Move assignment.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Iterators and references (including \ref row_view's and \ref field_view's) to
     * elements in `*this` are invalidated. Iterators and references to elements in `other` remain
     * valid.
     *
     * \par Complexity
     * Constant.
     */
    row& operator=(row&& other) = default;

    /**
     * \brief Destructor.
     */
    ~row() = default;

    /**
     * \brief Constructs a row from a \ref row_view.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `r`'s (the contents of `r` will be copied
     * into `*this`).
     *
     * \par Complexity
     * Linear on `r.size()`.
     */
    row(row_view r) : impl_(r.begin(), r.size()) {}

    /**
     * \brief Replaces the contents with a \ref row_view.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `r`'s (the contents of `r` will be copied
     * into `*this`). Iterators and references (including \ref row_view's and \ref field_view's) to
     * elements in `*this` are invalidated.
     *
     * \par Complexity
     * Linear on `this->size()` and `r.size()`.
     */
    row& operator=(row_view r)
    {
        impl_.assign(r.begin(), r.size());
        return *this;
    }

    /// \copydoc row_view::begin
    const_iterator begin() const noexcept { return impl_.fields().data(); }

    /// \copydoc row_view::end
    const_iterator end() const noexcept { return impl_.fields().data() + impl_.fields().size(); }

    /// \copydoc row_view::at
    field_view at(std::size_t i) const { return impl_.fields().at(i); }

    /// \copydoc row_view::operator[]
    field_view operator[](std::size_t i) const noexcept { return impl_.fields()[i]; }

    /// \copydoc row_view::front
    field_view front() const noexcept { return impl_.fields().front(); }

    /// \copydoc row_view::back
    field_view back() const noexcept { return impl_.fields().back(); }

    /// \copydoc row_view::empty
    bool empty() const noexcept { return impl_.fields().empty(); }

    /// \copydoc row_view::size
    std::size_t size() const noexcept { return impl_.fields().size(); }

    /**
     * \brief Creates a \ref row_view that references `*this`.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned view will be valid until any function that invalidates iterators and
     * references is invoked on `*this` or `*this` is destroyed.
     *
     * \par Complexity
     * Constant.
     */
    operator row_view() const noexcept { return row_view(impl_.fields().data(), impl_.fields().size()); }

    /// \copydoc row_view::as_vector
    template <class Allocator>
    void as_vector(std::vector<field, Allocator>& out) const
    {
        out.assign(begin(), end());
    }

    /// \copydoc row_view::as_vector
    std::vector<field> as_vector() const { return std::vector<field>(begin(), end()); }
};

/**
 * \relates row
 * \brief Equality operator.
 * \details The containers are considered equal if they have the same number of elements and they
 * all compare equal, as defined by \ref field_view::operator==.
 *
 * \par Exception safety
 * No-throw guarantee.
 *
 * \par Complexity
 * Linear in `lhs.size()` and `rhs.size()`.
 */
inline bool operator==(const row& lhs, const row& rhs) noexcept { return row_view(lhs) == row_view(rhs); }

/**
 * \relates row
 * \brief Inequality operator.
 *
 * \par Exception safety
 * No-throw guarantee.
 *
 * \par Complexity
 * Linear in `lhs.size()` and `rhs.size()`.
 */
inline bool operator!=(const row& lhs, const row& rhs) { return !(lhs == rhs); }

/**
 * \relates row
 * \copydoc row::operator==(const row&,const row&)
 */
inline bool operator==(const row& lhs, const row_view& rhs) noexcept { return row_view(lhs) == rhs; }

/**
 * \relates row
 * \copydoc row::operator!=(const row&,const row&)
 */
inline bool operator!=(const row& lhs, const row_view& rhs) noexcept { return !(lhs == rhs); }

/**
 * \relates row
 * \copydoc row::operator==(const row&,const row&)
 */
inline bool operator==(const row_view& lhs, const row& rhs) noexcept { return lhs == row_view(rhs); }

/**
 * \relates row
 * \copydoc row::operator!=(const row&,const row&)
 */
inline bool operator!=(const row_view& lhs, const row& rhs) noexcept { return !(lhs == rhs); }

}  // namespace mysql
}  // namespace boost

#endif
