//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_VERSION_HPP
#define BOOST_BEAST_VERSION_HPP

#include <boost/beast/core/detail/config.hpp>
#include <boost/config.hpp>

//[version

/* Identifies the API version of Beast.

   This is a simple integer that is incremented by one every
   time a set of code changes is merged to the develop branch.
*/
#define BOOST_BEAST_VERSION 353

// A string describing BOOST_BEAST_VERSION, that can be used in http headers.
#define BOOST_BEAST_VERSION_STRING "Boost.Beast/" BOOST_STRINGIZE(BOOST_BEAST_VERSION)

//]

#endif

