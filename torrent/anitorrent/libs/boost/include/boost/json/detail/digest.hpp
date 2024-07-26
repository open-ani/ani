//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_DETAIL_DIGEST_HPP
#define BOOST_JSON_DETAIL_DIGEST_HPP

namespace boost {
namespace json {
namespace detail {

// Calculate salted digest of string
template<class ForwardIterator>
std::size_t
digest(
    ForwardIterator b,
    ForwardIterator e,
    std::size_t salt) noexcept
{
#if BOOST_JSON_ARCH == 64
    std::uint64_t const prime = 0x100000001B3ULL;
    std::uint64_t hash  = 0xcbf29ce484222325ULL;
#else
    std::uint32_t const prime = 0x01000193UL;
    std::uint32_t hash  = 0x811C9DC5UL;
#endif
    hash += salt;
    for(; b != e; ++b)
        hash = (*b ^ hash) * prime;
    return hash;
}

} // detail
} // namespace json
} // namespace boost

#endif
