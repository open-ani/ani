#ifndef BOOST_QVM_DETAIL_MAT_ASSIGN_HPP_INCLUDED
#define BOOST_QVM_DETAIL_MAT_ASSIGN_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/qvm/gen/mat_assign2.hpp>
#include <boost/qvm/gen/mat_assign3.hpp>
#include <boost/qvm/gen/mat_assign4.hpp>

namespace boost { namespace qvm {

namespace
qvm_detail
    {
    template <int M,int N>
    struct
    assign_mm_defined
        {
        static bool const value=false;
        };

    template <int I,int N>
    struct
    copy_matrix_elements
        {
        template <class A,class B>
        static
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        typename enable_if_c<
            mat_write_element_ref<A>::value,
            void>::type
        f( A & a, B const & b )
            {
            mat_traits<A>::template write_element<I/mat_traits<A>::cols,I%mat_traits<A>::cols>(a) =
                mat_traits<B>::template read_element<I/mat_traits<B>::cols,I%mat_traits<B>::cols>(b);
            copy_matrix_elements<I+1,N>::f(a,b);
            }

        template <class A,class B>
        static
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        typename enable_if_c<
            !mat_write_element_ref<A>::value,
            void>::type
        f( A & a, B const & b )
            {
            mat_traits<A>::template write_element<I/mat_traits<A>::cols,I%mat_traits<A>::cols>(a,
                mat_traits<B>::template read_element<I/mat_traits<B>::cols,I%mat_traits<B>::cols>(b));
            copy_matrix_elements<I+1,N>::f(a,b);
            }
        };

    template <int N>
    struct
    copy_matrix_elements<N,N>
        {
        template <class A,class B>
        static
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        void
        f( A &, B const & )
            {
            }
        };
    }

template <class A,class B>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL
typename enable_if_c<
    is_mat<A>::value && is_mat<B>::value &&
    mat_traits<A>::rows==mat_traits<B>::rows &&
    mat_traits<A>::cols==mat_traits<B>::cols &&
    !qvm_detail::assign_mm_defined<mat_traits<A>::rows,mat_traits<A>::cols>::value,
    A &>::type
assign( A & a, B const & b )
    {
    qvm_detail::copy_matrix_elements<0,mat_traits<A>::rows*mat_traits<A>::cols>::f(a,b);
    return a;
    }

} }

#endif
