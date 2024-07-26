//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_CONNECTION_POOL_RUN_WITH_TIMEOUT_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_CONNECTION_POOL_RUN_WITH_TIMEOUT_HPP

#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/error_code.hpp>

#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/associated_allocator.hpp>
#include <boost/asio/bind_executor.hpp>
#include <boost/asio/cancellation_signal.hpp>

#include <chrono>
#include <cstddef>
#include <memory>
#include <type_traits>
#include <utility>

// Runs a certain operation with a timeout. This is a lightweight replacement
// for parallel_group, since the latter has bugs (https://github.com/chriskohlhoff/asio/issues/1397)
// that make it unsuitable for us.

namespace boost {
namespace mysql {
namespace detail {

// Shared state, between the timer and the op.
// Not thread-safe - should only be used within the pool's executor.
template <class Timer, class Handler>
struct run_with_timeout_state
{
    using this_type = run_with_timeout_state<Timer, Handler>;

    // A cancellation signal to cancel the op if the timer fires first.
    asio::cancellation_signal op_signal;

    // The number of ops remaining. We won't call the handler until timer and op finish.
    std::size_t remaining;

    // The error code to call the handler with
    error_code final_ec;

    // The final handler
    Handler handler;

    // The timer that provides our timeout
    Timer& timer;

    run_with_timeout_state(Handler&& handler, Timer& timer)
        : remaining(2), handler(std::move(handler)), timer(timer)
    {
    }

    // Used by handlers. Ensures that memory is released before calling the handler
    static void complete_one_op(std::shared_ptr<this_type>&& ptr)
    {
        // All finished
        if (ptr->remaining == 0u)
        {
            // Save members required to call the handler
            auto h = std::move(ptr->handler);
            error_code ec = ptr->final_ec;

            // Free memory
            ptr.reset();

            // Call the handler
            std::move(h)(ec);
        }
    }

    // A specialized handler for the timer
    struct timer_handler
    {
        std::shared_ptr<this_type> st;

        void operator()(error_code ec)
        {
            // If the op has already completed, we don't care about the timer's result
            // Emitting the signal may call the handler inline, so we decrement first
            if (st->remaining-- == 2u)
            {
                st->final_ec = ec ? client_errc::cancelled : client_errc::timeout;
                st->op_signal.emit(asio::cancellation_type::terminal);
            }

            // Notify
            complete_one_op(std::move(st));
        }
    };

    // A specialized handler for the op. Ensures that the op is
    // run with the timer's executor and with the adequate cancellation slot
    struct op_handler
    {
        std::shared_ptr<this_type> st;

        void operator()(error_code ec)
        {
            // If the timer finished first, we don't care about the result
            if (st->remaining-- == 2u)
            {
                st->final_ec = ec;
                st->timer.cancel();
            }

            // Notify
            complete_one_op(std::move(st));
        }

        // Executor binding
        using executor_type = asio::any_io_executor;
        executor_type get_executor() const { return st->timer.get_executor(); }

        // Cancellation slot binding
        using cancellation_slot_type = asio::cancellation_slot;
        cancellation_slot_type get_cancellation_slot() const noexcept { return st->op_signal.slot(); }
    };
};

// Runs op in parallel with a timer. op must be a deferred operation with void(error_code) signature.
// Handler must be a suitable completion handler. Arbitrary completion tokens are not supported.
// Handler is called with the following error code:
//   - If the op finishes first, with op's error code.
//   - If the timer finishes first, without interruptions, with client_errc::timeout.
//   - If the timer finishes first because it was cancelled, with client_errc::cancelled.
// Both op and timer are run within the timer's executor.
// If timeout == 0, the timeout is disabled.
template <class Op, class Timer, class Handler>
void run_with_timeout(Op&& op, Timer& timer, std::chrono::steady_clock::duration timeout, Handler&& handler)
{
    if (timeout.count() > 0)
    {
        using state_t = run_with_timeout_state<Timer, typename std::decay<Handler>::type>;

        // Allocate the shared state
        auto alloc = asio::get_associated_allocator(handler);
        using alloc_t = typename std::allocator_traits<decltype(alloc)>::template rebind_alloc<state_t>;
        auto st = std::allocate_shared<state_t>(alloc_t(alloc), std::move(handler), timer);

        // Launch the timer
        timer.expires_after(timeout);
        timer.async_wait(typename state_t::timer_handler{st});

        // Launch the op
        std::move(op)(typename state_t::op_handler{std::move(st)});
    }
    else
    {
        std::forward<Op>(op)(asio::bind_executor(timer.get_executor(), std::move(handler)));
    }
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
