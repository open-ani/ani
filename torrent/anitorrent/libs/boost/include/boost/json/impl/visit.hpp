//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_VISIT_HPP
#define BOOST_JSON_IMPL_VISIT_HPP

namespace boost {
namespace json {


template<class Visitor>
auto
visit(
    Visitor&& v,
    value& jv) -> decltype(
        static_cast<Visitor&&>(v)( std::declval<std::nullptr_t&>() ) )
{
    switch(jv.kind())
    {
    default: // unreachable()?
    case kind::string:  return static_cast<Visitor&&>(v)( jv.get_string() );
    case kind::array:   return static_cast<Visitor&&>(v)( jv.get_array() );
    case kind::object:  return static_cast<Visitor&&>(v)( jv.get_object() );
    case kind::bool_:   return static_cast<Visitor&&>(v)( jv.get_bool() );
    case kind::int64:   return static_cast<Visitor&&>(v)( jv.get_int64() );
    case kind::uint64:  return static_cast<Visitor&&>(v)( jv.get_uint64() );
    case kind::double_: return static_cast<Visitor&&>(v)( jv.get_double() );
    case kind::null: {
        auto np = nullptr;
        return static_cast<Visitor&&>(v)(np) ;
    }
    }
}

template<class Visitor>
auto
visit(
    Visitor&& v,
    value const& jv) -> decltype(
        static_cast<Visitor&&>(v)( std::declval<std::nullptr_t const&>() ) )
{
    switch(jv.kind())
    {
    default: // unreachable()?
    case kind::string:  return static_cast<Visitor&&>(v)( jv.get_string() );
    case kind::array:   return static_cast<Visitor&&>(v)( jv.get_array() );
    case kind::object:  return static_cast<Visitor&&>(v)( jv.get_object() );
    case kind::bool_:   return static_cast<Visitor&&>(v)( jv.get_bool() );
    case kind::int64:   return static_cast<Visitor&&>(v)( jv.get_int64() );
    case kind::uint64:  return static_cast<Visitor&&>(v)( jv.get_uint64() );
    case kind::double_: return static_cast<Visitor&&>(v)( jv.get_double() );
    case kind::null: {
        auto const np = nullptr;
        return static_cast<Visitor&&>(v)(np) ;
    }
    }
}


template<class Visitor>
auto
visit(
    Visitor&& v,
    value&& jv) -> decltype(
        static_cast<Visitor&&>(v)( std::declval<std::nullptr_t&&>() ) )
{
    switch(jv.kind())
    {
    default: // unreachable()?
    case kind::string:  return static_cast<Visitor&&>(v)( std::move( jv.get_string() ) );
    case kind::array:   return static_cast<Visitor&&>(v)( std::move( jv.get_array() ) );
    case kind::object:  return static_cast<Visitor&&>(v)( std::move( jv.get_object() ) );
    case kind::bool_:   return static_cast<Visitor&&>(v)( std::move( jv.get_bool() ) );
    case kind::int64:   return static_cast<Visitor&&>(v)( std::move( jv.get_int64() ) );
    case kind::uint64:  return static_cast<Visitor&&>(v)( std::move( jv.get_uint64() ) );
    case kind::double_: return static_cast<Visitor&&>(v)( std::move( jv.get_double() ) );
    case kind::null:    return static_cast<Visitor&&>(v)( std::nullptr_t() ) ;
    }
}

} // namespace json
} // namespace boost

#endif
