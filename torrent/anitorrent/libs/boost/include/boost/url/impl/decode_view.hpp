//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IMPL_PCT_ENCODED_VIEW_HPP
#define BOOST_URL_IMPL_PCT_ENCODED_VIEW_HPP

#include <boost/url/grammar/type_traits.hpp>
#include <boost/static_assert.hpp>

namespace boost {
namespace urls {

class decode_view::iterator
{
    char const* begin_ = nullptr;
    char const* pos_ = nullptr;
    bool space_as_plus_ = true;

    friend decode_view;

    iterator(
        char const* str,
        bool space_as_plus) noexcept
        : begin_(str)
        , pos_(str)
        , space_as_plus_(
            space_as_plus)
    {
    }

    // end ctor
    iterator(
        char const* str,
        size_type n,
        bool space_as_plus) noexcept
        : begin_(str)
        , pos_(str + n)
        , space_as_plus_(space_as_plus)
    {
    }

public:
    using value_type = char;
    using reference = char;
    using pointer = void const*;
    using const_reference = char;
    using size_type = std::size_t;
    using difference_type = std::ptrdiff_t;
    using iterator_category =
        std::bidirectional_iterator_tag;

    iterator() = default;

    iterator(iterator const&) = default;

    iterator&
    operator=(iterator const&) = default;

    BOOST_URL_DECL
    reference
    operator*() const noexcept;

    iterator&
    operator++() noexcept
    {
        BOOST_ASSERT(pos_ != nullptr);
        if (*pos_ != '%')
            ++pos_;
        else
            pos_ += 3;
        return *this;
    }

    iterator&
    operator--() noexcept
    {
        BOOST_ASSERT(pos_ != begin_);
        if (pos_ - begin_ < 3 ||
                pos_[-3] != '%')
            --pos_;
        else
            pos_ -= 3;
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

    char const*
    base()
    {
        return pos_;
    }

    bool
    operator==(
        iterator const& other) const noexcept
    {
        return pos_ == other.pos_;
    }

    bool
    operator!=(
        iterator const& other) const noexcept
    {
        return !(*this == other);
    }
};

//------------------------------------------------

inline
auto
decode_view::
begin() const noexcept ->
    const_iterator
{
    return {p_, space_as_plus_};
}

inline
auto
decode_view::
end() const noexcept ->
    const_iterator
{
    return {p_, n_, space_as_plus_};
}

inline
auto
decode_view::
front() const noexcept ->
    const_reference
{
    BOOST_ASSERT( !empty() );
    return *begin();
}

inline
auto
decode_view::
back() const noexcept ->
    const_reference
{
    BOOST_ASSERT( !empty() );
    return *--end();
}

} // urls
} // boost

#endif
