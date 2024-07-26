//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_CONNECTION_HPP
#define BOOST_MYSQL_CONNECTION_HPP

#include <boost/mysql/buffer_params.hpp>
#include <boost/mysql/defaults.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/execution_state.hpp>
#include <boost/mysql/handshake_params.hpp>
#include <boost/mysql/metadata_mode.hpp>
#include <boost/mysql/results.hpp>
#include <boost/mysql/rows_view.hpp>
#include <boost/mysql/statement.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/algo_params.hpp>
#include <boost/mysql/detail/any_stream.hpp>
#include <boost/mysql/detail/any_stream_impl.hpp>
#include <boost/mysql/detail/connection_impl.hpp>
#include <boost/mysql/detail/execution_concepts.hpp>
#include <boost/mysql/detail/rebind_executor.hpp>
#include <boost/mysql/detail/socket_stream.hpp>
#include <boost/mysql/detail/throw_on_error_loc.hpp>
#include <boost/mysql/detail/writable_field_traits.hpp>

#include <boost/assert.hpp>

#include <memory>
#include <type_traits>
#include <utility>

/// The Boost libraries namespace.
namespace boost {
/// Boost.MySQL library namespace.
namespace mysql {

// Forward declarations
template <class... StaticRow>
class static_execution_state;

/**
 * \brief A connection to a MySQL server.
 * \details
 * Represents a connection to a MySQL server.
 *\n
 * `connection` is the main I/O object that this library implements. It owns a `Stream` object that
 * is accessed by functions involving network operations, as well as session state. You can access
 * the stream using \ref connection::stream, and its executor via \ref connection::get_executor. The
 * executor used by this object is always the same as the underlying stream.
 *\n
 * \par Thread safety
 * Distinct objects: safe. \n
 * Shared objects: unsafe. \n
 * This class is <b>not thread-safe</b>: for a single object, if you
 * call its member functions concurrently from separate threads, you will get a race condition.
 */
template <class Stream>
class connection
{
    detail::connection_impl impl_;

public:
    /**
     * \brief Initializing constructor.
     * \details
     * As part of the initialization, an internal `Stream` object is created.
     *
     * \par Exception safety
     * Basic guarantee. Throws if the `Stream` constructor throws
     * or if memory allocation for internal state fails.
     *
     * \param args Arguments to be forwarded to the `Stream` constructor.
     */
    template <
        class... Args,
        class EnableIf = typename std::enable_if<std::is_constructible<Stream, Args...>::value>::type>
    connection(Args&&... args) : connection(buffer_params(), std::forward<Args>(args)...)
    {
    }

    /**
     * \brief Initializing constructor with buffer params.
     * \details
     * As part of the initialization, an internal `Stream` object is created.
     *
     * \par Exception safety
     * Basic guarantee. Throws if the `Stream` constructor throws
     * or if memory allocation for internal state fails.
     *
     * \param buff_params Specifies initial sizes for internal buffers.
     * \param args Arguments to be forwarded to the `Stream` constructor.
     */
    template <
        class... Args,
        class EnableIf = typename std::enable_if<std::is_constructible<Stream, Args...>::value>::type>
    connection(const buffer_params& buff_params, Args&&... args)
        : impl_(buff_params.initial_read_size(), detail::make_stream<Stream>(std::forward<Args>(args)...))
    {
    }

    /**
     * \brief Move constructor.
     */
    connection(connection&& other) = default;

    /**
     * \brief Move assignment.
     */
    connection& operator=(connection&& rhs) = default;

#ifndef BOOST_MYSQL_DOXYGEN
    connection(const connection&) = delete;
    connection& operator=(const connection&) = delete;
#endif

    /// The executor type associated to this object.
    using executor_type = typename Stream::executor_type;

    /// Retrieves the executor associated to this object.
    executor_type get_executor() { return stream().get_executor(); }

    /// The `Stream` type this connection is using.
    using stream_type = Stream;

    /**
     * \brief Retrieves the underlying Stream object.
     * \details
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    Stream& stream() noexcept { return detail::cast<Stream>(impl_.stream()); }

    /**
     * \brief Retrieves the underlying Stream object.
     * \details
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    const Stream& stream() const noexcept { return detail::cast<Stream>(impl_.stream()); }

    /**
     * \brief Returns whether the connection negotiated the use of SSL or not.
     * \details
     * This function can be used to determine whether you are using a SSL
     * connection or not when using SSL negotiation.
     * \n
     * This function always returns `false` if the underlying
     * stream does not support SSL. This function always returns `false`
     * for connections that haven't been
     * established yet (handshake not run yet). If the handshake fails,
     * the return value is undefined.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \returns Whether the connection is using SSL.
     */
    bool uses_ssl() const noexcept { return impl_.ssl_active(); }

    /**
     * \brief Returns the current metadata mode that this connection is using.
     * \details
     * \par Exception safety
     * No-throw guarantee.
     *
     * \returns The matadata mode that will be used for queries and statement executions.
     */
    metadata_mode meta_mode() const noexcept { return impl_.meta_mode(); }

    /**
     * \brief Sets the metadata mode.
     * \details
     * Will affect any query and statement executions performed after the call.
     *
     * \par Exception safety
     * No-throw guarantee.
     *
     * \par Preconditions
     * No asynchronous operation should be outstanding when this function is called.
     *
     * \param v The new metadata mode.
     */
    void set_meta_mode(metadata_mode v) noexcept { impl_.set_meta_mode(v); }

