//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_URL_VIEW_BASE_HPP
#define BOOST_URL_URL_VIEW_BASE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/authority_view.hpp>
#include <boost/url/host_type.hpp>
#include <boost/url/ipv4_address.hpp>
#include <boost/url/ipv6_address.hpp>
#include <boost/url/params_view.hpp>
#include <boost/url/params_encoded_view.hpp>
#include <boost/url/pct_string_view.hpp>
#include <boost/url/scheme.hpp>
#include <boost/url/segments_encoded_view.hpp>
#include <boost/url/segments_view.hpp>
#include <boost/url/detail/url_impl.hpp>
#include <boost/url/grammar/string_token.hpp>
#include <boost/assert.hpp>
#include <cstddef>
#include <cstdint>
#include <iosfwd>
#include <memory>
#include <string>
#include <utility>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
namespace detail {
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
    url_view_base
    : private detail::parts_base
{
    detail::url_impl impl_;
    detail::url_impl const* pi_;

    friend class url;
    friend class url_base;
    friend class url_view;
    friend class static_url_base;
    friend class params_base;
    friend class params_encoded_base;
    friend class params_encoded_ref;
    friend class params_encoded_view;
    friend class params_ref;
    friend class params_view;
    friend class segments_base;
    friend class segments_encoded_base;
    friend class segments_encoded_ref;
    friend class segments_encoded_view;
    friend class segments_ref;
    friend class segments_view;
    friend struct detail::pattern;

    struct shared_impl;

    url_view_base() noexcept;

    explicit url_view_base(
        detail::url_impl const&) noexcept;

    ~url_view_base() = default;

    url_view_base(
        url_view_base const& o) noexcept
        : impl_(o.impl_)
        , pi_(o.pi_)
    {
        if (pi_ == &o.impl_)
            pi_ = &impl_;
    }

    url_view_base& operator=(
        url_view_base const&) = delete;

#ifndef BOOST_URL_DOCS
public:
#endif
    std::size_t
    digest(std::size_t = 0) const noexcept;

public:
    //--------------------------------------------
    //
    // Observers
    //
    //--------------------------------------------

    /** Return the maximum number of characters possible

        This represents the largest number
        of characters that are theoretically
        possible to represent in a url,
        not including any null terminator.
        In practice the actual possible size
        may be lower than this number.

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    static
    constexpr
    std::size_t
    max_size() noexcept
    {
        return BOOST_URL_MAX_SIZE;
    }

    /** Return the number of characters in the url

        This function returns the number of
        characters in the url's encoded string,
        not including any null terminator,
        if present.

        @par Example
        @code
        assert( url_view( "file:///Program%20Files" ).size() == 23 );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    std::size_t
    size() const noexcept
    {
        return pi_->offset(id_end);
    }

    /** Return true if the url is empty

        The empty string matches the
        <em>relative-ref</em> grammar.

        @par Example
        @code
        assert( url_view( "" ).empty() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        relative-ref  = relative-part [ "?" query ] [ "#" fragment ]

        relative-part = "//" authority path-abempty
                      / path-absolute
                      / path-noscheme
                      / path-empty
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.2"
            >4.2.  Relative Reference (rfc3986)</a>
    */
    bool
    empty() const noexcept
    {
        return pi_->offset(id_end) == 0;
    }

    /** Return a pointer to the url's character buffer

        This function returns a pointer to
        the first character of the url, which
        is not guaranteed to be null-terminated.

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    char const*
    data() const noexcept
    {
        return pi_->cs_;
    }

    /** Return the url string

        This function returns the entire url,
        which may contain percent escapes.

        @par Example
        @code
        assert( url_view( "http://www.example.com" ).buffer() == "http://www.example.com" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    core::string_view
    buffer() const noexcept
    {
        return core::string_view(
            data(), size());
    }

    /** Return the URL as a core::string_view

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

    */
    operator core::string_view() const noexcept
    {
        return buffer();
    }

    /** Return a shared, persistent copy of the url

        This function returns a read-only copy of
        the url, with shared lifetime. The returned
        value owns (persists) the underlying string.
        The algorithm used to create the value
        minimizes the number of individual memory
        allocations, making it more efficient than
        when using direct standard library functions.

        @par Example
        @code
        std::shared_ptr< url_view const > sp;
        {
            std::string s( "http://example.com" );
            url_view u( s );                        // u references characters in s

            assert( u.data() == s.data() );         // same buffer

            sp = u.persist();

            assert( sp->data() != s.data() );       // different buffer
            assert( sp->buffer() == s);             // same contents

            // s is destroyed and thus u
            // becomes invalid, but sp remains valid.
        }
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Calls to allocate may throw.
    */
    std::shared_ptr<
        url_view const> persist() const;

    //--------------------------------------------
    //
    // Scheme
    //
    //--------------------------------------------

    /** Return true a scheme is present

        This function returns true if this
        contains a scheme.

        @par Example
        @code
        assert( url_view( "http://www.example.com" ).has_scheme() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        URI             = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

        absolute-URI    = scheme ":" hier-part [ "?" query ]

        scheme          = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.1"
            >3.1. Scheme (rfc3986)</a>

        @see
            @ref scheme,
            @ref scheme_id.
    */
    bool
    has_scheme() const noexcept;

    /** Return the scheme

        This function returns the scheme if it
        exists, without a trailing colon (':').
        Otherwise it returns an empty string.
        Note that schemes are case-insensitive,
        and the canonical form is lowercased.

        @par Example
        @code
        assert( url_view( "http://www.example.com" ).scheme() == "http" );
        @endcode

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        scheme          = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )

        URI             = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

        absolute-URI    = scheme ":" hier-part [ "?" query ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.1"
            >3.1. Scheme (rfc3986)</a>

        @see
            @ref has_scheme,
            @ref scheme_id.
    */
    core::string_view
    scheme() const noexcept;

    /** Return the scheme

        This function returns a value which
        depends on the scheme in the url:

        @li If the scheme is a well-known
        scheme, corresponding value from
        the enumeration @ref urls::scheme
        is returned.

        @li If a scheme is present but is not
        a well-known scheme, the value
        returned is @ref urls::scheme::unknown.

        @li Otherwise, if the scheme is absent
        the value returned is
        @ref urls::scheme::none.

        @par Example
        @code
        assert( url_view( "wss://www.example.com/crypto.cgi" ).scheme_id() == scheme::wss );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        URI             = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

        absolute-URI    = scheme ":" hier-part [ "?" query ]

        scheme          = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.1"
            >3.1. Scheme (rfc3986)</a>

        @see
            @ref has_scheme,
            @ref scheme.
    */
    urls::scheme
    scheme_id() const noexcept;

    //--------------------------------------------
    //
    // Authority
    //
    //--------------------------------------------

    /** Return true if an authority is present

        This function returns true if the url
        contains an authority. The presence of
        an authority is denoted by a double
        slash ("//") at the beginning or after
        the scheme.

        @par Example
        @code
        assert( url_view( "http://www.example.com/index.htm" ).has_authority() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        authority       = [ userinfo "@" ] host [ ":" port ]

        URI             = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

        absolute-URI    = scheme ":" hier-part [ "?" query ]

        URI-reference   = URI / relative-ref

        relative-ref    = relative-part [ "?" query ] [ "#" fragment ]

        hier-part       = "//" authority path-abempty
                        ; (more...)

        relative-part   = "//" authority path-abempty
                        ; (more...)

        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2"
            >3.2. Authority (rfc3986)</a>

        @see
            @ref authority,
            @ref encoded_authority.
    */
    bool
    has_authority() const noexcept
    {
        return pi_->len(id_user) > 0;
    }

    /** Return the authority

        This function returns the authority as
        an @ref authority_view.

        @par Example
        @code
        authority_view a = url_view( "https://www.example.com:8080/index.htm" ).authority();
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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2"
            >3.2. Authority (rfc3986)</a>

        @see
            @ref encoded_authority,
            @ref has_authority.
    */
    authority_view
    authority() const noexcept;

    /** Return the authority.

        If present, this function returns a
        string representing the authority (which
        may be empty).
        Otherwise it returns an empty string.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "file://Network%20Drive/My%2DFiles" ).encoded_authority() == "Network%20Drive" );
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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.2"
            >3.2. Authority (rfc3986)</a>

        @see
            @ref authority,
            @ref has_authority.
    */
    pct_string_view
    encoded_authority() const noexcept;

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

    /** Return the userinfo

        If present, this function returns a
        string representing the userinfo (which
        may be empty).
        Otherwise it returns an empty string.
        Any percent-escapes in the string are
        decoded first.

        @note
        This function uses the string token
        return type customization. Depending on
        the token passed, the return type and
        behavior of the function can be different.
        See @ref string_token::return_string
        for more information.

        @par Example
        @code
        assert( url_view( "http://jane%2Ddoe:pass@example.com" ).userinfo() == "jane-doe:pass" );
        @endcode

        @par Complexity
        Linear in `this->userinfo().size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @return When called with no arguments,
        a value of type `std::string` is
        returned. Otherwise, the return type
        and meaning depends on the string token
        passed to the function.

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
        @li @ref host_type::none

        When @ref has_authority is false, the
        host type is @ref host_type::none.

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
        return pi_->host_type_;
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
        a string. Otherwise an empty string is returned.
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

    /** Return the IPv6 Zone ID

        If the host type is @ref host_type::ipv6,
        this function returns the Zone ID as
        a string. Otherwise an empty string is returned.
        Any percent-escapes in the string are
        decoded first.

        @par Example
        @code
        assert( url_view( "http://[fe80::1%25eth0]/" ).zone_id() == "eth0" );
        @endcode

        @par Complexity
        Linear in `this->encoded_zone_id().size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal = "[" ( IPv6address / IPv6addrz / IPvFuture  ) "]"

        ZoneID = 1*( unreserved / pct-encoded )

        IPv6addrz = IPv6address "%25" ZoneID
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc6874"
            >Representing IPv6 Zone Identifiers in Address Literals and Uniform Resource Identifiers</a>
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    zone_id(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_zone_id().decode(
            opt, std::move(token));
    }

    /** Return the IPv6 Zone ID

        If the host type is @ref host_type::ipv6,
        this function returns the Zone ID as
        a string. Otherwise an empty string is returned.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "http://[fe80::1%25eth0]/" ).encoded_zone_id() == "eth0" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        host        = IP-literal / IPv4address / reg-name

        IP-literal = "[" ( IPv6address / IPv6addrz / IPvFuture  ) "]"

        ZoneID = 1*( unreserved / pct-encoded )

        IPv6addrz = IPv6address "%25" ZoneID
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc6874"
            >Representing IPv6 Zone Identifiers in Address Literals and Uniform Resource Identifiers</a>
    */
    pct_string_view
    encoded_zone_id() const noexcept;

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

    //--------------------------------------------
    //
    // Path
    //
    //--------------------------------------------

    /** Return true if the path is absolute

        This function returns true if the path
        begins with a forward slash ('/').

        @par Example
        @code
        assert( url_view( "/path/to/file.txt" ).is_path_absolute() );
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
            @ref encoded_path,
            @ref encoded_segments.
            @ref path,
            @ref segments.
    */
    bool
    is_path_absolute() const noexcept
    {
        return
            pi_->len(id_path) > 0 &&
            pi_->cs_[pi_->offset(id_path)] == '/';
    }

    /** Return the path

        This function returns the path as a
        string. The path may be empty.
        Any percent-escapes in the string are
        decoded first.

        @par Example
        @code
        assert( url_view( "file:///Program%20Files/Games/config.ini" ).path() == "/Program Files/Games/config.ini" );
        @endcode

        @par Complexity
        Linear in `this->path().size()`.

        @par Exception Safety
        Calls to allocate may throw.

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
            >3.3. Path (rfc3986)</a>

        @see
            @ref is_path_absolute,
            @ref encoded_path,
            @ref encoded_segments.
            @ref segments.
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    path(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_path().decode(
            opt, std::move(token));
    }

    /** Return the path

        This function returns the path as a
        string. The path may be empty.
        Any percent-escapes in the string are
        decoded first.

        @par Example
        @code
        assert( url_view( "file:///Program%20Files/Games/config.ini" ).encoded_path() == "/Program%20Files/Games/config.ini" );
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
            >3.3. Path (rfc3986)</a>

        @see
            @ref is_path_absolute,
            @ref encoded_segments.
            @ref path,
            @ref segments.
    */
    pct_string_view
    encoded_path() const noexcept;

    /** Return the path as a container of segments

        This function returns a bidirectional
        view of strings over the path.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Any percent-escapes in strings returned
        when iterating the view are decoded first.

        @par Example
        @code
        segments_view sv = url_view( "/path/to/file.txt" ).segments();
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        path          = [ "/" ] segment *( "/" segment )
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3. Path (rfc3986)</a>

        @see
            @ref is_path_absolute,
            @ref encoded_path,
            @ref encoded_segments.
            @ref path,
            @ref segments_view.
    */
    segments_view
    segments() const noexcept;

    /** Return the path as a container of segments

        This function returns a bidirectional
        view of strings over the path.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Strings returned when iterating the
        range may contain percent escapes.

        @par Example
        @code
        segments_encoded_view sv = url_view( "/path/to/file.txt" ).encoded_segments();
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
            >3.3. Path (rfc3986)</a>

        @see
            @ref is_path_absolute,
            @ref encoded_path,
            @ref path,
            @ref segments,
            @ref segments_encoded_view.
    */
    segments_encoded_view
    encoded_segments() const noexcept;

    //--------------------------------------------
    //
    // Query
    //
    //--------------------------------------------

    /** Return true if a query is present

        This function returns true if this
        contains a query. An empty query is
        distinct from having no query.

        @par Example
        @code
        assert( url_view( "/sql?id=42&col=name&page-size=20" ).has_query() );
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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref encoded_query,
            @ref params,
            @ref query.
    */
    bool
    has_query() const noexcept;

    /** Return the query

        If this contains a query, it is returned
        as a string (which may be empty).
        Otherwise, an empty string is returned.
        Any percent-escapes in the string are
        decoded first.
        <br>
        When plus signs appear in the query
        portion of the url, they are converted
        to spaces automatically upon decoding.
        This behavior can be changed by setting
        decode options.

        @par Example
        @code
        assert( url_view( "/sql?id=42&name=jane%2Ddoe&page+size=20" ).query() == "id=42&name=jane-doe&page size=20" );
        @endcode

        @par Complexity
        Linear in `this->query().size()`.

        @par Exception Safety
        Calls to allocate may throw.

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
            @ref encoded_query,
            @ref has_query,
            @ref params.
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    query(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        // When interacting with the query as
        // an intact string, we do not treat
        // the plus sign as an encoded space.
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_query().decode(
            opt, std::move(token));
    }

    /** Return the query

        If this contains a query, it is returned
        as a string (which may be empty).
        Otherwise, an empty string is returned.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "/sql?id=42&name=jane%2Ddoe&page+size=20" ).encoded_query() == "id=42&name=jane%2Ddoe&page+size=20" );
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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref has_query,
            @ref params,
            @ref query.
    */
    pct_string_view
    encoded_query() const noexcept;

    /** Return the query as a container of parameters

        This function returns a bidirectional
        view of key/value pairs over the query.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Any percent-escapes in strings returned
        when iterating the view are decoded first.

        @par Example
        @code
        params_view pv = url_view( "/sql?id=42&name=jane%2Ddoe&page+size=20" ).params();
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
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4
            >3.4.  Query (rfc3986)</a>
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>

        @see
            @ref encoded_params,
            @ref encoded_query,
            @ref has_query,
            @ref query.
    */
    params_view
    params() const noexcept;

    params_view
    params(encoding_opts opt) const noexcept;

    /** Return the query as a container of parameters

        This function returns a bidirectional
        view of key/value pairs over the query.
        The returned view references the same
        underlying character buffer; ownership
        is not transferred.
        Strings returned when iterating the
        range may contain percent escapes.

        @par Example
        @code
        params_encoded_view pv = url_view( "/sql?id=42&name=jane%2Ddoe&page+size=20" ).encoded_params();
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4. Query (rfc3986)</a>

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
            @ref encoded_query,
            @ref has_query,
            @ref params,
            @ref query.
    */
    params_encoded_view
    encoded_params() const noexcept;

    //--------------------------------------------
    //
    // Fragment
    //
    //--------------------------------------------

    /** Return true if a fragment is present

        This function returns true if the url
        contains a fragment.
        An empty fragment is distinct from
        no fragment.

        @par Example
        @code
        assert( url_view( "http://www.example.com/index.htm#anchor" ).has_fragment() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

        relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.5"
            >3.5. Fragment (rfc3986)</a>

        @see
            @ref encoded_fragment,
            @ref fragment.
    */
    bool
    has_fragment() const noexcept;

    /** Return the fragment

        This function calculates the fragment
        of the url, with percent escapes decoded
        and without the leading pound sign ('#')
        whose presence indicates that the url
        contains a fragment.

        <br>

        This function accepts an optional
        <em>StringToken</em> parameter which
        controls the return type and behavior
        of the function:

        @li When called with no arguments,
        the return type of the function is
        `std::string`. Otherwise

        @li When called with a string token,
        the behavior and return type of the
        function depends on the type of string
        token being passed.

        @par Example
        @code
        assert( url_view( "http://www.example.com/index.htm#a%2D1" ).fragment() == "a-1" );
        @endcode

        @par Complexity
        Linear in `this->fragment().size()`.

        @par Exception Safety
        Calls to allocate may throw.
        String tokens may throw exceptions.

        @param token An optional string token to
        use. If this parameter is omitted, the
        function returns a new `std::string`.

        @par BNF
        @code
        fragment        = *( pchar / "/" / "?" )

        fragment-part   = [ "#" fragment ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.5"
            >3.5. Fragment (rfc3986)</a>

        @see
            @ref encoded_fragment,
            @ref has_fragment.
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    fragment(
        BOOST_URL_STRTOK_ARG(token)) const
    {
        encoding_opts opt;
        opt.space_as_plus = false;
        return encoded_fragment().decode(
            opt, std::move(token));
    }

    /** Return the fragment

        This function returns the fragment as a
        string with percent-escapes.
        Ownership is not transferred; the
        string returned references the underlying
        character buffer, which must remain valid
        or else undefined behavior occurs.

        @par Example
        @code
        assert( url_view( "http://www.example.com/index.htm#a%2D1" ).encoded_fragment() == "a%2D1" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        fragment        = *( pchar / "/" / "?" )

        pchar           = unreserved / pct-encoded / sub-delims / ":" / "@"
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.5"
            >3.5. Fragment (rfc3986)</a>

        @see
            @ref fragment,
            @ref has_fragment.
    */
    pct_string_view
    encoded_fragment() const noexcept;

    //--------------------------------------------
    //
    // Compound Fields
    //
    //--------------------------------------------

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

    /** Return the origin

        If an authority is present, this
        function returns the scheme and
        authority portion of the url.
        Otherwise, an empty string is
        returned.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "http://www.example.com:8080/index.htm?text=none#h1" ).encoded_origin() == "http://www.example.com:8080" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @see
            @ref encoded_resource,
            @ref encoded_target.
    */
    pct_string_view
    encoded_origin() const noexcept;

    /** Return the resource

        This function returns the resource, which
        is the portion of the url that includes
        only the path, query, and fragment.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "http://www.example.com/index.html?query#frag" ).encoded_resource() == "/index.html?query#frag" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3. Path (rfc3986)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4. Query (rfc3986)</a>

        @see
            @ref encoded_origin,
            @ref encoded_target.
    */
    pct_string_view
    encoded_resource() const noexcept;

    /** Return the target

        This function returns the target, which
        is the portion of the url that includes
        only the path and query.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "http://www.example.com/index.html?query#frag" ).encoded_target() == "/index.html?query" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3. Path (rfc3986)</a>
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4. Query (rfc3986)</a>

        @see
            @ref encoded_origin,
            @ref encoded_resource.
    */
    pct_string_view
    encoded_target() const noexcept;

    //--------------------------------------------
    //
    // Comparison
    //
    //--------------------------------------------

    /** Return the result of comparing this with another url

        This function compares two URLs
        according to Syntax-Based comparison
        algorithm.

        @par Complexity
        Linear in `min( u0.size(), u1.size() )`

        @par Exception Safety
        Throws nothing.

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>

        @return -1 if `*this < other`, 0 if
            `this == other`, and 1 if `this > other`.
    */
    int
    compare(url_view_base const& other) const noexcept;

    /** Return the result of comparing two URLs

        The URLs are compared component by
        component as if they were first
        normalized.

        @par Example
        @code
        url_view u0( "http://www.a.com/index.htm" );
        url_view u1( "http://www.a.com/index.htm" );
        assert( u0 == u1 );
        @endcode

        @par Effects
        @code
        url a(u0);
        a.normalize();
        url b(u1);
        b.normalize();
        return std::make_tuple(
                   a.scheme(),
                   a.user(),
                   a.password(),
                   a.host(),
                   a.port(),
                   a.path(),
                   a.query(),
                   a.fragment()) ==
               std::make_tuple(
                   b.scheme(),
                   b.user(),
                   b.password(),
                   b.host(),
                   b.port(),
                   b.path(),
                   b.query(),
                   b.fragment());
        @endcode

        @par Complexity
        Linear in `min( u0.size(), u1.size() )`

        @par Exception Safety
        Throws nothing

        @return `true` if `u0 == u1`

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>
    */
    friend
    bool
    operator==(
        url_view_base const& u0,
        url_view_base const& u1) noexcept
    {
        return u0.compare(u1) == 0;
    }

    /** Return the result of comparing two URLs

        The URLs are compared component by
        component as if they were first
        normalized.

        @par Example
        @code
        url_view u0( "http://www.a.com/index.htm" );
        url_view u1( "http://www.b.com/index.htm" );
        assert( u0 != u1 );
        @endcode

        @par Effects
        @code
        url a(u0);
        a.normalize();
        url b(u1);
        b.normalize();
        return std::make_tuple(
                   a.scheme(),
                   a.user(),
                   a.password(),
                   a.host(),
                   a.port(),
                   a.path(),
                   a.query(),
                   a.fragment()) !=
               std::make_tuple(
                   b.scheme(),
                   b.user(),
                   b.password(),
                   b.host(),
                   b.port(),
                   b.path(),
                   b.query(),
                   b.fragment());
        @endcode

        @par Complexity
        Linear in `min( u0.size(), u1.size() )`

        @par Exception Safety
        Throws nothing

        @return `true` if `u0 != u1`

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>
    */
    friend
    bool
    operator!=(
        url_view_base const& u0,
        url_view_base const& u1) noexcept
    {
        return ! (u0 == u1);
    }

    /** Return the result of comparing two URLs

        The URLs are compared component by
        component as if they were first
        normalized.

        @par Example
        @code
        url_view u0( "http://www.a.com/index.htm" );
        url_view u1( "http://www.b.com/index.htm" );
        assert( u0 < u1 );
        @endcode

        @par Effects
        @code
        url a(u0);
        a.normalize();
        url b(u1);
        b.normalize();
        return std::make_tuple(
                   a.scheme(),
                   a.user(),
                   a.password(),
                   a.host(),
                   a.port(),
                   a.path(),
                   a.query(),
                   a.fragment()) <
               std::make_tuple(
                   b.scheme(),
                   b.user(),
                   b.password(),
                   b.host(),
                   b.port(),
                   b.path(),
                   b.query(),
                   b.fragment());
        @endcode

        @par Complexity
        Linear in `min( u0.size(), u1.size() )`

        @par Exception Safety
        Throws nothing

        @return `true` if `u0 < u1`

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>
    */
    friend
    bool
    operator<(
        url_view_base const& u0,
        url_view_base const& u1) noexcept
    {
        return u0.compare(u1) < 0;
    }

    /** Return the result of comparing two URLs

        The URLs are compared component by
        component as if they were first
        normalized.

        @par Example
        @code
        url_view u0( "http://www.b.com/index.htm" );
        url_view u1( "http://www.b.com/index.htm" );
        assert( u0 <= u1 );
        @endcode

        @par Effects
        @code
        url a(u0);
        a.normalize();
        url b(u1);
        b.normalize();
        return std::make_tuple(
                   a.scheme(),
                   a.user(),
                   a.password(),
                   a.host(),
                   a.port(),
                   a.path(),
                   a.query(),
                   a.fragment()) <=
               std::make_tuple(
                   b.scheme(),
                   b.user(),
                   b.password(),
                   b.host(),
                   b.port(),
                   b.path(),
                   b.query(),
                   b.fragment());
        @endcode

        @par Complexity
        Linear in `min( u0.size(), u1.size() )`

        @par Exception Safety
        Throws nothing

        @return `true` if `u0 <= u1`

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>
    */
    friend
    bool
    operator<=(
        url_view_base const& u0,
        url_view_base const& u1) noexcept
    {
        return u0.compare(u1) <= 0;
    }

    /** Return the result of comparing two URLs

        The URLs are compared component by
        component as if they were first
        normalized.

        @par Example
        @code
        url_view u0( "http://www.b.com/index.htm" );
        url_view u1( "http://www.a.com/index.htm" );
        assert( u0 > u1 );
        @endcode

        @par Effects
        @code
        url a(u0);
        a.normalize();
        url b(u1);
        b.normalize();
        return std::make_tuple(
                   a.scheme(),
                   a.user(),
                   a.password(),
                   a.host(),
                   a.port(),
                   a.path(),
                   a.query(),
                   a.fragment()) >
               std::make_tuple(
                   b.scheme(),
                   b.user(),
                   b.password(),
                   b.host(),
                   b.port(),
                   b.path(),
                   b.query(),
                   b.fragment());
        @endcode

        @par Complexity
        Linear in `min( u0.size(), u1.size() )`

        @par Exception Safety
        Throws nothing

        @return `true` if `u0 > u1`

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>
    */
    friend
    bool
    operator>(
        url_view_base const& u0,
        url_view_base const& u1) noexcept
    {
        return u0.compare(u1) > 0;
    }

    /** Return the result of comparing two URLs

        The URLs are compared component by
        component as if they were first
        normalized.

        @par Example
        @code
        url_view u0( "http://www.a.com/index.htm" );
        url_view u1( "http://www.a.com/index.htm" );
        assert( u0 >= u1 );
        @endcode

        @par Effects
        @code
        url a(u0);
        a.normalize();
        url b(u1);
        b.normalize();
        return std::make_tuple(
                   a.scheme(),
                   a.user(),
                   a.password(),
                   a.host(),
                   a.port(),
                   a.path(),
                   a.query(),
                   a.fragment()) >=
               std::make_tuple(
                   b.scheme(),
                   b.user(),
                   b.password(),
                   b.host(),
                   b.port(),
                   b.path(),
                   b.query(),
                   b.fragment());
        @endcode

        @par Complexity
        Linear in `min( u0.size(), u1.size() )`

        @par Exception Safety
        Throws nothing

        @return `true` if `u0 >= u1`

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.2"
            >6.2.2 Syntax-Based Normalization (rfc3986)</a>
    */
    friend
    bool
    operator>=(
        url_view_base const& u0,
        url_view_base const& u1) noexcept
    {
        return u0.compare(u1) >= 0;
    }

    friend
    std::ostream&
    operator<<(
        std::ostream& os,
        url_view_base const& u)
    {
        return os << u.buffer();
    }

private:
    //--------------------------------------------
    //
    // implementation
    //
    //--------------------------------------------
    static
    int
    segments_compare(
        segments_encoded_view seg0,
        segments_encoded_view seg1) noexcept;
};

//------------------------------------------------

/** Format the url to the output stream

    This function serializes the url to
    the specified output stream. Any
    percent-escapes are emitted as-is;
    no decoding is performed.

    @par Example
    @code
    url_view u( "http://www.example.com/index.htm" );
    std::stringstream ss;
    ss << u;
    assert( ss.str() == "http://www.example.com/index.htm" );
    @endcode

    @par Effects
    @code
    return os << u.buffer();
    @endcode

    @par Complexity
    Linear in `u.buffer().size()`

    @par Exception Safety
    Basic guarantee.

    @return A reference to the output stream, for chaining

    @param os The output stream to write to.

    @param u The url to write.
*/
std::ostream&
operator<<(
    std::ostream& os,
    url_view_base const& u);

} // urls
} // boost

#endif
