// Copyright 2022 Jay Gohil, Hans Dembinski
//
// Distributed under the Boost Software License, version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_HISTOGRAM_UTILITY_BINOMIAL_PROPORTION_INTERVAL_HPP
#define BOOST_HISTOGRAM_UTILITY_BINOMIAL_PROPORTION_INTERVAL_HPP

#include <boost/histogram/detail/normal.hpp>
#include <boost/histogram/fwd.hpp>
#include <boost/throw_exception.hpp>
#include <cmath>
#include <stdexcept>
#include <type_traits>

namespace boost {
namespace histogram {
namespace utility {

/**
  Common base class for interval calculators.
*/
template <class ValueType>
class binomial_proportion_interval {
  static_assert(std::is_floating_point<ValueType>::value,
                "Value must be a floating point!");

public:
  using value_type = ValueType;
  using interval_type = std::pair<value_type, value_type>;

  /** Compute interval for given number of successes and failures.

    @param successes Number of successful trials.
    @param failures Number of failed trials.
  */
  virtual interval_type operator()(value_type successes,
                                   value_type failures) const noexcept = 0;

  /** Compute interval for a fraction accumulator.

    @param fraction Fraction accumulator.
  */
  template <class T>
  interval_type operator()(const accumulators::fraction<T>& fraction) const noexcept {
    return operator()(fraction.successes(), fraction.failures());
  }
};

class deviation;
class confidence_level;

/** Confidence level in units of deviations for intervals.

  Intervals become wider as the deviation value increases. The standard deviation
  corresponds to a value of 1 and corresponds to 68.3 % confidence level. The conversion
  between confidence level and deviations is based on a two-sided interval on the normal
  distribution.
 */
class deviation {
public:
  /// constructor from units of standard deviations
  explicit deviation(double d) : d_{d} {
    if (d <= 0)
      BOOST_THROW_EXCEPTION(std::invalid_argument("scaling factor must be positive"));
  }

  /// explicit conversion to units of standard deviations
  template <class T, class = std::enable_if_t<std::is_floating_point<T>::value>>
  explicit operator T() const noexcept {
    return static_cast<T>(d_);
  }

  /// implicit conversion to confidence level
  operator confidence_level() const noexcept; // need to implement confidence_level first

  friend deviation operator*(deviation d, double z) noexcept {
    return deviation(d.d_ * z);
  }
  friend deviation operator*(double z, deviation d) noexcept { return d * z; }
  friend bool operator==(deviation a, deviation b) noexcept { return a.d_ == b.d_; }
  friend bool operator!=(deviation a, deviation b) noexcept { return !(a == b); }

private:
  double d_;
};

/** Confidence level for intervals.

  Intervals become wider as the deviation value increases.
 */
class confidence_level {
public:
  /// constructor from confidence level (a probability)
  explicit confidence_level(double cl) : cl_{cl} {
    if (cl <= 0 || cl >= 1)
      BOOST_THROW_EXCEPTION(std::invalid_argument("0 < cl < 1 is required"));
  }

  /// explicit conversion to numerical confidence level
  template <class T, class = std::enable_if_t<std::is_floating_point<T>::value>>
  explicit operator T() const noexcept {
    return static_cast<T>(cl_);
  }

  /// implicit conversion to units of standard deviation
  operator deviation() const noexcept {
    return deviation{detail::normal_ppf(std::fma(0.5, cl_, 0.5))};
  }

  friend bool operator==(confidence_level a, confidence_level b) noexcept {
    return a.cl_ == b.cl_;
  }
  friend bool operator!=(confidence_level a, confidence_level b) noexcept {
    return !(a == b);
  }

private:
  double cl_;
};

inline deviation::operator confidence_level() const noexcept {
  // solve normal cdf(z) - cdf(-z) = 2 (cdf(z) - 0.5)
  return confidence_level{std::fma(2.0, detail::normal_cdf(d_), -1.0)};
}

} // namespace utility
} // namespace histogram
} // namespace boost

#endif