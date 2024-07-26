//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_STATIC_BUFFER_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_STATIC_BUFFER_HPP

// A very simplified variable-length buffer with fixed max-size

#include <boost/assert.hpp>
#include <boost/core/span.hpp>

#include <array>
#include <cstring>

namespace boost {
namespace mysql {
namespace detail {

template <std::size_t max_size>
class static_buffer
{
    std::array<std::uint8_t, max_size> buffer_{};
    std::size_t size_{};

public:
    static_buffer() noexcept = default;
    span<const std::uint8_t> to_span() const noexcept { return {buffer_.data(), size_}; }
    void append(const void* data, std::size_t data_size) noexcept
    {
        std::size_t new_size = size_ + data_size;
        BOOST_ASSERT(new_size <= max_size);
        std::memcpy(buffer_.data() + size_, data, data_size);
        size_ = new_size;
    }
    void clear() noexcept { size_ = 0; }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
