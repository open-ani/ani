/*!
@file
Defines an equivalent to the proposed `std::void_t`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_DETAIL_VOID_T_HPP
#define BOOST_HANA_DETAIL_VOID_T_HPP

#include <boost/hana/config.hpp>


namespace boost { namespace hana { namespace detail {
    template <typename ...>
    using void_t = void;
} }} // end namespace boost::hana

#endif // !BOOST_HANA_DETAIL_VOID_T_HPP
