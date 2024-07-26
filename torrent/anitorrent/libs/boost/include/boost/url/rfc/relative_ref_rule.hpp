//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_RFC_RELATIVE_REF_RULE_HPP
#define BOOST_URL_RFC_RELATIVE_REF_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/url_view.hpp>

namespace boost {
namespace urls {

/** Rule for relative-ref

    @par Value Type
    @code
    using value_type = url_view;
    @endcode

    @par Example
    Rules are used with the function @ref grammar::parse.
    @code
    system::result< url_view > rv = grammar::parse( "images/dot.gif?v=hide#a", relative_ref_rule );
    @endcode

    @par BNF
    @code
    relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.2"
        >4.2. Relative Reference (rfc3986)</a>

    @see
        @ref grammar::parse,
        @ref parse_relative_ref,
        @ref url_view.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ relative_ref_rule;
#else
struct relative_ref_rule_t
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

constexpr relative_ref_rule_t relative_ref_rule{};
#endif

} // urls
} // boost

#endif
