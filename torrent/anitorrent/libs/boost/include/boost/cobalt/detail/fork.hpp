// Copyright (c) 2023 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_DETAIL_FORK_HPP
#define BOOST_COBALT_DETAIL_FORK_HPP

#include <boost/cobalt/config.hpp>
#include <boost/cobalt/detail/await_result_helper.hpp>
#include <boost/cobalt/detail/util.hpp>
#include <boost/cobalt/this_thread.hpp>
#include <boost/cobalt/unique_handle.hpp>
#if defined(BOOST_COBALT_NO_PMR)
#include <boost/cobalt/detail/monotonic_resource.hpp>
#endif

#include <boost/asio/cancellation_signal.hpp>
#include <boost/intrusive_ptr.hpp>

#include <coroutine>

namespace boost::cobalt::detail
{

struct fork
{
  fork() = default;
  struct shared_state
  {
#if !defined(BOOST_COBALT_NO_PMR)
    pmr::monotonic_buffer_resource resource;
        template<typename ... Args>
    shared_state(Args && ... args)
      : resource(std::forward<Args>(args)...,
                 this_thread::get_default_resource())
    {
    }
#else
    detail::monotonic_resource resource;
    template<typename ... Args>
    shared_state(Args && ... args)
      : resource(std::forward<Args>(args)...)
    {
    }
#endif
    // the coro awaiting the fork statement, e.g. awaiting race
    unique_handle<void> coro;
    std::size_t use_count = 0u;

    friend void intrusive_ptr_add_ref(shared_state * st) {st->use_count++;}
    friend void intrusive_ptr_release(shared_state * st)
    {
      if (st->use_count-- == 1u)
        st->coro.reset();
    }

    bool outstanding_work() {return use_count != 0u;}

    const executor * exec = nullptr;
    bool wired_up() {return exec != nullptr;}

    using executor_type = executor;
    const executor_type & get_executor() const
    {
      BOOST_ASSERT(exec != nullptr);
      return *exec;
    }

#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    boost::source_location loc;
#endif
  };

  template<typename std::size_t BufferSize>
  struct static_shared_state : private std::array<char, BufferSize>, shared_state
  {
    static_shared_state() : shared_state{std::array<char, BufferSize>::data(),
                                         std::array<char, BufferSize>::size()}
    {}
  };


  struct wired_up_t {};
  constexpr static wired_up_t wired_up{};

  struct set_transaction_function
  {
    void * begin_transaction_this = nullptr;
    void (*begin_transaction_func)(void*) = nullptr;

    template<typename BeginTransaction>
    set_transaction_function(BeginTransaction & transaction)
        : begin_transaction_this(&transaction)
        , begin_transaction_func(
            +[](void * ptr)
            {
              (*static_cast<BeginTransaction*>(ptr))();
            })
    {

    }
  };
  struct promise_type
  {
    template<typename State, typename ... Rest>
    void * operator new(const std::size_t size, State & st, Rest &&...)
    {
      return st.resource.allocate(size);
    }

    template<typename ... Rest>
    void operator delete(void * raw, const std::size_t size, Rest && ...) noexcept;
    void operator delete(void *, const std::size_t) noexcept {}

    template<typename ... Rest>
    promise_type(shared_state & st, Rest & ...)
         : state(&st)
    {
    }

    intrusive_ptr<shared_state> state;
    asio::cancellation_slot cancel;

    using executor_type = executor;
    const executor_type & get_executor() const { return state->get_executor(); }

#if defined(BOOST_COBALT_NO_PMR)
    using allocator_type = detail::monotonic_allocator<void>;
    const allocator_type get_allocator() const { return &state->resource; }
#else
    using allocator_type = pmr::polymorphic_allocator<void>;
    const allocator_type get_allocator() const { return &state->resource; }
#endif

    using cancellation_slot_type = asio::cancellation_slot;
    cancellation_slot_type get_cancellation_slot() const { return cancel; }

    constexpr static std::suspend_never initial_suspend() noexcept {return {};}

    struct final_awaitable
    {
      promise_type * self;
      bool await_ready() noexcept
      {
        return self->state->use_count != 1u;
      }

      std::coroutine_handle<void> await_suspend(std::coroutine_handle<promise_type> h) noexcept
      {
        auto pp = h.promise().state.detach();

#if defined(BOOST_COBALT_NO_SELF_DELETE)
        h.promise().~promise_type();
#else
        // mem is in a monotonic_resource, this is fine on msvc- gcc doesn't like it though
        h.destroy();
#endif
        pp->use_count--;
        BOOST_ASSERT(pp->use_count == 0u);
        if (pp->coro)
          return pp->coro.release();
        else
          return std::noop_coroutine();
      }

      constexpr static void await_resume() noexcept {}
    };
    final_awaitable final_suspend()  noexcept
    {
      if (cancel.is_connected())
        cancel.clear();
      return final_awaitable{this};
    }
    void return_void()
    {
    }

    template<awaitable<promise_type> Aw>
    struct wrapped_awaitable
    {
      Aw & aw;
      constexpr static bool await_ready() noexcept
      {
        return false;
      }

      auto await_suspend(std::coroutine_handle<promise_type> h)
      {
        BOOST_ASSERT(h.promise().state->wired_up());
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
        if constexpr (requires {aw.await_suspend(h, boost::source_location ());})
            return aw.await_suspend(h, h.promise().state->loc);
#endif
        return aw.await_suspend(h);
      }

      auto await_resume()
      {
        return aw.await_resume();
      }
    };

    template<awaitable<promise_type> Aw>
    auto await_transform(Aw & aw)
    {
      return wrapped_awaitable<Aw>{aw};
    }

    struct wired_up_awaitable
    {
      promise_type * promise;
      bool await_ready() const noexcept
      {
        return promise->state->wired_up();
      }
      void await_suspend(std::coroutine_handle<promise_type>)
      {
      }
      constexpr static void await_resume() noexcept {}
    };

    auto await_transform(wired_up_t)
    {
      return wired_up_awaitable{this};
    }


    auto await_transform(set_transaction_function sf)
    {
      begin_transaction_this = sf.begin_transaction_this;
      begin_transaction_func = sf.begin_transaction_func;
      return std::suspend_never();
    }

    auto await_transform(asio::cancellation_slot slot)
    {
      this->cancel = slot;
      return std::suspend_never();
    }

    [[noreturn]] void unhandled_exception() noexcept {std::terminate();}

    void * begin_transaction_this = nullptr;
    void (*begin_transaction_func)(void*) = nullptr;

    void begin_transaction()
    {
      if (begin_transaction_this)
        begin_transaction_func(begin_transaction_this);
    }

    fork get_return_object()
    {
      return this;
    }
  };

  [[nodiscard]] bool done() const
  {
    return ! handle_ ||  handle_.done();
  }

  auto release() -> std::coroutine_handle<promise_type>
  {
    return handle_.release();
  }

  private:
  fork(promise_type * pt) : handle_(pt) {}

  unique_handle<promise_type> handle_;

};



}
#endif //BOOST_COBALT_DETAIL_FORK_HPP