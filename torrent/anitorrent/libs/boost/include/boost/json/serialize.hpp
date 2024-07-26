//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_SERIALIZE_HPP
#define BOOST_JSON_SERIALIZE_HPP

#include <boost/json/detail/config.hpp>
#include <boost/json/serialize_options.hpp>
#include <boost/json/value.hpp>
#include <iosfwd>
#include <string>

namespace boost {
namespace json {

/** Return a string representing a serialized element.

    This function serializes `t` as JSON and returns
    it as a `std::string`.

    @par Complexity
    Constant or linear in the size of `t`.

    @par Exception Safety
    Strong guarantee.
    Calls to allocate may throw.

    @return The serialized string

    @param t The value to serialize

    @param opts The options for the serializer. If this parameter
    is omitted, the serializer will output only standard JSON.
*/
/** @{ */
BOOST_JSON_DECL
std::string
serialize(value const& t, serialize_options const& opts = {});

BOOST_JSON_DECL
std::string
serialize(array const& t, serialize_options const& opts = {});

BOOST_JSON_DECL
std::string
serialize(object const& t, serialize_options const& opts = {});

BOOST_JSON_DECL
std::string
serialize(string const& t, serialize_options const& opts = {});

BOOST_JSON_DECL
std::string
serialize(string_view t, serialize_options const& opts = {});
/** @} */

} // namespace json
} // namespace boost

#endif
