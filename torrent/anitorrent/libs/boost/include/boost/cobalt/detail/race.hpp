//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_RACE_HPP
#define BOOST_COBALT_DETAIL_RACE_HPP

#include <boost/cobalt/detail/await_result_helper.hpp>
#include <boost/cobalt/detail/fork.hpp>
#include <boost/cobalt/detail/handler.hpp>
#include <boost/cobalt/detail/forward_cancellation.hpp>
#include <boost/cobalt/result.hpp>
#include <boost/cobalt/this_thread.hpp>
#include <boost/cobalt/detail/util.hpp>

#include <boost/asio/bind_allocator.hpp>
#include <boost/asio/bind_cancellation_slot.hpp>
#include <boost/asio/bind_executor.hpp>
#include <boost/asio/cancellation_signal.hpp>
#include <boost/asio/associated_cancellation_slot.hpp>


#include <boost/intrusive_ptr.hpp>
#include <boost/core/demangle.hpp>
#include <boost/core/span.hpp>
#include <boost/variant2/variant.hpp>

#include <coroutine>
#include <optional>
#include <algorithm>


namespace boost::cobalt::detail
{

struct left_race_tag {};

// helpers it determining the type of things;
template<typename Base, // range of aw
         typename Awaitable = Base>
struct race_traits
{
  // for a ranges race this is based on the range, not the AW in it.
  constexpr static bool is_lvalue = std::is_lvalue_reference_v<Base>;

  // what the value is supposed to be cast to before the co_await_operator
  using awaitable = std::conditional_t<is_lvalue, std::decay_t<Awaitable> &, Awaitable &&>;

  // do we need operator co_await
  constexpr static bool is_actual = awaitable_type<awaitable>;

  // the type with .await_ functions & interrupt_await
  using actual_awaitable
        = std::conditional_t<
            is_actual,
              awaitable,
              decltype(get_awaitable_type(std::declval<awaitable>()))>;

  // the type to be used with interruptible
  using interruptible_type
        = std::conditional_t<
              std::is_lvalue_reference_v<Base>,
              std::decay_t<actual_awaitable> &,
              std::decay_t<actual_awaitable> &&>;

  constexpr static bool interruptible =
      cobalt::interruptible<interruptible_type>;

  static void do_interrupt(std::decay_t<actual_awaitable> & aw)
  {
    if constexpr (interruptible)
        static_cast<interruptible_type>(aw).interrupt_await();
  }

};

struct interruptible_base
{
  virtual void interrupt_await() = 0;
};

template<asio::cancellation_type Ct, typename URBG, typename ... Args>
struct race_variadic_impl
{

  template<typename URBG_>
  race_variadic_impl(URBG_ && g, Args && ... args)
      : args{std::forward<Args>(args)...}, g(std::forward<URBG_>(g))
  {
  }

  std::tuple<Args...> args;
  URBG g;

  constexpr static std::size_t tuple_size = sizeof...(Args);

  struct awaitable : fork::static_shared_state<256 * tuple_size>
  {

#if !defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    boost::source_location loc;
#endif

    template<std::size_t ... Idx>
    awaitable(std::tuple<Args...> & args, URBG & g, std::index_sequence<Idx...>) :
        aws{args}
    {
      if constexpr (!std::is_same_v<URBG, left_race_tag>)
        std::shuffle(impls.begin(), impls.end(), g);
      std::fill(working.begin(), working.end(), nullptr);
    }

    std::tuple<Args...> & aws;
    std::array<asio::cancellation_signal, tuple_size> cancel_;

    template<typename > constexpr static auto make_null() {return nullptr;};
    std::array<asio::cancellation_signal*, tuple_size> cancel = {make_null<Args>()...};

    std::array<interruptible_base*, tuple_size> working;

    std::size_t index{std::numeric_limits<std::size_t>::max()};

    constexpr static bool all_void = (std::is_void_v<co_await_result_t<Args>> && ... );
    std::optional<variant2::variant<void_as_monostate<co_await_result_t<Args>>...>> result;
    std::exception_ptr error;

    bool has_result() const
    {
      return index != std::numeric_limits<std::size_t>::max();
    }

    void cancel_all()
    {
      interrupt_await();
      for (auto i = 0u; i < tuple_size; i++)
        if (auto &r = cancel[i]; r)
          std::exchange(r, nullptr)->emit(Ct);
    }

    void interrupt_await()
    {
      for (auto i : working)
        if (i)
          i->interrupt_await();
    }

