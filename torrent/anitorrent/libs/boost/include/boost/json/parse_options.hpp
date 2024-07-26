//
// Copyright (c) 2020 Krystian Stasiowski (sdkrystian@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_PARSE_OPTIONS_HPP
#define BOOST_JSON_PARSE_OPTIONS_HPP

#include <boost/json/detail/config.hpp>
#include <iosfwd>

namespace boost {
namespace json {

/** Enumeration of number parsing modes

    These values are used to select the way to parse numbers.

    @see
        @ref parse_options,
        @ref basic_parser,
        @ref parser.
*/
enum class number_precision : unsigned char
{
    /// Fast, but potentially less precise mode.
    imprecise,

    /// Slower, but precise mode.
    precise,

    /// The fastest mode, that only validates encountered numbers without
    /// parsing them.
    none,
};

/** Parser options

    This structure is used for specifying
    maximum parsing depth, and whether
    to allow various non-standard extensions.
    Default-constructed options set maximum
    parsing depth to 32 and specify that only
    standard JSON is allowed,

    @see
        @ref basic_parser,
        @ref parser.
*/
struct parse_options
{
    /** Maximum nesting level of arrays and objects.

        This specifies the maximum number of nested
        structures allowed while parsing a JSON text. If
        this limit is exceeded during a parse, an
        error is returned.

        @see
            @ref basic_parser,
            @ref stream_parser.
    */
    std::size_t max_depth = 32;

    /** Number pasing mode

        This selects the way to parse numbers. The default is to parse them
        fast, but with possible slight imprecision for floating point numbers
        with larger mantissas. Users can also choose to parse numbers slower
        but with full precision. Or to not parse them at all, and only validate
        numbers. The latter mode is useful for @ref basic_parser instantiations
        that wish to treat numbers in a custom way.

        @see
            @ref basic_parser,
            @ref stream_parser.
    */
    number_precision numbers = number_precision::imprecise;

    /** Non-standard extension option

        Allow C and C++ style comments to appear
        anywhere that whitespace is permissible.

        @see
            @ref basic_parser,
            @ref stream_parser.
    */
    bool allow_comments = false;

    /** Non-standard extension option

        Allow a trailing comma to appear after
        the last element of any array or object.

        @see
            @ref basic_parser,
            @ref stream_parser.
    */
    bool allow_trailing_commas = false;

    /** Non-standard extension option

        Allow invalid UTF-8 sequnces to appear
        in keys and strings.

        @note This increases parsing performance.

        @see
            @ref basic_parser,
            @ref stream_parser.
    */
    bool allow_invalid_utf8 = false;

    /** Non-standard extension option

        Allow `Infinity`, `-Infinity`, and `NaN` JSON literals. These values
        are produced by some popular JSON implementations for positive
        infinity, negative infinity and NaN special numbers respectively.

        @see
            @ref basic_parser,
            @ref stream_parser.
    */
    bool allow_infinity_and_nan = false;

    /** Set JSON parse options on input stream.

        The function stores parse options in the private storage of the stream. If
        the stream fails to allocate necessary private storage, `badbit` will be
        set on it.

        @return Reference to `is`.

        @par Complexity
        Amortized constant (due to potential memory allocation by the stream).

        @par Exception Safety
        Strong guarantee.
        The stream may throw as configured by
        [`std::ios::exceptions`](https://en.cppreference.com/w/cpp/io/basic_ios/exceptions).

        @param is The input stream.

        @param opts The options to store.
    */
    BOOST_JSON_DECL
    friend
    std::istream&
    operator>>( std::istream& is, parse_options const& opts );
};

} // namespace json
} // namespace boost

#endif
