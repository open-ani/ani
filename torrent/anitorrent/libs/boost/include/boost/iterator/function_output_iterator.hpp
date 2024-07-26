// (C) Copyright Jeremy Siek 2001.
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

// Revision History:

// 27 Feb 2001   Jeremy Siek
//      Initial checkin.

#ifndef BOOST_ITERATOR_FUNCTION_OUTPUT_ITERATOR_HPP
#define BOOST_ITERATOR_FUNCTION_OUTPUT_ITERATOR_HPP

#include <iterator>
#include <boost/config.hpp>
#include <boost/core/enable_if.hpp>
#include <boost/type_traits/is_same.hpp>
#include <boost/type_traits/remove_cv.hpp>
#ifndef BOOST_NO_CXX11_RVALUE_REFERENCES
#include <boost/type_traits/remove_reference.hpp>
#endif

namespace boost {
namespace iterators {

  template <class UnaryFunction>
  class function_output_iterator {
  private:
    typedef function_output_iterator self;

    class output_proxy {
    public:
      explicit output_proxy(UnaryFunction& f) BOOST_NOEXCEPT : m_f(f) { }

#ifdef BOOST_NO_CXX11_RVALUE_REFERENCES
      template <class T>
      typename boost::disable_if_c<
        boost::is_same< typename boost::remove_cv< T >::type, output_proxy >::value,
        output_proxy&
      >::type operator=(const T& value) {
        m_f(value);
        return *this;
      }
#else
      template <class T>
      typename boost::disable_if_c<
        boost::is_same< typename boost::remove_cv< typename boost::remove_reference< T >::type >::type, output_proxy >::value,
        output_proxy&
      >::type operator=(T&& value) {
        m_f(static_cast< T&& >(value));
        return *this;
      }
#endif

      BOOST_DEFAULTED_FUNCTION(output_proxy(output_proxy const& that), BOOST_NOEXCEPT : m_f(that.m_f) {})
      BOOST_DELETED_FUNCTION(output_proxy& operator=(output_proxy const&))

    private:
      UnaryFunction& m_f;
    };

  public:
    typedef std::output_iterator_tag iterator_category;
    typedef void                value_type;
    typedef void                difference_type;
    typedef void                pointer;
    typedef void                reference;

    explicit function_output_iterator() {}

    explicit function_output_iterator(const UnaryFunction& f)
      : m_f(f) {}

    output_proxy operator*() { return output_proxy(m_f); }
    self& operator++() { return *this; }
    self& operator++(int) { return *this; }

  private:
    UnaryFunction m_f;
  };

  template <class UnaryFunction>
  inline function_output_iterator<UnaryFunction>
  make_function_output_iterator(const UnaryFunction& f = UnaryFunction()) {
    return function_output_iterator<UnaryFunction>(f);
  }

} // namespace iterators

using iterators::function_output_iterator;
using iterators::make_function_output_iterator;

} // namespace boost

#endif // BOOST_ITERATOR_FUNCTION_OUTPUT_ITERATOR_HPP
