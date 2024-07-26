//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_META_CHECK_CONTEXT_IPP
#define BOOST_MYSQL_IMPL_META_CHECK_CONTEXT_IPP

#pragma once

#include <boost/mysql/detail/typing/meta_check_context.hpp>

void boost::mysql::detail::meta_check_context::add_field_absent_error()
{
    auto& stream = add_error();
    stream << "Field ";
    insert_field_name(stream);
    if (has_field_names(name_table_))
    {
        stream << " is not present in the data returned by the server";
    }
    else
    {
        stream << " can't be mapped: there are more fields in your C++ data type than in your query";
    }
}

void boost::mysql::detail::meta_check_context::add_type_mismatch_error(const char* cpp_type_name)
{
    auto& stream = add_error();
    stream << "Incompatible types for field ";
    insert_field_name(stream);
    stream << ": C++ type '" << cpp_type_name << "' is not compatible with DB type '"
           << column_type_to_str(current_meta()) << "'";
}

void boost::mysql::detail::meta_check_context::add_nullability_error()
{
    auto& stream = add_error();
    stream << "NULL checks failed for field ";
    insert_field_name(stream);
    stream << ": the database type may be NULL, but the C++ type cannot. Use std::optional<T> or "
              "boost::optional<T>";
}

boost::mysql::error_code boost::mysql::detail::meta_check_context::check_errors(diagnostics& diag) const
{
    if (errors_ != nullptr)
    {
        access::get_impl(diag).assign_client(errors_->str());
        return client_errc::metadata_check_failed;
    }
    return error_code();
}

#endif
