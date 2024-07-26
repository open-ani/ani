#ifndef BOOST_QVM_SCALAR_TRAITS_HPP_INCLUDED
#define BOOST_QVM_SCALAR_TRAITS_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/qvm/quat_traits.hpp>
#include <boost/qvm/vec_traits.hpp>
#include <boost/qvm/mat_traits.hpp>
#include <boost/qvm/config.hpp>

namespace boost { namespace qvm {

template <class Scalar>
struct
scalar_traits
    {
    static
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    Scalar
    value( int v )
        {
        return Scalar(v);
        }
    };

namespace
qvm_detail
    {
    template <class A,
        bool IsQ=is_quat<A>::value,
        bool IsV=is_vec<A>::value,
        bool IsM=is_mat<A>::value,
        bool IsS=is_scalar<A>::value>
    struct
    scalar_impl
        {
        typedef void type;
        };

    template <class A>
    struct
    scalar_impl<A,false,false,false,true>
        {
        typedef A type;
        };

    template <class A>
    struct
    scalar_impl<A,false,false,true,false>
        {
        typedef typename mat_traits<A>::scalar_type type;
        };

    template <class A>
    struct
    scalar_impl<A,false,true,false,false>
        {
        typedef typename vec_traits<A>::scalar_type type;
        };

    template <class A>
    struct
    scalar_impl<A,true,false,false,false>
        {
        typedef typename quat_traits<A>::scalar_type type;
        };
    }

template <class A>
struct
scalar
    {
    typedef typename qvm_detail::scalar_impl<A>::type type;
    };

} }

#endif
