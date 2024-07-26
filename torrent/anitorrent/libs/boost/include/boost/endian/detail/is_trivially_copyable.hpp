#ifndef BOOST_ENDIAN_DETAIL_IS_TRIVIALLY_COPYABLE_HPP_INCLUDED
#define BOOST_ENDIAN_DETAIL_IS_TRIVIALLY_COPYABLE_HPP_INCLUDED

// Copyright 2019, 2023 Peter Dimov
// Distributed under the Boost Software License, Version 1.0.
// http://www.boost.org/LICENSE_1_0.txt

#include <boost/config.hpp>
#include <type_traits>

namespace boost
{
namespace endian
{
namespace detail
{

#if defined( BOOST_LIBSTDCXX_VERSION ) && BOOST_LIBSTDCXX_VERSION < 50000

template<class T> struct is_trivially_copyable: std::integral_constant<bool,
    __has_trivial_copy(T) && __has_trivial_assign(T) && __has_trivial_destructor(T)> {};

#else

using std::is_trivially_copyable;

#endif

} // namespace detail
} // namespace endian
} // namespace boost

#endif  // BOOST_ENDIAN_DETAIL_IS_TRIVIALLY_COPYABLE_HPP_INCLUDED
