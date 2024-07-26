//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_SPAWN_HPP
#define BOOST_COBALT_SPAWN_HPP

#include <boost/cobalt/detail/spawn.hpp>

namespace boost::cobalt
{

template<with_get_executor Context, typename T, typename CompletionToken>
auto spawn(Context & context,
           task<T> && t,
           CompletionToken&& token)
{
    return asio::async_initiate<CompletionToken, void(std::exception_ptr, T)>(
            detail::async_initiate_spawn{}, token, std::move(t), context.get_executor());
}

template<std::convertible_to<executor> Executor, typename T, typename CompletionToken>
auto spawn(Executor executor, task<T> && t,
           CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(Executor))
{
    return asio::async_initiate<CompletionToken, void(std::exception_ptr, T)>(
            detail::async_initiate_spawn{}, token, std::move(t), executor);
}

template<with_get_executor Context, typename CompletionToken>
auto spawn(Context & context,
           task<void> && t,
           CompletionToken&& token)
{
    return asio::async_initiate<CompletionToken, void(std::exception_ptr)>(
            detail::async_initiate_spawn{}, token, std::move(t), context.get_executor());
}

template<std::convertible_to<executor> Executor, typename CompletionToken>
auto spawn(Executor executor, task<void> && t,
           CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(Executor))
{
    return asio::async_initiate<CompletionToken, void(std::exception_ptr)>(
            detail::async_initiate_spawn{}, token, std::move(t), executor);

}

}

#endif //BOOST_COBALT_SPAWN_HPP
