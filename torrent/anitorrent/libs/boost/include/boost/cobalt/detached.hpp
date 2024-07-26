//
// Copyright (c) 2023 Klemens Morgenstern (klemens.morgenstern@gmx.net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_COBALT_DETACHED_HPP
#define BOOST_COBALT_DETACHED_HPP

#include <boost/cobalt/detail/detached.hpp>

namespace boost::cobalt
{

struct detached
{
  using promise_type = detail::detached_promise;
};

inline detached detail::detached_promise::get_return_object() { return {}; }


}


#endif //BOOST_COBALT_DETACHED_HPP
