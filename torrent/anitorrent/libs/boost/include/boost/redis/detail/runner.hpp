/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_RUNNER_HPP
#define BOOST_REDIS_RUNNER_HPP

#include <boost/redis/detail/health_checker.hpp>
#include <boost/redis/config.hpp>
#include <boost/redis/response.hpp>
#include <boost/redis/detail/helper.hpp>
#include <boost/redis/error.hpp>
#include <boost/redis/logger.hpp>
#include <boost/redis/operation.hpp>
#include <boost/redis/detail/connector.hpp>
#include <boost/redis/detail/resolver.hpp>
#include <boost/redis/detail/handshaker.hpp>
#include <boost/asio/compose.hpp>
#include <boost/asio/connect.hpp>
#include <boost/asio/coroutine.hpp>
#include <boost/asio/experimental/parallel_group.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/steady_timer.hpp>
#include <string>
#include <memory>
#include <chrono>

namespace boost::redis::detail
{

void push_hello(config const& cfg, request& req);

template <class Runner, class Connection, class Logger>
struct hello_op {
   Runner* runner_ = nullptr;
   Connection* conn_ = nullptr;
   Logger logger_;
   asio::coroutine coro_{};

   template <class Self>
   void operator()(Self& self, system::error_code ec = {}, std::size_t = 0)
   {
      BOOST_ASIO_CORO_REENTER (coro_)
      {
         runner_->add_hello();

         BOOST_ASIO_CORO_YIELD
         conn_->async_exec(runner_->hello_req_, runner_->hello_resp_, std::move(self));
         logger_.on_hello(ec, runner_->hello_resp_);

         if (ec || runner_->has_error_in_response() || is_cancelled(self)) {
            logger_.trace("hello-op: error/canceled. Exiting ...");
            conn_->cancel(operation::run);
            self.complete(!!ec ? ec : asio::error::operation_aborted);
            return;
         }

         self.complete({});
      }
   }
};

template <class Runner, class Connection, class Logger>
class runner_op {
private:
   Runner* runner_ = nullptr;
   Connection* conn_ = nullptr;
   Logger logger_;
   asio::coroutine coro_{};

public:
   runner_op(Runner* runner, Connection* conn, Logger l)
   : runner_{runner}
   , conn_{conn}
   , logger_{l}
   {}

   template <class Self>
   void operator()( Self& self
                  , std::array<std::size_t, 3> order = {}
                  , system::error_code ec0 = {}
                  , system::error_code ec1 = {}
                  , system::error_code ec2 = {}
                  , std::size_t = 0)
   {
      BOOST_ASIO_CORO_REENTER (coro_)
      {
         BOOST_ASIO_CORO_YIELD
         asio::experimental::make_parallel_group(
            [this](auto token) { return runner_->async_run_all(*conn_, logger_, token); },
            [this](auto token) { return runner_->health_checker_.async_check_health(*conn_, logger_, token); },
            [this](auto token) { return runner_->async_hello(*conn_, logger_, token); }
         ).async_wait(
            asio::experimental::wait_for_all(),
            std::move(self));

         logger_.on_runner(ec0, ec1, ec2);

         if (is_cancelled(self)) {
            self.complete(asio::error::operation_aborted);
            return;
         }

         if (ec0 == error::connect_timeout || ec0 == error::resolve_timeout) {
            self.complete(ec0);
            return;
         }

         if (order[0] == 2 && !!ec2) {
            self.complete(ec2);
            return;
         }

         if (order[0] == 1 && ec1 == error::pong_timeout) {
            self.complete(ec1);
            return;
         }

         self.complete(ec0);
      }
   }
};

template <class Runner, class Connection, class Logger>
struct run_all_op {
   Runner* runner_ = nullptr;
   Connection* conn_ = nullptr;
   Logger logger_;
   asio::coroutine coro_{};

