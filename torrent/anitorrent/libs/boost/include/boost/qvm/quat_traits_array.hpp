#ifndef BOOST_QVM_QUAT_TRAITS_ARRAY_HPP_INCLUDED
#define BOOST_QVM_QUAT_TRAITS_ARRAY_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/qvm/config.hpp>
#include <boost/qvm/deduce_quat.hpp>
#include <boost/qvm/assert.hpp>

#if __cplusplus > 199711L

#include <array>

namespace boost { namespace qvm {

template <class T,std::size_t D>
struct
quat_traits<std::array<T,D>>
    {
    typedef void scalar_type;
    };
template <class T,std::size_t D>
struct
quat_traits<std::array<std::array<T,D>,4> >
    {
    typedef void scalar_type;
    };
template <class T,std::size_t D>
struct
quat_traits<std::array<std::array<T,4>,D> >
    {
    typedef void scalar_type;
    };
template <class T>
struct
quat_traits<std::array<std::array<T,4>,4> >
    {
    typedef void scalar_type;
    };
template <class T,std::size_t M,std::size_t N>
struct
quat_traits<std::array<std::array<T,M>,N> >
    {
    typedef void scalar_type;
    };

template <class T>
struct
quat_traits<std::array<T,4> >
    {
    typedef std::array<T,4> this_quaternion;
    typedef T scalar_type;

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element( this_quaternion const & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<4);
        return x[I];
        }

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type &
    write_element( this_quaternion & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<4);
        return x[I];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element_idx( int i, this_quaternion const & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<4);
        return x[i];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type &
    write_element_idx( int i, this_quaternion & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<4);
        return x[i];
        }
    };

template <class T>
struct
quat_traits<std::array<T,4> const>
    {
    typedef std::array<T,4> const this_quaternion;
    typedef T scalar_type;

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element( this_quaternion & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<4);
        return x[I];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element_idx( int i, this_quaternion & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<4);
        return x[i];
        }
    };

template <class T>
struct
deduce_quat<std::array<T,4> >
    {
    typedef quat<T> type;
    };

template <class T>
struct
deduce_quat<std::array<T,4> const>
    {
    typedef quat<T> type;
    };

template <class T1,class T2>
struct
deduce_quat2<std::array<T1,4>,std::array<T2,4> >
    {
    typedef quat<typename deduce_scalar<T1,T2>::type> type;
    };

template <class T1,class T2>
struct
deduce_quat2<std::array<T1,4> const,std::array<T2,4> >
    {
    typedef quat<typename deduce_scalar<T1,T2>::type> type;
    };

template <class T1,class T2>
struct
deduce_quat2<std::array<T1,4>,std::array<T2,4> const>
    {
    typedef quat<typename deduce_scalar<T1,T2>::type> type;
    };

template <class T1,class T2>
struct
deduce_quat2<std::array<T1,4> const,std::array<T2,4> const>
    {
    typedef quat<typename deduce_scalar<T1,T2>::type> type;
    };

} }

#endif

namespace boost { namespace qvm {

template <class T,int D>
struct
quat_traits<T[D]>
    {
    typedef void scalar_type;
    };
template <class T,int D>
struct
quat_traits<T[D][4]>
    {
    typedef void scalar_type;
    };
template <class T,int D>
struct
quat_traits<T[4][D]>
    {
    typedef void scalar_type;
    };
template <class T>
struct
quat_traits<T[4][4]>
    {
    typedef void scalar_type;
    };
template <class T,int M,int N>
struct
quat_traits<T[M][N]>
    {
    typedef void scalar_type;
    };

template <class T>
struct
quat_traits<T[4]>
    {
    typedef T this_quaternion[4];
    typedef T scalar_type;

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element( this_quaternion const & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<4);
        return x[I];
        }

    template <int I>
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type &
    write_element( this_quaternion & x )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<4);
        return x[I];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type
    read_element_idx( int i, this_quaternion const & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<4);
        return x[i];
        }

    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    scalar_type &
    write_element_idx( int i, this_quaternion & x )
        {
        BOOST_QVM_ASSERT(i>=0);
        BOOST_QVM_ASSERT(i<4);
        return x[i];
        }
    };

template <class T>
struct
deduce_quat<T[4]>
    {
    typedef quat<T> type;
    };

template <class T>
struct
deduce_quat<T const[4]>
    {
    typedef quat<T> type;
    };

template <class T1,class T2>
struct
deduce_quat2<T1[4],T2[4]>
    {
    typedef quat<typename deduce_scalar<T1,T2>::type> type;
    };

template <class T>
T (&ptr_qref( T * ptr ))[4]
    {
    return *reinterpret_cast<T (*)[4]>(ptr);
    }

} }

#endif
