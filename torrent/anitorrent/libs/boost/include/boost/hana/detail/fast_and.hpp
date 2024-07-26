/*!
@file
Defines `boost::hana::detail::fast_and`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_DETAIL_FAST_AND_HPP
#define BOOST_HANA_DETAIL_FAST_AND_HPP

#include <boost/hana/config.hpp>

#include <type_traits>


namespace boost { namespace hana { namespace detail {
    template <bool ...b>
    struct fast_and
        : std::is_same<fast_and<b...>, fast_and<((void) b, true)...>>
    { };
} }} // end namespace boost::hana

#endif // !BOOST_HANA_DETAIL_FAST_AND_HPP
