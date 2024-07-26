//
// Copyright 2020 Olzhas Zhumabek <anonymous.from.applecity@gmail.com>
// Copyright 2021 Pranam Lashkari <plashkari628@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_GIL_EXTENSION_IMAGE_PROCESSING_DIFFUSION_HPP
#define BOOST_GIL_EXTENSION_IMAGE_PROCESSING_DIFFUSION_HPP

#include <boost/gil/detail/math.hpp>
#include <boost/gil/algorithm.hpp>
#include <boost/gil/color_base_algorithm.hpp>
#include <boost/gil/image.hpp>
#include <boost/gil/image_view.hpp>
#include <boost/gil/image_view_factory.hpp>
#include <boost/gil/pixel.hpp>
#include <boost/gil/point.hpp>
#include <boost/gil/typedefs.hpp>

#include <functional>
#include <numeric>
#include <vector>

namespace boost { namespace gil {
namespace conductivity {
struct perona_malik_conductivity
{
    double kappa;
    template <typename Pixel>
    Pixel operator()(Pixel input)
    {
        using channel_type = typename channel_type<Pixel>::type;
        // C++11 doesn't seem to capture members
        static_transform(input, input, [this](channel_type value) {
            value /= kappa;
            return std::exp(-std::abs(value));
        });

        return input;
    }
};

struct gaussian_conductivity
{
    double kappa;
    template <typename Pixel>
    Pixel operator()(Pixel input)
    {
        using channel_type = typename channel_type<Pixel>::type;
        // C++11 doesn't seem to capture members
        static_transform(input, input, [this](channel_type value) {
            value /= kappa;
            return std::exp(-value * value);
        });

        return input;
    }
};

struct wide_regions_conductivity
{
    double kappa;
    template <typename Pixel>
    Pixel operator()(Pixel input)
    {
        using channel_type = typename channel_type<Pixel>::type;
        // C++11 doesn't seem to capture members
        static_transform(input, input, [this](channel_type value) {
            value /= kappa;
            return 1.0 / (1.0 + value * value);
        });

        return input;
    }
};

struct more_wide_regions_conductivity
{
    double kappa;
    template <typename Pixel>
    Pixel operator()(Pixel input)
    {
        using channel_type = typename channel_type<Pixel>::type;
        // C++11 doesn't seem to capture members
        static_transform(input, input, [this](channel_type value) {
            value /= kappa;
            return 1.0 / std::sqrt((1.0 + value * value));
        });

        return input;
    }
};
} // namespace diffusion

/**
    \brief contains discrete approximations of 2D Laplacian operator
*/
namespace laplace_function {
// The functions assume clockwise enumeration of stencil points, as such
// NW   North NE          0 1 2      (-1, -1) (0, -1) (+1, -1)
// West       East   ===> 7   3 ===> (-1, 0)          (+1, 0)
// SW   South SE          6 5 4      (-1, +1) (0, +1) (+1, +1)

/**
    \brief This function makes sure all Laplace functions enumerate
    values in the same order and direction.

    The first element is difference North West direction, second in North,
    and so on in clockwise manner. Leave element as zero if it is not
    to be computed.
*/
inline std::array<gil::point_t, 8> get_directed_offsets()
{
    return {point_t{-1, -1}, point_t{0, -1}, point_t{+1, -1}, point_t{+1, 0},
            point_t{+1, +1}, point_t{0, +1}, point_t{-1, +1}, point_t{-1, 0}};
}

template <typename PixelType>
using stencil_type = std::array<PixelType, 8>;

/**
    \brief 5 point stencil approximation of Laplacian

    Only main 4 directions are non-zero, the rest are zero
*/
struct stencil_5points
{
    double delta_t = 0.25;

    template <typename SubImageView>
    stencil_type<typename SubImageView::value_type> compute_laplace(SubImageView view,
                                                                    point_t origin)
    {
        auto current = view(origin);
        stencil_type<typename SubImageView::value_type> stencil;
        using channel_type = typename channel_type<typename SubImageView::value_type>::type;
        std::array<gil::point_t, 8> offsets(get_directed_offsets());
        typename SubImageView::value_type zero_pixel;
        static_fill(zero_pixel, 0);
        for (std::size_t index = 0; index < offsets.size(); ++index)
        {
            if (index % 2 != 0)
            {
                static_transform(view(origin.x + offsets[index].x, origin.y + offsets[index].y),
                                 current, stencil[index], std::minus<channel_type>{});
            }
            else
            {
                stencil[index] = zero_pixel;
            }
        }
        return stencil;
    }

