// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_RUN_HPP
#define BOOST_COBALT_RUN_HPP

#include <boost/cobalt/spawn.hpp>
#include <boost/cobalt/task.hpp>

#include <boost/asio/use_future.hpp>

namespace boost::cobalt
{

template<typename T>
T run(task<T> t)
{
#if !defined(BOOST_COBALT_NO_PMR)
  pmr::unsynchronized_pool_resource root_resource{this_thread::get_default_resource()};
    struct reset_res
    {
        void operator()(pmr::memory_resource * res)
        {
            this_thread::set_default_resource(res);
        }
    };
    std::unique_ptr<pmr::memory_resource, reset_res> pr{
            boost::cobalt::this_thread::set_default_resource(&root_resource)};
#endif
    std::future<T> f;
    {
      asio::io_context ctx{BOOST_ASIO_CONCURRENCY_HINT_1};
      struct reset_exec
      {
        std::optional<executor> exec;

        reset_exec()
        {
          if (this_thread::has_executor())
            exec = this_thread::get_executor();
        }

        ~reset_exec()
        {
          if (exec)
            this_thread::set_executor(*exec);
        }
      };

      reset_exec re;
      this_thread::set_executor(ctx.get_executor());
      f = spawn(ctx, std::move(t), asio::bind_executor(ctx.get_executor(), asio::use_future));
      ctx.run();
    }
    return f.get();
}

}

#endif //BOOST_COBALT_RUN_HPP
