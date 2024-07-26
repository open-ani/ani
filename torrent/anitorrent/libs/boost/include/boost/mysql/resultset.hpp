//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_RESULTSET_HPP
#define BOOST_MYSQL_RESULTSET_HPP

#include <boost/mysql/metadata.hpp>
#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/resultset_view.hpp>
#include <boost/mysql/row_view.hpp>
#include <boost/mysql/rows.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/assert.hpp>

namespace boost {
namespace mysql {

/**
 * \brief An owning resultset, containing metadata, rows and additional info.
 * \details
 * Similar to \ref results, but can only represent a single resultset (while `results` can hold
 * multiple resultsets). Can be used to take ownership of a \ref resultset_view.
 */
class resultset
{
public:
    /**
     * \brief Constructs an empty resultset.
     * \details
     * The constructed object has `this->has_value() == false`.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    resultset() = default;

    /**
     * \brief Copy constructor.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `other`'s.
     *
     * \par Complexity
     * Linear on rows and metadata size for `other`.
     */
    resultset(const resultset& other) = default;

    /**
     * \brief Move constructor.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Views obtained from `other` remain valid.
     *
     * \par Complexity
     * Constant.
     */
    resultset(resultset&& other) = default;

    /**
     * \brief Copy assignment.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `other`'s. Views obtained from `*this`
     * are invalidated.
     *
     * \par Complexity
     * Linear on rows and metadata size for `other`.
     */
    resultset& operator=(const resultset& other) = default;

    /**
     * \brief Move assignment.
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Views obtained from `*this` are invalidated. Views obtained from `other` remain valid.
     *
     * \par Complexity
     * Constant.
     */
    resultset& operator=(resultset&& other) = default;

    /**
     * \brief Destructor.
     */
    ~resultset() = default;

    /**
     * \brief Constructs a resultset object by taking ownership of a view.
     * \par Exception safety
     * Strong guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `v`'s (the contents of `v` will be copied
     * into `*this`).
     *
     * \par Complexity
     * Linear on rows and metadata size for `v`.
     */
    resultset(resultset_view v) { assign(v); }

    /**
     * \brief Replaces the contents of `*this` with a \ref resultset_view.
     * \par Exception safety
     * Basic guarantee. Internal allocations may throw.
     *
     * \par Object lifetimes
     * `*this` lifetime will be independent of `v`'s (the contents of `v` will be copied
     * into `*this`). Views obtained from `*this` are invalidated.
     *
     * \par Complexity
     * Linear on rows and metadata size for `v` and `*this`.
     */
    resultset& operator=(resultset_view v)
    {
        assign(v);
        return *this;
    }

    /**
     * \brief Returns whether this object contains actual data or not.
     * \details
     * Only returns true for default-constructed objects.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    bool has_value() const noexcept { return has_value_; }

    /**
     * \brief Returns the rows that this resultset contains.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * `*this`  or an object move-constructed from `*this` are alive.
     *
     * \par Complexity
     * Constant.
     */
    rows_view rows() const noexcept
    {
        BOOST_ASSERT(has_value_);
        return rws_;
    }

    /**
     * \brief Returns metadata for this resultset.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * `*this`  or an object move-constructed from `*this` are alive.
     *
     * \par Complexity
     * Constant.
     */
    metadata_collection_view meta() const noexcept
    {
        BOOST_ASSERT(has_value_);
        return meta_;
    }

    /**
     * \brief Returns the number of affected rows for this resultset.
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
        BOOST_ASSERT(has_value_);
        return affected_rows_;
    }

    /**
     * \brief Returns the last insert ID for this resultset.
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
        BOOST_ASSERT(has_value_);
        return last_insert_id_;
    }

    /**
     * \brief Returns the number of warnings for this resultset.
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
        BOOST_ASSERT(has_value_);
        return warnings_;
    }

    /**
     * \brief Returns additional information for this resultset.
     * \details
     * The format of this information is documented by MySQL <a
     * href="https://dev.mysql.com/doc/c-api/8.0/en/mysql-info.html">here</a>.
     * \n
     * The returned string always uses ASCII encoding, regardless of the connection's character set.
     *
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * `*this`  or an object move-constructed from `*this` are alive.
     *
     * \par Complexity
     * Constant.
     */
    string_view info() const noexcept
    {
        BOOST_ASSERT(has_value_);
        return string_view(info_.data(), info_.size());
    }

    /**
     * \brief Returns whether this resultset represents a procedure OUT params.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    bool is_out_params() const noexcept
    {
        BOOST_ASSERT(has_value_);
        return is_out_params_;
    }

private:
    bool has_value_{false};
    std::vector<metadata> meta_;
    ::boost::mysql::rows rws_;
    std::uint64_t affected_rows_{};
    std::uint64_t last_insert_id_{};
    std::uint16_t warnings_{};
    std::vector<char> info_;
    bool is_out_params_{false};

    BOOST_MYSQL_DECL
    void assign(resultset_view v);
};

}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/resultset.ipp>
#endif

#endif
