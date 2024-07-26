// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGY_SPHERICAL_ENVELOPE_BOXES_HPP
#define BOOST_GEOMETRY_STRATEGY_SPHERICAL_ENVELOPE_BOXES_HPP

#include <vector>

#include <boost/geometry/algorithms/detail/envelope/initialize.hpp>
#include <boost/geometry/algorithms/detail/envelope/range_of_boxes.hpp>

namespace boost { namespace geometry
{

namespace strategy { namespace envelope
{

class spherical_boxes
{
public:
    template <typename Box>
    class state
    {
        friend spherical_boxes;

        std::vector<Box> m_boxes;
    };

    template <typename Box>
    static void apply(state<Box> & st, Box const& box)
    {
        st.m_boxes.push_back(box);
    }

    template <typename Box>
    static void result(state<Box> const& st, Box & box)
    {
        if (! st.m_boxes.empty())
        {
            geometry::detail::envelope::envelope_range_of_boxes::apply(st.m_boxes, box);
        }
        else
        {
            geometry::detail::envelope::initialize<Box, 0, dimension<Box>::value>::apply(box);
        }
    }
};

}} // namespace strategy::envelope

}} //namepsace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGY_SPHERICAL_ENVELOPE_BOXES_HPP
