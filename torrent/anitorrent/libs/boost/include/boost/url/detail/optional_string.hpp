//
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_DETAIL_OPTIONAL_STRING_HPP
#define BOOST_URL_DETAIL_OPTIONAL_STRING_HPP

#include <boost/url/detail/string_view.hpp>
#include <boost/core/detail/string_view.hpp>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
struct no_value_t;
#endif

namespace detail {
struct optional_string
{
    core::string_view s;
    bool b = false;
};

template <class String>
typename std::enable_if<
    std::is_convertible<String, core::string_view>::value,
    optional_string>::type
get_optional_string(
    String const& s)
{
    optional_string r;
    r.s = s;
    r.b = true;
    return r;
}

template <class T, class = void>
struct is_dereferenceable : std::false_type
{};

template <class T>
struct is_dereferenceable<
    T,
    void_t<
        decltype(*std::declval<T>())
        >> : std::true_type
{};

template <class OptionalString>
typename std::enable_if<
    !std::is_convertible<OptionalString, core::string_view>::value,
    optional_string>::type
get_optional_string(
    OptionalString const& opt)
{
    // If this goes off, it means the rule
    // passed in did not meet the requirements.
    // Please check the documentation of functions
    // that call get_optional_string.
    static_assert(
        is_dereferenceable<OptionalString>::value &&
        std::is_constructible<bool, OptionalString>::value &&
        !std::is_convertible<OptionalString, core::string_view>::value &&
        std::is_convertible<typename std::decay<decltype(*std::declval<OptionalString>())>::type, core::string_view>::value,
        "OptionalString requirements not met");
    optional_string r;
    r.s = opt ? detail::to_sv(*opt) : core::string_view{};
    r.b = static_cast<bool>(opt);
    return r;
}

inline
optional_string
get_optional_string(
    std::nullptr_t)
{
    return {};
}

inline
optional_string
get_optional_string(
    no_value_t const&)
{
    return {};
}


} // detail
} // urls
} // boost

#endif
