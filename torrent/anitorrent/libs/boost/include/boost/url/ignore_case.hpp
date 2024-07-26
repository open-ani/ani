//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IGNORE_CASE_HPP
#define BOOST_URL_IGNORE_CASE_HPP

#include <boost/url/detail/config.hpp>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
struct ignore_case_t
{
};
#endif

/** Ignore case when comparing

    This value may be optionally passed to
    functions accepting a parameter of type
    @ref ignore_case_param to indicate that
    comparisons should be case-insensitive.
*/
constexpr
#ifdef BOOST_URL_DOCS
__implementation_defined__
#else
ignore_case_t
#endif
ignore_case{};

/** An optional parameter to determine case-sensitivity

    Functions may use parameters of this type
    to allow the user to optionally indicate
    that comparisons should be case-insensitive
    when the value @ref ignore_case is passed.
*/
class ignore_case_param
{
    /** True if an algorithm should ignore case

        Functions accepting a parameter of type
        `ignore_case_param` can check `value`
        to determine if the caller has indicated
        that comparisons should ignore case.
    */
    bool value_ = false;

public:
    /** Constructor

        By default, comparisons are
        case-sensitive.

        @par Example
        This function performs case-sensitive
        comparisons when called with no
        arguments:
        @code
        void f( ignore_case_param = {} );
        @endcode
    */
    constexpr
    ignore_case_param() noexcept = default;

    /** Constructor

        Construction from @ref ignore_case
        indicates that comparisons should
        be case-insensitive.

        @par Example
        When @ref ignore_case is passed as
        an argument, this function ignores
        case when performing comparisons:
        @code
        void f( ignore_case_param = {} );
        @endcode
    */
    constexpr
    ignore_case_param(
    #ifdef BOOST_URL_DOCS
        __implementation_defined__
    #else
        ignore_case_t
    #endif
        ) noexcept
        : value_(true)
    {
    }

    /** True if an algorithm should ignore case

        Values of type `ignore_case_param`
        evaluate to true when constructed
        with the constant @ref ignore_case.
        Otherwise, they are default-constructed
        and evaluate to `false`.
    */
    operator
    bool() const noexcept
    {
        return value_;
    }
};

} // urls
} // boost

#endif
