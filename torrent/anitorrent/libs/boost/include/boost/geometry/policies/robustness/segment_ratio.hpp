// Boost.Geometry (aka GGL, Generic Geometry Library)

// Copyright (c) 2013 Barend Gehrels, Amsterdam, the Netherlands.

// This file was modified by Oracle on 2016-2021.
// Modifications copyright (c) 2016-2021 Oracle and/or its affiliates.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_POLICIES_ROBUSTNESS_SEGMENT_RATIO_HPP
#define BOOST_GEOMETRY_POLICIES_ROBUSTNESS_SEGMENT_RATIO_HPP

#include <type_traits>

#include <boost/config.hpp>
#include <boost/rational.hpp>

#include <boost/geometry/core/assert.hpp>
#include <boost/geometry/core/coordinate_promotion.hpp>
#include <boost/geometry/util/math.hpp>
#include <boost/geometry/util/numeric_cast.hpp>


namespace boost { namespace geometry
{


namespace detail { namespace segment_ratio
{

template
<
    typename Type,
    bool IsIntegral = std::is_integral<Type>::type::value
>
struct less {};

template <typename Type>
struct less<Type, true>
{
    template <typename Ratio>
    static inline bool apply(Ratio const& lhs, Ratio const& rhs)
    {
        return boost::rational<Type>(lhs.numerator(), lhs.denominator())
             < boost::rational<Type>(rhs.numerator(), rhs.denominator());
    }
};

template <typename Type>
struct less<Type, false>
{
    template <typename Ratio>
    static inline bool apply(Ratio const& lhs, Ratio const& rhs)
    {
        BOOST_GEOMETRY_ASSERT(lhs.denominator() != Type(0));
        BOOST_GEOMETRY_ASSERT(rhs.denominator() != Type(0));
        Type const a = lhs.numerator() / lhs.denominator();
        Type const b = rhs.numerator() / rhs.denominator();
        return ! geometry::math::equals(a, b)
            && a < b;
    }
};

template
<
    typename Type,
    bool IsIntegral = std::is_integral<Type>::type::value
>
struct equal {};

template <typename Type>
struct equal<Type, true>
{
    template <typename Ratio>
    static inline bool apply(Ratio const& lhs, Ratio const& rhs)
    {
        return boost::rational<Type>(lhs.numerator(), lhs.denominator())
            == boost::rational<Type>(rhs.numerator(), rhs.denominator());
    }
};

template <typename Type>
struct equal<Type, false>
{
    template <typename Ratio>
    static inline bool apply(Ratio const& lhs, Ratio const& rhs)
    {
        BOOST_GEOMETRY_ASSERT(lhs.denominator() != Type(0));
        BOOST_GEOMETRY_ASSERT(rhs.denominator() != Type(0));
        Type const a = lhs.numerator() / lhs.denominator();
        Type const b = rhs.numerator() / rhs.denominator();
        return geometry::math::equals(a, b);
    }
};

template
<
    typename Type,
    bool IsFloatingPoint = std::is_floating_point<Type>::type::value
>
struct possibly_collinear {};

template <typename Type>
struct possibly_collinear<Type, true>
{
    template <typename Ratio, typename Threshold>
    static inline bool apply(Ratio const& ratio, Threshold threshold)
    {
        return std::abs(ratio.denominator()) < threshold;
    }
};

// Any ratio based on non-floating point (or user defined floating point)
// is collinear if the denominator is exactly zero
template <typename Type>
struct possibly_collinear<Type, false>
{
    template <typename Ratio, typename Threshold>
    static inline bool apply(Ratio const& ratio, Threshold)
    {
        static Type const zero = 0;
        return ratio.denominator() == zero;
    }
};

}}

//! Small class to keep a ratio (e.g. 1/4)
//! Main purpose is intersections and checking on 0, 1, and smaller/larger
//! The prototype used Boost.Rational. However, we also want to store FP ratios,
//! (so numerator/denominator both in float)
//! and Boost.Rational starts with GCD which we prefer to avoid if not necessary
//! On a segment means: this ratio is between 0 and 1 (both inclusive)
//!
template <typename Type>
class segment_ratio
{
    // Type used for the approximation (a helper value)
    // and for the edge value (0..1) (a helper function).
    using floating_point_type =
        typename detail::promoted_to_floating_point<Type>::type;

    // Type-alias for the type itself
    using thistype = segment_ratio<Type>;

public:
    using int_type = Type;

    inline segment_ratio()
        : m_numerator(0)
        , m_denominator(1)
        , m_approximation(0)
    {}

    inline segment_ratio(Type const& numerator, Type const& denominator)
        : m_numerator(numerator)
        , m_denominator(denominator)
    {
        initialize();
    }

    segment_ratio(segment_ratio const&) = default;
    segment_ratio& operator=(segment_ratio const&) = default;
    segment_ratio(segment_ratio&&) = default;
    segment_ratio& operator=(segment_ratio&&) = default;

