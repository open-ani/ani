//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_ENCODE_HPP
#define BOOST_URL_DETAIL_ENCODE_HPP

#include <boost/url/encoding_opts.hpp>
#include <boost/url/pct_string_view.hpp>
#include <boost/url/grammar/hexdig_chars.hpp>
#include <boost/core/ignore_unused.hpp>
#include <cstdlib>

namespace boost {
namespace urls {
namespace detail {

constexpr
char const* const hexdigs[] = {
    "0123456789ABCDEF",
    "0123456789abcdef" };

//------------------------------------------------

// re-encode is to percent-encode a
// string that can already contain
// escapes. Characters not in the
// unreserved set are escaped, and
// escapes are passed through unchanged.
//
template<class CharSet>
std::size_t
re_encoded_size_unsafe(
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt) noexcept
{
    std::size_t n = 0;
    auto const end = s.end();
    auto it = s.begin();
    if(opt.space_as_plus)
    {
        while(it != end)
        {
            if(*it != '%')
            {
                if( unreserved(*it)
                    || *it == ' ')
                    n += 1; 
                else
                    n += 3;
                ++it;
            }
            else
            {
                BOOST_ASSERT(end - it >= 3);
                BOOST_ASSERT(
                    grammar::hexdig_value(
                        it[1]) >= 0);
                BOOST_ASSERT(
                    grammar::hexdig_value(
                        it[2]) >= 0);
                n += 3;
                it += 3;
            }
        }
    }
    else
    {
        while(it != end)
        {
            if(*it != '%')
            {
                if(unreserved(*it))
                    n += 1; 
                else
                    n += 3;
                ++it;
            }
            else
            {
                BOOST_ASSERT(end - it >= 3);
                BOOST_ASSERT(
                    grammar::hexdig_value(
                        it[1]) >= 0);
                BOOST_ASSERT(
                    grammar::hexdig_value(
                        it[2]) >= 0);
                n += 3;
                it += 3;
            }
        }
    }
    return n;
}

// unchecked
// returns decoded size
template<class CharSet>
std::size_t
re_encode_unsafe(
    char*& dest_,
    char const* const end,
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt) noexcept
{
    char const* const hex =
        detail::hexdigs[opt.lower_case];
    auto const encode = [end, hex](
        char*& dest,
        char c0) noexcept
    {
        auto c = static_cast<unsigned char>(c0);
        ignore_unused(end);
        *dest++ = '%';
        BOOST_ASSERT(dest != end);
        *dest++ = hex[c>>4];
        BOOST_ASSERT(dest != end);
        *dest++ = hex[c&0xf];
    };
    ignore_unused(end);

    auto dest = dest_;
    auto const dest0 = dest;
    auto const last = s.end();
    std::size_t dn = 0;
    auto it = s.begin();

    if(opt.space_as_plus)
    {
        while(it != last)
        {
            BOOST_ASSERT(dest != end);
            if(*it != '%')
            {
                if(*it == ' ')
                {
                    *dest++ = '+';
                }
                else if(unreserved(*it))
                {
                    *dest++ = *it;
                }
                else
                {
                    encode(dest, *it);
                    dn += 2;
                }
                ++it;
            }
            else
            {
                *dest++ = *it++;
                BOOST_ASSERT(dest != end);
                *dest++ = *it++;
                BOOST_ASSERT(dest != end);
                *dest++ = *it++;
                dn += 2;
            }
        }
    }
    else
    {
        while(it != last)
        {
            BOOST_ASSERT(dest != end);
            if(*it != '%')
            {
                if(unreserved(*it))
                {
                    *dest++ = *it;
                }
                else
                {
                    encode(dest, *it);
                    dn += 2;
                }
                ++it;
            }
            else
            {
                *dest++ = *it++;
                BOOST_ASSERT(dest != end);
                *dest++ = *it++;
                BOOST_ASSERT(dest != end);
                *dest++ = *it++;
                dn += 2;
            }
        }
    }
    dest_ = dest;
    return dest - dest0 - dn;
}

} // detail
} // urls
} // boost

#endif
