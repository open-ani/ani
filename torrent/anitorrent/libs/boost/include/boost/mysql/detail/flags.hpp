//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_FLAGS_HPP
#define BOOST_MYSQL_DETAIL_FLAGS_HPP

#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

namespace column_flags {

constexpr std::uint16_t not_null = 1;             // Field can't be NULL.
constexpr std::uint16_t pri_key = 2;              // Field is part of a primary key.
constexpr std::uint16_t unique_key = 4;           // Field is part of a unique key.
constexpr std::uint16_t multiple_key = 8;         // Field is part of a key.
constexpr std::uint16_t blob = 16;                // Field is a blob.
constexpr std::uint16_t unsigned_ = 32;           // Field is unsigned.
constexpr std::uint16_t zerofill = 64;            // Field is zerofill.
constexpr std::uint16_t binary = 128;             // Field is binary.
constexpr std::uint16_t enum_ = 256;              // field is an enum
constexpr std::uint16_t auto_increment = 512;     // field is a autoincrement field
constexpr std::uint16_t timestamp = 1024;         // Field is a timestamp.
constexpr std::uint16_t set = 2048;               // field is a set
constexpr std::uint16_t no_default_value = 4096;  // Field doesn't have default value.
constexpr std::uint16_t on_update_now = 8192;     // Field is set to NOW on UPDATE.
constexpr std::uint16_t part_key = 16384;         // Intern; Part of some key.
constexpr std::uint16_t num = 32768;              // Field is num (for clients)

}  // namespace column_flags

namespace status_flags {

constexpr std::uint32_t more_results = 8;
constexpr std::uint32_t no_backslash_escapes = 512;
constexpr std::uint32_t out_params = 4096;

}  // namespace status_flags

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
