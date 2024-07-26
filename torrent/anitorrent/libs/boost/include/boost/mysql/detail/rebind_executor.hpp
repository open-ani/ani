//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_REBIND_EXECUTOR_HPP
#define BOOST_MYSQL_DETAIL_REBIND_EXECUTOR_HPP

#include <boost/asio/ssl/stream.hpp>

namespace boost {
namespace mysql {
namespace detail {

// This is required because ssl::stream doesn't have a rebind_executor member type
template <class Stream, class Executor>
struct rebind_executor
{
    using type = typename Stream::template rebind_executor<Executor>::other;
};

template <class Stream, class Executor>
struct rebind_executor<boost::asio::ssl::stream<Stream>, Executor>
{
    using type = boost::asio::ssl::stream<typename rebind_executor<Stream, Executor>::type>;
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
