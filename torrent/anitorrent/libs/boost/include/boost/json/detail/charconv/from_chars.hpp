// Copyright 2022 Peter Dimov
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_JSON_DETAIL_CHARCONV_FROM_CHARS_HPP_INCLUDED
#define BOOST_JSON_DETAIL_CHARCONV_FROM_CHARS_HPP_INCLUDED

#include <boost/json/detail/charconv/detail/config.hpp>
#include <boost/json/detail/charconv/detail/from_chars_result.hpp>
#include <boost/json/detail/charconv/chars_format.hpp>
#include <system_error>

namespace boost { namespace json { namespace detail { namespace charconv {

//----------------------------------------------------------------------------------------------------------------------
// Floating Point
//----------------------------------------------------------------------------------------------------------------------

namespace detail {

std::errc errno_to_errc(int errno_value) noexcept;

} // Namespace detail

BOOST_JSON_DECL from_chars_result from_chars(const char* first, const char* last, double& value, chars_format fmt = chars_format::general) noexcept;

} // namespace charconv
} // namespace detail
} // namespace json
} // namespace boost

#endif // #ifndef BOOST_JSON_DETAIL_CHARCONV_FROM_CHARS_HPP_INCLUDED
