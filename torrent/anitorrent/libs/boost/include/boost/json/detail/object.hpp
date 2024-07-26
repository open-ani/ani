//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_DETAIL_OBJECT_HPP
#define BOOST_JSON_DETAIL_OBJECT_HPP

#include <boost/json/storage_ptr.hpp>
#include <boost/json/string_view.hpp>
#include <cstdlib>

namespace boost {
namespace json {

class object;
class value;
class key_value_pair;

namespace detail {

class unchecked_object
{
    // each element is two values,
    // first one is a string key,
    // second one is the value.
    value* data_;
    std::size_t size_;
    storage_ptr const& sp_;

public:
    inline
    ~unchecked_object();

    unchecked_object(
        value* data,
        std::size_t size, // # of kv-pairs
        storage_ptr const& sp) noexcept
        : data_(data)
        , size_(size)
        , sp_(sp)
    {
    }

    unchecked_object(
        unchecked_object&& other) noexcept
        : data_(other.data_)
        , size_(other.size_)
        , sp_(other.sp_)
    {
        other.data_ = nullptr;
    }

    storage_ptr const&
    storage() const noexcept
    {
        return sp_;
    }

    std::size_t
    size() const noexcept
    {
        return size_;
    }

    value*
    release() noexcept
    {
        auto const data = data_;
        data_ = nullptr;
        return data;
    }
};

template<class CharRange>
std::pair<key_value_pair*, std::size_t>
find_in_object(
    object const& obj,
    CharRange key) noexcept;

extern template
BOOST_JSON_DECL
std::pair<key_value_pair*, std::size_t>
find_in_object<string_view>(
    object const&,
    string_view key) noexcept;

} // detail
} // namespace json
} // namespace boost

#endif
