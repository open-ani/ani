//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_CHARACTER_SET_IPP
#define BOOST_MYSQL_IMPL_CHARACTER_SET_IPP

#pragma once

#include <boost/mysql/character_set.hpp>

namespace boost {
namespace mysql {
namespace detail {

inline bool in_range(unsigned char byte, unsigned char lower, unsigned char upper) noexcept
{
    return byte >= lower && byte <= upper;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

std::size_t boost::mysql::detail::next_char_utf8mb4(span<const unsigned char> input) noexcept
{
    // s[0]    s[1]    s[2]    s[3]    comment
    // 00-7F                           ascii
    // 80-c1                           invalid
    // c2-df   80-bf                   2byte
    // e0      a0-bf   80-bf           3byte, case 1
    // e1-ec   80-bf   80-bf           3byte, case 2
    // ed      80-9f   80-bf           3byte, case 3 (surrogates)
    // ee-ef   80-bf   80-bf           3byte, case 2
    // f0      90-bf   80-bf   80-bf   4byte, case 1
    // f1-f3   80-bf   80-bf   80-bf   4byte, case 2
    // f4      80-8f   80-bf   80-bf   4byte, case 3

    BOOST_ASSERT(!input.empty());

    auto first_char = input.front();
    if (first_char < 0x80)
    {
        return 1;
    }
    else if (first_char < 0xc2)
    {
        return 0;
    }
    else if (first_char < 0xe0)
    {
        return (input.size() < 2u || !in_range(input[1], 0x80, 0xbf)) ? 0 : 2;
    }
    else if (first_char == 0xe0)
    {
        return (input.size() < 3u || !in_range(input[1], 0xa0, 0xbf) || !in_range(input[2], 0x80, 0xbf)) ? 0
                                                                                                         : 3;
    }
    else if (first_char == 0xed)
    {
        return (input.size() < 3u || !in_range(input[1], 0x80, 0x9f) || !in_range(input[2], 0x80, 0xbf)) ? 0
                                                                                                         : 3;
    }
    else if (first_char <= 0xef)
    {
        // Includes e1-ec and ee-ef
        return (input.size() < 3u || !in_range(input[1], 0x80, 0xbf) || !in_range(input[2], 0x80, 0xbf)) ? 0
                                                                                                         : 3;
    }
    else if (first_char == 0xf0)
    {
        return (input.size() < 4u || !in_range(input[1], 0x90, 0xbf) || !in_range(input[2], 0x80, 0xbf) ||
                !in_range(input[3], 0x80, 0xbf))
                   ? 0
                   : 4;
    }
    else if (first_char <= 0xf3)
    {
        return (input.size() < 4u || !in_range(input[1], 0x80, 0xbf) || !in_range(input[2], 0x80, 0xbf) ||
                !in_range(input[3], 0x80, 0xbf))
                   ? 0
                   : 4;
    }
    else if (first_char == 0xf4)
    {
        return (input.size() < 4u || !in_range(input[1], 0x80, 0x8f) || !in_range(input[2], 0x80, 0xbf) ||
                !in_range(input[3], 0x80, 0xbf))
                   ? 0
                   : 4;
    }
    else
    {
        return 0;
    }
}

#endif
