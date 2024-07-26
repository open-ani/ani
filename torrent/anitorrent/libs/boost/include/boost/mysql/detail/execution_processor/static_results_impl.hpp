//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_EXECUTION_PROCESSOR_STATIC_RESULTS_IMPL_HPP
#define BOOST_MYSQL_DETAIL_EXECUTION_PROCESSOR_STATIC_RESULTS_IMPL_HPP

#include <boost/mysql/detail/config.hpp>

#ifdef BOOST_MYSQL_CXX14

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/metadata.hpp>
#include <boost/mysql/metadata_collection_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/execution_processor/execution_processor.hpp>
#include <boost/mysql/detail/typing/readable_field_traits.hpp>
#include <boost/mysql/detail/typing/row_traits.hpp>

#include <boost/assert.hpp>
#include <boost/mp11/algorithm.hpp>
#include <boost/mp11/integer_sequence.hpp>

#include <array>
#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

using results_reset_fn_t = void (*)(void*);
using results_parse_fn_t =
    error_code (*)(span<const std::size_t> pos_map, span<const field_view> from, void* to);

struct results_resultset_descriptor
{
    std::size_t num_columns;
    name_table_t name_table;
    meta_check_fn_t meta_check;
    results_parse_fn_t parse_fn;
};

struct static_per_resultset_data
{
    std::size_t meta_offset{};
    std::size_t meta_size{};
    std::size_t info_offset{};
    std::size_t info_size{};
    bool has_ok_packet_data{false};  // The OK packet information is default constructed, or actual data?
    std::uint64_t affected_rows{};   // OK packet data
    std::uint64_t last_insert_id{};  // OK packet data
    std::uint16_t warnings{};        // OK packet data
    bool is_out_params{false};       // Does this resultset contain OUT param information?
};

class results_external_data
{
public:
    struct ptr_data
    {
        void* rows;
        std::size_t* pos_map;
        static_per_resultset_data* per_resultset;
    };

    results_external_data(
        span<const results_resultset_descriptor> desc,
        results_reset_fn_t reset,
        ptr_data ptr
    ) noexcept
        : desc_(desc), reset_(reset), ptr_(ptr)
    {
    }

    void set_pointers(ptr_data ptr) noexcept { ptr_ = ptr; }

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
    results_parse_fn_t parse_fn(std::size_t idx) const noexcept
    {
        BOOST_ASSERT(idx < num_resultsets());
        return desc_[idx].parse_fn;
    }
    results_reset_fn_t reset_fn() const noexcept { return reset_; }
    void* rows() const noexcept { return ptr_.rows; }
    span<std::size_t> pos_map(std::size_t idx) const noexcept
    {
        return span<std::size_t>(ptr_.pos_map, num_columns(idx));
    }
    static_per_resultset_data& per_result(std::size_t idx) const noexcept
    {
        BOOST_ASSERT(idx < num_resultsets());
        return ptr_.per_resultset[idx];
    }

private:
    span<const results_resultset_descriptor> desc_;
    results_reset_fn_t reset_;
    ptr_data ptr_;
};

class static_results_erased_impl final : public execution_processor
{
public:
    static_results_erased_impl(results_external_data ext) noexcept : ext_(ext) {}

    results_external_data& ext_data() noexcept { return ext_; }

    metadata_collection_view get_meta(std::size_t index) const noexcept
    {
        const auto& resultset_data = ext_.per_result(index);
        return metadata_collection_view(meta_.data() + resultset_data.meta_offset, resultset_data.meta_size);
    }

    std::uint64_t get_affected_rows(std::size_t index) const noexcept
    {
        return ext_.per_result(index).affected_rows;
    }

    std::uint64_t get_last_insert_id(std::size_t index) const noexcept
    {
        return ext_.per_result(index).last_insert_id;
    }

    unsigned get_warning_count(std::size_t index) const noexcept { return ext_.per_result(index).warnings; }

    string_view get_info(std::size_t index) const noexcept
    {
        const auto& resultset_data = ext_.per_result(index);
        return string_view(info_.data() + resultset_data.info_offset, resultset_data.info_size);
    }

    bool get_is_out_params(std::size_t index) const noexcept { return ext_.per_result(index).is_out_params; }

private:
    // Virtual implementations
    BOOST_MYSQL_DECL
    void reset_impl() noexcept override final;

    BOOST_MYSQL_DECL
    error_code on_head_ok_packet_impl(const ok_view& pack, diagnostics& diag) override final;

    BOOST_MYSQL_DECL
    void on_num_meta_impl(std::size_t num_columns) override final;

    BOOST_MYSQL_DECL
    error_code on_meta_impl(const coldef_view& coldef, bool is_last, diagnostics& diag) override final;

    BOOST_MYSQL_DECL
    error_code on_row_impl(span<const std::uint8_t> msg, const output_ref&, std::vector<field_view>& fields)
        override final;

    BOOST_MYSQL_DECL
    error_code on_row_ok_packet_impl(const ok_view& pack) override final;

    void on_row_batch_start_impl() override final {}
    void on_row_batch_finish_impl() override final {}

