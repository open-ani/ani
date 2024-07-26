//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_MESSAGE_WRITER_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_MESSAGE_WRITER_HPP

#include <boost/mysql/impl/internal/protocol/constants.hpp>
#include <boost/mysql/impl/internal/protocol/protocol.hpp>

#include <array>
#include <cstddef>
#include <cstdint>

namespace boost {
namespace mysql {
namespace detail {

class chunk_processor
{
    std::size_t first_{};
    std::size_t last_{};

    std::size_t remaining() const noexcept { return last_ - first_; }

public:
    chunk_processor() = default;
    void reset(std::size_t first, std::size_t last) noexcept
    {
        BOOST_ASSERT(last >= first);
        first_ = first;
        last_ = last;
    }
    void on_bytes_written(std::size_t n) noexcept
    {
        BOOST_ASSERT(remaining() >= n);
        first_ += n;
    }
    bool done() const noexcept { return first_ == last_; }
    span<const std::uint8_t> get_chunk(const std::vector<std::uint8_t>& buff) const noexcept
    {
        BOOST_ASSERT(buff.size() >= last_);
        return {buff.data() + first_, remaining()};
    }
};

class message_writer
{
    std::vector<std::uint8_t> buffer_;
    std::size_t max_frame_size_;

    enum class coro_state
    {
        initial,
        in_progress,
        done,

        // Send a short pipeline (all messages fitting in a single frame).
        // Woraround to pipeline pings with close statement requests
        // until we implement proper pipelining in https://github.com/boostorg/mysql/issues/75
        short_pipeline,
    };

    struct state_t
    {
        coro_state coro{coro_state::initial};
        std::size_t remaining_frames{0};
        std::size_t processed_bytes{0};
        chunk_processor chunk;
        std::uint8_t* seqnum{nullptr};

        state_t() = default;
        state_t(std::size_t total_frames, std::uint8_t* seqnum) noexcept
            : remaining_frames(total_frames), seqnum(seqnum)
        {
        }
    } state_;

    std::size_t total_message_size() const noexcept { return buffer_.size() - frame_header_size; }

    void prepare_frame()
    {
        std::size_t size = (std::min)(max_frame_size_, total_message_size() - state_.processed_bytes);
        serialize_frame_header(
            frame_header{static_cast<std::uint32_t>(size), (*state_.seqnum)++},
            span<std::uint8_t, frame_header_size>(buffer_.data() + state_.processed_bytes, frame_header_size)
        );
        state_.chunk.reset(state_.processed_bytes, state_.processed_bytes + size + frame_header_size);
        state_.processed_bytes += size;
    }

    span<std::uint8_t> prepare_write(std::size_t msg_size, std::uint8_t& seqnum)
    {
        buffer_.resize(msg_size + frame_header_size);
        state_ = state_t(msg_size / max_frame_size_ + 1, &seqnum);
        return {buffer_.data() + frame_header_size, msg_size};
    }

    template <class Serializable>
    std::uint8_t* serialize_with_header(const Serializable& msg, std::uint8_t& seqnum, std::uint8_t* it)
    {
        // Calculate size
        std::size_t sz = msg.get_size();
        BOOST_ASSERT(sz < max_frame_size_);

        // Serialize header
        frame_header h{static_cast<std::uint32_t>(sz), seqnum++};
        serialize_frame_header(h, span<std::uint8_t, frame_header_size>(it, frame_header_size));
        it += frame_header_size;

        // Serialize the message
        msg.serialize(span<std::uint8_t>(it, sz));
        it += sz;

        return it;
    }

public:
    message_writer(std::size_t max_frame_size = MAX_PACKET_SIZE) noexcept : max_frame_size_(max_frame_size) {}

    template <class Serializable>
    void prepare_write(const Serializable& message, std::uint8_t& sequence_number)
    {
        auto buff = prepare_write(message.get_size(), sequence_number);
        message.serialize(buff);
        resume(0);
    }

    // Serializes two messages into the write buffer. They must fit in a single frame
    template <class Serializable1, class Serializable2>
    void prepare_pipelined_write(
        const Serializable1& msg1,
        std::uint8_t& seqnum1,
        const Serializable2& msg2,
        std::uint8_t& seqnum2
    )
    {
        // Prepare the buffer
        std::size_t total_size = msg1.get_size() + msg2.get_size() + 2 * frame_header_size;
        buffer_.resize(total_size);

        // Serialize the messages
        auto it = buffer_.data();
        it = serialize_with_header(msg1, seqnum1, it);
        it = serialize_with_header(msg2, seqnum2, it);

        // Set up writer
        state_ = state_t();
        state_.chunk.reset(0, total_size);
        state_.coro = coro_state::short_pipeline;
    }

    bool done() const noexcept { return state_.coro == coro_state::done; }

    span<const std::uint8_t> current_chunk() const
    {
        BOOST_ASSERT(!done());
        BOOST_ASSERT(!buffer_.empty());
        return state_.chunk.get_chunk(buffer_);
    }

    void resume(std::size_t n)
    {
        // This is implemented as a plain switch to workaround
        // a MSVC 14.3 codegen bug under release builds with asio::coroutine
        switch (state_.coro)
        {
        case coro_state::short_pipeline:
            state_.chunk.on_bytes_written(n);
            state_.coro = state_.chunk.done() ? coro_state::done : coro_state::short_pipeline;
            return;
        case coro_state::initial:
            for (; state_.remaining_frames != 0u; --state_.remaining_frames)
            {
                prepare_frame();
                while (!state_.chunk.done())
                {
                    state_.coro = coro_state::in_progress;
                    return;
                    // clang-format off
        case coro_state::in_progress:  // fallthrough
                    // clang-format on
                    state_.chunk.on_bytes_written(n);
                }
            }
        case coro_state::done:
        default: break;
        }
        state_.coro = coro_state::done;
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
