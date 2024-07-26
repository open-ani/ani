//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_RESULTS_HPP
#define BOOST_MYSQL_RESULTS_HPP

#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/resultset.hpp>
#include <boost/mysql/resultset_view.hpp>
#include <boost/mysql/rows_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/execution_processor/results_impl.hpp>
#include <boost/mysql/detail/results_iterator.hpp>

#include <boost/assert.hpp>
#include <boost/throw_exception.hpp>

#include <stdexcept>

namespace boost {
namespace mysql {

/**
 * \brief Holds the results of a SQL query (dynamic interface).
 * \details
 * This object can store the results of single and multi resultset queries.
 * For the former, you use \ref meta, \ref rows, \ref affected_rows and so on.
 * For the latter, this class is a random-access collection of \ref resultset objects.
 * \n
 * \par Thread safety
 * Distinct objects: safe. \n
 * Shared objects: unsafe. \n
 */
class results
{
public:
#ifdef BOOST_MYSQL_DOXYGEN
    /**
     * \brief A random access iterator to an element.
     * \details The exact type of the iterator is unspecified.
     */
    using iterator = __see_below__;
#else
    using iterator = detail::results_iterator;
#endif

    /// \copydoc iterator
    using const_iterator = iterator;

    /// A type that can hold elements in this collection with value semantics.
    using value_type = resultset;

    /// The reference type.
    using reference = resultset_view;

    /// \copydoc reference
    using const_reference = resultset_view;

    /// An unsigned integer type to represent sizes.
    using size_type = std::size_t;

    /// A signed integer type used to represent differences.
    using difference_type = std::ptrdiff_t;

    /**
     * \brief Default constructor.
     * \details Constructs an empty results object, with `this->has_value() == false`.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    results() = default;

    /**
     * \brief Copy constructor.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     */
    results(const results& other) = default;

    /**
     * \brief Move constructor.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * View objects obtained from `other` using \ref rows and \ref meta remain valid.
     * Any other views and iterators referencing `other` are invalidated.
     */
    results(results&& other) = default;

    /**
     * \brief Copy assignment.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * Views and iterators referencing `*this` are invalidated.
     */
    results& operator=(const results& other) = default;

    /**
     * \brief Move assignment.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * View objects obtained from `other` using \ref rows and \ref meta remain valid.
     * Any other views and iterators referencing `other` are invalidated. Views and iterators
     * referencing `*this` are invalidated.
     */
    results& operator=(results&& other) = default;

    /// Destructor
    ~results() = default;

    /**
     * \brief Returns whether the object holds a valid result.
     * \details Having `this->has_value()` is a precondition to call all data accessors.
     * Objects populated by \ref connection::execute and \ref connection::async_execute
     * are guaranteed to have `this->has_value() == true`.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    bool has_value() const noexcept { return impl_.is_complete(); }

    /**
     * \brief Returns the rows retrieved by the SQL query.
     * \details
     * For operations returning more than one resultset, returns the rows
     * for the first resultset.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * This function returns a view object, with reference semantics. The returned view points into
     * memory owned by `*this`, and will be valid as long as `*this` or an object move-constructed
     * from `*this` are alive.
     *
     * \par Complexity
     * Constant.
     */
    rows_view rows() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_.get_rows(0);
    }

    /**
     * \brief Returns metadata about the columns in the query.
     * \details
     * The returned collection will have as many \ref metadata objects as columns retrieved by
     * the SQL query, and in the same order.
     * \n
     * For operations returning more than one resultset, returns metadata
     * for the first resultset.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * This function returns a view object, with reference semantics. The returned view points into
     * memory owned by `*this`, and will be valid as long as `*this` or an object move-constructed
     * from `*this` are alive.
     *
     * \par Complexity
     * Constant.
     */
    metadata_collection_view meta() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_.get_meta(0);
    }

