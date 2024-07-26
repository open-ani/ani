/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_ADAPTER_DETAIL_RESPONSE_TRAITS_HPP
#define BOOST_REDIS_ADAPTER_DETAIL_RESPONSE_TRAITS_HPP

#include <boost/redis/resp3/node.hpp>
#include <boost/redis/response.hpp>
#include <boost/redis/adapter/detail/result_traits.hpp>
#include <boost/mp11.hpp>
#include <boost/system.hpp>

#include <tuple>
#include <limits>
#include <string_view>
#include <variant>

namespace boost::redis::adapter::detail
{

class ignore_adapter {
public:
   template <class String>
   void operator()(std::size_t, resp3::basic_node<String> const& nd, system::error_code& ec)
   {
      switch (nd.data_type) {
         case resp3::type::simple_error: ec = redis::error::resp3_simple_error; break;
         case resp3::type::blob_error: ec = redis::error::resp3_blob_error; break;
         case resp3::type::null: ec = redis::error::resp3_null; break;
         default:;
      }
   }

   [[nodiscard]]
   auto get_supported_response_size() const noexcept
      { return static_cast<std::size_t>(-1);}
};

template <class Response>
class static_adapter {
private:
   static constexpr auto size = std::tuple_size<Response>::value;
   using adapter_tuple = mp11::mp_transform<adapter_t, Response>;
   using variant_type = mp11::mp_rename<adapter_tuple, std::variant>;
   using adapters_array_type = std::array<variant_type, size>;

   adapters_array_type adapters_;

public:
   explicit static_adapter(Response& r)
   {
      assigner<size - 1>::assign(adapters_, r);
   }

   [[nodiscard]]
   auto get_supported_response_size() const noexcept
      { return size;}

   template <class String>
   void operator()(std::size_t i, resp3::basic_node<String> const& nd, system::error_code& ec)
   {
      using std::visit;
      // I am usure whether this should be an error or an assertion.
      BOOST_ASSERT(i < adapters_.size());
      visit([&](auto& arg){arg(nd, ec);}, adapters_.at(i));
   }
};

template <class Vector>
class vector_adapter {
private:
   using adapter_type = typename result_traits<Vector>::adapter_type;
   adapter_type adapter_;

public:
   explicit vector_adapter(Vector& v)
   : adapter_{internal_adapt(v)}
   { }

   [[nodiscard]]
   auto
   get_supported_response_size() const noexcept
      { return static_cast<std::size_t>(-1);}

   template <class String>
   void operator()(std::size_t, resp3::basic_node<String> const& nd, system::error_code& ec)
   {
      adapter_(nd, ec);
   }
};

template <class>
struct response_traits;

template <>
struct response_traits<ignore_t> {
   using response_type = ignore_t;
   using adapter_type = detail::ignore_adapter;

   static auto adapt(response_type&) noexcept
      { return detail::ignore_adapter{}; }
};

template <>
struct response_traits<result<ignore_t>> {
   using response_type = result<ignore_t>;
   using adapter_type = detail::ignore_adapter;

   static auto adapt(response_type&) noexcept
      { return detail::ignore_adapter{}; }
};

template <class String, class Allocator>
struct response_traits<result<std::vector<resp3::basic_node<String>, Allocator>>> {
   using response_type = result<std::vector<resp3::basic_node<String>, Allocator>>;
   using adapter_type = vector_adapter<response_type>;

   static auto adapt(response_type& v) noexcept
      { return adapter_type{v}; }
};

template <class ...Ts>
struct response_traits<response<Ts...>> {
   using response_type = response<Ts...>;
   using adapter_type = static_adapter<response_type>;

   static auto adapt(response_type& r) noexcept
      { return adapter_type{r}; }
};

template <class Adapter>
class wrapper {
public:
   explicit wrapper(Adapter adapter) : adapter_{adapter} {}

   template <class String>
   void operator()(resp3::basic_node<String> const& nd, system::error_code& ec)
      { return adapter_(0, nd, ec); }

   [[nodiscard]]
   auto get_supported_response_size() const noexcept
      { return adapter_.get_supported_response_size();}

private:
   Adapter adapter_;
};

template <class Adapter>
auto make_adapter_wrapper(Adapter adapter)
{
   return wrapper{adapter};
}

} // boost::redis::adapter::detail

#endif // BOOST_REDIS_ADAPTER_DETAIL_RESPONSE_TRAITS_HPP
