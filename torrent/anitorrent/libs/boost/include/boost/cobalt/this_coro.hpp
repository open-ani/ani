// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_THIS_CORO_HPP
#define BOOST_COBALT_THIS_CORO_HPP

#include <boost/cobalt/this_thread.hpp>
#include <boost/cobalt/detail/this_thread.hpp>

#include <boost/asio/associated_allocator.hpp>
#include <boost/asio/associated_cancellation_slot.hpp>
#include <boost/asio/associated_executor.hpp>
#include <boost/asio/this_coro.hpp>
#include <boost/asio/cancellation_state.hpp>


#include <coroutine>
#include <optional>
#include <tuple>

namespace boost::cobalt
{

namespace this_coro
{

/* tag::outline[]

// Awaitable type that returns the executor of the current coroutine.
struct executor_t {}
constexpr executor_t executor;

// Awaitable type that returns the cancellation state of the current coroutine.
struct cancellation_state_t {};
constexpr cancellation_state_t cancellation_state;

// Reset the cancellation state with custom or default filters.
constexpr __unspecified__ reset_cancellation_state();
template<typename Filter>
constexpr __unspecified__ reset_cancellation_state(
    Filter && filter);
template<typename InFilter, typename OutFilter>
constexpr __unspecified__ reset_cancellation_state(
    InFilter && in_filter,
    OutFilter && out_filter);

// get & set the throw_if_cancelled setting.
__unspecified__ throw_if_cancelled();
__unspecified__ throw_if_cancelled(bool value);

// Set the cancellation source in a detached.
__unspecified__ reset_cancellation_source();
__unspecified__ reset_cancellation_source(asio::cancellation_slot slot);

end::outline[]
 */

using namespace asio::this_coro;
//tag::outline[]

// get the allocator the promise
struct allocator_t {};
constexpr allocator_t allocator;

// get the current cancellation state-type
struct cancelled_t {};
constexpr cancelled_t cancelled;

// set the over-eager mode of a generator
struct initial_t {};
constexpr initial_t initial;
//end::outline[]

template<typename CancellationSlot = asio::cancellation_slot>
struct reset_cancellation_source_t
{
  CancellationSlot source;
};

template<typename CancellationSlot= asio::cancellation_slot>
reset_cancellation_source_t<CancellationSlot> reset_cancellation_source(CancellationSlot slot = {})
{
  return reset_cancellation_source_t<CancellationSlot>{std::move(slot)};
}

}

template<typename CancellationSlot = asio::cancellation_slot,
         typename DefaultFilter = asio::enable_terminal_cancellation>
struct promise_cancellation_base
{
  using cancellation_slot_type = asio::cancellation_slot;
  cancellation_slot_type get_cancellation_slot() const {return state_.slot();}

  template<typename InitialFilter = asio::enable_terminal_cancellation>
  promise_cancellation_base(CancellationSlot slot = {}, InitialFilter filter = {})
      : source_(slot), state_{source_, filter} {}


  // This await transformation resets the associated cancellation state.
  auto await_transform(cobalt::this_coro::cancelled_t) noexcept
  {
    return cancelled_t_awaitable{state_.cancelled()};
  }

  // This await transformation resets the associated cancellation state.
  auto await_transform(asio::this_coro::cancellation_state_t) noexcept
  {
    return cancellation_state_t_awaitable{state_};
  }

  // This await transformation resets the associated cancellation state.
  auto await_transform(asio::this_coro::reset_cancellation_state_0_t) noexcept
  {
    return reset_cancellation_state_0_t_awaitable{state_, source_};
  }

  // This await transformation resets the associated cancellation state.
  template <typename Filter>
  auto await_transform(
          asio::this_coro::reset_cancellation_state_1_t<Filter> reset) noexcept
  {
    return reset_cancellation_state_1_t_awaitable<Filter>{state_, BOOST_ASIO_MOVE_CAST(Filter)(reset.filter), source_};
  }

