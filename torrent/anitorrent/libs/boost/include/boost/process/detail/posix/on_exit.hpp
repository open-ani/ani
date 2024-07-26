// Copyright (c) 2016 Klemens D. Morgenstern
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_PROCESS_POSIX_ON_EXIT_HPP_
#define BOOST_PROCESS_POSIX_ON_EXIT_HPP_

#include <boost/asio/execution.hpp>
#include <boost/process/async.hpp>
#include <boost/process/detail/config.hpp>
#include <boost/process/detail/handler_base.hpp>
#include <boost/process/detail/posix/async_handler.hpp>
#include <system_error>
#include <functional>

namespace boost { namespace process { namespace detail {

template<typename Tuple>
inline asio::io_context& get_io_context(const Tuple & tup);

namespace posix {

struct on_exit_ : boost::process::detail::posix::async_handler
{
    std::function<void(int, const std::error_code&)> handler;
    on_exit_(const std::function<void(int, const std::error_code&)> & handler) : handler(handler)
    {
    }

    template<typename Executor>
    std::function<void(int, const std::error_code&)> on_exit_handler(Executor& exec)
    {
        auto v = boost::asio::prefer(boost::process::detail::get_io_context(exec.seq).get_executor(),
                                     boost::asio::execution::outstanding_work.tracked);
        auto handler_ = this->handler;
        return
            [handler_, v](int exit_code, const std::error_code & ec)
            {
                handler_(exit_code, ec);
            };

    }
};


}}}}
#endif /* BOOST_PROCESS_POSIX_ON_EXIT_HPP_ */
