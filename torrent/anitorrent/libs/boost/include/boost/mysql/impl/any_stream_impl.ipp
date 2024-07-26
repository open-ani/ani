//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_ANY_STREAM_IMPL_IPP
#define BOOST_MYSQL_IMPL_ANY_STREAM_IMPL_IPP

#pragma once

#include <boost/mysql/detail/any_stream_impl.hpp>

#ifdef BOOST_MYSQL_SEPARATE_COMPILATION
template class boost::mysql::detail::any_stream_impl<boost::asio::ssl::stream<boost::asio::ip::tcp::socket>>;
template class boost::mysql::detail::any_stream_impl<boost::asio::ip::tcp::socket>;
#endif

#endif
