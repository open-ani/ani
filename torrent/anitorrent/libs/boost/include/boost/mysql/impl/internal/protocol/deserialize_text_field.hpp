//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_DESERIALIZE_TEXT_FIELD_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_DESERIALIZE_TEXT_FIELD_HPP

#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>

#include <boost/mysql/detail/config.hpp>

#include <boost/mysql/impl/internal/protocol/serialization.hpp>

namespace boost {
namespace mysql {
namespace detail {

BOOST_MYSQL_DECL
deserialize_errc deserialize_text_field(string_view from, const metadata& meta, field_view& output);

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/internal/protocol/deserialize_text_field.ipp>
#endif

#endif