    template <typename Pixel>
    Pixel reduce(const stencil_type<Pixel>& stencil)
    {
        using channel_type = typename channel_type<Pixel>::type;
        auto result = []() {
            Pixel zero_pixel;
            static_fill(zero_pixel, channel_type(0));
            return zero_pixel;
        }();

        for (std::size_t index : {1u, 3u, 5u, 7u})
        {
            static_transform(result, stencil[index], result, std::plus<channel_type>{});
        }
        Pixel delta_t_pixel;
        static_fill(delta_t_pixel, delta_t);
        static_transform(result, delta_t_pixel, result, std::multiplies<channel_type>{});

        return result;
    }
};

/**
    \brief 9 point stencil approximation of Laplacian

    This is full 8 way approximation, though diagonal
    elements are halved during reduction.
*/
struct stencil_9points_standard
{
    double delta_t = 0.125;

    template <typename SubImageView>
    stencil_type<typename SubImageView::value_type> compute_laplace(SubImageView view,
                                                                    point_t origin)
    {
        stencil_type<typename SubImageView::value_type> stencil;
        auto out = stencil.begin();
        auto current = view(origin);
        using channel_type = typename channel_type<typename SubImageView::value_type>::type;
        std::array<gil::point_t, 8> offsets(get_directed_offsets());
        for (auto offset : offsets)
        {
            static_transform(view(origin.x + offset.x, origin.y + offset.y), current, *out++,
                             std::minus<channel_type>{});
        }

        return stencil;
    }

