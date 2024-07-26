// Boost.Geometry

// Copyright (c) 2019-2021 Barend Gehrels, Amsterdam, the Netherlands.

// Copyright (c) 2018-2023 Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_CORE_CONFIG_HPP
#define BOOST_GEOMETRY_CORE_CONFIG_HPP

#include <boost/config.hpp>
#include <boost/config/pragma_message.hpp>

// If no define is specified, it uses BOOST_GEOMETRY_NO_ROBUSTNESS, which means: no rescaling.
// (Rescaling was an earlier approach to fix robustness issues.)
// However, if BOOST_GEOMETRY_ROBUSTNESS_ALTERNATIVE is specified, it flips the default.
#if !defined(BOOST_GEOMETRY_NO_ROBUSTNESS) && !defined(BOOST_GEOMETRY_USE_RESCALING)
  #if defined(BOOST_GEOMETRY_ROBUSTNESS_ALTERNATIVE)
    #define BOOST_GEOMETRY_USE_RESCALING
  #else
    #define BOOST_GEOMETRY_NO_ROBUSTNESS
  #endif
#endif

#if defined(BOOST_GEOMETRY_USE_RESCALING) && ! defined(BOOST_GEOMETRY_ROBUSTNESS_ALTERNATIVE)
    BOOST_PRAGMA_MESSAGE("Rescaling is deprecated and its functionality will be removed in Boost 1.84")
#endif

#if defined(BOOST_GEOMETRY_USE_RESCALING) && defined(BOOST_GEOMETRY_NO_ROBUSTNESS)
    #error "Define either BOOST_GEOMETRY_NO_ROBUSTNESS (default) or BOOST_GEOMETRY_USE_RESCALING"
#endif

#endif // BOOST_GEOMETRY_CORE_CONFIG_HPP
