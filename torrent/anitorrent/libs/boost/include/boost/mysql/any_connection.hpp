//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_ANY_CONNECTION_HPP
#define BOOST_MYSQL_ANY_CONNECTION_HPP

#include <boost/mysql/character_set.hpp>
#include <boost/mysql/connect_params.hpp>
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
#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/connect_params_helpers.hpp>
#include <boost/mysql/detail/connection_impl.hpp>
#include <boost/mysql/detail/execution_concepts.hpp>
#include <boost/mysql/detail/throw_on_error_loc.hpp>

#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/consign.hpp>
#include <boost/asio/execution_context.hpp>
#include <boost/asio/ssl/context.hpp>
#include <boost/assert.hpp>
#include <boost/system/result.hpp>
#include <boost/variant2/variant.hpp>

#include <cstddef>
#include <memory>
#include <type_traits>
#include <utility>

namespace boost {
namespace mysql {

// Forward declarations
template <class... StaticRow>
class static_execution_state;

/**
 * \brief (EXPERIMENTAL) Configuration parameters that can be passed to \ref any_connection's constructor.
 *
 * \par Experimental
 * This part of the API is experimental, and may change in successive
 * releases without previous notice.
 */
struct any_connection_params
{
    /**
     * \brief An external SSL context containing options to configure TLS.
     * \details
     * Relevant only for SSL connections (those that result on \ref
     * any_connection::uses_ssl returning `true`).
     * \n
     * If the connection is configured to use TLS, an internal `asio::ssl::stream`
     * object will be created. If this member is set to a non-null value,
     * this internal object will be initialized using the passed context.
     * This is the only way to configure TLS options in `any_connection`.
     * \n
     * If the connection is configured to use TLS and this member is `nullptr`,
     * an internal `asio::ssl::context` object with suitable default options
     * will be created.
     *
     * \par Object lifetimes
     * If set to non-null, the pointee object must be kept alive until
     * all \ref any_connection objects constructed from `*this` are destroyed.
     */
    asio::ssl::context* ssl_context{};

    /**
     * \brief The initial size of the connection's read buffer.
     * \details A bigger read buffer can increase the number of rows
     * returned by \ref any_connection::read_some_rows.
     */
    std::size_t initial_read_buffer_size{default_initial_read_buffer_size};
};

/**
 * \brief (EXPERIMENTAL) A type-erased connection to a MySQL server.
 * \details
 * Represents a connection to a MySQL server. Compared to \ref connection, this class:
 * \n
 * \li Is type-erased. The type of the connection doesn't depend on the transport being used.
 *     Supported transports include plaintext TCP, SSL over TCP and UNIX domain sockets.
 * \li Is easier to connect, as \ref connect and \ref async_connect handle hostname resolution.
 * \li Can always be re-connected after being used or encountering an error.
 * \li Doesn't support default completion tokens.
 * \n
 * Provides a level of performance similar to \ref connection.
 * \n
 * This is a move-only type.
 * \n
 * \par Thread safety
 * Distinct objects: safe. \n
 * Shared objects: unsafe. \n
 * This class is <b>not thread-safe</b>: for a single object, if you
 * call its member functions concurrently from separate threads, you will get a race condition.
 *
 * \par Experimental
 * This part of the API is experimental, and may change in successive
 * releases without previous notice.
 */
class any_connection
{
    detail::connection_impl impl_;

#ifndef BOOST_MYSQL_DOXYGEN
    friend struct detail::access;
#endif

    BOOST_MYSQL_DECL
    static std::unique_ptr<detail::any_stream> create_stream(
        asio::any_io_executor ex,
        asio::ssl::context* ctx
    );

    template <class CompletionToken>
    using async_connect_owning_t = detail::async_connect_t<
        detail::any_address_view,
        decltype(asio::consign(std::declval<CompletionToken>(), std::unique_ptr<char[]>()))>;

public:
    /**
     * \brief Constructs a connection object from an executor and an optional set of parameters.
     * \details
     * The resulting connection has `this->get_executor() == ex`. Any internally required I/O objects
     * will be constructed using this executor.
     * \n
     * You can configure extra parameters, like the SSL context and buffer sizes, by passing
     * an \ref any_connection_params object to this constructor.
     */
    any_connection(boost::asio::any_io_executor ex, any_connection_params params = {})
        : impl_(params.initial_read_buffer_size, create_stream(std::move(ex), params.ssl_context))
    {
    }

