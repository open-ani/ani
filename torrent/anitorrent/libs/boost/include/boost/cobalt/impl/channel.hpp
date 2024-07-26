//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_IMPL_CHANNEL_HPP
#define BOOST_COBALT_IMPL_CHANNEL_HPP

#include <boost/cobalt/channel.hpp>
#include <boost/cobalt/result.hpp>

#include <boost/asio/post.hpp>

namespace boost::cobalt
{

#if !defined(BOOST_COBALT_NO_PMR)
template<typename T>
inline channel<T>::channel(
    std::size_t limit,
    executor executor,
    pmr::memory_resource * resource)
    : buffer_(limit, resource), executor_(executor) {}
#else
template<typename T>
inline channel<T>::channel(
    std::size_t limit,
    executor executor)
    : buffer_(limit), executor_(executor) {}
#endif

template<typename T>
auto channel<T>::get_executor() -> const executor_type &  {return executor_;}

template<typename T>
bool channel<T>::is_open() const {return !is_closed_;}


template<typename T>
channel<T>::~channel()
{
  while (!read_queue_.empty())
    read_queue_.front().awaited_from.reset();

  while (!write_queue_.empty())
    write_queue_.front().awaited_from.reset();

}

template<typename T>
void channel<T>::close()
{
  is_closed_ = true;
  while (!read_queue_.empty())
  {
    auto & op = read_queue_.front();
    op.unlink();
    op.cancelled = true;
    op.cancel_slot.clear();

    if (op.awaited_from)
      asio::post(executor_, std::move(op.awaited_from));
  }
  while (!write_queue_.empty())
  {
    auto & op = write_queue_.front();
    op.unlink();
    op.cancelled = true;
    op.cancel_slot.clear();
    if (op.awaited_from)
      asio::post(executor_, std::move(op.awaited_from));
  }
}


template<typename T>
struct channel<T>::read_op::cancel_impl
{
  read_op * op;
  cancel_impl(read_op * op) : op(op) {}
  void operator()(asio::cancellation_type)
  {
    op->cancelled = true;
    op->unlink();
    if (op->awaited_from)
      asio::post(
          op->chn->executor_,
          std::move(op->awaited_from));
    op->cancel_slot.clear();
  }
};

template<typename T>
template<typename Promise>
std::coroutine_handle<void> channel<T>::read_op::await_suspend(std::coroutine_handle<Promise> h)
{
  if constexpr (requires (Promise p) {p.get_cancellation_slot();})
    if ((cancel_slot = h.promise().get_cancellation_slot()).is_connected())
      cancel_slot.emplace<cancel_impl>(this);

  if (awaited_from)
    boost::throw_exception(std::runtime_error("already-awaited"), loc);
  awaited_from.reset(h.address());
  // currently nothing to read
  if constexpr (requires (Promise p) {p.begin_transaction();})
    begin_transaction = +[](void * p){std::coroutine_handle<Promise>::from_address(p).promise().begin_transaction();};

  if (chn->write_queue_.empty())
  {
    chn->read_queue_.push_back(*this);
    return std::noop_coroutine();
  }
  else
  {
    cancel_slot.clear();
    auto & op = chn->write_queue_.front();
    op.transactional_unlink();
    op.direct = true;
    if (op.ref.index() == 0)
      direct = std::move(*variant2::get<0>(op.ref));
    else
      direct = *variant2::get<1>(op.ref);
    BOOST_ASSERT(op.awaited_from);
    asio::post(chn->executor_, std::move(awaited_from));
    return op.awaited_from.release();
  }
}


template<typename T>
T channel<T>::read_op::await_resume()
{
  return await_resume(as_result_tag{}).value(loc);
}

template<typename T>
std::tuple<system::error_code, T> channel<T>::read_op::await_resume(const struct as_tuple_tag &)
{
  auto res = await_resume(as_result_tag{});

  if (res.has_error())
    return {res.error(), T{}};
  else
    return {system::error_code{}, std::move(*res)};

}

template<typename T>
system::result<T> channel<T>::read_op::await_resume(const struct as_result_tag &)
{
  if (cancel_slot.is_connected())
    cancel_slot.clear();

  if (cancelled)
   return {system::in_place_error, asio::error::operation_aborted};

  T value = direct ? std::move(*direct) : std::move(chn->buffer_.front());
  if (!direct)
    chn->buffer_.pop_front();

  if (!chn->write_queue_.empty())
  {
    auto &op = chn->write_queue_.front();
    BOOST_ASSERT(chn->read_queue_.empty());
    if (op.await_ready())
    {
      op.transactional_unlink();
      BOOST_ASSERT(op.awaited_from);
      asio::post(chn->executor_, std::move(op.awaited_from));
    }
  }
  return {system::in_place_value, value};
}

template<typename T>
struct channel<T>::write_op::cancel_impl
{
  write_op * op;
  cancel_impl(write_op * op) : op(op) {}
  void operator()(asio::cancellation_type)
  {
    op->cancelled = true;
    op->unlink();
    if (op->awaited_from)
      asio::post(
        op->chn->executor_, std::move(op->awaited_from));
    op->cancel_slot.clear();
  }
};

template<typename T>
template<typename Promise>
std::coroutine_handle<void> channel<T>::write_op::await_suspend(std::coroutine_handle<Promise> h)
{
  if constexpr (requires (Promise p) {p.get_cancellation_slot();})
    if ((cancel_slot = h.promise().get_cancellation_slot()).is_connected())
      cancel_slot.emplace<cancel_impl>(this);

  awaited_from.reset(h.address());
  if constexpr (requires (Promise p) {p.begin_transaction();})
    begin_transaction = +[](void * p){std::coroutine_handle<Promise>::from_address(p).promise().begin_transaction();};

  // currently nothing to read
  if (chn->read_queue_.empty())
  {
    chn->write_queue_.push_back(*this);
    return std::noop_coroutine();
  }
  else
  {
    cancel_slot.clear();
    auto & op = chn->read_queue_.front();
    op.transactional_unlink();
    if (ref.index() == 0)
      op.direct = std::move(*variant2::get<0>(ref));
    else
      op.direct = *variant2::get<1>(ref);

    BOOST_ASSERT(op.awaited_from);
    direct = true;
    asio::post(chn->executor_, std::move(awaited_from));

    return op.awaited_from.release();
  }
}

template<typename T>
std::tuple<system::error_code> channel<T>::write_op::await_resume(const struct as_tuple_tag &)
{
  return await_resume(as_result_tag{}).error();
}

template<typename T>
void channel<T>::write_op::await_resume()
{
  await_resume(as_result_tag{}).value(loc);
}

template<typename T>
system::result<void>  channel<T>::write_op::await_resume(const struct as_result_tag &)
{
  if (cancel_slot.is_connected())
    cancel_slot.clear();
  if (cancelled)
    boost::throw_exception(system::system_error(asio::error::operation_aborted), loc);


  if (!direct)
  {
    BOOST_ASSERT(!chn->buffer_.full());
    if (ref.index() == 0)
      chn->buffer_.push_back(std::move(*variant2::get<0>(ref)));
    else
      chn->buffer_.push_back(*variant2::get<1>(ref));
  }

  if (!chn->read_queue_.empty())
  {
    auto & op = chn->read_queue_.front();
    BOOST_ASSERT(chn->write_queue_.empty());
    if (op.await_ready())
    {
      op.transactional_unlink();
      BOOST_ASSERT(op.awaited_from);
      asio::post(chn->executor_, std::move(op.awaited_from));
    }
  }
  return system::in_place_value;
}

struct channel<void>::read_op::cancel_impl
{
  read_op * op;
  cancel_impl(read_op * op) : op(op) {}
  void operator()(asio::cancellation_type)
  {
    op->cancelled = true;
    op->unlink();
    asio::post(op->chn->executor_, std::move(op->awaited_from));
    op->cancel_slot.clear();
  }
};

struct channel<void>::write_op::cancel_impl
{
  write_op * op;
  cancel_impl(write_op * op) : op(op) {}
  void operator()(asio::cancellation_type)
  {
    op->cancelled = true;
    op->unlink();
    asio::post(op->chn->executor_, std::move(op->awaited_from));
    op->cancel_slot.clear();
  }
};

template<typename Promise>
std::coroutine_handle<void> channel<void>::read_op::await_suspend(std::coroutine_handle<Promise> h)
{
  if constexpr (requires (Promise p) {p.get_cancellation_slot();})
    if ((cancel_slot = h.promise().get_cancellation_slot()).is_connected())
      cancel_slot.emplace<cancel_impl>(this);

  awaited_from.reset(h.address());

  if constexpr (requires (Promise p) {p.begin_transaction();})
    begin_transaction = +[](void * p){std::coroutine_handle<Promise>::from_address(p).promise().begin_transaction();};

  // nothing to read currently, enqueue
  if (chn->write_queue_.empty())
  {
    chn->read_queue_.push_back(*this);
    return std::noop_coroutine();
  }
  else // we're good, we can read, so we'll do that, but we need to post, so we need to initialize a transactin.
  {
    cancel_slot.clear();
    auto & op = chn->write_queue_.front();
    op.unlink();
    op.direct = true;
    BOOST_ASSERT(op.awaited_from);
    direct = true;
    asio::post(chn->executor_, std::move(awaited_from));
    return op.awaited_from.release();
  }
}


template<typename Promise>
std::coroutine_handle<void> channel<void>::write_op::await_suspend(std::coroutine_handle<Promise> h)
{
  if constexpr (requires (Promise p) {p.get_cancellation_slot();})
    if ((cancel_slot = h.promise().get_cancellation_slot()).is_connected())
      cancel_slot.emplace<cancel_impl>(this);

  awaited_from.reset(h.address());
  // currently nothing to read
  if constexpr (requires (Promise p) {p.begin_transaction();})
    begin_transaction = +[](void * p){std::coroutine_handle<Promise>::from_address(p).promise().begin_transaction();};

  if (chn->read_queue_.empty())
  {
    chn->write_queue_.push_back(*this);
    return std::noop_coroutine();
  }
  else
  {
    cancel_slot.clear();
    auto & op = chn->read_queue_.front();
    op.unlink();
    op.direct = true;
    BOOST_ASSERT(op.awaited_from);
    direct = true;
    asio::post(chn->executor_, std::move(awaited_from));
    return op.awaited_from.release();
  }
}

}

#endif //BOOST_COBALT_IMPL_CHANNEL_HPP