    template<typename T, typename Error>
    void assign_error(system::result<T, Error> & res)
    try
    {
      std::move(res).value(loc);
    }
    catch(...)
    {
      error = std::current_exception();
    }

    template<typename T>
    void assign_error(system::result<T, std::exception_ptr> & res)
    {
      error = std::move(res).error();
    }

    template<std::size_t Idx>
    static detail::fork await_impl(awaitable & this_)
    try
    {
      using traits = race_traits<mp11::mp_at_c<mp11::mp_list<Args...>, Idx>>;

      typename traits::actual_awaitable aw_{
          get_awaitable_type(
              static_cast<typename traits::awaitable>(std::get<Idx>(this_.aws))
              )
      };

      as_result_t aw{aw_};


      struct interruptor final : interruptible_base
      {
        std::decay_t<typename traits::actual_awaitable> & aw;
        interruptor(std::decay_t<typename traits::actual_awaitable> & aw) : aw(aw) {}
        void interrupt_await() override
        {
          traits::do_interrupt(aw);
        }
      };
      interruptor in{aw_};
      //if constexpr (traits::interruptible)
        this_.working[Idx] = &in;

      auto transaction = [&this_, idx = Idx] {
        if (this_.has_result())
          boost::throw_exception(std::runtime_error("Another transaction already started"));
        this_.cancel[idx] = nullptr;
        // reserve the index early bc
        this_.index = idx;
        this_.cancel_all();
      };

      co_await fork::set_transaction_function(transaction);
      // check manually if we're ready
      auto rd = aw.await_ready();
      if (!rd)
      {
        this_.cancel[Idx] = &this_.cancel_[Idx];
        co_await this_.cancel[Idx]->slot();
        // make sure the executor is set
        co_await detail::fork::wired_up;

        // do the await - this doesn't call await-ready again
        if constexpr (std::is_void_v<decltype(aw_.await_resume())>)
        {
          auto res = co_await aw;
          if (!this_.has_result())
          {
            this_.index = Idx;
            if (res.has_error())
              this_.assign_error(res);
          }
          if constexpr(!all_void)
            if (this_.index == Idx && !res.has_error())
              this_.result.emplace(variant2::in_place_index<Idx>);
        }
        else
        {
          auto val = co_await aw;
          if (!this_.has_result())
            this_.index = Idx;
          if (this_.index == Idx)
          {
            if (val.has_error())
              this_.assign_error(val);
            else
              this_.result.emplace(variant2::in_place_index<Idx>, *std::move(val));
          }
        }
        this_.cancel[Idx] = nullptr;
      }
      else
      {
        if (!this_.has_result())
          this_.index = Idx;
        if constexpr (std::is_void_v<decltype(aw_.await_resume())>)
        {
          auto res = aw.await_resume();
          if (this_.index == Idx)
          {
            if (res.has_error())
              this_.assign_error(res);
            else
              this_.result.emplace(variant2::in_place_index<Idx>);
          }
        }
        else
        {
          if (this_.index == Idx)
          {
            auto res = aw.await_resume();
            if (res.has_error())
              this_.assign_error(res);
            else
              this_.result.emplace(variant2::in_place_index<Idx>, *std::move(res));
          }
          else
            aw.await_resume();
        }
        this_.cancel[Idx] = nullptr;
      }
      this_.cancel_all();
      this_.working[Idx] = nullptr;
    }
    catch(...)
    {
      if (!this_.has_result())
        this_.index = Idx;
      if (this_.index == Idx)
        this_.error = std::current_exception();
      this_.working[Idx] = nullptr;
    }

    std::array<detail::fork(*)(awaitable&), tuple_size> impls {
        []<std::size_t ... Idx>(std::index_sequence<Idx...>)
        {
          return std::array<detail::fork(*)(awaitable&), tuple_size>{&await_impl<Idx>...};
        }(std::make_index_sequence<tuple_size>{})
    };

    detail::fork last_forked;

    bool await_ready()
    {
      last_forked = impls[0](*this);
      return last_forked.done();
    }

