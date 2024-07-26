//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_GRAMMAR_IMPL_ERROR_HPP
#define BOOST_URL_GRAMMAR_IMPL_ERROR_HPP

#include <type_traits>

namespace boost {
namespace system {
template<>
struct is_error_code_enum<
    ::boost::urls::grammar::error>
{
    static bool const value = true;
};
template<>
struct is_error_condition_enum<
    ::boost::urls::grammar::condition>
{
    static bool const value = true;
};
} // system
} // boost

namespace boost {
namespace urls {
namespace grammar {

namespace detail {

struct BOOST_SYMBOL_VISIBLE
    error_cat_type
    : system::error_category
{
    BOOST_URL_DECL
    const char* name(
        ) const noexcept override;

    BOOST_URL_DECL
    std::string message(
        int) const override;

    BOOST_URL_DECL
    char const* message(
        int, char*, std::size_t
            ) const noexcept override;

    BOOST_URL_DECL
    system::error_condition
        default_error_condition(
            int code) const noexcept override;

    BOOST_SYSTEM_CONSTEXPR error_cat_type() noexcept
        : error_category(0x0536e50a30f9e9f2)
    {
    }
};

struct BOOST_SYMBOL_VISIBLE
    condition_cat_type
    : system::error_category
{
    BOOST_URL_DECL
    const char* name(
        ) const noexcept override;

    BOOST_URL_DECL
    std::string message(
        int) const override;

    BOOST_URL_DECL
    char const* message(
        int, char*, std::size_t
            ) const noexcept override;

    BOOST_SYSTEM_CONSTEXPR condition_cat_type()
        : error_category(0x0536e50a30f9e9f2)
    {
    }
};

BOOST_URL_DECL extern
    error_cat_type error_cat;

BOOST_URL_DECL extern
    condition_cat_type condition_cat;

} // detail

inline
system::error_code
make_error_code(
    error ev) noexcept
{
    return system::error_code{
        static_cast<std::underlying_type<
            error>::type>(ev),
                detail::error_cat};
}

inline
system::error_condition
make_error_condition(
    condition c) noexcept
{
    return system::error_condition{
        static_cast<std::underlying_type<
            condition>::type>(c),
                detail::condition_cat};
}

} // grammar
} // urls
} // boost

#endif
