/* Copyright (c) 2018-2024 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_RESP3_PARSER_HPP
#define BOOST_REDIS_RESP3_PARSER_HPP

#include <boost/redis/resp3/node.hpp>
#include <boost/system/error_code.hpp>
#include <array>
#include <string_view>
#include <cstdint>
#include <optional>

namespace boost::redis::resp3 {

class parser {
public:
   using node_type = basic_node<std::string_view>;
   using result = std::optional<node_type>;

   static constexpr std::size_t max_embedded_depth = 5;
   static constexpr std::string_view sep = "\r\n";

private:
   // The current depth. Simple data types will have depth 0, whereas
   // the elements of aggregates will have depth 1. Embedded types
   // will have increasing depth.
   std::size_t depth_;

   // The parser supports up to 5 levels of nested structures. The
   // first element in the sizes stack is a sentinel and must be
   // different from 1.
   std::array<std::size_t, max_embedded_depth + 1> sizes_;

   // Contains the length expected in the next bulk read.
   std::size_t bulk_length_;

   // The type of the next bulk. Contains type::invalid if no bulk is
   // expected.
   type bulk_;

   // The number of bytes consumed from the buffer.
   std::size_t consumed_;

   // Returns the number of bytes that have been consumed.
   auto consume_impl(type t, std::string_view elem, system::error_code& ec) -> node_type;

   void commit_elem() noexcept;

   // The bulk type expected in the next read. If none is expected
   // returns type::invalid.
   [[nodiscard]]
   auto bulk_expected() const noexcept -> bool
      { return bulk_ != type::invalid; }

public:
   parser();

   // Returns true when the parser is done with the current message.
   [[nodiscard]]
   auto done() const noexcept -> bool;

   auto get_suggested_buffer_growth(std::size_t hint) const noexcept -> std::size_t;

   auto get_consumed() const noexcept -> std::size_t;

   auto consume(std::string_view view, system::error_code& ec) noexcept -> result;

   void reset();
};

// Returns false if more data is needed. If true is returned the
// parser is either done or an error occured, that can be checked on
// ec.
template <class Adapter>
bool
parse(
   resp3::parser& p,
   std::string_view const& msg,
   Adapter& adapter,
   system::error_code& ec)
{
   while (!p.done()) {
      auto const res = p.consume(msg, ec);
      if (ec)
         return true;

      if (!res)
         return false;

      adapter(res.value(), ec);
      if (ec)
         return true;
   }

   return true;
}

} // boost::redis::resp3

#endif // BOOST_REDIS_RESP3_PARSER_HPP
