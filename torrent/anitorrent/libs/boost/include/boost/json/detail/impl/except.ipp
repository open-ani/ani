//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_DETAIL_IMPL_EXCEPT_IPP
#define BOOST_JSON_DETAIL_IMPL_EXCEPT_IPP

#include <boost/json/detail/except.hpp>
#include <boost/version.hpp>
#include <boost/throw_exception.hpp>
#include <stdexcept>

namespace boost {
namespace json {

namespace detail {

void
throw_system_error(
    system::error_code const& ec,
    source_location const& loc)
{
    throw_exception(
        system::system_error(ec),
        loc);
}

void
throw_system_error(
    error e,
    source_location const* loc)
{
    system::error_code ec;
    ec.assign(e, loc);

    throw_exception(
        system::system_error(ec),
        *loc);
}

} // detail
} // namespace json
} // namespace boost

#endif
