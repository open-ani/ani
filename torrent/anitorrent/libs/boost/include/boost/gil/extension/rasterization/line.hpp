//
// Copyright 2020 Olzhas Zhumabek <anonymous.from.applecity@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//
#ifndef BOOST_GIL_EXTENSION_RASTERIZATION_LINE_HPP
#define BOOST_GIL_EXTENSION_RASTERIZATION_LINE_HPP

#include <boost/gil/extension/rasterization/apply_rasterizer.hpp>
#include <boost/gil/point.hpp>

#include <cmath>
#include <cstddef>
#include <iterator>
#include <vector>

namespace boost { namespace gil {

struct line_rasterizer_t{};

/// \defgroup Rasterization
/// \brief A set of functions to rasterize shapes
///
/// Due to images being discrete, most shapes require specialized algorithms to
/// handle rasterization efficiently and solve problem of connectivity and being
/// close to the original shape.

/// \defgroup LineRasterization
/// \ingroup Rasterization
/// \brief A set of rasterizers for lines
///
/// The main problem with line rasterization is to do it efficiently, e.g. less
/// floating point operations. There are multiple algorithms that on paper
/// should reach the same result, but due to quirks of IEEE-754 they don't.
/// Please select one and stick to it if possible. At the moment only Bresenham
/// rasterizer is implemented.

/// \ingroup LineRasterization
/// \brief Rasterize a line according to Bresenham algorithm
///
/// Do note that if either width or height is 1, slope is set to zero.
/// reference:
/// https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm#:~:text=Bresenham's%20line%20algorithm%20is%20a,straight%20line%20between%20two%20points.
struct bresenham_line_rasterizer
{
    using type = line_rasterizer_t;

    bresenham_line_rasterizer(point_t start, point_t end)
        : start_point(start), end_point(end)
    {}

    std::ptrdiff_t point_count() const noexcept
    {
        const auto abs_width = std::abs(end_point.x - start_point.x) + 1;
        const auto abs_height = std::abs(end_point.y - start_point.y) + 1;
        return abs_width > abs_height ? abs_width : abs_height;
    }

    template <typename OutputIterator>
    void operator()(OutputIterator d_first) const
    {
        // mutable stack copies
        point_t start = start_point;
        point_t end = end_point;

        if (start == end)
        {
            // put the point and immediately exit, as later on division by zero will
            // occur
            *d_first = start;
            return;
        }

        auto width = std::abs(end.x - start.x) + 1;
        auto height = std::abs(end.y - start.y) + 1;
        bool const needs_flip = width < height;
        if (needs_flip)
        {
            // transpose the coordinate system if uncomfortable angle detected
            std::swap(width, height);
            std::swap(start.x, start.y);
            std::swap(end.x, end.y);
        }
        std::ptrdiff_t const x_increment = end.x >= start.x ? 1 : -1;
        std::ptrdiff_t const y_increment = end.y >= start.y ? 1 : -1;
        double const slope =
            height == 1 ? 0 : static_cast<double>(height) / static_cast<double>(width);
        std::ptrdiff_t y = start.y;
        double error_term = 0;
        for (std::ptrdiff_t x = start.x; x != end.x; x += x_increment)
        {
            // transpose coordinate system back to proper form if needed
            *d_first++ = needs_flip ? point_t{y, x} : point_t{x, y};
            error_term += slope;
            if (error_term >= 0.5)
            {
                --error_term;
                y += y_increment;
            }
        }
        *d_first++ = needs_flip ? point_t{end.y, end.x} : end;
    }

    point_t start_point;
    point_t end_point;
};

namespace detail {

template <typename View, typename Rasterizer, typename Pixel>
struct apply_rasterizer_op<View, Rasterizer, Pixel, line_rasterizer_t>
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
