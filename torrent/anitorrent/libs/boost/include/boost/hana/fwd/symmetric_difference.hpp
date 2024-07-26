/*!
@file
Forward declares `boost::hana::symmetric_difference`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_FWD_SYMMETRIC_DIFFERENCE_HPP
#define BOOST_HANA_FWD_SYMMETRIC_DIFFERENCE_HPP

#include <boost/hana/config.hpp>
#include <boost/hana/core/when.hpp>


namespace boost { namespace hana {
    // Note: This function is documented per datatype/concept only.
    //! @cond
    template <typename S, typename = void>
    struct symmetric_difference_impl : symmetric_difference_impl<S, when<true>> { };
    //! @endcond

    struct symmetric_difference_t {
        template <typename Xs, typename Ys>
        constexpr auto operator()(Xs&&, Ys&&) const;
    };

    BOOST_HANA_INLINE_VARIABLE constexpr symmetric_difference_t symmetric_difference{};
}} // end namespace boost::hana

#endif // !BOOST_HANA_FWD_SYMMETRIC_DIFFERENCE_HPP
