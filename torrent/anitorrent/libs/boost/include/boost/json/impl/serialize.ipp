//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_SERIALIZE_IPP
#define BOOST_JSON_IMPL_SERIALIZE_IPP

#include <boost/json/serialize.hpp>
#include <boost/json/serializer.hpp>
#include <ostream>

namespace boost {
namespace json {

namespace {

int serialize_xalloc = std::ios::xalloc();

enum class serialize_stream_flags : long
{
    allow_infinity_and_nan = 1,
};

std::underlying_type<serialize_stream_flags>::type
to_bitmask( serialize_options const& opts )
{
    using E = serialize_stream_flags;
    using I = std::underlying_type<E>::type;
    return (opts.allow_infinity_and_nan
        ? static_cast<I>(E::allow_infinity_and_nan) : 0);
}

serialize_options
get_stream_flags( std::ostream& os )
{
    auto const flags = os.iword(serialize_xalloc);

    serialize_options opts;
    using E = serialize_stream_flags;
    using I = std::underlying_type<E>::type;
    opts.allow_infinity_and_nan =
        flags & static_cast<I>(E::allow_infinity_and_nan);
    return opts;
}

} // namespace

static
void
serialize_impl(
    std::string& s,
    serializer& sr)
{
    // serialize to a small buffer to avoid
    // the first few allocations in std::string
    char buf[BOOST_JSON_STACK_BUFFER_SIZE];
    string_view sv;
    sv = sr.read(buf);
    if(sr.done())
    {
        // fast path
        s.append(
            sv.data(), sv.size());
        return;
    }
    std::size_t len = sv.size();
    s.reserve(len * 2);
    s.resize(s.capacity());
    BOOST_ASSERT(
        s.size() >= len * 2);
    std::memcpy(&s[0],
        sv.data(), sv.size());
    auto const lim =
        s.max_size() / 2;
    for(;;)
    {
        sv = sr.read(
            &s[0] + len,
            s.size() - len);
        len += sv.size();
        if(sr.done())
            break;
        // growth factor 2x
        if(s.size() < lim)
            s.resize(s.size() * 2);
        else
            s.resize(2 * lim);
    }
    s.resize(len);
}

std::string
serialize(
    value const& jv,
    serialize_options const& opts)
{
    unsigned char buf[256];
    serializer sr(
        storage_ptr(),
        buf,
        sizeof(buf),
        opts);
    sr.reset(&jv);
    std::string s;
    serialize_impl(s, sr);
    return s;
}

std::string
serialize(
    array const& arr,
    serialize_options const& opts)
{
    unsigned char buf[256];
    serializer sr(
        storage_ptr(),
        buf,
        sizeof(buf),
        opts);
    std::string s;
    sr.reset(&arr);
    serialize_impl(s, sr);
    return s;
}

std::string
serialize(
    object const& obj,
    serialize_options const& opts)
{
    unsigned char buf[256];
    serializer sr(
        storage_ptr(),
        buf,
        sizeof(buf),
        opts);
    std::string s;
    sr.reset(&obj);
    serialize_impl(s, sr);
    return s;
}

std::string
serialize(
    string const& str,
    serialize_options const& opts)
{
    return serialize( str.subview(), opts );
}

// this is here for key_value_pair::key()
std::string
serialize(
    string_view sv,
    serialize_options const& opts)
{
    unsigned char buf[256];
    serializer sr(
        storage_ptr(),
        buf,
        sizeof(buf),
        opts);
    std::string s;
    sr.reset(sv);
    serialize_impl(s, sr);
    return s;
}

//----------------------------------------------------------

//[example_operator_lt__lt_
// Serialize a value into an output stream

std::ostream&
operator<<( std::ostream& os, value const& jv )
{
    // Create a serializer
    serializer sr( get_stream_flags(os) );

    // Set the serializer up for our value
    sr.reset( &jv );

    // Loop until all output is produced.
    while( ! sr.done() )
    {
        // Use a local buffer to avoid allocation.
        char buf[ BOOST_JSON_STACK_BUFFER_SIZE ];

        // Fill our buffer with serialized characters and write it to the output stream.
        os << sr.read( buf );
    }

    return os;
}
//]

static
void
to_ostream(
    std::ostream& os,
    serializer& sr)
{
    while(! sr.done())
    {
        char buf[BOOST_JSON_STACK_BUFFER_SIZE];
        auto s = sr.read(buf);
        os.write(s.data(), s.size());
    }
}

std::ostream&
operator<<(
    std::ostream& os,
    array const& arr)
{
    serializer sr( get_stream_flags(os) );
    sr.reset(&arr);
    to_ostream(os, sr);
    return os;
}

std::ostream&
operator<<(
    std::ostream& os,
    object const& obj)
{
    serializer sr( get_stream_flags(os) );
    sr.reset(&obj);
    to_ostream(os, sr);
    return os;
}

std::ostream&
operator<<(
    std::ostream& os,
    string const& str)
{
    serializer sr( get_stream_flags(os) );
    sr.reset(&str);
    to_ostream(os, sr);
    return os;
}

std::ostream&
operator<<( std::ostream& os, serialize_options const& opts )
{
    os.iword(serialize_xalloc) = to_bitmask(opts);
    return os;
}

} // namespace json
} // namespace boost

#endif
