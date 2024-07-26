//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_CONNECTION_POOL_HPP
#define BOOST_MYSQL_CONNECTION_POOL_HPP

#include <boost/mysql/any_connection.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/pool_params.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/connection_pool_fwd.hpp>

#include <boost/asio/any_completion_handler.hpp>
#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/async_result.hpp>

#include <chrono>
#include <memory>
#include <utility>

namespace boost {
namespace mysql {

/**
 * \brief (EXPERIMENTAL) A proxy to a connection owned by a pool that returns it to the pool when destroyed.
 * \details
 * A `pooled_connection` behaves like to a `std::unique_ptr`: it has exclusive ownership of an
 * \ref any_connection created by the pool. When destroyed, it returns the connection to the pool.
 * A `pooled_connection` may own nothing. We say such a connection is invalid (`this->valid() == false`).
 * \n
 * This class is movable but not copyable.
 *
 * \par Object lifetimes
 * While `*this` is alive, the \ref connection_pool internal data will be kept alive
 * automatically. It's safe to destroy the `connection_pool` object before `*this`.
 *
 * \par Thread safety
 * By default, individual connections created by the pool are **not** thread-safe,
 * even if the pool was created using \ref pool_executor_params::thread_safe.
 * \n
 * Distinct objects: safe. \n
 * Shared objects: unsafe. \n
 *
 * \par Experimental
 * This part of the API is experimental, and may change in successive
 * releases without previous notice.
 */
class pooled_connection
{
#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
    friend class detail::basic_pool_impl<detail::io_traits, pooled_connection>;
#endif

    detail::connection_node* impl_{nullptr};
    std::shared_ptr<detail::pool_impl> pool_impl_;

    pooled_connection(detail::connection_node& node, std::shared_ptr<detail::pool_impl> pool_impl) noexcept
        : impl_(&node), pool_impl_(std::move(pool_impl))
    {
    }

public:
    /**
     * \brief Constructs an invalid pooled connection.
     * \details
     * The resulting object is invalid (`this->valid() == false`).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    pooled_connection() noexcept = default;

    /**
     * \brief Move constructor.
     * \details
     * Transfers connection ownership from `other` to `*this`.
     * \n
     * After this function returns, if `other.valid() == true`, `this->valid() == true`.
     * In any case, `other` will become invalid (`other.valid() == false`).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    pooled_connection(pooled_connection&& other) noexcept
        : impl_(other.impl_), pool_impl_(std::move(other.pool_impl_))
    {
        other.impl_ = nullptr;
    }

    /**
     * \brief Move assignment.
     * \details
     * If `this->valid()`, returns the connection owned by `*this` to the pool and marks
     * it as pending reset (as if the destructor was called).
     * It then transfers connection ownership from `other` to `*this`.
     * \n
     * After this function returns, if `other.valid() == true`, `this->valid() == true`.
     * In any case, `other` will become invalid (`other.valid() == false`).
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    pooled_connection& operator=(pooled_connection&& other) noexcept
    {
        if (impl_)
            detail::return_connection(std::move(pool_impl_), *impl_, true);
        impl_ = other.impl_;
        other.impl_ = nullptr;
        pool_impl_ = std::move(other.pool_impl_);
        return *this;
    }

#ifndef BOOST_MYSQL_DOXYGEN
    pooled_connection(const pooled_connection&) = delete;
    pooled_connection& operator=(const pooled_connection&) = delete;
#endif

    /**
     * \brief Destructor.
     * \details
     * If `this->valid() == true`, returns the owned connection to the pool
     * and marks it as pending reset. If your connection doesn't need to be reset
     * (e.g. because you didn't mutate session state), use \ref return_without_reset.
     *
     * \par Thead-safety
     * If the \ref connection_pool object that `*this` references has been constructed
     * with adequate executor configuration, this function is safe to be called concurrently
     * with \ref connection_pool::async_run, \ref connection_pool::async_get_connection,
     * \ref connection_pool::cancel and \ref return_without_reset on other `pooled_connection` objects.
     */
    ~pooled_connection()
    {
        if (impl_)
            detail::return_connection(std::move(pool_impl_), *impl_, true);
    }

