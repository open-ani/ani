// Boost.Geometry - gis-projections (based on PROJ4)

// Copyright (c) 2008-2015 Barend Gehrels, Amsterdam, the Netherlands.
// Copyright (c) 2023 Adam Wulkiewicz, Lodz, Poland.

// This file was modified by Oracle on 2017, 2018, 2019, 2022.
// Modifications copyright (c) 2017-2022, Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle.
// Contributed and/or modified by Adam Wulkiewicz, on behalf of Oracle.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

// This file is converted from PROJ4, http://trac.osgeo.org/proj
// PROJ4 is originally written by Gerald Evenden (then of the USGS)
// PROJ4 is maintained by Frank Warmerdam
// PROJ4 is converted to Boost.Geometry by Barend Gehrels

// Last updated version of proj: 8.2.1

// Original copyright notice:

// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

#ifndef BOOST_GEOMETRY_PROJECTIONS_TMERC_HPP
#define BOOST_GEOMETRY_PROJECTIONS_TMERC_HPP

#include <boost/geometry/srs/projections/impl/base_static.hpp>
#include <boost/geometry/srs/projections/impl/base_dynamic.hpp>
#include <boost/geometry/srs/projections/impl/projects.hpp>
#include <boost/geometry/srs/projections/impl/factory_entry.hpp>
#include <boost/geometry/srs/projections/impl/function_overloads.hpp>
#include <boost/geometry/srs/projections/impl/pj_mlfn.hpp>

#include <boost/geometry/util/condition.hpp>
#include <boost/geometry/util/math.hpp>


namespace boost { namespace geometry
{

namespace projections
{
    #ifndef DOXYGEN_NO_DETAIL
    namespace detail { namespace tmerc
    {

        static const double epsilon10 = 1.e-10;

        /* Constant for "exact" transverse mercator */
        static const int proj_etmerc_order = 6;

        template <typename T>
        inline T FC1() { return 1.; }
        template <typename T>
        inline T FC2() { return .5; }
        template <typename T>
        inline T FC3() { return .16666666666666666666666666666666666666; }
        template <typename T>
        inline T FC4() { return .08333333333333333333333333333333333333; }
        template <typename T>
        inline T FC5() { return .05; }
        template <typename T>
        inline T FC6() { return .03333333333333333333333333333333333333; }
        template <typename T>
        inline T FC7() { return .02380952380952380952380952380952380952; }
        template <typename T>
        inline T FC8() { return .01785714285714285714285714285714285714; }

        template <typename T>
        struct par_tmerc
        {
            T    esp;
            T    ml0;
            detail::en<T> en;
        };

        // More exact: Poder/Engsager
        template <typename T>
        struct par_tmerc_exact
        {
            T    Qn;     /* Merid. quad., scaled to the projection */
            T    Zb;     /* Radius vector in polar coord. systems  */
            T    cgb[6]; /* Constants for Gauss -> Geo lat */
            T    cbg[6]; /* Constants for Geo lat -> Gauss */
            T    utg[6]; /* Constants for transv. merc. -> geo */
            T    gtu[6]; /* Constants for geo -> transv. merc. */
        };

        template <typename T, typename Parameters>
        struct base_tmerc_ellipsoid
        {
            par_tmerc<T> m_proj_parm;

