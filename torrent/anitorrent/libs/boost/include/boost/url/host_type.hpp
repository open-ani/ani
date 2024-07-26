//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_HOST_TYPE_HPP
#define BOOST_URL_HOST_TYPE_HPP

#include <boost/url/detail/config.hpp>

namespace boost {
namespace urls {

/** Identifies the type of host in a URL.

    Values of this type are returned by URL views
    and containers to indicate the type of host
    present in a URL.
*/
enum class host_type
{
    // VFALCO 3 space indent or
    // else Doxygen malfunctions

    /**
     * No host is specified.
    */
    none,

    /**
     * A host is specified by reg-name.
    */
    name,

    /**
     * A host is specified by @ref ipv4_address.
    */
    ipv4,

    /**
     * A host is specified by @ref ipv6_address.
    */
    ipv6,

    /**
     * A host is specified by IPvFuture.
    */
    ipvfuture
};

} // urls
} // boost

#endif
