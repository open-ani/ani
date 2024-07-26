// Boost.Geometry (aka GGL, Generic Geometry Library)
//
// Copyright (c) 2008-2014 Bruno Lalande, Paris, France.
// Copyright (c) 2008-2014 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2009-2014 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2014-2021.
// Modifications copyright (c) 2014-2021, Oracle and/or its affiliates.

// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_GEOMETRIES_CONCEPTS_POINT_CONCEPT_HPP
#define BOOST_GEOMETRY_GEOMETRIES_CONCEPTS_POINT_CONCEPT_HPP

#include <cstddef>

#include <boost/concept_check.hpp>
#include <boost/core/ignore_unused.hpp>

#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/coordinate_dimension.hpp>
#include <boost/geometry/core/coordinate_system.hpp>

#include <boost/geometry/geometries/concepts/concept_type.hpp>


namespace boost { namespace geometry { namespace concepts
{

template <typename Geometry>
class Point
{
#ifndef DOXYGEN_NO_CONCEPT_MEMBERS

    typedef typename coordinate_type<Geometry>::type ctype;
    typedef typename coordinate_system<Geometry>::type csystem;

    // The following enum is used to fully instantiate the coordinate
    // system class; this is needed in order to check the units passed
    // to it for non-Cartesian coordinate systems.
    enum { cs_check = sizeof(csystem) };

    enum { ccount = dimension<Geometry>::value };

    template <typename P, std::size_t Dimension, std::size_t DimensionCount>
    struct dimension_checker
    {
        static void apply()
        {
            P* p = 0;
            geometry::set<Dimension>(*p, geometry::get<Dimension>(*p));
            dimension_checker<P, Dimension+1, DimensionCount>::apply();
        }
    };


    template <typename P, std::size_t DimensionCount>
    struct dimension_checker<P, DimensionCount, DimensionCount>
    {
        static void apply() {}
    };

public:

    /// BCCL macro to apply the Point concept
    BOOST_CONCEPT_USAGE(Point)
    {
        dimension_checker<Geometry, 0, ccount>::apply();
    }
#endif
};


/*!
\brief point concept (const version).

\ingroup const_concepts

\details The ConstPoint concept apply the same as the Point concept,
but does not apply write access.

*/
template <typename Geometry>
class ConstPoint
{
#ifndef DOXYGEN_NO_CONCEPT_MEMBERS

    typedef typename coordinate_type<Geometry>::type ctype;
    typedef typename coordinate_system<Geometry>::type csystem;

    // The following enum is used to fully instantiate the coordinate
    // system class; this is needed in order to check the units passed
    // to it for non-Cartesian coordinate systems.
    enum { cs_check = sizeof(csystem) };

    enum { ccount = dimension<Geometry>::value };

    template <typename P, std::size_t Dimension, std::size_t DimensionCount>
    struct dimension_checker
    {
        static void apply()
        {
            const P* p = 0;
            ctype coord(geometry::get<Dimension>(*p));
            boost::ignore_unused(p, coord);
            dimension_checker<P, Dimension+1, DimensionCount>::apply();
        }
    };


    template <typename P, std::size_t DimensionCount>
    struct dimension_checker<P, DimensionCount, DimensionCount>
    {
        static void apply() {}
    };

public:

    /// BCCL macro to apply the ConstPoint concept
    BOOST_CONCEPT_USAGE(ConstPoint)
    {
        dimension_checker<Geometry, 0, ccount>::apply();
    }
#endif
};


template <typename Geometry>
struct concept_type<Geometry, point_tag>
{
    using type = Point<Geometry>;
};

template <typename Geometry>
struct concept_type<Geometry const, point_tag>
{
    using type = ConstPoint<Geometry>;
};


}}} // namespace boost::geometry::concepts

#endif // BOOST_GEOMETRY_GEOMETRIES_CONCEPTS_POINT_CONCEPT_HPP
