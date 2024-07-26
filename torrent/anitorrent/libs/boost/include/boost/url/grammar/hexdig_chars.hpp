//
// Copyright (c) 2021 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_HEXDIG_CHARS_HPP
#define BOOST_URL_GRAMMAR_HEXDIG_CHARS_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/grammar/detail/charset.hpp>

namespace boost {
namespace urls {
namespace grammar {

/** The set of hexadecimal digits

    @par Example
    Character sets are used with rules and the
    functions @ref find_if and @ref find_if_not.
    @code
    system::result< core::string_view > rv = parse( "8086FC19", token_rule( hexdig_chars ) );
    @endcode

    @par BNF
    @code
    HEXDIG      = DIGIT
                / "A" / "B" / "C" / "D" / "E" / "F"
                / "a" / "b" / "c" / "d" / "e" / "f"
    @endcode

    @note The RFCs are inconsistent on the case
    sensitivity of hexadecimal digits. Existing
    uses suggest case-insensitivity is a de-facto
    standard.

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc5234#appendix-B.1"
        >B.1. Core Rules (rfc5234)</a>
    @li <a href="https://datatracker.ietf.org/doc/html/rfc7230#section-1.2"
        >1.2. Syntax Notation (rfc7230)</a>
    @li <a href="https://datatracker.ietf.org/doc/html/rfc5952#section-2.3"
        >2.3. Uppercase or Lowercase (rfc5952)</a>
    @li <a href="https://datatracker.ietf.org/doc/html/rfc5952#section-4.3"
        >4.3. Lowercase (rfc5952)</a>

    @see
        @ref find_if,
        @ref find_if_not,
        @ref hexdig_value,
        @ref parse,
        @ref token_rule.
*/
#ifdef BOOST_URL_DOCS
constexpr __implementation_defined__ hexdig_chars;
#else
struct hexdig_chars_t
{
    /** Return true if c is in the character set.
    */
    constexpr
    bool
    operator()(char c) const noexcept
    {
        return
            (c >= '0' && c <= '9') ||
            (c >= 'A' && c <= 'F') ||
            (c >= 'a' && c <= 'f');
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

constexpr hexdig_chars_t hexdig_chars{};
#endif

// VFALCO We can declare
// these later if needed
//
//struct hexdig_upper_chars;
//struct hexdig_lower_chars;

/** Return the decimal value of a hex character

    This function returns the decimal
    value of a hexadecimal character,
    or -1 if the argument is not a
    valid hexadecimal digit.

    @par BNF
    @code
    HEXDIG      = DIGIT
                / "A" / "B" / "C" / "D" / "E" / "F"
                / "a" / "b" / "c" / "d" / "e" / "f"
    @endcode

    @param ch The character to check

    @return The decimal value or -1
*/
inline
signed char
hexdig_value(char ch) noexcept
{
    // Idea for switch statement to
    // minimize emitted assembly from
    // Glen Fernandes
    signed char res;
    switch(ch)
    {
    default:            res = -1; break;
    case '0':           res =  0; break;
    case '1':           res =  1; break;
    case '2':           res =  2; break;
    case '3':           res =  3; break;
    case '4':           res =  4; break;
    case '5':           res =  5; break;
    case '6':           res =  6; break;
    case '7':           res =  7; break;
    case '8':           res =  8; break;
    case '9':           res =  9; break;
    case 'a': case 'A': res = 10; break;
    case 'b': case 'B': res = 11; break;
    case 'c': case 'C': res = 12; break;
    case 'd': case 'D': res = 13; break;
    case 'e': case 'E': res = 14; break;
    case 'f': case 'F': res = 15; break;
    }
    return res;
}

} // grammar
} // urls
} // boost

#endif
