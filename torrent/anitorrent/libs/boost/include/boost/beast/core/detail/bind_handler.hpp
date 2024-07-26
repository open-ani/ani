//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_DETAIL_BIND_HANDLER_HPP
#define BOOST_BEAST_DETAIL_BIND_HANDLER_HPP

#include <boost/beast/core/error.hpp>
#include <boost/beast/core/detail/tuple.hpp>
#include <boost/asio/associator.hpp>
#include <boost/asio/handler_continuation_hook.hpp>
#include <boost/core/ignore_unused.hpp>
#include <boost/mp11/integer_sequence.hpp>
#include <boost/bind/std_placeholders.hpp>
#include <boost/is_placeholder.hpp>
#include <functional>
#include <type_traits>
#include <utility>

namespace boost {
namespace beast {
namespace detail {

//------------------------------------------------------------------------------
//
// bind_handler
//
//------------------------------------------------------------------------------

template<class Handler, class... Args>
class bind_wrapper
{
    using args_type = detail::tuple<Args...>;

    Handler h_;
    args_type args_;

    template <template <typename, typename> class Associator,
        typename T, typename DefaultCandidate>
    friend struct net::associator;

    template<class Arg, class Vals>
    static
    typename std::enable_if<
        std::is_placeholder<typename
            std::decay<Arg>::type>::value == 0 &&
        boost::is_placeholder<typename
            std::decay<Arg>::type>::value == 0,
        Arg&&>::type
    extract(Arg&& arg, Vals&& vals)
    {
        boost::ignore_unused(vals);
        return std::forward<Arg>(arg);
    }

    template<class Arg, class Vals>
    static
    typename std::enable_if<
        std::is_placeholder<typename
            std::decay<Arg>::type>::value != 0,
        tuple_element<std::is_placeholder<
            typename std::decay<Arg>::type>::value - 1,
        Vals>>::type&&
    extract(Arg&&, Vals&& vals)
    {
        return detail::get<std::is_placeholder<
            typename std::decay<Arg>::type>::value - 1>(
                std::forward<Vals>(vals));
    }

    template<class Arg, class Vals>
    static
    typename std::enable_if<
        boost::is_placeholder<typename
            std::decay<Arg>::type>::value != 0 &&
        std::is_placeholder<typename
            std::decay<Arg>::type>::value == 0,
        tuple_element<boost::is_placeholder<
            typename std::decay<Arg>::type>::value - 1,
        Vals>>::type&&
    extract(Arg&&, Vals&& vals)
    {
        return detail::get<boost::is_placeholder<
            typename std::decay<Arg>::type>::value - 1>(
                std::forward<Vals>(vals));
    }

    template<class ArgsTuple, std::size_t... S>
    static
    void
    invoke(
        Handler& h,
        ArgsTuple& args,
        tuple<>&&,
        mp11::index_sequence<S...>)
    {
        boost::ignore_unused(args);
        h(detail::get<S>(std::move(args))...);
    }

    template<
        class ArgsTuple,
        class ValsTuple,
        std::size_t... S>
    static
    void
    invoke(
        Handler& h,
        ArgsTuple& args,
        ValsTuple&& vals,
        mp11::index_sequence<S...>)
    {
        boost::ignore_unused(args);
        boost::ignore_unused(vals);
        h(extract(detail::get<S>(std::move(args)),
            std::forward<ValsTuple>(vals))...);
    }

public:
    using result_type = void; // asio needs this

    bind_wrapper(bind_wrapper&&) = default;
    bind_wrapper(bind_wrapper const&) = default;

    template<
        class DeducedHandler,
        class... Args_>
    explicit
    bind_wrapper(
        DeducedHandler&& handler,
        Args_&&... args)
        : h_(std::forward<DeducedHandler>(handler))
        , args_(std::forward<Args_>(args)...)
    {
    }

    template<class... Values>
    void
    operator()(Values&&... values)
    {
        invoke(h_, args_,
            tuple<Values&&...>(
                std::forward<Values>(values)...),
            mp11::index_sequence_for<Args...>());
    }

    //

