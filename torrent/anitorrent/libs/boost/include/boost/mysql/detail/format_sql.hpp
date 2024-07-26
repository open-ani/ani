//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_FORMAT_SQL_HPP
#define BOOST_MYSQL_DETAIL_FORMAT_SQL_HPP

#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/writable_field_traits.hpp>

#include <initializer_list>
#include <string>
#include <type_traits>
#ifdef BOOST_MYSQL_HAS_CONCEPTS
#include <concepts>
#endif

namespace boost {
namespace mysql {

// Forward decls
template <class T>
struct formatter;

class format_context_base;
class format_arg;
struct format_options;

namespace detail {

class format_state;

struct formatter_is_unspecialized
{
};

template <class T>
constexpr bool has_specialized_formatter() noexcept
{
    return !std::is_base_of<formatter_is_unspecialized, formatter<T>>::value;
}

// Note: having the concept use separate atomic clauses improves diagnostics
template <class T>
constexpr bool is_formattable_type()
{
    return is_writable_field<T>::value || has_specialized_formatter<T>();
}

#ifdef BOOST_MYSQL_HAS_CONCEPTS

// If you're getting an error referencing this concept,
// it means that you are attempting to format a type that doesn't support it.
template <class T>
concept formattable =
    // This covers basic types and optionals
    is_writable_field<T>::value ||
    // This covers custom types that specialized boost::mysql::formatter
    has_specialized_formatter<T>();

#define BOOST_MYSQL_FORMATTABLE ::boost::mysql::detail::formattable

#else

#define BOOST_MYSQL_FORMATTABLE class

#endif

// A type-erased custom argument passed to format, invoking formatter<T>::format
struct format_custom_arg
{
    const void* obj;
    void (*format_fn)(const void*, format_context_base&);

    template <class T>
    static format_custom_arg create(const T& obj) noexcept
    {
        return {&obj, &do_format<T>};
    }

    template <class T>
    static void do_format(const void* obj, format_context_base& ctx)
    {
        return formatter<T>::format(*static_cast<const T*>(obj), ctx);
    }
};

// A type-erased argument passed to format. Built-in types are passed
// directly in the struct (as a field_view), instead of by pointer,
// to reduce the number of do_format instantiations
struct format_arg_value
{
    bool is_custom;
    union data_t
    {
        field_view fv;
        format_custom_arg custom;

        data_t(field_view fv) noexcept : fv(fv) {}
        data_t(format_custom_arg v) noexcept : custom(v) {}
    } data;
};

// make_format_value: creates a type erased format_arg_value from a typed value.
// Used for types having is_writable_field<T>
template <class T>
format_arg_value make_format_value_impl(const T& v, std::true_type) noexcept
{
    static_assert(
        !has_specialized_formatter<T>(),
        "formatter<T> specializations for basic types (satisfying the WritableField concept) are not "
        "supported. Please remove the formatter specialization"
    );
    return {false, to_field(v)};
}

// Used for types having !is_writable_field<T>
template <class T>
format_arg_value make_format_value_impl(const T& v, std::false_type) noexcept
{
    return {true, format_custom_arg::create(v)};
}

template <class T>
format_arg_value make_format_value(const T& v) noexcept
{
    static_assert(
        is_formattable_type<T>(),
        "T is not formattable. Please use a formattable type or specialize formatter<T> to make it "
        "formattable"
    );
    return make_format_value_impl(v, is_writable_field<T>{});
}

BOOST_MYSQL_DECL
void vformat_sql_to(format_context_base& ctx, string_view format_str, std::initializer_list<format_arg> args);

BOOST_MYSQL_DECL
std::string vformat_sql(
    const format_options& opts,
    string_view format_str,
    std::initializer_list<format_arg> args
);

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
