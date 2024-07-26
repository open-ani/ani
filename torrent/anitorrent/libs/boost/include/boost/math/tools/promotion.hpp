// boost\math\tools\promotion.hpp

// Copyright John Maddock 2006.
// Copyright Paul A. Bristow 2006.
// Copyright Matt Borland 2023.

// Use, modification and distribution are subject to the
// Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

// Promote arguments functions to allow math functions to have arguments
// provided as integer OR real (floating-point, built-in or UDT)
// (called ArithmeticType in functions that use promotion)
// that help to reduce the risk of creating multiple instantiations.
// Allows creation of an inline wrapper that forwards to a foo(RT, RT) function,
// so you never get to instantiate any mixed foo(RT, IT) functions.

#ifndef BOOST_MATH_PROMOTION_HPP
#define BOOST_MATH_PROMOTION_HPP

#ifdef _MSC_VER
#pragma once
#endif

#include <boost/math/tools/config.hpp>
#include <type_traits>

#if defined __has_include
#  if __cplusplus > 202002L || (defined(_MSVC_LANG) && _MSVC_LANG > 202002L)
#    if __has_include (<stdfloat>)
#    include <stdfloat>
#    endif
#  endif
#endif

namespace boost
{
  namespace math
  {
    namespace tools
    {
      // If either T1 or T2 is an integer type,
      // pretend it was a double (for the purposes of further analysis).
      // Then pick the wider of the two floating-point types
      // as the actual signature to forward to.
      // For example:
      // foo(int, short) -> double foo(double, double);
      // foo(int, float) -> double foo(double, double);
      // Note: NOT float foo(float, float)
      // foo(int, double) -> foo(double, double);
      // foo(double, float) -> double foo(double, double);
      // foo(double, float) -> double foo(double, double);
      // foo(any-int-or-float-type, long double) -> foo(long double, long double);
      // but ONLY float foo(float, float) is unchanged.
      // So the only way to get an entirely float version is to call foo(1.F, 2.F),
      // But since most (all?) the math functions convert to double internally,
      // probably there would not be the hoped-for gain by using float here.

      // This follows the C-compatible conversion rules of pow, etc
      // where pow(int, float) is converted to pow(double, double).

      template <class T>
      struct promote_arg
      { // If T is integral type, then promote to double.
        using type = typename std::conditional<std::is_integral<T>::value, double, T>::type;
      };
      // These full specialisations reduce std::conditional usage and speed up
      // compilation:
      template <> struct promote_arg<float> { using type = float; };
      template <> struct promote_arg<double>{ using type = double; };
      template <> struct promote_arg<long double> { using type = long double; };
      template <> struct promote_arg<int> {  using type = double; };

      #ifdef __STDCPP_FLOAT16_T__
      template <> struct promote_arg<std::float16_t> { using type = std::float16_t; };
      #endif
      #ifdef __STDCPP_FLOAT32_T__
      template <> struct promote_arg<std::float32_t> { using type = std::float32_t; };
      #endif
      #ifdef __STDCPP_FLOAT64_T__
      template <> struct promote_arg<std::float64_t> { using type = std::float64_t; };
      #endif
      #ifdef __STDCPP_FLOAT128_T__
      template <> struct promote_arg<std::float128_t> { using type = std::float128_t; };
      #endif

      template <typename T>
      using promote_arg_t = typename promote_arg<T>::type;