            // FORWARD(e_forward)  ellipse
            // Project coordinates from geographic (lon, lat) to cartesian (x, y)
            inline void fwd(Parameters const& par, T const& lp_lon, T const& lp_lat, T& xy_x, T& xy_y) const
            {
                static const T half_pi = detail::half_pi<T>();
                static const T FC1 = tmerc::FC1<T>();
                static const T FC2 = tmerc::FC2<T>();
                static const T FC3 = tmerc::FC3<T>();
                static const T FC4 = tmerc::FC4<T>();
                static const T FC5 = tmerc::FC5<T>();
                static const T FC6 = tmerc::FC6<T>();
                static const T FC7 = tmerc::FC7<T>();
                static const T FC8 = tmerc::FC8<T>();

                T al, als, n, cosphi, sinphi, t;

                /*
                    * Fail if our longitude is more than 90 degrees from the
                    * central meridian since the results are essentially garbage.
                    * Is error -20 really an appropriate return value?
                    *
                    *  http://trac.osgeo.org/proj/ticket/5
                    */
                if( lp_lon < -half_pi || lp_lon > half_pi )
                {
                    xy_x = HUGE_VAL;
                    xy_y = HUGE_VAL;
                    BOOST_THROW_EXCEPTION( projection_exception(error_lat_or_lon_exceed_limit) );
                    return;
                }

                sinphi = sin(lp_lat);
                cosphi = cos(lp_lat);
                t = fabs(cosphi) > 1e-10 ? sinphi/cosphi : 0.;
                t *= t;
                al = cosphi * lp_lon;
                als = al * al;
                al /= sqrt(1. - par.es * sinphi * sinphi);
                n = this->m_proj_parm.esp * cosphi * cosphi;
                xy_x = par.k0 * al * (FC1 +
                    FC3 * als * (1. - t + n +
                    FC5 * als * (5. + t * (t - 18.) + n * (14. - 58. * t)
                    + FC7 * als * (61. + t * ( t * (179. - t) - 479. ) )
                    )));
                xy_y = par.k0 * (pj_mlfn(lp_lat, sinphi, cosphi, this->m_proj_parm.en) - this->m_proj_parm.ml0 +
                    sinphi * al * lp_lon * FC2 * ( 1. +
                    FC4 * als * (5. - t + n * (9. + 4. * n) +
                    FC6 * als * (61. + t * (t - 58.) + n * (270. - 330 * t)
                    + FC8 * als * (1385. + t * ( t * (543. - t) - 3111.) )
                    ))));
            }

            // INVERSE(e_inverse)  ellipsoid
            // Project coordinates from cartesian (x, y) to geographic (lon, lat)
            inline void inv(Parameters const& par, T const& xy_x, T const& xy_y, T& lp_lon, T& lp_lat) const
            {
                static const T half_pi = detail::half_pi<T>();
                static const T FC1 = tmerc::FC1<T>();
                static const T FC2 = tmerc::FC2<T>();
                static const T FC3 = tmerc::FC3<T>();
                static const T FC4 = tmerc::FC4<T>();
                static const T FC5 = tmerc::FC5<T>();
                static const T FC6 = tmerc::FC6<T>();
                static const T FC7 = tmerc::FC7<T>();
                static const T FC8 = tmerc::FC8<T>();

                T n, con, cosphi, d, ds, sinphi, t;

                lp_lat = pj_inv_mlfn(this->m_proj_parm.ml0 + xy_y / par.k0, par.es, this->m_proj_parm.en);
                if (fabs(lp_lat) >= half_pi) {
                    lp_lat = xy_y < 0. ? -half_pi : half_pi;
                    lp_lon = 0.;
                } else {
                    sinphi = sin(lp_lat);
                    cosphi = cos(lp_lat);
                    t = fabs(cosphi) > 1e-10 ? sinphi/cosphi : 0.;
                    n = this->m_proj_parm.esp * cosphi * cosphi;
                    d = xy_x * sqrt(con = 1. - par.es * sinphi * sinphi) / par.k0;
                    con *= t;
                    t *= t;
                    ds = d * d;
                    lp_lat -= (con * ds / (1.-par.es)) * FC2 * (1. -
                        ds * FC4 * (5. + t * (3. - 9. *  n) + n * (1. - 4 * n) -
                        ds * FC6 * (61. + t * (90. - 252. * n +
                            45. * t) + 46. * n
                        - ds * FC8 * (1385. + t * (3633. + t * (4095. + 1574. * t)) )
                        )));
                    lp_lon = d*(FC1 -
                        ds*FC3*( 1. + 2.*t + n -
                        ds*FC5*(5. + t*(28. + 24.*t + 8.*n) + 6.*n
                        - ds * FC7 * (61. + t * (662. + t * (1320. + 720. * t)) )
                    ))) / cosphi;
                }
            }

