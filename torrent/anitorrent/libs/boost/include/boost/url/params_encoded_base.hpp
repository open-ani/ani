//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARAMS_ENCODED_BASE_HPP
#define BOOST_URL_PARAMS_ENCODED_BASE_HPP

#include <boost/url/detail/config.hpp>
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
class BOOST_URL_DECL params_encoded_base
{
    friend class url_view_base;
    friend class params_encoded_ref;
    friend class params_encoded_view;

    detail::query_ref ref_;

    params_encoded_base() = default;
    params_encoded_base(
        params_encoded_base const&) = default;
    params_encoded_base& operator=(
        params_encoded_base const&) = default;
    params_encoded_base(
        detail::query_ref const& ref) noexcept;

public:
    /** A Bidirectional iterator to a query parameter

        Objects of this type allow iteration
        through the parameters in the query.
        Strings returned by iterators may
        contain percent escapes.
        The values returned are read-only;
        changes to parameters must be made
        through the container instead, if the
        container supports modification.

        <br>

        The strings produced when iterators
        are dereferenced refer to the underlying
        character buffer.
        Ownership is not transferred; the caller
        is responsible for ensuring that the
        lifetime of the buffer extends until
        it is no longer referenced by any
        container or iterator.
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
        params_encoded_view::value_type qp( *url_view( "?first=John&last=Doe" ).params().find( "first" ) );
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
    using reference = param_pct_view;

    /// @copydoc reference
    using const_reference = param_pct_view;

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

    /** Return the query corresponding to these params

        This function returns the query string
        referenced by the container.
        The returned string may contain
        percent escapes.

        @par Example
        @code
        assert( url_view( "?first=John&last=Doe" ).encoded_params().buffer() == "first=John&last=Doe" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par BNF
        @code
        query-params    = query-param *( "&" query-param )
        query-param     = key [ "=" value ]
        key             = *qpchar
        value           = *( qpchar / "=" )
        @endcode

        @par Specification
        @li <a href="https://en.wikipedia.org/wiki/Query_string"
            >Query string (Wikipedia)</a>
    */
    pct_string_view
    buffer() const noexcept;

    /** Return true if there are no params

        @par Example
        @code
        assert( ! url_view( "?key=value" ).encoded_params().empty() );
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
        assert( url_view( "?key=value").encoded_params().size() == 1 );
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
        the specified key,
        which may contain percent escapes.
        The comparison is performed as if all
        escaped characters were decoded first.

        @par Example
        @code
        assert( url_view( "?first=John&last=Doe" ).encoded_params().contains( "first" ) );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` contains an invalid percent-encoding.

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
        pct_string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Return the number of matching keys

        This function examines the parameters
        in the container to find the number of
        matches for the specified key,
        which may contain percent escapes.
        The comparison is performed as if all
        escaped characters were decoded first.

        @par Example
        @code
        assert( url_view( "?first=John&last=Doe" ).encoded_params().count( "first" ) == 1 );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` contains an invalid percent-encoding.

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
        pct_string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Find a matching key

        This function examines the parameters
        in the container to find a match for
        the specified key,
        which may contain percent escapes.
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
        assert( url_view( "?first=John&last=Doe" ).encoded_params().find( "First", ignore_case )->value == "John" );
        @endcode

        @par Effects
        @code
        return this->find( this->begin(), key, ic );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` contains an invalid percent-encoding.

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
        pct_string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Find a matching key

        This function examines the parameters
        in the container to find a match for
        the specified key, which may contain
        percent escapes.
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

        assert( u.encoded_params().find( "first" ) != u.encoded_params().find( "first", ignore_case ) );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` contains an invalid percent-encoding.

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
        pct_string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Find a matching key
    
        This function examines the parameters
        in the container to find a match for
        the specified key, which may contain
        percent escapes.
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
        assert( url_view( "?first=John&last=Doe" ).encoded_params().find_last( "last" )->value == "Doe" );
        @endcode

        @par Complexity
        Linear in `this->buffer().size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` contains an invalid percent-encoding.

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
        pct_string_view key,
        ignore_case_param ic = {}) const noexcept;

    /** Find a matching key
    
        This function examines the parameters
        in the container to find a match for
        the specified key, which may contain
        percent escapes.
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

        assert( u.encoded_params().find_last( "last" ) != u.encoded_params().find_last( "last", ignore_case ) );
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
        pct_string_view key,
        ignore_case_param ic = {}) const noexcept;

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
    params_encoded_base const& qp);

} // urls
} // boost

#include <boost/url/impl/params_encoded_base.hpp>

#endif
