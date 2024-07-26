/* Copyright 2006-2023 Joaquin M Lopez Munoz.
 * Distributed under the Boost Software License, Version 1.0.
 * (See accompanying file LICENSE_1_0.txt or copy at
 * http://www.boost.org/LICENSE_1_0.txt)
 *
 * See http://www.boost.org/libs/flyweight for library home page.
 */

#ifndef BOOST_FLYWEIGHT_DETAIL_RECURSIVE_LW_MUTEX_HPP
#define BOOST_FLYWEIGHT_DETAIL_RECURSIVE_LW_MUTEX_HPP

#if defined(_MSC_VER)
#pragma once
#endif

/* Recursive lightweight mutex.
 *   - If <mutex> is provided, it uses std::recursive_mutex.
 *   - Else if Pthreads is available, it uses a Pthreads mutex with
 *     PTHREAD_MUTEX_RECURSIVE attribute.
 *   - Else if on Windows/Cygwin, it relies on boost::detail::lightweight_mutex
 *     for an implementation based on (recursive) Win32 CRITICAL_SECTION.
 *   - Else, it provides a dummy implementation.
 */

#include <boost/config.hpp>

#if !defined(BOOST_NO_CXX11_HDR_MUTEX)
#include <boost/noncopyable.hpp>
#include <mutex>

namespace boost{

namespace flyweights{

namespace detail{

struct recursive_lightweight_mutex:noncopyable
{
  recursive_lightweight_mutex(){}

  struct scoped_lock;
  friend struct scoped_lock;
  struct scoped_lock:noncopyable
  {
  public:
    scoped_lock(recursive_lightweight_mutex& m):m_(m.m_){m_.lock();}
    ~scoped_lock(){m_.unlock();}

  private:
    std::recursive_mutex& m_;
  };

private:
  std::recursive_mutex m_;
};

} /* namespace flyweights::detail */

} /* namespace flyweights */

} /* namespace boost */
#elif defined(BOOST_HAS_PTHREADS)
/* code shamelessly ripped from <boost/detail/lwm_pthreads.hpp> */

#include <boost/assert.hpp>
#include <boost/noncopyable.hpp>
#include <pthread.h>

namespace boost{

namespace flyweights{

namespace detail{

struct recursive_lightweight_mutex:noncopyable
{
  recursive_lightweight_mutex()
  {
    pthread_mutexattr_t attr;
    BOOST_VERIFY(pthread_mutexattr_init(&attr)==0);
    BOOST_VERIFY(pthread_mutexattr_settype(&attr,PTHREAD_MUTEX_RECURSIVE)==0);
    BOOST_VERIFY(pthread_mutex_init(&m_,&attr)==0);
    BOOST_VERIFY(pthread_mutexattr_destroy(&attr)==0);
  }

  ~recursive_lightweight_mutex(){pthread_mutex_destroy(&m_);}

  struct scoped_lock;
  friend struct scoped_lock;
  struct scoped_lock:noncopyable
  {
  public:
    scoped_lock(recursive_lightweight_mutex& m):m_(m.m_)
    {
      BOOST_VERIFY(pthread_mutex_lock(&m_)==0);
    }

    ~scoped_lock(){BOOST_VERIFY(pthread_mutex_unlock(&m_)==0);}

  private:
    pthread_mutex_t& m_;
  };

private:
  pthread_mutex_t m_;
};

} /* namespace flyweights::detail */

} /* namespace flyweights */

} /* namespace boost */
#elif defined(_WIN32) || defined(__WIN32__) || defined(__CYGWIN__)
#include <boost/smart_ptr/detail/lightweight_mutex.hpp>

namespace boost{

namespace flyweights{

namespace detail{

typedef boost::detail::lightweight_mutex recursive_lightweight_mutex;

} /* namespace flyweights::detail */

} /* namespace flyweights */

} /* namespace boost */
#else
namespace boost{

namespace flyweights{

namespace detail{

struct recursive_lightweight_mutex
{
  typedef recursive_lightweight_mutex scoped_lock;
};

} /* namespace flyweights::detail */

} /* namespace flyweights */

} /* namespace boost */
#endif

#endif
