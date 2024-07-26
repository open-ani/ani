//
// Copyright (c) 2009-2011 Artyom Beilis (Tonkikh)
//
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_LOCALE_ENCODING_UTF_HPP_INCLUDED
#define BOOST_LOCALE_ENCODING_UTF_HPP_INCLUDED

#include <boost/locale/encoding_errors.hpp>
#include <boost/locale/utf.hpp>
#include <boost/locale/util/string.hpp>
#include <iterator>

#ifdef BOOST_MSVC
#    pragma warning(push)
#    pragma warning(disable : 4275 4251 4231 4660)
#endif

namespace boost { namespace locale { namespace conv {
    /// \addtogroup codepage
    ///
    /// @{

    /// Convert a Unicode text in range [begin,end) to other Unicode encoding
    ///
    /// \throws conversion_error: Conversion failed (e.g. \a how is \c stop and any character cannot be decoded)
    template<typename CharOut, typename CharIn>
    std::basic_string<CharOut> utf_to_utf(const CharIn* begin, const CharIn* end, method_type how = default_method)
    {
        std::basic_string<CharOut> result;
        result.reserve(end - begin);
        std::back_insert_iterator<std::basic_string<CharOut>> inserter(result);
        while(begin != end) {
            const utf::code_point c = utf::utf_traits<CharIn>::decode(begin, end);
            if(c == utf::illegal || c == utf::incomplete) {
                if(how == stop)
                    throw conversion_error();
            } else
                utf::utf_traits<CharOut>::encode(c, inserter);
        }
        return result;
    }

    /// Convert a Unicode NULL terminated string \a str other Unicode encoding
    ///
    /// \throws conversion_error: Conversion failed (e.g. \a how is \c stop and any character cannot be decoded)
    template<typename CharOut, typename CharIn>
    std::basic_string<CharOut> utf_to_utf(const CharIn* str, method_type how = default_method)
    {
        return utf_to_utf<CharOut, CharIn>(str, util::str_end(str), how);
    }

    /// Convert a Unicode string \a str other Unicode encoding
    ///
    /// \throws conversion_error: Conversion failed (e.g. \a how is \c stop and any character cannot be decoded)
    template<typename CharOut, typename CharIn>
    std::basic_string<CharOut> utf_to_utf(const std::basic_string<CharIn>& str, method_type how = default_method)
    {
        return utf_to_utf<CharOut, CharIn>(str.c_str(), str.c_str() + str.size(), how);
    }

    /// @}

}}} // namespace boost::locale::conv

#ifdef BOOST_MSVC
#    pragma warning(pop)
#endif

#endif
