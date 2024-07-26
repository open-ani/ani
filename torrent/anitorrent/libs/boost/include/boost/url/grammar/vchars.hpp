//
// Copyright (c) 2021 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_VCHARS_HPP
#define BOOST_URL_GRAMMAR_VCHARS_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/grammar/detail/charset.hpp>

namespace boost {
namespace urls {
namespace grammar {

/** The set of visible characters

    @par Example
    Character sets are used with rules and the
    functions @ref find_if and @ref find_if_not.
    @code
    system::result< core::string_view > rv = parse( "JohnDoe", token_rule( vchars ) );
    @endcode

    @par BNF
    @code
    VCHAR       = 0x21-0x7E
                ; visible (printing) characters
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc5234#appendix-B.1"
        >B.1. Core Rules (rfc5234)</a>

    @see
        @ref find_if,
        @ref find_if_not,
        @ref parse,
        @ref token_rule.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ vchars;
#else
struct vchars_t
{
    constexpr
    bool
    operator()(char c) const noexcept
    {
        return c >= 0x21 && c <= 0x7e;
    }

#ifdef BOOST_URL_USE_SSE2
    char const*
    find_if(
        char const* first,
        char const* last) const noexcept
    {
        return detail::find_if_pred(
            *this, first, last);
    }

    char const*
    find_if_not(
        char const* first,
        char const* last) const noexcept
    {
        return detail::find_if_not_pred(
            *this, first, last);
    }
#endif
};

constexpr vchars_t vchars{};
#endif

} // grammar
} // urls
} // boost

#endif
