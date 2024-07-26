//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_MESSAGE_READER_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_MESSAGE_READER_HPP

#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/error_code.hpp>

#include <boost/mysql/impl/internal/protocol/constants.hpp>
#include <boost/mysql/impl/internal/protocol/protocol.hpp>
#include <boost/mysql/impl/internal/sansio/read_buffer.hpp>

#include <boost/asio/coroutine.hpp>
#include <boost/assert.hpp>

#include <cstddef>
#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

// Flow:
//   Prepare a read operation with prepare_read()
//   In a loop, until done():
//      prepare_buffer() to resize the buffer to an appropriate size
//      Read bytes against buffer()
//      Call resume with the number of bytes read
// Or call prepare_read() and check done() to attempt to get a cached message
//    (further prepare_read calls should use keep_state=true)
class message_reader
{
public:
    message_reader(std::size_t initial_buffer_size, std::size_t max_frame_size = MAX_PACKET_SIZE)
        : buffer_(initial_buffer_size), max_frame_size_(max_frame_size)
    {
    }

    void reset() noexcept
    {
        buffer_.reset();
        state_ = parse_state();
    }

    // Prepares a read operation. sequence_number should be kept alive until
    // the next read is prepared or no more calls to resume() are expected.
    // If keep_state=true, and the op is not complete, parsing state is preserved
    void prepare_read(std::uint8_t& sequence_number, bool keep_state = false) noexcept
    {
        if (!keep_state || done())
            state_ = parse_state(sequence_number);
        else
            state_.sequence_number = &sequence_number;
        resume(0);
    }

    // Is parsing the current message done?
    bool done() const noexcept { return state_.coro.is_complete(); }

    // Returns any errors generated during parsing. Requires this->done()
    error_code error() const noexcept
    {
        BOOST_ASSERT(done());
        return state_.ec;
    }

    // Returns the last parsed message. Valid until prepare_buffer()
    // is next called. Requires done() && !error()
    span<const std::uint8_t> message() const noexcept
    {
        BOOST_ASSERT(done());
        BOOST_ASSERT(!error());
        return buffer_.current_message();
    }

    // Returns buffer space suitable to read bytes to
    span<std::uint8_t> buffer() noexcept { return buffer_.free_area(); }

    // Removes old messages stored in the buffer, and resizes it, if required, to accomodate
    // the message currently being parsed.
    void prepare_buffer()
    {
        buffer_.remove_reserved();
        buffer_.grow_to_fit(state_.required_size);
        state_.required_size = 0;
    }

    // The main operation. Call it after reading bytes against buffer(),
    // with the number of bytes read
    void resume(std::size_t bytes_read)
    {
        frame_header header{};
        buffer_.move_to_pending(bytes_read);

        BOOST_ASIO_CORO_REENTER(state_.coro)
        {
            // Move the previously parsed message to the reserved area, if any
            buffer_.move_to_reserved(buffer_.current_message_size());

            while (true)
            {
                // Read the header
                set_required_size(frame_header_size);
                while (buffer_.pending_size() < frame_header_size)
                    BOOST_ASIO_CORO_YIELD;

                // Mark the header as belonging to the current message
                buffer_.move_to_current_message(frame_header_size);

                // Deserialize the header
                header = deserialize_frame_header(span<const std::uint8_t, frame_header_size>(
                    buffer_.pending_first() - frame_header_size,
                    frame_header_size
                ));

                // Process the sequence number
                if (*state_.sequence_number != header.sequence_number)
                {
                    state_.ec = client_errc::sequence_number_mismatch;
                    BOOST_ASIO_CORO_YIELD break;
                }
                ++*state_.sequence_number;

                // Process the packet size
                state_.body_bytes = header.size;
                state_.more_frames_follow = (state_.body_bytes == max_frame_size_);

                // We are done with the header
                if (state_.is_first_frame)
                {
                    // If it's the 1st frame, we can just move the header bytes to the reserved
                    // area, avoiding a big memmove
                    buffer_.move_to_reserved(frame_header_size);
                }
                else
                {
                    buffer_.remove_current_message_last(frame_header_size);
                }
                state_.is_first_frame = false;

                // Read the body
                set_required_size(state_.body_bytes);
                while (buffer_.pending_size() < state_.body_bytes)
                    BOOST_ASIO_CORO_YIELD;

                buffer_.move_to_current_message(state_.body_bytes);

                // Check if we're done
                if (!state_.more_frames_follow)
                {
                    BOOST_ASIO_CORO_YIELD break;
                }
            }
        }
    }

    // Exposed for testing
    const read_buffer& internal_buffer() const noexcept { return buffer_; }

private:
    read_buffer buffer_;
    std::size_t max_frame_size_;

    struct parse_state
    {
        asio::coroutine coro;
        std::uint8_t* sequence_number{};
        bool is_first_frame{true};
        std::size_t body_bytes{0};
        bool more_frames_follow{false};
        std::size_t required_size{0};
        error_code ec;

        parse_state() = default;
        parse_state(std::uint8_t& seqnum) noexcept : sequence_number(&seqnum) {}
    } state_;

    void set_required_size(std::size_t required_bytes) noexcept
    {
        if (required_bytes > buffer_.pending_size())
            state_.required_size = required_bytes - buffer_.pending_size();
        else
            state_.required_size = 0;
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
