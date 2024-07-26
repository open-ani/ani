//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_TYPING_GET_TYPE_INDEX_HPP
#define BOOST_MYSQL_DETAIL_TYPING_GET_TYPE_INDEX_HPP

#include <boost/mp11/algorithm.hpp>
#include <boost/mp11/list.hpp>

namespace boost {
namespace mysql {
namespace detail {

constexpr std::size_t index_not_found = static_cast<std::size_t>(-1);

template <class SpanRowType, class... RowType>
constexpr std::size_t get_type_index() noexcept
{
    using lunique = mp11::mp_unique<mp11::mp_list<RowType...>>;
    using index_t = mp11::mp_find<lunique, SpanRowType>;
    return index_t::value < mp11::mp_size<lunique>::value ? index_t::value : index_not_found;
}

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#endif
