//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_EXECUTION_PROCESSOR_STATIC_EXECUTION_STATE_IMPL_HPP
#define BOOST_MYSQL_DETAIL_EXECUTION_PROCESSOR_STATIC_EXECUTION_STATE_IMPL_HPP

#include <boost/mysql/detail/config.hpp>

#ifdef BOOST_MYSQL_CXX14

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>
#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/execution_processor/execution_processor.hpp>
#include <boost/mysql/detail/typing/get_type_index.hpp>
#include <boost/mysql/detail/typing/row_traits.hpp>

#include <boost/assert.hpp>

#include <array>
#include <cstddef>
#include <vector>

namespace boost {
namespace mysql {
namespace detail {

using execst_parse_fn_t =
    error_code (*)(span<const std::size_t> pos_map, span<const field_view> from, const output_ref& ref);

struct execst_resultset_descriptor
{
    std::size_t num_columns;
    name_table_t name_table;
    meta_check_fn_t meta_check;
    execst_parse_fn_t parse_fn;
    std::size_t type_index;
};

class execst_external_data
{
public:
    struct ptr_data
    {
        std::size_t* pos_map;
    };

    execst_external_data(span<const execst_resultset_descriptor> desc, ptr_data ptr) noexcept
        : desc_(desc), ptr_(ptr)
    {
    }

    std::size_t num_resultsets() const noexcept { return desc_.size(); }
    std::size_t num_columns(std::size_t idx) const noexcept
    {
        BOOST_ASSERT(idx < num_resultsets());
        return desc_[idx].num_columns;
    }
    name_table_t name_table(std::size_t idx) const noexcept
    {
        BOOST_ASSERT(idx < num_resultsets());
        return desc_[idx].name_table;
    }
    meta_check_fn_t meta_check_fn(std::size_t idx) const noexcept
    {
        BOOST_ASSERT(idx < num_resultsets());
        return desc_[idx].meta_check;
    }
    execst_parse_fn_t parse_fn(std::size_t idx) const noexcept
    {
        BOOST_ASSERT(idx < num_resultsets());
        return desc_[idx].parse_fn;
    }
    std::size_t type_index(std::size_t idx) const noexcept
    {
        BOOST_ASSERT(idx < num_resultsets());
        return desc_[idx].type_index;
    }
    span<std::size_t> pos_map(std::size_t idx) const noexcept
    {
        return span<std::size_t>(ptr_.pos_map, num_columns(idx));
    }

    void set_pointers(ptr_data ptr) noexcept { ptr_ = ptr; }

private:
    span<const execst_resultset_descriptor> desc_;
    ptr_data ptr_;
};

class static_execution_state_erased_impl final : public execution_processor
{
public:
    static_execution_state_erased_impl(execst_external_data ext) noexcept : ext_(ext) {}

    execst_external_data& ext_data() noexcept { return ext_; }

    metadata_collection_view meta() const noexcept { return meta_; }

    std::uint64_t get_affected_rows() const noexcept
    {
        BOOST_ASSERT(ok_data_.has_value);
        return ok_data_.affected_rows;
    }

    std::uint64_t get_last_insert_id() const noexcept
    {
        BOOST_ASSERT(ok_data_.has_value);
        return ok_data_.last_insert_id;
    }

    unsigned get_warning_count() const noexcept
    {
        BOOST_ASSERT(ok_data_.has_value);
        return ok_data_.warnings;
    }

    string_view get_info() const noexcept
    {
        BOOST_ASSERT(ok_data_.has_value);
        return string_view(info_.data(), info_.size());
    }

    bool get_is_out_params() const noexcept
    {
        BOOST_ASSERT(ok_data_.has_value);
        return ok_data_.is_out_params;
    }

private:
    // Data
    struct ok_packet_data
    {
        bool has_value{false};           // The OK packet information is default constructed, or actual data?
        std::uint64_t affected_rows{};   // OK packet data
        std::uint64_t last_insert_id{};  // OK packet data
        std::uint16_t warnings{};        // OK packet data
        bool is_out_params{false};       // Does this resultset contain OUT param information?
    };

    execst_external_data ext_;
    std::size_t resultset_index_{};
    ok_packet_data ok_data_;
    std::vector<char> info_;
    std::vector<metadata> meta_;

