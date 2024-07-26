/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_CONNECTION_BASE_HPP
#define BOOST_REDIS_CONNECTION_BASE_HPP

#include <boost/redis/adapter/adapt.hpp>
#include <boost/redis/detail/helper.hpp>
#include <boost/redis/error.hpp>
#include <boost/redis/operation.hpp>
#include <boost/redis/request.hpp>
#include <boost/redis/resp3/type.hpp>
#include <boost/redis/config.hpp>
#include <boost/redis/detail/runner.hpp>
#include <boost/redis/usage.hpp>

#include <boost/system.hpp>
#include <boost/asio/basic_stream_socket.hpp>
#include <boost/asio/bind_executor.hpp>
#include <boost/asio/experimental/parallel_group.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/steady_timer.hpp>
#include <boost/asio/write.hpp>
#include <boost/assert.hpp>
#include <boost/core/ignore_unused.hpp>
#include <boost/asio/ssl/stream.hpp>
#include <boost/asio/read_until.hpp>
#include <boost/asio/buffer.hpp>
#include <boost/asio/experimental/channel.hpp>

#include <algorithm>
#include <array>
#include <chrono>
#include <deque>
#include <memory>
#include <string_view>
#include <type_traits>
#include <functional>

namespace boost::redis::detail
{

template <class DynamicBuffer>
std::string_view buffer_view(DynamicBuffer buf) noexcept
{
   char const* start = static_cast<char const*>(buf.data(0, buf.size()).data());
   return std::string_view{start, std::size(buf)};
}

template <class AsyncReadStream, class DynamicBuffer>
class append_some_op {
private:
   AsyncReadStream& stream_;
   DynamicBuffer buf_;
   std::size_t size_ = 0;
   std::size_t tmp_ = 0;
   asio::coroutine coro_{};

public:
   append_some_op(AsyncReadStream& stream, DynamicBuffer buf, std::size_t size)
   : stream_ {stream}
   , buf_ {std::move(buf)}
   , size_{size}
   { }

   template <class Self>
   void operator()( Self& self
                  , system::error_code ec = {}
                  , std::size_t n = 0)
   {
      BOOST_ASIO_CORO_REENTER (coro_)
      {
         tmp_ = buf_.size();
         buf_.grow(size_);

         BOOST_ASIO_CORO_YIELD
         stream_.async_read_some(buf_.data(tmp_, size_), std::move(self));
         if (ec) {
            self.complete(ec, 0);
            return;
         }

         buf_.shrink(buf_.size() - tmp_ - n);
         self.complete({}, n);
      }
   }
};

template <class AsyncReadStream, class DynamicBuffer, class CompletionToken>
auto
async_append_some(
   AsyncReadStream& stream,
   DynamicBuffer buffer,
   std::size_t size,
   CompletionToken&& token)
{
   return asio::async_compose
      < CompletionToken
      , void(system::error_code, std::size_t)
      >(append_some_op<AsyncReadStream, DynamicBuffer> {stream, buffer, size}, token, stream);
}

template <class Conn>
struct exec_op {
   using req_info_type = typename Conn::req_info;
   using adapter_type = typename Conn::adapter_type;

   Conn* conn_ = nullptr;
   std::shared_ptr<req_info_type> info_ = nullptr;
   asio::coroutine coro{};

