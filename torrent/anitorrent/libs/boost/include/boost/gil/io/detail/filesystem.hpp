//
// Copyright 2022 Mateusz Loskot <mateusz at loskot dot net>
//
// Distributed under the Boost Software License, Version 1.0
// See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt
//
#ifndef BOOST_GIL_IO_DETAIL_FILESYSTEM_HPP
#define BOOST_GIL_IO_DETAIL_FILESYSTEM_HPP

#include <boost/config.hpp>

#if !defined(BOOST_GIL_IO_USE_BOOST_FILESYSTEM) && !defined(BOOST_NO_CXX17_HDR_FILESYSTEM)
#if defined(__cpp_lib_filesystem)
#include <filesystem>
#define BOOST_GIL_IO_USE_STD_FILESYSTEM
#elif defined(__cpp_lib_experimental_filesystem)
#include <experimental/filesystem>
#define BOOST_GIL_IO_USE_STD_FILESYSTEM
#define BOOST_GIL_IO_USE_STD_EXPERIMENTAL_FILESYSTEM
#endif
#endif // !BOOST_GIL_IO_USE_BOOST_FILESYSTEM && !BOOST_NO_CXX17_HDR_FILESYSTEM

#if !defined(BOOST_GIL_IO_USE_STD_FILESYSTEM)
// Disable warning: conversion to 'std::atomic<int>::__integral_type {aka int}' from 'long int' may alter its value
#if defined(BOOST_CLANG)
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wshorten-64-to-32"
#endif

#if defined(BOOST_GCC) && (BOOST_GCC >= 40900)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wconversion"
#endif

#define BOOST_FILESYSTEM_VERSION 3
#include <boost/filesystem.hpp>
#define BOOST_GIL_IO_USE_BOOST_FILESYSTEM

#if defined(BOOST_CLANG)
#pragma clang diagnostic pop
#endif

#if defined(BOOST_GCC) && (BOOST_GCC >= 40900)
#pragma GCC diagnostic pop
#endif

#endif

namespace boost { namespace gil { namespace detail {

#if defined(BOOST_GIL_IO_USE_STD_EXPERIMENTAL_FILESYSTEM)
namespace filesystem = std::experimental::filesystem;
#elif defined(BOOST_GIL_IO_USE_STD_FILESYSTEM)
namespace filesystem = std::filesystem;
#else
#if !defined(BOOST_GIL_IO_USE_BOOST_FILESYSTEM)
#error "Boost.Filesystem is required if C++17 <filesystem> is not available"
#endif
namespace filesystem = boost::filesystem;
#endif

}}} // namespace boost::gil::detail

#endif