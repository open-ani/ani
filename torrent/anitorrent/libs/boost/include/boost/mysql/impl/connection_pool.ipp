//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_CONNECTION_POOL_IPP
#define BOOST_MYSQL_IMPL_CONNECTION_POOL_IPP

#pragma once

#include <boost/mysql/connection_pool.hpp>

#include <boost/mysql/detail/connection_pool_fwd.hpp>

#include <boost/mysql/impl/internal/connection_pool/connection_pool_impl.hpp>

#include <memory>

void boost::mysql::detail::return_connection(
    std::shared_ptr<pool_impl> pool,
    connection_node& node,
    bool should_reset
) noexcept
{
    // This is safe to be called from any thread, and is noexcept
    node.mark_as_collectable(should_reset);

    // If, for any reason, this notification fails, the connection will
    // be collected when the next ping is due.
    try
    {
        // A handler to be passed to dispatch. Binds the executor
        // and keeps the pool alive
        struct dispatch_handler
        {
            std::shared_ptr<pool_impl> pool_ptr;
            connection_node* node_ptr;

            using executor_type = asio::any_io_executor;
            executor_type get_executor() const noexcept { return pool_ptr->get_executor(); }

            void operator()() const { node_ptr->notify_collectable(); }
        };

        asio::dispatch(dispatch_handler{std::move(pool), &node});
    }
    catch (...)
    {
    }
}

boost::mysql::any_connection& boost::mysql::detail::get_connection(boost::mysql::detail::connection_node& node
) noexcept
{
    return node.connection();
}

boost::mysql::connection_pool::connection_pool(pool_executor_params&& ex_params, pool_params&& params, int)
    : impl_(std::make_shared<detail::pool_impl>(std::move(ex_params), std::move(params)))
{
}

boost::mysql::connection_pool::executor_type boost::mysql::connection_pool::get_executor() noexcept
{
    return impl_->get_executor();
}

void boost::mysql::connection_pool::async_run_erased(
    std::shared_ptr<detail::pool_impl> pool,
    asio::any_completion_handler<void(error_code)> handler
)
{
    pool->async_run(std::move(handler));
}

void boost::mysql::connection_pool::async_get_connection_erased(
    std::shared_ptr<detail::pool_impl> pool,
    std::chrono::steady_clock::duration timeout,
    diagnostics* diag,
    asio::any_completion_handler<void(error_code, pooled_connection)> handler
)
{
    pool->async_get_connection(timeout, diag, std::move(handler));
}

void boost::mysql::connection_pool::cancel()
{
    BOOST_ASSERT(valid());

    // A handler to be passed to dispatch. Binds the executor
    // and keeps the pool alive
    struct dispatch_handler
    {
        std::shared_ptr<detail::pool_impl> pool_ptr;

        using executor_type = asio::any_io_executor;
        executor_type get_executor() const noexcept { return pool_ptr->get_executor(); }

        void operator()() const { pool_ptr->cancel_unsafe(); }
    };

    asio::dispatch(dispatch_handler{impl_});
}

#endif
