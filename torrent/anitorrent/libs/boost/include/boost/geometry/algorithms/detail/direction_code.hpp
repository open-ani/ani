// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2015-2020 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2015-2020.
// Modifications copyright (c) 2015-2020 Oracle and/or its affiliates.

// Contributed and/or modified by Menelaos Karavelas, on behalf of Oracle
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_ALGORITHMS_DETAIL_DIRECTION_CODE_HPP
#define BOOST_GEOMETRY_ALGORITHMS_DETAIL_DIRECTION_CODE_HPP


#include <type_traits>

#include <boost/geometry/core/access.hpp>
#include <boost/geometry/core/static_assert.hpp>
#include <boost/geometry/arithmetic/infinite_line_functions.hpp>
#include <boost/geometry/algorithms/detail/make/make.hpp>
#include <boost/geometry/util/math.hpp>
#include <boost/geometry/util/select_coordinate_type.hpp>
#include <boost/geometry/util/normalize_spheroidal_coordinates.hpp>


namespace boost { namespace geometry
{


#ifndef DOXYGEN_NO_DETAIL
namespace detail
{

template <typename CSTag>
struct direction_code_impl
{
    BOOST_GEOMETRY_STATIC_ASSERT_FALSE(
        "Not implemented for this coordinate system.",
        CSTag);
};

template <>
struct direction_code_impl<cartesian_tag>
{
    template <typename PointSegmentA, typename PointSegmentB, typename Point2>
    static inline int apply(PointSegmentA const& segment_a, PointSegmentB const& segment_b,
                            Point2 const& point)
    {
        using calc_t = typename geometry::select_coordinate_type
            <
                PointSegmentA, PointSegmentB, Point2
            >::type;

        using line_type = model::infinite_line<calc_t>;

        // Situation and construction of perpendicular line
        //
        //     P1     a--------------->b   P2
        //                             |
        //                             |
        //                             v
        //
        // P1 is located right of the (directional) perpendicular line
        // and therefore gets a negative side_value, and returns -1.
        // P2 is to the left of the perpendicular line and returns 1.
        // If the specified point is located on top of b, it returns 0.

        line_type const line
            = detail::make::make_perpendicular_line<calc_t>(segment_a,
                segment_b, segment_b);

        if (arithmetic::is_degenerate(line))
        {
            return 0;
        }

        calc_t const sv = arithmetic::side_value(line, point);
        static calc_t const zero = 0;
        return sv == zero ? 0 : sv > zero ? 1 : -1;
    }
};

template <>
struct direction_code_impl<spherical_equatorial_tag>
{
    template <typename PointSegmentA, typename PointSegmentB, typename Point2>
    static inline int apply(PointSegmentA const& segment_a, PointSegmentB const& segment_b,
                            Point2 const& p)
    {
        {
            using units_sa_t =  typename cs_angular_units<PointSegmentA>::type;
            using units_sb_t =  typename cs_angular_units<PointSegmentB>::type;
            using units_p_t = typename cs_angular_units<Point2>::type;
            BOOST_GEOMETRY_STATIC_ASSERT(
                (std::is_same<units_sa_t, units_sb_t>::value),
                "Not implemented for different units.",
                units_sa_t, units_sb_t);
            BOOST_GEOMETRY_STATIC_ASSERT(
                (std::is_same<units_sa_t, units_p_t>::value),
                "Not implemented for different units.",
                units_sa_t, units_p_t);
        }

        using coor_sa_t = typename coordinate_type<PointSegmentA>::type;
        using coor_sb_t = typename coordinate_type<PointSegmentB>::type;
        using coor_p_t = typename coordinate_type<Point2>::type;

        // Declare unit type (equal for all types) and calc type (coerced to most precise)
        using units_t = typename cs_angular_units<Point2>::type;
        using calc_t = typename geometry::select_coordinate_type
            <
                PointSegmentA, PointSegmentB, Point2
            >::type;
        using constants_sa_t = math::detail::constants_on_spheroid<coor_sa_t, units_t>;
        using constants_sb_t = math::detail::constants_on_spheroid<coor_sb_t, units_t>;
        using constants_p_t = math::detail::constants_on_spheroid<coor_p_t, units_t>;

        static coor_sa_t const pi_half_sa = constants_sa_t::max_latitude();
        static coor_sb_t const pi_half_sb = constants_sb_t::max_latitude();
        static coor_p_t const pi_half_p = constants_p_t::max_latitude();
        static calc_t const c0 = 0;

        coor_sa_t const a0 = geometry::get<0>(segment_a);
        coor_sa_t const a1 = geometry::get<1>(segment_a);
        coor_sb_t const b0 = geometry::get<0>(segment_b);
        coor_sb_t const b1 = geometry::get<1>(segment_b);
        coor_p_t const p0 = geometry::get<0>(p);
        coor_p_t const p1 = geometry::get<1>(p);

        if ( (math::equals(b0, a0) && math::equals(b1, a1))
          || (math::equals(b0, p0) && math::equals(b1, p1)) )
        {
            return 0;
        }

        bool const is_a_pole = math::equals(pi_half_sa, math::abs(a1));
        bool const is_b_pole = math::equals(pi_half_sb, math::abs(b1));
        bool const is_p_pole = math::equals(pi_half_p, math::abs(p1));

        if ( is_b_pole && ((is_a_pole && math::sign(b1) == math::sign(a1))
                        || (is_p_pole && math::sign(b1) == math::sign(p1))) )
        {
            return 0;
        }

        // NOTE: as opposed to the implementation for cartesian CS
        // here point b is the origin

        calc_t const dlon1 = math::longitude_distance_signed<units_t, calc_t>(b0, a0);
        calc_t const dlon2 = math::longitude_distance_signed<units_t, calc_t>(b0, p0);

        bool is_antilon1 = false, is_antilon2 = false;
        calc_t const dlat1 = latitude_distance_signed<units_t, calc_t>(b1, a1, dlon1, is_antilon1);
        calc_t const dlat2 = latitude_distance_signed<units_t, calc_t>(b1, p1, dlon2, is_antilon2);

        calc_t const mx = is_a_pole || is_b_pole || is_p_pole
            ? c0
            : (std::min)(is_antilon1 ? c0 : math::abs(dlon1),
                         is_antilon2 ? c0 : math::abs(dlon2));
        calc_t const my = (std::min)(math::abs(dlat1),
                                     math::abs(dlat2));

        int s1 = 0, s2 = 0;
        if (mx >= my)
        {
            s1 = dlon1 > 0 ? 1 : -1;
            s2 = dlon2 > 0 ? 1 : -1;
        }
        else
        {
            s1 = dlat1 > 0 ? 1 : -1;
            s2 = dlat2 > 0 ? 1 : -1;
        }

        return s1 == s2 ? -1 : 1;
    }

