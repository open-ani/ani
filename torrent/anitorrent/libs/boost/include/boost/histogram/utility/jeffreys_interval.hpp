// Copyright 2022 Jay Gohil, Hans Dembinski
//
// Distributed under the Boost Software License, version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_HISTOGRAM_UTILITY_JEFFREYS_INTERVAL_HPP
#define BOOST_HISTOGRAM_UTILITY_JEFFREYS_INTERVAL_HPP

#include <boost/histogram/fwd.hpp>
#include <boost/histogram/utility/binomial_proportion_interval.hpp>
#include <boost/math/distributions/beta.hpp>
#include <cmath>

namespace boost {
namespace histogram {
namespace utility {

/**
  Jeffreys interval.

  This is the Bayesian credible interval with a Jeffreys prior. Although it has a
  Bayesian derivation, it has good coverage. The interval boundaries are close to the
  Wilson interval. A special property of this interval is that it is equal-tailed; the
  probability of the true value to be above or below the interval is approximately equal.

  To avoid coverage probability tending to zero when the fraction approaches 0 or 1,
  this implementation uses a modification described in section 4.1.2 of the
  paper by L.D. Brown, T.T. Cai, A. DasGupta, Statistical Science 16 (2001) 101-133,
  doi:10.1214/ss/1009213286.
*/
template <class ValueType>
class jeffreys_interval : public binomial_proportion_interval<ValueType> {
public:
  using value_type = typename jeffreys_interval::value_type;
  using interval_type = typename jeffreys_interval::interval_type;

  /** Construct Jeffreys interval computer.

    @param cl Confidence level for the interval. The default value produces a
    confidence level of 68 % equivalent to one standard deviation. Both `deviation` and
    `confidence_level` objects can be used to initialize the interval.
  */
  explicit jeffreys_interval(confidence_level cl = deviation{1}) noexcept
      : alpha_half_{static_cast<value_type>(0.5 - 0.5 * static_cast<double>(cl))} {}

  using binomial_proportion_interval<ValueType>::operator();

  /** Compute interval for given number of successes and failures.

    @param successes Number of successful trials.
    @param failures Number of failed trials.
  */
  interval_type operator()(value_type successes, value_type failures) const noexcept {
    // See L.D. Brown, T.T. Cai, A. DasGupta, Statistical Science 16 (2001) 101-133,
    // doi:10.1214/ss/1009213286, section 4.1.2.
    const value_type half{0.5};
    const value_type total = successes + failures;

    // if successes or failures are 0, modified interval is equal to Clopper-Pearson
    if (successes == 0) return {0, 1 - std::pow(alpha_half_, 1 / total)};
    if (failures == 0) return {std::pow(alpha_half_, 1 / total), 1};

    math::beta_distribution<value_type> beta(successes + half, failures + half);
    const value_type a = successes == 1 ? 0 : math::quantile(beta, alpha_half_);
    const value_type b = failures == 1 ? 1 : math::quantile(beta, 1 - alpha_half_);
    return {a, b};
  }

private:
  value_type alpha_half_;
};

} // namespace utility
} // namespace histogram
} // namespace boost

#endif