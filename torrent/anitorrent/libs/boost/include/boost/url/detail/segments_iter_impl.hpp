//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_SEGMENTS_ITER_IMPL_HPP
#define BOOST_URL_DETAIL_SEGMENTS_ITER_IMPL_HPP

#include <boost/url/detail/parts_base.hpp>
#include <boost/url/detail/url_impl.hpp>
#include <boost/core/detail/string_view.hpp>
#include <string>

namespace boost {
namespace urls {
namespace detail {

struct segments_iter_impl
    : private parts_base
{
    path_ref ref;
    std::size_t pos = 0;
    std::size_t next = 0;
    std::size_t index = 0;
    std::size_t dn = 0;
private:
    pct_string_view s_;
public:

    segments_iter_impl() = default;
    segments_iter_impl(
        segments_iter_impl const&) noexcept = default;
    segments_iter_impl& operator=(
        segments_iter_impl const&) noexcept = default;

    // begin
    segments_iter_impl(
        detail::path_ref const&) noexcept;

    // end
    segments_iter_impl(
        detail::path_ref const&,
        int) noexcept;

    // at index
    segments_iter_impl(
        url_impl const& u_,
        std::size_t pos_,
        std::size_t i_) noexcept;

    void update() noexcept;

    BOOST_URL_DECL
    void
    increment() noexcept;

    BOOST_URL_DECL
    void
    decrement() noexcept;

    pct_string_view
    dereference() const noexcept
    {
        return s_;
    }

    bool
    equal(
        segments_iter_impl const& other) const noexcept
    {
        BOOST_ASSERT(ref.alias_of(other.ref));
        return index == other.index;
    }
};

} // detail
} // urls
} // boost

#endif
