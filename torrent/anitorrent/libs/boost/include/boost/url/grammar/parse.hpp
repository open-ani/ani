//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_PARSE_HPP
#define BOOST_URL_GRAMMAR_PARSE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/grammar/type_traits.hpp>

namespace boost {
namespace urls {
namespace grammar {

//------------------------------------------------

/** Parse a character buffer using a rule

    @param it A pointer to the start. The
    caller's variable is changed to
    reflect the amount of input consumed.

    @param end A pointer to the end.

    @param r The rule to use

    @return The parsed value upon success,
    otherwise an error.

    @see
        @ref result.
*/
template<class Rule>
system::result<typename Rule::value_type>
parse(
    char const*& it,
    char const* end,
    Rule const& r);       

/** Parse a character buffer using a rule

    This function parses a complete string into
    the specified sequence of rules. If the
    string is not completely consumed, an
    error is returned instead.

    @param s The input string

    @param r The rule to use

    @return The parsed value upon success,
    otherwise an error.

    @see
        @ref result.
*/
template<class Rule>
system::result<typename Rule::value_type>
parse(
    core::string_view s,
    Rule const& r);

//------------------------------------------------

#ifndef BOOST_URL_DOCS
namespace detail {

template<class Rule>
struct rule_ref
{
    Rule const& r_;

    using value_type =
        typename Rule::value_type;

    system::result<value_type>
    parse(
        char const*& it,
        char const* end) const
    {
        return r_.parse(it, end);
    }
};

} // detail
#endif

/** Return a reference to a rule

    This function returns a rule which
    references the specified object. This is
    used to reduce the number of bytes of
    storage (`sizeof`) required by a combinator
    when it stores a copy of the object.
    <br>
    Ownership of the object is not transferred;
    the caller is responsible for ensuring the
    lifetime of the object is extended until it
    is no longer referenced. For best results,
    `ref` should only be used with compile-time
    constants.

    @param r The rule to use
*/
template<class Rule>
constexpr
#ifdef BOOST_URL_DOCS
__implementation_defined__
#else
typename std::enable_if<
    is_rule<Rule>::value &&
    ! std::is_same<Rule,
        detail::rule_ref<Rule> >::value,
    detail::rule_ref<Rule> >::type
#endif
ref(Rule const& r) noexcept
{
    return detail::rule_ref<
        Rule>{r};
}

#ifndef BOOST_URL_DOCS
// If you get a compile error here it
// means you called ref with something
// that is not a CharSet or Rule!
constexpr
void
ref(...) = delete;
#endif

} // grammar
} // urls
} // boost

#include <boost/url/grammar/impl/parse.hpp>

#endif
