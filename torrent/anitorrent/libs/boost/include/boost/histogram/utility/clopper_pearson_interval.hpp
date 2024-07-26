// Copyright 2022 Jay Gohil, Hans Dembinski
//
// Distributed under the Boost Software License, version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_HISTOGRAM_UTILITY_CLOPPER_PEARSON_INTERVAL_HPP
#define BOOST_HISTOGRAM_UTILITY_CLOPPER_PEARSON_INTERVAL_HPP

#include <boost/histogram/fwd.hpp>
#include <boost/histogram/utility/binomial_proportion_interval.hpp>
#include <boost/math/distributions/beta.hpp>
#include <cmath>

namespace boost {
namespace histogram {
namespace utility {

/**
  Clopper-Pearson interval.

  This is the classic frequentist interval obtained with the Neyman construction.
  It is therefore often called the 'exact' interval. It is guaranteed to have at least the
  requested confidence level for all values of the fraction.

  The interval is wider than others that produce coverage closer to the expected
  confidence level over a random ensemble of factions. The Clopper-Pearson interval
  essentially always overcovers for such a random ensemble, which is undesirable in
  practice. The Clopper-Pearson interval is recommended when it is important to be
  conservative, but the Wilson interval should be preferred in most applications.

  C. Clopper, E.S. Pearson (1934), Biometrika 26 (4): 404-413.
  doi:10.1093/biomet/26.4.404.
*/
template <class ValueType>
class clopper_pearson_interval : public binomial_proportion_interval<ValueType> {
public:
  using value_type = typename clopper_pearson_interval::value_type;
  using interval_type = typename clopper_pearson_interval::interval_type;

  /** Construct Clopper-Pearson interval computer.

    @param cl Confidence level for the interval. The default value produces a
    confidence level of 68 % equivalent to one standard deviation. Both `deviation` and
    `confidence_level` objects can be used to initialize the interval.
  */
  explicit clopper_pearson_interval(confidence_level cl = deviation{1}) noexcept
      : alpha_half_{static_cast<value_type>(0.5 - 0.5 * static_cast<double>(cl))} {}

  using binomial_proportion_interval<ValueType>::operator();

  /** Compute interval for given number of successes and failures.

    @param successes Number of successful trials.
    @param failures Number of failed trials.
  */
  interval_type operator()(value_type successes, value_type failures) const noexcept {
    // analytical solution when successes or failures are zero
    // T. Mans (2014), Electronic Journal of Statistics. 8 (1): 817-840.
    // arXiv:1303.1288. doi:10.1214/14-EJS909.
    const value_type total = successes + failures;
    if (successes == 0) return {0, 1 - std::pow(alpha_half_, 1 / total)};
    if (failures == 0) return {std::pow(alpha_half_, 1 / total), 1};

    // Source:
    // https://en.wikipedia.org/wiki/
    //   Binomial_proportion_confidence_interval#Clopper%E2%80%93Pearson_interval
    math::beta_distribution<value_type> beta_a(successes, failures + 1);
    const value_type a = math::quantile(beta_a, alpha_half_);
    math::beta_distribution<value_type> beta_b(successes + 1, failures);
    const value_type b = math::quantile(beta_b, 1 - alpha_half_);
    return {a, b};
  }

private:
  value_type alpha_half_;
};

} // namespace utility
} // namespace histogram
} // namespace boost

#endif