            static inline std::string get_name()
            {
                return "tmerc_ellipsoid";
            }

        };

        template <typename T, typename Parameters>
        struct base_tmerc_ellipsoid_exact
        {
            par_tmerc_exact<T> m_proj_parm;

            static inline std::string get_name()
            {
                return "tmerc_ellipsoid";
            }

            /* Helper functions for "exact" transverse mercator */
            inline
            static T gatg(const T *p1, int len_p1, T B, T cos_2B, T sin_2B)
            {
                T h = 0, h1, h2 = 0;

                const T two_cos_2B = 2*cos_2B;
                const T* p = p1 + len_p1;
                h1 = *--p;
                while (p - p1) {
                    h = -h2 + two_cos_2B*h1 + *--p;
                    h2 = h1;
                    h1 = h;
                }
                return (B + h*sin_2B);
            }

            /* Complex Clenshaw summation */
            inline
            static T clenS(const T *a, int size,
                            T sin_arg_r, T cos_arg_r,
                            T sinh_arg_i, T cosh_arg_i,
                            T *R, T *I)
            {
                T r, i, hr, hr1, hr2, hi, hi1, hi2;

                /* arguments */
                const T* p = a + size;
                r =  2*cos_arg_r*cosh_arg_i;
                i = -2*sin_arg_r*sinh_arg_i;

                /* summation loop */
                hi1 = hr1 = hi = 0;
                hr = *--p;
                for (; a - p;) {
                    hr2 = hr1;
                    hi2 = hi1;
                    hr1 = hr;
                    hi1 = hi;
                    hr  = -hr2 + r*hr1 - i*hi1 + *--p;
                    hi  = -hi2 + i*hr1 + r*hi1;
                }

                r   = sin_arg_r*cosh_arg_i;
                i   = cos_arg_r*sinh_arg_i;
                *R  = r*hr - i*hi;
                *I  = r*hi + i*hr;
                return *R;
            }

            /* Real Clenshaw summation */
            static T clens(const T *a, int size, T arg_r)
            {
                T r, hr, hr1, hr2, cos_arg_r;

                const T* p = a + size;
                cos_arg_r  = cos(arg_r);
                r          =  2*cos_arg_r;

                /* summation loop */
                hr1 = 0;
                hr = *--p;
                for (; a - p;) {
                    hr2 = hr1;
                    hr1 = hr;
                    hr  = -hr2 + r*hr1 + *--p;
                }
                return sin(arg_r)*hr;
            }

