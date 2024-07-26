// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2007-2012 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2013-2022.
// Modifications copyright (c) 2013-2022 Oracle and/or its affiliates.

// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)


#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_FOLLOW_HELPERS_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_FOLLOW_HELPERS_HPP

#include <vector>

#include <boost/core/ignore_unused.hpp>
#include <boost/range/size.hpp>

#include <boost/geometry/algorithms/detail/overlay/get_turn_info_helpers.hpp>
#include <boost/geometry/algorithms/detail/overlay/overlay_type.hpp>
#include <boost/geometry/algorithms/detail/overlay/segment_identifier.hpp>
#include <boost/geometry/algorithms/detail/relate/boundary_checker.hpp>
#include <boost/geometry/algorithms/not_implemented.hpp>

#include <boost/geometry/core/assert.hpp>

#include <boost/geometry/util/condition.hpp>
#include <boost/geometry/util/range.hpp>
#include <boost/geometry/util/type_traits.hpp>

#include <type_traits>

namespace boost { namespace geometry
{

#ifndef DOXYGEN_NO_DETAIL
namespace detail { namespace relate {

// NOTE: This iterates through single geometries for which turns were not generated.
//       It doesn't mean that the geometry is disjoint, only that no turns were detected.

template
<
    std::size_t OpId,
    typename Geometry,
    typename Tag = typename geometry::tag<Geometry>::type,
    bool IsMulti = util::is_multi<Geometry>::value
>
struct for_each_disjoint_geometry_if
    : public not_implemented<Tag>
{};

template <std::size_t OpId, typename Geometry, typename Tag>
struct for_each_disjoint_geometry_if<OpId, Geometry, Tag, false>
{
    template <typename TurnIt, typename Pred>
    static void apply(TurnIt first, TurnIt last, Geometry const& geometry, Pred & pred)
    {
        if (first == last)
        {
            pred(geometry);
        }
    }
};

template <std::size_t OpId, typename Geometry, typename Tag>
struct for_each_disjoint_geometry_if<OpId, Geometry, Tag, true>
{
    template <typename TurnIt, typename Pred>
    static void apply(TurnIt first, TurnIt last, Geometry const& geometry, Pred & pred)
    {
        if (first == last)
        {
            for_empty(geometry, pred);
        }
        else
        {
            for_turns(first, last, geometry, pred);
        }
    }

    template <typename Pred>
    static void for_empty(Geometry const& geometry, Pred & pred)
    {
        // O(N)
        // check predicate for each contained geometry without generated turn
        for (auto it = boost::begin(geometry); it != boost::end(geometry) ; ++it)
        {
            if (! pred(*it))
            {
                break;
            }
        }
    }

    template <typename TurnIt, typename Pred>
    static void for_turns(TurnIt first, TurnIt last, Geometry const& geometry, Pred & pred)
    {
        BOOST_GEOMETRY_ASSERT(first != last);

        std::size_t const count = boost::size(geometry);

        // O(I)
        // gather info about turns generated for contained geometries
        std::vector<bool> detected_intersections(count, false);
        for (TurnIt it = first; it != last; ++it)
        {
            signed_size_type multi_index = it->operations[OpId].seg_id.multi_index;
            BOOST_GEOMETRY_ASSERT(multi_index >= 0);
            std::size_t const index = static_cast<std::size_t>(multi_index);
            BOOST_GEOMETRY_ASSERT(index < count);
            detected_intersections[index] = true;
        }

        // O(N)
        // check predicate for each contained geometry without generated turn
        for (std::size_t index = 0; index < detected_intersections.size(); ++index)
        {
            // if there were no intersections for this multi_index
            if (detected_intersections[index] == false)
            {
                if (! pred(range::at(geometry, index)))
                {
                    break;
                }
            }
        }
    }
};


// WARNING! This class stores pointers!
// Passing a reference to local variable will result in undefined behavior!
template <typename Point>
class point_info
{
public:
    point_info() : sid_ptr(NULL), pt_ptr(NULL) {}
    point_info(Point const& pt, segment_identifier const& sid)
        : sid_ptr(boost::addressof(sid))
        , pt_ptr(boost::addressof(pt))
    {}
    segment_identifier const& seg_id() const
    {
        BOOST_GEOMETRY_ASSERT(sid_ptr);
        return *sid_ptr;
    }
    Point const& point() const
    {
        BOOST_GEOMETRY_ASSERT(pt_ptr);
        return *pt_ptr;
    }

    //friend bool operator==(point_identifier const& l, point_identifier const& r)
    //{
    //    return l.seg_id() == r.seg_id()
    //        && detail::equals::equals_point_point(l.point(), r.point());
    //}

private:
    const segment_identifier * sid_ptr;
    const Point * pt_ptr;
};

// WARNING! This class stores pointers!
// Passing a reference to local variable will result in undefined behavior!
class same_single
{
public:
    same_single(segment_identifier const& sid)
        : sid_ptr(boost::addressof(sid))
    {}

    bool operator()(segment_identifier const& sid) const
    {
        return sid.multi_index == sid_ptr->multi_index;
    }

    template <typename Point>
    bool operator()(point_info<Point> const& pid) const
    {
        return operator()(pid.seg_id());
    }

private:
    const segment_identifier * sid_ptr;
};

class same_ring
{
public:
    same_ring(segment_identifier const& sid)
        : sid_ptr(boost::addressof(sid))
    {}

