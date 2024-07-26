//
// Copyright (c) 2023 Alexander Grund
//
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_NOWIDE_QUOTED_HPP_INCLUDED
#define BOOST_NOWIDE_QUOTED_HPP_INCLUDED

#include <boost/nowide/config.hpp>
#include <boost/nowide/detail/is_path.hpp>
#include <boost/nowide/utf/convert.hpp>
#include <iomanip>
#include <istream>
#include <ostream>
#include <type_traits>

#if defined(__cpp_lib_quoted_string_io) && __cpp_lib_quoted_string_io >= 201304

namespace boost {
namespace nowide {
    /// \cond INTERNAL
    namespace detail {
        template<class Path>
        struct quoted;
        template<typename T>
        using remove_cvref_t = typename std::remove_cv<typename std::remove_reference<T>::type>::type;

    } // namespace detail
    /// \endcond

    /// \brief Allows insertion and extraction of `filesystem::path` into/from streams.
    ///
    /// When used in an expression such as `out << quoted(path)`, where `out` is an output stream,
    /// has the effect as-if `out << std::quoted(path.native())` was used.
    ///
    /// When used in an expression like `in >> quoted(path)`, where `in` is an input stream,
    /// has the effect as-if `in >> std::quoted(path.native())` was used if that would be valid.
    /// To that effect a temporary string is used, which on success is assigned to `path`.
    ///
    /// Will automatically convert between the streams `char_type` and `path::value_type` if neccessary.
    template<class Path>
#ifdef BOOST_NOWIDE_DOXYGEN
    unspecified_type
#else
    detail::enable_if_path_t<detail::remove_cvref_t<Path>, detail::quoted<Path&>>
#endif
    quoted(Path& path)
    {
        return {path};
    }

    /// \cond INTERNAL
    // Same but for const-refs and r-values
    template<class Path>
    detail::enable_if_path_t<detail::remove_cvref_t<Path>, detail::quoted<const Path&>> quoted(const Path& path)
    {
        return {path};
    }

    namespace detail {
        template<typename CharOut,
                 typename CharIn,
                 typename = typename std::enable_if<!std::is_same<CharOut, CharIn>::value>::type>
        std::basic_string<CharOut> maybe_convert_string(const std::basic_string<CharIn>& s)
        {
            return utf::convert_string<CharOut>(s);
        }
        template<typename Char>
        const std::basic_string<Char>& maybe_convert_string(const std::basic_string<Char>& s)
        {
            return s;
        }

        template<typename T>
        using requires_non_const =
          typename std::enable_if<!std::is_const<typename std::remove_reference<T>::type>::value>::type;

        template<class Path>
        struct quoted
        {
            Path value;
            template<typename CharType>
            friend std::basic_ostream<CharType>& operator<<(std::basic_ostream<CharType>& out, const quoted& path)
            {
                return out << std::quoted(maybe_convert_string<CharType>(path.value.native()));
            }

            template<typename CharType, class Path2 = Path, typename = requires_non_const<Path2>>
            friend std::basic_istream<CharType>& operator>>(std::basic_istream<CharType>& in, const quoted& path)
            {
                std::basic_string<CharType> value;
                using PlainPath = remove_cvref_t<Path>;
                if(in >> std::quoted(value))
                    path.value = PlainPath(maybe_convert_string<typename PlainPath::value_type>(value));
                return in;
            }
        };

    } // namespace detail
    /// \endcond
} // namespace nowide
} // namespace boost

#elif defined(BOOST_PRAGMA_MESSAGE)
BOOST_PRAGMA_MESSAGE("To use boost::nowide::quoted at least C++14 is required.")
#endif

#endif