    /**
     * \brief Returns whether the object owns a connection or not.
     * \par Exception safety
     * No-throw guarantee.
     */
    bool valid() const noexcept { return impl_ != nullptr; }

    /**
     * \brief Retrieves the connection owned by this object.
     * \par Preconditions
     * The object should own a connection (`this->valid() == true`).
     *
     * \par Object lifetimes
     * The returned reference is valid as long as `*this` or an object
     * move-constructed or move-assigned from `*this` is alive.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    any_connection& get() noexcept { return detail::get_connection(*impl_); }

    /// \copydoc get
    const any_connection& get() const noexcept { return detail::get_connection(*impl_); }

    /// \copydoc get
    any_connection* operator->() noexcept { return &get(); }

    /// \copydoc get
    const any_connection* operator->() const noexcept { return &get(); }

    /**
     * \brief Returns the owned connection to the pool and marks it as not requiring reset.
     * \details
     * Returns a connection to the pool and marks it as idle. This will
     * skip the \ref any_connection::async_reset_connection call to wipe session state.
     * \n
     * This can provide a performance gain, but must be used with care. Failing to wipe
     * session state can lead to resource leaks (prepared statements not being released),
     * incorrect results and vulnerabilities (different logical operations interacting due
     * to leftover state).
     * \n
     * Please read the documentation on \ref any_connection::async_reset_connection before
     * calling this function. If in doubt, don't use it, and leave the destructor return
     * the connection to the pool for you.
     * \n
     * When this function returns, `*this` will own nothing (`this->valid() == false`).
     *
     * \par Preconditions
     * `this->valid() == true`
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Thead-safety
     * If the \ref connection_pool object that `*this` references has been constructed
     * with adequate executor configuration, this function is safe to be called concurrently
     * with \ref connection_pool::async_run, \ref connection_pool::async_get_connection,
     * \ref connection_pool::cancel and `~pooled_connection`.
     */
    void return_without_reset() noexcept
    {
        BOOST_ASSERT(valid());
        detail::return_connection(std::move(pool_impl_), *impl_, false);
        impl_ = nullptr;
    }
};

/**
 * \brief (EXPERIMENTAL) A pool of connections of variable size.
 * \details
 * A connection pool creates and manages \ref any_connection objects.
 * Using a pool allows to reuse sessions, avoiding part of the overhead associated
 * to session establishment. It also features built-in error handling and reconnection.
 * See the discussion and examples for more details on when to use this class.
 * \n
 * Connections are retrieved by \ref async_get_connection, which yields a
 * \ref pooled_connection object. They are returned to the pool when the
 * `pooled_connection` is destroyed, or by calling \ref pooled_connection::return_without_reset.
 * \n
 * A pool needs to be run before it can return any connection. Use \ref async_run for this.
 * Pools can only be run once.
 * \n
 * Connections are created, connected and managed internally by the pool, following
 * a well-defined state model. Please refer to the discussion for details.
 * \n
 * Due to oddities in Boost.Asio's universal async model, this class only
 * exposes async functions. You can use `asio::use_future` to transform them
 * into sync functions (please read the discussion for details).
 * \n
 * This is a move-only type.
 *
 * \par Thread-safety
 * By default, connection pools are *not* thread-safe, but most functions can
 * be made thread-safe by passing an adequate \ref pool_executor_params objects
 * to the constructor. See \ref pool_executor_params::thread_safe and the discussion
 * for details.
 * \n
 * Distinct objects: safe. \n
 * Shared objects: unsafe, unless passing adequate values to the constructor.
 *
 * \par Object lifetimes
 * Connection pool objects create an internal state object that is referenced
 * by other objects and operations (like \ref pooled_connection). This object
 * will be kept alive using shared ownership semantics even after the `connection_pool`
 * object is destroyed. This results in intuitive lifetime rules.
 *
 * \par Experimental
 * This part of the API is experimental, and may change in successive
 * releases without previous notice.
 */
class connection_pool
{
    std::shared_ptr<detail::pool_impl> impl_;

#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
#endif

