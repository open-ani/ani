//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_GENERATOR_HPP
#define BOOST_COBALT_DETAIL_GENERATOR_HPP

#include <boost/cobalt/concepts.hpp>
#include <boost/cobalt/result.hpp>
#include <boost/cobalt/detail/exception.hpp>
#include <boost/cobalt/detail/forward_cancellation.hpp>
#include <boost/cobalt/detail/this_thread.hpp>
#include <boost/cobalt/unique_handle.hpp>
#include <boost/cobalt/detail/wrapper.hpp>

#include <boost/asio/bind_allocator.hpp>
#include <boost/core/exchange.hpp>
#include <boost/variant2/variant.hpp>

namespace boost::cobalt
{


template<typename Yield, typename Push>
struct generator;

namespace detail
{

template<typename Yield, typename Push>
struct generator_yield_awaitable;

template<typename Yield, typename Push>
struct generator_receiver;

template<typename Yield, typename Push>
struct generator_receiver_base
{
  std::optional<Push> pushed_value;
  auto get_awaitable(const Push  & push) requires std::is_copy_constructible_v<Push>
  {
    using impl = generator_receiver<Yield, Push>;
    return typename impl::awaitable{static_cast<impl*>(this), &push};
  }
  auto get_awaitable(      Push && push)
  {
    using impl = generator_receiver<Yield, Push>;
    return typename impl::awaitable{static_cast<impl*>(this), &push};
  }
};

template<typename Yield>
struct generator_receiver_base<Yield, void>
{
  bool pushed_value{false};

  auto get_awaitable()
  {
    using impl = generator_receiver<Yield, void>;
    return typename impl::awaitable{static_cast<impl*>(this), static_cast<void*>(nullptr)};
  }
};

template<typename Yield, typename Push>
struct generator_promise;

template<typename Yield, typename Push>
struct generator_receiver : generator_receiver_base<Yield, Push>
{
  std::exception_ptr exception;
  std::optional<Yield> result, result_buffer;
  Yield get_result()
  {
    if (result_buffer)
    {
      auto res = *std::exchange(result, std::nullopt);
      if (result_buffer)
        result.emplace(*std::exchange(result_buffer, std::nullopt));
      return res;
    }
    else
      return *std::exchange(result, std::nullopt);
  }
  bool done = false;
  unique_handle<void> awaited_from{nullptr};
  unique_handle<generator_promise<Yield, Push>> yield_from{nullptr};

  bool lazy = false;

  bool ready() { return exception || result || done; }

  generator_receiver() = default;
  generator_receiver(generator_receiver && lhs)
  : generator_receiver_base<Yield, Push>{std::move(lhs.pushed_value)},
    exception(std::move(lhs.exception)), done(lhs.done),
    result(std::move(lhs.result)),
    result_buffer(std::move(lhs.result_buffer)),
    awaited_from(std::move(lhs.awaited_from)), yield_from{std::move(lhs.yield_from)},
    lazy(lhs.lazy), reference(lhs.reference), cancel_signal(lhs.cancel_signal)

  {
    if (!lhs.done && !lhs.exception)
    {
      reference = this;
      lhs.exception = moved_from_exception();
    }
    lhs.done = true;
  }

  ~generator_receiver()
  {
    if (!done && reference == this)
      reference = nullptr;
  }

  generator_receiver(generator_receiver * &reference, asio::cancellation_signal & cancel_signal)
  : reference(reference), cancel_signal(cancel_signal)
  {
    reference = this;
  }

  generator_receiver  * &reference;
  asio::cancellation_signal & cancel_signal;

  using yield_awaitable = generator_yield_awaitable<Yield, Push>;

  yield_awaitable get_yield_awaitable(generator_promise<Yield, Push> * pro) {return {pro}; }
  static yield_awaitable terminator()   {return {nullptr}; }

  template<typename T>
  void yield_value(T && t)
  {
    if (!result)
      result.emplace(std::forward<T>(t));
    else
    {
      BOOST_ASSERT(!result_buffer);
      result_buffer.emplace(std::forward<T>(t));
    }

  }

