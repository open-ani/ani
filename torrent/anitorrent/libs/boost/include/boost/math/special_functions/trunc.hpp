//  Copyright John Maddock 2007.
//  Copyright Matt Borland 2023.
//  Use, modification and distribution are subject to the
//  Boost Software License, Version 1.0. (See accompanying file
//  LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

#ifndef BOOST_MATH_TRUNC_HPP
#define BOOST_MATH_TRUNC_HPP

#ifdef _MSC_VER
#pragma once
#endif

#include <type_traits>
#include <boost/math/special_functions/math_fwd.hpp>
#include <boost/math/tools/config.hpp>
#include <boost/math/ccmath/detail/config.hpp>
#include <boost/math/policies/error_handling.hpp>
#include <boost/math/special_functions/fpclassify.hpp>
#include <boost/math/tools/is_constant_evaluated.hpp>

#if !defined(BOOST_MATH_NO_CCMATH) && !defined(BOOST_MATH_NO_CONSTEXPR_DETECTION)
#include <boost/math/ccmath/ldexp.hpp>
#    define BOOST_MATH_HAS_CONSTEXPR_LDEXP
#endif

namespace boost{ namespace math{ namespace detail{

template <class T, class Policy>
inline tools::promote_args_t<T> trunc(const T& v, const Policy& pol, const std::false_type&)
{
   BOOST_MATH_STD_USING
   using result_type = tools::promote_args_t<T>;
   if(!(boost::math::isfinite)(v))
   {
      return policies::raise_rounding_error("boost::math::trunc<%1%>(%1%)", nullptr, static_cast<result_type>(v), static_cast<result_type>(v), pol);
   }
   return (v >= 0) ? static_cast<result_type>(floor(v)) : static_cast<result_type>(ceil(v));
}

template <class T, class Policy>
inline tools::promote_args_t<T> trunc(const T& v, const Policy&, const std::true_type&)
{
   return v;
}

}

template <class T, class Policy>
inline tools::promote_args_t<T> trunc(const T& v, const Policy& pol)
{
   return detail::trunc(v, pol, std::integral_constant<bool, detail::is_integer_for_rounding<T>::value>());
}
template <class T>
inline tools::promote_args_t<T> trunc(const T& v)
{
   return trunc(v, policies::policy<>());
}
//
// The following functions will not compile unless T has an
// implicit conversion to the integer types.  For user-defined
// number types this will likely not be the case.  In that case
// these functions should either be specialized for the UDT in
// question, or else overloads should be placed in the same
// namespace as the UDT: these will then be found via argument
// dependent lookup.  See our concept archetypes for examples.
//
// Non-standard numeric limits syntax "(std::numeric_limits<int>::max)()"
// is to avoid macro substiution from MSVC
// https://stackoverflow.com/questions/27442885/syntax-error-with-stdnumeric-limitsmax
//
template <class T, class Policy>
inline int itrunc(const T& v, const Policy& pol)
{
   BOOST_MATH_STD_USING
   using result_type = tools::promote_args_t<T>;
   result_type r = boost::math::trunc(v, pol);

   #ifdef BOOST_MATH_HAS_CONSTEXPR_LDEXP
   if constexpr (std::is_arithmetic_v<result_type>
                 #ifdef BOOST_MATH_FLOAT128_TYPE
                 && !std::is_same_v<BOOST_MATH_FLOAT128_TYPE, result_type>
                 #endif
                )
   {
      constexpr result_type max_val = boost::math::ccmath::ldexp(static_cast<result_type>(1), std::numeric_limits<int>::digits);
      
      if (r >= max_val || r < -max_val)
      {
         return static_cast<int>(boost::math::policies::raise_rounding_error("boost::math::itrunc<%1%>(%1%)", nullptr, v, static_cast<int>(0), pol));
      }
   }
   else
   {
      static const result_type max_val = ldexp(static_cast<result_type>(1), std::numeric_limits<int>::digits);
   
      if (r >= max_val || r < -max_val)
      {
         return static_cast<int>(boost::math::policies::raise_rounding_error("boost::math::itrunc<%1%>(%1%)", nullptr, v, static_cast<int>(0), pol));
      }
   }
   #else
   static const result_type max_val = ldexp(static_cast<result_type>(1), std::numeric_limits<int>::digits);

   if (r >= max_val || r < -max_val)
   {
      return static_cast<int>(boost::math::policies::raise_rounding_error("boost::math::itrunc<%1%>(%1%)", nullptr, v, static_cast<int>(0), pol));
   }
   #endif

   return static_cast<int>(r);
}
template <class T>
inline int itrunc(const T& v)
{
   return itrunc(v, policies::policy<>());
}

template <class T, class Policy>
inline long ltrunc(const T& v, const Policy& pol)
{
   BOOST_MATH_STD_USING
   using result_type = tools::promote_args_t<T>;
   result_type r = boost::math::trunc(v, pol);

   #ifdef BOOST_MATH_HAS_CONSTEXPR_LDEXP
   if constexpr (std::is_arithmetic_v<result_type>
                 #ifdef BOOST_MATH_FLOAT128_TYPE
                 && !std::is_same_v<BOOST_MATH_FLOAT128_TYPE, result_type>
                 #endif
                )
   {
      constexpr result_type max_val = boost::math::ccmath::ldexp(static_cast<result_type>(1), std::numeric_limits<long>::digits);
      
      if (r >= max_val || r < -max_val)
      {
         return static_cast<long>(boost::math::policies::raise_rounding_error("boost::math::ltrunc<%1%>(%1%)", nullptr, v, static_cast<long>(0), pol));
      }
   }
   else
   {
      static const result_type max_val = ldexp(static_cast<result_type>(1), std::numeric_limits<long>::digits);
   
      if (r >= max_val || r < -max_val)
      {
         return static_cast<long>(boost::math::policies::raise_rounding_error("boost::math::ltrunc<%1%>(%1%)", nullptr, v, static_cast<long>(0), pol));
      }
   }
   #else
   static const result_type max_val = ldexp(static_cast<result_type>(1), std::numeric_limits<long>::digits);

   if (r >= max_val || r < -max_val)
   {
      return static_cast<long>(boost::math::policies::raise_rounding_error("boost::math::ltrunc<%1%>(%1%)", nullptr, v, static_cast<long>(0), pol));
   }
   #endif

   return static_cast<long>(r);
}
template <class T>
inline long ltrunc(const T& v)
{
   return ltrunc(v, policies::policy<>());
}

template <class T, class Policy>
inline long long lltrunc(const T& v, const Policy& pol)
{
   BOOST_MATH_STD_USING
   using result_type = tools::promote_args_t<T>;
   result_type r = boost::math::trunc(v, pol);

   #ifdef BOOST_MATH_HAS_CONSTEXPR_LDEXP
   if constexpr (std::is_arithmetic_v<result_type>
                 #ifdef BOOST_MATH_FLOAT128_TYPE
                 && !std::is_same_v<BOOST_MATH_FLOAT128_TYPE, result_type>
                 #endif
                )
   {
      constexpr result_type max_val = boost::math::ccmath::ldexp(static_cast<result_type>(1), std::numeric_limits<long long>::digits);
      
      if (r >= max_val || r < -max_val)
      {
         return static_cast<long long>(boost::math::policies::raise_rounding_error("boost::math::lltrunc<%1%>(%1%)", nullptr, v, static_cast<long long>(0), pol));
      }
   }
   else
   {
      static const result_type max_val = ldexp(static_cast<result_type>(1), std::numeric_limits<long long>::digits);
   
      if (r >= max_val || r < -max_val)
      {
         return static_cast<long long>(boost::math::policies::raise_rounding_error("boost::math::lltrunc<%1%>(%1%)", nullptr, v, static_cast<long long>(0), pol));
      }
   }
   #else
   static const result_type max_val = ldexp(static_cast<result_type>(1), std::numeric_limits<long long>::digits);

   if (r >= max_val || r < -max_val)
   {
      return static_cast<long long>(boost::math::policies::raise_rounding_error("boost::math::lltrunc<%1%>(%1%)", nullptr, v, static_cast<long long>(0), pol));
   }
   #endif

   return static_cast<long long>(r);
}
template <class T>
inline long long lltrunc(const T& v)
{
   return lltrunc(v, policies::policy<>());
}

template <class T, class Policy>
inline typename std::enable_if<std::is_constructible<int, T>::value, int>::type
   iconvert(const T& v, const Policy&)
{
   return static_cast<int>(v);
}

template <class T, class Policy>
inline typename std::enable_if<!std::is_constructible<int, T>::value, int>::type
   iconvert(const T& v, const Policy& pol)
{
   using boost::math::itrunc;
   return itrunc(v, pol);
}

template <class T, class Policy>
inline typename std::enable_if<std::is_constructible<long, T>::value, long>::type
   lconvert(const T& v, const Policy&)
{
   return static_cast<long>(v);
}

template <class T, class Policy>
inline typename std::enable_if<!std::is_constructible<long, T>::value, long>::type
   lconvert(const T& v, const Policy& pol)
{
   using boost::math::ltrunc;
   return ltrunc(v, pol);
}

template <class T, class Policy>
inline typename std::enable_if<std::is_constructible<long long, T>::value, long long>::type
   llconvertert(const T& v, const Policy&)
{
   return static_cast<long long>(v);
}

template <class T, class Policy>
inline typename std::enable_if<!std::is_constructible<long long, T>::value, long long>::type
   llconvertert(const T& v, const Policy& pol)
{
   using boost::math::lltrunc;
   return lltrunc(v, pol);
}

}} // namespaces

#endif // BOOST_MATH_TRUNC_HPP
