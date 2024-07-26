// Copyright Alexander Nasonov & Paul A. Bristow 2006.

// Use, modification and distribution are subject to the
// Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_DETAIL_LCAST_PRECISION_HPP_INCLUDED
#define BOOST_DETAIL_LCAST_PRECISION_HPP_INCLUDED

#include <climits>
#include <ios>
#include <limits>

#include <boost/config.hpp>
#include <boost/integer_traits.hpp>

namespace boost { namespace detail {

class lcast_abstract_stub {};

// Calculate an argument to pass to std::ios_base::precision from
// lexical_cast. See alternative implementation for broken standard
// libraries in lcast_get_precision below. Keep them in sync, please.
template<class T>
struct lcast_precision
{
    using limits = std::numeric_limits<T>;

    BOOST_STATIC_CONSTANT(bool, use_default_precision =
            !limits::is_specialized || limits::is_exact
        );

    BOOST_STATIC_CONSTANT(bool, is_specialized_bin =
            !use_default_precision &&
            limits::radix == 2 && limits::digits > 0
        );

    BOOST_STATIC_CONSTANT(bool, is_specialized_dec =
            !use_default_precision &&
            limits::radix == 10 && limits::digits10 > 0
        );

    BOOST_STATIC_CONSTANT(std::streamsize, streamsize_max =
            boost::integer_traits<std::streamsize>::const_max
        );

    BOOST_STATIC_CONSTANT(unsigned int, precision_dec = limits::digits10 + 1U);

    static_assert(!is_specialized_dec ||
            precision_dec <= streamsize_max + 0UL
        , "");

    BOOST_STATIC_CONSTANT(unsigned long, precision_bin =
            2UL + limits::digits * 30103UL / 100000UL
        );

    static_assert(!is_specialized_bin ||
            (limits::digits + 0UL < ULONG_MAX / 30103UL &&
            precision_bin > limits::digits10 + 0UL &&
            precision_bin <= streamsize_max + 0UL)
        , "");

    BOOST_STATIC_CONSTANT(std::streamsize, value =
            is_specialized_bin ? precision_bin
                               : is_specialized_dec ? precision_dec : 6
        );
};


template<class T>
inline std::streamsize lcast_get_precision(T* = 0)
{
    return lcast_precision<T>::value;
}

template<class T>
inline void lcast_set_precision(std::ios_base& stream, T*)
{
    stream.precision(lcast_get_precision<T>());
}

template<class Source, class Target>
inline void lcast_set_precision(std::ios_base& stream, Source*, Target*)
{
    std::streamsize const s = lcast_get_precision(static_cast<Source*>(0));
    std::streamsize const t = lcast_get_precision(static_cast<Target*>(0));
    stream.precision(s > t ? s : t);
}

}}

#endif //  BOOST_DETAIL_LCAST_PRECISION_HPP_INCLUDED

