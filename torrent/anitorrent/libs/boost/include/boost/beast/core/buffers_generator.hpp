//
// Copyright (c) 2022 Seth Heeren (sgheeren at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_CORE_BUFFERS_GENERATOR_HPP
#define BOOST_BEAST_CORE_BUFFERS_GENERATOR_HPP

#include <boost/beast/core/detail/config.hpp>
#include <boost/beast/core/detail/type_traits.hpp>
#include <boost/beast/core/error.hpp>
#include <boost/beast/core/stream_traits.hpp>
#include <boost/asio/async_result.hpp>
#include <type_traits>

namespace boost {
namespace beast {

/** Determine if type satisfies the <em>BuffersGenerator</em> requirements.

    This metafunction is used to determine if the specified type meets the
    requirements for a buffers generator.

    The static member `value` will evaluate to `true` if so, `false` otherwise.

    @tparam T a type to check
*/
#ifdef BOOST_BEAST_DOXYGEN
template <class T>
struct is_buffers_generator
    : integral_constant<bool, automatically_determined>
{
};
#else
template<class T, class = void>
struct is_buffers_generator
    : std::false_type
{
};

template<class T>
struct is_buffers_generator<
    T, detail::void_t<decltype(
        bool(std::declval<T const&>().is_done()),
        typename T::const_buffers_type(
            std::declval<T&>().prepare(
                std::declval<error_code&>())),
        std::declval<T&>().consume(
            std::size_t{})
    )>> : std::true_type
{
};
#endif

/** Write all output from a BuffersGenerator to a stream.

    This function is used to write all of the buffers generated
    by a caller-provided BuffersGenerator to a stream. The call
    will block until one of the following conditions is true:

    @li A call to the generator's `is_done` returns `false`.

    @li An error occurs.

    This operation is implemented in terms of one or more calls
    to the stream's `write_some` function.

    @param stream The stream to which the data is to be written.
    The type must support the <em>SyncWriteStream</em> concept.

    @param generator The generator to use.

    @param ec Set to the error, if any occurred.

    @return The number of bytes written to the stream.

    @see BuffersGenerator
*/
template<
    class SyncWriteStream,
    class BuffersGenerator
#if ! BOOST_BEAST_DOXYGEN
    , typename std::enable_if<is_buffers_generator<
        typename std::decay<BuffersGenerator>::
            type>::value>::type* = nullptr
#endif
    >
std::size_t
write(
    SyncWriteStream& stream,
    BuffersGenerator&& generator,
    beast::error_code& ec);

/** Write all output from a BuffersGenerator to a stream.

    This function is used to write all of the buffers generated
    by a caller-provided BuffersGenerator to a stream. The call
    will block until one of the following conditions is true:

    @li A call to the generator's `is_done` returns `false`.

    @li An error occurs.

    This operation is implemented in terms of one or more calls
    to the stream's `write_some` function.

    @param stream The stream to which the data is to be written.
    The type must support the <em>SyncWriteStream</em> concept.

    @param generator The generator to use.

    @return The number of bytes written to the stream.

    @throws system_error Thrown on failure.

    @see BuffersGenerator
*/
template<
    class SyncWriteStream,
    class BuffersGenerator
#if ! BOOST_BEAST_DOXYGEN
    , typename std::enable_if<is_buffers_generator<
        typename std::decay<BuffersGenerator>::
            type>::value>::type* = nullptr
#endif
    >
std::size_t
write(
    SyncWriteStream& stream,
    BuffersGenerator&& generator);

/** Write all output from a BuffersGenerator asynchronously to a
    stream.

    This function is used to write all of the buffers generated
    by a caller-provided `BuffersGenerator` to a stream. The
    function call always returns immediately. The asynchronous
    operation will continue until one of the following
    conditions is true:

    @li A call to the generator's `is_done` returns `false`.

    @li An error occurs.

    This operation is implemented in terms of zero or more calls
    to the stream's `async_write_some` function, and is known as
    a <em>composed operation</em>.  The program must ensure that
    the stream performs no other writes until this operation
    completes.

    @param stream The stream to which the data is to be written.
    The type must support the <em>SyncWriteStream</em> concept.

    @param generator The generator to use.

    @param token The completion handler to invoke when the
    operation completes. The implementation takes ownership of
    the handler by performing a decay-copy. The equivalent
    function signature of the handler must be:
    @code
    void handler(
        error_code const& error,        // result of operation
        std::size_t bytes_transferred   // the number of bytes written to the stream
    );
    @endcode
    If the handler has an associated immediate executor,
    an immediate completion will be dispatched to it.
    Otherwise, the handler will not be invoked from within
    this function. Invocation of the handler will be performed in a
    manner equivalent to using `net::post`.

    @see BuffersGenerator
*/
template<
    class AsyncWriteStream,
    class BuffersGenerator,
    BOOST_BEAST_ASYNC_TPARAM2 CompletionToken
    = net::default_completion_token_t<executor_type<AsyncWriteStream>>
#if !BOOST_BEAST_DOXYGEN
    , typename std::enable_if<is_buffers_generator<
        BuffersGenerator>::value>::type* = nullptr
#endif
    >
BOOST_BEAST_ASYNC_RESULT2(CompletionToken)
async_write(
    AsyncWriteStream& stream,
    BuffersGenerator generator,
    CompletionToken&& token
        = net::default_completion_token_t<executor_type<AsyncWriteStream>>{});

} // beast
} // boost

#include <boost/beast/core/impl/buffers_generator.hpp>

#endif