    /**
     * \brief Constructs a connection object from an execution context and an optional set of parameters.
     * \details
     * The resulting connection has `this->get_executor() == ctx.get_executor()`.
     * Any internally required I/O objects will be constructed using this executor.
     * \n
     * You can configure extra parameters, like the SSL context and buffer sizes, by passing
     * an \ref any_connection_params object to this constructor.
     * \n
     * This function participates in overload resolution only if `ExecutionContext`
     * satisfies the `ExecutionContext` requirements imposed by Boost.Asio.
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
    any_connection(ExecutionContext& ctx, any_connection_params params = {})
        : any_connection(ctx.get_executor(), params)
    {
    }

    /**
     * \brief Move constructor.
     */
    any_connection(any_connection&& other) = default;

    /**
     * \brief Move assignment.
     */
    any_connection& operator=(any_connection&& rhs) = default;

#ifndef BOOST_MYSQL_DOXYGEN
    any_connection(const any_connection&) = delete;
    any_connection& operator=(const any_connection&) = delete;
#endif

    /**
     * \brief Destructor.
     * \details
     * Closes the connection at the transport layer (by closing any underlying socket objects).
     * If you require a clean close, call \ref close or \ref async_close before the connection
     * is destroyed.
     */
    ~any_connection() = default;

    /// The executor type associated to this object.
    using executor_type = asio::any_io_executor;

    /**
     * \brief Retrieves the executor associated to this object.
     * \par Exception safety
     * No-throw guarantee.
     */
    executor_type get_executor() noexcept { return impl_.stream().get_executor(); }

    /**
     * \brief Returns whether the connection negotiated the use of SSL or not.
     * \details
     * This function can be used to determine whether you are using a SSL
     * connection or not when using SSL negotiation.
     * \n
     * This function always returns `false`
     * for connections that haven't been established yet. If the connection establishment fails,
     * the return value is undefined.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    bool uses_ssl() const noexcept { return impl_.ssl_active(); }

    /**
     * \brief Returns whether backslashes are being treated as escape sequences.
     * \details
     * By default, the server treats backslashes in string values as escape characters.
     * This behavior can be disabled by activating the <a
     *   href="https://dev.mysql.com/doc/refman/8.0/en/sql-mode.html#sqlmode_no_backslash_escapes">`NO_BACKSLASH_ESCAPES`</a>
     * SQL mode.
     * \n
     * Every time an operation involving server communication completes, the server reports whether
     * this mode was activated or not as part of the response. Connections store this information
     * and make it available through this function.
     * \n
     * \li If backslash are treated like escape characters, returns `true`.
     * \li If `NO_BACKSLASH_ESCAPES` has been activated, returns `false`.
     * \li If connection establishment hasn't happened yet, returns `true`.
     * \li Calling this function while an async operation that changes backslash behavior
     *     is outstanding may return `true` or `false`.
     * \n
     * This function does not involve server communication.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    bool backslash_escapes() const noexcept { return impl_.backslash_escapes(); }

    /**
     * \brief Returns the character set used by this connection.
     * \details
     * Connections attempt to keep track of the current character set.
     * Deficiencies in the protocol can cause the character set to be unknown, though.
     * When the character set is known, this function returns
     * the character set currently in use. Otherwise, returns \ref client_errc::unknown_character_set.
     * \n
     * The following functions can modify the return value of this function: \n
     *   \li Prior to connection, the character set is always unknown.
     *   \li \ref connect and \ref async_connect may set the current character set
     *       to a known value, depending on the requested collation.
     *   \li \ref set_character_set always and \ref async_set_character_set always
     *       set the current character set to the passed value.
     *   \li \ref reset_connection and \ref async_reset_connection always makes the current character
     *       unknown.
     *
     * \par Avoid changing the character set directly
     * If you change the connection's character set directly using SQL statements
     * like `"SET NAMES utf8mb4"`, the client has no way to track this change,
     * and this function will return incorrect results.
     *
     * \par Errors
     * \li \ref client_errc::unknown_character_set if the current character set is unknown.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    system::result<character_set> current_character_set() const noexcept
    {
        return impl_.current_character_set();
    }

    /**
     * \brief Returns format options suitable to format SQL according to the current connection configuation.
     * \details
     * If the current character set is known (as given by \ref current_character_set), returns
     * a value suitable to be passed to SQL formatting functions. Otherwise, returns an error.
     *
     * \par Errors
     * \li \ref client_errc::unknown_character_set if the current character set is unknown.
     *
     * \par Exception safety
     * No-throw guarantee.
     */
    system::result<format_options> format_opts() const noexcept
    {
        auto res = current_character_set();
        if (res.has_error())
            return res.error();
        return format_options{res.value(), backslash_escapes()};
    }

