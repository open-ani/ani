//
// Copyright (c) 2021 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_CHARSET_HPP
#define BOOST_URL_GRAMMAR_CHARSET_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/grammar/detail/charset.hpp>
#include <boost/static_assert.hpp>
#include <cstdint>
#include <type_traits>
#include <utility>

namespace boost {
namespace urls {
namespace grammar {

/** Alias for `std::true_type` if T satisfies <em>CharSet</em>.

    This metafunction determines if the
    type `T` meets these requirements of
    <em>CharSet</em>:

    @li An instance of `T` is invocable
    with this equivalent function signature:
    @code
    bool T::operator()( char ) const noexcept;
    @endcode

    @par Example
    Use with `enable_if` on the return value:
    @code
    template< class CharSet >
    typename std::enable_if< is_charset<T>::value >::type
    func( CharSet const& cs );
    @endcode

    @tparam T the type to check.
*/
#ifdef BOOST_URL_DOCS
template<class T>
using is_charset = __see_below__;
#else
template<class T, class = void>
struct is_charset : std::false_type {};

template<class T>
struct is_charset<T, void_t<
    decltype(
    std::declval<bool&>() =
        std::declval<T const&>().operator()(
            std::declval<char>())
            ) > > : std::true_type
{
};
#endif

//------------------------------------------------

/** Find the first character in the string that is in the set.

    @par Exception Safety
    Throws nothing.

    @return A pointer to the found character,
    otherwise the value `last`.

    @param first A pointer to the first character
    in the string to search.

    @param last A pointer to one past the last
    character in the string to search.

    @param cs The character set to use.

    @see
        @ref find_if_not.
*/
template<class CharSet>
char const*
find_if(
    char const* const first,
    char const* const last,
    CharSet const& cs) noexcept
{
    // If you get a compile error here
    // it means your type does not meet
    // the requirements. Please check the
    // documentation.
    static_assert(
        is_charset<CharSet>::value,
        "CharSet requirements not met");

    return detail::find_if(first, last, cs,
        detail::has_find_if<CharSet>{});
}

/** Find the first character in the string that is not in CharSet

    @par Exception Safety
    Throws nothing.

    @return A pointer to the found character,
    otherwise the value `last`.

    @param first A pointer to the first character
    in the string to search.

    @param last A pointer to one past the last
    character in the string to search.

    @param cs The character set to use.

    @see
        @ref find_if_not.
*/
template<class CharSet>
char const*
find_if_not(
    char const* const first,
    char const* const last,
    CharSet const& cs) noexcept
{
    // If you get a compile error here
    // it means your type does not meet
    // the requirements. Please check the
    // documentation.
    static_assert(
        is_charset<CharSet>::value,
        "CharSet requirements not met");

    return detail::find_if_not(first, last, cs,
        detail::has_find_if_not<CharSet>{});
}

//------------------------------------------------

#ifndef BOOST_URL_DOCS
namespace detail {

template<class CharSet>
struct charset_ref
{
    CharSet const& cs_;

    constexpr
    bool
    operator()(char ch) const noexcept
    {
        return cs_(ch);
    }

    char const*
    find_if(
        char const* first,
        char const* last) const noexcept
    {
        return grammar::find_if(
            first, last, cs_);
    }

    char const*
    find_if_not(
        char const* first,
        char const* last) const noexcept
    {
        return grammar::find_if_not(
            first, last, cs_ );
    }
};

} // detail
#endif

/** Return a reference to a character set

    This function returns a character set which
    references the specified object. This is
    used to reduce the number of bytes of
    storage (`sizeof`) required by a combinator
    when it stores a copy of the object.
    <br>
    Ownership of the object is not transferred;
    the caller is responsible for ensuring the
    lifetime of the object is extended until it
    is no longer referenced. For best results,
    `ref` should only be used with compile-time
    constants.
*/
template<class CharSet>
constexpr
#ifdef BOOST_URL_DOCS
__implementation_defined__
#else
typename std::enable_if<
    is_charset<CharSet>::value &&
    ! std::is_same<CharSet,
        detail::charset_ref<CharSet> >::value,
    detail::charset_ref<CharSet> >::type
#endif
ref(CharSet const& cs) noexcept
{
    return detail::charset_ref<
        CharSet>{cs};
}

} // grammar
} // urls
} // boost

#endif
