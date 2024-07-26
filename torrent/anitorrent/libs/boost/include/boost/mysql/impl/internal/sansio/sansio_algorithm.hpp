//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_IMPL_INTERNAL_SANSIO_SANSIO_ALGORITHM_HPP
#define BOOST_MYSQL_IMPL_INTERNAL_SANSIO_SANSIO_ALGORITHM_HPP

#include <boost/mysql/error_code.hpp>

#include <boost/mysql/impl/internal/sansio/connection_state_data.hpp>
#include <boost/mysql/impl/internal/sansio/next_action.hpp>

#include <type_traits>

namespace boost {
namespace mysql {
namespace detail {

class sansio_algorithm
{
protected:
    connection_state_data* st_;

    next_action read(std::uint8_t& seqnum, bool keep_parsing_state = false)
    {
        // buffer is attached by the algo runner
        st_->reader.prepare_read(seqnum, keep_parsing_state);
        return next_action::read(next_action::read_args_t{{}, false});
    }

    template <class Serializable>
    next_action write(const Serializable& msg, std::uint8_t& seqnum)
    {
        // buffer is attached by the algo runner
        st_->writer.prepare_write(msg, seqnum);
        return next_action::write(next_action::write_args_t{{}, false});
    }

    sansio_algorithm(connection_state_data& st) noexcept : st_(&st) {}

public:
    const connection_state_data& conn_state() const noexcept { return *st_; }
    connection_state_data& conn_state() noexcept { return *st_; }
};

class any_algo_ref
{
    template <class Algo>
    static next_action do_resume(sansio_algorithm* self, error_code ec)
    {
        return static_cast<Algo*>(self)->resume(ec);
    }

    using fn_t = next_action (*)(sansio_algorithm*, error_code);

    sansio_algorithm* algo_{};
    fn_t fn_{};

public:
    template <
        class Algo,
        class = typename std::enable_if<std::is_base_of<sansio_algorithm, Algo>::value>::type>
    any_algo_ref(Algo& op) noexcept : algo_(&op), fn_(&do_resume<Algo>)
    {
    }

    sansio_algorithm& get() noexcept { return *algo_; }
    const sansio_algorithm& get() const noexcept { return *algo_; }
    next_action resume(error_code ec) { return fn_(algo_, ec); }
};

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
