//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_RANGE_RULE_HPP
#define BOOST_URL_GRAMMAR_RANGE_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/grammar/parse.hpp>
#include <boost/url/grammar/type_traits.hpp>
#include <boost/static_assert.hpp>
#include <cstddef>
#include <iterator>
#include <type_traits>

#include <stddef.h> // ::max_align_t

namespace boost {
namespace urls {
namespace grammar {

/** A forward range of parsed elements

    Objects of this type are forward ranges
    returned when parsing using the
    @ref range_rule.
    Iteration is performed by re-parsing the
    underlying character buffer. Ownership
    of the buffer is not transferred; the
    caller is responsible for ensuring that
    the lifetime of the buffer extends until
    it is no longer referenced by the range.

    @note

    The implementation may use temporary,
    recycled storage for type-erasure. Objects
    of type `range` are intended to be used
    ephemerally. That is, for short durations
    such as within a function scope. If it is
    necessary to store the range for a long
    period of time or with static storage
    duration, it is necessary to copy the
    contents to an object of a different type.

    @tparam T The value type of the range

    @see
        @ref parse,
        @ref range_rule.
*/
template<class T>
class range
{
    // buffer size for type-erased rule
    static constexpr
        std::size_t BufferSize = 128;

    struct small_buffer
    {
        alignas(alignof(::max_align_t))
        unsigned char buf[BufferSize];

        void const* addr() const noexcept
        {
            return buf;
        }

        void* addr() noexcept
        {
            return buf;
        }
    };

    small_buffer sb_;
    core::string_view s_;
    std::size_t n_ = 0;

    //--------------------------------------------

    struct any_rule;

    template<class R, bool>
    struct impl1;

    template<
        class R0, class R1, bool>
    struct impl2;

    template<
        class R0, class R1>
    friend struct range_rule_t;

    any_rule&
    get() noexcept
    {
        return *reinterpret_cast<
            any_rule*>(sb_.addr());
    }

    any_rule const&
    get() const noexcept
    {
        return *reinterpret_cast<
            any_rule const*>(
                sb_.addr());
    }

    template<class R>
    range(
        core::string_view s,
        std::size_t n,
        R const& r);

    template<
        class R0, class R1>
    range(
        core::string_view s,
        std::size_t n,
        R0 const& first,
        R1 const& next);

public:
    /** The type of each element of the range
    */
    using value_type = T;

    /** The type of each element of the range
    */
    using reference = T const&;

    /** The type of each element of the range
    */
    using const_reference = T const&;

    /** Provided for compatibility, unused
    */
    using pointer = void const*;

    /** The type used to represent unsigned integers
    */
    using size_type = std::size_t;

    /** The type used to represent signed integers
    */
    using difference_type = std::ptrdiff_t;

    /** A constant, forward iterator to elements of the range
    */
    class iterator;

    /** A constant, forward iterator to elements of the range
    */
    using const_iterator = iterator;

    /** Destructor
    */
    ~range();

    /** Constructor

        Default-constructed ranges have
        zero elements.

        @par Exception Safety
        Throws nothing.
    */
    range() noexcept;

    /** Constructor

        The new range references the
        same underlying character buffer.
        Ownership is not transferred; the
        caller is responsible for ensuring
        that the lifetime of the buffer
        extends until it is no longer
        referenced. The moved-from object
        becomes as if default-constructed.

        @par Exception Safety
        Throws nothing.
    */
    range(range&&) noexcept;

    /** Constructor

        The copy references the same
        underlying character buffer.
        Ownership is not transferred; the
        caller is responsible for ensuring
        that the lifetime of the buffer
        extends until it is no longer
        referenced.

        @par Exception Safety
        Throws nothing.
    */
    range(range const&) noexcept;

    /** Constructor

        After the move, this references the
        same underlying character buffer. Ownership
        is not transferred; the caller is responsible
        for ensuring that the lifetime of the buffer
        extends until it is no longer referenced.
        The moved-from object becomes as if
        default-constructed.

        @par Exception Safety
        Throws nothing.
    */
    range&
    operator=(range&&) noexcept;

