//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_STATIC_RESULTS_IMPL_IPP
#define BOOST_MYSQL_IMPL_STATIC_RESULTS_IMPL_IPP

#pragma once

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/execution_processor/static_results_impl.hpp>
#include <boost/mysql/detail/row_impl.hpp>

#include <boost/mysql/impl/internal/protocol/protocol.hpp>

#ifdef BOOST_MYSQL_CXX14
void boost::mysql::detail::static_results_erased_impl::reset_impl() noexcept
{
    ext_.reset_fn()(ext_.rows());
    info_.clear();
    meta_.clear();
    resultset_index_ = 0;
}

boost::mysql::error_code boost::mysql::detail::static_results_erased_impl::on_head_ok_packet_impl(
    const ok_view& pack,
    diagnostics& diag
)
{
    add_resultset();
    auto err = on_ok_packet_impl(pack);
    if (err)
        return err;
    return meta_check(diag);
}

void boost::mysql::detail::static_results_erased_impl::on_num_meta_impl(std::size_t num_columns)
{
    auto& resultset_data = add_resultset();
    meta_.reserve(meta_.size() + num_columns);
    resultset_data.meta_size = num_columns;
}

boost::mysql::error_code boost::mysql::detail::static_results_erased_impl::on_meta_impl(
    const coldef_view& coldef,
    bool is_last,
    diagnostics& diag
)

{
    std::size_t meta_index = meta_.size() - current_resultset().meta_offset;

    // Store the new object
    meta_.push_back(create_meta(coldef));

    // Fill the pos map entry for this field, if any
    pos_map_add_field(current_pos_map(), current_name_table(), meta_index, coldef.name);

    return is_last ? meta_check(diag) : error_code();
}

boost::mysql::error_code boost::mysql::detail::static_results_erased_impl::on_row_impl(
    span<const std::uint8_t> msg,
    const output_ref&,
    std::vector<field_view>& fields
)

{
    auto meta = current_resultset_meta();

    // Allocate temporary storage
    fields.clear();
    span<field_view> storage = add_fields(fields, meta.size());

    // deserialize the row
    auto err = deserialize_row(encoding(), msg, meta, storage);
    if (err)
        return err;

    // parse it against the appropriate tuple element
    return ext_.parse_fn(resultset_index_ - 1)(current_pos_map(), storage, ext_.rows());
}

boost::mysql::error_code boost::mysql::detail::static_results_erased_impl::on_row_ok_packet_impl(
    const ok_view& pack
)
{
    return on_ok_packet_impl(pack);
}

boost::mysql::detail::static_per_resultset_data& boost::mysql::detail::static_results_erased_impl::
    add_resultset()
{
    ++resultset_index_;
    auto& resultset_data = current_resultset();
    resultset_data = static_per_resultset_data();
    resultset_data.meta_offset = meta_.size();
    resultset_data.info_offset = info_.size();
    pos_map_reset(current_pos_map());
    return resultset_data;
}

boost::mysql::error_code boost::mysql::detail::static_results_erased_impl::on_ok_packet_impl(
    const ok_view& pack
)
{
    auto& resultset_data = current_resultset();
    resultset_data.affected_rows = pack.affected_rows;
    resultset_data.last_insert_id = pack.last_insert_id;
    resultset_data.warnings = pack.warnings;
    resultset_data.info_size = pack.info.size();
    resultset_data.has_ok_packet_data = true;
    resultset_data.is_out_params = pack.is_out_params();
    info_.insert(info_.end(), pack.info.begin(), pack.info.end());
    bool should_be_last = resultset_index_ == ext_.num_resultsets();
    bool is_last = !pack.more_results();
    return should_be_last == is_last ? error_code() : client_errc::num_resultsets_mismatch;
}
#endif

#endif
