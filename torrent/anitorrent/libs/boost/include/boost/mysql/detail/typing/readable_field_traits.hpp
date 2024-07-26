//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_TYPING_READABLE_FIELD_TRAITS_HPP
#define BOOST_MYSQL_DETAIL_TYPING_READABLE_FIELD_TRAITS_HPP

#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/date.hpp>
#include <boost/mysql/datetime.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_kind.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>
#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/string_view.hpp>
#include <boost/mysql/time.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/typing/meta_check_context.hpp>
#include <boost/mysql/detail/typing/pos_map.hpp>
#include <boost/mysql/detail/void_t.hpp>

#include <cstdint>
#include <limits>
#include <string>
#include <type_traits>

namespace boost {
namespace mysql {
namespace detail {

// Helpers for integers
template <class SignedInt>
error_code parse_signed_int(field_view input, SignedInt& output)
{
    using unsigned_t = typename std::make_unsigned<SignedInt>::type;
    using limits_t = std::numeric_limits<SignedInt>;

    auto kind = input.kind();
    if (kind == field_kind::int64)
    {
        auto v = input.get_int64();
        if (v < (limits_t::min)() || v > (limits_t::max)())
        {
            return client_errc::static_row_parsing_error;
        }
        output = static_cast<SignedInt>(v);
        return error_code();
    }
    else if (kind == field_kind::uint64)
    {
        auto v = input.get_uint64();
        if (v > static_cast<unsigned_t>((limits_t::max)()))
        {
            return client_errc::static_row_parsing_error;
        }
        output = static_cast<SignedInt>(v);
        return error_code();
    }
    else
    {
        return client_errc::static_row_parsing_error;
    }
}

template <class UnsignedInt>
error_code parse_unsigned_int(field_view input, UnsignedInt& output)
{
    if (input.kind() != field_kind::uint64)
    {
        return client_errc::static_row_parsing_error;
    }
    auto v = input.get_uint64();
    if (v > (std::numeric_limits<UnsignedInt>::max)())
    {
        return client_errc::static_row_parsing_error;
    }
    output = static_cast<UnsignedInt>(v);
    return error_code();
}

// We want all integer types to be allowed as fields. Some integers
// may have the same width as others, but different type (e.g. long and long long
// may both be 64-bit, but different types). Auxiliar int_traits to allow this to work
template <class T, bool is_signed = std::is_signed<T>::value, std::size_t width = sizeof(T)>
struct int_traits
{
    static constexpr bool is_supported = false;
};

template <class T>
struct int_traits<T, true, 1>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "int8_t";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::tinyint: return !ctx.current_meta().is_unsigned();
        default: return false;
        }
    }
    static error_code parse(field_view input, T& output) { return parse_signed_int(input, output); }
};

template <class T>
struct int_traits<T, false, 1>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "uint8_t";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::tinyint: return ctx.current_meta().is_unsigned();
        default: return false;
        }
    }
    static error_code parse(field_view input, T& output) { return parse_unsigned_int(input, output); }
};

template <class T>
struct int_traits<T, true, 2>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "int16_t";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::tinyint: return true;
        case column_type::smallint:
        case column_type::year: return !ctx.current_meta().is_unsigned();
        default: return false;
        }
    }
    static error_code parse(field_view input, T& output) { return parse_signed_int(input, output); }
};

template <class T>
struct int_traits<T, false, 2>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "uint16_t";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::tinyint:
        case column_type::smallint:
        case column_type::year: return ctx.current_meta().is_unsigned();
        default: return false;
        }
    }
    static error_code parse(field_view input, T& output) { return parse_unsigned_int(input, output); }
};

template <class T>
struct int_traits<T, true, 4>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "int32_t";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::tinyint:
        case column_type::smallint:
        case column_type::year:
        case column_type::mediumint: return true;
        case column_type::int_: return !ctx.current_meta().is_unsigned();
        default: return false;
        }
    }
    static error_code parse(field_view input, T& output) { return parse_signed_int(input, output); }
};

template <class T>
struct int_traits<T, false, 4>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "uint32_t";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::tinyint:
        case column_type::smallint:
        case column_type::year:
        case column_type::mediumint:
        case column_type::int_: return ctx.current_meta().is_unsigned();
        default: return false;
        }
    }
    static error_code parse(field_view input, T& output) { return parse_unsigned_int(input, output); }
};

