// Copyright (c) 2009-2020 Vladimir Batov.
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. See http://www.boost.org/LICENSE_1_0.txt.

#ifndef BOOST_CONVERT_SPIRIT_BASED_CONVERTER_HPP
#define BOOST_CONVERT_SPIRIT_BASED_CONVERTER_HPP

#include <boost/convert/base.hpp>
#include <boost/convert/detail/config.hpp>
#include <boost/spirit/include/qi.hpp>
#include <boost/spirit/include/karma.hpp>

namespace boost { namespace cnv { struct spirit; }}

struct boost::cnv::spirit : boost::cnv::cnvbase<boost::cnv::spirit>
{
    using this_type = boost::cnv::spirit;
    using base_type = boost::cnv::cnvbase<this_type>;

    using base_type::operator();

    template<typename string_type, typename out_type>
    void
    str_to(cnv::range<string_type> range, optional<out_type>& result_out) const
    {
        using parser = typename boost::spirit::traits::create_parser<out_type>::type;

        auto    beg = range.begin();
        auto    end = range.end();
        auto result = out_type();

        if (boost::spirit::qi::parse(beg, end, parser(), result))
            if (beg == end) // ensure the whole string has been parsed
                result_out = result;
    }
    template<typename in_type, typename char_type>
    cnv::range<char_type*>
    to_str(in_type value_in, char_type* beg) const
    {
        using generator = typename boost::spirit::traits::create_generator<in_type>::type;

        auto  end = beg;
        bool good = boost::spirit::karma::generate(end, generator(), value_in);

        return cnv::range<char_type*>(beg, good ? end : beg);
    }
};

#endif // BOOST_CONVERT_SPIRIT_BASED_CONVERTER_HPP

