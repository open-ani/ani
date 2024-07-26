//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_GATHER_HPP
#define BOOST_COBALT_DETAIL_GATHER_HPP

#include <boost/cobalt/detail/await_result_helper.hpp>
#include <boost/cobalt/detail/exception.hpp>
#include <boost/cobalt/detail/fork.hpp>
#include <boost/cobalt/detail/forward_cancellation.hpp>
#include <boost/cobalt/detail/util.hpp>
#include <boost/cobalt/detail/wrapper.hpp>
#include <boost/cobalt/task.hpp>
#include <boost/cobalt/this_thread.hpp>

#include <boost/asio/associated_cancellation_slot.hpp>
#include <boost/asio/bind_cancellation_slot.hpp>
#include <boost/asio/cancellation_signal.hpp>


#include <boost/core/ignore_unused.hpp>
#include <boost/intrusive_ptr.hpp>
#include <boost/system/result.hpp>
#include <boost/variant2/variant.hpp>

#include <array>
#include <coroutine>
#include <algorithm>

namespace boost::cobalt::detail
{


template<typename ... Args>
struct gather_variadic_impl
{
  using tuple_type = std::tuple<decltype(get_awaitable_type(std::declval<Args&&>()))...>;

  gather_variadic_impl(Args && ... args)
      : args{std::forward<Args>(args)...}
  {
  }

  std::tuple<Args...> args;

  constexpr static std::size_t tuple_size = sizeof...(Args);

  struct awaitable : fork::static_shared_state<256 * tuple_size>
  {
    template<std::size_t ... Idx>
    awaitable(std::tuple<Args...> & args, std::index_sequence<Idx...>)
      : aws(awaitable_type_getter<Args>(std::get<Idx>(args))...)
    {
    }

    tuple_type aws;
    std::array<asio::cancellation_signal, tuple_size> cancel;

    template<typename T>
    using result_store_part = variant2::variant<
        variant2::monostate,
        void_as_monostate<co_await_result_t<T>>,
        std::exception_ptr>;

    std::tuple<result_store_part<Args>...> result;


    template<std::size_t Idx>
    void interrupt_await_step()
    {
      using type= std::tuple_element_t<Idx, std::tuple<Args...>>;
      using t = std::conditional_t<
          std::is_reference_v<std::tuple_element_t<Idx, decltype(aws)>>,
          co_awaitable_type<type> &,
          co_awaitable_type<type> &&>;

      if constexpr (interruptible<t>)
          static_cast<t>(std::get<Idx>(aws)).interrupt_await();
    }
    void interrupt_await()
    {
      mp11::mp_for_each<mp11::mp_iota_c<sizeof...(Args)>>
          ([&](auto idx)
           {
             interrupt_await_step<idx>();
           });
    }

    // GCC doesn't like member funs
    template<std::size_t Idx>
    static detail::fork await_impl(awaitable & this_)
    try
    {
      auto & aw = std::get<Idx>(this_.aws);
      // check manually if we're ready
      auto rd = aw.await_ready();
      if (!rd)
      {
        co_await this_.cancel[Idx].slot();
        // make sure the executor is set
        co_await detail::fork::wired_up;
        // do the await - this doesn't call await-ready again

        if constexpr (std::is_void_v<decltype(aw.await_resume())>)
        {
          co_await aw;
          std::get<Idx>(this_.result).template emplace<1u>();
        }
        else
          std::get<Idx>(this_.result).template emplace<1u>(co_await aw);
      }
      else
      {
        if constexpr (std::is_void_v<decltype(aw.await_resume())>)
        {
          aw.await_resume();
          std::get<Idx>(this_.result).template emplace<1u>();
        }
        else
          std::get<Idx>(this_.result).template emplace<1u>(aw.await_resume());
      }
    }
    catch(...)
    {
      std::get<Idx>(this_.result).template emplace<2u>(std::current_exception());
    }

