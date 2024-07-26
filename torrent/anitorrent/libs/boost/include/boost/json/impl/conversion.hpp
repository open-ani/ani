//
// Copyright (c) 2020 Krystian Stasiowski (sdkrystian@gmail.com)
// Copyright (c) 2022 Dmitry Arkhipov (grisumbras@yandex.ru)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_CONVERSION_HPP
#define BOOST_JSON_IMPL_CONVERSION_HPP

#include <boost/json/fwd.hpp>
#include <boost/json/value.hpp>
#include <boost/json/string_view.hpp>
#include <boost/describe/enumerators.hpp>
#include <boost/describe/members.hpp>
#include <boost/describe/bases.hpp>
#include <boost/mp11/algorithm.hpp>
#include <boost/mp11/utility.hpp>
#include <boost/system/result.hpp>

#include <iterator>
#include <tuple>
#include <utility>
#ifndef BOOST_NO_CXX17_HDR_VARIANT
# include <variant>
#endif // BOOST_NO_CXX17_HDR_VARIANT

namespace boost {
namespace json {
namespace detail {

#ifdef __cpp_lib_nonmember_container_access
using std::size;
#endif

template<std::size_t I, class T>
using tuple_element_t = typename std::tuple_element<I, T>::type;

template<class T>
using iterator_type = decltype(std::begin(std::declval<T&>()));
template<class T>
using iterator_traits = std::iterator_traits< iterator_type<T> >;

template<class T>
using value_type = typename iterator_traits<T>::value_type;
template<class T>
using mapped_type = tuple_element_t< 1, value_type<T> >;

// had to make the metafunction always succeeding in order to make it work
// with msvc 14.0
template<class T>
using key_type_helper = tuple_element_t< 0, value_type<T> >;
template<class T>
using key_type = mp11::mp_eval_or<
    void,
    key_type_helper,
    T>;

template<class T>
using are_begin_and_end_same = std::is_same<
    iterator_type<T>,
    decltype(std::end(std::declval<T&>()))>;

// msvc 14.0 gets confused when std::is_same is used directly
template<class A, class B>
using is_same_msvc_140 = std::is_same<A, B>;
template<class T>
using is_its_own_value = is_same_msvc_140<value_type<T>, T>;

template<class T>
using not_its_own_value = mp11::mp_not< is_its_own_value<T> >;

template<class T>
using begin_iterator_category = typename std::iterator_traits<
    iterator_type<T>>::iterator_category;

template<class T>
using has_positive_tuple_size = mp11::mp_bool<
    (std::tuple_size<T>::value > 0) >;

template<class T>
using has_unique_keys = has_positive_tuple_size<decltype(
    std::declval<T&>().emplace(
        std::declval<value_type<T>>()))>;

template<class T>
using has_string_type = std::is_same<
    typename T::string_type, std::basic_string<typename T::value_type> >;

template<class T>
struct is_value_type_pair_helper : std::false_type
{ };
template<class T1, class T2>
struct is_value_type_pair_helper<std::pair<T1, T2>> : std::true_type
{ };
template<class T>
using is_value_type_pair = is_value_type_pair_helper<value_type<T>>;

template<class T>
using has_size_member_helper
    = std::is_convertible<decltype(std::declval<T&>().size()), std::size_t>;
template<class T>
using has_size_member = mp11::mp_valid_and_true<has_size_member_helper, T>;
template<class T>
using has_free_size_helper
    = std::is_convertible<
        decltype(size(std::declval<T const&>())),
        std::size_t>;
template<class T>
using has_free_size = mp11::mp_valid_and_true<has_free_size_helper, T>;
template<class T>
using size_implementation = mp11::mp_cond<
    has_size_member<T>, mp11::mp_int<3>,
    has_free_size<T>,   mp11::mp_int<2>,
    std::is_array<T>,   mp11::mp_int<1>,
    mp11::mp_true,      mp11::mp_int<0>>;

template<class T>
std::size_t
try_size(T&& cont, mp11::mp_int<3>)
{
    return cont.size();
}

template<class T>
std::size_t
try_size(T& cont, mp11::mp_int<2>)
{
    return size(cont);
}

template<class T, std::size_t N>
std::size_t
try_size(T(&)[N], mp11::mp_int<1>)
{
    return N;
}

template<class T>
std::size_t
try_size(T&, mp11::mp_int<0>)
{
    return 0;
}

template<class T>
using has_push_back_helper
    = decltype(std::declval<T&>().push_back(std::declval<value_type<T>>()));
template<class T>
using has_push_back = mp11::mp_valid<has_push_back_helper, T>;
template<class T>
using inserter_implementation = mp11::mp_cond<
    is_tuple_like<T>, mp11::mp_int<2>,
    has_push_back<T>, mp11::mp_int<1>,
    mp11::mp_true,    mp11::mp_int<0>>;

template<class T>
iterator_type<T>
inserter(
    T& target,
    mp11::mp_int<2>)
{
    return target.begin();
}

template<class T>
std::back_insert_iterator<T>
inserter(
    T& target,
    mp11::mp_int<1>)
{
    return std::back_inserter(target);
}

template<class T>
std::insert_iterator<T>
inserter(
    T& target,
    mp11::mp_int<0>)
{
    return std::inserter( target, target.end() );
}

using value_from_conversion = mp11::mp_true;
using value_to_conversion = mp11::mp_false;

struct user_conversion_tag { };
struct context_conversion_tag : user_conversion_tag { };
struct full_context_conversion_tag : context_conversion_tag { };
struct native_conversion_tag { };
struct value_conversion_tag : native_conversion_tag { };
struct object_conversion_tag : native_conversion_tag { };
struct array_conversion_tag : native_conversion_tag { };
struct string_conversion_tag : native_conversion_tag { };
struct bool_conversion_tag : native_conversion_tag { };
struct number_conversion_tag : native_conversion_tag { };
struct integral_conversion_tag : number_conversion_tag { };
struct floating_point_conversion_tag : number_conversion_tag { };
struct null_like_conversion_tag { };
struct string_like_conversion_tag { };
struct map_like_conversion_tag { };
struct path_conversion_tag { };
struct sequence_conversion_tag { };
struct tuple_conversion_tag { };
struct described_class_conversion_tag { };
struct described_enum_conversion_tag { };
struct variant_conversion_tag { };
struct optional_conversion_tag { };
struct no_conversion_tag { };

template<class... Args>
using supports_tag_invoke = decltype(tag_invoke( std::declval<Args>()... ));

template<class T>
using has_user_conversion_from_impl = supports_tag_invoke<
    value_from_tag, value&, T&& >;
template<class T>
using has_user_conversion_to_impl = supports_tag_invoke<
    value_to_tag<T>, value const& >;
template<class T>
using has_nonthrowing_user_conversion_to_impl = supports_tag_invoke<
    try_value_to_tag<T>, value const& >;
template< class T, class Dir >
using has_user_conversion1 = mp11::mp_if<
    std::is_same<Dir, value_from_conversion>,
    mp11::mp_valid<has_user_conversion_from_impl, T>,
    mp11::mp_or<
        mp11::mp_valid<has_user_conversion_to_impl, T>,
        mp11::mp_valid<has_nonthrowing_user_conversion_to_impl, T>>>;

template< class Ctx, class T >
using has_context_conversion_from_impl = supports_tag_invoke<
    value_from_tag, value&, T&&, Ctx const& >;
template< class Ctx, class T >
using has_context_conversion_to_impl = supports_tag_invoke<
    value_to_tag<T>, value const&, Ctx const& >;
template< class Ctx, class T >
using has_nonthrowing_context_conversion_to_impl = supports_tag_invoke<
    try_value_to_tag<T>, value const&, Ctx const& >;
template< class Ctx, class T, class Dir >
using has_user_conversion2 = mp11::mp_if<
    std::is_same<Dir, value_from_conversion>,
    mp11::mp_valid<has_context_conversion_from_impl, Ctx, T>,
    mp11::mp_or<
        mp11::mp_valid<has_context_conversion_to_impl, Ctx, T>,
        mp11::mp_valid<has_nonthrowing_context_conversion_to_impl, Ctx, T>>>;

template< class Ctx, class T >
using has_full_context_conversion_from_impl = supports_tag_invoke<
    value_from_tag, value&, T&&, Ctx const&, Ctx const& >;
template< class Ctx, class T >
using has_full_context_conversion_to_impl = supports_tag_invoke<
    value_to_tag<T>, value const&, Ctx const&,  Ctx const& >;
template< class Ctx, class T >
using has_nonthrowing_full_context_conversion_to_impl = supports_tag_invoke<
    try_value_to_tag<T>, value const&, Ctx const&, Ctx const& >;
template< class Ctx, class T, class Dir >
using has_user_conversion3 = mp11::mp_if<
    std::is_same<Dir, value_from_conversion>,
    mp11::mp_valid<has_full_context_conversion_from_impl, Ctx, T>,
    mp11::mp_or<
        mp11::mp_valid<has_full_context_conversion_to_impl, Ctx, T>,
        mp11::mp_valid<
            has_nonthrowing_full_context_conversion_to_impl, Ctx, T>>>;

template< class T >
using described_non_public_members = describe::describe_members<
    T, describe::mod_private | describe::mod_protected>;
template< class T >
using described_bases = describe::describe_bases<
    T, describe::mod_any_access>;

#if defined(BOOST_MSVC) && BOOST_MSVC < 1920

template< class T >
struct described_member_t_impl;

template< class T, class C >
struct described_member_t_impl<T C::*>
{
    using type = T;
};

template< class T, class D >
using described_member_t = remove_cvref<
    typename described_member_t_impl<
        remove_cvref<decltype(D::pointer)> >::type>;

#else

template< class T, class D >
using described_member_t = remove_cvref<decltype(
    std::declval<T&>().* D::pointer )>;

#endif

template< class T >
using described_members = describe::describe_members<
    T, describe::mod_any_access | describe::mod_inherited>;

// user conversion (via tag_invoke)
template< class Ctx, class T, class Dir >
using user_conversion_category = mp11::mp_cond<
    has_user_conversion3<Ctx, T, Dir>, full_context_conversion_tag,
    has_user_conversion2<Ctx, T, Dir>, context_conversion_tag,
    has_user_conversion1<T, Dir>,      user_conversion_tag>;

// native conversions (constructors and member functions of value)
template< class T >
using native_conversion_category = mp11::mp_cond<
    std::is_same<T, value>,  value_conversion_tag,
    std::is_same<T, array>,  array_conversion_tag,
    std::is_same<T, object>, object_conversion_tag,
    std::is_same<T, string>, string_conversion_tag>;

// generic conversions
template< class T >
using generic_conversion_category = mp11::mp_cond<
    std::is_same<T, bool>,     bool_conversion_tag,
    std::is_integral<T>,       integral_conversion_tag,
    std::is_floating_point<T>, floating_point_conversion_tag,
    is_null_like<T>,           null_like_conversion_tag,
    is_string_like<T>,         string_like_conversion_tag,
    is_map_like<T>,            map_like_conversion_tag,
    is_sequence_like<T>,       sequence_conversion_tag,
    is_tuple_like<T>,          tuple_conversion_tag,
    is_described_class<T>,     described_class_conversion_tag,
    is_described_enum<T>,      described_enum_conversion_tag,
    is_variant_like<T>,        variant_conversion_tag,
    is_optional_like<T>,       optional_conversion_tag,
    is_path_like<T>,           path_conversion_tag,
    // failed to find a suitable implementation
    mp11::mp_true,             no_conversion_tag>;

template< class T >
using nested_type = typename T::type;
template< class T1, class T2 >
using conversion_category_impl_helper = mp11::mp_eval_if_not<
    std::is_same<detail::no_conversion_tag, T1>,
    T1,
    mp11::mp_eval_or_q, T1, mp11::mp_quote<nested_type>, T2>;
template< class Ctx, class T, class Dir >
struct conversion_category_impl
{
    using type = mp11::mp_fold<
        mp11::mp_list<
            mp11::mp_defer<user_conversion_category, Ctx, T, Dir>,
            mp11::mp_defer<native_conversion_category, T>,
            mp11::mp_defer<generic_conversion_category, T>>,
        no_conversion_tag,
        conversion_category_impl_helper>;
};
template< class Ctx, class T, class Dir >
using conversion_category =
    typename conversion_category_impl< Ctx, T, Dir >::type;

template< class T >
using any_conversion_tag = mp11::mp_not<
    std::is_same< T, no_conversion_tag > >;

template< class T, class Dir, class... Ctxs >
struct conversion_category_impl< std::tuple<Ctxs...>, T, Dir >
{
    using ctxs = mp11::mp_list< remove_cvref<Ctxs>... >;
    using cats = mp11::mp_list<
        conversion_category<remove_cvref<Ctxs>, T, Dir>... >;

