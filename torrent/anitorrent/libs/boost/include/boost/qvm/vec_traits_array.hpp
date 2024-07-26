#ifndef BOOST_QVM_VEC_TRAITS_ARRAY_HPP_INCLUDED
#define BOOST_QVM_VEC_TRAITS_ARRAY_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/qvm/config.hpp>
#include <boost/qvm/deduce_vec.hpp>
#include <boost/qvm/assert.hpp>

#if __cplusplus > 199711L

#include <array>

namespace boost { namespace qvm {

template <class T,std::size_t M,std::size_t N>
struct
vec_traits<std::array<std::array<T,M>,N> >
    {
    static int const dim=0;
    typedef void scalar_type;
    };

template <class T,std::size_t Dim>
struct
vec_traits<std::array<T, Dim> >
    {
    typedef std::array<T, Dim> this_vector;
    typedef T scalar_type;
    static int const dim=int(Dim);

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element( this_vector const & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<int(Dim));
        return x[I];
        }

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type &
    write_element( this_vector & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<int(Dim));
        return x[I];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element_idx( int i, this_vector const & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<int(Dim));
        return x[i];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type &
    write_element_idx( int i, this_vector & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<int(Dim));
        return x[i];
        }
    };

template <class T,std::size_t Dim>
struct
vec_traits<std::array<T, Dim> const>
    {
    typedef std::array<T, Dim> const this_vector;
    typedef T scalar_type;
    static int const dim=int(Dim);

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element( this_vector & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<int(Dim));
        return x[I];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element_idx( int i, this_vector & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<int(Dim));
        return x[i];
        }
    };

template <class T,std::size_t Dim,int D>
struct
deduce_vec<std::array<T,Dim>,D>
    {
    typedef vec<T,D> type;
    };

template <class T,std::size_t Dim,int D>
struct
deduce_vec<std::array<T,Dim> const,D>
    {
    typedef vec<T,D> type;
    };

template <class T1,class T2,std::size_t Dim,int D>
struct
deduce_vec2<std::array<T1,Dim>,std::array<T2,Dim>,D>
    {
    typedef vec<typename deduce_scalar<T1,T2>::type,D> type;
    };

template <class T1,class T2,std::size_t Dim,int D>
struct
deduce_vec2<std::array<T1,Dim> const,std::array<T2,Dim>,D>
    {
    typedef vec<typename deduce_scalar<T1,T2>::type,D> type;
    };

template <class T1,class T2,std::size_t Dim,int D>
struct
deduce_vec2<std::array<T1,Dim>,std::array<T2,Dim> const,D>
    {
    typedef vec<typename deduce_scalar<T1,T2>::type,D> type;
    };

template <class T1,class T2,std::size_t Dim,int D>
struct
deduce_vec2<std::array<T1,Dim> const,std::array<T2,Dim> const,D>
    {
    typedef vec<typename deduce_scalar<T1,T2>::type,D> type;
    };

} }

#endif

namespace boost { namespace qvm {

template <class T,int M,int N>
struct
vec_traits<T[M][N]>
    {
    static int const dim=0;
    typedef void scalar_type;
    };

template <class T,int Dim>
struct
vec_traits<T[Dim]>
    {
    typedef T this_vector[Dim];
    typedef T scalar_type;
    static int const dim=Dim;

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element( this_vector const & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<Dim);
        return x[I];
        }

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type &
    write_element( this_vector & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<Dim);
        return x[I];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element_idx( int i, this_vector const & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<Dim);
        return x[i];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type &
    write_element_idx( int i, this_vector & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<Dim);
        return x[i];
        }
    };

template <class T,int Dim,int D>
struct
deduce_vec<T[Dim],D>
    {
    typedef vec<T,D> type;
    };

template <class T,int Dim,int D>
struct
deduce_vec<T const[Dim],D>
    {
    typedef vec<T,D> type;
    };

template <class T1,class T2,int Dim,int D>
struct
deduce_vec2<T1[Dim],T2[Dim],D>
    {
    typedef vec<typename deduce_scalar<T1,T2>::type,D> type;
    };

template <int Dim,class T>
T (&ptr_vref( T * ptr ))[Dim]
    {
    return *reinterpret_cast<T (*)[Dim]>(ptr);
    }

} }

#endif
