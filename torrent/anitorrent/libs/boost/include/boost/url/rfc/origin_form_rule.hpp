//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_RFC_ORIGIN_FORM_RULE_HPP
#define BOOST_URL_RFC_ORIGIN_FORM_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/url_view.hpp>

namespace boost {
namespace urls {

/** Rule for origin-form

    This appears in the HTTP/1 request-line grammar.

    @par Value Type
    @code
    using value_type = url_view;
    @endcode

    @par Example
    Rules are used with the function @ref grammar::parse.
    @code
    system::result< url_view > rv = grammar::parse( "/index.htm?layout=mobile", origin_form_rule );
    @endcode

    @par BNF
    @code
    origin-form    = absolute-path [ "?" query ]

    absolute-path = 1*( "/" segment )
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc7230#section-5.3.1"
        >5.3.1.  origin-form (rfc7230)</a>

    @see
        @ref grammar::parse,
        @ref parse_origin_form,
        @ref url_view.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ origin_form_rule;
#else
struct origin_form_rule_t
{
    using value_type =
        url_view;

    BOOST_URL_DECL
    system::result<value_type>
    parse(
        char const*& it,
        char const* end
            ) const noexcept;
};

constexpr origin_form_rule_t origin_form_rule{};
#endif

} // urls
} // boost

#endif
