//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_STRING_VIEW_HPP
#define BOOST_URL_STRING_VIEW_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/detail/string_view.hpp>

namespace boost {
namespace urls {

/** The type of string_view used by the library

    String views are used to pass character
    buffers into or out of functions. Ownership
    of the underlying character buffer is not
    transferred; the caller is responsible for
    ensuring that the lifetime of character
    buffer extends until it is no longer
    referenced.

    @warning This alias is no longer supported and
    should not be used in new code. Please use
    `core::string_view` instead.

    This alias is included for backwards
    compatibility with earlier versions of the
    library.

    However, it will be removed in future releases,
    and using it in new code is not recommended.

    Please use the updated version instead to
    ensure compatibility with future versions of
    the library.

*/
using string_view
    BOOST_URL_DEPRECATED("Use core::string_view instead") =
    boost::core::string_view;

} // urls
} // boost

#endif
