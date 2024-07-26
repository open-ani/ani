// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_JSON_DETAIL_CHARCONV_DETAIL_FROM_CHARS_RESULT_HPP
#define BOOST_JSON_DETAIL_CHARCONV_DETAIL_FROM_CHARS_RESULT_HPP

#include <system_error>

namespace boost { namespace json { namespace detail { namespace charconv {

// 22.13.3, Primitive numerical input conversion

template <typename UC>
struct from_chars_result_t
{
    const UC* ptr;

    // Values:
    // 0 = no error
    // EINVAL = invalid_argument
    // ERANGE = result_out_of_range
    std::errc ec;

    friend constexpr bool operator==(const from_chars_result_t<UC>& lhs, const from_chars_result_t<UC>& rhs) noexcept
    {
        return lhs.ptr == rhs.ptr && lhs.ec == rhs.ec;
    }

    friend constexpr bool operator!=(const from_chars_result_t<UC>& lhs, const from_chars_result_t<UC>& rhs) noexcept
    {
        return !(lhs == rhs);
    }
};
using from_chars_result = from_chars_result_t<char>;

}}}} // Namespaces

#endif // BOOST_JSON_DETAIL_CHARCONV_DETAIL_FROM_CHARS_RESULT_HPP
