//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_STATIC_RESULTS_HPP
#define BOOST_MYSQL_STATIC_RESULTS_HPP

#include <boost/mysql/detail/config.hpp>

#ifdef BOOST_MYSQL_CXX14

#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/execution_processor/static_results_impl.hpp>

#include <boost/assert.hpp>

namespace boost {
namespace mysql {

/**
 * \brief Holds the results of a SQL query (static interface).
 * \details
 * This object can store the results of single and multi resultset queries
 * in a type-safe manner.
 *
 * \tparam StaticRow The row or row types that will be returned by the server.
 * There must be one for every resultset returned by the query, and always at least one.
 * All the passed types must fulfill the `StaticRow` concept.
 *
 * \par Thread safety
 * Distinct objects: safe. \n
 * Shared objects: unsafe. \n
 */
template <class... StaticRow>
class static_results
{
public:
    /**
     * \brief Default constructor.
     * \details Constructs an empty results object, with `this->has_value() == false`.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    static_results() = default;

    /**
     * \brief Copy constructor.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     */
    static_results(const static_results& other) = default;

    /**
     * \brief Move constructor.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * View objects obtained from `other` remain valid.
     */
    static_results(static_results&& other) = default;

    /**
     * \brief Copy assignment.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * Views referencing `*this` are invalidated.
     */
    static_results& operator=(const static_results& other) = default;

    /**
     * \brief Move assignment.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * View objects obtained from `other` remain valid.
     * Views and referencing `*this` are invalidated.
     */
    static_results& operator=(static_results&& other) = default;

    /// Destructor
    ~static_results() = default;

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
    bool has_value() const noexcept { return impl_.get_interface().is_complete(); }

    /**
     * \brief Returns the rows retrieved by the SQL query.
     * \details
     *
     * \tparam I Resultset index. For operations returning more than one resultset, you can explicitly
     * specify this parameter to obtain the rows contained in the i-th resultset. If left unspecified,
     * rows for the first resultset are returned.
     *
     * \return Returns a read-only span of the `I`-th row type.
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
    template <std::size_t I = 0>
#ifdef BOOST_MYSQL_DOXYGEN
    boost::span<const StaticRow... [I]>
#else
    boost::span<const typename std::tuple_element<I, std::tuple<StaticRow...> >::type>
#endif
    rows() const noexcept {
        static_assert(I < sizeof...(StaticRow), "Index I out of range");
        BOOST_ASSERT(has_value());
        return impl_.template get_rows<I>();
    }

    /**
     * \brief Returns metadata about the columns in the query.
     * \details
     * The returned collection will have as many \ref metadata objects as columns retrieved by
     * the SQL query, and in the same order. Note that this may not be the same order as in the `StaticRow`
     * type, since columns may be mapped by name or discarded. This function returns the representation that
     * was retrieved from the database.
     *
     * \tparam I Resultset index. For operations returning more than one resultset, you can explicitly
     * specify this parameter to obtain metadata for the i-th resultset. If left unspecified,
     * metadata for the first resultset is returned.
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
    template <std::size_t I = 0>
    metadata_collection_view meta() const noexcept
    {
        static_assert(I < sizeof...(StaticRow), "Index I out of range");
        BOOST_ASSERT(has_value());
        return impl_.get_interface().get_meta(I);
    }

    /**
     * \brief Returns the number of rows affected by the executed SQL statement.
     * \details
     * \tparam I Resultset index. For operations returning more than one resultset, you can explicitly
     * specify this parameter to obtain the number of affected rows by the i-th resultset. If left
     * unspecified, the number of affected rows by the first resultset is returned.
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
    template <std::size_t I = 0>
    std::uint64_t affected_rows() const noexcept
    {
        static_assert(I < sizeof...(StaticRow), "Index I out of range");
        BOOST_ASSERT(has_value());
        return impl_.get_interface().get_affected_rows(I);
    }

    /**
     * \brief Returns the last insert ID produced by the executed SQL statement.
     * \details
     * \tparam I Resultset index. For operations returning more than one resultset, you can explicitly
     * specify this parameter to obtain the last insert ID for the i-th resultset. If left unspecified,
     * the last insert ID for the first resultset is returned.
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
    template <std::size_t I = 0>
    std::uint64_t last_insert_id() const noexcept
    {
        static_assert(I < sizeof...(StaticRow), "I index out of range");
        BOOST_ASSERT(has_value());
        return impl_.get_interface().get_last_insert_id(I);
    }

    /**
     * \brief Returns the number of warnings produced by the executed SQL statement.
     * \details
     * \tparam I Resultset index. For operations returning more than one resultset, you can explicitly
     * specify this parameter to obtain the warning count for the i-th resultset. If left unspecified,
     * the warning count for the first resultset is returned.
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
    template <std::size_t I = 0>
    unsigned warning_count() const noexcept
    {
        static_assert(I < sizeof...(StaticRow), "I index out of range");
        BOOST_ASSERT(has_value());
        return impl_.get_interface().get_warning_count(I);
    }

    /**
     * \brief Returns additional text information about the execution of the SQL statement.
     * \details
     * The format of this information is documented by MySQL <a
     * href="https://dev.mysql.com/doc/c-api/8.0/en/mysql-info.html">here</a>.
     * \n
     * The returned string always uses ASCII encoding, regardless of the connection's character set.
     *
     * \tparam I Resultset index. For operations returning more than one resultset, you can explicitly
     * specify this parameter to obtain the value for the i-th resultset. If left unspecified,
     * the value for the first resultset is returned.
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
    template <std::size_t I = 0>
    string_view info() const noexcept
    {
        static_assert(I < sizeof...(StaticRow), "I index out of range");
        BOOST_ASSERT(has_value());
        return impl_.get_interface().get_info(I);
    }

private:
    detail::static_results_impl<StaticRow...> impl_;
#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
#endif
};

}  // namespace mysql
}  // namespace boost

#endif  // BOOST_MYSQL_CXX14

#endif
