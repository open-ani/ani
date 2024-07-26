//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_REPLACEMENT_FIELD_RULE_HPP
#define BOOST_URL_DETAIL_REPLACEMENT_FIELD_RULE_HPP

#include <boost/url/error.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/grammar/variant_rule.hpp>
#include <boost/url/grammar/unsigned_rule.hpp>

namespace boost {
namespace urls {
namespace detail {

// replacement_field ::=  "{" [arg_id] [":" format_spec "}"
struct replacement_field_rule_t
{
    using value_type = core::string_view;

    BOOST_URL_DECL
    system::result<value_type>
    parse(
        char const*& it,
        char const* end) const noexcept;
};

constexpr replacement_field_rule_t replacement_field_rule{};

// identifier        ::=  id_start id_continue*
// id_start          ::=  "a"..."z" | "A"..."Z" | "_"
// id_continue       ::=  id_start | digit
struct identifier_rule_t
{
    using value_type = core::string_view;

    BOOST_URL_DECL
    system::result<value_type>
    parse(
        char const*& it,
        char const* end) const noexcept;
};

constexpr identifier_rule_t identifier_rule{};

// arg_id            ::=  integer | identifier
// integer           ::=  digit+
// digit             ::=  "0"..."9"
static constexpr auto arg_id_rule =
    grammar::variant_rule(
        identifier_rule,
        grammar::unsigned_rule<std::size_t>{});

struct format_spec_rule_t
{
    using value_type = core::string_view;

    system::result<value_type>
    parse(
        char const*& it,
        char const* end) const noexcept;
};

constexpr format_spec_rule_t format_spec_rule{};

} // detail
} // urls
} // boost

#endif