            /* Ellipsoidal, forward */
            //static PJ_XY exact_e_fwd (PJ_LP lp, PJ *P)
            inline void fwd(Parameters const& /*par*/,
                            T const& lp_lon,
                            T const& lp_lat,
                            T& xy_x, T& xy_y) const
            {
                //PJ_XY xy = {0.0,0.0};
                //const auto *Q = &(static_cast<struct tmerc_data*>(par.opaque)->exact);

                /* ell. LAT, LNG -> Gaussian LAT, LNG */
                T Cn  = gatg (this->m_proj_parm.cbg, proj_etmerc_order, lp_lat,
                    cos(2*lp_lat), sin(2*lp_lat));
                /* Gaussian LAT, LNG -> compl. sph. LAT */
                const T sin_Cn = sin (Cn);
                const T cos_Cn = cos (Cn);
                const T sin_Ce = sin (lp_lon);
                const T cos_Ce = cos (lp_lon);

                const T cos_Cn_cos_Ce = cos_Cn*cos_Ce;
                Cn = atan2 (sin_Cn, cos_Cn_cos_Ce);

                const T inv_denom_tan_Ce = 1. / hypot (sin_Cn, cos_Cn_cos_Ce);
                const T tan_Ce = sin_Ce*cos_Cn * inv_denom_tan_Ce;
            #if 0
                // Variant of the above: found not to be measurably faster
                const T sin_Ce_cos_Cn = sin_Ce*cos_Cn;
                const T denom = sqrt(1 - sin_Ce_cos_Cn * sin_Ce_cos_Cn);
                const T tan_Ce = sin_Ce_cos_Cn / denom;
            #endif

                /* compl. sph. N, E -> ell. norm. N, E */
                T Ce = asinh ( tan_Ce );     /* Replaces: Ce  = log(tan(FORTPI + Ce*0.5)); */

            /*
            *  Non-optimized version:
            *  const T sin_arg_r  = sin(2*Cn);
            *  const T cos_arg_r  = cos(2*Cn);
            *
            *  Given:
            *      sin(2 * Cn) = 2 sin(Cn) cos(Cn)
            *          sin(atan(y)) = y / sqrt(1 + y^2)
            *          cos(atan(y)) = 1 / sqrt(1 + y^2)
            *      ==> sin(2 * Cn) = 2 tan_Cn / (1 + tan_Cn^2)
            *
            *      cos(2 * Cn) = 2cos^2(Cn) - 1
            *                  = 2 / (1 + tan_Cn^2) - 1
            */
                const T two_inv_denom_tan_Ce = 2 * inv_denom_tan_Ce;
                const T two_inv_denom_tan_Ce_square = two_inv_denom_tan_Ce * inv_denom_tan_Ce;
                const T tmp_r = cos_Cn_cos_Ce * two_inv_denom_tan_Ce_square;
                const T sin_arg_r  = sin_Cn * tmp_r;
                const T cos_arg_r  = cos_Cn_cos_Ce * tmp_r - 1;

            /*
            *  Non-optimized version:
            *  const T sinh_arg_i = sinh(2*Ce);
            *  const T cosh_arg_i = cosh(2*Ce);
            *
            *  Given
            *      sinh(2 * Ce) = 2 sinh(Ce) cosh(Ce)
            *          sinh(asinh(y)) = y
            *          cosh(asinh(y)) = sqrt(1 + y^2)
            *      ==> sinh(2 * Ce) = 2 tan_Ce sqrt(1 + tan_Ce^2)
            *
            *      cosh(2 * Ce) = 2cosh^2(Ce) - 1
            *                   = 2 * (1 + tan_Ce^2) - 1
            *
            * and 1+tan_Ce^2 = 1 + sin_Ce^2 * cos_Cn^2 / (sin_Cn^2 + cos_Cn^2 * cos_Ce^2)
            *                = (sin_Cn^2 + cos_Cn^2 * cos_Ce^2 + sin_Ce^2 * cos_Cn^2) / (sin_Cn^2 + cos_Cn^2 * cos_Ce^2)
            *                = 1. / (sin_Cn^2 + cos_Cn^2 * cos_Ce^2)
            *                = inv_denom_tan_Ce^2
            *
            */
                const T sinh_arg_i = tan_Ce * two_inv_denom_tan_Ce;
                const T cosh_arg_i = two_inv_denom_tan_Ce_square - 1;

                T dCn, dCe;
                Cn += clenS (this->m_proj_parm.gtu, proj_etmerc_order,
                            sin_arg_r, cos_arg_r, sinh_arg_i, cosh_arg_i,
                            &dCn, &dCe);
                Ce += dCe;
                if (fabs (Ce) <= 2.623395162778) {
                    xy_y  = this->m_proj_parm.Qn * Cn + this->m_proj_parm.Zb;  /* Northing */
                    xy_x  = this->m_proj_parm.Qn * Ce;          /* Easting  */
                } else {
                    BOOST_THROW_EXCEPTION( projection_exception(error_tolerance_condition) );
                    xy_x = xy_y = HUGE_VAL;
                }
            }


