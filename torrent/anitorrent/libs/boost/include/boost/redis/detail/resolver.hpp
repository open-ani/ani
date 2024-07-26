/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_RESOLVER_HPP
#define BOOST_REDIS_RESOLVER_HPP

#include <boost/redis/config.hpp>
#include <boost/redis/detail/helper.hpp>
#include <boost/redis/error.hpp>
#include <boost/asio/compose.hpp>
#include <boost/asio/coroutine.hpp>
#include <boost/asio/experimental/parallel_group.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/steady_timer.hpp>
#include <string>
#include <chrono>

namespace boost::redis::detail
{

template <class Resolver>
struct resolve_op {
   Resolver* resv_ = nullptr;
   asio::coroutine coro{};

   template <class Self>
   void operator()( Self& self
                  , std::array<std::size_t, 2> order = {}
                  , system::error_code ec1 = {}
                  , asio::ip::tcp::resolver::results_type res = {}
                  , system::error_code ec2 = {})
   {
      BOOST_ASIO_CORO_REENTER (coro)
      {
         resv_->timer_.expires_after(resv_->timeout_);

         BOOST_ASIO_CORO_YIELD
         asio::experimental::make_parallel_group(
            [this](auto token)
            {
               return resv_->resv_.async_resolve(resv_->addr_.host, resv_->addr_.port, token);
            },
            [this](auto token) { return resv_->timer_.async_wait(token);}
         ).async_wait(
            asio::experimental::wait_for_one(),
            std::move(self));

         if (is_cancelled(self)) {
            self.complete(asio::error::operation_aborted);
            return;
         }

         switch (order[0]) {
            case 0: {
               // Resolver completed first.
               resv_->results_ = res;
               self.complete(ec1);
            } break;

            case 1: {
               if (ec2) {
                  // Timer completed first with error, perhaps a
                  // cancellation going on.
                  self.complete(ec2);
               } else {
                  // Timer completed first without an error, this is a
                  // resolve timeout.
                  self.complete(error::resolve_timeout);
               }
            } break;

            default: BOOST_ASSERT(false);
         }
      }
   }
};

template <class Executor>
class resolver {
public:
   using timer_type =
      asio::basic_waitable_timer<
         std::chrono::steady_clock,
         asio::wait_traits<std::chrono::steady_clock>,
         Executor>;

   resolver(Executor ex) : resv_{ex} , timer_{ex} {}

   template <class CompletionToken>
   auto async_resolve(CompletionToken&& token)
   {
      return asio::async_compose
         < CompletionToken
         , void(system::error_code)
         >(resolve_op<resolver>{this}, token, resv_);
   }

   std::size_t cancel(operation op)
   {
      switch (op) {
         case operation::resolve:
         case operation::all:
            resv_.cancel();
            timer_.cancel();
            break;
         default: /* ignore */;
      }

      return 0;
   }

   auto const& results() const noexcept
      { return results_;}

   void set_config(config const& cfg)
   {
      addr_ = cfg.addr;
      timeout_ = cfg.resolve_timeout;
   }

private:
   using resolver_type = asio::ip::basic_resolver<asio::ip::tcp, Executor>;
   template <class> friend struct resolve_op;

   resolver_type resv_;
   timer_type timer_;
   address addr_;
   std::chrono::steady_clock::duration timeout_;
   asio::ip::tcp::resolver::results_type results_;
};

} // boost::redis::detail

#endif // BOOST_REDIS_RESOLVER_HPP
