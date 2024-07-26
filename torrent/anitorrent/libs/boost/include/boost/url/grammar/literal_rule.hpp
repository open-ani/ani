//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_LITERAL_RULE_HPP
#define BOOST_URL_GRAMMAR_LITERAL_RULE_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/error_types.hpp>
#include <boost/core/detail/string_view.hpp>
#include <cstdlib>

namespace boost {
namespace urls {
namespace grammar {

/** Match a string literal exactly

    If there is no more input, or if the
    end of the input is reached, and a prefix
    of the literal matches exactly, the error
    returned is @ref error::need_more.

    @par Value Type
    @code
    using value_type = core::string_view;
    @endcode

    @par Example
    Rules are used with the function @ref parse.
    @code
    system::result< core::string_view > rv = parse( "HTTP", literal_rule( "HTTP" ) );
    @endcode

    @see
        @ref delim_rule,
        @ref parse.
*/
#ifdef BOOST_URL_DOCS
constexpr
__implementation_defined__
literal_rule( char const* s );
#else
class literal_rule
{
    char const* s_ = nullptr;
    std::size_t n_ = 0;

    constexpr
    static
    std::size_t
    len(char const* s) noexcept
    {
        return *s
            ? 1 + len(s + 1)
            : 0;
    }

public:
    using value_type = core::string_view;

    constexpr
    explicit
    literal_rule(
        char const* s) noexcept
        : s_(s)
        , n_(len(s))
    {
    }

    BOOST_URL_DECL
    system::result<value_type>
    parse(
        char const*& it,
        char const* end) const noexcept;
};
#endif

} // grammar
} // urls
} // boost

#endif