            /* Ellipsoidal, inverse */
            inline void inv(Parameters const& /*par*/,
                            T const& xy_x,
                            T const& xy_y,
                            T& lp_lon,
                            T& lp_lat) const
            {
                //PJ_LP lp = {0.0,0.0};
                //const auto *Q = &(static_cast<struct tmerc_data*>(par.opaque)->exact);

                /* normalize N, E */
                T Cn = (xy_y - this->m_proj_parm.Zb)/this->m_proj_parm.Qn;
                T Ce = xy_x/this->m_proj_parm.Qn;

                if (fabs(Ce) <= 2.623395162778) { /* 150 degrees */
                    /* norm. N, E -> compl. sph. LAT, LNG */
                    const T sin_arg_r  = sin(2*Cn);
                    const T cos_arg_r  = cos(2*Cn);

                    //const T sinh_arg_i = sinh(2*Ce);
                    //const T cosh_arg_i = cosh(2*Ce);
                    const T exp_2_Ce = exp(2*Ce);
                    const T half_inv_exp_2_Ce = 0.5 / exp_2_Ce;
                    const T sinh_arg_i = 0.5 * exp_2_Ce - half_inv_exp_2_Ce;
                    const T cosh_arg_i = 0.5 * exp_2_Ce + half_inv_exp_2_Ce;

                    T dCn_ignored, dCe;
                    Cn += clenS(this->m_proj_parm.utg, proj_etmerc_order,
                                sin_arg_r, cos_arg_r, sinh_arg_i, cosh_arg_i,
                                &dCn_ignored, &dCe);
                    Ce += dCe;

                    /* compl. sph. LAT -> Gaussian LAT, LNG */
                    const T sin_Cn = sin (Cn);
                    const T cos_Cn = cos (Cn);

            #if 0
                    // Non-optimized version:
                    T sin_Ce, cos_Ce;
                    Ce = atan (sinh (Ce));  // Replaces: Ce = 2*(atan(exp(Ce)) - FORTPI);
                    sin_Ce = sin (Ce);
                    cos_Ce = cos (Ce);
                    Ce = atan2 (sin_Ce, cos_Ce*cos_Cn);
                    Cn = atan2 (sin_Cn*cos_Ce,  hypot (sin_Ce, cos_Ce*cos_Cn));
            #else
            /*
            *      One can divide both member of Ce = atan2(...) by cos_Ce, which gives:
            *      Ce     = atan2 (tan_Ce, cos_Cn) = atan2(sinh(Ce), cos_Cn)
            *
            *      and the same for Cn = atan2(...)
            *      Cn     = atan2 (sin_Cn, hypot (sin_Ce, cos_Ce*cos_Cn)/cos_Ce)
            *             = atan2 (sin_Cn, hypot (sin_Ce/cos_Ce, cos_Cn))
            *             = atan2 (sin_Cn, hypot (tan_Ce, cos_Cn))
            *             = atan2 (sin_Cn, hypot (sinhCe, cos_Cn))
            */
                    const T sinhCe = sinh (Ce);
                    Ce = atan2 (sinhCe, cos_Cn);
                    const T modulus_Ce = hypot (sinhCe, cos_Cn);
                    Cn = atan2 (sin_Cn, modulus_Ce);
            #endif

                    /* Gaussian LAT, LNG -> ell. LAT, LNG */

                    // Optimization of the computation of cos(2*Cn) and sin(2*Cn)
                    const T tmp = 2 * modulus_Ce / (sinhCe * sinhCe + 1);
                    const T sin_2_Cn = sin_Cn * tmp;
                    const T cos_2_Cn = tmp * modulus_Ce - 1.;
                    //const T cos_2_Cn = cos(2 * Cn);
                    //const T sin_2_Cn = sin(2 * Cn);

                    lp_lat = gatg (this->m_proj_parm.cgb,  proj_etmerc_order, Cn, cos_2_Cn, sin_2_Cn);
                    lp_lon = Ce;
                }
                else {
                    BOOST_THROW_EXCEPTION( projection_exception(error_tolerance_condition) );
                    lp_lat = lp_lon = HUGE_VAL;
                }
            }

        };

