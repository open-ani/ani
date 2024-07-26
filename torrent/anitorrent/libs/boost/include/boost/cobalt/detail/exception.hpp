//
// Copyright (c) 2022 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETAIL_EXCEPTION_HPP
#define BOOST_COBALT_DETAIL_EXCEPTION_HPP

#include <boost/config.hpp>
#include <boost/cobalt/config.hpp>

#include <exception>

namespace boost::cobalt::detail
{

BOOST_COBALT_DECL std::exception_ptr moved_from_exception();
BOOST_COBALT_DECL std::exception_ptr detached_exception();
BOOST_COBALT_DECL std::exception_ptr completed_unexpected();
BOOST_COBALT_DECL std::exception_ptr wait_not_ready();
BOOST_COBALT_DECL std::exception_ptr already_awaited();
BOOST_COBALT_DECL std::exception_ptr allocation_failed();

template<typename >
std::exception_ptr wait_not_ready() { return boost::cobalt::detail::wait_not_ready();}

}

#endif //BOOST_COBALT_DETAIL_EXCEPTION_HPP
