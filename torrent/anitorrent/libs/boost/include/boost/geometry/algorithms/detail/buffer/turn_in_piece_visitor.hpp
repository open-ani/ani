// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2012-2020 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2017 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2016-2022.
// Modifications copyright (c) 2016-2022 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_BUFFER_TURN_IN_PIECE_VISITOR_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_BUFFER_TURN_IN_PIECE_VISITOR_HPP

#include <boost/geometry/core/assert.hpp>
#include <boost/geometry/core/config.hpp>

#include <boost/geometry/algorithms/comparable_distance.hpp>
#include <boost/geometry/algorithms/covered_by.hpp>
#include <boost/geometry/algorithms/detail/disjoint/point_box.hpp>
#include <boost/geometry/algorithms/detail/disjoint/box_box.hpp>
#include <boost/geometry/algorithms/detail/dummy_geometries.hpp>
#include <boost/geometry/algorithms/detail/buffer/buffer_policies.hpp>
#include <boost/geometry/geometries/box.hpp>


namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL

namespace detail { namespace buffer
{

template
<
    typename CsTag,
    typename Turns,
    typename Pieces,
    typename DistanceStrategy,
    typename UmbrellaStrategy

>
class turn_in_piece_visitor
{
    Turns& m_turns; // because partition is currently operating on const input only
    Pieces const& m_pieces; // to check for piece-type
    DistanceStrategy const& m_distance_strategy; // to check if point is on original or one_sided
    UmbrellaStrategy const& m_umbrella_strategy;

    template <typename Operation, typename Piece>
    inline bool skip(Operation const& op, Piece const& piece) const
    {
        if (op.piece_index == piece.index)
        {
            return true;
        }
        Piece const& pc = m_pieces[op.piece_index];
        if (pc.left_index == piece.index || pc.right_index == piece.index)
        {
            if (pc.type == strategy::buffer::buffered_flat_end)
            {
                // If it is a flat end, don't compare against its neighbor:
                // it will always be located on one of the helper segments
                return true;
            }
            if (pc.type == strategy::buffer::buffered_concave)
            {
                // If it is concave, the same applies: the IP will be
                // located on one of the helper segments
                return true;
            }
        }

        return false;
    }

    template <typename NumericType>
    inline bool is_one_sided(NumericType const& left, NumericType const& right) const
    {
        static NumericType const zero = 0;
        return geometry::math::equals(left, zero)
            || geometry::math::equals(right, zero);
    }

    template <typename Point>
    inline bool has_zero_distance_at(Point const& point) const
    {
        return is_one_sided(m_distance_strategy.apply(point, point,
                strategy::buffer::buffer_side_left),
            m_distance_strategy.apply(point, point,
                strategy::buffer::buffer_side_right));
    }

public:

    inline turn_in_piece_visitor(Turns& turns, Pieces const& pieces,
                                 DistanceStrategy const& distance_strategy,
                                 UmbrellaStrategy const& umbrella_strategy)
        : m_turns(turns)
        , m_pieces(pieces)
        , m_distance_strategy(distance_strategy)
        , m_umbrella_strategy(umbrella_strategy)
    {}

    template <typename Turn, typename Piece>
    inline bool apply(Turn const& turn, Piece const& piece)
    {
        if (! turn.is_turn_traversable)
        {
            // Already handled
            return true;
        }

        if (piece.type == strategy::buffer::buffered_flat_end
            || piece.type == strategy::buffer::buffered_concave)
        {
            // Turns cannot be located within flat-end or concave pieces
            return true;
        }

        if (skip(turn.operations[0], piece) || skip(turn.operations[1], piece))
        {
            return true;
        }

        return apply(turn, piece, piece.m_piece_border);
    }

    template <typename Turn, typename Piece, typename Border>
    inline bool apply(Turn const& turn, Piece const& piece, Border const& border)
    {
        if (! geometry::covered_by(turn.point, border.m_envelope, m_umbrella_strategy))
        {
            // Easy check: if turn is not in the (expanded) envelope
            return true;
        }

        if (piece.type == geometry::strategy::buffer::buffered_empty_side)
        {
            return false;
        }

        if (piece.type == geometry::strategy::buffer::buffered_point)
        {
            // Optimization for a buffer around points: if distance from center
            // is not between min/max radius, it is either inside or outside,
            // and more expensive checks are not necessary.
            auto const d = geometry::comparable_distance(piece.m_center, turn.point,
                                                         m_umbrella_strategy);

            if (d < border.m_min_comparable_radius)
            {
                Turn& mutable_turn = m_turns[turn.turn_index];
                mutable_turn.is_turn_traversable = false;
                return true;
            }
            if (d > border.m_max_comparable_radius)
            {
                return true;
            }
        }

        // Check if buffer is one-sided (at this point), because then a point
        // on the original border is not considered as within.
        bool const one_sided = has_zero_distance_at(turn.point);

        typename Border::state_type state;
        if (! border.point_on_piece(turn.point, one_sided,
                                    turn.is_linear_end_point, state))
        {
            return true;
        }

        if (state.is_inside() && ! state.is_on_boundary())
        {
            Turn& mutable_turn = m_turns[turn.turn_index];
            mutable_turn.is_turn_traversable = false;
        }

        return true;
    }
};


}} // namespace detail::buffer
#endif // DOXYGEN_NO_DETAIL


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_BUFFER_TURN_IN_PIECE_VISITOR_HPP
