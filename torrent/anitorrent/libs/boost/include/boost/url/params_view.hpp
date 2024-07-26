//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARAMS_VIEW_HPP
#define BOOST_URL_PARAMS_VIEW_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/params_base.hpp>

namespace boost {
namespace urls {

/** A view representing query parameters in a URL

    Objects of this type are used to interpret
    the query parameters as a bidirectional view
    of key/value pairs.

    The view does not retain ownership of the
    elements and instead references the original
    character buffer. The caller is responsible
    for ensuring that the lifetime of the buffer
    extends until it is no longer referenced.

    @par Example
    @code
    url_view u( "?first=John&last=Doe" );

    params_view p = u.params();
    @endcode

    Percent escapes in strings returned when
    dereferencing iterators are automatically
    decoded.

    @par Iterator Invalidation
    Changes to the underlying character buffer
    can invalidate iterators which reference it.
*/
class params_view
    : public params_base
{
    friend class url_view_base;
    friend class params_encoded_view;
    friend class params_ref;

    params_view(
        detail::query_ref const& ref,
        encoding_opts opt) noexcept;

public:
    /** Constructor

        Default-constructed params have
        zero elements.

        @par Example
        @code
        params_view qp;
        @endcode

        @par Effects
        @code
        return params_view( "" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    params_view() = default;

    /** Constructor

        After construction both views reference
        the same character buffer.

        Ownership is not transferred; the caller
        is responsible for ensuring the lifetime
        of the buffer extends until it is no
        longer referenced.

        @par Postconditions
        @code
        this->buffer().data() == other.buffer().data()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing
    */
    params_view(
        params_view const& other) = default;

    /** Constructor

        After construction both views will
        reference the same character buffer
        but this instance will use the specified
        @ref encoding_opts when the values
        are decoded.

        Ownership is not transferred; the caller
        is responsible for ensuring the lifetime
        of the buffer extends until it is no
        longer referenced.

        @par Postconditions
        @code
        this->buffer().data() == other.buffer().data()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing
    */
    params_view(
        params_view const& other,
        encoding_opts opt) noexcept;

    /** Constructor

        This function constructs params from
        a valid query parameter string, which
        can contain percent escapes. Unlike
        the parameters in URLs, the string
        passed here should not start with "?".
        Upon construction, the view references
        the character buffer pointed to by `s`.
        The caller is responsible for ensuring
        that the lifetime of the buffer extends
        until it is no longer referenced.

        @par Example
        @code
        params_view qp( "first=John&last=Doe" );
        @endcode

        @par Effects
        @code
        return parse_query( s ).value();
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
        `s` contains an invalid query parameter
        string.

        @param s The string to parse.

        @par BNF
        @code
        query-params    = [ query-param ] *( "&" query-param )

        query-param     = key [ "=" value ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4.  Query</a>
    */
    BOOST_URL_DECL
    params_view(
        core::string_view s);

    /** Constructor

        This function constructs params from
        a valid query parameter string, which
        can contain percent escapes.

        This instance will use the specified
        @ref encoding_opts when the values
        are decoded.

        Unlike the parameters in URLs, the string
        passed here should not start with "?".
        Upon construction, the view will
        reference the character buffer pointed
        to by `s`. The caller is responsible
        for ensuring that the lifetime of the
        buffer extends until it is no longer
        referenced.

        @par Example
        @code
        encoding_opts opt;
        opt.space_as_plus = true;
        params_view qp( "name=John+Doe", opt );
        @endcode

        @par Effects
        @code
        return params_view(parse_query( s ).value(), opt);
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
        `s` contains an invalid query parameter
        string.

        @param s The string to parse.

        @param opt The options for decoding. If
        this parameter is omitted, `space_as_plus`
        is used.

        @par BNF
        @code
        query-params    = [ query-param ] *( "&" query-param )

        query-param     = key [ "=" value ]
        @endcode

        @par Specification
        @li <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-3.4"
            >3.4.  Query</a>
    */
    BOOST_URL_DECL
    params_view(
        core::string_view s,
        encoding_opts opt);

    /** Assignment

        After assignment, both views reference
        the same underlying character buffer.

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
    params_view&
    operator=(
        params_view const&) = default;
};

} // urls
} // boost

#endif
