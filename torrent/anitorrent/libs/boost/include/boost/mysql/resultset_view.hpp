//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_RESULTSET_VIEW_HPP
#define BOOST_MYSQL_RESULTSET_VIEW_HPP

#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/rows_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/execution_processor/results_impl.hpp>

#include <boost/assert.hpp>

namespace boost {
namespace mysql {

/**
 * \brief A non-owning reference to a resultset.
 * \details
 * A `resultset_view` points to memory owned by an external object, usually a \ref results.
 * The view and any other reference type obtained from it are valid as long as the
 * object they point to is alive.
 */
class resultset_view
{
public:
    /**
     * \brief Constructs a view with `this->has_value() == false`.
     * \par Exception safety
     * No-throw guarantee.
     */
    resultset_view() = default;

    /**
     * \brief Returns whether this is a null view or not.
     * \details
     * Only returns true for default-constructed views.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Complexity
     * Constant.
     */
    bool has_value() const noexcept { return impl_ != nullptr; }

    /**
     * \brief Returns the rows for this resultset.
     * \par Preconditions
     * `this->has_value() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned reference and any other references obtained from it are valid as long as
     * the object that `*this` points to is alive.
     *
     * \par Complexity
     * Constant.
     */
    rows_view rows() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_->get_rows(index_);
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
     * the object that `*this` points to is alive.
     *
     * \par Complexity
     * Constant.
     */
    metadata_collection_view meta() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_->get_meta(index_);
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
        BOOST_ASSERT(has_value());
        return impl_->get_affected_rows(index_);
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
        BOOST_ASSERT(has_value());
        return impl_->get_last_insert_id(index_);
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
        BOOST_ASSERT(has_value());
        return impl_->get_warning_count(index_);
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
     * the object that `*this` points to is alive.
     *
     * \par Complexity
     * Constant.
     */
    string_view info() const noexcept
    {
        BOOST_ASSERT(has_value());
        return impl_->get_info(index_);
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
        BOOST_ASSERT(has_value());
        return impl_->get_is_out_params(index_);
    }

#ifndef BOOST_MYSQL_DOXYGEN
    const resultset_view* operator->() const noexcept { return this; }
#endif

private:
    const detail::results_impl* impl_{};
    std::size_t index_{};

    resultset_view(const detail::results_impl& impl, std::size_t index) noexcept : impl_(&impl), index_(index)
    {
    }

#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
#endif
};

}  // namespace mysql
}  // namespace boost

#endif
