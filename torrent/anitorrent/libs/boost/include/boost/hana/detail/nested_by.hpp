/*!
@file
Defines `boost::hana::detail::nested_by`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_DETAIL_NESTED_BY_HPP
#define BOOST_HANA_DETAIL_NESTED_BY_HPP

#include <boost/hana/config.hpp>
#include <boost/hana/detail/nested_by_fwd.hpp>

#include <boost/hana/functional/flip.hpp>
#include <boost/hana/functional/partial.hpp>


namespace boost { namespace hana { namespace detail {
    //! @cond
    template <typename Algorithm>
    template <typename Predicate, typename Object>
    constexpr decltype(auto) nested_by_t<Algorithm>::
    operator()(Predicate&& predicate, Object&& object) const {
        return Algorithm{}(static_cast<Object&&>(object),
                           static_cast<Predicate&&>(predicate));
    }

    template <typename Algorithm>
    template <typename Predicate>
    constexpr decltype(auto)
    nested_by_t<Algorithm>::operator()(Predicate&& predicate) const {
        return hana::partial(hana::flip(Algorithm{}),
                             static_cast<Predicate&&>(predicate));
    }
    //! @endcond
} }} // end namespace boost::hana

#endif // !BOOST_HANA_DETAIL_NESTED_BY_HPP
