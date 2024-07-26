//
// Copyright 2005-2007 Adobe Systems Incorporated
//
// Distributed under the Boost Software License, Version 1.0
// See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt
//
#ifndef BOOST_GIL_EXTENSION_NUMERIC_AFFINE_HPP
#define BOOST_GIL_EXTENSION_NUMERIC_AFFINE_HPP

#include <boost/gil/point.hpp>

namespace boost { namespace gil {

////////////////////////////////////////////////////////////////////////////////////////
///
/// Simple matrix to do 2D affine transformations. It is actually 3x3 but the last column is [0 0 1]
///
////////////////////////////////////////////////////////////////////////////////////////
template <typename T>
class matrix3x2 {
public:
    matrix3x2() : a(1), b(0), c(0), d(1), e(0), f(0) {}
    matrix3x2(T A, T B, T C, T D, T E, T F) : a(A),b(B),c(C),d(D),e(E),f(F) {}
    matrix3x2(const matrix3x2& mat) : a(mat.a), b(mat.b), c(mat.c), d(mat.d), e(mat.e), f(mat.f) {}
    matrix3x2& operator=(const matrix3x2& m)           { a=m.a; b=m.b; c=m.c; d=m.d; e=m.e; f=m.f; return *this; }

    matrix3x2& operator*=(const matrix3x2& m)          { (*this) = (*this)*m; return *this; }

    static matrix3x2 get_rotate(T rads)                { T c=std::cos(rads); T s=std::sin(rads); return matrix3x2(c,s,-s,c,0,0); }
    static matrix3x2 get_translate(point<T> const& t)
    {
        return matrix3x2(1, 0, 0, 1, t.x, t.y);
    }
    static matrix3x2 get_translate(T x, T y)           { return matrix3x2(1  ,0,0,1  ,x,  y  ); }
    static matrix3x2 get_scale(point<T> const& s)
    {
        return matrix3x2(s.x, 0, 0, s.y, 0, 0);
    }
    static matrix3x2 get_scale(T x, T y)           { return matrix3x2(x,  0,0,y,  0  ,0  ); }
    static matrix3x2 get_scale(T s)                { return matrix3x2(s  ,0,0,s  ,0  ,0  ); }

    T a,b,c,d,e,f;
};

template <typename T> BOOST_FORCEINLINE
matrix3x2<T> operator*(const matrix3x2<T>& m1, const matrix3x2<T>& m2) {
    return matrix3x2<T>(
                m1.a * m2.a + m1.b * m2.c,
                m1.a * m2.b + m1.b * m2.d,
                m1.c * m2.a + m1.d * m2.c,
                m1.c * m2.b + m1.d * m2.d,
                m1.e * m2.a + m1.f * m2.c + m2.e,
                m1.e * m2.b + m1.f * m2.d + m2.f );
}

template <typename T, typename F>
BOOST_FORCEINLINE
point<F> operator*(point<T> const& p, matrix3x2<F> const& m)
{
    return { m.a*p.x + m.c*p.y + m.e, m.b*p.x + m.d*p.y + m.f };
}

////////////////////////////////////////////////////////////////////////////////////////
/// Define affine mapping that transforms the source coordinates by the affine transformation
////////////////////////////////////////////////////////////////////////////////////////
/*
template <typename MapFn>
concept MappingFunctionConcept {
    typename mapping_traits<MapFn>::result_type;   where PointNDConcept<result_type>;

    template <typename Domain> { where PointNDConcept<Domain> }
    result_type transform(MapFn&, const Domain& src);
};
*/

template <typename T> struct mapping_traits;

template <typename F>
struct mapping_traits<matrix3x2<F>>
{
    using result_type =  point<F>;
};

template <typename F, typename F2>
BOOST_FORCEINLINE
point<F> transform(matrix3x2<F> const& mat, point<F2> const& src)
{
    return src * mat;
}

/// Returns the inverse of the given affine transformation matrix
///
/// \warning Floating point arithmetic, use Boost.Rational if precision maters
template <typename T>
boost::gil::matrix3x2<T> inverse(boost::gil::matrix3x2<T> m)
{
    T const determinant = m.a * m.d - m.b * m.c;

    boost::gil::matrix3x2<T> res;
    res.a = m.d / determinant;
    res.b = -m.b / determinant;
    res.c = -m.c / determinant;
    res.d = m.a / determinant;
    res.e = (m.c * m.f - m.d * m.e) / determinant;
    res.f = (m.b * m.e - m.a * m.f) / determinant;

    return res;
}

/// \fn gil::matrix3x2 center_rotate
/// \tparam T     Data type for source image dimensions
/// \tparam F     Data type for angle through which image is to be rotated
/// @param  dims  dimensions of source image
/// @param  rads  angle through which image is to be rotated
/// @return   A transformation matrix for rotating the source image about its center
/// \brief    rotates an image from its center point
///           using consecutive affine transformations.
template<typename T, typename F>
boost::gil::matrix3x2<F> center_rotate(boost::gil::point<T> dims,F rads)
{
    const F PI = F(3.141592653589793238);
    const F c_theta = std::abs(std::cos(rads));
    const F s_theta = std::abs(std::sin(rads));

    // Bound checks for angle rads
    while(rads + PI < 0)
    {
        rads = rads + PI;
    }

    while(rads > PI)
    {
        rads = rads - PI;
    }

    // Basic Rotation Matrix
    boost::gil::matrix3x2<F> rotate = boost::gil::matrix3x2<F>::get_rotate(rads);

    // Find distance for translating the image into view
    boost::gil::matrix3x2<F> translation(0,0,0,0,0,0);
    if(rads > 0)
    {
        translation.b = s_theta;
    }
    else
    {
        translation.c = s_theta;
    }

    if(std::abs(rads) > PI/2)
    {
        translation.a = c_theta;
        translation.d = c_theta;
    }

    // To bring the complete image into view
    boost::gil::matrix3x2<F> translate =
        boost::gil::matrix3x2<F>::get_translate(-1 * dims * translation);

    // To fit inside the source dimensions
    boost::gil::matrix3x2<F> scale =
        boost::gil::matrix3x2<F>::get_scale(
            s_theta * dims.y / dims.x + c_theta ,
            s_theta * dims.x / dims.y + c_theta
        );

    return scale *  translate * rotate;
}

}} // namespace boost::gil

#endif