  // This await transformation resets the associated cancellation state.
  template <typename InFilter, typename OutFilter>
  auto await_transform(
          asio::this_coro::reset_cancellation_state_2_t<InFilter, OutFilter> reset)
  noexcept
  {
      return reset_cancellation_state_2_t_awaitable<InFilter, OutFilter>{state_,
                    BOOST_ASIO_MOVE_CAST(InFilter)(reset.in_filter),
                    BOOST_ASIO_MOVE_CAST(OutFilter)(reset.out_filter),
                    source_};
  }
  const asio::cancellation_state & cancellation_state() const {return state_;}
  asio::cancellation_state & cancellation_state() {return state_;}
  asio::cancellation_type cancelled() const
  {
    return state_.cancelled();
  }

  cancellation_slot_type get_cancellation_slot() {return state_.slot();}

  void reset_cancellation_source(CancellationSlot source = CancellationSlot())
  {
    source_ = source;
    state_ = asio::cancellation_state{source, DefaultFilter()};
    state_.clear();
  }

        CancellationSlot & source() {return source_;}
  const CancellationSlot & source() const {return source_;}
 private:
  CancellationSlot source_;
  asio::cancellation_state state_{source_, DefaultFilter() };

  struct cancelled_t_awaitable
  {
    asio::cancellation_type state;

    bool await_ready() const noexcept
    {
      return true;
    }

    void await_suspend(std::coroutine_handle<void>) noexcept
    {
    }

    auto await_resume() const
    {
      return state;
    }
  };

  struct cancellation_state_t_awaitable
  {
    asio::cancellation_state &state;

    bool await_ready() const noexcept
    {
      return true;
    }

    void await_suspend(std::coroutine_handle<void>) noexcept
    {
    }

    auto await_resume() const
    {
      return state;
    }
  };

  struct reset_cancellation_state_0_t_awaitable
  {
    asio::cancellation_state &state;
    CancellationSlot &source;

    bool await_ready() const noexcept
    {
      return true;
    }

    void await_suspend(std::coroutine_handle<void>) noexcept
    {
    }

    auto await_resume() const
    {
      state = asio::cancellation_state(source, DefaultFilter());
    }
  };

  template<typename Filter>
  struct reset_cancellation_state_1_t_awaitable
  {
    asio::cancellation_state & state;
    Filter filter_;
    CancellationSlot &source;

    bool await_ready() const noexcept
    {
      return true;
    }

    void await_suspend(std::coroutine_handle<void>) noexcept
    {
    }

    auto await_resume()
    {
      state = asio::cancellation_state(
          source,
          BOOST_ASIO_MOVE_CAST(Filter)(filter_));
    }
  };

  template<typename InFilter, typename OutFilter>
  struct reset_cancellation_state_2_t_awaitable
  {
    asio::cancellation_state & state;
    InFilter in_filter_;
    OutFilter out_filter_;
    CancellationSlot &source;


    bool await_ready() const noexcept
    {
      return true;
    }

    void await_suspend(std::coroutine_handle<void>) noexcept
    {
    }

    auto await_resume()
    {
      state = asio::cancellation_state(
          source,
          BOOST_ASIO_MOVE_CAST(InFilter)(in_filter_),
          BOOST_ASIO_MOVE_CAST(OutFilter)(out_filter_));
    }
  };
};

struct promise_throw_if_cancelled_base
{
  promise_throw_if_cancelled_base(bool throw_if_cancelled = true) : throw_if_cancelled_(throw_if_cancelled) {}

  // This await transformation determines whether cancellation is propagated as
  // an exception.
  auto await_transform(this_coro::throw_if_cancelled_0_t)
  noexcept
  {
    return throw_if_cancelled_0_awaitable_{throw_if_cancelled_};
  }

  // This await transformation sets whether cancellation is propagated as an
  // exception.
  auto await_transform(this_coro::throw_if_cancelled_1_t throw_if_cancelled)
  noexcept
  {
      return throw_if_cancelled_1_awaitable_{this, throw_if_cancelled.value};
  }
  bool throw_if_cancelled() const {return throw_if_cancelled_;}
 protected:
  bool throw_if_cancelled_{true};

  struct throw_if_cancelled_0_awaitable_
  {
    bool value_;

    bool await_ready() const noexcept
    {
      return true;
    }

    void await_suspend(std::coroutine_handle<void>) noexcept
    {
    }

    auto await_resume()
    {
      return value_;
    }
  };

  struct throw_if_cancelled_1_awaitable_
  {
    promise_throw_if_cancelled_base* this_;
    bool value_;