    static constexpr std::chrono::steady_clock::duration get_default_timeout() noexcept
    {
        return std::chrono::seconds(30);
    }

    struct initiate_run
    {
        template <class Handler>
        void operator()(Handler&& h, std::shared_ptr<detail::pool_impl> self)
        {
            async_run_erased(std::move(self), std::forward<Handler>(h));
        }
    };

    BOOST_MYSQL_DECL
    static void async_run_erased(
        std::shared_ptr<detail::pool_impl> pool,
        asio::any_completion_handler<void(error_code)> handler
    );

    struct initiate_get_connection
    {
        template <class Handler>
        void operator()(
            Handler&& h,
            std::shared_ptr<detail::pool_impl> self,
            std::chrono::steady_clock::duration timeout,
            diagnostics* diag
        )
        {
            async_get_connection_erased(std::move(self), timeout, diag, std::forward<Handler>(h));
        }
    };

    BOOST_MYSQL_DECL
    static void async_get_connection_erased(
        std::shared_ptr<detail::pool_impl> pool,
        std::chrono::steady_clock::duration timeout,
        diagnostics* diag,
        asio::any_completion_handler<void(error_code, pooled_connection)> handler
    );

    template <class CompletionToken>
    auto async_get_connection_impl(
        std::chrono::steady_clock::duration timeout,
        diagnostics* diag,
        CompletionToken&& token
    )
        -> decltype(asio::async_initiate<CompletionToken, void(error_code, pooled_connection)>(
            initiate_get_connection{},
            token,
            impl_,
            timeout,
            diag
        ))
    {
        BOOST_ASSERT(valid());
        return asio::async_initiate<CompletionToken, void(error_code, pooled_connection)>(
            initiate_get_connection{},
            token,
            impl_,
            timeout,
            diag
        );
    }

    BOOST_MYSQL_DECL
    connection_pool(pool_executor_params&& ex_params, pool_params&& params, int);

public:
    /**
     * \brief Constructs a connection pool.
     * \details
     * Internal I/O objects (like timers) are constructed using
     * `ex_params.pool_executor`. Connections are constructed using
     * `ex_params.connection_executor`. This can be used to create
     * thread-safe pools.
     * \n
     * The pool is created in a "not-running" state. Call \ref async_run to transition to the
     * "running" state. Calling \ref async_get_connection in the "not-running" state will fail
     * with \ref client_errc::cancelled.
     * \n
     * The constructed pool is always valid (`this->valid() == true`).
     *
     * \par Exception safety
     * Strong guarantee. Exceptions may be thrown by memory allocations.
     * \throws std::invalid_argument If `params` contains values that violate the rules described in \ref
     *         pool_params.
     */
    connection_pool(pool_executor_params ex_params, pool_params params)
        : connection_pool(std::move(ex_params), std::move(params), 0)
    {
    }

    /**
     * \brief Constructs a connection pool.
     * \details
     * Both internal I/O objects and connections are constructed using the passed executor.
     * \n
     * The pool is created in a "not-running" state. Call \ref async_run to transition to the
     * "running" state. Calling \ref async_get_connection in the "not-running" state will fail
     * with \ref client_errc::cancelled.
     * \n
     * The constructed pool is always valid (`this->valid() == true`).
     *
     * \par Exception safety
     * Strong guarantee. Exceptions may be thrown by memory allocations.
     * \throws std::invalid_argument If `params` contains values that violate the rules described in \ref
     *         pool_params.
     */
    connection_pool(asio::any_io_executor ex, pool_params params)
        : connection_pool(pool_executor_params{ex, ex}, std::move(params), 0)
    {
    }

