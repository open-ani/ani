//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_SERIALIZATION_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_PROTOCOL_SERIALIZATION_HPP

#include <boost/mysql/client_errc.hpp>
#include <boost/mysql/error_code.hpp>
#include <boost/mysql/field_view.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/impl/internal/protocol/basic_types.hpp>
#include <boost/mysql/impl/internal/protocol/capabilities.hpp>
#include <boost/mysql/impl/internal/protocol/protocol_field_type.hpp>

#include <boost/assert.hpp>
#include <boost/core/span.hpp>
#include <boost/endian/conversion.hpp>
#include <boost/endian/detail/endian_load.hpp>
#include <boost/endian/detail/endian_store.hpp>

#include <cstddef>
#include <cstdint>
#include <cstring>
#include <type_traits>

namespace boost {
namespace mysql {
namespace detail {

// We operate with this enum directly in the deserialization routines for efficiency, then transform it to an
// actual error code
enum class deserialize_errc
{
    ok = 0,
    incomplete_message = 1,
    protocol_value_error,
    server_unsupported
};
inline error_code to_error_code(deserialize_errc v) noexcept
{
    switch (v)
    {
    case deserialize_errc::ok: return error_code();
    case deserialize_errc::incomplete_message: return error_code(client_errc::incomplete_message);
    case deserialize_errc::protocol_value_error: return error_code(client_errc::protocol_value_error);
    case deserialize_errc::server_unsupported: return error_code(client_errc::server_unsupported);
    default: BOOST_ASSERT(false); return error_code();  // avoid warnings
    }
}

class serialization_context
{
    std::uint8_t* first_;

public:
    explicit serialization_context(std::uint8_t* first) noexcept : first_(first) {}
    std::uint8_t* first() const noexcept { return first_; }
    void advance(std::size_t size) noexcept { first_ += size; }
    void write(const void* buffer, std::size_t size) noexcept
    {
        if (size)
        {
            BOOST_ASSERT(buffer != nullptr);
            std::memcpy(first_, buffer, size);
            advance(size);
        }
    }
    void write(std::uint8_t elm) noexcept
    {
        *first_ = elm;
        ++first_;
    }
};

class deserialization_context
{
    const std::uint8_t* first_;
    const std::uint8_t* last_;

public:
    deserialization_context(span<const std::uint8_t> data) noexcept
        : deserialization_context(data.data(), data.size())
    {
    }
    deserialization_context(const std::uint8_t* first, std::size_t size) noexcept
        : first_(first), last_(first + size){};
    const std::uint8_t* first() const noexcept { return first_; }
    const std::uint8_t* last() const noexcept { return last_; }
    void advance(std::size_t sz) noexcept
    {
        first_ += sz;
        BOOST_ASSERT(last_ >= first_);
    }
    void rewind(std::size_t sz) noexcept { first_ -= sz; }
    std::size_t size() const noexcept { return last_ - first_; }
    bool empty() const noexcept { return last_ == first_; }
    bool enough_size(std::size_t required_size) const noexcept { return size() >= required_size; }
    deserialize_errc copy(void* to, std::size_t sz) noexcept
    {
        if (!enough_size(sz))
            return deserialize_errc::incomplete_message;
        memcpy(to, first_, sz);
        advance(sz);
        return deserialize_errc::ok;
    }
    string_view get_string(std::size_t sz) const noexcept
    {
        return string_view(reinterpret_cast<const char*>(first_), sz);
    }
    error_code check_extra_bytes() const noexcept
    {
        return empty() ? error_code() : error_code(client_errc::extra_bytes);
    }
    span<const std::uint8_t> to_span() const noexcept { return span<const std::uint8_t>(first_, size()); }
};

// integers
template <class T, class = typename std::enable_if<std::is_integral<T>::value>::type>
deserialize_errc deserialize(deserialization_context& ctx, T& output) noexcept
{
    constexpr std::size_t sz = sizeof(T);
    if (!ctx.enough_size(sz))
    {
        return deserialize_errc::incomplete_message;
    }
    output = endian::endian_load<T, sz, boost::endian::order::little>(ctx.first());
    ctx.advance(sz);
    return deserialize_errc::ok;
}

template <class T, class = typename std::enable_if<std::is_integral<T>::value>::type>
void serialize(serialization_context& ctx, T input) noexcept
{
    endian::endian_store<T, sizeof(T), endian::order::little>(ctx.first(), input);
    ctx.advance(sizeof(T));
}

template <class T, class = typename std::enable_if<std::is_integral<T>::value>::type>
constexpr std::size_t get_size(T) noexcept
{
    return sizeof(T);
}

// int3
inline deserialize_errc deserialize(deserialization_context& ctx, int3& output) noexcept
{
    if (!ctx.enough_size(3))
        return deserialize_errc::incomplete_message;
    output.value = endian::load_little_u24(ctx.first());
    ctx.advance(3);
    return deserialize_errc::ok;
}
inline void serialize(serialization_context& ctx, int3 input) noexcept
{
    endian::store_little_u24(ctx.first(), input.value);
    ctx.advance(3);
}
constexpr std::size_t get_size(int3) noexcept { return 3; }

// int_lenenc
inline deserialize_errc deserialize(deserialization_context& ctx, int_lenenc& output) noexcept
{
    std::uint8_t first_byte = 0;
    auto err = deserialize(ctx, first_byte);
    if (err != deserialize_errc::ok)
    {
        return err;
    }

    if (first_byte == 0xFC)
    {
        std::uint16_t value = 0;
        err = deserialize(ctx, value);
        output.value = value;
    }
    else if (first_byte == 0xFD)
    {
        int3 value{};
        err = deserialize(ctx, value);
        output.value = value.value;
    }
    else if (first_byte == 0xFE)
    {
        std::uint64_t value = 0;
        err = deserialize(ctx, value);
        output.value = value;
    }
    else
    {
        err = deserialize_errc::ok;
        output.value = first_byte;
    }
    return err;
}
inline void serialize(serialization_context& ctx, int_lenenc input) noexcept
{
    if (input.value < 251)
    {
        serialize(ctx, static_cast<std::uint8_t>(input.value));
    }
    else if (input.value < 0x10000)
    {
        ctx.write(0xfc);
        serialize(ctx, static_cast<std::uint16_t>(input.value));
    }
    else if (input.value < 0x1000000)
    {
        ctx.write(0xfd);
        serialize(ctx, int3{static_cast<std::uint32_t>(input.value)});
    }
    else
    {
        ctx.write(0xfe);
        serialize(ctx, static_cast<std::uint64_t>(input.value));
    }
}
inline std::size_t get_size(int_lenenc input) noexcept
{
    if (input.value < 251)
        return 1;
    else if (input.value < 0x10000)
        return 3;
    else if (input.value < 0x1000000)
        return 4;
    else
        return 9;
}

// protocol_field_type
inline deserialize_errc deserialize(deserialization_context& ctx, protocol_field_type& output) noexcept
{
    std::underlying_type<protocol_field_type>::type value = 0;
    auto err = deserialize(ctx, value);
    output = static_cast<protocol_field_type>(value);
    return err;
}
inline void serialize(serialization_context& ctx, protocol_field_type input) noexcept
{
    serialize(ctx, static_cast<std::underlying_type<protocol_field_type>::type>(input));
}
constexpr std::size_t get_size(protocol_field_type) noexcept { return sizeof(protocol_field_type); }

// string_fixed
template <std::size_t N>
deserialize_errc deserialize(deserialization_context& ctx, string_fixed<N>& output) noexcept
{
    if (!ctx.enough_size(N))
        return deserialize_errc::incomplete_message;
    memcpy(output.value.data(), ctx.first(), N);
    ctx.advance(N);
    return deserialize_errc::ok;
}

template <std::size_t N>
void serialize(serialization_context& ctx, const string_fixed<N>& input) noexcept
{
    ctx.write(input.value.data(), N);
}

template <std::size_t N>
constexpr std::size_t get_size(const string_fixed<N>&) noexcept
{
    return N;
}

// string_null
inline deserialize_errc deserialize(deserialization_context& ctx, string_null& output) noexcept
{
    auto string_end = std::find(ctx.first(), ctx.last(), 0);
    if (string_end == ctx.last())
    {
        return deserialize_errc::incomplete_message;
    }
    std::size_t length = string_end - ctx.first();
    output.value = ctx.get_string(length);
    ctx.advance(length + 1);  // skip the null terminator
    return deserialize_errc::ok;
}
inline void serialize(serialization_context& ctx, string_null input) noexcept
{
    ctx.write(input.value.data(), input.value.size());
    ctx.write(0);  // null terminator
}
inline std::size_t get_size(string_null input) noexcept { return input.value.size() + 1; }

// string_eof
inline deserialize_errc deserialize(deserialization_context& ctx, string_eof& output) noexcept
{
    std::size_t size = ctx.size();
    output.value = ctx.get_string(size);
    ctx.advance(size);
    return deserialize_errc::ok;
}
inline void serialize(serialization_context& ctx, string_eof input) noexcept
{
    ctx.write(input.value.data(), input.value.size());
}
inline std::size_t get_size(string_eof input) noexcept { return input.value.size(); }

// string_lenenc
inline deserialize_errc deserialize(deserialization_context& ctx, string_lenenc& output) noexcept
{
    int_lenenc length;
    auto err = deserialize(ctx, length);
    if (err != deserialize_errc::ok)
    {
        return err;
    }
    if (length.value > (std::numeric_limits<std::size_t>::max)())
    {
        return deserialize_errc::protocol_value_error;
    }
    auto len = static_cast<std::size_t>(length.value);
    if (!ctx.enough_size(len))
    {
        return deserialize_errc::incomplete_message;
    }

    output.value = ctx.get_string(len);
    ctx.advance(len);
    return deserialize_errc::ok;
}
inline void serialize(serialization_context& ctx, string_lenenc input) noexcept
{
    serialize(ctx, int_lenenc{input.value.size()});
    ctx.write(input.value.data(), input.value.size());
}
inline std::size_t get_size(string_lenenc input) noexcept
{
    return get_size(int_lenenc{input.value.size()}) + input.value.size();
}

// serialize, deserialize, and get size of multiple fields at the same time
template <class FirstType, class SecondType, class... Rest>
deserialize_errc deserialize(
    deserialization_context& ctx,
    FirstType& first,
    SecondType& second,
    Rest&... tail
) noexcept
{
    deserialize_errc err = deserialize(ctx, first);
    if (err == deserialize_errc::ok)
    {
        err = deserialize(ctx, second, tail...);
    }
    return err;
}

template <class FirstType, class SecondType, class... Rest>
void serialize(
    serialization_context& ctx,
    const FirstType& first,
    const SecondType& second,
    const Rest&... rest
) noexcept
{
    serialize(ctx, first);
    serialize(ctx, second, rest...);
}

template <class FirstType, class SecondType, class... Rest>
std::size_t get_size(const FirstType& first, const SecondType& second, const Rest&... rest) noexcept
{
    return get_size(first) + get_size(second, rest...);
}

// helpers
inline string_view to_string(span<const std::uint8_t> v) noexcept
{
    return string_view(reinterpret_cast<const char*>(v.data()), v.size());
}
inline span<const std::uint8_t> to_span(string_view v) noexcept
{
    return span<const std::uint8_t>(reinterpret_cast<const std::uint8_t*>(v.data()), v.size());
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
