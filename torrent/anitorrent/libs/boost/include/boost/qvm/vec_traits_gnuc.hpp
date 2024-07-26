#ifndef BOOST_QVM_VEC_TRAITS_GNUC_HPP_INCLUDED
#define BOOST_QVM_VEC_TRAITS_GNUC_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#if defined(__GNUC__) && defined(__SSE2__)

#include <boost/qvm/config.hpp>
#include <boost/qvm/assert.hpp>
#include <boost/qvm/static_assert.hpp>

namespace boost { namespace qvm {

namespace
qvm_detail
    {
    template <class V, class T, int D>
    struct
    vec_traits_gnuc_impl
        {
        typedef T scalar_type;
        static int const dim=D;

        template <int I>
        static
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        scalar_type
        read_element( V const & x )
            {
            BOOST_QVM_STATIC_ASSERT(I>=0);
            BOOST_QVM_STATIC_ASSERT(I<dim);
            return x[I];
            }

        template <int I>
        static
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        void
        write_element( V & x, scalar_type s )
            {
            BOOST_QVM_STATIC_ASSERT(I>=0);
            BOOST_QVM_STATIC_ASSERT(I<dim);
            x[I] = s;
            }

        static
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        scalar_type
        read_element_idx( int i, V const & x )
            {
            BOOST_QVM_ASSERT(i>=0);
            BOOST_QVM_ASSERT(i<dim);
            return x[i];
            }

        static
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        void
        write_element_idx( int i, V & x, scalar_type s )
            {
            BOOST_QVM_ASSERT(i>=0);
            BOOST_QVM_ASSERT(i<dim);
            x[i] = s;
            }
        };
    }

template <class> struct vec_traits;
template <class> struct is_vec;

#define BOOST_QVM_GNUC_VEC_TYPE(T,D)\
    template <>\
    struct\
    vec_traits<T __attribute__((vector_size(sizeof(T)*D)))>:\
        qvm_detail::vec_traits_gnuc_impl<T __attribute__((vector_size(sizeof(T)*D))),T,D>\
        {\
        };\
    template <>\
    struct\
    is_vec<T __attribute__((vector_size(sizeof(T)*D)))>\
        {\
        enum { value = true };\
        };

BOOST_QVM_GNUC_VEC_TYPE(float,2)
BOOST_QVM_GNUC_VEC_TYPE(float,4)
BOOST_QVM_GNUC_VEC_TYPE(double,2)
BOOST_QVM_GNUC_VEC_TYPE(double,4)

#undef BOOST_QVM_GNUC_VEC_TYPE

} }

#endif

#endif
