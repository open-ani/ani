//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_UNSIGNED_RULE_HPP
#define BOOST_URL_GRAMMAR_UNSIGNED_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/grammar/charset.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/static_assert.hpp>
#include <limits>
#include <type_traits>

namespace boost {
namespace urls {
namespace grammar {

/** Match an unsigned decimal

    Extra leading zeroes are disallowed.

    @par Value Type
    @code
    using value_type = Unsigned;
    @endcode

    @par Example
    Rules are used with the function @ref parse.
    @code
    system::result< unsigned short > rv = parse( "32767", unsigned_rule< unsigned short >{} );
    @endcode

    @par BNF
    @code
    unsigned      = "0" / ( ["1"..."9"] *DIGIT )
    @endcode

    @tparam Unsigned The unsigned integer type used
    to store the result.

    @see
        @ref grammar::parse.
*/
#ifdef BOOST_URL_DOCS
template<class Unsigned>
struct unsigned_rule;
#else
template<class Unsigned>
struct unsigned_rule
{
    BOOST_STATIC_ASSERT(
        std::numeric_limits<
            Unsigned>::is_integer &&
        ! std::numeric_limits<
            Unsigned>::is_signed);

    using value_type = Unsigned;

    auto
    parse(
        char const*& it,
        char const* end
            ) const noexcept ->
        system::result<value_type>;
};
#endif

} // grammar
} // urls
} // boost

#include <boost/url/grammar/impl/unsigned_rule.hpp>

#endif
