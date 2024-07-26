//
// Copyright (c) 2021 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_LUT_CHARS_HPP
#define BOOST_URL_GRAMMAR_LUT_CHARS_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/grammar/detail/charset.hpp>
#include <cstdint>
#include <type_traits>

// Credit to Peter Dimov for ideas regarding
// SIMD constexpr, and character set masks.

namespace boost {
namespace urls {
namespace grammar {

#ifndef BOOST_URL_DOCS
namespace detail {
template<class T, class = void>
struct is_pred : std::false_type {};

template<class T>
struct is_pred<T, void_t<
    decltype(
    std::declval<bool&>() =
        std::declval<T const&>().operator()(
            std::declval<char>())
            ) > > : std::true_type
{
};
} // detail
#endif

/** A set of characters

    The characters defined by instances of
    this set are provided upon construction.
    The `constexpr` implementation allows
    these to become compile-time constants.

    @par Example
    Character sets are used with rules and the
    functions @ref find_if and @ref find_if_not.
    @code
    constexpr lut_chars vowel_chars = "AEIOU" "aeiou";

    system::result< core::string_view > rv = parse( "Aiea", token_rule( vowel_chars ) );
    @endcode

    @see
        @ref find_if,
        @ref find_if_not,
        @ref parse,
        @ref token_rule.
*/
class lut_chars
{
    std::uint64_t mask_[4] = {};

    constexpr
    static
    std::uint64_t
    lo(char c) noexcept
    {
        return static_cast<
            unsigned char>(c) & 3;
    }

    constexpr
    static
    std::uint64_t
    hi(char c) noexcept
    {
        return 1ULL << (static_cast<
            unsigned char>(c) >> 2);
    }

    constexpr
    static
    lut_chars
    construct(
        char const* s) noexcept
    {
        return *s
            ? lut_chars(*s) +
                construct(s+1)
            : lut_chars();
    }

    constexpr
    static
    lut_chars
    construct(
        unsigned char ch,
        bool b) noexcept
    {
        return b
            ? lut_chars(ch)
            : lut_chars();
    }

    template<class Pred>
    constexpr
    static
    lut_chars
    construct(
        Pred pred,
        unsigned char ch) noexcept
    {
        return ch == 255
            ? construct(ch, pred(static_cast<char>(ch)))
            : construct(ch, pred(static_cast<char>(ch))) +
                construct(pred, ch + 1);
    }

    constexpr
    lut_chars() = default;

    constexpr
    lut_chars(
        std::uint64_t m0,
        std::uint64_t m1,
        std::uint64_t m2,
        std::uint64_t m3) noexcept
        : mask_{ m0, m1, m2, m3 }
    {
    }

public:
    /** Constructor

        This function constructs a character
        set which has as a single member,
        the character `ch`.

        @par Example
        @code
        constexpr lut_chars asterisk( '*' );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param ch A character.
    */
    constexpr
    lut_chars(char ch) noexcept
        : mask_ {
            lo(ch) == 0 ? hi(ch) : 0,
            lo(ch) == 1 ? hi(ch) : 0,
            lo(ch) == 2 ? hi(ch) : 0,
            lo(ch) == 3 ? hi(ch) : 0 }
    {
    }

    /** Constructor

        This function constructs a character
        set which has as members, all of the
        characters present in the null-terminated
        string `s`.

        @par Example
        @code
        constexpr lut_chars digits = "0123456789";
        @endcode

        @par Complexity
        Linear in `::strlen(s)`, or constant
        if `s` is a constant expression.

        @par Exception Safety
        Throws nothing.

        @param s A null-terminated string.
    */
    constexpr
    lut_chars(
        char const* s) noexcept
        : lut_chars(construct(s))
    {
    }

