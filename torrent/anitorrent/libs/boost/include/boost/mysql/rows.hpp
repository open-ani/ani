//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_ROWS_HPP
#define BOOST_MYSQL_ROWS_HPP

#include <boost/mysql/field_view.hpp>
#include <boost/mysql/row.hpp>
#include <boost/mysql/row_view.hpp>
#include <boost/mysql/rows_view.hpp>

#include <boost/mysql/detail/row_impl.hpp>
#include <boost/mysql/detail/rows_iterator.hpp>

#include <boost/throw_exception.hpp>

#include <stdexcept>

namespace boost {
namespace mysql {

/**
 * \brief An owning, read-only sequence of rows.
 * \details
 * Models an owning, matrix-like container. Indexing a `rows` object (by using iterators,
 * \ref rows::at or \ref rows::operator[]) returns a \ref row_view object, representing a
 * single row. All rows in the collection are the same size (as given by \ref num_columns).
 * \n
 * A `rows` object owns a chunk of memory in which it stores its elements. The \ref rows_view
 * objects obtained on element access point into the `rows`' internal storage. These views (and any
 * \ref row_view and \ref field_view obtained from the former) behave
 * like references, and are valid as long as pointers, iterators and references into the `rows`
 * object remain valid.
 * \n
 * Although owning, `rows` is read-only. It's optimized for memory re-use.
 */
class rows
{
public:
#ifdef BOOST_MYSQL_DOXYGEN
    /**
     * \brief A random access iterator to an element.
     * \details The exact type of the iterator is unspecified.
     */
    using iterator = __see_below__;
#else
    using iterator = detail::rows_iterator;
#endif

    /// \copydoc iterator
    using const_iterator = iterator;

    /**
     * \brief A type that can hold elements in this collection with value semantics.
     * \details Note that element accesors (like \ref rows_view::operator[]) return \ref reference
     * objects instead of `value_type` objects. You can use this type if you need an owning class.
     */
    using value_type = row;

    /// The reference type.
    using reference = row_view;

    /// \copydoc reference
    using const_reference = row_view;

    /// An unsigned integer type to represent sizes.
    using size_type = std::size_t;

    /// A signed integer type used to represent differences.
    using difference_type = std::ptrdiff_t;

    /**
     * \brief Construct an empty `rows` object.
     * \par Exception safety
     * No-throw guarantee.
     */
    rows() = default;

    /**
     * \brief Copy constructor.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `other`'s.
     *
     * \par Complexity
     * Linear on `other.size() * other.num_columns()`.
     */
    rows(const rows& other) = default;

    /**
     * \brief Move constructor.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Iterators and references (including \ref rows_view's, \ref row_view's and \ref
     * field_view's) to elements in `other` remain valid.
     *
     * \par Complexity
     * Constant.
     */
    rows(rows&& other) = default;

    /**
     * \brief Copy assignment.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `other`'s. Iterators and references
     * (including \ref rows_view's, \ref row_view's and \ref field_view's) to elements in `*this`
     * are invalidated.
     *
     * \par Complexity
     * Linear on `this->size() * this->num_columns()` and `other.size() * other.num_columns()`.
     */
    rows& operator=(const rows& other) = default;

    /**
     * \brief Move assignment.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Iterators and references (including \ref rows_view's \ref row_view's and \ref
     * field_view's) to elements in `*this` are invalidated. Iterators and references to elements in
     * `other` remain valid.
     *
     * \par Complexity
     * Constant.
     */
    rows& operator=(rows&& other) = default;

    /**
     * \brief Destructor.
     */
    ~rows() = default;

    /**
     * \brief Constructs a rows object from a \ref rows_view.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `r`'s (the contents of `r` will be copied
     * into `*this`).
     *
     * \par Complexity
     * Linear on `r.size() * r.num_columns()`.
     */
    rows(const rows_view& r) : impl_(r.fields_, r.num_fields_), num_columns_(r.num_columns_) {}

