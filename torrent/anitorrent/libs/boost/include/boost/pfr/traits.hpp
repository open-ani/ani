// Copyright (c) 2022 Denis Mikhailov
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_PFR_TRAITS_HPP
#define BOOST_PFR_TRAITS_HPP
#pragma once

#include <boost/pfr/detail/config.hpp>

#include <boost/pfr/detail/possible_reflectable.hpp>
#include <type_traits>

/// \file boost/pfr/traits.hpp
/// Contains traits \forcedlink{is_reflectable} and \forcedlink{is_implicitly_reflectable} for detecting an ability to reflect type.
///
/// \b Synopsis:

namespace boost { namespace pfr {

/// Has a static const member variable `value` when it is known that type T can or can't be reflected using Boost.PFR; otherwise, there is no member variable.
/// Every user may (and in some difficult cases - should) specialize is_reflectable on his own.
///
/// \b Example:
/// \code
///     namespace boost { namespace pfr {
///         template<class All> struct is_reflectable<A, All> : std::false_type {};       // 'A' won't be interpreted as reflectable everywhere
///         template<> struct is_reflectable<B, boost_fusion_tag> : std::false_type {};   // 'B' won't be interpreted as reflectable in only Boost Fusion
///     }}
/// \endcode
/// \note is_reflectable affects is_implicitly_reflectable, the decision made by is_reflectable is used by is_implicitly_reflectable.
template<class T, class WhatFor>
struct is_reflectable { /*  does not have 'value' because value is unknown */ };

// these specs can't be inherited from 'std::integral_constant< bool, boost::pfr::is_reflectable<T, WhatFor>::value >',
// because it will break the sfinae-friendliness
template<class T, class WhatFor>
struct is_reflectable<const T, WhatFor> : boost::pfr::is_reflectable<T, WhatFor> {};

template<class T, class WhatFor>
struct is_reflectable<volatile T, WhatFor> : boost::pfr::is_reflectable<T, WhatFor> {};

template<class T, class WhatFor>
struct is_reflectable<const volatile T, WhatFor> : boost::pfr::is_reflectable<T, WhatFor> {};

/// Checks the input type for the potential to be reflected.
/// Specialize is_reflectable if you disagree with is_implicitly_reflectable's default decision.
template<class T, class WhatFor>
using is_implicitly_reflectable = std::integral_constant< bool, boost::pfr::detail::possible_reflectable<T, WhatFor>(1L) >;

/// Checks the input type for the potential to be reflected.
/// Specialize is_reflectable if you disagree with is_implicitly_reflectable_v's default decision.
template<class T, class WhatFor>
constexpr bool is_implicitly_reflectable_v = is_implicitly_reflectable<T, WhatFor>::value;

}} // namespace boost::pfr

#endif // BOOST_PFR_TRAITS_HPP