    template <typename Pixel>
    Pixel reduce(const stencil_type<Pixel>& stencil)
    {
        using channel_type = typename channel_type<Pixel>::type;
        auto result = []() {
            Pixel zero_pixel;
            static_fill(zero_pixel, channel_type(0));
            return zero_pixel;
        }();
        for (std::size_t index : {1u, 3u, 5u, 7u})
        {
            static_transform(result, stencil[index], result, std::plus<channel_type>{});
        }

        for (std::size_t index : {0u, 2u, 4u, 6u})
        {
            Pixel half_pixel;
            static_fill(half_pixel, channel_type(1 / 2.0));
            static_transform(stencil[index], half_pixel, half_pixel,
                             std::multiplies<channel_type>{});
            static_transform(result, half_pixel, result, std::plus<channel_type>{});
        }

        Pixel delta_t_pixel;
        static_fill(delta_t_pixel, delta_t);
        static_transform(result, delta_t_pixel, result, std::multiplies<channel_type>{});

        return result;
    }
};
} // namespace laplace_function

namespace brightness_function {
using laplace_function::stencil_type;
struct identity
{
    template <typename Pixel>
    stencil_type<Pixel> operator()(const stencil_type<Pixel>& stencil)
    {
        return stencil;
    }
};

// TODO: Figure out how to implement color gradient brightness, as it
// seems to need dx and dy using sobel or scharr kernels

struct rgb_luminance
{
    using pixel_type = rgb32f_pixel_t;
    stencil_type<pixel_type> operator()(const stencil_type<pixel_type>& stencil)
    {
        stencil_type<pixel_type> output;
        std::transform(stencil.begin(), stencil.end(), output.begin(), [](const pixel_type& pixel) {
            float32_t luminance = 0.2126f * pixel[0] + 0.7152f * pixel[1] + 0.0722f * pixel[2];
            pixel_type result_pixel;
            static_fill(result_pixel, luminance);
            return result_pixel;
        });
        return output;
    }
};

} // namespace brightness_function

enum class matlab_connectivity
{
    minimal,
    maximal
};

enum class matlab_conduction_method
{
    exponential,
    quadratic
};

template <typename InputView, typename OutputView>
void classic_anisotropic_diffusion(const InputView& input, const OutputView& output,
                                   unsigned int num_iter, double kappa)
{
    anisotropic_diffusion(input, output, num_iter, laplace_function::stencil_5points{},
                          brightness_function::identity{},
                          conductivity::perona_malik_conductivity{kappa});
}

template <typename InputView, typename OutputView>
void matlab_anisotropic_diffusion(const InputView& input, const OutputView& output,
                                  unsigned int num_iter, double kappa,
                                  matlab_connectivity connectivity,
                                  matlab_conduction_method conduction_method)
{
    if (connectivity == matlab_connectivity::minimal)
    {
        if (conduction_method == matlab_conduction_method::exponential)
        {
            anisotropic_diffusion(input, output, num_iter, laplace_function::stencil_5points{},
                                  brightness_function::identity{},
                                  conductivity::gaussian_conductivity{kappa});
        }
        else if (conduction_method == matlab_conduction_method::quadratic)
        {
            anisotropic_diffusion(input, output, num_iter, laplace_function::stencil_5points{},
                                  brightness_function::identity{},
                                  conductivity::gaussian_conductivity{kappa});
        }
        else
        {
            throw std::logic_error("unhandled conduction method found");
        }
    }
    else if (connectivity == matlab_connectivity::maximal)
    {
        if (conduction_method == matlab_conduction_method::exponential)
        {
            anisotropic_diffusion(input, output, num_iter, laplace_function::stencil_5points{},
                                  brightness_function::identity{},
                                  conductivity::gaussian_conductivity{kappa});
        }
        else if (conduction_method == matlab_conduction_method::quadratic)
        {
            anisotropic_diffusion(input, output, num_iter, laplace_function::stencil_5points{},
                                  brightness_function::identity{},
                                  conductivity::gaussian_conductivity{kappa});
        }
        else
        {
            throw std::logic_error("unhandled conduction method found");
        }
    }
    else
    {
        throw std::logic_error("unhandled connectivity found");
    }
}

template <typename InputView, typename OutputView>
void default_anisotropic_diffusion(const InputView& input, const OutputView& output,
                                   unsigned int num_iter, double kappa)
{
    anisotropic_diffusion(input, output, num_iter, laplace_function::stencil_9points_standard{},
                          brightness_function::identity{}, conductivity::gaussian_conductivity{kappa});
}

/// \brief Performs diffusion according to Perona-Malik equation
///
/// WARNING: Output channel type must be floating point,
/// otherwise there will be loss in accuracy which most
/// probably will lead to incorrect results (input will be unchanged).
/// Anisotropic diffusion is a smoothing algorithm that respects
/// edge boundaries and can work as an edge detector if suitable
/// iteration count is set and grayscale image view is used
/// as an input
template <typename InputView, typename OutputView,
          typename LaplaceStrategy = laplace_function::stencil_9points_standard,
          typename BrightnessFunction = brightness_function::identity,
          typename DiffusivityFunction = conductivity::gaussian_conductivity>
void anisotropic_diffusion(const InputView& input, const OutputView& output, unsigned int num_iter,
                           LaplaceStrategy laplace, BrightnessFunction brightness,
                           DiffusivityFunction diffusivity)
{
    using input_pixel_type = typename InputView::value_type;
    using pixel_type = typename OutputView::value_type;
    using channel_type = typename channel_type<pixel_type>::type;
    using computation_image = image<pixel_type>;
    const auto width = input.width();
    const auto height = input.height();
    const auto zero_pixel = []() {
        pixel_type pixel;
        static_fill(pixel, static_cast<channel_type>(0));

        return pixel;
    }();
    computation_image result_image(width + 2, height + 2, zero_pixel);
    auto result = view(result_image);
    computation_image scratch_result_image(width + 2, height + 2, zero_pixel);
    auto scratch_result = view(scratch_result_image);
    transform_pixels(input, subimage_view(result, 1, 1, width, height),
                     [](const input_pixel_type& pixel) {
                         pixel_type converted;
                         for (std::size_t i = 0; i < num_channels<pixel_type>{}; ++i)
                         {
                             converted[i] = pixel[i];
                         }
                         return converted;
                     });

    for (unsigned int iteration = 0; iteration < num_iter; ++iteration)
    {
        for (std::ptrdiff_t relative_y = 0; relative_y < height; ++relative_y)
        {
            for (std::ptrdiff_t relative_x = 0; relative_x < width; ++relative_x)
            {
                auto x = relative_x + 1;
                auto y = relative_y + 1;
                auto stencil = laplace.compute_laplace(result, point_t(x, y));
                auto brightness_stencil = brightness(stencil);
                laplace_function::stencil_type<pixel_type> diffusivity_stencil;
                std::transform(brightness_stencil.begin(), brightness_stencil.end(),
                               diffusivity_stencil.begin(), diffusivity);
                laplace_function::stencil_type<pixel_type> product_stencil;
                std::transform(stencil.begin(), stencil.end(), diffusivity_stencil.begin(),
                               product_stencil.begin(), [](pixel_type lhs, pixel_type rhs) {
                                   static_transform(lhs, rhs, lhs, std::multiplies<channel_type>{});
                                   return lhs;
                               });
                static_transform(result(x, y), laplace.reduce(product_stencil),
                                 scratch_result(x, y), std::plus<channel_type>{});
            }
        }
        using std::swap;
        swap(result, scratch_result);
    }

    copy_pixels(subimage_view(result, 1, 1, width, height), output);
}

}} // namespace boost::gil

#endif
