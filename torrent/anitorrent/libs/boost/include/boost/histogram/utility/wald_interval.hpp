// Copyright 2022 Jay Gohil, Hans Dembinski
//
// Distributed under the Boost Software License, version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_HISTOGRAM_UTILITY_WALD_INTERVAL_HPP
#define BOOST_HISTOGRAM_UTILITY_WALD_INTERVAL_HPP

#include <boost/histogram/fwd.hpp>
#include <boost/histogram/utility/binomial_proportion_interval.hpp>
#include <cmath>
#include <utility>

namespace boost {
namespace histogram {
namespace utility {

/**
  Wald interval or normal approximation interval.

  The Wald interval is a symmetric interval. It is simple to compute, but has poor
  statistical properties and is universally rejected by statisticians. It should always be
  replaced by another iternal, for example, the Wilson interval.

  The Wald interval can be derived easily using the plug-in estimate of the variance for
  the binomial distribution, which is likely a reason for its omnipresence. Without
  further insight into statistical theory, it is not obvious that this derivation is
  flawed and that better alternatives exist.

  The Wald interval undercovers on average. It is unsuitable when the sample size is small
  or when the fraction is close to 0 or 1. e. Its limits are not naturally bounded by 0
  or 1. It produces empty intervals if the number of successes or failures is zero.

  For a critique of the Wald interval, see (a selection):

  L.D. Brown, T.T. Cai, A. DasGupta, Statistical Science 16 (2001) 101-133.
  R. D. Cousins, K. E. Hymes, J. Tucker, Nucl. Instrum. Meth. A 612 (2010) 388-398.
*/
template <class ValueType>
class wald_interval : public binomial_proportion_interval<ValueType> {
public:
  using value_type = typename wald_interval::value_type;
  using interval_type = typename wald_interval::interval_type;

  /** Construct Wald interval computer.

    @param d Number of standard deviations for the interval. The default value 1
    corresponds to a confidence level of 68 %. Both `deviation` and `confidence_level`
    objects can be used to initialize the interval.
  */
  explicit wald_interval(deviation d = deviation{1.0}) noexcept
      : z_{static_cast<value_type>(d)} {}

  using binomial_proportion_interval<ValueType>::operator();

  /** Compute interval for given number of successes and failures.

    @param successes Number of successful trials.
    @param failures Number of failed trials.
  */
  interval_type operator()(value_type successes, value_type failures) const noexcept {
    // See https://en.wikipedia.org/wiki/
    //   Binomial_proportion_confidence_interval
    //   #Normal_approximation_interval_or_Wald_interval
    const value_type total_inv = 1 / (successes + failures);
    const value_type a = successes * total_inv;
    const value_type b = (z_ * total_inv) * std::sqrt(successes * failures * total_inv);
    return {a - b, a + b};
  }

private:
  value_type z_;
};

} // namespace utility
} // namespace histogram
} // namespace boost

#endif