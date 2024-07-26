//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_CI_STRING_HPP
#define BOOST_URL_GRAMMAR_CI_STRING_HPP

#include <boost/url/detail/config.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/grammar/detail/ci_string.hpp>
#include <cstdlib>

namespace boost {
namespace urls {
namespace grammar {

// Algorithms for interacting with low-ASCII
// characters and strings, for implementing
// semantics in RFCs. These routines do not
// use std::locale.

//------------------------------------------------

/** Return c converted to lowercase

    This function returns the character,
    converting it to lowercase if it is
    uppercase.
    The function is defined only for
    low-ASCII characters.

    @par Example
    @code
    assert( to_lower( 'A' ) == 'a' );
    @endcode

    @par Exception Safety
    Throws nothing.

    @return The converted character

    @param c The character to convert

    @see
        @ref to_upper.
*/
constexpr
char
to_lower(char c) noexcept
{
    return detail::to_lower(c);
}

/** Return c converted to uppercase

    This function returns the character,
    converting it to uppercase if it is
    lowercase.
    The function is defined only for
    low-ASCII characters.

    @par Example
    @code
    assert( to_upper( 'a' ) == 'A' );
    @endcode

    @par Exception Safety
    Throws nothing.

    @return The converted character

    @param c The character to convert

    @see
        @ref to_lower.
*/
constexpr
char
to_upper(char c) noexcept
{
    return detail::to_upper(c);
}

//------------------------------------------------

/** Return the case-insensitive comparison of s0 and s1

    This returns the lexicographical comparison
    of two strings, ignoring case.
    The function is defined only for strings
    containing low-ASCII characters.

    @par Example
    @code
    assert( ci_compare( "boost", "Boost" ) == 0 );
    @endcode

    @par Exception Safety
    Throws nothing.

    @return 0 if the strings are equal, -1 if
    `s0` is less than `s1`, or 1 if `s0` is
    greater than s1.

    @param s0 The first string

    @param s1 The second string

    @see
        @ref ci_is_equal,
        @ref ci_is_less.
*/
BOOST_URL_DECL
int
ci_compare(
    core::string_view s0,
    core::string_view s1) noexcept;

/** Return the case-insensitive digest of a string

    The hash function is non-cryptographic and
    not hardened against algorithmic complexity
    attacks.
    Returned digests are suitable for usage in
    unordered containers.
    The function is defined only for strings
    containing low-ASCII characters.

    @return The digest

    @param s The string
*/
BOOST_URL_DECL
std::size_t
ci_digest(
    core::string_view s) noexcept;

//------------------------------------------------

/** Return true if s0 equals s1 using case-insensitive comparison

    The function is defined only for strings
    containing low-ASCII characters.

    @par Example
    @code
    assert( ci_is_equal( "Boost", "boost" ) );
    @endcode

    @see
        @ref ci_compare,
        @ref ci_is_less.
*/
#ifdef BOOST_URL_DOCS
template<
    class String0,
    class String1>
bool
ci_is_equal(
    String0 const& s0,
    String1 const& s1);
#else

template<
    class String0,
    class String1>
auto
ci_is_equal(
    String0 const& s0,
    String1 const& s1) ->
        typename std::enable_if<
            ! std::is_convertible<
                String0, core::string_view>::value ||
            ! std::is_convertible<
                String1, core::string_view>::value,
        bool>::type
{
    // this overload supports forward iterators and
    // does not assume the existence core::string_view::size
    if( detail::type_id<String0>() >
        detail::type_id<String1>())
        return detail::ci_is_equal(s1, s0);
    return detail::ci_is_equal(s0, s1);
}

inline
bool
ci_is_equal(
    core::string_view s0,
    core::string_view s1) noexcept
{
    // this overload is faster as it makes use of
    // core::string_view::size
    if(s0.size() != s1.size())
        return false;
    return detail::ci_is_equal(s0, s1);
}
#endif

/** Return true if s0 is less than s1 using case-insensitive comparison 

    The comparison algorithm implements a
    case-insensitive total order on the set
    of all strings; however, it is not a
    lexicographical comparison.
    The function is defined only for strings
    containing low-ASCII characters.

    @par Example
    @code
    assert( ! ci_is_less( "Boost", "boost" ) );
    @endcode

    @see
        @ref ci_compare,
        @ref ci_is_equal.
*/
inline
bool
ci_is_less(
    core::string_view s0,
    core::string_view s1) noexcept
{
    if(s0.size() != s1.size())
        return s0.size() < s1.size();
    return detail::ci_is_less(s0, s1);
}

//------------------------------------------------

/** A case-insensitive hash function object for strings

    The hash function is non-cryptographic and
    not hardened against algorithmic complexity
    attacks.
    This is a suitable hash function for
    unordered containers.
    The function is defined only for strings
    containing low-ASCII characters.

    @par Example
    @code
    boost::unordered_map< std::string, std::string, ci_hash, ci_equal > m1;

    std::unordered_map  < std::string, std::string, ci_hash, ci_equal > m2; // (since C++20)
    @endcode

    @see
        @ref ci_equal,
        @ref ci_less.
*/
#ifdef BOOST_URL_DOCS
using ci_hash = __see_below__;
#else
struct ci_hash
{
    using is_transparent = void;

    std::size_t
    operator()(
        core::string_view s) const noexcept
    {
        return ci_digest(s);
    }
};
#endif

/** A case-insensitive equals predicate for strings

    The function object returns `true` when
    two strings are equal, ignoring case.
    This is a suitable equality predicate for
    unordered containers.
    The function is defined only for strings
    containing low-ASCII characters.

    @par Example
    @code
    boost::unordered_map< std::string, std::string, ci_hash, ci_equal > m1;

    std::unordered_map  < std::string, std::string, ci_hash, ci_equal > m2; // (since C++20)
    @endcode

    @see
        @ref ci_hash,
        @ref ci_less.
*/
#ifdef BOOST_URL_DOCS
using ci_equal = __see_below__;
#else
struct ci_equal
{
    using is_transparent = void;

    template<
        class String0, class String1>
    bool
    operator()(
        String0 s0,
        String1 s1) const noexcept
    {
        return ci_is_equal(s0, s1);
    }
};
#endif

/** A case-insensitive less predicate for strings

    The comparison algorithm implements a
    case-insensitive total order on the set
    of all ASCII strings; however, it is
    not a lexicographical comparison.
    This is a suitable predicate for
    ordered containers.
    The function is defined only for strings
    containing low-ASCII characters.

    @par Example
    @code
    boost::container::map< std::string, std::string, ci_less > m1;

    std::map< std::string, std::string, ci_less > m2; // (since C++14)
    @endcode

    @see
        @ref ci_equal,
        @ref ci_hash.
*/
#ifdef BOOST_URL_DOCS
using ci_less = __see_below__;
#else
struct ci_less
{
    using is_transparent = void;

    std::size_t
    operator()(
        core::string_view s0,
        core::string_view s1) const noexcept
    {
        return ci_is_less(s0, s1);
    }
};
#endif

} // grammar
} // urls
} // boost

#endif