  struct awaitable
  {
    generator_receiver *self;
    std::exception_ptr ex;
    asio::cancellation_slot cl;

    variant2::variant<variant2::monostate, Push *, const Push *> to_push;


    awaitable(generator_receiver * self, Push * to_push) : self(self), to_push(to_push)
    {
    }
    awaitable(generator_receiver * self, const Push * to_push)
        : self(self), to_push(to_push)
    {
    }

    awaitable(const awaitable & aw) noexcept : self(aw.self), to_push(aw.to_push)
    {
    }

    bool await_ready() const
    {
        BOOST_ASSERT(!ex);
        return self->ready();
    }

    template<typename Promise>
    std::coroutine_handle<void> await_suspend(std::coroutine_handle<Promise> h)
    {
      if (self->done) // ok, so we're actually done already, so noop
        return std::noop_coroutine();


      if (!ex && self->awaited_from != nullptr) // generator already being awaited, that's an error!
          ex = already_awaited();

      if (ex)
        return h;

      if constexpr (requires (Promise p) {p.get_cancellation_slot();})
        if ((cl = h.promise().get_cancellation_slot()).is_connected())
          cl.emplace<forward_cancellation>(self->cancel_signal);

      self->awaited_from.reset(h.address());

      std::coroutine_handle<void> res = std::noop_coroutine();
      if (self->yield_from != nullptr)
        res = self->yield_from.release();

      if ((to_push.index() > 0) && !self->pushed_value && self->lazy)
      {
        if constexpr (std::is_void_v<Push>)
          self->pushed_value = true;
        else
        {
          if (to_push.index() == 1)
            self->pushed_value.emplace(std::move(*variant2::get<1>(to_push)));
          else
          {
            if constexpr (std::is_copy_constructible_v<Push>)
              self->pushed_value.emplace(std::move(*variant2::get<2>(to_push)));
            else
            {
              BOOST_ASSERT(!"push value is not movable");
            }
          }
        }
        to_push = variant2::monostate{};
      }
      return std::coroutine_handle<void>::from_address(res.address());
    }

    Yield await_resume(const boost::source_location & loc = BOOST_CURRENT_LOCATION)
    {
      return await_resume(as_result_tag{}).value(loc);
    }

    std::tuple<std::exception_ptr, Yield> await_resume(
        const as_tuple_tag &)
    {
      auto res = await_resume(as_result_tag{});
      if (res.has_error())
          return {res.error(), Yield{}};
      else
          return {nullptr, res.value()};
    }

    system::result<Yield, std::exception_ptr> await_resume(const as_result_tag& )
    {
      if (cl.is_connected())
        cl.clear();
      if (ex)
        return {system::in_place_error, ex};
      if (self->exception)
        return {system::in_place_error, std::exchange(self->exception, nullptr)};
      if (!self->result) // missing co_return this is accepted behaviour, if the compiler agrees
        return {system::in_place_error, std::make_exception_ptr(std::runtime_error("cobalt::generator returned void"))};

      if (to_push.index() > 0)
      {
        BOOST_ASSERT(!self->pushed_value);
        if constexpr (std::is_void_v<Push>)
          self->pushed_value = true;
        else
        {
          if (to_push.index() == 1)
            self->pushed_value.emplace(std::move(*variant2::get<1>(to_push)));
          else
          {
            if constexpr (std::is_copy_constructible_v<Push>)
              self->pushed_value.emplace(std::move(*variant2::get<2>(to_push)));
            else
            {
              BOOST_ASSERT(!"push value is not movable");
            }
          }

        }
        to_push = variant2::monostate{};
      }

      // now we also want to resume the coroutine, so it starts work
      if (self->yield_from != nullptr && !self->lazy)
      {
        auto exec = self->yield_from->get_executor();
        auto alloc = asio::get_associated_allocator(self->yield_from);
        asio::post(
            std::move(exec),
            asio::bind_allocator(
                alloc,
                [y = std::exchange(self->yield_from, nullptr)]() mutable
                {
                  if (y->receiver) // make sure we only resume eagerly when attached to a generator object
                    std::move(y)();
                }));
      }

      return {system::in_place_value, self->get_result()};
    }

