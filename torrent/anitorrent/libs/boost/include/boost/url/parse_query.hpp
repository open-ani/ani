//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/CPPAlliance/url
//

#ifndef BOOST_URL_PARSE_PARAMS_HPP
#define BOOST_URL_PARSE_PARAMS_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/params_encoded_view.hpp>
#include <boost/core/detail/string_view.hpp>

namespace boost {
namespace urls {

/** Parse a string and return an encoded params view

    This function parses the string and returns the
    corresponding params object if the string is valid,
    otherwise returns an error.

    @par BNF
    @code
    @endcode

    @par Exception Safety
    No-throw guarantee.

    @return A valid view on success, otherwise an
    error code.

    @param s The string to parse

    @par Specification

    @see
        @ref params_encoded_view.
*/
BOOST_URL_DECL
system::result<params_encoded_view>
parse_query(core::string_view s) noexcept;

} // urls
} // boost

#endif
