//
// Copyright 2020 Debabrata Mandal <mandaldebabrata123@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//

#ifndef BOOST_GIL_IMAGE_PROCESSING_HISTOGRAM_MATCHING_HPP
#define BOOST_GIL_IMAGE_PROCESSING_HISTOGRAM_MATCHING_HPP

#include <boost/gil/algorithm.hpp>
#include <boost/gil/histogram.hpp>
#include <boost/gil/image.hpp>

#include <algorithm>
#include <cmath>
#include <map>
#include <vector>

namespace boost { namespace gil {

/////////////////////////////////////////
/// Histogram Matching(HM)
/////////////////////////////////////////
/// \defgroup HM HM
/// \brief Contains implementation and description of the algorithm used to compute
///        global histogram matching of input images.
///
///        Algorithm :-
///        1. Calculate histogram A(pixel) of input image and G(pixel) of reference image.
///        2. Compute the normalized cumulative(CDF) histograms of A and G.
///        3. Match the histograms using transofrmation  => CDF(A(px)) = CDF(G(px'))
///                                                      => px' = Inv-CDF (CDF(px))
///

/// \fn histogram_matching
/// \ingroup HM
/// \tparam SrcKeyType Key Type of input histogram
/// @param src_hist INPUT Input source histogram
/// @param ref_hist INPUT Input reference histogram
/// \brief Overload for histogram matching algorithm, takes in a single source histogram &
///        reference histogram and returns the color map used for histogram matching.
///
template <typename SrcKeyType, typename RefKeyType>
auto histogram_matching(histogram<SrcKeyType> const& src_hist, histogram<RefKeyType> const& ref_hist)
    -> std::map<SrcKeyType, SrcKeyType>
{
    histogram<SrcKeyType> dst_hist;
    return histogram_matching(src_hist, ref_hist, dst_hist);
}

/// \overload histogram_matching
/// \ingroup HM
/// \tparam SrcKeyType Key Type of input histogram
/// \tparam RefKeyType Key Type of reference histogram
/// \tparam DstKeyType Key Type of output histogram
/// @param src_hist INPUT source histogram
/// @param ref_hist INPUT reference histogram
/// @param dst_hist OUTPUT Output histogram
/// \brief Overload for histogram matching algorithm, takes in source histogram, reference
///        histogram & destination histogram and returns the color map used for histogram
///        matching as well as transforming the destination histogram.
///
template <typename SrcKeyType, typename RefKeyType, typename DstKeyType>
auto histogram_matching(
    histogram<SrcKeyType> const& src_hist,
    histogram<RefKeyType> const& ref_hist,
    histogram<DstKeyType>& dst_hist)
    -> std::map<SrcKeyType, DstKeyType>
{
    static_assert(
        std::is_integral<SrcKeyType>::value &&
        std::is_integral<RefKeyType>::value &&
        std::is_integral<DstKeyType>::value,
        "Source, Refernce or Destination histogram type is not appropriate.");

    using value_t = typename histogram<SrcKeyType>::value_type;
    dst_hist.clear();
    double src_sum      = src_hist.sum();
    double ref_sum      = ref_hist.sum();
    auto cumltv_srchist = cumulative_histogram(src_hist);
    auto cumltv_refhist = cumulative_histogram(ref_hist);
    std::map<SrcKeyType, RefKeyType> inverse_mapping;

    std::vector<typename histogram<RefKeyType>::key_type> src_keys, ref_keys;
    src_keys             = src_hist.sorted_keys();
    ref_keys             = ref_hist.sorted_keys();
    std::ptrdiff_t start = ref_keys.size() - 1;
    RefKeyType ref_max;
    if (start >= 0)
        ref_max = std::get<0>(ref_keys[start]);

    for (std::ptrdiff_t j = src_keys.size() - 1; j >= 0; --j)
    {
        double src_val = (cumltv_srchist[src_keys[j]] * ref_sum) / src_sum;
        while (cumltv_refhist[ref_keys[start]] > src_val && start > 0)
        {
            start--;
        }
        if (std::abs(cumltv_refhist[ref_keys[start]] - src_val) >
            std::abs(cumltv_refhist(std::min<RefKeyType>(ref_max, std::get<0>(ref_keys[start + 1]))) -
                src_val))
        {
            inverse_mapping[std::get<0>(src_keys[j])] =
                std::min<RefKeyType>(ref_max, std::get<0>(ref_keys[start + 1]));
        }
        else
        {
            inverse_mapping[std::get<0>(src_keys[j])] = std::get<0>(ref_keys[start]);
        }
        if (j == 0)
            break;
    }
    std::for_each(src_hist.begin(), src_hist.end(), [&](value_t const& v) {
        dst_hist[inverse_mapping[std::get<0>(v.first)]] += v.second;
    });
    return inverse_mapping;
}

/// \overload histogram_matching
/// \ingroup HM
/// @param src_view  INPUT source image view
/// @param ref_view  INPUT Reference image view
/// @param dst_view  OUTPUT Output image view
/// @param bin_width INPUT Histogram bin width
/// @param mask      INPUT Specify is mask is to be used
/// @param src_mask  INPUT Mask vector over input image
/// @param ref_mask  INPUT Mask vector over reference image
/// \brief Overload for histogram matching algorithm, takes in both source, reference &
///        destination image views and histogram matches the input image using the
///        reference image.
///
template <typename SrcView, typename ReferenceView, typename DstView>
void histogram_matching(
    SrcView const& src_view,
    ReferenceView const& ref_view,
    DstView const& dst_view,
    std::size_t bin_width = 1,
    bool mask = false,
    std::vector<std::vector<bool>> src_mask = {},
    std::vector<std::vector<bool>> ref_mask = {})
{
    gil_function_requires<ImageViewConcept<SrcView>>();
    gil_function_requires<ImageViewConcept<ReferenceView>>();
    gil_function_requires<MutableImageViewConcept<DstView>>();

    static_assert(
        color_spaces_are_compatible<
            typename color_space_type<SrcView>::type,
            typename color_space_type<ReferenceView>::type>::value,
        "Source and reference view must have same color space");

    static_assert(
        color_spaces_are_compatible<
            typename color_space_type<SrcView>::type,
            typename color_space_type<DstView>::type>::value,
        "Source and destination view must have same color space");

    // Defining channel type
    using source_channel_t = typename channel_type<SrcView>::type;
    using ref_channel_t    = typename channel_type<ReferenceView>::type;
    using dst_channel_t    = typename channel_type<DstView>::type;
    using coord_t          = typename SrcView::x_coord_t;

    std::size_t const channels     = num_channels<SrcView>::value;
    coord_t const width            = src_view.width();
    coord_t const height           = src_view.height();
    source_channel_t src_pixel_min = std::numeric_limits<source_channel_t>::min();
    source_channel_t src_pixel_max = std::numeric_limits<source_channel_t>::max();
    ref_channel_t ref_pixel_min    = std::numeric_limits<ref_channel_t>::min();
    ref_channel_t ref_pixel_max    = std::numeric_limits<ref_channel_t>::max();

    for (std::size_t i = 0; i < channels; i++)
    {
        histogram<source_channel_t> src_histogram;
        histogram<ref_channel_t> ref_histogram;
        fill_histogram(
            nth_channel_view(src_view, i), src_histogram, bin_width, false, false, mask, src_mask,
            std::tuple<source_channel_t>(src_pixel_min),
            std::tuple<source_channel_t>(src_pixel_max), true);
        fill_histogram(
            nth_channel_view(ref_view, i), ref_histogram, bin_width, false, false, mask, ref_mask,
            std::tuple<ref_channel_t>(ref_pixel_min), std::tuple<ref_channel_t>(ref_pixel_max),
            true);
        auto inverse_mapping = histogram_matching(src_histogram, ref_histogram);
        for (std::ptrdiff_t src_y = 0; src_y < height; ++src_y)
        {
            auto src_it = nth_channel_view(src_view, i).row_begin(src_y);
            auto dst_it = nth_channel_view(dst_view, i).row_begin(src_y);
            for (std::ptrdiff_t src_x = 0; src_x < width; ++src_x)
            {
                if (mask && !src_mask[src_y][src_x])
                    dst_it[src_x][0] = src_it[src_x][0];
                else
                    dst_it[src_x][0] =
                        static_cast<dst_channel_t>(inverse_mapping[src_it[src_x][0]]);
            }
        }
    }
}

}}  //namespace boost::gil

#endif
