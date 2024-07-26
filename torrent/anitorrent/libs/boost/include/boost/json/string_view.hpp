//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_STRING_VIEW_HPP
#define BOOST_JSON_STRING_VIEW_HPP

#include <boost/json/detail/config.hpp>
#include <boost/core/detail/string_view.hpp>
#include <type_traits>
#ifndef BOOST_NO_CXX17_HDR_STRING_VIEW
# include <string_view>
#endif

namespace boost {
namespace json {

#ifdef BOOST_JSON_DOCS

/** The type of string view used by the library.

    The type has API equivalent to that of `std::string_view` and is
    convertible to/from it.
*/
using string_view = __see_below__;

#else

using string_view = boost::core::string_view;

#endif

namespace detail {

template<class T>
using is_string_viewish = typename std::enable_if<
    std::is_convertible<
        T const&, string_view>::value &&
    ! std::is_convertible<
        T const&, char const*>::value
            >::type;

} // detail

} // namespace json
} // namespace boost

#endif