    // Data
    results_external_data ext_;
    std::vector<metadata> meta_;
    std::vector<char> info_;
    std::size_t resultset_index_{0};

    // Helpers
    span<std::size_t> current_pos_map() noexcept { return ext_.pos_map(resultset_index_ - 1); }
    span<const std::size_t> current_pos_map() const noexcept { return ext_.pos_map(resultset_index_ - 1); }
    name_table_t current_name_table() const noexcept { return ext_.name_table(resultset_index_ - 1); }
    static_per_resultset_data& current_resultset() noexcept { return ext_.per_result(resultset_index_ - 1); }
    metadata_collection_view current_resultset_meta() const noexcept
    {
        return get_meta(resultset_index_ - 1);
    }

    BOOST_MYSQL_DECL
    static_per_resultset_data& add_resultset();

    BOOST_MYSQL_DECL
    error_code on_ok_packet_impl(const ok_view& pack);

    error_code meta_check(diagnostics& diag) const
    {
        return ext_.meta_check_fn(resultset_index_ - 1)(current_pos_map(), current_resultset_meta(), diag);
    }
};

template <class... StaticRow>
using results_rows_t = std::tuple<std::vector<StaticRow>...>;

template <class... StaticRow>
struct results_fns
{
    using rows_t = results_rows_t<StaticRow...>;

    struct reset_fn
    {
        rows_t& obj;

        template <std::size_t I>
        void operator()(boost::mp11::mp_size_t<I>) const noexcept
        {
            std::get<I>(obj).clear();
        }
    };

    static void reset(void* rows_ptr) noexcept
    {
        auto& rows = *static_cast<rows_t*>(rows_ptr);
        boost::mp11::mp_for_each<boost::mp11::mp_iota_c<sizeof...(StaticRow)>>(reset_fn{rows});
    }

    template <std::size_t I>
    static error_code do_parse(span<const std::size_t> pos_map, span<const field_view> from, void* to)
    {
        auto& v = std::get<I>(*static_cast<rows_t*>(to));
        v.emplace_back();
        return parse(pos_map, from, v.back());
    }

    template <std::size_t I>
    static constexpr results_resultset_descriptor create_descriptor()
    {
        using T = mp11::mp_at_c<mp11::mp_list<StaticRow...>, I>;
        return {
            get_row_size<T>(),
            get_row_name_table<T>(),
            &meta_check<T>,
            &do_parse<I>,
        };
    }

    template <std::size_t... I>
    static constexpr std::array<results_resultset_descriptor, sizeof...(StaticRow)> create_descriptors(mp11::index_sequence<
                                                                                                       I...>)
    {
        return {{create_descriptor<I>()...}};
    }
};

template <class... StaticRow>
constexpr std::array<results_resultset_descriptor, sizeof...(StaticRow)>
    results_resultset_descriptor_table = results_fns<StaticRow...>::create_descriptors(
        mp11::make_index_sequence<sizeof...(StaticRow)>()
    );

template <BOOST_MYSQL_STATIC_ROW... StaticRow>
class static_results_impl
{
    // Data that requires knowing template params
    struct
    {
        results_rows_t<StaticRow...> rows;
        std::array<std::size_t, max_num_columns<StaticRow...>> pos_map{};
        std::array<static_per_resultset_data, sizeof...(StaticRow)> per_resultset{};
    } data_;

    // The type-erased impl, that will use pointers to the above storage
    static_results_erased_impl impl_;

    results_external_data::ptr_data ptr_data() noexcept
    {
        return {
            &data_.rows,
            data_.pos_map.data(),
            data_.per_resultset.data(),
        };
    }

    void set_pointers() noexcept { impl_.ext_data().set_pointers(ptr_data()); }

public:
    static_results_impl() noexcept
        : impl_(results_external_data(
              results_resultset_descriptor_table<StaticRow...>,
              &results_fns<StaticRow...>::reset,
              ptr_data()
          ))
    {
    }

    static_results_impl(const static_results_impl& rhs) : data_(rhs.data_), impl_(rhs.impl_)
    {
        set_pointers();
    }

    static_results_impl(static_results_impl&& rhs) noexcept
        : data_(std::move(rhs.data_)), impl_(std::move(rhs.impl_))
    {
        set_pointers();
    }

    static_results_impl& operator=(const static_results_impl& rhs)
    {
        data_ = rhs.data_;
        impl_ = rhs.impl_;
        set_pointers();
        return *this;
    }

    static_results_impl& operator=(static_results_impl&& rhs)
    {
        data_ = std::move(rhs.data_);
        impl_ = std::move(rhs.impl_);
        set_pointers();
        return *this;
    }

    // User facing
    template <std::size_t I>
    boost::span<const typename std::tuple_element<I, std::tuple<StaticRow...>>::type> get_rows(
    ) const noexcept
    {
        return std::get<I>(data_.rows);
    }

    const static_results_erased_impl& get_interface() const noexcept { return impl_; }
    static_results_erased_impl& get_interface() noexcept { return impl_; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#ifdef BOOST_MYSQL_HEADER_ONLY
#include <boost/mysql/impl/static_results_impl.ipp>
#endif

#endif  // BOOST_MYSQL_CXX14

#endif
