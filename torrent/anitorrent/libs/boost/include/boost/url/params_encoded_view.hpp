//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARAMS_ENCODED_VIEW_HPP
#define BOOST_URL_PARAMS_ENCODED_VIEW_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/url/params_encoded_base.hpp>
#include <boost/url/params_view.hpp>
#include <boost/core/detail/string_view.hpp>
#include <iosfwd>
#include <utility>

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

    params_encoded_view p = u.encoded_params();
    @endcode

    Strings produced when elements are returned
    have type @ref param_pct_view and represent
    encoded strings. Strings passed to member
    functions may contain percent escapes, and
    throw exceptions on invalid inputs.

    @par Iterator Invalidation
    Changes to the underlying character buffer
    can invalidate iterators which reference it.
*/
class BOOST_URL_DECL params_encoded_view
    : public params_encoded_base
{
    friend class url_view_base;
    friend class params_view;
    friend class params_encoded_ref;
    friend struct query_rule_t;

    params_encoded_view(
        detail::query_ref const& ref) noexcept;

public:
    /** Constructor

        Default-constructed params have
        zero elements.

        @par Example
        @code
        params_encoded_view qp;
        @endcode

        @par Effects
        @code
        return params_encoded_view( "" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    params_encoded_view() = default;

    /** Constructor

        After construction both views
        reference the same character buffer.

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
    params_encoded_view(
        params_encoded_view const& other) = default;

    /** Constructor

        This function constructs params from
        a valid query parameter string, which
        can contain percent escapes. Unlike
        the parameters in URLs, the string
        passed here should not start with "?".
        Upon construction, the view
        references the character buffer pointed
        to by `s`. The caller is responsible
        for ensuring that the lifetime of the
        buffer extends until it is no longer
        referenced.

        @par Example
        @code
        params_encoded_view qp( "first=John&last=Doe" );
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
    params_encoded_view(
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
    params_encoded_view&
    operator=(
        params_encoded_view const&) = default;

    /** Conversion

        This conversion returns a new view which
        references the same underlying character
        buffer, and whose iterators and members
        return ordinary strings with decoding
        applied to any percent escapes.

        Ownership is not transferred; the caller
        is responsible for ensuring the lifetime
        of the buffer extends until it is no
        longer referenced.

        @par Example
        @code
        params_view qp = parse_path( "/path/to/file.txt" ).value();
        @endcode

        @par Postconditions
        @code
        params_view( *this ).buffer().data() == this->buffer().data()
        @endcode

        @par Complexity
        Constant

        @par Exception Safety
        Throws nothing
    */
    operator
    params_view() const noexcept;

    //--------------------------------------------

    friend
    BOOST_URL_DECL
    system::result<params_encoded_view>
    parse_query(core::string_view s) noexcept;
};

} // urls
} // boost

#endif