    void interrupt_await() &
    {
      if (!self)
        return ;
      ex = detached_exception();

      if (self->awaited_from)
        self->awaited_from.release().resume();
    }
  };

  void interrupt_await() &
  {
    exception = detached_exception();
    awaited_from.release().resume();
  }

  void rethrow_if()
  {
    if (exception)
      std::rethrow_exception(exception);
  }
};

template<typename Yield, typename Push>
struct generator_promise
    : promise_memory_resource_base,
      promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>,
      promise_throw_if_cancelled_base,
      enable_awaitables<generator_promise<Yield, Push>>,
      enable_await_allocator<generator_promise<Yield, Push>>,
      enable_await_executor< generator_promise<Yield, Push>>
{
  using promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>::await_transform;
  using promise_throw_if_cancelled_base::await_transform;
  using enable_awaitables<generator_promise<Yield, Push>>::await_transform;
  using enable_await_allocator<generator_promise<Yield, Push>>::await_transform;
  using enable_await_executor<generator_promise<Yield, Push>>::await_transform;

  [[nodiscard]] generator<Yield, Push> get_return_object()
  {
    return generator<Yield, Push>{this};
  }

  mutable asio::cancellation_signal signal;

  using executor_type = executor;
  executor_type exec;
  const executor_type & get_executor() const {return exec;}

  template<typename ... Args>
  generator_promise(Args & ...args)
    :
#if !defined(BOOST_COBALT_NO_PMR)
      promise_memory_resource_base(detail::get_memory_resource_from_args(args...)),
#endif
      exec{detail::get_executor_from_args(args...)}
  {
    this->reset_cancellation_source(signal.slot());
  }

  std::suspend_never initial_suspend() {return {};}

  struct final_awaitable
  {
    generator_promise * generator;
    bool await_ready() const noexcept
    {
      return generator->receiver && generator->receiver->awaited_from.get() == nullptr;
    }

    auto await_suspend(std::coroutine_handle<generator_promise> h) noexcept
    {
      std::coroutine_handle<void> res = std::noop_coroutine();
      if (generator->receiver && generator->receiver->awaited_from.get() != nullptr)
        res = generator->receiver->awaited_from.release();
      if (generator->receiver)
        generator->receiver->done = true;


      if (auto & rec = h.promise().receiver; rec != nullptr)
      {
        if (!rec->done && !rec->exception)
          rec->exception = detail::completed_unexpected();
        rec->done = true;
        rec->awaited_from.reset(nullptr);
        rec = nullptr;
      }

      detail::self_destroy(h);
      return res;
    }

    void await_resume() noexcept
    {
      if (generator->receiver)
        generator->receiver->done = true;
    }
  };

  auto final_suspend() noexcept
  {
    return final_awaitable{this};
  }

  void unhandled_exception()
  {
    if (this->receiver)
      this->receiver->exception = std::current_exception();
    else
      throw ;
  }

  void return_value(const Yield & res) requires std::is_copy_constructible_v<Yield>
  {
    if (this->receiver)
      this->receiver->yield_value(res);
  }

  void return_value(Yield && res)
  {
    if (this->receiver)
      this->receiver->yield_value(std::move(res));
  }

  generator_receiver<Yield, Push>* receiver{nullptr};

  auto await_transform(this_coro::initial_t val)
  {
    if(receiver)
    {
      receiver->lazy = true;
      return receiver->get_yield_awaitable(this);
    }
    else
      return generator_receiver<Yield, Push>::terminator();
  }

  template<typename Yield_>
  auto yield_value(Yield_ && ret)
  {
    if(receiver)
    {
      // if this is lazy, there might still be a value in there.
      receiver->yield_value(std::forward<Yield_>(ret));
      return receiver->get_yield_awaitable(this);
    }
    else
      return generator_receiver<Yield, Push>::terminator();
  }

  void interrupt_await() &
  {
    if (this->receiver)
    {
      this->receiver->exception = detached_exception();
      std::coroutine_handle<void>::from_address(this->receiver->awaited_from.release()).resume();
    }
  }

  ~generator_promise()
  {
    if (this->receiver)
    {
      if (!this->receiver->done && !this->receiver->exception)
        this->receiver->exception = detail::completed_unexpected();
      this->receiver->done = true;
      this->receiver->awaited_from.reset(nullptr);
    }
  }

};

template<typename Yield, typename Push>
struct generator_yield_awaitable
{
  generator_promise<Yield, Push> *self;
  constexpr bool await_ready() const
  {
    return self && self->receiver && self->receiver->pushed_value && !self->receiver->result;
  }

