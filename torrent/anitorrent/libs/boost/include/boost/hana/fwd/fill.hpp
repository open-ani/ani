/*!
@file
Forward declares `boost::hana::fill`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_FWD_FILL_HPP
#define BOOST_HANA_FWD_FILL_HPP

#include <boost/hana/config.hpp>
#include <boost/hana/core/when.hpp>


namespace boost { namespace hana {
    //! Replace all the elements of a structure with a fixed value.
    //! @ingroup group-Functor
    //!
    //!
    //! Signature
    //! ---------
    //! Given `F` a Functor, the signature is
    //! \f$
    //!     \mathtt{fill} : F(T) \times U \to F(U)
    //! \f$
    //!
    //! @param xs
    //! The structure to fill with a `value`.
    //!
    //! @param value
    //! A value by which every element `x` of the structure is replaced,
    //! unconditionally.
    //!
    //!
    //! Example
    //! -------
    //! @include example/fill.cpp
#ifdef BOOST_HANA_DOXYGEN_INVOKED
    constexpr auto fill = [](auto&& xs, auto&& value) {
        return tag-dispatched;
    };
#else
    template <typename Xs, typename = void>
    struct fill_impl : fill_impl<Xs, when<true>> { };

    struct fill_t {
        template <typename Xs, typename Value>
        constexpr auto operator()(Xs&& xs, Value&& value) const;
    };

    BOOST_HANA_INLINE_VARIABLE constexpr fill_t fill{};
#endif
}} // end namespace boost::hana

#endif // !BOOST_HANA_FWD_FILL_HPP
