// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2008-2012 Bruno Lalande, Paris, France.
// Copyright (c) 2008-2012 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2009-2012 Mateusz Loskot, London, UK.

// This file was modified by Oracle on 2020-2021.
// Modifications copyright (c) 2020-2021 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Parts of Boost.Geometry are redesigned from Geodan's Geographic Library
// (geolib/GGL), copyright (c) 1995-2010 Geodan, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)


#ifndef BOOST_GEOMETRY_GEOMETRIES_CONCEPTS_LINESTRING_CONCEPT_HPP
#define BOOST_GEOMETRY_GEOMETRIES_CONCEPTS_LINESTRING_CONCEPT_HPP


#include <boost/concept_check.hpp>
#include <boost/range/concepts.hpp>

#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/mutable_range.hpp>
#include <boost/geometry/core/point_type.hpp>

#include <boost/geometry/geometries/concepts/concept_type.hpp>
#include <boost/geometry/geometries/concepts/point_concept.hpp>



namespace boost { namespace geometry { namespace concepts
{

template <typename Geometry>
class Linestring
{
#ifndef DOXYGEN_NO_CONCEPT_MEMBERS
    typedef typename point_type<Geometry>::type point_type;

    BOOST_CONCEPT_ASSERT( (concepts::Point<point_type>) );
    BOOST_CONCEPT_ASSERT( (boost::RandomAccessRangeConcept<Geometry>) );

public :

    BOOST_CONCEPT_USAGE(Linestring)
    {
        Geometry* ls = 0;
        traits::clear<Geometry>::apply(*ls);
        traits::resize<Geometry>::apply(*ls, 0);
        point_type* point = 0;
        traits::push_back<Geometry>::apply(*ls, *point);
    }
#endif
};


/*!
\brief Linestring concept (const version)
\ingroup const_concepts
\details The ConstLinestring concept check the same as the Linestring concept,
but does not check write access.
*/
template <typename Geometry>
class ConstLinestring
{
#ifndef DOXYGEN_NO_CONCEPT_MEMBERS
    typedef typename point_type<Geometry>::type point_type;

    BOOST_CONCEPT_ASSERT( (concepts::ConstPoint<point_type>) );
    //BOOST_CONCEPT_ASSERT( (boost::RandomAccessRangeConcept<Geometry>) );
    // Relaxed the concept.
    BOOST_CONCEPT_ASSERT( (boost::ForwardRangeConcept<Geometry>) );


public :

    BOOST_CONCEPT_USAGE(ConstLinestring)
    {
    }
#endif
};


template <typename Geometry>
struct concept_type<Geometry, linestring_tag>
{
    using type = Linestring<Geometry>;
};

template <typename Geometry>
struct concept_type<Geometry const, linestring_tag>
{
    using type = ConstLinestring<Geometry>;
};


}}} // namespace boost::geometry::concepts


#endif // BOOST_GEOMETRY_GEOMETRIES_CONCEPTS_LINESTRING_CONCEPT_HPP