    /** Constructor.

        This function constructs a character
        set which has as members, every value
        of `char ch` for which the expression
        `pred(ch)` returns `true`.

        @par Example
        @code
        struct is_digit
        {
            constexpr bool
            operator()(char c ) const noexcept
            {
                return c >= '0' && c <= '9';
            }
        };

        constexpr lut_chars digits( is_digit{} );
        @endcode

        @par Complexity
        Linear in `pred`, or constant if
        `pred(ch)` is a constant expression.

        @par Exception Safety
        Throws nothing.

        @param pred The function object to
        use for determining membership in
        the character set.
    */
    template<class Pred
#ifndef BOOST_URL_DOCS
        ,class = typename std::enable_if<
            detail::is_pred<Pred>::value &&
        ! std::is_base_of<
            lut_chars, Pred>::value>::type
#endif
    >
    constexpr
    lut_chars(Pred const& pred) noexcept
        : lut_chars(
            construct(pred, 0))
    {
    }

    /** Return true if ch is in the character set.

        This function returns true if the
        character `ch` is in the set, otherwise
        it returns false.

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param ch The character to test.
    */
    constexpr
    bool
    operator()(
        unsigned char ch) const noexcept
    {
        return operator()(static_cast<char>(ch));
    }

    /// @copydoc operator()(unsigned char) const
    constexpr
    bool
    operator()(char ch) const noexcept
    {
        return mask_[lo(ch)] & hi(ch);
    }

    /** Return the union of two character sets.

        This function returns a new character
        set which contains all of the characters
        in `cs0` as well as all of the characters
        in `cs`.

        @par Example
        This creates a character set which
        includes all letters and numbers
        @code
        constexpr lut_chars alpha_chars(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            "abcdefghijklmnopqrstuvwxyz");

        constexpr lut_chars alnum_chars = alpha_chars + "0123456789";
        @endcode

        @par Complexity
        Constant.

        @return The new character set.

        @param cs0 A character to join
        
        @param cs1 A character to join
    */
    friend
    constexpr
    lut_chars
    operator+(
        lut_chars const& cs0,
        lut_chars const& cs1) noexcept
    {
        return lut_chars(
            cs0.mask_[0] | cs1.mask_[0],
            cs0.mask_[1] | cs1.mask_[1],
            cs0.mask_[2] | cs1.mask_[2],
            cs0.mask_[3] | cs1.mask_[3]);
    }

    /** Return a new character set by subtracting

        This function returns a new character
        set which is formed from all of the
        characters in `cs0` which are not in `cs`.

        @par Example
        This statement declares a character set
        containing all the lowercase letters
        which are not vowels:
        @code
        constexpr lut_chars consonants = lut_chars("abcdefghijklmnopqrstuvwxyz") - "aeiou";
        @endcode

        @par Complexity
        Constant.

        @return The new character set.

        @param cs0 A character set to join.
        
        @param cs1 A character set to join.
    */
    friend
    constexpr
    lut_chars
    operator-(
        lut_chars const& cs0,
        lut_chars const& cs1) noexcept
    {
        return lut_chars(
            cs0.mask_[0] & ~cs1.mask_[0],
            cs0.mask_[1] & ~cs1.mask_[1],
            cs0.mask_[2] & ~cs1.mask_[2],
            cs0.mask_[3] & ~cs1.mask_[3]);
    }

    /** Return a new character set which is the complement of another character set.

        This function returns a new character
        set which contains all of the characters
        that are not in `*this`.

        @par Example
        This statement declares a character set
        containing everything but vowels:
        @code
        constexpr lut_chars not_vowels = ~lut_chars( "AEIOU" "aeiou" );
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @return The new character set.
    */
    constexpr
    lut_chars
    operator~() const noexcept
    {
        return lut_chars(
            ~mask_[0],
            ~mask_[1],
            ~mask_[2],
            ~mask_[3]
        );
    }

#ifndef BOOST_URL_DOCS
#ifdef BOOST_URL_USE_SSE2
    char const*
    find_if(
        char const* first,
        char const* last) const noexcept
    {
        return detail::find_if_pred(
            *this, first, last);
    }

    char const*
    find_if_not(
        char const* first,
        char const* last) const noexcept
    {
        return detail::find_if_not_pred(
            *this, first, last);
    }
#endif
#endif
};

} // grammar
} // urls
} // boost

#endif
