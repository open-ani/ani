//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_EXCEPT_HPP
#define BOOST_URL_DETAIL_EXCEPT_HPP

#include <boost/url/error_types.hpp>
#include <boost/assert/source_location.hpp>

namespace boost {
namespace urls {
namespace detail {

BOOST_URL_DECL void BOOST_NORETURN
throw_system_error(
    system::error_code const& ec,
    source_location const& loc =
        BOOST_URL_POS);

BOOST_URL_DECL void BOOST_NORETURN
throw_errc(
    boost::system::errc::errc_t ev,
    source_location const& loc =
        BOOST_URL_POS);

//-----

BOOST_URL_DECL void BOOST_NORETURN
throw_invalid_argument(
    source_location const& loc =
        BOOST_URL_POS);

BOOST_URL_DECL void BOOST_NORETURN
throw_length_error(
    source_location const& loc =
        BOOST_URL_POS);

} // detail
} // urls
} // boost

#endif