    /// \copydoc connection::meta_mode
    metadata_mode meta_mode() const noexcept { return impl_.meta_mode(); }

    /// \copydoc connection::set_meta_mode
    void set_meta_mode(metadata_mode v) noexcept { impl_.set_meta_mode(v); }

    /**
     * \brief Establishes a connection to a MySQL server.
     * \details
     * This function performs the following:
     * \n
     * \li If a connection has already been established (by a previous call to \ref connect
     *     or \ref async_connect), closes it at the transport layer (by closing any underlying socket)
     *     and discards any protocol state associated to it. (If you require
     *     a clean close, call \ref close or \ref async_close before using this function).
     * \li If the connection is configured to use TCP (`params.server_address.type() ==
     *     address_type::host_and_port`), resolves the passed hostname to a set of endpoints. An empty
     *     hostname is equivalent to `"localhost"`.
     * \li Establishes the physical connection (performing the
     *     TCP or UNIX socket connect).
     * \li Performs the MySQL handshake to establish a session. If the
     *     connection is configured to use TLS, the TLS handshake is performed as part of this step.
     * \li If any of the above steps fail, the TCP or UNIX socket connection is closed.
     * \n
     * You can configure some options using the \ref connect_params struct.
     * \n
     * The decision to use TLS or not is performed using the following:
     * \n
     * \li If the transport is not TCP (`params.server_address.type() != address_type::host_and_port`),
     *     the connection will never use TLS.
     * \li If the transport is TCP, and `params.ssl == ssl_mode::disable`, the connection will not use TLS.
     * \li If the transport is TCP, and `params.ssl == ssl_mode::enable`, the connection will use TLS
     *     only if the server supports it.
     * \li If the transport is TCP, and `params.ssl == ssl_mode::require`, the connection will always use TLS.
     *     If the server doesn't support it, this function will fail with \ref
     *     client_errc::server_doesnt_support_ssl.
     * \n
     * If `params.connection_collation` is within a set of well-known collations, this function
     * sets the current character set, such that \ref current_character_set returns a non-null value.
     * The default collation (`utf8mb4_general_ci`) is the only one guaranteed to be in the set of well-known
     * collations.
     */
    void connect(const connect_params& params, error_code& ec, diagnostics& diag)
    {
        impl_.connect(detail::make_view(params.server_address), detail::make_hparams(params), ec, diag);
    }

