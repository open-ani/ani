//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_STATIC_STRING_HPP
#define BOOST_BEAST_STATIC_STRING_HPP

#include <boost/beast/core/detail/static_string.hpp>
#include <boost/static_string/static_string.hpp>

namespace boost {
namespace beast {

template<std::size_t N,
    class CharT = char,
    class Traits = std::char_traits<CharT> >
using static_string = boost::static_strings::basic_static_string<N, CharT,
    Traits>;

template<class Integer>
inline auto
to_static_string(Integer x)
-> decltype(boost::static_strings::to_static_string(x))
{
    return boost::static_strings::to_static_string(x);
}

} // beast
} // boost

#endif
