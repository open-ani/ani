// Boost.GIL (Generic Image Library) - tests
//
// Copyright 2020 Olzhas Zhumabek <anonymous.from.applecity@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//
#ifndef BOOST_GIL_EXTENSION_IMAGE_PROCESSING_HOUGH_PARAMETER_HPP
#define BOOST_GIL_EXTENSION_IMAGE_PROCESSING_HOUGH_PARAMETER_HPP

#include "boost/gil/point.hpp"

#include <cmath>
#include <cstddef>

namespace boost
{
namespace gil
{
/// \ingroup HoughTransform
/// \brief A type to encapsulate Hough transform parameter range
///
/// This type provides a way to express value range for a parameter
/// as well as some factory functions to simplify initialization
template <typename T>
struct hough_parameter
{
    T start_point;
    T step_size;
    std::size_t step_count;

    /// \ingroup HoughTransform
    /// \brief Create Hough parameter from value neighborhood and step count
    ///
    /// This function will take start_point as middle point, and in both
    /// directions will try to walk half_step_count times until distance of
    /// neighborhood is reached
    static hough_parameter<T> from_step_count(T start_point, T neighborhood,
                                              std::size_t half_step_count)
    {
        T step_size = neighborhood / half_step_count;
        std::size_t step_count = half_step_count * 2 + 1;
        // explicitly fill out members, as aggregate init will error out with narrowing
        hough_parameter<T> parameter;
        parameter.start_point = start_point - neighborhood;
        parameter.step_size = step_size;
        parameter.step_count = step_count;
        return parameter;
    }

    /// \ingroup HoughTransform
    /// \brief Create Hough parameter from value neighborhood and step size
    ///
    /// This function will take start_point as middle point, and in both
    /// directions will try to walk step_size at a time until distance of
    /// neighborhood is reached
    static hough_parameter<T> from_step_size(T start_point, T neighborhood, T step_size)
    {
        std::size_t step_count =
            2 * static_cast<std::size_t>(std::floor(neighborhood / step_size)) + 1;
        // do not use step_size - neighborhood, as step_size might not allow
        // landing exactly on that value when starting from start_point
        // also use parentheses on step_count / 2 because flooring is exactly
        // what we want

        // explicitly fill out members, as aggregate init will error out with narrowing
        hough_parameter<T> parameter;
        parameter.start_point = start_point - step_size * (step_count / 2);
        parameter.step_size = step_size;
        parameter.step_count = step_count;
        return parameter;
    }
};

/// \ingroup HoughTransform
/// \brief Calculate minimum angle which would be observable if walked on a circle
///
/// When drawing a circle or moving around a point in circular motion, it is
/// important to not do too many steps, but also to not have disconnected
/// trajectory. This function will calculate the minimum angle that is observable
/// when walking on a circle or tilting a line.
/// WARNING: do keep in mind IEEE 754 quirks, e.g. no-associativity,
/// no-commutativity and precision. Do not expect expressions that are
/// mathematically the same to produce the same values
inline double minimum_angle_step(point_t dimensions)
{
    auto longer_dimension = dimensions.x > dimensions.y ? dimensions.x : dimensions.y;
    return std::atan2(1, longer_dimension);
}

/// \ingroup HoughTransform
/// \brief Create a Hough transform parameter with optimal angle step
///
/// Due to computational intensity and noise sensitivity of Hough transform,
/// having any candidates missed or computed again is problematic. This function
/// will properly encapsulate optimal value range around approx_angle with amplitude of
/// neighborhood in each direction.
/// WARNING: do keep in mind IEEE 754 quirks, e.g. no-associativity,
/// no-commutativity and precision. Do not expect expressions that are
/// mathematically the same to produce the same values
inline auto make_theta_parameter(double approx_angle, double neighborhood, point_t dimensions)
    -> hough_parameter<double>
{
    auto angle_step = minimum_angle_step(dimensions);

    // std::size_t step_count =
    //     2 * static_cast<std::size_t>(std::floor(neighborhood / angle_step)) + 1;
    // return {approx_angle - angle_step * (step_count / 2), angle_step, step_count};
    return hough_parameter<double>::from_step_size(approx_angle, neighborhood, angle_step);
}
}} // namespace boost::gil
#endif
