// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_DETAIL_TYPE_TRAITS_HPP
#define BOOST_CHARCONV_DETAIL_TYPE_TRAITS_HPP

#include <boost/charconv/detail/config.hpp>
#include <type_traits>

namespace boost { namespace charconv { namespace detail {

template <typename T>
struct is_signed { static constexpr bool value = std::is_signed<T>::value; };

#ifdef BOOST_CHARCONV_HAS_INT128

template <>
struct is_signed<boost::int128_type> { static constexpr bool value = true; };

template <>
struct is_signed<boost::uint128_type> { static constexpr bool value = false; };

#endif

#if defined(BOOST_NO_CXX17_INLINE_VARIABLES) && (!defined(BOOST_MSVC) || BOOST_MSVC != 1900)

template <typename T>
constexpr bool is_signed<T>::value;

#endif

template <typename T>
struct make_unsigned { using type = typename std::make_unsigned<T>::type; };

template <>
struct make_unsigned<uint128> { using type = uint128; };

#ifdef BOOST_CHARCONV_HAS_INT128

template <>
struct make_unsigned<boost::int128_type> { using type = boost::uint128_type; };

template <>
struct make_unsigned<boost::uint128_type> { using type = boost::uint128_type; };

#endif

template <typename T>
using make_unsigned_t = typename make_unsigned<T>::type;

template <typename T>
struct make_signed { using type = typename std::make_signed<T>::type; };

#ifdef BOOST_CHARCONV_HAS_INT128

template <>
struct make_signed<boost::int128_type> { using type = boost::int128_type; };

template <>
struct make_signed<boost::uint128_type> { using type = boost::int128_type; };

#endif

}}} // Namespaces

#endif //BOOST_CHARCONV_DETAIL_TYPE_TRAITS_HPP
