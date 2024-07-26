//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_TYPING_ROW_TRAITS_HPP
#define BOOST_MYSQL_DETAIL_TYPING_ROW_TRAITS_HPP

#include <boost/mysql/detail/config.hpp>

#ifdef BOOST_MYSQL_CXX14

#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>
#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/typing/meta_check_context.hpp>
#include <boost/mysql/detail/typing/pos_map.hpp>
#include <boost/mysql/detail/typing/readable_field_traits.hpp>

#include <boost/assert.hpp>
#include <boost/describe/members.hpp>
#include <boost/mp11/algorithm.hpp>
#include <boost/mp11/utility.hpp>

#include <cstddef>
#include <tuple>
#include <type_traits>
#include <utility>

namespace boost {
namespace mysql {
namespace detail {

// Helpers to check that all the fields satisfy ReadableField
// and produce meaningful error messages, with the offending field type, at least

// Workaround clang 3.6 not liking generic lambdas in the below constexpr function
struct readable_field_checker
{
    template <class TypeIdentity>
    constexpr void operator()(TypeIdentity) const noexcept
    {
        using T = typename TypeIdentity::type;
        static_assert(
            is_readable_field<T>::value,
            "You're trying to use an unsupported field type in a row type. Review your row type definitions."
        );
    }
};

template <class TypeList>
static constexpr bool check_readable_field() noexcept
{
    mp11::mp_for_each<TypeList>(readable_field_checker{});
    return true;
}

// Workaround std::array::data not being constexpr in C++14
template <class T, std::size_t N>
struct array_wrapper
{
    T data_[N];

    constexpr boost::span<const T> span() const noexcept { return boost::span<const T>(data_); }
};

template <class T>
struct array_wrapper<T, 0>
{
    struct
    {
    } data_;  // allow empty brace initialization

    constexpr boost::span<const T> span() const noexcept { return boost::span<const T>(); }
};

// Workaround for char_traits::length not being constexpr in C++14
// Only used to retrieve Describe member name lengths
constexpr std::size_t get_length(const char* s) noexcept
{
    const char* p = s;
    while (*p)
        ++p;
    return p - s;
}

// Helpers
class parse_functor
{
    span<const std::size_t> pos_map_;
    span<const field_view> fields_;
    std::size_t index_{};
    error_code ec_;

public:
    parse_functor(span<const std::size_t> pos_map, span<const field_view> fields) noexcept
        : pos_map_(pos_map), fields_(fields)
    {
    }

    template <class ReadableField>
    void operator()(ReadableField& output)
    {
        auto ec = readable_field_traits<ReadableField>::parse(
            map_field_view(pos_map_, index_++, fields_),
            output
        );
        if (!ec_)
            ec_ = ec;
    }

    error_code error() const noexcept { return ec_; }
};

// Base template
template <class T, bool is_describe_struct = boost::describe::has_describe_members<T>::value>
class row_traits;

// Describe structs
template <class DescribeStruct>
using row_members = boost::describe::
    describe_members<DescribeStruct, boost::describe::mod_public | boost::describe::mod_inherited>;

template <class MemberDescriptor>
constexpr string_view get_member_name(MemberDescriptor d) noexcept
{
    return string_view(d.name, get_length(d.name));
}

template <template <class...> class ListType, class... MemberDescriptor>
constexpr array_wrapper<string_view, sizeof...(MemberDescriptor)> get_describe_names(ListType<
                                                                                     MemberDescriptor...>)
{
    return {{get_member_name(MemberDescriptor())...}};
}

template <class DescribeStruct>
constexpr auto describe_names_storage = get_describe_names(row_members<DescribeStruct>{});

template <class DescribeStruct>
class row_traits<DescribeStruct, true>
{
    using members = row_members<DescribeStruct>;

    template <class D>
    struct descriptor_to_type
    {
        using helper = decltype(std::declval<DescribeStruct>().*std::declval<D>().pointer);
        using type = typename std::remove_reference<helper>::type;
    };

    using member_types = mp11::mp_transform<descriptor_to_type, members>;

    static_assert(check_readable_field<member_types>(), "");

public:
    using types = member_types;

    static constexpr std::size_t size() noexcept { return boost::mp11::mp_size<members>::value; }

    static constexpr name_table_t name_table() noexcept
    {
        return describe_names_storage<DescribeStruct>.span();
    }

    static void parse(parse_functor& parser, DescribeStruct& to)
    {
        boost::mp11::mp_for_each<members>([&](auto D) { parser(to.*D.pointer); });
    }
};

// Tuples
template <class T>
struct is_tuple : std::false_type
{
};
template <class... T>
struct is_tuple<std::tuple<T...>> : std::true_type
{
};

template <class... ReadableField>
class row_traits<std::tuple<ReadableField...>, false>
{
    using tuple_type = std::tuple<ReadableField...>;
    using field_types = boost::mp11::mp_list<boost::mp11::mp_identity<ReadableField>...>;

    static_assert(check_readable_field<field_types>(), "");

public:
    using types = field_types;
    static constexpr std::size_t size() noexcept { return std::tuple_size<tuple_type>::value; }
    static constexpr name_table_t name_table() noexcept { return name_table_t(); }
    static void parse(parse_functor& parser, tuple_type& to) { boost::mp11::tuple_for_each(to, parser); }
};

// We want is_static_row to only inspect the shape of the row (i.e. it's a tuple vs. it's nothing we know),
// and not individual fields. These are static_assert-ed in individual row_traits. This gives us an error
// message that contains the offending types, at least.
template <class T>
struct is_static_row
{
    static constexpr bool value = is_tuple<T>::value || describe::has_describe_members<T>::value;
};

#ifdef BOOST_MYSQL_HAS_CONCEPTS

template <class T>
concept static_row = is_static_row<T>::value;

#define BOOST_MYSQL_STATIC_ROW ::boost::mysql::detail::static_row

#else
#define BOOST_MYSQL_STATIC_ROW class
#endif

// External interface
template <BOOST_MYSQL_STATIC_ROW StaticRow>
constexpr std::size_t get_row_size()
{
    return row_traits<StaticRow>::size();
}

template <BOOST_MYSQL_STATIC_ROW StaticRow>
constexpr name_table_t get_row_name_table()
{
    return row_traits<StaticRow>::name_table();
}

template <BOOST_MYSQL_STATIC_ROW StaticRow>
error_code meta_check(span<const std::size_t> pos_map, metadata_collection_view meta, diagnostics& diag)
{
    using fields = typename row_traits<StaticRow>::types;
    BOOST_ASSERT(pos_map.size() == get_row_size<StaticRow>());
    return meta_check_field_type_list<fields>(pos_map, get_row_name_table<StaticRow>(), meta, diag);
}

template <BOOST_MYSQL_STATIC_ROW StaticRow>
error_code parse(span<const std::size_t> pos_map, span<const field_view> from, StaticRow& to)
{
    BOOST_ASSERT(pos_map.size() == get_row_size<StaticRow>());
    BOOST_ASSERT(from.size() >= get_row_size<StaticRow>());
    parse_functor ctx(pos_map, from);
    row_traits<StaticRow>::parse(ctx, to);
    return ctx.error();
}

using meta_check_fn_t =
    error_code (*)(span<const std::size_t> field_map, metadata_collection_view meta, diagnostics& diag);

// For multi-resultset - helper
template <class... StaticRow>
constexpr std::size_t max_num_columns = (std::max)({get_row_size<StaticRow>()...});

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif  // BOOST_MYSQL_CXX14

#endif
