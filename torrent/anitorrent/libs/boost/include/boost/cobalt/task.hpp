//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_TASK_HPP
#define BOOST_COBALT_TASK_HPP

#include <boost/cobalt/detail/handler.hpp>
#include <boost/cobalt/detail/task.hpp>
#include <boost/cobalt/op.hpp>

#include <boost/asio/append.hpp>
#include <boost/asio/deferred.hpp>

namespace boost::cobalt
{

// tag::outline[]
template<typename Return>
struct [[nodiscard]] task
{
    task(task &&lhs) noexcept = default;
    task& operator=(task &&) noexcept = default;

    // enable `co_await`
    auto operator co_await ();

    // end::outline[]
    task(const task &) = delete;
    task& operator=(const task &) = delete;

    using promise_type = detail::task_promise<Return>;

 private:
    template<typename>
    friend struct detail::task_promise;

    task(detail::task_promise<Return> * task) : receiver_(task)
    {
    }

    detail::task_receiver<Return> receiver_;
    friend struct detail::async_initiate_spawn;
    // tag::outline[]
};
// end::outline[]


struct use_task_t
{
  /// Default constructor.
  constexpr use_task_t()
  {
  }

  /// Adapts an executor to add the @c use_task_t completion token as the
  /// default.
  template <typename InnerExecutor>
  struct executor_with_default : InnerExecutor
  {
    /// Specify @c use_task_t as the default completion token type.
    typedef use_task_t default_completion_token_type;

    executor_with_default(const InnerExecutor& ex) noexcept
        : InnerExecutor(ex)
    {
    }

    /// Construct the adapted executor from the inner executor type.
    template <typename InnerExecutor1>
    executor_with_default(const InnerExecutor1& ex,
                          typename std::enable_if<
                              std::conditional<
                                  !std::is_same<InnerExecutor1, executor_with_default>::value,
                                  std::is_convertible<InnerExecutor1, InnerExecutor>,
                                  std::false_type
                              >::type::value>::type = 0) noexcept
        : InnerExecutor(ex)
    {
    }
  };

  /// Type alias to adapt an I/O object to use @c use_task_t as its
  /// default completion token type.
  template <typename T>
  using as_default_on_t = typename T::template rebind_executor<
      executor_with_default<typename T::executor_type> >::other;

  /// Function helper to adapt an I/O object to use @c use_task_t as its
  /// default completion token type.
  template <typename T>
  static typename std::decay_t<T>::template rebind_executor<
      executor_with_default<typename std::decay_t<T>::executor_type>
  >::other
  as_default_on(T && object)
  {
    return typename std::decay_t<T>::template rebind_executor<
        executor_with_default<typename std::decay_t<T>::executor_type>
    >::other(std::forward<T>(object));
  }

};

constexpr use_task_t use_task{};

template<typename T>
inline auto task<T>::operator co_await () {return receiver_.get_awaitable();}

}


namespace boost::asio
{

template<typename ... Args>
struct async_result<boost::cobalt::use_task_t, void(Args...)>
{
  using return_type = cobalt::task<
      typename decltype(cobalt::interpret_as_result(std::declval<std::tuple<Args...>>()))::value_type>;

  template <typename Initiation, typename... InitArgs>
  static auto initiate(Initiation initiation,
                       boost::cobalt::use_task_t,
                       InitArgs ... args) -> return_type

  {
    co_return co_await async_initiate<
          const cobalt::use_op_t&, void(Args...)>(
              std::move(initiation),
              cobalt::use_op, std::move(args)...);
  }
};

}


#endif //BOOST_COBALT_COBALT_HPP
