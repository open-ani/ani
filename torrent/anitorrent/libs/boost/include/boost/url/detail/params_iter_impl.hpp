//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_PARAMS_ITER_IMPL_HPP
#define BOOST_URL_DETAIL_PARAMS_ITER_IMPL_HPP

#include <boost/url/param.hpp>
#include <boost/url/detail/parts_base.hpp>
#include <boost/url/detail/url_impl.hpp>
#include <boost/assert.hpp>

namespace boost {
namespace urls {
namespace detail {

struct BOOST_URL_DECL params_iter_impl
    : parts_base
{
    query_ref ref;
    std::size_t index = 0;
    std::size_t pos;
    std::size_t nk;
    std::size_t nv;
    std::size_t dk;
    std::size_t dv;

    params_iter_impl() = default;
    params_iter_impl(
        params_iter_impl const&) = default;
    params_iter_impl& operator=(
        params_iter_impl const&) = default;

    // begin
    params_iter_impl(
        query_ref const&) noexcept;

    // end
    params_iter_impl(
        query_ref const&,
        int) noexcept;

    // at index
    params_iter_impl(
        query_ref const&,
        std::size_t,
        std::size_t) noexcept;
    void setup() noexcept;
    void increment() noexcept;
    void decrement() noexcept;
    param_pct_view
        dereference() const noexcept;
    pct_string_view key() const noexcept;

    auto
    next() const noexcept ->
        params_iter_impl
    {
        auto next = *this;
        next.increment();
        return next;
    }

    bool
    equal(
        params_iter_impl const&
            other) const noexcept
    {
        // different containers
        BOOST_ASSERT(ref.alias_of(other.ref));
        return index == other.index;
    }
};

} // detail
} // urls
} // boost

#endif