    template< class I >
    using exists = mp11::mp_less< I, mp11::mp_size<cats> >;

    using context2 = mp11::mp_find< cats, full_context_conversion_tag >;
    using context1 = mp11::mp_find< cats, context_conversion_tag >;
    using context0 = mp11::mp_find< cats, user_conversion_tag >;
    using index = mp11::mp_cond<
        exists<context2>, context2,
        exists<context1>, context1,
        exists<context0>, context0,
        mp11::mp_true, mp11::mp_find_if< cats, any_conversion_tag > >;
    using type = mp11::mp_eval_or<
        no_conversion_tag,
        mp11::mp_at, cats, index >;
};

struct no_context
{};

struct allow_exceptions
{};

template <class T, class Dir>
using can_convert = mp11::mp_not<
    std::is_same<
        detail::conversion_category<no_context, T, Dir>,
        detail::no_conversion_tag>>;

template<class Impl1, class Impl2>
using conversion_round_trips_helper = mp11::mp_or<
    std::is_same<Impl1, Impl2>,
    std::is_base_of<user_conversion_tag, Impl1>,
    std::is_base_of<user_conversion_tag, Impl2>>;
template< class Ctx, class T, class Dir >
using conversion_round_trips  = conversion_round_trips_helper<
    conversion_category<Ctx, T, Dir>,
    conversion_category<Ctx, T, mp11::mp_not<Dir>>>;

template< class T1, class T2 >
struct copy_cref_helper
{
    using type = remove_cvref<T2>;
};
template< class T1, class T2 >
using copy_cref = typename copy_cref_helper< T1, T2 >::type;

template< class T1, class T2 >
struct copy_cref_helper<T1 const, T2>
{
    using type = remove_cvref<T2> const;
};
template< class T1, class T2 >
struct copy_cref_helper<T1&, T2>
{
    using type = copy_cref<T1, T2>&;
};
template< class T1, class T2 >
struct copy_cref_helper<T1&&, T2>
{
    using type = copy_cref<T1, T2>&&;
};

template< class Rng, class Traits >
using forwarded_value_helper = mp11::mp_if<
    std::is_convertible<
        typename Traits::reference,
        copy_cref<Rng, typename Traits::value_type> >,
    copy_cref<Rng, typename Traits::value_type>,
    typename Traits::value_type >;

template< class Rng >
using forwarded_value = forwarded_value_helper<
    Rng, iterator_traits< Rng > >;

template< class Ctx, class T, class Dir >
struct supported_context
{
    using type = Ctx;

