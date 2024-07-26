//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARAMS_BASE_HPP
#define BOOST_URL_PARAMS_BASE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/encoding_opts.hpp>
#include <boost/url/ignore_case.hpp>
#include <boost/url/param.hpp>
#include <boost/url/detail/params_iter_impl.hpp>
#include <boost/url/detail/url_impl.hpp>
#include <iosfwd>

namespace boost {
namespace urls {

/** Common functionality for containers

    This base class is used by the library
    to provide common member functions for
    containers. This cannot be instantiated
    directly; Instead, use one of the
    containers or functions:

    @par Containers
    @li @ref params_ref
    @li @ref params_view
    @li @ref params_encoded_ref
    @li @ref params_encoded_view
*/
class BOOST_URL_DECL params_base
{
    friend class url_view_base;
    friend class params_ref;
    friend class params_view;

    detail::query_ref ref_;
    encoding_opts opt_;

    params_base() noexcept;
    params_base(
        detail::query_ref const& ref,
        encoding_opts opt) noexcept;
    params_base(
        params_base const&) = default;
    params_base& operator=(
        params_base const&) = default;

public:
    /** A Bidirectional iterator to a query parameter

        Objects of this type allow iteration
        through the parameters in the query.
        Any percent-escapes in returned strings
        are decoded first.
        The values returned are read-only;
        changes to parameters must be made
        through the container instead, if the
        container supports modification.

        <br>

        The strings produced when iterators are
        dereferenced belong to the iterator and
        become invalidated when that particular
        iterator is incremented, decremented,
        or destroyed.

        @note

        The implementation may use temporary,
        recycled storage to store decoded
        strings. These iterators are meant
        to be used ephemerally. That is, for
        short durations such as within a
        function scope. Do not store
        iterators with static storage
        duration or as long-lived objects.
    */
#ifdef BOOST_URL_DOCS
    using iterator = __see_below__;
#else
    class iterator;
#endif

    /// @copydoc iterator
    using const_iterator = iterator;

    /** The value type

        Values of this type represent parameters
        whose strings retain unique ownership by
        making a copy.

        @par Example
        @code
        params_view::value_type qp( *url_view( "?first=John&last=Doe" ).params().find( "first" ) );
        @endcode

        @see
            @ref param.
    */
    using value_type = param;

    /** The reference type

        This is the type of value returned when
        iterators of the view are dereferenced.

        @see
            @ref param_view.
    */
    using reference = param;

    /// @copydoc reference
    using const_reference = param;

    /** An unsigned integer type to represent sizes.
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
        assert( url_view( "?first=John&last=Doe" ).params().buffer() == "?first=John&last=Doe" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    pct_string_view
    buffer() const noexcept;

    /** Return true if there are no params

        @par Example
        @code
        assert( ! url_view( "?key=value" ).params().empty() );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    bool
    empty() const noexcept;

    /** Return the number of params

        @par Example
        @code
        assert( url_view( "?key=value").params().size() == 1 );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    std::size_t
    size() const noexcept;

    /** Return an iterator to the beginning

        @par Complexity
        Linear in the size of the first param.

        @par Exception Safety
        Throws nothing.
    */
    iterator
    begin() const noexcept;

    /** Return an iterator to the end

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    iterator
    end() const noexcept;

    //--------------------------------------------

    /** Return true if a matching key exists

        This function examines the parameters
        in the container to find a match for
        the specified key.
        The comparison is performed as if all
        escaped characters were decoded first.

        @par Example
        @code
        assert( url_view( "?first=John&last=Doe" ).params().contains( "first" ) );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @par Exception Safety
        Throws nothing.

        @param key The key to match.
        By default, a case-sensitive
        comparison is used.

        @param ic An optional parameter. If
        the value @ref ignore_case is passed
        here, the comparison is
        case-insensitive.
    */
    bool
    contains(
        core::string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Return the number of matching keys

        This function examines the
        parameters in the container to
        find the number of matches for
        the specified key.
        The comparison is performed as if all
        escaped characters were decoded first.

        @par Example
        @code
        assert( url_view( "?first=John&last=Doe" ).params().count( "first" ) == 1 );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @par Exception Safety
        Throws nothing.

        @param key The key to match.
        By default, a case-sensitive
        comparison is used.

        @param ic An optional parameter. If
        the value @ref ignore_case is passed
        here, the comparison is
        case-insensitive.
    */
    std::size_t
    count(
        core::string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Find a matching key

        This function examines the parameters
        in the container to find a match for
        the specified key.
        The comparison is performed as if all
        escaped characters were decoded first.

        <br>

        The search starts from the first param
        and proceeds forward until either the
        key is found or the end of the range is
        reached, in which case `end()` is
        returned.

        @par Example
        @code
        assert( (*url_view( "?first=John&last=Doe" ).params().find( "First", ignore_case )).value == "John" );
        @endcode

        @par Effects
        @code
        return this->find( this->begin(), key, ic );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @return an iterator to the param

        @param key The key to match.
        By default, a case-sensitive
        comparison is used.

        @param ic An optional parameter. If
        the value @ref ignore_case is passed
        here, the comparison is
        case-insensitive.
    */
    iterator
    find(
        core::string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Find a matching key

        This function examines the
        parameters in the container to
        find a match for the specified key.
        The comparison is performed as if all
        escaped characters were decoded first.

        <br>

        The search starts at `from`
        and proceeds forward until either the
        key is found or the end of the range is
        reached, in which case `end()` is
        returned.

        @par Example
        @code
        url_view u( "?First=John&Last=Doe" );

        assert( u.params().find( "first" ) != u.params().find( "first", ignore_case ) );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @return an iterator to the param

        @param from The position to begin the
            search from. This can be `end()`.

        @param key The key to match.
        By default, a case-sensitive
        comparison is used.

        @param ic An optional parameter. If
        the value @ref ignore_case is passed
        here, the comparison is
        case-insensitive.
    */
    iterator
    find(
        iterator from,
        core::string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Find a matching key
    
        This function examines the
        parameters in the container to
        find a match for the specified key.
        The comparison is performed as if all
        escaped characters were decoded first.

        <br>

        The search starts from the last param
        and proceeds backwards until either the
        key is found or the beginning of the
        range is reached, in which case `end()`
        is returned.

        @par Example
        @code
        assert( (*url_view( "?first=John&last=Doe" ).params().find_last( "last" )).value == "Doe" );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @return an iterator to the param

        @param key The key to match.
        By default, a case-sensitive
        comparison is used.

        @param ic An optional parameter. If
        the value @ref ignore_case is passed
        here, the comparison is
        case-insensitive.
    */
    iterator
    find_last(
        core::string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Find a matching key
    
        This function examines the
        parameters in the container to
        find a match for the specified key.
        The comparison is performed as if all
        escaped characters were decoded first.

        <br>

        The search starts prior to `before`
        and proceeds backwards until either the
        key is found or the beginning of the
        range is reached, in which case `end()`
        is returned.

        @par Example
        @code
        url_view u( "?First=John&Last=Doe" );

        assert( u.params().find_last( "last" ) != u.params().find_last( "last", ignore_case ) );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @return an iterator to the param

        @param before One past the position
        to begin the search from. This can
        be `end()`.

        @param key The key to match.
        By default, a case-sensitive
        comparison is used.

        @param ic An optional parameter. If
        the value @ref ignore_case is passed
        here, the comparison is
        case-insensitive.
    */
    iterator
    find_last(
        iterator before,
        core::string_view key,
        ignore_case_param ic = {}) const noexcept;

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
    params_base const& qp);

} // urls
} // boost

#include <boost/url/impl/params_base.hpp>

#endif
