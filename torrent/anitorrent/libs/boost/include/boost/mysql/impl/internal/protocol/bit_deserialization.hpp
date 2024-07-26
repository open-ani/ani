//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_BIT_DESERIALIZATION_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_BIT_DESERIALIZATION_HPP

#include <boost/mysql/field_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/impl/internal/protocol/serialization.hpp>

#include <boost/endian/conversion.hpp>

#include <cstring>

namespace boost {
namespace mysql {
namespace detail {

// All BIT values come as binary values between 1 and 8 bytes length packed in string_lenenc's,
// for both the text and the binary protocols. As the text protocol already unpacks the
// string_lenenc layer, this function is in charge of just parsing the binary payload. The length of
// the BIT value depends on how the type was defined in the table (e.g. BIT(14) will send a 2 byte
// value; BIT(54) will send a 7 byte one). Values are sent as big-endian.
inline deserialize_errc deserialize_bit(string_view from, field_view& to) noexcept
{
    std::size_t num_bytes = from.size();
    if (num_bytes < 1 || num_bytes > 8)
    {
        return deserialize_errc::protocol_value_error;
    }
    unsigned char temp[8]{};
    unsigned char* dest = temp + sizeof(temp) - num_bytes;
    std::memcpy(dest, from.data(), num_bytes);
    to = field_view(endian::load_big_u64(temp));
    return deserialize_errc::ok;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif