#ifndef BOOST_DESCRIBE_DESCRIPTOR_BY_NAME_HPP_INCLUDED
#define BOOST_DESCRIBE_DESCRIPTOR_BY_NAME_HPP_INCLUDED

// Copyright 2021 Peter Dimov
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#include <boost/describe/detail/cx_streq.hpp>
#include <boost/describe/detail/config.hpp>

#if defined(BOOST_DESCRIBE_CXX14)

#include <boost/mp11/algorithm.hpp>
#include <boost/mp11/bind.hpp>
#include <boost/mp11/integral.hpp>

namespace boost
{
namespace describe
{

namespace detail
{

template<class D, class N> using match_by_name = mp11::mp_bool<cx_streq(N::name(), D::name)>;

#define BOOST_DESCRIBE_MAKE_NAME_IMPL2(s, k) struct _boost_name_##s##_##k { static constexpr char const * name() { return #s; } }
#define BOOST_DESCRIBE_MAKE_NAME_IMPL(s, k) BOOST_DESCRIBE_MAKE_NAME_IMPL2(s, k)

} // namespace detail

#define BOOST_DESCRIBE_MAKE_NAME(s) BOOST_DESCRIBE_MAKE_NAME_IMPL(s, __LINE__)

template<class L, class N> using descriptor_by_name = mp11::mp_at<L, mp11::mp_find_if_q<L, mp11::mp_bind_back<detail::match_by_name, N>>>;

} // namespace describe
} // namespace boost

#endif // defined(BOOST_DESCRIBE_CXX14)

#endif // #ifndef BOOST_DESCRIBE_DESCRIPTOR_BY_NAME_HPP_INCLUDED