    template<typename H>
    auto await_suspend(
        std::coroutine_handle<H> h,
        const boost::source_location & loc = BOOST_CURRENT_LOCATION)
    {
      this->loc = loc;

      this->exec = &cobalt::detail::get_executor(h);
      last_forked.release().resume();

      if (!this->outstanding_work()) // already done, resume rightaway.
        return false;

      for (std::size_t idx = 1u;
           idx < tuple_size; idx++) // we'
      {
        auto l = impls[idx](*this);
        const auto d = l.done();
        l.release();
        if (d)
          break;
      }

      if (!this->outstanding_work()) // already done, resume rightaway.
        return false;

      // arm the cancel
      assign_cancellation(
          h,
          [&](asio::cancellation_type ct)
          {
            for (auto & cs : cancel)
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
      if constexpr (all_void)
        return index;
      else
        return std::move(*result);
    }

    auto await_resume(const as_tuple_tag &)
    {
      if constexpr (all_void)
        return std::make_tuple(error, index);
      else
        return std::make_tuple(error, std::move(*result));
    }

    auto await_resume(const as_result_tag & )
        -> system::result<std::conditional_t<all_void, std::size_t, variant2::variant<void_as_monostate<co_await_result_t<Args>>...>>, std::exception_ptr>
    {
      if (error)
        return {system::in_place_error, error};
      if constexpr (all_void)
        return {system::in_place_value, index};
      else
        return {system::in_place_value, std::move(*result)};
    }
  };
  awaitable operator co_await() &&
  {
    return awaitable{args, g, std::make_index_sequence<tuple_size>{}};
  }
};


template<asio::cancellation_type Ct, typename URBG, typename Range>
struct race_ranged_impl
{

  using result_type = co_await_result_t<std::decay_t<decltype(*std::begin(std::declval<Range>()))>>;
  template<typename URBG_>
  race_ranged_impl(URBG_ && g, Range && rng)
      : range{std::forward<Range>(rng)}, g(std::forward<URBG_>(g))
  {
  }

  Range range;
  URBG g;

  struct awaitable : fork::shared_state
  {

#if !defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    boost::source_location loc;
#endif

    using type = std::decay_t<decltype(*std::begin(std::declval<Range>()))>;
    using traits = race_traits<Range, type>;

    std::size_t index{std::numeric_limits<std::size_t>::max()};

    std::conditional_t<
        std::is_void_v<result_type>,
        variant2::monostate,
        std::optional<result_type>> result;

    std::exception_ptr error;

#if !defined(BOOST_COBALT_NO_PMR)
    pmr::monotonic_buffer_resource res;
    pmr::polymorphic_allocator<void> alloc{&resource};

    Range &aws;

    struct dummy
    {
      template<typename ... Args>
      dummy(Args && ...) {}
    };

    std::conditional_t<traits::interruptible,
      pmr::vector<std::decay_t<typename traits::actual_awaitable>*>,
      dummy> working{std::size(aws), alloc};

    /* all below `reorder` is reordered
     *
     * cancel[idx] is for aws[reorder[idx]]
    */
    pmr::vector<std::size_t> reorder{std::size(aws), alloc};
    pmr::vector<asio::cancellation_signal> cancel_{std::size(aws), alloc};
    pmr::vector<asio::cancellation_signal*> cancel{std::size(aws), alloc};

#else
    Range &aws;

    struct dummy
    {
      template<typename ... Args>
      dummy(Args && ...) {}
    };

    std::conditional_t<traits::interruptible,
        std::vector<std::decay_t<typename traits::actual_awaitable>*>,
    dummy> working{std::size(aws), std::allocator<void>()};

    /* all below `reorder` is reordered
     *
     * cancel[idx] is for aws[reorder[idx]]
    */
    std::vector<std::size_t> reorder{std::size(aws), std::allocator<void>()};
    std::vector<asio::cancellation_signal> cancel_{std::size(aws), std::allocator<void>()};
    std::vector<asio::cancellation_signal*> cancel{std::size(aws), std::allocator<void>()};

#endif

    bool has_result() const {return index != std::numeric_limits<std::size_t>::max(); }


    awaitable(Range & aws, URBG & g)
        : fork::shared_state((256 + sizeof(co_awaitable_type<type>) + sizeof(std::size_t)) * std::size(aws))
        , aws(aws)
    {
      std::generate(reorder.begin(), reorder.end(), [i = std::size_t(0u)]() mutable {return i++;});
      if constexpr (traits::interruptible)
        std::fill(working.begin(), working.end(), nullptr);
      if constexpr (!std::is_same_v<URBG, left_race_tag>)
        std::shuffle(reorder.begin(), reorder.end(), g);
    }

    void cancel_all()
    {
      interrupt_await();
      for (auto & r : cancel)
        if (r)
          std::exchange(r, nullptr)->emit(Ct);
    }
    void interrupt_await()
    {
      if constexpr (traits::interruptible)
        for (auto aw : working)
          if (aw)
            traits::do_interrupt(*aw);
    }


