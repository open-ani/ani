//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_DEC_OCTET_RULE_HPP
#define BOOST_URL_GRAMMAR_DEC_OCTET_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>

namespace boost {
namespace urls {
namespace grammar {

/** Match a decimal octet

    A decimal octet is precise way of
    saying a number from 0 to 255. These
    are commonly used in IPv4 addresses.

    @par Value Type
    @code
    using value_type = unsigned char;
    @endcode

    @par Example
    Rules are used with the function @ref parse.
    @code
    system::result< unsigned char > rv = parse( "255", dec_octet_rule );
    @endcode

    @par BNF
    @code
    dec-octet   = DIGIT                 ; 0-9
                / %x31-39 DIGIT         ; 10-99
                / "1" 2DIGIT            ; 100-199
                / "2" %x30-34 DIGIT     ; 200-249
                / "25" %x30-35          ; 250-255
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
        >3.2.2.  Host (rfc3986)</a>

    @see
        @ref parse.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ dec_octet_rule;
#else
struct dec_octet_rule_t
{
    using value_type = unsigned char;

    BOOST_URL_DECL
    auto
    parse(
        char const*& it,
        char const* end
            ) const noexcept ->
        system::result<value_type>;
};

constexpr dec_octet_rule_t dec_octet_rule{};
#endif

} // grammar
} // urls
} // boost

#endif
