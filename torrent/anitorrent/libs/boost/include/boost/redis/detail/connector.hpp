/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_CONNECTOR_HPP
#define BOOST_REDIS_CONNECTOR_HPP

#include <boost/redis/detail/helper.hpp>
#include <boost/redis/error.hpp>
#include <boost/asio/compose.hpp>
#include <boost/asio/connect.hpp>
#include <boost/asio/coroutine.hpp>
#include <boost/asio/experimental/parallel_group.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/steady_timer.hpp>
#include <string>
#include <chrono>

namespace boost::redis::detail
{

template <class Connector, class Stream>
struct connect_op {
   Connector* ctor_ = nullptr;
   Stream* stream = nullptr;
   asio::ip::tcp::resolver::results_type const* res_ = nullptr;
   asio::coroutine coro{};

   template <class Self>
   void operator()( Self& self
                  , std::array<std::size_t, 2> const& order = {}
                  , system::error_code const& ec1 = {}
                  , asio::ip::tcp::endpoint const& ep= {}
                  , system::error_code const& ec2 = {})
   {
      BOOST_ASIO_CORO_REENTER (coro)
      {
         ctor_->timer_.expires_after(ctor_->timeout_);

         BOOST_ASIO_CORO_YIELD
         asio::experimental::make_parallel_group(
            [this](auto token)
            {
               auto f = [](system::error_code const&, auto const&) { return true; };
               return asio::async_connect(*stream, *res_, f, token);
            },
            [this](auto token) { return ctor_->timer_.async_wait(token);}
         ).async_wait(
            asio::experimental::wait_for_one(),
            std::move(self));

         if (is_cancelled(self)) {
            self.complete(asio::error::operation_aborted);
            return;
         }

         switch (order[0]) {
            case 0: {
               ctor_->endpoint_ = ep;
               self.complete(ec1);
            } break;
            case 1:
            {
               if (ec2) {
                  self.complete(ec2);
               } else {
                  self.complete(error::connect_timeout);
               }
            } break;

            default: BOOST_ASSERT(false);
         }
      }
   }
};

template <class Executor>
class connector {
public:
   using timer_type =
      asio::basic_waitable_timer<
         std::chrono::steady_clock,
         asio::wait_traits<std::chrono::steady_clock>,
         Executor>;

   connector(Executor ex)
   : timer_{ex}
   {}

   void set_config(config const& cfg)
      { timeout_ = cfg.connect_timeout; }

   template <class Stream, class CompletionToken>
   auto
   async_connect(
         Stream& stream,
         asio::ip::tcp::resolver::results_type const& res,
         CompletionToken&& token)
   {
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(connect_op<connector, Stream>{this, &stream, &res}, token, timer_);
   }

   std::size_t cancel(operation op)
   {
      switch (op) {
         case operation::connect:
         case operation::all:
            timer_.cancel();
            break;
         default: /* ignore */;
      }

      return 0;
   }

   auto const& endpoint() const noexcept { return endpoint_;}

private:
   template <class, class> friend struct connect_op;

   timer_type timer_;
   std::chrono::steady_clock::duration timeout_ = std::chrono::seconds{2};
   asio::ip::tcp::endpoint endpoint_;
};

} // boost::redis::detail

#endif // BOOST_REDIS_CONNECTOR_HPP
