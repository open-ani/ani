//
// Copyright (c) 2023 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_HANDLE_HPP
#define BOOST_COBALT_HANDLE_HPP

#include <boost/asio/associator.hpp>
#include <coroutine>
#include <memory>

namespace boost::cobalt
{

template<typename T>
struct unique_handle
{
  unique_handle() noexcept = default;
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  explicit unique_handle(T * promise,
                         const boost::source_location & loc = BOOST_CURRENT_LOCATION) noexcept : handle_(promise), loc_(loc) {}
#else
  explicit unique_handle(T * promise) noexcept : handle_(promise) {}
#endif
  unique_handle(std::nullptr_t) noexcept {}

  std::coroutine_handle<T> release()
  {
    return std::coroutine_handle<T>::from_promise(*handle_.release());
  }

  void* address() const noexcept { return get_handle_().address(); }

#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  static unique_handle from_address(
      void* a,
      const boost::source_location & loc = BOOST_CURRENT_LOCATION) noexcept
  {
    unique_handle res;
    res.loc_ = loc;
    res.handle_.reset(&std::coroutine_handle<T>::from_address(a).promise());
    return res;
  }
#else
  static unique_handle from_address(void* a) noexcept
  {
    unique_handle res;
    res.handle_.reset(&std::coroutine_handle<T>::from_address(a).promise());
    return res;
  }
#endif


  bool done() const noexcept { return get_handle_().done(); }
  explicit operator bool() const { return static_cast<bool>(handle_); }

  void destroy() { handle_.reset(); }

  void operator()() const &
  {
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    BOOST_ASIO_HANDLER_LOCATION((loc_.file_name(), loc_.line(), loc_.function_name()));
#endif
    resume();
  }
  void resume()     const & { get_handle_().resume(); }

  void operator()() &&
  {
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    BOOST_ASIO_HANDLER_LOCATION((loc_.file_name(), loc_.line(), loc_.function_name()));
#endif
    release().resume();
  }
  void resume()     && { release().resume(); }

  T & promise() {return *handle_;}

#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  constexpr static unique_handle from_promise(
      T &p,
      const boost::source_location & loc = BOOST_CURRENT_LOCATION) noexcept
  {
    unique_handle res;
    res.loc_ = loc;
    res.handle_.reset(&p);
    return res;
  }
#else
  constexpr static unique_handle from_promise(T &p) noexcept
  {
    unique_handle res;
    res.handle_.reset(&p);
    return res;
  }
#endif

        T & operator*()       {return *handle_;}
  const T & operator*() const {return *handle_;}


        T * operator->()       {return handle_.get();}
  const T * operator->() const {return handle_.get();}

        T * get()        {return handle_.get();}
  const T * get() const  {return handle_.get();}

#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  void reset(T * handle = nullptr,
             const boost::source_location & loc = BOOST_CURRENT_LOCATION)
  {
    loc_ = loc; handle_.reset(handle);
  }
#else
  void reset(T * handle = nullptr) {handle_.reset(handle);}
#endif

  friend
  auto operator==(const unique_handle & h, std::nullptr_t) {return h.handle_ == nullptr;}
  friend
  auto operator!=(const unique_handle & h, std::nullptr_t) {return h.handle_ != nullptr;}

 private:
  struct deleter_
  {
    void operator()(T * p)
    {
      std::coroutine_handle<T>::from_promise(*p).destroy();
    }
  };

  std::coroutine_handle<T> get_handle_() const
  {
    return std::coroutine_handle<T>::from_promise(*handle_);
  }

  std::unique_ptr<T, deleter_> handle_;
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  boost::source_location loc_;
#endif
};

template<>
struct unique_handle<void>
{
  unique_handle() noexcept = default;
  unique_handle(std::nullptr_t) noexcept {}
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  explicit unique_handle(void * handle,
                         const boost::source_location & loc = BOOST_CURRENT_LOCATION)  noexcept : handle_(handle), loc_(loc) {}
#else
  explicit unique_handle(void * handle)  noexcept : handle_(handle) {}
#endif
  std::coroutine_handle<void> release()
  {
    return std::coroutine_handle<void>::from_address(handle_.release());
  }
  void* address() const noexcept { return get_handle_().address(); }
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  static unique_handle<void> from_address(void* a,
                                          const boost::source_location & loc = BOOST_CURRENT_LOCATION) noexcept
  {

    unique_handle res;
    res.loc_ = loc;
    res.handle_.reset(std::coroutine_handle<void>::from_address(a).address());
    return res;
  }
#else
static unique_handle<void> from_address(void* a) noexcept
  {

    unique_handle res;
    res.handle_.reset(std::coroutine_handle<void>::from_address(a).address());
    return res;
  }
#endif

