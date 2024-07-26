//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_WRITABLE_FIELD_TRAITS_HPP
#define BOOST_MYSQL_DETAIL_WRITABLE_FIELD_TRAITS_HPP

#include <boost/mysql/field_view.hpp>

#include <boost/mysql/detail/config.hpp>

#include <type_traits>

namespace boost {
namespace mysql {
namespace detail {

template <class T, class En1 = void, class En2 = void>
struct writable_field_traits
{
    static constexpr bool is_supported = false;
};

template <>
struct writable_field_traits<bool>
{
    static constexpr bool is_supported = true;
    static field_view to_field(bool value) noexcept { return field_view(value ? 1 : 0); }
};

template <class T>
struct writable_field_traits<
    T,
    typename std::enable_if<
        std::is_constructible<field_view, const T&>::value && std::is_object<T>::value>::type,
    void>
{
    static constexpr bool is_supported = true;
    static field_view to_field(const T& value) noexcept { return field_view(value); }
};

// Optionals. To avoid dependencies, we use a "concept".
// We consider a type an optional if has a `bool has_value() const` and
// `const value_type& value() const`
template <class T>
struct writable_field_traits<
    T,
    void,
    typename std::enable_if<
        std::is_same<decltype(std::declval<const T&>().has_value()), bool>::value &&
        std::is_same<decltype(std::declval<const T&>().value()), const typename T::value_type&>::value>::type>
{
    using value_traits = writable_field_traits<typename T::value_type>;
    static constexpr bool is_supported = value_traits::is_supported;
    static field_view to_field(const T& value) noexcept
    {
        return value.has_value() ? value_traits::to_field(value.value()) : field_view();
    }
};

template <class T>
field_view to_field(const T& value) noexcept
{
    return writable_field_traits<T>::to_field(value);
}

template <class T>
struct is_writable_field : std::integral_constant<bool, writable_field_traits<T>::is_supported>
{
};

// field_view_forward_iterator
template <typename T, typename = void>
struct is_field_view_forward_iterator : std::false_type
{
};

// clang-format off
template <typename T>
struct is_field_view_forward_iterator<
    T,
    typename std::enable_if<
        std::is_convertible<
            typename std::iterator_traits<T>::reference,
            field_view
        >::value
        &&
        std::is_base_of<
            std::forward_iterator_tag, 
            typename std::iterator_traits<T>::iterator_category
        >::value
    >::type
> : std::true_type { };
// clang-format on

#ifdef BOOST_MYSQL_HAS_CONCEPTS

template <class T>
concept field_view_forward_iterator = is_field_view_forward_iterator<T>::value;

#define BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR ::boost::mysql::detail::field_view_forward_iterator

#else  // BOOST_MYSQL_HAS_CONCEPTS

#define BOOST_MYSQL_FIELD_VIEW_FORWARD_ITERATOR class

#endif  // BOOST_MYSQL_HAS_CONCEPTS

// writable_field_tuple
template <class... T>
struct is_writable_field_tuple_impl : std::false_type
{
};

template <class... T>
struct is_writable_field_tuple_impl<std::tuple<T...>>
    : mp11::mp_all_of<mp11::mp_list<typename std::remove_reference<T>::type...>, is_writable_field>
{
};

template <class Tuple>
struct is_writable_field_tuple : is_writable_field_tuple_impl<typename std::decay<Tuple>::type>
{
};

#ifdef BOOST_MYSQL_HAS_CONCEPTS

template <class T>
concept writable_field_tuple = is_writable_field_tuple<T>::value;

#define BOOST_MYSQL_WRITABLE_FIELD_TUPLE ::boost::mysql::detail::writable_field_tuple

#else  // BOOST_MYSQL_HAS_CONCEPTS

#define BOOST_MYSQL_WRITABLE_FIELD_TUPLE class

#endif  // BOOST_MYSQL_HAS_CONCEPTS

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
