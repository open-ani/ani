//
// process/this_process/detail/environment_win.hpp
// ~~~~~~~~~~~~~~~~~~~~~~~~
//
// Copyright (c) 2021 Klemens D. Morgenstern (klemens dot morgenstern at gmx dot net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_PROCESS_V2_DETAIL_IMPL_ENVIRONMENT_WIN_IPP
#define BOOST_PROCESS_V2_DETAIL_IMPL_ENVIRONMENT_WIN_IPP


#include <boost/process/v2/detail/config.hpp>
#include <boost/process/v2/detail/environment_win.hpp>
#include <boost/process/v2/detail/impl/environment.ipp>
#include <boost/process/v2/detail/last_error.hpp>

#include <algorithm>
#include <cwctype>
#include <cstring>

#include <shellapi.h>

#include <boost/process/v2/cstring_ref.hpp>
#include <boost/process/v2/error.hpp>

BOOST_PROCESS_V2_BEGIN_NAMESPACE

namespace environment
{
namespace detail
{

std::basic_string<char_type, value_char_traits<char_type>> get(
        basic_cstring_ref<char_type, key_char_traits<char_type>> key,
        error_code & ec)
{
  std::basic_string<char_type, value_char_traits<char_type>> buf;

  std::size_t size = 0u;
  do
  {
    buf.resize(buf.size() + 4096);
    size = ::GetEnvironmentVariableW(key.c_str(), &buf.front(), static_cast<DWORD>(buf.size()));
  }
  while (size == buf.size());

  buf.resize(size);

  if (buf.size() == 0)
    BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)

  return buf;
}

void set(basic_cstring_ref<char_type,   key_char_traits<char_type>>   key,
         basic_cstring_ref<char_type, value_char_traits<char_type>> value,
         error_code & ec)
{
  if (!::SetEnvironmentVariableW(key.c_str(), value.c_str()))
    BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
}

void unset(basic_cstring_ref<char_type, key_char_traits<char_type>> key,
           error_code & ec)
{
  if (!::SetEnvironmentVariableW(key.c_str(), nullptr))
    BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
}


std::basic_string<char, value_char_traits<char>> get(
        basic_cstring_ref<char, key_char_traits<char>> key,
        error_code & ec)
{
  std::basic_string<char, value_char_traits<char>> buf;

  std::size_t size = 0u;
  do
  {
    buf.resize(buf.size() + 4096);
    size = ::GetEnvironmentVariableA(key.c_str(), &buf.front(), static_cast<DWORD>(buf.size()));
  }
  while (size == buf.size());

  buf.resize(size);

  if (buf.size() == 0)
    BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)

  return buf;
}

void set(basic_cstring_ref<char,   key_char_traits<char>>   key,
         basic_cstring_ref<char, value_char_traits<char>> value,
         error_code & ec)
{
  if (!::SetEnvironmentVariableA(key.c_str(), value.c_str()))
    BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
}

void unset(basic_cstring_ref<char, key_char_traits<char>> key,
           error_code & ec)
{
  if (!::SetEnvironmentVariableA(key.c_str(), nullptr))
    BOOST_PROCESS_V2_ASSIGN_LAST_ERROR(ec)
}


native_handle_type load_native_handle() { return ::GetEnvironmentStringsW(); }
void native_handle_deleter::operator()(native_handle_type nh) const
{
    ::FreeEnvironmentStringsW(nh);
}

native_iterator next(native_iterator nh)
{
    while (*nh != L'\0')
        nh++;
    return ++nh;
}


native_iterator find_end(native_handle_type nh)
{
  while ((*nh != L'\0') || (*std::next(nh) != L'\0'))
    nh++;
  return ++nh;
}

bool is_executable(const filesystem::path & pth, error_code & ec)
{
    return filesystem::is_regular_file(pth, ec) && SHGetFileInfoW(pth.native().c_str(), 0,0,0, SHGFI_EXETYPE);
}

}
}
BOOST_PROCESS_V2_END_NAMESPACE

#endif //BOOST_PROCESS_V2_DETAIL_IMPL_ENVIRONMENT_WIN_IPP