        template <typename T, typename Parameters>
        struct base_tmerc_spheroid
        {
            par_tmerc<T> m_proj_parm;

            // FORWARD(s_forward)  sphere
            // Project coordinates from geographic (lon, lat) to cartesian (x, y)
            inline void fwd(Parameters const& par, T const& lp_lon, T const& lp_lat, T& xy_x, T& xy_y) const
            {
                static const T half_pi = detail::half_pi<T>();

                T b, cosphi;

                /*
                    * Fail if our longitude is more than 90 degrees from the
                    * central meridian since the results are essentially garbage.
                    * Is error -20 really an appropriate return value?
                    *
                    *  http://trac.osgeo.org/proj/ticket/5
                    */
                if( lp_lon < -half_pi || lp_lon > half_pi )
                {
                    xy_x = HUGE_VAL;
                    xy_y = HUGE_VAL;
                    BOOST_THROW_EXCEPTION( projection_exception(error_lat_or_lon_exceed_limit) );
                    return;
                }

                cosphi = cos(lp_lat);
                b = cosphi * sin(lp_lon);
                if (fabs(fabs(b) - 1.) <= epsilon10)
                    BOOST_THROW_EXCEPTION( projection_exception(error_tolerance_condition) );

                xy_x = this->m_proj_parm.ml0 * log((1. + b) / (1. - b));
                xy_y = cosphi * cos(lp_lon) / sqrt(1. - b * b);

                b = fabs( xy_y );
                if (b >= 1.) {
                    if ((b - 1.) > epsilon10)
                        BOOST_THROW_EXCEPTION( projection_exception(error_tolerance_condition) );
                    else xy_y = 0.;
                } else
                    xy_y = acos(xy_y);

                if (lp_lat < 0.)
                    xy_y = -xy_y;
                xy_y = this->m_proj_parm.esp * (xy_y - par.phi0);
            }

            // INVERSE(s_inverse)  sphere
            // Project coordinates from cartesian (x, y) to geographic (lon, lat)
            inline void inv(Parameters const& par, T const& xy_x, T const& xy_y, T& lp_lon, T& lp_lat) const
            {
                T h, g;

                h = exp(xy_x / this->m_proj_parm.esp);
                g = .5 * (h - 1. / h);
                h = cos(par.phi0 + xy_y / this->m_proj_parm.esp);
                lp_lat = asin(sqrt((1. - h * h) / (1. + g * g)));

                /* Make sure that phi is on the correct hemisphere when false northing is used */
                if (xy_y < 0. && -lp_lat+par.phi0 < 0.0) lp_lat = -lp_lat;

                lp_lon = (g != 0.0 || h != 0.0) ? atan2(g, h) : 0.;
            }

            static inline std::string get_name()
            {
                return "tmerc_spheroid";
            }

        };

        template <typename Parameters, typename T>
        inline void setup(Parameters const& par, par_tmerc<T>& proj_parm)
        {
            if (par.es != 0.0) {
                proj_parm.en = pj_enfn<T>(par.es);
                proj_parm.ml0 = pj_mlfn(par.phi0, sin(par.phi0), cos(par.phi0), proj_parm.en);
                proj_parm.esp = par.es / (1. - par.es);
            } else {
                proj_parm.esp = par.k0;
                proj_parm.ml0 = .5 * proj_parm.esp;
            }
        }

