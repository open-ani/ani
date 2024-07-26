//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_RESULTS_IMPL_IPP
#define BOOST_MYSQL_IMPL_RESULTS_IMPL_IPP

#pragma once

#include <boost/mysql/detail/execution_processor/results_impl.hpp>

#include <boost/mysql/impl/internal/protocol/protocol.hpp>

boost::mysql::detail::per_resultset_data& boost::mysql::detail::resultset_container::emplace_back()
{
    if (!first_has_data_)
    {
        first_ = per_resultset_data();
        first_has_data_ = true;
        return first_;
    }
    else
    {
        rest_.emplace_back();
        return rest_.back();
    }
}

boost::mysql::row_view boost::mysql::detail::results_impl::get_out_params() const noexcept
{
    BOOST_ASSERT(is_complete());
    for (std::size_t i = 0; i < per_result_.size(); ++i)
    {
        if (per_result_[i].is_out_params)
        {
            auto res = get_rows(i);
            return res.empty() ? row_view() : res[0];
        }
    }
    return row_view();
}

void boost::mysql::detail::results_impl::reset_impl() noexcept
{
    meta_.clear();
    per_result_.clear();
    info_.clear();
    rows_.clear();
    num_fields_at_batch_start_ = no_batch;
}

void boost::mysql::detail::results_impl::on_num_meta_impl(std::size_t num_columns)
{
    auto& resultset_data = add_resultset();
    meta_.reserve(meta_.size() + num_columns);
    resultset_data.num_columns = num_columns;
}

boost::mysql::error_code boost::mysql::detail::results_impl::
    on_head_ok_packet_impl(const ok_view& pack, diagnostics&)
{
    add_resultset();
    on_ok_packet_impl(pack);
    return error_code();
}

boost::mysql::error_code boost::mysql::detail::results_impl::
    on_meta_impl(const coldef_view& coldef, bool, diagnostics&)
{
    meta_.push_back(create_meta(coldef));
    return error_code();
}

boost::mysql::error_code boost::mysql::detail::results_impl::
    on_row_impl(span<const std::uint8_t> msg, const output_ref&, std::vector<field_view>&)
{
    BOOST_ASSERT(has_active_batch());

    // add row storage
    std::size_t num_fields = current_resultset().num_columns;
    span<field_view> storage = rows_.add_fields(num_fields);
    ++current_resultset().num_rows;

    // deserialize the row
    auto err = deserialize_row(encoding(), msg, current_resultset_meta(), storage);
    if (err)
        return err;

    return error_code();
}

boost::mysql::error_code boost::mysql::detail::results_impl::on_row_ok_packet_impl(const ok_view& pack)
{
    on_ok_packet_impl(pack);
    return error_code();
}

void boost::mysql::detail::results_impl::on_row_batch_start_impl()
{
    BOOST_ASSERT(!has_active_batch());
    num_fields_at_batch_start_ = rows_.fields().size();
}

void boost::mysql::detail::results_impl::on_row_batch_finish_impl() { finish_batch(); }

void boost::mysql::detail::results_impl::finish_batch()
{
    if (has_active_batch())
    {
        rows_.copy_strings_as_offsets(
            num_fields_at_batch_start_,
            rows_.fields().size() - num_fields_at_batch_start_
        );
        num_fields_at_batch_start_ = no_batch;
    }
}

boost::mysql::detail::per_resultset_data& boost::mysql::detail::results_impl::add_resultset()
{
    // Allocate a new per-resultset object
    auto& resultset_data = per_result_.emplace_back();
    resultset_data.meta_offset = meta_.size();
    resultset_data.field_offset = rows_.fields().size();
    resultset_data.info_offset = info_.size();
    return resultset_data;
}

void boost::mysql::detail::results_impl::on_ok_packet_impl(const ok_view& pack)
{
    auto& resultset_data = current_resultset();
    resultset_data.affected_rows = pack.affected_rows;
    resultset_data.last_insert_id = pack.last_insert_id;
    resultset_data.warnings = pack.warnings;
    resultset_data.info_size = pack.info.size();
    resultset_data.has_ok_packet_data = true;
    resultset_data.is_out_params = pack.is_out_params();
    info_.insert(info_.end(), pack.info.begin(), pack.info.end());
    if (!pack.more_results())
    {
        finish_batch();
        rows_.offsets_to_string_views();
    }
}

#endif
