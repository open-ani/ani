//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Dmitry Arkhipov (grisumbras@yandex.ru)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_FWD_HPP
#define BOOST_JSON_FWD_HPP

#include <boost/json/detail/config.hpp>

namespace boost {
namespace json {

// Forward declarations

#ifndef BOOST_JSON_DOCS

class array;
class object;
class string;
class value;

class key_value_pair;
class storage_ptr;

struct value_from_tag;

template<class T>
struct value_to_tag;

template<class T>
struct try_value_to_tag;

template<class T1, class T2>
struct result_for;

template<class T>
struct is_string_like;

template<class T>
struct is_sequence_like;

template<class T>
struct is_map_like;

template<class T>
struct is_tuple_like;

template<class T>
struct is_null_like;

template<class T>
struct is_described_class;

template<class T>
struct is_described_enum;

template<class T>
void value_from( T&& t, value& jv );

template<class T, class Context>
void value_from( T&& t, value& jv, Context const& ctx );

template<class T>
T value_to( value const & v );

template<class T, class Context>
T value_to( value const & v, Context const& ctx );

template<class T>
typename result_for<T, value>::type
try_value_to( value const & jv );

template<class T, class Context>
typename result_for<T, value>::type
try_value_to( value const & jv, Context const& ctx );

template<class T>
typename result_for<T, value>::type
result_from_errno( int e, boost::source_location const* loc ) noexcept;

#endif

} // namespace json
} // namespace boost

#endif
