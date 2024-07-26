//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IPV6_ADDRESS_HPP
#define BOOST_URL_IPV6_ADDRESS_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error.hpp>
#include <boost/url/error_types.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/grammar/string_token.hpp>
#include <array>
#include <cstdint>
#include <iosfwd>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
class ipv4_address;
#endif

/** An IP version 6 style address.

    Objects of this type are used to construct,
    parse, and manipulate IP version 6 addresses.

    @par BNF
    @code
    IPv6address =                            6( h16 ":" ) ls32
                /                       "::" 5( h16 ":" ) ls32
                / [               h16 ] "::" 4( h16 ":" ) ls32
                / [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
                / [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
                / [ *3( h16 ":" ) h16 ] "::"    h16 ":"   ls32
                / [ *4( h16 ":" ) h16 ] "::"              ls32
                / [ *5( h16 ":" ) h16 ] "::"              h16
                / [ *6( h16 ":" ) h16 ] "::"

    ls32        = ( h16 ":" h16 ) / IPv4address
                ; least-significant 32 bits of address

    h16         = 1*4HEXDIG
                ; 16 bits of address represented in hexadecimal
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc4291"
        >IP Version 6 Addressing Architecture (rfc4291)</a>
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
        >3.2.2. Host (rfc3986)</a>

    @see
        @ref ipv4_address,
        @ref parse_ipv6_address.
*/
class ipv6_address
{
public:
    /** The number of characters in the longest possible IPv6 string.

        The longest IPv6 address is:
        @code
        ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff
        @endcode

        @see
            @ref to_buffer.
    */
    // ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff
    // ::ffff:255.255.255.255
    // 12345678901234567890123456789012345678901234567890
    //          1         2         3        4
    static
    constexpr
    std::size_t max_str_len = 49;

    /** The type used to represent an address as an array of bytes.

        Octets are stored in network byte order.
    */
    using bytes_type = std::array<
        unsigned char, 16>;

    /** Constructor.

        Default constructed objects represent
        the unspecified address.

        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.2"
            >2.5.2. The Unspecified Address</a>

        @see
            @ref is_unspecified
    */
    ipv6_address() = default;

    /** Constructor.
    */
    ipv6_address(
        ipv6_address const&) = default;

    /** Copy Assignment
    */
    ipv6_address&
    operator=(
        ipv6_address const&) = default;

    /** Construct from an array of bytes.

        This function constructs an address
        from the array in `bytes`, which is
        interpreted in big-endian.

        @param bytes The value to construct from.
    */
    BOOST_URL_DECL
    ipv6_address(
        bytes_type const& bytes) noexcept;

    /** Construct from an IPv4 address.

        This function constructs an IPv6 address
        from the IPv4 address `addr`. The resulting
        address is an IPv4-Mapped IPv6 Address.

        @param addr The address to construct from.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.5.2"
            >2.5.5.2. IPv4-Mapped IPv6 Address (rfc4291)</a>
    */
    BOOST_URL_DECL
    ipv6_address(
        ipv4_address const& addr) noexcept;

    /** Construct from a string.

        This function constructs an address from
        the string `s`, which must contain a valid
        IPv6 address string or else an exception
        is thrown.

        @note For a non-throwing parse function,
        use @ref parse_ipv6_address.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        The input failed to parse correctly.

        @param s The string to parse.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>

        @see
            @ref parse_ipv6_address.
    */
    BOOST_URL_DECL
    ipv6_address(
        core::string_view s);

    /** Return the address as bytes, in network byte order
    */
    bytes_type
    to_bytes() const noexcept
    {
        return addr_;
    }