        template <typename Parameters, typename T>
        inline void setup_exact(Parameters const& par, par_tmerc_exact<T>& proj_parm)
        {
            assert( par.es > 0 );

            /* third flattening n */
            //since we do not keep n in parameters we compute it here;
            const T n = pow(tan(asin(par.e)/2),2);
            T np = n;

            /* COEF. OF TRIG SERIES GEO <-> GAUSS */
            /* cgb := Gaussian -> Geodetic, KW p190 - 191 (61) - (62) */
            /* cbg := Geodetic -> Gaussian, KW p186 - 187 (51) - (52) */
            /* PROJ_ETMERC_ORDER = 6th degree : Engsager and Poder: ICC2007 */

            proj_parm.cgb[0] = n*( 2 + n*(-2/3.0  + n*(-2      + n*(116/45.0 + n*(26/45.0 +
                        n*(-2854/675.0 ))))));
            proj_parm.cbg[0] = n*(-2 + n*( 2/3.0  + n*( 4/3.0  + n*(-82/45.0 + n*(32/45.0 +
                        n*( 4642/4725.0))))));
            np     *= n;
            proj_parm.cgb[1] = np*(7/3.0 + n*( -8/5.0  + n*(-227/45.0 + n*(2704/315.0 +
                        n*( 2323/945.0)))));
            proj_parm.cbg[1] = np*(5/3.0 + n*(-16/15.0 + n*( -13/9.0  + n*( 904/315.0 +
                        n*(-1522/945.0)))));
            np     *= n;
            /* n^5 coeff corrected from 1262/105 -> -1262/105 */
            proj_parm.cgb[2] = np*( 56/15.0  + n*(-136/35.0 + n*(-1262/105.0 +
                        n*( 73814/2835.0))));
            proj_parm.cbg[2] = np*(-26/15.0  + n*(  34/21.0 + n*(    8/5.0   +
                        n*(-12686/2835.0))));
            np     *= n;
            /* n^5 coeff corrected from 322/35 -> 332/35 */
            proj_parm.cgb[3] = np*(4279/630.0 + n*(-332/35.0 + n*(-399572/14175.0)));
            proj_parm.cbg[3] = np*(1237/630.0 + n*( -12/5.0  + n*( -24832/14175.0)));
            np     *= n;
            proj_parm.cgb[4] = np*(4174/315.0 + n*(-144838/6237.0 ));
            proj_parm.cbg[4] = np*(-734/315.0 + n*( 109598/31185.0));
            np     *= n;
            proj_parm.cgb[5] = np*(601676/22275.0 );
            proj_parm.cbg[5] = np*(444337/155925.0);

            /* Constants of the projections */
            /* Transverse Mercator (UTM, ITM, etc) */
            np = n*n;
            /* Norm. mer. quad, K&W p.50 (96), p.19 (38b), p.5 (2) */
            proj_parm.Qn = par.k0/(1 + n) * (1 + np*(1/4.0 + np*(1/64.0 + np/256.0)));
            /* coef of trig series */
            /* utg := ell. N, E -> sph. N, E,  KW p194 (65) */
            /* gtu := sph. N, E -> ell. N, E,  KW p196 (69) */
            proj_parm.utg[0] = n*(-0.5  + n*( 2/3.0 + n*(-37/96.0 + n*( 1/360.0 +
                        n*(  81/512.0 + n*(-96199/604800.0))))));
            proj_parm.gtu[0] = n*( 0.5  + n*(-2/3.0 + n*(  5/16.0 + n*(41/180.0 +
                        n*(-127/288.0 + n*(  7891/37800.0 ))))));
            proj_parm.utg[1] = np*(-1/48.0 + n*(-1/15.0 + n*(437/1440.0 + n*(-46/105.0 +
                        n*( 1118711/3870720.0)))));
            proj_parm.gtu[1] = np*(13/48.0 + n*(-3/5.0  + n*(557/1440.0 + n*(281/630.0 +
                        n*(-1983433/1935360.0)))));
            np      *= n;
            proj_parm.utg[2] = np*(-17/480.0 + n*(  37/840.0 + n*(  209/4480.0  +
                        n*( -5569/90720.0 ))));
            proj_parm.gtu[2] = np*( 61/240.0 + n*(-103/140.0 + n*(15061/26880.0 +
                        n*(167603/181440.0))));
            np      *= n;
            proj_parm.utg[3] = np*(-4397/161280.0 + n*(  11/504.0 + n*( 830251/7257600.0)));
            proj_parm.gtu[3] = np*(49561/161280.0 + n*(-179/168.0 + n*(6601661/7257600.0)));
            np     *= n;
            proj_parm.utg[4] = np*(-4583/161280.0 + n*(  108847/3991680.0));
            proj_parm.gtu[4] = np*(34729/80640.0  + n*(-3418889/1995840.0));
            np     *= n;
            proj_parm.utg[5] = np*(-20648693/638668800.0);
            proj_parm.gtu[5] = np*(212378941/319334400.0);

            /* Gaussian latitude value of the origin latitude */
            const T Z = base_tmerc_ellipsoid_exact<T, Parameters>::gatg (proj_parm.cbg, proj_etmerc_order, par.phi0, cos(2*par.phi0), sin(2*par.phi0));

            /* Origin northing minus true northing at the origin latitude */
            /* i.e. true northing = N - par.Zb                         */
            proj_parm.Zb  = - proj_parm.Qn*(Z + base_tmerc_ellipsoid_exact<T, Parameters>::clens(proj_parm.gtu, proj_etmerc_order, 2*Z));
        }

    }} // namespace detail::tmerc
    #endif // doxygen

