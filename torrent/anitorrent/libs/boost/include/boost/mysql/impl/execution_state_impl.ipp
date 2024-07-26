//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_EXECUTION_STATE_IMPL_IPP
#define BOOST_MYSQL_IMPL_EXECUTION_STATE_IMPL_IPP

#pragma once

#include <boost/mysql/detail/execution_processor/execution_state_impl.hpp>
#include <boost/mysql/detail/row_impl.hpp>

#include <boost/mysql/impl/internal/protocol/protocol.hpp>

void boost::mysql::detail::execution_state_impl::on_ok_packet_impl(const ok_view& pack)
{
    eof_data_.has_value = true;
    eof_data_.affected_rows = pack.affected_rows;
    eof_data_.last_insert_id = pack.last_insert_id;
    eof_data_.warnings = pack.warnings;
    eof_data_.is_out_params = pack.is_out_params();
    info_.assign(pack.info.begin(), pack.info.end());
}

void boost::mysql::detail::execution_state_impl::reset_impl() noexcept
{
    meta_.clear();
    eof_data_ = ok_data();
    info_.clear();
}

boost::mysql::error_code boost::mysql::detail::execution_state_impl::
    on_head_ok_packet_impl(const ok_view& pack, diagnostics&)
{
    on_new_resultset();
    on_ok_packet_impl(pack);
    return error_code();
}

void boost::mysql::detail::execution_state_impl::on_num_meta_impl(std::size_t num_columns)
{
    on_new_resultset();
    meta_.reserve(num_columns);
}

boost::mysql::error_code boost::mysql::detail::execution_state_impl::
    on_meta_impl(const coldef_view& coldef, bool, diagnostics&)
{
    meta_.push_back(create_meta(coldef));
    return error_code();
}

boost::mysql::error_code boost::mysql::detail::execution_state_impl::on_row_impl(
    span<const std::uint8_t> msg,
    const output_ref&,
    std::vector<field_view>& fields
)

{
    // add row storage
    span<field_view> storage = add_fields(fields, meta_.size());

    // deserialize the row
    return deserialize_row(encoding(), msg, meta_, storage);
}

boost::mysql::error_code boost::mysql::detail::execution_state_impl::on_row_ok_packet_impl(const ok_view& pack
)
{
    on_ok_packet_impl(pack);
    return error_code();
}

#endif
