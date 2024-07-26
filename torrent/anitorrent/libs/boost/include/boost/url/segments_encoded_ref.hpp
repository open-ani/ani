//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_SEGMENTS_ENCODED_REF_HPP
#define BOOST_URL_SEGMENTS_ENCODED_REF_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/segments_encoded_base.hpp>
#include <initializer_list>
#include <iterator>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
class url_base;
class segments_encoded_view;
#endif

/** A view representing path segments in a URL

    Objects of this type are used to interpret
    the path as a bidirectional view of segments,
    where each segment is a string which may
    contain percent-escapes.

    The view does not retain ownership of the
    elements and instead references the original
    character buffer. The caller is responsible
    for ensuring that the lifetime of the buffer
    extends until it is no longer referenced.

    The view is modifiable; calling non-const
    members causes changes to the referenced
    url.

    @par Example
    @code
    url u( "/path/to/file.txt" );

    segments_encoded_ref ps = u.encoded_segments();
    @endcode

    The strings returned when iterators are
    dereferenced have type @ref pct_string_view
    and may contain percent-escapes.

    Reserved characters in inputs are
    automatically escaped.
    Escapes in inputs are preserved.

    Exceptions are thrown on invalid inputs.

    @par Iterator Invalidation
    Changes to the underlying character buffer
    can invalidate iterators which reference it.
    Modifications made through the container
    invalidate some or all iterators:
    <br>

    @li @ref push_back : Only `end()`.

    @li @ref assign, @ref clear,
        @ref operator= : All elements.

    @li @ref erase : Erased elements and all
        elements after (including `end()`).

    @li @ref insert : All elements at or after
        the insertion point (including `end()`).

    @li @ref replace : Modified
        elements and all elements
        after (including `end()`).

    @see
        @ref segments_encoded_view,
        @ref segments_view,
        @ref segments_ref.
*/
class segments_encoded_ref
    : public segments_encoded_base
{
    friend class url_base;

    url_base* u_ = nullptr;

    segments_encoded_ref(
        url_base& u) noexcept;

public:
    //--------------------------------------------
    //
    // Special Members
    //
    //--------------------------------------------

    /** Constructor

        After construction, both views
        reference the same url. Ownership is not
        transferred; the caller is responsible
        for ensuring the lifetime of the url
        extends until it is no longer
        referenced.

        @par Postconditions
        @code
        &this->url() == &other.url();
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param other The other view.
    */
    segments_encoded_ref(
        segments_encoded_ref const& other) = default;

    /** Assignment

        The existing contents are replaced
        by a copy of the other segments.

        <br>
        All iterators are invalidated.

        @note
        None of the character buffers referenced
        by `other` may overlap the buffer of the
        underlying url, or else the behavior
        is undefined.

        @par Effects
        @code
        this->assign( other.begin(), other.end() );
        @endcode

        @par Complexity
        Linear in `other.buffer().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param other The segments to assign.
    */
    /** @{ */
    BOOST_URL_DECL
    segments_encoded_ref&
    operator=(segments_encoded_ref const& other);

    BOOST_URL_DECL
    segments_encoded_ref&
    operator=(segments_encoded_view const& other);
    /** @} */

    /** Assignment

        The existing contents are replaced
        by a copy of the contents of the
        initializer list.
        Reserved characters in the list are
        automatically escaped.
        Escapes in the list are preserved.

        <br>
        All iterators are invalidated.

        @par Example
        @code
        url u;

        u.encoded_segments() = {"path", "to", "file.txt"};
        @endcode

        @par Preconditions
        None of the character buffers referenced
        by the list may overlap the character buffer
        of the underlying url, or else the behavior
        is undefined.

        @par Effects
        @code
        this->assign( init.begin(), init.end() );
        @endcode

        @par Complexity
        Linear in `init.size() + this->url().encoded_query().size() + this->url().encoded_fragment().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The list contains an invalid percent-encoding.

        @param init The list of segments to assign.
    */
    BOOST_URL_DECL
    segments_encoded_ref&
    operator=(std::initializer_list<
        pct_string_view> init);

    /** Conversion

        @see
            @ref segments_encoded_view.
    */
    BOOST_URL_DECL
    operator
    segments_encoded_view() const noexcept;

    //--------------------------------------------
    //
    // Observers
    //
    //--------------------------------------------

    /** Return the referenced url

        This function returns the url referenced
        by the view.

        @par Example
        @code
        url u( "/path/to/file.txt" );

        assert( &u.encoded_segments().url() == &u );
        @endcode

        @par Exception Safety
        Throws nothing.
    */
    url_base&
    url() const noexcept
    {
        return *u_;
    }

    //--------------------------------------------
    //
    // Modifiers
    //
    //--------------------------------------------

    /** Clear the contents of the container

        <br>
        All iterators are invalidated.

        @par Effects
        @code
        this->url().set_encoded_path( "" );
        @endcode

        @par Postconditions
        @code
        this->empty() == true
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size() + this->url().encoded_fragment().size()`.

        @par Exception Safety
        Throws nothing.
    */
    void
    clear() noexcept;

    /** Assign segments

        The existing contents are replaced
        by a copy of the contents of the
        initializer list.
        Reserved characters in the list are
        automatically escaped.
        Escapes in the list are preserved.

        <br>
        All iterators are invalidated.

        @note
        None of the character buffers referenced
        by the list may overlap the character
        buffer of the underlying url, or else
        the behavior is undefined.

        @par Example
        @code
        url u;

        u.segments().assign( {"path", "to", "file.txt"} );
        @endcode

        @par Complexity
        Linear in `init.size() + this->url().encoded_query().size() + this->url().encoded_fragment().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The list contains an invalid percent-encoding.

        @param init The list of segments to assign.
    */
    BOOST_URL_DECL
    void
    assign(std::initializer_list<
        pct_string_view> init);

    /** Assign segments

        The existing contents are replaced
        by a copy of the contents of the range.
        Reserved characters in the range are
        automatically escaped.
        Escapes in the range are preserved.

        <br>
        All iterators are invalidated.

        @note
        None of the character buffers referenced
        by the range may overlap the character
        buffer of the underlying url, or else
        the behavior is undefined.

        @par Mandates
        @code
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, pct_string_view >::value == true
        @endcode

        @par Complexity
        Linear in `std::distance( first, last ) + this->url().encoded_query().size() + this->url().encoded_fragment().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The range contains an invalid percent-encoding.

        @param first, last The range of segments
        to assign.
    */
    template<class FwdIt>
    void
    assign(FwdIt first, FwdIt last);

    //--------------------------------------------

    /** Insert segments

        This function inserts a segment
        before the specified position.
        Reserved characters in the segment are
        automatically escaped.
        Escapes in the segment are preserved.

        <br>
        All iterators that are equal to
        `before` or come after are invalidated.

        @par Complexity
        Linear in `s.size() + this->url().encoded_resource().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The segment contains an invalid percent-encoding.

        @return An iterator to the inserted
        segment.

        @param before An iterator before which
        the segment is inserted. This may
        be equal to `end()`.

        @param s The segment to insert.
    */
    BOOST_URL_DECL
    iterator
    insert(
        iterator before,
        pct_string_view s);

    /** Insert segments

        This function inserts the segments
        in an initializer list before the
        specified position.
        Reserved characters in the list are
        automatically escaped.
        Escapes in the list are preserved.

        <br>
        All iterators that are equal to
        `before` or come after are invalidated.

        @note
        None of the character buffers referenced
        by the list may overlap the character
        buffer of the underlying url, or else
        the behavior is undefined.

        @par Example
        @code
        url u( "/file.txt" );

        u.encoded_segments().insert( u.encoded_segments().begin(), { "path", "to" } );
        @endcode

        @par Complexity
        Linear in `init.size() + this->url().encoded_resource().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The list contains an invalid percent-encoding.

        @return An iterator to the first
        element inserted, or `before` if
        `init.size() == 0`.

        @param before An iterator before which
        the list is inserted. This may
        be equal to `end()`.

        @param init The list of segments to insert.
    */
    BOOST_URL_DECL
    iterator
    insert(
        iterator before,
        std::initializer_list<
            pct_string_view> init);

    /** Insert segments

        This function inserts the segments in
        a range before the specified position.
        Reserved characters in the range are
        automatically escaped.
        Escapes in the range are preserved.

        <br>
        All iterators that are equal to
        `before` or come after are invalidated.

        @note
        None of the character buffers referenced
        by the range may overlap the character
        buffer of the underlying url, or else
        the behavior is undefined.

        @par Mandates
        @code
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, pct_string_view >::value == true
        @endcode

        @par Complexity
        Linear in `std::distance( first, last ) + this->url().encoded_resource().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The range contains an invalid percent-encoding.

        @return An iterator to the first
        segment inserted, or `before` if
        `init.empty()`.

        @param before An iterator before which
        the range is inserted. This may
        be equal to `end()`.

        @param first, last The range of segments
        to insert.
    */
    template<class FwdIt>
    iterator
    insert(
        iterator before,
        FwdIt first,
        FwdIt last);

    //--------------------------------------------

    /** Erase segments

        This function removes a segment.

        <br>
        All iterators that are equal to
        `pos` or come after are invalidated.

        @par Complexity
        Linear in `this->url().encoded_resource().size()`.

        @par Exception Safety
        Throws nothing.

        @return An iterator to one past
        the removed segment.

        @param pos An iterator to the element.
    */
    iterator
    erase(
        iterator pos) noexcept;

    /** Erase segments

        This function removes a range of segments
        from the container.

        <br>
        All iterators that are equal to
        `first` or come after are invalidated.

        @par Complexity
        Linear in `this->url().encoded_resource().size()`.

        @par Exception Safety
        Throws nothing.

        @return An iterator to one past
        the removed range.

        @param first, last The range of
        segments to erase.
    */
    BOOST_URL_DECL
    iterator
    erase(
        iterator first,
        iterator last) noexcept;

    //--------------------------------------------

    /** Replace segments

        This function replaces the segment at
        the specified position.
        Reserved characters in the string are
        automatically escaped.
        Escapes in the string are preserved.

        <br>
        All iterators that are equal to
        `pos` or come after are invalidated.

        @par Complexity
        Linear in `s.size() + this->url().encoded_resouce().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the replaced segment.

        @param pos An iterator to the segment.

        @param s The string to assign.
    */
    BOOST_URL_DECL
    iterator
    replace(
        iterator pos,
        pct_string_view s);

    /** Replace segments

        This function replaces a range of
        segments with one segment.
        Reserved characters in the string are
        automatically escaped.
        Escapes in the string are preserved.

        <br>
        All iterators that are equal to
        `from` or come after are invalidated.

        @par Complexity
        Linear in `s.size() + this->url().encoded_resouce().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The string contains an invalid percent-encoding.

        @return An iterator to the new segment.

        @param from, to The range of segments to replace.

        @param s The string to assign.
    */
    BOOST_URL_DECL
    iterator
    replace(
        iterator from,
        iterator to,
        pct_string_view s);

    /** Replace segments

        This function replaces a range of
        segments with a list of segments in
        an initializer list.
        Reserved characters in the list are
        automatically escaped.
        Escapes in the list are preserved.

        <br>
        All iterators that are equal to
        `from` or come after are invalidated.

        @par Preconditions
        None of the character buffers referenced
        by the list may overlap the character
        buffer of the underlying url, or else
        the behavior is undefined.

        @par Complexity
        Linear in `init.size() + this->url().encoded_resouce().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The list contains an invalid percent-encoding.

        @return An iterator to the first
        segment inserted, or one past `to` if
        `init.size() == 0`.

        @param from, to The range of segments to replace.

        @param init The list of segments to assign.
    */
    BOOST_URL_DECL
    iterator
    replace(
        iterator from,
        iterator to,
        std::initializer_list<
            pct_string_view> init);

    /** Replace segments

        This function replaces a range of
        segments with annother range of segments.
        Reserved characters in the new range are
        automatically escaped.
        Escapes in the new range are preserved.

        <br>
        All iterators that are equal to
        `from` or come after are invalidated.

        @par Preconditions
        None of the character buffers referenced
        by the new range may overlap the character
        buffer of the underlying url, or else
        the behavior is undefined.

        @par Complexity
        Linear in `std::distance( first, last ) + this->url().encoded_resouce().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The range contains an invalid percent-encoding.

        @return An iterator to the first
        segment inserted, or one past `to` if
        `init.size() == 0`.

        @param from, to The range of segments to replace.

        @param first, last The range of segments to assign.
    */
    template<class FwdIt>
    iterator
    replace(
        iterator from,
        iterator to,
        FwdIt first,
        FwdIt last);

    //--------------------------------------------

    /** Append a segment

        This function appends a segment to
        the end of the path.
        Reserved characters in the string are
        automatically escaped.
        Escapes in the string are preserved.

        <br>
        All end iterators are invalidated.

        @par Postconditions
        @code
        this->back() == s
        @endcode

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The string contains an invalid percent-encoding.

        @param s The segment to append.
    */
    void
    push_back(
        pct_string_view s);

    /** Remove the last segment

        This function removes the last segment
        from the container.

        <br>
        Iterators to the last segment as well
        as all end iterators are invalidated.

        @par Preconditions
        @code
        not this->empty()
        @endcode

        @par Exception Safety
        Throws nothing.
    */
    void
    pop_back() noexcept;

private:
    template<class FwdIt>
    iterator
    insert(
        iterator before,
        FwdIt first,
        FwdIt last,
        std::input_iterator_tag) = delete;

    template<class FwdIt>
    iterator
    insert(
        iterator before,
        FwdIt first,
        FwdIt last,
        std::forward_iterator_tag);
};

} // urls
} // boost

// This is in <boost/url/url_base.hpp>
//
// #include <boost/url/impl/segments_encoded_ref.hpp>

#endif
