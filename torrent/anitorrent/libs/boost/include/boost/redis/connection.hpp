/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_CONNECTION_HPP
#define BOOST_REDIS_CONNECTION_HPP

#include <boost/redis/detail/connection_base.hpp>
#include <boost/redis/logger.hpp>
#include <boost/redis/config.hpp>
#include <boost/asio/io_context.hpp>
#include <boost/asio/coroutine.hpp>
#include <boost/asio/steady_timer.hpp>
#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/any_completion_handler.hpp>

#include <chrono>
#include <memory>
#include <limits>

namespace boost::redis {
namespace detail
{
template <class Connection, class Logger>
struct reconnection_op {
   Connection* conn_ = nullptr;
   Logger logger_;
   asio::coroutine coro_{};

   template <class Self>
   void operator()(Self& self, system::error_code ec = {})
   {
      BOOST_ASIO_CORO_REENTER (coro_) for (;;)
      {
         BOOST_ASIO_CORO_YIELD
         conn_->impl_.async_run(conn_->cfg_, logger_, std::move(self));
         conn_->cancel(operation::receive);
         logger_.on_connection_lost(ec);
         if (!conn_->will_reconnect() || is_cancelled(self)) {
            conn_->cancel(operation::reconnection);
            self.complete(!!ec ? ec : asio::error::operation_aborted);
            return;
         }

         conn_->timer_.expires_after(conn_->cfg_.reconnect_wait_interval);
         BOOST_ASIO_CORO_YIELD
         conn_->timer_.async_wait(std::move(self));
         BOOST_REDIS_CHECK_OP0(;)
         if (!conn_->will_reconnect()) {
            self.complete(asio::error::operation_aborted);
            return;
         }
         conn_->reset_stream();
      }
   }
};
} // detail

/** @brief A SSL connection to the Redis server.
 *  @ingroup high-level-api
 *
 *  This class keeps a healthy connection to the Redis instance where
 *  commands can be sent at any time. For more details, please see the
 *  documentation of each individual function.
 *
 *  @tparam Socket The socket type e.g. asio::ip::tcp::socket.
 *
 */
template <class Executor>
class basic_connection {
public:
   /// Executor type.
   using executor_type = Executor;

   /// Returns the underlying executor.
   executor_type get_executor() noexcept
      { return impl_.get_executor(); }

   /// Rebinds the socket type to another executor.
   template <class Executor1>
   struct rebind_executor
   {
      /// The connection type when rebound to the specified executor.
      using other = basic_connection<Executor1>;
   };

   /** @brief Constructor
    *
    *  @param ex Executor on which connection operation will run.
    *  @param ctx SSL context.
    *  @param max_read_size Maximum read size that is passed to
    *  the internal `asio::dynamic_buffer` constructor.
    */
   explicit
   basic_connection(
      executor_type ex,
      asio::ssl::context ctx = asio::ssl::context{asio::ssl::context::tlsv12_client},
      std::size_t max_read_size = (std::numeric_limits<std::size_t>::max)())
   : impl_{ex, std::move(ctx), max_read_size}
   , timer_{ex}
   { }

   /// Contructs from a context.
   explicit
   basic_connection(
      asio::io_context& ioc,
      asio::ssl::context ctx = asio::ssl::context{asio::ssl::context::tlsv12_client},
      std::size_t max_read_size = (std::numeric_limits<std::size_t>::max)())
   : basic_connection(ioc.get_executor(), std::move(ctx), max_read_size)
   { }

   /** @brief Starts underlying connection operations.
    *
    *  This member function provides the following functionality
    *
    *  1. Resolve the address passed on `boost::redis::config::addr`.
    *  2. Connect to one of the results obtained in the resolve operation.
    *  3. Send a [HELLO](https://redis.io/commands/hello/) command where each of its parameters are read from `cfg`.
    *  4. Start a health-check operation where ping commands are sent
    *     at intervals specified in
    *     `boost::redis::config::health_check_interval`.  The message passed to
    *     `PING` will be `boost::redis::config::health_check_id`.  Passing a
    *     timeout with value zero will disable health-checks.  If the Redis
    *     server does not respond to a health-check within two times the value
    *     specified here, it will be considered unresponsive and the connection
    *     will be closed and a new connection will be stablished.
    *  5. Starts read and write operations with the Redis
    *  server. More specifically it will trigger the write of all
    *  requests i.e. calls to `async_exec` that happened prior to this
    *  call.
    *
    *  When a connection is lost for any reason, a new one is
    *  stablished automatically. To disable reconnection call
    *  `boost::redis::connection::cancel(operation::reconnection)`.
    *
    *  @param cfg Configuration paramters.
    *  @param l Logger object. The interface expected is specified in the class `boost::redis::logger`.
    *  @param token Completion token.
    *
    *  The completion token must have the following signature
    *
    *  @code
    *  void f(system::error_code);
    *  @endcode
    *
    *  For example on how to call this function refer to
    *  cpp20_intro.cpp or any other example.
    */
   template <
      class Logger = logger,
      class CompletionToken = asio::default_completion_token_t<executor_type>>
   auto
   async_run(
      config const& cfg = {},
      Logger l = Logger{},
      CompletionToken token = CompletionToken{})
   {
      using this_type = basic_connection<executor_type>;

      cfg_ = cfg;
      l.set_prefix(cfg_.log_prefix);
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(detail::reconnection_op<this_type, Logger>{this, l}, token, timer_);
   }

