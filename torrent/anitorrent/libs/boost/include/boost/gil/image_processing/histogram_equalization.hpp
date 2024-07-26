//
// Copyright 2020 Debabrata Mandal <mandaldebabrata123@gmail.com>
//
// Use, modification and distribution are subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)
//
#ifndef BOOST_GIL_IMAGE_PROCESSING_HISTOGRAM_EQUALIZATION_HPP
#define BOOST_GIL_IMAGE_PROCESSING_HISTOGRAM_EQUALIZATION_HPP

#include <boost/gil/histogram.hpp>
#include <boost/gil/image.hpp>

#include <cmath>
#include <map>
#include <vector>

namespace boost { namespace gil {

/////////////////////////////////////////
/// Histogram Equalization(HE)
/////////////////////////////////////////
/// \defgroup HE HE
/// \brief Contains implementation and description of the algorithm used to compute
///        global histogram equalization of input images.
///
///        Algorithm :-
///        1. If histogram A is to be equalized compute the cumulative histogram of A.
///        2. Let CFD(A) refer to the cumulative histogram of A
///        3. For a uniform histogram A', CDF(A') = A'
///        4. We need to transfrom A to A' such that
///        5. CDF(A') = CDF(A) => A' = CDF(A)
///        6. Hence the pixel transform , px => histogram_of_ith_channel[px].
///

/// \fn histogram_equalization
/// \ingroup HE
/// \tparam SrcKeyType Key Type of input histogram
/// @param src_hist INPUT Input source histogram
/// \brief Overload for histogram equalization algorithm, takes in a single source histogram
///        and returns the color map used for histogram equalization.
///
template <typename SrcKeyType>
auto histogram_equalization(histogram<SrcKeyType> const& src_hist)
    -> std::map<SrcKeyType, SrcKeyType>
{
    histogram<SrcKeyType> dst_hist;
    return histogram_equalization(src_hist, dst_hist);
}

/// \overload histogram_equalization
/// \ingroup HE
/// \tparam SrcKeyType Key Type of input histogram
/// \tparam DstKeyType Key Type of output histogram
/// @param src_hist INPUT source histogram
/// @param dst_hist OUTPUT Output histogram
/// \brief Overload for histogram equalization algorithm, takes in both source histogram &
///        destination histogram and returns the color map used for histogram equalization
///        as well as transforming the destination histogram.
///
template <typename SrcKeyType, typename DstKeyType>
auto histogram_equalization(histogram<SrcKeyType> const& src_hist, histogram<DstKeyType>& dst_hist)
    -> std::map<SrcKeyType, DstKeyType>
{
    static_assert(
        std::is_integral<SrcKeyType>::value &&
        std::is_integral<DstKeyType>::value,
        "Source and destination histogram types are not appropriate");

    using value_t = typename histogram<SrcKeyType>::value_type;
    dst_hist.clear();
    double sum          = src_hist.sum();
    SrcKeyType min_key  = std::numeric_limits<DstKeyType>::min();
    SrcKeyType max_key  = std::numeric_limits<DstKeyType>::max();
    auto cumltv_srchist = cumulative_histogram(src_hist);
    std::map<SrcKeyType, DstKeyType> color_map;
    std::for_each(cumltv_srchist.begin(), cumltv_srchist.end(), [&](value_t const& v) {
        DstKeyType trnsfrmd_key =
            static_cast<DstKeyType>((v.second * (max_key - min_key)) / sum + min_key);
        color_map[std::get<0>(v.first)] = trnsfrmd_key;
    });
    std::for_each(src_hist.begin(), src_hist.end(), [&](value_t const& v) {
        dst_hist[color_map[std::get<0>(v.first)]] += v.second;
    });
    return color_map;
}

/// \overload histogram_equalization
/// \ingroup HE
/// @param src_view  INPUT source image view
/// @param dst_view  OUTPUT Output image view
/// @param bin_width INPUT Histogram bin width
/// @param mask      INPUT Specify is mask is to be used
/// @param src_mask  INPUT Mask vector over input image
/// \brief Overload for histogram equalization algorithm, takes in both source & destination
///        image views and histogram equalizes the input image.
///
template <typename SrcView, typename DstView>
void histogram_equalization(
    SrcView const& src_view,
    DstView const& dst_view,
    std::size_t bin_width = 1,
    bool mask = false,
    std::vector<std::vector<bool>> src_mask = {})
{
    gil_function_requires<ImageViewConcept<SrcView>>();
    gil_function_requires<MutableImageViewConcept<DstView>>();

    static_assert(
        color_spaces_are_compatible<
            typename color_space_type<SrcView>::type,
            typename color_space_type<DstView>::type>::value,
        "Source and destination views must have same color space");

    // Defining channel type
    using source_channel_t = typename channel_type<SrcView>::type;
    using dst_channel_t    = typename channel_type<DstView>::type;
    using coord_t          = typename SrcView::x_coord_t;

    std::size_t const channels = num_channels<SrcView>::value;
    coord_t const width        = src_view.width();
    coord_t const height       = src_view.height();
    std::size_t pixel_max      = std::numeric_limits<dst_channel_t>::max();
    std::size_t pixel_min      = std::numeric_limits<dst_channel_t>::min();

    for (std::size_t i = 0; i < channels; i++)
    {
        histogram<source_channel_t> h;
        fill_histogram(nth_channel_view(src_view, i), h, bin_width, false, false, mask, src_mask);
        h.normalize();
        auto h2 = cumulative_histogram(h);
        for (std::ptrdiff_t src_y = 0; src_y < height; ++src_y)
        {
            auto src_it = nth_channel_view(src_view, i).row_begin(src_y);
            auto dst_it = nth_channel_view(dst_view, i).row_begin(src_y);
            for (std::ptrdiff_t src_x = 0; src_x < width; ++src_x)
            {
                if (mask && !src_mask[src_y][src_x])
                    dst_it[src_x][0] = channel_convert<dst_channel_t>(src_it[src_x][0]);
                else
                    dst_it[src_x][0] = static_cast<dst_channel_t>(
                        h2[src_it[src_x][0]] * (pixel_max - pixel_min) + pixel_min);
            }
        }
    }
}

}}  //namespace boost::gil

#endif
