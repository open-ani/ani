/*!
@file
Defines `boost::hana::Metafunction`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_CONCEPT_METAFUNCTION_HPP
#define BOOST_HANA_CONCEPT_METAFUNCTION_HPP

#include <boost/hana/fwd/concept/metafunction.hpp>

#include <boost/hana/config.hpp>
#include <boost/hana/core/tag_of.hpp>
#include <boost/hana/detail/integral_constant.hpp>


namespace boost { namespace hana {
    namespace detail {
        template <typename F, typename Tag = typename tag_of<F>::type>
        struct metafunction_dispatch
            : hana::integral_constant<bool,
                Metafunction<Tag>::value
            >
        { };

        template <typename F>
        struct metafunction_dispatch<F, F>
            : hana::integral_constant<bool, false>
        { };
    }

    template <typename F>
    struct Metafunction
        : detail::metafunction_dispatch<F>
    { };
}} // end namespace boost::hana

#endif // !BOOST_HANA_CONCEPT_METAFUNCTION_HPP
