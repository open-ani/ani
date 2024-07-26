#ifndef BOOST_LEAF_DETAIL_DEMANGLE_HPP_INCLUDED
#define BOOST_LEAF_DETAIL_DEMANGLE_HPP_INCLUDED

// Copyright 2018-2023 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

// This file is based on boost::core::demangle
//
// Copyright 2014 Peter Dimov
// Copyright 2014 Andrey Semashev
//
// Distributed under the Boost Software License, Version 1.0.
// See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt

#include <boost/leaf/config.hpp>

#include <cstring>

namespace boost { namespace leaf {

namespace leaf_detail
{
    template <int N>
    BOOST_LEAF_CONSTEXPR inline char const * check_prefix( char const * t, char const (&prefix)[N] )
    {
        return std::strncmp(t,prefix,sizeof(prefix)-1)==0 ? t+sizeof(prefix)-1 : t;
    }
}

template <class Name>
inline char const * type()
{
    using leaf_detail::check_prefix;
    char const * t =
#ifdef __FUNCSIG__
        __FUNCSIG__;
#else
        __PRETTY_FUNCTION__;
#endif
#if defined(__clang__)
    BOOST_LEAF_ASSERT(check_prefix(t,"const char *boost::leaf::type() ")==t+32);
    return t+32;
#elif defined(__GNUC__)
    BOOST_LEAF_ASSERT(check_prefix(t,"const char* boost::leaf::type() ")==t+32);
    return t+32;
#else
    char const * clang_style = check_prefix(t,"const char *boost::leaf::type() ");
    if( clang_style!=t )
        return clang_style;
    char const * gcc_style = check_prefix(t,"const char* boost::leaf::type() ");
    if( gcc_style!=t )
        return gcc_style;
#endif
    return t;
}

} }

////////////////////////////////////////

// __has_include is currently supported by GCC and Clang. However GCC 4.9 may have issues and
// returns 1 for 'defined( __has_include )', while '__has_include' is actually not supported:
// https://gcc.gnu.org/bugzilla/show_bug.cgi?id=63662
#if defined(__has_include) && (!defined(__GNUC__) || defined(__clang__) || (__GNUC__ + 0) >= 5)
#   if __has_include(<cxxabi.h>)
#       define BOOST_LEAF_HAS_CXXABI_H
#   endif
#elif defined( __GLIBCXX__ ) || defined( __GLIBCPP__ )
#   define BOOST_LEAF_HAS_CXXABI_H
#endif

#if defined( BOOST_LEAF_HAS_CXXABI_H )
#   include <cxxabi.h>
//  For some archtectures (mips, mips64, x86, x86_64) cxxabi.h in Android NDK is implemented by gabi++ library
//  (https://android.googlesource.com/platform/ndk/+/master/sources/cxx-stl/gabi++/), which does not implement
//  abi::__cxa_demangle(). We detect this implementation by checking the include guard here.
#   if defined( __GABIXX_CXXABI_H__ )
#       undef BOOST_LEAF_HAS_CXXABI_H
#   else
#       include <cstdlib>
#       include <cstddef>
#   endif
#endif

#if BOOST_LEAF_CFG_STD_STRING

#include <string>

namespace boost { namespace leaf {

namespace leaf_detail
{
    inline char const * demangle_alloc( char const * name ) noexcept;
    inline void demangle_free( char const * name ) noexcept;

    class scoped_demangled_name
    {
    private:

        char const * m_p;

    public:

        explicit scoped_demangled_name( char const * name ) noexcept :
            m_p( demangle_alloc( name ) )
        {
        }

        ~scoped_demangled_name() noexcept
        {
            demangle_free( m_p );
        }

        char const * get() const noexcept
        {
            return m_p;
        }

        scoped_demangled_name( scoped_demangled_name const& ) = delete;
        scoped_demangled_name& operator= ( scoped_demangled_name const& ) = delete;
    };

#ifdef BOOST_LEAF_HAS_CXXABI_H

    inline char const * demangle_alloc( char const * name ) noexcept
    {
        int status = 0;
        std::size_t size = 0;
        return abi::__cxa_demangle( name, NULL, &size, &status );
    }

    inline void demangle_free( char const * name ) noexcept
    {
        std::free( const_cast< char* >( name ) );
    }

    inline std::string demangle( char const * name )
    {
        scoped_demangled_name demangled_name( name );
        char const * p = demangled_name.get();
        if( !p )
            p = name;
        return p;
    }

#else

    inline char const * demangle_alloc( char const * name ) noexcept
    {
        return name;
    }

    inline void demangle_free( char const * ) noexcept
    {
    }

    inline char const * demangle( char const * name )
    {
        return name;
    }

#endif
}

} }

#else

namespace boost { namespace leaf {

namespace leaf_detail
{
    inline char const * demangle( char const * name )
    {
        return name;
    }
}

} }

#endif

#ifdef BOOST_LEAF_HAS_CXXABI_H
#   undef BOOST_LEAF_HAS_CXXABI_H
#endif

#endif
