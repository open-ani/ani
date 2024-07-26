//
// Copyright 2020 Debabrata Mandal <mandaldebabrata123@gmail.com>
//
// Distributed under the Boost Software License, Version 1.0
// See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt
//

#ifndef BOOST_GIL_EXTENSION_HISTOGRAM_STL_HISTOGRAM_HPP
#define BOOST_GIL_EXTENSION_HISTOGRAM_STL_HISTOGRAM_HPP

#include <boost/gil/concepts/concept_check.hpp>
#include <boost/gil/gray.hpp>
#include <boost/gil/histogram.hpp>
#include <boost/gil/image_view.hpp>
#include <boost/gil/image_view_factory.hpp>

#include <array>
#include <map>
#include <utility>
#include <vector>

namespace boost { namespace gil {

//////////////////////////////////////////////////////////
/// Histogram extension for STL container
//////////////////////////////////////////////////////////
/// \defgroup Histogram - STL Containers
/// \brief Collection of functions to provide histogram support in GIL using Standard
///        Template Library Containers
/// The conversion from Boost.GIL images to compatible histograms are provided. The supported
/// container types would be std::vector, std::array, std::map.
///
/// Some general constraints on STL extension:-
/// 1. Supports only 1D histogram.
/// 2. Cannot use signed images with compatible random access containers.
/// 3. Automatic resize of std::array in case of shortage of bins, to ensure
///    correctness comes before performance.
/// 4. Container key type (if exists) has to be one of std::integral types to be
///    GIL compatible.
/// 5. Container value type has to be of std::arithmetic types.
///

///
/// \ingroup Histogram - STL Containers
/// \brief Overload for std::vector of fill_histogram
///
template <typename SrcView, typename T>
void fill_histogram(SrcView const& srcview, std::vector<T>& histogram, bool accumulate = false)
{
    gil_function_requires<ImageViewConcept<SrcView>>();
    static_assert(std::is_arithmetic<T>::value, "Improper container type for images.");
    static_assert(
        std::is_unsigned<typename channel_type<SrcView>::type>::value,
        "Improper container type for signed images.");

    using channel_t = typename channel_type<SrcView>::type;
    using pixel_t   = pixel<channel_t, gray_layout_t>;

    if (!accumulate)
        histogram.clear();
    histogram.resize(std::numeric_limits<channel_t>::max() + 1);

    for_each_pixel(color_converted_view<pixel_t>(srcview), [&](pixel_t const& p) {
        ++histogram[static_cast<std::size_t>(p)];
    });
}

/// \ingroup Histogram - STL Containers
/// \brief Overload for std::array of fill_histogram
///
template <typename SrcView, typename T, std::size_t N>
void fill_histogram(SrcView const& srcview, std::array<T, N>& histogram, bool accumulate = false)
{
    gil_function_requires<ImageViewConcept<SrcView>>();
    static_assert(std::is_arithmetic<T>::value && N > 0, "Improper container type for images.");
    static_assert(
        std::is_unsigned<typename channel_type<SrcView>::type>::value,
        "Improper container type for signed images.");

    using channel_t = typename channel_type<SrcView>::type;
    using pixel_t   = pixel<channel_t, gray_layout_t>;

    const size_t pixel_max = std::numeric_limits<channel_t>::max();
    const float scale      = (histogram.size() - 1.0f) / pixel_max;

    if (!accumulate)
        std::fill(std::begin(histogram), std::end(histogram), 0);

    for_each_pixel(color_converted_view<pixel_t>(srcview), [&](pixel_t const& p) {
        ++histogram[static_cast<std::size_t>(p * scale)];
    });
}

/// \ingroup Histogram - STL Containers
/// \brief Overload for std::map of fill_histogram
///
template <typename SrcView, typename T1, typename T2>
void fill_histogram(SrcView const& srcview, std::map<T1, T2>& histogram, bool accumulate = false)
{
    gil_function_requires<ImageViewConcept<SrcView>>();
    static_assert(
        std::is_arithmetic<T1>::value && std::is_integral<T2>::value,
        "Improper container type for images.");

    using channel_t = typename channel_type<SrcView>::type;
    using pixel_t   = pixel<channel_t, gray_layout_t>;

    if (!accumulate)
        histogram.clear();

    for_each_pixel(color_converted_view<pixel_t>(srcview), [&](pixel_t const& p) {
        ++histogram[static_cast<std::size_t>(p)];
    });
}

/// \ingroup Histogram - STL Containers
/// \brief Overload for std::vector of cumulative_histogram
///
template <typename T>
std::vector<T> cumulative_histogram(std::vector<T>& hist)
{
    std::vector<T> cumulative_hist(hist.size());
    static_assert(std::is_arithmetic<T>::value, "Improper container type for images.");
    T cumulative_counter = 0;
    for (std::size_t i = 0; i < hist.size(); i++)
    {
        cumulative_counter += hist[i];
        cumulative_hist[i] = cumulative_counter;
    }
    return cumulative_hist;
}

/// \ingroup Histogram - STL Containers
/// \brief Overload for std::array of cumulative_histogram
///
template <typename T, std::size_t N>
std::array<T, N> cumulative_histogram(std::array<T, N>& histogram)
{
    std::array<T, N> cumulative_hist;
    static_assert(std::is_arithmetic<T>::value && N > 0, "Improper container type for images.");
    T cumulative_counter = 0;
    for (std::size_t i = 0; i < N; i++)
    {
        cumulative_counter += histogram[i];
        cumulative_hist[i] = cumulative_counter;
    }
    return cumulative_hist;
}

/// \ingroup Histogram - STL Containers
/// \brief Overload for std::map of cumulative_histogram
///
template <typename T1, typename T2>
std::map<T1, T2> cumulative_histogram(std::map<T1, T2>& histogram)
{
    std::map<T1, T2> cumulative_hist;
    static_assert(
        std::is_arithmetic<T1>::value && std::is_integral<T2>::value,
        "Improper container type for images.");
    T2 cumulative_counter = 0;
    for (auto const& it : histogram)
    {
        cumulative_counter += it.second;
        cumulative_hist[it.first] = cumulative_counter;
    }
    return cumulative_hist;
}

}}  // namespace boost::gil

#endif
