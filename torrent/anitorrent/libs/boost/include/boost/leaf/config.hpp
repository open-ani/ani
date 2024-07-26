#ifndef BOOST_LEAF_CONFIG_HPP_INCLUDED
#define BOOST_LEAF_CONFIG_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

// The following is based in part on Boost Config.

// (C) Copyright John Maddock 2001 - 2003.
// (C) Copyright Martin Wille 2003.
// (C) Copyright Guillaume Melquiond 2003.

#ifndef BOOST_LEAF_ASSERT
#   include <cassert>
#   define BOOST_LEAF_ASSERT assert
#endif

////////////////////////////////////////

#ifdef BOOST_LEAF_DIAGNOSTICS
#   warning BOOST_LEAF_DIAGNOSTICS has been renamed to BOOST_LEAF_CFG_DIAGNOSTICS.
#   define BOOST_LEAF_CFG_DIAGNOSTICS BOOST_LEAF_DIAGNOSTICS
#endif

////////////////////////////////////////

#ifdef BOOST_LEAF_TLS_FREERTOS

#   ifndef BOOST_LEAF_EMBEDDED
#       define BOOST_LEAF_EMBEDDED
#   endif

#endif

////////////////////////////////////////

#ifdef BOOST_LEAF_EMBEDDED

#   ifndef BOOST_LEAF_CFG_DIAGNOSTICS
#       define BOOST_LEAF_CFG_DIAGNOSTICS 0
#   endif

#   ifndef BOOST_LEAF_CFG_STD_SYSTEM_ERROR
#       define BOOST_LEAF_CFG_STD_SYSTEM_ERROR 0
#   endif

#   ifndef BOOST_LEAF_CFG_STD_STRING
#       define BOOST_LEAF_CFG_STD_STRING 0
#   endif

#   ifndef BOOST_LEAF_CFG_CAPTURE
#       define BOOST_LEAF_CFG_CAPTURE 0
#   endif

#endif

////////////////////////////////////////

#ifndef BOOST_LEAF_CFG_DIAGNOSTICS
#   define BOOST_LEAF_CFG_DIAGNOSTICS 1
#endif

#ifndef BOOST_LEAF_CFG_STD_SYSTEM_ERROR
#   define BOOST_LEAF_CFG_STD_SYSTEM_ERROR 1
#endif

#ifndef BOOST_LEAF_CFG_STD_STRING
#   define BOOST_LEAF_CFG_STD_STRING 1
#endif

#ifndef BOOST_LEAF_CFG_CAPTURE
#   define BOOST_LEAF_CFG_CAPTURE 1
#endif

#ifndef BOOST_LEAF_CFG_WIN32
#   define BOOST_LEAF_CFG_WIN32 0
#endif

#ifndef BOOST_LEAF_CFG_GNUC_STMTEXPR
#   ifdef __GNUC__
#   	define BOOST_LEAF_CFG_GNUC_STMTEXPR 1
#   else
#   	define BOOST_LEAF_CFG_GNUC_STMTEXPR 0
#   endif
#endif

#if BOOST_LEAF_CFG_DIAGNOSTICS!=0 && BOOST_LEAF_CFG_DIAGNOSTICS!=1
#   error BOOST_LEAF_CFG_DIAGNOSTICS must be 0 or 1.
#endif

#if BOOST_LEAF_CFG_STD_SYSTEM_ERROR!=0 && BOOST_LEAF_CFG_STD_SYSTEM_ERROR!=1
#   error BOOST_LEAF_CFG_STD_SYSTEM_ERROR must be 0 or 1.
#endif

#if BOOST_LEAF_CFG_STD_STRING!=0 && BOOST_LEAF_CFG_STD_STRING!=1
#   error BOOST_LEAF_CFG_STD_STRING must be 0 or 1.
#endif

#if BOOST_LEAF_CFG_CAPTURE!=0 && BOOST_LEAF_CFG_CAPTURE!=1
#   error BOOST_LEAF_CFG_CAPTURE must be 0 or 1.
#endif

#if BOOST_LEAF_CFG_WIN32!=0 && BOOST_LEAF_CFG_WIN32!=1
#   error BOOST_LEAF_CFG_WIN32 must be 0 or 1.
#endif

#if BOOST_LEAF_CFG_GNUC_STMTEXPR!=0 && BOOST_LEAF_CFG_GNUC_STMTEXPR!=1
#   error BOOST_LEAF_CFG_GNUC_STMTEXPR must be 0 or 1.
#endif

#if BOOST_LEAF_CFG_DIAGNOSTICS && !BOOST_LEAF_CFG_STD_STRING
#   error BOOST_LEAF_CFG_DIAGNOSTICS requires BOOST_LEAF_CFG_STD_STRING, which has been disabled.
#endif

#if BOOST_LEAF_CFG_STD_SYSTEM_ERROR && !BOOST_LEAF_CFG_STD_STRING
#   error BOOST_LEAF_CFG_STD_SYSTEM_ERROR requires BOOST_LEAF_CFG_STD_STRING, which has been disabled.
#endif