    /**
     * \brief Constructs a connection pool.
     * \details
     * Both internal I/O objects and connections are constructed using `ctx.get_executor()`.
     * \n
     * The pool is created in a "not-running" state. Call \ref async_run to transition to the
     * "running" state. Calling \ref async_get_connection in the "not-running" state will fail
     * with \ref client_errc::cancelled.
     * \n
     * The constructed pool is always valid (`this->valid() == true`).
     * \n
     * This function participates in overload resolution only if `ExecutionContext`
     * satisfies the `ExecutionContext` requirements imposed by Boost.Asio.
     *
     * \par Exception safety
     * Strong guarantee. Exceptions may be thrown by memory allocations.
     * \throws std::invalid_argument If `params` contains values that violate the rules described in \ref
     *         pool_params.
     */
    template <
        class ExecutionContext
#ifndef BOOST_MYSQL_DOXYGEN
        ,
        class = typename std::enable_if<std::is_convertible<
            decltype(std::declval<ExecutionContext&>().get_executor()),
            asio::any_io_executor>::value>::type
#endif
        >
    connection_pool(ExecutionContext& ctx, pool_params params)
        : connection_pool({ctx.get_executor(), ctx.get_executor()}, std::move(params), 0)
    {
    }

#ifndef BOOST_MYSQL_DOXYGEN
    connection_pool(const connection_pool&) = delete;
    connection_pool& operator=(const connection_pool&) = delete;
#endif

    /**
     * \brief Move-constructor.
     * \details
     * Constructs a connection pool by taking ownership of `other`.
     * \n
     * After this function returns, if `other.valid() == true`, `this->valid() == true`.
     * In any case, `other` will become invalid (`other.valid() == false`).
     * \n
     * Moving a connection pool with outstanding async operations
     * is safe.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Thead-safety
     * This function is never thread-safe, regardless of the executor
     * configuration passed to the constructor. Calling this function
     * concurrently with any other function introduces data races.
     */
    connection_pool(connection_pool&& other) = default;

    /**
     * \brief Move assignment.
     * \details
     * Assigns `other` to `*this`, transferring ownership.
     * \n
     * After this function returns, if `other.valid() == true`, `this->valid() == true`.
     * In any case, `other` will become invalid (`other.valid() == false`).
     * \n
     * Moving a connection pool with outstanding async operations
     * is safe.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Thead-safety
     * This function is never thread-safe, regardless of the executor
     * configuration passed to the constructor. Calling this function
     * concurrently with any other function introduces data races.
     */
    connection_pool& operator=(connection_pool&& other) = default;

    /// Destructor.
    ~connection_pool() = default;

    /**
     * \brief Returns whether the object is in a moved-from state.
     * \details
     * This function returns always `true` except for pools that have been
     * moved-from. Moved-from objects don't represent valid pools. They can only
     * be assigned to or destroyed.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Thead-safety
     * This function is never thread-safe, regardless of the executor
     * configuration passed to the constructor. Calling this function
     * concurrently with any other function introduces data races.
     */
    bool valid() const noexcept { return impl_.get() != nullptr; }

    /// The executor type associated to this object.
    using executor_type = asio::any_io_executor;

    /**
     * \brief Retrieves the executor associated to this object.
     * \details
     * Returns the pool executor passed to the constructor, as per
     * \ref pool_executor_params::pool_executor.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Thead-safety
     * This function is never thread-safe, regardless of the executor
     * configuration passed to the constructor. Calling this function
     * concurrently with any other function introduces data races.
     */
    BOOST_MYSQL_DECL
    executor_type get_executor() noexcept;