      template <class T1, class T2>
      struct promote_args_2
      { // Promote, if necessary, & pick the wider of the two floating-point types.
        // for both parameter types, if integral promote to double.
        using T1P = typename promote_arg<T1>::type; // T1 perhaps promoted.
        using T2P = typename promote_arg<T2>::type; // T2 perhaps promoted.
        using intermediate_type = typename std::conditional<
          std::is_floating_point<T1P>::value && std::is_floating_point<T2P>::value, // both T1P and T2P are floating-point?
#ifdef __STDCPP_FLOAT128_T__
           typename std::conditional<std::is_same<std::float128_t, T1P>::value || std::is_same<std::float128_t, T2P>::value, // either long double?
            std::float128_t,
#endif 
#ifdef BOOST_MATH_USE_FLOAT128
           typename std::conditional<std::is_same<__float128, T1P>::value || std::is_same<__float128, T2P>::value, // either long double?
            __float128,
#endif 
             typename std::conditional<std::is_same<long double, T1P>::value || std::is_same<long double, T2P>::value, // either long double?
               long double, // then result type is long double.
#ifdef __STDCPP_FLOAT64_T__
             typename std::conditional<std::is_same<std::float64_t, T1P>::value || std::is_same<std::float64_t, T2P>::value, // either float64?
               std::float64_t, // then result type is float64_t.
#endif
               typename std::conditional<std::is_same<double, T1P>::value || std::is_same<double, T2P>::value, // either double?
                  double, // result type is double.
#ifdef __STDCPP_FLOAT32_T__
             typename std::conditional<std::is_same<std::float32_t, T1P>::value || std::is_same<std::float32_t, T2P>::value, // either float32?
               std::float32_t, // then result type is float32_t.
#endif
                  float // else result type is float.
             >::type
#ifdef BOOST_MATH_USE_FLOAT128
             >::type
#endif
#ifdef __STDCPP_FLOAT128_T__
             >::type
#endif
#ifdef __STDCPP_FLOAT64_T__
             >::type
#endif
#ifdef __STDCPP_FLOAT32_T__
             >::type
#endif
             >::type,
          // else one or the other is a user-defined type:
          typename std::conditional<!std::is_floating_point<T2P>::value && std::is_convertible<T1P, T2P>::value, T2P, T1P>::type>::type;

#ifdef __STDCPP_FLOAT64_T__
          // If long doubles are doubles then we should prefer to use std::float64_t when available
          using type = std::conditional_t<(sizeof(double) == sizeof(long double) && std::is_same<intermediate_type, long double>::value), std::float64_t, intermediate_type>;
#else
          using type = intermediate_type;
#endif
      }; // promote_arg2
      // These full specialisations reduce std::conditional usage and speed up
      // compilation:
      template <> struct promote_args_2<float, float> { using type = float; };
      template <> struct promote_args_2<double, double>{ using type = double; };
      template <> struct promote_args_2<long double, long double> { using type = long double; };
      template <> struct promote_args_2<int, int> {  using type = double; };
      template <> struct promote_args_2<int, float> {  using type = double; };
      template <> struct promote_args_2<float, int> {  using type = double; };
      template <> struct promote_args_2<int, double> {  using type = double; };
      template <> struct promote_args_2<double, int> {  using type = double; };
      template <> struct promote_args_2<int, long double> {  using type = long double; };
      template <> struct promote_args_2<long double, int> {  using type = long double; };
      template <> struct promote_args_2<float, double> {  using type = double; };
      template <> struct promote_args_2<double, float> {  using type = double; };
      template <> struct promote_args_2<float, long double> {  using type = long double; };
      template <> struct promote_args_2<long double, float> {  using type = long double; };
      template <> struct promote_args_2<double, long double> {  using type = long double; };
      template <> struct promote_args_2<long double, double> {  using type = long double; };

      #ifdef __STDCPP_FLOAT128_T__
      template <> struct promote_args_2<int, std::float128_t> { using type = std::float128_t; };
      template <> struct promote_args_2<std::float128_t, int> { using type = std::float128_t; };
      template <> struct promote_args_2<std::float128_t, float> { using type = std::float128_t; };
      template <> struct promote_args_2<float, std::float128_t> { using type = std::float128_t; };
      template <> struct promote_args_2<std::float128_t, double> { using type = std::float128_t; };
      template <> struct promote_args_2<double, std::float128_t> { using type = std::float128_t; };
      template <> struct promote_args_2<std::float128_t, long double> { using type = std::float128_t; };
      template <> struct promote_args_2<long double, std::float128_t> { using type = std::float128_t; };

      #ifdef __STDCPP_FLOAT16_T__
      template <> struct promote_args_2<std::float128_t, std::float16_t> { using type = std::float128_t; };
      template <> struct promote_args_2<std::float16_t, std::float128_t> { using type = std::float128_t; };
      #endif

      #ifdef __STDCPP_FLOAT32_T__
      template <> struct promote_args_2<std::float128_t, std::float32_t> { using type = std::float128_t; };
      template <> struct promote_args_2<std::float32_t, std::float128_t> { using type = std::float128_t; };
      #endif

      #ifdef __STDCPP_FLOAT64_T__
      template <> struct promote_args_2<std::float128_t, std::float64_t> { using type = std::float128_t; };
      template <> struct promote_args_2<std::float64_t, std::float128_t> { using type = std::float128_t; };
      #endif

      template <> struct promote_args_2<std::float128_t, std::float128_t> { using type = std::float128_t; };
      #endif

      #ifdef __STDCPP_FLOAT64_T__
      template <> struct promote_args_2<int, std::float64_t> { using type = std::float64_t; };
      template <> struct promote_args_2<std::float64_t, int> { using type = std::float64_t; };
      template <> struct promote_args_2<std::float64_t, float> { using type = std::float64_t; };
      template <> struct promote_args_2<float, std::float64_t> { using type = std::float64_t; };
      template <> struct promote_args_2<std::float64_t, double> { using type = std::float64_t; };
      template <> struct promote_args_2<double, std::float64_t> { using type = std::float64_t; };
      template <> struct promote_args_2<std::float64_t, long double> { using type = long double; };
      template <> struct promote_args_2<long double, std::float64_t> { using type = long double; };