    static
    type const&
    get( Ctx const& ctx ) noexcept
    {
        return ctx;
    }
};

template< class T, class Dir, class... Ctxs >
struct supported_context< std::tuple<Ctxs...>, T, Dir >
{
    using Ctx = std::tuple<Ctxs...>;
    using impl = conversion_category_impl<Ctx, T, Dir>;
    using index = typename impl::index;
    using next_supported = supported_context<
        mp11::mp_at< typename impl::ctxs, index >, T, Dir >;
    using type = typename next_supported::type;

    static
    type const&
    get( Ctx const& ctx ) noexcept
    {
        return next_supported::get( std::get<index::value>( ctx ) );
    }
};

template< class T >
using value_result_type = typename std::decay<
    decltype( std::declval<T&>().value() )>::type;

template< class T >
using can_reset = decltype( std::declval<T&>().reset() );

template< class T >
using has_valueless_by_exception =
    decltype( std::declval<T const&>().valueless_by_exception() );

} // namespace detail

template <class T>
struct result_for<T, value>
{
    using type = system::result< detail::remove_cvref<T> >;
};

template<class T>
struct is_string_like
    : std::is_convertible<T, string_view>
{ };

template<class T>
struct is_path_like
    : mp11::mp_all<
        mp11::mp_valid_and_true<detail::is_its_own_value, T>,
        mp11::mp_valid_and_true<detail::has_string_type, T>>
{ };
template<class T>
struct is_sequence_like
    : mp11::mp_all<
        mp11::mp_valid_and_true<detail::are_begin_and_end_same, T>,
        mp11::mp_valid_and_true<detail::not_its_own_value, T>,
        mp11::mp_valid<detail::begin_iterator_category, T>>
{ };

template<class T>
struct is_map_like
    : mp11::mp_all<
        is_sequence_like<T>,
        mp11::mp_valid_and_true<detail::is_value_type_pair, T>,
        is_string_like<detail::key_type<T>>,
        mp11::mp_valid_and_true<detail::has_unique_keys, T>>
{ };

template<class T>
struct is_tuple_like
    : mp11::mp_valid_and_true<detail::has_positive_tuple_size, T>
{ };

template<>
struct is_null_like<std::nullptr_t>
    : std::true_type
{ };

#ifndef BOOST_NO_CXX17_HDR_VARIANT
template<>
struct is_null_like<std::monostate>
    : std::true_type
{ };
#endif // BOOST_NO_CXX17_HDR_VARIANT

template<class T>
struct is_described_class
    : mp11::mp_and<
        describe::has_describe_members<T>,
        mp11::mp_not< std::is_union<T> >,
        mp11::mp_empty<
            mp11::mp_eval_or<
                mp11::mp_list<>, detail::described_non_public_members, T>>,
        mp11::mp_empty<
            mp11::mp_eval_or<mp11::mp_list<>, detail::described_bases, T>>>
{ };

template<class T>
struct is_described_enum
    : describe::has_describe_enumerators<T>
{ };

template<class T>
struct is_variant_like : mp11::mp_valid<detail::has_valueless_by_exception, T>
{ };

template<class T>
struct is_optional_like
    : mp11::mp_and<
        mp11::mp_not<std::is_void<
            mp11::mp_eval_or<void, detail::value_result_type, T>>>,
        mp11::mp_valid<detail::can_reset, T>>
{ };

} // namespace json
} // namespace boost

#endif // BOOST_JSON_IMPL_CONVERSION_HPP