    std::array<detail::fork(*)(awaitable&), tuple_size> impls {
      []<std::size_t ... Idx>(std::index_sequence<Idx...>)
      {
        return std::array<detail::fork(*)(awaitable&), tuple_size>{&await_impl<Idx>...};
      }(std::make_index_sequence<tuple_size>{})
    };

    detail::fork last_forked;
    std::size_t last_index = 0u;

    bool await_ready()
    {
      while (last_index < tuple_size)
      {
        last_forked = impls[last_index++](*this);
        if (!last_forked.done())
          return false; // one coro didn't immediately complete!
      }
      last_forked.release();
      return true;
    }

    template<typename H>
    auto await_suspend(
          std::coroutine_handle<H> h
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
        , const boost::source_location & loc = BOOST_CURRENT_LOCATION
#endif
    )
    {
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
      this->loc = loc;
#endif
      this->exec = &cobalt::detail::get_executor(h);
      last_forked.release().resume();
      while (last_index < tuple_size)
        impls[last_index++](*this).release();

      if (!this->outstanding_work()) // already done, resume rightaway.
        return false;

      // arm the cancel
      assign_cancellation(
          h,
          [&](asio::cancellation_type ct)
          {
            for (auto & cs : cancel)
              cs.emit(ct);
          });


      this->coro.reset(h.address());
      return true;
    }

    template<typename T>
    using result_part = system::result<co_await_result_t<T>, std::exception_ptr>;

#if _MSC_VER
    BOOST_NOINLINE
#endif
    std::tuple<result_part<Args> ...> await_resume()
    {
      return mp11::tuple_transform(
          []<typename T>(variant2::variant<variant2::monostate, T, std::exception_ptr> & var)
              -> system::result<monostate_as_void<T>, std::exception_ptr>
          {
            BOOST_ASSERT(var.index() != 0u);
            if (var.index() == 1u)
            {
              if constexpr (std::is_same_v<T, variant2::monostate>)
                return {system::in_place_value};
              else
                return {system::in_place_value, std::move(get<1>(var))};
            }
            else
              return {system::in_place_error, std::move(get<2>(var))};

          }
          , result);
    }
  };
  awaitable operator co_await() &&
  {
    return awaitable(args, std::make_index_sequence<sizeof...(Args)>{});
  }
};

template<typename Range>
struct gather_ranged_impl
{
  Range aws;

  using result_type = system::result<
      co_await_result_t<std::decay_t<decltype(*std::begin(std::declval<Range>()))>>,
      std::exception_ptr>;

  using result_storage_type = variant2::variant<
      variant2::monostate,
      void_as_monostate<
          co_await_result_t<std::decay_t<decltype(*std::begin(std::declval<Range>()))>>
        >,
      std::exception_ptr>;

  struct awaitable : fork::shared_state
  {
    using type = std::decay_t<decltype(*std::begin(std::declval<Range>()))>;
#if !defined(BOOST_COBALT_NO_PMR)
    pmr::polymorphic_allocator<void> alloc{&resource};
    std::conditional_t<awaitable_type<type>, Range &,
                       pmr::vector<co_awaitable_type<type>>> aws;

    pmr::vector<bool> ready{std::size(aws), alloc};
    pmr::vector<asio::cancellation_signal> cancel{std::size(aws), alloc};
    pmr::vector<result_storage_type> result{cancel.size(), alloc};

#else
    std::allocator<void> alloc{};
    std::conditional_t<awaitable_type<type>, Range &,
        std::vector<co_awaitable_type<type>>> aws;

    std::vector<bool> ready{std::size(aws), alloc};
    std::vector<asio::cancellation_signal> cancel{std::size(aws), alloc};
    std::vector<result_storage_type> result{cancel.size(), alloc};
#endif


