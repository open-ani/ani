//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Dmitry Arkhipov (grisumbras@yandex.ru)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_SYSTEM_ERROR_HPP
#define BOOST_JSON_SYSTEM_ERROR_HPP

#include <boost/json/detail/config.hpp>
#include <boost/json/fwd.hpp>
#include <boost/assert/source_location.hpp>
#include <boost/json/result_for.hpp>
#include <boost/system/error_code.hpp>
#include <boost/system/result.hpp>
#include <boost/system/system_error.hpp>

namespace boost {
namespace json {

/** The type of error code used by the library.

    @note This alias is deprecated in favor of `boost::system::error_code`.
    It is included for backwards compatibility and shouldn't be used in new
    code. It will be removed completely in version 1.87.0.
*/
typedef boost::system::error_code
    BOOST_DEPRECATED("Use boost::system::error_code instead")
    error_code;

/** The type of error category used by the library.

    @note This alias is deprecated in favor of `boost::system::error_category`.
    It is included for backwards compatibility and shouldn't be used in new
    code. It will be removed completely in version 1.87.0.
*/
typedef boost::system::error_category
    BOOST_DEPRECATED("Use boost::system::error_category instead")
    error_category;

/** The type of error condition used by the library.

    @note This alias is deprecated in favor of
    `boost::system::error_condition`. It is included for backwards
    compatibility and shouldn't be used in new code. It will be removed
    completely in version 1.87.0.
*/
typedef boost::system::error_condition
    BOOST_DEPRECATED("Use boost::system::error_condition instead")
    error_condition;

/** The type of system error thrown by the library.

    @note This alias is deprecated in favor of `boost::system::system_error`.
    It is included for backwards compatibility and shouldn't be used in new
    code. It will be removed completely in version 1.87.0.
*/
typedef boost::system::system_error
    BOOST_DEPRECATED("Use boost::system::system_error instead")
    system_error;

/** The type of result returned by library functions

    This is an alias template used as the return type for functions that can
    either return a value, or fail with an error code. This is a brief
    synopsis of the type:

    @par Declaration
    @code
    template< class T >
    class result
    {
    public:
        // Return true if the result contains an error
        constexpr bool has_error() const noexcept;

        // These two return true if the result contains a value
        constexpr bool has_value() const noexcept;
        constexpr explicit operator bool() const noexcept;

        // Return the value or throw an exception if has_value() == false
        constexpr T& value();
        constexpr T const& value() const;

        // Return the value, assuming the result contains it
        constexpr T& operator*();
        constexpr T const& operator*() const;

        // Return the error, which is default constructed if has_error() == false
        constexpr error_code error() const noexcept;
        ...more
    };
    @endcode

    @par Usage
    Given the function @ref try_value_to with this signature:

    @code
    template< class T>
    result< T > try_value_to( const value& jv );
    @endcode

    The following statement captures the value in a variable upon success,
    otherwise throws:
    @code
    int n = try_value_to<int>( jv ).value();
    @endcode

    This statement captures the result in a variable and inspects the error
    condition:
    @code
    result< int > r = try_value_to<int>( jv );
    if( r )
        std::cout << *r;
    else
        std::cout << r.error();
    @endcode

    @note This alias is deprecated in favor of `boost::system::result`. It is
    included for backwards compatibility and shouldn't be used in new code. It
    will be removed completely in version 1.87.0.

    @tparam T The type of value held by the result.
*/
template< class T >
using
    result
#ifndef BOOST_MSVC
    BOOST_DEPRECATED("Use boost::system::result instead")
#endif
    = boost::system::result<T>;

} // namespace json
} // namespace boost

#endif
