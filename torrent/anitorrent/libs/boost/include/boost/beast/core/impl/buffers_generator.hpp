//
// Copyright (c) 2022 Seth Heeren (sgheeren at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_CORE_IMPL_BUFFERS_GENERATOR_HPP
#define BOOST_BEAST_CORE_IMPL_BUFFERS_GENERATOR_HPP

#include <boost/beast/core/buffers_generator.hpp>

#include <boost/asio/write.hpp>
#include <boost/asio/async_result.hpp>
#include <boost/asio/compose.hpp>
#include <boost/asio/coroutine.hpp>

#include <boost/beast/core/buffer_traits.hpp>
#include <boost/beast/core/stream_traits.hpp>

#include <boost/throw_exception.hpp>
#include <type_traits>

namespace boost {
namespace beast {

namespace detail {

template <
    class AsyncWriteStream,
    class BuffersGenerator>
struct write_buffers_generator_op
    : boost::asio::coroutine
{
    write_buffers_generator_op(
        AsyncWriteStream& s, BuffersGenerator g)
        : s_(s)
        , g_(std::move(g))
    {
    }

    template<class Self>
    void operator()(
        Self& self, error_code ec = {}, std::size_t n = 0)
    {
        BOOST_ASIO_CORO_REENTER(*this)
        {
            while(! g_.is_done())
            {
                BOOST_ASIO_CORO_YIELD
                {
                    auto cb = g_.prepare(ec);
                    if(ec)
                        goto complete;
                    s_.async_write_some(
                        cb, std::move(self));
                }
                if(ec)
                    goto complete;

                g_.consume(n);

                total_ += n;
            }

        complete:
            self.complete(ec, total_);
        }
    }

private:
    AsyncWriteStream& s_;
    BuffersGenerator g_;
    std::size_t total_ = 0;
};

} // detail

template<
    class SyncWriteStream,
    class BuffersGenerator,
    typename std::enable_if< //
        is_buffers_generator<typename std::decay<
            BuffersGenerator>::type>::value>::type* /*= nullptr*/
    >
size_t
write(
    SyncWriteStream& stream,
    BuffersGenerator&& generator,
    beast::error_code& ec)
{
    static_assert(
        is_sync_write_stream<SyncWriteStream>::value,
        "SyncWriteStream type requirements not met");

    ec.clear();
    size_t total = 0;
    while(! generator.is_done())
    {
        auto cb = generator.prepare(ec);
        if(ec)
            break;

        size_t n = net::write(stream, cb, ec);

        if(ec)
            break;

        generator.consume(n);
        total += n;
    }

    return total;
}

//----------------------------------------------------------

template<
    class SyncWriteStream,
    class BuffersGenerator,
    typename std::enable_if<is_buffers_generator<
        typename std::decay<BuffersGenerator>::type>::value>::
        type* /*= nullptr*/
    >
std::size_t
write(SyncWriteStream& stream, BuffersGenerator&& generator)
{
    static_assert(
        is_sync_write_stream<SyncWriteStream>::value,
        "SyncWriteStream type requirements not met");
    beast::error_code ec;
    std::size_t n = write(
        stream, std::forward<BuffersGenerator>(generator), ec);
    if(ec)
        BOOST_THROW_EXCEPTION(system_error{ ec });
    return n;
}

//----------------------------------------------------------

template<
    class AsyncWriteStream,
    class BuffersGenerator,
    BOOST_BEAST_ASYNC_TPARAM2 CompletionToken,
    typename std::enable_if<is_buffers_generator<
        BuffersGenerator>::value>::type* /*= nullptr*/
    >
BOOST_BEAST_ASYNC_RESULT2(CompletionToken)
async_write(
    AsyncWriteStream& stream,
    BuffersGenerator generator,
    CompletionToken&& token)
{
    static_assert(
        beast::is_async_write_stream<AsyncWriteStream>::value,
        "AsyncWriteStream type requirements not met");

    return net::async_compose< //
        CompletionToken,
        void(error_code, std::size_t)>(
        detail::write_buffers_generator_op<
            AsyncWriteStream,
            BuffersGenerator>{ stream, std::move(generator) },
        token,
        stream);
}

} // namespace beast
} // namespace boost

#endif