    template<typename T, typename Error>
    void assign_error(system::result<T, Error> & res)
    try
    {
      std::move(res).value(loc);
    }
    catch(...)
    {
      error = std::current_exception();
    }

    template<typename T>
    void assign_error(system::result<T, std::exception_ptr> & res)
    {
      error = std::move(res).error();
    }

    static detail::fork await_impl(awaitable & this_, std::size_t idx)
    try
    {
      typename traits::actual_awaitable aw_{
          get_awaitable_type(
              static_cast<typename traits::awaitable>(*std::next(std::begin(this_.aws), idx))
              )};

      as_result_t aw{aw_};

      if constexpr (traits::interruptible)
        this_.working[idx] = &aw_;

      auto transaction = [&this_, idx = idx] {
        if (this_.has_result())
          boost::throw_exception(std::runtime_error("Another transaction already started"));
        this_.cancel[idx] = nullptr;
        // reserve the index early bc
        this_.index = idx;
        this_.cancel_all();
      };

      co_await fork::set_transaction_function(transaction);
      // check manually if we're ready
      auto rd = aw.await_ready();
      if (!rd)
      {
        this_.cancel[idx] = &this_.cancel_[idx];
        co_await this_.cancel[idx]->slot();
        // make sure the executor is set
        co_await detail::fork::wired_up;

        // do the await - this doesn't call await-ready again
        if constexpr (std::is_void_v<result_type>)
        {
          auto res = co_await aw;
          if (!this_.has_result())
          {
            if (res.has_error())
              this_.assign_error(res);
            this_.index = idx;
          }
        }
        else
        {
          auto val = co_await aw;
          if (!this_.has_result())
            this_.index = idx;
          if (this_.index == idx)
          {
            if (val.has_error())
              this_.assign_error(val);
            else
              this_.result.emplace(*std::move(val));
          }
        }
        this_.cancel[idx] = nullptr;
      }
      else
      {

        if (!this_.has_result())
          this_.index = idx;
        if constexpr (std::is_void_v<decltype(aw_.await_resume())>)
        {
          auto val = aw.await_resume();
          if (val.has_error())
            this_.assign_error(val);
        }
        else
        {
          if (this_.index == idx)
          {
            auto val = aw.await_resume();
            if (val.has_error())
              this_.assign_error(val);
            else
              this_.result.emplace(*std::move(val));
          }
          else
            aw.await_resume();
        }
        this_.cancel[idx] = nullptr;
      }
      this_.cancel_all();
      if constexpr (traits::interruptible)
        this_.working[idx] = nullptr;
    }
    catch(...)
    {
      if (!this_.has_result())
        this_.index = idx;
      if (this_.index == idx)
        this_.error = std::current_exception();
      if constexpr (traits::interruptible)
        this_.working[idx] = nullptr;
    }

    detail::fork last_forked;

    bool await_ready()
    {
      last_forked = await_impl(*this, reorder.front());
      return last_forked.done();
    }

    template<typename H>
    auto await_suspend(std::coroutine_handle<H> h,
                       const boost::source_location & loc = BOOST_CURRENT_LOCATION)
    {
      this->loc = loc;
      this->exec = &detail::get_executor(h);
      last_forked.release().resume();

      if (!this->outstanding_work()) // already done, resume rightaway.
        return false;

      for (auto itr = std::next(reorder.begin());
           itr < reorder.end(); std::advance(itr, 1)) // we'
      {
        auto l = await_impl(*this, *itr);
        auto d = l.done();
        l.release();
        if (d)
          break;
      }

      if (!this->outstanding_work()) // already done, resume rightaway.
        return false;

      // arm the cancel
      assign_cancellation(
          h,
          [&](asio::cancellation_type ct)
          {
            for (auto & cs : cancel)
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
      if constexpr (std::is_void_v<result_type>)
        return index;
      else
        return std::make_pair(index, *result);
    }

    auto await_resume(const as_tuple_tag &)
    {
      if constexpr (std::is_void_v<result_type>)
        return std::make_tuple(error, index);
      else
        return std::make_tuple(error, std::make_pair(index, std::move(*result)));
    }

    auto await_resume(const as_result_tag & )
    -> system::result<result_type, std::exception_ptr>
    {
      if (error)
        return {system::in_place_error, error};
      if constexpr (std::is_void_v<result_type>)
        return {system::in_place_value, index};
      else
        return {system::in_place_value, std::make_pair(index, std::move(*result))};
    }

  };
  awaitable operator co_await() &&
  {
    return awaitable{range, g};
  }
};

}

#endif //BOOST_COBALT_DETAIL_RACE_HPP
