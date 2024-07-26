//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_PARSER_IPP
#define BOOST_JSON_IMPL_PARSER_IPP

#include <boost/json/parser.hpp>
#include <boost/json/basic_parser_impl.hpp>
#include <boost/json/error.hpp>
#include <cstring>
#include <stdexcept>
#include <utility>

namespace boost {
namespace json {

parser::
parser(
    storage_ptr sp,
    parse_options const& opt,
    unsigned char* buffer,
    std::size_t size) noexcept
    : p_(
        opt,
        std::move(sp),
        buffer,
        size)
{
    reset();
}

parser::
parser(
    storage_ptr sp,
    parse_options const& opt) noexcept
    : p_(
        opt,
        std::move(sp),
        nullptr,
        0)
{
    reset();
}

void
parser::
reset(storage_ptr sp) noexcept
{
    p_.reset();
    p_.handler().st.reset(sp);
}

std::size_t
parser::
write_some(
    char const* data,
    std::size_t size,
    system::error_code& ec)
{
    auto const n = p_.write_some(
        false, data, size, ec);
    BOOST_ASSERT(ec || p_.done());
    return n;
}

std::size_t
parser::
write_some(
    char const* data,
    std::size_t size,
    std::error_code& ec)
{
    system::error_code jec;
    std::size_t const result = write_some(data, size, jec);
    ec = jec;
    return result;
}

std::size_t
parser::
write_some(
    char const* data,
    std::size_t size)
{
    system::error_code ec;
    auto const n = write_some(
        data, size, ec);
    if(ec)
        detail::throw_system_error( ec );
    return n;
}

std::size_t
parser::
write(
    char const* data,
    std::size_t size,
    system::error_code& ec)
{
    auto const n = write_some(
        data, size, ec);
    if(! ec && n < size)
    {
        BOOST_JSON_FAIL(ec, error::extra_data);
        p_.fail(ec);
    }
    return n;
}

std::size_t
parser::
write(
    char const* data,
    std::size_t size,
    std::error_code& ec)
{
    system::error_code jec;
    std::size_t const result = write(data, size, jec);
    ec = jec;
    return result;
}

std::size_t
parser::
write(
    char const* data,
    std::size_t size)
{
    system::error_code ec;
    auto const n = write(
        data, size, ec);
    if(ec)
        detail::throw_system_error( ec );
    return n;
}

value
parser::
release()
{
    if( ! p_.done())
    {
        // prevent undefined behavior
        if(! p_.last_error())
        {
            system::error_code ec;
            BOOST_JSON_FAIL(ec, error::incomplete);
            p_.fail(ec);
        }
        detail::throw_system_error(
            p_.last_error());
    }
    return p_.handler().st.release();
}

} // namespace json
} // namespace boost

#endif