   template <class Self>
   void operator()(Self& self , system::error_code ec = {}, std::size_t = 0)
   {
      BOOST_ASIO_CORO_REENTER (coro)
      {
         // Check whether the user wants to wait for the connection to
         // be stablished.
         if (info_->req_->get_config().cancel_if_not_connected && !conn_->is_open()) {
            BOOST_ASIO_CORO_YIELD
            asio::post(std::move(self));
            return self.complete(error::not_connected, 0);
         }

         conn_->add_request_info(info_);

EXEC_OP_WAIT:
         BOOST_ASIO_CORO_YIELD
         info_->async_wait(std::move(self));

         if (info_->ec_) {
            self.complete(info_->ec_, 0);
            return;
         }

         if (info_->stop_requested()) {
            // Don't have to call remove_request as it has already
            // been by cancel(exec).
            return self.complete(asio::error::operation_aborted, 0);
         }

         if (is_cancelled(self)) {
            if (!info_->is_waiting()) {
               using c_t = asio::cancellation_type;
               auto const c = self.get_cancellation_state().cancelled();
               if ((c & c_t::terminal) != c_t::none) {
                  // Cancellation requires closing the connection
                  // otherwise it stays in inconsistent state.
                  conn_->cancel(operation::run);
                  return self.complete(asio::error::operation_aborted, 0);
               } else {
                  // Can't implement other cancelation types, ignoring.
                  self.get_cancellation_state().clear();

                  // TODO: Find out a better way to ignore
                  // cancelation.
                  goto EXEC_OP_WAIT;
               }
            } else {
               // Cancelation can be honored.
               conn_->remove_request(info_);
               self.complete(asio::error::operation_aborted, 0);
               return;
            }
         }

         self.complete(info_->ec_, info_->read_size_);
      }
   }
};

template <class Conn, class Logger>
struct run_op {
   Conn* conn = nullptr;
   Logger logger_;
   asio::coroutine coro{};

   template <class Self>
   void operator()( Self& self
                  , std::array<std::size_t, 2> order = {}
                  , system::error_code ec0 = {}
                  , system::error_code ec1 = {})
   {
      BOOST_ASIO_CORO_REENTER (coro)
      {
         conn->reset();

         BOOST_ASIO_CORO_YIELD
         asio::experimental::make_parallel_group(
            [this](auto token) { return conn->reader(logger_, token);},
            [this](auto token) { return conn->writer(logger_, token);}
         ).async_wait(
            asio::experimental::wait_for_one(),
            std::move(self));

         if (is_cancelled(self)) {
            logger_.trace("run-op: canceled. Exiting ...");
            self.complete(asio::error::operation_aborted);
            return;
         }

         logger_.on_run(ec0, ec1);

         switch (order[0]) {
           case 0: self.complete(ec0); break;
           case 1: self.complete(ec1); break;
           default: BOOST_ASSERT(false);
         }
      }
   }
};

template <class Conn, class Logger>
struct writer_op {
   Conn* conn_;
   Logger logger_;
   asio::coroutine coro{};

   template <class Self>
   void operator()( Self& self
                  , system::error_code ec = {}
                  , std::size_t n = 0)
   {
      ignore_unused(n);

      BOOST_ASIO_CORO_REENTER (coro) for (;;)
      {
         while (conn_->coalesce_requests()) {
            if (conn_->use_ssl())
               BOOST_ASIO_CORO_YIELD asio::async_write(conn_->next_layer(), asio::buffer(conn_->write_buffer_), std::move(self));
            else
               BOOST_ASIO_CORO_YIELD asio::async_write(conn_->next_layer().next_layer(), asio::buffer(conn_->write_buffer_), std::move(self));

            logger_.on_write(ec, conn_->write_buffer_);

            if (ec) {
               logger_.trace("writer-op: error. Exiting ...");
               conn_->cancel(operation::run);
               self.complete(ec);
               return;
            }

            if (is_cancelled(self)) {
               logger_.trace("writer-op: canceled. Exiting ...");
               self.complete(asio::error::operation_aborted);
               return;
            }

            conn_->on_write();

            // A socket.close() may have been called while a
            // successful write might had already been queued, so we
            // have to check here before proceeding.
            if (!conn_->is_open()) {
               logger_.trace("writer-op: canceled (2). Exiting ...");
               self.complete({});
               return;
            }
         }

         BOOST_ASIO_CORO_YIELD
         conn_->writer_timer_.async_wait(std::move(self));
         if (!conn_->is_open() || is_cancelled(self)) {
            logger_.trace("writer-op: canceled (3). Exiting ...");
            // Notice this is not an error of the op, stoping was
            // requested from the outside, so we complete with
            // success.
            self.complete({});
            return;
         }
      }
   }
};

template <class Conn, class Logger>
struct reader_op {
   using parse_result = typename Conn::parse_result;
   using parse_ret_type = typename Conn::parse_ret_type;
   Conn* conn_;
   Logger logger_;
   parse_ret_type res_{parse_result::resp, 0};
   asio::coroutine coro{};

