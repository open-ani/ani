//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2020 Krystian Stasiowski (sdkrystian@gmail.com)
// Copyright (c) 2021 Dmitry Arkhipov (grisumbras@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_DETAIL_VALUE_TO_HPP
#define BOOST_JSON_DETAIL_VALUE_TO_HPP

#include <boost/json/value.hpp>
#include <boost/json/conversion.hpp>
#include <boost/json/result_for.hpp>
#include <boost/describe/enum_from_string.hpp>

#ifndef BOOST_NO_CXX17_HDR_OPTIONAL
# include <optional>
#endif

namespace boost {
namespace json {

namespace detail {

template<class T>
using has_reserve_member_helper = decltype(std::declval<T&>().reserve(0));
template<class T>
using has_reserve_member = mp11::mp_valid<has_reserve_member_helper, T>;
template<class T>
using reserve_implementation = mp11::mp_cond<
    is_tuple_like<T>,      mp11::mp_int<2>,
    has_reserve_member<T>, mp11::mp_int<1>,
    mp11::mp_true,         mp11::mp_int<0>>;

template<class T>
error
try_reserve(
    T&,
    std::size_t size,
    mp11::mp_int<2>)
{
    constexpr std::size_t N = std::tuple_size<remove_cvref<T>>::value;
    if ( N != size )
        return error::size_mismatch;
    return error();
}

template<typename T>
error
try_reserve(
    T& cont,
    std::size_t size,
    mp11::mp_int<1>)
{
    cont.reserve(size);
    return error();
}

template<typename T>
error
try_reserve(
    T&,
    std::size_t,
    mp11::mp_int<0>)
{
    return error();
}


// identity conversion
template< class Ctx >
system::result<value>
value_to_impl(
    value_conversion_tag,
    try_value_to_tag<value>,
    value const& jv,
    Ctx const& )
{
    return jv;
}

template< class Ctx >
value
value_to_impl(
    value_conversion_tag, value_to_tag<value>, value const& jv, Ctx const& )
{
    return jv;
}

// object
template< class Ctx >
system::result<object>
value_to_impl(
    object_conversion_tag,
    try_value_to_tag<object>,
    value const& jv,
    Ctx const& )
{
    object const* obj = jv.if_object();
    if( obj )
        return *obj;
    system::error_code ec;
    BOOST_JSON_FAIL(ec, error::not_object);
    return ec;
}

// array
template< class Ctx >
system::result<array>
value_to_impl(
    array_conversion_tag,
    try_value_to_tag<array>,
    value const& jv,
    Ctx const& )
{
    array const* arr = jv.if_array();
    if( arr )
        return *arr;
    system::error_code ec;
    BOOST_JSON_FAIL(ec, error::not_array);
    return ec;
}

// string
template< class Ctx >
system::result<string>
value_to_impl(
    string_conversion_tag,
    try_value_to_tag<string>,
    value const& jv,
    Ctx const& )
{
    string const* str = jv.if_string();
    if( str )
        return *str;
    system::error_code ec;
    BOOST_JSON_FAIL(ec, error::not_string);
    return ec;
}

// bool
template< class Ctx >
system::result<bool>
value_to_impl(
    bool_conversion_tag, try_value_to_tag<bool>, value const& jv, Ctx const& )
{
    auto b = jv.if_bool();
    if( b )
        return *b;
    system::error_code ec;
    BOOST_JSON_FAIL(ec, error::not_bool);
    return {boost::system::in_place_error, ec};
}

// integral and floating point
template< class T, class Ctx >
system::result<T>
value_to_impl(
    number_conversion_tag, try_value_to_tag<T>, value const& jv, Ctx const& )
{
    system::error_code ec;
    auto const n = jv.to_number<T>(ec);
    if( ec.failed() )
        return {boost::system::in_place_error, ec};
    return {boost::system::in_place_value, n};
}

// null-like conversion
template< class T, class Ctx >
system::result<T>
value_to_impl(
    null_like_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& )
{
    if( jv.is_null() )
        return {boost::system::in_place_value, T{}};
    system::error_code ec;
    BOOST_JSON_FAIL(ec, error::not_null);
    return {boost::system::in_place_error, ec};
}

// string-like types
template< class T, class Ctx >
system::result<T>
value_to_impl(
    string_like_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& )
{
    auto str = jv.if_string();
    if( str )
        return {boost::system::in_place_value, T(str->subview())};
    system::error_code ec;
    BOOST_JSON_FAIL(ec, error::not_string);
    return {boost::system::in_place_error, ec};
}

// map-like containers
template< class T, class Ctx >
system::result<T>
value_to_impl(
    map_like_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& ctx )
{
    object const* obj = jv.if_object();
    if( !obj )
    {
        system::error_code ec;
        BOOST_JSON_FAIL(ec, error::not_object);
        return {boost::system::in_place_error, ec};
    }

    T res;
    error const e = detail::try_reserve(
        res, obj->size(), reserve_implementation<T>());
    if( e != error() )
    {
        system::error_code ec;
        BOOST_JSON_FAIL( ec, e );
        return {boost::system::in_place_error, ec};
    }

    auto ins = detail::inserter(res, inserter_implementation<T>());
    for( key_value_pair const& kv: *obj )
    {
        auto elem_res = try_value_to<mapped_type<T>>( kv.value(), ctx );
        if( elem_res.has_error() )
            return {boost::system::in_place_error, elem_res.error()};
        *ins++ = value_type<T>{
            key_type<T>(kv.key()),
            std::move(*elem_res)};
    }
    return res;
}

// all other containers
template< class T, class Ctx >
system::result<T>
value_to_impl(
    sequence_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& ctx )
{
    array const* arr = jv.if_array();
    if( !arr )
    {
        system::error_code ec;
        BOOST_JSON_FAIL(ec, error::not_array);
        return {boost::system::in_place_error, ec};
    }

    T result;
    error const e = detail::try_reserve(
        result, arr->size(), reserve_implementation<T>());
    if( e != error() )
    {
        system::error_code ec;
        BOOST_JSON_FAIL( ec, e );
        return {boost::system::in_place_error, ec};
    }

    auto ins = detail::inserter(result, inserter_implementation<T>());
    for( value const& val: *arr )
    {
        auto elem_res = try_value_to<value_type<T>>( val, ctx );
        if( elem_res.has_error() )
            return {boost::system::in_place_error, elem_res.error()};
        *ins++ = std::move(*elem_res);
    }
    return result;
}

// tuple-like types
template< class T, class Ctx >
system::result<T>
try_make_tuple_elem(value const& jv, Ctx const& ctx, system::error_code& ec)
{
    if( ec.failed() )
        return {boost::system::in_place_error, ec};

    auto result = try_value_to<T>( jv, ctx );
    ec = result.error();
    return result;
}

template <class T, class Ctx, std::size_t... Is>
system::result<T>
try_make_tuple_like(
    array const& arr, Ctx const& ctx, boost::mp11::index_sequence<Is...>)
{
    system::error_code ec;
    auto items = std::make_tuple(
        try_make_tuple_elem<
            typename std::decay<tuple_element_t<Is, T>>::type >(
                arr[Is], ctx, ec)
            ...);
    if( ec.failed() )
        return {boost::system::in_place_error, ec};

    return {
        boost::system::in_place_value, T(std::move(*std::get<Is>(items))...)};
}

template< class T, class Ctx >
system::result<T>
value_to_impl(
    tuple_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& ctx )
{
    system::error_code ec;

    array const* arr = jv.if_array();
    if( !arr )
    {
        BOOST_JSON_FAIL(ec, error::not_array);
        return {boost::system::in_place_error, ec};
    }

    constexpr std::size_t N = std::tuple_size<remove_cvref<T>>::value;
    if( N != arr->size() )
    {
        BOOST_JSON_FAIL(ec, error::size_mismatch);
        return {boost::system::in_place_error, ec};
    }

    return try_make_tuple_like<T>(
        *arr, ctx, boost::mp11::make_index_sequence<N>());
}

template< class Ctx, class T, bool non_throwing = true >
struct to_described_member
{
    using Ds = described_members<T>;

