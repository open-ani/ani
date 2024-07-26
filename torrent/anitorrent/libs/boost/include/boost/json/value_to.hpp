//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2020 Krystian Stasiowski (sdkrystian@gmail.com)
// Copyright (c) 2022 Dmitry Arkhipov (grisumbras@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_VALUE_TO_HPP
#define BOOST_JSON_VALUE_TO_HPP

#include <boost/json/detail/value_to.hpp>

namespace boost {
namespace json {

/** Convert a @ref value to an object of type `T`.

    This function attempts to convert a @ref value
    to `T` using

    @li one of @ref value's accessors, or

    @li a library-provided generic conversion, or

    @li a user-provided overload of `tag_invoke`.

    Out of the box the function supports types satisfying
    <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    arrays, arithmetic types, `bool`, `std::tuple`, `std::pair`,
    `std::variant`, `std::optional`, `std::monostate`, and `std::nullopt_t`.

    Conversion of other types is done by calling an overload of `tag_invoke`
    found by argument-dependent lookup. Its signature should be similar to:

    @code
    template< class FullContext >
    T tag_invoke( value_to_tag<T>, const value&, const Context& , const FullContext& );
    @endcode

    or

    @code
    T tag_invoke( value_to_tag<T>, const value&, const Context& );
    @endcode

    or

    @code
    result<T> tag_invoke( value_to_tag<T>, const value& );
    @endcode

    The overloads are checked for existence in that order and the first that
    matches will be selected. <br>

    The object returned by the function call is returned by @ref value_to as
    the result of the conversion. <br>

    The `ctx` argument can be used either as a tag type to provide conversions
    for third-party types, or to pass extra data to the conversion function.

    @par Constraints
    @code
    ! std::is_reference< T >::value
    @endcode

    @par Exception Safety
    Strong guarantee.

    @tparam T The type to convert to.

    @tparam Context The type of context passed to the conversion function.

    @returns `jv` converted to `result<T>`.

    @param jv The @ref value to convert.

    @param ctx Context passed to the conversion function.

    @see @ref value_to_tag, @ref value_from,
    <a href="http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2019/p1895r0.pdf">
        tag_invoke: A general pattern for supporting customisable functions</a>
*/
template< class T, class Context >
T
value_to( value const& jv, Context const& ctx )
{
    BOOST_STATIC_ASSERT(! std::is_reference<T>::value);
    using bare_T = detail::remove_cvref<T>;
    BOOST_STATIC_ASSERT(detail::conversion_round_trips<
        Context, bare_T, detail::value_to_conversion>::value);
    using cat = detail::value_to_category<Context, bare_T>;
    return detail::value_to_impl( cat(), value_to_tag<bare_T>(), jv, ctx );
}

/** Convert a @ref value to an object of type `T`.

    This function attempts to convert a @ref value
    to `T` using

    @li one of @ref value's accessors, or

    @li a library-provided generic conversion, or

    @li a user-provided overload of `tag_invoke`.

    Out of the box the function supports types satisfying
    <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    arrays, arithmetic types, `bool`, `std::tuple`, `std::pair`,
    `std::variant`, `std::optional`, `std::monostate`, and `std::nullopt_t`.

    Conversion of other types is done by calling an overload of `tag_invoke`
    found by argument-dependent lookup. Its signature should be similar to:

    @code
    T tag_invoke( value_to_tag<T>, const value& );
    @endcode

    The object returned by the function call is
    returned by @ref value_to as the result of the
    conversion.

    @par Constraints
    @code
    ! std::is_reference< T >::value
    @endcode

    @par Exception Safety
    Strong guarantee.

    @tparam T The type to convert to.

    @returns `jv` converted to `T`.

    @param jv The @ref value to convert.

    @see @ref value_to_tag, @ref value_from,
    <a href="http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2019/p1895r0.pdf">
        tag_invoke: A general pattern for supporting customisable functions</a>
*/
template<class T>
T
value_to(const value& jv)
{
    return value_to<T>( jv, detail::no_context() );
}

/** Convert a @ref value to a @ref result of `T`.

    This function attempts to convert a @ref value
    to `result<T>` using

    @li one of @ref value's accessors, or

    @li a library-provided generic conversion, or

    @li a user-provided overload of `tag_invoke`.

    Out of the box the function supports types satisfying
    <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    arrays, arithmetic types, `bool`, `std::tuple`, `std::pair`,
    `std::variant`, `std::optional`, `std::monostate`, and `std::nullopt_t`.

    Conversion of other types is done by calling an overload of `tag_invoke`
    found by argument-dependent lookup. Its signature should be similar to:

    @code
    template< class FullContext >
    result<T> tag_invoke( try_value_to_tag<T>, const value&, const Context& , const FullContext& );
    @endcode

    or

    @code
    result<T> tag_invoke( try_value_to_tag<T>, const value&, const Context& );
    @endcode

    or

    @code
    result<T> tag_invoke( try_value_to_tag<T>, const value& );
    @endcode

    The overloads are checked for existence in that order and the first that
    matches will be selected. <br>

    If an error occurs during conversion, the result will store the error code
    associated with the error. If an exception is thrown, the function will
    attempt to retrieve the associated error code and return it, otherwise it
    will return `error::exception`, unless the exception type is
    `std::bad_alloc`, which will be allowed to propagate. <br>

    The `ctx` argument can be used either as a tag type to provide conversions
    for third-party types, or to pass extra data to the conversion function.

    @par Constraints
    @code
    ! std::is_reference< T >::value
    @endcode

    @par Exception Safety
    Strong guarantee.

    @tparam T The type to convert to.

    @tparam Context The type of context passed to the conversion function.

    @param jv The @ref value to convert.

    @param ctx Context passed to the conversion function.

    @returns `jv` converted to `result<T>`.

    @see @ref value_to_tag, @ref value_to, @ref value_from,
    <a href="http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2019/p1895r0.pdf">
        tag_invoke: A general pattern for supporting customisable functions</a>
*/
template< class T, class Context >
typename result_for<T, value>::type
try_value_to( value const& jv, Context const& ctx )
{
    BOOST_STATIC_ASSERT(! std::is_reference<T>::value);
    using bare_T = detail::remove_cvref<T>;
    BOOST_STATIC_ASSERT(detail::conversion_round_trips<
        Context, bare_T, detail::value_to_conversion>::value);
    using cat = detail::value_to_category<Context, bare_T>;
    return detail::value_to_impl(
        cat(), try_value_to_tag<bare_T>(), jv, ctx );
}

/** Convert a @ref value to a @ref result of `T`.

    This function attempts to convert a @ref value
    to `result<T>` using

    @li one of @ref value's accessors, or

    @li a library-provided generic conversion, or

    @li a user-provided overload of `tag_invoke`.

    Out of the box the function supports types satisfying
    <a href="https://en.cppreference.com/w/cpp/named_req/SequenceContainer"><em>SequenceContainer</em></a>,
    arrays, arithmetic types, `bool`, `std::tuple`, `std::pair`,
    `std::variant`, `std::optional`, `std::monostate`, and `std::nullopt_t`.

    Conversion of other types is done by calling an overload of `tag_invoke`
    found by argument-dependent lookup. Its signature should be similar to:

    @code
    result<T> tag_invoke( try_value_to_tag<T>, const value& );
    @endcode

    If an error occurs during conversion, the result will store the error code
    associated with the error. If an exception is thrown, the function will
    attempt to retrieve the associated error code and return it, otherwise it
    will return `error::exception`, unless the exception type is
    `std::bad_alloc`, which will be allowed to propagate.

    @par Constraints
    @code
    ! std::is_reference< T >::value
    @endcode

    @par Exception Safety
    Strong guarantee.

    @tparam T The type to convert to.

    @param jv The @ref value to convert.

    @returns `jv` converted to `result<T>`.

    @see @ref value_to_tag, @ref value_to, @ref value_from,
    <a href="http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2019/p1895r0.pdf">
        tag_invoke: A general pattern for supporting customisable functions</a>
*/
template<class T>
typename result_for<T, value>::type
try_value_to(const value& jv)
{
    return try_value_to<T>( jv, detail::no_context() );
}

/** Convert a @ref value to an object of type `T`.

    This overload is **deleted** and participates in overload resolution only
    when `U` is not @ref value. The overload exists to prevent unintented
    creation of temporary @ref value instances, e.g.

    @code
    auto flag = value_to<bool>(true);
    @endcode
*/
template<class T, class U
#ifndef BOOST_JSON_DOCS
    , class = typename std::enable_if<!std::is_same<U, value>::value>::type
#endif
>
T
value_to(U const& jv) = delete;

/** Determine a @ref value can be converted to `T`.

    If @ref value can be converted to `T` via a
    call to @ref value_to, the static data member `value`
    is defined as `true`. Otherwise, `value` is
    defined as `false`.

    @see @ref value_to
*/
#ifdef BOOST_JSON_DOCS
template<class T>
using has_value_to = __see_below__;
#else
template<class T>
using has_value_to = detail::can_convert<
    detail::remove_cvref<T>, detail::value_to_conversion>;
#endif

} // namespace json
} // namespace boost

#endif
