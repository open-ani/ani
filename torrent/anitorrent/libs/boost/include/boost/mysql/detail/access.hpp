//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_ACCESS_HPP
#define BOOST_MYSQL_DETAIL_ACCESS_HPP

#include <utility>

namespace boost {
namespace mysql {
namespace detail {

// Exposes access to the implementation of public access, which is sometimes
// required by library internals.
struct access
{
    template <class T>
    static decltype(std::declval<T>().impl_)& get_impl(T& obj) noexcept
    {
        return obj.impl_;
    }

    template <class T>
    static const decltype(std::declval<T>().impl_)& get_impl(const T& obj) noexcept
    {
        return obj.impl_;
    }

    template <class T, class... Args>
    static T construct(Args&&... args)
    {
        return T(std::forward<Args>(args)...);
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