    /** Assignment

        The copy references the same
        underlying character buffer.
        Ownership is not transferred; the
        caller is responsible for ensuring
        that the lifetime of the buffer
        extends until it is no longer
        referenced.

        @par Exception Safety
        Throws nothing.
    */
    range&
    operator=(range const&) noexcept;

    /** Return an iterator to the beginning
    */
    iterator begin() const noexcept;

    /** Return an iterator to the end
    */
    iterator end() const noexcept;

    /** Return true if the range is empty
    */
    bool
    empty() const noexcept
    {
        return n_ == 0;
    }

    /** Return the number of elements in the range
    */
    std::size_t
    size() const noexcept
    {
        return n_;
    }

    /** Return the matching part of the string
    */
    core::string_view
    string() const noexcept
    {
        return s_;
    }
};

//------------------------------------------------

#ifndef BOOST_URL_DOCS
template<
    class R0,
    class R1 = void>
struct range_rule_t;
#endif

//------------------------------------------------

/** Match a repeating number of elements

    Elements are matched using the passed rule.
    <br>
    Normally when the rule returns an error,
    the range ends and the input is rewound to
    one past the last character that matched
    successfully. However, if the rule returns
    the special value @ref error::end_of_range, the
    input is not rewound. This allows for rules
    which consume input without producing
    elements in the range. For example, to
    relax the grammar for a comma-delimited
    list by allowing extra commas in between
    elements.

    @par Value Type
    @code
    using value_type = range< typename Rule::value_type >;
    @endcode

    @par Example
    Rules are used with the function @ref parse.
    @code
    // range    = 1*( ";" token )

    system::result< range<core::string_view> > rv = parse( ";alpha;xray;charlie",
        range_rule(
            tuple_rule(
                squelch( delim_rule( ';' ) ),
                token_rule( alpha_chars ) ),
            1 ) );
    @endcode

    @par BNF
    @code
    range        = <N>*<M>next
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc5234#section-3.6"
        >3.6.  Variable Repetition (rfc5234)</a>

    @param next The rule to use for matching
    each element. The range extends until this
    rule returns an error.

    @param N The minimum number of elements for
    the range to be valid. If omitted, this
    defaults to zero.

    @param M The maximum number of elements for
    the range to be valid. If omitted, this
    defaults to unlimited.

    @see
        @ref alpha_chars,
        @ref delim_rule,
        @ref error::end_of_range,
        @ref parse,
        @ref range,
        @ref tuple_rule,
        @ref squelch.
*/
#ifdef BOOST_URL_DOCS
template<class Rule>
constexpr
__implementation_defined__
range_rule(
    Rule next,
    std::size_t N = 0,
    std::size_t M =
        std::size_t(-1)) noexcept;
#else
template<class R>
struct range_rule_t<R>
{
    using value_type =
        range<typename R::value_type>;

    system::result<value_type>
    parse(
        char const*& it,
        char const* end) const;

private:
    constexpr
    range_rule_t(
        R const& next,
        std::size_t N,
        std::size_t M) noexcept
        : next_(next)
        , N_(N)
        , M_(M)
    {
    }

    template<class R_>
    friend
    constexpr
    range_rule_t<R_>
    range_rule(
        R_ const& next,
        std::size_t N,
        std::size_t M) noexcept;

