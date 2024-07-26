//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_ROW_IMPL_HPP
#define BOOST_MYSQL_DETAIL_ROW_IMPL_HPP

#include <boost/mysql/field_view.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/core/span.hpp>

#include <cstddef>
#include <vector>

namespace boost {
namespace mysql {
namespace detail {

// Adds num_fields default-constructed fields to the vector, return pointer to the first
// allocated value. Used to allocate fields before deserialization
inline span<field_view> add_fields(std::vector<field_view>& storage, std::size_t num_fields)
{
    std::size_t old_size = storage.size();
    storage.resize(old_size + num_fields);
    return span<field_view>(storage.data() + old_size, num_fields);
}

// A field_view vector with strings pointing into a
// single character buffer. Used to implement owning row types
class row_impl
{
public:
    row_impl() = default;

    BOOST_MYSQL_DECL
    row_impl(const row_impl&);

    row_impl(row_impl&&) = default;

    BOOST_MYSQL_DECL
    row_impl& operator=(const row_impl&);

    row_impl& operator=(row_impl&&) = default;

    ~row_impl() = default;

    // Copies the given span into *this
    BOOST_MYSQL_DECL
    row_impl(const field_view* fields, std::size_t size);

    // Copies the given span into *this, used by row/rows in assignment from view
    BOOST_MYSQL_DECL
    void assign(const field_view* fields, std::size_t size);

    // Adds new default constructed fields to provide storage to deserialization
    span<field_view> add_fields(std::size_t num_fields)
    {
        return ::boost::mysql::detail::add_fields(fields_, num_fields);
    }

    // Saves strings in the [first, first+num_fields) range into the string buffer, used by execute
    BOOST_MYSQL_DECL
    void copy_strings_as_offsets(std::size_t first, std::size_t num_fields);

    // Restores any offsets into string views, used by execute
    BOOST_MYSQL_DECL
    void offsets_to_string_views();

    const std::vector<field_view>& fields() const noexcept { return fields_; }

    void clear() noexcept
    {
        fields_.clear();
        string_buffer_.clear();
    }

private:
    std::vector<field_view> fields_;
    std::vector<unsigned char> string_buffer_;
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/row_impl.ipp>
#endif

#endif
