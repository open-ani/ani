//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_ESCAPE_STRING_HPP
#define BOOST_MYSQL_DETAIL_ESCAPE_STRING_HPP

#include <boost/mysql/error_code.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/output_string.hpp>

#include <boost/config.hpp>

namespace boost {
namespace mysql {

// Forward decls
struct format_options;

namespace detail {

BOOST_ATTRIBUTE_NODISCARD BOOST_MYSQL_DECL error_code
escape_string(string_view input, const format_options& opts, char quote_char, output_string_ref output);

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/escape_string.ipp>
#endif

#endif