    /**
     * \brief Runs the pool task in charge of managing connections.
     * \details
     * This function creates and connects new connections, and resets and pings
     * already created ones. You need to call this function for \ref async_get_connection
     * to succeed.
     * \n
     * The async operation will run indefinitely, until the pool is cancelled
     * (by being destroyed or calling \ref cancel). The operation completes once
     * all internal connection operations (including connects, pings and resets)
     * complete.
     * \n
     * It is safe to call this function after calling \ref cancel.
     *
     * \par Preconditions
     * This function can be called at most once for a single pool.
     * Formally, `async_run` hasn't been called before on `*this` or any object
     * used to move-construct or move-assign `*this`.
     * \n
     * Additionally, `this->valid() == true`.
     *
     * \par Object lifetimes
     * While the operation is outstanding, the pool's internal data will be kept alive.
     * It is safe to destroy `*this` while the operation is outstanding.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`
     *
     * \par Errors
     * This function always complete successfully. The handler signature ensures
     * maximum compatibility with Boost.Asio infrastructure.
     *
     * \par Executor
     * This function will run entirely in the pool's executor (as given by `this->get_executor()`).
     * No internal data will be accessed or modified as part of the initiating function.
     * This simplifies thread-safety.
     *
     * \par Thead-safety
     * When the pool is constructed with adequate executor configuration, this function
     * is safe to be called concurrently with \ref async_get_connection, \ref cancel,
     * `~pooled_connection` and \ref pooled_connection::return_without_reset.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_run(CompletionToken&& token) BOOST_MYSQL_RETURN_TYPE(
        decltype(asio::async_initiate<CompletionToken, void(error_code)>(initiate_run{}, token, impl_))
    )
    {
        BOOST_ASSERT(valid());
        return asio::async_initiate<CompletionToken, void(error_code)>(initiate_run{}, token, impl_);
    }

    /// \copydoc async_get_connection(diagnostics&,CompletionToken&&)
    template <
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::pooled_connection))
            CompletionToken>
    auto async_get_connection(CompletionToken&& token) BOOST_MYSQL_RETURN_TYPE(
        decltype(async_get_connection_impl({}, nullptr, std::forward<CompletionToken>(token)))
    )
    {
        return async_get_connection_impl(
            get_default_timeout(),
            nullptr,
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Retrieves a connection from the pool.
     * \details
     * Retrieves an idle connection from the pool to be used.
     * \n
     * If this function completes successfully (empty error code), the return \ref pooled_connection
     * will have `valid() == true` and will be usable. If it completes with a non-empty error code,
     * it will have `valid() == false`.
     * \n
     * If a connection is idle when the operation is started, it will complete immediately
     * with that connection. Otherwise, it will wait for a connection to become idle
     * (possibly creating one in the process, if pool configuration allows it), up to
     * a duration of 30 seconds.
     * \n
     * If a timeout happens because connection establishment has failed, appropriate
     * diagnostics will be returned.
     *
     * \par Preconditions
     * `this->valid() == true` \n
     *
     * \par Object lifetimes
     * While the operation is outstanding, the pool's internal data will be kept alive.
     * It is safe to destroy `*this` while the operation is outstanding.
     *
     * \par Handler signature
     * The handler signature for this operation is
     * `void(boost::mysql::error_code, boost::mysql::pooled_connection)`
     *
     * \par Errors
     * \li Any error returned by \ref any_connection::async_connect, if a timeout
     *     happens because connection establishment is failing.
     * \li \ref client_errc::timeout, if a timeout happens for any other reason
     *     (e.g. all connections are in use and limits forbid creating more).
     * \li \ref client_errc::cancelled if \ref cancel was called before the operation is started or while
     *     it is outstanding, or if the pool is not running.
     *
     * \par Executor
     * This function will run entirely in the pool's executor (as given by `this->get_executor()`).
     * No internal data will be accessed or modified as part of the initiating function.
     * This simplifies thread-safety.
     *
     * \par Thead-safety
     * When the pool is constructed with adequate executor configuration, this function
     * is safe to be called concurrently with \ref async_run, \ref cancel,
     * `~pooled_connection` and \ref pooled_connection::return_without_reset.
     */
    template <
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::pooled_connection))
            CompletionToken>
    auto async_get_connection(diagnostics& diag, CompletionToken&& token) BOOST_MYSQL_RETURN_TYPE(
        decltype(async_get_connection_impl({}, nullptr, std::forward<CompletionToken>(token)))
    )
    {
        return async_get_connection_impl(get_default_timeout(), &diag, std::forward<CompletionToken>(token));
    }