    // These are needed because in intersection strategies ratios are assigned
    // in fractions and if a user passes CalculationType then ratio Type in
    // turns is taken from geometry coordinate_type and the one used in
    // a strategy uses Type selected using CalculationType.
    // See: detail::overlay::intersection_info_base
    // and  policies::relate::segments_intersection_points
    //      in particular segments_collinear() where ratios are assigned.
    template<typename T> friend class segment_ratio;
    template <typename T>
    segment_ratio(segment_ratio<T> const& r)
        : m_numerator(r.m_numerator)
        , m_denominator(r.m_denominator)
    {
        initialize();
    }
    template <typename T>
    segment_ratio& operator=(segment_ratio<T> const& r)
    {
        m_numerator = r.m_numerator;
        m_denominator = r.m_denominator;
        initialize();
        return *this;
    }
    template <typename T>
    segment_ratio(segment_ratio<T> && r)
        : m_numerator(std::move(r.m_numerator))
        , m_denominator(std::move(r.m_denominator))
    {
        initialize();
    }
    template <typename T>
    segment_ratio& operator=(segment_ratio<T> && r)
    {
        m_numerator = std::move(r.m_numerator);
        m_denominator = std::move(r.m_denominator);
        initialize();
        return *this;
    }

    inline Type const& numerator() const { return m_numerator; }
    inline Type const& denominator() const { return m_denominator; }

    inline void assign(Type const& numerator, Type const& denominator)
    {
        m_numerator = numerator;
        m_denominator = denominator;
        initialize();
    }

    inline void initialize()
    {
        // Minimal normalization
        // 1/-4 => -1/4, -1/-4 => 1/4
        if (m_denominator < zero_instance())
        {
            m_numerator = -m_numerator;
            m_denominator = -m_denominator;
        }

        m_approximation =
            m_denominator == zero_instance() ? floating_point_type{0}
            : (
                util::numeric_cast<floating_point_type>(m_numerator) * scale()
                / util::numeric_cast<floating_point_type>(m_denominator)
            );
    }

    inline bool is_zero() const { return math::equals(m_numerator, Type(0)); }
    inline bool is_one() const { return math::equals(m_numerator, m_denominator); }
    inline bool on_segment() const
    {
        // e.g. 0/4 or 4/4 or 2/4
        return m_numerator >= zero_instance() && m_numerator <= m_denominator;
    }
    inline bool in_segment() const
    {
        // e.g. 1/4
        return m_numerator > zero_instance() && m_numerator < m_denominator;
    }
    inline bool on_end() const
    {
        // e.g. 0/4 or 4/4
        return is_zero() || is_one();
    }
    inline bool left() const
    {
        // e.g. -1/4
        return m_numerator < zero_instance();
    }
    inline bool right() const
    {
        // e.g. 5/4
        return m_numerator > m_denominator;
    }

    //! Returns a value between 0.0 and 1.0
    //! 0.0 means: exactly in the middle
    //! 1.0 means: exactly on one of the edges (or even over it)
    inline floating_point_type edge_value() const
    {
        using fp = floating_point_type;
        fp const one{1.0};
        floating_point_type const result
                = fp(2) * geometry::math::abs(fp(0.5) - m_approximation / scale());
        return result > one ? one : result;
    }

    template <typename Threshold>
    inline bool possibly_collinear(Threshold threshold) const
    {
        return detail::segment_ratio::possibly_collinear<Type>::apply(*this, threshold);
    }

    inline bool operator< (thistype const& other) const
    {
        return close_to(other)
            ? detail::segment_ratio::less<Type>::apply(*this, other)
            : m_approximation < other.m_approximation;
    }

    inline bool operator== (thistype const& other) const
    {
        return close_to(other)
            && detail::segment_ratio::equal<Type>::apply(*this, other);
    }

    static inline thistype zero()
    {
        static thistype result(0, 1);
        return result;
    }

    static inline thistype one()
    {
        static thistype result(1, 1);
        return result;
    }

#if defined(BOOST_GEOMETRY_DEFINE_STREAM_OPERATOR_SEGMENT_RATIO)
    friend std::ostream& operator<<(std::ostream &os, segment_ratio const& ratio)
    {
        os << ratio.m_numerator << "/" << ratio.m_denominator
           << " (" << (static_cast<double>(ratio.m_numerator)
                        / static_cast<double>(ratio.m_denominator))
           << ")";
        return os;
    }
#endif

private :

    Type m_numerator;
    Type m_denominator;

    // Contains ratio on scale 0..1000000 (for 0..1)
    // This is an approximation for fast and rough comparisons
    // Boost.Rational is used if the approximations are close.
    // Reason: performance, Boost.Rational does a GCD by default and also the
    // comparisons contain while-loops.
    floating_point_type m_approximation;

    inline bool close_to(thistype const& other) const
    {
        static floating_point_type const threshold{50.0};
        return geometry::math::abs(m_approximation - other.m_approximation)
                < threshold;
    }

    static inline floating_point_type scale()
    {
        static floating_point_type const fp_scale{1000000.0};
        return fp_scale;
    }

    static inline Type zero_instance()
    {
        return 0;
    }
};


}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_POLICIES_ROBUSTNESS_SEGMENT_RATIO_HPP
