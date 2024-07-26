// Copyright 2022 Hans Dembinski, Jay Gohil
//
// Distributed under the Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_HISTOGRAM_DETAIL_NORMAL_HPP
#define BOOST_HISTOGRAM_DETAIL_NORMAL_HPP

#include <boost/histogram/detail/erf_inv.hpp>
#include <cmath>

namespace boost {
namespace histogram {
namespace detail {

inline double normal_cdf(double x) noexcept {
  return std::fma(0.5, std::erf(x / std::sqrt(2)), 0.5);
}

inline double normal_ppf(double p) noexcept {
  return std::sqrt(2) * erf_inv(2 * (p - 0.5));
}

} // namespace detail
} // namespace histogram
} // namespace boost

#endif
