//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_ENCODING_OPTS_HPP
#define BOOST_URL_ENCODING_OPTS_HPP

#include <boost/url/detail/config.hpp>

namespace boost {
namespace urls {

/** Percent-encoding options

    These options are used to customize
    the behavior of algorithms which use
    percent escapes, such as encoding
    or decoding.

    @see
        @ref encode,
        @ref encoded_size,
        @ref pct_string_view.
*/
struct BOOST_URL_DECL encoding_opts
{
    /** True if spaces encode to and from plus signs

        This option controls whether or not
        the PLUS character ("+") is used to
        represent the SP character (" ") when
        encoding or decoding.
        Although not prescribed by the RFC, plus
        signs are commonly treated as spaces upon
        decoding when used in the query of URLs
        using well known schemes such as HTTP.

        @par Specification
        @li <a href="https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1">
            application/x-www-form-urlencoded (w3.org)</a>
    */
    bool space_as_plus = false;

    /** True if hexadecimal digits are emitted as lower case

        By default, percent-encoding algorithms
        emit hexadecimal digits A through F as
        uppercase letters. When this option is
        `true`, lowercase letters are used.
    */
    bool lower_case = false;

    /** True if nulls are not allowed

        Normally all possible character values
        (from 0 to 255) are allowed, with reserved
        characters being replaced with escapes
        upon encoding. When this option is true,
        attempting to decode a null will result
        in an error.
    */
    bool disallow_null = false;

#ifndef BOOST_URL_DOCS
    encoding_opts(
        bool space_as_plus_ = false,
        bool lower_case_ = false,
        bool disallow_null_ = false) noexcept;
#endif
};

} // urls
} // boost

#endif