template <class T>
struct int_traits<T, true, 8>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "int64_t";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::tinyint:
        case column_type::smallint:
        case column_type::year:
        case column_type::mediumint:
        case column_type::int_: return true;
        case column_type::bigint: return !ctx.current_meta().is_unsigned();
        default: return false;
        }
    }
    static error_code parse(field_view input, T& output) { return parse_signed_int(input, output); }
};

template <class T>
struct int_traits<T, false, 8>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "uint64_t";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::tinyint:
        case column_type::smallint:
        case column_type::year:
        case column_type::mediumint:
        case column_type::int_:
        case column_type::bigint: return ctx.current_meta().is_unsigned();
        case column_type::bit: return true;
        default: return false;
        }
    }
    static error_code parse(field_view input, std::uint64_t& output)
    {
        return parse_unsigned_int(input, output);
    }
};

// Traits
template <typename T, class EnableIf = void>
struct readable_field_traits
{
    static constexpr bool is_supported = false;
};

template <>
struct readable_field_traits<char, void> : int_traits<char>
{
};

template <>
struct readable_field_traits<signed char, void> : int_traits<signed char>
{
};

template <>
struct readable_field_traits<unsigned char, void> : int_traits<unsigned char>
{
};

template <>
struct readable_field_traits<short, void> : int_traits<short>
{
};

template <>
struct readable_field_traits<unsigned short, void> : int_traits<unsigned short>
{
};

template <>
struct readable_field_traits<int, void> : int_traits<int>
{
};

template <>
struct readable_field_traits<unsigned int, void> : int_traits<unsigned int>
{
};

template <>
struct readable_field_traits<long, void> : int_traits<long>
{
};

template <>
struct readable_field_traits<unsigned long, void> : int_traits<unsigned long>
{
};

template <>
struct readable_field_traits<long long, void> : int_traits<long long>
{
};

template <>
struct readable_field_traits<unsigned long long, void> : int_traits<unsigned long long>
{
};

template <>
struct readable_field_traits<bool, void>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "bool";
    static bool meta_check(meta_check_context& ctx)
    {
        return ctx.current_meta().type() == column_type::tinyint && !ctx.current_meta().is_unsigned();
    }
    static error_code parse(field_view input, bool& output)
    {
        if (input.kind() != field_kind::int64)
        {
            return client_errc::static_row_parsing_error;
        }
        output = input.get_int64() != 0;
        return error_code();
    }
};

template <>
struct readable_field_traits<float, void>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "float";
    static bool meta_check(meta_check_context& ctx)
    {
        return ctx.current_meta().type() == column_type::float_;
    }
    static error_code parse(field_view input, float& output)
    {
        if (input.kind() != field_kind::float_)
        {
            return client_errc::static_row_parsing_error;
        }
        output = input.get_float();
        return error_code();
    }
};

template <>
struct readable_field_traits<double, void>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "double";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::float_:
        case column_type::double_: return true;
        default: return false;
        }
    }
    static error_code parse(field_view input, double& output)
    {
        auto kind = input.kind();
        if (kind == field_kind::float_)
        {
            output = input.get_float();
            return error_code();
        }
        else if (kind == field_kind::double_)
        {
            output = input.get_double();
            return error_code();
        }
        else
        {
            return client_errc::static_row_parsing_error;
        }
    }
};

template <class Allocator>
struct readable_field_traits<std::basic_string<char, std::char_traits<char>, Allocator>, void>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "string";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::decimal:
        case column_type::char_:
        case column_type::varchar:
        case column_type::text:
        case column_type::enum_:
        case column_type::set:
        case column_type::json: return true;
        default: return false;
        }
    }
    static error_code parse(
        field_view input,
        std::basic_string<char, std::char_traits<char>, Allocator>& output
    )
    {
        if (input.kind() != field_kind::string)
        {
            return client_errc::static_row_parsing_error;
        }
        output = input.get_string();
        return error_code();
    }
};

template <class Allocator>
struct readable_field_traits<std::vector<unsigned char, Allocator>, void>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "blob";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::binary:
        case column_type::varbinary:
        case column_type::blob:
        case column_type::geometry:
        case column_type::unknown: return true;
        default: return false;
        }
    }
    static error_code parse(field_view input, std::vector<unsigned char, Allocator>& output)
    {
        if (input.kind() != field_kind::blob)
        {
            return client_errc::static_row_parsing_error;
        }
        auto view = input.get_blob();
        output.assign(view.begin(), view.end());
        return error_code();
    }
};

