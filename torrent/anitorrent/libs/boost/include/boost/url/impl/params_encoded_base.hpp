//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IMPL_PARAMS_ENCODED_BASE_HPP
#define BOOST_URL_IMPL_PARAMS_ENCODED_BASE_HPP

#include <boost/url/detail/params_iter_impl.hpp>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
class params_ref;
#endif

//------------------------------------------------

class params_encoded_base::iterator
{
    detail::params_iter_impl it_;

    friend class params_encoded_base;
    friend class params_encoded_ref;

    iterator(detail::query_ref const& ref) noexcept;
    iterator(detail::query_ref const& ref, int) noexcept;
    iterator(
        detail::params_iter_impl const& it)
        : it_(it)
    {
    }

public:
    using value_type =
        params_encoded_base::value_type;
    using reference =
        params_encoded_base::reference;
    using pointer = reference;
    using difference_type = std::ptrdiff_t;
    using iterator_category =
        std::bidirectional_iterator_tag;

    iterator() = default;
    iterator(iterator const&) = default;
    iterator& operator=(
        iterator const&) = default;

    iterator&
    operator++() noexcept
    {
        it_.increment();
        return *this;
    }

    iterator
    operator++(int) noexcept
    {
        auto tmp = *this;
        ++*this;
        return tmp;
    }

    iterator&
    operator--() noexcept
    {
        it_.decrement();
        return *this;
    }

    iterator
    operator--(int) noexcept
    {
        auto tmp = *this;
        --*this;
        return tmp;
    }

    reference
    operator*() const
    {
        return it_.dereference();
    }

    pointer
    operator->() const
    {
        return it_.dereference();
    }

    friend
    bool
    operator==(
        iterator const& it0,
        iterator const& it1) noexcept
    {
        return it0.it_.equal(it1.it_);
    }

    friend
    bool
    operator!=(
        iterator const& it0,
        iterator const& it1) noexcept
    {
        return ! it0.it_.equal(it1.it_);
    }
};

//------------------------------------------------
//
// Observers
//
//------------------------------------------------

inline
bool
params_encoded_base::
contains(
    pct_string_view key,
    ignore_case_param ic) const noexcept
{
    return find_impl(
        begin().it_, key, ic) != end();
}

inline
auto
params_encoded_base::
find(
    pct_string_view key,
    ignore_case_param ic) const noexcept ->
        iterator
{
    return find_impl(
        begin().it_, key, ic);
}

inline
auto
params_encoded_base::
find(
    iterator it,
    pct_string_view key,
    ignore_case_param ic) const noexcept ->
        iterator
{
    return find_impl(
        it.it_, key, ic);
}

inline
auto
params_encoded_base::
find_last(
    pct_string_view key,
    ignore_case_param ic) const noexcept ->
        iterator
{
    return find_last_impl(
        end().it_, key, ic);
}

inline
auto
params_encoded_base::
find_last(
    iterator it,
    pct_string_view key,
    ignore_case_param ic) const noexcept ->
        iterator
{
    return find_last_impl(
        it.it_, key, ic);
}

} // urls
} // boost

#endif
