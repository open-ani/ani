#ifndef BOOST_QVM_IS_SCALAR_HPP_INCLUDED
#define BOOST_QVM_IS_SCALAR_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

namespace boost { namespace qvm {

template <class T>
struct
is_scalar
    {
    static bool const value=false;
    };
template <class T>
struct
is_scalar<T const>:
    is_scalar<T>
    {
    };
template <> struct is_scalar<signed char> { static bool const value=true; };
template <> struct is_scalar<unsigned char> { static bool const value=true; };
template <> struct is_scalar<signed short> { static bool const value=true; };
template <> struct is_scalar<unsigned short> { static bool const value=true; };
template <> struct is_scalar<signed int> { static bool const value=true; };
template <> struct is_scalar<unsigned int> { static bool const value=true; };
template <> struct is_scalar<signed long> { static bool const value=true; };
template <> struct is_scalar<unsigned long> { static bool const value=true; };
template <> struct is_scalar<signed long long> { static bool const value=true; };
template <> struct is_scalar<unsigned long long> { static bool const value=true; };
template <> struct is_scalar<float> { static bool const value=true; };
template <> struct is_scalar<double> { static bool const value=true; };
template <> struct is_scalar<long double> { static bool const value=true; };
} }

#endif
