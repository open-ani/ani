//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_URL_VIEW_HPP
#define BOOST_URL_URL_VIEW_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/url_view_base.hpp>
#include <utility>

namespace boost {
namespace urls {

/** A non-owning reference to a valid URL 

    Objects of this type represent valid URL
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
    as a <em>URI-reference</em> and throws an
    exception on error. Upon success, the
    constructed object points to the passed
    character buffer; ownership is not
    transferred.
    @code
    url_view u( "https://www.example.com/index.htm?text=none#a1" );
    @endcode

    @par Example 2
    Parsing functions like @ref parse_uri_reference
    return a @ref result containing either a valid
    @ref url_view upon succcess, otherwise they
    contain an error. The error can be converted to
    an exception by the caller if desired:
    @code
    system::result< url_view > rv = parse_uri_reference( "https://www.example.com/index.htm?text=none#a1" );
    @endcode

    @par BNF
    @code
    URI-reference = URI / relative-ref

    URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

    relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
    @endcode

    @par Specification
    @li <a href="https://tools.ietf.org/html/rfc3986"
        >Uniform Resource Identifier (URI): Generic Syntax (rfc3986)</a>

    @see
        @ref parse_absolute_uri,
        @ref parse_origin_form,
        @ref parse_relative_ref,
        @ref parse_uri,
        @ref parse_uri_reference.
*/
class BOOST_URL_DECL url_view
    : public url_view_base
{
    friend std::hash<url_view>;
    friend class url_view_base;
    friend class params_base;
    friend class params_encoded_base;

#ifndef BOOST_URL_DOCS
    // VFALCO docca emits this erroneously
    friend struct detail::url_impl;
#endif

    using url_view_base::digest;

    explicit
    url_view(
        detail::url_impl const& impl) noexcept
        : url_view_base(impl)
    {
    }

public:
    //--------------------------------------------
    //
    // Special Members
    //
    //--------------------------------------------

    /** Destructor

        Any params, segments, iterators, or
        other views which reference the same
        underlying character buffer remain
        valid.
    */
    ~url_view() = default;

    /** Constructor

        Default constructed views refer to
        a string with zero length, which
        always remains valid. This matches
        the grammar for a relative-ref with
        an empty path and no query or
        fragment.

        @par Example
        @code
        url_view u;
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
    url_view() noexcept;

    /** Constructor

        This function constructs a URL from
        the string `s`, which must contain a
        valid <em>URI</em> or <em>relative-ref</em>
        or else an exception is thrown. Upon
        successful construction, the view
        refers to the characters in the
        buffer pointed to by `s`.
        Ownership is not transferred; The caller
        is responsible for ensuring that the
        lifetime of the buffer extends until
        it is no longer referenced.

        @par Example
        @code
        url_view u( "http://www.example.com/index.htm" );
        @endcode

        @par Effects
        @code
        return parse_uri_reference( s ).value();
        @endcode

        @par Complexity
        Linear in `s.size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        The input failed to parse correctly.

        @param s The string to parse.

        @par BNF
        @code
        URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

        relative-ref  = relative-part [ "?" query ] [ "#" fragment ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.1"
            >4.1. URI Reference</a>

        @see
            @ref parse_uri_reference.
    */
    url_view(core::string_view s);

    /// @copydoc url_view(core::string_view)
    template<
        class String
#ifndef BOOST_URL_DOCS
        , class = typename std::enable_if<
            std::is_convertible<
                String,
                core::string_view
                    >::value &&
            !std::is_convertible<
                String*,
                url_view_base*
                    >::value
            >::type
#endif
    >
    url_view(
        String const& s)
        : url_view(
            detail::to_sv(s))
    {
    }

    /** Constructor

        After construction, both views
        reference the same underlying character
        buffer. Ownership is not transferred.

        @par Postconditions
        @code
        this->buffer().data() == other.buffer().data()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    url_view(
        url_view const& other) noexcept
        : url_view(static_cast<
            url_view_base const&>(other))
    {
    }

    /** Constructor

        After construction, both views
        reference the same underlying character
        buffer. Ownership is not transferred.

        @par Postconditions
        @code
        this->buffer().data() == other.buffer().data()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    url_view(
        url_view_base const& other) noexcept;

    /** Assignment

        After assignment, both views
        reference the same underlying character
        buffer. Ownership is not transferred.

        @par Postconditions
        @code
        this->buffer().data() == other.buffer().data()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    url_view&
    operator=(
        url_view const& other) noexcept
    {
        if (this != &other)
            *this = static_cast<
                url_view_base const&>(other);
        return *this;
    }

    /** Assignment

        After assignment, both views
        reference the same underlying character
        buffer. Ownership is not transferred.

        @par Postconditions
        @code
        this->buffer().data() == other.buffer().data()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    url_view& operator=(
        url_view_base const& other) noexcept;

    //--------------------------------------------
    //
    // Observers
    //
    //--------------------------------------------

    /** Return the maximum number of characters possible

        This represents the largest number of
        characters that are possible in a url,
        not including any null terminator.

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
};

} // urls
} // boost

//------------------------------------------------

// std::hash specialization
#ifndef BOOST_URL_DOCS
namespace std {
template<>
struct hash< ::boost::urls::url_view >
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
    operator()(::boost::urls::url_view const& u) const noexcept
    {
        return u.digest(salt_);
    }

private:
    std::size_t salt_ = 0;
};
} // std
#endif

#endif