  explicit operator bool() const { return static_cast<bool>(handle_); }
  bool done() const noexcept { return get_handle_().done(); }

  void operator()() const &
  {
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    BOOST_ASIO_HANDLER_LOCATION((loc_.file_name(), loc_.line(), loc_.function_name()));
#endif
    resume();
  }
  void resume() const & { get_handle_().resume(); }

  void operator()() &&
  {
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
    BOOST_ASIO_HANDLER_LOCATION((loc_.file_name(), loc_.line(), loc_.function_name()));
#endif
    release().resume();
  }
  void resume()     && { release().resume(); }

  void destroy() { handle_.reset(); }

        void * get()       { return handle_.get(); }
  const void * get() const { return handle_.get(); }

#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  void reset(void * handle = nullptr,
             const boost::source_location & loc = BOOST_CURRENT_LOCATION)
  {
    loc_ = loc;
    handle_.reset(handle);
  }
#else
  void reset(void * handle = nullptr) {handle_.reset(handle);}
#endif

  friend
  auto operator==(const unique_handle & h, std::nullptr_t) {return h.handle_ == nullptr;}
  friend
  auto operator!=(const unique_handle & h, std::nullptr_t) {return h.handle_ != nullptr;}
 private:
  struct deleter_
  {
    void operator()(void * p)
    {
      std::coroutine_handle<void>::from_address(p).destroy();
    }
  };

  std::coroutine_handle<void> get_handle_() const
  {
    return std::coroutine_handle<void>::from_address(handle_.get());
  }

  std::unique_ptr<void, deleter_> handle_;
#if defined(BOOST_ASIO_ENABLE_HANDLER_TRACKING)
  boost::source_location loc_;
#endif
};

template<>
struct unique_handle<std::noop_coroutine_promise>
{
  unique_handle() noexcept = default;
  unique_handle(std::nullptr_t) noexcept {}

  std::coroutine_handle<void> release()
  {
    return std::noop_coroutine();
  }
  void* address() const noexcept { return std::noop_coroutine().address(); }
  bool done() const noexcept { return true;}
  void operator()() const {}
  void resume() const {}
  void destroy() {}
  explicit operator bool() const { return true; }

  struct executor_type
  {
    template<typename Fn>
    void execute(Fn &&) const {}
  };

  executor_type get_executor() const {return {}; }

  friend
  auto operator==(const unique_handle &, std::nullptr_t) {return false;}
  friend
  auto operator!=(const unique_handle &, std::nullptr_t) {return true;}
};

}

namespace boost::asio
{

template <template <typename, typename> class Associator,
    typename Promise, typename DefaultCandidate>
    requires (!std::is_void_v<Promise>)
struct associator<Associator,
    boost::cobalt::unique_handle<Promise>, DefaultCandidate>
  : Associator<Promise, DefaultCandidate>
{
  static typename Associator<Promise, DefaultCandidate>::type
  get(const boost::cobalt::unique_handle<Promise>& h) BOOST_ASIO_NOEXCEPT
  {
    return Associator<Promise, DefaultCandidate>::get(*h);
  }

  static BOOST_ASIO_AUTO_RETURN_TYPE_PREFIX2(
      typename Associator<Handler, DefaultCandidate>::type)
  get(const boost::cobalt::unique_handle<Promise>& h,
      const DefaultCandidate& c) BOOST_ASIO_NOEXCEPT
    BOOST_ASIO_AUTO_RETURN_TYPE_SUFFIX((
      Associator<Promise, DefaultCandidate>::get(*h, c)))
  {
    return Associator<Promise, DefaultCandidate>::get(*h, c);
  }
};

}


#endif //BOOST_COBALT_HANDLE_HPP