    /**
     * \brief Establishes a connection to a MySQL server.
     * \details
     * This function is only available if `Stream` satisfies the
     * `SocketStream` concept.
     * \n
     * Connects the underlying stream and performs the handshake
     * with the server. The underlying stream is closed in case of error. Prefer
     * this function to \ref connection::handshake.
     * \n
     * If using a SSL-capable stream, the SSL handshake will be performed by this function.
     * \n
     * `endpoint` should be convertible to `Stream::lowest_layer_type::endpoint_type`.
     */
    template <typename EndpointType>
    void connect(
        const EndpointType& endpoint,
        const handshake_params& params,
        error_code& ec,
        diagnostics& diag
    )
    {
        static_assert(
            detail::is_socket_stream<Stream>::value,
            "connect can only be used if Stream satisfies the SocketStream concept"
        );
        impl_.connect<typename Stream::lowest_layer_type::endpoint_type>(endpoint, params, ec, diag);
    }

    /// \copydoc connect
    template <typename EndpointType>
    void connect(const EndpointType& endpoint, const handshake_params& params)
    {
        static_assert(
            detail::is_socket_stream<Stream>::value,
            "connect can only be used if Stream satisfies the SocketStream concept"
        );
        error_code err;
        diagnostics diag;
        connect(endpoint, params, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc connect
     * \par Object lifetimes
     * The strings pointed to by `params` should be kept alive by the caller
     * until the operation completes, as no copy is made by the library.
     * `endpoint` is copied as required and doesn't need to be kept alive.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <
        typename EndpointType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code))
    async_connect(
        const EndpointType& endpoint,
        const handshake_params& params,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        static_assert(
            detail::is_socket_stream<Stream>::value,
            "async_connect can only be used if Stream satisfies the SocketStream concept"
        );
        return async_connect(endpoint, params, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_connect
    template <
        typename EndpointType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code))
    async_connect(
        const EndpointType& endpoint,
        const handshake_params& params,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        static_assert(
            detail::is_socket_stream<Stream>::value,
            "async_connect can only be used if Stream satisfies the SocketStream concept"
        );
        return impl_.async_connect<typename Stream::lowest_layer_type::endpoint_type>(
            endpoint,
            params,
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Performs the MySQL-level handshake.
     * \details
     * Does not connect the underlying stream.
     * If the `Stream` template parameter fulfills the `SocketConnection`
     * requirements, use \ref connection::connect instead of this function.
     * \n
     * If using a SSL-capable stream, the SSL handshake will be performed by this function.
     */
    void handshake(const handshake_params& params, error_code& ec, diagnostics& diag)
    {
        impl_.run(impl_.make_params_handshake(params, diag), ec);
    }

    /// \copydoc handshake
    void handshake(const handshake_params& params)
    {
        error_code err;
        diagnostics diag;
        handshake(params, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc handshake
     * \par Object lifetimes
     * The strings pointed to by `params` should be kept alive by the caller
     * until the operation completes, as no copy is made by the library.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_handshake(
        const handshake_params& params,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_handshake_t<CompletionToken&&>)
    {
        return async_handshake(params, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_handshake
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_handshake(
        const handshake_params& params,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_handshake_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_handshake(params, diag),
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Executes a text query or prepared statement.
     * \details
     * Sends `req` to the server for execution and reads the response into `result`.
     * `result` may be either a \ref results or \ref static_results object.
     * `req` should may be either a type convertible to \ref string_view containing valid SQL
     * or a bound prepared statement, obtained by calling \ref statement::bind.
     * If a string, it must be encoded using the connection's character set.
     * Any string parameters provided to \ref statement::bind should also be encoded
     * using the connection's character set.
     * \n
     * After this operation completes successfully, `result.has_value() == true`.
     * \n
     * Metadata in `result` will be populated according to `this->meta_mode()`.
     */
    template <BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest, BOOST_MYSQL_RESULTS_TYPE ResultsType>
    void execute(const ExecutionRequest& req, ResultsType& result, error_code& err, diagnostics& diag)
    {
        impl_.execute(req, result, err, diag);
    }

    /// \copydoc execute
    template <BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest, BOOST_MYSQL_RESULTS_TYPE ResultsType>
    void execute(const ExecutionRequest& req, ResultsType& result)
    {
        error_code err;
        diagnostics diag;
        execute(req, result, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc execute
     * \par Object lifetimes
     * If `CompletionToken` is a deferred completion token (e.g. `use_awaitable`), the caller is
     * responsible for managing `req`'s validity following these rules:
     * \n
     * \li If `req` is `string_view`, the string pointed to by `req`
     *     must be kept alive by the caller until the operation is initiated.
     * \li If `req` is a \ref bound_statement_tuple, and any of the parameters is a reference
     *     type (like `string_view`), the caller must keep the values pointed by these references alive
     *     until the operation is initiated.
     * \li If `req` is a \ref bound_statement_iterator_range, the caller must keep objects in
     *     the iterator range passed to \ref statement::bind alive until the  operation is initiated.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <
        BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest,
        BOOST_MYSQL_RESULTS_TYPE ResultsType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_execute(
        ExecutionRequest&& req,
        ResultsType& result,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_execute_t<ExecutionRequest&&, ResultsType, CompletionToken&&>)
    {
        return async_execute(
            std::forward<ExecutionRequest>(req),
            result,
            impl_.shared_diag(),
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc async_execute
    template <
        BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest,
        BOOST_MYSQL_RESULTS_TYPE ResultsType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_execute(
        ExecutionRequest&& req,
        ResultsType& result,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_execute_t<ExecutionRequest&&, ResultsType, CompletionToken&&>)
    {
        return impl_.async_execute(
            std::forward<ExecutionRequest>(req),
            result,
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Starts a SQL execution as a multi-function operation.
     * \details
     * Writes the execution request and reads the initial server response and the column
     * metadata, but not the generated rows or subsequent resultsets, if any.
     * `st` may be either an \ref execution_state or \ref static_execution_state object.
     * \n
     * After this operation completes, `st` will have
     * \ref execution_state::meta populated.
     * Metadata will be populated according to `this->meta_mode()`.
     * \n
     * If the operation generated any rows or more than one resultset, these <b>must</b> be read (by using
     * \ref read_some_rows and \ref read_resultset_head) before engaging in any further network operation.
     * Otherwise, the results are undefined.
     * \n
     * req may be either a type convertible to \ref string_view containing valid SQL
     * or a bound prepared statement, obtained by calling \ref statement::bind.
     * If a string, it must be encoded using the connection's character set.
     * Any string parameters provided to \ref statement::bind should also be encoded
     * using the connection's character set.
     * \n
     * When using the static interface, this function will detect schema mismatches for the first
     * resultset. Further errors may be detected by \ref read_resultset_head and \ref read_some_rows.
     */
    template <
        BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest,
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType>
    void start_execution(
        const ExecutionRequest& req,
        ExecutionStateType& st,
        error_code& err,
        diagnostics& diag
    )
    {
        impl_.start_execution(req, st, err, diag);
    }

    /// \copydoc start_execution
    template <
        BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest,
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType>
    void start_execution(const ExecutionRequest& req, ExecutionStateType& st)
    {
        error_code err;
        diagnostics diag;
        start_execution(req, st, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc start_execution
     * \par Object lifetimes
     * If `CompletionToken` is a deferred completion token (e.g. `use_awaitable`), the caller is
     * responsible for managing `req`'s validity following these rules:
     * \n
     * \li If `req` is `string_view`, the string pointed to by `req`
     *     must be kept alive by the caller until the operation is initiated.
     * \li If `req` is a \ref bound_statement_tuple, and any of the parameters is a reference
     *     type (like `string_view`), the caller must keep the values pointed by these references alive
     *     until the operation is initiated.
     * \li If `req` is a \ref bound_statement_iterator_range, the caller must keep objects in
     *     the iterator range passed to \ref statement::bind alive until the  operation is initiated.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <
        BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest,
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_start_execution(
        ExecutionRequest&& req,
        ExecutionStateType& st,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
        BOOST_MYSQL_RETURN_TYPE(detail::async_start_execution_t<
                                ExecutionRequest&&,
                                ExecutionStateType,
                                CompletionToken&&>)
    {
        return async_start_execution(
            std::forward<ExecutionRequest>(req),
            st,
            impl_.shared_diag(),
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc async_start_execution
    template <
        BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest,
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_start_execution(
        ExecutionRequest&& req,
        ExecutionStateType& st,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
        BOOST_MYSQL_RETURN_TYPE(detail::async_start_execution_t<
                                ExecutionRequest&&,
                                ExecutionStateType,
                                CompletionToken&&>)
    {
        return impl_.async_start_execution(
            std::forward<ExecutionRequest>(req),
            st,
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief (Deprecated - will be removed in Boost 1.86) Executes a SQL text query.
     * \details
     * Sends `query_string` to the server for execution and reads the response into `result`.
     * query_string should be encoded using the connection's character set.
     * \n
     * After this operation completes successfully, `result.has_value() == true`.
     * \n
     * Metadata in `result` will be populated according to `this->meta_mode()`.
     * \n
     * \par Security
     * If you compose `query_string` by concatenating strings manually, <b>your code is
     * vulnerable to SQL injection attacks</b>. If your query contains patameters unknown at
     * compile time, use prepared statements instead of this function.
     *
     * \par Deprecation warning
     * This function is deprecated and will be removed in Boost 1.86. Please
     * use \ref execute or \ref async_execute instead.
     */
    BOOST_DEPRECATED("query is deprecated and will be removed in Boost 1.86. Use execute, instead")
    void query(string_view query_string, results& result, error_code& err, diagnostics& diag)
    {
        execute(query_string, result, err, diag);
    }

    /// \copydoc query
    BOOST_DEPRECATED("query is deprecated and will be removed in Boost 1.86. Use execute, instead")
    void query(string_view query_string, results& result) { execute(query_string, result); }

    /**
     * \copydoc query
     * \details
     * \par Object lifetimes
     * If `CompletionToken` is a deferred completion token (e.g. `use_awaitable`), the string
     * pointed to by `query_string` must be kept alive by the caller until the operation is
     * initiated.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_DEPRECATED("async_query is deprecated and will be removed in Boost 1.86. Use async_execute, instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_query(
        string_view query_string,
        results& result,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_query(query_string, result, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_query
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_DEPRECATED("async_query is deprecated and will be removed in Boost 1.86. Use async_execute, instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_query(
        string_view query_string,
        results& result,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_execute(query_string, result, diag, std::forward<CompletionToken>(token));
    }

    /**
     * \brief (Deprecated - will be removed in Boost 1.86) Starts a text query as a multi-function operation.
     * \details
     * Writes the query request and reads the initial server response and the column
     * metadata, but not the generated rows or subsequent resultsets, if any.
     * After this operation completes, `st` will have
     * \ref execution_state::meta populated.
     * Metadata will be populated according to `this->meta_mode()`.
     * \n
     * If the operation generated any rows or more than one resultset, these <b>must</b> be read (by using
     * \ref read_some_rows and \ref read_resultset_head) before engaging in any further network operation.
     * Otherwise, the results are undefined.
     * \n
     * `query_string` should be encoded using the connection's character set.
     *
     * \par Deprecation warning
     * This function is deprecated and will be removed in Boost 1.86. Please
     * use \ref execute or \ref async_execute instead.
     */
    BOOST_DEPRECATED(
        "start_query is deprecated and will be removed in Boost 1.86. Use start_execution, instead"
    )
    void start_query(string_view query_string, execution_state& st, error_code& err, diagnostics& diag)
    {
        start_execution(query_string, st, err, diag);
    }

    /// \copydoc start_query
    BOOST_DEPRECATED(
        "start_query is deprecated and will be removed in Boost 1.86. Use start_execution, instead"
    )
    void start_query(string_view query_string, execution_state& st) { start_execution(query_string, st); }

    /**
     * \copydoc start_query
     * \details
     * \par Object lifetimes
     * If `CompletionToken` is a deferred completion token (e.g. `use_awaitable`), the string
     * pointed to by `query_string` must be kept alive by the caller until the operation is
     * initiated.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_DEPRECATED(
        "async_start_query is deprecated and will be removed in Boost 1.86. Use async_start_execution, "
        "instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_start_query(
        string_view query_string,
        execution_state& st,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_start_query(query_string, st, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_start_query
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_DEPRECATED(
        "async_start_query is deprecated and will be removed in Boost 1.86. Use async_start_execution, "
        "instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_start_query(
        string_view query_string,
        execution_state& st,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_start_execution(query_string, st, diag, std::forward<CompletionToken>(token));
    }

    /**
     * \brief Prepares a statement server-side.
     * \details
     * `stmt` should be encoded using the connection's character set.
     * \n
     * The returned statement has `valid() == true`.
     */
    statement prepare_statement(string_view stmt, error_code& err, diagnostics& diag)
    {
        return impl_.run(detail::prepare_statement_algo_params{&diag, stmt}, err);
    }

    /// \copydoc prepare_statement
    statement prepare_statement(string_view stmt)
    {
        error_code err;
        diagnostics diag;
        statement res = prepare_statement(stmt, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
        return res;
    }

    /**
     * \copydoc prepare_statement
     * \details
     * \par Object lifetimes
     * If `CompletionToken` is a deferred completion token (e.g. `use_awaitable`), the string
     * pointed to by `stmt` must be kept alive by the caller until the operation is
     * initiated.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code, boost::mysql::statement)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::statement))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_prepare_statement(
        string_view stmt,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_prepare_statement_t<CompletionToken&&>)
    {
        return async_prepare_statement(stmt, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_prepare_statement
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::statement))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_prepare_statement(
        string_view stmt,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_prepare_statement_t<CompletionToken&&>)
    {
        return impl_.async_run(
            detail::prepare_statement_algo_params{&diag, stmt},
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief (Deprecated - will be removed in Boost 1.86) Executes a prepared statement.
     * \details
     * Executes a statement with the given parameters and reads the response into `result`.
     * \n
     * After this operation completes successfully, `result.has_value() == true`.
     * \n
     * The statement actual parameters (`params`) are passed as a `std::tuple` of elements.
     * See the `WritableFieldTuple` concept defition for more info. You should pass exactly as many
     * parameters as `this->num_params()`, or the operation will fail with an error.
     * String parameters should be encoded using the connection's character set.
     * \n
     * Metadata in `result` will be populated according to `conn.meta_mode()`, where `conn`
     * is the connection that prepared this statement.
     *
     * \par Deprecation warning
     * This function is deprecated and will be removed in Boost 1.86. Please
     * use \ref execute or \ref async_execute instead.
     *
     * \par Preconditions
     *    `stmt.valid() == true`
     */
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        class EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    BOOST_DEPRECATED("execute_statement is deprecated and will be removed in Boost 1.86. Use execute, instead"
    )
    void execute_statement(
        const statement& stmt,
        const WritableFieldTuple& params,
        results& result,
        error_code& err,
        diagnostics& diag
    )
    {
        execute(stmt.bind(params), result, err, diag);
    }

    /// \copydoc execute_statement
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        class EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    BOOST_DEPRECATED("execute_statement is deprecated and will be removed in Boost 1.86. Use execute, instead"
    )
    void execute_statement(const statement& stmt, const WritableFieldTuple& params, results& result)
    {
        execute(stmt.bind(params), result);
    }

    /**
     * \copydoc execute_statement
     * \par Object lifetimes
     * If `CompletionToken` is deferred (like `use_awaitable`), and `params` contains any reference
     * type (like `string_view`), the caller must keep the values pointed by these references alive
     * until the operation is initiated. Value types will be copied/moved as required, so don't need
     * to be kept alive. It's not required to keep `stmt` alive, either.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type),
        class EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    BOOST_DEPRECATED(
        "async_execute_statement is deprecated and will be removed in Boost 1.86. Use async_execute, instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_execute_statement(
        const statement& stmt,
        WritableFieldTuple&& params,
        results& result,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_execute_statement(
            stmt,
            std::forward<WritableFieldTuple>(params),
            result,
            impl_.shared_diag(),
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc async_execute_statement
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type),
        class EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    BOOST_DEPRECATED(
        "async_execute_statement is deprecated and will be removed in Boost 1.86. Use async_execute, instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_execute_statement(
        const statement& stmt,
        WritableFieldTuple&& params,
        results& result,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_execute(
            stmt.bind(std::forward<WritableFieldTuple>(params)),
            result,
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief (Deprecated - will be removed in Boost 1.86) Starts a statement execution
     *        as a multi-function operation.
     * \details
     * Writes the execute request and reads the initial server response and the column
     * metadata, but not the generated rows or subsequent resultsets, if any. After this operation completes,
     * `st` will have \ref execution_state::meta populated. Metadata will be populated according to
     * `this->meta_mode()`.
     * \n
     * If the operation generated any rows or more than one resultset, these <b>must</b> be read (by using
     * \ref read_some_rows and \ref read_resultset_head) before engaging in any further network operation.
     * Otherwise, the results are undefined.
     * \n
     * The statement actual parameters (`params`) are passed as a `std::tuple` of elements.
     * String parameters should be encoded using the connection's character set.
     *
     * \par Deprecation warning
     * This function is deprecated and will be removed in Boost 1.86. Please
     * use \ref execute or \ref async_execute instead.
     *
     * \par Preconditions
     *    `stmt.valid() == true`
     */
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        class EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    BOOST_DEPRECATED(
        "start_statement_execution is deprecated and will be removed in Boost 1.86. Use start_execution, "
        "instead"
    )
    void start_statement_execution(
        const statement& stmt,
        const WritableFieldTuple& params,
        execution_state& st,
        error_code& err,
        diagnostics& diag
    )
    {
        start_execution(stmt.bind(params), st, err, diag);
    }

    /// \copydoc start_statement_execution(const statement&,const WritableFieldTuple&,execution_state&,error_code&,diagnostics&)
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        class EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    BOOST_DEPRECATED(
        "start_statement_execution is deprecated and will be removed in Boost 1.86. Use start_execution, "
        "instead"
    )
    void start_statement_execution(
        const statement& stmt,
        const WritableFieldTuple& params,
        execution_state& st
    )
    {
        start_execution(stmt.bind(params), st);
    }

    /**
     * \copydoc start_statement_execution(const statement&,const WritableFieldTuple&,execution_state&,error_code&,diagnostics&)
     * \details
     * \par Object lifetimes
     * If `CompletionToken` is deferred (like `use_awaitable`), and `params` contains any reference
     * type (like `string_view`), the caller must keep the values pointed by these references alive
     * until the operation is initiated. Value types will be copied/moved as required, so don't need
     * to be kept alive. It's not required to keep `stmt` alive, either.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type),
        class EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    BOOST_DEPRECATED(
        "async_start_statement_execution is deprecated and will be removed in Boost 1.86. Use "
        "async_start_execution, instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_start_statement_execution(
        const statement& stmt,
        WritableFieldTuple&& params,
        execution_state& st,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_start_statement_execution(
            stmt,
            std::forward<WritableFieldTuple>(params),
            st,
            impl_.shared_diag(),
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc async_start_statement_execution(const statement&,WritableFieldTuple&&,execution_state&,CompletionToken&&)
    template <
        BOOST_MYSQL_WRITABLE_FIELD_TUPLE WritableFieldTuple,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type),
        class EnableIf =
            typename std::enable_if<detail::is_writable_field_tuple<WritableFieldTuple>::value>::type>
    BOOST_DEPRECATED(
        "async_start_statement_execution is deprecated and will be removed in Boost 1.86. Use "
        "async_start_execution, instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_start_statement_execution(
        const statement& stmt,
        WritableFieldTuple&& params,
        execution_state& st,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_start_execution(
            stmt.bind(std::forward<WritableFieldTuple>(params)),
            st,
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief (Deprecated - will be removed in Boost 1.86) Starts a statement execution as a multi-function
     *        operation.
     *
     * \details
     * Writes the execute request and reads the initial server response and the column
     * metadata, but not the generated rows or any subsequent resultsets, if any. After this operation
     * completes, `st` will have \ref execution_state::meta populated.
     * \n
     * If the operation generated any rows or more than one resultset, these <b>must</b> be read (by using
     * \ref read_some_rows and \ref read_resultset_head) before engaging in any further network operation.
     * Otherwise, the results are undefined.
     * \n
     * The statement actual parameters are passed as an iterator range.
     * String parameters should be encoded using the connection's character set.
     *
     * \par Deprecation warning
     * This function is deprecated and will be removed in Boost 1.86. Please
     * use \ref execute or \ref async_execute instead.
     *
     * \par Preconditions
     *    `stmt.valid() == true`
     */
    template <BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR FieldViewFwdIterator>
    BOOST_DEPRECATED(
        "start_statement_execution is deprecated and will be removed in Boost 1.86. Use start_execution, "
        "instead"
    )
    void start_statement_execution(
        const statement& stmt,
        FieldViewFwdIterator params_first,
        FieldViewFwdIterator params_last,
        execution_state& st,
        error_code& ec,
        diagnostics& diag
    )
    {
        start_execution(stmt.bind(params_first, params_last), st, ec, diag);
    }

    /// \copydoc start_statement_execution(const statement&,FieldViewFwdIterator,FieldViewFwdIterator,execution_state&,error_code&,diagnostics&)
    template <BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR FieldViewFwdIterator>
    BOOST_DEPRECATED(
        "start_statement_execution is deprecated and will be removed in Boost 1.86. Use start_execution, "
        "instead"
    )
    void start_statement_execution(
        const statement& stmt,
        FieldViewFwdIterator params_first,
        FieldViewFwdIterator params_last,
        execution_state& st
    )
    {
        start_execution(stmt.bind(params_first, params_last), st);
    }

    /**
     * \copydoc start_statement_execution(const statement&,FieldViewFwdIterator,FieldViewFwdIterator,execution_state&,error_code&,diagnostics&)
     * \details
     * \par Object lifetimes
     * If `CompletionToken` is deferred (like `use_awaitable`), the caller must keep objects in
     * the iterator range alive until the  operation is initiated.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <
        BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR FieldViewFwdIterator,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_DEPRECATED(
        "async_start_statement_execution is deprecated and will be removed in Boost 1.86. Use "
        "async_start_execution, instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_start_statement_execution(
        const statement& stmt,
        FieldViewFwdIterator params_first,
        FieldViewFwdIterator params_last,
        execution_state& st,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_start_statement_execution(
            stmt,
            params_first,
            params_last,
            st,
            impl_.shared_diag(),
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc async_start_statement_execution(const statement&,FieldViewFwdIterator,FieldViewFwdIterator,execution_state&,CompletionToken&&)
    template <
        BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR FieldViewFwdIterator,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_DEPRECATED(
        "async_start_statement_execution is deprecated and will be removed in Boost 1.86. Use "
        "async_start_execution, instead"
    )
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code)) async_start_statement_execution(
        const statement& stmt,
        FieldViewFwdIterator params_first,
        FieldViewFwdIterator params_last,
        execution_state& st,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_start_execution(
            stmt.bind(params_first, params_last),
            st,
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Closes a statement, deallocating it from the server.
     * \details
     * After this operation succeeds, `stmt` must not be used again for execution.
     * \par Performance warning
     * This function is currently affected by a peformance issue described
     * in https://github.com/boostorg/mysql/issues/181. Consider using
     * \ref reset_connection or \ref async_reset_connection instead.
     * \n
     * \par Preconditions
     *    `stmt.valid() == true`
     */
    void close_statement(const statement& stmt, error_code& err, diagnostics& diag)
    {
        impl_.run(impl_.make_params_close_statement(stmt, diag), err);
    }

    /// \copydoc close_statement
    void close_statement(const statement& stmt)
    {
        error_code err;
        diagnostics diag;
        close_statement(stmt, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc close_statement
     * \details
     * \par Object lifetimes
     * It is not required to keep `stmt` alive, as copies are made by the implementation as required.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_close_statement(
        const statement& stmt,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_close_statement_t<CompletionToken&&>)
    {
        return async_close_statement(stmt, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_close_statement
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_close_statement(
        const statement& stmt,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_close_statement_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_close_statement(stmt, diag),
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Reads a batch of rows.
     * \details
     * The number of rows that will be read is unspecified. If the operation represented by `st`
     * has still rows to read, at least one will be read. If there are no more rows, or
     * `st.should_read_rows() == false`, returns an empty `rows_view`.
     * \n
     * The number of rows that will be read depends on the input buffer size. The bigger the buffer,
     * the greater the batch size (up to a maximum). You can set the initial buffer size in the
     * constructor. The buffer may be
     * grown bigger by other read operations, if required.
     * \n
     * The returned view points into memory owned by `*this`. It will be valid until
     * `*this` performs the next network operation or is destroyed.
     */
    rows_view read_some_rows(execution_state& st, error_code& err, diagnostics& diag)
    {
        return impl_.run(impl_.make_params_read_some_rows(st, diag), err);
    }

    /// \copydoc read_some_rows(execution_state&,error_code&,diagnostics&)
    rows_view read_some_rows(execution_state& st)
    {
        error_code err;
        diagnostics diag;
        rows_view res = read_some_rows(st, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
        return res;
    }

    /**
     * \copydoc read_some_rows(execution_state&,error_code&,diagnostics&)
     * \details
     * \par Handler signature
     * The handler signature for this operation is
     * `void(boost::mysql::error_code, boost::mysql::rows_view)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::rows_view))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_read_some_rows(
        execution_state& st,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_read_some_rows_dynamic_t<CompletionToken&&>)
    {
        return async_read_some_rows(st, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_read_some_rows(execution_state&,CompletionToken&&)
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::rows_view))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_read_some_rows(
        execution_state& st,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_read_some_rows_dynamic_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_read_some_rows(st, diag),
            std::forward<CompletionToken>(token)
        );
    }

#ifdef BOOST_MYSQL_CXX14

    /**
     * \brief Reads a batch of rows.
     * \details
     * Reads a batch of rows of unspecified size into the storage given by `output`.
     * At most `output.size()` rows will be read. If the operation represented by `st`
     * has still rows to read, and `output.size() > 0`, at least one row will be read.
     * \n
     * Returns the number of read rows.
     * \n
     * If there are no more rows, or `st.should_read_rows() == false`, this function is a no-op and returns
     * zero.
     * \n
     * The number of rows that will be read depends on the input buffer size. The bigger the buffer,
     * the greater the batch size (up to a maximum). You can set the initial buffer size in `connection`'s
     * constructor, using \ref buffer_params::initial_read_size. The buffer may be
     * grown bigger by other read operations, if required.
     * \n
     * Rows read by this function are owning objects, and don't hold any reference to
     * the connection's internal buffers (contrary what happens with the dynamic interface's counterpart).
     * \n
     * `SpanStaticRow` must exactly be one of the types in the `StaticRow` parameter pack.
     * The type must match the resultset that is currently being processed by `st`. For instance,
     * given `static_execution_state<T1, T2>`, when reading rows for the second resultset, `SpanStaticRow`
     * must exactly be `T2`. If this is not the case, a runtime error will be issued.
     * \n
     * This function can report schema mismatches.
     */
    template <class SpanStaticRow, class... StaticRow>
    std::size_t read_some_rows(
        static_execution_state<StaticRow...>& st,
        span<SpanStaticRow> output,
        error_code& err,
        diagnostics& diag
    )
    {
        return impl_.run(impl_.make_params_read_some_rows(st, output, diag), err);
    }

    /**
     * \brief Reads a batch of rows.
     * \details
     * Reads a batch of rows of unspecified size into the storage given by `output`.
     * At most `output.size()` rows will be read. If the operation represented by `st`
     * has still rows to read, and `output.size() > 0`, at least one row will be read.
     * \n
     * Returns the number of read rows.
     * \n
     * If there are no more rows, or `st.should_read_rows() == false`, this function is a no-op and returns
     * zero.
     * \n
     * The number of rows that will be read depends on the input buffer size. The bigger the buffer,
     * the greater the batch size (up to a maximum). You can set the initial buffer size in `connection`'s
     * constructor, using \ref buffer_params::initial_read_size. The buffer may be
     * grown bigger by other read operations, if required.
     * \n
     * Rows read by this function are owning objects, and don't hold any reference to
     * the connection's internal buffers (contrary what happens with the dynamic interface's counterpart).
     * \n
     * `SpanStaticRow` must exactly be one of the types in the `StaticRow` parameter pack.
     * The type must match the resultset that is currently being processed by `st`. For instance,
     * given `static_execution_state<T1, T2>`, when reading rows for the second resultset, `SpanStaticRow`
     * must exactly be `T2`. If this is not the case, a runtime error will be issued.
     * \n
     * This function can report schema mismatches.
     */
    template <class SpanStaticRow, class... StaticRow>
    std::size_t read_some_rows(static_execution_state<StaticRow...>& st, span<SpanStaticRow> output)
    {
        error_code err;
        diagnostics diag;
        std::size_t res = read_some_rows(st, output, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
        return res;
    }

    /**
     * \brief Reads a batch of rows.
     * \details
     * Reads a batch of rows of unspecified size into the storage given by `output`.
     * At most `output.size()` rows will be read. If the operation represented by `st`
     * has still rows to read, and `output.size() > 0`, at least one row will be read.
     * \n
     * Returns the number of read rows.
     * \n
     * If there are no more rows, or `st.should_read_rows() == false`, this function is a no-op and returns
     * zero.
     * \n
     * The number of rows that will be read depends on the input buffer size. The bigger the buffer,
     * the greater the batch size (up to a maximum). You can set the initial buffer size in `connection`'s
     * constructor, using \ref buffer_params::initial_read_size. The buffer may be
     * grown bigger by other read operations, if required.
     * \n
     * Rows read by this function are owning objects, and don't hold any reference to
     * the connection's internal buffers (contrary what happens with the dynamic interface's counterpart).
     * \n
     * `SpanStaticRow` must exactly be one of the types in the `StaticRow` parameter pack.
     * The type must match the resultset that is currently being processed by `st`. For instance,
     * given `static_execution_state<T1, T2>`, when reading rows for the second resultset, `SpanStaticRow`
     * must exactly be `T2`. If this is not the case, a runtime error will be issued.
     * \n
     * This function can report schema mismatches.
     *
     * \par Handler signature
     * The handler signature for this operation is
     * `void(boost::mysql::error_code, std::size_t)`.
     *
     * \par Object lifetimes
     * The storage that `output` references must be kept alive until the operation completes.
     */
    template <
        class SpanStaticRow,
        class... StaticRow,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, std::size_t))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code, std::size_t))
    async_read_some_rows(
        static_execution_state<StaticRow...>& st,
        span<SpanStaticRow> output,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return async_read_some_rows(st, output, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /**
     * \brief Reads a batch of rows.
     * \details
     * Reads a batch of rows of unspecified size into the storage given by `output`.
     * At most `output.size()` rows will be read. If the operation represented by `st`
     * has still rows to read, and `output.size() > 0`, at least one row will be read.
     * \n
     * Returns the number of read rows.
     * \n
     * If there are no more rows, or `st.should_read_rows() == false`, this function is a no-op and returns
     * zero.
     * \n
     * The number of rows that will be read depends on the input buffer size. The bigger the buffer,
     * the greater the batch size (up to a maximum). You can set the initial buffer size in `connection`'s
     * constructor, using \ref buffer_params::initial_read_size. The buffer may be
     * grown bigger by other read operations, if required.
     * \n
     * Rows read by this function are owning objects, and don't hold any reference to
     * the connection's internal buffers (contrary what happens with the dynamic interface's counterpart).
     * \n
     * `SpanStaticRow` must exactly be one of the types in the `StaticRow` parameter pack.
     * The type must match the resultset that is currently being processed by `st`. For instance,
     * given `static_execution_state<T1, T2>`, when reading rows for the second resultset, `SpanStaticRow`
     * must exactly be `T2`. If this is not the case, a runtime error will be issued.
     * \n
     * This function can report schema mismatches.
     *
     * \par Handler signature
     * The handler signature for this operation is
     * `void(boost::mysql::error_code, std::size_t)`.
     *
     * \par Object lifetimes
     * The storage that `output` references must be kept alive until the operation completes.
     */
    template <
        class SpanStaticRow,
        class... StaticRow,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, std::size_t))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    BOOST_ASIO_INITFN_AUTO_RESULT_TYPE(CompletionToken, void(error_code, std::size_t))
    async_read_some_rows(
        static_execution_state<StaticRow...>& st,
        span<SpanStaticRow> output,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    )
    {
        return impl_.async_run(
            impl_.make_params_read_some_rows(st, output, diag),
            std::forward<CompletionToken>(token)
        );
    }
#endif

    /**
     * \brief Reads metadata for subsequent resultsets in a multi-resultset operation.
     * \details
     * If `st.should_read_head() == true`, this function will read the next resultset's
     * initial response message and metadata, if any. If the resultset indicates a failure
     * (e.g. the query associated to this resultset contained an error), this function will fail
     * with that error.
     * \n
     * If `st.should_read_head() == false`, this function is a no-op.
     * \n
     * `st` may be either an \ref execution_state or \ref static_execution_state object.
     * \n
     * This function is only relevant when using multi-function operations with statements
     * that return more than one resultset.
     * \n
     * When using the static interface, this function will detect schema mismatches for the resultset
     * currently being read. Further errors may be detected by subsequent invocations of this function
     * and by \ref read_some_rows.
     */
    template <BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType>
    void read_resultset_head(ExecutionStateType& st, error_code& err, diagnostics& diag)
    {
        return impl_.run(impl_.make_params_read_resultset_head(st, diag), err);
    }

    /// \copydoc read_resultset_head
    template <BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType>
    void read_resultset_head(ExecutionStateType& st)
    {
        error_code err;
        diagnostics diag;
        read_resultset_head(st, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc read_resultset_head
     * \par Handler signature
     * The handler signature for this operation is
     * `void(boost::mysql::error_code)`.
     */
    template <
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_read_resultset_head(
        ExecutionStateType& st,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_read_resultset_head_t<CompletionToken&&>)
    {
        return async_read_resultset_head(st, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_read_resultset_head
    template <
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
            CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_read_resultset_head(
        ExecutionStateType& st,
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_read_resultset_head_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_read_resultset_head(st, diag),
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Checks whether the server is alive.
     * \details
     * If the server is alive, this function will complete without error.
     * If it's not, it will fail with the relevant network or protocol error.
     * \n
     * Note that ping requests are treated as any other type of request at the protocol
     * level, and won't be prioritized anyhow by the server. If the server is stuck
     * in a long-running query, the ping request won't be answered until the query is
     * finished.
     */
    void ping(error_code& err, diagnostics& diag) { impl_.run(impl_.make_params_ping(diag), err); }

    /// \copydoc ping
    void ping()
    {
        error_code err;
        diagnostics diag;
        ping(err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc ping
     * \details
     * \n
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_ping(CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type))
        BOOST_MYSQL_RETURN_TYPE(detail::async_ping_t<CompletionToken&&>)
    {
        return async_ping(impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_ping
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_ping(
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_ping_t<CompletionToken&&>)
    {
        return impl_.async_run(impl_.make_params_ping(diag), std::forward<CompletionToken>(token));
    }

    /**
     * \brief Resets server-side session state, like variables and prepared statements.
     * \details
     * Resets all server-side state for the current session:
     * \n
     *   \li Rolls back any active transactions and resets autocommit mode.
     *   \li Releases all table locks.
     *   \li Drops all temporary tables.
     *   \li Resets all session system variables to their default values (including the ones set by `SET
     *       NAMES`) and clears all user-defined variables.
     *   \li Closes all prepared statements.
     * \n
     * A full reference on the affected session state can be found
     * <a href="https://dev.mysql.com/doc/c-api/8.0/en/mysql-reset-connection.html">here</a>.
     * \n
     * This function will not reset the current physical connection and won't cause re-authentication.
     * It is faster than closing and re-opening a connection.
     * \n
     * The connection must be connected and authenticated before calling this function.
     * This function involves communication with the server, and thus may fail.
     *
     * \par Warning on character sets
     * This function will restore the connection's character set and collation **to the server's default**,
     * and not to the one specified during connection establishment. Some servers have `latin1` as their
     * default character set, which is not usually what you want. Use a `SET NAMES` statement after using
     * this function to be sure.
     * \n
     * You can find the character set that your server will use after reset by running:
     * \code
     * "SELECT @@global.character_set_client, @@global.character_set_results;"
     * \endcode
     */
    void reset_connection(error_code& err, diagnostics& diag)
    {
        impl_.run(impl_.make_params_reset_connection(diag), err);
    }

    /// \copydoc reset_connection
    void reset_connection()
    {
        error_code err;
        diagnostics diag;
        reset_connection(err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc reset_connection
     * \details
     * \n
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_reset_connection(CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type))
        BOOST_MYSQL_RETURN_TYPE(detail::async_reset_connection_t<CompletionToken&&>)
    {
        return async_reset_connection(impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_reset_connection
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_reset_connection(
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_reset_connection_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_reset_connection(diag),
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Closes the connection to the server.
     * \details
     * This function is only available if `Stream` satisfies the `SocketStream` concept.
     * \n
     * Sends a quit request, performs the TLS shutdown (if required)
     * and closes the underlying stream. Prefer this function to \ref connection::quit.
     */
    void close(error_code& err, diagnostics& diag)
    {
        static_assert(
            detail::is_socket_stream<Stream>::value,
            "close can only be used if Stream satisfies the SocketStream concept"
        );
        impl_.run(impl_.make_params_close(diag), err);
    }

    /// \copydoc close
    void close()
    {
        static_assert(
            detail::is_socket_stream<Stream>::value,
            "close can only be used if Stream satisfies the SocketStream concept"
        );
        error_code err;
        diagnostics diag;
        close(err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc close
     * \details
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_close(CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type))
        BOOST_MYSQL_RETURN_TYPE(detail::async_close_connection_t<CompletionToken&&>)
    {
        static_assert(
            detail::is_socket_stream<Stream>::value,
            "async_close can only be used if Stream satisfies the SocketStream concept"
        );
        return async_close(impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_close
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_close(
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_close_connection_t<CompletionToken&&>)
    {
        static_assert(
            detail::is_socket_stream<Stream>::value,
            "async_close can only be used if Stream satisfies the SocketStream concept"
        );
        return impl_.async_run(impl_.make_params_close(diag), std::forward<CompletionToken>(token));
    }

    /**
     * \brief Notifies the MySQL server that the client wants to end the session and shutdowns SSL.
     * \details Sends a quit request to the MySQL server. If the connection is using SSL,
     * this function will also perform the SSL shutdown. You should
     * close the underlying physical connection after calling this function.
     * \n
     * If the `Stream` template parameter fulfills the `SocketConnection`
     * requirements, use \ref connection::close instead of this function,
     * as it also takes care of closing the underlying stream.
     */
    void quit(error_code& err, diagnostics& diag) { impl_.run(impl_.make_params_quit(diag), err); }

    /// \copydoc quit
    void quit()
    {
        error_code err;
        diagnostics diag;
        quit(err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc quit
     * \details
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_quit(CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type))
        BOOST_MYSQL_RETURN_TYPE(detail::async_quit_connection_t<CompletionToken&&>)
    {
        return async_quit(impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_quit
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code))
                  CompletionToken BOOST_ASIO_DEFAULT_COMPLETION_TOKEN_TYPE(executor_type)>
    auto async_quit(
        diagnostics& diag,
        CompletionToken&& token BOOST_ASIO_DEFAULT_COMPLETION_TOKEN(executor_type)
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_quit_connection_t<CompletionToken&&>)
    {
        return impl_.async_run(impl_.make_params_quit(diag), std::forward<CompletionToken>(token));
    }

    /**
     * \brief Rebinds the connection type to another executor.
     * \details
     * The `Stream` type must either provide a `rebind_executor`
     * member with the same semantics, or be an instantiation of `boost::asio::ssl::stream` with
     * a `Stream` type providing a `rebind_executor` member.
     */
    template <typename Executor1>
    struct rebind_executor
    {
        /// The connection type when rebound to the specified executor.
        using other = connection<typename detail::rebind_executor<Stream, Executor1>::type>;
    };
};

}  // namespace mysql
}  // namespace boost

#endif
