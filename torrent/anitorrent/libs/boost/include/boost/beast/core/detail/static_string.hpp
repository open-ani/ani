//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_DETAIL_STATIC_STRING_HPP
#define BOOST_BEAST_DETAIL_STATIC_STRING_HPP

#include <boost/assert.hpp>
#include <boost/core/ignore_unused.hpp>
#include <string>
#include <type_traits>

namespace boost {
namespace beast {
namespace detail {

// Maximum number of characters in the decimal
// representation of a binary number. This includes
// the potential minus sign.
//
inline
std::size_t constexpr
max_digits(std::size_t bytes)
{
    return static_cast<std::size_t>(
        bytes * 2.41) + 1 + 1;
}

template<class CharT, class Integer, class Traits>
CharT*
raw_to_string(
    CharT* buf, Integer x, std::true_type)
{
    if(x == 0)
    {
        Traits::assign(*--buf, '0');
        return buf;
    }
    if(x < 0)
    {
        x = -x;
        for(;x > 0; x /= 10)
            Traits::assign(*--buf ,
                "0123456789"[x % 10]);
        Traits::assign(*--buf, '-');
        return buf;
    }
    for(;x > 0; x /= 10)
        Traits::assign(*--buf ,
            "0123456789"[x % 10]);
    return buf;
}

template<class CharT, class Integer, class Traits>
CharT*
raw_to_string(
    CharT* buf, Integer x, std::false_type)
{
    if(x == 0)
    {
        *--buf = '0';
        return buf;
    }
    for(;x > 0; x /= 10)
        Traits::assign(*--buf ,
            "0123456789"[x % 10]);
    return buf;
}

template<
    class CharT,
    class Integer,
    class Traits = std::char_traits<CharT>>
CharT*
raw_to_string(CharT* last, std::size_t size, Integer i)
{
    boost::ignore_unused(size);
    BOOST_ASSERT(size >= max_digits(sizeof(Integer)));
    return raw_to_string<CharT, Integer, Traits>(
        last, i, std::is_signed<Integer>{});
}

} // detail
} // beast
} // boost

#endif
