//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_ERROR_HPP
#define BOOST_JSON_IMPL_ERROR_HPP

#include <boost/system/error_category.hpp>
#include <type_traits>

namespace boost {
namespace system {
template<>
struct is_error_code_enum< ::boost::json::error >
{
    static bool const value = true;
};
template<>
struct is_error_condition_enum< ::boost::json::condition >
{
    static bool const value = true;
};
} // system
} // boost

namespace std {
template<>
struct is_error_code_enum< ::boost::json::error >
{
    static bool const value = true;
};
template<>
struct is_error_condition_enum< ::boost::json::condition >
{
    static bool const value = true;
};
} // std

namespace boost {
namespace json {
namespace detail {

struct error_code_category_t
    : system::error_category
{
    constexpr
    error_code_category_t()
        : system::error_category(0xB9A9B9922177C772)
    {}

    BOOST_JSON_DECL
    const char*
    name() const noexcept override;

    BOOST_JSON_DECL
    char const*
    message( int ev, char* buf, std::size_t len ) const noexcept override;

    BOOST_JSON_DECL
    std::string
    message( int ev ) const override;

    BOOST_JSON_DECL
    system::error_condition
    default_error_condition( int ev ) const noexcept override;
};

extern
BOOST_JSON_DECL
error_code_category_t error_code_category;

struct error_condition_category_t
    : system::error_category
{
    constexpr
    error_condition_category_t()
        : system::error_category(0x37CEF5A036D24FD1)
    {}

    BOOST_JSON_DECL
    const char*
    name() const noexcept override;

    BOOST_JSON_DECL
    char const*
    message( int ev, char*, std::size_t ) const noexcept override;

    BOOST_JSON_DECL
    std::string
    message( int cv ) const override;
};

extern
BOOST_JSON_DECL
error_condition_category_t error_condition_category;

} // namespace detail

inline
BOOST_SYSTEM_CONSTEXPR
system::error_code
make_error_code(error e) noexcept
{

    return system::error_code(
        static_cast<std::underlying_type<error>::type>(e),
        detail::error_code_category );
}

inline
BOOST_SYSTEM_CONSTEXPR
system::error_condition
make_error_condition(condition c) noexcept
{
    return system::error_condition(
        static_cast<std::underlying_type<condition>::type>(c),
        detail::error_condition_category );
}

} // namespace json
} // namespace boost

#endif