   /** @brief Receives server side pushes asynchronously.
    *
    *  When pushes arrive and there is no `async_receive` operation in
    *  progress, pushed data, requests, and responses will be paused
    *  until `async_receive` is called again.  Apps will usually want
    *  to call `async_receive` in a loop. 
    *
    *  To cancel an ongoing receive operation apps should call
    *  `connection::cancel(operation::receive)`.
    *
    *  @param token Completion token.
    *
    *  For an example see cpp20_subscriber.cpp. The completion token must
    *  have the following signature
    *
    *  @code
    *  void f(system::error_code, std::size_t);
    *  @endcode
    *
    *  Where the second parameter is the size of the push received in
    *  bytes.
    */
   template <class CompletionToken = asio::default_completion_token_t<executor_type>>
   auto async_receive(CompletionToken token = CompletionToken{})
      { return impl_.async_receive(std::move(token)); }

   
   /** @brief Receives server pushes synchronously without blocking.
    *
    *  Receives a server push synchronously by calling `try_receive` on
    *  the underlying channel. If the operation fails because
    *  `try_receive` returns `false`, `ec` will be set to
    *  `boost::redis::error::sync_receive_push_failed`.
    *
    *  @param ec Contains the error if any occurred.
    *
    *  @returns The number of bytes read from the socket.
    */
   std::size_t receive(system::error_code& ec)
   {
      return impl_.receive(ec);
   }

   template <
      class Response = ignore_t,
      class CompletionToken = asio::default_completion_token_t<executor_type>
   >
   [[deprecated("Set the response with set_receive_response and use the other overload.")]]
   auto
   async_receive(
      Response& response,
      CompletionToken token = CompletionToken{})
   {
      return impl_.async_receive(response, token);
   }

   /** @brief Executes commands on the Redis server asynchronously.
    *
    *  This function sends a request to the Redis server and waits for
    *  the responses to each individual command in the request. If the
    *  request contains only commands that don't expect a response,
    *  the completion occurs after it has been written to the
    *  underlying stream.  Multiple concurrent calls to this function
    *  will be automatically queued by the implementation.
    *
    *  @param req Request.
    *  @param resp Response.
    *  @param token Completion token.
    *
    *  For an example see cpp20_echo_server.cpp. The completion token must
    *  have the following signature
    *
    *  @code
    *  void f(system::error_code, std::size_t);
    *  @endcode
    *
    *  Where the second parameter is the size of the response received
    *  in bytes.
    */
   template <
      class Response = ignore_t,
      class CompletionToken = asio::default_completion_token_t<executor_type>
   >
   auto
   async_exec(
      request const& req,
      Response& resp = ignore,
      CompletionToken&& token = CompletionToken{})
   {
      return impl_.async_exec(req, resp, std::forward<CompletionToken>(token));
   }

   /** @brief Cancel operations.
    *
    *  @li `operation::exec`: Cancels operations started with
    *  `async_exec`. Affects only requests that haven't been written
    *  yet.
    *  @li operation::run: Cancels the `async_run` operation.
    *  @li operation::receive: Cancels any ongoing calls to `async_receive`.
    *  @li operation::all: Cancels all operations listed above.
    *
    *  @param op: The operation to be cancelled.
    */
   void cancel(operation op = operation::all)
   {
      switch (op) {
         case operation::reconnection:
         case operation::all:
            cfg_.reconnect_wait_interval = std::chrono::seconds::zero();
            timer_.cancel();
            break;
         default: /* ignore */;
      }

      impl_.cancel(op);
   }

   /// Returns true if the connection was canceled.
   bool will_reconnect() const noexcept
      { return cfg_.reconnect_wait_interval != std::chrono::seconds::zero();}

   /// Returns the ssl context.
   auto const& get_ssl_context() const noexcept
      { return impl_.get_ssl_context();}

