#ifndef BOOST_ICL_DETAIL_REQUIRES_CXX11_HPP_INCLUDED
#define BOOST_ICL_DETAIL_REQUIRES_CXX11_HPP_INCLUDED

// Copyright 2023 Peter Dimov
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#include <boost/config.hpp>
#include <boost/config/pragma_message.hpp>

#if defined(BOOST_NO_CXX11_VARIADIC_TEMPLATES) || \
    defined(BOOST_NO_CXX11_RVALUE_REFERENCES) || \
    defined(BOOST_NO_CXX11_DECLTYPE) || \
    defined(BOOST_NO_CXX11_CONSTEXPR) || \
    defined(BOOST_NO_CXX11_NOEXCEPT) || \
    defined(BOOST_NO_CXX11_HDR_RATIO)

BOOST_PRAGMA_MESSAGE("C++03 support is deprecated in Boost.Icl 1.84 and will be removed in Boost.Icl 1.86.")

#endif

#endif // #ifndef BOOST_ICL_DETAIL_REQUIRES_CXX11_HPP_INCLUDED
