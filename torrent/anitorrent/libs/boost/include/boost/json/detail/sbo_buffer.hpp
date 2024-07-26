//
// Copyright (c) 2023 Dmitry Arkhipov (grisumbras@yandex.ru)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//


#ifndef BOOST_JSON_DETAIL_SBO_BUFFER_HPP
#define BOOST_JSON_DETAIL_SBO_BUFFER_HPP

#include <boost/json/detail/config.hpp>
#include <boost/json/detail/except.hpp>
#include <string>
#include <array>

namespace boost {
namespace json {
namespace detail {

template< std::size_t N >
class sbo_buffer
{
    struct size_ptr_pair
    {
        std::size_t size;
        char* ptr;
    };
    BOOST_STATIC_ASSERT( N >= sizeof(size_ptr_pair) );

    union {
        std::array<char, N> buffer_;
        std::size_t capacity_;
    };
    char* data_ = buffer_.data();
    std::size_t size_ = 0;

    bool
    is_small() const noexcept
    {
        return data_ == buffer_.data();
    }

    void
    dispose()
    {
        if( is_small() )
            return;

        delete[] data_;
#if defined(__GNUC__)
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif
        buffer_ = {};
#if defined(__GNUC__)
# pragma GCC diagnostic pop
#endif
        data_ = buffer_.data();
    }

    static constexpr
    std::size_t
    max_size() noexcept
    {
        return BOOST_JSON_MAX_STRING_SIZE;
    }

public:
    sbo_buffer()
        : buffer_()
    {}

    sbo_buffer( sbo_buffer&& other ) noexcept
        : size_(other.size_)
    {
        if( other.is_small() )
        {
            buffer_ = other.buffer_;
            data_ = buffer_.data();
        }
        else
        {
            data_ = other.data_;
            other.data_ = other.buffer_.data();
        }
        BOOST_ASSERT( other.is_small() );
    }

    sbo_buffer&
    operator=( sbo_buffer&& other ) noexcept
    {
        if( &other == this )
            return this;

        if( other.is_small() )
        {
            buffer_ = other.buffer_;
            data_ = buffer_.data();
        }
        else
        {
            data_ = other.data_;
            other.data_ = other.buffer_.data();
        }

        size_ = other.size_;
        other.size_ = 0;

        return *this;
    }

    ~sbo_buffer()
    {
        if( !is_small() )
            delete[] data_;
    }

    std::size_t
    capacity() const noexcept
    {
        return is_small() ? buffer_.size() : capacity_;
    }

    void
    reset() noexcept
    {
        dispose();
        clear();
    }

    void
    clear()
    {
        size_ = 0;
    }

    void
    grow( std::size_t size )
    {
        if( !size )
            return;

        if( max_size() - size_ < size )
        {
            BOOST_STATIC_CONSTEXPR source_location loc = BOOST_CURRENT_LOCATION;
            detail::throw_system_error( error::number_too_large, &loc );
        }

        std::size_t const old_capacity = this->capacity();
        std::size_t new_capacity = size_ + size;

        // growth factor 2
        if( old_capacity <= max_size() - old_capacity ) // check for overflow
            new_capacity = (std::max)(old_capacity * 2, new_capacity);

        char* new_data = new char[new_capacity];
        std::memcpy(new_data, data_, size_);

        dispose();
        data_ = new_data;
        capacity_ = new_capacity;
    }

    char*
    append( char const* ptr, std::size_t size )
    {
        grow(size);

        if(BOOST_JSON_LIKELY( size ))
            std::memcpy( data_ + size_, ptr, size );
        size_ += size;
        return data_;
    }

    std::size_t
    size() noexcept
    {
        return size_;
    }
};

} // namespace detail
} // namespace json
} // namespace boost

#endif // BOOST_JSON_DETAIL_SBO_BUFFER_HPP
