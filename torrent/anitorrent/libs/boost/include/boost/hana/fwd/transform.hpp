/*!
@file
Forward declares `boost::hana::transform`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_FWD_TRANSFORM_HPP
#define BOOST_HANA_FWD_TRANSFORM_HPP

#include <boost/hana/config.hpp>
#include <boost/hana/core/when.hpp>


namespace boost { namespace hana {
    //! Map a function over a `Functor`.
    //! @ingroup group-Functor
    //!
    //!
    //! Signature
    //! ---------
    //! Given `F` a Functor, the signature is
    //! \f$
    //!     \mathtt{transform} : F(T) \times (T \to U) \to F(U)
    //! \f$
    //!
    //! @param xs
    //! The structure to map `f` over.
    //!
    //! @param f
    //! A function called as `f(x)` on element(s) `x` of the structure,
    //! and returning a new value to replace `x` in the structure.
    //!
    //!
    //! Example
    //! -------
    //! @include example/transform.cpp
#ifdef BOOST_HANA_DOXYGEN_INVOKED
    constexpr auto transform = [](auto&& xs, auto&& f) {
        return tag-dispatched;
    };
#else
    template <typename Xs, typename = void>
    struct transform_impl : transform_impl<Xs, when<true>> { };

    struct transform_t {
        template <typename Xs, typename F>
        constexpr auto operator()(Xs&& xs, F&& f) const;
    };

    BOOST_HANA_INLINE_VARIABLE constexpr transform_t transform{};
#endif
}} // end namespace boost::hana

#endif // !BOOST_HANA_FWD_TRANSFORM_HPP
