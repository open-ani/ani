//
// Copyright 2020 Debabrata Mandal <mandaldebabrata123@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_GIL_IMAGE_PROCESSING_ADAPTIVE_HISTOGRAM_EQUALIZATION_HPP
#define BOOST_GIL_IMAGE_PROCESSING_ADAPTIVE_HISTOGRAM_EQUALIZATION_HPP

#include <boost/gil/algorithm.hpp>
#include <boost/gil/histogram.hpp>
#include <boost/gil/image.hpp>
#include <boost/gil/image_processing/histogram_equalization.hpp>
#include <boost/gil/image_view_factory.hpp>

#include <cmath>
#include <map>
#include <vector>

namespace boost { namespace gil {

/////////////////////////////////////////
/// Adaptive Histogram Equalization(AHE)
/////////////////////////////////////////
/// \defgroup AHE AHE
/// \brief Contains implementation and description of the algorithm used to compute
///        adaptive histogram equalization of input images. Naming for the AHE functions
///        are done in the following way
///             <feature-1>_<feature-2>_.._<feature-n>ahe
///        For example, for AHE done using local (non-overlapping) tiles/blocks and
///        final output interpolated among tiles , it is called
///             non_overlapping_interpolated_clahe
///

namespace detail {

/// \defgroup AHE-helpers AHE-helpers
/// \brief AHE helper functions

/// \fn double actual_clip_limit
/// \ingroup AHE-helpers
/// \brief Computes the actual clip limit given a clip limit value using binary search.
///        Reference -  Adaptive Histogram Equalization and Its Variations
///                     (http://www.cs.unc.edu/techreports/86-013.pdf, Pg - 15)
///
template <typename SrcHist>
double actual_clip_limit(SrcHist const& src_hist, double cliplimit = 0.03)
{
    double epsilon       = 1.0;
    using value_t        = typename SrcHist::value_type;
    double sum           = src_hist.sum();
    std::size_t num_bins = src_hist.size();

    cliplimit = sum * cliplimit;
    long low = 0, high = cliplimit, middle = low;
    while (high - low >= 1)
    {
        middle      = (low + high + 1) >> 1;
        long excess = 0;
        std::for_each(src_hist.begin(), src_hist.end(), [&](value_t const& v) {
            if (v.second > middle)
                excess += v.second - middle;
        });
        if (std::abs(excess - (cliplimit - middle) * num_bins) < epsilon)
            break;
        else if (excess > (cliplimit - middle) * num_bins)
            high = middle - 1;
        else
            low = middle + 1;
    }
    return middle / sum;
}

/// \fn void clip_and_redistribute
/// \ingroup AHE-helpers
/// \brief Clips and redistributes excess pixels based on the actual clip limit value
///        obtained from the other helper function actual_clip_limit
///        Reference - Graphic Gems 4, Pg. 474
///        (http://cas.xav.free.fr/Graphics%20Gems%204%20-%20Paul%20S.%20Heckbert.pdf)
///
template <typename SrcHist, typename DstHist>
void clip_and_redistribute(SrcHist const& src_hist, DstHist& dst_hist, double clip_limit = 0.03)
{
    using value_t            = typename SrcHist::value_type;
    double sum               = src_hist.sum();
    double actual_clip_value = detail::actual_clip_limit(src_hist, clip_limit);
    // double actual_clip_value = clip_limit;
    long actual_clip_limit = actual_clip_value * sum;
    double excess          = 0;
    std::for_each(src_hist.begin(), src_hist.end(), [&](value_t const& v) {
        if (v.second > actual_clip_limit)
            excess += v.second - actual_clip_limit;
    });
    std::for_each(src_hist.begin(), src_hist.end(), [&](value_t const& v) {
        if (v.second >= actual_clip_limit)
            dst_hist[dst_hist.key_from_tuple(v.first)] = clip_limit * sum;
        else
            dst_hist[dst_hist.key_from_tuple(v.first)] = v.second + excess / src_hist.size();
    });
    long rem = long(excess) % src_hist.size();
    if (rem == 0)
        return;
    long period       = round(src_hist.size() / rem);
    std::size_t index = 0;
    while (rem)
    {
        if (dst_hist(index) >= clip_limit * sum)
        {
            index = (index + 1) % src_hist.size();
        }
        dst_hist(index)++;
        rem--;
        index = (index + period) % src_hist.size();
    }
}

} // namespace detail

/// \fn void non_overlapping_interpolated_clahe
/// \ingroup AHE
/// @param src_view      Input   Source image view
/// @param dst_view      Output  Output image view
/// @param tile_width_x  Input   Tile width along x-axis to apply HE
/// @param tile_width_y  Input   Tile width along x-axis to apply HE
/// @param clip_limit    Input   Clipping limit to be applied
/// @param bin_width     Input   Bin widths for histogram
/// @param mask          Input   Specify if mask is to be used
/// @param src_mask      Input   Mask on input image to ignore specified pixels
/// \brief Performs local histogram equalization on tiles of size (tile_width_x, tile_width_y)
///        Then uses the clip limit to redistribute excess pixels above the limit uniformly to
///        other bins. The clip limit is specified as a fraction i.e. a bin's value is clipped
///        if bin_value >= clip_limit * (Total number of pixels in the tile)
///
template <typename SrcView, typename DstView>
void non_overlapping_interpolated_clahe(
    SrcView const& src_view,
    DstView const& dst_view,
    std::ptrdiff_t tile_width_x             = 20,
    std::ptrdiff_t tile_width_y             = 20,
    double clip_limit                       = 0.03,
    std::size_t bin_width                   = 1.0,
    bool mask                               = false,
    std::vector<std::vector<bool>> src_mask = {})
{
    gil_function_requires<ImageViewConcept<SrcView>>();
    gil_function_requires<MutableImageViewConcept<DstView>>();

    static_assert(
        color_spaces_are_compatible<
            typename color_space_type<SrcView>::type,
            typename color_space_type<DstView>::type>::value,
        "Source and destination views must have same color space");

    using source_channel_t = typename channel_type<SrcView>::type;
    using dst_channel_t    = typename channel_type<DstView>::type;
    using coord_t          = typename SrcView::x_coord_t;

    std::size_t const channels = num_channels<SrcView>::value;
    coord_t const width        = src_view.width();
    coord_t const height       = src_view.height();

    // Find control points

    std::vector<coord_t> sample_x;
    coord_t sample_x1 = tile_width_x / 2;
    coord_t sample_y1 = tile_width_y / 2;

    auto extend_left   = tile_width_x;
    auto extend_top    = tile_width_y;
    auto extend_right  = (tile_width_x - width % tile_width_x) % tile_width_x + tile_width_x;
    auto extend_bottom = (tile_width_y - height % tile_width_y) % tile_width_y + tile_width_y;

    auto new_width  = width + extend_left + extend_right;
    auto new_height = height + extend_top + extend_bottom;

    image<typename SrcView::value_type> padded_img(new_width, new_height);

    auto top_left_x     = tile_width_x;
    auto top_left_y     = tile_width_y;
    auto bottom_right_x = tile_width_x + width;
    auto bottom_right_y = tile_width_y + height;

    copy_pixels(src_view, subimage_view(view(padded_img), top_left_x, top_left_y, width, height));

    for (std::size_t k = 0; k < channels; k++)
    {
        std::vector<histogram<source_channel_t>> prev_row(new_width / tile_width_x),
            next_row((new_width / tile_width_x));
        std::vector<std::map<source_channel_t, source_channel_t>> prev_map(
            new_width / tile_width_x),
            next_map((new_width / tile_width_x));

        coord_t prev = 0, next = 1;
        auto channel_view = nth_channel_view(view(padded_img), k);

        for (std::ptrdiff_t i = top_left_y; i < bottom_right_y; ++i)
        {
            if ((i - sample_y1) / tile_width_y >= next || i == top_left_y)
            {
                if (i != top_left_y)
                {
                    prev = next;
                    next++;
                }
                prev_row = next_row;
                prev_map = next_map;
                for (std::ptrdiff_t j = sample_x1; j < new_width; j += tile_width_x)
                {
                    auto img_view = subimage_view(
                        channel_view, j - sample_x1, next * tile_width_y,
                        std::max<int>(
                            std::min<int>(tile_width_x + j - sample_x1, bottom_right_x) -
                                (j - sample_x1),
                            0),
                        std::max<int>(
                            std::min<int>((next + 1) * tile_width_y, bottom_right_y) -
                                next * tile_width_y,
                            0));

                    fill_histogram(
                        img_view, next_row[(j - sample_x1) / tile_width_x], bin_width, false,
                        false);

                    detail::clip_and_redistribute(
                        next_row[(j - sample_x1) / tile_width_x],
                        next_row[(j - sample_x1) / tile_width_x], clip_limit);

                    next_map[(j - sample_x1) / tile_width_x] =
                        histogram_equalization(next_row[(j - sample_x1) / tile_width_x]);
                }
            }
            bool prev_row_mask = 1, next_row_mask = 1;
            if (prev == 0)
                prev_row_mask = false;
            else if (next + 1 == new_height / tile_width_y)
                next_row_mask = false;
            for (std::ptrdiff_t j = top_left_x; j < bottom_right_x; ++j)
            {
                bool prev_col_mask = true, next_col_mask = true;
                if ((j - sample_x1) / tile_width_x == 0)
                    prev_col_mask = false;
                else if ((j - sample_x1) / tile_width_x + 1 == new_width / tile_width_x - 1)
                    next_col_mask = false;

                // Bilinear interpolation
                point_t top_left(
                    (j - sample_x1) / tile_width_x * tile_width_x + sample_x1,
                                    prev * tile_width_y + sample_y1);
                point_t top_right(top_left.x + tile_width_x, top_left.y);
                point_t bottom_left(top_left.x, top_left.y + tile_width_y);
                point_t bottom_right(top_left.x + tile_width_x, top_left.y + tile_width_y);

                long double x_diff = top_right.x - top_left.x;
                long double y_diff = bottom_left.y - top_left.y;

                long double x1 = (j - top_left.x) / x_diff;
                long double x2 = (top_right.x - j) / x_diff;
                long double y1 = (i - top_left.y) / y_diff;
                long double y2 = (bottom_left.y - i) / y_diff;

                if (prev_row_mask == 0)
                    y1 = 1;
                else if (next_row_mask == 0)
                    y2 = 1;
                if (prev_col_mask == 0)
                    x1 = 1;
                else if (next_col_mask == 0)
                    x2 = 1;

                long double numerator =
                    ((prev_row_mask & prev_col_mask) * x2 *
                         prev_map[(top_left.x - sample_x1) / tile_width_x][channel_view(j, i)] +
                     (prev_row_mask & next_col_mask) * x1 *
                         prev_map[(top_right.x - sample_x1) / tile_width_x][channel_view(j, i)]) *
                        y2 +
                    ((next_row_mask & prev_col_mask) * x2 *
                         next_map[(bottom_left.x - sample_x1) / tile_width_x][channel_view(j, i)] +
                     (next_row_mask & next_col_mask) * x1 *
                         next_map[(bottom_right.x - sample_x1) / tile_width_x][channel_view(j, i)]) *
                        y1;

                if (mask && !src_mask[i - top_left_y][j - top_left_x])
                {
                    dst_view(j - top_left_x, i - top_left_y) =
                        channel_convert<dst_channel_t>(
                            static_cast<source_channel_t>(channel_view(i, j)));
                }
                else
                {
                    dst_view(j - top_left_x, i - top_left_y) =
                        channel_convert<dst_channel_t>(static_cast<source_channel_t>(numerator));
                }
            }
        }
    }
}

}}  //namespace boost::gil

#endif
