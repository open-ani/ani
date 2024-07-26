//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_THIS_THREAD_HPP
#define BOOST_COBALT_DETAIL_THIS_THREAD_HPP

#include <boost/cobalt/this_thread.hpp>

#include <boost/asio/uses_executor.hpp>
#include <boost/mp11/algorithm.hpp>

namespace boost::cobalt::detail
{

inline executor
extract_executor(executor exec) { return exec; }

#if defined(BOOST_COBALT_CUSTOM_EXECUTOR) || defined(BOOST_COBALT_USE_IO_CONTEXT)
BOOST_COBALT_DECL executor
extract_executor(asio::any_io_executor exec);
#endif

template<typename ... Args>
executor get_executor_from_args(Args &&... args)
{
  using args_type = mp11::mp_list<std::decay_t<Args>...>;
  constexpr static auto I = mp11::mp_find<args_type, asio::executor_arg_t>::value;
  if constexpr (sizeof...(Args) == I)
    return this_thread::get_executor();
  else  //
    return extract_executor(std::get<I + 1u>(std::tie(args...)));
}

#if !defined(BOOST_COBALT_NO_PMR)
template<typename ... Args>
pmr::memory_resource * get_memory_resource_from_args(Args &&... args)
{
  using args_type = mp11::mp_list<std::decay_t<Args>...>;
  constexpr static auto I = mp11::mp_find<args_type, std::allocator_arg_t>::value;
  if constexpr (sizeof...(Args) == I)
    return this_thread::get_default_resource();
  else  //
    return std::get<I + 1u>(std::tie(args...)).resource();
}

template<typename ... Args>
pmr::memory_resource * get_memory_resource_from_args_global(Args &&... args)
{
  using args_type = mp11::mp_list<std::decay_t<Args>...>;
  constexpr static auto I = mp11::mp_find<args_type, std::allocator_arg_t>::value;
  if constexpr (sizeof...(Args) == I)
    return pmr::get_default_resource();
  else  //
    return std::get<I + 1u>(std::tie(args...)).resource();
}
#endif

}

#endif //BOOST_COBALT_DETAIL_THIS_THREAD_HPP
