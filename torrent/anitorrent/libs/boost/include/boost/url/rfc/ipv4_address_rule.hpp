//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_RFC_IPV4_ADDRESS_RULE_HPP
#define BOOST_URL_RFC_IPV4_ADDRESS_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/ipv4_address.hpp>
#include <boost/url/error_types.hpp>

namespace boost {
namespace urls {

/** Rule for an IP version 4 style address

    @par Value Type
    @code
    using value_type = ipv4_address;
    @endcode

    @par Example
    Rules are used with the function @ref grammar::parse.
    @code
    system::result< ipv4_address > rv = grammar::parse( "192.168.0.1", ipv4_address_rule );
    @endcode

    @par BNF
    @code
    IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet

    dec-octet   = DIGIT                 ; 0-9
                / %x31-39 DIGIT         ; 10-99
                / "1" 2DIGIT            ; 100-199
                / "2" %x30-34 DIGIT     ; 200-249
                / "25" %x30-35          ; 250-255
    @endcode

    @par Specification
    @li <a href="https://en.wikipedia.org/wiki/IPv4"
        >IPv4 (Wikipedia)</a>
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
        >3.2.2. Host (rfc3986)</a>

    @see
        @ref ipv4_address,
        @ref parse_ipv4_address,
        @ref grammar::parse.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ ipv4_address_rule;
#else
struct ipv4_address_rule_t
{
    using value_type =
        ipv4_address;

    BOOST_URL_DECL
    auto
    parse(
        char const*& it,
        char const* end
            ) const noexcept ->
        system::result<ipv4_address>;
};

constexpr ipv4_address_rule_t ipv4_address_rule{};
#endif

} // urls
} // boost

#endif
