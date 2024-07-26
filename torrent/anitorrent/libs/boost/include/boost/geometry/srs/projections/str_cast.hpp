// Boost.Geometry

// Copyright (c) 2018-2023, Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_SRS_PROJECTIONS_STR_CAST_HPP
#define BOOST_GEOMETRY_SRS_PROJECTIONS_STR_CAST_HPP

#include <boost/config.hpp>
#include <boost/geometry/core/exception.hpp>
#include <boost/throw_exception.hpp>
#include <boost/type_traits/remove_cv.hpp>

namespace boost { namespace geometry
{

class bad_str_cast : public geometry::exception
{
    char const* what() const noexcept override
    {
        return "Unable to convert from string.";
    }
};

#ifndef DOXYGEN_NO_DETAIL
namespace detail
{

template
<
    typename T,
    bool IsIntegral = std::is_integral<T>::value,
    bool IsSigned = std::is_signed<T>::value
>
struct str_cast_traits_strtox
{
    static inline T apply(const char *str, char **str_end)
    {
        return strtod(str, str_end);
    }
};

template <typename T>
struct str_cast_traits_strtox<T, true, true>
{
    static inline T apply(const char *str, char **str_end)
    {
        return strtol(str, str_end, 0);
    }
};

template <typename T>
struct str_cast_traits_strtox<T, true, false>
{
    static inline T apply(const char *str, char **str_end)
    {
        return strtoul(str, str_end, 0);
    }
};

template <typename T>
struct str_cast_traits_strtox<T, false, false>
{
    static inline T apply(const char *str, char **str_end)
    {
        return strtod(str, str_end);
    }
};

template <>
struct str_cast_traits_strtox<long long, true, true>
{
    static inline long long apply(const char *str, char **str_end)
    {
        return strtoll(str, str_end, 0);
    }
};

template <>
struct str_cast_traits_strtox<unsigned long long, true, false>
{
    static inline unsigned long long apply(const char *str, char **str_end)
    {
        return strtoull(str, str_end, 0);
    }
};

template <>
struct str_cast_traits_strtox<float, false, false>
{
    static inline float apply(const char *str, char **str_end)
    {
        return strtof(str, str_end);
    }
};

template <>
struct str_cast_traits_strtox<long double, false, false>
{
    static inline long double apply(const char *str, char **str_end)
    {
        return strtold(str, str_end);
    }
};

template <typename T>
struct str_cast_traits_generic
{
    static inline T apply(const char *str)
    {
        char * str_end = (char*)(void*)str;
        T res = str_cast_traits_strtox
                    <
                        typename boost::remove_cv<T>::type
                    >::apply(str, &str_end);
        if (str_end == str)
        {
            BOOST_THROW_EXCEPTION( bad_str_cast() );
        }
        return res;
    }
};

} // namespace detail
#endif // DOXYGEN_NO_DETAIL

template <typename T>
struct str_cast_traits
{
    template <typename String>
    static inline T apply(String const& str)
    {
        return detail::str_cast_traits_generic<T>::apply(str.c_str());
    }
};

template <typename T, typename String>
inline T str_cast(String const& str)
{
    return str_cast_traits<T>::apply(str);
}


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_SRS_PROJECTIONS_STR_CAST_HPP
