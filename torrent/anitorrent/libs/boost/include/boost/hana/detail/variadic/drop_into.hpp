/*!
@file
Defines `boost::hana::detail::variadic::drop_into`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_DETAIL_VARIADIC_DROP_INTO_HPP
#define BOOST_HANA_DETAIL_VARIADIC_DROP_INTO_HPP

#include <boost/hana/config.hpp>

#include <cstddef>
#include <utility>


namespace boost { namespace hana { namespace detail { namespace variadic {
    template <std::size_t n, typename F, typename = std::make_index_sequence<n>>
    struct dropper;

    template <std::size_t n, typename F, std::size_t ...ignore>
    struct dropper<n, F, std::index_sequence<ignore...>> {
        F f;

        template <typename ...Rest>
        constexpr auto go(decltype(ignore, (void*)0)..., Rest ...rest) const
        { return f(*rest...); }

        template <typename ...Xs>
        constexpr auto operator()(Xs ...xs) const
        { return go(&xs...); }
    };

    template <std::size_t n>
    struct make_dropper {
        template <typename F>
        constexpr auto operator()(F f) const
        { return dropper<n, decltype(f)>{f}; }
    };

    template <std::size_t n>
    BOOST_HANA_INLINE_VARIABLE constexpr make_dropper<n> drop_into{};
}} }} // end namespace boost::hana

#endif // !BOOST_HANA_DETAIL_VARIADIC_DROP_INTO_HPP
