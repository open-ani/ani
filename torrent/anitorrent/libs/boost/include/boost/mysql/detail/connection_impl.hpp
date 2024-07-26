//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_CONNECTION_IMPL_HPP
#define BOOST_MYSQL_DETAIL_CONNECTION_IMPL_HPP

#include <boost/mysql/any_address.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/execution_state.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/handshake_params.hpp>
#include <boost/mysql/metadata_mode.hpp>
#include <boost/mysql/rows_view.hpp>
#include <boost/mysql/statement.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/algo_params.hpp>
#include <boost/mysql/detail/any_execution_request.hpp>
#include <boost/mysql/detail/any_stream.hpp>
#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/connect_params_helpers.hpp>
#include <boost/mysql/detail/execution_processor/execution_processor.hpp>
#include <boost/mysql/detail/execution_processor/execution_state_impl.hpp>
#include <boost/mysql/detail/run_algo.hpp>
#include <boost/mysql/detail/typing/get_type_index.hpp>
#include <boost/mysql/detail/writable_field_traits.hpp>

#include <boost/asio/any_completion_handler.hpp>
#include <boost/core/ignore_unused.hpp>
#include <boost/mp11/integer_sequence.hpp>
#include <boost/system/result.hpp>

#include <array>
#include <cstddef>
#include <cstring>
#include <memory>
#include <tuple>
#include <utility>

namespace boost {
namespace mysql {

// Forward decl
template <class... StaticRow>
class static_execution_state;

struct character_set;

namespace detail {

// Forward decl
class connection_state;

// Helper typedefs
template <class T>
using any_handler = asio::any_completion_handler<void(error_code, T)>;

using any_void_handler = asio::any_completion_handler<void(error_code)>;

//
// execution helpers
//
template <class... T, std::size_t... I>
std::array<field_view, sizeof...(T)> tuple_to_array_impl(const std::tuple<T...>& t, mp11::index_sequence<I...>) noexcept
{
    boost::ignore_unused(t);  // MSVC gets confused if sizeof...(T) == 0
    return std::array<field_view, sizeof...(T)>{{to_field(std::get<I>(t))...}};
}

template <class... T>
std::array<field_view, sizeof...(T)> tuple_to_array(const std::tuple<T...>& t) noexcept
{
    return tuple_to_array_impl(t, mp11::make_index_sequence<sizeof...(T)>());
}

struct query_request_getter
{
    any_execution_request value;
    any_execution_request get() const noexcept { return value; }
};
inline query_request_getter make_request_getter(string_view q, std::vector<field_view>&) noexcept
{
    return query_request_getter{q};
}

struct stmt_it_request_getter
{
    statement stmt;
    span<const field_view> params;  // Points into the connection state's shared fields

    any_execution_request get() const noexcept { return any_execution_request(stmt, params); }
};

template <class FieldViewFwdIterator>
inline stmt_it_request_getter make_request_getter(
    const bound_statement_iterator_range<FieldViewFwdIterator>& req,
    std::vector<field_view>& shared_fields
)
{
    auto& impl = access::get_impl(req);
    shared_fields.assign(impl.first, impl.last);
    return {impl.stmt, shared_fields};
}

template <std::size_t N>
struct stmt_tuple_request_getter
{
    statement stmt;
    std::array<field_view, N> params;

    any_execution_request get() const noexcept { return any_execution_request(stmt, params); }
};
template <class WritableFieldTuple>
stmt_tuple_request_getter<std::tuple_size<WritableFieldTuple>::value>
make_request_getter(const bound_statement_tuple<WritableFieldTuple>& req, std::vector<field_view>&)
{
    auto& impl = access::get_impl(req);
    return {impl.stmt, tuple_to_array(impl.params)};
}

class connection_impl
{
    std::unique_ptr<any_stream> stream_;
    std::unique_ptr<connection_state> st_;

    // Misc
    BOOST_MYSQL_DECL
    static std::vector<field_view>& get_shared_fields(connection_state& st) noexcept;

    // Generic algorithm
    struct run_algo_initiation
    {
        template <class Handler, class AlgoParams>
        void operator()(Handler&& handler, any_stream* stream, connection_state* st, AlgoParams params)
        {
            async_run_algo(*stream, *st, params, std::forward<Handler>(handler));
        }
    };

