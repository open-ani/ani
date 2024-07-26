//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_RFC_DETAIL_PATH_RULES_HPP
#define BOOST_URL_RFC_DETAIL_PATH_RULES_HPP

#include "boost/url/rfc/pchars.hpp"
#include "boost/url/rfc/pct_encoded_rule.hpp"
#include "boost/url/grammar/delim_rule.hpp"
#include "boost/url/grammar/range_rule.hpp"
#include "boost/url/grammar/tuple_rule.hpp"

namespace boost {
namespace urls {
namespace detail {

/** Rule for segment

    @par BNF
    @code
    segment       = *pchar
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
        >3.3. Path (rfc3986)</a>

    @see
        @ref grammar::parse.
*/
constexpr auto segment_rule =
    pct_encoded_rule(pchars);

} // detail
} // urls
} // boost

#endif
