//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_READ_SOME_ROWS_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_READ_SOME_ROWS_HPP

#include <boost/mysql/diagnostics.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>

#include <boost/mysql/detail/algo_params.hpp>
#include <boost/mysql/detail/execution_processor/execution_processor.hpp>

#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/sansio_algorithm.hpp>

#include <boost/asio/coroutine.hpp>

#include <cstddef>

namespace boost {
namespace mysql {
namespace detail {

class read_some_rows_algo : public sansio_algorithm, asio::coroutine
{
    read_some_rows_algo_params params_;
    std::size_t rows_read_{0};

    BOOST_ATTRIBUTE_NODISCARD static std::pair<error_code, std::size_t> process_some_rows(
        connection_state_data& st,
        execution_processor& proc,
        output_ref output,
        diagnostics& diag
    )
    {
        // Process all read messages until they run out, an error happens
        // or an EOF is received
        std::size_t read_rows = 0;
        error_code err;
        proc.on_row_batch_start();
        while (true)
        {
            // Check for errors (like seqnum mismatches)
            if (st.reader.error())
                return {st.reader.error(), read_rows};

            // Get the row message
            auto buff = st.reader.message();

            // Deserialize it
            auto res = deserialize_row_message(buff, st.flavor, diag);
            if (res.type == row_message::type_t::error)
            {
                err = res.data.err;
            }
            else if (res.type == row_message::type_t::row)
            {
                output.set_offset(read_rows);
                err = proc.on_row(res.data.row, output, st.shared_fields);
                if (!err)
                    ++read_rows;
            }
            else
            {
                st.backslash_escapes = res.data.ok_pack.backslash_escapes();
                err = proc.on_row_ok_packet(res.data.ok_pack);
            }

            if (err)
                return {err, read_rows};

            // TODO: can we make this better?
            if (!proc.is_reading_rows() || read_rows >= output.max_size())
                break;

            // Attempt to parse the next message
            st.reader.prepare_read(proc.sequence_number());
            if (!st.reader.done())
                break;
        }
        proc.on_row_batch_finish();
        return {error_code(), read_rows};
    }

    execution_processor& processor() noexcept { return *params_.proc; }

public:
    read_some_rows_algo(connection_state_data& st, read_some_rows_algo_params params) noexcept
        : sansio_algorithm(st), params_(params)
    {
    }

    read_some_rows_algo_params params() const noexcept { return params_; }

    next_action resume(error_code ec)
    {
        if (ec)
            return ec;

        BOOST_ASIO_CORO_REENTER(*this)
        {
            // Clear diagnostics
            params_.diag->clear();

            // Clear any previous use of shared fields.
            // Required for the dynamic version to work.
            st_->shared_fields.clear();

            // If we are not reading rows, return
            if (!processor().is_reading_rows())
                return next_action();

            // Read at least one message. Keep parsing state, in case a previous message
            // was parsed partially
            BOOST_ASIO_CORO_YIELD return read(processor().sequence_number(), true);

            // Process messages
            std::tie(ec, rows_read_) = process_some_rows(*st_, processor(), params_.output, *params_.diag);
            return ec;
        }

        return next_action();
    }

    std::size_t result() const noexcept { return rows_read_; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
