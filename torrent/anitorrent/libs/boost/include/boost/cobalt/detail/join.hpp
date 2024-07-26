//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_JOIN_HPP
#define BOOST_COBALT_DETAIL_JOIN_HPP

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
struct join_variadic_impl
{
  using tuple_type = std::tuple<decltype(get_awaitable_type(std::declval<Args&&>()))...>;

  join_variadic_impl(Args && ... args)
      : args{std::forward<Args>(args)...}
  {
  }

  std::tuple<Args...> args;

  constexpr static std::size_t tuple_size = sizeof...(Args);

  struct awaitable : fork::static_shared_state<256 * tuple_size>
  {
    template<std::size_t ... Idx>
    awaitable(std::tuple<Args...> & args, std::index_sequence<Idx...>) :
        aws(awaitable_type_getter<Args>(std::get<Idx>(args))...)
    {
    }

    tuple_type aws;

    std::array<asio::cancellation_signal, tuple_size> cancel_;
    template<typename > constexpr static auto make_null() {return nullptr;};
    std::array<asio::cancellation_signal*, tuple_size> cancel = {make_null<Args>()...};

    constexpr static bool all_void = (std::is_void_v<co_await_result_t<Args>> && ...);
    template<typename T>
    using result_store_part =
        std::optional<void_as_monostate<co_await_result_t<T>>>;

    std::conditional_t<all_void,
                       variant2::monostate,
                       std::tuple<result_store_part<Args>...>> result;
    std::exception_ptr error;

    template<std::size_t Idx>
    void cancel_step()
    {
      auto &r = cancel[Idx];
      if (r)
        std::exchange(r, nullptr)->emit(asio::cancellation_type::all);
    }

    void cancel_all()
    {
      mp11::mp_for_each<mp11::mp_iota_c<sizeof...(Args)>>
          ([&](auto idx)
           {
             cancel_step<idx>();
           });
    }



    template<std::size_t Idx>
    void interrupt_await_step()
    {
      using type = std::tuple_element_t<Idx, tuple_type>;
      using t = std::conditional_t<std::is_reference_v<std::tuple_element_t<Idx, std::tuple<Args...>>>,
          type &,
          type &&>;

      if constexpr (interruptible<t>)
        if (this->cancel[Idx] != nullptr)
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
        this_.cancel[Idx] = &this_.cancel_[Idx];
        co_await this_.cancel[Idx]->slot();
        // make sure the executor is set
        co_await detail::fork::wired_up;
        // do the await - this doesn't call await-ready again

        if constexpr (std::is_void_v<decltype(aw.await_resume())>)
        {
          co_await aw;
          if constexpr (!all_void)
            std::get<Idx>(this_.result).emplace();
        }
        else
          std::get<Idx>(this_.result).emplace(co_await aw);
      }
      else
      {
        if constexpr (std::is_void_v<decltype(aw.await_resume())>)
        {
          aw.await_resume();
          if constexpr (!all_void)
            std::get<Idx>(this_.result).emplace();
        }
        else
          std::get<Idx>(this_.result).emplace(aw.await_resume());
      }

    }
    catch(...)
    {
      if (!this_.error)
           this_.error = std::current_exception();
      this_.cancel_all();
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
      this->exec = &detail::get_executor(h);
      last_forked.release().resume();
      while (last_index < tuple_size)
        impls[last_index++](*this).release();

      if (error)
        cancel_all();

      if (!this->outstanding_work()) // already done, resume rightaway.
        return false;

      // arm the cancel
      assign_cancellation(
          h,
          [&](asio::cancellation_type ct)
          {
            for (auto cs : cancel)
              if (cs)
                cs->emit(ct);
          });

      this->coro.reset(h.address());
      return true;
    }

#if _MSC_VER
    BOOST_NOINLINE
#endif
    auto await_resume()
    {
      if (error)
        std::rethrow_exception(error);
      if constexpr(!all_void)
        return mp11::tuple_transform(
            []<typename T>(std::optional<T> & var)
                -> T
            {
              BOOST_ASSERT(var.has_value());
              return std::move(*var);
            }, result);
    }

    auto await_resume(const as_tuple_tag &)
    {
      using t = decltype(await_resume());
      if constexpr(!all_void)
      {
        if (error)
          return std::make_tuple(error, t{});
        else
          return std::make_tuple(std::current_exception(),
                                 mp11::tuple_transform(
              []<typename T>(std::optional<T> & var)
                  -> T
              {
                BOOST_ASSERT(var.has_value());
                return std::move(*var);
              }, result));
      }
      else
        return std::make_tuple(error);
    }

    auto await_resume(const as_result_tag &)
    {
      using t = decltype(await_resume());
      using rt = system::result<t, std::exception_ptr>;
      if (error)
        return rt(system::in_place_error, error);

      if constexpr(!all_void)
        return mp11::tuple_transform(
            []<typename T>(std::optional<T> & var)
                -> rt
            {
              BOOST_ASSERT(var.has_value());
              return std::move(*var);
            }, result);
      else
        return rt{system::in_place_value};
    }
  };
  awaitable operator co_await() &&
  {
    return awaitable(args, std::make_index_sequence<sizeof...(Args)>{});
  }
};

template<typename Range>
struct join_ranged_impl
{
  Range aws;

  using result_type = co_await_result_t<std::decay_t<decltype(*std::begin(std::declval<Range>()))>>;

  constexpr static std::size_t result_size =
      sizeof(std::conditional_t<std::is_void_v<result_type>, variant2::monostate, result_type>);

  struct awaitable : fork::shared_state
  {
    struct dummy
    {
      template<typename ... Args>
      dummy(Args && ...) {}
    };