template <>
struct readable_field_traits<date, void>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "date";
    static bool meta_check(meta_check_context& ctx) { return ctx.current_meta().type() == column_type::date; }
    static error_code parse(field_view input, date& output)
    {
        if (input.kind() != field_kind::date)
        {
            return client_errc::static_row_parsing_error;
        }
        output = input.get_date();
        return error_code();
    }
};

template <>
struct readable_field_traits<datetime, void>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "datetime";
    static bool meta_check(meta_check_context& ctx)
    {
        switch (ctx.current_meta().type())
        {
        case column_type::datetime:
        case column_type::timestamp: return true;
        default: return false;
        }
    }
    static error_code parse(field_view input, datetime& output)
    {
        if (input.kind() != field_kind::datetime)
        {
            return client_errc::static_row_parsing_error;
        }
        output = input.get_datetime();
        return error_code();
    }
};

template <>
struct readable_field_traits<time, void>
{
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = "time";
    static bool meta_check(meta_check_context& ctx) { return ctx.current_meta().type() == column_type::time; }
    static error_code parse(field_view input, time& output)
    {
        if (input.kind() != field_kind::time)
        {
            return client_errc::static_row_parsing_error;
        }
        output = input.get_time();
        return error_code();
    }
};

// std::optional<T> and boost::optional<T>. To avoid dependencies,
// this is achieved through a "concept"
template <class T, class = void>
struct is_readable_optional : std::false_type
{
};

template <class T>
struct is_readable_optional<
    T,
    void_t<
        typename std::enable_if<
            std::is_same<decltype(std::declval<T&>().value()), typename T::value_type&>::value>::type,
        decltype(std::declval<T&>().emplace()),  // T should be default constructible
        decltype(std::declval<T&>().reset())>> : std::true_type
{
};

template <class T>
struct readable_field_traits<
    T,
    typename std::enable_if<
        is_readable_optional<T>::value && readable_field_traits<typename T::value_type>::is_supported>::type>
{
    using value_type = typename T::value_type;
    static constexpr bool is_supported = true;
    static constexpr const char* type_name = readable_field_traits<value_type>::type_name;
    static bool meta_check(meta_check_context& ctx)
    {
        ctx.set_nullability_checked();
        return readable_field_traits<value_type>::meta_check(ctx);
    }
    static error_code parse(field_view input, T& output)
    {
        if (input.is_null())
        {
            output.reset();
            return error_code();
        }
        else
        {
            output.emplace();
            return readable_field_traits<value_type>::parse(input, output.value());
        }
    }
};

template <class T>
struct is_readable_field
{
    static constexpr bool value = readable_field_traits<T>::is_supported;
};

template <typename ReadableField>
void meta_check_field_impl(meta_check_context& ctx)
{
    using traits_t = readable_field_traits<ReadableField>;

    // Verify that the field is present
    if (ctx.is_current_field_absent())
    {
        ctx.add_field_absent_error();
        return;
    }

    // Perform the check
    bool ok = traits_t::meta_check(ctx);
    if (!ok)
    {
        ctx.add_type_mismatch_error(traits_t::type_name);
    }

    // Check nullability
    if (!ctx.nullability_checked() && !ctx.current_meta().is_not_null())
    {
        ctx.add_nullability_error();
    }
}

template <typename ReadableField>
void meta_check_field(meta_check_context& ctx)
{
    static_assert(is_readable_field<ReadableField>::value, "Should be a ReadableField");
    meta_check_field_impl<ReadableField>(ctx);
    ctx.advance();
}

struct meta_check_field_fn
{
    meta_check_context ctx;

    template <class T>
    void operator()(T)
    {
        meta_check_field<typename T::type>(ctx);
    }
};

template <typename ReadableFieldList>
error_code meta_check_field_type_list(
    span<const std::size_t> field_map,
    name_table_t name_table,
    metadata_collection_view meta,
    diagnostics& diag
)
{
    meta_check_field_fn fn{meta_check_context(field_map, name_table, meta)};
    boost::mp11::mp_for_each<ReadableFieldList>(fn);
    return fn.ctx.check_errors(diag);
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
