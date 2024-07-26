//
// Copyright (c) 2022 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_VARIANT_HPP
#define BOOST_URL_VARIANT_HPP

#include <boost/url/detail/config.hpp>
#include <boost/variant2/variant.hpp>

namespace boost {
namespace urls {

/** The type of variant used by the library

    @warning This alias is no longer supported and
    should not be used in new code. Please use
    `boost::variant2::variant` instead.

    This alias is included for backwards
    compatibility with earlier versions of the
    library.

    However, it will be removed in future releases,
    and using it in new code is not recommended.

    Please use the updated version instead to
    ensure compatibility with future versions of
    the library.

*/
template<class... Ts>
using variant
    BOOST_URL_DEPRECATED("Use variant2::variant instead") =
    boost::variant2::variant<Ts...>;

} // urls
} // boost

#endif
