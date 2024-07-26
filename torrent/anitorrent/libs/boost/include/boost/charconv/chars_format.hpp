// Copyright 2023 Matt Borland
// Distributed under the Boost Software License, Version 1.0.
// https://www.boost.org/LICENSE_1_0.txt

#ifndef BOOST_CHARCONV_CHARS_FORMAT_HPP
#define BOOST_CHARCONV_CHARS_FORMAT_HPP

namespace boost { namespace charconv {

// Floating-point format for primitive numerical conversion
// chars_format is a bitmask type (16.3.3.3.3)
enum class chars_format : unsigned
{
    scientific = 1 << 0,
    fixed = 1 << 1,
    hex = 1 << 2,
    general = fixed | scientific
};

}} // Namespaces

#endif // BOOST_CHARCONV_CHARS_FORMAT_HPP
