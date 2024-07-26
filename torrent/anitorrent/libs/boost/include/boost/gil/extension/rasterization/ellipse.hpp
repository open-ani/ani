//
// Copyright 2021 Prathamesh Tagore <prathameshtagore@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//
#ifndef BOOST_GIL_EXTENSION_RASTERIZATION_ELLIPSE_HPP
#define BOOST_GIL_EXTENSION_RASTERIZATION_ELLIPSE_HPP

#include <boost/gil/concepts/pixel.hpp>
#include <boost/gil/extension/rasterization/apply_rasterizer.hpp>
#include <boost/gil/point.hpp>

#include <array>
#include <stdexcept>
#include <vector>

namespace boost { namespace gil {

struct ellipse_rasterizer_t{};

/// \defgroup EllipseRasterization
/// \ingroup Rasterization
/// \brief Ellipse rasterization algorithms.

/// \ingroup EllipseRasterization
/// \brief Performs ellipse rasterization using midpoint algorithm. Initially, program considers
/// origin as center of ellipse and obtains first quadrant trajectory points. After that,
/// it shifts origin to provided co-ordinates of center and then draws the curve.
struct midpoint_ellipse_rasterizer
{
    using type = ellipse_rasterizer_t;

    /// \brief Creates a midpoint ellipse rasterizer
    /// \param center - Point containing positive integer x co-ordinate and y co-ordinate of the
    /// center respectively.
    /// \param semi_axes - Point containing positive integer lengths of horizontal semi-axis
    /// and vertical semi-axis respectively.
    midpoint_ellipse_rasterizer(point<unsigned int> center_point,
        point<unsigned int> semi_axes_values)
        : center(center_point)
        , semi_axes(semi_axes_values)
    {}

    /// \brief Returns a vector containing co-ordinates of first quadrant points which lie on
    /// rasterizer trajectory of the ellipse.
    auto obtain_trajectory() const
        -> std::vector<point_t>
    {
        // Citation : J. Van Aken, "An Efficient Ellipse-Drawing Algorithm" in IEEE Computer
        // Graphics and Applications, vol. 4, no. 09, pp. 24-35, 1984.
        // doi: 10.1109/MCG.1984.275994
        // keywords: {null}
        // url: https://doi.ieeecomputersociety.org/10.1109/MCG.1984.275994
        std::vector<point_t> trajectory_points;
        std::ptrdiff_t x = semi_axes[0], y = 0;

        // Variables declared on following lines are temporary variables used for improving
        // performance since they help in converting all multiplicative operations inside the while
        // loop into additive/subtractive operations.
        long long int const t1 = semi_axes[0] * semi_axes[0];
        long long int const t4 = semi_axes[1] * semi_axes[1];
        long long int t2, t3, t5, t6, t8, t9;
        t2 = 2 * t1, t3 = 2 * t2;
        t5 = 2 * t4, t6 = 2 * t5;
        long long int const t7 = semi_axes[0] * t5;
        t8 = 2 * t7, t9 = 0;

        // Following variables serve as decision parameters and help in choosing the right point
        // to be included in rasterizer trajectory.
        long long int d1, d2;
        d1 = t2 - t7 + t4 / 2, d2 = t1 / 2 - t8 + t5;

        while (d2 < 0)
        {
            trajectory_points.push_back({x, y});
            y += 1;
            t9 += t3;
            if (d1 < 0)
            {
                d1 += t9 + t2;
                d2 += t9;
            }
            else
            {
                x -= 1;
                t8 -= t6;
                d1 += t9 + t2 - t8;
                d2 += t5 + t9 - t8;
            }
        }
        while (x >= 0)
        {
            trajectory_points.push_back({x, y});
            x -= 1;
            t8 -= t6;
            if (d2 < 0)
            {
                y += 1;
                t9 += t3;
                d2 += t5 + t9 - t8;
            }
            else
            {
                d2 += t5 - t8;
            }
        }
        return trajectory_points;
    }

    /// \brief Fills pixels returned by function 'obtain_trajectory' as well as pixels
    /// obtained from their reflection along major axis, minor axis and line passing through
    /// center with slope -1 using colours provided by user.
    /// \param view - Gil view of image on which the elliptical curve is to be drawn.
    /// \param pixel - Pixel value for the elliptical curve to be drawn.
    /// \param trajectory_points - Constant vector specifying pixel co-ordinates of points lying
    ///                            on rasterizer trajectory.
    /// \tparam View - Type of input image view.
    /// \tparam Pixel - Type of pixel. Must be compatible to the pixel type of the image view
    template<typename View, typename Pixel>
    void draw_curve(View& view, Pixel const& pixel,
        std::vector<point_t> const& trajectory_points) const
    {
        using pixel_t = typename View::value_type;
        if (!pixels_are_compatible<pixel_t, Pixel>())
        {
            throw std::runtime_error("Pixel type of the given image is not compatible to the "
                "type of the provided pixel.");
        }

        // mutable center copy
        point<unsigned int> center2(center);
        --center2[0], --center2[1]; // For converting center co-ordinate values to zero based indexing.
        for (point_t pnt : trajectory_points)
        {
            std::array<std::ptrdiff_t, 4> co_ords = {center2[0] + pnt[0],
            center2[0] - pnt[0], center2[1] + pnt[1], center2[1] - pnt[1]
            };
            bool validity[4]{};
            if (co_ords[0] < view.width())
            {
                validity[0] = true;
            }
            if (co_ords[1] >= 0 && co_ords[1] < view.width())
            {
                validity[1] = true;
            }
            if (co_ords[2] < view.height())
            {
                validity[2] = true;
            }
            if (co_ords[3] >= 0 && co_ords[3] < view.height())
            {
                validity[3] = true;
            }

            if (validity[0] && validity[2])
            {
                view(co_ords[0], co_ords[2]) = pixel;
            }
            if (validity[1] && validity[2])
            {
                view(co_ords[1], co_ords[2]) = pixel;
            }
            if (validity[1] && validity[3])
            {
                view(co_ords[1], co_ords[3]) = pixel;
            }
            if (validity[0] && validity[3])
            {
                view(co_ords[0], co_ords[3]) = pixel;
            }
        }
    }

    /// \brief Calls the function 'obtain_trajectory' and then passes obtained trajectory points
    ///        in the function 'draw_curve' for drawing the desired ellipse.
    /// \param view - Gil view of image on which the elliptical curve is to be drawn.
    /// \param pixel - Pixel value for the elliptical curve to be drawn.
    /// \tparam View - Type of input image view.
    /// \tparam Pixel - Type of pixel. Must be compatible to the pixel type of the image view
    template<typename View, typename Pixel>
    void operator()(View& view, Pixel const& pixel) const
    {
        draw_curve(view, pixel, obtain_trajectory());
    }

    point<unsigned int> center;
    point<unsigned int> semi_axes;
};

namespace detail {

template <typename View, typename Rasterizer, typename Pixel>
struct apply_rasterizer_op<View, Rasterizer, Pixel, ellipse_rasterizer_t>
{
    void operator()(
        View const& view, Rasterizer const& rasterizer, Pixel const& pixel)
    {
        rasterizer(view, pixel);
    }
};

} //namespace detail

}} // namespace boost::gil

#endif
