//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_IMPL_RANGE_HPP
#define BOOST_URL_GRAMMAR_IMPL_RANGE_HPP

#include <boost/url/detail/except.hpp>
#include <boost/url/grammar/error.hpp>
#include <boost/url/grammar/recycled.hpp>
#include <boost/core/empty_value.hpp>
#include <boost/assert.hpp>
#include <boost/static_assert.hpp>
#include <exception>
#include <iterator>
#include <new>

#include <stddef.h> // ::max_align_t

namespace boost {
namespace urls {
namespace grammar {

// VFALCO This could be reused for
// other things that need to type-erase

//------------------------------------------------
//
// any_rule
//
//------------------------------------------------

// base class for the type-erased rule pair
template<class T>
struct range<T>::
    any_rule
{
    virtual
    ~any_rule() = default;

    virtual
    void
    move(void* dest) noexcept
    {
        ::new(dest) any_rule(
            std::move(*this));
    }

    virtual
    void
    copy(void* dest) const noexcept
    {
        ::new(dest) any_rule(*this);
    }

    virtual
    system::result<T>
    first(
        char const*&,
        char const*) const noexcept
    {
        return system::error_code{};
    }

    virtual
    system::result<T>
    next(
        char const*&,
        char const*) const noexcept
    {
        return system::error_code{};
    }
};

//------------------------------------------------

// small
template<class T>
template<class R, bool Small>
struct range<T>::impl1
    : any_rule
    , private empty_value<R>
{
    explicit
    impl1(R const& next) noexcept
        : empty_value<R>(
            empty_init,
            next)
    {
    }

private:
    impl1(impl1&&) noexcept = default;
    impl1(impl1 const&) noexcept = default;

    void
    move(void* dest
        ) noexcept override
    {
        ::new(dest) impl1(
            std::move(*this));
    }

    void
    copy(void* dest
        ) const noexcept override
    {
        ::new(dest) impl1(*this);
    }

    system::result<T>
    first(
        char const*& it,
        char const* end)
            const noexcept override
    {
        return grammar::parse(
            it, end, this->get());
    }

    system::result<T>
    next(
        char const*& it,
        char const* end)
            const noexcept override
    {
        return grammar::parse(
            it, end, this->get());
    }
};

//------------------------------------------------

// big
template<class T>
template<class R>
struct range<T>::impl1<R, false>
    : any_rule
{
    explicit
    impl1(R const& next) noexcept
    {
        ::new(p_->addr()) impl{next};
    }

private:
    struct impl
    {
        R r;
    };

    recycled_ptr<
        aligned_storage<impl>> p_;

    impl1(impl1&&) noexcept = default;
    impl1(impl1 const&) noexcept = default;

    impl const&
    get() const noexcept
    {
        return *reinterpret_cast<
            impl const*>(p_->addr());
    }

    ~impl1()
    {
        if(p_)
            get().~impl();
    }

    void
    move(void* dest
        ) noexcept override
    {
        ::new(dest) impl1(
            std::move(*this));
    }

    void
    copy(void* dest
        ) const noexcept override
    {
        ::new(dest) impl1(*this);
    }

    system::result<T>
    first(
        char const*& it,
        char const* end)
            const noexcept override
    {
        return grammar::parse(
            it, end, this->get().r);
    }

    system::result<T>
    next(
        char const*& it,
        char const* end)
            const noexcept override
    {
        return grammar::parse(
            it, end, this->get().r);
    }
};

//------------------------------------------------

// small
template<class T>
template<
    class R0, class R1, bool Small>
struct range<T>::impl2
    : any_rule
    , private empty_value<R0, 0>
    , private empty_value<R1, 1>
{
    impl2(
        R0 const& first,
        R1 const& next) noexcept
        : empty_value<R0,0>(
            empty_init, first)
        , empty_value<R1,1>(
            empty_init, next)
    {
    }

private:
    impl2(impl2&&) noexcept = default;
    impl2(impl2 const&) noexcept = default;

    void
    move(void* dest
        ) noexcept override
    {
        ::new(dest) impl2(
            std::move(*this));
    }

    void
    copy(void* dest
        ) const noexcept override
    {
        ::new(dest) impl2(*this);
    }

    system::result<T>
    first(
        char const*& it,
        char const* end)
            const noexcept override
    {
        return grammar::parse(it, end,
            empty_value<
                R0,0>::get());
    }

    system::result<T>
    next(
        char const*& it,
        char const* end)
            const noexcept override
    {
        return grammar::parse(it, end,
            empty_value<
                R1,1>::get());
    }
};

//------------------------------------------------

// big
template<class T>
template<
    class R0, class R1>
struct range<T>::impl2<R0, R1, false>
    : any_rule
{
    impl2(
        R0 const& first,
        R1 const& next) noexcept
    {
        ::new(p_->addr()) impl{
            first, next};
    }

private:
    struct impl
    {
        R0 first;
        R1 next;
    };

    recycled_ptr<
        aligned_storage<impl>> p_;

    impl2(impl2&&) noexcept = default;
    impl2(impl2 const&) noexcept = default;

    impl const&
    get() const noexcept
    {
        return *reinterpret_cast<
            impl const*>(p_->addr());
    }

    ~impl2()
    {
        if(p_)
            get().~impl();
    }

    void
    move(void* dest
        ) noexcept override
    {
        ::new(dest) impl2(
            std::move(*this));
    }

    void
    copy(void* dest
        ) const noexcept override
    {
        ::new(dest) impl2(*this);
    }

    system::result<T>
    first(
        char const*& it,
        char const* end)
            const noexcept override
    {
        return grammar::parse(
            it, end, get().first);
    }

    system::result<T>
    next(
        char const*& it,
        char const* end)
            const noexcept override
    {
        return grammar::parse(
            it, end, get().next);
    }
};

//------------------------------------------------
//
// iterator
//
//------------------------------------------------

template<class T>
class range<T>::
    iterator
{
public:
    using value_type = T;
    using reference = T const&;
    using pointer = void const*;
    using difference_type =
        std::ptrdiff_t;
    using iterator_category =
        std::forward_iterator_tag;

    iterator() = default;
    iterator(
        iterator const&) = default;
    iterator& operator=(
        iterator const&) = default;

    reference
    operator*() const noexcept
    {
        return *rv_;
    }

    bool
    operator==(
        iterator const& other) const noexcept
    {
        // can't compare iterators
        // from different containers!
        BOOST_ASSERT(r_ == other.r_);

        return p_ == other.p_;
    }

    bool
    operator!=(
        iterator const& other) const noexcept
    {
        return !(*this == other);
    }

    iterator&
    operator++() noexcept
    {
        BOOST_ASSERT(
            p_ != nullptr);
        auto const end =
            r_->s_.data() +
            r_->s_.size();
        rv_ = r_->get().next(p_, end);
        if( !rv_ )
            p_ = nullptr;
        return *this;
    }

    iterator
    operator++(int) noexcept
    {
        auto tmp = *this;
        ++*this;
        return tmp;
    }

private:
    friend class range<T>;

    range<T> const* r_ = nullptr;
    char const* p_ = nullptr;
    system::result<T> rv_;

    iterator(
        range<T> const& r) noexcept
        : r_(&r)
        , p_(r.s_.data())
    {
        auto const end =
            r_->s_.data() +
            r_->s_.size();
        rv_ = r_->get().first(p_, end);
        if( !rv_ )
            p_ = nullptr;
    }

    constexpr
    iterator(
        range<T> const& r,
        int) noexcept
        : r_(&r)
        , p_(nullptr)
    {
    }
};

//------------------------------------------------

template<class T>
template<class R>
range<T>::
range(
    core::string_view s,
    std::size_t n,
    R const& next)
    : s_(s)
    , n_(n)
{
    BOOST_STATIC_ASSERT(
        sizeof(impl1<R, false>) <=
            BufferSize);

    ::new(&get()) impl1<R,
        sizeof(impl1<R, true>) <=
            BufferSize>(next);
}

//------------------------------------------------

template<class T>
template<
    class R0, class R1>
range<T>::
range(
    core::string_view s,
    std::size_t n,
    R0 const& first,
    R1 const& next)
    : s_(s)
    , n_(n)
{
    BOOST_STATIC_ASSERT(
        sizeof(impl2<R0, R1, false>) <=
            BufferSize);

    ::new(&get()) impl2<R0, R1,
        sizeof(impl2<R0, R1, true>
            ) <= BufferSize>(
                first, next);
}

//------------------------------------------------

template<class T>
range<T>::
~range()
{
    get().~any_rule();
}

template<class T>
range<T>::
range() noexcept
{
    ::new(&get()) any_rule{};
    char const* it = nullptr;
    get().first(it, nullptr);
    get().next(it, nullptr);
}

template<class T>
range<T>::
range(
    range&& other) noexcept
    : s_(other.s_)
    , n_(other.n_)
{
    other.s_ = {};
    other.n_ = {};
    other.get().move(&get());
    other.get().~any_rule();
    ::new(&other.get()) any_rule{};
}

template<class T>
range<T>::
range(
    range const& other) noexcept
    : s_(other.s_)
    , n_(other.n_)
{
    other.get().copy(&get());
}

template<class T>
auto
range<T>::
operator=(
    range&& other) noexcept ->
        range&
{
    s_ = other.s_;
    n_ = other.n_;
    other.s_ = {};
    other.n_ = 0;
    // VFALCO we rely on nothrow move
    // construction here, but if necessary we
    // could move to a local buffer first.
    get().~any_rule();
    other.get().move(&get());
    other.get().~any_rule();
    ::new(&other.get()) any_rule{};
    return *this;
}

template<class T>
auto
range<T>::
operator=(
    range const& other) noexcept ->
        range&
{
    s_ = other.s_;
    n_ = other.n_;
    // VFALCO we rely on nothrow copy
    // construction here, but if necessary we
    // could construct to a local buffer first.
    get().~any_rule();
    other.get().copy(&get());
    return *this;
}

template<class T>
auto
range<T>::
begin() const noexcept ->
    iterator
{
    return { *this };
}

template<class T>
auto
range<T>::
end() const noexcept ->
    iterator
{
    return { *this, 0 };
}

//------------------------------------------------

template<class R>
auto
range_rule_t<R>::
parse(
    char const*& it,
    char const* end) const ->
        system::result<value_type>
{
    using T = typename R::value_type;

    std::size_t n = 0;
    auto const it0 = it;
    auto it1 = it;
    auto rv = (grammar::parse)(
        it, end, next_);
    if( !rv )
    {
        if(rv.error() != error::end_of_range)
        {
            // rewind unless error::end_of_range
            it = it1;
        }
        if(n < N_)
        {
            // too few
            BOOST_URL_RETURN_EC(
                error::mismatch);
        }
        // good
        return range<T>(
            core::string_view(it0, it - it0),
                n, next_);
    }
    for(;;)
    {
        ++n;
        it1 = it;
        rv = (grammar::parse)(
            it, end, next_);
        if( !rv )
        {
            if(rv.error() != error::end_of_range)
            {
                // rewind unless error::end_of_range
                it = it1;
            }
            break;
        }
        if(n >= M_)
        {
            // too many
            BOOST_URL_RETURN_EC(
                error::mismatch);
        }
    }
    if(n < N_)
    {
        // too few
        BOOST_URL_RETURN_EC(
            error::mismatch);
    }
    // good
    return range<T>(
        core::string_view(it0, it - it0),
            n, next_);
}

//------------------------------------------------

template<class R0, class R1>
auto
range_rule_t<R0, R1>::
parse(
    char const*& it,
    char const* end) const ->
        system::result<range<typename
            R0::value_type>>
{
    using T = typename R0::value_type;

    std::size_t n = 0;
    auto const it0 = it;
    auto it1 = it;
    auto rv = (grammar::parse)(
        it, end, first_);
    if( !rv )
    {
        if(rv.error() != error::end_of_range)
        {
            // rewind unless error::end_of_range
            it = it1;
        }
        if(n < N_)
        {
            // too few
            BOOST_URL_RETURN_EC(
                error::mismatch);
        }
        // good
        return range<T>(
            core::string_view(it0, it - it0),
                n, first_, next_);
    }
    for(;;)
    {
        ++n;
        it1 = it;
        rv = (grammar::parse)(
            it, end, next_);
        if( !rv )
        {
            if(rv.error() != error::end_of_range)
            {
                // rewind unless error::end_of_range
                it = it1;
            }
            break;
        }
        if(n >= M_)
        {
            // too many
            BOOST_URL_RETURN_EC(
                error::mismatch);
        }
    }
    if(n < N_)
    {
        // too few
        BOOST_URL_RETURN_EC(
            error::mismatch);
    }
    // good
    return range<T>(
        core::string_view(it0, it - it0),
            n, first_, next_);
}

} // grammar
} // urls
} // boost

#endif
