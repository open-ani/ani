//
// Copyright 2005-2007 Adobe Systems Incorporated
// Copyright 2022 Marco Langer <langer.m86 at gmail dot com>
//
// Distributed under the Boost Software License, Version 1.0
// See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt
//
#ifndef BOOST_GIL_EXTENSION_DYNAMIC_IMAGE_ALGORITHM_HPP
#define BOOST_GIL_EXTENSION_DYNAMIC_IMAGE_ALGORITHM_HPP

#include <boost/gil/extension/dynamic_image/any_image.hpp>

#include <boost/gil/algorithm.hpp>

#include <boost/variant2/variant.hpp>

#include <functional>
#include <utility>

////////////////////////////////////////////////////////////////////////////////////////
/// \file
/// \brief Some basic STL-style algorithms when applied to runtime type specified image views
/// \author Lubomir Bourdev and Hailin Jin \n
///         Adobe Systems Incorporated
/// \date 2005-2007 \n Last updated on September 24, 2006
///
////////////////////////////////////////////////////////////////////////////////////////

namespace boost { namespace gil {

namespace detail {

struct equal_pixels_fn : binary_operation_obj<equal_pixels_fn, bool>
{
    template <typename V1, typename V2>
    BOOST_FORCEINLINE
    bool apply_compatible(V1 const& v1, V2 const& v2) const
    {
        return equal_pixels(v1, v2);
    }
};

} // namespace detail

/// \ingroup ImageViewSTLAlgorithmsEqualPixels
/// \tparam Types Model Boost.MP11-compatible list of models of ImageViewConcept
/// \tparam View Model MutableImageViewConcept
template <typename ...Types, typename View>
auto equal_pixels(any_image_view<Types...> const& src, View const& dst) -> bool
{
    return variant2::visit(
        std::bind(detail::equal_pixels_fn(), std::placeholders::_1, dst),
        src);
}

/// \ingroup ImageViewSTLAlgorithmsEqualPixels
/// \tparam View Model ImageViewConcept
/// \tparam Types Model Boost.MP11-compatible list of models of MutableImageViewConcept
template <typename View, typename ...Types>
auto equal_pixels(View const& src, any_image_view<Types...> const& dst) -> bool
{
    return variant2::visit(
        std::bind(detail::equal_pixels_fn(), src, std::placeholders::_1),
        dst);
}

/// \ingroup ImageViewSTLAlgorithmsEqualPixels
/// \tparam Types1 Model Boost.MP11-compatible list of models of ImageViewConcept
/// \tparam Types2 Model Boost.MP11-compatible list of models of MutableImageViewConcept
template <typename ...Types1, typename ...Types2>
auto equal_pixels(any_image_view<Types1...> const& src, any_image_view<Types2...> const& dst) -> bool
{
    return variant2::visit(detail::equal_pixels_fn(), src, dst);
}

namespace detail {

struct copy_pixels_fn : public binary_operation_obj<copy_pixels_fn>
{
    template <typename View1, typename View2>
    BOOST_FORCEINLINE
    void apply_compatible(View1 const& src, View2 const& dst) const
    {
        copy_pixels(src,dst);
    }
};

} // namespace detail

/// \ingroup ImageViewSTLAlgorithmsCopyPixels
/// \tparam Types Model Boost.MP11-compatible list of models of ImageViewConcept
/// \tparam View Model MutableImageViewConcept
template <typename ...Types, typename View>
void copy_pixels(any_image_view<Types...> const& src, View const& dst)
{
    variant2::visit(std::bind(detail::copy_pixels_fn(), std::placeholders::_1, dst), src);
}

/// \ingroup ImageViewSTLAlgorithmsCopyPixels
/// \tparam Types Model Boost.MP11-compatible list of models of MutableImageViewConcept
/// \tparam View Model ImageViewConcept
template <typename ...Types, typename View>
void copy_pixels(View const& src, any_image_view<Types...> const& dst)
{
    variant2::visit(std::bind(detail::copy_pixels_fn(), src, std::placeholders::_1), dst);
}

/// \ingroup ImageViewSTLAlgorithmsCopyPixels
/// \tparam Types1 Model Boost.MP11-compatible list of models of ImageViewConcept
/// \tparam Types2 Model Boost.MP11-compatible list of models of MutableImageViewConcept
template <typename ...Types1, typename ...Types2>
void copy_pixels(any_image_view<Types1...> const& src, any_image_view<Types2...> const& dst)
{
    variant2::visit(detail::copy_pixels_fn(), src, dst);
}

//forward declaration for default_color_converter (see full definition in color_convert.hpp)
struct default_color_converter;

/// \ingroup ImageViewSTLAlgorithmsCopyAndConvertPixels
/// \tparam Types Model Boost.MP11-compatible list of models of ImageViewConcept
/// \tparam View Model MutableImageViewConcept
/// \tparam CC Model ColorConverterConcept
template <typename ...Types, typename View, typename CC>
void copy_and_convert_pixels(any_image_view<Types...> const& src, View const& dst, CC cc)
{
    using cc_fn = detail::copy_and_convert_pixels_fn<CC>;
    variant2::visit(std::bind(cc_fn{cc}, std::placeholders::_1, dst), src);
}

/// \ingroup ImageViewSTLAlgorithmsCopyAndConvertPixels
/// \tparam Types Model Boost.MP11-compatible list of models of ImageViewConcept
/// \tparam View Model MutableImageViewConcept
template <typename ...Types, typename View>
void copy_and_convert_pixels(any_image_view<Types...> const& src, View const& dst)
{
    using cc_fn = detail::copy_and_convert_pixels_fn<default_color_converter>;
    variant2::visit(std::bind(cc_fn{}, std::placeholders::_1, dst), src);
}

/// \ingroup ImageViewSTLAlgorithmsCopyAndConvertPixels
/// \tparam View Model ImageViewConcept
/// \tparam Types Model Boost.MP11-compatible list of models of MutableImageViewConcept
/// \tparam CC Model ColorConverterConcept
template <typename View, typename ...Types, typename CC>
void copy_and_convert_pixels(View const& src, any_image_view<Types...> const& dst, CC cc)
{
    using cc_fn = detail::copy_and_convert_pixels_fn<CC>;
    variant2::visit(std::bind(cc_fn{cc}, src, std::placeholders::_1), dst);
}

/// \ingroup ImageViewSTLAlgorithmsCopyAndConvertPixels
/// \tparam View Model ImageViewConcept
/// \tparam Type Model Boost.MP11-compatible list of models of MutableImageViewConcept
template <typename View, typename ...Types>
void copy_and_convert_pixels(View const& src, any_image_view<Types...> const& dst)
{
    using cc_fn = detail::copy_and_convert_pixels_fn<default_color_converter>;
    variant2::visit(std::bind(cc_fn{}, src, std::placeholders::_1), dst);
}

/// \ingroup ImageViewSTLAlgorithmsCopyAndConvertPixels
/// \tparam Types1 Model Boost.MP11-compatible list of models of ImageViewConcept
/// \tparam Types2 Model Boost.MP11-compatible list of models of MutableImageViewConcept
/// \tparam CC Model ColorConverterConcept
template <typename ...Types1, typename ...Types2, typename CC>
void copy_and_convert_pixels(
    any_image_view<Types1...> const& src,
    any_image_view<Types2...> const& dst, CC cc)
{
    variant2::visit(detail::copy_and_convert_pixels_fn<CC>(cc), src, dst);
}

/// \ingroup ImageViewSTLAlgorithmsCopyAndConvertPixels
/// \tparam Types1 Model Boost.MP11-compatible list of models of ImageViewConcept
/// \tparam Types2 Model Boost.MP11-compatible list of models of MutableImageViewConcept
template <typename ...Types1, typename ...Types2>
void copy_and_convert_pixels(
    any_image_view<Types1...> const& src,
    any_image_view<Types2...> const& dst)
{
    variant2::visit(
        detail::copy_and_convert_pixels_fn<default_color_converter>(), src, dst);
}

namespace detail {

template <bool IsCompatible>
struct fill_pixels_fn1
{
    template <typename V, typename Value>
    static void apply(V const& src, Value const& val) { fill_pixels(src, val); }
};

// copy_pixels invoked on incompatible images
template <>
struct fill_pixels_fn1<false>
{
    template <typename V, typename Value>
    static void apply(V const&, Value const&) { throw std::bad_cast();}
};

template <typename Value>
struct fill_pixels_fn
{
    fill_pixels_fn(Value const& val) : val_(val) {}