    // Connect
    static connect_algo_params make_params_connect(
        diagnostics& diag,
        const handshake_params& params,
        bool secure_channel
    ) noexcept
    {
        return connect_algo_params{&diag, params, secure_channel};
    }

    template <class EndpointType>
    static bool is_secure_channel(const EndpointType&) noexcept
    {
        return false;
    }

    static bool is_secure_channel(const any_address_view& addr) noexcept
    {
        return addr.type == address_type::unix_path;
    }

    template <class EndpointType>
    struct initiate_connect
    {
        template <class Handler>
        void operator()(
            Handler&& handler,
            any_stream* stream,
            connection_state* st,
            const EndpointType& endpoint,
            handshake_params params,
            diagnostics* diag
        )
        {
            stream->set_endpoint(&endpoint);
            async_run_algo(
                *stream,
                *st,
                make_params_connect(*diag, params, is_secure_channel(endpoint)),
                std::forward<Handler>(handler)
            );
        }
    };

    // execute
    struct initiate_execute
    {
        template <class Handler, class ExecutionRequest>
        void operator()(
            Handler&& handler,
            any_stream* stream,
            connection_state* st,
            const ExecutionRequest& req,
            execution_processor* proc,
            diagnostics* diag
        )
        {
            auto getter = make_request_getter(req, get_shared_fields(*st));
            async_run_algo(
                *stream,
                *st,
                execute_algo_params{diag, getter.get(), proc},
                std::forward<Handler>(handler)
            );
        }
    };

    // start execution
    struct initiate_start_execution
    {
        template <class Handler, class ExecutionRequest>
        void operator()(
            Handler&& handler,
            any_stream* stream,
            connection_state* st,
            const ExecutionRequest& req,
            execution_processor* proc,
            diagnostics* diag
        )
        {
            auto getter = make_request_getter(req, get_shared_fields(*st));
            async_run_algo(
                *stream,
                *st,
                start_execution_algo_params{diag, getter.get(), proc},
                std::forward<Handler>(handler)
            );
        }
    };

public:
    BOOST_MYSQL_DECL connection_impl(std::size_t read_buff_size, std::unique_ptr<any_stream> stream);
    connection_impl(const connection_impl&) = delete;
    BOOST_MYSQL_DECL connection_impl(connection_impl&&) noexcept;
    connection_impl& operator=(const connection_impl&) = delete;
    BOOST_MYSQL_DECL connection_impl& operator=(connection_impl&&) noexcept;
    BOOST_MYSQL_DECL ~connection_impl();

    any_stream& stream() noexcept
    {
        BOOST_ASSERT(stream_);
        return *stream_;
    }

    const any_stream& stream() const noexcept
    {
        BOOST_ASSERT(stream_);
        return *stream_;
    }

    BOOST_MYSQL_DECL metadata_mode meta_mode() const noexcept;
    BOOST_MYSQL_DECL void set_meta_mode(metadata_mode v) noexcept;
    BOOST_MYSQL_DECL std::vector<field_view>& get_shared_fields() noexcept;
    BOOST_MYSQL_DECL bool ssl_active() const noexcept;
    BOOST_MYSQL_DECL bool backslash_escapes() const noexcept;
    BOOST_MYSQL_DECL system::result<character_set> current_character_set() const noexcept;

    // Generic algorithm
    template <class AlgoParams, class CompletionToken>
    auto async_run(AlgoParams params, CompletionToken&& token)
        -> decltype(asio::async_initiate<CompletionToken, completion_signature_t<AlgoParams>>(
            run_algo_initiation(),
            token,
            stream_.get(),
            st_.get(),
            params
        ))
    {
        return asio::async_initiate<CompletionToken, completion_signature_t<AlgoParams>>(
            run_algo_initiation(),
            token,
            stream_.get(),
            st_.get(),
            params
        );
    }

    template <class AlgoParams>
    typename AlgoParams::result_type run(AlgoParams params, error_code& ec)
    {
        return run_algo(*stream_, *st_, params, ec);
    }

    // Connect
    template <class EndpointType>
    void connect(
        const EndpointType& endpoint,
        const handshake_params& params,
        error_code& err,
        diagnostics& diag
    )
    {
        stream_->set_endpoint(&endpoint);
        run_algo(*stream_, *st_, make_params_connect(diag, params, is_secure_channel(endpoint)), err);
    }

