//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2020 Krystian Stasiowski (sdkrystian@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_PARSE_INTO_HPP
#define BOOST_JSON_IMPL_PARSE_INTO_HPP

#include <boost/json/basic_parser_impl.hpp>
#include <boost/json/error.hpp>
#include <istream>

namespace boost {
namespace json {

template<class V>
void
parse_into(
    V& v,
    string_view sv,
    system::error_code& ec,
    parse_options const& opt )
{
    parser_for<V> p( opt, &v );

    std::size_t n = p.write_some( false, sv.data(), sv.size(), ec );

    if( !ec && n < sv.size() )
    {
        BOOST_JSON_FAIL( ec, error::extra_data );
    }
}

template<class V>
void
parse_into(
    V& v,
    string_view sv,
    std::error_code& ec,
    parse_options const& opt )
{
    system::error_code jec;
    parse_into(v, sv, jec, opt);
    ec = jec;
}

template<class V>
void
parse_into(
    V& v,
    string_view sv,
    parse_options const& opt )
{
    system::error_code ec;
    parse_into(v, sv, ec, opt);
    if( ec.failed() )
        detail::throw_system_error( ec );
}

template<class V>
void
parse_into(
    V& v,
    std::istream& is,
    system::error_code& ec,
    parse_options const& opt )
{
    parser_for<V> p( opt, &v );

    char read_buffer[BOOST_JSON_STACK_BUFFER_SIZE];
    do
    {
        if( is.eof() )
        {
            p.write_some(false, nullptr, 0, ec);
            break;
        }

        if( !is )
        {
            BOOST_JSON_FAIL( ec, error::input_error );
            break;
        }

        is.read(read_buffer, sizeof(read_buffer));
        std::size_t const consumed = static_cast<std::size_t>( is.gcount() );

        std::size_t const n = p.write_some( true, read_buffer, consumed, ec );
        if( !ec.failed() && n < consumed )
        {
            BOOST_JSON_FAIL( ec, error::extra_data );
        }
    }
    while( !ec.failed() );
}

template<class V>
void
parse_into(
    V& v,
    std::istream& is,
    std::error_code& ec,
    parse_options const& opt )
{
    system::error_code jec;
    parse_into(v, is, jec, opt);
    ec = jec;
}

template<class V>
void
parse_into(
    V& v,
    std::istream& is,
    parse_options const& opt )
{
    system::error_code ec;
    parse_into(v, is, ec, opt);
    if( ec.failed() )
        detail::throw_system_error( ec );
}

} // namespace boost
} // namespace json

#endif