    /** Return the address as a string.

        The returned string does not
        contain surrounding square brackets.

        When called with no arguments, the
        return type is `std::string`.
        Otherwise, the return type and style
        of output is determined by which string
        token is passed.

        @par Example
        @code
        ipv6_address::bytes_type b = {{
                0, 1, 0, 2, 0, 3, 0, 4,
                0, 5, 0, 6, 0, 7, 0, 8 }};
        ipv6_address a(b);
        assert(a.to_string() == "1:2:3:4:5:6:7:8");
        assert( ipv4_address(0x01020304).to_string() == "1.2.3.4" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        String tokens may throw exceptions.

        @return The return type of the string token.
        If the token parameter is omitted, then
        a new `std::string` is returned.
        Otherwise, the function return type
        is the result type of the token.

        @param token An optional string token.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291#section-2.2">
            2.2. Text Representation of Addresses (rfc4291)</a>
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    to_string(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        to_string_impl(token);
        return token.result();
    }

    /** Write a dotted decimal string representing the address to a buffer

        The resulting buffer is not null-terminated.

        @throw std::length_error `dest_size < ipv6_address::max_str_len`

        @return The formatted string

        @param dest The buffer in which to write,
        which must have at least `dest_size` space.

        @param dest_size The size of the output buffer.
    */
    BOOST_URL_DECL
    core::string_view
    to_buffer(
        char* dest,
        std::size_t dest_size) const;

    /** Return true if the address is unspecified

        The address 0:0:0:0:0:0:0:0 is called the
        unspecified address. It indicates the
        absence of an address.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.2">
            2.5.2. The Unspecified Address (rfc4291)</a>
    */
    BOOST_URL_DECL
    bool
    is_unspecified() const noexcept;

    /** Return true if the address is a loopback address

        The unicast address 0:0:0:0:0:0:0:1 is called
        the loopback address. It may be used by a node
        to send an IPv6 packet to itself.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.3">
            2.5.3. The Loopback Address (rfc4291)</a>
    */
    BOOST_URL_DECL
    bool
    is_loopback() const noexcept;

    /** Return true if the address is a mapped IPv4 address

        This address type is used to represent the
        addresses of IPv4 nodes as IPv6 addresses.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.5.2">
            2.5.5.2. IPv4-Mapped IPv6 Address (rfc4291)</a>
    */
    BOOST_URL_DECL
    bool
    is_v4_mapped() const noexcept;

    /** Return true if two addresses are equal
    */
    friend
    bool
    operator==(
        ipv6_address const& a1,
        ipv6_address const& a2) noexcept
    {
        return a1.addr_ == a2.addr_;
    }

    /** Return true if two addresses are not equal
    */
    friend
    bool
    operator!=(
        ipv6_address const& a1,
        ipv6_address const& a2) noexcept
    {
        return !( a1 == a2 );
    }

    /** Return an address object that represents the loopback address

        The unicast address 0:0:0:0:0:0:0:1 is called
        the loopback address. It may be used by a node
        to send an IPv6 packet to itself.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291#section-2.5.3">
            2.5.3. The Loopback Address (rfc4291)</a>
    */
    BOOST_URL_DECL
    static
    ipv6_address
    loopback() noexcept;

    // hidden friend
    friend
    std::ostream&
    operator<<(
        std::ostream& os,
        ipv6_address const& addr)
    {
        char buf[ipv6_address::max_str_len];
        auto const s = addr.to_buffer(
            buf, sizeof(buf));
        os << s;
        return os;
    }


private:
    BOOST_URL_DECL
    std::size_t
    print_impl(
        char* dest) const noexcept;

    BOOST_URL_DECL
    void
    to_string_impl(
        string_token::arg& t) const;

    bytes_type addr_{{}};
};

/** Format the address to an output stream

    This function writes the address to an
    output stream using standard notation.

    @return The output stream, for chaining.

    @param os The output stream to write to.

    @param addr The address to write.
*/
std::ostream&
operator<<(
    std::ostream& os,
    ipv6_address const& addr);

//------------------------------------------------

/** Parse a string containing an IPv6 address.

    This function attempts to parse the string
    as an IPv6 address and returns a result
    containing the address upon success, or
    an error code if the string does not contain
    a valid IPv6 address.

    @par Exception Safety
    Throws nothing.

    @return A result containing the address.

    @param s The string to parse.
*/
BOOST_URL_DECL
system::result<ipv6_address>
parse_ipv6_address(
    core::string_view s) noexcept;

} // urls
} // boost

#endif
