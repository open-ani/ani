//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_PARTS_BASE_HPP
#define BOOST_URL_DETAIL_PARTS_BASE_HPP

#include <boost/url/error.hpp>

namespace boost {
namespace urls {
namespace detail {

// mix-in to provide part
// constants and variables
struct parts_base
{
    enum
    {
        id_scheme = -1, // trailing ':'
        id_user,        // leading "//"
        id_pass,        // leading ':', trailing '@'
        id_host,
        id_port,        // leading ':'
        id_path,
        id_query,       // leading '?'
        id_frag,        // leading '#'
        id_end          // one past the end
    };

    enum class from : char {
        // this belongs to a string
        string = 0,
        // this belongs to url_base
        // segments/params containers point to
        // another url
        url = 1,
        // this belongs to authority_view
        // id_user does not have the leading "//"
        authority = 2,
    };
};

} // detail
} // urls
} // boost

#endif