    using result_type = mp11::mp_eval_if_c<
        !non_throwing, T, system::result, T>;

    result_type& res;
    object const& obj;
    std::size_t count;
    Ctx const& ctx;

    template< class I >
    void
    operator()(I)
    {
        if( !res )
            return;

        using D = mp11::mp_at<Ds, I>;
        using M = described_member_t<T, D>;

        auto const found = obj.find(D::name);
        if( found == obj.end() )
        {
            BOOST_IF_CONSTEXPR( !is_optional_like<M>::value )
            {
                system::error_code ec;
                BOOST_JSON_FAIL(ec, error::unknown_name);
                res = {boost::system::in_place_error, ec};
            }
            return;
        }

#if defined(__GNUC__) && BOOST_GCC_VERSION >= 80000 && BOOST_GCC_VERSION < 11000
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Wunused"
# pragma GCC diagnostic ignored "-Wunused-variable"
#endif
        auto member_res = try_value_to<M>( found->value(), ctx );
#if defined(__GNUC__) && BOOST_GCC_VERSION >= 80000 && BOOST_GCC_VERSION < 11000
# pragma GCC diagnostic pop
#endif
        if( member_res )
        {
            (*res).* D::pointer = std::move(*member_res);
            ++count;
        }
        else
            res = {boost::system::in_place_error, member_res.error()};
    }
};

// described classes
template< class T, class Ctx >
system::result<T>
value_to_impl(
    described_class_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& ctx )
{
    BOOST_STATIC_ASSERT( std::is_default_constructible<T>::value );
    system::result<T> res;

    auto* obj = jv.if_object();
    if( !obj )
    {
        system::error_code ec;
        BOOST_JSON_FAIL(ec, error::not_object);
        res = {boost::system::in_place_error, ec};
        return res;
    }

    to_described_member< Ctx, T > member_converter{ res, *obj, 0u, ctx };

    using Ds = typename decltype(member_converter)::Ds;
    constexpr std::size_t N = mp11::mp_size<Ds>::value;
    mp11::mp_for_each< mp11::mp_iota_c<N> >(member_converter);

    if( !res )
        return res;

    if( member_converter.count != obj->size() )
    {
        system::error_code ec;
        BOOST_JSON_FAIL(ec, error::size_mismatch);
        res = {boost::system::in_place_error, ec};
        return res;
    }

    return res;
}

// described enums
template< class T, class Ctx >
system::result<T>
value_to_impl(
    described_enum_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& )
{
    T val = {};
    (void)jv;
#ifdef BOOST_DESCRIBE_CXX14
    system::error_code ec;

    auto str = jv.if_string();
    if( !str )
    {
        BOOST_JSON_FAIL(ec, error::not_string);
        return {system::in_place_error, ec};
    }

    if( !describe::enum_from_string(str->data(), val) )
    {
        BOOST_JSON_FAIL(ec, error::unknown_name);
        return {system::in_place_error, ec};
    }
#endif

    return {system::in_place_value, val};
}

// optionals
template< class T, class Ctx >
system::result<T>
value_to_impl(
    optional_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& ctx)
{
    using Inner = value_result_type<T>;
    if( jv.is_null() )
        return {};
    else
        return try_value_to<Inner>(jv, ctx);
}

// variants
template< class T, class V, class I >
using variant_construction_category = mp11::mp_cond<
    std::is_constructible< T, variant2::in_place_index_t<I::value>, V >,
        mp11::mp_int<2>,
#ifndef BOOST_NO_CXX17_HDR_VARIANT
    std::is_constructible< T, std::in_place_index_t<I::value>, V >,
        mp11::mp_int<1>,
#endif // BOOST_NO_CXX17_HDR_VARIANT
    mp11::mp_true,
        mp11::mp_int<0> >;

template< class T, class I, class V >
T
initialize_variant( V&& v, mp11::mp_int<0> )
{
    T t;
    t.template emplace<I::value>( std::move(v) );
    return t;
}

template< class T, class I, class V >
T
initialize_variant( V&& v, mp11::mp_int<2> )
{
    return T( variant2::in_place_index_t<I::value>(), std::move(v) );
}

#ifndef BOOST_NO_CXX17_HDR_VARIANT
template< class T, class I, class V >
T
initialize_variant( V&& v, mp11::mp_int<1> )
{
    return T( std::in_place_index_t<I::value>(), std::move(v) );
}
#endif // BOOST_NO_CXX17_HDR_VARIANT

struct locally_prohibit_exceptions
{};

template< class Ctx >
Ctx const&
make_locally_nonthrowing_context(Ctx const& ctx) noexcept
{
    return ctx;
}

template< class... Ctxes >
std::tuple<Ctxes...> const&
make_locally_nonthrowing_context(std::tuple<Ctxes...> const& ctx) noexcept
{
    return ctx;
}

template< class... Ctxes >
std::tuple<locally_prohibit_exceptions, allow_exceptions, Ctxes...>
make_locally_nonthrowing_context(std::tuple<allow_exceptions, Ctxes...> const& ctx)
    noexcept
{
    return std::tuple_cat(std::make_tuple( locally_prohibit_exceptions() ), ctx);
}

template< class Ctx >
Ctx const&
remove_local_exception_prohibition(Ctx const& ctx) noexcept
{
    return ctx;
}

template< class T, class... Ts, std::size_t... Is>
std::tuple<Ts...>
remove_local_exception_prohibition_helper(
    std::tuple<T, Ts...> const& tup,
    mp11::index_sequence<Is...>) noexcept
{
    return std::tuple<Ts...>( std::get<Is + 1>(tup)... );
}

template< class... Ctxes >
std::tuple<Ctxes...>
remove_local_exception_prohibition(
    std::tuple<locally_prohibit_exceptions, Ctxes...> const& ctx) noexcept
{
    return remove_local_exception_prohibition_helper(
        ctx, mp11::index_sequence_for<Ctxes...>() );
}

template< class T, class Ctx >
struct alternative_converter
{
    system::result<T>& res;
    value const& jv;
    Ctx const& ctx;

