//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_IMPL_OPTIONAL_RULE_HPP
#define BOOST_URL_GRAMMAR_IMPL_OPTIONAL_RULE_HPP

#include <boost/url/grammar/error.hpp>

namespace boost {
namespace urls {
namespace grammar {

template<class R>
auto
optional_rule_t<R>::
parse(
    char const*& it,
    char const* end) const ->
        system::result<value_type>
{
    if(it == end)
        return boost::none;
    auto const it0 = it;
    auto rv =
        this->get().parse(it, end);
    if(rv)
        return value_type(*rv);
    it = it0;
    return boost::none;
}

} // grammar
} // urls
} // boost

#endif
