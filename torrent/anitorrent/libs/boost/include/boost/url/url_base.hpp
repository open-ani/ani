//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_URL_BASE_HPP
#define BOOST_URL_URL_BASE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/ipv4_address.hpp>
#include <boost/url/ipv6_address.hpp>
#include <boost/url/params_encoded_ref.hpp>
#include <boost/url/params_ref.hpp>
#include <boost/url/pct_string_view.hpp>
#include <boost/url/scheme.hpp>
#include <boost/url/segments_encoded_ref.hpp>
#include <boost/url/segments_ref.hpp>
#include <boost/url/url_view_base.hpp>
#include <cstdint>
#include <initializer_list>
#include <memory>
#include <string>
#include <utility>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
namespace detail {
struct any_params_iter;
struct any_segments_iter;
struct params_iter_impl;
struct segments_iter_impl;
struct pattern;
}
#endif

/** Common functionality for containers

    This base class is used by the library
    to provide common member functions for
    containers. This cannot be instantiated
    directly; Instead, use one of the
    containers or functions:

    @par Containers
        @li @ref url
        @li @ref url_view
        @li @ref static_url

    @par Functions
        @li @ref parse_absolute_uri
        @li @ref parse_origin_form
        @li @ref parse_relative_ref
        @li @ref parse_uri
        @li @ref parse_uri_reference
*/
class BOOST_URL_DECL
    url_base
    : public url_view_base
{
    char* s_ = nullptr;
    std::size_t cap_ = 0;

    friend class url;
    friend class static_url_base;
    friend class params_ref;
    friend class segments_ref;
    friend class segments_encoded_ref;
    friend class params_encoded_ref;
    friend struct detail::pattern;

    struct op_t
    {
        ~op_t();
        op_t(url_base&,
            core::string_view* = nullptr,
            core::string_view* = nullptr) noexcept;
        void move(char*, char const*,
            std::size_t) noexcept;

        url_base& u;
        core::string_view* s0 = nullptr;
        core::string_view* s1 = nullptr;
        char* old = nullptr;
    };

    virtual ~url_base() noexcept = default;
    url_base() noexcept = default;
    url_base(detail::url_impl const&) noexcept;
    explicit url_base(core::string_view);
    void reserve_impl(std::size_t n);
    void copy(url_view_base const&);
    virtual void clear_impl() noexcept = 0;
    virtual void reserve_impl(
        std::size_t, op_t&) = 0;
    virtual void cleanup(op_t&) = 0;

public:
    //--------------------------------------------
    //
    // Observers
    //
    //--------------------------------------------

    /** Return the url as a null-terminated string

        This function returns a pointer to a null
        terminated string representing the url,
        which may contain percent escapes.

        @par Example
        @code
        assert( std::strlen( url( "http://www.example.com" ).c_str() ) == 22 );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    char const*
    c_str() const noexcept
    {
        return pi_->cs_;
    }

    /** Return the number of characters that can be stored without reallocating

        This does not include the null terminator,
        which is always present.

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    std::size_t
    capacity() const noexcept
    {
        return cap_;
    }

    /** Clear the contents while preserving the capacity

        @par Postconditions
        @code
        this->empty() == true
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        No-throw guarantee.
    */
    void
    clear() noexcept
    {
        this->clear_impl();
    }

    /** Adjust the capacity without changing the size

        This function adjusts the capacity
        of the container in characters, without
        affecting the current contents. Has
        no effect if `n <= this->capacity()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @throw bad_alloc Allocation failure

        @param n The capacity in characters,
        excluding any null terminator.
    */
    void
    reserve(std::size_t n)
    {
        reserve_impl(n);
    }

    //--------------------------------------------
    //
    // Fluent API
    //

    //--------------------------------------------
    //
    // Scheme
    //
    //--------------------------------------------

    /** Set the scheme

        The scheme is set to the specified
        string, which must contain a valid
        scheme without any trailing colon
        (':').
        Note that schemes are case-insensitive,
        and the canonical form is lowercased.

        @par Example
        @code
        assert( url( "http://www.example.com" ).set_scheme( "https" ).scheme_id() == scheme::https );
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid scheme.

        @param s The scheme to set.

        @par BNF
        @code
        scheme        = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.1">
            3.1. Scheme (rfc3986)</a>

        @see
            @ref remove_scheme.
    */
    url_base&
    set_scheme(core::string_view s);

    /** Set the scheme

        This function sets the scheme to the specified
        known @ref urls::scheme id, which may not be
        @ref scheme::unknown or else an exception is
        thrown. If the id is @ref scheme::none, this
        function behaves as if @ref remove_scheme
        were called.

        @par Example
        @code
        assert( url( "http://example.com/echo.cgi" ).set_scheme_id( scheme::wss ).buffer() == "wss://example.com/echo.cgi" );
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The scheme is invalid.

        @param id The scheme to set.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.1">
            3.1. Scheme (rfc3986)</a>
    */
    url_base&
#ifndef BOOST_URL_DOCS
    set_scheme_id(urls::scheme id);
#else
    set_scheme_id(scheme id);
#endif

    /** Remove the scheme

        This function removes the scheme if it
        is present.

        @par Example
        @code
        assert( url("http://www.example.com/index.htm" ).remove_scheme().buffer() == "//www.example.com/index.htm" );
        @endcode

        @par Postconditions
        @code
        this->has_scheme() == false && this->scheme_id() == scheme::none
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.1">
            3.1. Scheme (rfc3986)</a>

        @see
            @ref set_scheme.
    */
    url_base&
    remove_scheme();

    //--------------------------------------------
    //
    // Authority
    //
    //--------------------------------------------

    /** Set the authority

        This function sets the authority
        to the specified string.
        The string may contain percent-escapes.

        @par Example
        @code
        assert( url().set_encoded_authority( "My%20Computer" ).has_authority() );
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_eror
        The string contains an invalid percent-encoding.

        @param s The authority string to set.

        @par BNF
        @code
        authority     = [ userinfo "@" ] host [ ":" port ]

        userinfo      = *( unreserved / pct-encoded / sub-delims / ":" )
        host          = IP-literal / IPv4address / reg-name
        port          = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2">
            3.2. Authority (rfc3986)</a>
        @see
            @ref remove_authority.
    */
    url_base&
    set_encoded_authority(
        pct_string_view s);

    /** Remove the authority

        This function removes the authority,
        which includes the userinfo, host, and
        a port if present.

        @par Example
        @code
        assert( url( "http://example.com/echo.cgi" ).remove_authority().buffer() == "http:/echo.cgi" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == false && this->has_userinfo() == false && this->has_port() == false
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        authority     = [ userinfo "@" ] host [ ":" port ]

        userinfo      = *( unreserved / pct-encoded / sub-delims / ":" )
        host          = IP-literal / IPv4address / reg-name
        port          = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2">
            3.2. Authority (rfc3986)</a>

        @see
            @ref set_encoded_authority.
    */
    url_base&
    remove_authority();

    //--------------------------------------------
    //
    // Userinfo
    //
    //--------------------------------------------

    /** Set the userinfo

        The userinfo is set to the given string,
        which may contain percent-escapes.
        Any special or reserved characters in the
        string are automatically percent-encoded.
        The effects on the user and password
        depend on the presence of a colon (':')
        in the string:

        @li If an unescaped colon exists, the
        characters up to the colon become
        the user and the rest of the characters
        after the colon become the password.
        In this case @ref has_password returns
        true. Otherwise,

        @li If there is no colon, the user is
        set to the string. The function
        @ref has_password returns false.

        @note
        The interpretation of the userinfo as
        individual user and password components
        is scheme-dependent. Transmitting
        passwords in URLs is deprecated.

        @par Example
        @code
        assert( url( "http://example.com" ).set_userinfo( "user:pass" ).encoded_user() == "user" );
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set.

        @par BNF
        @code
        userinfo      = [ [ user ] [ ':' password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">
            3.2.1. User Information (rfc3986)</a>

        @see
            @ref remove_userinfo,
            @ref set_encoded_userinfo.
    */
    url_base&
    set_userinfo(
        core::string_view s);

    /** Set the userinfo.

        The userinfo is set to the given string,
        which may contain percent-escapes.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.
        The effects on the user and password
        depend on the presence of a colon (':')
        in the string:

        @li If an unescaped colon exists, the
        characters up to the colon become
        the user and the rest of the characters
        after the colon become the password.
        In this case @ref has_password returns
        true. Otherwise,

        @li If there is no colon, the user is
        set to the string. The function
        @ref has_password returns false.

        @note
        The interpretation of the userinfo as
        individual user and password components
        is scheme-dependent. Transmitting
        passwords in URLs is deprecated.

        @par Example
        @code
        assert( url( "http://example.com" ).set_encoded_userinfo( "john%20doe" ).user() == "john doe" );
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set.

        @par BNF
        @code
        userinfo      = [ [ user ] [ ':' password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">
            3.2.1. User Information (rfc3986)</a>

        @see
            @ref remove_userinfo,
            @ref set_userinfo.
    */
    url_base&
    set_encoded_userinfo(
        pct_string_view s);

    /** Remove the userinfo

        This function removes the userinfo if
        present, without removing any authority.

        @par Example
        @code
        assert( url( "http://user@example.com" ).remove_userinfo().has_userinfo() == false );
        @endcode

        @par Postconditions
        @code
        this->has_userinfo() == false && this->encoded_userinfo().empty == true
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        userinfo      = [ [ user ] [ ':' password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">
            3.2.1. User Information (rfc3986)</a>

        @see
            @ref set_encoded_userinfo,
            @ref set_userinfo.
    */
    url_base&
    remove_userinfo() noexcept;

    //--------------------------------------------

    /** Set the user

        This function sets the user part of the
        userinfo to the string.
        Any special or reserved characters in the
        string are automatically percent-encoded.

        @par Example
        @code
        assert( url().set_user("john doe").encoded_userinfo() == "john%20doe" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true && this->has_userinfo() == true
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set.

        @par BNF
        @code
        userinfo      = [ [ user ] [ ':' password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">
            3.2.1. User Information (rfc3986)</a>

        @see
            @ref remove_password,
            @ref set_encoded_password,
            @ref set_encoded_user,
            @ref set_password.
    */
    url_base&
    set_user(
        core::string_view s);

    /** Set the user

        This function sets the user part of the
        userinfo the the string, which may
        contain percent-escapes.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.

        @par Example
        @code
        assert( url().set_encoded_user("john%20doe").userinfo() == "john doe" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true && this->has_userinfo() == true
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set.

        @par BNF
        @code
        userinfo      = [ [ user ] [ ':' password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">
            3.2.1. User Information (rfc3986)</a>

        @see
            @ref remove_password,
            @ref set_encoded_password,
            @ref set_password,
            @ref set_user.
    */
    url_base&
    set_encoded_user(
        pct_string_view s);

    /** Set the password.

        This function sets the password in
        the userinfo to the string.
        Reserved characters in the string are
        percent-escaped in the result.

        @note
        The interpretation of the userinfo as
        individual user and password components
        is scheme-dependent. Transmitting
        passwords in URLs is deprecated.

        @par Example
        @code
        assert( url("http://user@example.com").set_password( "pass" ).encoded_userinfo() == "user:pass" );
        @endcode

        @par Postconditions
        @code
        this->has_password() == true && this->password() == s
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set. This string may
        contain any characters, including nulls.

        @par BNF
        @code
        userinfo      = [ [ user ] [ ':' password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">
            3.2.1. User Information (rfc3986)</a>

        @see
            @ref remove_password,
            @ref set_encoded_password,
            @ref set_encoded_user,
            @ref set_user.
    */
    url_base&
    set_password(
        core::string_view s);

    /** Set the password.

        This function sets the password in
        the userinfo to the string, which
        may contain percent-escapes.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.

        @note
        The interpretation of the userinfo as
        individual user and password components
        is scheme-dependent. Transmitting
        passwords in URLs is deprecated.

        @par Example
        @code
        assert( url("http://user@example.com").set_encoded_password( "pass" ).encoded_userinfo() == "user:pass" );
        @endcode

        @par Postconditions
        @code
        this->has_password() == true
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set. This string may
        contain any characters, including nulls.

        @par BNF
        @code
        userinfo      = [ [ user ] [ ':' password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">
            3.2.1. User Information (rfc3986)</a>

        @see
            @ref remove_password,
            @ref set_encoded_password,
            @ref set_encoded_user,
            @ref set_user.
    */
    url_base&
    set_encoded_password(
        pct_string_view s);

    /** Remove the password

        This function removes the password from
        the userinfo if a password exists. If
        there is no userinfo or no authority,
        the call has no effect.

        @note
        The interpretation of the userinfo as
        individual user and password components
        is scheme-dependent. Transmitting
        passwords in URLs is deprecated.

        @par Example
        @code
        assert( url( "http://user:pass@example.com" ).remove_password().authority().buffer() == "user@example.com" );
        @endcode

        @par Postconditions
        @code
        this->has_password() == false && this->encoded_password().empty() == true
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        userinfo      = [ [ user ] [ ':' password ] ]

        user          = *( unreserved / pct-encoded / sub-delims )
        password      = *( unreserved / pct-encoded / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1">
            3.2.1. User Information (rfc3986)</a>

        @see
            @ref set_encoded_password,
            @ref set_encoded_user,
            @ref set_password,
            @ref set_user.
    */
    url_base&
    remove_password() noexcept;

    //--------------------------------------------
    //
    // Host
    //
    //--------------------------------------------

    /** Set the host

        Depending on the contents of the passed
        string, this function sets the host:

        @li If the string is a valid IPv4 address,
        then the host is set to the address.
        The host type is @ref host_type::ipv4.

        @li If the string is a valid IPv6 address
        enclosed in square brackets, then the
        host is set to that address.
        The host type is @ref host_type::ipv6.

        @li If the string is a valid IPvFuture
        address enclosed in square brackets, then
        the host is set to that address.
        The host type is @ref host_type::ipvfuture.

        @li Otherwise, the host name is set to
        the string, which may be empty.
        Reserved characters in the string are
        percent-escaped in the result.
        The host type is @ref host_type::name.

        In all cases, when this function returns,
        the URL contains an authority.

        @par Example
        @code
        assert( url( "http://www.example.com" ).set_host( "127.0.0.1" ).buffer() == "http://127.0.0.1" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal  = "[" ( IPv6address / IPvFuture  ) "]"

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://en.wikipedia.org/wiki/IPv4"
            >IPv4 (Wikipedia)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291"
            >IP Version 6 Addressing Architecture (rfc4291)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host,
            @ref set_encoded_host_address,
            @ref set_encoded_host_name,
            @ref set_host_address,
            @ref set_host_ipv4,
            @ref set_host_ipv6,
            @ref set_host_ipvfuture,
            @ref set_host_name.
    */
    url_base&
    set_host(
        core::string_view s);

    /** Set the host

        Depending on the contents of the passed
        string, this function sets the host:

        @li If the string is a valid IPv4 address,
        then the host is set to the address.
        The host type is @ref host_type::ipv4.

        @li If the string is a valid IPv6 address
        enclosed in square brackets, then the
        host is set to that address.
        The host type is @ref host_type::ipv6.

        @li If the string is a valid IPvFuture
        address enclosed in square brackets, then
        the host is set to that address.
        The host type is @ref host_type::ipvfuture.

        @li Otherwise, the host name is set to
        the string. This string can contain percent
        escapes, or can be empty.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.
        The host type is @ref host_type::name.

        In all cases, when this function returns,
        the URL contains an authority.

        @par Example
        @code
        assert( url( "http://www.example.com" ).set_host( "127.0.0.1" ).buffer() == "http://127.0.0.1" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal  = "[" ( IPv6address / IPvFuture  ) "]"

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://en.wikipedia.org/wiki/IPv4"
            >IPv4 (Wikipedia)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291"
            >IP Version 6 Addressing Architecture (rfc4291)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host_address,
            @ref set_encoded_host_name,
            @ref set_host,
            @ref set_host_address,
            @ref set_host_ipv4,
            @ref set_host_ipv6,
            @ref set_host_ipvfuture,
            @ref set_host_name.
    */
    url_base&
    set_encoded_host(pct_string_view s);

    /** Set the host to an address

        Depending on the contents of the passed
        string, this function sets the host:

        @li If the string is a valid IPv4 address,
        then the host is set to the address.
        The host type is @ref host_type::ipv4.

        @li If the string is a valid IPv6 address,
        then the host is set to that address.
        The host type is @ref host_type::ipv6.

        @li If the string is a valid IPvFuture,
        then the host is set to that address.
        The host type is @ref host_type::ipvfuture.

        @li Otherwise, the host name is set to
        the string, which may be empty.
        Reserved characters in the string are
        percent-escaped in the result.
        The host type is @ref host_type::name.

        In all cases, when this function returns,
        the URL contains an authority.

        @par Example
        @code
        assert( url( "http://www.example.com" ).set_host_address( "127.0.0.1" ).buffer() == "http://127.0.0.1" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true
        @endcode

        @par Complexity
        Linear in `s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set.

        @par BNF
        @code
        IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet

        dec-octet   = DIGIT                 ; 0-9
                    / %x31-39 DIGIT         ; 10-99
                    / "1" 2DIGIT            ; 100-199
                    / "2" %x30-34 DIGIT     ; 200-249
                    / "25" %x30-35          ; 250-255

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

        IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://en.wikipedia.org/wiki/IPv4"
            >IPv4 (Wikipedia)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291"
            >IP Version 6 Addressing Architecture (rfc4291)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host,
            @ref set_encoded_host_address,
            @ref set_encoded_host_name,
            @ref set_host,
            @ref set_host_address,
            @ref set_host_ipv4,
            @ref set_host_ipv6,
            @ref set_host_ipvfuture,
            @ref set_host_name.
    */
    url_base&
    set_host_address(core::string_view s);

    /** Set the host to an address

        Depending on the contents of the passed
        string, this function sets the host:

        @li If the string is a valid IPv4 address,
        then the host is set to the address.
        The host type is @ref host_type::ipv4.

        @li If the string is a valid IPv6 address,
        then the host is set to that address.
        The host type is @ref host_type::ipv6.

        @li If the string is a valid IPvFuture,
        then the host is set to that address.
        The host type is @ref host_type::ipvfuture.

        @li Otherwise, the host name is set to
        the string. This string can contain percent
        escapes, or can be empty.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.
        The host type is @ref host_type::name.

        In all cases, when this function returns,
        the URL contains an authority.

        @par Example
        @code
        assert( url( "http://www.example.com" ).set_host( "127.0.0.1" ).buffer() == "http://127.0.0.1" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set.

        @par BNF
        @code
        IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet

        dec-octet   = DIGIT                 ; 0-9
                    / %x31-39 DIGIT         ; 10-99
                    / "1" 2DIGIT            ; 100-199
                    / "2" %x30-34 DIGIT     ; 200-249
                    / "25" %x30-35          ; 250-255

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

        IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )

        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://en.wikipedia.org/wiki/IPv4"
            >IPv4 (Wikipedia)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc4291"
            >IP Version 6 Addressing Architecture (rfc4291)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host,
            @ref set_encoded_host_name,
            @ref set_host,
            @ref set_host_address,
            @ref set_host_ipv4,
            @ref set_host_ipv6,
            @ref set_host_ipvfuture,
            @ref set_host_name.
    */
    url_base&
    set_encoded_host_address(
        pct_string_view s);

    /** Set the host to an address

        The host is set to the specified IPv4
        address.
        The host type is @ref host_type::ipv4.

        @par Example
        @code
        assert( url("http://www.example.com").set_host_ipv4( ipv4_address( "127.0.0.1" ) ).buffer() == "http://127.0.0.1" );
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Postconditions
        @code
        this->has_authority() == true && this->host_ipv4_address() == addr && this->host_type() == host_type::ipv4
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param addr The address to set.

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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host,
            @ref set_encoded_host_address,
            @ref set_encoded_host_name,
            @ref set_host,
            @ref set_host_address,
            @ref set_host_ipv6,
            @ref set_host_ipvfuture,
            @ref set_host_name.
    */
    url_base&
    set_host_ipv4(
        ipv4_address const& addr);

    /** Set the host to an address

        The host is set to the specified IPv6
        address.
        The host type is @ref host_type::ipv6.

        @par Example
        @code
        assert( url().set_host_ipv6( ipv6_address( "1::6:c0a8:1" ) ).authority().buffer() == "[1::6:c0a8:1]" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true && this->host_ipv6_address() == addr && this->host_type() == host_type::ipv6
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param addr The address to set.

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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host,
            @ref set_encoded_host_address,
            @ref set_encoded_host_name,
            @ref set_host,
            @ref set_host_address,
            @ref set_host_ipv4,
            @ref set_host_ipvfuture,
            @ref set_host_name.
    */
    url_base&
    set_host_ipv6(
        ipv6_address const& addr);

    /** Set the host to an address

        The host is set to the specified IPvFuture
        string.
        The host type is @ref host_type::ipvfuture.

        @par Example
        @code
        assert( url().set_host_ipvfuture( "v42.bis" ).buffer() == "//[v42.bis]" );
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Postconditions
        @code
        this->has_authority() == true && this->host_ipvfuture) == s && this->host_type() == host_type::ipvfuture
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set.

        @par BNF
        @code
        IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host,
            @ref set_encoded_host_address,
            @ref set_encoded_host_name,
            @ref set_host,
            @ref set_host_address,
            @ref set_host_ipv4,
            @ref set_host_ipv6,
            @ref set_host_name.
    */
    url_base&
    set_host_ipvfuture(
        core::string_view s);

    /** Set the host to a name

        The host is set to the specified string,
        which may be empty.
        Reserved characters in the string are
        percent-escaped in the result.
        The host type is @ref host_type::name.

        @par Example
        @code
        assert( url( "http://www.example.com/index.htm").set_host_name( "localhost" ).host_address() == "localhost" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true && this->host_ipv6_address() == addr && this->host_type() == host_type::name
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set.

        @par BNF
        @code
        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host,
            @ref set_encoded_host_address,
            @ref set_encoded_host_name,
            @ref set_host,
            @ref set_host_address,
            @ref set_host_ipv4,
            @ref set_host_ipv6,
            @ref set_host_ipvfuture.
    */
    url_base&
    set_host_name(
        core::string_view s);

    /** Set the host to a name

        The host is set to the specified string,
        which may contain percent-escapes and
        can be empty.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.
        The host type is @ref host_type::name.

        @par Example
        @code
        assert( url( "http://www.example.com/index.htm").set_encoded_host_name( "localhost" ).host_address() == "localhost" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true && this->host_ipv6_address() == addr && this->host_type() == host_type::name
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set.

        @par BNF
        @code
        reg-name    = *( unreserved / pct-encoded / "-" / ".")
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2">
            3.2.2. Host (rfc3986)</a>

        @see
            @ref set_encoded_host,
            @ref set_encoded_host_address,
            @ref set_host,
            @ref set_host_address,
            @ref set_host_ipv4,
            @ref set_host_ipv6,
            @ref set_host_ipvfuture,
            @ref set_host_name.
    */
    url_base&
    set_encoded_host_name(
        pct_string_view s);

    //--------------------------------------------

    /** Set the port

        The port is set to the specified integer.

        @par Example
        @code
        assert( url( "http://www.example.com" ).set_port_number( 8080 ).authority().buffer() == "www.example.com:8080" );
        @endcode

        @par Postconditions
        @code
        this->has_authority() == true && this->has_port() == true && this->port_number() == n
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param n The port number to set.

        @par BNF
        @code
        authority     = [ userinfo "@" ] host [ ":" port ]

        port          = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.3">
            3.2.3. Port (rfc3986)</a>

        @see
            @ref remove_port,
            @ref set_port.
    */
    url_base&
    set_port_number(std::uint16_t n);

    /** Set the port

        This port is set to the string, which
        must contain only digits or be empty.
        An empty port string is distinct from
        having no port.

        @par Example
        @code
        assert( url( "http://www.example.com" ).set_port( "8080" ).authority().buffer() == "www.example.com:8080" );
        @endcode

        @par Postconditions
        @code
        this->has_port() == true && this->port_number() == n && this->port() == std::to_string(n)
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` does not contain a valid port.

        @param s The port string to set.

        @par BNF
        @code
        port          = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.3">
            3.2.3. Port (rfc3986)</a>

        @see
            @ref remove_port,
            @ref set_port.
    */
    url_base&
    set_port(core::string_view s);

    /** Remove the port

        If a port exists, it is removed. The rest
        of the authority is unchanged.

        @par Example
        @code
        assert( url( "http://www.example.com:80" ).remove_port().authority().buffer() == "www.example.com" );
        @endcode

        @par Postconditions
        @code
        this->has_port() == false && this->port_number() == 0 && this->port() == ""
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        authority     = [ userinfo "@" ] host [ ":" port ]

        port          = *DIGIT
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.3">
            3.2.3. Port (rfc3986)</a>

        @see
            @ref set_port.
    */
    url_base&
    remove_port() noexcept;

    //--------------------------------------------
    //
    // Path
    //
    //--------------------------------------------

    /** Set if the path is absolute

        This function adjusts the path to make
        it absolute or not, depending on the
        parameter.

        @note
        If an authority is present, the path
        is always absolute. In this case, the
        function has no effect.

        @par Example
        @code
        url u( "path/to/file.txt" );
        assert( u.set_path_absolute( true ) );
        assert( u.buffer() == "/path/to/file.txt" );
        @endcode

        @par Postconditions
        @code
        this->is_path_absolute() == true && this->encoded_path().front() == '/'
        @endcode

        @return true on success.

        @par Complexity
        Linear in `this->size()`.

        @par BNF
        @code
        path          = path-abempty    ; begins with "/" or is empty
                      / path-absolute   ; begins with "/" but not "//"
                      / path-noscheme   ; begins with a non-colon segment
                      / path-rootless   ; begins with a segment
                      / path-empty      ; zero characters

        path-abempty  = *( "/" segment )
        path-absolute = "/" [ segment-nz *( "/" segment ) ]
        path-noscheme = segment-nz-nc *( "/" segment )
        path-rootless = segment-nz *( "/" segment )
        path-empty    = 0<pchar>
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3.  Path (rfc3986)</a>

        @see
            @ref encoded_segments,
            @ref segments,
            @ref set_encoded_path,
            @ref set_path.
    */
    bool
    set_path_absolute(bool absolute);

    /** Set the path.

        This function sets the path to the
        string, which may be empty.
        Reserved characters in the string are
        percent-escaped in the result.

        @note
        The library may adjust the final result
        to ensure that no other parts of the url
        is semantically affected.

        @note
        This function does not encode '/' chars, which
        are unreserved for paths but reserved for
        path segments. If a path segment should include
        encoded '/'s to differentiate it from path separators,
        the functions @ref set_encoded_path or @ref segments
        should be used instead.

        @par Example
        @code
        url u( "http://www.example.com" );

        u.set_path( "path/to/file.txt" );

        assert( u.path() == "/path/to/file.txt" );
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set.

        @par BNF
        @code
        path          = path-abempty    ; begins with "/" or is empty
                      / path-absolute   ; begins with "/" but not "//"
                      / path-noscheme   ; begins with a non-colon segment
                      / path-rootless   ; begins with a segment
                      / path-empty      ; zero characters

        path-abempty  = *( "/" segment )
        path-absolute = "/" [ segment-nz *( "/" segment ) ]
        path-noscheme = segment-nz-nc *( "/" segment )
        path-rootless = segment-nz *( "/" segment )
        path-empty    = 0<pchar>
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3.  Path (rfc3986)</a>

        @see
            @ref encoded_segments,
            @ref segments,
            @ref set_encoded_path,
            @ref set_path_absolute.
    */
    url_base&
    set_path(
        core::string_view s);

    /** Set the path.

        This function sets the path to the
        string, which may contain percent-escapes
        and can be empty.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.

        @note
        The library may adjust the final result
        to ensure that no other parts of the url
        is semantically affected.

        @par Example
        @code
        url u( "http://www.example.com" );

        u.set_encoded_path( "path/to/file.txt" );

        assert( u.encoded_path() == "/path/to/file.txt" );
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set.

        @par BNF
        @code
        path          = path-abempty    ; begins with "/" or is empty
                      / path-absolute   ; begins with "/" but not "//"
                      / path-noscheme   ; begins with a non-colon segment
                      / path-rootless   ; begins with a segment
                      / path-empty      ; zero characters

        path-abempty  = *( "/" segment )
        path-absolute = "/" [ segment-nz *( "/" segment ) ]
        path-noscheme = segment-nz-nc *( "/" segment )
        path-rootless = segment-nz *( "/" segment )
        path-empty    = 0<pchar>
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3.  Path (rfc3986)</a>

        @see
            @ref encoded_segments,
            @ref segments,
            @ref set_path,
            @ref set_path_absolute.
    */
    url_base&
    set_encoded_path(
        pct_string_view s);

    /** Return the path as a container of segments

        This function returns a bidirectional
        view of segments over the path.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Any percent-escapes in strings returned
        when iterating the view are decoded first.
        The container is modifiable; changes
        to the container are reflected in the
        underlying URL.

        @par Example
        @code
        url u( "http://example.com/path/to/file.txt" );

        segments sv = u.segments();
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        path          = path-abempty    ; begins with "/" or is empty
                      / path-absolute   ; begins with "/" but not "//"
                      / path-noscheme   ; begins with a non-colon segment
                      / path-rootless   ; begins with a segment
                      / path-empty      ; zero characters

        path-abempty  = *( "/" segment )
        path-absolute = "/" [ segment-nz *( "/" segment ) ]
        path-noscheme = segment-nz-nc *( "/" segment )
        path-rootless = segment-nz *( "/" segment )
        path-empty    = 0<pchar>
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3.  Path (rfc3986)</a>

        @see
            @ref encoded_segments,
            @ref set_encoded_path,
            @ref set_path,
            @ref set_path_absolute.
    */
    urls::segments_ref
    segments() noexcept;

    /// @copydoc url_view_base::segments
    segments_view
    segments() const noexcept
    {
        return url_view_base::segments();
    }

    /** Return the path as a container of segments

        This function returns a bidirectional
        view of segments over the path.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Strings returned when iterating the
        range may contain percent escapes.
        The container is modifiable; changes
        to the container are reflected in the
        underlying URL.

        @par Example
        @code
        url u( "http://example.com/path/to/file.txt" );

        segments_encoded_ref sv = u.encoded_segments();
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        path          = path-abempty    ; begins with "/" or is empty
                      / path-absolute   ; begins with "/" but not "//"
                      / path-noscheme   ; begins with a non-colon segment
                      / path-rootless   ; begins with a segment
                      / path-empty      ; zero characters

        path-abempty  = *( "/" segment )
        path-absolute = "/" [ segment-nz *( "/" segment ) ]
        path-noscheme = segment-nz-nc *( "/" segment )
        path-rootless = segment-nz *( "/" segment )
        path-empty    = 0<pchar>
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3.  Path (rfc3986)</a>

        @see
            @ref encoded_segments,
            @ref set_encoded_path,
            @ref set_path,
            @ref set_path_absolute.
    */
    segments_encoded_ref
    encoded_segments() noexcept;

    /// @copydoc url_view_base::encoded_segments
    segments_encoded_view
    encoded_segments() const noexcept
    {
        return url_view_base::encoded_segments();
    }

    //--------------------------------------------
    //
    // Query
    //
    //--------------------------------------------

    /** Set the query

        This sets the query to the string, which
        can be empty.
        An empty query is distinct from having
        no query.
        Reserved characters in the string are
        percent-escaped in the result.

        @par Example
        @code
        assert( url( "http://example.com" ).set_query( "id=42" ).query() == "id=42" );
        @endcode

        @par Postconditions
        @code
        this->has_query() == true && this->query() == s
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set.

        @par BNF
        @code
        query           = *( pchar / "/" / "?" )

        query-param     = key [ "=" value ]
        query-params    = [ query-param ] *( "&" query-param )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref params,
            @ref remove_query,
            @ref set_encoded_query.
    */
    url_base&
    set_query(
        core::string_view s);

    /** Set the query

        This sets the query to the string, which
        may contain percent-escapes and can be
        empty.
        An empty query is distinct from having
        no query.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.

        @par Example
        @code
        assert( url( "http://example.com" ).set_encoded_query( "id=42" ).encoded_query() == "id=42" );
        @endcode

        @par Postconditions
        @code
        this->has_query() == true && this->query() == decode_view( s );
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @param s The string to set.

        @throws system_error
        `s` contains an invalid percent-encoding.

        @par BNF
        @code
        query           = *( pchar / "/" / "?" )

        query-param     = key [ "=" value ]
        query-params    = [ query-param ] *( "&" query-param )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref params,
            @ref remove_query,
            @ref set_query.
    */
    url_base&
    set_encoded_query(
        pct_string_view s);

    /** Return the query as a container of parameters

        This function returns a bidirectional
        view of key/value pairs over the query.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Any percent-escapes in strings returned
        when iterating the view are decoded first.
        The container is modifiable; changes
        to the container are reflected in the
        underlying URL.

        @par Example
        @code
        params_ref pv = url( "/sql?id=42&name=jane%2Ddoe&page+size=20" ).params();
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        query           = *( pchar / "/" / "?" )

        query-param     = key [ "=" value ]
        query-params    = [ query-param ] *( "&" query-param )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref remove_query,
            @ref set_encoded_query,
            @ref set_query.
    */
    params_ref
    params() noexcept;

    /// @copydoc url_view_base::params
    params_view
    params() const noexcept
    {
        return url_view_base::params();
    }

    /** Return the query as a container of parameters

        This function returns a bidirectional
        view of key/value pairs over the query.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Any percent-escapes in strings returned
        when iterating the view are decoded first.
        The container is modifiable; changes
        to the container are reflected in the
        underlying URL.

        @par Example
        @code
        encoding_opts opt;
        opt.space_as_plus = true;
        params_ref pv = url( "/sql?id=42&name=jane+doe&page+size=20" ).params(opt);
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param opt The options for decoding. If
        this parameter is omitted, the `space_as_plus`
        is used.

        @par BNF
        @code
        query           = *( pchar / "/" / "?" )

        query-param     = key [ "=" value ]
        query-params    = [ query-param ] *( "&" query-param )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref remove_query,
            @ref set_encoded_query,
            @ref set_query.
    */
    params_ref
    params(encoding_opts opt) noexcept;

    /// @copydoc url_view_base::encoded_params
    params_encoded_view
    encoded_params() const noexcept
    {
        return url_view_base::encoded_params();
    }

    /** Return the query as a container of parameters

        This function returns a bidirectional
        view of key/value pairs over the query.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Strings returned when iterating the
        range may contain percent escapes.
        The container is modifiable; changes
        to the container are reflected in the
        underlying URL.

        @par Example
        @code
        params_encoded_ref pv = url( "/sql?id=42&name=jane%2Ddoe&page+size=20" ).encoded_params();
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        query           = *( pchar / "/" / "?" )

        query-param     = key [ "=" value ]
        query-params    = [ query-param ] *( "&" query-param )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref params,
            @ref remove_query,
            @ref set_encoded_query,
            @ref set_query.
    */
    params_encoded_ref
    encoded_params() noexcept;

    /** Set the query params

        This sets the query params to the list
        of param_view, which can be empty.

        An empty list of params is distinct from
        having no params.

        Reserved characters in the string are
        percent-escaped in the result.

        @par Example
        @code
        assert( url( "http://example.com" ).set_params( {"id", "42"} ).query() == "id=42" );
        @endcode

        @par Postconditions
        @code
        this->has_query() == true
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @par Complexity
        Linear.

        @param ps The params to set.

        @par BNF
        @code
        query           = *( pchar / "/" / "?" )

        query-param     = key [ "=" value ]
        query-params    = [ query-param ] *( "&" query-param )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref remove_query,
            @ref set_encoded_query,
            @ref set_query.
    */
    url_base&
    set_params( std::initializer_list<param_view> ps ) noexcept;

    /** Set the query params

        This sets the query params to the elements
        in the list, which may contain
        percent-escapes and can be empty.

        An empty list of params is distinct from
        having no query.

        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.

        @par Example
        @code
        assert( url( "http://example.com" ).set_encoded_params( {"id", "42"} ).encoded_query() == "id=42" );
        @endcode

        @par Postconditions
        @code
        this->has_query() == true
        @endcode

        @par Complexity
        Linear.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @param ps The params to set.

        @throws system_error
        some element in `ps` contains an invalid percent-encoding.

        @par BNF
        @code
        query           = *( pchar / "/" / "?" )

        query-param     = key [ "=" value ]
        query-params    = [ query-param ] *( "&" query-param )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4. Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref set_params,
            @ref params,
            @ref remove_query,
            @ref set_encoded_query,
            @ref set_query.
    */
    url_base&
    set_encoded_params( std::initializer_list< param_pct_view > ps ) noexcept;

    /** Remove the query

        If a query is present, it is removed.
        An empty query is distinct from having
        no query.

        @par Example
        @code
        assert( url( "http://www.example.com?id=42" ).remove_query().buffer() == "http://www.example.com" );
        @endcode

        @par Postconditions
        @code
        this->has_query() == false && this->params().empty()
        @endcode

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        query           = *( pchar / "/" / "?" )

        query-param     = key [ "=" value ]
        query-params    = [ query-param ] *( "&" query-param )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref params,
            @ref set_encoded_query,
            @ref set_query.
    */
    url_base&
    remove_query() noexcept;

    //--------------------------------------------
    //
    // Fragment
    //
    //--------------------------------------------

    /** Remove the fragment

        This function removes the fragment.
        An empty fragment is distinct from
        having no fragment.

        @par Example
        @code
        assert( url( "?first=john&last=doe#anchor" ).remove_fragment().buffer() == "?first=john&last=doe" );
        @endcode

        @par Postconditions
        @code
        this->has_fragment() == false && this->encoded_fragment() == ""
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        fragment    = *( pchar / "/" / "?" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.5"
            >3.5.  Fragment</a>

        @see
            @ref remove_fragment,
            @ref set_encoded_fragment,
            @ref set_fragment.
    */
    url_base&
    remove_fragment() noexcept;

    /** Set the fragment.

        This function sets the fragment to the
        specified string, which may be empty.
        An empty fragment is distinct from
        having no fragment.
        Reserved characters in the string are
        percent-escaped in the result.

        @par Example
        @code
        assert( url("?first=john&last=doe" ).set_encoded_fragment( "john doe" ).encoded_fragment() == "john%20doe" );
        @endcode

        @par Postconditions
        @code
        this->has_fragment() == true && this->fragment() == s
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param s The string to set.

        @par BNF
        @code
        fragment    = *( pchar / "/" / "?" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.5"
            >3.5.  Fragment</a>

        @see
            @ref remove_fragment,
            @ref set_encoded_fragment.
    */
    url_base&
    set_fragment(
        core::string_view s);

    /** Set the fragment.

        This function sets the fragment to the
        specified string, which may contain
        percent-escapes and which may be empty.
        An empty fragment is distinct from
        having no fragment.
        Escapes in the string are preserved,
        and reserved characters in the string
        are percent-escaped in the result.

        @par Example
        @code
        assert( url("?first=john&last=doe" ).set_encoded_fragment( "john%2Ddoe" ).fragment() == "john-doe" );
        @endcode

        @par Postconditions
        @code
        this->has_fragment() == true && this->fragment() == decode_view( s )
        @endcode

        @par Complexity
        Linear in `this->size() + s.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid percent-encoding.

        @param s The string to set.

        @par BNF
        @code
        fragment    = *( pchar / "/" / "?" )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.5"
            >3.5.  Fragment</a>

        @see
            @ref remove_fragment,
            @ref set_fragment.
    */
    url_base&
    set_encoded_fragment(
        pct_string_view s);

    //--------------------------------------------
    //
    // Compound Fields
    //
    //--------------------------------------------

    /** Remove the origin component

        This function removes the origin, which
        consists of the scheme and authority.

        @par Example
        @code
        assert( url( "http://www.example.com/index.htm" ).remove_origin().buffer() == "/index.htm" );
        @endcode

        @par Postconditions
        @code
        this->scheme_id() == scheme::none && this->has_authority() == false
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Throws nothing.
    */
    url_base&
    remove_origin();

    //--------------------------------------------
    //
    // Normalization
    //
    //--------------------------------------------

    /** Normalize the URL components

        Applies Syntax-based normalization to
        all components of the URL.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>

    */
    url_base&
    normalize();

    /** Normalize the URL scheme

        Applies Syntax-based normalization to the
        URL scheme.

        The scheme is normalized to lowercase.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>

    */
    url_base&
    normalize_scheme();

    /** Normalize the URL authority

        Applies Syntax-based normalization to the
        URL authority.

        Percent-encoding triplets are normalized
        to uppercase letters. Percent-encoded
        octets that correspond to unreserved
        characters are decoded.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>

    */
    url_base&
    normalize_authority();

    /** Normalize the URL path

        Applies Syntax-based normalization to the
        URL path.

        Percent-encoding triplets are normalized
        to uppercase letters. Percent-encoded
        octets that correspond to unreserved
        characters are decoded. Redundant
        path-segments are removed.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>

    */
    url_base&
    normalize_path();

    /** Normalize the URL query

        Applies Syntax-based normalization to the
        URL query.

        Percent-encoding triplets are normalized
        to uppercase letters. Percent-encoded
        octets that correspond to unreserved
        characters are decoded.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>

    */
    url_base&
    normalize_query();

    /** Normalize the URL fragment

        Applies Syntax-based normalization to the
        URL fragment.

        Percent-encoding triplets are normalized
        to uppercase letters. Percent-encoded
        octets that correspond to unreserved
        characters are decoded.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>

    */
    url_base&
    normalize_fragment();

    //
    // (end of fluent API)
    //
    //--------------------------------------------

    //--------------------------------------------
    //
    // Resolution
    //
    //--------------------------------------------

    /** Resolve a URL reference against this base URL

        This function attempts to resolve a URL
        reference `ref` against this base URL
        in a manner similar to that of a web browser
        resolving an anchor tag.

        This URL must satisfy the <em>URI</em>
        grammar. In other words, it must contain
        a scheme.

        Relative references are only usable when
        in the context of a base absolute URI.
        This process of resolving a relative
        <em>reference</em> within the context of
        a <em>base</em> URI is defined in detail
        in rfc3986 (see below).

        The resolution process works as if the
        relative reference is appended to the base
        URI and the result is normalized.

        Given the input base URL, this function
        resolves the relative reference
        as if performing the following steps:

        @li Ensure the base URI has at least a scheme
        @li Normalizing the reference path
        @li Merge base and reference paths
        @li Normalize the merged path

        This function places the result of the
        resolution into this URL in place.

        If an error occurs, the contents of
        this URL are unspecified and a @ref result
        with an `system::error_code` is returned.

        @note Abnormal hrefs where the number of ".."
        segments exceeds the number of segments in
        the base path are handled by including the
        unmatched ".." segments in the result, as described
        in <a href="https://www.rfc-editor.org/errata/eid4547"
        >Errata 4547</a>.

        @par Example
        @code
        url base1( "/one/two/three" );
        base1.resolve("four");
        assert( base1.buffer() == "/one/two/four" );

        url base2( "http://example.com/" )
        base2.resolve("/one");
        assert( base2.buffer() == "http://example.com/one" );

        url base3( "http://example.com/one" );
        base3.resolve("/two");
        assert( base3.buffer() == "http://example.com/two" );

        url base4( "http://a/b/c/d;p?q" );
        base4.resolve("g#s");
        assert( base4.buffer() == "http://a/b/c/g#s" );
        @endcode

        @par BNF
        @code
        absolute-URI  = scheme ":" hier-part [ "?" query ]
        @endcode

        @par Exception Safety
        Basic guarantee.
        Calls to allocate may throw.

        @return An empty @ref result upon success,
        otherwise an error code if `!base.has_scheme()`.

        @param ref The URL reference to resolve.

        @par Specification
        <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-5"
            >5. Reference Resolution (rfc3986)</a>

        @see
            @ref url,
            @ref url_view.
    */
    system::result<void>
    resolve(
        url_view_base const& ref);

    friend
    system::result<void>
    resolve(
        url_view_base const& base,
        url_view_base const& ref,
        url_base& dest);

private:
    //--------------------------------------------
    //
    // implementation
    //
    //--------------------------------------------

    void  check_invariants() const noexcept;

    char* resize_impl(int, std::size_t, op_t&);
    char* resize_impl(int, int, std::size_t, op_t&);
    char* shrink_impl(int, std::size_t, op_t&);
    char* shrink_impl(int, int, std::size_t, op_t&);

    void  set_scheme_impl(core::string_view, urls::scheme);
    char* set_user_impl(std::size_t n, op_t& op);
    char* set_password_impl(std::size_t n, op_t& op);
    char* set_userinfo_impl(std::size_t n, op_t& op);
    char* set_host_impl(std::size_t n, op_t& op);
    char* set_port_impl(std::size_t n, op_t& op);
    char* set_path_impl(std::size_t n, op_t& op);

    core::string_view
    first_segment() const noexcept;

    detail::segments_iter_impl
    edit_segments(
        detail::segments_iter_impl const&,
        detail::segments_iter_impl const&,
        detail::any_segments_iter&& it0,
        int absolute = -1);

    auto
    edit_params(
        detail::params_iter_impl const&,
        detail::params_iter_impl const&,
        detail::any_params_iter&&) ->
            detail::params_iter_impl;

    system::result<void>
    resolve_impl(
        url_view_base const& base,
        url_view_base const& ref);

    template<class CharSet>
    void normalize_octets_impl(int,
        CharSet const& allowed, op_t&) noexcept;
    void decoded_to_lower_impl(int id) noexcept;
    void to_lower_impl(int id) noexcept;
};

