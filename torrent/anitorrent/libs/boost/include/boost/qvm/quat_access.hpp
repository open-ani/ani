#ifndef BOOST_QVM_QUAT_ACCESS_HPP_INCLUDED
#define BOOST_QVM_QUAT_ACCESS_HPP_INCLUDED

// Copyright 2008-2022 Emil Dotchevski and Reverge Studios, Inc.

// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#include <boost/qvm/config.hpp>
#include <boost/qvm/quat_traits.hpp>
#include <boost/qvm/deduce_vec.hpp>
#include <boost/qvm/static_assert.hpp>
#include <boost/qvm/enable_if.hpp>

namespace boost { namespace qvm {

template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value,typename quat_traits<Q>::scalar_type>::type S( Q const & a ) { return quat_traits<Q>::template read_element<0>(a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value,typename quat_traits<Q>::scalar_type>::type X( Q const & a ) { return quat_traits<Q>::template read_element<1>(a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value,typename quat_traits<Q>::scalar_type>::type Y( Q const & a ) { return quat_traits<Q>::template read_element<2>(a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value,typename quat_traits<Q>::scalar_type>::type Z( Q const & a ) { return quat_traits<Q>::template read_element<3>(a); }

namespace
qvm_detail
    {
    template <int I,class Q>
    struct
    q_element_access
        {
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        void
        operator=( typename quat_traits<Q>::scalar_type s )
            {
            quat_traits<Q>::template write_element<I>(*reinterpret_cast<Q *>(this), s);
            }

        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        operator typename vec_traits<Q>::scalar_type() const
            {
            return quat_traits<Q>::template read_element<I>(*reinterpret_cast<Q const *>(this));
            }
        };

    template <class Q>
    struct
    quat_v_
        {
        template <class R
#if __cplusplus >= 201103L
            , class = typename enable_if<is_vec<R> >::type
#endif
        >
        operator R() const
            {
            R r;
            assign(r,*this);
            return r;
            }

        private:

        quat_v_( quat_v_ const & );
        quat_v_ const & operator=( quat_v_ const & );
        ~quat_v_();
        };

    template <class Q,bool WriteElementRef=quat_write_element_ref<Q>::value>
    struct quat_v_write_traits;

    template <class Q>
    struct
    quat_v_write_traits<Q,true>
        {
        typedef qvm_detail::quat_v_<Q> this_vector;
        typedef typename quat_traits<Q>::scalar_type scalar_type;
        static int const dim=3;

        template <int I>
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        static
        scalar_type &
        write_element( this_vector & q )
            {
            BOOST_QVM_STATIC_ASSERT(I>=0);
            BOOST_QVM_STATIC_ASSERT(I<dim);
            return quat_traits<Q>::template write_element<I+1>( reinterpret_cast<Q &>(q) );
            }
        };

    template <class Q>
    struct
    quat_v_write_traits<Q,false>
        {
        typedef qvm_detail::quat_v_<Q> this_vector;
        typedef typename quat_traits<Q>::scalar_type scalar_type;
        static int const dim=3;

        template <int I>
        BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
        static
        void
        write_element( this_vector & q, scalar_type s )
            {
            BOOST_QVM_STATIC_ASSERT(I>=0);
            BOOST_QVM_STATIC_ASSERT(I<dim);
            quat_traits<Q>::template write_element<I+1>( reinterpret_cast<Q &>(q), s );
            }
        };
    }

template <class V>
struct vec_traits;

template <class Q>
struct
vec_traits< qvm_detail::quat_v_<Q> >:
    qvm_detail::quat_v_write_traits<Q>
    {
    typedef qvm_detail::quat_v_<Q> this_vector;
    typedef typename quat_traits<Q>::scalar_type scalar_type;
    static int const dim=3;

    template <int I>
    BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_CRITICAL
    static
    scalar_type
    read_element( this_vector const & q )
        {
        BOOST_QVM_STATIC_ASSERT(I>=0);
        BOOST_QVM_STATIC_ASSERT(I<dim);
        return quat_traits<Q>::template read_element<I+1>( reinterpret_cast<Q const &>(q) );
        }
    };

template <class Q,int D>
struct
deduce_vec<qvm_detail::quat_v_<Q>,D>
    {
    typedef vec<typename quat_traits<Q>::scalar_type,D> type;
    };

template <class Q,int D>
struct
deduce_vec2<qvm_detail::quat_v_<Q>,qvm_detail::quat_v_<Q>,D>
    {
    typedef vec<typename quat_traits<Q>::scalar_type,D> type;
    };

template <class Q>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL
typename enable_if_c<
    is_quat<Q>::value,
    qvm_detail::quat_v_<Q> const &>::type
V( Q const & a )
    {
    return reinterpret_cast<qvm_detail::quat_v_<Q> const &>(a);
    }

template <class Q>
BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL
typename enable_if_c<
    is_quat<Q>::value,
    qvm_detail::quat_v_<Q> &>::type
V( Q & a )
    {
    return reinterpret_cast<qvm_detail::quat_v_<Q> &>(a);
    }

template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value && quat_write_element_ref<Q>::value,typename quat_traits<Q>::scalar_type &>::type S( Q & a ) { return quat_traits<Q>::template write_element<0>(a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value && quat_write_element_ref<Q>::value,typename quat_traits<Q>::scalar_type &>::type X( Q & a ) { return quat_traits<Q>::template write_element<1>(a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value && quat_write_element_ref<Q>::value,typename quat_traits<Q>::scalar_type &>::type Y( Q & a ) { return quat_traits<Q>::template write_element<2>(a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value && quat_write_element_ref<Q>::value,typename quat_traits<Q>::scalar_type &>::type Z( Q & a ) { return quat_traits<Q>::template write_element<3>(a); }

template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value && !quat_write_element_ref<Q>::value,qvm_detail::q_element_access<0,Q> &>::type S( Q & a ) { return *reinterpret_cast<qvm_detail::q_element_access<0, Q> *>(&a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value && !quat_write_element_ref<Q>::value,qvm_detail::q_element_access<1,Q> &>::type X( Q & a ) { return *reinterpret_cast<qvm_detail::q_element_access<1, Q> *>(&a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value && !quat_write_element_ref<Q>::value,qvm_detail::q_element_access<2,Q> &>::type Y( Q & a ) { return *reinterpret_cast<qvm_detail::q_element_access<2, Q> *>(&a); }
template <class Q> BOOST_QVM_CONSTEXPR BOOST_QVM_INLINE_TRIVIAL typename enable_if_c<is_quat<Q>::value && !quat_write_element_ref<Q>::value,qvm_detail::q_element_access<3,Q> &>::type Z( Q & a ) { return *reinterpret_cast<qvm_detail::q_element_access<3, Q> *>(&a); }

} }

#endif
