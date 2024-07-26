#ifndef BOOST_ENDIAN_DETAIL_STATIC_ASSERT_HPP_INCLUDED
#define BOOST_ENDIAN_DETAIL_STATIC_ASSERT_HPP_INCLUDED

// Copyright 2023 Peter Dimov
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#define BOOST_ENDIAN_STATIC_ASSERT(...) static_assert(__VA_ARGS__, #__VA_ARGS__)

#endif  // BOOST_ENDIAN_DETAIL_STATIC_ASSERT_HPP_INCLUDED
