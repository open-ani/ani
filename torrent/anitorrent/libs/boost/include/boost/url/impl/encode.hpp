//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IMPL_ENCODE_HPP
#define BOOST_URL_IMPL_ENCODE_HPP

#include <boost/url/detail/encode.hpp>
#include <boost/url/detail/except.hpp>
#include <boost/url/encoding_opts.hpp>
#include <boost/url/grammar/charset.hpp>
#include <boost/url/grammar/hexdig_chars.hpp>
#include <boost/url/grammar/type_traits.hpp>
#include <boost/assert.hpp>
#include <boost/static_assert.hpp>

namespace boost {
namespace urls {

//------------------------------------------------

template<class CharSet>
std::size_t
encoded_size(
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt) noexcept
{
/*  If you get a compile error here, it
    means that the value you passed does
    not meet the requirements stated in
    the documentation.
*/
    static_assert(
        grammar::is_charset<CharSet>::value,
        "Type requirements not met");

    std::size_t n = 0;
    auto it = s.data();
    auto const last = it + s.size();

    if(! opt.space_as_plus ||
        unreserved(' '))
    {
        while(it != last)
        {
            if(unreserved(*it))
                n += 1;
            else
                n += 3;
            ++it;
        }
    }
    else
    {
        while(it != last)
        {
            auto c = *it;
            if(unreserved(c))
                ++n;
            else if(c == ' ')
                ++n;
            else
                n += 3;
            ++it;
        }
    }
    return n;
}

//------------------------------------------------

template<class CharSet>
std::size_t
encode(
    char* dest,
    std::size_t size,
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt)
{
/*  If you get a compile error here, it
    means that the value you passed does
    not meet the requirements stated in
    the documentation.
*/
    static_assert(
        grammar::is_charset<CharSet>::value,
        "Type requirements not met");

    // '%' must be reserved
    BOOST_ASSERT(! unreserved('%'));

    char const* const hex =
        detail::hexdigs[opt.lower_case];
    auto const encode = [hex](
        char*& dest,
        unsigned char c) noexcept
    {
        *dest++ = '%';
        *dest++ = hex[c>>4];
        *dest++ = hex[c&0xf];
    };

    auto it = s.data();
    auto const end = dest + size;
    auto const last = it + s.size();
    auto const dest0 = dest;
    auto const end3 = end - 3;

    if(! opt.space_as_plus)
    {
        while(it != last)
        {
            if(unreserved(*it))
            {
                if(dest == end)
                    return dest - dest0;
                *dest++ = *it++;
                continue;
            }
            if(dest > end3)
                return dest - dest0;
            encode(dest, *it++);
        }
        return dest - dest0;
    }
    else if(! unreserved(' '))
    {
        // VFALCO space is usually reserved,
        // and we depend on this for an
        // optimization. if this assert
        // goes off we can split the loop
        // below into two versions.
        BOOST_ASSERT(! unreserved(' '));

        while(it != last)
        {
            if(unreserved(*it))
            {
                if(dest == end)
                    return dest - dest0;
                *dest++ = *it++;
                continue;
            }
            if(*it == ' ')
            {
                if(dest == end)
                    return dest - dest0;
                *dest++ = '+';
                ++it;
                continue;
            }
            if(dest > end3)
                return dest - dest0;
            encode(dest, *it++);
        }
    }
    return dest - dest0;
}

//------------------------------------------------

// unsafe encode just
// asserts on the output buffer
//
template<class CharSet>
std::size_t
encode_unsafe(
    char* dest,
    std::size_t size,
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt)
{
    // '%' must be reserved
    BOOST_ASSERT(! unreserved('%'));

    auto it = s.data();
    auto const last = it + s.size();
    auto const end = dest + size;
    ignore_unused(end);

    char const* const hex =
        detail::hexdigs[opt.lower_case];
    auto const encode = [end, hex](
        char*& dest,
        unsigned char c) noexcept
    {
        ignore_unused(end);
        *dest++ = '%';
        BOOST_ASSERT(dest != end);
        *dest++ = hex[c>>4];
        BOOST_ASSERT(dest != end);
        *dest++ = hex[c&0xf];
    };

    auto const dest0 = dest;
    if(! opt.space_as_plus)
    {
        while(it != last)
        {
            BOOST_ASSERT(dest != end);
            if(unreserved(*it))
                *dest++ = *it++;
            else
                encode(dest, *it++);
        }
    }
    else
    {
        // VFALCO space is usually reserved,
        // and we depend on this for an
        // optimization. if this assert
        // goes off we can split the loop
        // below into two versions.
        BOOST_ASSERT(! unreserved(' '));

        while(it != last)
        {
            BOOST_ASSERT(dest != end);
            if(unreserved(*it))
            {
                *dest++ = *it++;
            }
            else if(*it == ' ')
            {
                *dest++ = '+';
                ++it;
            }
            else
            {
                encode(dest, *it++);
            }
        }
    }
    return dest - dest0;
}

//------------------------------------------------

template<
    class StringToken,
    class CharSet>
BOOST_URL_STRTOK_RETURN
encode(
    core::string_view s,
    CharSet const& unreserved,
    encoding_opts opt,
    StringToken&& token) noexcept
{
/*  If you get a compile error here, it
    means that the value you passed does
    not meet the requirements stated in
    the documentation.
*/
    static_assert(
        grammar::is_charset<CharSet>::value,
        "Type requirements not met");

    auto const n = encoded_size(
        s, unreserved, opt);
    auto p = token.prepare(n);
    if(n > 0)
        encode_unsafe(
            p, n, s, unreserved, opt);
    return token.result();
}

} // urls
} // boost

#endif
