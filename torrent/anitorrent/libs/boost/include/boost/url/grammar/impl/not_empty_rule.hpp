//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_IMPL_NOT_EMPTY_RULE_HPP
#define BOOST_URL_GRAMMAR_IMPL_NOT_EMPTY_RULE_HPP

#include <boost/url/grammar/error.hpp>
#include <boost/url/grammar/parse.hpp>

namespace boost {
namespace urls {
namespace grammar {

template<class R>
auto
not_empty_rule_t<R>::
parse(
    char const*& it,
    char const* end) const ->
        system::result<value_type>
{
    if(it == end)
    {
        // empty
        BOOST_URL_RETURN_EC(
            error::mismatch);
    }
    auto const it0 = it;
    auto rv = r_.parse(it, end);
    if(  !rv )
    {
        // error
        return rv;
    }
    if(it == it0)
    {
        // empty
        BOOST_URL_RETURN_EC(
            error::mismatch);
    }
    // value
    return rv;
}

} // grammar
} // urls
} // boost

#endif
