//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_OUTPUT_STRING_HPP
#define BOOST_MYSQL_DETAIL_OUTPUT_STRING_HPP

#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>

#include <cstddef>

#ifdef BOOST_MYSQL_HAS_CONCEPTS
#include <concepts>
#endif

namespace boost {
namespace mysql {
namespace detail {

#ifdef BOOST_MYSQL_HAS_CONCEPTS

template <class T>
concept output_string = std::movable<T> && requires(T& t, const char* data, std::size_t sz) {
    t.append(data, sz);
    t.clear();
};

#define BOOST_MYSQL_OUTPUT_STRING ::boost::mysql::detail::output_string

#else

#define BOOST_MYSQL_OUTPUT_STRING class

#endif

class output_string_ref
{
    using append_fn_t = void (*)(void*, const char*, std::size_t);

    append_fn_t append_fn_;
    void* container_;

    template <class T>
    static void do_append(void* container, const char* data, std::size_t size)
    {
        static_cast<T*>(container)->append(data, size);
    }

public:
    output_string_ref(append_fn_t append_fn, void* container) noexcept
        : append_fn_(append_fn), container_(container)
    {
    }

    template <class T>
    static output_string_ref create(T& obj) noexcept
    {
        return output_string_ref(&do_append<T>, &obj);
    }

    void append(string_view data)
    {
        if (data.size() > 0u)
            append_fn_(container_, data.data(), data.size());
    }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