   /// Resets the underlying stream.
   void reset_stream()
      { impl_.reset_stream(); }

   /// Returns a reference to the next layer.
   auto& next_layer() noexcept
      { return impl_.next_layer(); }

   /// Returns a const reference to the next layer.
   auto const& next_layer() const noexcept
      { return impl_.next_layer(); }

   /// Sets the response object of `async_receive` operations.
   template <class Response>
   void set_receive_response(Response& response)
      { impl_.set_receive_response(response); }

   /// Returns connection usage information.
   usage get_usage() const noexcept
      { return impl_.get_usage(); }

private:
   using timer_type =
      asio::basic_waitable_timer<
         std::chrono::steady_clock,
         asio::wait_traits<std::chrono::steady_clock>,
         Executor>;

   template <class, class> friend struct detail::reconnection_op;

   config cfg_;
   detail::connection_base<executor_type> impl_;
   timer_type timer_;
};

/** \brief A basic_connection that type erases the executor.
 *  \ingroup high-level-api
 *
 *  This connection type uses the asio::any_io_executor and
 *  asio::any_completion_token to reduce compilation times.
 *
 *  For documentaiton of each member function see
 *  `boost::redis::basic_connection`.
 */
class connection {
public:
   /// Executor type.
   using executor_type = asio::any_io_executor;

   /// Contructs from an executor.
   explicit
   connection(
      executor_type ex,
      asio::ssl::context ctx = asio::ssl::context{asio::ssl::context::tlsv12_client},
      std::size_t max_read_size = (std::numeric_limits<std::size_t>::max)());

   /// Contructs from a context.
   explicit
   connection(
      asio::io_context& ioc,
      asio::ssl::context ctx = asio::ssl::context{asio::ssl::context::tlsv12_client},
      std::size_t max_read_size = (std::numeric_limits<std::size_t>::max)());

   /// Returns the underlying executor.
   executor_type get_executor() noexcept
      { return impl_.get_executor(); }

   /// Calls `boost::redis::basic_connection::async_run`.
   template <class CompletionToken>
   auto async_run(config const& cfg, logger l, CompletionToken token)
   {
      return asio::async_initiate<
         CompletionToken, void(boost::system::error_code)>(
            [](auto handler, connection* self, config const* cfg, logger l)
            {
               self->async_run_impl(*cfg, l, std::move(handler));
            }, token, this, &cfg, l);
   }

   /// Calls `boost::redis::basic_connection::async_receive`.
   template <class Response, class CompletionToken>
   [[deprecated("Set the response with set_receive_response and use the other overload.")]]
   auto async_receive(Response& response, CompletionToken token)
   {
      return impl_.async_receive(response, std::move(token));
   }

   /// Calls `boost::redis::basic_connection::async_receive`.
   template <class CompletionToken>
   auto async_receive(CompletionToken token)
      { return impl_.async_receive(std::move(token)); }

   /// Calls `boost::redis::basic_connection::receive`.
   std::size_t receive(system::error_code& ec)
   {
      return impl_.receive(ec);
   }

   /// Calls `boost::redis::basic_connection::async_exec`.
   template <class Response, class CompletionToken>
   auto async_exec(request const& req, Response& resp, CompletionToken token)
   {
      return impl_.async_exec(req, resp, std::move(token));
   }

   /// Calls `boost::redis::basic_connection::cancel`.
   void cancel(operation op = operation::all);

   /// Calls `boost::redis::basic_connection::will_reconnect`.
   bool will_reconnect() const noexcept
      { return impl_.will_reconnect();}

   /// Calls `boost::redis::basic_connection::next_layer`.
   auto& next_layer() noexcept
      { return impl_.next_layer(); }

   /// Calls `boost::redis::basic_connection::next_layer`.
   auto const& next_layer() const noexcept
      { return impl_.next_layer(); }

   /// Calls `boost::redis::basic_connection::reset_stream`.
   void reset_stream()
      { impl_.reset_stream();}

   /// Sets the response object of `async_receive` operations.
   template <class Response>
   void set_receive_response(Response& response)
      { impl_.set_receive_response(response); }

   /// Returns connection usage information.
   usage get_usage() const noexcept
      { return impl_.get_usage(); }

   /// Returns the ssl context.
   auto const& get_ssl_context() const noexcept
      { return impl_.get_ssl_context();}

private:
   void
   async_run_impl(
      config const& cfg,
      logger l,
      asio::any_completion_handler<void(boost::system::error_code)> token);

   basic_connection<executor_type> impl_;
};

} // boost::redis

#endif // BOOST_REDIS_CONNECTION_HPP
