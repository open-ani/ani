//
// Copyright (c) 2022 Seth Heeren (sgheeren at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

#ifndef BOOST_BEAST_HTTP_IMPL_MESSAGE_GENERATOR_HPP
#define BOOST_BEAST_HTTP_IMPL_MESSAGE_GENERATOR_HPP

#include <boost/beast/http/message_generator.hpp>
#include <boost/smart_ptr/make_unique.hpp>
#include <boost/beast/core/buffers_generator.hpp>

namespace boost {
namespace beast {
namespace http {

template <bool isRequest, class Body, class Fields>
message_generator::message_generator(
    http::message<isRequest, Body, Fields>&& m)
    : impl_(boost::make_unique<
            generator_impl<isRequest, Body, Fields>>(
          std::move(m)))
{
}

template <bool isRequest, class Body, class Fields>
struct message_generator::generator_impl
    : message_generator::impl_base
{
    explicit generator_impl(
        http::message<isRequest, Body, Fields>&& m)
        : m_(std::move(m))
        , sr_(m_)
    {
    }

    bool
    is_done() override
    {
        return sr_.is_done();
    }

    const_buffers_type
    prepare(error_code& ec) override
    {
        sr_.next(ec, visit{*this});
        return current_;
    }

    void
    consume(std::size_t n) override
    {
        sr_.consume((std::min)(n, beast::buffer_bytes(current_)));
    }

    bool
    keep_alive() const noexcept override
    {
        return m_.keep_alive();
    }

private:
    static constexpr unsigned max_fixed_bufs = 12;

    http::message<isRequest, Body, Fields> m_;
    http::serializer<isRequest, Body, Fields> sr_;

    std::array<net::const_buffer, max_fixed_bufs> bs_;
    const_buffers_type current_ = bs_; // subspan

    struct visit
    {
        generator_impl& self_;

        template<class ConstBufferSequence>
        void
        operator()(error_code&, ConstBufferSequence const& buffers)
        {
            auto& s = self_.bs_;
            auto& cur = self_.current_;

            auto it = net::buffer_sequence_begin(buffers);

            std::size_t n =
                std::distance(it, net::buffer_sequence_end(buffers));

            n = (std::min)(s.size(), n);

            cur = { s.data(), n };
            std::copy_n(it, n, cur.begin());
        }
    };

};

} // namespace http
} // namespace beast
} // namespace boost

#endif
