//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/http_proto
//

#ifndef BOOST_URL_IMPL_GRAMMAR_TOKEN_RULE_HPP
#define BOOST_URL_IMPL_GRAMMAR_TOKEN_RULE_HPP

#include <boost/url/grammar/error.hpp>

namespace boost {
namespace urls {
namespace grammar {

template<class CharSet>
auto
token_rule_t<CharSet>::
parse(
    char const*& it,
    char const* end
        ) const noexcept ->
    system::result<value_type>
{
    auto const it0 = it;
    if(it == end)
    {
        BOOST_URL_RETURN_EC(
            error::need_more);
    }
    it = (find_if_not)(it, end, cs_);
    if(it != it0)
        return core::string_view(it0, it - it0);
    BOOST_URL_RETURN_EC(
        error::mismatch);
}

} // grammar
} // urls
} // boost

#endif