    // Virtual impls
    BOOST_MYSQL_DECL
    void reset_impl() noexcept override final;

    BOOST_MYSQL_DECL
    error_code on_head_ok_packet_impl(const ok_view& pack, diagnostics& diag) override final;

    BOOST_MYSQL_DECL
    void on_num_meta_impl(std::size_t num_columns) override final;

    BOOST_MYSQL_DECL
    error_code on_meta_impl(const coldef_view& coldef, bool is_last, diagnostics& diag) override final;

    BOOST_MYSQL_DECL
    error_code on_row_impl(
        span<const std::uint8_t> msg,
        const output_ref& ref,
        std::vector<field_view>& fields
    ) override final;

    BOOST_MYSQL_DECL
    error_code on_row_ok_packet_impl(const ok_view& pack) override final;

    void on_row_batch_start_impl() noexcept override final {}

    void on_row_batch_finish_impl() noexcept override final {}

    // Auxiliar
    name_table_t current_name_table() const noexcept { return ext_.name_table(resultset_index_ - 1); }
    span<std::size_t> current_pos_map() noexcept { return ext_.pos_map(resultset_index_ - 1); }
    span<const std::size_t> current_pos_map() const noexcept { return ext_.pos_map(resultset_index_ - 1); }

    error_code meta_check(diagnostics& diag) const
    {
        return ext_.meta_check_fn(resultset_index_ - 1)(current_pos_map(), meta_, diag);
    }

    BOOST_MYSQL_DECL
    void on_new_resultset() noexcept;

    BOOST_MYSQL_DECL
    error_code on_ok_packet_impl(const ok_view& pack);
};

template <class StaticRow>
static error_code execst_parse_fn(
    span<const std::size_t> pos_map,
    span<const field_view> from,
    const output_ref& ref
)
{
    return parse(pos_map, from, ref.span_element<StaticRow>());
}

template <class... StaticRow>
constexpr std::array<execst_resultset_descriptor, sizeof...(StaticRow)> create_execst_resultset_descriptors()
{
    return {{{
        get_row_size<StaticRow>(),
        get_row_name_table<StaticRow>(),
        &meta_check<StaticRow>,
        &execst_parse_fn<StaticRow>,
        get_type_index<StaticRow, StaticRow...>(),
    }...}};
}

template <class... StaticRow>
constexpr std::array<execst_resultset_descriptor, sizeof...(StaticRow)>
    execst_resultset_descriptor_table = create_execst_resultset_descriptors<StaticRow...>();

template <BOOST_MYSQL_STATIC_ROW... StaticRow>
class static_execution_state_impl
{
    // Storage for our data, which requires knowing the template args
    struct
    {
        std::array<std::size_t, max_num_columns<StaticRow...>> pos_map{};
    } data_;

    // The type-erased impl, that will use pointers to the above storage
    static_execution_state_erased_impl impl_;

    execst_external_data::ptr_data ptr_data() noexcept
    {
        return {
            data_.pos_map.data(),
        };
    }

    void set_pointers() noexcept { impl_.ext_data().set_pointers(ptr_data()); }

public:
    static_execution_state_impl() noexcept
        : impl_({execst_resultset_descriptor_table<StaticRow...>, ptr_data()})
    {
    }

    static_execution_state_impl(const static_execution_state_impl& rhs) : data_(rhs.data_), impl_(rhs.impl_)
    {
        set_pointers();
    }

    static_execution_state_impl(static_execution_state_impl&& rhs) noexcept
        : data_(std::move(rhs.data_)), impl_(std::move(rhs.impl_))
    {
        set_pointers();
    }

    static_execution_state_impl& operator=(const static_execution_state_impl& rhs)
    {
        data_ = rhs.data_;
        impl_ = rhs.impl_;
        set_pointers();
        return *this;
    }

    static_execution_state_impl& operator=(static_execution_state_impl&& rhs)
    {
        data_ = std::move(rhs.data_);
        impl_ = std::move(rhs.impl_);
        set_pointers();
        return *this;
    }

    ~static_execution_state_impl() = default;

    const static_execution_state_erased_impl& get_interface() const noexcept { return impl_; }
    static_execution_state_erased_impl& get_interface() noexcept { return impl_; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/static_execution_state_impl.ipp>
#endif

#endif  // BOOST_MYSQL_CXX14

#endif
