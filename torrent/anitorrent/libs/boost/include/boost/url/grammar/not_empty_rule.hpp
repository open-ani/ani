//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_NOT_EMPTY_RULE_HPP
#define BOOST_URL_GRAMMAR_NOT_EMPTY_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/grammar/type_traits.hpp>

namespace boost {
namespace urls {
namespace grammar {

/** Match another rule, if the result is not empty

    This adapts another rule such that
    when an empty string is successfully
    parsed, the result is an error.

    @par Value Type
    @code
    using value_type = typename Rule::value_type;
    @endcode

    @par Example
    Rules are used with the function @ref parse.
    @code
    system::result< decode_view > rv = parse( "Program%20Files",
        not_empty_rule( pct_encoded_rule( unreserved_chars ) ) );
    @endcode

    @param r The rule to match

    @see
        @ref parse,
        @ref pct_encoded_rule,
        @ref unreserved_chars.
*/
#ifdef BOOST_URL_DOCS
template<class Rule>
constexpr
__implementation_defined__
not_empty_rule( Rule r );
#else
template<class R>
struct not_empty_rule_t
{
    using value_type =
        typename R::value_type;

    auto
    parse(
        char const*& it,
        char const* end) const ->
            system::result<value_type>;

    template<class R_>
    friend
    constexpr
    auto
    not_empty_rule(
        R_ const& r) ->
            not_empty_rule_t<R_>;

private:
    constexpr
    not_empty_rule_t(
        R const& r) noexcept
        : r_(r)
    {
    }

    R r_;
};

template<class Rule>
auto
constexpr
not_empty_rule(
    Rule const& r) ->
        not_empty_rule_t<Rule>
{
    // If you get a compile error here it
    // means that your rule does not meet
    // the type requirements. Please check
    // the documentation.
    static_assert(
        is_rule<Rule>::value,
        "Rule requirements not met");

    return { r };
}
#endif

} // grammar
} // urls
} // boost

#include <boost/url/grammar/impl/not_empty_rule.hpp>

#endif
