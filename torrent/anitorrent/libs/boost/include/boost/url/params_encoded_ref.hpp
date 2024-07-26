//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARAMS_ENCODED_REF_HPP
#define BOOST_URL_PARAMS_ENCODED_REF_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/ignore_case.hpp>
#include <boost/url/params_encoded_view.hpp>
#include <initializer_list>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
class url_base;
class params_encoded_view;
#endif

/** A view representing query parameters in a URL

    Objects of this type are used to interpret
    the query parameters as a bidirectional view
    of key value pairs.

    The view does not retain ownership of the
    elements and instead references the original
    url. The caller is responsible for ensuring
    that the lifetime of the referenced url
    extends until it is no longer referenced.

    The view is modifiable; calling non-const
    members causes changes to the referenced
    url.

    @par Example
    @code
    url u( "?first=John&last=Doe" );

    params_encoded_ref p = u.encoded_params();
    @endcode

    Strings produced when elements are returned
    have type @ref param_pct_view and represent
    encoded strings. Strings passed to member
    functions may contain percent escapes, and
    throw exceptions on invalid inputs.

    @par Iterator Invalidation
    Changes to the underlying character buffer
    can invalidate iterators which reference it.
    Modifications made through the container
    invalidate some iterators to the underlying
    character buffer:
    @li @ref append : Only `end()`.
    @li @ref assign, @ref clear,
        `operator=` : All params.
    @li @ref erase : Erased params and all
        params after (including `end()`).
    @li @ref insert : All params at or after
        the insertion point (including `end()`).
    @li @ref replace, @ref set : Modified
        params and all params
        after (including `end()`).
*/
class BOOST_URL_DECL params_encoded_ref
    : public params_encoded_base
{
    friend class url_base;

    url_base* u_ = nullptr;

    params_encoded_ref(
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
    params_encoded_ref(
        params_encoded_ref const& other) = default;

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
    params_encoded_ref&
    operator=(
        params_encoded_ref const& other);

    /** Assignment

        After assignment, the previous contents
        of the query parameters are replaced by
        the contents of the initializer-list.

        <br>
        All iterators are invalidated.

        @par Preconditions
        None of character buffers referenced by
        `init` may overlap the character buffer of
        the underlying url, or else the behavior
        is undefined.

        @par Effects
        @code
        this->assign( init.begin(), init.end() );
        @endcode

        @par Complexity
        Linear in `init.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `init` contains an invalid percent-encoding.

        @param init The list of params to assign.
    */
    params_encoded_ref&
    operator=(std::initializer_list<
        param_pct_view> init);

    /** Conversion

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    operator
    params_encoded_view() const noexcept;

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

        assert( &u.encoded_params().url() == &u );
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

    /** Assign params

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

        u.encoded_params().assign({ { "first", "John" }, { "last", "Doe" } });
        @endcode

        @par Complexity
        Linear in `init.size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `init` contains an invalid percent-encoding.

        @param init The list of params to assign.
    */
    void
    assign(
        std::initializer_list<
            param_pct_view> init);

    /** Assign params

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
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, param_pct_view >::value == true
        @endcode

        @par Complexity
        Linear in the size of the range.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The range contains an invalid percent-encoding.

        @param first, last The range of params
        to assign.
    */
    template<class FwdIt>
    void
    assign(FwdIt first, FwdIt last);

    //--------------------------------------------

    /** Append params

        This function appends a param to the view.

        <br>
        The `end()` iterator is invalidated.

        @par Example
        @code
        url u;

        u.encoded_params().append( { "first", "John" } );
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `p` contains an invalid percent-encoding.

        @return An iterator to the new element.

        @param p The param to append.
    */
    iterator
    append(
        param_pct_view const& p);

    /** Append params

        This function appends the params in
        an <em>initializer-list</em> to the view.

        <br>
        The `end()` iterator is invalidated.

        @par Example
        @code
        url u;

        u.encoded_params().append({ {"first", "John"}, {"last", "Doe"} });
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `init` contains an invalid percent-encoding.

        @return An iterator to the first new element.

        @param init The list of params to append.
    */
    iterator
    append(
        std::initializer_list<
            param_pct_view> init);

    /** Append params

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
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, param_pct_view >::value == true
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The range contains an invalid percent-encoding.

        @return An iterator to the first new element.

        @param first, last The range of params
        to append.
    */
    template<class FwdIt>
    iterator
    append(
        FwdIt first, FwdIt last);

    //--------------------------------------------

    /** Insert params

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
        Exceptions thrown on invalid input.

        @throw system_error
        `p` contains an invalid percent-encoding.

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
        param_pct_view const& p);

    /** Insert params

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
        Exceptions thrown on invalid input.

        @throw system_error
        `init` contains an invalid percent-encoding.

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
            param_pct_view> init);

    /** Insert params

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
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, param_pct_view >::value == true
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The range contains an invalid percent-encoding.

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

    /** Erase params

        This function removes an element from
        the container.

        <br>
        All iterators that are equal to
        `pos` or come after are invalidated.

        @par Example
        @code
        url u( "?first=John&last=Doe" );

        params_encoded_ref::iterator it = u.encoded_params().erase( u.encoded_params().begin() );

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

    /** Erase params

        This function removes a range of params
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
        params to erase.
    */
    iterator
    erase(
        iterator first,
        iterator last) noexcept;

    /** Erase params

        <br>
        All iterators are invalidated.

        @par Postconditions
        @code
        this->count( key, ic ) == 0
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` contains an invalid percent-encoding.

        @return The number of params removed
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
        pct_string_view key,
        ignore_case_param ic = {}) noexcept;

    //--------------------------------------------

    /** Replace params

        This function replaces the contents
        of the element at `pos` with the
        specified param.

        <br>
        All iterators that are equal to
        `pos` or come after are invalidated.

        @note
        The strings passed in must not come
        from the element being replaced,
        or else the behavior is undefined.

        @par Example
        @code
        url u( "?first=John&last=Doe" );

        u.encoded_params().replace( u.encoded_params().begin(), { "title", "Mr" });

        assert( u.encoded_query() == "title=Mr&last=Doe" );
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `p` contains an invalid percent-encoding.

        @return An iterator to the element.

        @param pos An iterator to the element.

        @param p The param to assign.
    */
    iterator
    replace(
        iterator pos,
        param_pct_view const& p);

    /** Replace params

        This function replaces a range of
        params with the params in an
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
        Exceptions thrown on invalid input.

        @throw system_error
        `init` contains an invalid percent-encoding.

        @return An iterator to the first
        element inserted, or one past `to` if
        `init.size() == 0`.

        @param from,to The range of params
        to replace.

        @param init The list of params to assign.
    */
    iterator
    replace(
        iterator from,
        iterator to,
        std::initializer_list<
            param_pct_view> init);

    /** Replace params

        This function replaces a range of
        params with a range of params.

        <br>
        All iterators that are equal to
        `from` or come after are invalidated.

        @note
        The strings referenced by the inputs
        must not come from the underlying url,
        or else the behavior is undefined.

        @par Mandates
        @code
        std::is_convertible< std::iterator_traits< FwdIt >::reference_type, param_pct_view >::value == true
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        The range contains an invalid percent-encoding.

        @return An iterator to the first
        element inserted, or one past `to` if
        `first == last`.

        @param from,to The range of params to
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

        u.encoded_params().unset( u.encoded_params().begin() );

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

        @note
        The string passed in must not come
        from the element being replaced,
        or else the behavior is undefined.

        @par Example
        @code
        url u( "?id=42&id=69" );

        u.encoded_params().set( u.encoded_params().begin(), "none" );

        assert( u.encoded_query() == "id=none&id=69" );
        @endcode

        @par Complexity
        Linear in `this->url().encoded_query().size()`.

        @par Exception Safety
        Strong guarantee.
        Calls to allocate may throw.
        Exceptions thrown on invalid input.

        @throw system_error
        `value` contains an invalid percent-encoding.

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
        pct_string_view value);

    /** Set a value

        This function performs one of two
        actions depending on the value of
        `this->contains( key, ic )`.

        @li If key is contained in the view
        then one of the matching params has
        its value changed to the specified value.
        The remaining params with a matching
        key are erased. Otherwise,

        @li If `key` is not contained in the
        view, then the function apppends the
        param `{ key, value }`.

        <br>
        All iterators are invalidated.

        @note
        The strings passed in must not come
        from the element being replaced,
        or else the behavior is undefined.

        @par Example
        @code
        url u( "?id=42&id=69" );

        u.encoded_params().set( "id", "none" );

        assert( u.encoded_params().count( "id" ) == 1 );
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
        Exceptions thrown on invalid input.

        @throw system_error
        `key` or `value` contain an invalid
        percent-encoding.

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
        pct_string_view key,
        pct_string_view value,
        ignore_case_param ic = {});

private:
    detail::params_iter_impl
    find_impl(
        detail::params_iter_impl,
        pct_string_view,
        ignore_case_param) const noexcept;

    detail::params_iter_impl
    find_last_impl(
        detail::params_iter_impl,
        pct_string_view,
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
// #include <boost/url/impl/params_encoded_ref.hpp>

#endif
