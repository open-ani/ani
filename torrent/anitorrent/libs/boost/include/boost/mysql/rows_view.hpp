//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_ROWS_VIEW_HPP
#define BOOST_MYSQL_ROWS_VIEW_HPP

#include <boost/mysql/field_view.hpp>
#include <boost/mysql/row.hpp>
#include <boost/mysql/row_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/rows_iterator.hpp>

#include <boost/assert.hpp>
#include <boost/throw_exception.hpp>

#include <cstddef>
#include <stdexcept>

namespace boost {
namespace mysql {

/**
 * \brief A non-owning read-only reference to a sequence of rows.
 * \details
 * Models a non-owning matrix-like container. Indexing a `rows_view` object (by using iterators,
 * \ref rows_view::at or \ref rows_view::operator[]) returns a \ref row_view object, representing a
 * single row. All rows in the collection are the same size (as given by \ref num_columns).
 * \n
 * A `rows_view` object points to memory owned by an external entity (like `string_view` does). The
 * validity of a `rows_view` object depends on how it was obtained:
 * \n
 * \li If it was constructed from a \ref rows object (by calling \ref rows::operator rows_view()),
 *     the view acts as a reference to the `rows`' allocated memory, and is valid as long as
 *     references to that `rows` elements are valid.
 * \li If it was obtained by calling \ref connection::read_some_rows it's valid until the
 *     `connection` performs the next network call or is destroyed.
 * \n
 * \ref row_view's and \ref field_view's obtained by using a `rows_view` object are valid as long as
 * the underlying storage that `*this` points to is valid. Destroying `*this` doesn't invalidate
 * such references.
 * \n
 * Calling any member function on an invalid view results in undefined behavior.
 * \n
 * Instances of this class are usually created by the library, not by the user.
 */
class rows_view
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

    /// A type that can hold elements in this collection with value semantics.
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
     * \brief Construct an empty (but valid) view.
     * \par Exception safety
     * No-throw guarantee.
     */
    rows_view() = default;

    /**
     * \brief Returns an iterator to the first element in the collection.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    const_iterator begin() const noexcept { return iterator(fields_, num_columns_, 0); }

    /**
     * \brief Returns an iterator to one-past-the-last element in the collection.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    const_iterator end() const noexcept { return iterator(fields_, num_columns_, size()); }

    /**
     * \brief Returns the i-th row or throws an exception.
     * \par Exception safety
     * Strong guranatee. Throws on invalid input.
     * \throws std::out_of_range `i >= this->size()`
     *
     * \par Complexity
     * Constant.
     */
    row_view at(std::size_t i) const
    {
        if (i >= size())
            BOOST_THROW_EXCEPTION(std::out_of_range("rows_view::at"));
        return detail::row_slice(fields_, num_columns_, i);
    }

    /**
     * \brief Returns the i-th row (unchecked access).
     * \par Preconditions
     * `i < this->size()`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    row_view operator[](std::size_t i) const noexcept
    {
        BOOST_ASSERT(i < size());
        return detail::row_slice(fields_, num_columns_, i);
    }

    /**
     * \brief Returns the first row.
     * \par Preconditions
     * `this->size() > 0`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    row_view front() const noexcept { return (*this)[0]; }

    /**
     * \brief Returns the last row.
     * \par Preconditions
     * `this->size() > 0`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    row_view back() const noexcept { return (*this)[size() - 1]; }

    /**
     * \brief Returns true if there are no rows in the collection (i.e. `this->size() == 0`)
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    bool empty() const noexcept { return num_fields_ == 0; }

    /**
     * \brief Returns the number of rows in the collection.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    std::size_t size() const noexcept { return (num_columns_ == 0) ? 0 : (num_fields_ / num_columns_); }

    /**
     * \brief Returns the number of elements each row in the collection has.
     * \details For every \ref row_view object r obtained from this collection,
     * `r.size() == this->num_columns()`.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    std::size_t num_columns() const noexcept { return num_columns_; }

    /**
     * \brief Equality operator.
     * \details The containers are considered equal if they have the same number of rows and
     * they all compare equal, as defined by \ref row_view::operator==.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Linear on `this->size() * this->num_columns()`.
     */
    bool operator==(const rows_view& rhs) const noexcept
    {
        if (num_fields_ != rhs.num_fields_ || num_columns_ != rhs.num_columns_)
            return false;
        for (std::size_t i = 0; i < num_fields_; ++i)
        {
            if (fields_[i] != rhs.fields_[i])
                return false;
        }
        return true;
    }

    /**
     * \brief Inequality operator.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Linear on `this->size() * this->num_columns()`.
     */
    inline bool operator!=(const rows_view& rhs) const noexcept { return !(*this == rhs); }

private:
    const field_view* fields_{};
    std::size_t num_fields_{};
    std::size_t num_columns_{};

    rows_view(const field_view* fields, std::size_t num_fields, std::size_t num_columns) noexcept
        : fields_(fields), num_fields_(num_fields), num_columns_(num_columns)
    {
        BOOST_ASSERT(fields != nullptr || num_fields == 0);  // fields null => num_fields 0
        BOOST_ASSERT(num_fields == 0 || num_columns != 0);   // num_fields != 0 => num_columns != 0
        BOOST_ASSERT(num_columns == 0 || (num_fields % num_columns == 0));
    }

#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
    friend class rows;
#endif
};

}  // namespace mysql
}  // namespace boost

#endif
