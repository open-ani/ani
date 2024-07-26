//
// Copyright 2005-2007 Adobe Systems Incorporated
// Copyright 2019 Miral Shah <miralshah2211@gmail.com>
// Copyright 2019-2021 Pranam Lashkari <plashkari628@gmail.com>
//
// Distributed under the Boost Software License, Version 1.0
// See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt
//
#ifndef BOOST_GIL_IMAGE_PROCESSING_CONVOLVE_HPP
#define BOOST_GIL_IMAGE_PROCESSING_CONVOLVE_HPP

#include <boost/gil/image_processing/kernel.hpp>

#include <boost/gil/algorithm.hpp>
#include <boost/gil/image_view_factory.hpp>
#include <boost/gil/metafunctions.hpp>
#include <boost/gil/pixel_numeric_operations.hpp>

#include <boost/assert.hpp>

#include <algorithm>
#include <cstddef>
#include <functional>
#include <type_traits>
#include <vector>

namespace boost { namespace gil {

// 2D spatial seperable convolutions and cross-correlations

namespace detail {

/// \brief Computes the cross-correlation of 1D kernel with rows of an image.
/// \tparam PixelAccum - Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView - Specifies the type of gil view of source image which is to be row correlated
/// with the kernel.
/// \tparam Kernel - Specifies the type of 1D kernel which will be row correlated with source image.
/// \tparam DstView -  Specifies the type of gil view which will store the result of row
/// correlation between source image and kernel.
/// \tparam Correlator - Specifies the type of correlator which should be used for performing
/// correlation.
/// \param src_view - Gil view of source image used in correlation.
/// \param kernel - 1D kernel which will be correlated with source image.
/// \param dst_view - Gil view which will store the result of row correlation between "src_view"
/// and "kernel".
/// \param option - Specifies the manner in which boundary pixels of "dst_view" should be computed.
/// \param correlator - Correlator which will be used for performing correlation.
template
<
    typename PixelAccum,
    typename SrcView,
    typename Kernel,
    typename DstView,
    typename Correlator
>
void correlate_rows_impl(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option,
    Correlator correlator)
{
    BOOST_ASSERT(src_view.dimensions() == dst_view.dimensions());
    BOOST_ASSERT(kernel.size() != 0);

    if(kernel.size() == 1)
    {
        // Reduces to a multiplication
        view_multiplies_scalar<PixelAccum>(src_view, *kernel.begin(), dst_view);
        return;
    }

    using src_pixel_ref_t = typename pixel_proxy<typename SrcView::value_type>::type;
    using dst_pixel_ref_t = typename pixel_proxy<typename DstView::value_type>::type;
    using x_coord_t = typename SrcView::x_coord_t;
    using y_coord_t = typename SrcView::y_coord_t;

    x_coord_t const width = src_view.width();
    y_coord_t const height = src_view.height();
    if (width == 0)
        return;

    PixelAccum acc_zero;
    pixel_zeros_t<PixelAccum>()(acc_zero);
    if (option == boundary_option::output_ignore || option == boundary_option::output_zero)
    {
        typename DstView::value_type dst_zero;
        pixel_assigns_t<PixelAccum, dst_pixel_ref_t>()(acc_zero, dst_zero);
        if (width < static_cast<x_coord_t>(kernel.size()))
        {
            if (option == boundary_option::output_zero)
                fill_pixels(dst_view, dst_zero);
        }
        else
        {
            std::vector<PixelAccum> buffer(width);
            for (y_coord_t y = 0; y < height; ++y)
            {
                assign_pixels(src_view.row_begin(y), src_view.row_end(y), &buffer.front());
                typename DstView::x_iterator it_dst = dst_view.row_begin(y);
                if (option == boundary_option::output_zero)
                    std::fill_n(it_dst, kernel.left_size(), dst_zero);
                it_dst += kernel.left_size();
                correlator(&buffer.front(), &buffer.front() + width + 1 - kernel.size(),
                           kernel.begin(), it_dst);
                it_dst += width + 1 - kernel.size();
                if (option == boundary_option::output_zero)
                    std::fill_n(it_dst, kernel.right_size(), dst_zero);
            }
        }
    }
    else
    {
        std::vector<PixelAccum> buffer(width + kernel.size() - 1);
        for (y_coord_t y = 0; y < height; ++y)
        {
            PixelAccum *it_buffer = &buffer.front();
            if (option == boundary_option::extend_padded)
            {
                assign_pixels(
                    src_view.row_begin(y) - kernel.left_size(),
                    src_view.row_end(y) + kernel.right_size(),
                    it_buffer);
            }
            else if (option == boundary_option::extend_zero)
            {
                std::fill_n(it_buffer, kernel.left_size(), acc_zero);
                it_buffer += kernel.left_size();
                assign_pixels(src_view.row_begin(y), src_view.row_end(y), it_buffer);
                it_buffer += width;
                std::fill_n(it_buffer, kernel.right_size(), acc_zero);
            }
            else if (option == boundary_option::extend_constant)
            {
                PixelAccum filler;
                pixel_assigns_t<src_pixel_ref_t, PixelAccum>()(*src_view.row_begin(y), filler);
                std::fill_n(it_buffer, kernel.left_size(), filler);
                it_buffer += kernel.left_size();
                assign_pixels(src_view.row_begin(y), src_view.row_end(y), it_buffer);
                it_buffer += width;
                pixel_assigns_t<src_pixel_ref_t, PixelAccum>()(src_view.row_end(y)[-1], filler);
                std::fill_n(it_buffer, kernel.right_size(), filler);
            }

            correlator(
                &buffer.front(), &buffer.front() + width,
                kernel.begin(),
                dst_view.row_begin(y));
        }
    }
}

/// \brief Provides functionality for performing 1D correlation between the kernel and a buffer
/// storing row pixels of source image. Kernel size is to be provided through constructor for all
/// instances.
template <typename PixelAccum>
class correlator_n
{
public:
    correlator_n(std::size_t size) : size_(size) {}