   template <class Self>
   void operator()( Self& self
                  , system::error_code ec = {}
                  , std::size_t n = 0)
   {
      ignore_unused(n);

      BOOST_ASIO_CORO_REENTER (coro) for (;;)
      {
         // Appends some data to the buffer if necessary.
         if ((res_.first == parse_result::needs_more) || std::empty(conn_->read_buffer_)) {
            if (conn_->use_ssl()) {
               BOOST_ASIO_CORO_YIELD
               async_append_some(
                  conn_->next_layer(),
                  conn_->dbuf_,
                  conn_->get_suggested_buffer_growth(),
                  std::move(self));
            } else {
               BOOST_ASIO_CORO_YIELD
               async_append_some(
                  conn_->next_layer().next_layer(),
                  conn_->dbuf_,
                  conn_->get_suggested_buffer_growth(),
                  std::move(self));
            }

            logger_.on_read(ec, n);

            // EOF is not treated as error.
            if (ec == asio::error::eof) {
               logger_.trace("reader-op: EOF received. Exiting ...");
               conn_->cancel(operation::run);
               return self.complete({}); // EOFINAE: EOF is not an error.
            }

            // The connection is not viable after an error.
            if (ec) {
               logger_.trace("reader-op: error. Exiting ...");
               conn_->cancel(operation::run);
               self.complete(ec);
               return;
            }

            // Somebody might have canceled implicitly or explicitly
            // while we were suspended and after queueing so we have to
            // check.
            if (!conn_->is_open() || is_cancelled(self)) {
               logger_.trace("reader-op: canceled. Exiting ...");
               self.complete(ec);
               return;
            }
         }

         res_ = conn_->on_read(buffer_view(conn_->dbuf_), ec);
         if (ec) {
            logger_.trace("reader-op: parse error. Exiting ...");
            conn_->cancel(operation::run);
            self.complete(ec);
            return;
         }

         if (res_.first == parse_result::push) {
            if (!conn_->receive_channel_.try_send(ec, res_.second)) {
               BOOST_ASIO_CORO_YIELD
               conn_->receive_channel_.async_send(ec, res_.second, std::move(self));
            }

            if (ec) {
               logger_.trace("reader-op: error. Exiting ...");
               conn_->cancel(operation::run);
               self.complete(ec);
               return;
            }

            if (!conn_->is_open() || is_cancelled(self)) {
               logger_.trace("reader-op: canceled (2). Exiting ...");
               self.complete(asio::error::operation_aborted);
               return;
            }

         }
      }
   }
};

/** @brief Base class for high level Redis asynchronous connections.
 *  @ingroup high-level-api
 *
 *  @tparam Executor The executor type.
 *
 */
template <class Executor>
class connection_base {
public:
   /// Executor type
   using executor_type = Executor;

   /// Type of the next layer
   using next_layer_type = asio::ssl::stream<asio::basic_stream_socket<asio::ip::tcp, Executor>>;

   using clock_type = std::chrono::steady_clock;
   using clock_traits_type = asio::wait_traits<clock_type>;
   using timer_type = asio::basic_waitable_timer<clock_type, clock_traits_type, executor_type>;

   using this_type = connection_base<Executor>;

   /// Constructs from an executor.
   connection_base(
      executor_type ex,
      asio::ssl::context ctx,
      std::size_t max_read_size)
   : ctx_{std::move(ctx)}
   , stream_{std::make_unique<next_layer_type>(ex, ctx_)}
   , writer_timer_{ex}
   , receive_channel_{ex, 256}
   , runner_{ex, {}}
   , dbuf_{read_buffer_, max_read_size}
   {
      set_receive_response(ignore);
      writer_timer_.expires_at((std::chrono::steady_clock::time_point::max)());
   }

