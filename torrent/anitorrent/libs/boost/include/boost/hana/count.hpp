/*!
@file
Defines `boost::hana::count`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_COUNT_HPP
#define BOOST_HANA_COUNT_HPP

#include <boost/hana/fwd/count.hpp>

#include <boost/hana/concept/foldable.hpp>
#include <boost/hana/config.hpp>
#include <boost/hana/core/dispatch.hpp>
#include <boost/hana/count_if.hpp>
#include <boost/hana/equal.hpp>


namespace boost { namespace hana {
    //! @cond
    template <typename Xs, typename Value>
    constexpr auto count_t::operator()(Xs&& xs, Value&& value) const {
        using S = typename hana::tag_of<Xs>::type;
        using Count = BOOST_HANA_DISPATCH_IF(count_impl<S>,
            hana::Foldable<S>::value
        );

    #ifndef BOOST_HANA_CONFIG_DISABLE_CONCEPT_CHECKS
        static_assert(hana::Foldable<S>::value,
        "hana::count(xs, value) requires 'xs' to be Foldable");
    #endif

        return Count::apply(static_cast<Xs&&>(xs), static_cast<Value&&>(value));
    }
    //! @endcond

    template <typename T, bool condition>
    struct count_impl<T, when<condition>> : default_ {
        template <typename Xs, typename Value>
        static constexpr auto apply(Xs&& xs, Value&& value) {
            return hana::count_if(static_cast<Xs&&>(xs),
                hana::equal.to(static_cast<Value&&>(value)));
        }
    };
}} // end namespace boost::hana

#endif // !BOOST_HANA_COUNT_HPP
