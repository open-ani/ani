//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_CONNECTION_POOL_CONNECTION_POOL_IMPL_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_CONNECTION_POOL_CONNECTION_POOL_IMPL_HPP

#include <boost/mysql/any_connection.hpp>
#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/pool_params.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/mysql/impl/internal/connection_pool/connection_node.hpp>
#include <boost/mysql/impl/internal/connection_pool/internal_pool_params.hpp>
#include <boost/mysql/impl/internal/connection_pool/timer_list.hpp>
#include <boost/mysql/impl/internal/connection_pool/wait_group.hpp>

#include <boost/asio/any_completion_handler.hpp>
#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/bind_executor.hpp>
#include <boost/asio/compose.hpp>
#include <boost/asio/coroutine.hpp>
#include <boost/asio/deferred.hpp>
#include <boost/asio/dispatch.hpp>
#include <boost/asio/error.hpp>
#include <boost/asio/post.hpp>
#include <boost/core/ignore_unused.hpp>

#include <chrono>
#include <cstddef>
#include <list>
#include <memory>

namespace boost {
namespace mysql {
namespace detail {

// Templating on ConnectionWrapper is useful for mocking in tests.
// Production code always uses ConnectionWrapper = pooled_connection.
template <class IoTraits, class ConnectionWrapper>
class basic_pool_impl : public std::enable_shared_from_this<basic_pool_impl<IoTraits, ConnectionWrapper>>
{
    using this_type = basic_pool_impl<IoTraits, ConnectionWrapper>;
    using node_type = basic_connection_node<IoTraits>;
    using timer_type = typename IoTraits::timer_type;
    using timer_block_type = timer_block<timer_type>;
    using shared_state_type = conn_shared_state<IoTraits>;

    enum class state_t
    {
        initial,
        running,
        cancelled,
    };

    state_t state_{state_t::initial};
    internal_pool_params params_;
    asio::any_io_executor ex_;
    asio::any_io_executor conn_ex_;
    std::list<node_type> all_conns_;
    shared_state_type shared_st_;
    wait_group wait_gp_;
    timer_type cancel_timer_;

    std::shared_ptr<this_type> shared_from_this_wrapper()
    {
        // Some compilers get confused without this explicit cast
        return static_cast<std::enable_shared_from_this<this_type>*>(this)->shared_from_this();
    }

    void create_connection()
    {
        all_conns_.emplace_back(params_, ex_, conn_ex_, shared_st_);
        wait_gp_.run_task(all_conns_.back().async_run(asio::deferred));
    }

    error_code get_diagnostics(diagnostics* diag) const
    {
        if (state_ == state_t::cancelled)
        {
            return client_errc::cancelled;
        }
        else if (shared_st_.last_ec)
        {
            if (diag)
                *diag = shared_st_.last_diag;
            return shared_st_.last_ec;
        }
        else
        {
            return client_errc::timeout;
        }
    }

    struct run_op : asio::coroutine
    {
        std::shared_ptr<this_type> obj_;

        run_op(std::shared_ptr<this_type> obj) noexcept : obj_(std::move(obj)) {}

        template <class Self>
        void operator()(Self& self, error_code ec = {})
        {
            // TODO: per-operation cancellation here doesn't do the right thing
            boost::ignore_unused(ec);
            BOOST_ASIO_CORO_REENTER(*this)
            {
                // Ensure we run within the pool executor (possibly a strand)
                BOOST_ASIO_CORO_YIELD
                asio::dispatch(obj_->ex_, std::move(self));

                // Check that we're not running and set the state adequately
                BOOST_ASSERT(obj_->state_ == state_t::initial);
                obj_->state_ = state_t::running;

                // Create the initial connections
                for (std::size_t i = 0; i < obj_->params_.initial_size; ++i)
                    obj_->create_connection();

                // Wait for the cancel notification to arrive.
                BOOST_ASIO_CORO_YIELD
                obj_->cancel_timer_.async_wait(std::move(self));

                // If the token passed to async_run had a bound executor,
                // the handler will be invoked within that executor.
                // Dispatch so we run within the pool's executor.
                BOOST_ASIO_CORO_YIELD
                asio::dispatch(obj_->ex_, std::move(self));

                // Deliver the cancel notification to all other tasks
                obj_->state_ = state_t::cancelled;
                for (auto& conn : obj_->all_conns_)
                    conn.cancel();
                obj_->shared_st_.pending_requests.notify_all();

                // Wait for all connection tasks to exit
                BOOST_ASIO_CORO_YIELD
                obj_->wait_gp_.async_wait(std::move(self));

                // Done
                obj_.reset();
                self.complete(error_code());
            }
        }
    };

    struct get_connection_op : asio::coroutine
    {
        std::shared_ptr<this_type> obj_;
        std::chrono::steady_clock::time_point timeout_;
        diagnostics* diag_;
        std::unique_ptr<timer_block_type> timer_;
        error_code stored_ec_;