    template <typename SrcIterator, typename KernelIterator, typename DstIterator>
    void operator()(
        SrcIterator src_begin,
        SrcIterator src_end,
        KernelIterator kernel_begin,
        DstIterator dst_begin)
    {
        correlate_pixels_n<PixelAccum>(src_begin, src_end, kernel_begin, size_, dst_begin);
    }

private:
    std::size_t size_{0};
};

/// \brief Provides functionality for performing 1D correlation between the kernel and a buffer
/// storing row pixels of source image. Kernel size is a template parameter and must be
/// compulsorily specified while using.
template <std::size_t Size, typename PixelAccum>
struct correlator_k
{
    template <typename SrcIterator, typename KernelIterator, typename DstIterator>
    void operator()(
        SrcIterator src_begin,
        SrcIterator src_end,
        KernelIterator kernel_begin,
        DstIterator dst_begin)
    {
        correlate_pixels_k<Size, PixelAccum>(src_begin, src_end, kernel_begin, dst_begin);
    }
};

} // namespace detail

/// \ingroup ImageAlgorithms
/// \brief Correlate 1D variable-size kernel along the rows of image.
/// \tparam PixelAccum Specifies tha data type which will be used while creating buffer container
/// which is utilized for holding source image pixels after applying appropriate boundary
/// manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be row correlated with source image.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum, typename SrcView, typename Kernel, typename DstView>
BOOST_FORCEINLINE
void correlate_rows(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    detail::correlate_rows_impl<PixelAccum>(
        src_view, kernel, dst_view, option, detail::correlator_n<PixelAccum>(kernel.size()));
}

/// \ingroup ImageAlgorithms
/// \brief Correlates 1D variable-size kernel along the columns of image.
/// \tparam PixelAccum Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be column correlated with source
/// image.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum, typename SrcView, typename Kernel, typename DstView>
BOOST_FORCEINLINE
void correlate_cols(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    correlate_rows<PixelAccum>(
        transposed_view(src_view), kernel, transposed_view(dst_view), option);
}

/// \ingroup ImageAlgorithms
/// \brief Convolves 1D variable-size kernel along the rows of image.
/// \tparam PixelAccum Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be row convoluted with source image.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum, typename SrcView, typename Kernel, typename DstView>
BOOST_FORCEINLINE
void convolve_rows(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    correlate_rows<PixelAccum>(src_view, reverse_kernel(kernel), dst_view, option);
}

/// \ingroup ImageAlgorithms
/// \brief Convolves 1D variable-size kernel along the columns of image.
/// \tparam PixelAccum Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be column convoluted with source
/// image.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum, typename SrcView, typename Kernel, typename DstView>
BOOST_FORCEINLINE
void convolve_cols(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    convolve_rows<PixelAccum>(
        transposed_view(src_view), kernel, transposed_view(dst_view), option);
}

/// \ingroup ImageAlgorithms
/// \brief Correlate 1D fixed-size kernel along the rows of image.
/// \tparam PixelAccum Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be row correlated with source image.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum, typename SrcView, typename Kernel, typename DstView>
BOOST_FORCEINLINE
void correlate_rows_fixed(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    using correlator = detail::correlator_k<Kernel::static_size, PixelAccum>;
    detail::correlate_rows_impl<PixelAccum>(src_view, kernel, dst_view, option, correlator{});
}

/// \ingroup ImageAlgorithms
/// \brief Correlate 1D fixed-size kernel along the columns of image
/// \tparam PixelAccum Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be column correlated with source
/// image.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum,typename SrcView,typename Kernel,typename DstView>
BOOST_FORCEINLINE
void correlate_cols_fixed(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    correlate_rows_fixed<PixelAccum>(
        transposed_view(src_view), kernel, transposed_view(dst_view), option);
}

/// \ingroup ImageAlgorithms
/// \brief Convolve 1D fixed-size kernel along the rows of image
/// \tparam PixelAccum Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be row convolved with source image.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum, typename SrcView, typename Kernel, typename DstView>
BOOST_FORCEINLINE
void convolve_rows_fixed(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    correlate_rows_fixed<PixelAccum>(src_view, reverse_kernel(kernel), dst_view, option);
}

/// \ingroup ImageAlgorithms
/// \brief Convolve 1D fixed-size kernel along the columns of image
/// \tparam PixelAccum Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be column convolved with source
/// image.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum, typename SrcView, typename Kernel, typename DstView>
BOOST_FORCEINLINE
void convolve_cols_fixed(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    convolve_rows_fixed<PixelAccum>(
        transposed_view(src_view), kernel, transposed_view(dst_view), option);
}

namespace detail
{

/// \ingroup ImageAlgorithms
/// \brief Convolve 1D variable-size kernel along both rows and columns of image
/// \tparam PixelAccum Specifies tha data type which will be used for creating buffer container
/// utilized for holding source image pixels after applying appropriate boundary manipulations.
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 1D kernel which will be used for 1D row and column
/// convolution.
/// \tparam DstView Models MutableImageViewConcept
template <typename PixelAccum, typename SrcView, typename Kernel, typename DstView>
BOOST_FORCEINLINE
void convolve_1d(
    SrcView const& src_view,
    Kernel const& kernel,
    DstView const& dst_view,
    boundary_option option = boundary_option::extend_zero)
{
    convolve_rows<PixelAccum>(src_view, kernel, dst_view, option);
    convolve_cols<PixelAccum>(dst_view, kernel, dst_view, option);
}

template <typename SrcView, typename DstView, typename Kernel>
void convolve_2d_impl(SrcView const& src_view, DstView const& dst_view, Kernel const& kernel)
{
    int flip_ker_row, flip_ker_col, row_boundary, col_boundary;
    float aux_total;
    for (std::ptrdiff_t view_row = 0; view_row < src_view.height(); ++view_row)
    {
        for (std::ptrdiff_t view_col = 0; view_col < src_view.width(); ++view_col)
        {
            aux_total = 0.0f;
            for (std::size_t kernel_row = 0; kernel_row < kernel.size(); ++kernel_row)
            {
                flip_ker_row = kernel.size() - 1 - kernel_row;      // row index of flipped kernel

                for (std::size_t kernel_col = 0; kernel_col < kernel.size(); ++kernel_col)
                {
                    flip_ker_col = kernel.size() - 1 - kernel_col; // column index of flipped kernel

                    // index of input signal, used for checking boundary
                    row_boundary = view_row + (kernel.center_y() - flip_ker_row);
                    col_boundary = view_col + (kernel.center_x() - flip_ker_col);

                    // ignore input samples which are out of bound
                    if (row_boundary >= 0 && row_boundary < src_view.height() &&
                        col_boundary >= 0 && col_boundary < src_view.width())
                    {
                        aux_total +=
                            src_view(col_boundary, row_boundary)[0] *
                            kernel.at(flip_ker_row, flip_ker_col);
                    }
                }
            }
            dst_view(view_col, view_row) = aux_total;
        }
    }
}

/// \ingroup ImageAlgorithms
/// \brief convolve_2d can only use convolve_option_extend_zero as convolve_boundary_option
///  this is the default option and cannot be changed for now
///  (In future there are plans to improve the algorithm and allow user to use other options as well)
/// \tparam SrcView Models ImageViewConcept
/// \tparam Kernel Specifies the type of 2D kernel which will be used while convolution.
/// \tparam DstView Models MutableImageViewConcept
template <typename SrcView, typename DstView, typename Kernel>
void convolve_2d(SrcView const& src_view, Kernel const& kernel, DstView const& dst_view)
{
    BOOST_ASSERT(src_view.dimensions() == dst_view.dimensions());
    BOOST_ASSERT(kernel.size() != 0);

    gil_function_requires<ImageViewConcept<SrcView>>();
    gil_function_requires<MutableImageViewConcept<DstView>>();
    static_assert(color_spaces_are_compatible
    <
        typename color_space_type<SrcView>::type,
        typename color_space_type<DstView>::type
    >::value, "Source and destination views must have pixels with the same color space");

    for (std::size_t i = 0; i < src_view.num_channels(); i++)
    {
        detail::convolve_2d_impl(
            nth_channel_view(src_view, i),
            nth_channel_view(dst_view, i),
            kernel
        );
    }
}

}}} // namespace boost::gil::detail

#endif
