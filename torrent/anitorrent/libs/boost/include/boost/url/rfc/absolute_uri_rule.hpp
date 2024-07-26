//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_RFC_ABSOLUTE_URI_RULE_HPP
#define BOOST_URL_RFC_ABSOLUTE_URI_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/url_view.hpp>

namespace boost {
namespace urls {

/** Rule for absolute-URI

    @par Value Type
    @code
    using value_type = url_view;
    @endcode

    @par Example
    Rules are used with the function @ref grammar::parse.
    @code
    system::result< url_view > rv = grammar::parse( "http://example.com/index.htm?id=1", absolute_uri_rule );
    @endcode

    @par BNF
    @code
    absolute-URI    = scheme ":" hier-part [ "?" query ]

    hier-part       = "//" authority path-abempty
                    / path-absolute
                    / path-rootless
                    / path-empty
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.3"
        >4.3. Absolute URI (rfc3986)</a>

    @see
        @ref grammar::parse,
        @ref parse_absolute_uri,
        @ref url_view.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ absolute_uri_rule;
#else
struct absolute_uri_rule_t
{
    using value_type = url_view;

    BOOST_URL_DECL
    auto
    parse(
        char const*& it,
        char const* end
            ) const noexcept ->
        system::result<value_type>;
};

constexpr absolute_uri_rule_t absolute_uri_rule{};
#endif

} // urls
} // boost

#endif
