//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_ERROR_IPP
#define BOOST_JSON_IMPL_ERROR_IPP

#include <boost/json/error.hpp>

namespace boost {
namespace json {
namespace detail {

// msvc 14.0 has a bug that warns about inability to use constexpr
// construction here, even though there's no constexpr construction
#if defined(_MSC_VER) && _MSC_VER <= 1900
# pragma warning( push )
# pragma warning( disable : 4592 )
#endif
BOOST_JSON_CONSTINIT
error_code_category_t error_code_category;

BOOST_JSON_CONSTINIT
error_condition_category_t error_condition_category;
#if defined(_MSC_VER) && _MSC_VER <= 1900
# pragma warning( pop )
#endif

char const*
error_code_category_t::name() const noexcept
{
    return "boost.json";
}

char const*
error_code_category_t::message( int ev, char*, std::size_t ) const noexcept
{
    switch(static_cast<error>(ev))
    {
    default:
case error::syntax: return "syntax error";
case error::extra_data: return "extra data";
case error::incomplete: return "incomplete JSON";
case error::exponent_overflow: return "exponent overflow";
case error::too_deep: return "too deep";
case error::illegal_leading_surrogate: return "illegal leading surrogate";
case error::illegal_trailing_surrogate: return "illegal trailing surrogate";
case error::expected_hex_digit: return "expected hex digit";
case error::expected_utf16_escape: return "expected utf16 escape";
case error::object_too_large: return "object too large";
case error::array_too_large: return "array too large";
case error::key_too_large: return "key too large";
case error::string_too_large: return "string too large";
case error::number_too_large: return "number too large";
case error::input_error: return "input error";

case error::exception: return "got exception";
case error::out_of_range: return "out of range";
case error::test_failure: return "test failure";

case error::missing_slash: return "missing slash character";
case error::invalid_escape: return "invalid escape sequence";
case error::token_not_number: return "token is not a number";
case error::value_is_scalar: return "current value is scalar";
case error::not_found: return "no referenced value";
case error::token_overflow: return "token overflow";
case error::past_the_end: return "past-the-end token not supported";

case error::not_number: return "not a number";
case error::not_exact: return "not exact";
case error::not_null: return "value is not null";
case error::not_bool: return "value is not boolean";
case error::not_array: return "value is not an array";
case error::not_object: return "value is not an object";
case error::not_string: return "value is not a string";
case error::not_int64: return "value is not a std::int64_t number";
case error::not_uint64: return "value is not a std::uint64_t number";
case error::not_double: return "value is not a double";
case error::not_integer: return "value is not integer";
case error::size_mismatch: return "source composite size does not match target size";
case error::exhausted_variants: return "exhausted all variants";
case error::unknown_name: return "unknown name";
    }
}

std::string
error_code_category_t::message( int ev ) const
{
    return message( ev, nullptr, 0 );
}

system::error_condition
error_code_category_t::default_error_condition( int ev) const noexcept
{
    switch(static_cast<error>(ev))
    {
    default:
        return {ev, *this};

case error::syntax:
case error::extra_data:
case error::incomplete:
case error::exponent_overflow:
case error::too_deep:
case error::illegal_leading_surrogate:
case error::illegal_trailing_surrogate:
case error::expected_hex_digit:
case error::expected_utf16_escape:
case error::object_too_large:
case error::array_too_large:
case error::key_too_large:
case error::string_too_large:
case error::number_too_large:
case error::input_error:
    return condition::parse_error;

case error::missing_slash:
case error::invalid_escape:
    return condition::pointer_parse_error;

case error::token_not_number:
case error::value_is_scalar:
case error::not_found:
case error::token_overflow:
case error::past_the_end:
    return condition::pointer_use_error;

case error::not_number:
case error::not_exact:
case error::not_null:
case error::not_bool:
case error::not_array:
case error::not_object:
case error::not_string:
case error::not_int64:
case error::not_uint64:
case error::not_double:
case error::not_integer:
case error::size_mismatch:
case error::exhausted_variants:
case error::unknown_name:
    return condition::conversion_error;

case error::exception:
case error::out_of_range:
    return condition::generic_error;
    }
}

char const*
error_condition_category_t::name() const noexcept
{
    return "boost.json";
}

char const*
error_condition_category_t::message( int cv, char*, std::size_t ) const noexcept
{
    switch(static_cast<condition>(cv))
    {
    default:
    case condition::parse_error:
        return "A JSON parse error occurred";
    case condition::pointer_parse_error:
        return "A JSON Pointer parse error occurred";
    case condition::pointer_use_error:
        return "An error occurred when JSON Pointer was used with"
            " a value";
    case condition::conversion_error:
        return "An error occurred during conversion";
    }
}

std::string
error_condition_category_t::message( int cv ) const
{
    return message( cv, nullptr, 0 );
}

} // namespace detail

} // namespace json
} // namespace boost

#endif
