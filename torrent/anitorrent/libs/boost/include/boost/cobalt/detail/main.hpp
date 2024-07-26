//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_DETAIL_COBALT_MAIN_HPP
#define BOOST_DETAIL_COBALT_MAIN_HPP

#include <boost/cobalt/main.hpp>
#include <boost/cobalt/this_coro.hpp>


#include <boost/config.hpp>


namespace boost::asio
{

template<typename Executor>
class basic_signal_set;

}

namespace boost::cobalt::detail
{

extern "C"
{
int main(int argc, char * argv[]);
}

struct signal_helper
{
    asio::cancellation_signal signal;
};

struct main_promise : signal_helper,
                      promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>,
                      promise_throw_if_cancelled_base,
                      enable_awaitables<main_promise>,
                      enable_await_allocator<main_promise>,
                      enable_await_executor<main_promise>
{
    main_promise(int, char **) : promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>(
            signal_helper::signal.slot(), asio::enable_total_cancellation())
    {
        [[maybe_unused]] volatile auto p = &detail::main;
    }

#if !defined(BOOST_COBALT_NO_PMR)
    inline static pmr::memory_resource * my_resource = pmr::get_default_resource();
    void * operator new(const std::size_t size)
    {
        return my_resource->allocate(size);
    }

    void operator delete(void * raw, const std::size_t size)
    {
        return my_resource->deallocate(raw, size);
    }
#endif
    std::suspend_always initial_suspend() {return {};}

    BOOST_COBALT_DECL
    auto final_suspend() noexcept -> std::suspend_never;

    void unhandled_exception() { throw ; }
    void return_value(int res = 0)
    {
        if (result)
            *result = res;
    }

    friend auto ::co_main (int argc, char * argv[]) -> boost::cobalt::main;
    BOOST_COBALT_DECL
    static int run_main( ::boost::cobalt::main mn);

    friend int main(int argc, char * argv[])
    {
#if !defined(BOOST_COBALT_NO_PMR)
      pmr::unsynchronized_pool_resource root_resource;
      struct reset_res
      {
        void operator()(pmr::memory_resource * res)
        {
          this_thread::set_default_resource(res);
        }
      };
      std::unique_ptr<pmr::memory_resource, reset_res> pr{
        boost::cobalt::this_thread::set_default_resource(&root_resource)};
      char buffer[8096];
      pmr::monotonic_buffer_resource main_res{buffer, 8096, &root_resource};
      my_resource = &main_res;
#endif
      return run_main(co_main(argc, argv));
    }

    using executor_type = executor;
    const executor_type & get_executor() const {return *exec_;}

#if !defined(BOOST_COBALT_NO_PMR)
    using allocator_type = pmr::polymorphic_allocator<void>;
    using resource_type  = pmr::unsynchronized_pool_resource;

    mutable resource_type resource{my_resource};
    allocator_type get_allocator() const { return allocator_type(&resource); }
#endif

    using promise_cancellation_base<asio::cancellation_slot, asio::enable_total_cancellation>::await_transform;
    using promise_throw_if_cancelled_base::await_transform;
    using enable_awaitables<main_promise>::await_transform;
    using enable_await_allocator<main_promise>::await_transform;
    using enable_await_executor<main_promise>::await_transform;

  private:
    int * result;
    std::optional<asio::executor_work_guard<executor_type>> exec;
    std::optional<executor_type> exec_;
    asio::basic_signal_set<executor_type> * signal_set;
    ::boost::cobalt::main get_return_object()
    {
        return ::boost::cobalt::main{this};
    }
};

}

namespace std
{

template<typename Char>
struct coroutine_traits<boost::cobalt::main, int, Char>
{
  using promise_type = boost::cobalt::detail::main_promise;
};

}

#endif //BOOST_DETAIL_COBALT_MAIN_HPP
