//
// Copyright (c) 2022 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PCT_STRING_VIEW_HPP
#define BOOST_URL_PCT_STRING_VIEW_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/encoding_opts.hpp>
#include <boost/url/error_types.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/grammar/string_token.hpp>
#include <boost/url/grammar/string_view_base.hpp>
#include <cstddef>
#include <iterator>
#include <string>
#include <type_traits>
#include <utility>

namespace boost {
namespace urls {

//------------------------------------------------

#ifndef BOOST_URL_DOCS
class decode_view;
class pct_string_view;

pct_string_view
make_pct_string_view_unsafe(
    char const*, std::size_t,
        std::size_t) noexcept;

namespace detail {
core::string_view&
ref(pct_string_view& s) noexcept;
} // detail
#endif

//------------------------------------------------

/** A reference to a valid percent-encoded string

    Objects of this type behave like a
    `core::string_view` and have the same interface,
    but offer an additional invariant: they can
    only be constructed from strings containing
    valid percent-escapes.

    Attempting construction from a string
    containing invalid or malformed percent
    escapes results in an exception.

    @par Operators
    The following operators are supported between
    @ref pct_string_view and any object that is
    convertible to `core::string_view`

    @code
    bool operator==( pct_string_view, pct_string_view ) noexcept;
    bool operator!=( pct_string_view, pct_string_view ) noexcept;
    bool operator<=( pct_string_view, pct_string_view ) noexcept;
    bool operator< ( pct_string_view, pct_string_view ) noexcept;
    bool operator> ( pct_string_view, pct_string_view ) noexcept;
    bool operator>=( pct_string_view, pct_string_view ) noexcept;
    @endcode
*/
class pct_string_view final
    : public grammar::string_view_base
{
    std::size_t dn_ = 0;

#ifndef BOOST_URL_DOCS
    friend
    pct_string_view
    make_pct_string_view_unsafe(
        char const*, std::size_t,
            std::size_t) noexcept;

    friend
    core::string_view&
    detail::ref(pct_string_view&) noexcept;
#endif

    // unsafe
    pct_string_view(
        char const* data,
        std::size_t size,
        std::size_t dn) noexcept
        : string_view_base(data, size)
        , dn_(dn)
    {
    }

    BOOST_URL_DECL
    void
    decode_impl(
        string_token::arg& dest,
        encoding_opts opt) const;

public:
    /** Constructor

        Default constructed string are empty.

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    constexpr pct_string_view() = default;

    /** Constructor

        The copy references the same
        underlying character buffer.
        Ownership is not transferred.

        @par Postconditions
        @code
        this->data() == other.data()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par other The string to copy.
    */
    constexpr
    pct_string_view(
        pct_string_view const& other) = default;

    /** Constructor

        The newly constructed string references
        the specified character buffer.
        Ownership is not transferred.

        @par Postconditions
        @code
        this->data() == core::string_view(s).data()
        @endcode

        @par Complexity
        Linear in `core::string_view(s).size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        The string contains an invalid percent encoding.

        @tparam String A type convertible to `core::string_view`

        @param s The string to construct from.
    */
    template<
        class String
#ifndef BOOST_URL_DOCS
        , class = typename std::enable_if<
            std::is_convertible<
                String,
                core::string_view
                    >::value>::type
#endif
    >
    pct_string_view(
        String const& s)
        : pct_string_view(
            detail::to_sv(s))
    {
    }

    /** Constructor (deleted)
    */
    pct_string_view(
        std::nullptr_t) = delete;

    /** Constructor

        The newly constructed string references
        the specified character buffer. Ownership
        is not transferred.

        @par Postconditions
        @code
        this->data() == s && this->size() == len
        @endcode

        @par Complexity
        Linear in `len`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
         The string contains an invalid percent encoding.

        @param s, len The string to construct from.
    */
    pct_string_view(
        char const* s,
        std::size_t len)
        : pct_string_view(
            core::string_view(s, len))
    {
    }

    /** Constructor

        The newly constructed string references
        the specified character buffer. Ownership
        is not transferred.

        @par Postconditions
        @code
        this->data() == s.data() && this->size() == s.size()
        @endcode

        @par Complexity
        Linear in `s.size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        The string contains an invalid percent encoding.

        @param s The string to construct from.
    */
    BOOST_URL_DECL
    pct_string_view(
        core::string_view s);

    /** Assignment

        The copy references the same
        underlying character buffer.
        Ownership is not transferred.

        @par Postconditions
        @code
        this->data() == other.data()
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par other The string to copy.
    */
    pct_string_view& operator=(
        pct_string_view const& other) = default;

    friend
    BOOST_URL_DECL
    system::result<pct_string_view>
    make_pct_string_view(
        core::string_view s) noexcept;

    //--------------------------------------------

    /** Return the decoded size

        This function returns the number of
        characters in the resulting string if
        percent escapes were converted into
        ordinary characters.

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    std::size_t
    decoded_size() const noexcept
    {
        return dn_;
    }

    /** Return the string as a range of decoded characters

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @see
            @ref decode_view.
    */
    decode_view
    operator*() const noexcept;

    /** Return the string with percent-decoding

        This function converts percent escapes
        in the string into ordinary characters
        and returns the result.
        When called with no arguments, the
        return type is `std::string`.
        Otherwise, the return type and style
        of output is determined by which string
        token is passed.

        @par Example
        @code
        assert( pct_string_view( "Program%20Files" ).decode() == "Program Files" );
        @endcode

        @par Complexity
        Linear in `this->size()`.

        @par Exception Safety
        Calls to allocate may throw.
        String tokens may throw exceptions.

        @param opt The options for encoding. If
        this parameter is omitted, the default
        options are used.

        @param token An optional string token.
        If this parameter is omitted, then
        a new `std::string` is returned.
        Otherwise, the function return type
        is the result type of the token.

        @see
            @ref encoding_opts,
            @ref string_token::return_string.
    */
    template<BOOST_URL_STRTOK_TPARAM>
    BOOST_URL_STRTOK_RETURN
    decode(
        encoding_opts opt = {},
        BOOST_URL_STRTOK_ARG(token)) const
    {
/*      If you get a compile error here, it
        means that the token you passed does
        not meet the requirements stated
        in the documentation.
*/
        static_assert(
            string_token::is_token<
                StringToken>::value,
            "Type requirements not met");

        decode_impl(token, opt);
        return token.result();
    }

#ifndef BOOST_URL_DOCS
    // arrow support
    pct_string_view const*
    operator->() const noexcept
    {
        return this;
    }
#endif

    //--------------------------------------------

    // VFALCO No idea why this fails in msvc
    /** Swap
    */
    /*BOOST_CXX14_CONSTEXPR*/ void swap(
        pct_string_view& s ) noexcept
    {
        string_view_base::swap(s);
        std::swap(dn_, s.dn_);
    }
};

//------------------------------------------------

#ifndef BOOST_URL_DOCS
namespace detail {
// obtain modifiable reference to
// underlying string, to handle
// self-intersection on modifiers.
inline
core::string_view&
ref(pct_string_view& s) noexcept
{
    return s.s_;
}

} // detail
#endif

//------------------------------------------------

/** Return a valid percent-encoded string

    If `s` is a valid percent-encoded string,
    the function returns the buffer as a valid
    view which may be used to perform decoding
    or measurements.
    Otherwise the result contains an error code.
    Upon success, the returned view references
    the original character buffer;
    Ownership is not transferred.

    @par Complexity
    Linear in `s.size()`.

    @par Exception Safety
    Throws nothing.

    @param s The string to validate.
*/
BOOST_URL_DECL
system::result<pct_string_view>
make_pct_string_view(
    core::string_view s) noexcept;

#ifndef BOOST_URL_DOCS
// VFALCO semi-private for now
inline
pct_string_view
make_pct_string_view_unsafe(
    char const* data,
    std::size_t size,
    std::size_t decoded_size) noexcept
{
#if 0
    BOOST_ASSERT(! make_pct_string_view(
        core::string_view(data, size)).has_error());
#endif
    return pct_string_view(
        data, size, decoded_size);
}
#endif

#ifndef BOOST_URL_DOCS
namespace detail {
template <>
inline
core::string_view
to_sv(pct_string_view const& s) noexcept
{
    return s.substr();
}
} // detail
#endif

} // urls
} // boost

#endif