   /// Returns the ssl context.
   auto const& get_ssl_context() const noexcept
      { return ctx_;}

   /// Resets the underlying stream.
   void reset_stream()
   {
      stream_ = std::make_unique<next_layer_type>(writer_timer_.get_executor(), ctx_);
   }

   /// Returns a reference to the next layer.
   auto& next_layer() noexcept { return *stream_; }

   /// Returns a const reference to the next layer.
   auto const& next_layer() const noexcept { return *stream_; }

   /// Returns the associated executor.
   auto get_executor() {return writer_timer_.get_executor();}

   /// Cancels specific operations.
   void cancel(operation op)
   {
      runner_.cancel(op);
      if (op == operation::all) {
         cancel_impl(operation::run);
         cancel_impl(operation::receive);
         cancel_impl(operation::exec);
         return;
      } 

      cancel_impl(op);
   }

   template <class Response, class CompletionToken>
   auto async_exec(request const& req, Response& resp, CompletionToken token)
   {
      using namespace boost::redis::adapter;
      auto f = boost_redis_adapt(resp);
      BOOST_ASSERT_MSG(req.get_expected_responses() <= f.get_supported_response_size(), "Request and response have incompatible sizes.");

      auto info = std::make_shared<req_info>(req, f, get_executor());

      return asio::async_compose
         < CompletionToken
         , void(system::error_code, std::size_t)
         >(exec_op<this_type>{this, info}, token, writer_timer_);
   }

   template <class Response, class CompletionToken>
   [[deprecated("Set the response with set_receive_response and use the other overload.")]]
   auto async_receive(Response& response, CompletionToken token)
   {
      set_receive_response(response);
      return receive_channel_.async_receive(std::move(token));
   }

   template <class CompletionToken>
   auto async_receive(CompletionToken token)
      { return receive_channel_.async_receive(std::move(token)); }

   std::size_t receive(system::error_code& ec)
   {
      std::size_t size = 0;

      auto f = [&](system::error_code const& ec2, std::size_t n)
      {
         ec = ec2;
         size = n;
      };

      auto const res = receive_channel_.try_receive(f);
      if (ec)
         return 0;

      if (!res)
         ec = error::sync_receive_push_failed;

      return size;
   }

   template <class Logger, class CompletionToken>
   auto async_run(config const& cfg, Logger l, CompletionToken token)
   {
      runner_.set_config(cfg);
      l.set_prefix(runner_.get_config().log_prefix);
      return runner_.async_run(*this, l, std::move(token));
   }

   template <class Response>
   void set_receive_response(Response& response)
   {
      using namespace boost::redis::adapter;
      auto g = boost_redis_adapt(response);
      receive_adapter_ = adapter::detail::make_adapter_wrapper(g);
   }

   usage get_usage() const noexcept
      { return usage_; }

   auto run_is_canceled() const noexcept
      { return cancel_run_called_; }

private:
   using receive_channel_type = asio::experimental::channel<executor_type, void(system::error_code, std::size_t)>;
   using runner_type = runner<executor_type>;
   using adapter_type = std::function<void(std::size_t, resp3::basic_node<std::string_view> const&, system::error_code&)>;
   using receiver_adapter_type = std::function<void(resp3::basic_node<std::string_view> const&, system::error_code&)>;
   using exec_notifier_type = receive_channel_type;

   auto use_ssl() const noexcept
      { return runner_.get_config().use_ssl;}

   auto cancel_on_conn_lost() -> std::size_t
   {
      // Must return false if the request should be removed.
      auto cond = [](auto const& ptr)
      {
         BOOST_ASSERT(ptr != nullptr);

         if (ptr->is_waiting()) {
            return !ptr->req_->get_config().cancel_on_connection_lost;
         } else {
            return !ptr->req_->get_config().cancel_if_unresponded;
         }
      };

      auto point = std::stable_partition(std::begin(reqs_), std::end(reqs_), cond);

      auto const ret = std::distance(point, std::end(reqs_));

      std::for_each(point, std::end(reqs_), [](auto const& ptr) {
         ptr->stop();
      });

      reqs_.erase(point, std::end(reqs_));

      std::for_each(std::begin(reqs_), std::end(reqs_), [](auto const& ptr) {
         return ptr->mark_waiting();
      });

      return ret;
   }