    using type = std::decay_t<decltype(*std::begin(std::declval<Range>()))>;
#if !defined(BOOST_COBALT_NO_PMR)
    pmr::polymorphic_allocator<void> alloc{&resource};

    std::conditional_t<awaitable_type<type>, Range &,
                       pmr::vector<co_awaitable_type<type>>> aws;

    pmr::vector<bool> ready{std::size(aws), alloc};
    pmr::vector<asio::cancellation_signal> cancel_{std::size(aws), alloc};
    pmr::vector<asio::cancellation_signal*> cancel{std::size(aws), alloc};



    std::conditional_t<
        std::is_void_v<result_type>,
        dummy,
        pmr::vector<std::optional<void_as_monostate<result_type>>>>
          result{
          cancel.size(),
          alloc};
#else
    std::allocator<void> alloc;
    std::conditional_t<awaitable_type<type>, Range &,  std::vector<co_awaitable_type<type>>> aws;

    std::vector<bool> ready{std::size(aws), alloc};
    std::vector<asio::cancellation_signal> cancel_{std::size(aws), alloc};
    std::vector<asio::cancellation_signal*> cancel{std::size(aws), alloc};

    std::conditional_t<
        std::is_void_v<result_type>,
        dummy,
        std::vector<std::optional<void_as_monostate<result_type>>>>
          result{
          cancel.size(),
          alloc};
#endif
    std::exception_ptr error;

    awaitable(Range & aws_, std::false_type /* needs  operator co_await */)
      :  fork::shared_state((512 + sizeof(co_awaitable_type<type>) + result_size) * std::size(aws_))
      , aws{alloc}
      , ready{std::size(aws_), alloc}
      , cancel_{std::size(aws_), alloc}
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
        : fork::shared_state((512 + sizeof(co_awaitable_type<type>) + result_size) * std::size(aws))
        , aws(aws)
    {
      std::transform(std::begin(aws), std::end(aws), std::begin(ready), [](auto & aw) {return aw.await_ready();});
    }

    awaitable(Range & aws)
      : awaitable(aws, std::bool_constant<awaitable_type<type>>{})
    {
    }

    void cancel_all()
    {
      for (auto & r : cancel)
        if (r)
          std::exchange(r, nullptr)->emit(asio::cancellation_type::all);
    }

    void interrupt_await()
    {
      using t = std::conditional_t<std::is_reference_v<Range>,
          co_awaitable_type<type> &,
          co_awaitable_type<type> &&>;

      if constexpr (interruptible<t>)
      {
        std::size_t idx = 0u;
        for (auto & aw : aws)
          if (cancel[idx])
            static_cast<t>(aw).interrupt_await();
      }
    }


    static detail::fork await_impl(awaitable & this_, std::size_t idx)
    try
    {
      auto & aw = *std::next(std::begin(this_.aws), idx);
      auto rd = aw.await_ready();
      if (!rd)
      {
        this_.cancel[idx] = &this_.cancel_[idx];
        co_await this_.cancel[idx]->slot();
        co_await detail::fork::wired_up;
        if constexpr (std::is_void_v<decltype(aw.await_resume())>)
          co_await aw;
        else
          this_.result[idx].emplace(co_await aw);
      }
      else
      {
        if constexpr (std::is_void_v<decltype(aw.await_resume())>)
          aw.await_resume();
        else
          this_.result[idx].emplace(aw.await_resume());
      }
    }
    catch(...)
    {
      if (!this_.error)
        this_.error = std::current_exception();
      this_.cancel_all();
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

      if (error)
        cancel_all();

      if (!this->outstanding_work()) // already done, resume right away.
        return false;

      // arm the cancel
      assign_cancellation(
          h,
          [&](asio::cancellation_type ct)
          {
            for (auto cs : cancel)
              if (cs)
                cs->emit(ct);
          });


      this->coro.reset(h.address());
      return true;
    }

    auto await_resume(const as_tuple_tag & )
    {
#if defined(BOOST_COBALT_NO_PMR)
      std::vector<result_type> rr;
#else
      pmr::vector<result_type> rr{this_thread::get_allocator()};
#endif

      if (error)
        return std::make_tuple(error, rr);
      if constexpr (!std::is_void_v<result_type>)
      {
        rr.reserve(result.size());
        for (auto & t : result)
          rr.push_back(*std::move(t));
        return std::make_tuple(std::exception_ptr(), std::move(rr));
      }
    }

    auto await_resume(const as_result_tag & )
    {
#if defined(BOOST_COBALT_NO_PMR)
      std::vector<result_type> rr;
#else
      pmr::vector<result_type> rr{this_thread::get_allocator()};
#endif

      if (error)
        return system::result<decltype(rr), std::exception_ptr>(error);
      if constexpr (!std::is_void_v<result_type>)
      {
        rr.reserve(result.size());
        for (auto & t : result)
          rr.push_back(*std::move(t));
        return rr;
      }
    }

#if _MSC_VER
    BOOST_NOINLINE
#endif
    auto await_resume()
    {
      if (error)
        std::rethrow_exception(error);
      if constexpr (!std::is_void_v<result_type>)
      {
#if defined(BOOST_COBALT_NO_PMR)
        std::vector<result_type> rr;
#else
        pmr::vector<result_type> rr{this_thread::get_allocator()};
#endif
        rr.reserve(result.size());
        for (auto & t : result)
          rr.push_back(*std::move(t));
        return rr;
      }
    }
  };
  awaitable operator co_await() && {return awaitable{aws};}
};

}


#endif //BOOST_COBALT_DETAIL_JOIN_HPP
