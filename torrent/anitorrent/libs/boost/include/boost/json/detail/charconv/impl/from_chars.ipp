// Copyright 2022 Peter Dimov
// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

// https://stackoverflow.com/questions/38060411/visual-studio-2015-wont-suppress-error-c4996
#ifndef _SCL_SECURE_NO_WARNINGS
# define _SCL_SECURE_NO_WARNINGS
#endif
#ifndef NO_WARN_MBCS_MFC_DEPRECATION
# define NO_WARN_MBCS_MFC_DEPRECATION
#endif

#include <boost/json/detail/charconv/detail/fast_float/fast_float.hpp>
#include <boost/json/detail/charconv/detail/from_chars_float_impl.hpp>
#include <boost/json/detail/charconv/from_chars.hpp>
#include <system_error>
#include <string>
#include <cstdlib>
#include <cerrno>
#include <cstring>

#if defined(__GNUC__) && __GNUC__ < 5
# pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif

std::errc boost::json::detail::charconv::detail::errno_to_errc(int errno_value) noexcept
{
    switch (errno_value)
    {
        case EINVAL:
            return std::errc::invalid_argument;
        case ERANGE:
            return std::errc::result_out_of_range;
        default:
            return std::errc();
    }
}

boost::json::detail::charconv::from_chars_result boost::json::detail::charconv::from_chars(const char* first, const char* last, double& value, boost::json::detail::charconv::chars_format fmt) noexcept
{
    if (fmt != boost::json::detail::charconv::chars_format::hex)
    {
        return boost::json::detail::charconv::detail::fast_float::from_chars(first, last, value, fmt);
    }
    return boost::json::detail::charconv::detail::from_chars_float_impl(first, last, value, fmt);
}
