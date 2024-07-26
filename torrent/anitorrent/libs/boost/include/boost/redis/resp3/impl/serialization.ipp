/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#include <boost/redis/resp3/serialization.hpp>
#include <boost/redis/resp3/parser.hpp>

namespace boost::redis::resp3 {

void boost_redis_to_bulk(std::string& payload, std::string_view data)
{
   auto const str = std::to_string(data.size());

   payload += to_code(type::blob_string);
   payload.append(std::cbegin(str), std::cend(str));
   payload += parser::sep;
   payload.append(std::cbegin(data), std::cend(data));
   payload += parser::sep;
}

void add_header(std::string& payload, type t, std::size_t size)
{
   auto const str = std::to_string(size);

   payload += to_code(t);
   payload.append(std::cbegin(str), std::cend(str));
   payload += parser::sep;
}

void add_blob(std::string& payload, std::string_view blob)
{
   payload.append(std::cbegin(blob), std::cend(blob));
   payload += parser::sep;
}

void add_separator(std::string& payload)
{
   payload += parser::sep;
}
} // boost::redis::resp3