   auto cancel_unwritten_requests() -> std::size_t
   {
      auto f = [](auto const& ptr)
      {
         BOOST_ASSERT(ptr != nullptr);
         return !ptr->is_waiting();
      };

      auto point = std::stable_partition(std::begin(reqs_), std::end(reqs_), f);

      auto const ret = std::distance(point, std::end(reqs_));

      std::for_each(point, std::end(reqs_), [](auto const& ptr) {
         ptr->stop();
      });

      reqs_.erase(point, std::end(reqs_));
      return ret;
   }

   void cancel_impl(operation op)
   {
      switch (op) {
         case operation::exec:
         {
            cancel_unwritten_requests();
         } break;
         case operation::run:
         {
            // Protects the code below from being called more than
            // once, see https://github.com/boostorg/redis/issues/181
            if (std::exchange(cancel_run_called_, true)) {
               return;
            }

            close();
            writer_timer_.cancel();
            receive_channel_.cancel();
            cancel_on_conn_lost();
         } break;
         case operation::receive:
         {
            receive_channel_.cancel();
         } break;
         default: /* ignore */;
      }
   }

   void on_write()
   {
      // We have to clear the payload right after writing it to use it
      // as a flag that informs there is no ongoing write.
      write_buffer_.clear();

      // Notice this must come before the for-each below.
      cancel_push_requests();

      // There is small optimization possible here: traverse only the
      // partition of unwritten requests instead of them all.
      std::for_each(std::begin(reqs_), std::end(reqs_), [](auto const& ptr) {
         BOOST_ASSERT_MSG(ptr != nullptr, "Expects non-null pointer.");
         if (ptr->is_staged()) {
            ptr->mark_written();
         }
      });
   }

   struct req_info {
   public:
      using node_type = resp3::basic_node<std::string_view>;
      using wrapped_adapter_type = std::function<void(node_type const&, system::error_code&)>;

      explicit req_info(request const& req, adapter_type adapter, executor_type ex)
      : notifier_{ex, 1}
      , req_{&req}
      , adapter_{}
      , expected_responses_{req.get_expected_responses()}
      , status_{status::waiting}
      , ec_{{}}
      , read_size_{0}
      {
         adapter_ = [this, adapter](node_type const& nd, system::error_code& ec)
         {
            auto const i = req_->get_expected_responses() - expected_responses_;
            adapter(i, nd, ec);
         };
      }

      auto proceed()
      {
         notifier_.try_send(std::error_code{}, 0);
      }

      void stop()
      {
         notifier_.close();
      }

      [[nodiscard]] auto is_waiting() const noexcept
         { return status_ == status::waiting; }

      [[nodiscard]] auto is_written() const noexcept
         { return status_ == status::written; }

      [[nodiscard]] auto is_staged() const noexcept
         { return status_ == status::staged; }

      void mark_written() noexcept
         { status_ = status::written; }

      void mark_staged() noexcept
         { status_ = status::staged; }

      void mark_waiting() noexcept
         { status_ = status::waiting; }

      [[nodiscard]] auto stop_requested() const noexcept
         { return !notifier_.is_open();}

      template <class CompletionToken>
      auto async_wait(CompletionToken token)
      {
         return notifier_.async_receive(std::move(token));
      }

   //private:
      enum class status
      { waiting
      , staged
      , written
      };

      exec_notifier_type notifier_;
      request const* req_;
      wrapped_adapter_type adapter_;

      // Contains the number of commands that haven't been read yet.
      std::size_t expected_responses_;
      status status_;

      system::error_code ec_;
      std::size_t read_size_;
   };

   void remove_request(std::shared_ptr<req_info> const& info)
   {
      reqs_.erase(std::remove(std::begin(reqs_), std::end(reqs_), info));
   }

