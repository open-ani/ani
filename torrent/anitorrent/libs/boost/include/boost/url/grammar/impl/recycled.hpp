//
// Copyright (c) 2022 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_IMPL_RECYCLED_PTR_HPP
#define BOOST_URL_GRAMMAR_IMPL_RECYCLED_PTR_HPP

#include <boost/assert.hpp>

namespace boost {
namespace urls {
namespace grammar {

//------------------------------------------------

template<class T>
recycled<T>::
~recycled()
{
    std::size_t n = 0;
    // VFALCO we should probably deallocate
    // in reverse order of allocation but
    // that requires a doubly-linked list.
    auto it = head_;
    while(it)
    {
        ++n;
        auto next = it->next;
        BOOST_ASSERT(
            it->refs == 0);
        delete it;
        it = next;
    }
    detail::recycled_remove(
        sizeof(U) * n);
}

template<class T>
auto
recycled<T>::
acquire() ->
    U*
{
    U* p;
    {
#if !defined(BOOST_URL_DISABLE_THREADS)
        std::lock_guard<
            std::mutex> lock(m_);
#endif
        p = head_;
        if(p)
        {
            // reuse
            head_ = head_->next;
            detail::recycled_remove(
                sizeof(U));
            ++p->refs;
        }
        else
        {
            p = new U;
        }
    }
    BOOST_ASSERT(p->refs == 1);
    return p;
}

template<class T>
void
recycled<T>::
release(U* u) noexcept
{
    if(--u->refs != 0)
        return;
    {
#if !defined(BOOST_URL_DISABLE_THREADS)
        std::lock_guard<
            std::mutex> lock(m_);
#endif
        u->next = head_;
        head_ = u;
    }
    detail::recycled_add(
        sizeof(U));
}

//------------------------------------------------

template<class T>
recycled_ptr<T>::
~recycled_ptr()
{
    if(p_)
        bin_->release(p_);
}

template<class T>
recycled_ptr<T>::
recycled_ptr(
    recycled<T>& bin)
    : bin_(&bin)
    , p_(bin.acquire())
{
}

template<class T>
recycled_ptr<T>::
recycled_ptr(
    recycled<T>& bin,
    std::nullptr_t) noexcept
    : bin_(&bin)
{
}

template<class T>
recycled_ptr<T>::
recycled_ptr()
    : recycled_ptr(nullptr)
{
    p_ = bin_->acquire();
}

template<class T>
recycled_ptr<T>::
recycled_ptr(
    std::nullptr_t) noexcept
    : recycled_ptr([]() -> B&
        {
            // VFALCO need guaranteed constexpr-init
            static B r;
            return r;
        }(), nullptr)
{
}

template<class T>
recycled_ptr<T>::
recycled_ptr(
    recycled_ptr const& other) noexcept
    : bin_(other.bin_)
    , p_(other.p_)
{
    if(p_)
        ++p_->refs;
}

template<class T>
recycled_ptr<T>::
recycled_ptr(
    recycled_ptr&& other) noexcept
    : bin_(other.bin_)
    , p_(other.p_)
{
    other.p_ = nullptr;
}

template<class T>
auto
recycled_ptr<T>::
operator=(
    recycled_ptr&& other) noexcept ->
        recycled_ptr&
{
    BOOST_ASSERT(
        bin_ == other.bin_);
    if(p_)
        bin_->release(p_);
    p_ = other.p_;
    other.p_ = nullptr;
    return *this;
}

template<class T>
auto
recycled_ptr<T>::
operator=(
    recycled_ptr const& other) noexcept ->
        recycled_ptr&
{
    BOOST_ASSERT(
        bin_ == other.bin_);
    if(p_)
        bin_->release(p_);
    p_ = other.p_;
    if(p_)
        ++p_->refs;
    return *this;
}

template<class T>
T&
recycled_ptr<T>::
acquire()
{
    if(! p_)
        p_ = bin_->acquire();
    return p_->t;
}

template<class T>
void
recycled_ptr<T>::
release() noexcept
{
    if(p_)
    {
        bin_->release(p_);
        p_ = nullptr;
    }
}

} // grammar
} // urls
} // boost

#endif
