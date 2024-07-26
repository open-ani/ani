//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_DETAIL_EXCEPT_HPP
#define BOOST_JSON_DETAIL_EXCEPT_HPP

#include <boost/json/error.hpp>

namespace boost {
namespace json {
namespace detail {

#define BOOST_JSON_FAIL(ec, e) \
    BOOST_STATIC_CONSTEXPR source_location loc = BOOST_CURRENT_LOCATION; \
    (ec).assign(e, &loc);

BOOST_JSON_DECL
void
BOOST_NORETURN
throw_system_error(
    system::error_code const& ec,
    source_location const& loc = BOOST_CURRENT_LOCATION);

BOOST_JSON_DECL
void
BOOST_NORETURN
throw_system_error(
    error e,
    source_location const* loc);

} // detail
} // namespace json
} // namespace boost

#endif
