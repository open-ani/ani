// Copyright (c) 2009-2020 Vladimir Batov.
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. See http://www.boost.org/LICENSE_1_0.txt.

#ifndef BOOST_CONVERT_PRINTF_HPP
#define BOOST_CONVERT_PRINTF_HPP

#include <boost/convert/base.hpp>
#include <boost/make_default.hpp>
#include <boost/mpl/vector.hpp>
#include <boost/mpl/find.hpp>
#include <string>
#include <cstdio>

namespace boost { namespace cnv { struct printf; }}

struct boost::cnv::printf : boost::cnv::cnvbase<boost::cnv::printf>
{
    using this_type = boost::cnv::printf;
    using base_type = boost::cnv::cnvbase<this_type>;

    using base_type::operator();

    template<typename in_type>
    cnv::range<char*>
    to_str(in_type value_in, char* buf) const
    {
        char_cptr fmt = printf_format(pos<in_type>());
        int num_chars = snprintf(buf, bufsize_, fmt, precision_, value_in);
        bool  success = num_chars < bufsize_;

        return cnv::range<char*>(buf, success ? (buf + num_chars) : buf);
    }
    template<typename string_type, typename out_type>
    void
    str_to(cnv::range<string_type> range, optional<out_type>& result_out) const
    {
        out_type result = boost::make_default<out_type>();
        char_cptr   fmt = sscanf_format(pos<out_type>());
        int    num_read = sscanf(&*range.begin(), fmt, &result);

        if (num_read == 1)
            result_out = result;
    }

    private:

    template<typename Type> int pos() const
    {
        // C1. The orders of types and formats must match.

        using types = boost::mpl::vector<
                          double, float, int, unsigned int, short int,
                          unsigned short int, long int, unsigned long int>;
        using found = typename boost::mpl::find<types, Type>::type;
        using   pos = typename found::pos;

        return pos::value;
    }
    char_cptr printf_format(int type_pos) const
    {
        char_cptr BOOST_CONSTEXPR_OR_CONST d_fmt[3][8] =
        {
            { "%.*f", "%.*f", "%.*d", "%.*u", "%.*hd", "%.*hu", "%.*ld", "%.*lu" }, //C1. fxd
            { "%.*e", "%.*e", "%.*d", "%.*u", "%.*hd", "%.*hu", "%.*ld", "%.*lu" }, //C1. sci
            { "%.*a", "%.*a", "%.*d", "%.*u", "%.*hd", "%.*hu", "%.*ld", "%.*lu" }  //C1. hex
        };
        char_cptr BOOST_CONSTEXPR_OR_CONST x_fmt[3][8] =
        {
            { "%.*f", "%.*f", "%.*x", "%.*x", "%.*hx", "%.*hx", "%.*lx", "%.*lx" }, //C1. fxd
            { "%.*e", "%.*e", "%.*x", "%.*x", "%.*hx", "%.*hx", "%.*lx", "%.*lx" }, //C1. sci
            { "%.*a", "%.*a", "%.*x", "%.*x", "%.*hx", "%.*hx", "%.*lx", "%.*lx" }  //C1. hex
        };
        char_cptr BOOST_CONSTEXPR_OR_CONST o_fmt[3][8] =
        {
            { "%.*f", "%.*f", "%.*o", "%.*o", "%.*ho", "%.*ho", "%.*lo", "%.*lo" }, //C1. fxd
            { "%.*e", "%.*e", "%.*o", "%.*o", "%.*ho", "%.*ho", "%.*lo", "%.*lo" }, //C1. sci
            { "%.*a", "%.*a", "%.*o", "%.*o", "%.*ho", "%.*ho", "%.*lo", "%.*lo" }  //C1. hex
        };
        return base_ == base::dec ? d_fmt[int(notation_)][type_pos]
             : base_ == base::hex ? x_fmt[int(notation_)][type_pos]
             : base_ == base::oct ? o_fmt[int(notation_)][type_pos]
             : (BOOST_ASSERT(0), nullptr);
    }
    char_cptr sscanf_format(int type_pos) const
    {
        char_cptr BOOST_CONSTEXPR_OR_CONST d_fmt[3][8] =
        {
            { "%lf", "%f", "%d", "%u", "%hd", "%hu", "%ld", "%lu" }, //C1. fxd
            { "%le", "%e", "%d", "%u", "%hd", "%hu", "%ld", "%lu" }, //C1. sci
            { "%la", "%a", "%d", "%u", "%hd", "%hu", "%ld", "%lu" }  //C1. hex
        };
        char_cptr BOOST_CONSTEXPR_OR_CONST x_fmt[3][8] =
        {
            { "%lf", "%f", "%x", "%x", "%hx", "%hx", "%lx", "%lx" }, //C1. fxd
            { "%le", "%e", "%x", "%x", "%hx", "%hx", "%lx", "%lx" }, //C1. sci
            { "%la", "%a", "%x", "%x", "%hx", "%hx", "%lx", "%lx" }  //C1. hex
        };
        char_cptr BOOST_CONSTEXPR_OR_CONST o_fmt[3][8] =
        {
            { "%lf", "%f", "%o", "%o", "%ho", "%ho", "%lo", "%lo" }, //C1. fxd
            { "%le", "%e", "%o", "%o", "%ho", "%ho", "%lo", "%lo" }, //C1. sci
            { "%la", "%a", "%o", "%o", "%ho", "%ho", "%lo", "%lo" }  //C1. hex
        };
        return base_ == base::dec ? d_fmt[int(notation_)][type_pos]
             : base_ == base::hex ? x_fmt[int(notation_)][type_pos]
             : base_ == base::oct ? o_fmt[int(notation_)][type_pos]
             : (BOOST_ASSERT(0), nullptr);
    }
};

#endif // BOOST_CONVERT_PRINTF_HPP
