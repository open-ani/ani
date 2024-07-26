#ifndef BOOST_QVM_VEC_ACCESS_HPP_INCLUDED
#define BOOST_QVM_VEC_ACCESS_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/qvm/vec_traits.hpp>
#include <boost/qvm/config.hpp>
#include <boost/qvm/static_assert.hpp>
#include <boost/qvm/enable_if.hpp>

namespace boost { namespace qvm {

template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type X( V const & a ) { BOOST_QVM_STATIC_ASSERT(0<vec_traits<V>::dim); return vec_traits<V>::template read_element<0>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type Y( V const & a ) { BOOST_QVM_STATIC_ASSERT(1<vec_traits<V>::dim); return vec_traits<V>::template read_element<1>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type Z( V const & a ) { BOOST_QVM_STATIC_ASSERT(2<vec_traits<V>::dim); return vec_traits<V>::template read_element<2>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type W( V const & a ) { BOOST_QVM_STATIC_ASSERT(3<vec_traits<V>::dim); return vec_traits<V>::template read_element<3>(a); }

template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A0( V const & a ) { BOOST_QVM_STATIC_ASSERT(0<vec_traits<V>::dim); return vec_traits<V>::template read_element<0>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A1( V const & a ) { BOOST_QVM_STATIC_ASSERT(1<vec_traits<V>::dim); return vec_traits<V>::template read_element<1>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A2( V const & a ) { BOOST_QVM_STATIC_ASSERT(2<vec_traits<V>::dim); return vec_traits<V>::template read_element<2>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A3( V const & a ) { BOOST_QVM_STATIC_ASSERT(3<vec_traits<V>::dim); return vec_traits<V>::template read_element<3>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A4( V const & a ) { BOOST_QVM_STATIC_ASSERT(4<vec_traits<V>::dim); return vec_traits<V>::template read_element<4>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A5( V const & a ) { BOOST_QVM_STATIC_ASSERT(5<vec_traits<V>::dim); return vec_traits<V>::template read_element<5>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A6( V const & a ) { BOOST_QVM_STATIC_ASSERT(6<vec_traits<V>::dim); return vec_traits<V>::template read_element<6>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A7( V const & a ) { BOOST_QVM_STATIC_ASSERT(7<vec_traits<V>::dim); return vec_traits<V>::template read_element<7>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A8( V const & a ) { BOOST_QVM_STATIC_ASSERT(8<vec_traits<V>::dim); return vec_traits<V>::template read_element<8>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value,typename vec_traits<V>::scalar_type>::type A9( V const & a ) { BOOST_QVM_STATIC_ASSERT(9<vec_traits<V>::dim); return vec_traits<V>::template read_element<9>(a); }

namespace
qvm_detail
    {
    template <int I,class V>
    struct
    v_element_access
        {
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        void
        operator=( typename vec_traits<V>::scalar_type s )
            {
            vec_traits<V>::template write_element<I>(*reinterpret_cast<V *>(this), s);
            }

        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        operator typename vec_traits<V>::scalar_type() const
            {
            return vec_traits<V>::template read_element<I>(*reinterpret_cast<V const *>(this));
            }
        };
    }

template <int I,class V>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL
typename enable_if_c<
    is_vec<V>::value,
    typename vec_traits<V>::scalar_type>::type
A( V const & a )
    {
    BOOST_QVM_STATIC_ASSERT(I>=0);
    BOOST_QVM_STATIC_ASSERT(I<vec_traits<V>::dim);
    return vec_traits<V>::template read_element<I>(a);
    }

template <int I,class V>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL
typename enable_if_c<
    is_vec<V>::value && vec_write_element_ref<V>::value,
    typename vec_traits<V>::scalar_type &>::type
A( V & a )
    {
    BOOST_QVM_STATIC_ASSERT(I>=0);
    BOOST_QVM_STATIC_ASSERT(I<vec_traits<V>::dim);
    return vec_traits<V>::template write_element<I>(a);
    }

template <int I,class V>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL
typename enable_if_c<
    is_vec<V>::value && !vec_write_element_ref<V>::value,
    qvm_detail::v_element_access<I,V> &>::type
A( V & a )
    {
    BOOST_QVM_STATIC_ASSERT(I>=0);
    BOOST_QVM_STATIC_ASSERT(I<vec_traits<V>::dim);
    return *reinterpret_cast<qvm_detail::v_element_access<I,V> *>(&a);
    }

template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type X( V & a ) { BOOST_QVM_STATIC_ASSERT(0<vec_traits<V>::dim); return vec_traits<V>::template write_element<0>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type Y( V & a ) { BOOST_QVM_STATIC_ASSERT(1<vec_traits<V>::dim); return vec_traits<V>::template write_element<1>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type Z( V & a ) { BOOST_QVM_STATIC_ASSERT(2<vec_traits<V>::dim); return vec_traits<V>::template write_element<2>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type W( V & a ) { BOOST_QVM_STATIC_ASSERT(3<vec_traits<V>::dim); return vec_traits<V>::template write_element<3>(a); }

template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A0( V & a ) {  BOOST_QVM_STATIC_ASSERT(0<vec_traits<V>::dim); return vec_traits<V>::template write_element<0>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A1( V & a ) {  BOOST_QVM_STATIC_ASSERT(1<vec_traits<V>::dim); return vec_traits<V>::template write_element<1>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A2( V & a ) {  BOOST_QVM_STATIC_ASSERT(2<vec_traits<V>::dim); return vec_traits<V>::template write_element<2>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A3( V & a ) {  BOOST_QVM_STATIC_ASSERT(3<vec_traits<V>::dim); return vec_traits<V>::template write_element<3>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A4( V & a ) {  BOOST_QVM_STATIC_ASSERT(4<vec_traits<V>::dim); return vec_traits<V>::template write_element<4>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A5( V & a ) {  BOOST_QVM_STATIC_ASSERT(5<vec_traits<V>::dim); return vec_traits<V>::template write_element<5>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A6( V & a ) {  BOOST_QVM_STATIC_ASSERT(6<vec_traits<V>::dim); return vec_traits<V>::template write_element<6>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A7( V & a ) {  BOOST_QVM_STATIC_ASSERT(7<vec_traits<V>::dim); return vec_traits<V>::template write_element<7>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A8( V & a ) {  BOOST_QVM_STATIC_ASSERT(8<vec_traits<V>::dim); return vec_traits<V>::template write_element<8>(a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && vec_write_element_ref<V>::value,typename vec_traits<V>::scalar_type &>::type A9( V & a ) {  BOOST_QVM_STATIC_ASSERT(9<vec_traits<V>::dim); return vec_traits<V>::template write_element<9>(a); }

template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<0,V> &>::type X( V & a ) { BOOST_QVM_STATIC_ASSERT(0<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<0, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<1,V> &>::type Y( V & a ) { BOOST_QVM_STATIC_ASSERT(1<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<1, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<2,V> &>::type Z( V & a ) { BOOST_QVM_STATIC_ASSERT(2<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<2, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<3,V> &>::type W( V & a ) { BOOST_QVM_STATIC_ASSERT(3<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<3, V> *>(&a); }

template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<0,V> &>::type A0( V & a ) {  BOOST_QVM_STATIC_ASSERT(0<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<0, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<1,V> &>::type A1( V & a ) {  BOOST_QVM_STATIC_ASSERT(1<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<1, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<2,V> &>::type A2( V & a ) {  BOOST_QVM_STATIC_ASSERT(2<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<2, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<3,V> &>::type A3( V & a ) {  BOOST_QVM_STATIC_ASSERT(3<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<3, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<4,V> &>::type A4( V & a ) {  BOOST_QVM_STATIC_ASSERT(4<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<4, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<5,V> &>::type A5( V & a ) {  BOOST_QVM_STATIC_ASSERT(5<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<5, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<6,V> &>::type A6( V & a ) {  BOOST_QVM_STATIC_ASSERT(6<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<6, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<7,V> &>::type A7( V & a ) {  BOOST_QVM_STATIC_ASSERT(7<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<7, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<8,V> &>::type A8( V & a ) {  BOOST_QVM_STATIC_ASSERT(8<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<8, V> *>(&a); }
template <class V> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_vec<V>::value && !vec_write_element_ref<V>::value,qvm_detail::v_element_access<9,V> &>::type A9( V & a ) {  BOOST_QVM_STATIC_ASSERT(9<vec_traits<V>::dim); return *reinterpret_cast<qvm_detail::v_element_access<9, V> *>(&a); }

} }

#endif
