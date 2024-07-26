// Boost.Geometry

// Copyright (c) 2022 Adam Wulkiewicz, Lodz, Poland.

// Copyright (c) 2022, Oracle and/or its affiliates.
// Contributed and/or modified by Vissarion Fysikopoulos, on behalf of Oracle

// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

// This file is converted from PROJ, https://github.com/OSGeo/PROJ

// Last updated version of proj: 9.0.0

// Original copyright notice:

/******************************************************************************
 *
 * Project:  PROJ
 * Purpose:  Generic method to compute inverse projection from forward method
 * Author:   Even Rouault <even dot rouault at spatialys dot com>
 *
 ******************************************************************************
 * Copyright (c) 2018, Even Rouault <even dot rouault at spatialys dot com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ****************************************************************************/

#ifndef BOOST_GEOMETRY_PROJECTIONS_IMPL_PJ_GENERIC_INVERSE_HPP
#define BOOST_GEOMETRY_PROJECTIONS_IMPL_PJ_GENERIC_INVERSE_HPP

#include <algorithm>
#include <cmath>

#include <boost/geometry/util/math.hpp>

/** Compute (lam, phi) corresponding to input (xy_x, xy_y) for projection P.
 *
 * Uses Newton-Raphson method, extended to 2D variables, that is using
 * inversion of the Jacobian 2D matrix of partial derivatives. The derivatives
 * are estimated numerically from the P->fwd method evaluated at close points.
 *
 * Note: thresholds used have been verified to work with adams_ws2 and wink2
 *
 * Starts with initial guess provided by user in lpInitial
 */


namespace boost { namespace geometry { namespace projections
{

namespace detail
{

template <typename T, typename Parameters, typename Projection>
void pj_generic_inverse_2d(T const& xy_x,
                           T const& xy_y,
                           Parameters const& par,
                           Projection const& proj,
                           T& lp_lat,
                           T& lp_lon)
{
    T deriv_lam_X = 0;
    T deriv_lam_Y = 0;
    T deriv_phi_X = 0;
    T deriv_phi_Y = 0;

    for (int i = 0; i < 15; i++)
    {
        T xyApprox_x;
        T xyApprox_y;
        proj->fwd(par, lp_lon, lp_lat, xyApprox_x, xyApprox_y);
        T const deltaX = xyApprox_x - xy_x;
        T const deltaY = xyApprox_y - xy_y;
        if (fabs(deltaX) < 1e-10 && fabs(deltaY) < 1e-10) return;

        if (fabs(deltaX) > 1e-6 || fabs(deltaY) > 1e-6)
        {
            // Compute Jacobian matrix (only if we aren't close to the final
            // result to speed things a bit)
            T lp2_lat;
            T lp2_lon;
            T xy2_x;
            T xy2_y;
            T const dLam = lp_lat > 0 ? -1e-6 : 1e-6;
            lp2_lat = lp_lat + dLam;
            lp2_lon = lp_lon;
            proj->fwd(par, lp2_lon, lp2_lat, xy2_x, xy2_y);
            //xy2 = P->fwd(lp2, P);
            T const deriv_X_lam = (xy2_x - xyApprox_x) / dLam;
            T const deriv_Y_lam = (xy2_y - xyApprox_y) / dLam;

            T const dPhi = lp_lon > 0 ? -1e-6 : 1e-6;
            lp2_lat = lp_lat;
            lp2_lon = lp_lon + dPhi;
            proj->fwd(par, lp2_lon, lp2_lat, xy2_x, xy2_y);
            //xy2 = P->fwd(lp2, P);
            T const deriv_X_phi = (xy2_x - xyApprox_x) / dPhi;
            T const deriv_Y_phi = (xy2_y - xyApprox_y) / dPhi;

            // Inverse of Jacobian matrix
            T const det = deriv_X_lam * deriv_Y_phi - deriv_X_phi * deriv_Y_lam;
            if (det != 0)
            {
                deriv_lam_X = deriv_Y_phi / det;
                deriv_lam_Y = -deriv_X_phi / det;
                deriv_phi_X = -deriv_Y_lam / det;
                deriv_phi_Y = deriv_X_lam / det;
            }
        }

        if (xy_x != 0)
        {
            // Limit the amplitude of correction to avoid overshoots due to
            // bad initial guess
            T const delta_lam = (std::max)(
                (std::min)(deltaX * deriv_lam_X + deltaY * deriv_lam_Y, 0.3),
                -0.3);
            lp_lat -= delta_lam;
            if (lp_lat < -math::pi<T>())
                lp_lat = -math::pi<T>();
            else if (lp_lat > math::pi<T>())
                lp_lat = math::pi<T>();
        }

        if (xy_y != 0)
        {
            T const delta_phi = (std::max)(
                (std::min)(deltaX * deriv_phi_X + deltaY * deriv_phi_Y, 0.3),
                -0.3);
            lp_lon -= delta_phi;
            static T const half_pi = math::half_pi<T>();
            if (lp_lon < -half_pi)
                lp_lon = -half_pi;
            else if (lp_lon > half_pi)
                lp_lon = half_pi;
        }
    }
    //pj_ctx_set_errno(P->ctx, PJD_ERR_NON_CONVERGENT);
}

} // namespace detail

}}} // namespace boost::geometry::projections

#endif // BOOST_GEOMETRY_PROJECTIONS_IMPL_PJ_GENERIC_INVERSE_HPP
