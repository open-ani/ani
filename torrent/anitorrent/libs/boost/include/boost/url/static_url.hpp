//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_STATIC_URL_HPP
#define BOOST_URL_STATIC_URL_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/url_base.hpp>
#include <boost/align/align_up.hpp>
#include <boost/static_assert.hpp>
#include <cstddef>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
template<std::size_t Capacity>
class static_url;
#endif

// VFALCO This class is for reducing
// the number of template instantiations,
// and keep definitions in the library

/** Common implementation for all static URLs

    This base class is used by the library
    to provide common functionality for
    static URLs. Users should not use this
    class directly. Instead, construct an
    instance of one of the containers
    or call a parsing function.

    @par Containers
        @li @ref url
        @li @ref url_view
        @li @ref static_url

    @par Parsing Functions
        @li @ref parse_absolute_uri
        @li @ref parse_origin_form
        @li @ref parse_relative_ref
        @li @ref parse_uri
        @li @ref parse_uri_reference
*/
class BOOST_URL_DECL
    static_url_base
    : public url_base
{
    template<std::size_t>
    friend class static_url;

    ~static_url_base() = default;
    static_url_base(
        char* buf, std::size_t cap) noexcept;
    static_url_base(
        char* buf, std::size_t cap, core::string_view s);
    void clear_impl() noexcept override;
    void reserve_impl(std::size_t, op_t&) override;
    void cleanup(op_t&) override;

    void
    copy(url_view_base const& u)
    {
        this->url_base::copy(u);
    }

};

//------------------------------------------------

