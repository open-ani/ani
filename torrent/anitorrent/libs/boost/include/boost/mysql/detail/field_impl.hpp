//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_FIELD_IMPL_HPP
#define BOOST_MYSQL_DETAIL_FIELD_IMPL_HPP

#include <boost/mysql/bad_field_access.hpp>
#include <boost/mysql/blob.hpp>
#include <boost/mysql/date.hpp>
#include <boost/mysql/datetime.hpp>
#include <boost/mysql/field_kind.hpp>
#include <boost/mysql/time.hpp>

#include <boost/mp11/algorithm.hpp>
#include <boost/throw_exception.hpp>
#include <boost/variant2/variant.hpp>

#include <string>
#include <type_traits>

namespace boost {
namespace mysql {
namespace detail {

// Breaks a circular dependency between field_view and field
struct field_impl
{
    using null_t = boost::variant2::monostate;

    using variant_type = boost::variant2::variant<
        null_t,         // Any of the below when the value is NULL
        std::int64_t,   // signed TINYINT, SMALLINT, MEDIUMINT, INT, BIGINT
        std::uint64_t,  // unsigned TINYINT, SMALLINT, MEDIUMINT, INT, BIGINT, YEAR, BIT
        std::string,    // CHAR, VARCHAR,  TEXT (all sizes), , ENUM,
                        // SET, DECIMAL
        blob,           // BINARY, VARBINARY, BLOB (all sizes), GEOMETRY
        float,          // FLOAT
        double,         // DOUBLE
        date,           // DATE
        datetime,       // DATETIME, TIMESTAMP
        time            // TIME
        >;

    variant_type data;

    field_impl() = default;

    template <typename... Args>
    field_impl(Args&&... args) noexcept(std::is_nothrow_constructible<variant_type, Args...>::value)
        : data(std::forward<Args>(args)...)
    {
    }

    field_kind kind() const noexcept { return static_cast<field_kind>(data.index()); }

    template <typename T>
    const T& as() const
    {
        const T* res = boost::variant2::get_if<T>(&data);
        if (!res)
            BOOST_THROW_EXCEPTION(bad_field_access());
        return *res;
    }

    template <typename T>
    T& as()
    {
        T* res = boost::variant2::get_if<T>(&data);
        if (!res)
            BOOST_THROW_EXCEPTION(bad_field_access());
        return *res;
    }

    template <typename T>
    const T& get() const noexcept
    {
        constexpr auto I = mp11::mp_find<variant_type, T>::value;
        return boost::variant2::unsafe_get<I>(data);
    }

    template <typename T>
    T& get() noexcept
    {
        constexpr auto I = mp11::mp_find<variant_type, T>::value;
        return boost::variant2::unsafe_get<I>(data);
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
