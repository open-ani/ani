//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_SEGMENTS_BASE_HPP
#define BOOST_URL_SEGMENTS_BASE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/ignore_case.hpp>
#include <boost/url/detail/url_impl.hpp>
#include <iosfwd>
#include <string>

namespace boost {
namespace urls {

/** Common functionality for containers

    This base class is used by the library
    to provide common member functions for
    containers. This cannot be instantiated
    directly; Instead, use one of the
    containers or functions:

    @par Containers
    @li @ref segments_ref
    @li @ref segments_view
    @li @ref segments_encoded_ref
    @li @ref segments_encoded_view
*/
class segments_base
{
    detail::path_ref ref_;

    friend class url_view_base;
    friend class segments_ref;
    friend class segments_view;

    segments_base(
        detail::path_ref const& ref) noexcept;
    segments_base() = default;
    segments_base(
        segments_base const&) = default;
    segments_base& operator=(
        segments_base const&) = default;

public:
    /** A Bidirectional iterator to a path segment

        Objects of this type allow iteration
        through the segments in the path.
        Any percent-escapes in returned strings
        are decoded first.
        The values returned are read-only;
        changes to segments must be made
        through the container instead, if the
        container supports modification.

        <br>

        The strings produced when iterators are
        dereferenced belong to the iterator and
        become invalidated when that particular
        iterator is incremented, decremented,
        or destroyed.
    */
#ifdef BOOST_URL_DOCS
    using iterator = __see_below__;
#else
    class iterator;
#endif

    /// @copydoc iterator
    using const_iterator = iterator;

    /** The value type

        Values of this type represent a segment
        where unique ownership is retained by
        making a copy.

        @par Example
        @code
        segments_base::value_type ps( url_view( "/path/to/file.txt" ).segments().back() );
        @endcode
    */
    using value_type = std::string;

    /** The reference type

        This is the type of value returned when
        iterators of the view are dereferenced.
    */
    using reference = std::string;

    /// @copydoc reference
    using const_reference = std::string;

    /** An unsigned integer type used to represent size.
    */
    using size_type = std::size_t;

    /** A signed integer type used to represent differences.
    */
    using difference_type = std::ptrdiff_t;

    //--------------------------------------------
    //
    // Observers
    //
    //--------------------------------------------

    /** Return the maximum number of characters possible

        This represents the largest number of
        characters that are possible in a path,
        not including any null terminator.

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

    /** Return the referenced character buffer.

        This function returns the character
        buffer referenced by the view.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "/path/to/file.txt" ).segments().buffer() == "/path/to/file.txt" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    BOOST_URL_DECL
    pct_string_view
    buffer() const noexcept;

    /** Returns true if this references an absolute path.

        Absolute paths always start with a
        forward slash ('/').

        @par Example
        @code
        assert( url_view( "/path/to/file.txt" ).segments().is_absolute() == true );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    BOOST_URL_DECL
    bool
    is_absolute() const noexcept;

    /** Return true if there are no segments

        @par Example
        @code
        assert( ! url_view( "/index.htm" ).segments().empty() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    BOOST_URL_DECL
    bool
    empty() const noexcept;

    /** Return the number of segments
    
        @par Example
        @code
        assert( url_view( "/path/to/file.txt" ).segments().size() == 3 );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    BOOST_URL_DECL
    std::size_t
    size() const noexcept;

    /** Return the first segment

        This function returns a string with the
        first segment of the path without any
        leading or trailing '/' separators.
        Any percent-escapes in the string are
        decoded first.

        @par Preconditions
        @code
        this->empty() == false
        @endcode

        @par Effects
        @code
        return *begin();
        @endcode

        @par Example
        @code
        assert( url_view( "/path/to/file.txt" ).segments().front() == "path" );
        @endcode

        @par Complexity
        Linear in `this->front().size()`.

        @par Exception Safety
        Calls to allocate may throw.
    */
    std::string
    front() const noexcept;

    /** Return the last segment

        @par Preconditions
        @code
        this->empty() == false
        @endcode

        @par Example
        @code
        assert( url_view( "/path/to/file.txt" ).segments().back() == "file.txt" );
        @endcode

        @par Preconditions
        @code
        this->empty() == false
        @endcode

        @par Effects
        @code
        return *--end();
        @endcode

        @par Complexity
        Linear in `this->back().size()`.

        @par Exception Safety
        Calls to allocate may throw.
    */
    std::string
    back() const noexcept;

    /** Return an iterator to the beginning

        @par Complexity
        Linear in `this->front().size()` or
        constant if `this->empty()`.

        @par Exception Safety
        Throws nothing.
    */
    BOOST_URL_DECL
    iterator
    begin() const noexcept;

    /** Return an iterator to the end

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    BOOST_URL_DECL
    iterator
    end() const noexcept;
};

//------------------------------------------------

/** Format to an output stream

    Any percent-escapes are emitted as-is;
    no decoding is performed.

    @par Complexity
    Linear in `ps.buffer().size()`.

    @par Effects
    @code
    return os << ps.buffer();
    @endcode
*/
BOOST_URL_DECL
std::ostream&
operator<<(
    std::ostream& os,
    segments_base const& ps);

} // urls
} // boost

#include <boost/url/impl/segments_base.hpp>

#endif