      #ifdef __STDCPP_FLOAT16_T__
      template <> struct promote_args_2<std::float64_t, std::float16_t> { using type = std::float64_t; };
      template <> struct promote_args_2<std::float16_t, std::float64_t> { using type = std::float64_t; };
      #endif

      #ifdef __STDCPP_FLOAT32_T__
      template <> struct promote_args_2<std::float64_t, std::float32_t> { using type = std::float64_t; };
      template <> struct promote_args_2<std::float32_t, std::float64_t> { using type = std::float64_t; };
      #endif

      template <> struct promote_args_2<std::float64_t, std::float64_t> { using type = std::float64_t; };
      #endif

      #ifdef __STDCPP_FLOAT32_T__
      template <> struct promote_args_2<int, std::float32_t> { using type = std::float32_t; };
      template <> struct promote_args_2<std::float32_t, int> { using type = std::float32_t; };
      template <> struct promote_args_2<std::float32_t, float> { using type = std::float32_t; };
      template <> struct promote_args_2<float, std::float32_t> { using type = std::float32_t; };
      template <> struct promote_args_2<std::float32_t, double> { using type = double; };
      template <> struct promote_args_2<double, std::float32_t> { using type = double; };
      template <> struct promote_args_2<std::float32_t, long double> { using type = long double; };
      template <> struct promote_args_2<long double, std::float32_t> { using type = long double; };

      #ifdef __STDCPP_FLOAT16_T__
      template <> struct promote_args_2<std::float32_t, std::float16_t> { using type = std::float32_t; };
      template <> struct promote_args_2<std::float16_t, std::float32_t> { using type = std::float32_t; };
      #endif

      template <> struct promote_args_2<std::float32_t, std::float32_t> { using type = std::float32_t; };
      #endif

      #ifdef __STDCPP_FLOAT16_T__
      template <> struct promote_args_2<int, std::float16_t> { using type = std::float16_t; };
      template <> struct promote_args_2<std::float16_t, int> { using type = std::float16_t; };
      template <> struct promote_args_2<std::float16_t, float> { using type = float; };
      template <> struct promote_args_2<float, std::float16_t> { using type = float; };
      template <> struct promote_args_2<std::float16_t, double> { using type = double; };
      template <> struct promote_args_2<double, std::float16_t> { using type = double; };
      template <> struct promote_args_2<std::float16_t, long double> { using type = long double; };
      template <> struct promote_args_2<long double, std::float16_t> { using type = long double; };

      template <> struct promote_args_2<std::float16_t, std::float16_t> { using type = std::float16_t; };
      #endif

      template <typename T, typename U>
      using promote_args_2_t = typename promote_args_2<T, U>::type;

      template <class T1, class T2=float, class T3=float, class T4=float, class T5=float, class T6=float>
      struct promote_args
      {
         using type = typename promote_args_2<
            typename std::remove_cv<T1>::type,
            typename promote_args_2<
               typename std::remove_cv<T2>::type,
               typename promote_args_2<
                  typename std::remove_cv<T3>::type,
                  typename promote_args_2<
                     typename std::remove_cv<T4>::type,
                     typename promote_args_2<
                        typename std::remove_cv<T5>::type, typename std::remove_cv<T6>::type
                     >::type
                  >::type
               >::type
            >::type
         >::type;

#if defined(BOOST_MATH_NO_LONG_DOUBLE_MATH_FUNCTIONS)
         //
         // Guard against use of long double if it's not supported:
         //
         static_assert((0 == std::is_same<type, long double>::value), "Sorry, but this platform does not have sufficient long double support for the special functions to be reliably implemented.");
#endif
      };

      template <class T1, class T2=float, class T3=float, class T4=float, class T5=float, class T6=float>
      using promote_args_t = typename promote_args<T1, T2, T3, T4, T5, T6>::type;

      //
      // This struct is the same as above, but has no static assert on long double usage,
      // it should be used only on functions that can be implemented for long double
      // even when std lib support is missing or broken for that type.
      //
      template <class T1, class T2=float, class T3=float, class T4=float, class T5=float, class T6=float>
      struct promote_args_permissive
      {
         using type = typename promote_args_2<
            typename std::remove_cv<T1>::type,
            typename promote_args_2<
               typename std::remove_cv<T2>::type,
               typename promote_args_2<
                  typename std::remove_cv<T3>::type,
                  typename promote_args_2<
                     typename std::remove_cv<T4>::type,
                     typename promote_args_2<
                        typename std::remove_cv<T5>::type, typename std::remove_cv<T6>::type
                     >::type
                  >::type
               >::type
            >::type
         >::type;
      };

      template <class T1, class T2=float, class T3=float, class T4=float, class T5=float, class T6=float>
      using promote_args_permissive_t = typename promote_args_permissive<T1, T2, T3, T4, T5, T6>::type;

    } // namespace tools
  } // namespace math
} // namespace boost

#endif // BOOST_MATH_PROMOTION_HPP

