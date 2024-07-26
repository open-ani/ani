//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2020 Krystian Stasiowski (sdkrystian@gmail.com)
// Copyright (c) 2022 Dmitry Arkhipov (grisumbras@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_DETAIL_VALUE_FROM_HPP
#define BOOST_JSON_DETAIL_VALUE_FROM_HPP

#include <boost/json/conversion.hpp>
#include <boost/describe/enum_to_string.hpp>
#include <boost/mp11/algorithm.hpp>

#ifndef BOOST_NO_CXX17_HDR_OPTIONAL
# include <optional>
#endif

namespace boost {
namespace json {

namespace detail {

template< class Ctx, class T >
struct append_tuple_element {
    array& arr;
    Ctx const& ctx;
    T&& t;

    template<std::size_t I>
    void
    operator()(mp11::mp_size_t<I>) const
    {
        using std::get;
        arr.emplace_back(value_from(
            get<I>(std::forward<T>(t)), ctx, arr.storage() ));
    }
};

//----------------------------------------------------------
// User-provided conversion

template< class T, class Ctx >
void
value_from_impl( user_conversion_tag, value& jv, T&& from, Ctx const& )
{
    tag_invoke( value_from_tag(), jv, static_cast<T&&>(from) );
}

template< class T, class Ctx >
void
value_from_impl( context_conversion_tag, value& jv, T&& from, Ctx const& ctx)
{
    using Sup = supported_context<Ctx, T, value_from_conversion>;
    tag_invoke( value_from_tag(), jv, static_cast<T&&>(from), Sup::get(ctx) );
}

template< class T, class Ctx >
void
value_from_impl(
    full_context_conversion_tag, value& jv, T&& from, Ctx const& ctx)
{
    using Sup = supported_context<Ctx, T, value_from_conversion>;
    tag_invoke(
        value_from_tag(), jv, static_cast<T&&>(from), Sup::get(ctx), ctx );
}

//----------------------------------------------------------
// Native conversion

template< class T, class Ctx >
void
value_from_impl( native_conversion_tag, value& jv, T&& from, Ctx const& )
{
    jv = std::forward<T>(from);
}

// null-like types
template< class T, class Ctx >
void
value_from_impl( null_like_conversion_tag, value& jv, T&&, Ctx const& )
{
    // do nothing
    BOOST_ASSERT(jv.is_null());
    (void)jv;
}

// string-like types
template< class T, class Ctx >
void
value_from_impl( string_like_conversion_tag, value& jv, T&& from, Ctx const& )
{
    auto sv = static_cast<string_view>(from);
    jv.emplace_string().assign(sv);
}

// map-like types
template< class T, class Ctx >
void
value_from_impl( map_like_conversion_tag, value& jv, T&& from, Ctx const& ctx )
{
    using std::get;
    object& obj = jv.emplace_object();
    obj.reserve(detail::try_size(from, size_implementation<T>()));
    for (auto&& elem : from)
        obj.emplace(
            get<0>(elem),
            value_from( get<1>(elem), ctx, obj.storage() ));
}

// ranges
template< class T, class Ctx >
void
value_from_impl( sequence_conversion_tag, value& jv, T&& from, Ctx const& ctx )
{
    array& result = jv.emplace_array();
    result.reserve(detail::try_size(from, size_implementation<T>()));
    using ForwardedValue = forwarded_value<T&&>;
    for (auto&& elem : from)
        result.emplace_back(
            value_from(
                // not a static_cast in order to appease clang < 4.0
                ForwardedValue(elem),
                ctx,
                result.storage() ));
}

// tuple-like types
template< class T, class Ctx >
void
value_from_impl( tuple_conversion_tag, value& jv, T&& from, Ctx const& ctx )
{
    constexpr std::size_t n =
        std::tuple_size<remove_cvref<T>>::value;
    array& arr = jv.emplace_array();
    arr.reserve(n);
    mp11::mp_for_each<mp11::mp_iota_c<n>>(
        append_tuple_element< Ctx, T >{ arr, ctx, std::forward<T>(from) });
}

// no suitable conversion implementation
template< class T, class Ctx >
void
value_from_impl( no_conversion_tag, value&, T&&, Ctx const& )
{
    static_assert(
        !std::is_same<T, T>::value,
        "No suitable tag_invoke overload found for the type");
}

template< class Ctx, class T >
struct from_described_member
{
    using Ds = described_members< remove_cvref<T> >;

    object& obj;
    Ctx const& ctx;
    T&& from;

    template< class I >
    void
    operator()(I) const
    {
        using D = mp11::mp_at<Ds, I>;
        obj.emplace(
            D::name,
            value_from(
                static_cast<T&&>(from).* D::pointer,
                ctx,
                obj.storage()));
    }
};

// described classes
template< class T, class Ctx >
void
value_from_impl(
    described_class_conversion_tag, value& jv, T&& from, Ctx const& ctx )
{
    object& obj = jv.emplace_object();
    from_described_member<Ctx, T> member_converter{
        obj, ctx, static_cast<T&&>(from)};

    using Ds = typename decltype(member_converter)::Ds;
    constexpr std::size_t N = mp11::mp_size<Ds>::value;
    obj.reserve(N);
    mp11::mp_for_each< mp11::mp_iota_c<N> >(member_converter);
}

// described enums
template< class T, class Ctx >
void
value_from_impl(
    described_enum_conversion_tag, value& jv, T from, Ctx const& )
{
    (void)jv;
    (void)from;
#ifdef BOOST_DESCRIBE_CXX14
    char const* const name = describe::enum_to_string(from, nullptr);
    if( name )
    {
        string& str = jv.emplace_string();
        str.assign(name);
    }
    else
    {
        using Integer = typename std::underlying_type< remove_cvref<T> >::type;
        jv = static_cast<Integer>(from);
    }
#endif
}

// optionals
template< class T, class Ctx >
void
value_from_impl(
    optional_conversion_tag, value& jv, T&& from, Ctx const& ctx )
{
    if( from )
        value_from( *from, ctx, jv );
    else
        jv = nullptr;
}

// variants
template< class Ctx >
struct value_from_visitor
{
    value& jv;
    Ctx const& ctx;

    template<class T>
    void
    operator()(T&& t)
    {
        value_from( static_cast<T&&>(t), ctx, jv );
    }
};

template< class Ctx, class T >
void
value_from_impl( variant_conversion_tag, value& jv, T&& from, Ctx const& ctx )
{
    visit( value_from_visitor<Ctx>{ jv, ctx }, static_cast<T&&>(from) );
}

template< class Ctx, class T >
void
value_from_impl( path_conversion_tag, value& jv, T&& from, Ctx const& )
{
    std::string s = from.generic_string();
    string_view sv = s;
    jv.emplace_string().assign(sv);
}

//----------------------------------------------------------
// Contextual conversions

template< class Ctx, class T >
using value_from_category = conversion_category<
    Ctx, T, value_from_conversion >;

} // detail

#ifndef BOOST_NO_CXX17_HDR_OPTIONAL
inline
void
tag_invoke(
    value_from_tag,
    value& jv,
    std::nullopt_t)
{
    // do nothing
    BOOST_ASSERT(jv.is_null());
    (void)jv;
}
#endif

} // namespace json
} // namespace boost

#endif
