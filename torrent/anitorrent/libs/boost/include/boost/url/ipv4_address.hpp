//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_IPV4_ADDRESS_HPP
#define BOOST_URL_IPV4_ADDRESS_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error.hpp>
#include <boost/url/error_types.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/grammar/string_token.hpp>
#include <string>
#include <array>
#include <cstdint>
#include <iosfwd>

namespace boost {
namespace urls {

/** An IP version 4 style address.

    Objects of this type are used to construct,
    parse, and manipulate IP version 6 addresses.

    @par BNF
    @code
    IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet

    dec-octet   = DIGIT                 ; 0-9
                / %x31-39 DIGIT         ; 10-99
                / "1" 2DIGIT            ; 100-199
                / "2" %x30-34 DIGIT     ; 200-249
                / "25" %x30-35          ; 250-255
    @endcode

    @par Specification
    @li <a href="https://en.wikipedia.org/wiki/IPv4"
        >IPv4 (Wikipedia)</a>
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
        >3.2.2. Host (rfc3986)</a>

    @see
        @ref parse_ipv4_address,
        @ref ipv6_address.
*/
class ipv4_address
{
public:
    /** The number of characters in the longest possible IPv4 string.

        The longest ipv4 address string is "255.255.255.255".
    */
    static
    constexpr
    std::size_t max_str_len = 15;

    /** The type used to represent an address as an unsigned integer
    */
    using uint_type =
        std::uint_least32_t;

    /** The type used to represent an address as an array of bytes
    */
    using bytes_type =
        std::array<unsigned char, 4>;

    /** Constructor.
    */
    ipv4_address() = default;

    /** Constructor.
    */
    ipv4_address(
        ipv4_address const&) = default;

    /** Copy Assignment.
    */
    ipv4_address&
    operator=(
        ipv4_address const&) = default;

    //
    //---
    //

    /** Construct from an unsigned integer.

        This function constructs an address from
        the unsigned integer `u`, where the most
        significant byte forms the first octet
        of the resulting address.

        @param u The integer to construct from.
    */
    BOOST_URL_DECL
    explicit
    ipv4_address(
        uint_type u) noexcept;

    /** Construct from an array of bytes.

        This function constructs an address
        from the array in `bytes`, which is
        interpreted in big-endian.

        @param bytes The value to construct from.
    */
    BOOST_URL_DECL
    explicit
    ipv4_address(
        bytes_type const& bytes) noexcept;

    /** Construct from a string.

        This function constructs an address from
        the string `s`, which must contain a valid
        IPv4 address string or else an exception
        is thrown.

        @note For a non-throwing parse function,
        use @ref parse_ipv4_address.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        The input failed to parse correctly.

        @param s The string to parse.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>

        @see
            @ref parse_ipv4_address.
    */
    BOOST_URL_DECL
    explicit
    ipv4_address(
        core::string_view s);

    /** Return the address as bytes, in network byte order.
    */
    BOOST_URL_DECL
    bytes_type
    to_bytes() const noexcept;

    /** Return the address as an unsigned integer.
    */
    BOOST_URL_DECL
    uint_type
    to_uint() const noexcept;

    /** Return the address as a string in dotted decimal format

        When called with no arguments, the
        return type is `std::string`.
        Otherwise, the return type and style
        of output is determined by which string
        token is passed.

        @par Example
        @code
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

        @throw std::length_error `dest_size < ipv4_address::max_str_len`

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

    /** Return true if the address is a loopback address
    */
    BOOST_URL_DECL
    bool
    is_loopback() const noexcept;

    /** Return true if the address is unspecified
    */
    BOOST_URL_DECL
    bool
    is_unspecified() const noexcept;

    /** Return true if the address is a multicast address
    */
    BOOST_URL_DECL
    bool
    is_multicast() const noexcept;

    /** Return true if two addresses are equal
    */
    friend
    bool
    operator==(
        ipv4_address const& a1,
        ipv4_address const& a2) noexcept
    {
        return a1.addr_ == a2.addr_;
    }

    /** Return true if two addresses are not equal
    */
    friend
    bool
    operator!=(
        ipv4_address const& a1,
        ipv4_address const& a2) noexcept
    {
        return a1.addr_ != a2.addr_;
    }

    /** Return an address object that represents any address
    */
    static
    ipv4_address
    any() noexcept
    {
        return ipv4_address();
    }

    /** Return an address object that represents the loopback address
    */
    static
    ipv4_address
    loopback() noexcept
    {
        return ipv4_address(0x7F000001);
    }

    /** Return an address object that represents the broadcast address
    */
    static
    ipv4_address
    broadcast() noexcept
    {
        return ipv4_address(0xFFFFFFFF);
    }

    // hidden friend
    friend
    std::ostream&
    operator<<(
        std::ostream& os,
        ipv4_address const& addr)
    {
        char buf[ipv4_address::max_str_len];
        os << addr.to_buffer(buf, sizeof(buf));
        return os;
    }

private:
    friend class ipv6_address;

    BOOST_URL_DECL
    std::size_t
    print_impl(
        char* dest) const noexcept;

    BOOST_URL_DECL
    void
    to_string_impl(
        string_token::arg& t) const;

    uint_type addr_ = 0;
};

/** Format the address to an output stream.

    IPv4 addresses written to output streams
    are written in their dotted decimal format.

    @param os The output stream.

    @param addr The address to format.
*/
std::ostream&
operator<<(
    std::ostream& os,
    ipv4_address const& addr);

//------------------------------------------------

/** Return an IPv4 address from an IP address string in dotted decimal form
*/
BOOST_URL_DECL
system::result<ipv4_address>
parse_ipv4_address(
    core::string_view s) noexcept;

} // urls
} // boost

#endif