    awaitable(Range & aws_, std::false_type /* needs operator co_await */)
      : fork::shared_state((512 + sizeof(co_awaitable_type<type>)) * std::size(aws_))
      , aws{alloc}
      , ready{std::size(aws_), alloc}
      , cancel{std::size(aws_), alloc}
    {
      aws.reserve(std::size(aws_));
      for (auto && a : aws_)
      {
        using a_0 = std::decay_t<decltype(a)>;
        using a_t = std::conditional_t<
            std::is_lvalue_reference_v<Range>, a_0 &, a_0 &&>;
        aws.emplace_back(awaitable_type_getter<a_t>(static_cast<a_t>(a)));
      }


      std::transform(std::begin(this->aws),
                     std::end(this->aws),
                     std::begin(ready),
                     [](auto & aw) {return aw.await_ready();});
    }
    awaitable(Range & aws, std::true_type /* needs operator co_await */)
        : fork::shared_state((512 + sizeof(co_awaitable_type<type>)) * std::size(aws))
        , aws(aws)
    {
      std::transform(std::begin(aws), std::end(aws), std::begin(ready), [](auto & aw) {return aw.await_ready();});
    }

    awaitable(Range & aws)
      : awaitable(aws, std::bool_constant<awaitable_type<type>>{})
    {
    }

    void interrupt_await()
    {
      using t = std::conditional_t<std::is_reference_v<Range>,
                                   co_awaitable_type<type> &,
                                   co_awaitable_type<type> &&>;

      if constexpr (interruptible<t>)
        for (auto & aw : aws)
          static_cast<t>(aw).interrupt_await();
    }

    static detail::fork await_impl(awaitable & this_, std::size_t idx)
    try
    {
      auto & aw = *std::next(std::begin(this_.aws), idx);
      auto rd = aw.await_ready();
      if (!rd)
      {
        co_await this_.cancel[idx].slot();
        co_await detail::fork::wired_up;
        if constexpr (std::is_void_v<decltype(aw.await_resume())>)
        {
          co_await aw;
          this_.result[idx].template emplace<1u>();
        }
        else
          this_.result[idx].template emplace<1u>(co_await aw);
      }
      else
      {
        if constexpr (std::is_void_v<decltype(aw.await_resume())>)
        {
          aw.await_resume();
          this_.result[idx].template emplace<1u>();
        }
        else
          this_.result[idx].template emplace<1u>(aw.await_resume());
      }
    }
    catch(...)
    {
      this_.result[idx].template emplace<2u>(std::current_exception());

    }

    detail::fork last_forked;
    std::size_t last_index = 0u;

    bool await_ready()
    {
      while (last_index < cancel.size())
      {
        last_forked = await_impl(*this, last_index++);
        if (!last_forked.done())
          return false; // one coro didn't immediately complete!
      }
      last_forked.release();
      return true;
    }

    template<typename H>
    auto await_suspend(
          std::coroutine_handle<H> h
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
        , const boost::source_location & loc = BOOST_CURRENT_LOCATION
#endif
    )
    {
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
      this->loc = loc;
#endif
      exec = &detail::get_executor(h);

      last_forked.release().resume();
      while (last_index < cancel.size())
        await_impl(*this, last_index++).release();

      if (!this->outstanding_work()) // already done, resume rightaway.
        return false;

      // arm the cancel
      assign_cancellation(
          h,
          [&](asio::cancellation_type ct)
          {
            for (auto & cs : cancel)
              cs.emit(ct);
          });

      this->coro.reset(h.address());
      return true;
    }

#if _MSC_VER
    BOOST_NOINLINE
#endif
    auto await_resume()
    {
#if !defined(BOOST_COBALT_NO_PMR)
      pmr::vector<result_type> res{result.size(), this_thread::get_allocator()};
#else
      std::vector<result_type> res(result.size());
#endif

      std::transform(
          result.begin(), result.end(), res.begin(),
          [](result_storage_type & res) -> result_type
          {
            BOOST_ASSERT(res.index() != 0u);
            if (res.index() == 1u)
            {
              if constexpr (std::is_void_v<typename result_type::value_type>)
                return system::in_place_value;
              else
                return {system::in_place_value, std::move(get<1u>(res))};
            }
            else
              return {system::in_place_error, get<2u>(res)};
          });

      return res;
    }
  };
  awaitable operator co_await() && {return awaitable{aws};}
};

}

#endif //BOOST_COBALT_DETAIL_GATHER_HPP