    template< class I >
    void operator()( I ) const
    {
        if( res )
            return;

        auto&& local_ctx = make_locally_nonthrowing_context(ctx);
        using V = mp11::mp_at<T, I>;
        auto attempt = try_value_to<V>(jv, local_ctx);
        if( attempt )
        {
            using cat = variant_construction_category<T, V, I>;
            res = initialize_variant<T, I>( std::move(*attempt), cat() );
        }
    }
};

template< class T, class Ctx >
system::result<T>
value_to_impl(
    variant_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& ctx)
{
    system::error_code ec;
    BOOST_JSON_FAIL(ec, error::exhausted_variants);

    using Is = mp11::mp_iota< mp11::mp_size<T> >;

    system::result<T> res = {system::in_place_error, ec};
    mp11::mp_for_each<Is>( alternative_converter<T, Ctx>{res, jv, ctx} );
    return res;
}

template< class T, class Ctx >
system::result<T>
value_to_impl(
    path_conversion_tag, try_value_to_tag<T>, value const& jv, Ctx const& )
{
    auto str = jv.if_string();
    if( !str )
    {
        system::error_code ec;
        BOOST_JSON_FAIL(ec, error::not_string);
        return {boost::system::in_place_error, ec};
    }

    string_view sv = str->subview();
    return {boost::system::in_place_value, T( sv.begin(), sv.end() )};
}

//----------------------------------------------------------
// User-provided conversions; throwing -> throwing
template< class T, class Ctx >
mp11::mp_if< mp11::mp_valid<has_user_conversion_to_impl, T>, T >
value_to_impl(
    user_conversion_tag, value_to_tag<T> tag, value const& jv, Ctx const&)
{
    return tag_invoke(tag, jv);
}

template<
    class T,
    class Ctx,
    class Sup = supported_context<Ctx, T, value_to_conversion>
>
mp11::mp_if<
    mp11::mp_valid< has_context_conversion_to_impl, typename Sup::type, T>, T >
value_to_impl(
    context_conversion_tag,
    value_to_tag<T> tag,
    value const& jv,
    Ctx const& ctx )
{
    return tag_invoke( tag, jv, Sup::get(ctx) );
}

template<
    class T,
    class Ctx,
    class Sup = supported_context<Ctx, T, value_to_conversion>
>
mp11::mp_if<
    mp11::mp_valid<
        has_full_context_conversion_to_impl, typename Sup::type, T>,
    T>
value_to_impl(
    full_context_conversion_tag,
    value_to_tag<T> tag,
    value const& jv,
    Ctx const& ctx )
{
    return tag_invoke( tag, jv, Sup::get(ctx), ctx );
}

//----------------------------------------------------------
// User-provided conversions; throwing -> nonthrowing
template< class T, class Ctx >
mp11::mp_if_c< !mp11::mp_valid<has_user_conversion_to_impl, T>::value, T>
value_to_impl(
    user_conversion_tag, value_to_tag<T>, value const& jv, Ctx const& )
{
    auto res = tag_invoke(try_value_to_tag<T>(), jv);
    if( res.has_error() )
        throw_system_error( res.error() );
    return std::move(*res);
}

template<
    class T,
    class Ctx,
    class Sup = supported_context<Ctx, T, value_to_conversion>
>
mp11::mp_if_c<
    !mp11::mp_valid<
        has_context_conversion_to_impl, typename Sup::type, T>::value,
    T>
value_to_impl(
    context_conversion_tag, value_to_tag<T>, value const& jv, Ctx const& ctx )
{
    auto res = tag_invoke( try_value_to_tag<T>(), jv, Sup::get(ctx) );
    if( res.has_error() )
        throw_system_error( res.error() );
    return std::move(*res);
}

template< class Ctx >
std::tuple<allow_exceptions, Ctx>
make_throwing_context(Ctx const& ctx)
{
    return std::tuple<allow_exceptions, Ctx>(allow_exceptions(), ctx);
}

template< class... Ctxes >
std::tuple<allow_exceptions, Ctxes...>
make_throwing_context(std::tuple<Ctxes...> const& ctx)
{
    return std::tuple_cat(std::make_tuple( allow_exceptions() ), ctx);
}

template< class... Ctxes >
std::tuple<allow_exceptions, Ctxes...> const&
make_throwing_context(std::tuple<allow_exceptions, Ctxes...> const& ctx)
    noexcept
{
    return ctx;
}

template<
    class T,
    class Ctx,
    class Sup = supported_context<Ctx, T, value_to_conversion>
>
mp11::mp_if_c<
    !mp11::mp_valid<
        has_full_context_conversion_to_impl, typename Sup::type, T>::value,
    T>
value_to_impl(
    full_context_conversion_tag,
    value_to_tag<T>,
    value const& jv,
    Ctx const& ctx )
{
    auto res = tag_invoke(
        try_value_to_tag<T>(),
        jv,
        Sup::get(ctx),
        make_throwing_context(ctx));
    if( res.has_error() )
        throw_system_error( res.error() );
    return std::move(*res);
}

//----------------------------------------------------------
// User-provided conversions; nonthrowing -> nonthrowing
template< class T, class Ctx >
mp11::mp_if<
    mp11::mp_valid<
        has_nonthrowing_user_conversion_to_impl, T>, system::result<T> >
value_to_impl(
    user_conversion_tag, try_value_to_tag<T>, value const& jv, Ctx const& )
{
    return tag_invoke(try_value_to_tag<T>(), jv);
}

template<
    class T,
    class Ctx,
    class Sup = supported_context<Ctx, T, value_to_conversion>
>
mp11::mp_if<
    mp11::mp_valid<
        has_nonthrowing_context_conversion_to_impl, typename Sup::type, T>,
    system::result<T> >
value_to_impl(
    context_conversion_tag,
    try_value_to_tag<T> tag,
    value const& jv,
    Ctx const& ctx )
{
    return tag_invoke( tag, jv, Sup::get(ctx) );
}

template<
    class T,
    class Ctx,
    class Sup = supported_context<Ctx, T, value_to_conversion>
>
mp11::mp_if<
    mp11::mp_valid<
        has_nonthrowing_full_context_conversion_to_impl,
        typename Sup::type,
        T>,
    system::result<T> >
value_to_impl(
    full_context_conversion_tag,
    try_value_to_tag<T> tag,
    value const& jv,
    Ctx const& ctx )
{
    return tag_invoke( tag, jv, Sup::get(ctx), ctx );
}

//----------------------------------------------------------
// User-provided conversions; nonthrowing -> throwing

template< class Ctx >
struct does_allow_exceptions : std::false_type
{ };

template< class... Ctxes >
struct does_allow_exceptions< std::tuple<allow_exceptions, Ctxes...> >
    : std::true_type
{ };

template< class T, class... Args >
system::result<T>
wrap_conversion_exceptions( std::true_type, value_to_tag<T>, Args&& ... args )
{
    return {
        boost::system::in_place_value,
        tag_invoke( value_to_tag<T>(), static_cast<Args&&>(args)... )};
}

template< class T, class... Args >
system::result<T>
wrap_conversion_exceptions( std::false_type, value_to_tag<T>, Args&& ... args )
{
#ifndef BOOST_NO_EXCEPTIONS
    try
    {
#endif
        return wrap_conversion_exceptions(
            std::true_type(),
            value_to_tag<T>(),
            static_cast<Args&&>(args)... );
#ifndef BOOST_NO_EXCEPTIONS
    }
    catch( std::bad_alloc const&)
    {
        throw;
    }
    catch( system::system_error const& e)
    {
        return {boost::system::in_place_error, e.code()};
    }
    catch( ... )
    {
        system::error_code ec;
        BOOST_JSON_FAIL(ec, error::exception);
        return {boost::system::in_place_error, ec};
    }
#endif
}

template< class T, class Ctx >
mp11::mp_if_c<
    !mp11::mp_valid<has_nonthrowing_user_conversion_to_impl, T>::value,
    system::result<T> >
value_to_impl(
    user_conversion_tag, try_value_to_tag<T>, value const& jv, Ctx const& )
{
    return wrap_conversion_exceptions(
        does_allow_exceptions<Ctx>(), value_to_tag<T>(), jv);
}

template<
    class T,
    class Ctx,
    class Sup = supported_context<Ctx, T, value_to_conversion>
>
mp11::mp_if_c<
    !mp11::mp_valid<
        has_nonthrowing_context_conversion_to_impl,
        typename Sup::type,
        T>::value,
    system::result<T> >
value_to_impl(
    context_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& ctx )
{
    return wrap_conversion_exceptions(
        does_allow_exceptions<Ctx>(), value_to_tag<T>(), jv, Sup::get(ctx) );
}

template<
    class T,
    class Ctx,
    class Sup = supported_context<Ctx, T, value_to_conversion>
>
mp11::mp_if_c<
    !mp11::mp_valid<
        has_nonthrowing_full_context_conversion_to_impl,
        typename Sup::type,
        T>::value,
    system::result<T> >
value_to_impl(
    full_context_conversion_tag,
    try_value_to_tag<T>,
    value const& jv,
    Ctx const& ctx )
{
    return wrap_conversion_exceptions(
        does_allow_exceptions<Ctx>(),
        value_to_tag<T>(),
        jv,
        Sup::get(ctx),
        remove_local_exception_prohibition(ctx) );
}

// no suitable conversion implementation
template< class T, class Ctx >
T
value_to_impl( no_conversion_tag, value_to_tag<T>, value const&, Ctx const& )
{
    static_assert(
        !std::is_same<T, T>::value,
        "No suitable tag_invoke overload found for the type");
}

// generic wrapper over non-throwing implementations
template< class Impl, class T, class Ctx >
T
value_to_impl( Impl impl, value_to_tag<T>, value const& jv, Ctx const& ctx )
{
    return value_to_impl(
        impl, try_value_to_tag<T>(), jv, make_throwing_context(ctx) ).value();
}

template< class Ctx, class T >
using value_to_category = conversion_category<
    Ctx, T, value_to_conversion >;

} // detail

#ifndef BOOST_NO_CXX17_HDR_OPTIONAL
inline
system::result<std::nullopt_t>
tag_invoke(
    try_value_to_tag<std::nullopt_t>,
    value const& jv)
{
    if( jv.is_null() )
        return std::nullopt;
    system::error_code ec;
    BOOST_JSON_FAIL(ec, error::not_null);
    return ec;
}
#endif

} // namespace json
} // namespace boost

#endif
