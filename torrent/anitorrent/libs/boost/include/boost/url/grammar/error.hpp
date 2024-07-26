//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_ERROR_HPP
#define BOOST_URL_GRAMMAR_ERROR_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>

namespace boost {
namespace urls {
namespace grammar {

/** Error codes returned when using rules

    @see
        @ref condition,
        @ref parse.
*/
enum class error
{
    // VFALCO 3 space indent or
    // else Doxygen malfunctions

    //
    // (informational)
    //

    /**
     * More input is needed to match the rule
     *
     * A rule reached the end of the input,
     * resulting in a partial match. The error
     * is recoverable; the caller may obtain
     * more input if possible and attempt to
     * parse the character buffer again.
     * Custom rules should only return this
     * error if it is completely unambiguous
     * that the rule cannot be matched without
     * more input.
    */
    need_more = 1,

    /**
     * The rule did not match the input.
     *
     * This error is returned when a rule fails
     * to match the input. The error is recoverable;
     * the caller may rewind the input pointer and
     * attempt to parse again using a different rule.
    */
    mismatch,

    /**
     * A rule reached the end of a range
     *
     * This indicates that the input was consumed
     * when parsing a @ref range. The @ref range_rule
     * avoids rewinding the input buffer when
     * this error is returned. Thus the consumed
     * characters are be considered part of the
     * range without contributing additional
     * elements.
    */
    end_of_range,

    /**
     * Leftover input remaining after match.
    */
    leftover,

    //--------------------------------------------
    //
    // condition::fatal
    //
    //--------------------------------------------

    /**
     * A rule encountered unrecoverable invalid input.
     *
     * This error is returned when input is matching
     * but one of the requirements is violated. For
     * example if a percent escape is found, but
     * one or both characters that follow are not
     * valid hexadecimal digits. This is usually an
     * unrecoverable error.
    */
    invalid,

    /** An integer overflowed during parsing.
    */
    out_of_range,

    /**
     * An unspecified syntax error was found.
    */
    syntax
};

//------------------------------------------------

/** Error conditions for errors received from rules

    @see
        @ref error,
        @ref parse.
*/
enum class condition
{
    /**
     * A fatal error in syntax was encountered.

       This indicates that parsing cannot continue.
    */
    fatal = 1
};

} // grammar
} // urls
} // boost

#include <boost/url/grammar/impl/error.hpp>

#endif
