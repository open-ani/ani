//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_CONNECTION_POOL_WAIT_GROUP_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_CONNECTION_POOL_WAIT_GROUP_HPP

#include <boost/mysql/error_code.hpp>

#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/bind_executor.hpp>
#include <boost/asio/steady_timer.hpp>

#include <chrono>
#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

class wait_group
{
    std::size_t running_tasks_{};
    asio::steady_timer finished_;

public:
    wait_group(asio::any_io_executor ex)
        : finished_(std::move(ex), (std::chrono::steady_clock::time_point::max)())
    {
    }

    asio::any_io_executor get_executor() { return finished_.get_executor(); }

    void on_task_start() noexcept { ++running_tasks_; }

    void on_task_finish() noexcept
    {
        if (--running_tasks_ == 0u)
            finished_.cancel();  // If this happens to fail, terminate() is the best option
    }

    // Note: this operation always completes with a cancelled error code
    // (for simplicity).
    template <class CompletionToken>
    void async_wait(CompletionToken&& token)
    {
        return finished_.async_wait(std::forward<CompletionToken>(token));
    }

    // Runs op calling the adequate group member functions when op is started and finished.
    // The operation is run within this->get_executor()
    template <class Op>
    void run_task(Op&& op)
    {
        on_task_start();
        std::forward<Op>(op)(asio::bind_executor(get_executor(), [this](error_code) { on_task_finish(); }));
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