  std::coroutine_handle<void> await_suspend(
        std::coroutine_handle<generator_promise<Yield, Push>> h
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
      , const boost::source_location & loc = BOOST_CURRENT_LOCATION
#endif
      )
  {
    if (self == nullptr) // we're a terminator, kill it
    {
      if (auto & rec = h.promise().receiver; rec != nullptr)
      {
        if (!rec->done && !rec->exception)
          rec->exception = detail::completed_unexpected();
        rec->done = true;
        rec->awaited_from.reset(nullptr);
        rec = nullptr;
      }

      detail::self_destroy(h);
      return std::noop_coroutine();
    }
    std::coroutine_handle<void> res = std::noop_coroutine();
    if (self->receiver->awaited_from.get() != nullptr)
      res = self->receiver->awaited_from.release();
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    self->receiver->yield_from.reset(&h.promise(), loc);
#else
    self->receiver->yield_from.reset(&h.promise());
#endif
    return res;
  }

  Push await_resume()
  {
    BOOST_ASSERT(self->receiver);
    BOOST_ASSERT(self->receiver->pushed_value);
    return *std::exchange(self->receiver->pushed_value, std::nullopt);
  }
};

template<typename Yield>
struct generator_yield_awaitable<Yield, void>
{
  generator_promise<Yield, void> *self;
  constexpr bool await_ready() { return self && self->receiver && self->receiver->pushed_value; }

  std::coroutine_handle<> await_suspend(
      std::coroutine_handle<generator_promise<Yield, void>> h
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
      , const boost::source_location & loc = BOOST_CURRENT_LOCATION
#endif
  )
  {
    if (self == nullptr) // we're a terminator, kill it
    {
      if (auto & rec = h.promise().receiver; rec != nullptr)
      {
        if (!rec->done && !rec->exception)
          rec->exception = detail::completed_unexpected();
        rec->done = true;
        rec->awaited_from.reset(nullptr);
        rec = nullptr;
      }
      detail::self_destroy(h);
      return std::noop_coroutine();
    }
    std::coroutine_handle<void> res = std::noop_coroutine();
    BOOST_ASSERT(self);
    if (self->receiver->awaited_from.get() != nullptr)
      res = self->receiver->awaited_from.release();
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    self->receiver->yield_from.reset(&h.promise(), loc);
#else
    self->receiver->yield_from.reset(&h.promise());
#endif
    return res;
  }

  void await_resume()
  {
    BOOST_ASSERT(self->receiver->pushed_value);
    self->receiver->pushed_value = false;
  }

};


template<typename Yield, typename Push>
struct generator_base
{
  auto operator()(      Push && push)
  {
    return static_cast<generator<Yield, Push>*>(this)->receiver_.get_awaitable(std::move(push));
  }
  auto operator()(const Push &  push) requires std::is_copy_constructible_v<Push>
  {
    return static_cast<generator<Yield, Push>*>(this)->receiver_.get_awaitable(push);
  }
};

template<typename Yield>
struct generator_base<Yield, void>
{
  auto operator co_await ()
  {
    return static_cast<generator<Yield, void>*>(this)->receiver_.get_awaitable();
  }
};

template<typename T>
struct generator_with_awaitable
{
  generator_base<T, void> &g;
  std::optional<typename detail::generator_receiver<T, void>::awaitable> awaitable;

  template<typename Promise>
  void await_suspend(std::coroutine_handle<Promise> h)
  {
    g.cancel();
    awaitable.emplace(g.operator co_await());
    return awaitable->await_suspend(h);
  }

  void await_resume() {}

};

}

}

#endif //BOOST_COBALT_DETAIL_GENERATOR_HPP