    bool operator()(segment_identifier const& sid) const
    {
        return sid.multi_index == sid_ptr->multi_index
            && sid.ring_index == sid_ptr->ring_index;
    }

private:
    const segment_identifier * sid_ptr;
};

// WARNING! This class stores pointers!
// Passing a reference to local variable will result in undefined behavior!
template <typename SameRange = same_single>
class segment_watcher
{
public:
    segment_watcher()
        : m_seg_id_ptr(NULL)
    {}

    bool update(segment_identifier const& seg_id)
    {
        bool result = m_seg_id_ptr == 0 || !SameRange(*m_seg_id_ptr)(seg_id);
        m_seg_id_ptr = boost::addressof(seg_id);
        return result;
    }

private:
    const segment_identifier * m_seg_id_ptr;
};

// WARNING! This class stores pointers!
// Passing a reference to local variable will result in undefined behavior!
template <typename TurnInfo, std::size_t OpId>
class exit_watcher
{
    static std::size_t const op_id = OpId;
    static std::size_t const other_op_id = (OpId + 1) % 2;

    typedef typename TurnInfo::point_type point_type;
    typedef detail::relate::point_info<point_type> point_info;

public:
    exit_watcher()
        : m_exit_operation(overlay::operation_none)
        , m_exit_turn_ptr(NULL)
    {}

    void enter(TurnInfo const& turn)
    {
        m_other_entry_points.push_back(
            point_info(turn.point, turn.operations[other_op_id].seg_id) );
    }

    // TODO: exit_per_geometry parameter looks not very safe
    //       wrong value may be easily passed

    void exit(TurnInfo const& turn, bool exit_per_geometry = true)
    {
        //segment_identifier const& seg_id = turn.operations[op_id].seg_id;
        segment_identifier const& other_id = turn.operations[other_op_id].seg_id;
        overlay::operation_type exit_op = turn.operations[op_id].operation;

        // search for the entry point in the same range of other geometry
        auto entry_it = std::find_if(m_other_entry_points.begin(),
                                     m_other_entry_points.end(),
                                     same_single(other_id));

        // this end point has corresponding entry point
        if ( entry_it != m_other_entry_points.end() )
        {
            // erase the corresponding entry point
            m_other_entry_points.erase(entry_it);

            if ( exit_per_geometry || m_other_entry_points.empty() )
            {
                // here we know that we possibly left LS
                // we must still check if we didn't get back on the same point
                m_exit_operation = exit_op;
                m_exit_turn_ptr = boost::addressof(turn);
            }
        }
    }

    bool is_outside() const
    {
        // if we didn't entered anything in the past, we're outside
        return m_other_entry_points.empty();
    }

    bool is_outside(TurnInfo const& turn) const
    {
        return m_other_entry_points.empty()
            || std::none_of(m_other_entry_points.begin(),
                            m_other_entry_points.end(),
                            same_single(
                                turn.operations[other_op_id].seg_id));
    }

    overlay::operation_type get_exit_operation() const
    {
        return m_exit_operation;
    }

    point_type const& get_exit_point() const
    {
        BOOST_GEOMETRY_ASSERT(m_exit_operation != overlay::operation_none);
        BOOST_GEOMETRY_ASSERT(m_exit_turn_ptr);
        return m_exit_turn_ptr->point;
    }

    TurnInfo const& get_exit_turn() const
    {
        BOOST_GEOMETRY_ASSERT(m_exit_operation != overlay::operation_none);
        BOOST_GEOMETRY_ASSERT(m_exit_turn_ptr);
        return *m_exit_turn_ptr;
    }

    void reset_detected_exit()
    {
        m_exit_operation = overlay::operation_none;
    }

    void reset()
    {
        m_exit_operation = overlay::operation_none;
        m_other_entry_points.clear();
    }

private:
    overlay::operation_type m_exit_operation;
    const TurnInfo * m_exit_turn_ptr;
    std::vector<point_info> m_other_entry_points; // TODO: use map here or sorted vector?
};

template <std::size_t OpId, typename Turn, typename Strategy>
inline bool turn_on_the_same_ip(Turn const& prev_turn, Turn const& curr_turn,
                                Strategy const& strategy)
{
    segment_identifier const& prev_seg_id = prev_turn.operations[OpId].seg_id;
    segment_identifier const& curr_seg_id = curr_turn.operations[OpId].seg_id;

    if ( prev_seg_id.multi_index != curr_seg_id.multi_index
      || prev_seg_id.ring_index != curr_seg_id.ring_index )
    {
        return false;
    }

    // TODO: will this work if between segments there will be some number of degenerated ones?

    if ( prev_seg_id.segment_index != curr_seg_id.segment_index
      && ( ! curr_turn.operations[OpId].fraction.is_zero()
        || prev_seg_id.segment_index + 1 != curr_seg_id.segment_index ) )
    {
        return false;
    }

    return detail::equals::equals_point_point(prev_turn.point, curr_turn.point, strategy);
}

template <typename IntersectionPoint, typename OperationInfo, typename BoundaryChecker>
static inline bool is_ip_on_boundary(IntersectionPoint const& ip,
                                     OperationInfo const& operation_info,
                                     BoundaryChecker const& boundary_checker)
{
    // IP on the first or the last point of the linestring
    return (operation_info.position == overlay::position_back
            || operation_info.position == overlay::position_front)
         // check if this point is a boundary
         ? boundary_checker.is_endpoint_boundary(ip)
         : false;
}


}} // namespace detail::relate
#endif // DOXYGEN_NO_DETAIL

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_RELATE_FOLLOW_HELPERS_HPP
