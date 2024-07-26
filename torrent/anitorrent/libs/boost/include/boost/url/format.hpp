//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_FORMAT_HPP
#define BOOST_URL_FORMAT_HPP

#include <boost/url/detail/config.hpp>
#include <boost/core/detail/string_view.hpp>
#include <boost/url/url.hpp>
#include <boost/url/detail/vformat.hpp>
#include <initializer_list>

namespace boost {
namespace urls {

/** Format arguments into a URL

    Format arguments according to the format
    URL string into a @ref url.

    The rules for a format URL string are the same
    as for a `std::format_string`, where replacement
    fields are delimited by curly braces.

    The URL components to which replacement fields
    belong are identified before replacement is
    applied and any invalid characters for that
    formatted argument are percent-escaped.

    Hence, the delimiters between URL components,
    such as `:`, `//`, `?`, and `#`, should be
    included in the URL format string. Likewise,
    a format string with a single `"{}"` is
    interpreted as a path and any replacement
    characters invalid in this component will be
    encoded to form a valid URL.

    @par Example
    @code
    assert(format("{}", "Hello world!").buffer() == "Hello%20world%21");
    @endcode

    @par Preconditions
    All replacement fields must be valid and the
    resulting URL should be valid after arguments
    are formatted into the URL.

    Because any invalid characters for a URL
    component are encoded by this function, only
    replacements in the scheme and port components
    might be invalid, as these components do not
    allow percent-encoding of arbitrary
    characters.

    @return A URL holding the formatted result.

    @param fmt The format URL string.
    @param args Arguments to be formatted.

    @throws system_error
    `fmt` contains an invalid format string and
    the result contains an invalid URL after
    replacements are applied.

    @par BNF
    @code
    replacement_field ::=  "{" [arg_id] [":" (format_spec | chrono_format_spec)] "}"
    arg_id            ::=  integer | identifier
    integer           ::=  digit+
    digit             ::=  "0"..."9"
    identifier        ::=  id_start id_continue*
    id_start          ::=  "a"..."z" | "A"..."Z" | "_"
    id_continue       ::=  id_start | digit
    @endcode

    @par Specification
    @li <a href="https://fmt.dev/latest/syntax.html"
        >Format String Syntax</a>

    @see
        @ref format_to.

*/
template <class... Args>
url
format(
    core::string_view fmt,
    Args&&... args)
{
    return detail::vformat(
        fmt, detail::make_format_args(
            std::forward<Args>(args)...));
}

/** Format arguments into a URL

    Format arguments according to the format
    URL string into a @ref url_base.

    The rules for a format URL string are the same
    as for a `std::format_string`, where replacement
    fields are delimited by curly braces.

    The URL components to which replacement fields
    belong are identified before replacement is
    applied and any invalid characters for that
    formatted argument are percent-escaped.

    Hence, the delimiters between URL components,
    such as `:`, `//`, `?`, and `#`, should be
    included in the URL format string. Likewise,
    a format string with a single `"{}"` is
    interpreted as a path and any replacement
    characters invalid in this component will be
    encoded to form a valid URL.

    @par Example
    @code
    static_url<30> u;
    format(u, "{}", "Hello world!");
    assert(u.buffer() == "Hello%20world%21");
    @endcode

    @par Preconditions
    All replacement fields must be valid and the
    resulting URL should be valid after arguments
    are formatted into the URL.

    Because any invalid characters for a URL
    component are encoded by this function, only
    replacements in the scheme and port components
    might be invalid, as these components do not
    allow percent-encoding of arbitrary
    characters.

    @par Exception Safety
    Strong guarantee.

    @param u An object that derives from @ref url_base.
    @param fmt The format URL string.
    @param args Arguments to be formatted.

    @throws system_error
    `fmt` contains an invalid format string and
    `u` contains an invalid URL after replacements
    are applied.

    @par BNF
    @code
    replacement_field ::=  "{" [arg_id] [":" (format_spec | chrono_format_spec)] "}"
    arg_id            ::=  integer | identifier
    integer           ::=  digit+
    digit             ::=  "0"..."9"
    identifier        ::=  id_start id_continue*
    id_start          ::=  "a"..."z" | "A"..."Z" | "_"
    id_continue       ::=  id_start | digit
    @endcode

    @par Specification
    @li <a href="https://fmt.dev/latest/syntax.html"
        >Format String Syntax</a>

    @see
        @ref format.

*/
template <class... Args>
void
format_to(
    url_base& u,
    core::string_view fmt,
    Args&&... args)
{
    detail::vformat_to(
        u, fmt, detail::make_format_args(
            std::forward<Args>(args)...));
}

/** Format arguments into a URL

    Format arguments according to the format
    URL string into a @ref url.

    This overload allows type-erased arguments
    to be passed as an initializer_list, which
    is mostly convenient for named parameters.

    All arguments must be convertible to a
    implementation defined type able to store a
    type-erased reference to any valid format
    argument.

    The rules for a format URL string are the same
    as for a `std::format_string`, where replacement
    fields are delimited by curly braces.

    The URL components to which replacement fields
    belong are identified before replacement is
    applied and any invalid characters for that
    formatted argument are percent-escaped.

    Hence, the delimiters between URL components,
    such as `:`, `//`, `?`, and `#`, should be
    included in the URL format string. Likewise,
    a format string with a single `"{}"` is
    interpreted as a path and any replacement
    characters invalid in this component will be
    encoded to form a valid URL.

    @par Example
    @code
    assert(format("user/{id}", {{"id", 1}}).buffer() == "user/1");
    @endcode

    @par Preconditions
    All replacement fields must be valid and the
    resulting URL should be valid after arguments
    are formatted into the URL.

    Because any invalid characters for a URL
    component are encoded by this function, only
    replacements in the scheme and port components
    might be invalid, as these components do not
    allow percent-encoding of arbitrary
    characters.

    @return A URL holding the formatted result.

    @param fmt The format URL string.
    @param args Arguments to be formatted.

    @throws system_error
    `fmt` contains an invalid format string and
    the result contains an invalid URL after
    replacements are applied.

    @par BNF
    @code
    replacement_field ::=  "{" [arg_id] [":" (format_spec | chrono_format_spec)] "}"
    arg_id            ::=  integer | identifier
    integer           ::=  digit+
    digit             ::=  "0"..."9"
    identifier        ::=  id_start id_continue*
    id_start          ::=  "a"..."z" | "A"..."Z" | "_"
    id_continue       ::=  id_start | digit
    @endcode

    @par Specification
    @li <a href="https://fmt.dev/latest/syntax.html"
        >Format String Syntax</a>

    @see
        @ref format_to.

*/
inline
url
format(
    core::string_view fmt,
#ifdef BOOST_URL_DOCS
    std::initializer_list<__see_below__> args
#else
    std::initializer_list<detail::format_arg> args
#endif
    )
{
    return detail::vformat(
        fmt, detail::format_args(
            args.begin(), args.end()));
}

/** Format arguments into a URL

    Format arguments according to the format
    URL string into a @ref url_base.

    This overload allows type-erased arguments
    to be passed as an initializer_list, which
    is mostly convenient for named parameters.

    All arguments must be convertible to a
    implementation defined type able to store a
    type-erased reference to any valid format
    argument.

    The rules for a format URL string are the same
    as for a `std::format_string`, where replacement
    fields are delimited by curly braces.

    The URL components to which replacement fields
    belong are identified before replacement is
    applied and any invalid characters for that
    formatted argument are percent-escaped.

    Hence, the delimiters between URL components,
    such as `:`, `//`, `?`, and `#`, should be
    included in the URL format string. Likewise,
    a format string with a single `"{}"` is
    interpreted as a path and any replacement
    characters invalid in this component will be
    encoded to form a valid URL.

    @par Example
    @code
    static_url<30> u;
    format_to(u, "user/{id}", {{"id", 1}})
    assert(u.buffer() == "user/1");
    @endcode

    @par Preconditions
    All replacement fields must be valid and the
    resulting URL should be valid after arguments
    are formatted into the URL.

    Because any invalid characters for a URL
    component are encoded by this function, only
    replacements in the scheme and port components
    might be invalid, as these components do not
    allow percent-encoding of arbitrary
    characters.

    @par Exception Safety
    Strong guarantee.

    @param u An object that derives from @ref url_base.
    @param fmt The format URL string.
    @param args Arguments to be formatted.

    @throws system_error
    `fmt` contains an invalid format string and
    `u` contains an invalid URL after replacements
    are applied.

    @par BNF
    @code
    replacement_field ::=  "{" [arg_id] [":" (format_spec | chrono_format_spec)] "}"
    arg_id            ::=  integer | identifier
    integer           ::=  digit+
    digit             ::=  "0"..."9"
    identifier        ::=  id_start id_continue*
    id_start          ::=  "a"..."z" | "A"..."Z" | "_"
    id_continue       ::=  id_start | digit
    @endcode

    @par Specification
    @li <a href="https://fmt.dev/latest/syntax.html"
        >Format String Syntax</a>

    @see
        @ref format.

*/
inline
void
format_to(
    url_base& u,
    core::string_view fmt,
#ifdef BOOST_URL_DOCS
    std::initializer_list<__see_below__> args
#else
    std::initializer_list<detail::format_arg> args
#endif
    )
{
    detail::vformat_to(
        u, fmt, detail::format_args(
            args.begin(), args.end()));
}

/** Designate a named argument for a replacement field

    Construct a named argument for a format URL
    string that contains named replacement fields.

    The function parameters should be convertible
    to an implementation defined type able to
    store the name and a reference to any type
    potentially used as a format argument.

    @par Example
    @code
    assert(format("user/{id}", arg("id", 1)).buffer() == "user/1");
    @endcode

    @return An temporary object with reference
    semantics for a named argument

    @param name The argument name
    @param arg The argument value

    @see
        @ref format,
        @ref format_to.

*/
template <class T>
#ifdef BOOST_URL_DOCS
__implementation_defined__
#else
detail::named_arg<T>
#endif
arg(core::string_view name, T const& arg)
{
    return {name, arg};
}

} // url
} // boost

#endif
