// Boost.Geometry - gis-projections (based on PROJ4)

// Copyright (c) 2022, Oracle and/or its affiliates.

// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle.

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

// This file is converted from PROJ4, http://trac.osgeo.org/proj
// PROJ4 is originally written by Gerald Evenden (then of the USGS)
// PROJ4 is maintained by Frank Warmerdam
// PROJ4 is converted to Boost.Geometry by Barend Gehrels

// Last updated version of proj: 9.0.0

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

#ifndef BOOST_GEOMETRY_PROJECTIONS_COL_URBAN_HPP
#define BOOST_GEOMETRY_PROJECTIONS_COL_URBAN_HPP

#include <boost/geometry/util/math.hpp>

#include <boost/geometry/srs/projections/impl/base_static.hpp>
#include <boost/geometry/srs/projections/impl/base_dynamic.hpp>
#include <boost/geometry/srs/projections/impl/pj_param.hpp>
#include <boost/geometry/srs/projections/impl/projects.hpp>
#include <boost/geometry/srs/projections/impl/factory_entry.hpp>

namespace boost { namespace geometry
{

namespace projections
{
    #ifndef DOXYGEN_NO_DETAIL
    namespace detail { namespace col_urban
    {
            template <typename T>
            struct par_col_urban
            {
                T h0; // height of projection origin, divided by semi-major axis (a)
                T rho0; // adimensional value, contrary to Guidance note 7.2
                T A;
                T B; // adimensional value, contrary to Guidance note 7.2
                T C;
                T D; // adimensional value, contrary to Guidance note 7.2
            };

            template <typename T, typename Parameters>
            struct base_col_urban_spheroid
            {
                par_col_urban<T> m_proj_parm;

                // FORWARD(s_forward)  spheroid
                // Project coordinates from geographic (lon, lat) to cartesian (x, y)
                inline void fwd(Parameters const& par, T const& lp_lon, T const& lp_lat, T& xy_x, T& xy_y) const
                {
                    const T cosphi = cos(lp_lat);
                    const T sinphi = sin(lp_lat);
                    const T nu = 1. / sqrt(1 - par.es * sinphi * sinphi);
                    const T lam_nu_cosphi = lp_lon * nu * cosphi;
                    xy_x = this->m_proj_parm.A * lam_nu_cosphi;
                    const T sinphi_m = sin(0.5 * (lp_lat + par.phi0));
                    const T rho_m = (1 - par.es) / pow(1 - par.es * sinphi_m * sinphi_m, 1.5);
                    const T G = 1 + this->m_proj_parm.h0 / rho_m;
                    xy_y = G * this->m_proj_parm.rho0 * ((lp_lat - par.phi0) + this->m_proj_parm.B * lam_nu_cosphi * lam_nu_cosphi);
                }

                // INVERSE(s_inverse)  spheroid
                // Project coordinates from cartesian (x, y) to geographic (lon, lat)
                inline void inv(Parameters const& par, T const& xy_x, T const& xy_y, T& lp_lon, T& lp_lat) const
                {
                    lp_lat = par.phi0 + xy_y / this->m_proj_parm.D - this->m_proj_parm.B * (xy_x / this->m_proj_parm.C) * (xy_x / this->m_proj_parm.C);
                    const double sinphi = sin(lp_lat);
                    const double nu = 1. / sqrt(1 - par.es * sinphi * sinphi);
                    lp_lon = xy_x / (this->m_proj_parm.C * nu * cos(lp_lat));

                }

                static inline std::string get_name()
                {
                    return "col_urban_spheroid";
                }

            };

            // Colombia Urban
            template <typename Params, typename Parameters, typename T>
            inline void setup(Params const& params, Parameters const& par, par_col_urban<T>& proj_parm)
            {
                T h0_unscaled;
                if ( !pj_param_f<srs::spar::h_0>(params, "h_0", srs::dpar::h_0, h0_unscaled) ){
                    h0_unscaled = T(0);
                }
                proj_parm.h0 = h0_unscaled / par.a;
                const T sinphi0 = sin(par.phi0);
                const T nu0 = 1. / sqrt(1 - par.es * sinphi0 * sinphi0);
                proj_parm.A = 1 + proj_parm.h0 / nu0;
                proj_parm.rho0 = (1 - par.es) / pow(1 - par.es * sinphi0 * sinphi0, 1.5);
                proj_parm.B = tan(par.phi0) / (2 * proj_parm.rho0 * nu0);
                proj_parm.C = 1 + proj_parm.h0;
                proj_parm.D = proj_parm.rho0 * (1 + proj_parm.h0 / (1 - par.es));
            }

    }} // namespace col_urban
    #endif // doxygen

    /*!
        \brief Colombia Urban
        \ingroup projections
        \tparam Geographic latlong point type
        \tparam Cartesian xy point type
        \tparam Parameters parameter type
    */
    template <typename T, typename Parameters>
    struct col_urban_spheroid : public detail::col_urban::base_col_urban_spheroid<T, Parameters>
    {
        template <typename Params>
        inline col_urban_spheroid(Params const& params, Parameters & par)
        {
            detail::col_urban::setup(params, par, this->m_proj_parm);
        }
    };

    #ifndef DOXYGEN_NO_DETAIL
    namespace detail
    {

        // Static projection
        BOOST_GEOMETRY_PROJECTIONS_DETAIL_STATIC_PROJECTION_FI(srs::spar::proj_col_urban, col_urban_spheroid)

        // Factory entry(s)
        BOOST_GEOMETRY_PROJECTIONS_DETAIL_FACTORY_ENTRY_FI(col_urban_entry, col_urban_spheroid)

        BOOST_GEOMETRY_PROJECTIONS_DETAIL_FACTORY_INIT_BEGIN(col_urban_init)
        {
            BOOST_GEOMETRY_PROJECTIONS_DETAIL_FACTORY_INIT_ENTRY(col_urban, col_urban_entry);
        }

    } // namespace detail
    #endif // doxygen

} // namespace projections

}} // namespace boost::geometry

#endif // BOOST_GEOMETRY_PROJECTIONS_COL_URBAN_HPP
