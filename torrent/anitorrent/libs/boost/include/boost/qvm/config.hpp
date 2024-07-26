// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_QVM_CONFIG_HPP_INCLUDED
#define BOOST_QVM_CONFIG_HPP_INCLUDED

#if defined( BOOST_STRICT_CONFIG ) || defined( BOOST_QVM_NO_WORKAROUNDS )
#   define BOOST_QVM_WORKAROUND( symbol, test ) 0
#else
#   define BOOST_QVM_WORKAROUND( symbol, test ) ((symbol) != 0 && ((symbol) test))
#endif

#define BOOST_QVM_CLANG 0
#if defined(__clang__)
#   undef BOOST_QVM_CLANG
#   define BOOST_QVM_CLANG (__clang_major__ * 100 + __clang_minor__)
#endif

#if BOOST_QVM_WORKAROUND( BOOST_QVM_CLANG, < 304 )
#   define BOOST_QVM_DEPRECATED(msg)
#elif defined(__GNUC__) || defined(__clang__)
#   define BOOST_QVM_DEPRECATED(msg) __attribute__((deprecated(msg)))
#elif defined(_MSC_VER) && _MSC_VER >= 1900
#   define BOOST_QVM_DEPRECATED(msg) [[deprecated(msg)]]
#else
#   define BOOST_QVM_DEPRECATED(msg)
#endif

#ifndef BOOST_QVM_FORCEINLINE
#   if defined(_MSC_VER)
#       define BOOST_QVM_FORCEINLINE __forceinline
#   elif defined(__GNUC__) && __GNUC__>3
#       define BOOST_QVM_FORCEINLINE inline __attribute__ ((always_inline))
#   else
#       define BOOST_QVM_FORCEINLINE inline
#   endif
#endif

#ifndef BOOST_QVM_INLINE
#   define BOOST_QVM_INLINE inline
#endif

#ifndef BOOST_QVM_INLINE_TRIVIAL
#   define BOOST_QVM_INLINE_TRIVIAL BOOST_QVM_FORCEINLINE
#endif

#ifndef BOOST_QVM_INLINE_CRITICAL
#   define BOOST_QVM_INLINE_CRITICAL BOOST_QVM_FORCEINLINE
#endif

#ifndef BOOST_QVM_INLINE_OPERATIONS
#   define BOOST_QVM_INLINE_OPERATIONS BOOST_QVM_INLINE
#endif

#ifndef BOOST_QVM_INLINE_RECURSION
#   define BOOST_QVM_INLINE_RECURSION BOOST_QVM_INLINE_OPERATIONS
#endif

#ifndef BOOST_QVM_CONSTEXPR
#   if __cplusplus >= 201703L
#       define BOOST_QVM_CONSTEXPR constexpr
#   else
#       define BOOST_QVM_CONSTEXPR
#   endif
#endif

#endif
