//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_EXECUTION_PROCESSOR_RESULTS_IMPL_HPP
#define BOOST_MYSQL_DETAIL_EXECUTION_PROCESSOR_RESULTS_IMPL_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>
#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/rows_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>
#include <boost/mysql/detail/execution_processor/execution_processor.hpp>
#include <boost/mysql/detail/row_impl.hpp>

#include <boost/assert.hpp>

namespace boost {
namespace mysql {
namespace detail {

struct per_resultset_data
{
    std::size_t num_columns{};       // Number of columns this resultset has
    std::size_t meta_offset{};       // Offset into the vector of metadata
    std::size_t field_offset;        // Offset into the vector of fields (append mode only)
    std::size_t num_rows{};          // Number of rows this resultset has (append mode only)
    std::uint64_t affected_rows{};   // OK packet data
    std::uint64_t last_insert_id{};  // OK packet data
    std::uint16_t warnings{};        // OK packet data
    std::size_t info_offset{};       // Offset into the vector of info characters
    std::size_t info_size{};         // Number of characters that this resultset's info string has
    bool has_ok_packet_data{false};  // The OK packet information is default constructed, or actual data?
    bool is_out_params{false};       // Does this resultset contain OUT param information?
};

// A container similar to a vector with SBO. To avoid depending on Boost.Container
class resultset_container
{
    bool first_has_data_{false};
    per_resultset_data first_;
    std::vector<per_resultset_data> rest_;

public:
    resultset_container() = default;
    std::size_t size() const noexcept { return !first_has_data_ ? 0 : rest_.size() + 1; }
    bool empty() const noexcept { return !first_has_data_; }
    void clear() noexcept
    {
        first_has_data_ = false;
        rest_.clear();
    }
    per_resultset_data& operator[](std::size_t i) noexcept
    {
        return const_cast<per_resultset_data&>(const_cast<const resultset_container&>(*this)[i]);
    }
    const per_resultset_data& operator[](std::size_t i) const noexcept
    {
        BOOST_ASSERT(i < size());
        return i == 0 ? first_ : rest_[i - 1];
    }
    per_resultset_data& back() noexcept
    {
        return const_cast<per_resultset_data&>(const_cast<const resultset_container&>(*this).back());
    }
    const per_resultset_data& back() const noexcept
    {
        BOOST_ASSERT(first_has_data_);
        return rest_.empty() ? first_ : rest_.back();
    }
    BOOST_MYSQL_DECL per_resultset_data& emplace_back();
};

// Rows for all resultsets are stored in a single rows_impl object.
// - When a row batch is started, we record how many fields we had before the batch.
// - When rows are read, fields are allocated in the rows_impl object, then deserialized against
//   the allocated storage. At this point, strings/blobs point into the connection read buffer.
// - When a row batch is finished, we copy strings/blobs into the rows_impl, then transform them
//   into offsets to allow rows_impl to grow.
// - When the final OK packet is received, offsets are transformed back into views.
class results_impl final : public execution_processor
{
public:
    results_impl() = default;

    BOOST_MYSQL_DECL
    row_view get_out_params() const noexcept;

    std::size_t num_resultsets() const noexcept { return per_result_.size(); }

    rows_view get_rows(std::size_t index) const noexcept
    {
        const auto& resultset_data = per_result_[index];
        return access::construct<rows_view>(
            rows_.fields().data() + resultset_data.field_offset,
            resultset_data.num_rows * resultset_data.num_columns,
            resultset_data.num_columns
        );
    }

    metadata_collection_view get_meta(std::size_t index) const noexcept
    {
        const auto& resultset_data = get_resultset(index);
        return metadata_collection_view(
            meta_.data() + resultset_data.meta_offset,
            resultset_data.num_columns
        );
    }

    std::uint64_t get_affected_rows(std::size_t index) const noexcept
    {
        return get_resultset(index).affected_rows;
    }

    std::uint64_t get_last_insert_id(std::size_t index) const noexcept
    {
        return get_resultset(index).last_insert_id;
    }

    unsigned get_warning_count(std::size_t index) const noexcept { return get_resultset(index).warnings; }

    string_view get_info(std::size_t index) const noexcept
    {
        const auto& resultset_data = get_resultset(index);
        return string_view(info_.data() + resultset_data.info_offset, resultset_data.info_size);
    }

    bool get_is_out_params(std::size_t index) const noexcept { return get_resultset(index).is_out_params; }

    results_impl& get_interface() noexcept { return *this; }

private:
    // Virtual impls
    BOOST_MYSQL_DECL
    void reset_impl() noexcept override final;

    BOOST_MYSQL_DECL
    void on_num_meta_impl(std::size_t num_columns) override final;

    BOOST_MYSQL_DECL
    error_code on_head_ok_packet_impl(const ok_view& pack, diagnostics&) override final;

    BOOST_MYSQL_DECL
    error_code on_meta_impl(const coldef_view&, bool, diagnostics&) override final;

    BOOST_MYSQL_DECL
    error_code on_row_impl(span<const std::uint8_t> msg, const output_ref&, std::vector<field_view>&)
        override final;

    BOOST_MYSQL_DECL
    error_code on_row_ok_packet_impl(const ok_view& pack) override final;

    BOOST_MYSQL_DECL
    void on_row_batch_start_impl() override final;

    BOOST_MYSQL_DECL
    void on_row_batch_finish_impl() override final;

    // Data
    std::vector<metadata> meta_;
    resultset_container per_result_;
    std::vector<char> info_;
    row_impl rows_;
    std::size_t num_fields_at_batch_start_{no_batch};

    // Auxiliar
    static constexpr std::size_t no_batch = std::size_t(-1);

    bool has_active_batch() const noexcept { return num_fields_at_batch_start_ != no_batch; }

    BOOST_MYSQL_DECL
    void finish_batch();

    per_resultset_data& current_resultset() noexcept
    {
        BOOST_ASSERT(!per_result_.empty());
        return per_result_.back();
    }

    const per_resultset_data& current_resultset() const noexcept
    {
        BOOST_ASSERT(!per_result_.empty());
        return per_result_.back();
    }

    BOOST_MYSQL_DECL
    per_resultset_data& add_resultset();

    BOOST_MYSQL_DECL
    void on_ok_packet_impl(const ok_view& pack);

    const per_resultset_data& get_resultset(std::size_t index) const noexcept
    {
        BOOST_ASSERT(index < per_result_.size());
        return per_result_[index];
    }

    metadata_collection_view current_resultset_meta() const noexcept
    {
        return get_meta(per_result_.size() - 1);
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/results_impl.ipp>
#endif

#endif
