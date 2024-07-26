//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_RFC_AUTHORITY_RULE_HPP
#define BOOST_URL_RFC_AUTHORITY_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/authority_view.hpp>
#include <boost/url/error_types.hpp>

namespace boost {
namespace urls {

/** Rule for authority

    @par Value Type
    @code
    using value_type = authority_view;
    @endcode

    @par Example
    Rules are used with the function @ref grammar::parse.
    @code
    system::result< authority_view > rv = grammar::parse( "user:pass@example.com:8080", authority_rule );
    @endcode

    @par BNF
    @code
    authority   = [ userinfo "@" ] host [ ":" port ]
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2"
        >3.2. Authority (rfc3986)</a>

    @see
        @ref authority_view,
        @ref grammar::parse,
        @ref parse_authority.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ authority_rule;
#else
struct authority_rule_t
{
    using value_type = authority_view;

    BOOST_URL_DECL
    auto
    parse(
        char const*& it,
        char const* end
            ) const noexcept ->
        system::result<value_type>;
};

constexpr authority_rule_t authority_rule{};
#endif

} // urls
} // boost

#endif
