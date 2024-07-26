//
// Copyright (c) 2019-2024 Ruben Perez Hidalgo (rubenperez038 at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_MYSQL_DETAIL_EXECUTION_CONCEPTS_HPP
#define BOOST_MYSQL_DETAIL_EXECUTION_CONCEPTS_HPP

#include <boost/mysql/statement.hpp>
#include <boost/mysql/string_view.hpp>

#include <boost/mysql/detail/config.hpp>

#include <type_traits>

#ifdef BOOST_MYSQL_HAS_CONCEPTS

namespace boost {
namespace mysql {

// Forward decls
template <class... StaticRow>
class static_execution_state;

template <class... StaticRow>
class static_results;

class execution_state;
class results;

namespace detail {

// Execution state
template <class T>
struct is_static_execution_state : std::false_type
{
};

template <class... T>
struct is_static_execution_state<static_execution_state<T...>> : std::true_type
{
};

template <class T>
concept execution_state_type = std::is_same_v<T, execution_state> || is_static_execution_state<T>::value;

// Results
template <class T>
struct is_static_results : std::false_type
{
};

template <class... T>
struct is_static_results<static_results<T...>> : std::true_type
{
};

template <class T>
concept results_type = std::is_same_v<T, results> || is_static_results<T>::value;

// Execution request
template <class T>
struct is_bound_statement_tuple : std::false_type
{
};

template <class T>
struct is_bound_statement_tuple<bound_statement_tuple<T>> : std::true_type
{
};

template <class T>
struct is_bound_statement_range : std::false_type
{
};

template <class T>
struct is_bound_statement_range<bound_statement_iterator_range<T>> : std::true_type
{
};

template <class T>
struct is_execution_request
{
    using without_cvref = typename std::remove_cv<typename std::remove_reference<T>::type>::type;
    static constexpr bool value = std::is_convertible<T, string_view>::value ||
                                  is_bound_statement_tuple<without_cvref>::value ||
                                  is_bound_statement_range<without_cvref>::value;
};

template <class T>
concept execution_request = is_execution_request<T>::value;

}  // namespace detail
}  // namespace mysql
}  // namespace boost

#define BOOST_MYSQL_EXECUTION_STATE_TYPE ::boost::mysql::detail::execution_state_type
#define BOOST_MYSQL_RESULTS_TYPE ::boost::mysql::detail::results_type
#define BOOST_MYSQL_EXECUTION_REQUEST ::boost::mysql::detail::execution_request

#else

#define BOOST_MYSQL_EXECUTION_STATE_TYPE class
#define BOOST_MYSQL_RESULTS_TYPE class
#define BOOST_MYSQL_EXECUTION_REQUEST class

#endif

#endif
