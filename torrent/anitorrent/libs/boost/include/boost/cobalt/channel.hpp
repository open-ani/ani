//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_CHANNEL_HPP
#define BOOST_COBALT_CHANNEL_HPP

#include <boost/cobalt/this_thread.hpp>
#include <boost/cobalt/unique_handle.hpp>
#include <boost/cobalt/detail/util.hpp>

#include <boost/asio/cancellation_signal.hpp>
#include <boost/asio/cancellation_type.hpp>
#include <boost/circular_buffer.hpp>
#include <boost/config.hpp>
#include <boost/intrusive/list.hpp>
#include <boost/variant2/variant.hpp>

#include <optional>

namespace boost::cobalt
{

template<typename T>
struct channel_reader;

// tag::outline[]
template<typename T>
struct channel
{
  // end::outline[]
#if defined(BOOST_COBALT_NO_PMR)
  channel(std::size_t limit = 0u,
          executor executor = this_thread::get_executor());
#else
  // tag::outline[]
  // create a channel with a buffer limit, executor & resource.
  explicit
  channel(std::size_t limit = 0u,
          executor executor = this_thread::get_executor(),
          pmr::memory_resource * resource = this_thread::get_default_resource());
  // end::outline[]
#endif
  // tag::outline[]
  // not movable.
  channel(channel && rhs) noexcept = delete;
  channel & operator=(channel && lhs) noexcept = delete;

  using executor_type = executor;
  const executor_type & get_executor();

  // Closes the channel
  ~channel();
  bool is_open() const;
  // close the operation, will cancel all pending ops, too
  void close();

  // end::outline[]
 private:
#if !defined(BOOST_COBALT_NO_PMR)
  boost::circular_buffer<T, pmr::polymorphic_allocator<T>> buffer_;
#else
  boost::circular_buffer<T> buffer_;
#endif
  executor_type executor_;
  bool is_closed_{false};

  struct read_op : intrusive::list_base_hook<intrusive::link_mode<intrusive::auto_unlink> >
  {
    channel * chn;
    boost::source_location loc;
    bool cancelled = false;
    std::optional<T> direct{};
    asio::cancellation_slot cancel_slot{};
    unique_handle<void> awaited_from{nullptr};
    void (*begin_transaction)(void*) = nullptr;

    void transactional_unlink()
    {
      if (begin_transaction)
          begin_transaction(awaited_from.get());
      this->unlink();
    }

    struct cancel_impl;
    bool await_ready() { return !chn->buffer_.empty(); }
    template<typename Promise>
    BOOST_NOINLINE 
    std::coroutine_handle<void> await_suspend(std::coroutine_handle<Promise> h);
    T await_resume();
    std::tuple<system::error_code, T> await_resume(const struct as_tuple_tag & );
    system::result<T> await_resume(const struct as_result_tag &);
    explicit operator bool() const {return chn && chn->is_open();}
  };

  struct write_op : intrusive::list_base_hook<intrusive::link_mode<intrusive::auto_unlink> >
  {
    channel * chn;
    variant2::variant<T*, const T*> ref;
    boost::source_location loc;
    bool cancelled = false, direct = false;
    asio::cancellation_slot cancel_slot{};

    unique_handle<void> awaited_from{nullptr};
    void (*begin_transaction)(void*) = nullptr;

    void transactional_unlink()
    {
      if (begin_transaction)
          begin_transaction(awaited_from.get());
      this->unlink();
    }

    struct cancel_impl;

    bool await_ready() { return !chn->buffer_.full(); }
    template<typename Promise>
    BOOST_NOINLINE 
    std::coroutine_handle<void> await_suspend(std::coroutine_handle<Promise> h);
    void await_resume();
    std::tuple<system::error_code> await_resume(const struct as_tuple_tag & );
    system::result<void> await_resume(const struct as_result_tag &);
    explicit operator bool() const {return chn && chn->is_open();}
  };

