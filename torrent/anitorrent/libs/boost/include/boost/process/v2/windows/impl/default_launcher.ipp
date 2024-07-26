//
// boost/process/v2/windows/impl/default_launcher.ipp
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
// Copyright (c) 2022 Klemens D. Morgenstern (klemens dot morgenstern at gmx dot net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
#ifndef BOOST_PROCESS_V2_WINDOWS_IMPL_DEFAULT_LAUNCHER_IPP
#define BOOST_PROCESS_V2_WINDOWS_IMPL_DEFAULT_LAUNCHER_IPP

#include <boost/process/v2/detail/config.hpp>
#include <boost/process/v2/windows/default_launcher.hpp>

BOOST_PROCESS_V2_BEGIN_NAMESPACE
namespace windows
{

  std::size_t default_launcher::escaped_argv_length(basic_string_view<wchar_t> ws)
  {
    if (ws.empty())
      return 2u; // just quotes

    constexpr static auto space = L' ';
    constexpr static auto quote = L'"';

    const auto has_space = ws.find(space) != basic_string_view<wchar_t>::npos;
    const auto quoted = (ws.front() == quote) && (ws.back() == quote);
    const auto needs_escape = has_space && !quoted ;

    if (!needs_escape)
      return ws.size();
    else
      return ws.size() + std::count(ws.begin(), ws.end(), quote) + 2u;
  }


  std::size_t default_launcher::escape_argv_string(wchar_t * itr, std::size_t max_size, 
                                        basic_string_view<wchar_t> ws)
  { 
    const auto sz = escaped_argv_length(ws);
    if (sz > max_size)
      return 0u;
    if (ws.empty())      
    {
      itr[0] = L'"';
      itr[1] = L'"';
      return 2u;
    }

    const auto has_space = ws.find(L' ') != basic_string_view<wchar_t>::npos;
    const auto quoted = (ws.front() == L'"') && (ws.back() ==  L'"');
    const auto needs_escape = has_space && !quoted;

    if (!needs_escape)
      return std::copy(ws.begin(), ws.end(), itr) - itr;

    if (sz < (2u + ws.size()))
      return 0u;
      
    const auto end = itr + sz; 
    const auto begin = itr;
    *(itr ++) = L'"';
    for (auto wc : ws)
    {
      if (wc == L'"')
        *(itr++) = L'\\';
      *(itr++) = wc;
    }

    *(itr ++) = L'"';
    return itr - begin;
  }

}
BOOST_PROCESS_V2_END_NAMESPACE


#endif //BOOST_PROCESS_V2_WINDOWS_IMPL_DEFAULT_LAUNCHER_IPP