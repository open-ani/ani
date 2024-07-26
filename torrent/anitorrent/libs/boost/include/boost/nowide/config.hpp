//
// Copyright (c) 2012 Artyom Beilis (Tonkikh)
// Copyright (c) 2019 - 2022 Alexander Grund
//
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_NOWIDE_CONFIG_HPP_INCLUDED
#define BOOST_NOWIDE_CONFIG_HPP_INCLUDED

/// @file

#include <boost/config.hpp>
#include <boost/nowide/replacement.hpp>
#include <boost/version.hpp>

//! @cond Doxygen_Suppress

#if defined(BOOST_ALL_DYN_LINK) || defined(BOOST_NOWIDE_DYN_LINK)
#ifdef BOOST_NOWIDE_SOURCE
#define BOOST_NOWIDE_DECL BOOST_SYMBOL_EXPORT
#else
#define BOOST_NOWIDE_DECL BOOST_SYMBOL_IMPORT
#endif // BOOST_NOWIDE_SOURCE
#else
#define BOOST_NOWIDE_DECL
#endif // BOOST_NOWIDE_DYN_LINK

// Automatically link to the correct build variant where possible.
#if !defined(BOOST_ALL_NO_LIB) && !defined(BOOST_NOWIDE_NO_LIB) && !defined(BOOST_NOWIDE_SOURCE)
//
// Set the name of our library, this will get undef'ed by auto_link.hpp
// once it's done with it:
//
#define BOOST_LIB_NAME boost_nowide
//
// If we're importing code from a dll, then tell auto_link.hpp about it:
//
#if defined(BOOST_ALL_DYN_LINK) || defined(BOOST_NOWIDE_DYN_LINK)
#define BOOST_DYN_LINK
#endif
//
// And include the header that does the work:
//
#include <boost/config/auto_link.hpp>
#endif // auto-linking disabled

//! @endcond

/// @def BOOST_NOWIDE_USE_WCHAR_OVERLOADS
/// @brief Whether to use the wchar_t* overloads in fstream-classes.
///
/// Enabled by default on Windows and Cygwin as the latter may use wchar_t in filesystem::path.
#ifndef BOOST_NOWIDE_USE_WCHAR_OVERLOADS
#if defined(BOOST_WINDOWS) || defined(__CYGWIN__) || defined(BOOST_NOWIDE_DOXYGEN)
#define BOOST_NOWIDE_USE_WCHAR_OVERLOADS 1
#else
#define BOOST_NOWIDE_USE_WCHAR_OVERLOADS 0
#endif
#endif

/// @def BOOST_NOWIDE_USE_FILEBUF_REPLACEMENT
/// @brief Define to 1 to use the class from <filebuf.hpp> that is used on Windows.
///
/// - On Windows: No effect, always overwritten to 1
/// - Others (including Cygwin): Defaults to the value of #BOOST_NOWIDE_USE_WCHAR_OVERLOADS if not set.
///
/// When set to 0 boost::nowide::basic_filebuf will be an alias for std::basic_filebuf.
///
/// Affects boost::nowide::basic_filebuf,
/// boost::nowide::basic_ofstream, boost::nowide::basic_ifstream, boost::nowide::basic_fstream
#if defined(BOOST_WINDOWS) || defined(BOOST_NOWIDE_DOXYGEN)
#ifdef BOOST_NOWIDE_USE_FILEBUF_REPLACEMENT
#undef BOOST_NOWIDE_USE_FILEBUF_REPLACEMENT
#endif
#define BOOST_NOWIDE_USE_FILEBUF_REPLACEMENT 1
#elif !defined(BOOST_NOWIDE_USE_FILEBUF_REPLACEMENT)
#define BOOST_NOWIDE_USE_FILEBUF_REPLACEMENT BOOST_NOWIDE_USE_WCHAR_OVERLOADS
#endif

//! @cond Doxygen_Suppress

#if BOOST_VERSION < 106500 && defined(__GNUC__) && __GNUC__ >= 7
#define BOOST_NOWIDE_FALLTHROUGH __attribute__((fallthrough))
#else
#define BOOST_NOWIDE_FALLTHROUGH BOOST_FALLTHROUGH
#endif

// The std::codecvt<char16/32_t, char, std::mbstate_t> are deprecated in C++20
// These macros can suppress this warning
#if defined(_MSC_VER)
#define BOOST_NOWIDE_SUPPRESS_UTF_CODECVT_DEPRECATION_BEGIN __pragma(warning(push)) __pragma(warning(disable : 4996))
#define BOOST_NOWIDE_SUPPRESS_UTF_CODECVT_DEPRECATION_END __pragma(warning(pop))
#elif(__cplusplus >= 202002L) && defined(__clang__)
#define BOOST_NOWIDE_SUPPRESS_UTF_CODECVT_DEPRECATION_BEGIN \
    _Pragma("clang diagnostic push") _Pragma("clang diagnostic ignored \"-Wdeprecated-declarations\"")
#define BOOST_NOWIDE_SUPPRESS_UTF_CODECVT_DEPRECATION_END _Pragma("clang diagnostic pop")
#elif(__cplusplus >= 202002L) && defined(__GNUC__)
#define BOOST_NOWIDE_SUPPRESS_UTF_CODECVT_DEPRECATION_BEGIN \
    _Pragma("GCC diagnostic push") _Pragma("GCC diagnostic ignored \"-Wdeprecated-declarations\"")
#define BOOST_NOWIDE_SUPPRESS_UTF_CODECVT_DEPRECATION_END _Pragma("GCC diagnostic pop")
#else
#define BOOST_NOWIDE_SUPPRESS_UTF_CODECVT_DEPRECATION_BEGIN
#define BOOST_NOWIDE_SUPPRESS_UTF_CODECVT_DEPRECATION_END
#endif

//! @endcond

namespace boost {
///
/// \brief This namespace includes implementations of the standard library functions and
/// classes such that they accept UTF-8 strings on Windows.
/// On other platforms (i.e. not on Windows) those functions and classes are just aliases
/// of the corresponding ones from the std namespace or behave like them.
///
namespace nowide {}
} // namespace boost

#endif
