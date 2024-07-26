//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_NULL_RESOURCE_IPP
#define BOOST_JSON_IMPL_NULL_RESOURCE_IPP

#include <boost/json/null_resource.hpp>
#include <boost/throw_exception.hpp>

namespace boost {
namespace json {

namespace detail {

/** A resource which always fails.

    This memory resource always throws the exception
    `std::bad_alloc` in calls to `allocate`.
*/
class null_resource final
    : public container::pmr::memory_resource
{
public:
    /// Copy constructor (deleted)
    null_resource(
        null_resource const&) = delete;

    /// Copy assignment (deleted)
    null_resource& operator=(
        null_resource const&) = delete;

    /** Constructor

        This constructs the resource.

        @par Complexity
        Constant.

        @par Exception Safety
        No-throw guarantee.
    */
    /** @{ */
    null_resource() noexcept = default;

protected:
    void*
    do_allocate(
        std::size_t,
        std::size_t) override
    {
        throw_exception( std::bad_alloc(), BOOST_CURRENT_LOCATION );
    }

    void
    do_deallocate(
        void*,
        std::size_t,
        std::size_t) override
    {
        // do nothing
    }

    bool
    do_is_equal(
        memory_resource const& mr
            ) const noexcept override
    {
        return this == &mr;
    }
};

} // detail

container::pmr::memory_resource*
get_null_resource() noexcept
{
    static detail::null_resource mr;
    return &mr;
}

} // namespace json
} // namespace boost

#endif
