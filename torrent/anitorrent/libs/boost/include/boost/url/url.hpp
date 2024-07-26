//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_URL_HPP
#define BOOST_URL_URL_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/url_base.hpp>
#include <boost/assert.hpp>
#include <utility>

namespace boost {
namespace urls {

/** A modifiable container for a URL.

    This container owns a url, represented
    by a null-terminated character buffer
    which is managed by performing dymamic
    memory allocations as needed.
    The contents may be inspected and modified,
    and the implementation maintains a useful
    invariant: changes to the url always
    leave it in a valid state.

    @par Exception Safety

    @li Functions marked `noexcept` provide the
    no-throw guarantee, otherwise:

    @li Functions which throw offer the strong
    exception safety guarantee.

    @par BNF
    @code
    URI-reference = URI / relative-ref

    URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

    relative-ref  = relative-part [ "?" query ] [ "#" fragment ]

    absolute-URI  = scheme ":" hier-part [ "?" query ]
    @endcode

    @par Specification
    @li <a href="https://tools.ietf.org/html/rfc3986"
        >Uniform Resource Identifier (URI): Generic Syntax (rfc3986)</a>

    @see
        @ref parse_absolute_uri,
        @ref parse_relative_ref,
        @ref parse_uri,
        @ref parse_uri_reference,
        @ref resolve.
*/
class BOOST_URL_DECL url
    : public url_base
{
    friend std::hash<url>;

    using url_view_base::digest;

public:
    //--------------------------------------------
    //
    // Special Members
    //
    //--------------------------------------------

    /** Destructor

        Any params, segments, iterators, or
        views which reference this object are
        invalidated. The underlying character
        buffer is destroyed, invalidating all
        references to it.
    */
    virtual ~url();

    /** Constructor

        Default constructed urls contain
        a zero-length string. This matches
        the grammar for a relative-ref with
        an empty path and no query or
        fragment.

        @par Example
        @code
        url u;
        @endcode

        @par Postconditions
        @code
        this->empty() == true
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
        @endcode

        @par Specification
        <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.2"
            >4.2. Relative Reference (rfc3986)</a>
    */
    url() noexcept;

    /** Constructor

        This function constructs a URL from
        the string `s`, which must contain a
        valid <em>URI</em> or <em>relative-ref</em>
        or else an exception is thrown.
        The new url retains ownership by
        allocating a copy of the passed string.

        @par Example
        @code
        url u( "https://www.example.com" );
        @endcode

        @par Effects
        @code
        return url( parse_uri_reference( s ).value() );
        @endcode

        @par Postconditions
        @code
        this->buffer().data() != s.data()
        @endcode

        @par Complexity
        Linear in `s.size()`.

        @par Exception Safety
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The input does not contain a valid url.

        @param s The string to parse.

        @par BNF
        @code
        URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

        relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.1"
            >4.1. URI Reference</a>
    */
    explicit
    url(core::string_view s);

    /** Constructor

        The contents of `u` are transferred
        to the newly constructed object,
        which includes the underlying
        character buffer.
        After construction, the moved-from
        object is as if default constructed.

        @par Postconditions
        @code
        u.empty() == true
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param u The url to move from.
    */
    url(url&& u) noexcept;

    /** Constructor

        The newly constructed object
        contains a copy of `u`.

        @par Postconditions
        @code
        this->buffer() == u.buffer() && this->buffer().data() != u.buffer().data()
        @endcode

        @par Complexity
        Linear in `u.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @throw std::length_error `u.size() > max_size()`.

        @param u The url to copy.
    */
    url(url_view_base const& u)
    {
        copy(u);
    }

    /** Constructor

        The newly constructed object
        contains a copy of `u`.

        @par Postconditions
        @code
        this->buffer() == u.buffer() && this->buffer().data() != u.buffer().data()
        @endcode

        @par Complexity
        Linear in `u.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @throw std::length_error `u.size() > max_size()`.

        @param u The url to copy.
    */
    url(url const& u)
        : url(static_cast<
            url_view_base const&>(u))
    {
    }

    /** Assignment

        The contents of `u` are transferred to
        `this`, including the underlying
        character buffer. The previous contents
        of `this` are destroyed.
        After assignment, the moved-from
        object is as if default constructed.

        @par Postconditions
        @code
        u.empty() == true
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param u The url to assign from.
    */
    url&
    operator=(url&& u) noexcept;

    /** Assignment

        The contents of `u` are copied and
        the previous contents of `this` are
        destroyed.
        Capacity is preserved, or increases.

        @par Postconditions
        @code
        this->buffer() == u.buffer() && this->buffer().data() != u.buffer().data()
        @endcode

        @par Complexity
        Linear in `u.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @throw std::length_error `u.size() > max_size()`.

        @param u The url to copy.
    */
    url&
    operator=(
        url_view_base const& u)
    {
        copy(u);
        return *this;
    }

    /** Assignment

        The contents of `u` are copied and
        the previous contents of `this` are
        destroyed.
        Capacity is preserved, or increases.

        @par Postconditions
        @code
        this->buffer() == u.buffer() && this->buffer().data() != u.buffer().data()
        @endcode

        @par Complexity
        Linear in `u.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param u The url to copy.
    */
    url&
    operator=(url const& u)
    {
        return (*this)=static_cast<
            url_view_base const&>(u);
    }

    //--------------------------------------------

    /** Swap the contents.

        Exchanges the contents of this url with another
        url. All views, iterators and references remain valid.

        If `this == &other`, this function call has no effect.

        @par Example
        @code
        url u1( "https://www.example.com" );
        url u2( "https://www.boost.org" );
        u1.swap(u2);
        assert(u1 == "https://www.boost.org" );
        assert(u2 == "https://www.example.com" );
        @endcode

        @par Complexity
        Constant

        @par Exception Safety
        Throws nothing.

        @param other The object to swap with

    */
    void
    swap(url& other) noexcept;

    /** Swap

        Exchanges the contents of `v0` with another `v1`.
        All views, iterators and references remain
        valid.

        If `&v0 == &v1`, this function call has no effect.

        @par Example
        @code
        url u1( "https://www.example.com" );
        url u2( "https://www.boost.org" );
        std::swap(u1, u2);
        assert(u1 == "https://www.boost.org" );
        assert(u2 == "https://www.example.com" );
        @endcode

        @par Effects
        @code
        v0.swap( v1 );
        @endcode

        @par Complexity
        Constant

        @par Exception Safety
        Throws nothing

        @param v0, v1 The objects to swap

        @see
            @ref url::swap
    */
    friend
    void
    swap(url& v0, url& v1) noexcept
    {
        v0.swap(v1);
    }

    //--------------------------------------------
    //
    // fluent api
    //

    /// @copydoc url_base::set_scheme
    url& set_scheme(core::string_view s) { url_base::set_scheme(s); return *this; }
    /// @copydoc url_base::set_scheme_id
    url& set_scheme_id(urls::scheme id) { url_base::set_scheme_id(id); return *this; }
    /// @copydoc url_base::remove_scheme
    url& remove_scheme() { url_base::remove_scheme(); return *this; }

    /// @copydoc url_base::set_encoded_authority
    url& set_encoded_authority(pct_string_view s) { url_base::set_encoded_authority(s); return *this; }
    /// @copydoc url_base::remove_authority
    url& remove_authority() { url_base::remove_authority(); return *this; }

    /// @copydoc url_base::set_userinfo
    url& set_userinfo(core::string_view s) { url_base::set_userinfo(s); return *this; }
    /// @copydoc url_base::set_encoded_userinfo
    url& set_encoded_userinfo(pct_string_view s) { url_base::set_encoded_userinfo(s); return *this; }
    /// @copydoc url_base::remove_userinfo
    url& remove_userinfo() noexcept { url_base::remove_userinfo(); return *this; }
    /// @copydoc url_base::set_user
    url& set_user(core::string_view s) { url_base::set_user(s); return *this; }
    /// @copydoc url_base::set_encoded_user
    url& set_encoded_user(pct_string_view s) { url_base::set_encoded_user(s); return *this; }
    /// @copydoc url_base::set_password
    url& set_password(core::string_view s) { url_base::set_password(s); return *this; }
    /// @copydoc url_base::set_encoded_password
    url& set_encoded_password(pct_string_view s) { url_base::set_encoded_password(s); return *this; }
    /// @copydoc url_base::remove_password
    url& remove_password() noexcept { url_base::remove_password(); return *this; }

    /// @copydoc url_base::set_host
    url& set_host(core::string_view s) { url_base::set_host(s); return *this; }
    /// @copydoc url_base::set_encoded_host
    url& set_encoded_host(pct_string_view s) { url_base::set_encoded_host(s); return *this; }
    /// @copydoc url_base::set_host_address
    url& set_host_address(core::string_view s) { url_base::set_host_address(s); return *this; }
    /// @copydoc url_base::set_encoded_host_address
    url& set_encoded_host_address(pct_string_view s) { url_base::set_encoded_host_address(s); return *this; }
    /// @copydoc url_base::set_host_ipv4
    url& set_host_ipv4(ipv4_address const& addr) { url_base::set_host_ipv4(addr); return *this; }
    /// @copydoc url_base::set_host_ipv6
    url& set_host_ipv6(ipv6_address const& addr) { url_base::set_host_ipv6(addr); return *this; }
    /// @copydoc url_base::set_host_ipvfuture
    url& set_host_ipvfuture(core::string_view s) { url_base::set_host_ipvfuture(s); return *this; }
    /// @copydoc url_base::set_host_name
    url& set_host_name(core::string_view s) { url_base::set_host_name(s); return *this; }
    /// @copydoc url_base::set_encoded_host_name
    url& set_encoded_host_name(pct_string_view s) { url_base::set_encoded_host_name(s); return *this; }
    /// @copydoc url_base::set_port_number
    url& set_port_number(std::uint16_t n) { url_base::set_port_number(n); return *this; }
    /// @copydoc url_base::set_port
    url& set_port(core::string_view s) { url_base::set_port(s); return *this; }
    /// @copydoc url_base::remove_port
    url& remove_port() noexcept { url_base::remove_port(); return *this; }

    /// @copydoc url_base::set_path_absolute
    //bool set_path_absolute(bool absolute);
    /// @copydoc url_base::set_path
    url& set_path(core::string_view s) { url_base::set_path(s); return *this; }
    /// @copydoc url_base::set_encoded_path
    url& set_encoded_path(pct_string_view s) { url_base::set_encoded_path(s); return *this; }

    /// @copydoc url_base::set_query
    url& set_query(core::string_view s) { url_base::set_query(s); return *this; }
    /// @copydoc url_base::set_encoded_query
    url& set_encoded_query(pct_string_view s) { url_base::set_encoded_query(s); return *this; }
    /// @copydoc url_base::set_params
    url& set_params(std::initializer_list<param_view> ps) { url_base::set_params(ps); return *this; }
    /// @copydoc url_base::set_encoded_params
    url& set_encoded_params(std::initializer_list< param_pct_view > ps) { url_base::set_encoded_params(ps); return *this; }
    /// @copydoc url_base::remove_query
    url& remove_query() noexcept { url_base::remove_query(); return *this; }

    /// @copydoc url_base::remove_fragment
    url& remove_fragment() noexcept { url_base::remove_fragment(); return *this; }
    /// @copydoc url_base::set_fragment
    url& set_fragment(core::string_view s) { url_base::set_fragment(s); return *this; }
    /// @copydoc url_base::set_encoded_fragment
    url& set_encoded_fragment(pct_string_view s) { url_base::set_encoded_fragment(s); return *this; }

    /// @copydoc url_base::remove_origin
    url& remove_origin() { url_base::remove_origin(); return *this; }

    /// @copydoc url_base::normalize
    url& normalize() { url_base::normalize(); return *this; }
    /// @copydoc url_base::normalize_scheme
    url& normalize_scheme() { url_base::normalize_scheme(); return *this; }
    /// @copydoc url_base::normalize_authority
    url& normalize_authority() { url_base::normalize_authority(); return *this; }
    /// @copydoc url_base::normalize_path
    url& normalize_path() { url_base::normalize_path(); return *this; }
    /// @copydoc url_base::normalize_query
    url& normalize_query() { url_base::normalize_query(); return *this; }
    /// @copydoc url_base::normalize_fragment
    url& normalize_fragment() { url_base::normalize_fragment(); return *this; }

    //--------------------------------------------

private:
    char* allocate(std::size_t);
    void deallocate(char* s);

    void clear_impl() noexcept override;
    void reserve_impl(std::size_t, op_t&) override;
    void cleanup(op_t&) override;
};

} // urls
} // boost

//------------------------------------------------

// std::hash specialization
#ifndef BOOST_URL_DOCS
namespace std {
template<>
struct hash< ::boost::urls::url >
{
    hash() = default;
    hash(hash const&) = default;
    hash& operator=(hash const&) = default;

    explicit
    hash(std::size_t salt) noexcept
        : salt_(salt)
    {
    }

    std::size_t
    operator()(::boost::urls::url const& u) const noexcept
    {
        return u.digest(salt_);
    }

private:
    std::size_t salt_ = 0;
};
} // std
#endif

#endif
