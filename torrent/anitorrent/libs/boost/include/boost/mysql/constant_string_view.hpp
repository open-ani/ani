//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_CONSTANT_STRING_VIEW_HPP
#define BOOST_MYSQL_CONSTANT_STRING_VIEW_HPP

#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/config.hpp>

#include <type_traits>

namespace boost {
namespace mysql {

/**
 * \brief (EXPERIMENTAL) A string view that should be known at compile-time.
 * \details
 * This type is used when a string function argument must always be known at compile-time
 * except in rare cases. See \ref format_sql format strings for an example.
 * \n
 * \par Object lifetimes
 * This type holds internally a \ref string_view, and follows the same lifetime rules as `string_view`.
 * We recommend to only use this type as a function argument, to provide compile-time checks.
 */
class constant_string_view
{
    string_view impl_;

#ifndef BOOST_MYSQL_DOXYGEN
    constexpr constant_string_view(string_view value, int) noexcept : impl_(value) {}
    friend constexpr constant_string_view runtime(string_view) noexcept;
#endif

public:
    /**
     * \brief Consteval constructor.
     * \details
     * Constructs a \ref string_view from the passed argument.
     * \n
     * This function is `consteval`: it results in a compile-time error
     * if the passed value is not known at compile-time. You can bypass
     * this check using the \ref runtime function. This check works only
     * for C++20 and above. No check is performed for lower C++ standard versions.
     * \n
     * This constructor is only considered if a \ref string_view can be constructed
     * from the passed value.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * Ownership is not transferred to the constructed object. As with `string_view`,
     * the user is responsible for keeping the original character buffer alive.
     */
    template <
        class T
#ifndef BOOST_MYSQL_DOXYGEN
        ,
        class = typename std::enable_if<std::is_convertible<const T&, string_view>::value>::type
#endif
        >
    BOOST_MYSQL_CONSTEVAL constant_string_view(const T& value) noexcept : impl_(value)
    {
    }

    /**
     * \brief Retrieves the underlying string view.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Object lifetimes
     * The returned view has the same lifetime rules as `*this`.
     */
    constexpr string_view get() const noexcept { return impl_; }
};

/**
 * \brief (EXPERIMENTAL) Creates a \ref constant_string_view from a runtime value.
 * \details
 * You can use this function to bypass the `consteval` check performed by \ref constant_string_view
 * constructor.
 * \n
 * Don't use this function unless you know what you are doing. `consteval` checks exist
 * for the sake of security. Make sure to only pass trusted values to the relevant API.
 *
 * \par Exception safety
 * No-throw guarantee.
 *
 * \par Object lifetimes
 * The returned value has the same lifetime semantics as the passed view.
 */
constexpr constant_string_view runtime(string_view value) noexcept { return constant_string_view(value, 0); }

}  // namespace mysql
}  // namespace boost

#endif
