// Copyright 2022 Hans Dembinski, Jay Gohil
//
// Distributed under the Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_HISTOGRAM_DETAIL_ERF_INF_HPP
#define BOOST_HISTOGRAM_DETAIL_ERF_INF_HPP

#include <cmath>

namespace boost {
namespace histogram {
namespace detail {

// Simple implementation of erf_inv so that we do not depend on boost::math.
// If you happen to discover this, prefer the boost::math implementation,
// it is more accurate for x very close to -1 or 1 and faster.
// The only virtue of this implementation is its simplicity.
template <int Iterate = 3>
double erf_inv(double x) noexcept {
  // Strategy: solve f(y) = x - erf(y) = 0 for given x with Newton's method.
  // f'(y) = -erf'(y) = -2/sqrt(pi) e^(-y^2)
  // Has quadratic convergence. Since erf_inv<0> is accurate to 1e-3,
  // we should have machine precision after three iterations.
  const double x0 = erf_inv<Iterate - 1>(x); // recursion
  const double fx0 = x - std::erf(x0);
  const double pi = std::acos(-1);
  double fpx0 = -2.0 / std::sqrt(pi) * std::exp(-x0 * x0);
  return x0 - fx0 / fpx0; // = x1
}

template <>
inline double erf_inv<0>(double x) noexcept {
  // Specialization to get initial estimate.
  // This formula is accurate to about 1e-3.
  // Based on https://stackoverflow.com/questions/27229371/inverse-error-function-in-c
  const double a = std::log((1 - x) * (1 + x));
  const double b = std::fma(0.5, a, 4.120666747961526);
  const double c = 6.47272819164 * a;
  return std::copysign(std::sqrt(-b + std::sqrt(std::fma(b, b, -c))), x);
}

} // namespace detail
} // namespace histogram
} // namespace boost

#endif