    /// \copydoc async_get_connection(std::chrono::steady_clock::duration,diagnostics&,CompletionToken&&)
    template <
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::pooled_connection))
            CompletionToken>
    auto async_get_connection(std::chrono::steady_clock::duration timeout, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(
            decltype(async_get_connection_impl({}, nullptr, std::forward<CompletionToken>(token)))
        )
    {
        return async_get_connection_impl(timeout, nullptr, std::forward<CompletionToken>(token));
    }

    /**
     * \brief Retrieves a connection from the pool.
     * \details
     * Retrieves an idle connection from the pool to be used.
     * \n
     * If this function completes successfully (empty error code), the return \ref pooled_connection
     * will have `valid() == true` and will be usable. If it completes with a non-empty error code,
     * it will have `valid() == false`.
     * \n
     * If a connection is idle when the operation is started, it will complete immediately
     * with that connection. Otherwise, it will wait for a connection to become idle
     * (possibly creating one in the process, if pool configuration allows it), up to
     * a duration of `timeout`. A zero timeout disables it.
     * \n
     * If a timeout happens because connection establishment has failed, appropriate
     * diagnostics will be returned.
     *
     * \par Preconditions
     * `this->valid() == true` \n
     * Timeout values must be positive: `timeout.count() >= 0`.
     *
     * \par Object lifetimes
     * While the operation is outstanding, the pool's internal data will be kept alive.
     * It is safe to destroy `*this` while the operation is outstanding.
     *
     * \par Handler signature
     * The handler signature for this operation is
     * `void(boost::mysql::error_code, boost::mysql::pooled_connection)`
     *
     * \par Errors
     * \li Any error returned by \ref any_connection::async_connect, if a timeout
     *     happens because connection establishment is failing.
     * \li \ref client_errc::timeout, if a timeout happens for any other reason
     *     (e.g. all connections are in use and limits forbid creating more).
     * \li \ref client_errc::cancelled if \ref cancel was called before the operation is started or while
     *     it is outstanding, or if the pool is not running.
     *
     * \par Executor
     * This function will run entirely in the pool's executor (as given by `this->get_executor()`).
     * No internal data will be accessed or modified as part of the initiating function.
     * This simplifies thread-safety.
     *
     * \par Thead-safety
     * When the pool is constructed with adequate executor configuration, this function
     * is safe to be called concurrently with \ref async_run, \ref cancel,
     * `~pooled_connection` and \ref pooled_connection::return_without_reset.
     */
    template <
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::pooled_connection))
            CompletionToken>
    auto async_get_connection(
        std::chrono::steady_clock::duration timeout,
        diagnostics& diag,
        CompletionToken&& token
    )
        BOOST_MYSQL_RETURN_TYPE(
            decltype(async_get_connection_impl({}, nullptr, std::forward<CompletionToken>(token)))
        )
    {
        return async_get_connection_impl(timeout, &diag, std::forward<CompletionToken>(token));
    }

    /**
     * \brief Stops any current outstanding operation and marks the pool as cancelled.
     * \details
     * This function has the following effects:
     * \n
     * \li Stops the currently outstanding \ref async_run operation, if any, which will complete
     *     with a success error code.
     * \li Cancels any outstanding \ref async_get_connection operations, which will complete with
     *     \ref client_errc::cancelled.
     * \li Marks the pool as cancelled. Successive `async_get_connection` calls will complete
     *     immediately with \ref client_errc::cancelled.
     * \n
     * This function will return immediately, without waiting for the cancelled operations to complete.
     * \n
     * You may call this function any number of times. Successive calls will have no effect.
     *
     * \par Preconditions
     * `this->valid() == true`
     *
     * \par Exception safety
     * Basic guarantee. Memory allocations and acquiring mutexes may throw.
     *
     * \par Thead-safety
     * When the pool is constructed with adequate executor configuration, this function
     * is safe to be called concurrently with \ref async_run, \ref async_get_connection,
     * `~pooled_connection` and \ref pooled_connection::return_without_reset.
     */
    BOOST_MYSQL_DECL
    void cancel();
};

}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/connection_pool.ipp>
#endif

#endif