    using result_type = void;
    template <typename V>
    result_type operator()(V const& view) const
    {
        fill_pixels_fn1
        <
            pixels_are_compatible
            <
                typename V::value_type,
                Value
            >::value
        >::apply(view, val_);
    }

    Value val_;
};

} // namespace detail

/// \ingroup ImageViewSTLAlgorithmsFillPixels
/// \brief fill_pixels for any image view. The pixel to fill with must be compatible with the current view
/// \tparam Types Model Boost.MP11-compatible list of models of MutableImageViewConcept
template <typename ...Types, typename Value>
void fill_pixels(any_image_view<Types...> const& view, Value const& val)
{
    variant2::visit(detail::fill_pixels_fn<Value>(val), view);
}

namespace detail {

template <typename F>
struct for_each_pixel_fn
{
    for_each_pixel_fn(F&& fun) : fun_(std::move(fun)) {}

    template <typename View>
    auto operator()(View const& view) -> F
    {
        return for_each_pixel(view, fun_);
    }

    F fun_;
};

} // namespace detail

/// \defgroup ImageViewSTLAlgorithmsForEachPixel for_each_pixel
/// \ingroup ImageViewSTLAlgorithms
/// \brief std::for_each for any image views
///
/// \ingroup ImageViewSTLAlgorithmsForEachPixel
template <typename ...Types, typename F>
auto for_each_pixel(any_image_view<Types...> const& view, F fun) -> F
{
    return variant2::visit(detail::for_each_pixel_fn<F>(std::move(fun)), view);
}

}}  // namespace boost::gil

#endif