   using reqs_type = std::deque<std::shared_ptr<req_info>>;

   template <class, class> friend struct reader_op;
   template <class, class> friend struct writer_op;
   template <class, class> friend struct run_op;
   template <class> friend struct exec_op;
   template <class, class, class> friend struct run_all_op;

   void cancel_push_requests()
   {
      auto point = std::stable_partition(std::begin(reqs_), std::end(reqs_), [](auto const& ptr) {
            return !(ptr->is_staged() && ptr->req_->get_expected_responses() == 0);
      });

      std::for_each(point, std::end(reqs_), [](auto const& ptr) {
         ptr->proceed();
      });

      reqs_.erase(point, std::end(reqs_));
   }

   [[nodiscard]] bool is_writing() const noexcept
   {
      return !write_buffer_.empty();
   }

   void add_request_info(std::shared_ptr<req_info> const& info)
   {
      reqs_.push_back(info);

      if (info->req_->has_hello_priority()) {
         auto rend = std::partition_point(std::rbegin(reqs_), std::rend(reqs_), [](auto const& e) {
               return e->is_waiting();
         });

         std::rotate(std::rbegin(reqs_), std::rbegin(reqs_) + 1, rend);
      }

      if (is_open() && !is_writing())
         writer_timer_.cancel();
   }

   template <class CompletionToken, class Logger>
   auto reader(Logger l, CompletionToken&& token)
   {
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(reader_op<this_type, Logger>{this, l}, token, writer_timer_);
   }

   template <class CompletionToken, class Logger>
   auto writer(Logger l, CompletionToken&& token)
   {
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(writer_op<this_type, Logger>{this, l}, token, writer_timer_);
   }

   template <class Logger, class CompletionToken>
   auto async_run_lean(config const& cfg, Logger l, CompletionToken token)
   {
      runner_.set_config(cfg);
      l.set_prefix(runner_.get_config().log_prefix);
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(run_op<this_type, Logger>{this, l}, token, writer_timer_);
   }

   [[nodiscard]] bool coalesce_requests()
   {
      // Coalesces the requests and marks them staged. After a
      // successful write staged requests will be marked as written.
      auto const point = std::partition_point(std::cbegin(reqs_), std::cend(reqs_), [](auto const& ri) {
            return !ri->is_waiting();
      });

      std::for_each(point, std::cend(reqs_), [this](auto const& ri) {
         // Stage the request.
         write_buffer_ += ri->req_->payload();
         ri->mark_staged();
         usage_.commands_sent += ri->expected_responses_;
      });

      usage_.bytes_sent += std::size(write_buffer_);

      return point != std::cend(reqs_);
   }

   bool is_waiting_response() const noexcept
   {
      if (std::empty(reqs_))
         return false;

      // Under load and on low-latency networks we might start
      // receiving responses before the write operation completed and
      // the request is still maked as staged and not written.  See
      // https://github.com/boostorg/redis/issues/170
      return !reqs_.front()->is_waiting();
   }

   void close()
   {
      if (stream_->next_layer().is_open()) {
         system::error_code ec;
         stream_->next_layer().close(ec);
      }
   }

   auto is_open() const noexcept { return stream_->next_layer().is_open(); }
   auto& lowest_layer() noexcept { return stream_->lowest_layer(); }

   auto is_next_push()
   {
      BOOST_ASSERT(!read_buffer_.empty());

      // Useful links to understand the heuristics below.
      //
      // - https://github.com/redis/redis/issues/11784
      // - https://github.com/redis/redis/issues/6426
      // - https://github.com/boostorg/redis/issues/170

      // The message's resp3 type is a push.
      if (resp3::to_type(read_buffer_.front()) == resp3::type::push)
         return true;

      // This is non-push type and the requests queue is empty. I have
      // noticed this is possible, for example with -MISCONF. I don't
      // know why they are not sent with a push type so we can
      // distinguish them from responses to commands. If we are lucky
      // enough to receive them when the command queue is empty they
      // can be treated as server pushes, otherwise it is impossible
      // to handle them properly
      if (reqs_.empty())
         return true;

      // The request does not expect any response but we got one. This
      // may happen if for example, subscribe with wrong syntax.
      if (reqs_.front()->expected_responses_ == 0)
         return true;

      // Added to deal with MONITOR and also to fix PR170 which
      // happens under load and on low-latency networks, where we
      // might start receiving responses before the write operation
      // completed and the request is still maked as staged and not
      // written.
      return reqs_.front()->is_waiting();
   }

