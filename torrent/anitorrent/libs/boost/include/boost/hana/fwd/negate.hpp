/*!
@file
Forward declares `boost::hana::negate`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_FWD_NEGATE_HPP
#define BOOST_HANA_FWD_NEGATE_HPP

#include <boost/hana/config.hpp>
#include <boost/hana/core/when.hpp>


namespace boost { namespace hana {
    //! Return the inverse of an element of a group.
    //! @ingroup group-Group
    //!
    //!
    //! Example
    //! -------
    //! @include example/negate.cpp
#ifdef BOOST_HANA_DOXYGEN_INVOKED
    constexpr auto negate = [](auto&& x) -> decltype(auto) {
        return tag-dispatched;
    };
#else
    template <typename G, typename = void>
    struct negate_impl : negate_impl<G, when<true>> { };

    struct negate_t {
        template <typename X>
        constexpr decltype(auto) operator()(X&& x) const;
    };

    BOOST_HANA_INLINE_VARIABLE constexpr negate_t negate{};
#endif
}} // end namespace boost::hana

#endif // !BOOST_HANA_FWD_NEGATE_HPP
