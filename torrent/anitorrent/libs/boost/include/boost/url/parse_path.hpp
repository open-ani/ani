//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARSE_PATH_HPP
#define BOOST_URL_PARSE_PATH_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/segments_encoded_view.hpp>

namespace boost {
namespace urls {

/** Parse a string and return an encoded segment view

    This function parses the string and returns the
    corresponding path object if the string is valid,
    otherwise returns an error.

    @par BNF
    @code
    path          = [ "/" ] segment *( "/" segment )
    @endcode

    @par Exception Safety
    No-throw guarantee.

    @return A valid view on success, otherwise an
    error code.

    @param s The string to parse

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
        >3.3.  Path (rfc3986)</a>

    @see
        @ref segments_encoded_view.
*/
BOOST_URL_DECL
system::result<segments_encoded_view>
parse_path(core::string_view s) noexcept;

} // urls
} // boost

#endif
