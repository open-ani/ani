/*!
@file
Forward declares `boost::hana::lift`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_FWD_LIFT_HPP
#define BOOST_HANA_FWD_LIFT_HPP

#include <boost/hana/config.hpp>
#include <boost/hana/core/when.hpp>


namespace boost { namespace hana {
    //! Lift a value into an `Applicative` structure.
    //! @ingroup group-Applicative
    //!
    //! `lift<A>` takes a normal value and embeds it into a structure whose
    //! shape is represented by the `A` `Applicative`. Note that the value
    //! may be a function, in which case the created structure may be
    //! `ap`plied to another `Applicative` structure containing values.
    //!
    //!
    //! Signature
    //! ---------
    //! Given an Applicative `A`, the signature is
    //! @f$ \mathtt{lift}_A : T \to A(T) @f$.
    //!
    //! @tparam A
    //! A tag representing the `Applicative` into which the value is lifted.
    //!
    //! @param x
    //! The value to lift into the applicative.
    //!
    //!
    //! Example
    //! -------
    //! @include example/lift.cpp
#ifdef BOOST_HANA_DOXYGEN_INVOKED
    template <typename A>
    constexpr auto lift = [](auto&& x) {
        return tag-dispatched;
    };
#else
    template <typename A, typename = void>
    struct lift_impl : lift_impl<A, when<true>> { };

    template <typename A>
    struct lift_t {
        template <typename X>
        constexpr auto operator()(X&& x) const;
    };

    template <typename A>
    BOOST_HANA_INLINE_VARIABLE constexpr lift_t<A> lift{};
#endif
}} // end namespace boost::hana

#endif // !BOOST_HANA_FWD_LIFT_HPP
