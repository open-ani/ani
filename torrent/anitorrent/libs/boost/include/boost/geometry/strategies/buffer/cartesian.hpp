// Boost.Geometry

// Copyright (c) 2021-2022, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_BUFFER_CARTESIAN_HPP
#define BOOST_GEOMETRY_STRATEGIES_BUFFER_CARTESIAN_HPP


#include <boost/geometry/strategies/buffer/services.hpp>
#include <boost/geometry/strategies/distance/cartesian.hpp>


namespace boost { namespace geometry
{

namespace strategies { namespace buffer
{

template <typename CalculationType = void>
struct cartesian
    : public strategies::distance::cartesian<CalculationType>
{};


namespace services
{

template <typename Geometry>
struct default_strategy<Geometry, cartesian_tag>
{
    using type = strategies::buffer::cartesian<>;
};


} // namespace services

}} // namespace strategies::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_BUFFER_CARTESIAN_HPP