    template <class EndpointType, class CompletionToken>
    auto async_connect(
        const EndpointType& endpoint,
        const handshake_params& params,
        diagnostics& diag,
        CompletionToken&& token
    )
        -> decltype(asio::async_initiate<CompletionToken, void(error_code)>(
            initiate_connect<EndpointType>(),
            token,
            stream_.get(),
            st_.get(),
            endpoint,
            params,
            &diag
        ))
    {
        return asio::async_initiate<CompletionToken, void(error_code)>(
            initiate_connect<EndpointType>(),
            token,
            stream_.get(),
            st_.get(),
            endpoint,
            params,
            &diag
        );
    }

    // Handshake
    handshake_algo_params make_params_handshake(const handshake_params& params, diagnostics& diag)
        const noexcept
    {
        return {&diag, params, false};
    }

    // Execute
    template <class ExecutionRequest, class ResultsType>
    void execute(const ExecutionRequest& req, ResultsType& result, error_code& err, diagnostics& diag)
    {
        auto getter = make_request_getter(req, get_shared_fields(*st_));
        run_algo(
            *stream_,
            *st_,
            execute_algo_params{&diag, getter.get(), &access::get_impl(result).get_interface()},
            err
        );
    }

    template <class ExecutionRequest, class ResultsType, class CompletionToken>
    auto async_execute(
        ExecutionRequest&& req,
        ResultsType& result,
        diagnostics& diag,
        CompletionToken&& token
    )
        -> decltype(asio::async_initiate<CompletionToken, void(error_code)>(
            initiate_execute(),
            token,
            stream_.get(),
            st_.get(),
            std::forward<ExecutionRequest>(req),
            &access::get_impl(result).get_interface(),
            &diag
        ))
    {
        return asio::async_initiate<CompletionToken, void(error_code)>(
            initiate_execute(),
            token,
            stream_.get(),
            st_.get(),
            std::forward<ExecutionRequest>(req),
            &access::get_impl(result).get_interface(),
            &diag
        );
    }

    // Start execution
    template <class ExecutionRequest, class ExecutionStateType>
    void start_execution(
        const ExecutionRequest& req,
        ExecutionStateType& exec_st,
        error_code& err,
        diagnostics& diag
    )
    {
        auto getter = make_request_getter(req, get_shared_fields(*st_));
        run_algo(
            *stream_,
            *st_,
            start_execution_algo_params{&diag, getter.get(), &access::get_impl(exec_st).get_interface()},
            err
        );
    }

    template <class ExecutionRequest, class ExecutionStateType, class CompletionToken>
    auto async_start_execution(
        ExecutionRequest&& req,
        ExecutionStateType& exec_st,
        diagnostics& diag,
        CompletionToken&& token
    )
        -> decltype(asio::async_initiate<CompletionToken, void(error_code)>(
            initiate_start_execution(),
            token,
            stream_.get(),
            st_.get(),
            std::forward<ExecutionRequest>(req),
            &access::get_impl(exec_st).get_interface(),
            &diag
        ))
    {
        return asio::async_initiate<CompletionToken, void(error_code)>(
            initiate_start_execution(),
            token,
            stream_.get(),
            st_.get(),
            std::forward<ExecutionRequest>(req),
            &access::get_impl(exec_st).get_interface(),
            &diag
        );
    }

    // Read some rows (dynamic)
    read_some_rows_dynamic_algo_params make_params_read_some_rows(execution_state& st, diagnostics& diag)
        const noexcept
    {
        return {&diag, &access::get_impl(st).get_interface()};
    }

    // Read some rows (static)
    template <class SpanRowType, class... RowType>
    read_some_rows_algo_params make_params_read_some_rows(
        static_execution_state<RowType...>& exec_st,
        span<SpanRowType> output,
        diagnostics& diag
    ) const noexcept
    {
        constexpr std::size_t index = get_type_index<SpanRowType, RowType...>();
        static_assert(index != index_not_found, "SpanRowType must be one of the types returned by the query");
        return {&diag, &access::get_impl(exec_st).get_interface(), output_ref(output, index)};
    }

    // Read resultset head
    template <class ExecutionStateType>
    read_resultset_head_algo_params make_params_read_resultset_head(ExecutionStateType& st, diagnostics& diag)
        const noexcept
    {
        return {&diag, &detail::access::get_impl(st).get_interface()};
    }

