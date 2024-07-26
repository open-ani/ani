//
// Copyright (c) 2022 Seth Heeren (sgheeren at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//
#ifndef BOOST_BEAST_HTTP_MESSAGE_GENERATOR_HPP
#define BOOST_BEAST_HTTP_MESSAGE_GENERATOR_HPP

#include <boost/beast/core/span.hpp>
#include <boost/beast/http/message.hpp>
#include <boost/beast/http/serializer.hpp>
#include <memory>

namespace boost {
namespace beast {
namespace http {

/** Type-erased buffers generator for @ref http::message
   
    Implements the BuffersGenerator concept for any concrete instance of the
    @ref http::message template.
   
    @ref http::message_generator takes ownership of a message on construction,
    erasing the concrete type from the interface.
   
    This makes it practical for use in server applications to implement request
    handling:
   
    @code
    template <class Body, class Fields>
    http::message_generator handle_request(
        string_view doc_root,
        http::request<Body, Fields>&& request);
    @endcode
   
    The @ref beast::write and @ref beast::async_write operations are provided
    for BuffersGenerator. The @ref http::message::keep_alive property is made
    available for use after writing the message.
*/
class message_generator
{
public:
    using const_buffers_type = span<net::const_buffer>;

    template <bool isRequest, class Body, class Fields>
    message_generator(http::message<isRequest, Body, Fields>&&);

    /// `BuffersGenerator`
    bool is_done() const {
        return impl_->is_done();
    }

    /// `BuffersGenerator`
    const_buffers_type
    prepare(error_code& ec)
    {
        return impl_->prepare(ec);
    }

    /// `BuffersGenerator`
    void
    consume(std::size_t n)
    {
        impl_->consume(n);
    }

    /// Returns the result of `m.keep_alive()` on the underlying message
    bool
    keep_alive() const noexcept
    {
        return impl_->keep_alive();
    }

private:
    struct impl_base
    {
        virtual ~impl_base() = default;
        virtual bool is_done() = 0;
        virtual const_buffers_type prepare(error_code& ec) = 0;
        virtual void consume(std::size_t n) = 0;
        virtual bool keep_alive() const noexcept = 0;
    };

    std::unique_ptr<impl_base> impl_;

    template <bool isRequest, class Body, class Fields>
    struct generator_impl;
};

} // namespace http
} // namespace beast
} // namespace boost

#include <boost/beast/http/impl/message_generator.hpp>

#endif
