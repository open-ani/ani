//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_BUFFER_PARAMS_HPP
#define BOOST_MYSQL_BUFFER_PARAMS_HPP

#include <boost/mysql/defaults.hpp>

#include <cstddef>

namespace boost {
namespace mysql {

/**
 * \brief Buffer configuration parameters for a connection.
 */
class buffer_params
{
    std::size_t initial_read_size_;

public:
    /// The default value of \ref initial_read_size.
    static constexpr std::size_t default_initial_read_size = default_initial_read_buffer_size;

    /**
     * \brief Initializing constructor.
     * \param initial_read_size Initial size of the read buffer. A bigger read buffer
     * can increase the number of rows returned by \ref connection::read_some_rows.
     */
    constexpr explicit buffer_params(std::size_t initial_read_size = default_initial_read_size) noexcept
        : initial_read_size_(initial_read_size)
    {
    }

    /// Gets the initial size of the read buffer.
    constexpr std::size_t initial_read_size() const noexcept { return initial_read_size_; }

    /// Sets the initial size of the read buffer.
    void set_initial_read_size(std::size_t v) noexcept { initial_read_size_ = v; }
};

}  // namespace mysql
}  // namespace boost

#endif