    /*!
        \brief Transverse Mercator projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Cylindrical
         - Spheroid
         - Ellipsoid
        \par Example
        \image html ex_tmerc.gif
    */
    //approximate tmerc algorithm
    /*
    template <typename T, typename Parameters>
    struct tmerc_ellipsoid : public detail::tmerc::base_tmerc_ellipsoid<T, Parameters>
    {
        template <typename Params>
        inline tmerc_ellipsoid(Params const&, Parameters const& par)
        {
            detail::tmerc::setup(par, this->m_proj_parm);
        }
    };
    */
    template <typename T, typename Parameters>
    struct tmerc_ellipsoid : public detail::tmerc::base_tmerc_ellipsoid_exact<T, Parameters>
    {
        template <typename Params>
        inline tmerc_ellipsoid(Params const&, Parameters const& par)
        {
            detail::tmerc::setup_exact(par, this->m_proj_parm);
        }
    };

    /*!
        \brief Transverse Mercator projection
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
        \par Projection characteristics
         - Cylindrical
         - Spheroid
         - Ellipsoid
        \par Example
        \image html ex_tmerc.gif
    */
    template <typename T, typename Parameters>
    struct tmerc_spheroid : public detail::tmerc::base_tmerc_spheroid<T, Parameters>
    {
        template <typename Params>
        inline tmerc_spheroid(Params const&, Parameters const& par)
        {
            detail::tmerc::setup(par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_DETAIL
    namespace detail
    {

        // Static projection
        BOOST_GEOMETRY_PROJECTIONS_DETAIL_STATIC_PROJECTION_FI2(srs::spar::proj_tmerc, tmerc_spheroid, tmerc_ellipsoid)

        // Factory entry(s) - dynamic projection
        BOOST_GEOMETRY_PROJECTIONS_DETAIL_FACTORY_ENTRY_FI2(tmerc_entry, tmerc_spheroid, tmerc_ellipsoid)

        BOOST_GEOMETRY_PROJECTIONS_DETAIL_FACTORY_INIT_BEGIN(tmerc_init)
        {
            BOOST_GEOMETRY_PROJECTIONS_DETAIL_FACTORY_INIT_ENTRY(tmerc, tmerc_entry)
        }

    } // namespace detail
    #endif // doxygen

} // namespace projections

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_PROJECTIONS_TMERC_HPP

