// Copyright 2022 Jay Gohil, Hans Dembinski
//
// Distributed under the Boost Software License, version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_HISTOGRAM_UTILITY_WILSON_INTERVAL_HPP
#define BOOST_HISTOGRAM_UTILITY_WILSON_INTERVAL_HPP

#include <boost/histogram/fwd.hpp>
#include <boost/histogram/utility/binomial_proportion_interval.hpp>
#include <cmath>
#include <utility>

namespace boost {
namespace histogram {
namespace utility {

/**
  Wilson interval.

  The Wilson score interval is simple to compute, has good coverage. Intervals are
  automatically bounded between 0 and 1 and never empty. The interval is asymmetric.

  Wilson, E. B. (1927). "Probable inference, the law of succession, and statistical
  inference". Journal of the American Statistical Association. 22 (158): 209-212.
  doi:10.1080/01621459.1927.10502953. JSTOR 2276774.

  The coverage probability for a random ensemble of fractions is close to the nominal
  value. Unlike the Clopper-Pearson interval, the Wilson score interval is not
  conservative. For some values of the fractions, the interval undercovers and overcovers
  for neighboring values. This is a shared property of all alternatives to the
  Clopper-Pearson interval.

  The Wilson score intervals is widely recommended for general use in the literature. For
  a review of the literature, see R. D. Cousins, K. E. Hymes, J. Tucker, Nucl. Instrum.
  Meth. A 612 (2010) 388-398.
*/
template <class ValueType>
class wilson_interval : public binomial_proportion_interval<ValueType> {
public:
  using value_type = typename wilson_interval::value_type;
  using interval_type = typename wilson_interval::interval_type;

  /** Construct Wilson interval computer.

    @param d Number of standard deviations for the interval. The default value 1
    corresponds to a confidence level of 68 %. Both `deviation` and `confidence_level`
    objects can be used to initialize the interval.
  */
  explicit wilson_interval(deviation d = deviation{1.0}) noexcept
      : z_{static_cast<value_type>(d)} {}

  using binomial_proportion_interval<ValueType>::operator();

  /** Compute interval for given number of successes and failures.

    @param successes Number of successful trials.
    @param failures Number of failed trials.
  */
  interval_type operator()(value_type successes, value_type failures) const noexcept {
    // See https://en.wikipedia.org/wiki/
    //   Binomial_proportion_confidence_interval
    //   #Wilson_score_interval

    // We make sure calculation is done in single precision if value_type is float
    // by converting all literals to value_type. Double literals in the equation
    // would turn intermediate values to double.
    const value_type half{0.5}, quarter{0.25}, zsq{z_ * z_};
    const value_type total = successes + failures;
    const value_type minv = 1 / (total + zsq);
    const value_type t1 = (successes + half * zsq) * minv;
    const value_type t2 =
        z_ * minv * std::sqrt(successes * failures / total + quarter * zsq);
    return {t1 - t2, t1 + t2};
  }

private:
  value_type z_;
};

} // namespace utility
} // namespace histogram
} // namespace boost

#endif