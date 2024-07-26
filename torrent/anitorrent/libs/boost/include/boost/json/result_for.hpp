//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2024 Dmitry Arkhipov (grisumbras@yandex.ru)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_RESULT_FOR_HPP
#define BOOST_JSON_RESULT_FOR_HPP

#include <boost/json/detail/config.hpp>
#include <boost/json/fwd.hpp>
#include <boost/assert/source_location.hpp>
#include <boost/system/result.hpp>

namespace boost {
namespace json {

/**
   Helper trait that returns @ref result

   The primary template is an incomplete type. The library provides a partial
   specialisation `result_for<T1, value>`, that has nested type alias `type`
   that aliases the type `result<T1>`.

   The purpose of this trait is to let users provide non-throwing conversions
   for their types without creating a physical dependency on Boost.Json. For
   example:

   @code
   namespace boost
   {
   namespace json
   {

   template<class T>
   struct value_to_tag;

   template<class T1, class T2>
   struct result_for;
   }
   }

   namespace mine
   {
       class my_class;
       ...
       template<class JsonValue>
       boost::json::result_for<my_class, JsonValue>
       tag_invoke(boost::json::try_value_to_tag<my_class>, const JsonValue& jv)
       { ... }
   }
   @endcode

    @see @ref try_value_to, @ref try_value_to_tag
*/
template <class T1, class T2>
struct result_for;

/** Create @ref result storing a portable error code

    This function constructs a `boost::system::result<T>` that stores
    `boost::system::error_code` with `value()` equal to `e` and `category()`
    equal to `boost::system::generic_category()`. <br>

    The main use for this function is in implementation of functions returning
    @ref result, without including `boost/json/system_error.hpp` or even
    `<system_error>`. In particular, it may be useful for customizations of
    @ref try_value_to without creating a physical dependency on Boost.JSON.
    For example:

    @code
    #include <cerrno>
    #include <boost/assert/source_location.hpp>

    namespace boost
    {
    namespace json
    {

    class value;

    template<class T>
    struct try_value_to_tag;

    template<class T1, class T2>
    struct result_for;

    template <class T>
    typename result_for<T, value>::type
    result_from_errno(int e, boost::source_location const* loc) noexcept

    }
    }

    namespace mine
    {

    class my_class;
    ...
    template<class JsonValue>
    boost::json::result_for<my_class, JsonValue>
    tag_invoke(boost::json::try_value_to_tag<my_class>, const JsonValue& jv)
    {
        BOOST_STATIC_CONSTEXPR boost::source_location loc = BOOST_CURRENT_LOCATION;
        if( !jv.is_null() )
            return boost::json::result_from_errno<my_class>(EINVAL, &loc);
        return my_class();
    }

    }
    @endcode

    @par Exception Safety
    Does not throw exceptions.

    @tparam T The value type of returned `result`.

    @param e The error value.

    @param loc The error location.

    @returns `boost::system::error_code` with `value()` equal to `e` and
    `category()` equal to `boost::system::generic_category()`.

    @see @ref try_value_to_tag, @ref try_value_to, @ref result_for,
    <a href="https://www.boost.org/doc/libs/develop/libs/system/doc/html/system.html#ref_generic_category">
        `boost::system::generic_category`</a>,
    <a href="https://www.boost.org/doc/libs/master/libs/assert/doc/html/assert.html#source_location_support">
        `boost::source_location`</a>.
*/
template <class T>
typename result_for<T, value>::type
result_from_errno(int e, boost::source_location const* loc) noexcept
{
    system::error_code ec(e, system::generic_category(), loc);
    return {system::in_place_error, ec};
}

} // namespace json
} // namespace boost

#endif // BOOST_JSON_RESULT_FOR_HPP