    /// \copydoc connect
    void connect(const connect_params& params)
    {
        error_code err;
        diagnostics diag;
        connect(params, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc connect
     *
     * \par Object lifetimes
     * The implementation will copy `params` as required, so it needs not be
     * kept alive.
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(error_code)) CompletionToken>
    auto async_connect(const connect_params& params, diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(async_connect_owning_t<CompletionToken&&>)
    {
        auto stable_prms = detail::make_stable(params);
        return impl_.async_connect(
            stable_prms.address,
            stable_prms.hparams,
            diag,
            asio::consign(std::forward<CompletionToken>(token), std::move(stable_prms.string_buffer))
        );
    }

    /// \copydoc async_connect
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(error_code)) CompletionToken>
    auto async_connect(const connect_params& params, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(async_connect_owning_t<CompletionToken&&>)
    {
        return async_connect(params, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /**
     * \copydoc connect
     * This function has the same behavior as the other `async_connect` overloads,
     * but perform less copies.
     * \par Object lifetimes
     * Zero-copy overload: no copies of the value pointed to by `params`
     * will be made. It must be kept alive for the duration of the operation,
     * until the final completion handler is called. If you are in doubt,
     * prefer the overloads taking a `const connect_params&`, which will ensure
     * lifetime correctness for you.
     *
     * \par Preconditions
     * `params != nullptr`
     *
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(error_code)) CompletionToken>
    auto async_connect(const connect_params* params, diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_connect_t<detail::any_address_view, CompletionToken&&>)
    {
        BOOST_ASSERT(params != nullptr);
        return impl_.async_connect(
            detail::make_view(params->server_address),
            detail::make_hparams(*params),
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc connection::execute
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

    /// \copydoc connection::async_execute
    template <
        BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest,
        BOOST_MYSQL_RESULTS_TYPE ResultsType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_execute(ExecutionRequest&& req, ResultsType& result, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_execute_t<ExecutionRequest&&, ResultsType, CompletionToken&&>)
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
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_execute(
        ExecutionRequest&& req,
        ResultsType& result,
        diagnostics& diag,
        CompletionToken&& token
    ) BOOST_MYSQL_RETURN_TYPE(detail::async_execute_t<ExecutionRequest&&, ResultsType, CompletionToken&&>)
    {
        return impl_.async_execute(
            std::forward<ExecutionRequest>(req),
            result,
            diag,
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc connection::start_execution
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

    /// \copydoc connection::async_start_execution
    template <
        BOOST_MYSQL_EXECUTION_REQUEST ExecutionRequest,
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_start_execution(ExecutionRequest&& req, ExecutionStateType& st, CompletionToken&& token)
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
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_start_execution(
        ExecutionRequest&& req,
        ExecutionStateType& st,
        diagnostics& diag,
        CompletionToken&& token
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

    /// \copydoc connection::prepare_statement
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

    /// \copydoc connection::async_prepare_statement
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::statement))
                  CompletionToken>
    auto async_prepare_statement(string_view stmt, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_prepare_statement_t<CompletionToken&&>)
    {
        return async_prepare_statement(stmt, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_prepare_statement
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::statement))
                  CompletionToken>
    auto async_prepare_statement(string_view stmt, diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_prepare_statement_t<CompletionToken&&>)
    {
        return impl_.async_run(
            detail::prepare_statement_algo_params{&diag, stmt},
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc connection::close_statement
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

    /// \copydoc connection::async_close_statement
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_close_statement(const statement& stmt, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_close_statement_t<CompletionToken&&>)
    {
        return async_close_statement(stmt, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_close_statement
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_close_statement(const statement& stmt, diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_close_statement_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_close_statement(stmt, diag),
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc connection::read_some_rows
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

    /// \copydoc connection::async_read_some_rows(execution_state&,CompletionToken&&)
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::rows_view))
                  CompletionToken>
    auto async_read_some_rows(execution_state& st, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_read_some_rows_dynamic_t<CompletionToken&&>)
    {
        return async_read_some_rows(st, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_read_some_rows(execution_state&,CompletionToken&&)
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code, ::boost::mysql::rows_view))
                  CompletionToken>
    auto async_read_some_rows(execution_state& st, diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_read_some_rows_dynamic_t<CompletionToken&&>)
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
     * the greater the batch size (up to a maximum). You can set the initial buffer size in the
     * constructor. The buffer may be grown bigger by other read operations, if required.
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
     * the greater the batch size (up to a maximum). You can set the initial buffer size in the
     * constructor. The buffer may be grown bigger by other read operations, if required.
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
     * the greater the batch size (up to a maximum). You can set the initial buffer size in the
     * constructor. The buffer may be grown bigger by other read operations, if required.
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
    auto async_read_some_rows(
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
     * the greater the batch size (up to a maximum). You can set the initial buffer size in the
     * constructor. The buffer may be grown bigger by other read operations, if required.
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
    auto async_read_some_rows(
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

    /// \copydoc connection::read_resultset_head
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

    /// \copydoc connection::async_read_resultset_head
    template <
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_read_resultset_head(ExecutionStateType& st, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_read_resultset_head_t<CompletionToken&&>)
    {
        return async_read_resultset_head(st, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_read_resultset_head
    template <
        BOOST_MYSQL_EXECUTION_STATE_TYPE ExecutionStateType,
        BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_read_resultset_head(ExecutionStateType& st, diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_read_resultset_head_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_read_resultset_head(st, diag),
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Sets the connection's character set, as per SET NAMES.
     * \details
     * Sets the connection's character set by running a
     * <a href="https://dev.mysql.com/doc/refman/8.0/en/set-names.html">`SET NAMES`</a>
     * SQL statement, using the passed \ref character_set::name as the charset name to set.
     * \n
     * This function will also update the value returned by \ref current_character_set, so
     * prefer using this function over raw SQL statements.
     * \n
     * If the server was unable to set the character set to the requested value (e.g. because
     * the server does not support the requested charset), this function will fail,
     * as opposed to how \ref connect behaves when an unsupported collation is passed.
     * This is a limitation of MySQL servers.
     * \n
     * You need to perform connection establishment for this function to succeed, since it
     * involves communicating with the server.
     *
     * \par Object lifetimes
     * `charset` will be copied as required, and does not need to be kept alive.
     */
    void set_character_set(const character_set& charset, error_code& err, diagnostics& diag)
    {
        impl_.run(impl_.make_params_set_character_set(charset, diag), err);
    }

    /// \copydoc set_character_set
    void set_character_set(const character_set& charset)
    {
        error_code err;
        diagnostics diag;
        set_character_set(charset, err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /**
     * \copydoc set_character_set
     * \details
     * \n
     * \par Handler signature
     * The handler signature for this operation is `void(boost::mysql::error_code)`.
     */
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_set_character_set(const character_set& charset, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_set_character_set_t<CompletionToken&&>)
    {
        return async_set_character_set(charset, impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_set_character_set
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_set_character_set(const character_set& charset, diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_set_character_set_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_set_character_set(charset, diag),
            std::forward<CompletionToken>(token)
        );
    }

    /// \copydoc connection::ping
    void ping(error_code& err, diagnostics& diag) { impl_.run(impl_.make_params_ping(diag), err); }

    /// \copydoc ping
    void ping()
    {
        error_code err;
        diagnostics diag;
        ping(err, diag);
        detail::throw_on_error_loc(err, diag, BOOST_CURRENT_LOCATION);
    }

    /// \copydoc connection::async_ping
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_ping(CompletionToken&& token) BOOST_MYSQL_RETURN_TYPE(detail::async_ping_t<CompletionToken&&>)
    {
        return async_ping(impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_ping
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_ping(diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_ping_t<CompletionToken&&>)
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
     * default character set, which is not usually what you want. Since there is no way to know this
     * character set, \ref current_character_set will return `nullptr` after the operation succeeds.
     * We recommend always using \ref set_character_set or \ref async_set_character_set after calling this
     * function.
     * \n
     * You can find the character set that your server will use after the reset by running:
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
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_reset_connection(CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_reset_connection_t<CompletionToken&&>)
    {
        return async_reset_connection(impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_reset_connection
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_reset_connection(diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_reset_connection_t<CompletionToken&&>)
    {
        return impl_.async_run(
            impl_.make_params_reset_connection(diag),
            std::forward<CompletionToken>(token)
        );
    }

    /**
     * \brief Cleanly closes the connection to the server.
     * \details
     * This function does the following:
     * \n
     * \li Sends a quit request. This is required by the MySQL protocol, to inform
     *     the server that we're closing the connection gracefully.
     * \li If the connection is using TLS (`this->uses_ssl() == true`), performs
     *     the TLS shutdown.
     * \li Closes the transport-level connection (the TCP or UNIX socket).
     * \n
     * Since this function involves writing a message to the server, it can fail.
     * Only use this function if you know that the connection is healthy and you want
     * to cleanly close it.
     * \n
     * If you don't call this function, the destructor or successive connects will
     * perform a transport-layer close. This doesn't cause any resource leaks, but may
     * cause warnings to be written to the server logs.
     */
    void close(error_code& err, diagnostics& diag)
    {
        this->impl_.run(this->impl_.make_params_close(diag), err);
    }

    /// \copydoc close
    void close()
    {
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
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(error_code)) CompletionToken>
    auto async_close(CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_close_connection_t<CompletionToken&&>)
    {
        return async_close(impl_.shared_diag(), std::forward<CompletionToken>(token));
    }

    /// \copydoc async_close
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(error_code)) CompletionToken>
    auto async_close(diagnostics& diag, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(detail::async_close_connection_t<CompletionToken&&>)
    {
        return this->impl_.async_run(
            this->impl_.make_params_close(diag),
            std::forward<CompletionToken>(token)
        );
    }
};

}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/any_connection.ipp>
#endif

#endif