    /**
     * \brief Replaces the contents of `*this` with a \ref rows_view.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `r`'s (the contents of `r` will be copied
     * into `*this`). Iterators and references (including \ref rows_view's \ref row_view's and \ref
     * field_view's) to elements in `*this` are invalidated.
     *
     * \par Complexity
     * Linear on `r.size() * r.num_columns()`.
     */
    rows& operator=(const rows_view& rhs)
    {
        impl_.assign(rhs.fields_, rhs.num_fields_);
        num_columns_ = rhs.num_columns_;
        return *this;
    }

    /// \copydoc rows_view::begin
    iterator begin() const noexcept { return iterator(impl_.fields().data(), num_columns_, 0); }

    /// \copydoc rows_view::end
    iterator end() const noexcept { return iterator(impl_.fields().data(), num_columns_, size()); }

    /// \copydoc rows_view::at
    row_view at(std::size_t i) const
    {
        if (i >= size())
            BOOST_THROW_EXCEPTION(std::out_of_range("rows::at"));
        return detail::row_slice(impl_.fields().data(), num_columns_, i);
    }

    /// \copydoc rows_view::operator[]
    row_view operator[](std::size_t i) const noexcept
    {
        BOOST_ASSERT(i < size());
        return detail::row_slice(impl_.fields().data(), num_columns_, i);
    }

    /// \copydoc rows_view::front
    row_view front() const noexcept { return (*this)[0]; }

    /// \copydoc rows_view::back
    row_view back() const noexcept { return (*this)[size() - 1]; }

    /// \copydoc rows_view::empty
    bool empty() const noexcept { return impl_.fields().empty(); }

    /// \copydoc rows_view::size
    std::size_t size() const noexcept { return num_columns_ == 0 ? 0 : impl_.fields().size() / num_columns_; }

    /// \copydoc rows_view::num_columns
    std::size_t num_columns() const noexcept { return num_columns_; }

    /**
     * \brief Creates a \ref rows_view that references `*this`.
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
    operator rows_view() const noexcept
    {
        return rows_view(impl_.fields().data(), impl_.fields().size(), num_columns_);
    }

private:
    detail::row_impl impl_;
    std::size_t num_columns_{};
};

/**
 * \relates rows
 * \brief Equality operator.
 * \details The containers are considered equal if they have the same number of rows and
 * they all compare equal, as defined by \ref row_view::operator==.
 *
 * \par Exception safety
 * No-throw guarantee.
 *
 * \par Complexity
 * Linear in `lhs.size() * lhs.num_columns()` and `rhs.size() * rhs.num_columns()`.
 */
inline bool operator==(const rows& lhs, const rows& rhs) noexcept { return rows_view(lhs) == rows_view(rhs); }

/**
 * \relates rows
 * \brief Inequality operator.
 *
 * \par Exception safety
 * No-throw guarantee.
 *
 * \par Complexity
 * Linear in `lhs.size() * lhs.num_columns()` and `rhs.size() * rhs.num_columns()`.
 */
inline bool operator!=(const rows& lhs, const rows& rhs) noexcept { return !(lhs == rhs); }

/**
 * \relates rows
 * \copydoc rows::operator==(const rows&, const rows&)
 */
inline bool operator==(const rows_view& lhs, const rows& rhs) noexcept { return lhs == rows_view(rhs); }

/**
 * \relates rows
 * \copydoc rows::operator!=(const rows&, const rows&)
 */
inline bool operator!=(const rows_view& lhs, const rows& rhs) noexcept { return !(lhs == rhs); }

/**
 * \relates rows
 * \copydoc rows::operator==(const rows&, const rows&)
 */
inline bool operator==(const rows& lhs, const rows_view& rhs) noexcept { return rows_view(lhs) == rhs; }

/**
 * \relates rows
 * \copydoc rows::operator!=(const rows&, const rows&)
 */
inline bool operator!=(const rows& lhs, const rows_view& rhs) noexcept { return !(lhs == rhs); }

}  // namespace mysql
}  // namespace boost

#endif
