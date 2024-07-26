//
// Copyright (c) 2024 Dmitry Arkhipov (grisumbras@yandex.ru)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IS_DEALLOCATE_TRIVIAL_HPP
#define BOOST_JSON_IS_DEALLOCATE_TRIVIAL_HPP

namespace boost {
namespace json {

/** Return true if a memory resource's deallocate function has no effect.

    This metafunction may be specialized to indicate to the library that calls
    to the `deallocate` function of a `boost::container::pmr::memory_resource`
    have no effect. The implementation will elide such calls when it is safe to
    do so. By default, the implementation assumes that all memory resources
    require a call to `deallocate` for each memory region obtained by
    calling `allocate`.

    @par Example

    This example specializes the metafuction for `my_resource`,
    to indicate that calls to deallocate have no effect:

    @code

    // Forward-declaration for a user-defined memory resource
    struct my_resource;

    // It is necessary to specialize the template from
    // inside the namespace in which it is declared:

    namespace boost {
    namespace json {

    template<>
    struct is_deallocate_trivial< my_resource >
    {
        static constexpr bool value = true;
    };

    } // namespace json
    } // namespace boost

    @endcode

    It is usually not necessary for users to check this trait.
    Instead, they can call @ref storage_ptr::is_deallocate_trivial
    to determine if the pointed-to memory resource has a trivial
    deallocate function.

    @see
        @ref storage_ptr,
       [`boost::container::pmr::memory_resource`](https://www.boost.org/doc/libs/release/doc/html/boost/container/pmr/memory_resource.html).
*/
template<class T>
struct is_deallocate_trivial
{
    /** A bool equal to true if calls to `T::do_deallocate` have no effect.

        The primary template sets `value` to false.
    */
    static constexpr bool value = false;
};

} // namespace json
} // namespace boost

#endif // BOOST_JSON_IS_DEALLOCATE_TRIVIAL_HPP
