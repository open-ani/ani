//
// Copyright (c) 2021 Peter Dimov
// Copyright (c) 2021 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_PARSE_INTO_HPP
#define BOOST_JSON_PARSE_INTO_HPP

#include <boost/json/detail/config.hpp>

#include <boost/json/basic_parser.hpp>
#include <boost/json/string_view.hpp>
#include <boost/json/system_error.hpp>
#include <boost/json/detail/parse_into.hpp>
#include <boost/system/result.hpp>

namespace boost {
namespace json {

/** `basic_parser` that parses into a given type

    This is an alias template for @ref basic_parser instantiations that use
    a dedicated handler that parses directly into an object provided by the
    user instead of creating a @ref value.

    Objects of type `parser_for<T>` have constructor signature equivalent to
    `parser_for( parse_options const&, T& )`.

    @tparam T the type to parse into. This type must be
    [*DefaultConstructible*](https://en.cppreference.com/w/cpp/named_req/DefaultConstructible).
*/
template< class T >
using parser_for =
#ifndef BOOST_JSON_DOCS
    basic_parser<detail::into_handler<T>>;
#else
    __see_below__;
#endif

/** Parse a JSON text into a user-defined object.

    This function parses an entire string in one step and fills an object
    provided by the user. If the buffer does not contain a complete serialized
    JSON text, an error occurs. In this case `v` may be partially filled.

    The function supports default constructible types satisfying
    <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    arrays, arithmetic types, `bool`, `std::tuple`, `std::pair`,
    `std::optional`, `std::nullptr_t`, and structs and enums described using
    Boost.Describe.

    @par Complexity
    Linear in `sv.size()`.

    @par Exception Safety
    Basic guarantee.
    Calls to `memory_resource::allocate` may throw.

    @param v The type to parse into.

    @param sv The string to parse.

    @param ec Set to the error, if any occurred.

    @param opt The options for the parser. If this parameter
    is omitted, the parser will accept only standard JSON.
*/
/** @{ */
template<class V>
void
parse_into(
    V& v,
    string_view sv,
    system::error_code& ec,
    parse_options const& opt = {} );

template<class V>
void
parse_into(
    V& v,
    string_view sv,
    std::error_code& ec,
    parse_options const& opt = {} );
/** @} */

/** Parse a JSON text into a user-defined object.

    This function parses an entire string in one step and fills an object
    provided by the user. If the buffer does not contain a complete serialized
    JSON text, an exception is thrown. In this case `v` may be
    partially filled.

    The function supports default constructible types satisfying
    <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    arrays, arithmetic types, `bool`, `std::tuple`, `std::pair`,
    `std::optional`, `std::nullptr_t`, and structs and enums described using
    Boost.Describe.

    @par Complexity
    Linear in `sv.size()`.

    @par Exception Safety
    Basic guarantee.
    Calls to `memory_resource::allocate` may throw.

    @param v The type to parse into.

    @param sv The string to parse.

    @param opt The options for the parser. If this parameter
    is omitted, the parser will accept only standard JSON.

    @throw `boost::system::system_error` on failed parse.
*/
template<class V>
void
parse_into(
    V& v,
    string_view sv,
    parse_options const& opt = {} );

/** Parse a JSON text into a user-defined object.

    This function reads data from an input stream and fills an object provided
    by the user. If the buffer does not contain a complete serialized JSON
    text, or contains extra non-whitespace data, an error occurs. In this case
    `v` may be partially filled.

    The function supports default constructible types satisfying
    <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    arrays, arithmetic types, `bool`, `std::tuple`, `std::pair`,
    `std::optional`, `std::nullptr_t`, and structs and enums described using
    Boost.Describe.

    @par Complexity
    Linear in the size of consumed input.

    @par Exception Safety
    Basic guarantee.
    Calls to `memory_resource::allocate` may throw.
    The stream may throw as described by
    [`std::ios::exceptions`](https://en.cppreference.com/w/cpp/io/basic_ios/exceptions).

    @param v The type to parse into.

    @param is The stream to read from.

    @param ec Set to the error, if any occurred.

    @param opt The options for the parser. If this parameter
    is omitted, the parser will accept only standard JSON.
*/
/** @{ */
template<class V>
void
parse_into(
    V& v,
    std::istream& is,
    system::error_code& ec,
    parse_options const& opt = {} );

template<class V>
void
parse_into(
    V& v,
    std::istream& is,
    std::error_code& ec,
    parse_options const& opt = {} );
/** @} */

/** Parse a JSON text into a user-defined object.

    This function reads data from an input stream and fills an object provided
    by the user. If the buffer does not contain a complete serialized JSON
    text, or contains extra non-whitespace data, an exception is thrown. In
    this case `v` may be partially filled.

    The function supports default constructible types satisfying
    <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    arrays, arithmetic types, `bool`, `std::tuple`, `std::pair`,
    `std::optional`, `std::nullptr_t`, and structs and enums described using
    Boost.Describe.

    @par Complexity
    Linear in the size of consumed input.

    @par Exception Safety
    Basic guarantee.
    Calls to `memory_resource::allocate` may throw.
    The stream may throw as described by
    [`std::ios::exceptions`](https://en.cppreference.com/w/cpp/io/basic_ios/exceptions).

    @param v The type to parse into.

    @param is The stream to read from.

    @param opt The options for the parser. If this parameter
    is omitted, the parser will accept only standard JSON.

    @throw `boost::system::system_error` on failed parse.
*/
template<class V>
void
parse_into(
    V& v,
    std::istream& is,
    parse_options const& opt = {} );

} // namespace boost
} // namespace json

#include <boost/json/impl/parse_into.hpp>

#endif
