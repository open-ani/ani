//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_WEBSOCKET_SSL_HPP
#define BOOST_BEAST_WEBSOCKET_SSL_HPP

#include <boost/beast/core/detail/config.hpp>
#include <boost/beast/websocket/teardown.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/ssl/stream.hpp>

namespace boost {
namespace beast {

/** Tear down a `net::ssl::stream`.

    This tears down a connection. The implementation will call
    the overload of this function based on the `Stream` parameter
    used to consruct the socket. When `Stream` is a user defined
    type, and not a `net::ip::tcp::socket` or any
    `net::ssl::stream`, callers are responsible for
    providing a suitable overload of this function.

    @note

    This function serves as a customization point and is not intended
    to be called directly.

    @param role The role of the local endpoint

    @param stream The stream to tear down.

    @param ec Set to the error if any occurred.
*/
template<class SyncStream>
void
teardown(
    role_type role,
    net::ssl::stream<SyncStream>& stream,
    error_code& ec);

/** Start tearing down a `net::ssl::stream`.

    This begins tearing down a connection asynchronously.
    The implementation will call the overload of this function
    based on the `Stream` parameter used to consruct the socket.
    When `Stream` is a user defined type, and not a
    `net::ip::tcp::socket` or any `net::ssl::stream`,
    callers are responsible for providing a suitable overload
    of this function.

    @note

    This function serves as a customization point and is not intended
    to be called directly.

    @param role The role of the local endpoint

    @param stream The stream to tear down.

    @param handler The completion handler to invoke when the operation
    completes. The implementation takes ownership of the handler by
    performing a decay-copy. The equivalent function signature of
    the handler must be:
    @code
    void handler(
        error_code const& error // result of operation
    );
    @endcode
    If the handler has an associated immediate executor,
    an immediate completion will be dispatched to it.
    Otherwise, the handler will not be invoked from within
    this function. Invocation of the handler will be performed in a
    manner equivalent to using `net::post`.

    @par Per-Operation Cancellation

    This asynchronous operation supports cancellation for the following
    net::cancellation_type values:

    @li @c net::cancellation_type::terminal
    @li @c net::cancellation_type::partial
    @li @c net::cancellation_type::total

    if they are also supported by the socket's @c async_teardown
    and @c async_shutdown operation.

*/
template<class AsyncStream, class TeardownHandler>
void
async_teardown(
    role_type role,
    net::ssl::stream<AsyncStream>& stream,
    TeardownHandler&& handler);

} // beast
} // boost

#include <boost/beast/websocket/impl/ssl.hpp>

#endif
