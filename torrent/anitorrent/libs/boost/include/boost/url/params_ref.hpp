//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARAMS_REF_HPP
#define BOOST_URL_PARAMS_REF_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/ignore_case.hpp>
#include <boost/url/params_base.hpp>
#include <initializer_list>
#include <iterator>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
class url_base;
class params_view;
#endif

/** A view representing query parameters in a URL

    Objects of this type are used to interpret
    the query parameters as a bidirectional view
    of key/value pairs.
    The view does not retain ownership of the
    elements and instead references the original
    url. The caller is responsible for ensuring
    that the lifetime of the referenced url
    extends until it is no longer referenced.
    The view is modifiable; calling non-const
    members causes changes to the referenced
    url.

    <br>

    Percent escapes in strings returned when
    dereferencing iterators are automatically
    decoded.
    Reserved characters in strings supplied
    to modifier functions are automatically
    percent-escaped.

    @par Example
    @code
    url u( "?first=John&last=Doe" );

    params_ref p = u.params();
    @endcode

    @par Iterator Invalidation
    Changes to the underlying character buffer
    can invalidate iterators which reference it.
    Modifications made through the container
    invalidate some or all iterators:
    <br>

    @li @ref append : Only `end()`.

    @li @ref assign, @ref clear,
        `operator=` : All elements.

    @li @ref erase : Erased elements and all
        elements after (including `end()`).

    @li @ref insert : All elements at or after
        the insertion point (including `end()`).

    @li @ref replace, @ref set : Modified
        elements and all elements
        after (including `end()`).
*/
class BOOST_URL_DECL params_ref
    : public params_base
{
    friend class url_base;

    url_base* u_ = nullptr;

    params_ref(
        url_base& u,
        encoding_opts opt) noexcept;

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
        &this->url() == &other.url()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param other The other view.
    */
    params_ref(
        params_ref const& other) = default;

    /** Constructor

        After construction, both views will
        reference the same url but this
        instance will use the specified
        @ref encoding_opts when the values
        are decoded.

        Ownership is not transferred; the
        caller is responsible for ensuring
        the lifetime of the url extends
        until it is no longer referenced.

        @par Postconditions
        @code
        &this->url() == &other.url()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param other The other view.
        @param opt The options for decoding. If
        this parameter is omitted, `space_as_plus`
        is used.

    */
    params_ref(
        params_ref const& other,
        encoding_opts opt) noexcept;

    /** Assignment

        The previous contents of this are
        replaced by the contents of `other.

        <br>
        All iterators are invalidated.

        @note
        The strings referenced by `other`
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Effects
        @code
        this->assign( other.begin(), other.end() );
        @endcode

        @par Complexity
        Linear in `other.buffer().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param other The params to assign.
    */
    params_ref&
    operator=(
        params_ref const& other);

    /** Assignment

        After assignment, the previous contents
        of the query parameters are replaced by
        the contents of the initializer-list.

        @par Preconditions
        None of character buffers referenced by
        `init` may overlap the character buffer of
        the underlying url, or else the behavior
        is undefined.

        @par Effects
        @code
        this->assign( init );
        @endcode

        @par Complexity
        Linear in `init.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param init The list of params to assign.
    */
    params_ref&
    operator=(
        std::initializer_list<
            param_view> init);

    /** Conversion
    */
    operator
    params_view() const noexcept;

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
        url u( "?key=value" );

        assert( &u.segments().url() == &u );
        @endcode

        @par Exception Safety
        @code
        Throws nothing.
        @endcode
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
        this->url().remove_query();
        @endcode

        @par Postconditions
        @code
        this->empty() == true && this->url().has_query() == false
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    void
    clear() noexcept;

    //--------------------------------------------

    /** Assign elements

        This function replaces the entire
        contents of the view with the params
        in the <em>initializer-list</em>.

        <br>
        All iterators are invalidated.

        @note
        The strings referenced by the inputs
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Example
        @code
        url u;

        u.params().assign( {{ "first", "John" }, { "last", "Doe" }} );
        @endcode

        @par Complexity
        Linear in `init.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param init The list of params to assign.
    */
    void
    assign(
        std::initializer_list<
            param_view> init);

    /** Assign elements

        This function replaces the entire
        contents of the view with the params
        in the range.

        <br>
        All iterators are invalidated.

        @note
        The strings referenced by the inputs
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Mandates
        @code
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, param_view >::value == true
        @endcode

        @par Complexity
        Linear in the size of the range.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @param first, last The range of params
        to assign.
    */
    template<class FwdIt>
    void
    assign(FwdIt first, FwdIt last);

    //--------------------------------------------

    /** Append elements

        This function appends a param to the view.

        <br>
        The `end()` iterator is invalidated.

        @par Example
        @code
        url u;

        u.params().append( { "first", "John" } );
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the new element.

        @param p The param to append.
    */
    iterator
    append(
        param_view const& p);

    /** Append elements

        This function appends the params in
        an <em>initializer-list</em> to the view.

        <br>
        The `end()` iterator is invalidated.

        @par Example
        @code
        url u;

        u.params().append({ { "first", "John" }, { "last", "Doe" } });
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the first new element.

        @param init The list of params to append.
    */
    iterator
    append(
        std::initializer_list<
            param_view> init);

    /** Append elements

        This function appends a range of params
        to the view.

        <br>
        The `end()` iterator is invalidated.

        @note
        The strings referenced by the inputs
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Mandates
        @code
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, param_view >::value == true
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the first new element.

        @param first, last The range of params
        to append.
    */
    template<class FwdIt>
    iterator
    append(
        FwdIt first, FwdIt last);

    //--------------------------------------------

    /** Insert elements

        This function inserts a param
        before the specified position.

        <br>
        All iterators that are equal to
        `before` or come after are invalidated.

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the inserted
        element.

        @param before An iterator before which
        the param is inserted. This may
        be equal to `end()`.

        @param p The param to insert.
    */
    iterator
    insert(
        iterator before,
        param_view const& p);

    /** Insert elements

        This function inserts the params in
        an <em>initializer-list</em> before
        the specified position.

        <br>
        All iterators that are equal to
        `before` or come after are invalidated.

        @note
        The strings referenced by the inputs
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the first
        element inserted, or `before` if
        `init.size() == 0`.

        @param before An iterator before which
        the element is inserted. This may
        be equal to `end()`.

        @param init The list of params to insert.
    */
    iterator
    insert(
        iterator before,
        std::initializer_list<
            param_view> init);

    /** Insert elements

        This function inserts a range of
        params before the specified position.

        <br>
        All iterators that are equal to
        `before` or come after are invalidated.

        @note
        The strings referenced by the inputs
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Mandates
        @code
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, param_view >::value == true
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the first
        element inserted, or `before` if
        `first == last`.

        @param before An iterator before which
        the element is inserted. This may
        be equal to `end()`.

        @param first, last The range of params
        to insert.
    */
    template<class FwdIt>
    iterator
    insert(
        iterator before,
        FwdIt first,
        FwdIt last);

    //--------------------------------------------

    /** Erase elements

        This function removes an element from
        the container.

        <br>
        All iterators that are equal to
        `pos` or come after are invalidated.

        @par Example
        @code
        url u( "?first=John&last=Doe" );

        params_ref::iterator it = u.params().erase( u.params().begin() );

        assert( u.encoded_query() == "last=Doe" );
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Throws nothing.

        @return An iterator to one past
        the removed element.

        @param pos An iterator to the element.
    */
    iterator
    erase(iterator pos) noexcept;

    /** Erase elements

        This function removes a range of elements
        from the container.

        <br>
        All iterators that are equal to
        `first` or come after are invalidated.

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Throws nothing.

        @return An iterator to one past
        the removed range.

        @param first, last The range of
        elements to erase.
    */
    iterator
    erase(
        iterator first,
        iterator last) noexcept;

    /** Erase elements

        <br>
        All iterators are invalidated.

        @par Postconditions
        @code
        this->count( key, ic ) == 0
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Throws nothing.

        @return The number of elements removed
        from the container.

        @param key The key to match.
        By default, a case-sensitive
        comparison is used.

        @param ic An optional parameter. If
        the value @ref ignore_case is passed
        here, the comparison is
        case-insensitive.
    */
    std::size_t
    erase(
        core::string_view key,
        ignore_case_param ic = {}) noexcept;

    //--------------------------------------------

    /** Replace elements

        This function replaces the contents
        of the element at `pos` with the
        specified param.

        <br>
        All iterators that are equal to
        `pos` or come after are invalidated.

        @par Example
        @code
        url u( "?first=John&last=Doe" );

        u.params().replace( u.params().begin(), { "title", "Mr" });

        assert( u.encoded_query() == "title=Mr&last=Doe" );
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the element.

        @param pos An iterator to the element.

        @param p The param to assign.
    */
    iterator
    replace(
        iterator pos,
        param_view const& p);

    /** Replace elements

        This function replaces a range of
        elements with the params in an
        <em>initializer-list</em>.

        <br>
        All iterators that are equal to
        `from` or come after are invalidated.

        @note
        The strings referenced by the inputs
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the first
        element inserted, or one past `to` if
        `init.size() == 0`.

        @param from,to The range of elements
        to replace.

        @param init The list of params to assign.
    */
    iterator
    replace(
        iterator from,
        iterator to,
        std::initializer_list<
            param_view> init);

    /** Replace elements

        This function replaces a range of
        elements with a range of params.

        <br>
        All iterators that are equal to
        `from` or come after are invalidated.

        @note
        The strings referenced by the inputs
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Mandates
        @code
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, param_view >::value == true
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the first
        element inserted, or one past `to` if
        `first == last`.

        @param from,to The range of elements to
        replace.

        @param first, last The range of params
        to assign.
    */
    template<class FwdIt>
    iterator
    replace(
        iterator from,
        iterator to,
        FwdIt first,
        FwdIt last);

    //--------------------------------------------

    /** Remove the value on an element

        This function removes the value of
        an element at the specified position.
        After the call returns, `has_value`
        for the element is false.

        <br>
        All iterators that are equal to
        `pos` or come after are invalidated.

        @par Example
        @code
        url u( "?first=John&last=Doe" );

        u.params().unset( u.params().begin() );

        assert( u.encoded_query() == "first&last=Doe" );
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Throws nothing.

        @return An iterator to the element.

        @param pos An iterator to the element.
    */
    iterator
    unset(
        iterator pos) noexcept;

    /** Set a value

        This function replaces the value of an
        element at the specified position.

        <br>
        All iterators that are equal to
        `pos` or come after are invalidated.

        @par Example
        @code
        url u( "?id=42&id=69" );

        u.params().set( u.params().begin(), "none" );

        assert( u.encoded_query() == "id=none&id=69" );
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the element.

        @param pos An iterator to the element.

        @param value The value to assign. The
        empty string still counts as a value.
        That is, `has_value` for the element
        is true.
    */
    iterator
    set(
        iterator pos,
        core::string_view value);

    /** Set a value

        This function performs one of two
        actions depending on the value of
        `this->contains( key, ic )`.

        @li If key is contained in the view
        then one of the matching elements has
        its value changed to the specified value.
        The remaining elements with a matching
        key are erased. Otherwise,

        @li If `key` is not contained in the
        view, then the function apppends the
        param `{ key, value }`.

        <br>
        All iterators are invalidated.

        @par Example
        @code
        url u( "?id=42&id=69" );

        u.params().set( "id", "none" );

        assert( u.params().count( "id" ) == 1 );
        @endcode

        @par Postconditions
        @code
        this->count( key, ic ) == 1 && this->find( key, ic )->value == value
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.

        @return An iterator to the appended
        or modified element.

        @param key The key to match.
        By default, a case-sensitive
        comparison is used.

        @param value The value to assign. The
        empty string still counts as a value.
        That is, `has_value` for the element
        is true.

        @param ic An optional parameter. If
        the value @ref ignore_case is passed
        here, the comparison is
        case-insensitive.
    */
    iterator
    set(
        core::string_view key,
        core::string_view value,
        ignore_case_param ic = {});

    //--------------------------------------------

private:
    detail::params_iter_impl
    find_impl(
        detail::params_iter_impl,
        core::string_view,
        ignore_case_param) const noexcept;

    detail::params_iter_impl
    find_last_impl(
        detail::params_iter_impl,
        core::string_view,
        ignore_case_param) const noexcept;

    template<class FwdIt>
    void
    assign(FwdIt first, FwdIt last,
        std::forward_iterator_tag);

    // Doxygen cannot render ` = delete`
    template<class FwdIt>
    void
    assign(FwdIt first, FwdIt last,
        std::input_iterator_tag) = delete;

    template<class FwdIt>
    iterator
    insert(
        iterator before,
        FwdIt first,
        FwdIt last,
        std::forward_iterator_tag);

    // Doxygen cannot render ` = delete`
    template<class FwdIt>
    iterator
    insert(
        iterator before,
        FwdIt first,
        FwdIt last,
        std::input_iterator_tag) = delete;
};

} // urls
} // boost

// This is in <boost/url/url_base.hpp>
//
// #include <boost/url/impl/params_ref.hpp>

#endif
