//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_VISIT_HPP
#define BOOST_JSON_VISIT_HPP

#include <boost/json/detail/config.hpp>
#include <boost/json/value.hpp>
#include <type_traits>
#include <utility>

namespace boost {
namespace json {

/** Invoke a function object with the contents of a @ref value

    Invokes `v` as if by `std::forward<Visitor>(v)( X )`, where `X` is

    @li `jv.get_array()` if `jv.is_array()`, or

    @li `jv.get_object()` if `jv.is_object()`, or

    @li `jv.get_string()` if `jv.is_string()`, or

    @li `jv.get_int64()` if `jv.is_int64()`, or

    @li `jv.get_uint64()` if `jv.is_uint64()`, or

    @li `jv.get_double()` if `jv.is_double()`, or

    @li `jv.get_bool()` if `jv.is_bool()`, or

    @li reference to an object of type `std::nullptr_t` if `jv.is_null()`.

    @return The value returned by Visitor.

    @param v The visitation function to invoke

    @param jv The value to visit.
*/
template<class Visitor>
auto
visit(
    Visitor&& v,
    value& jv) -> decltype(
        static_cast<Visitor&&>(v)( std::declval<std::nullptr_t&>() ) );

/** Invoke a function object with the contents of a @ref value

    Invokes `v` as if by `std::forward<Visitor>(v)( X )`, where `X` is

    @li `jv.get_array()` if `jv.is_array()`, or

    @li `jv.get_object()` if `jv.is_object()`, or

    @li `jv.get_string()` if `jv.is_string()`, or

    @li `jv.get_int64()` if `jv.is_int64()`, or

    @li `jv.get_uint64()` if `jv.is_uint64()`, or

    @li `jv.get_double()` if `jv.is_double()`, or

    @li `jv.get_bool()` if `jv.is_bool()`, or

    @li reference to an object of type `const std::nullptr_t` if `jv.is_null()`.

    @return The value returned by Visitor.

    @param v The visitation function to invoke

    @param jv The value to visit.
*/
template<class Visitor>
auto
visit(
    Visitor &&v,
    value const &jv) -> decltype(
        static_cast<Visitor&&>(v)( std::declval<std::nullptr_t const&>() ) );

/** Invoke a function object with the contents of a @ref value

    Invokes `v` as if by `std::forward<Visitor>(v)( X )`, where `X` is

    @li `std::move( jv.get_array() )` if `jv.is_array()`, or

    @li `std::move( jv.get_object() )` if `jv.is_object()`, or

    @li `std::move( jv.get_string() )` if `jv.is_string()`, or

    @li `std::move( jv.get_int64() )` if `jv.is_int64()`, or

    @li `std::move( jv.get_uint64() )` if `jv.is_uint64()`, or

    @li `std::move( jv.get_double() )` if `jv.is_double()`, or

    @li `std::move( jv.get_bool() )` if `jv.is_bool()`, or

    @li `std::nullptr_t()` if `jv.is_null()`.

    @return The value returned by Visitor.

    @param v The visitation function to invoke

    @param jv The value to visit.
*/
template<class Visitor>
auto
visit(
    Visitor &&v,
    value&& jv) -> decltype(
        static_cast<Visitor&&>(v)( std::declval<std::nullptr_t&&>() ) );

} // namespace json
} // namespace boost

#include <boost/json/impl/visit.hpp>

#endif
