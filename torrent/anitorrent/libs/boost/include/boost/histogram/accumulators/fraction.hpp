// Copyright 2022 Jay Gohil, Hans Dembinski
//
// Distributed under the Boost Software License, version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_HISTOGRAM_ACCUMULATORS_FRACTION_HPP
#define BOOST_HISTOGRAM_ACCUMULATORS_FRACTION_HPP

#include <boost/core/nvp.hpp>
#include <boost/histogram/fwd.hpp> // for fraction<>
#include <boost/histogram/utility/wilson_interval.hpp>
#include <type_traits> // for std::common_type

namespace boost {
namespace histogram {
namespace accumulators {

/**
  Accumulate boolean samples and compute the fraction of true samples.

  This accumulator should be used to calculate the efficiency or success fraction of a
  random process as a function of process parameters. It returns the fraction of
  successes, the variance of this fraction, and a two-sided confidence interval with 68.3
  % confidence level for this fraction.

  There is no unique way to compute an interval for a success fraction. This class returns
  the Wilson score interval, because it is widely recommended in the literature for
  general use. More interval computers can be found in `boost/histogram/utility`, which
  can be used to compute intervals for other confidence levels.
*/
template <class ValueType>
class fraction {
public:
  using value_type = ValueType;
  using const_reference = const value_type&;
  using real_type = typename std::conditional<std::is_floating_point<value_type>::value,
                                              value_type, double>::type;
  using interval_type = typename utility::wilson_interval<real_type>::interval_type;

  fraction() noexcept = default;

  /// Initialize to external successes and failures.
  fraction(const_reference successes, const_reference failures) noexcept
      : succ_(successes), fail_(failures) {}

  /// Allow implicit conversion from fraction with a different value type.
  template <class T>
  fraction(const fraction<T>& e) noexcept
      : fraction{static_cast<value_type>(e.successes()),
                 static_cast<value_type>(e.failures())} {}

  /// Insert boolean sample x.
  void operator()(bool x) noexcept {
    if (x)
      ++succ_;
    else
      ++fail_;
  }

  /// Add another accumulator.
  fraction& operator+=(const fraction& rhs) noexcept {
    succ_ += rhs.succ_;
    fail_ += rhs.fail_;
    return *this;
  }

  /// Return number of boolean samples that were true.
  const_reference successes() const noexcept { return succ_; }

  /// Return number of boolean samples that were false.
  const_reference failures() const noexcept { return fail_; }

  /// Return total number of boolean samples.
  value_type count() const noexcept { return succ_ + fail_; }

  /// Return success fraction of boolean samples.
  real_type value() const noexcept { return static_cast<real_type>(succ_) / count(); }

  /// Return variance of the success fraction.
  real_type variance() const noexcept {
    // We want to compute Var(p) for p = X / n with Var(X) = n p (1 - p)
    // For Var(X) see
    // https://en.wikipedia.org/wiki/Binomial_distribution#Expected_value_and_variance
    // Error propagation: Var(p) = p'(X)^2 Var(X) = p (1 - p) / n
    const real_type p = value();
    return p * (1 - p) / count();
  }

  /// Return standard interval with 68.3 % confidence level (Wilson score interval).
  interval_type confidence_interval() const noexcept {
    return utility::wilson_interval<real_type>()(static_cast<real_type>(successes()),
                                                 static_cast<real_type>(failures()));
  }

  bool operator==(const fraction& rhs) const noexcept {
    return succ_ == rhs.succ_ && fail_ == rhs.fail_;
  }

  bool operator!=(const fraction& rhs) const noexcept { return !operator==(rhs); }

  template <class Archive>
  void serialize(Archive& ar, unsigned /* version */) {
    ar& make_nvp("successes", succ_);
    ar& make_nvp("failures", fail_);
  }

private:
  value_type succ_{};
  value_type fail_{};
};

} // namespace accumulators
} // namespace histogram
} // namespace boost

#ifndef BOOST_HISTOGRAM_DOXYGEN_INVOKED

namespace std {
template <class T, class U>
/// Specialization for boost::histogram::accumulators::fraction.
struct common_type<boost::histogram::accumulators::fraction<T>,
                   boost::histogram::accumulators::fraction<U>> {
  using type = boost::histogram::accumulators::fraction<common_type_t<T, U>>;
};
} // namespace std

#endif

#endif
