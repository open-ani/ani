//
// detail/handler_alloc_helpers.hpp
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
// Copyright (c) 2003-2024 Christopher M. Kohlhoff (chris at kohlhoff dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_ASIO_DETAIL_HANDLER_ALLOC_HELPERS_HPP
#define BOOST_ASIO_DETAIL_HANDLER_ALLOC_HELPERS_HPP

#if defined(_MSC_VER) && (_MSC_VER >= 1200)
# pragma once
#endif // defined(_MSC_VER) && (_MSC_VER >= 1200)

#include <boost/asio/detail/config.hpp>
#include <boost/asio/detail/memory.hpp>
#include <boost/asio/detail/noncopyable.hpp>
#include <boost/asio/detail/recycling_allocator.hpp>
#include <boost/asio/detail/thread_info_base.hpp>
#include <boost/asio/associated_allocator.hpp>

#include <boost/asio/detail/push_options.hpp>

namespace boost {
namespace asio {
namespace detail {

inline void* default_allocate(std::size_t s,
    std::size_t align = BOOST_ASIO_DEFAULT_ALIGN)
{
#if !defined(BOOST_ASIO_DISABLE_SMALL_BLOCK_RECYCLING)
  return boost::asio::detail::thread_info_base::allocate(
      boost::asio::detail::thread_context::top_of_thread_call_stack(),
      s, align);
#else // !defined(BOOST_ASIO_DISABLE_SMALL_BLOCK_RECYCLING)
  return boost::asio::aligned_new(align, s);
#endif // !defined(BOOST_ASIO_DISABLE_SMALL_BLOCK_RECYCLING)
}

inline void default_deallocate(void* p, std::size_t s)
{
#if !defined(BOOST_ASIO_DISABLE_SMALL_BLOCK_RECYCLING)
  boost::asio::detail::thread_info_base::deallocate(
      boost::asio::detail::thread_context::top_of_thread_call_stack(), p, s);
#else // !defined(BOOST_ASIO_DISABLE_SMALL_BLOCK_RECYCLING)
  (void)s;
  boost::asio::aligned_delete(p);
#endif // !defined(BOOST_ASIO_DISABLE_SMALL_BLOCK_RECYCLING)
}

template <typename T>
class default_allocator
{
public:
  typedef T value_type;

  template <typename U>
  struct rebind
  {
    typedef default_allocator<U> other;
  };

  default_allocator() noexcept
  {
  }

  template <typename U>
  default_allocator(const default_allocator<U>&) noexcept
  {
  }

  T* allocate(std::size_t n)
  {
    return static_cast<T*>(default_allocate(sizeof(T) * n, alignof(T)));
  }

  void deallocate(T* p, std::size_t n)
  {
    default_deallocate(p, sizeof(T) * n);
  }
};

template <>
class default_allocator<void>
{
public:
  typedef void value_type;

  template <typename U>
  struct rebind
  {
    typedef default_allocator<U> other;
  };

  default_allocator() noexcept
  {
  }

  template <typename U>
  default_allocator(const default_allocator<U>&) noexcept
  {
  }
};

template <typename Allocator>
struct get_default_allocator
{
  typedef Allocator type;

  static type get(const Allocator& a)
  {
    return a;
  }
};

template <typename T>
struct get_default_allocator<std::allocator<T>>
{
  typedef default_allocator<T> type;

  static type get(const std::allocator<T>&)
  {
    return type();
  }
};

} // namespace detail
} // namespace asio
} // namespace boost

#define BOOST_ASIO_DEFINE_HANDLER_PTR(op) \
  struct ptr \
  { \
    Handler* h; \
    op* v; \
    op* p; \
    ~ptr() \
    { \
      reset(); \
    } \
    static op* allocate(Handler& handler) \
    { \
      typedef typename ::boost::asio::associated_allocator< \
        Handler>::type associated_allocator_type; \
      typedef typename ::boost::asio::detail::get_default_allocator< \
        associated_allocator_type>::type default_allocator_type; \
      BOOST_ASIO_REBIND_ALLOC(default_allocator_type, op) a( \
            ::boost::asio::detail::get_default_allocator< \
              associated_allocator_type>::get( \
                ::boost::asio::get_associated_allocator(handler))); \
      return a.allocate(1); \
    } \
    void reset() \
    { \
      if (p) \
      { \
        p->~op(); \
        p = 0; \
      } \
      if (v) \
      { \
        typedef typename ::boost::asio::associated_allocator< \
          Handler>::type associated_allocator_type; \
        typedef typename ::boost::asio::detail::get_default_allocator< \
          associated_allocator_type>::type default_allocator_type; \
        BOOST_ASIO_REBIND_ALLOC(default_allocator_type, op) a( \
              ::boost::asio::detail::get_default_allocator< \
                associated_allocator_type>::get( \
                  ::boost::asio::get_associated_allocator(*h))); \
        a.deallocate(static_cast<op*>(v), 1); \
        v = 0; \
      } \
    } \
  } \
  /**/

#define BOOST_ASIO_DEFINE_TAGGED_HANDLER_ALLOCATOR_PTR(purpose, op) \
  struct ptr \
  { \
    const Alloc* a; \
    void* v; \
    op* p; \
    ~ptr() \
    { \
      reset(); \
    } \
    static op* allocate(const Alloc& a) \
    { \
      typedef typename ::boost::asio::detail::get_recycling_allocator< \
        Alloc, purpose>::type recycling_allocator_type; \
      BOOST_ASIO_REBIND_ALLOC(recycling_allocator_type, op) a1( \
            ::boost::asio::detail::get_recycling_allocator< \
              Alloc, purpose>::get(a)); \
      return a1.allocate(1); \
    } \
    void reset() \
    { \
      if (p) \
      { \
        p->~op(); \
        p = 0; \
      } \
      if (v) \
      { \
        typedef typename ::boost::asio::detail::get_recycling_allocator< \
          Alloc, purpose>::type recycling_allocator_type; \
        BOOST_ASIO_REBIND_ALLOC(recycling_allocator_type, op) a1( \
              ::boost::asio::detail::get_recycling_allocator< \
                Alloc, purpose>::get(*a)); \
        a1.deallocate(static_cast<op*>(v), 1); \
        v = 0; \
      } \
    } \
  } \
  /**/

#define BOOST_ASIO_DEFINE_HANDLER_ALLOCATOR_PTR(op) \
  BOOST_ASIO_DEFINE_TAGGED_HANDLER_ALLOCATOR_PTR( \
      ::boost::asio::detail::thread_info_base::default_tag, op ) \
  /**/

#include <boost/asio/detail/pop_options.hpp>

#endif // BOOST_ASIO_DETAIL_HANDLER_ALLOC_HELPERS_HPP
