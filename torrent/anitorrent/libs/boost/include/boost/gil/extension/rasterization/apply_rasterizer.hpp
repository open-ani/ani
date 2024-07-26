#ifndef BOOST_GIL_EXTENSION_RASTERIZATION_APPLY_RASTERIZER
#define BOOST_GIL_EXTENSION_RASTERIZATION_APPLY_RASTERIZER

namespace boost { namespace gil {

namespace detail {

template <typename View, typename Rasterizer, typename Pixel, typename Tag>
struct apply_rasterizer_op
{
    void operator()(
        View const& view, Rasterizer const& rasterizer, Pixel const& pixel);
};

} // namespace detail

template <typename View, typename Rasterizer, typename Pixel>
void apply_rasterizer(
    View const& view, Rasterizer const& rasterizer, Pixel const& pixel)
{
    using tag_t = typename Rasterizer::type;
    detail::apply_rasterizer_op<View, Rasterizer, Pixel, tag_t>{}(
        view, rasterizer, pixel);
}

}} // namespace boost::gil

#endif
