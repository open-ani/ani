//
// Copyright (c) 2023 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_BEAST_TEST_IMMEDIATE_EXECUTOR_HPP
#define BOOST_BEAST_TEST_IMMEDIATE_EXECUTOR_HPP

#include <boost/asio/any_io_executor.hpp>
#include <boost/asio/execution_context.hpp>

namespace boost
{
namespace beast
{
namespace test
{

/** A immediate executor that directly invokes and counts how often that happened. */

class immediate_executor
{
    asio::execution_context* context_ = nullptr;
    std::size_t &count_;

  public:
    immediate_executor(std::size_t & count) noexcept : count_(count) {}

    asio::execution_context &query(asio::execution::context_t) const noexcept
    {
        BOOST_ASSERT(false);
        return *context_;
    }

    constexpr static asio::execution::blocking_t
    query(asio::execution::blocking_t) noexcept
    {
        return asio::execution::blocking_t::never_t{};
    }

    constexpr static asio::execution::relationship_t
    query(asio::execution::relationship_t) noexcept
    {
        return asio::execution::relationship_t::fork_t{};
    }
    // this function takes the function F and runs it on the event loop.
    template<class F>
    void
    execute(F f) const
    {
        count_++;
        std::forward<F>(f)();
    }

    bool
    operator==(immediate_executor const &) const noexcept
    {
        return true;
    }

    bool
    operator!=(immediate_executor const &) const noexcept
    {
        return false;
    }
};

} // test
} // beast

#if ! BOOST_BEAST_DOXYGEN
namespace asio
{
namespace traits
{
template<typename F>
struct execute_member<beast::test::immediate_executor, F>
{
    static constexpr bool is_valid    = true;
    static constexpr bool is_noexcept = false;
    typedef void result_type;
};

template<>
struct equality_comparable<beast::test::immediate_executor>
{
    static constexpr bool is_valid    = true;
    static constexpr bool is_noexcept = true;
};

template<>
struct query_member<beast::test::immediate_executor, execution::context_t>
{
    static constexpr bool is_valid    = true;
    static constexpr bool is_noexcept = true;
    typedef execution_context& result_type;
};

template<typename Property>
struct query_static_constexpr_member<
    beast::test::immediate_executor,
    Property,
    typename enable_if<std::is_convertible<Property, execution::blocking_t>::value>::type>
{
    static constexpr bool is_valid    = true;
    static constexpr bool is_noexcept = true;
    typedef execution::blocking_t::never_t result_type;
    static constexpr result_type value() noexcept
    {
        return result_type();
    }
};
} // traits
} // asio
#endif

} // boost

#endif //BOOST_BEAST_TEST_IMMEDIATE_EXECUTOR_HPP
