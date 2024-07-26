//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_ROW_VIEW_HPP
#define BOOST_MYSQL_ROW_VIEW_HPP

#include <boost/mysql/field.hpp>
#include <boost/mysql/field_view.hpp>

#include <boost/mysql/detail/access.hpp>

#include <boost/throw_exception.hpp>

#include <cstddef>
#include <stdexcept>
#include <vector>

namespace boost {
namespace mysql {

/**
 * \brief A non-owning read-only reference to a sequence of fields.
 * \details
 * A `row_view` points to memory owned by an external entity (like `string_view` does). The validity
 * of a `row_view` depends on how it was obtained:
 * \n
 * \li If it was constructed from a \ref row object (by calling \ref row::operator row_view()), the
 *     view acts as a reference to the row's allocated memory, and is valid as long as references
 *     to that row elements are valid.
 * \li If it was obtained by indexing a \ref rows object, the same applies.
 * \li If it was obtained by indexing a \ref rows_view object, it's valid as long as the
 *    `rows_view` is valid.
 * \n
 * Calling any member function on an invalid view results in undefined behavior.
 * \n
 * When indexed (by using iterators, \ref row_view::at or \ref row_view::operator[]), it returns
 * \ref field_view elements that are valid as long as the underlying storage that `*this` points
 * to is valid. Destroying a `row_view` doesn't invalidate `field_view`s obtained from
 * it.
 * \n Instances of this class are usually created by the library, not by the user.
 */
class row_view
{
public:
    /**
     * \brief Constructs an empty (but valid) view.
     * \par Exception safety
     * No-throw guarantee.
     */
    row_view() = default;

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
     * \brief Returns an iterator to the first field in the row.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    iterator begin() const noexcept { return fields_; }

    /**
     * \brief Returns an iterator to one-past-the-last field in the row.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    iterator end() const noexcept { return fields_ + size_; }

    /**
     * \brief Returns the i-th element in the row or throws an exception.
     * \par Exception safety
     * Strong guranatee. Throws on invalid input.
     * \throws std::out_of_range `i >= this->size()`
     *
     * \par Complexity
     * Constant.
     */
    field_view at(std::size_t i) const
    {
        if (i >= size_)
            BOOST_THROW_EXCEPTION(std::out_of_range("row_view::at"));
        return fields_[i];
    }

    /**
     * \brief Returns the i-th element in the row (unchecked access).
     * \par Preconditions
     * `i < this->size()`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    field_view operator[](std::size_t i) const noexcept { return fields_[i]; }

    /**
     * \brief Returns the first element in the row.
     * \par Preconditions
     * `this->size() > 0`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    field_view front() const noexcept { return *fields_; }

    /**
     * \brief Returns the last element in the row.
     * \par Preconditions
     * `this->size() > 0`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    field_view back() const noexcept { return fields_[size_ - 1]; }

    /**
     * \brief Returns true if there are no fields in the row (i.e. `this->size() == 0`).
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    bool empty() const noexcept { return size_ == 0; }

    /**
     * \brief Returns the number of fields in the row.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    std::size_t size() const noexcept { return size_; }

    /**
     * \brief Converts the row into a `std::vector` of \ref field's.
     * \details As \ref row objects are read-only, you can use this function if you need to mutate
     * fields in a row.
     *
     * \par Exception safety
     * Basic guarantee. Allocations may throw.
     *
     * \par Complexity
     * Linear in `this->size()`.
     */
    template <class Allocator>
    void as_vector(std::vector<field, Allocator>& out) const
    {
        out.assign(begin(), end());
    }

    /// \copydoc as_vector
    std::vector<field> as_vector() const { return std::vector<field>(begin(), end()); }

#ifndef BOOST_MYSQL_DOXYGEN
    // Required by iterators
    const row_view* operator->() const noexcept { return this; }
#endif

private:
    row_view(const field_view* f, std::size_t size) noexcept : fields_(f), size_(size) {}
    const field_view* fields_{};
    std::size_t size_{};

#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
    friend class row;
#endif
};

/**
 * \relates row_view
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
inline bool operator==(const row_view& lhs, const row_view& rhs) noexcept
{
    if (lhs.size() != rhs.size())
        return false;
    for (std::size_t i = 0; i < lhs.size(); ++i)
    {
        if (lhs[i] != rhs[i])
            return false;
    }
    return true;
}

/**
 * \relates row_view
 * \brief Inequality operator.
 *
 * \par Exception safety
 * No-throw guarantee.
 *
 * \par Complexity
 * Linear in `lhs.size()` and `rhs.size()`.
 */
inline bool operator!=(const row_view& lhs, const row_view& rhs) noexcept { return !(lhs == rhs); }

}  // namespace mysql
}  // namespace boost

#endif
