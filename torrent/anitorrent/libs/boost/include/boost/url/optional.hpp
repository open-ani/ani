//
// Copyright (c) 2022 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_OPTIONAL_HPP
#define BOOST_URL_OPTIONAL_HPP

#include <boost/url/detail/config.hpp>
#include <boost/optional.hpp>

namespace boost {
namespace urls {

/** The type of optional used by the library

    @warning This alias is no longer supported and
    should not be used in new code. Please use
    `boost::optional` instead.

    This alias is included for backwards
    compatibility with earlier versions of the
    library.

    However, it will be removed in future releases,
    and using it in new code is not recommended.

    Please use the updated version instead to
    ensure compatibility with future versions of
    the library.

*/
#ifndef BOOST_URL_DOCS
template<class T>
using optional
    BOOST_URL_DEPRECATED("Use boost::optional<T> instead") =
    boost::optional<T>;
#else
template<class T>
using optional = boost::optional<T>;
#endif

} // urls
} // boost

#endif
