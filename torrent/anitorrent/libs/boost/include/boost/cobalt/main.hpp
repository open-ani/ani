// Copyright (c) 2022 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_COBALT_MAIN_HPP
#define BOOST_COBALT_MAIN_HPP

#include <boost/cobalt/concepts.hpp>
#include <boost/cobalt/this_coro.hpp>

#include <boost/asio/io_context.hpp>



#include <coroutine>
#include <optional>

namespace boost::cobalt
{

namespace detail { struct main_promise; }
class main;

}

auto co_main(int argc, char * argv[]) -> boost::cobalt::main;

namespace boost::cobalt
{

class main
{
  detail::main_promise * promise;
  main(detail::main_promise * promise) : promise(promise) {}
  friend auto ::co_main(int argc, char * argv[]) -> boost::cobalt::main;
  friend struct detail::main_promise;
};

}



#include <boost/cobalt/detail/main.hpp>

#endif //BOOST_COBALT_MAIN_HPP
