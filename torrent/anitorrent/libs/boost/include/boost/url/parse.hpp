//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARSE_HPP
#define BOOST_URL_PARSE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/url_view.hpp>

namespace boost {
namespace urls {

/** Return a reference to a parsed URL string

    This function parses a string according
    to the grammar below and returns a view
    referencing the passed string upon success,
    else returns an error.
    Ownership of the string is not transferred;
    the caller is responsible for ensuring that
    the lifetime of the character buffer extends
    until the view is no longer being accessed.

    @par Example
    @code
    system::result< url_view > rv = parse_absolute_uri( "http://example.com/index.htm?id=1" );
    @endcode

    @par BNF
    @code
    absolute-URI    = scheme ":" hier-part [ "?" query ]

    hier-part       = "//" authority path-abempty
                    / path-absolute
                    / path-rootless
                    / path-empty
    @endcode

    @throw std::length_error `s.size() > url_view::max_size`

    @return A @ref result containing a value or an error

    @param s The string to parse

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.3"
        >4.3. Absolute URI (rfc3986)</a>

    @see
        @ref parse_origin_form,
        @ref parse_relative_ref,
        @ref parse_uri,
        @ref parse_uri_reference,
        @ref url_view.
*/
BOOST_URL_DECL
system::result<url_view>
parse_absolute_uri(
    core::string_view s);

//------------------------------------------------

/** Return a reference to a parsed URL string

    This function parses a string according
    to the grammar below and returns a view
    referencing the passed string upon success,
    else returns an error.
    Ownership of the string is not transferred;
    the caller is responsible for ensuring that
    the lifetime of the character buffer extends
    until the view is no longer being accessed.

    @par Example
    @code
    system::result< url_view > = parse_origin_form( "/index.htm?layout=mobile" );
    @endcode

    @par BNF
    @code
    origin-form    = absolute-path [ "?" query ]

    absolute-path = 1*( "/" segment )
    @endcode

    @throw std::length_error `s.size() > url_view::max_size`

    @return A @ref result containing a value or an error

    @param s The string to parse

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc7230#section-5.3.1"
        >5.3.1.  origin-form (rfc7230)</a>

    @see
        @ref parse_absolute_uri,
        @ref parse_relative_ref,
        @ref parse_uri,
        @ref parse_uri_reference,
        @ref url_view.
*/
BOOST_URL_DECL
system::result<url_view>
parse_origin_form(
    core::string_view s);

//------------------------------------------------

/** Return a reference to a parsed URL string

    This function parses a string according
    to the grammar below and returns a view
    referencing the passed string upon success,
    else returns an error.
    Ownership of the string is not transferred;
    the caller is responsible for ensuring that
    the lifetime of the character buffer extends
    until the view is no longer being accessed.

    @par Example
    @code
    system::result< url_view > = parse_relative_ref( "images/dot.gif?v=hide#a" );
    @endcode

    @par BNF
    @code
    relative-ref  = relative-part [ "?" query ] [ "#" fragment ]

    relative-part = "//" authority path-abempty
                  / path-absolute
                  / path-noscheme
                  / path-abempty
                  / path-empty
    @endcode

    @return A @ref result containing a value or an error

    @param s The string to parse

    @throw std::length_error `s.size() > url_view::max_size`

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.2"
        >4.2. Relative Reference (rfc3986)</a>
    @li <a href="https://www.rfc-editor.org/errata/eid5428"
        >Errata ID: 5428 (rfc3986)</a>

    @see
        @ref parse_absolute_uri,
        @ref parse_origin_form,
        @ref parse_uri,
        @ref parse_uri_reference,
        @ref url_view.
*/
BOOST_URL_DECL
system::result<url_view>
parse_relative_ref(
    core::string_view s);

//------------------------------------------------

/** Return a reference to a parsed URL string

    This function parses a string according
    to the grammar below and returns a view
    referencing the passed string upon success,
    else returns an error.
    Ownership of the string is not transferred;
    the caller is responsible for ensuring that
    the lifetime of the character buffer extends
    until the view is no longer being accessed.

    @par Example
    @code
    system::result< url_view > = parse_uri( "https://www.example.com/index.htm?id=guest#s1" );
    @endcode

    @par BNF
    @code
    URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

    hier-part     = "//" authority path-abempty
                  / path-absolute
                  / path-rootless
                  / path-empty
    @endcode

    @throw std::length_error `s.size() > url_view::max_size`

    @return A @ref result containing a value or an error

    @param s The string to parse

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3"
        >3. Syntax Components (rfc3986)</a>

    @see
        @ref parse_absolute_uri,
        @ref parse_origin_form,
        @ref parse_relative_ref,
        @ref parse_uri_reference,
        @ref url_view.
*/
BOOST_URL_DECL
system::result<url_view>
parse_uri(
    core::string_view s);

//------------------------------------------------

/** Return a reference to a parsed URL string

    This function parses a string according
    to the grammar below and returns a view
    referencing the passed string upon success,
    else returns an error.
    Ownership of the string is not transferred;
    the caller is responsible for ensuring that
    the lifetime of the character buffer extends
    until the view is no longer being accessed.

    @par Example
    @code
    system::result< url_view > = parse_uri_reference( "ws://echo.example.com/?name=boost#demo" );
    @endcode

    @par BNF
    @code
    URI-reference = URI / relative-ref

    URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

    relative-ref  = relative-part [ "?" query ] [ "#" fragment ]

    hier-part     = "//" authority path-abempty
                  / path-absolute
                  / path-rootless
                  / path-empty

    relative-part = "//" authority path-abempty
                  / path-absolute
                  / path-noscheme
                  / path-abempty
                  / path-empty
    @endcode

    @throw std::length_error `s.size() > url_view::max_size`

    @return A @ref result containing a value or an error

    @param s The string to parse

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.1"
        >4.1. URI Reference (rfc3986)</a>
    @li <a href="https://www.rfc-editor.org/errata/eid5428"
        >Errata ID: 5428 (rfc3986)</a>

    @see
        @ref parse_absolute_uri,
        @ref parse_origin_form,
        @ref parse_relative_ref,
        @ref parse_uri,
        @ref url_view.
*/
BOOST_URL_DECL
system::result<url_view>
parse_uri_reference(
    core::string_view s);

} // url
} // boost

#endif
