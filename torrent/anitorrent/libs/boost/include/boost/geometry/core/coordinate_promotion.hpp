// Boost.Geometry

// Copyright (c) 2021 Barend Gehrels, Amsterdam, the Netherlands.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_GEOMETRY_CORE_COORDINATE_PROMOTION_HPP
#define BOOST_GEOMETRY_CORE_COORDINATE_PROMOTION_HPP

#include <boost/geometry/core/coordinate_type.hpp>

// TODO: move this to a future headerfile implementing traits for these types
#include <boost/rational.hpp>
#include <boost/multiprecision/cpp_bin_float.hpp>
#include <boost/multiprecision/cpp_int.hpp>

namespace boost { namespace geometry
{

namespace traits
{

// todo

} // namespace traits

/*!
    \brief Meta-function converting, if necessary, to "a floating point" type
    \details
        - if input type is integer, type is double
        - else type is input type
    \ingroup utility
 */
// TODO: replace with, or call, promoted_to_floating_point
template <typename T, typename PromoteIntegerTo = double>
struct promote_floating_point
{
    typedef std::conditional_t
        <
            std::is_integral<T>::value,
            PromoteIntegerTo,
            T
        > type;
};

// TODO: replace with promoted_to_floating_point
template <typename Geometry>
struct fp_coordinate_type
{
    typedef typename promote_floating_point
        <
            typename coordinate_type<Geometry>::type
        >::type type;
};

namespace detail
{

// Promote any integral type to double. Floating point
// and other user defined types stay as they are, unless specialized.
// TODO: we shold add a coordinate_promotion traits for promotion to
// floating point or (larger) integer types.
template <typename Type>
struct promoted_to_floating_point
{
    using type = std::conditional_t
        <
            std::is_integral<Type>::value, double, Type
        >;
};

// Boost.Rational goes to double
template <typename T>
struct promoted_to_floating_point<boost::rational<T>>
{
    using type = double;
};

// Any Boost.Multiprecision goes to double (for example int128_t),
// unless specialized
template <typename Backend>
struct promoted_to_floating_point<boost::multiprecision::number<Backend>>
{
    using type = double;
};

// Boost.Multiprecision binary floating point numbers are used as FP.
template <unsigned Digits>
struct promoted_to_floating_point
    <
        boost::multiprecision::number
            <
                boost::multiprecision::cpp_bin_float<Digits>
            >
    >
{
    using type = boost::multiprecision::number
        <
            boost::multiprecision::cpp_bin_float<Digits>
        >;
};

}


}} // namespace boost::geometry


#endif // BOOST_GEOMETRY_CORE_COORDINATE_PROMOTION_HPP
