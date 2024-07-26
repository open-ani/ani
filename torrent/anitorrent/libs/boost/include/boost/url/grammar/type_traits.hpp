//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_TYPE_TRAITS_HPP
#define BOOST_URL_GRAMMAR_TYPE_TRAITS_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <type_traits>

namespace boost {
namespace urls {
namespace grammar {

/** Determine if T meets the requirements of Rule

    This is an alias for `std::true_type` if
    `T` meets the requirements, otherwise it
    is an alias for `std::false_type`.

    @par Example
    @code
    struct U
    {
        struct value_type;

        auto
        parse(
            char const*& it,
            char const* end) const ->
                system::result<value_type>
    };

    static_assert( is_rule<U>::value, "Requirements not met" );
    @endcode

    @see
        @ref parse.
*/
#ifdef BOOST_URL_DOCS
template<class T>
using is_rule = __see_below__;
#else
template<class T, class = void>
struct is_rule : std::false_type {};

template<class T>
struct is_rule<T, void_t<decltype(
    std::declval<system::result<typename T::value_type>&>() =
        std::declval<T const&>().parse(
            std::declval<char const*&>(),
            std::declval<char const*>())
    )>> : std::is_nothrow_copy_constructible<T>
{
};
#endif

} // grammar
} // urls
} // boost

#endif
