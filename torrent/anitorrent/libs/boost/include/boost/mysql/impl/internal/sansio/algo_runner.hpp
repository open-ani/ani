//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_ALGO_RUNNER_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_ALGO_RUNNER_HPP

#include <boost/mysql/error_code.hpp>

#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/next_action.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>

#include <cstddef>
#include <type_traits>

#ifdef BOOST_MYSQL_VALGRIND_TESTS
#include <valgrind/memcheck.h>
#endif

namespace boost {
namespace mysql {
namespace detail {

// Valgrind
#ifdef BOOST_MYSQL_VALGRIND_TESTS
inline void valgrind_make_mem_defined(const void* data, std::size_t size)
{
    VALGRIND_MAKE_MEM_DEFINED(data, size);
}
#else
inline void valgrind_make_mem_defined(const void*, std::size_t) noexcept {}
#endif

class algo_runner : asio::coroutine
{
    any_algo_ref algo_;

    connection_state_data& conn_state() noexcept { return algo_.get().conn_state(); }

public:
    algo_runner(any_algo_ref algo) : algo_(algo) {}

    next_action resume(error_code ec, std::size_t bytes_transferred)
    {
        next_action act;

        BOOST_ASIO_CORO_REENTER(*this)
        {
            // Run until completion
            while (true)
            {
                // Run the op
                act = algo_.resume(ec);

                // Check next action
                if (act.is_done())
                {
                    return act;
                }
                else if (act.type() == next_action::type_t::read)
                {
                    // Read until a complete message is received
                    // (may be zero times if cached)
                    while (!conn_state().reader.done() && !ec)
                    {
                        conn_state().reader.prepare_buffer();
                        BOOST_ASIO_CORO_YIELD return next_action::read(
                            {conn_state().reader.buffer(), conn_state().ssl_active()}
                        );
                        valgrind_make_mem_defined(conn_state().reader.buffer().data(), bytes_transferred);
                        conn_state().reader.resume(bytes_transferred);
                    }

                    // Check for errors
                    if (!ec)
                        ec = conn_state().reader.error();

                    // We've got a message, continue
                }
                else if (act.type() == next_action::type_t::write)
                {
                    // Write until a complete message was written
                    while (!conn_state().writer.done() && !ec)
                    {
                        BOOST_ASIO_CORO_YIELD return next_action::write(
                            {conn_state().writer.current_chunk(), conn_state().ssl_active()}
                        );
                        conn_state().writer.resume(bytes_transferred);
                    }

                    // We fully wrote a message, continue
                }
                else
                {
                    // Other ops always require I/O
                    BOOST_ASIO_CORO_YIELD return act;
                }
            }
        }

        return next_action();
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
