//
// Copyright (c) 2020 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_NULL_RESOURCE_HPP
#define BOOST_JSON_NULL_RESOURCE_HPP

#include <boost/container/pmr/memory_resource.hpp>
#include <boost/json/detail/config.hpp>
#include <boost/json/memory_resource.hpp>

namespace boost {
namespace json {

/** Return a pointer to the null resource.

    This memory resource always throws the exception
    `std::bad_alloc` in calls to `allocate`.

    @par Complexity
    Constant.

    @par Exception Safety
    No-throw guarantee.
*/
BOOST_JSON_DECL
boost::container::pmr::memory_resource*
get_null_resource() noexcept;

} // namespace json
} // namespace boost

#endif
