//
// Copyright (c) 2022 Klemens D. Morgenstern (klemens dot morgenstern at gmx dot net)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
#ifndef BOOST_BEAST_BUFFER_REF_HPP
#define BOOST_BEAST_BUFFER_REF_HPP

#include <cstddef>

namespace boost {
namespace beast {

#if !defined(BOOST_ASIO_NO_DYNAMIC_BUFFER_V1)

/** The buffer ref provides a wrapper around beast buffers
 * to make them usable with asio dynamic_buffer v1.
 *
 * v2 is current not supported, so that
 * `BOOST_ASIO_NO_DYNAMIC_BUFFER_V1` mustn't be defined.
 *
 * @par Example
 *
 * @code
 *
 * asio::tcp::socket sock;
 * beast::flat_buffer fb;
 * asio::read_until(sock, ref(fb) '\n');
 *
 * @endcode
 *
 * @tparam Buffer The underlying buffer
 */
template<typename Buffer>
struct buffer_ref
{
    /// The ConstBufferSequence used to represent the readable bytes.
    using const_buffers_type = typename Buffer::const_buffers_type;

    /// The MutableBufferSequence used to represent the writable bytes.
    using mutable_buffers_type = typename Buffer::mutable_buffers_type;

    /// Returns the number of readable bytes.
    std::size_t
    size() const noexcept
    {
        return buffer_.size();
    }

    /// Return the maximum number of bytes, both readable and writable, that can ever be held.
    std::size_t
    max_size() const noexcept
    {
        return buffer_.max_size();
    }

    /// Return the maximum number of bytes, both readable and writable, that can be held without requiring an allocation.
    std::size_t
    capacity() const noexcept
    {
        return buffer_.capacity();
    }

    /// Returns a constant buffer sequence representing the readable bytes
    const_buffers_type
    data() const noexcept
    {
        return buffer_.data();
    }

    /// Get a list of buffers that represents the output
    /// sequence, with the given size.
    /**
     * Ensures that the output sequence can accommodate @c n bytes, resizing the
     * vector object as necessary.
     *
     * @returns An object of type @c mutable_buffers_type that satisfies
     * MutableBufferSequence requirements, representing vector memory at the
     * start of the output sequence of size @c n.
     *
     * @throws std::length_error If <tt>size() + n > max_size()</tt>.
     *
     * @note The returned object is invalidated by any @c dynamic_vector_buffer
     * or @c vector member function that modifies the input sequence or output
     * sequence.
     */
    mutable_buffers_type prepare(std::size_t n)
    {
        return buffer_.prepare(n);
    }

    /// Move bytes from the output sequence to the input
    /// sequence.
    /**
     * @param n The number of bytes to append from the start of the output
     * sequence to the end of the input sequence. The remainder of the output
     * sequence is discarded.
     *
     * Requires a preceding call <tt>prepare(x)</tt> where <tt>x >= n</tt>, and
     * no intervening operations that modify the input or output sequence.
     *
     * @note If @c n is greater than the size of the output sequence, the entire
     * output sequence is moved to the input sequence and no error is issued.
     */
    void commit(std::size_t n)
    {
        return buffer_.commit(n);
    }

    /// Remove `n` bytes from the readable byte sequence.
    /**
     * @b DynamicBuffer_v1: Removes @c n characters from the beginning of the
     * input sequence. @note If @c n is greater than the size of the input
     * sequence, the entire input sequence is consumed and no error is issued.
     */
    void consume(std::size_t n)
    {
        return buffer_.consume(n);
    }

    /// The type of the underlying buffer.
    using buffer_type = Buffer;

    /// Create a buffer reference around @c buffer.
    buffer_ref(Buffer & buffer) : buffer_(buffer) {}

    /// Copy the reference.
    buffer_ref(const buffer_ref& buffer) = default;

private:
    Buffer &buffer_;
};


template<class Allocator>
class basic_flat_buffer;
template<std::size_t N>
class flat_static_buffer;
template<class Allocator>
class basic_multi_buffer;
template<std::size_t N>
class static_buffer;

/// Create a buffer_ref for basic_flat_buffer.
template<class Allocator>
inline buffer_ref<basic_flat_buffer<Allocator>> ref(basic_flat_buffer<Allocator> & buf)
{
    return buffer_ref<basic_flat_buffer<Allocator>>(buf);
}

/// Create a buffer_ref for flat_static_buffer.
template<std::size_t N>
inline buffer_ref<flat_static_buffer<N>> ref(flat_static_buffer<N> & buf)
{
    return buffer_ref<flat_static_buffer<N>>(buf);
}

/// Create a buffer_ref for basic_multi_buffer.
template<class Allocator>
inline buffer_ref<basic_multi_buffer<Allocator>> ref(basic_multi_buffer<Allocator> & buf)
{
    return buffer_ref<basic_multi_buffer<Allocator>>(buf);
}

/// Create a buffer_ref for static_buffer.
template<std::size_t N>
inline buffer_ref<static_buffer<N>> ref(static_buffer<N> & buf)
{
    return buffer_ref<static_buffer<N>>(buf);
}

#endif // !defined(BOOST_ASIO_NO_DYNAMIC_BUFFER_V1)

}
}

#endif //BOOST_BEAST_BUFFER_REF_HPP
