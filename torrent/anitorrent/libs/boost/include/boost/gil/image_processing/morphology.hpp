//
// Copyright 2021 Prathamesh Tagore <prathameshtagore@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_GIL_IMAGE_PROCESSING_MORPHOLOGY_HPP
#define BOOST_GIL_IMAGE_PROCESSING_MORPHOLOGY_HPP

#include <boost/gil/image_processing/kernel.hpp>
#include <boost/gil/gray.hpp>
#include <boost/gil/image_processing/threshold.hpp>

namespace boost { namespace gil { namespace detail {

enum class morphological_operation
{
    dilation,
    erosion,
};

/// \addtogroup ImageProcessing
/// @{

/// \brief Implements morphological operations at pixel level.This function
/// compares neighbouring pixel values according to the kernel and choose
/// minimum/mamximum neighbouring pixel value and assigns it to the pixel under
/// consideration.
/// \param src_view - Source/Input image view.
/// \param dst_view - View which stores the final result of operations performed by this function.
/// \param kernel - Kernel matrix/structuring element containing 0's and 1's
/// which will be used for applying the required morphological operation.
/// \param identifier - Indicates the type of morphological operation to be applied.
/// \tparam SrcView type of source image.
/// \tparam DstView type of output image.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename DstView, typename Kernel>
void morph_impl(SrcView const& src_view, DstView const& dst_view, Kernel const& kernel,
                morphological_operation identifier)
{
    std::ptrdiff_t flip_ker_row, flip_ker_col, row_boundary, col_boundary;
    typename channel_type<typename SrcView::value_type>::type target_element;
    for (std::ptrdiff_t view_row = 0; view_row < src_view.height(); ++view_row)
    {
        for (std::ptrdiff_t view_col = 0; view_col < src_view.width(); ++view_col)
        {
            target_element = src_view(view_col, view_row);
            for (std::size_t kernel_row = 0; kernel_row < kernel.size(); ++kernel_row)
            {
                flip_ker_row = kernel.size() - 1 - kernel_row; // row index of flipped kernel

                for (std::size_t kernel_col = 0; kernel_col < kernel.size(); ++kernel_col)
                {
                    flip_ker_col = kernel.size() - 1 - kernel_col; // column index of flipped kernel

                    // We ensure that we consider only those pixels which are overlapped
                    // on a non-zero kernel_element as
                    if (kernel.at(flip_ker_row, flip_ker_col) == 0)
                    {
                        continue;
                    }
                    // index of input signal, used for checking boundary
                    row_boundary = view_row + (kernel.center_y() - flip_ker_row);
                    col_boundary = view_col + (kernel.center_x() - flip_ker_col);

                    // ignore input samples which are out of bound
                    if (row_boundary >= 0 && row_boundary < src_view.height() &&
                        col_boundary >= 0 && col_boundary < src_view.width())
                    {

                        if (identifier == morphological_operation::dilation)
                        {
                            target_element =
                                (std::max)(src_view(col_boundary, row_boundary)[0], target_element);
                        }
                        else if (identifier == morphological_operation::erosion)
                        {
                            target_element =
                                (std::min)(src_view(col_boundary, row_boundary)[0], target_element);
                        }
                    }
                }
            }
            dst_view(view_col, view_row) = target_element;
        }
    }
}

/// \brief Checks feasibility of the desired operation and passes parameter
/// values to the function morph_impl alongwith individual channel views of the
/// input image.
/// \param src_view - Source/Input image view.
/// \param dst_view - View which stores the final result of operations performed by this function.
/// \param kernel - Kernel matrix/structuring element containing 0's and 1's
/// which will be used for applying the required morphological operation.
/// \param identifier - Indicates the type of morphological operation to be applied.
/// \tparam SrcView type of source image.
/// \tparam DstView type of output image.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename DstView, typename Kernel>
void morph(SrcView const& src_view, DstView const& dst_view, Kernel const& ker_mat,
           morphological_operation identifier)
{
    BOOST_ASSERT(ker_mat.size() != 0 && src_view.dimensions() == dst_view.dimensions());
    gil_function_requires<ImageViewConcept<SrcView>>();
    gil_function_requires<MutableImageViewConcept<DstView>>();

    gil_function_requires<ColorSpacesCompatibleConcept<typename color_space_type<SrcView>::type,
                                                       typename color_space_type<DstView>::type>>();

    gil::image<typename DstView::value_type> intermediate_img(src_view.dimensions());

    for (std::size_t i = 0; i < src_view.num_channels(); i++)
    {
        morph_impl(nth_channel_view(src_view, i), nth_channel_view(view(intermediate_img), i),
                   ker_mat, identifier);
    }
    copy_pixels(view(intermediate_img), dst_view);
}

/// \brief Calculates the difference between pixel values of first image_view
/// and second image_view.
/// \param src_view1 - First parameter for subtraction of views.
/// \param src_view2 - Second parameter for subtraction of views.
/// \param diff_view - View containing result of the subtraction of second view from
/// the first view.
/// \tparam SrcView type of source/Input images used for subtraction.
/// \tparam DiffView type of image view containing the result of subtraction.
template <typename SrcView, typename DiffView>
void difference_impl(SrcView const& src_view1, SrcView const& src_view2, DiffView const& diff_view)
{
    for (std::ptrdiff_t view_row = 0; view_row < src_view1.height(); ++view_row)
        for (std::ptrdiff_t view_col = 0; view_col < src_view1.width(); ++view_col)
            diff_view(view_col, view_row) =
                src_view1(view_col, view_row) - src_view2(view_col, view_row);
}

/// \brief Passes parameter values to the function 'difference_impl' alongwith
/// individual channel views of input images.
/// \param src_view1 - First parameter for subtraction of views.
/// \param src_view2 - Second parameter for subtraction of views.
/// \param diff_view - View containing result of the subtraction of second view from the first view.
/// \tparam SrcView type of source/Input images used for subtraction.
/// \tparam DiffView type of image view containing the result of subtraction.
template <typename SrcView, typename DiffView>
void difference(SrcView const& src_view1, SrcView const& src_view2, DiffView const& diff_view)
{
    gil_function_requires<ImageViewConcept<SrcView>>();
    gil_function_requires<MutableImageViewConcept<DiffView>>();

    gil_function_requires<ColorSpacesCompatibleConcept<
        typename color_space_type<SrcView>::type, typename color_space_type<DiffView>::type>>();

    for (std::size_t i = 0; i < src_view1.num_channels(); i++)
    {
        difference_impl(nth_channel_view(src_view1, i), nth_channel_view(src_view2, i),
                        nth_channel_view(diff_view, i));
    }
}
} // namespace detail

/// \brief Applies morphological dilation on the input image view using given
/// structuring element. It gives the maximum overlapped value to the pixel
/// overlapping with the center element of structuring element. \param src_view
/// - Source/input image view.
/// \param int_op_view - view for writing output and performing intermediate operations.
/// \param ker_mat - Kernel matrix/structuring element containing 0's and 1's which will be used for
/// applying dilation.
/// \param iterations - Specifies the number of times dilation is to be applied on the input image
/// view.
/// \tparam SrcView type of source image, models gil::ImageViewConcept.
/// \tparam IntOpView type of output image, models gil::MutableImageViewConcept.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename IntOpView, typename Kernel>
void dilate(SrcView const& src_view, IntOpView const& int_op_view, Kernel const& ker_mat,
            int iterations)
{
    copy_pixels(src_view, int_op_view);
    for (int i = 0; i < iterations; ++i)
        morph(int_op_view, int_op_view, ker_mat, detail::morphological_operation::dilation);
}

/// \brief Applies morphological erosion on the input image view using given
/// structuring element. It gives the minimum overlapped value to the pixel
/// overlapping with the center element of structuring element.
/// \param src_view - Source/input image view.
/// \param int_op_view - view for writing output and performing intermediate operations.
/// \param ker_mat - Kernel matrix/structuring element containing 0's and 1's which will be used for
/// applying erosion.
/// \param iterations - Specifies the number of times erosion is to be applied on the input
/// image view.
/// \tparam SrcView type of source image, models gil::ImageViewConcept.
/// \tparam IntOpView type of output image, models gil::MutableImageViewConcept.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename IntOpView, typename Kernel>
void erode(SrcView const& src_view, IntOpView const& int_op_view, Kernel const& ker_mat,
           int iterations)
{
    copy_pixels(src_view, int_op_view);
    for (int i = 0; i < iterations; ++i)
        morph(int_op_view, int_op_view, ker_mat, detail::morphological_operation::erosion);
}

/// \brief Performs erosion and then dilation on the input image view . This
/// operation is utilized for removing noise from images.
/// \param src_view - Source/input image view.
/// \param int_op_view - view for writing output and performing intermediate operations.
/// \param ker_mat - Kernel matrix/structuring element containing 0's and 1's which will be used for
/// applying the opening operation.
/// \tparam SrcView type of source image, models gil::ImageViewConcept.
/// \tparam IntOpView type of output image, models gil::MutableImageViewConcept.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename IntOpView, typename Kernel>
void opening(SrcView const& src_view, IntOpView const& int_op_view, Kernel const& ker_mat)
{
    erode(src_view, int_op_view, ker_mat, 1);
    dilate(int_op_view, int_op_view, ker_mat, 1);
}

/// \brief Performs dilation and then erosion on the input image view which is
/// exactly opposite to the opening operation . Closing operation can be
/// utilized for closing small holes inside foreground objects.
/// \param src_view - Source/input image view.
/// \param int_op_view - view for writing output and performing intermediate operations.
/// \param ker_mat - Kernel matrix/structuring element containing 0's and 1's which will be used for
/// applying the closing operation.
/// \tparam SrcView type of source image, models gil::ImageViewConcept.
/// \tparam IntOpView type of output image, models gil::MutableImageViewConcept.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename IntOpView, typename Kernel>
void closing(SrcView const& src_view, IntOpView const& int_op_view, Kernel const& ker_mat)
{
    dilate(src_view, int_op_view, ker_mat, 1);
    erode(int_op_view, int_op_view, ker_mat, 1);
}

/// \brief Calculates the difference between image views generated after
/// applying dilation dilation and erosion on an image . The resultant image
/// will look like the outline of the object(s) present in the image.
/// \param src_view - Source/input image view.
/// \param dst_view - Destination view which will store the final result of morphological
/// gradient operation.
/// \param ker_mat - Kernel matrix/structuring element containing 0's and 1's which
/// will be used for applying the morphological gradient operation.
/// \tparam SrcView type of source image, models gil::ImageViewConcept.
/// \tparam DstView type of output image, models gil::MutableImageViewConcept.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename DstView, typename Kernel>
void morphological_gradient(SrcView const& src_view, DstView const& dst_view, Kernel const& ker_mat)
{
    using namespace boost::gil;
    gil::image<typename DstView::value_type> int_dilate(src_view.dimensions()),
        int_erode(src_view.dimensions());
    dilate(src_view, view(int_dilate), ker_mat, 1);
    erode(src_view, view(int_erode), ker_mat, 1);
    difference(view(int_dilate), view(int_erode), dst_view);
}

/// \brief Calculates the difference between input image view and the view
/// generated by opening operation on the input image view.
/// \param src_view - Source/input image view.
/// \param dst_view - Destination view which will store the final result of top hat operation.
/// \param ker_mat - Kernel matrix/structuring element containing 0's and 1's which will be used for
/// applying the top hat operation.
/// \tparam SrcView type of source image, models gil::ImageViewConcept.
/// \tparam DstView type of output image, models gil::MutableImageViewConcept.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename DstView, typename Kernel>
void top_hat(SrcView const& src_view, DstView const& dst_view, Kernel const& ker_mat)
{
    using namespace boost::gil;
    gil::image<typename DstView::value_type> int_opening(src_view.dimensions());
    opening(src_view, view(int_opening), ker_mat);
    difference(src_view, view(int_opening), dst_view);
}

/// \brief Calculates the difference between closing of the input image and
/// input image.
/// \param src_view - Source/input image view.
/// \param dst_view - Destination view which will store the final result of black hat operation.
/// \param ker_mat - Kernel matrix/structuring element containing 0's and 1's
/// which will be used for applying the black hat operation.
/// \tparam SrcView type of source image, models gil::ImageViewConcept.
/// \tparam DstView type of output image, models gil::MutableImageViewConcept.
/// \tparam Kernel type of structuring element.
template <typename SrcView, typename DstView, typename Kernel>
void black_hat(SrcView const& src_view, DstView const& dst_view, Kernel const& ker_mat)
{
    using namespace boost::gil;
    gil::image<typename DstView::value_type> int_closing(src_view.dimensions());
    closing(src_view, view(int_closing), ker_mat);
    difference(view(int_closing), src_view, dst_view);
}
/// @}
}}     // namespace boost::gil
#endif // BOOST_GIL_IMAGE_PROCESSING_MORPHOLOGY_HPP
