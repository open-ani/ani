/* Copyright (c) 2018-2024 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#include <boost/redis/resp3/parser.hpp>
#include <boost/redis/error.hpp>
#include <boost/assert.hpp>

#include <charconv>
#include <limits>

namespace boost::redis::resp3 {

void to_int(std::size_t& i, std::string_view sv, system::error_code& ec)
{
   auto const res = std::from_chars(sv.data(), sv.data() + std::size(sv), i);
   if (res.ec != std::errc())
      ec = error::not_a_number;
}

parser::parser()
{
   reset();
}

void parser::reset()
{
   depth_ = 0;
   sizes_ = {{1}};
   bulk_length_ = (std::numeric_limits<std::size_t>::max)();
   bulk_ = type::invalid;
   consumed_ = 0;
   sizes_[0] = 2; // The sentinel must be more than 1.
}

std::size_t
parser::get_suggested_buffer_growth(std::size_t hint) const noexcept
{
   if (!bulk_expected())
      return hint;

   if (hint < bulk_length_ + 2)
      return bulk_length_ + 2;

   return hint;
}

std::size_t
parser::get_consumed() const noexcept
{
   return consumed_;
}

bool
parser::done() const noexcept
{
   return depth_ == 0 && bulk_ == type::invalid && consumed_ != 0;
}

void
parser::commit_elem() noexcept
{
   --sizes_[depth_];
   while (sizes_[depth_] == 0) {
      --depth_;
      --sizes_[depth_];
   }
}

auto
parser::consume(std::string_view view, system::error_code& ec) noexcept -> parser::result
{
   switch (bulk_) {
      case type::invalid:
      {
         auto const pos = view.find(sep, consumed_);
         if (pos == std::string::npos)
            return {}; // Needs more data to proceeed.

         auto const t = to_type(view.at(consumed_));
         auto const content = view.substr(consumed_ + 1, pos - 1 - consumed_);
         auto const ret = consume_impl(t, content, ec);
         if (ec)
            return {};

         consumed_ = pos + 2;
         if (!bulk_expected())
            return ret;

      } [[fallthrough]];

      default: // Handles bulk.
      {
         auto const span = bulk_length_ + 2;
         if ((std::size(view) - consumed_) < span)
            return {}; // Needs more data to proceeed.

         auto const bulk_view = view.substr(consumed_, bulk_length_);
         node_type const ret = {bulk_, 1, depth_, bulk_view};
         bulk_ = type::invalid;
         commit_elem();

         consumed_ += span;
         return ret;
      }
   }
}

auto
parser::consume_impl(
   type t,
   std::string_view elem,
   system::error_code& ec) -> parser::node_type
{
   BOOST_ASSERT(!bulk_expected());

   node_type ret;
   switch (t) {
      case type::streamed_string_part:
      {
         to_int(bulk_length_ , elem, ec);
         if (ec)
            return {};

         if (bulk_length_ == 0) {
            ret = {type::streamed_string_part, 1, depth_, {}};
            sizes_[depth_] = 1; // We are done.
            bulk_ = type::invalid;
            commit_elem();
         } else {
            bulk_ = type::streamed_string_part;
         }
      } break;
      case type::blob_error:
      case type::verbatim_string:
      case type::blob_string:
      {
         if (elem.at(0) == '?') {
            // NOTE: This can only be triggered with blob_string.
            // Trick: A streamed string is read as an aggregate of
            // infinite length. When the streaming is done the server
            // is supposed to send a part with length 0.
            sizes_[++depth_] = (std::numeric_limits<std::size_t>::max)();
            ret = {type::streamed_string, 0, depth_, {}};
         } else {
            to_int(bulk_length_ , elem , ec);
            if (ec)
               return {};

            bulk_ = t;
         }
      } break;
      case type::boolean:
      {
         if (std::empty(elem)) {
             ec = error::empty_field;
             return {};
         }

         if (elem.at(0) != 'f' && elem.at(0) != 't') {
             ec = error::unexpected_bool_value;
             return {};
         }

         ret = {t, 1, depth_, elem};
         commit_elem();
      } break;
      case type::doublean:
      case type::big_number:
      case type::number:
      {
         if (std::empty(elem)) {
             ec = error::empty_field;
             return {};
         }
      } [[fallthrough]];
      case type::simple_error:
      case type::simple_string:
      case type::null:
      {
         ret = {t, 1, depth_, elem};
         commit_elem();
      } break;
      case type::push:
      case type::set:
      case type::array:
      case type::attribute:
      case type::map:
      {
         std::size_t l = -1;
         to_int(l, elem, ec);
         if (ec)
            return {};

         ret = {t, l, depth_, {}};
         if (l == 0) {
            commit_elem();
         } else {
            if (depth_ == max_embedded_depth) {
               ec = error::exceeeds_max_nested_depth;
               return {};
            }

            ++depth_;

            sizes_[depth_] = l * element_multiplicity(t);
         }
      } break;
      default:
      {
         ec = error::invalid_data_type;
         return {};
      }
   }

   return ret;
}
} // boost::redis::resp3
