//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_ERROR_HPP
#define BOOST_JSON_ERROR_HPP

#include <boost/json/detail/config.hpp>
#include <boost/json/system_error.hpp>

namespace boost {
namespace json {

/** Error codes returned by JSON operations

*/
enum class error
{
    //
    // parse errors
    //

    /// syntax error
    syntax = 1,

    /// extra data
    extra_data,

    /// incomplete JSON
    incomplete,

    /// exponent too large
    exponent_overflow,

    /// too deep
    too_deep,

    /// illegal leading surrogate
    illegal_leading_surrogate,

    /// illegal trailing surrogate
    illegal_trailing_surrogate,

    /// expected hex digit
    expected_hex_digit,

    /// expected utf16 escape
    expected_utf16_escape,

    /// An object contains too many elements
    object_too_large,

    /// An array contains too many elements
    array_too_large,

    /// A key is too large
    key_too_large,

    /// A string is too large
    string_too_large,

    /// A number is too large
    number_too_large,

    /// error occured when trying to read input
    input_error,

    //
    // generic errors
    //

    /// An exception was thrown during operation
    exception,

    /// A requested element is outside of container's range
    out_of_range,

    /// test failure
    test_failure,

    //
    // JSON Pointer errors
    //

    /// missing slash character before token reference
    missing_slash,

    /// invalid escape sequence
    invalid_escape,

    /// token should be a number but cannot be parsed as such
    token_not_number,

    /// current value is neither an object nor an array
    value_is_scalar,

    /// current value does not contain referenced value
    not_found,

    /// token cannot be represented by std::size_t
    token_overflow,

    /// past-the-end index is not supported
    past_the_end,

    //
    // Conversion errors
    //

    /// JSON number was expected during conversion
    not_number,

    /// number cast is not exact
    not_exact,

    /// JSON null was expected during conversion
    not_null,

    /// JSON bool was expected during conversion
    not_bool,

    /// JSON array was expected during conversion
    not_array,

    /// JSON object was expected during conversion
    not_object,

    /// JSON string was expected during conversion
    not_string,

    /// std::int64_t was expected during conversion
    not_int64,

    /// std::uint64_t was expected during conversion
    not_uint64,

    /// `double` was expected during conversion
    not_double,

    /// JSON integer was expected during conversion
    not_integer,

    /// source composite has size incompatible with target
    size_mismatch,

    /// none of the possible conversions were successful
    exhausted_variants,

    /// the key does not correspond to a known name
    unknown_name,
};

/** Error conditions corresponding to JSON errors
*/
enum class condition
{
    /// A parser-related error
    parse_error = 1,

    /// An error related to parsing JSON pointer string
    pointer_parse_error,

    /// An error related to applying JSON pointer string to a value
    pointer_use_error,

    /// A conversion error
    conversion_error,

    /// A generic error
    generic_error,
};

} // namespace json
} // namespace boost

#include <boost/json/impl/error.hpp>

#endif