    template <typename Units, typename T>
    static inline T latitude_distance_signed(T const& lat1, T const& lat2, T const& lon_ds, bool & is_antilon)
    {
        using constants = math::detail::constants_on_spheroid<T, Units>;
        static T const pi = constants::half_period();
        static T const c0 = 0;

        T res = lat2 - lat1;

        is_antilon = math::equals(math::abs(lon_ds), pi);
        if (is_antilon)
        {
            res = lat2 + lat1;
            if (res >= c0)
                res = pi - res;
            else
                res = -pi - res;
        }

        return res;
    }
};

template <>
struct direction_code_impl<spherical_polar_tag>
{
    template <typename PointSegmentA, typename PointSegmentB, typename Point2>
    static inline int apply(PointSegmentA segment_a, PointSegmentB segment_b,
                            Point2 p)
    {
        using constants_sa_t = math::detail::constants_on_spheroid
            <
                typename coordinate_type<PointSegmentA>::type,
                typename cs_angular_units<PointSegmentA>::type
            >;
        using constants_p_t = math::detail::constants_on_spheroid
            <
                typename coordinate_type<Point2>::type,
                typename cs_angular_units<Point2>::type
            >;

        geometry::set<1>(segment_a,
            constants_sa_t::max_latitude() - geometry::get<1>(segment_a));
        geometry::set<1>(segment_b,
            constants_sa_t::max_latitude() - geometry::get<1>(segment_b));
        geometry::set<1>(p,
            constants_p_t::max_latitude() - geometry::get<1>(p));

        return direction_code_impl
                <
                    spherical_equatorial_tag
                >::apply(segment_a, segment_b, p);
    }
};

// if spherical_tag is passed then pick cs_tag based on PointSegmentA type
// with spherical_equatorial_tag as the default
template <>
struct direction_code_impl<spherical_tag>
{
    template <typename PointSegmentA, typename PointSegmentB, typename Point2>
    static inline int apply(PointSegmentA segment_a, PointSegmentB segment_b,
                            Point2 p)
    {
        return direction_code_impl
            <
                std::conditional_t
                    <
                        std::is_same
                            <
                                typename geometry::cs_tag<PointSegmentA>::type,
                                spherical_polar_tag
                            >::value,
                        spherical_polar_tag,
                        spherical_equatorial_tag
                    >
            >::apply(segment_a, segment_b, p);
    }
};

template <>
struct direction_code_impl<geographic_tag>
    : direction_code_impl<spherical_equatorial_tag>
{};

// Gives sense of direction for point p, collinear w.r.t. segment (a,b)
// Returns -1 if p goes backward w.r.t (a,b), so goes from b in direction of a
// Returns 1 if p goes forward, so extends (a,b)
// Returns 0 if p is equal with b, or if (a,b) is degenerate
// Note that it does not do any collinearity test, that should be done before
// In some cases the "segment" consists of different source points, and therefore
// their types might differ.
template <typename CSTag, typename PointSegmentA, typename PointSegmentB, typename Point2>
inline int direction_code(PointSegmentA const& segment_a, PointSegmentB const& segment_b,
                          Point2 const& p)
{
    return direction_code_impl<CSTag>::apply(segment_a, segment_b, p);
}


} // namespace detail
#endif //DOXYGEN_NO_DETAIL


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_ALGORITHMS_DETAIL_DIRECTION_CODE_HPP
