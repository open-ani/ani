//
// Copyright (c) 2022 Dmitry Arkhipov (grisumbras@yandex.ru)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_SET_POINTER_OPTIONS_HPP
#define BOOST_JSON_SET_POINTER_OPTIONS_HPP

#include <boost/json/detail/config.hpp>

namespace boost {
namespace json {

/** Options for @ref value::set_at_pointer
 *
 * This structure is used for controlling behavior of
 * @ref value::set_at_pointer regarding creation of intermediate elements.
 */
struct set_pointer_options
{
    /** Whether to create arrays

        The option controls whether arrays are created when a pointer token is
        a number or a past-the-end marker.
    */
    bool create_arrays = true;

    /** Whether to create objects

        The option controls whether objects are created for valid pointer
        tokens.
    */
    bool create_objects = true;

    /** Whether to replace non-null scalars

        If the option is `true` any non-object, non-array value can be replaced
        by either an @ref object or a @ref array. If it's `false`, only `null`
        values can be replaced.
    */
    bool replace_any_scalar = false;


    /** Maximum amount of elements added per one pointer token

        When addressing @ref array elements the number represented by pointer
        token can exceed the size of the array. In that case several elements
        may be created to satisfy the request. This option limits the amount of
        elements that can be added per one pointer token. This can be
        important, because in general such operations can be dangerous, since a
        relatively small string `"/18446744073709551616"` can request all of
        the machine's memory. For that reason the default value for this option
        is 1, that is no more than 1 element can be added per one pointer
        token.
    */
    std::size_t max_created_elements = 1;
};

} // namespace json
} // namespace boost

#endif // BOOST_JSON_SET_POINTER_OPTIONS_HPP
