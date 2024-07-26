//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_DETAIL_IMPL_STACK_IPP
#define BOOST_JSON_DETAIL_IMPL_STACK_IPP

#include <boost/json/detail/stack.hpp>

namespace boost {
namespace json {
namespace detail {

stack::
~stack()
{
    if(base_ != buf_)
        sp_->deallocate(
            base_, cap_);
}

stack::
stack(
    storage_ptr sp,
    unsigned char* buf,
    std::size_t buf_size) noexcept
    : sp_(std::move(sp))
    , cap_(buf_size)
    , base_(buf)
    , buf_(buf)
{
}

void
stack::
reserve(std::size_t n)
{
    if(cap_ >= n)
        return;
    auto const base = static_cast<
        unsigned char*>(sp_->allocate(n));
    if(base_)
    {
        if(size_ > 0)
            std::memcpy(base, base_, size_);
        if(base_ != buf_)
            sp_->deallocate(base_, cap_);
    }
    base_ = base;
    cap_ = n;
}

} // detail
} // namespace json
} // namespace boost

#endif
