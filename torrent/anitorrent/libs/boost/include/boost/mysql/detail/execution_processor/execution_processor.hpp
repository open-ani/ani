//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_EXECUTION_PROCESSOR_EXECUTION_PROCESSOR_HPP
#define BOOST_MYSQL_DETAIL_EXECUTION_PROCESSOR_EXECUTION_PROCESSOR_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>
#include <boost/mysql/metadata_mode.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/access.hpp>
#include <boost/mysql/detail/coldef_view.hpp>
#include <boost/mysql/detail/ok_view.hpp>
#include <boost/mysql/detail/resultset_encoding.hpp>

#include <boost/assert.hpp>
#include <boost/config.hpp>
#include <boost/core/span.hpp>

#include <cstddef>
#include <cstdint>
#include <limits>

namespace boost {
namespace mysql {
namespace detail {

// A type-erased reference to be used as the output range for static_execution_state
class output_ref
{
    // Pointer to the first element of the span
    void* data_{};

    // Number of elements in the span
    std::size_t max_size_{(std::numeric_limits<std::size_t>::max)()};

    // Identifier for the type of elements. Index in the resultset type list
    std::size_t type_index_{};

    // Offset into the span's data (static_execution_state). Otherwise unused
    std::size_t offset_{};

public:
    constexpr output_ref() noexcept = default;

    template <class T>
    constexpr output_ref(boost::span<T> span, std::size_t type_index, std::size_t offset = 0) noexcept
        : data_(span.data()), max_size_(span.size()), type_index_(type_index), offset_(offset)
    {
    }

    std::size_t max_size() const noexcept { return max_size_; }
    std::size_t type_index() const noexcept { return type_index_; }
    std::size_t offset() const noexcept { return offset_; }
    void set_offset(std::size_t v) noexcept { offset_ = v; }

    template <class T>
    T& span_element() const noexcept
    {
        BOOST_ASSERT(data_);
        return static_cast<T*>(data_)[offset_];
    }
};

class execution_processor
{
public:
    virtual ~execution_processor() {}

    void reset(resultset_encoding enc, metadata_mode mode) noexcept
    {
        state_ = state_t::reading_first;
        encoding_ = enc;
        mode_ = mode;
        seqnum_ = 0;
        remaining_meta_ = 0;
        reset_impl();
    }

    BOOST_ATTRIBUTE_NODISCARD
    error_code on_head_ok_packet(const ok_view& pack, diagnostics& diag)
    {
        BOOST_ASSERT(is_reading_head());
        auto err = on_head_ok_packet_impl(pack, diag);
        set_state_for_ok(pack);
        return err;
    }

    void on_num_meta(std::size_t num_columns)
    {
        BOOST_ASSERT(is_reading_head());
        on_num_meta_impl(num_columns);
        remaining_meta_ = num_columns;
        set_state(state_t::reading_metadata);
    }

    BOOST_ATTRIBUTE_NODISCARD
    error_code on_meta(const coldef_view& pack, diagnostics& diag)
    {
        BOOST_ASSERT(is_reading_meta());
        bool is_last = --remaining_meta_ == 0;
        auto err = on_meta_impl(pack, is_last, diag);
        if (is_last)
            set_state(state_t::reading_rows);
        return err;
    }

    void on_row_batch_start()
    {
        BOOST_ASSERT(is_reading_rows());
        on_row_batch_start_impl();
    }

    void on_row_batch_finish() { on_row_batch_finish_impl(); }

    BOOST_ATTRIBUTE_NODISCARD
    error_code on_row(span<const std::uint8_t> msg, const output_ref& ref, std::vector<field_view>& storage)
    {
        BOOST_ASSERT(is_reading_rows());
        return on_row_impl(msg, ref, storage);
    }

    BOOST_ATTRIBUTE_NODISCARD
    error_code on_row_ok_packet(const ok_view& pack)
    {
        BOOST_ASSERT(is_reading_rows());
        auto err = on_row_ok_packet_impl(pack);
        set_state_for_ok(pack);
        return err;
    }

    bool is_reading_first() const noexcept { return state_ == state_t::reading_first; }
    bool is_reading_first_subseq() const noexcept { return state_ == state_t::reading_first_subseq; }
    bool is_reading_head() const noexcept
    {
        return state_ == state_t::reading_first || state_ == state_t::reading_first_subseq;
    }
    bool is_reading_meta() const noexcept { return state_ == state_t::reading_metadata; }
    bool is_reading_rows() const noexcept { return state_ == state_t::reading_rows; }
    bool is_complete() const noexcept { return state_ == state_t::complete; }

    resultset_encoding encoding() const noexcept { return encoding_; }
    std::uint8_t& sequence_number() noexcept { return seqnum_; }
    metadata_mode meta_mode() const noexcept { return mode_; }

protected:
    virtual void reset_impl() noexcept = 0;
    virtual error_code on_head_ok_packet_impl(const ok_view& pack, diagnostics& diag) = 0;
    virtual void on_num_meta_impl(std::size_t num_columns) = 0;
    virtual error_code on_meta_impl(const coldef_view& coldef, bool is_last, diagnostics& diag) = 0;
    virtual error_code on_row_ok_packet_impl(const ok_view& pack) = 0;
    virtual error_code on_row_impl(
        span<const std::uint8_t> msg,
        const output_ref& ref,
        std::vector<field_view>& storage
    ) = 0;
    virtual void on_row_batch_start_impl() = 0;
    virtual void on_row_batch_finish_impl() = 0;

    metadata create_meta(const coldef_view& coldef) const
    {
        return access::construct<metadata>(coldef, mode_ == metadata_mode::full);
    }

private:
    enum class state_t
    {
        // waiting for 1st packet, for the 1st resultset
        reading_first,

        // same, but for subsequent resultsets (distiguised to provide a cleaner xp to
        // the user in (static_)execution_state)
        reading_first_subseq,

        // waiting for metadata packets
        reading_metadata,

        // waiting for rows
        reading_rows,

        // done
        complete
    };

    state_t state_{state_t::reading_first};
    resultset_encoding encoding_{resultset_encoding::text};
    std::uint8_t seqnum_{};
    metadata_mode mode_{metadata_mode::minimal};
    std::size_t remaining_meta_{};

    void set_state(state_t v) noexcept { state_ = v; }

    void set_state_for_ok(const ok_view& pack) noexcept
    {
        if (pack.more_results())
        {
            set_state(state_t::reading_first_subseq);
        }
        else
        {
            set_state(state_t::complete);
        }
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
