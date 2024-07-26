// Boost.Geometry

// Copyright (c) 2021, Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Licensed under the Boost Software License version 1.0.
// http://www.boost.org/users/license.html

#ifndef BOOST_GEOMETRY_STRATEGY_CARTESIAN_ENVELOPE_BOXES_HPP
#define BOOST_GEOMETRY_STRATEGY_CARTESIAN_ENVELOPE_BOXES_HPP

#include <boost/geometry/algorithms/detail/envelope/initialize.hpp>
#include <boost/geometry/strategy/cartesian/expand_box.hpp>

namespace boost { namespace geometry
{

namespace strategy { namespace envelope
{

class cartesian_boxes
{
public:
    template <typename Box>
    class state
    {
        friend cartesian_boxes;

        Box m_box;
        bool m_initialized = false;
    };

    template <typename Box>
    static void apply(state<Box> & st, Box const& box)
    {
        if (! st.m_initialized)
        {
            st.m_box = box;
            st.m_initialized = true;
        }
        else
        {
            strategy::expand::cartesian_box::apply(st.m_box, box);
        }
    }

    template <typename Box>
    static void result(state<Box> const& st, Box & box)
    {
        if (st.m_initialized)
        {
            box = st.m_box;
        }
        else
        {
            geometry::detail::envelope::initialize<Box, 0, dimension<Box>::value>::apply(box);
        }
    }
};

}} // namespace strategy::envelope

}} //namepsace boost::geometry

#endif // BOOST_GEOMETRY_STRATEGY_CARTESIAN_ENVELOPE_BOXES_HPP
