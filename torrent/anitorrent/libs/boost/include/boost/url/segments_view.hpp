//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_SEGMENTS_VIEW_HPP
#define BOOST_URL_SEGMENTS_VIEW_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/segments_base.hpp>
#include <boost/core/detail/string_view.hpp>

namespace boost {
namespace urls {

/** A view representing path segments in a URL

    Objects of this type are used to interpret
    the path as a bidirectional view of segment
    strings.

    The view does not retain ownership of the
    elements and instead references the original
    character buffer. The caller is responsible
    for ensuring that the lifetime of the buffer
    extends until it is no longer referenced.

    @par Example
    @code
    url_view u( "/path/to/file.txt" );

    segments_view ps = u.segments();

    assert( ps.buffer().data() == u.buffer().data() );
    @endcode

    Percent escapes in strings returned when
    dereferencing iterators are automatically
    decoded.

    @par Iterator Invalidation
    Changes to the underlying character buffer
    can invalidate iterators which reference it.

    @see
        @ref segments_encoded_view,
        @ref segments_encoded_ref,
        @ref segments_ref.
*/
class segments_view
    : public segments_base
{
    friend class url_view_base;
    friend class segments_encoded_view;
    friend class segments_ref;

    segments_view(
        detail::path_ref const& ref) noexcept;

public:
    /** Constructor

        Default-constructed segments have
        zero elements.

        @par Example
        @code
        segments_view ps;
        @endcode

        @par Effects
        @code
        return segments_view( "" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    segments_view() = default;

    /** Constructor

        After construction, viewss
        reference the same underlying character
        buffer.

        Ownership is not transferred; the caller
        is responsible for ensuring the lifetime
        of the buffer extends until it is no
        longer referenced.

        @par Postconditions
        @code
        this->buffer().data() == other.buffer().data()
        @endcode

        @par Complexity
        Constant

        @par Exception Safety
        Throws nothing
    */
    segments_view(
        segments_view const& other) = default;

    /** Constructor

        This function constructs segments from
        a valid path string, which can contain
        percent escapes.
        Upon construction, the view references
        the character buffer pointed to by `s`.
        caller is responsible for ensuring
        that the lifetime of the buffer
        extends until the view is destroyed.

        @par Example
        @code
        segments_view ps( "/path/to/file.txt" );
        @endcode

        @par Effects
        @code
        return parse_path( s ).value();
        @endcode

        @par Postconditions
        @code
        this->buffer().data() == s.data()
        @endcode

        @par Complexity
        Linear in `s`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `s` contains an invalid path.

        @param s The string to parse.

        @par BNF
        @code
        path = [ "/" ] [ segment *( "/" segment ) ]

        segment = *pchar
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.3"
            >3.3.  Path</a>
    */
    BOOST_URL_DECL
    segments_view(
        core::string_view s);

    /** Assignment

        After assignment, both views
        reference the same underlying character
        buffer.

        Ownership is not transferred; the caller
        is responsible for ensuring the lifetime
        of the buffer extends until it is no
        longer referenced.

        @par Postconditions
        @code
        this->buffer().data() == other.buffer().data()
        @endcode

        @par Complexity
        Constant

        @par Exception Safety
        Throws nothing
    */
    segments_view&
    operator=(segments_view const& other) = default;
};

} // urls
} // boost

#endif
