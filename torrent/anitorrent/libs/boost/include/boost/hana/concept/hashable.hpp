/*!
@file
Defines `boost::hana::Hashable`.

Copyright Louis Dionne 2016
Copyright Jason Rice 2016
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_CONCEPT_HASHABLE_HPP
#define BOOST_HANA_CONCEPT_HASHABLE_HPP

#include <boost/hana/fwd/concept/hashable.hpp>

#include <boost/hana/config.hpp>
#include <boost/hana/core/default.hpp>
#include <boost/hana/core/tag_of.hpp>
#include <boost/hana/detail/integral_constant.hpp>
#include <boost/hana/hash.hpp>


namespace boost { namespace hana {
    template <typename T>
    struct Hashable
        : hana::integral_constant<bool,
            !is_default<hash_impl<typename tag_of<T>::type>>::value
        >
    { };
}} // end namespace boost::hana

#endif // !BOOST_HANA_CONCEPT_HASHABLE_HPP
