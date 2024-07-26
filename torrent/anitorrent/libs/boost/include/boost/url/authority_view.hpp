//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_AUTHORITY_VIEW_HPP
#define BOOST_URL_AUTHORITY_VIEW_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/host_type.hpp>
#include <boost/url/ipv4_address.hpp>
#include <boost/url/ipv6_address.hpp>
#include <boost/url/pct_string_view.hpp>
#include <boost/url/detail/except.hpp>
#include <boost/url/detail/url_impl.hpp>
#include <boost/assert.hpp>
#include <cstddef>
#include <iosfwd>
#include <utility>

namespace boost {
namespace urls {

/** A non-owning reference to a valid authority

    Objects of this type represent valid authority
    strings constructed from a parsed, external
    character buffer whose storage is managed
    by the caller. That is, it acts like a
    `core::string_view` in terms of ownership.
    The caller is responsible for ensuring
    that the lifetime of the underlying
    character buffer extends until it is no
    longer referenced.

    @par Example 1
    Construction from a string parses the input
    as an <em>authority</em> and throws an
    exception on error. Upon success, the
    constructed object points to the passed
    character buffer; ownership is not
    transferred.
    @code
    authority_view a( "user:pass@www.example.com:8080" );
    @endcode

    @par Example 2
    The parsing function @ref parse_authority returns
    a @ref result containing either a valid
    @ref authority_view upon succcess, otherwise it
    contain an error. The error can be converted to
    an exception by the caller if desired:
    @code
    system::result< authority_view > rv = parse_authority( "user:pass@www.example.com:8080" );
    @endcode

    @par BNF
    @code
    authority     = [ userinfo "@" ] host [ ":" port ]

    userinfo      = user [ ":" [ password ] ]

    user          = *( unreserved / pct-encoded / sub-delims )
    password      = *( unreserved / pct-encoded / sub-delims / ":" )

    host          = IP-literal / IPv4address / reg-name

    port          = *DIGIT
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2"
        >3.2. Authority (rfc3986)</a>

    @see
        @ref parse_authority.
*/
class BOOST_URL_DECL
    authority_view
    : private detail::parts_base
{
    detail::url_impl u_;

#ifndef BOOST_URL_DOCS
    // VFALCO docca emits this erroneously
    friend struct detail::url_impl;
#endif

    explicit
    authority_view(
        detail::url_impl const& u) noexcept;

public:
    //--------------------------------------------
    //
    // Special Members
    //
    //--------------------------------------------

    /** Destructor
    */
    virtual
    ~authority_view();

    /** Constructor

        Default constructed authorities
        refer to a string with zero length,
        which is always valid. This matches
        the grammar for a zero-length host.

        @par Exception Safety
        Throws nothing.

        @par Specification
    */
    authority_view() noexcept;

    /** Construct from a string.

        This function attempts to construct
        an authority from the string `s`,
        which must be a valid ['authority] or
        else an exception is thrown. Upon
        successful construction, the view
        refers to the characters in the
        buffer pointed to by `s`.
        Ownership is not transferred; The
        caller is responsible for ensuring
        that the lifetime of the buffer
        extends until the view is destroyed.

        @par BNF
        @code
        authority     = [ userinfo "@" ] host [ ":" port ]

        userinfo      = user [ ":" [ password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )

        host          = IP-literal / IPv4address / reg-name

        port          = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2"
            >3.2. Authority (rfc3986)</a>

        @see
            @ref parse_authority.
    */
    explicit
    authority_view(core::string_view s);

    /** Constructor
    */
    authority_view(
        authority_view const&) noexcept;

    /** Assignment
    */
    authority_view&
    operator=(
        authority_view const&) noexcept;

    //--------------------------------------------
    //
    // Observers
    //
    //--------------------------------------------

    /** Return the number of characters in the authority

        This function returns the number of
        characters in the authority.

        @par Example
        @code
        assert( authority_view( "user:pass@www.example.com:8080" ).size() == 30 );
        @endcode

        @par Exception Safety
        Throws nothing.
    */
    std::size_t
    size() const noexcept
    {
        return u_.offset(id_end);
    }

    /** Return true if the authority is empty

        An empty authority has an empty host,
        no userinfo, and no port.

        @par Example
        @code
        assert( authority_view( "" ).empty() );
        @endcode

        @par Exception Safety
        Throws nothing.
    */
    bool
    empty() const noexcept
    {
        return size() == 0;
    }

    /** Return a pointer to the first character

        This function returns a pointer to the
        beginning of the view, which is not
        guaranteed to be null-terminated.

        @par Exception Safety
        Throws nothing.
    */
    char const*
    data() const noexcept
    {
        return u_.cs_;
    }

    /** Return the complete authority

        This function returns the authority
        as a percent-encoded string.

        @par Example
        @code
        assert( parse_authority( "www.example.com" ).value().buffer() == "www.example.com" );
        @endcode

        @par BNF
        @code
        authority   = [ userinfo "@" ] host [ ":" port ]
        @endcode

        @par Exception Safety
        Throws nothing.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2"
            >3.2. Authority (rfc3986)</a>
    */
    core::string_view
    buffer() const noexcept
    {
        return core::string_view(data(), size());
    }

    //--------------------------------------------
    //
    // Userinfo
    //
    //--------------------------------------------

    /** Return true if a userinfo is present

        This function returns true if this
        contains a userinfo.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).has_userinfo() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        userinfo    = user [ ":" [ password ] ]

        authority   = [ userinfo "@" ] host [ ":" port ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1"
            >3.2.1. User Information (rfc3986)</a>

        @see
            @ref has_password,
            @ref encoded_password,
            @ref encoded_user,
            @ref encoded_userinfo,
            @ref password,
            @ref user,
            @ref userinfo.

    */
    bool
    has_userinfo() const noexcept;

    /** Return the userinfo

        If present, this function returns a
        string representing the userinfo (which
        may be empty).
        Otherwise it returns an empty string.
        Any percent-escapes in the string are
        decoded first.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).userinfo() == "jane-doe:pass" );
        @endcode

        @par Complexity
        Linear in `this->userinfo().size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par BNF
        @code
        userinfo    = user [ ":" [ password ] ]

        authority   = [ userinfo "@" ] host [ ":" port ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1"
            >3.2.1. User Information (rfc3986)</a>

        @see
            @ref has_password,
            @ref has_userinfo,
            @ref encoded_password,
            @ref encoded_user,
            @ref encoded_userinfo,
            @ref password,
            @ref user.
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    userinfo(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_userinfo().decode(
            opt, std::move(token));
    }

    /** Return the userinfo

        If present, this function returns a
        string representing the userinfo (which
        may be empty).
        Otherwise it returns an empty string.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).encoded_userinfo() == "jane%2Ddoe:pass" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing

        @par BNF
        @code
        userinfo    = user [ ":" [ password ] ]

        authority   = [ userinfo "@" ] host [ ":" port ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1"
            >3.2.1. User Information (rfc3986)</a>

        @see
            @ref has_password,
            @ref has_userinfo,
            @ref encoded_password,
            @ref encoded_user,
            @ref password,
            @ref user,
            @ref userinfo.
    */
    pct_string_view
    encoded_userinfo() const noexcept;

    //--------------------------------------------

    /** Return the user

        If present, this function returns a
        string representing the user (which
        may be empty).
        Otherwise it returns an empty string.
        Any percent-escapes in the string are
        decoded first.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).user() == "jane-doe" );
        @endcode

        @par Complexity
        Linear in `this->user().size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par BNF
        @code
        userinfo    = user [ ":" [ password ] ]

        user        = *( unreserved / pct-encoded / sub-delims )
        password    = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1"
            >3.2.1. User Information (rfc3986)</a>

        @see
            @ref has_password,
            @ref has_userinfo,
            @ref encoded_password,
            @ref encoded_user,
            @ref encoded_userinfo,
            @ref password,
            @ref userinfo.
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    user(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_user().decode(
            opt, std::move(token));
    }

    /** Return the user

        If present, this function returns a
        string representing the user (which
        may be empty).
        Otherwise it returns an empty string.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).encoded_user() == "jane%2Ddoe" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        userinfo    = user [ ":" [ password ] ]

        user        = *( unreserved / pct-encoded / sub-delims )
        password    = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1"
            >3.2.1. User Information (rfc3986)</a>

        @see
            @ref has_password,
            @ref has_userinfo,
            @ref encoded_password,
            @ref encoded_userinfo,
            @ref password,
            @ref user,
            @ref userinfo.
    */
    pct_string_view
    encoded_user() const noexcept;

    /** Return true if a password is present

        This function returns true if the
        userinfo is present and contains
        a password.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).has_password() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        userinfo    = user [ ":" [ password ] ]

        user        = *( unreserved / pct-encoded / sub-delims )
        password    = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1"
            >3.2.1. User Information (rfc3986)</a>

        @see
            @ref has_userinfo,
            @ref encoded_password,
            @ref encoded_user,
            @ref encoded_userinfo,
            @ref password,
            @ref user,
            @ref userinfo.
    */
    bool
    has_password() const noexcept;

    /** Return the password

        If present, this function returns a
        string representing the password (which
        may be an empty string).
        Otherwise it returns an empty string.
        Any percent-escapes in the string are
        decoded first.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).password() == "pass" );
        @endcode

        @par Complexity
        Linear in `this->password().size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par BNF
        @code
        userinfo    = user [ ":" [ password ] ]

        user        = *( unreserved / pct-encoded / sub-delims )
        password    = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1"
            >3.2.1. User Information (rfc3986)</a>

        @see
            @ref has_password,
            @ref has_userinfo,
            @ref encoded_password,
            @ref encoded_user,
            @ref encoded_userinfo,
            @ref user,
            @ref userinfo.
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    password(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_password().decode(
            opt, std::move(token));
    }

    /** Return the password

        This function returns the password portion
        of the userinfo as a percent-encoded string.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).encoded_password() == "pass" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        userinfo    = user [ ":" [ password ] ]

        user        = *( unreserved / pct-encoded / sub-delims )
        password    = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1"
            >3.2.1. User Information (rfc3986)</a>

        @see
            @ref has_password,
            @ref has_userinfo,
            @ref encoded_user,
            @ref encoded_userinfo,
            @ref password,
            @ref user,
            @ref userinfo.
    */
    pct_string_view
    encoded_password() const noexcept;

    //--------------------------------------------
    //
    // Host
    //
    //--------------------------------------------

    /** Return the host type

        This function returns one of the
        following constants representing the
        type of host present.

        @li @ref host_type::ipv4
        @li @ref host_type::ipv6
        @li @ref host_type::ipvfuture
        @li @ref host_type::name

        @par Example
        @code
        assert( url_view( "https://192.168.0.1/local.htm" ).host_type() == host_type::ipv4 );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    urls::host_type
    host_type() const noexcept
    {
        return u_.host_type_;
    }

    /** Return the host

        This function returns the host portion
        of the authority as a string, or the
        empty string if there is no authority.
        Any percent-escapes in the string are
        decoded first.

        @par Example
        @code
        assert( url_view( "https://www%2droot.example.com/" ).host() == "www-root.example.com" );
        @endcode

        @par Complexity
        Linear in `this->host().size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal  = "[" ( IPv6address / IPvFuture  ) "]"

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    host(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_host().decode(
            opt, std::move(token));
    }

    /** Return the host

        This function returns the host portion
        of the authority as a string, or the
        empty string if there is no authority.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "https://www%2droot.example.com/" ).encoded_host() == "www%2droot.example.com" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal  = "[" ( IPv6address / IPvFuture  ) "]"

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    pct_string_view
    encoded_host() const noexcept;

    /** Return the host

        The value returned by this function
        depends on the type of host returned
        from the function @ref host_type.

        @li If the type is @ref host_type::ipv4,
        then the IPv4 address string is returned.

        @li If the type is @ref host_type::ipv6,
        then the IPv6 address string is returned,
        without any enclosing brackets.

        @li If the type is @ref host_type::ipvfuture,
        then the IPvFuture address string is returned,
        without any enclosing brackets.

        @li If the type is @ref host_type::name,
        then the host name string is returned.
        Any percent-escapes in the string are
        decoded first.

        @li If the type is @ref host_type::none,
        then an empty string is returned.

        @par Example
        @code
        assert( url_view( "https://[1::6:c0a8:1]/" ).host_address() == "1::6:c0a8:1" );
        @endcode

        @par Complexity
        Linear in `this->host_address().size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal  = "[" ( IPv6address / IPvFuture  ) "]"

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    host_address(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_host_address().decode(
            opt, std::move(token));
    }

    /** Return the host

        The value returned by this function
        depends on the type of host returned
        from the function @ref host_type.

        @li If the type is @ref host_type::ipv4,
        then the IPv4 address string is returned.

        @li If the type is @ref host_type::ipv6,
        then the IPv6 address string is returned,
        without any enclosing brackets.

        @li If the type is @ref host_type::ipvfuture,
        then the IPvFuture address string is returned,
        without any enclosing brackets.

        @li If the type is @ref host_type::name,
        then the host name string is returned.
        Any percent-escapes in the string are
        decoded first.

        @li If the type is @ref host_type::none,
        then an empty string is returned.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "https://www%2droot.example.com/" ).encoded_host_address() == "www%2droot.example.com" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal  = "[" ( IPv6address / IPvFuture  ) "]"

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    pct_string_view
    encoded_host_address() const noexcept;

    /** Return the host IPv4 address

        If the host type is @ref host_type::ipv4,
        this function returns the address as
        a value of type @ref ipv4_address.
        Otherwise, if the host type is not an IPv4
        address, it returns a default-constructed
        value which is equal to the unspecified
        address "0.0.0.0".

        @par Example
        @code
        assert( url_view( "http://127.0.0.1/index.htm?user=win95" ).host_ipv4_address() == ipv4_address( "127.0.0.1" ) );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    ipv4_address
    host_ipv4_address() const noexcept;

    /** Return the host IPv6 address

        If the host type is @ref host_type::ipv6,
        this function returns the address as
        a value of type @ref ipv6_address.
        Otherwise, if the host type is not an IPv6
        address, it returns a default-constructed
        value which is equal to the unspecified
        address "0:0:0:0:0:0:0:0".

        @par Example
        @code
        assert( url_view( "ftp://[::1]/" ).host_ipv6_address() == ipv6_address( "::1" ) );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    ipv6_address
    host_ipv6_address() const noexcept;

    /** Return the host IPvFuture address

        If the host type is @ref host_type::ipvfuture,
        this function returns the address as
        a string.
        Otherwise, if the host type is not an
        IPvFuture address, it returns an
        empty string.

        @par Example
        @code
        assert( url_view( "http://[v1fe.d:9]/index.htm" ).host_ipvfuture() == "v1fe.d:9" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        IPvFuture  = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    core::string_view
    host_ipvfuture() const noexcept;

    /** Return the host name

        If the host type is @ref host_type::name,
        this function returns the name as
        a string.
        Otherwise, if the host type is not an
        name, it returns an empty string.
        Any percent-escapes in the string are
        decoded first.

        @par Example
        @code
        assert( url_view( "https://www%2droot.example.com/" ).host_name() == "www-root.example.com" );
        @endcode

        @par Complexity
        Linear in `this->host_name().size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal  = "[" ( IPv6address / IPvFuture  ) "]"

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    host_name(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_host_name().decode(
            opt, std::move(token));
    }

    /** Return the host name

        If the host type is @ref host_type::name,
        this function returns the name as
        a string.
        Otherwise, if the host type is not an
        name, it returns an empty string.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "https://www%2droot.example.com/" ).encoded_host_name() == "www%2droot.example.com" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal  = "[" ( IPv6address / IPvFuture  ) "]"

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2. Host (rfc3986)</a>
    */
    pct_string_view
    encoded_host_name() const noexcept;

    //--------------------------------------------
    //
    // Port
    //
    //--------------------------------------------

    /** Return true if a port is present

        This function returns true if an
        authority is present and contains a port.

        @par Example
        @code
        assert( url_view( "wss://www.example.com:443" ).has_port() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        authority   = [ userinfo "@" ] host [ ":" port ]

        port        = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.3"
            >3.2.3. Port (rfc3986)</a>

        @see
            @ref encoded_host_and_port,
            @ref port,
            @ref port_number.
    */
    bool
    has_port() const noexcept;

    /** Return the port

        If present, this function returns a
        string representing the port (which
        may be empty).
        Otherwise it returns an empty string.

        @par Example
        @code
        assert( url_view( "http://localhost.com:8080" ).port() == "8080" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        port        = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.3"
            >3.2.3. Port (rfc3986)</a>

        @see
            @ref encoded_host_and_port,
            @ref has_port,
            @ref port_number.
    */
    core::string_view
    port() const noexcept;

    /** Return the port

        If a port is present and the numerical
        value is representable, it is returned
        as an unsigned integer. Otherwise, the
        number zero is returned.

        @par Example
        @code
        assert( url_view( "http://localhost.com:8080" ).port_number() == 8080 );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        port        = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.3"
            >3.2.3. Port (rfc3986)</a>

        @see
            @ref encoded_host_and_port,
            @ref has_port,
            @ref port.
    */
    std::uint16_t
    port_number() const noexcept;

    /** Return the host and port

        If an authority is present, this
        function returns the host and optional
        port as a string, which may be empty.
        Otherwise it returns an empty string.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "http://www.example.com:8080/index.htm" ).encoded_host_and_port() == "www.example.com:8080" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        authority   = [ userinfo "@" ] host [ ":" port ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2"
            >3.2.2.  Host (rfc3986)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.3"
            >3.2.3. Port (rfc3986)</a>

        @see
            @ref has_port,
            @ref port,
            @ref port_number.
    */
    pct_string_view
    encoded_host_and_port() const noexcept;

    //--------------------------------------------
    //
    // Comparison
    //
    //--------------------------------------------

    /** Return the result of comparing this with another authority

        This function compares two authorities
        according to Syntax-Based comparison
        algorithm.

        @par Exception Safety
        Throws nothing.

        @return -1 if `*this < other`, 0 if
        `this == other`, and 1 if `this > other`.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>
    */
    int
    compare(authority_view const& other) const noexcept;

    /** Return the result of comparing two authorities
        The authorities are compared component
        by component as if they were first
        normalized.

        @par Complexity
        Linear in `min( a0.size(), a1.size() )`

        @par Exception Safety
        Throws nothing
    */
    friend
    bool
    operator==(
        authority_view const& a0,
        authority_view const& a1) noexcept
    {
        return a0.compare(a1) == 0;
    }

    /** Return the result of comparing two authorities
        The authorities are compared component
        by component as if they were first
        normalized.

        @par Complexity
        Linear in `min( a0.size(), a1.size() )`

        @par Exception Safety
        Throws nothing
    */
    friend
    bool
    operator!=(
        authority_view const& a0,
        authority_view const& a1) noexcept
    {
        return ! (a0 == a1);
    }

    /** Return the result of comparing two authorities
        The authorities are compared component
        by component as if they were first
        normalized.

        @par Complexity
        Linear in `min( a0.size(), a1.size() )`

        @par Exception Safety
        Throws nothing
    */
    friend
    bool
    operator<(
        authority_view const& a0,
        authority_view const& a1) noexcept
    {
        return a0.compare(a1) < 0;
    }

    /** Return the result of comparing two authorities
        The authorities are compared component
        by component as if they were first
        normalized.

        @par Complexity
        Linear in `min( a0.size(), a1.size() )`

        @par Exception Safety
        Throws nothing
    */
    friend
    bool
    operator<=(
        authority_view const& a0,
        authority_view const& a1) noexcept
    {
        return a0.compare(a1) <= 0;
    }

    /** Return the result of comparing two authorities
        The authorities are compared component
        by component as if they were first
        normalized.

        @par Complexity
        Linear in `min( a0.size(), a1.size() )`

        @par Exception Safety
        Throws nothing
    */
    friend
    bool
    operator>(
        authority_view const& a0,
        authority_view const& a1) noexcept
    {
        return a0.compare(a1) > 0;
    }

    /** Return the result of comparing two authorities
        The authorities are compared component
        by component as if they were first
        normalized.

        @par Complexity
        Linear in `min( a0.size(), a1.size() )`

        @par Exception Safety
        Throws nothing
    */
    friend
    bool
    operator>=(
        authority_view const& a0,
        authority_view const& a1) noexcept
    {
        return a0.compare(a1) >= 0;
    }

    //--------------------------------------------

    // hidden friend
    friend
    std::ostream&
    operator<<(
        std::ostream& os,
        authority_view const& a)
    {
        return os << a.buffer();
    }
};

/** Format the encoded authority to the output stream

    This function serializes the encoded URL
    to the output stream.

    @par Example
    @code
    authority_view a( "www.example.com" );

    std::cout << a << std::endl;
    @endcode

    @return A reference to the output stream, for chaining

    @param os The output stream to write to

    @param a The URL to write
*/
std::ostream&
operator<<(
    std::ostream& os,
    authority_view const& a);

//------------------------------------------------

/** Parse an authority

    This function parses a string according to
    the authority grammar below, and returns an
    @ref authority_view referencing the string.
    Ownership of the string is not transferred;
    the caller is responsible for ensuring that
    the lifetime of the string extends until the
    view is no longer being accessed.

    @par BNF
    @code
    authority     = [ userinfo "@" ] host [ ":" port ]

    userinfo      = user [ ":" [ password ] ]

    user          = *( unreserved / pct-encoded / sub-delims )
    password      = *( unreserved / pct-encoded / sub-delims / ":" )

    host          = IP-literal / IPv4address / reg-name

    port          = *DIGIT
    @endcode

    @par Exception Safety
    Throws nothing.

    @return A view to the parsed authority

    @param s The string to parse

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2"
        >3.2. Authority (rfc3986)</a>

    @see
        @ref authority_view.
*/
BOOST_URL_DECL
system::result<authority_view>
parse_authority(
    core::string_view s) noexcept;

//------------------------------------------------

} // urls
} // boost

#endif
