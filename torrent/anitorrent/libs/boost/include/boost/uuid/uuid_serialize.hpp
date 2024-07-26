// Boost uuid_serialize.hpp header file  ----------------------------------------------//

// Copyright 2007 Andy Tompkins.
// Distributed under the Boost Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// https://www.boost.org/LICENSE_1_0.txt)

// Revision History
//  12 Nov 2007 - Initial Revision
//  25 Feb 2008 - moved to namespace boost::uuids::detail

#ifndef BOOST_UUID_SERIALIZE_HPP
#define BOOST_UUID_SERIALIZE_HPP

#include <boost/uuid/uuid.hpp>
#include <boost/type_traits/integral_constant.hpp>

// BOOST_CLASS_IMPLEMENTATION(boost::uuids::uuid, boost::serialization::primitive_type)

namespace boost
{
namespace serialization
{

template<class T> struct implementation_level_impl;
template<> struct implementation_level_impl<const uuids::uuid>: boost::integral_constant<int, 1> {};

} // namespace serialization
} // namespace boost

#endif // BOOST_UUID_SERIALIZE_HPP