    R const next_;
    std::size_t N_;
    std::size_t M_;
};

template<class Rule>
constexpr
range_rule_t<Rule>
range_rule(
    Rule const& next,
    std::size_t N = 0,
    std::size_t M =
        std::size_t(-1)) noexcept
{
    // If you get a compile error here it
    // means that your rule does not meet
    // the type requirements. Please check
    // the documentation.
    static_assert(
        is_rule<Rule>::value,
        "Rule requirements not met");

    return range_rule_t<Rule>{
        next, N, M};
}
#endif

//------------------------------------------------

/** Match a repeating number of elements

    Two rules are used for match. The rule
    `first` is used for matching the first
    element, while the `next` rule is used
    to match every subsequent element.
    <br>
    Normally when the rule returns an error,
    the range ends and the input is rewound to
    one past the last character that matched
    successfully. However, if the rule returns
    the special value @ref error::end_of_range, the
    input is not rewound. This allows for rules
    which consume input without producing
    elements in the range. For example, to
    relax the grammar for a comma-delimited
    list by allowing extra commas in between
    elements.

    @par Value Type
    @code
    using value_type = range< typename Rule::value_type >;
    @endcode

    @par Example
    Rules are used with the function @ref parse.
    @code
    // range    = [ token ] *( "," token )

    system::result< range< core::string_view > > rv = parse( "whiskey,tango,foxtrot",
        range_rule(
            token_rule( alpha_chars ),          // first
            tuple_rule(                      // next
                squelch( delim_rule(',') ),
                token_rule( alpha_chars ) ) ) );
    @endcode

    @par BNF
    @code
    range       = <1>*<1>first
                / first <N-1>*<M-1>next
    @endcode

    @par Specification
    @li <a href="https://datatracker.ietf.org/doc/html/rfc5234#section-3.6"
        >3.6.  Variable Repetition (rfc5234)</a>

    @param first The rule to use for matching
    the first element. If this rule returns
    an error, the range is empty.

    @param next The rule to use for matching
    each subsequent element. The range extends
    until this rule returns an error.

    @param N The minimum number of elements for
    the range to be valid. If omitted, this
    defaults to zero.

    @param M The maximum number of elements for
    the range to be valid. If omitted, this
    defaults to unlimited.

    @see
        @ref alpha_chars,
        @ref delim_rule,
        @ref error::end_of_range,
        @ref parse,
        @ref range,
        @ref tuple_rule,
        @ref squelch.
*/
#ifdef BOOST_URL_DOCS
template<
    class Rule1, class Rule2>
constexpr
__implementation_defined__
range_rule(
    Rule1 first,
    Rule2 next,
    std::size_t N = 0,
    std::size_t M =
        std::size_t(-1)) noexcept;
#else
template<class R0, class R1>
struct range_rule_t
{
    using value_type =
        range<typename R0::value_type>;

    system::result<value_type>
    parse(
        char const*& it,
        char const* end) const;

private:
    constexpr
    range_rule_t(
        R0 const& first,
        R1 const& next,
        std::size_t N,
        std::size_t M) noexcept
        : first_(first)
        , next_(next)
        , N_(N)
        , M_(M)
    {
    }

    template<
        class R0_, class R1_>
    friend
    constexpr
    auto
    range_rule(
        R0_ const& first,
        R1_ const& next,
        std::size_t N,
        std::size_t M) noexcept ->
#if 1
            typename std::enable_if<
                ! std::is_integral<R1_>::value,
                range_rule_t<R0_, R1_>>::type;
#else
        range_rule_t<R0_, R1_>;
#endif

    R0 const first_;
    R1 const next_;
    std::size_t N_;
    std::size_t M_;
};

template<
    class Rule1, class Rule2>
constexpr
auto
range_rule(
    Rule1 const& first,
    Rule2 const& next,
    std::size_t N = 0,
    std::size_t M =
        std::size_t(-1)) noexcept ->
#if 1
    typename std::enable_if<
        ! std::is_integral<Rule2>::value,
        range_rule_t<Rule1, Rule2>>::type
#else
    range_rule_t<Rule1, Rule2>
#endif
{
    // If you get a compile error here it
    // means that your rule does not meet
    // the type requirements. Please check
    // the documentation.
    static_assert(
        is_rule<Rule1>::value,
        "Rule requirements not met");
    static_assert(
        is_rule<Rule2>::value,
        "Rule requirements not met");

    // If you get a compile error here it
    // means that your rules do not have
    // the exact same value_type. Please
    // check the documentation.
    static_assert(
        std::is_same<
            typename Rule1::value_type,
            typename Rule2::value_type>::value,
        "Rule requirements not met");

    return range_rule_t<Rule1, Rule2>{
        first, next, N, M};
}
#endif

} // grammar
} // urls
} // boost

#include <boost/url/grammar/impl/range_rule.hpp>

#endif
