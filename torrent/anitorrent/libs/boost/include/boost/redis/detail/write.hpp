/* Copyright (c) 2018-2023 Marcelo Zimbres Silva (mzimbres@gmail.com)
 *
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE.txt)
 */

#ifndef BOOST_REDIS_WRITE_HPP
#define BOOST_REDIS_WRITE_HPP

#include <boost/asio/write.hpp>
#include <boost/redis/request.hpp>

namespace boost::redis::detail {

/** \brief Writes a request synchronously.
 *  \ingroup low-level-api
 *
 *  \param stream Stream to write the request to.
 *  \param req Request to write.
 */
template<class SyncWriteStream>
auto write(SyncWriteStream& stream, request const& req)
{
   return asio::write(stream, asio::buffer(req.payload()));
}

template<class SyncWriteStream>
auto write(SyncWriteStream& stream, request const& req, system::error_code& ec)
{
   return asio::write(stream, asio::buffer(req.payload()), ec);
}

/** \brief Writes a request asynchronously.
 *  \ingroup low-level-api
 *
 *  \param stream Stream to write the request to.
 *  \param req Request to write.
 *  \param token Asio completion token.
 */
template<
   class AsyncWriteStream,
   class CompletionToken = asio::default_completion_token_t<typename AsyncWriteStream::executor_type>
   >
auto async_write(
   AsyncWriteStream& stream,
   request const& req,
   CompletionToken&& token =
      asio::default_completion_token_t<typename AsyncWriteStream::executor_type>{})
{
   return asio::async_write(stream, asio::buffer(req.payload()), token);
}

} // boost::redis::detail

#endif // BOOST_REDIS_WRITE_HPP