//------------------------------------------------

/** Resolve a URL reference against a base URL

    This function attempts to resolve a URL
    reference `ref` against the base URL `base`
    in a manner similar to that of a web browser
    resolving an anchor tag.

    The base URL must satisfy the <em>URI</em>
    grammar. In other words, it must contain
    a scheme.

    Relative references are only usable when
    in the context of a base absolute URI.
    This process of resolving a relative
    <em>reference</em> within the context of
    a <em>base</em> URI is defined in detail
    in rfc3986 (see below).

    The resolution process works as if the
    relative reference is appended to the base
    URI and the result is normalized.

    Given the input base URL, this function
    resolves the relative reference
    as if performing the following steps:

    @li Ensure the base URI has at least a scheme
    @li Normalizing the reference path
    @li Merge base and reference paths
    @li Normalize the merged path

    This function places the result of the
    resolution into `dest`, which can be
    any of the url containers that inherit
    from @ref url_base.

    If an error occurs, the contents of
    `dest` is unspecified and `ec` is set.

    @note Abnormal hrefs where the number of ".."
    segments exceeds the number of segments in
    the base path are handled by including the
    unmatched ".." segments in the result, as described
    in <a href="https://www.rfc-editor.org/errata/eid4547"
    >Errata 4547</a>.

    @par Example
    @code
    url dest;
    system::error_code ec;

    resolve("/one/two/three", "four", dest, ec);
    assert( dest.str() == "/one/two/four" );

    resolve("http://example.com/", "/one", dest, ec);
    assert( dest.str() == "http://example.com/one" );

    resolve("http://example.com/one", "/two", dest, ec);
    assert( dest.str() == "http://example.com/two" );

    resolve("http://a/b/c/d;p?q", "g#s", dest, ec);
    assert( dest.str() == "http://a/b/c/g#s" );
    @endcode

    @par BNF
    @code
    absolute-URI  = scheme ":" hier-part [ "?" query ]
    @endcode

    @par Exception Safety
    Basic guarantee.
    Calls to allocate may throw.

    @return An empty @ref result upon success,
    otherwise an error code if `!base.has_scheme()`.

    @param base The base URL to resolve against.

    @param ref The URL reference to resolve.

    @param dest The container where the result
    is written, upon success.

    @par Specification
    <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-5"
        >5. Reference Resolution (rfc3986)</a>

    @see
        @ref url,
        @ref url_view.
*/
inline
system::result<void>
resolve(
    url_view_base const& base,
    url_view_base const& ref,
    url_base& dest)
{
    if (&dest != &base)
        dest.copy(base);
    return dest.resolve(ref);
}

} // urls
} // boost

// These are here because of circular references
#include <boost/url/impl/params_ref.hpp>
#include <boost/url/impl/params_encoded_ref.hpp>
#include <boost/url/impl/segments_ref.hpp>
#include <boost/url/impl/segments_encoded_ref.hpp>

#endif
