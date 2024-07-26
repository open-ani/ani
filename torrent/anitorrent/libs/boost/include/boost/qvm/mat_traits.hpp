#ifndef BOOST_QVM_TRAITS_HPP_INCLUDED
#define BOOST_QVM_TRAITS_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/qvm/is_scalar.hpp>
#include <boost/qvm/enable_if.hpp>
#include <boost/qvm/config.hpp>

namespace boost { namespace qvm {

template <class M>
struct
mat_traits
    {
    static int const rows=0;
    static int const cols=0;
    typedef void scalar_type;
    };

template <class T>
struct
is_mat
    {
    static bool const value = is_scalar<typename mat_traits<T>::scalar_type>::value && mat_traits<T>::rows>0 && mat_traits<T>::cols>0;
    };

namespace
qvm_detail
    {
    template <class T, T>
    struct
    mtr_dispatch_yes
        {
        char x, y;
        };
    }

template <class T>
class
mat_write_element_ref
    {
    template <class U>
    static qvm_detail::mtr_dispatch_yes<typename mat_traits<U>::scalar_type & (*)( U & ), &mat_traits<U>::template write_element<0,0> > check(int);

    template <class>
    static char check(long);

    public:

    static bool const value = sizeof(check<T>(0)) > 1;
    };

template <int R, int C, class M>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
typename enable_if_c<
    mat_write_element_ref<M>::value,
    void>::type
write_mat_element( M & m, typename mat_traits<M>::scalar_type s )
    {
    mat_traits<M>::template write_element<R,C>(m) = s;
    }

template <int R, int C, class M>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
typename enable_if_c<
    !mat_write_element_ref<M>::value,
    void>::type
write_mat_element( M & m, typename mat_traits<M>::scalar_type s )
    {
    mat_traits<M>::template write_element<R,C>(m, s);
    }

template <class M>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
typename enable_if_c<
    mat_write_element_ref<M>::value,
    void>::type
write_mat_element_idx( int r, int c, M & m, typename mat_traits<M>::scalar_type s )
    {
    mat_traits<M>::write_element_idx(r, c, m) = s;
    }

template <class M>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
typename enable_if_c<
    !mat_write_element_ref<M>::value,
    void>::type
write_mat_element_idx( int r, int c, M & m, typename mat_traits<M>::scalar_type s )
    {
    mat_traits<M>::write_element_idx(r, c, m, s);
    }

} }

#endif
