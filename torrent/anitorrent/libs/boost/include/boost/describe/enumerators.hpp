#ifndef BOOST_DESCRIBE_ENUMERATORS_HPP_INCLUDED
#define BOOST_DESCRIBE_ENUMERATORS_HPP_INCLUDED

// Copyright 2020, 2021 Peter Dimov
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#include <boost/describe/detail/void_t.hpp>
#include <boost/describe/detail/config.hpp>

#if defined(BOOST_DESCRIBE_CXX11)

#include <type_traits>

namespace boost
{
namespace describe
{

// describe_enumerators<E>

template<class E> using describe_enumerators = decltype( boost_enum_descriptor_fn( static_cast<E**>(0) ) );

// has_describe_enumerators<E>

namespace detail
{

template<class E, class En = void> struct has_describe_enumerators: std::false_type
{
};

template<class E> struct has_describe_enumerators<E, void_t<describe_enumerators<E>>>: std::true_type
{
};

} // namespace detail

template<class E> using has_describe_enumerators = detail::has_describe_enumerators<E>;

} // namespace describe
} // namespace boost

#endif // defined(BOOST_DESCRIBE_CXX11)

#endif // #ifndef BOOST_DESCRIBE_ENUMERATORS_HPP_INCLUDED
