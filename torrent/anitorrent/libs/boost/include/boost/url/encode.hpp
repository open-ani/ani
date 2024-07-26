//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_ENCODE_HPP
#define BOOST_URL_ENCODE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/encoding_opts.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/grammar/all_chars.hpp>
#include <boost/url/grammar/string_token.hpp>

namespace boost {
namespace urls {

/** Return the buffer size needed for percent-encoding

    This function returns the exact number
    of bytes necessary to store the result
    of applying percent-encoding to the
    string using the given options and
    character set.
    No encoding is actually performed.

    @par Example
    @code
    assert( encoded_size( "My Stuff", pchars ) == 10 );
    @endcode

    @par Exception Safety
    Throws nothing.

    @return The number of bytes needed,
    excluding any null terminator.

    @param s The string to measure.

    @param unreserved The set of characters
    that is not percent-encoded.

    @param opt The options for encoding. If
    this parameter is omitted, the default
    options are be used.

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-2.1"
        >2.1. Percent-Encoding (rfc3986)</a>

    @see
        @ref encode,
        @ref encoding_opts,
        @ref make_pct_string_view.
*/
template<class CharSet>
std::size_t
encoded_size(
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt = {}) noexcept;

//------------------------------------------------

/** Apply percent-encoding to a string

    This function applies percent-encoding
    to the string using the given options and
    character set. The destination buffer
    provided by the caller is used to store
    the result, which may be truncated if
    there is insufficient space.

    @par Example
    @code
    char buf[100];
    assert( encode( buf, sizeof(buf), "Program Files", pchars ) == 15 );
    @endcode

    @par Exception Safety
    Throws nothing.

    @return The number of characters written
    to the destination buffer.

    @param dest The destination buffer
    to write to.

    @param size The number of writable
    characters pointed to by `dest`.
    If this is less than `encoded_size(s)`,
    the result is truncated.

    @param s The string to encode.

    @param unreserved The set of characters
    that is not percent-encoded.

    @param opt The options for encoding. If
    this parameter is omitted, the default
    options are used.

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-2.1"
        >2.1. Percent-Encoding (rfc3986)</a>

    @see
        @ref encode,
        @ref encoded_size,
        @ref make_pct_string_view.
*/
template<class CharSet>
std::size_t
encode(
    char* dest,
    std::size_t size,
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt = {});

#ifndef BOOST_URL_DOCS
// VFALCO semi-private for now
template<class CharSet>
std::size_t
encode_unsafe(
    char* dest,
    std::size_t size,
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt);
#endif

//------------------------------------------------

/** Return a percent-encoded string

    This function applies percent-encoding
    to the string using the given options and
    character set, and returns the result as
    a string when called with default arguments.

    @par Example
    @code
    encoding_opts opt;
    opt.space_as_plus = true;
    std::string s = encode( "My Stuff", opt, pchars );

    assert( s == "My+Stuff" );
    @endcode

    @par Exception Safety
    Calls to allocate may throw.

    @return The string

    @param s The string to encode.

    @param unreserved The set of characters
    that is not percent-encoded.

    @param opt The options for encoding. If
    this parameter is omitted, the default
    options are used.

    @param token A string token.

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-2.1"
        >2.1. Percent-Encoding (rfc3986)</a>

    @see
        @ref encode,
        @ref encoded_size,
        @ref encoding_opts,
*/
template<
    BOOST_URL_STRTOK_TPARAM,
    class CharSet>
BOOST_URL_STRTOK_RETURN
encode(
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt = {},
    BOOST_URL_STRTOK_ARG(token)) noexcept;

} // urls
} // boost

#include <boost/url/impl/encode.hpp>

#endif
