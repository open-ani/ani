//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IMPL_PARAMS_BASE_HPP
#define BOOST_URL_IMPL_PARAMS_BASE_HPP

#include <boost/url/detail/params_iter_impl.hpp>
#include <iterator>

namespace boost {
namespace urls {

//------------------------------------------------

class BOOST_URL_DECL params_base::iterator
{
    detail::params_iter_impl it_;
    bool space_as_plus_ = true;

    friend class params_base;
    friend class params_ref;

    iterator(
        detail::query_ref const& ref,
        encoding_opts opt) noexcept;

    iterator(
        detail::query_ref const& impl,
        encoding_opts opt,
        int) noexcept;

    iterator(
        detail::params_iter_impl const& it,
        encoding_opts opt) noexcept
        : it_(it)
        , space_as_plus_(opt.space_as_plus)
    {
    }

public:
    using value_type = params_base::value_type;
    using reference = params_base::reference;
    using pointer = reference;
    using difference_type =
        params_base::difference_type;
    using iterator_category =
        std::bidirectional_iterator_tag;

    iterator() = default;
    iterator(iterator const&) = default;
    iterator& operator=(
        iterator const&) noexcept = default;

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
    operator*() const;

    // the return value is too expensive
    pointer operator->() const = delete;

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


} // urls
} // boost

#endif