////////////////////////////////////////

// Configure BOOST_LEAF_NO_EXCEPTIONS, unless already #defined
#ifndef BOOST_LEAF_NO_EXCEPTIONS

#   if defined(__clang__) && !defined(__ibmxl__)
//  Clang C++ emulates GCC, so it has to appear early.

#       if !__has_feature(cxx_exceptions)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(__DMC__)
//  Digital Mars C++

#       if !defined(_CPPUNWIND)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(__GNUC__) && !defined(__ibmxl__)
//  GNU C++:

#       if !defined(__EXCEPTIONS)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(__KCC)
//  Kai C++

#       if !defined(_EXCEPTIONS)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(__CODEGEARC__)
//  CodeGear - must be checked for before Borland

#       if !defined(_CPPUNWIND) && !defined(__EXCEPTIONS)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(__BORLANDC__)
//  Borland

#       if !defined(_CPPUNWIND) && !defined(__EXCEPTIONS)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(__MWERKS__)
//  Metrowerks CodeWarrior

#       if !__option(exceptions)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(__IBMCPP__) && defined(__COMPILER_VER__) && defined(__MVS__)
//  IBM z/OS XL C/C++

#       if !defined(_CPPUNWIND) && !defined(__EXCEPTIONS)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(__ibmxl__)
//  IBM XL C/C++ for Linux (Little Endian)

#       if !__has_feature(cxx_exceptions)
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif

#   elif defined(_MSC_VER)
//  Microsoft Visual C++
//
//  Must remain the last #elif since some other vendors (Metrowerks, for
//  example) also #define _MSC_VER

#       if !_CPPUNWIND
#           define BOOST_LEAF_NO_EXCEPTIONS
#       endif
#   endif

#endif

////////////////////////////////////////

#ifdef _MSC_VER
#   define BOOST_LEAF_ALWAYS_INLINE __forceinline
#else
#   define BOOST_LEAF_ALWAYS_INLINE __attribute__((always_inline)) inline
#endif

////////////////////////////////////////

#if defined(__has_attribute) && defined(__SUNPRO_CC) && (__SUNPRO_CC > 0x5130)
#   if __has_attribute(nodiscard)
#       define BOOST_LEAF_ATTRIBUTE_NODISCARD [[nodiscard]]
#   endif
#elif defined(__has_cpp_attribute)
    //clang-6 accepts [[nodiscard]] with -std=c++14, but warns about it -pedantic
#   if __has_cpp_attribute(nodiscard) && !(defined(__clang__) && (__cplusplus < 201703L)) && !(defined(__GNUC__) && (__cplusplus < 201100))
#       define BOOST_LEAF_ATTRIBUTE_NODISCARD [[nodiscard]]
#   endif
#endif
#ifndef BOOST_LEAF_ATTRIBUTE_NODISCARD
#   define BOOST_LEAF_ATTRIBUTE_NODISCARD
#endif

////////////////////////////////////////

#ifndef BOOST_LEAF_CONSTEXPR
#   if __cplusplus > 201402L
#       define BOOST_LEAF_CONSTEXPR constexpr
#   else
#       define BOOST_LEAF_CONSTEXPR
#   endif
#endif

////////////////////////////////////////

#ifndef BOOST_LEAF_DEPRECATED
#   if __cplusplus > 201402L
#       define BOOST_LEAF_DEPRECATED(msg) [[deprecated(msg)]]
#   else
#       define BOOST_LEAF_DEPRECATED(msg)
#   endif
#endif

////////////////////////////////////////

#ifndef BOOST_LEAF_NO_EXCEPTIONS
#   include <exception>
#   if (defined(__cpp_lib_uncaught_exceptions) && __cpp_lib_uncaught_exceptions >= 201411L) || (defined(_MSC_VER) && _MSC_VER >= 1900)
#       define BOOST_LEAF_STD_UNCAUGHT_EXCEPTIONS 1
#   else
#       define BOOST_LEAF_STD_UNCAUGHT_EXCEPTIONS 0
#   endif
#endif

////////////////////////////////////////

#ifdef __GNUC__
#   define BOOST_LEAF_SYMBOL_VISIBLE __attribute__((__visibility__("default")))
#else
#   define BOOST_LEAF_SYMBOL_VISIBLE
#endif

////////////////////////////////////////

#if defined(__GNUC__) && !(defined(__clang__) || defined(__INTEL_COMPILER) || defined(__ICL) || defined(__ICC) || defined(__ECC)) && (__GNUC__ * 100 + __GNUC_MINOR__) < 409
#   ifndef BOOST_LEAF_NO_CXX11_REF_QUALIFIERS
#       define BOOST_LEAF_NO_CXX11_REF_QUALIFIERS
#   endif
#endif

////////////////////////////////////////

// Configure TLS access
#include <boost/leaf/config/tls.hpp>

////////////////////////////////////////

#endif
