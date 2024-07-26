/*!
@file
Defines `boost::hana::Orderable`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_CONCEPT_ORDERABLE_HPP
#define BOOST_HANA_CONCEPT_ORDERABLE_HPP

#include <boost/hana/fwd/concept/orderable.hpp>

#include <boost/hana/config.hpp>
#include <boost/hana/core/default.hpp>
#include <boost/hana/core/tag_of.hpp>
#include <boost/hana/detail/integral_constant.hpp>
#include <boost/hana/less.hpp>


namespace boost { namespace hana {
    template <typename Ord>
    struct Orderable
        : hana::integral_constant<bool,
            !is_default<less_impl<typename tag_of<Ord>::type,
                                  typename tag_of<Ord>::type>>::value
        >
    { };
}} // end namespace boost::hana

#endif // !BOOST_HANA_CONCEPT_ORDERABLE_HPP
