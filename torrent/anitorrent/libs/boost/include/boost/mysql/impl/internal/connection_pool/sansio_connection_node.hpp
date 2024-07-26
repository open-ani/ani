//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_CONNECTION_POOL_SANSIO_CONNECTION_NODE_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_CONNECTION_POOL_SANSIO_CONNECTION_NODE_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>

#include <boost/assert.hpp>

namespace boost {
namespace mysql {
namespace detail {

// The status the connection is in
enum class connection_status
{
    // Connection task hasn't initiated yet.
    // This status doesn't count as pending. This facilitates tracking pending connections.
    initial,

    // Connection is trying to connect
    connect_in_progress,

    // Connect failed and we're sleeping
    sleep_connect_failed_in_progress,

    // Connection is trying to reset
    reset_in_progress,

    // Connection is trying to ping
    ping_in_progress,

    // Connection can be handed to the user
    idle,

    // Connection has been handed to the user
    in_use,

    // After cancel
    terminated,
};

// The next I/O action the connection should take. There's
// no 1-1 mapping to connection_status
enum class next_connection_action
{
    // Do nothing, exit the loop
    none,

    // Issue a connect
    connect,

    // Connect failed, issue a sleep
    sleep_connect_failed,

    // Wait until a collection request is issued or the ping interval elapses
    idle_wait,

    // Issue a reset
    reset,

    // Issue a ping
    ping,
};

// A collection_state represents the possibility that a connection
// that was in_use was returned by the user
enum class collection_state
{
    // Connection wasn't returned
    none,

    // Connection was returned and needs reset
    needs_collect,

    // Connection was returned and doesn't need reset
    needs_collect_with_reset
};

// CRTP. Derived should implement the entering_xxx and exiting_xxx hook functions.
// Derived must derive from this class
template <class Derived>
class sansio_connection_node
{
    connection_status status_;

    inline bool is_pending(connection_status status) noexcept
    {
        return status != connection_status::initial && status != connection_status::idle &&
               status != connection_status::in_use && status != connection_status::terminated;
    }

    inline static next_connection_action status_to_action(connection_status status) noexcept
    {
        switch (status)
        {
        case connection_status::connect_in_progress: return next_connection_action::connect;
        case connection_status::sleep_connect_failed_in_progress:
            return next_connection_action::sleep_connect_failed;
        case connection_status::ping_in_progress: return next_connection_action::ping;
        case connection_status::reset_in_progress: return next_connection_action::reset;
        case connection_status::idle:
        case connection_status::in_use: return next_connection_action::idle_wait;
        default: return next_connection_action::none;
        }
    }

    next_connection_action set_status(connection_status new_status)
    {
        auto& derived = static_cast<Derived&>(*this);

        // Notify we're entering/leaving the idle status
        if (new_status == connection_status::idle && status_ != connection_status::idle)
            derived.entering_idle();
        else if (new_status != connection_status::idle && status_ == connection_status::idle)
            derived.exiting_idle();

        // Notify we're entering/leaving a pending status
        if (!is_pending(status_) && is_pending(new_status))
            derived.entering_pending();
        else if (is_pending(status_) && !is_pending(new_status))
            derived.exiting_pending();

        // Actually update status
        status_ = new_status;

        return status_to_action(new_status);
    }

public:
    sansio_connection_node(connection_status initial_status = connection_status::initial) noexcept
        : status_(initial_status)
    {
    }

    void mark_as_in_use() noexcept
    {
        BOOST_ASSERT(status_ == connection_status::idle);
        set_status(connection_status::in_use);
    }

    void cancel() { set_status(connection_status::terminated); }

    next_connection_action resume(error_code ec, collection_state col_st)
    {
        switch (status_)
        {
        case connection_status::initial: return set_status(connection_status::connect_in_progress);
        case connection_status::connect_in_progress:
            return ec ? set_status(connection_status::sleep_connect_failed_in_progress)
                      : set_status(connection_status::idle);
        case connection_status::sleep_connect_failed_in_progress:
            return set_status(connection_status::connect_in_progress);
        case connection_status::idle:
            // The wait finished with no interruptions, and the connection
            // is still idle. Time to ping.
            return set_status(connection_status::ping_in_progress);
        case connection_status::in_use:
            // If col_st != none, the user has notified us to collect the connection.
            // This happens after they return the connection to the pool.
            // Update status and continue
            if (col_st == collection_state::needs_collect)
            {
                // No reset needed, we're idle
                return set_status(connection_status::idle);
            }
            else if (col_st == collection_state::needs_collect_with_reset)
            {
                return set_status(connection_status::reset_in_progress);
            }
            else
            {
                // The user is still using the connection (it's taking long, but can happen).
                // Idle wait again until they return the connection.
                return next_connection_action::idle_wait;
            }
        case connection_status::ping_in_progress:
        case connection_status::reset_in_progress:
            // Reconnect if there was an error. Otherwise, we're idle
            return ec ? set_status(connection_status::connect_in_progress)
                      : set_status(connection_status::idle);
        case connection_status::terminated:
        default: return next_connection_action::none;
        }
    }

    // Exposed for testing
    connection_status status() const noexcept { return status_; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
