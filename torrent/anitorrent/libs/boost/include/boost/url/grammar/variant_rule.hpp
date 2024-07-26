//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_VARIANT_RULE_HPP
#define BOOST_URL_GRAMMAR_VARIANT_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/variant.hpp>
#include <boost/url/grammar/detail/tuple.hpp>

namespace boost {
namespace urls {
namespace grammar {

/** Match one of a set of rules

    Each specified rule is tried in sequence.
    When the first match occurs, the result
    is stored and returned in the variant. If
    no match occurs, an error is returned.

    @par Value Type
    @code
    using value_type = variant< typename Rules::value_type... >;
    @endcode

    @par Example
    Rules are used with the function @ref parse.
    @code
    // request-target = origin-form
    //                / absolute-form
    //                / authority-form
    //                / asterisk-form

    system::result< variant< url_view, url_view, authority_view, core::string_view > > rv = grammar::parse(
        "/index.html?width=full",
        variant_rule(
            origin_form_rule,
            absolute_uri_rule,
            authority_rule,
            delim_rule('*') ) );
    @endcode

    @par BNF
    @code
    variant     = rule1 / rule2 / rule3...
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc5234#section-3.2"
        >3.2.  Alternatives (rfc5234)</a>
    @li <a href="https://datatracker.ietf.org/doc/html/rfc7230#section-5.3"
        >5.3.  Request Target (rfc7230)</a>

    @see
        @ref absolute_uri_rule,
        @ref authority_rule,
        @ref delim_rule,
        @ref parse,
        @ref origin_form_rule,
        @ref url_view.
*/
#ifdef BOOST_URL_DOCS
template<class... Rules>
constexpr
__implementation_defined__
variant_rule( Rules... rn ) noexcept;
#else
template<
    class R0, class... Rn>
class variant_rule_t
{
public:
    using value_type = variant<
        typename R0::value_type,
        typename Rn::value_type...>;

    auto
    parse(
        char const*& it,
        char const* end) const ->
            system::result<value_type>;

    template<
        class R0_,
        class... Rn_>
    friend
    constexpr
    auto
    variant_rule(
        R0_ const& r0,
        Rn_ const&... rn) noexcept ->
            variant_rule_t<R0_, Rn_...>;

private:
    constexpr
    variant_rule_t(
        R0 const& r0,
        Rn const&... rn) noexcept
        : rn_(r0, rn...)
    {
    }

    detail::tuple<R0, Rn...> rn_;
};

template<
    class R0,
    class... Rn>
constexpr
auto
variant_rule(
    R0 const& r0,
    Rn const&... rn) noexcept ->
        variant_rule_t<R0, Rn...>;
#endif

} // grammar
} // urls
} // boost

#include <boost/url/grammar/impl/variant_rule.hpp>

#endif
