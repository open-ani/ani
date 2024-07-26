//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_ESCAPE_STRING_IPP
#define BOOST_MYSQL_IMPL_ESCAPE_STRING_IPP

#pragma once

#include <boost/mysql/character_set.hpp>
#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/escape_string.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/output_string.hpp>

#include <boost/mysql/impl/internal/call_next_char.hpp>

namespace boost {
namespace mysql {
namespace detail {

// A (possibly null) escape sequence of two characters. Used as the return value for escapers
class escape_sequence
{
    char data_[2]{};

public:
    escape_sequence() = default;
    escape_sequence(char ch1, char ch2) noexcept : data_{ch1, ch2} {}

    bool is_escape() const noexcept { return data_[0] != '\0'; }
    string_view data() const noexcept { return string_view(data_, 2); }
};

// Escaper is a function object that takes a char and returns a
// escape_sequence determining whether we should escape the char or not
template <class Escaper>
BOOST_ATTRIBUTE_NODISCARD error_code
escape_impl(string_view input, character_set charset, Escaper escaper, output_string_ref output)
{
    const char* it = input.data();
    const char* end = it + input.size();

    // The raw range is a range of contiguous characters that don't need escaping.
    // We only append the raw range once we find a character that needs escaping
    const char* raw_begin = it;
    while (it != end)
    {
        escape_sequence seq = escaper(*it);
        if (seq.is_escape())
        {
            // Dump what we already had
            output.append({raw_begin, it});

            // Output the escape sequence
            output.append(seq.data());

            // Advance
            ++it;

            // Update the start of the range that doesn't need escaping
            raw_begin = it;
        }
        else
        {
            // Advance with the charset function
            std::size_t char_size = detail::call_next_char(charset, it, end);
            if (char_size == 0u)
                return client_errc::invalid_encoding;
            it += char_size;
        }
    }

    // Dump the remaining of the string, if any
    output.append({raw_begin, end});

    // Done
    return error_code();
}

struct backslash_escaper
{
    escape_sequence operator()(char input) const noexcept
    {
        switch (input)
        {
        case '\0': return {'\\', '0'};
        case '\n': return {'\\', 'n'};
        case '\r': return {'\\', 'r'};
        case '\\': return {'\\', '\\'};
        case '\'': return {'\\', '\''};
        case '"': return {'\\', '"'};
        case '\x1a': return {'\\', 'Z'};    // Ctrl+Z
        default: return escape_sequence();  // No escape
        }
    };
};

struct quote_escaper
{
    char quot;

    quote_escaper(char q) noexcept : quot(q) {}

    escape_sequence operator()(char input) const noexcept
    {
        return input == quot ? escape_sequence(quot, quot) : escape_sequence();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

boost::mysql::error_code boost::mysql::detail::escape_string(
    string_view input,
    const format_options& opts,
    char escape_char,
    output_string_ref output
)
{
    return (escape_char == '`' || !opts.backslash_escapes)
               ? detail::escape_impl(input, opts.charset, quote_escaper(escape_char), output)
               : detail::escape_impl(input, opts.charset, backslash_escaper(), output);
}

#endif
