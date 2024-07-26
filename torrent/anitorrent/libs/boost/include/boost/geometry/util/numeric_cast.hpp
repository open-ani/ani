// Boost.Geometry

// Copyright (c) 2024 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_UTIL_NUMERIC_CAST_HPP
#define BOOST_GEOMETRY_UTIL_NUMERIC_CAST_HPP

#include <boost/numeric/conversion/cast.hpp>

namespace boost { namespace geometry { namespace util

{

#ifndef DOXYGEN_NO_DETAIL
namespace detail
{

/// brief calls numeric cast
template <typename Target, typename Source>
struct numeric_caster
{
    static inline Target apply(Source const& source)
    {
        return boost::numeric_cast<Target>(source);
    }
};

} // namespace detail
#endif

// Calls either boost::numeric_cast, or functionality specific for Boost.Geometry
// (such as rational_cast for Boost.Rational)
template <typename Target, typename Source>
inline Target numeric_cast(Source const& source)
{
    return detail::numeric_caster<Target, Source>::apply(source);
}

}}} // namespace boost::geometry::util

#endif // BOOST_GEOMETRY_UTIL_NUMERIC_CAST_HPP
