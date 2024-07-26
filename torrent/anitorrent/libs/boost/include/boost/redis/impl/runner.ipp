/* Copyright (c) 2018-2024 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#include <boost/redis/detail/runner.hpp>

namespace boost::redis::detail
{

void push_hello(config const& cfg, request& req)
{
   if (!cfg.username.empty() && !cfg.password.empty() && !cfg.clientname.empty())
      req.push("HELLO", "3", "AUTH", cfg.username, cfg.password, "SETNAME", cfg.clientname);
   else if (cfg.password.empty() && cfg.clientname.empty())
      req.push("HELLO", "3");
   else if (cfg.clientname.empty())
      req.push("HELLO", "3", "AUTH", cfg.username, cfg.password);
   else
      req.push("HELLO", "3", "SETNAME", cfg.clientname);

   if (cfg.database_index && cfg.database_index.value() != 0)
      req.push("SELECT", cfg.database_index.value());
}

} // boost::redis::detail
