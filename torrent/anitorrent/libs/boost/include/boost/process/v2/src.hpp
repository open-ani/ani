// Copyright (c) 2021 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_PROCESS_V2_SRC_HPP
#define BOOST_PROCESS_V2_SRC_HPP

#define BOOST_PROCESS_V2_SOURCE

#include <boost/process/v2/detail/config.hpp>

#if defined(BOOST_PROCESS_V2_HEADER_ONLY)
# error Do not compile Process V2 library source with BOOST_PROCESS_V2_HEADER_ONLY defined. You should probably define BOOST_PROCESS_V2_SEPARATE_COMPILATION
#endif

#include <boost/process/v2/impl/error.ipp>
#include <boost/process/v2/impl/pid.ipp>
#include <boost/process/v2/ext/impl/exe.ipp>
#include <boost/process/v2/ext/impl/cwd.ipp>
#include <boost/process/v2/ext/impl/cmd.ipp>
#include <boost/process/v2/ext/impl/env.ipp>
#include <boost/process/v2/ext/detail/impl/proc_info.ipp>
#include <boost/process/v2/detail/impl/environment.ipp>
#include <boost/process/v2/detail/impl/last_error.ipp>
#include <boost/process/v2/detail/impl/throw_error.ipp>
#include <boost/process/v2/detail/impl/utf8.ipp>
#include <boost/process/v2/impl/default_launcher.ipp>
#include <boost/process/v2/impl/environment.ipp>
#include <boost/process/v2/impl/process_handle.ipp>
#include <boost/process/v2/impl/shell.ipp>

#endif //BOOST_PROCESS_V2_SRC_HPP