   auto get_suggested_buffer_growth() const noexcept
   {
      return parser_.get_suggested_buffer_growth(4096);
   }

   enum class parse_result { needs_more, push, resp };

   using parse_ret_type = std::pair<parse_result, std::size_t>;

   parse_ret_type on_finish_parsing(parse_result t)
   {
      if (t == parse_result::push) {
         usage_.pushes_received += 1;
         usage_.push_bytes_received += parser_.get_consumed();
      } else {
         usage_.responses_received += 1;
         usage_.response_bytes_received += parser_.get_consumed();
      }

      on_push_ = false;
      dbuf_.consume(parser_.get_consumed());
      auto const res = std::make_pair(t, parser_.get_consumed());
      parser_.reset();
      return res;
   }

   parse_ret_type on_read(std::string_view data, system::error_code& ec)
   {
      // We arrive here in two states:
      //
      //    1. While we are parsing a message. In this case we
      //       don't want to determine the type of the message in the
      //       buffer (i.e. response vs push) but leave it untouched
      //       until the parsing of a complete message ends.
      //
      //    2. On a new message, in which case we have to determine
      //       whether the next messag is a push or a response.
      //
      if (!on_push_) // Prepare for new message.
         on_push_ = is_next_push();

      if (on_push_) {
         if (!resp3::parse(parser_, data, receive_adapter_, ec))
            return std::make_pair(parse_result::needs_more, 0);

         if (ec)
            return std::make_pair(parse_result::push, 0);

         return on_finish_parsing(parse_result::push);
      }

      BOOST_ASSERT_MSG(is_waiting_response(), "Not waiting for a response (using MONITOR command perhaps?)");
      BOOST_ASSERT(!reqs_.empty());
      BOOST_ASSERT(reqs_.front() != nullptr);
      BOOST_ASSERT(reqs_.front()->expected_responses_ != 0);

      if (!resp3::parse(parser_, data, reqs_.front()->adapter_, ec))
         return std::make_pair(parse_result::needs_more, 0);

      if (ec) {
         reqs_.front()->ec_ = ec;
         reqs_.front()->proceed();
         return std::make_pair(parse_result::resp, 0);
      }

      reqs_.front()->read_size_ += parser_.get_consumed();

      if (--reqs_.front()->expected_responses_ == 0) {
         // Done with this request.
         reqs_.front()->proceed();
         reqs_.pop_front();
      }

      return on_finish_parsing(parse_result::resp);
   }

   void reset()
   {
      write_buffer_.clear();
      read_buffer_.clear();
      parser_.reset();
      on_push_ = false;
      cancel_run_called_ = false;
   }

   asio::ssl::context ctx_;
   std::unique_ptr<next_layer_type> stream_;

   // Notice we use a timer to simulate a condition-variable. It is
   // also more suitable than a channel and the notify operation does
   // not suspend.
   timer_type writer_timer_;
   receive_channel_type receive_channel_;
   runner_type runner_;
   receiver_adapter_type receive_adapter_;

   using dyn_buffer_type = asio::dynamic_string_buffer<char, std::char_traits<char>, std::allocator<char>>;

   std::string read_buffer_;
   dyn_buffer_type dbuf_;
   std::string write_buffer_;
   reqs_type reqs_;
   resp3::parser parser_{};
   bool on_push_ = false;
   bool cancel_run_called_ = false;

   usage usage_;
};

} // boost::redis::detail

#endif // BOOST_REDIS_CONNECTION_BASE_HPP
