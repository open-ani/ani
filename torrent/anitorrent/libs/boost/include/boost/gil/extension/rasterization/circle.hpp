//
// Copyright 2020 Olzhas Zhumabek <anonymous.from.applecity@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//
#ifndef BOOST_GIL_EXTENSION_RASTERIZATION_CIRCLE_HPP
#define BOOST_GIL_EXTENSION_RASTERIZATION_CIRCLE_HPP

#include <boost/gil/detail/math.hpp>
#include <boost/gil/extension/rasterization/apply_rasterizer.hpp>
#include <boost/gil/point.hpp>

#include <cmath>
#include <cstddef>
#include <vector>

namespace boost { namespace gil {

struct circle_rasterizer_t{};

/// \defgroup CircleRasterization
/// \ingroup Rasterization
/// \brief Circle rasterization algorithms
///
/// The main problems are connectivity and equation following. Circle can be easily moved
/// to new offset, and rotation has no effect on it (not recommended to do rotation).

/// \ingroup CircleRasterization
/// \brief Rasterize trigonometric circle according to radius by sine and radius by cosine
///
/// This rasterizer is the one used that is used in standard Hough circle transform in
/// the books. It is also quite expensive to compute.
/// WARNING: the product of this rasterizer does not follow circle equation, even though it
/// produces quite round like shapes.
struct trigonometric_circle_rasterizer
{
    using type = circle_rasterizer_t;

    /// \brief Creates a trigonometric circle rasterizer
    /// \param center_point - Point containing positive integer x co-ordinate and y co-ordinate of the
    /// center respectively.
    /// \param circle_radius - Radius of the circle
    trigonometric_circle_rasterizer(point_t center_point, std::ptrdiff_t circle_radius)
        : center(center_point), radius(circle_radius)
    {}

    /// \brief Calculates minimum angle step that is distinguishable when walking on circle
    ///
    /// It is important to not have disconnected circle and to not compute unnecessarily,
    /// thus the result of this function is used when rendering.
    double minimum_angle_step() const noexcept
    {
        const auto diameter = radius * 2 - 1;
        return std::atan2(1.0, diameter);
    }

    /// \brief Calculate the amount of points that rasterizer will output
    std::ptrdiff_t point_count() const noexcept
    {
        return 8 * static_cast<std::ptrdiff_t>(
                       std::round(detail::pi / 4 / minimum_angle_step()) + 1);
    }

    /// \brief perform rasterization and output into d_first
    template <typename OutputIterator>
    void operator()(OutputIterator d_first) const
    {
        const double minimum_angle_step = std::atan2(1.0, radius);
        auto translate_mirror_points = [this, &d_first](point_t p) {
            *d_first++ = point_t{center.x + p.x, center.y + p.y};
            *d_first++ = point_t{center.x + p.x, center.y - p.y};
            *d_first++ = point_t{center.x - p.x, center.y + p.y};
            *d_first++ = point_t{center.x - p.x, center.y - p.y};
            *d_first++ = point_t{center.x + p.y, center.y + p.x};
            *d_first++ = point_t{center.x + p.y, center.y - p.x};
            *d_first++ = point_t{center.x - p.y, center.y + p.x};
            *d_first++ = point_t{center.x - p.y, center.y - p.x};
        };
        const std::ptrdiff_t iteration_count = point_count() / 8;
        double angle = 0;
        // do note that + 1 was done inside count estimation, thus <= is not needed, only <
        for (std::ptrdiff_t i = 0; i < iteration_count; ++i, angle += minimum_angle_step)
        {
            std::ptrdiff_t x = static_cast<std::ptrdiff_t>(std::round(radius * std::cos(angle)));
            std::ptrdiff_t y = static_cast<std::ptrdiff_t>(std::round(radius * std::sin(angle)));
            translate_mirror_points({x, y});
        }
    }

    point_t center;
    std::ptrdiff_t radius;
};

/// \ingroup CircleRasterization
/// \brief Perform circle rasterization according to Midpoint algorithm
///
/// This algorithm givess reasonable output and is cheap to compute.
/// reference:
/// https://en.wikipedia.org/wiki/Midpoint_circle_algorithm
struct midpoint_circle_rasterizer
{
    using type = circle_rasterizer_t;

    /// \brief Creates a midpoint circle rasterizer
    /// \param center_point - Point containing positive integer x co-ordinate and y co-ordinate of the
    /// center respectively.
    /// \param circle_radius - Radius of the circle
    midpoint_circle_rasterizer(point_t center_point, std::ptrdiff_t circle_radius)
        : center(center_point), radius(circle_radius)
    {}

    /// \brief Calculate the amount of points that rasterizer will output
    std::ptrdiff_t point_count() const noexcept
    {
        // the reason for pulling 8 out is so that when the expression radius * cos(45 degrees)
        // is used, it would yield the same result as here
        // + 1 at the end is because the point at radius itself is computed as well
        return 8 * static_cast<std::ptrdiff_t>(
                       std::round(radius * std::cos(boost::gil::detail::pi / 4)) + 1);
    }

    /// \brief perform rasterization and output into d_first
    template <typename OutputIterator>
    void operator()(OutputIterator d_first) const
    {
        auto translate_mirror_points = [this, &d_first](point_t p) {
            *d_first++ = point_t{center.x + p.x, center.y + p.y};
            *d_first++ = point_t{center.x + p.x, center.y - p.y};
            *d_first++ = point_t{center.x - p.x, center.y + p.y};
            *d_first++ = point_t{center.x - p.x, center.y - p.y};
            *d_first++ = point_t{center.x + p.y, center.y + p.x};
            *d_first++ = point_t{center.x + p.y, center.y - p.x};
            *d_first++ = point_t{center.x - p.y, center.y + p.x};
            *d_first++ = point_t{center.x - p.y, center.y - p.x};
        };
        std::ptrdiff_t iteration_distance = point_count() / 8;
        std::ptrdiff_t y_current = radius;
        std::ptrdiff_t r_squared = radius * radius;
        translate_mirror_points({0, y_current});
        for (std::ptrdiff_t x = 1; x < iteration_distance; ++x)
        {
            std::ptrdiff_t midpoint = x * x + y_current * y_current - y_current - r_squared;
            if (midpoint > 0)
            {
                --y_current;
            }
            translate_mirror_points({x, y_current});
        }
    }

    point_t center;
    std::ptrdiff_t radius;
};

namespace detail {

template <typename View, typename Rasterizer, typename Pixel>
struct apply_rasterizer_op<View, Rasterizer, Pixel, circle_rasterizer_t>
{
    void operator()(
        View const& view, Rasterizer const& rasterizer, Pixel const& pixel)
    {
        std::vector<point_t> trajectory(rasterizer.point_count());
        rasterizer(std::begin(trajectory));

        for (auto const& point : trajectory)
        {
            view(point) = pixel;
        }
    }
};

} //namespace detail

}} // namespace boost::gil

#endif