    /**
     * \brief Returns the number of rows affected by the executed SQL statement.
     * \details
     * For operations returning more than one resultset, returns the
     * first resultset's affected rows.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    std::uint64_t affected_rows() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_.get_affected_rows(0);
    }

    /**
     * \brief Returns the last insert ID produced by the executed SQL statement.
     * \details
     * For operations returning more than one resultset, returns the
     * first resultset's last insert ID.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    std::uint64_t last_insert_id() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_.get_last_insert_id(0);
    }

    /**
     * \brief Returns the number of warnings produced by the executed SQL statement.
     * \details
     * For operations returning more than one resultset, returns the
     * first resultset's warning count.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    unsigned warning_count() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_.get_warning_count(0);
    }

    /**
     * \brief Returns additional text information about the execution of the SQL statement.
     * \details
     * The format of this information is documented by MySQL <a
     * href="https://dev.mysql.com/doc/c-api/8.0/en/mysql-info.html">here</a>.
     * \n
     * The returned string always uses ASCII encoding, regardless of the connection's character set.
     * \n
     * For operations returning more than one resultset, returns the
     * first resultset's info.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * This function returns a view object, with reference semantics. The returned view points into
     * memory owned by `*this`, and will be valid as long as `*this` or an object move-constructed
     * from `*this` are alive.
     *
     * \par Complexity
     * Constant.
     */
    string_view info() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_.get_info(0);
    }

    /**
     * \brief Returns an iterator pointing to the first resultset that this object contains.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned iterator and any reference obtained from it are valid as long as
     * `*this` is alive. Move operations invalidate iterators.
     *
     * \par Complexity
     * Constant.
     */
    iterator begin() const noexcept
    {
        BOOST_ASSERT(has_value());
        return iterator(&impl_, 0);
    }

    /**
     * \brief Returns an iterator pointing to one-past-the-last resultset that this object contains.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned iterator and any reference obtained from it are valid as long as
     * `*this` is alive. Move operations invalidate iterators.
     *
     * \par Complexity
     * Constant.
     */
    iterator end() const noexcept
    {
        BOOST_ASSERT(has_value());
        return iterator(&impl_, size());
    }

    /**
     * \brief Returns the i-th resultset or throws an exception.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * Strong guranatee. Throws on invalid input.
     * \throws std::out_of_range `i >= this->size()`
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * `*this` is alive. Move operations invalidate references.
     *
     * \par Complexity
     * Constant.
     */
    inline resultset_view at(std::size_t i) const
    {
        BOOST_ASSERT(has_value());
        if (i >= size())
            BOOST_THROW_EXCEPTION(std::out_of_range("results::at: out of range"));
        return detail::access::construct<resultset_view>(impl_, i);
    }

    /**
     * \brief Returns the i-th resultset (unchecked access).
     * \par Preconditions
     * `this->has_value() == true && i < this->size()`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * `*this` is alive. Move operations invalidate references.
     *
     * \par Complexity
     * Constant.
     */
    resultset_view operator[](std::size_t i) const noexcept
    {
        BOOST_ASSERT(has_value());
        BOOST_ASSERT(i < size());
        return detail::access::construct<resultset_view>(impl_, i);
    }

    /**
     * \brief Returns the first resultset.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * `*this` is alive. Move operations invalidate references.
     *
     * \par Complexity
     * Constant.
     */
    resultset_view front() const noexcept { return (*this)[0]; }

    /**
     * \brief Returns the last resultset.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * `*this` is alive. Move operations invalidate references.
     *
     * \par Complexity
     * Constant.
     */
    resultset_view back() const noexcept { return (*this)[size() - 1]; }

    /**
     * \brief Returns whether the collection contains any resultset.
     * \details
     * This function is provided for compatibility with standard collections,
     * and always returns false, since any valid `results` contains at least one resultset.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    bool empty() const noexcept
    {
        BOOST_ASSERT(has_value());
        return false;
    }

    /**
     * \brief Returns the number of resultsets that this collection contains.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    std::size_t size() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_.num_resultsets();
    }

    /**
     * \brief Returns the output parameters of a stored procedure call.
     * \details
     * Relevant for `CALL` operations performed using prepared statements that
     * bind placeholders to `OUT` or `INOUT` parameters. Returns a row containing a field per
     * bound output parameter.
     * \n
     * If this operation had no output parameters (e.g. it wasn't a `CALL`), returns an empty row.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * `*this` is alive. Move operations invalidate references.
     *
     * \par Complexity
     * Linear on `this->size()`.
     */
    row_view out_params() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_.get_out_params();
    }

private:
    detail::results_impl impl_;
#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
#endif
};

}  // namespace mysql
}  // namespace boost

#endif