    friend
    bool asio_handler_is_continuation(
        bind_wrapper* op)
    {
        using boost::asio::asio_handler_is_continuation;
        return asio_handler_is_continuation(
                std::addressof(op->h_));
    }
};

template<class Handler, class... Args>
class bind_back_wrapper;

template<class Handler, class... Args>
class bind_front_wrapper;

//------------------------------------------------------------------------------
//
// bind_front
//
//------------------------------------------------------------------------------

template<class Handler, class... Args>
class bind_front_wrapper
{
    Handler h_;
    detail::tuple<Args...> args_;

    template <template <typename, typename> class Associator,
        typename T, typename DefaultCandidate>
    friend struct net::associator;

    template<std::size_t... I, class... Ts>
    void
    invoke(
        std::false_type,
        mp11::index_sequence<I...>,
        Ts&&... ts)
    {
        h_( detail::get<I>(std::move(args_))...,
            std::forward<Ts>(ts)...);
    }

    template<std::size_t... I, class... Ts>
    void
    invoke(
        std::true_type,
        mp11::index_sequence<I...>,
        Ts&&... ts)
    {
        std::mem_fn(h_)(
            detail::get<I>(std::move(args_))...,
            std::forward<Ts>(ts)...);
    }

public:
    using result_type = void; // asio needs this

    bind_front_wrapper(bind_front_wrapper&&) = default;
    bind_front_wrapper(bind_front_wrapper const&) = default;

    template<class Handler_, class... Args_>
    bind_front_wrapper(
        Handler_&& handler,
        Args_&&... args)
        : h_(std::forward<Handler_>(handler))
        , args_(std::forward<Args_>(args)...)
    {
    }

    template<class... Ts>
    void operator()(Ts&&... ts)
    {
        invoke(
            std::is_member_function_pointer<Handler>{},
            mp11::index_sequence_for<Args...>{},
            std::forward<Ts>(ts)...);
    }

    //

    friend
    bool asio_handler_is_continuation(
        bind_front_wrapper* op)
    {
        using boost::asio::asio_handler_is_continuation;
        return asio_handler_is_continuation(
            std::addressof(op->h_));
    }
};

} // detail
} // beast
} // boost

//------------------------------------------------------------------------------

namespace boost {
namespace asio {

template <template <typename, typename> class Associator,
    typename Handler, typename... Args, typename DefaultCandidate>
struct associator<Associator,
    beast::detail::bind_wrapper<Handler, Args...>, DefaultCandidate>
    : Associator<Handler, DefaultCandidate>
{
    static typename Associator<Handler, DefaultCandidate>::type get(
        const beast::detail::bind_wrapper<Handler, Args...>& h) noexcept
    {
        return Associator<Handler, DefaultCandidate>::get(h.h_);
    }

    static auto get(const beast::detail::bind_wrapper<Handler, Args...>& h,
        const DefaultCandidate& c) noexcept
    -> decltype(Associator<Handler, DefaultCandidate>::get(h.h_, c))
    {
    return Associator<Handler, DefaultCandidate>::get(h.h_, c);
    }
};

template <template <typename, typename> class Associator,
    typename Handler, typename... Args, typename DefaultCandidate>
struct associator<Associator,
    beast::detail::bind_front_wrapper<Handler, Args...>, DefaultCandidate>
    : Associator<Handler, DefaultCandidate>
{
    static typename Associator<Handler, DefaultCandidate>::type get(
        const beast::detail::bind_front_wrapper<Handler, Args...>& h) noexcept
    {
        return Associator<Handler, DefaultCandidate>::get(h.h_);
    }

    static auto get(const beast::detail::bind_front_wrapper<Handler, Args...>& h,
        const DefaultCandidate& c) noexcept
    -> decltype(Associator<Handler, DefaultCandidate>::get(h.h_, c))
    {
        return Associator<Handler, DefaultCandidate>::get(h.h_, c);
    }
};

} // asio
} // boost

//------------------------------------------------------------------------------

namespace std {

// VFALCO Using std::bind on a completion handler will
// cause undefined behavior later, because the executor
// associated with the handler is not propagated to the
// wrapper returned by std::bind; these overloads are
// deleted to prevent mistakes. If this creates a problem
// please contact me.

template<class Handler, class... Args>
void
bind(boost::beast::detail::bind_wrapper<
    Handler, Args...>, ...) = delete;

template<class Handler, class... Args>
void
bind(boost::beast::detail::bind_front_wrapper<
    Handler, Args...>, ...) = delete;

} // std

//------------------------------------------------------------------------------

#endif
