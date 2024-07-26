/*!
@file
Defines `boost::hana::detail::variadic::reverse_apply`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_DETAIL_VARIADIC_REVERSE_APPLY_HPP
#define BOOST_HANA_DETAIL_VARIADIC_REVERSE_APPLY_HPP

#include <boost/hana/config.hpp>
#include <boost/hana/detail/variadic/reverse_apply/unrolled.hpp>


namespace boost { namespace hana { namespace detail { namespace variadic {
    BOOST_HANA_INLINE_VARIABLE BOOST_HANA_CONSTEXPR_LAMBDA auto reverse_apply =
        [](auto&& f, auto&& ...x) -> decltype(auto) {
            return detail::variadic::reverse_apply_unrolled(
                static_cast<decltype(f)>(f),
                static_cast<decltype(x)>(x)...
            );
        };
}} }} // end namespace boost::hana

#endif // !BOOST_HANA_DETAIL_VARIADIC_REVERSE_APPLY_HPP