/** A modifiable container for a URL.

    This container owns a url, represented
    by an inline, null-terminated character
    buffer with fixed capacity.
    The contents may be inspected and modified,
    and the implementation maintains a useful
    invariant: changes to the url always
    leave it in a valid state.

    @par Example
    @code
    static_url< 1024 > u( "https://www.example.com" );
    @endcode

    @par Invariants
    @code
    this->capacity() == Capacity
    @endcode

    @tparam Capacity The maximum capacity
    in characters, not including the
    null terminator.

    @see
        @ref url,
        @ref url_view.
*/
template<std::size_t Capacity>
class static_url
    : public static_url_base
{
    char buf_[Capacity + 1];

    friend std::hash<static_url>;
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
    ~static_url() = default;

    /** Constructor

        Default constructed urls contain
        a zero-length string. This matches
        the grammar for a relative-ref with
        an empty path and no query or
        fragment.

        @par Example
        @code
        static_url< 1024 > u;
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
    static_url() noexcept
        : static_url_base(
            buf_, sizeof(buf_))
    {
    }

    /** Constructor

        This function constructs a url from
        the string `s`, which must contain a
        valid <em>URI</em> or <em>relative-ref</em>
        or else an exception is thrown.
        The new url retains ownership by
        making a copy of the passed string.

        @par Example
        @code
        static_url< 1024 > u( "https://www.example.com" );
        @endcode

        @par Effects
        @code
        return static_url( parse_uri_reference( s ).value() );
        @endcode

        @par Postconditions
        @code
        this->buffer().data() != s.data()
        @endcode

        @par Complexity
        Linear in `s.size()`.

        @par Exception Safety
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
    static_url(
        core::string_view s)
        : static_url_base(
            buf_, sizeof(buf_), s)
    {
    }

    /** Constructor

        The newly constructed object contains
        a copy of `u`.

        @par Postconditions
        @code
        this->buffer() == u.buffer() && this->buffer.data() != u.buffer().data()
        @endcode

        @par Complexity
        Linear in `u.size()`.

        @par Exception Safety
        Exception thrown if maximum size exceeded.

        @param u The url to copy.
    */
    static_url(
        static_url const& u) noexcept
        : static_url()
    {
        copy(u);
    }

    /** Constructor

        The newly constructed object contains
        a copy of `u`.

        @par Postconditions
        @code
        this->buffer() == u.buffer() && this->buffer.data() != u.buffer().data()
        @endcode

        @par Complexity
        Linear in `u.size()`.

        @par Exception Safety
        Exception thrown if capacity exceeded.

        @throw system_error
        Capacity would be exceeded.

        @param u The url to copy.
    */
    static_url(
        url_view_base const& u)
        : static_url()
    {
        copy(u);
    }

    /** Assignment

        The contents of `u` are copied and
        the previous contents of `this` are
        discarded.
        Capacity remains unchanged.

        @par Postconditions
        @code
        this->buffer() == u.buffer() && this->buffer().data() != u.buffer().data()
        @endcode

        @par Complexity
        Linear in `u.size()`.

        @par Exception Safety
        Throws nothing.

        @param u The url to copy.
    */
    static_url&
    operator=(
        static_url const& u) noexcept
    {
        if (this != &u)
            copy(u);
        return *this;
    }

    /** Assignment

        The contents of `u` are copied and
        the previous contents of `this` are
        discarded.

        @par Postconditions
        @code
        this->buffer() == u.buffer() && this->buffer().data() != u.buffer().data()
        @endcode

        @par Complexity
        Linear in `u.size()`.

        @par Exception Safety
        Strong guarantee.
        Exception thrown if capacity exceeded.

        @throw system_error
        Capacity would be exceeded.

        @param u The url to copy.
    */
    static_url&
    operator=(
        url_view_base const& u)
    {
        copy(u);
        return *this;
    }


    //--------------------------------------------
    //
    // fluent api
    //

    /// @copydoc url_base::set_scheme
    static_url& set_scheme(core::string_view s) { url_base::set_scheme(s); return *this; }
    /// @copydoc url_base::set_scheme_id
    static_url& set_scheme_id(urls::scheme id) { url_base::set_scheme_id(id); return *this; }
    /// @copydoc url_base::remove_scheme
    static_url& remove_scheme() { url_base::remove_scheme(); return *this; }

    /// @copydoc url_base::set_encoded_authority
    static_url& set_encoded_authority(pct_string_view s) { url_base::set_encoded_authority(s); return *this; }
    /// @copydoc url_base::remove_authority
    static_url& remove_authority() { url_base::remove_authority(); return *this; }

    /// @copydoc url_base::set_userinfo
    static_url& set_userinfo(core::string_view s) { url_base::set_userinfo(s); return *this; }
    /// @copydoc url_base::set_encoded_userinfo
    static_url& set_encoded_userinfo(pct_string_view s) { url_base::set_encoded_userinfo(s); return *this; }
    /// @copydoc url_base::remove_userinfo
    static_url& remove_userinfo() noexcept { url_base::remove_userinfo(); return *this; }
    /// @copydoc url_base::set_user
    static_url& set_user(core::string_view s) { url_base::set_user(s); return *this; }
    /// @copydoc url_base::set_encoded_user
    static_url& set_encoded_user(pct_string_view s) { url_base::set_encoded_user(s); return *this; }
    /// @copydoc url_base::set_password
    static_url& set_password(core::string_view s) { url_base::set_password(s); return *this; }
    /// @copydoc url_base::set_encoded_password
    static_url& set_encoded_password(pct_string_view s) { url_base::set_encoded_password(s); return *this; }
    /// @copydoc url_base::remove_password
    static_url& remove_password() noexcept { url_base::remove_password(); return *this; }

    /// @copydoc url_base::set_host
    static_url& set_host(core::string_view s) { url_base::set_host(s); return *this; }
    /// @copydoc url_base::set_encoded_host
    static_url& set_encoded_host(pct_string_view s) { url_base::set_encoded_host(s); return *this; }
    /// @copydoc url_base::set_host_address
    static_url& set_host_address(core::string_view s) { url_base::set_host_address(s); return *this; }
    /// @copydoc url_base::set_encoded_host_address
    static_url& set_encoded_host_address(pct_string_view s) { url_base::set_encoded_host_address(s); return *this; }
    /// @copydoc url_base::set_host_ipv4
    static_url& set_host_ipv4(ipv4_address const& addr) { url_base::set_host_ipv4(addr); return *this; }
    /// @copydoc url_base::set_host_ipv6
    static_url& set_host_ipv6(ipv6_address const& addr) { url_base::set_host_ipv6(addr); return *this; }
    /// @copydoc url_base::set_host_ipvfuture
    static_url& set_host_ipvfuture(core::string_view s) { url_base::set_host_ipvfuture(s); return *this; }
    /// @copydoc url_base::set_host_name
    static_url& set_host_name(core::string_view s) { url_base::set_host_name(s); return *this; }
    /// @copydoc url_base::set_encoded_host_name
    static_url& set_encoded_host_name(pct_string_view s) { url_base::set_encoded_host_name(s); return *this; }
    /// @copydoc url_base::set_port_number
    static_url& set_port_number(std::uint16_t n) { url_base::set_port_number(n); return *this; }
    /// @copydoc url_base::set_port
    static_url& set_port(core::string_view s) { url_base::set_port(s); return *this; }
    /// @copydoc url_base::remove_port
    static_url& remove_port() noexcept { url_base::remove_port(); return *this; }

    /// @copydoc url_base::set_path_absolute
    //bool set_path_absolute(bool absolute);
    /// @copydoc url_base::set_path
    static_url& set_path(core::string_view s) { url_base::set_path(s); return *this; }
    /// @copydoc url_base::set_encoded_path
    static_url& set_encoded_path(pct_string_view s) { url_base::set_encoded_path(s); return *this; }

    /// @copydoc url_base::set_query
    static_url& set_query(core::string_view s) { url_base::set_query(s); return *this; }
    /// @copydoc url_base::set_encoded_query
    static_url& set_encoded_query(pct_string_view s) { url_base::set_encoded_query(s); return *this; }
    /// @copydoc url_base::remove_query
    static_url& remove_query() noexcept { url_base::remove_query(); return *this; }

    /// @copydoc url_base::remove_fragment
    static_url& remove_fragment() noexcept { url_base::remove_fragment(); return *this; }
    /// @copydoc url_base::set_fragment
    static_url& set_fragment(core::string_view s) { url_base::set_fragment(s); return *this; }
    /// @copydoc url_base::set_encoded_fragment
    static_url& set_encoded_fragment(pct_string_view s) { url_base::set_encoded_fragment(s); return *this; }

    /// @copydoc url_base::remove_origin
    static_url& remove_origin() { url_base::remove_origin(); return *this; }

    /// @copydoc url_base::normalize
    static_url& normalize() { url_base::normalize(); return *this; }
    /// @copydoc url_base::normalize_scheme
    static_url& normalize_scheme() { url_base::normalize_scheme(); return *this; }
    /// @copydoc url_base::normalize_authority
    static_url& normalize_authority() { url_base::normalize_authority(); return *this; }
    /// @copydoc url_base::normalize_path
    static_url& normalize_path() { url_base::normalize_path(); return *this; }
    /// @copydoc url_base::normalize_query
    static_url& normalize_query() { url_base::normalize_query(); return *this; }
    /// @copydoc url_base::normalize_fragment
    static_url& normalize_fragment() { url_base::normalize_fragment(); return *this; }

    //--------------------------------------------
};

} // urls
} // boost

//------------------------------------------------

// std::hash specialization
#ifndef BOOST_URL_DOCS
namespace std {
template<std::size_t N>
struct hash< ::boost::urls::static_url<N> >
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
    operator()(::boost::urls::static_url<N> const& u) const noexcept
    {
        return u.digest(salt_);
    }

private:
    std::size_t salt_ = 0;
};
} // std
#endif

#endif