  boost::intrusive::list<read_op,  intrusive::constant_time_size<false> > read_queue_;
  boost::intrusive::list<write_op, intrusive::constant_time_size<false> > write_queue_;
 public:
  read_op   read(const boost::source_location & loc = BOOST_CURRENT_LOCATION)  {return  read_op{{}, this, loc}; }
  write_op write(const T  && value, const boost::source_location & loc = BOOST_CURRENT_LOCATION)
  {
    return write_op{{}, this, &value, loc};
  }
  write_op write(const T  &  value, const boost::source_location & loc = BOOST_CURRENT_LOCATION)
  {
    return write_op{{}, this, &value, loc};
  }
  write_op write(      T &&  value, const boost::source_location & loc = BOOST_CURRENT_LOCATION)
  {
    return write_op{{}, this, &value, loc};
  }
  write_op write(      T  &  value, const boost::source_location & loc = BOOST_CURRENT_LOCATION)
  {
    return write_op{{}, this, &value, loc};
  }
  /*
  // tag::outline[]
  // an awaitable that yields T
  using __read_op__ = __unspecified__;

  // an awaitable that yields void
  using __write_op__ = __unspecified__;

  // read a value to a channel
  __read_op__  read();

  // write a value to the channel
  __write_op__ write(const T  && value);
  __write_op__ write(const T  &  value);
  __write_op__ write(      T &&  value);
  __write_op__ write(      T  &  value);

  // write a value to the channel if T is void
  __write_op__ write();  // end::outline[]
   */
  // tag::outline[]

};
// end::outline[]

template<>
struct channel<void>
{
  explicit
  channel(std::size_t limit = 0u,
          executor executor = this_thread::get_executor())
        : limit_(limit), executor_(executor) {}
  channel(channel &&) noexcept = delete;
  channel & operator=(channel && lhs) noexcept = delete;

  using executor_type = executor;
  const executor_type & get_executor() {return executor_;}

  BOOST_COBALT_DECL ~channel();

  bool is_open() const {return !is_closed_;}
  BOOST_COBALT_DECL void close();

 private:
  std::size_t limit_;
  std::size_t n_{0u};
  executor_type executor_;
  bool is_closed_{false};

  struct read_op : intrusive::list_base_hook<intrusive::link_mode<intrusive::auto_unlink> >
  {
    channel * chn;
    boost::source_location loc;
    bool cancelled = false, direct = false;
    asio::cancellation_slot cancel_slot{};
    unique_handle<void> awaited_from{nullptr};
    void (*begin_transaction)(void*) = nullptr;

    void transactional_unlink()
    {
      if (begin_transaction)
          begin_transaction(awaited_from.get());
      this->unlink();
    }

    struct cancel_impl;
    bool await_ready() { return (chn->n_ > 0); }
    template<typename Promise>
    BOOST_NOINLINE 
    std::coroutine_handle<void> await_suspend(std::coroutine_handle<Promise> h);
    BOOST_COBALT_DECL void await_resume();
    BOOST_COBALT_DECL std::tuple<system::error_code> await_resume(const struct as_tuple_tag & );
    BOOST_COBALT_DECL system::result<void> await_resume(const struct as_result_tag &);
    explicit operator bool() const {return chn && chn->is_open();}
  };

  struct write_op : intrusive::list_base_hook<intrusive::link_mode<intrusive::auto_unlink> >
  {
    channel * chn;
    boost::source_location loc;
    bool cancelled = false, direct = false;
    asio::cancellation_slot cancel_slot{};
    unique_handle<void> awaited_from{nullptr};
    void (*begin_transaction)(void*) = nullptr;

    void transactional_unlink()
    {
      if (begin_transaction)
          begin_transaction(awaited_from.get());
      this->unlink();
    }

    struct cancel_impl;
    bool await_ready()
    {
      return chn->n_ < chn->limit_;
    }

    template<typename Promise>
    BOOST_NOINLINE 
    std::coroutine_handle<void> await_suspend(std::coroutine_handle<Promise> h);

    BOOST_COBALT_DECL void await_resume();
    BOOST_COBALT_DECL std::tuple<system::error_code> await_resume(const struct as_tuple_tag & );
    BOOST_COBALT_DECL system::result<void> await_resume(const struct as_result_tag &);
    explicit operator bool() const {return chn && chn->is_open();}
  };

  boost::intrusive::list<read_op,  intrusive::constant_time_size<false> > read_queue_;
  boost::intrusive::list<write_op, intrusive::constant_time_size<false> > write_queue_;
 public:
  read_op   read(const boost::source_location & loc = BOOST_CURRENT_LOCATION)  {return  read_op{{}, this, loc}; }
  write_op write(const boost::source_location & loc = BOOST_CURRENT_LOCATION)  {return write_op{{}, this, loc}; }
};

template<typename T>
struct channel_reader
{
  channel_reader(channel<T> & chan,
                 const boost::source_location & loc = BOOST_CURRENT_LOCATION) : chan_(&chan), loc_(loc) {}

  auto operator co_await ()
  {
    return chan_->read(loc_);
  }

  explicit operator bool () const {return chan_ && chan_->is_open();}

 private:
  channel<T> * chan_;
  boost::source_location loc_;
};

}

#include <boost/cobalt/impl/channel.hpp>

#endif //BOOST_COBALT_CHANNEL_HPP
