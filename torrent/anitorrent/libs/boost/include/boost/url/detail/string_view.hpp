//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_STRING_VIEW_HPP
#define BOOST_URL_DETAIL_STRING_VIEW_HPP

#include <boost/core/detail/string_view.hpp>

namespace boost {
namespace urls {
namespace detail {

// We use detail::to_sv(s) instead of core::string_view(s) whenever
// we should convert to core::string_view.
// This is a workaround for GCC >=8.0 <8.4
// See: https://github.com/boostorg/url/issues/672
template<class T>
core::string_view
to_sv(T const& t) noexcept
{
    return core::string_view(t);
}

} // detail
} // urls
} // boost

#endif
