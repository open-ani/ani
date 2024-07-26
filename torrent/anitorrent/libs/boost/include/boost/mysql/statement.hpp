//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_STATEMENT_HPP
#define BOOST_MYSQL_STATEMENT_HPP

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/writable_field_traits.hpp>

#include <boost/assert.hpp>

#include <cstdint>
#include <tuple>
#include <type_traits>

namespace boost {
namespace mysql {

/**
 * \brief A statement with bound parameters, represented as a `std::tuple`.
 * \details
 * This class satisfies `ExecutionRequest`. You can pass instances of this class to \ref connection::execute,
 * \ref connection::start_execution or their async counterparts.
 */
template <BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple>
class bound_statement_tuple;

/**
 * \brief A statement with bound parameters, represented as an iterator range.
 * \details
 * This class satisfies `ExecutionRequest`. You can pass instances of this class to \ref connection::execute,
 * \ref connection::start_execution or their async counterparts.
 */
template <BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR FieldViewFwdIterator>
class bound_statement_iterator_range;

/**
 * \brief Represents a server-side prepared statement.
 * \details
 * This is a lightweight class, holding a handle to a server-side prepared statement.
 * \n
 * Note that statement's destructor doesn't deallocate the statement from the
 * server, as this implies a network transfer that may fail.
 *
 * \par Thread safety
 * Distinct objects: safe. \n
 * Shared objects: unsafe. \n
 */
class statement
{
public:
    /**
     * \brief Default constructor.
     * \details Default constructed statements have `this->valid() == false`.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    statement() = default;

    /**
     * \brief Returns `true` if the object represents an actual server statement.
     * \details Calling any function other than assignment on a statement for which
     * this function returns `false` results in undefined behavior.
     * \n
     * Returns `false` for default-constructed statements.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    bool valid() const noexcept { return valid_; }

    /**
     * \brief Returns a server-side identifier for the statement (unique in a per-connection basis).
     * \details Note that, once a statement is closed, the server may recycle its ID.
     *
     * \par Preconditions
     * `this->valid() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    std::uint32_t id() const noexcept
    {
        BOOST_ASSERT(valid());
        return id_;
    }

    /**
     * \brief Returns the number of parameters that should be provided when executing the statement.
     * \par Preconditions
     * `this->valid() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    unsigned num_params() const noexcept
    {
        BOOST_ASSERT(valid());
        return num_params_;
    }

    /**
     * \brief Binds parameters to a statement.
     * \details
     * Creates an object that packages `*this` and the statement actual parameters `params`.
     * This object can be passed to \ref connection::execute, \ref connection::start_execution
     * and their async counterparts.
     * \n
     * The parameters are copied into a `std::tuple` by using `std::make_tuple`. This function
     * only participates in overload resolution if `std::make_tuple(FWD(args)...)` yields a
     * `WritableFieldTuple`. Equivalent to `this->bind(std::make_tuple(std::forward<T>(params)...))`.
     * \n
     * This function doesn't involve communication with the server.
     *
     * \par Preconditions
     * `this->valid() == true`
     * \n
     * \par Exception safety
     * Strong guarantee. Only throws if constructing any of the internal tuple elements throws.
     */
    template <class... T>
#ifdef BOOST_MYSQL_DOXYGEN
    bound_statement_tuple<std::tuple<__see_below__>>
#else
    auto
#endif
    bind(T&&... params) const->typename std::enable_if<
        detail::is_writable_field_tuple<decltype(std::make_tuple(std::forward<T>(params)...))>::value,
        bound_statement_tuple<decltype(std::make_tuple(std::forward<T>(params)...))>>::type
    {
        return bind(std::make_tuple(std::forward<T>(params)...));
    }

    /**
     * \brief Binds parameters to a statement.
     * \details
     * Creates an object that packages `*this` and the statement actual parameters `params`.
     * This object can be passed to \ref connection::execute, \ref connection::start_execution
     * or their async counterparts.
     * \n
     * The `params` tuple is decay-copied into the returned object.
     * \n
     * This function doesn't involve communication with the server.
     *
     * \par Preconditions
     * `this->valid() == true`
     * \n
     * \par Exception safety
     * Strong guarantee. Only throws if the decay-copy of the tuple throws.
     */
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        typename EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    bound_statement_tuple<typename std::decay<WritableFieldTuple>::type> bind(WritableFieldTuple&& params
    ) const;

    /**
     * \brief Binds parameters to a statement (iterator range overload).
     * \details
     * Creates an object that packages `*this` and the statement actual parameters, represented
     * as the iterator range `[params_first, params_last)`.
     * This object can be passed to \ref connection::execute, \ref connection::start_execution
     * or their async counterparts.
     * \n
     * This function doesn't involve communication with the server.
     *
     * \par Preconditions
     * `this->valid() == true`
     * \n
     * \par Exception safety
     * Strong guarantee. Only throws if copy-constructing iterators throws.
     */
    template <
        BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR FieldViewFwdIterator,
        typename EnableIf = typename std::enable_if<
            detail::is_field_view_forward_iterator<FieldViewFwdIterator>::value>::type>
    bound_statement_iterator_range<FieldViewFwdIterator> bind(
        FieldViewFwdIterator params_first,
        FieldViewFwdIterator params_last
    ) const;

private:
    bool valid_{false};
    std::uint32_t id_{0};
    std::uint16_t num_params_{0};

    statement(std::uint32_t id, std::uint16_t num_params) noexcept
        : valid_(true), id_(id), num_params_(num_params)
    {
    }

#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
#endif
};

}  // namespace mysql
}  // namespace boost

#include <boost/mysql/impl/statement.hpp>

#endif
