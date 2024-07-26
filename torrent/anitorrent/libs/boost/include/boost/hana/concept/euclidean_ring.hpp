/*!
@file
Defines `boost::hana::EuclideanRing`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_CONCEPT_EUCLIDEAN_RING_HPP
#define BOOST_HANA_CONCEPT_EUCLIDEAN_RING_HPP

#include <boost/hana/fwd/concept/euclidean_ring.hpp>

#include <boost/hana/config.hpp>
#include <boost/hana/core/default.hpp>
#include <boost/hana/core/tag_of.hpp>
#include <boost/hana/detail/integral_constant.hpp>
#include <boost/hana/div.hpp>
#include <boost/hana/mod.hpp>


namespace boost { namespace hana {
    template <typename R>
    struct EuclideanRing
        : hana::integral_constant<bool,
            !is_default<mod_impl<typename tag_of<R>::type,
                                 typename tag_of<R>::type>>::value &&
            !is_default<div_impl<typename tag_of<R>::type,
                                 typename tag_of<R>::type>>::value
        >
    { };
}} // end namespace boost::hana

#endif // !BOOST_HANA_CONCEPT_EUCLIDEAN_RING_HPP
