//
// Copyright (c) 2022 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_DETAIL_RECYCLED_HPP
#define BOOST_URL_GRAMMAR_DETAIL_RECYCLED_HPP

#include <utility>

namespace boost {
namespace urls {
namespace grammar {
namespace detail {

template<
    std::size_t Size,
    std::size_t Align>
struct aligned_storage_impl
{
    void* addr() noexcept
    {
        return buf_;
    }

    void const* addr() const noexcept
    {
        return buf_;
    }

private:
    alignas(Align)
    unsigned char buf_[Size];
};

constexpr
std::size_t
nearest_pow2(
    std::size_t x,
    std::size_t f = 0) noexcept
{
    return
        (f <= (std::size_t(-1)/2))
        ? ( x <= f
            ? f
            : nearest_pow2(x, 2 * f))
        : x;
}

//------------------------------------------------

BOOST_URL_DECL
void
recycled_add_impl(
    std::size_t) noexcept;

BOOST_URL_DECL
void
recycled_remove_impl(
    std::size_t) noexcept;

#ifdef BOOST_URL_REPORT

inline
void
recycled_add(
    std::size_t n) noexcept
{
    recycled_add_impl(n);
}

inline
void
recycled_remove(
    std::size_t n) noexcept
{
    recycled_remove_impl(n);
}

#else

inline void recycled_add(
    std::size_t) noexcept
{
}
inline void recycled_remove(
    std::size_t) noexcept
{
}

#endif

} // detail
} // grammar
} // urls
} // boost

#endif