   template <class Self>
   void operator()(Self& self, system::error_code ec = {}, std::size_t = 0)
   {
      BOOST_ASIO_CORO_REENTER (coro_)
      {
         BOOST_ASIO_CORO_YIELD
         runner_->resv_.async_resolve(std::move(self));
         logger_.on_resolve(ec, runner_->resv_.results());
         BOOST_REDIS_CHECK_OP0(conn_->cancel(operation::run);)

         BOOST_ASIO_CORO_YIELD
         runner_->ctor_.async_connect(conn_->next_layer().next_layer(), runner_->resv_.results(), std::move(self));
         logger_.on_connect(ec, runner_->ctor_.endpoint());
         BOOST_REDIS_CHECK_OP0(conn_->cancel(operation::run);)

         if (conn_->use_ssl()) {
            BOOST_ASIO_CORO_YIELD
            runner_->hsher_.async_handshake(conn_->next_layer(), std::move(self));
            logger_.on_ssl_handshake(ec);
            BOOST_REDIS_CHECK_OP0(conn_->cancel(operation::run);)
         }

         BOOST_ASIO_CORO_YIELD
         conn_->async_run_lean(runner_->cfg_, logger_, std::move(self));
         BOOST_REDIS_CHECK_OP0(;)
         self.complete(ec);
      }
   }
};

template <class Executor>
class runner {
public:
   runner(Executor ex, config cfg)
   : resv_{ex}
   , ctor_{ex}
   , hsher_{ex}
   , health_checker_{ex}
   , cfg_{cfg}
   { }

   std::size_t cancel(operation op)
   {
      resv_.cancel(op);
      ctor_.cancel(op);
      hsher_.cancel(op);
      health_checker_.cancel(op);
      return 0U;
   }

   void set_config(config const& cfg)
   {
      cfg_ = cfg;
      resv_.set_config(cfg);
      ctor_.set_config(cfg);
      hsher_.set_config(cfg);
      health_checker_.set_config(cfg);
   }

   template <class Connection, class Logger, class CompletionToken>
   auto async_run(Connection& conn, Logger l, CompletionToken token)
   {
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(runner_op<runner, Connection, Logger>{this, &conn, l}, token, conn);
   }

   config const& get_config() const noexcept {return cfg_;}

private:
   using resolver_type = resolver<Executor>;
   using connector_type = connector<Executor>;
   using handshaker_type = detail::handshaker<Executor>;
   using health_checker_type = health_checker<Executor>;
   using timer_type = typename connector_type::timer_type;

   template <class, class, class> friend struct run_all_op;
   template <class, class, class> friend class runner_op;
   template <class, class, class> friend struct hello_op;

   template <class Connection, class Logger, class CompletionToken>
   auto async_run_all(Connection& conn, Logger l, CompletionToken token)
   {
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(run_all_op<runner, Connection, Logger>{this, &conn, l}, token, conn);
   }

   template <class Connection, class Logger, class CompletionToken>
   auto async_hello(Connection& conn, Logger l, CompletionToken token)
   {
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(hello_op<runner, Connection, Logger>{this, &conn, l}, token, conn);
   }

   void add_hello()
   {
      hello_req_.clear();
      if (hello_resp_.has_value())
         hello_resp_.value().clear();
      push_hello(cfg_, hello_req_);
   }

   bool has_error_in_response() const noexcept
   {
      if (!hello_resp_.has_value())
         return true;

      auto f = [](auto const& e)
      {
         switch (e.data_type) {
            case resp3::type::simple_error:
            case resp3::type::blob_error: return true;
            default: return false;
         }
      };

      return std::any_of(std::cbegin(hello_resp_.value()), std::cend(hello_resp_.value()), f);
   }

   resolver_type resv_;
   connector_type ctor_;
   handshaker_type hsher_;
   health_checker_type health_checker_;
   request hello_req_;
   generic_response hello_resp_;
   config cfg_;
};

} // boost::redis::detail

#endif // BOOST_REDIS_RUNNER_HPP
