//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_MEMORY_RESOURCE_HPP
#define BOOST_JSON_MEMORY_RESOURCE_HPP

#include <boost/json/detail/config.hpp>

#include <boost/container/pmr/memory_resource.hpp>
#include <boost/container/pmr/polymorphic_allocator.hpp>
#include <boost/json/is_deallocate_trivial.hpp>

namespace boost {
namespace json {

/** The type of memory resource used by the library.

    @note This alias is deprecated in favor of
    `boost::container::pmr::memory_resource`. It is included for backwards
    compatibility and shouldn't be used in new code. It will be removed
    completely in version 1.87.0.
*/
typedef boost::container::pmr::memory_resource
    BOOST_DEPRECATED("Use boost::container::pmr::memory_resource instead")
    memory_resource;

/** The type of polymorphic allocator used by the library.

    @note This alias is deprecated in favor of
    `boost::container::pmr::polymorphic_allocator`. It is included for
    backwards compatibility and shouldn't be used in new code. It will be
    removed completely in version 1.87.0.
*/
template<class T>
using
    polymorphic_allocator
#ifndef BOOST_MSVC
    BOOST_DEPRECATED("Use boost::container::pmr::polymorphic_allocator instead")
#endif
    = boost::container::pmr::polymorphic_allocator<T>;

} // namespace json
} // namespace boost

#endif
