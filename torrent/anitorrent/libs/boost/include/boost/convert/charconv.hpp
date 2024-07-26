// Copyright (c) 2022 Dvir Yitzchaki.
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. See http://www.boost.org/LICENSE_1_0.txt.

#ifndef BOOST_CONVERT_CHARCONV_BASED_CONVERTER_HPP
#define BOOST_CONVERT_CHARCONV_BASED_CONVERTER_HPP

#ifdef BOOST_NO_CXX17_HDR_CHARCONV
#error "This header requires <charconv> which is unavailable"
#endif // BOOST_NO_CXX17_HDR_CHARCONV

#ifdef BOOST_NO_CXX17_STRUCTURED_BINDINGS
#error "This header requires structured bindings which is unavailable"
#endif // BOOST_NO_CXX17_STRUCTURED_BINDINGS

#ifdef BOOST_NO_CXX17_IF_CONSTEXPR
#error "This header requires constexpr if which is unavailable"
#endif // BOOST_NO_CXX17_IF_CONSTEXPR

#include <boost/convert/base.hpp>
#include <charconv>
#include <type_traits>

namespace boost { namespace cnv { struct charconv; }}

/// @brief std::to/from_chars-based extended converter
/// @details The converter offers good overall performance and moderate formatting facilities.

struct boost::cnv::charconv : boost::cnv::cnvbase<boost::cnv::charconv>
{
    using this_type = boost::cnv::charconv;
    using base_type = boost::cnv::cnvbase<this_type>;

    private:

    friend struct boost::cnv::cnvbase<this_type>;

    template<typename in_type>
    cnv::range<char*>
    to_str(in_type value_in, char* buf) const
    {
        const auto [ptr, ec] = [&]{
            if constexpr (std::is_integral_v<in_type>) {
                return std::to_chars(buf, buf + bufsize_, value_in, static_cast<int>(base_));
            } else {
                return std::to_chars(buf, buf + bufsize_, value_in, chars_format(), precision_);
            }
        }();
        bool success = ec == std::errc{};

        return cnv::range<char*>(buf, success ? ptr : buf);
    }

    template<typename string_type, typename out_type>
    void
    str_to(cnv::range<string_type> range, optional<out_type>& result_out) const
    {
        out_type result = boost::make_default<out_type>();
        const auto [ptr, ec] = [&]{
            if constexpr (std::is_integral_v<out_type>) {
                return std::from_chars(&*range.begin(), &*range.end(), result, static_cast<int>(base_));
            } else {
                return std::from_chars(&*range.begin(), &*range.end(), result, chars_format());
            }
        }();

        if (ec == std::errc{})
            result_out = result;
    }

    std::chars_format chars_format() const
    {
        static constexpr std::chars_format format[] =
        {
            std::chars_format::fixed,
            std::chars_format::scientific,
            std::chars_format::hex
        };
        return format[int(notation_)];
    }

    std::chars_format fmt_ = std::chars_format::fixed;
};

#endif // BOOST_CONVERT_CHARCONV_BASED_CONVERTER_HPP
