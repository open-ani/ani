//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_FORMAT_HPP
#define BOOST_URL_DETAIL_FORMAT_HPP

#include <boost/url/detail/format_args.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/url.hpp>

namespace boost {
namespace urls {
namespace detail {

BOOST_URL_DECL
void
vformat_to(
    url_base& u,
    core::string_view fmt,
    detail::format_args args);

inline
url
vformat(
    core::string_view fmt,
    detail::format_args args)
{
    url u;
    vformat_to(u, fmt, args);
    return u;
}

} // detail
} // url
} // boost

#endif
