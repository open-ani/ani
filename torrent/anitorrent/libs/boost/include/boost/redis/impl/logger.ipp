/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#include <boost/redis/logger.hpp>
#include <boost/system/error_code.hpp>
#include <iostream>
#include <iterator>

namespace boost::redis
{

void logger::write_prefix()
{
   if (!std::empty(prefix_))
      std::clog << prefix_;
}

void logger::on_resolve(system::error_code const& ec, asio::ip::tcp::resolver::results_type const& res)
{
   if (level_ < level::info)
      return;

   write_prefix();

   std::clog << "run-all-op: resolve addresses ";

   if (ec) {
      std::clog << ec.message() << std::endl;
   } else {
      auto begin = std::cbegin(res);
      auto end = std::cend(res);

      if (begin == end)
         return;

      std::clog << begin->endpoint();
      for (auto iter = std::next(begin); iter != end; ++iter)
         std::clog << ", " << iter->endpoint();
   }

   std::clog << std::endl;
}

void logger::on_connect(system::error_code const& ec, asio::ip::tcp::endpoint const& ep)
{
   if (level_ < level::info)
      return;

   write_prefix();

   std::clog << "run-all-op: connected to endpoint ";

   if (ec)
      std::clog << ec.message() << std::endl;
   else
      std::clog << ep;

   std::clog << std::endl;
}

void logger::on_ssl_handshake(system::error_code const& ec)
{
   if (level_ < level::info)
      return;

   write_prefix();

   std::clog << "Runner: SSL handshake " << ec.message() << std::endl;
}

void logger::on_connection_lost(system::error_code const& ec)
{
   if (level_ < level::info)
      return;

   write_prefix();

   if (ec)
      std::clog << "Connection lost: " << ec.message();
   else
      std::clog << "Connection lost.";

   std::clog << std::endl;
}

void
logger::on_write(
   system::error_code const& ec,
   std::string const& payload)
{
   if (level_ < level::info)
      return;

   write_prefix();

   if (ec)
      std::clog << "writer-op: " << ec.message();
   else
      std::clog << "writer-op: " << std::size(payload) << " bytes written.";

   std::clog << std::endl;
}

void logger::on_read(system::error_code const& ec, std::size_t n)
{
   if (level_ < level::info)
      return;

   write_prefix();

   if (ec)
      std::clog << "reader-op: " << ec.message();
   else
      std::clog << "reader-op: " << n << " bytes read.";

   std::clog << std::endl;
}

void logger::on_run(system::error_code const& reader_ec, system::error_code const& writer_ec)
{
   if (level_ < level::info)
      return;

   write_prefix();

   std::clog << "run-op: "
      << reader_ec.message() << " (reader), "
      << writer_ec.message() << " (writer)";

   std::clog << std::endl;
}

void
logger::on_hello(
   system::error_code const& ec,
   generic_response const& resp)
{
   if (level_ < level::info)
      return;

   write_prefix();

   if (ec) {
      std::clog << "hello-op: " << ec.message();
      if (resp.has_error())
         std::clog << " (" << resp.error().diagnostic << ")";
   } else {
      std::clog << "hello-op: Success";
   }

   std::clog << std::endl;
}

void
   logger::on_runner(
      system::error_code const& run_all_ec,
      system::error_code const& health_check_ec,
      system::error_code const& hello_ec)
{
   if (level_ < level::info)
      return;

   write_prefix();

   std::clog << "runner-op: "
      << run_all_ec.message() << " (async_run_all), "
      << health_check_ec.message() << " (async_health_check) " 
      << hello_ec.message() << " (async_hello).";

   std::clog << std::endl;
}

void
   logger::on_check_health(
      system::error_code const& ping_ec,
      system::error_code const& timeout_ec)
{
   if (level_ < level::info)
      return;

   write_prefix();

   std::clog << "check-health-op: "
      << ping_ec.message() << " (async_ping), "
      << timeout_ec.message() << " (async_check_timeout).";

   std::clog << std::endl;
}

void logger::trace(std::string_view reason)
{
   if (level_ < level::debug)
      return;

   write_prefix();

   std::clog << reason << std::endl;
}

} // boost::redis
