// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_PROCESS_V2_DETAIL_IMPL_ENVIRONMENT_IPP
#define BOOST_PROCESS_V2_DETAIL_IMPL_ENVIRONMENT_IPP

#include <boost/process/v2/detail/config.hpp>

#if defined(BOOST_PROCESS_V2_WINDOWS)
#include <boost/process/v2/detail/impl/environment_win.ipp>
#elif defined(BOOST_PROCESS_V2_POSIX)
#include <boost/process/v2/detail/impl/environment_posix.ipp>
#else
#error Operating System not supported.
#endif

#endif //BOOST_PROCESS_V2_DETAIL_IMPL_ENVIRONMENT_IPP