    bool await_ready() const noexcept
    {
      return true;
    }

    void await_suspend(std::coroutine_handle<void>) noexcept
    {
    }

    auto await_resume()
    {
      this_->throw_if_cancelled_ = value_;
    }
  };
};

struct promise_memory_resource_base
{
#if !defined(BOOST_COBALT_NO_PMR)
  using allocator_type = pmr::polymorphic_allocator<void>;
  allocator_type get_allocator() const {return allocator_type{resource};}

  template<typename ... Args>
  static void * operator new(const std::size_t size, Args & ... args)
  {
    auto res = detail::get_memory_resource_from_args(args...);
    const auto p = res->allocate(size + sizeof(pmr::memory_resource *), alignof(pmr::memory_resource *));
    auto pp = static_cast<pmr::memory_resource**>(p);
    *pp = res;
    return pp + 1;
  }

  static void operator delete(void * raw, const std::size_t size) noexcept
  {
    const auto p = static_cast<pmr::memory_resource**>(raw) - 1;
    pmr::memory_resource * res = *p;
    res->deallocate(p, size + sizeof(pmr::memory_resource *), alignof(pmr::memory_resource *));
  }
  promise_memory_resource_base(pmr::memory_resource * resource = this_thread::get_default_resource()) : resource(resource) {}

private:
  pmr::memory_resource * resource = this_thread::get_default_resource();
#endif
};

/// Allocate the memory and put the allocator behind the cobalt memory
template<typename AllocatorType>
void *allocate_coroutine(const std::size_t size, AllocatorType alloc_)
{
    using alloc_type = typename std::allocator_traits<AllocatorType>::template rebind_alloc<unsigned char>;
    alloc_type alloc{alloc_};

    const std::size_t align_needed = size % alignof(alloc_type);
    const std::size_t align_offset = align_needed != 0 ? alignof(alloc_type) - align_needed : 0ull;
    const std::size_t alloc_size = size + sizeof(alloc_type) + align_offset;
    const auto raw = std::allocator_traits<alloc_type>::allocate(alloc, alloc_size);
    new(raw + size + align_offset) alloc_type(std::move(alloc));

    return raw;
}

/// Deallocate the memory and destroy the allocator in the cobalt memory.
template<typename AllocatorType>
void deallocate_coroutine(void *raw_, const std::size_t size)
{
    using alloc_type = typename std::allocator_traits<AllocatorType>::template rebind_alloc<unsigned char>;
    const auto raw = static_cast<unsigned char *>(raw_);

    const std::size_t align_needed = size % alignof(alloc_type);
    const std::size_t align_offset = align_needed != 0 ? alignof(alloc_type) - align_needed : 0ull;
    const std::size_t alloc_size = size + sizeof(alloc_type) + align_offset;
    auto alloc_p = reinterpret_cast<alloc_type *>(raw + size + align_offset);

    auto alloc = std::move(*alloc_p);
    alloc_p->~alloc_type();
    using size_type = typename std::allocator_traits<alloc_type>::size_type;
    std::allocator_traits<alloc_type>::deallocate(alloc, raw, static_cast<size_type>(alloc_size));
}


template<typename Promise>
struct enable_await_allocator
{
  auto await_transform(this_coro::allocator_t)
  {
    return allocator_awaitable_{static_cast<Promise*>(this)->get_allocator()};

  }
 private:
  struct allocator_awaitable_
  {
    using allocator_type = typename Promise::allocator_type;

    allocator_type alloc;
    constexpr static bool await_ready() { return true; }

    bool await_suspend( std::coroutine_handle<void> ) { return false; }
    allocator_type await_resume()
    {
      return alloc;
    }
  };
};

template<typename Promise>
struct enable_await_executor
{
  auto await_transform(this_coro::executor_t)
  {
    return executor_awaitable_{static_cast<Promise*>(this)->get_executor()};
  }
 private:
  struct executor_awaitable_
  {
    using executor_type = typename Promise::executor_type;

    executor_type exec;
    constexpr static bool await_ready() { return true; }

    bool await_suspend( std::coroutine_handle<void> ) { return false; }
    executor_type await_resume()
    {
      return exec;
    }
  };
};

}

#endif //BOOST_COBALT_THIS_CORO_HPP
