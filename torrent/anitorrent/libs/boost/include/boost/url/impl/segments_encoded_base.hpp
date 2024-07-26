//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IMPL_SEGMENTS_ENCODED_BASE_HPP
#define BOOST_URL_IMPL_SEGMENTS_ENCODED_BASE_HPP

#include <boost/url/detail/segments_iter_impl.hpp>
#include <boost/assert.hpp>

namespace boost {
namespace urls {

class segments_encoded_base::iterator
{
    detail::segments_iter_impl it_;

    friend class url_base;
    friend class segments_encoded_base;
    friend class segments_encoded_ref;

    iterator(detail::path_ref const&) noexcept;
    iterator(detail::path_ref const&, int) noexcept;

    iterator(
        detail::segments_iter_impl const& it) noexcept
        : it_(it)
    {
    }

public:
    using value_type =
        segments_encoded_base::value_type;
    using reference =
        segments_encoded_base::reference;
    using pointer = reference;
    using difference_type = std::ptrdiff_t;
    using iterator_category =
        std::bidirectional_iterator_tag;

    iterator() = default;
    iterator(iterator const&) = default;
    iterator& operator=(
        iterator const&) = default;

    reference
    operator*() const noexcept
    {
        return it_.dereference();
    }

    pointer
    operator->() const noexcept
    {
        return it_.dereference();
    }

    iterator&
    operator++() noexcept
    {
        it_.increment();
        return *this;
    }

    iterator&
    operator--() noexcept
    {
        it_.decrement();
        return *this;
    }

    iterator
    operator++(int) noexcept
    {
        auto tmp = *this;
        ++*this;
        return tmp;
    }

    iterator
    operator--(int) noexcept
    {
        auto tmp = *this;
        --*this;
        return tmp;
    }

    bool
    operator==(
        iterator const& other) const noexcept
    {
        return it_.equal(other.it_);
    }

    bool
    operator!=(
        iterator const& other) const noexcept
    {
        return ! it_.equal(other.it_);
    }
};

//------------------------------------------------

inline
pct_string_view
segments_encoded_base::
front() const noexcept
{
    BOOST_ASSERT(! empty());
    return *begin();
}

inline
pct_string_view
segments_encoded_base::
back() const noexcept
{
    BOOST_ASSERT(! empty());
    return *--end();
}

} // urls
} // boost

#endif
