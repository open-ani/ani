/*!
@file
Defines `boost::hana::erase_key`.

Copyright Louis Dionne 2013-2022
Distributed under the Boost Software License, Version 1.0.
(See accompanying file LICENSE.md or copy at http://boost.org/LICENSE_1_0.txt)
 */

#ifndef BOOST_HANA_ERASE_KEY_HPP
#define BOOST_HANA_ERASE_KEY_HPP

#include <boost/hana/fwd/erase_key.hpp>

#include <boost/hana/config.hpp>
#include <boost/hana/core/dispatch.hpp>


namespace boost { namespace hana {
    //! @cond
    template <typename Set, typename ...Args>
    constexpr decltype(auto) erase_key_t::operator()(Set&& set, Args&& ...args) const {
        return erase_key_impl<typename hana::tag_of<Set>::type>::apply(
            static_cast<Set&&>(set),
            static_cast<Args&&>(args)...
        );
    }
    //! @endcond

    template <typename T, bool condition>
    struct erase_key_impl<T, when<condition>> : default_ {
        template <typename ...Args>
        static constexpr auto apply(Args&& ...) = delete;
    };
}} // end namespace boost::hana

#endif // !BOOST_HANA_ERASE_KEY_HPP