        get_connection_op(
            std::shared_ptr<this_type> obj,
            std::chrono::steady_clock::time_point timeout,
            diagnostics* diag
        ) noexcept
            : obj_(std::move(obj)), timeout_(timeout), diag_(diag)
        {
        }

        template <class Self>
        void do_complete(Self& self, error_code ec, ConnectionWrapper conn)
        {
            // Resetting the timer will remove it from the list thanks to the auto-unlink feature
            timer_.reset();
            obj_.reset();
            self.complete(ec, std::move(conn));
        }

        template <class Self>
        void complete_success(Self& self, node_type& node)
        {
            node.mark_as_in_use();
            do_complete(self, error_code(), ConnectionWrapper(node, std::move(obj_)));
        }

        template <class Self>
        void operator()(Self& self, error_code ec = {})
        {
            BOOST_ASIO_CORO_REENTER(*this)
            {
                // Clear diagnostics
                if (diag_)
                    diag_->clear();

                // Ensure we run within the pool's executor (or the handler's) (possibly a strand)
                BOOST_ASIO_CORO_YIELD
                asio::post(obj_->ex_, std::move(self));

                // This loop guards us against possible race conditions
                // between waiting on the pending request timer and getting the connection
                while (true)
                {
                    // If we're not running yet, or were cancelled, just return
                    if (obj_->state_ != state_t::running)
                    {
                        do_complete(
                            self,
                            obj_->state_ == state_t::initial ? client_errc::pool_not_running
                                                             : client_errc::cancelled,
                            ConnectionWrapper()
                        );
                        return;
                    }

                    // Try to get a connection without blocking
                    if (!obj_->shared_st_.idle_list.empty())
                    {
                        // There was a connection. Done.
                        complete_success(self, obj_->shared_st_.idle_list.front());
                        return;
                    }

                    // No luck. If there is room for more connections, create one.
                    // Don't create new connections if we have other connections pending
                    // (i.e. being connected, reset... ) - otherwise pool size increases for
                    // no reason when there is no connectivity.
                    if (obj_->all_conns_.size() < obj_->params_.max_size &&
                        obj_->shared_st_.num_pending_connections == 0u)
                    {
                        obj_->create_connection();
                    }

                    // Allocate a timer to perform waits.
                    if (!timer_)
                    {
                        timer_.reset(new timer_block_type(obj_->ex_));
                        obj_->shared_st_.pending_requests.push_back(*timer_);
                    }

                    // Wait to be notified, or until a timeout happens
                    timer_->timer.expires_at(timeout_);
                    BOOST_ASIO_CORO_YIELD timer_->timer.async_wait(std::move(self));
                    stored_ec_ = ec;

                    // If the token passed to async_run had a bound executor,
                    // the handler will be invoked within that executor.
                    // Dispatch so we run within the pool's executor.
                    BOOST_ASIO_CORO_YIELD asio::dispatch(obj_->ex_, std::move(self));

                    if (!stored_ec_)
                    {
                        // We've got a timeout. Try to give as much info as possible
                        do_complete(self, obj_->get_diagnostics(diag_), ConnectionWrapper());
                        return;
                    }
                }
            }
        }
    };

public:
    basic_pool_impl(pool_executor_params&& ex_params, pool_params&& params)
        : params_(make_internal_pool_params(std::move(params))),
          ex_(std::move(ex_params.pool_executor)),
          conn_ex_(std::move(ex_params.connection_executor)),
          wait_gp_(ex_),
          cancel_timer_(ex_, (std::chrono::steady_clock::time_point::max)())
    {
    }

    using executor_type = asio::any_io_executor;

    executor_type get_executor() { return ex_; }

    template <class CompletionToken>
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code))
    async_run(CompletionToken&& token)
    {
        return asio::async_compose<CompletionToken, void(error_code)>(
            run_op(shared_from_this_wrapper()),
            token,
            ex_
        );
    }

    // Not thread-safe
    void cancel_unsafe() { cancel_timer_.expires_at((std::chrono::steady_clock::time_point::min)()); }

    template <class CompletionToken>
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code, ConnectionWrapper))
    async_get_connection(
        std::chrono::steady_clock::time_point timeout,
        diagnostics* diag,
        CompletionToken&& token
    )
    {
        return asio::async_compose<CompletionToken, void(error_code, ConnectionWrapper)>(
            get_connection_op(shared_from_this_wrapper(), timeout, diag),
            token,
            ex_
        );
    }

    template <class CompletionToken>
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code, ConnectionWrapper))
    async_get_connection(
        std::chrono::steady_clock::duration timeout,
        diagnostics* diag,
        CompletionToken&& token
    )
    {
        return async_get_connection(
            timeout.count() > 0 ? std::chrono::steady_clock::now() + timeout
                                : (std::chrono::steady_clock::time_point::max)(),
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    // Exposed for testing
    std::list<node_type>& nodes() noexcept { return all_conns_; }
    shared_state_type& shared_state() noexcept { return shared_st_; }
    internal_pool_params& params() noexcept { return params_; }
    asio::any_io_executor connection_ex() noexcept { return conn_ex_; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
