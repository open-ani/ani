// Boost.Geometry

// Copyright (c) 2021-2022, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGIES_BUFFER_GEOGRAPHIC_HPP
#define BOOST_GEOMETRY_STRATEGIES_BUFFER_GEOGRAPHIC_HPP


#include <boost/geometry/strategies/buffer/services.hpp>
#include <boost/geometry/strategies/distance/geographic.hpp>


namespace boost { namespace geometry
{

namespace strategies { namespace buffer
{


template
<
    typename FormulaPolicy = strategy::andoyer,
    typename Spheroid = srs::spheroid<double>,
    typename CalculationType = void
>
class geographic
    : public strategies::distance::geographic<FormulaPolicy, Spheroid, CalculationType>
{
    using base_t = strategies::distance::geographic<FormulaPolicy, Spheroid, CalculationType>;

public:
    geographic() = default;

    explicit geographic(Spheroid const& spheroid)
        : base_t(spheroid)
    {}
};


namespace services
{

template <typename Geometry>
struct default_strategy<Geometry, geographic_tag>
{
    using type = strategies::buffer::geographic<>;
};


} // namespace services

}} // namespace strategies::buffer

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGIES_BUFFER_GEOGRAPHIC_HPP
