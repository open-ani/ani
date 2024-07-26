//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_IMPL_VARIANT_RULE_HPP
#define BOOST_URL_GRAMMAR_IMPL_VARIANT_RULE_HPP

#include <boost/url/grammar/error.hpp>
#include <boost/url/grammar/parse.hpp>
#include <cstdint>
#include <type_traits>

namespace boost {
namespace urls {
namespace grammar {

namespace detail {

// must come first
template<
    class R0,
    class... Rn,
    std::size_t I>
auto
parse_variant(
    char const*&,
    char const*,
    detail::tuple<
        R0, Rn...> const&,
    std::integral_constant<
        std::size_t, I> const&,
    std::false_type const&) ->
        system::result<variant<
            typename R0::value_type,
            typename Rn::value_type...>>
{
    // no match
    BOOST_URL_RETURN_EC(
        error::mismatch);
}

template<
    class R0,
    class... Rn,
    std::size_t I>
auto
parse_variant(
    char const*& it,
    char const* const end,
    detail::tuple<
        R0, Rn...> const& rn,
    std::integral_constant<
        std::size_t, I> const&,
    std::true_type const&) ->
        system::result<variant<
            typename R0::value_type,
            typename Rn::value_type...>>
{
    auto const it0 = it;
    auto rv = parse(
        it, end, get<I>(rn));
    if( rv )
        return variant<
            typename R0::value_type,
            typename Rn::value_type...>{
                variant2::in_place_index_t<I>{}, *rv};
    it = it0;
    return parse_variant(
        it, end, rn,
        std::integral_constant<
            std::size_t, I+1>{},
        std::integral_constant<bool,
            ((I + 1) < (1 +
                sizeof...(Rn)))>{});
}

} // detail

template<class R0, class... Rn>
auto
variant_rule_t<R0, Rn...>::
parse(
    char const*& it,
    char const* end) const ->
        system::result<value_type>
{
    return detail::parse_variant(
        it, end, rn_,
        std::integral_constant<
            std::size_t, 0>{},
        std::true_type{});
}

//------------------------------------------------

template<class R0, class... Rn>
auto
constexpr
variant_rule(
    R0 const& r0,
    Rn const&... rn) noexcept ->
        variant_rule_t<R0, Rn...>
{
    return { r0, rn... };
}

} // grammar
} // urls
} // boost

#endif
