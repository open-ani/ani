//
// Copyright (c) 2021 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_ALL_CHARS_HPP
#define BOOST_URL_GRAMMAR_ALL_CHARS_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/grammar/detail/charset.hpp>

namespace boost {
namespace urls {
namespace grammar {

/** The set of all characters

    @par Example
    Character sets are used with rules and the
    functions @ref find_if and @ref find_if_not.
    @code
    system::result< core::string_view > rv = parse( "JohnDoe", token_rule( all_chars ) );
    @endcode

    @par BNF
    @code
    ALL       = %x00-FF
    @endcode

    @see
        @ref find_if,
        @ref find_if_not,
        @ref parse,
        @ref token_rule.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ all_chars;
#else
struct all_chars_t
{
    constexpr
    all_chars_t() noexcept = default;

    constexpr
    bool
    operator()(char) const noexcept
    {
        return true;
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

/** A character set containing all characters.

    @see
        @ref all_chars_t
*/
constexpr all_chars_t all_chars{};
#endif

} // grammar
} // urls
} // boost

#endif