    // Close statement
    close_statement_algo_params make_params_close_statement(statement stmt, diagnostics& diag) const noexcept
    {
        return {&diag, stmt.id()};
    }

    // Set character set
    set_character_set_algo_params make_params_set_character_set(
        const character_set& charset,
        diagnostics& diag
    ) const noexcept
    {
        return {&diag, charset};
    }

    // Ping
    ping_algo_params make_params_ping(diagnostics& diag) const noexcept { return {&diag}; }

    // Reset connection
    reset_connection_algo_params make_params_reset_connection(
        diagnostics& diag,
        const character_set& charset = {}
    ) const noexcept
    {
        return {&diag, charset};
    }

    // Reset connection and issue a SET NAMES, using a pipeline.
    // Used internally by connection_pool.
    template <BOOST_ASIO_COMPLETION_TOKEN_FOR(void(::boost::mysql::error_code)) CompletionToken>
    auto async_reset_with_charset(const character_set& charset, CompletionToken&& token)
        BOOST_MYSQL_RETURN_TYPE(decltype(std::declval<connection_impl&>().async_run(
            std::declval<reset_connection_algo_params>(),
            std::forward<CompletionToken>(token)
        )))
    {
        return async_run(
            make_params_reset_connection(shared_diag(), charset),
            std::forward<CompletionToken>(token)
        );
    }

    // Quit connection
    quit_connection_algo_params make_params_quit(diagnostics& diag) const noexcept { return {&diag}; }

    // Close connection
    close_connection_algo_params make_params_close(diagnostics& diag) const noexcept { return {&diag}; }

    // TODO: get rid of this
    BOOST_MYSQL_DECL
    diagnostics& shared_diag() noexcept;
};

// To use some completion tokens, like deferred, in C++11, the old macros
// BOOST_ASIO_INITFN_AUTO_RESULT_TYPE are no longer enough.
// Helper typedefs to reduce duplication
template <class AlgoParams, class CompletionToken>
using async_run_t = decltype(std::declval<connection_impl&>()
                                 .async_run(std::declval<AlgoParams>(), std::declval<CompletionToken>()));

template <class EndpointType, class CompletionToken>
using async_connect_t = decltype(std::declval<connection_impl&>().async_connect(
    std::declval<const EndpointType&>(),
    std::declval<const handshake_params&>(),
    std::declval<diagnostics&>(),
    std::declval<CompletionToken>()
));

template <class ExecutionRequest, class ResultsType, class CompletionToken>
using async_execute_t = decltype(std::declval<connection_impl&>().async_execute(
    std::declval<ExecutionRequest>(),
    std::declval<ResultsType&>(),
    std::declval<diagnostics&>(),
    std::declval<CompletionToken>()
));

template <class ExecutionRequest, class ExecutionStateType, class CompletionToken>
using async_start_execution_t = decltype(std::declval<connection_impl&>().async_start_execution(
    std::declval<ExecutionRequest>(),
    std::declval<ExecutionStateType&>(),
    std::declval<diagnostics&>(),
    std::declval<CompletionToken>()
));

template <class CompletionToken>
using async_handshake_t = async_run_t<handshake_algo_params, CompletionToken>;

template <class CompletionToken>
using async_read_resultset_head_t = async_run_t<read_resultset_head_algo_params, CompletionToken>;

template <class CompletionToken>
using async_read_some_rows_dynamic_t = async_run_t<read_some_rows_dynamic_algo_params, CompletionToken>;

template <class CompletionToken>
using async_prepare_statement_t = async_run_t<prepare_statement_algo_params, CompletionToken>;

template <class CompletionToken>
using async_close_statement_t = async_run_t<close_statement_algo_params, CompletionToken>;

template <class CompletionToken>
using async_set_character_set_t = async_run_t<set_character_set_algo_params, CompletionToken>;

template <class CompletionToken>
using async_ping_t = async_run_t<ping_algo_params, CompletionToken>;

template <class CompletionToken>
using async_reset_connection_t = async_run_t<reset_connection_algo_params, CompletionToken>;

template <class CompletionToken>
using async_quit_connection_t = async_run_t<quit_connection_algo_params, CompletionToken>;

template <class CompletionToken>
using async_close_connection_t = async_run_t<close_connection_algo_params, CompletionToken>;

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/connection_impl.ipp>
#endif

#endif
