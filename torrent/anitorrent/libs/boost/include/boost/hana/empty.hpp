/*!
@file
Defines `boost::hana::empty`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_EMPTY_HPP
#define BOOST_HANA_EMPTY_HPP

#include <boost/hana/fwd/empty.hpp>

#include <boost/hana/concept/monad_plus.hpp>
#include <boost/hana/concept/sequence.hpp>
#include <boost/hana/config.hpp>
#include <boost/hana/core/dispatch.hpp>
#include <boost/hana/core/make.hpp>


namespace boost { namespace hana {
    //! @cond
    template <typename M>
    constexpr auto empty_t<M>::operator()() const {
    #ifndef BOOST_HANA_CONFIG_DISABLE_CONCEPT_CHECKS
        static_assert(hana::MonadPlus<M>::value,
        "hana::empty<M>() requires 'M' to be a MonadPlus");
    #endif

        using Empty = BOOST_HANA_DISPATCH_IF(empty_impl<M>,
            hana::MonadPlus<M>::value
        );

        return Empty::apply();
    }
    //! @endcond

    template <typename M, bool condition>
    struct empty_impl<M, when<condition>> : default_ {
        template <typename ...Args>
        static constexpr auto apply(Args&& ...) = delete;
    };

    template <typename S>
    struct empty_impl<S, when<Sequence<S>::value>> {
        static constexpr auto apply() {
            return hana::make<S>();
        }
    };
}} // end namespace boost::hana

#endif // !BOOST_HANA_EMPTY_HPP
