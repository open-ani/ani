// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2022 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_IO_WKT_DETAIL_PREFIX_HPP
#define BOOST_GEOMETRY_IO_WKT_DETAIL_PREFIX_HPP

namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace wkt
{

struct prefix_null
{
    static inline const char* apply() { return ""; }
};

struct prefix_point
{
    static inline const char* apply() { return "POINT"; }
};

struct prefix_polygon
{
    static inline const char* apply() { return "POLYGON"; }
};

struct prefix_linestring
{
    static inline const char* apply() { return "LINESTRING"; }
};

struct prefix_multipoint
{
    static inline const char* apply() { return "MULTIPOINT"; }
};

struct prefix_multilinestring
{
    static inline const char* apply() { return "MULTILINESTRING"; }
};

struct prefix_multipolygon
{
    static inline const char* apply() { return "MULTIPOLYGON"; }
};

struct prefix_segment
{
    static inline const char* apply() { return "SEGMENT"; }
};
struct prefix_box
{
    static inline const char* apply() { return "BOX"; }
};
struct prefix_geometrycollection
{
    static inline const char* apply() { return "GEOMETRYCOLLECTION"; }
};


}} // namespace wkt::impl
#endif


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_IO_WKT_DETAIL_PREFIX_HPP
