//
// Copyright (c) 2022 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_IMPL_TUPLE_RULE_HPP
#define BOOST_URL_GRAMMAR_IMPL_TUPLE_RULE_HPP

#include <boost/url/grammar/parse.hpp>
#include <boost/mp11/integral.hpp>
#include <boost/mp11/list.hpp>
#include <boost/mp11/tuple.hpp>
#include <type_traits>

namespace boost {
namespace urls {
namespace grammar {

namespace detail {

// returns a tuple
template<
    bool IsList,
    class R0, class... Rn>
struct parse_sequence
{
    using R = detail::tuple<R0, Rn...>;

    using L = mp11::mp_list<
        typename R0::value_type,
        typename Rn::value_type...>;

    using V = mp11::mp_remove<
        std::tuple<
            system::result<typename R0::value_type>,
            system::result<typename Rn::value_type>...>,
        system::result<void>>;

    template<std::size_t I>
    using is_void = std::is_same<
        mp11::mp_at_c<L, I>, void>;

    system::error_code ec;
    R const& rn;
    V vn;

    explicit
    parse_sequence(
        R const& rn_) noexcept
        : rn(rn_)
        , vn(mp11::mp_fill<
            V, system::error_code>{})
    {
    }

    void
    apply(
        char const*&,
        char const*,
        ...) const noexcept
    {
    }

    // for system::result<void>
    template<
        std::size_t Ir,
        std::size_t Iv>
    void
    apply(
        char const*& it,
        char const* end,
        mp11::mp_size_t<Ir> const&,
        mp11::mp_size_t<Iv> const&,
        mp11::mp_true const&)
    {
        system::result<void> rv =
            grammar::parse(
                it, end, get<Ir>(rn));
        if( !rv )
        {
            ec = rv.error();
            return;
        }
        apply(it, end,
            mp11::mp_size_t<Ir+1>{},
            mp11::mp_size_t<Iv>{});
    }

    template<
        std::size_t Ir,
        std::size_t Iv>
    void
    apply(
        char const*& it,
        char const* end,
        mp11::mp_size_t<Ir> const&,
        mp11::mp_size_t<Iv> const&,
        mp11::mp_false const&)
    {
        auto& rv = get<Iv>(vn);
        rv = grammar::parse(
            it, end, get<Ir>(rn));
        if( !rv )
        {
            ec = rv.error();
            return;
        }
        apply(it, end,
            mp11::mp_size_t<Ir+1>{},
            mp11::mp_size_t<Iv+1>{});
    }

    template<
        std::size_t Ir = 0,
        std::size_t Iv = 0>
    typename std::enable_if<
        Ir < 1 + sizeof...(Rn)>::type
    apply(
        char const*& it,
        char const* end,
        mp11::mp_size_t<Ir> const& ir = {},
        mp11::mp_size_t<Iv> const& iv = {}
            ) noexcept
    {
        apply(it, end, ir, iv, is_void<Ir>{});
    }

    struct deref
    {
        template<class R>
        auto
        operator()(R const& r) const ->
            decltype(*r)
        {
            return *r;
        }
    };

    auto
    make_result() noexcept ->
        system::result<typename tuple_rule_t<
            R0, Rn...>::value_type>
    {
        if(ec.failed())
            return ec;
        return mp11::tuple_transform(
            deref{}, vn);
    }
};

// returns a value_type
template<class R0, class... Rn>
struct parse_sequence<false, R0, Rn...>
{
    using R = detail::tuple<R0, Rn...>;

    using L = mp11::mp_list<
        typename R0::value_type,
        typename Rn::value_type...>;

    using V = mp11::mp_first<
        mp11::mp_remove<
            mp11::mp_list<
                system::result<typename R0::value_type>,
                system::result<typename Rn::value_type>...>,
            system::result<void>>>;

    template<std::size_t I>
    using is_void = std::is_same<
        mp11::mp_at_c<L, I>, void>;

    R const& rn;
    V v;

    explicit
    parse_sequence(
        R const& rn_) noexcept
        : rn(rn_)
        , v(system::error_code{})
    {
    }

    void
    apply(
        char const*&,
        char const*,
        ...) const noexcept
    {
    }

    // for system::result<void>
    template<
        std::size_t Ir,
        std::size_t Iv>
    BOOST_URL_NO_INLINE
    void
    apply(
        char const*& it,
        char const* end,
        mp11::mp_size_t<Ir> const&,
        mp11::mp_size_t<Iv> const&,
        mp11::mp_true const&)
    {
        system::result<void> rv =
            grammar::parse(
                it, end, get<Ir>(rn));
        if( !rv )
        {
            v = rv.error();
            return;
        }
        apply(it, end,
            mp11::mp_size_t<Ir+1>{},
            mp11::mp_size_t<Iv>{});
    }

    template<
        std::size_t Ir,
        std::size_t Iv>
    void
    apply(
        char const*& it,
        char const* end,
        mp11::mp_size_t<Ir> const&,
        mp11::mp_size_t<Iv> const&,
        mp11::mp_false const&)
    {
        v = grammar::parse(
            it, end, get<Ir>(rn));
        if( !v )
            return;
        apply(it, end,
            mp11::mp_size_t<Ir+1>{},
            mp11::mp_size_t<Iv+1>{});
    }

    template<
        std::size_t Ir = 0,
        std::size_t Iv = 0>
    typename std::enable_if<
        Ir < 1 + sizeof...(Rn)>::type
    apply(
        char const*& it,
        char const* end,
        mp11::mp_size_t<Ir> const& ir = {},
        mp11::mp_size_t<Iv> const& iv = {}
            ) noexcept
    {
        apply(it, end, ir, iv, is_void<Ir>{});
    }

    V
    make_result() noexcept
    {
        return v;
    }
};

} // detail

template<
    class R0,
    class... Rn>
auto
tuple_rule_t<R0, Rn...>::
parse(
    char const*& it,
    char const* end) const ->
        system::result<value_type>
{
    detail::parse_sequence<
        IsList, R0, Rn...> t(this->get());
    t.apply(it, end);
    return t.make_result();
}

} // grammar
} // urls
} // boost

#endif
