//
// boost/process/v2/default_launcher.hpp
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
// Copyright (c) 2022 Klemens D. Morgenstern (klemens dot morgenstern at gmx dot net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_PROCESS_V2_IMPL_DEFAULT_LAUNCHER_IPP
#define BOOST_PROCESS_V2_IMPL_DEFAULT_LAUNCHER_IPP

#include <boost/process/v2/detail/config.hpp>

#if defined(BOOST_PROCESS_V2_WINDOWS)
#include <boost/process/v2/windows/impl/default_launcher.ipp>
#else
#include <boost/process/v2/posix/detail/close_handles.ipp>
#endif



#endif //BOOST_PROCESS_V2_IMPL_DEFAULT_LAUNCHER_IPP