//
// Copyright (c) 2022 Dmitry Arkhipov (grisumbras@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_POINTER_IPP
#define BOOST_JSON_IMPL_POINTER_IPP

#include <boost/json/value.hpp>

namespace boost {
namespace json {

namespace detail {

class pointer_token
{
public:
    class iterator;

    pointer_token(
        string_view sv) noexcept
        : b_( sv.begin() + 1 )
        , e_( sv.end() )
    {
        BOOST_ASSERT( !sv.empty() );
        BOOST_ASSERT( *sv.data() == '/' );
    }

    iterator begin() const noexcept;
    iterator end() const noexcept;

private:
    char const* b_;
    char const* e_;
};

class pointer_token::iterator
{
public:
    using value_type = char;
    using reference = char;
    using pointer = value_type*;
    using difference_type = std::ptrdiff_t;
    using iterator_category = std::forward_iterator_tag;

    explicit iterator(char const* base) noexcept
        : base_(base)
    {
    }

    char operator*() const noexcept
    {
        switch( char c = *base_ )
        {
        case '~':
            c = base_[1];
            if( '0' == c )
                return '~';
            BOOST_ASSERT('1' == c);
            return '/';
        default:
            return c;
        }
    }

    iterator& operator++() noexcept
    {
        if( '~' == *base_ )
            base_ += 2;
        else
            ++base_;
        return *this;
    }

    iterator operator++(int) noexcept
    {
        iterator result = *this;
        ++(*this);
        return result;
    }

    char const* base() const noexcept
    {
        return base_;
    }

private:
    char const* base_;
};

bool operator==(pointer_token::iterator l, pointer_token::iterator r) noexcept
{
    return l.base() == r.base();
}

bool operator!=(pointer_token::iterator l, pointer_token::iterator r) noexcept
{
    return l.base() != r.base();
}

pointer_token::iterator pointer_token::begin() const noexcept
{
    return iterator(b_);
}

pointer_token::iterator pointer_token::end() const noexcept
{
    return iterator(e_);
}

bool operator==(pointer_token token, string_view sv) noexcept
{
    auto t_b = token.begin();
    auto const t_e = token.end();
    auto s_b = sv.begin();
    auto const s_e = sv.end();
    while( s_b != s_e )
    {
        if( t_e == t_b )
            return false;
        if( *t_b != *s_b )
            return false;
        ++t_b;
        ++s_b;
    }
    return t_b == t_e;
}

bool is_invalid_zero(
    char const* b,
    char const* e) noexcept
{
    // in JSON Pointer only zero index can start character '0'
    if( *b != '0' )
        return false;

    // if an index token starts with '0', then it should not have any more
    // characters: either the string should end, or new token should start
    ++b;
    if( b == e )
        return false;

    BOOST_ASSERT( *b != '/' );
    return true;
}

bool is_past_the_end_token(
    char const* b,
    char const* e) noexcept
{
    if( *b != '-' )
        return false;

    ++b;
    BOOST_ASSERT( (b == e) || (*b != '/') );
    return b == e;
}

std::size_t
parse_number_token(
    string_view sv,
    system::error_code& ec) noexcept
{
    BOOST_ASSERT( !sv.empty() );

    char const* b = sv.begin();
    BOOST_ASSERT( *b == '/' );

    ++b;
    char const* const e = sv.end();
    if( ( b == e )
        || is_invalid_zero(b, e) )
    {
        BOOST_JSON_FAIL(ec, error::token_not_number);
        return {};
    }

    if( is_past_the_end_token(b, e) )
    {
        ++b;
        BOOST_JSON_FAIL(ec, error::past_the_end);
        return {};
    }

    std::size_t result = 0;
    for( ; b != e; ++b )
    {
        char const c = *b;
        BOOST_ASSERT( c != '/' );

        unsigned d = c - '0';
        if( d > 9 )
        {
            BOOST_JSON_FAIL(ec, error::token_not_number);
            return {};
        }

        std::size_t new_result = result * 10 + d;
        if( new_result < result )
        {
            BOOST_JSON_FAIL(ec, error::token_overflow);
            return {};
        }

        result = new_result;

    }
    return result;
}

string_view
next_segment(
    string_view& sv,
    system::error_code& ec) noexcept
{
    if( sv.empty() )
        return sv;

    char const* const start = sv.begin();
    char const* b = start;
    if( *b++ != '/' )
    {
        BOOST_JSON_FAIL( ec, error::missing_slash );
        return {};
    }

    char const* e = sv.end();
    for( ; b < e; ++b )
    {
        char const c = *b;
        if( '/' == c )
            break;

        if( '~' == c )
        {
            if( ++b == e )
            {
                BOOST_JSON_FAIL( ec, error::invalid_escape );
                break;
            }

            switch (*b)
            {
            case '0': // fall through
            case '1':
                // valid escape sequence
                continue;
            default: {
                BOOST_JSON_FAIL( ec, error::invalid_escape );
                break;
            }
            }
            break;
        }
    }

    sv.remove_prefix( b - start );
    return string_view( start, b );
}

value*
if_contains_token(object const& obj, pointer_token token)
{
    if( obj.empty() )
        return nullptr;

    auto const it = detail::find_in_object(obj, token).first;
    if( !it )
        return nullptr;

    return &it->value();
}

template<
    class Value,
    class OnObject,
    class OnArray,
    class OnScalar >
Value*
walk_pointer(
    Value& jv,
    string_view sv,
    system::error_code& ec,
    OnObject on_object,
    OnArray on_array,
    OnScalar on_scalar)
{
    ec.clear();

    string_view segment = detail::next_segment( sv, ec );

    Value* result = &jv;
    while( true )
    {
        if( ec.failed() )
            return nullptr;

        if( !result )
        {
            BOOST_JSON_FAIL(ec, error::not_found);
            return nullptr;
        }

        if( segment.empty() )
            break;

        switch( result->kind() )
        {
        case kind::object: {
            auto& obj = result->get_object();

            detail::pointer_token const token( segment );
            segment = detail::next_segment( sv, ec );

            result = on_object( obj, token );
            break;
        }
        case kind::array: {
            auto const index = detail::parse_number_token( segment, ec );
            segment = detail::next_segment( sv, ec );

            auto& arr = result->get_array();
            result = on_array( arr, index, ec );
            break;
        }
        default: {
            if( on_scalar( *result, segment ) )
                break;
            BOOST_JSON_FAIL( ec, error::value_is_scalar );
        }}
    }

    BOOST_ASSERT( result );
    return result;
}

} // namespace detail

value const&
value::at_pointer(string_view ptr) const&
{
    system::error_code ec;
    auto const found = find_pointer(ptr, ec);
    if( !found )
        detail::throw_system_error( ec );
    return *found;
}

value const*
value::find_pointer( string_view sv, system::error_code& ec ) const noexcept
{
    return detail::walk_pointer(
        *this,
        sv,
        ec,
        []( object const& obj, detail::pointer_token token )
        {
            return detail::if_contains_token(obj, token);
        },
        []( array const& arr, std::size_t index, system::error_code& ec )
            -> value const*
        {
            if( ec )
                return nullptr;

            return arr.if_contains(index);
        },
        []( value const&, string_view)
        {
            return std::false_type();
        });
}

value*
value::find_pointer(string_view ptr, system::error_code& ec) noexcept
{
    value const& self = *this;
    return const_cast<value*>(self.find_pointer(ptr, ec));
}

value const*
value::find_pointer(string_view ptr, std::error_code& ec) const noexcept
{
    system::error_code jec;
    value const* result = find_pointer(ptr, jec);
    ec = jec;
    return result;
}

value*
value::find_pointer(string_view ptr, std::error_code& ec) noexcept
{
    value const& self = *this;
    return const_cast<value*>(self.find_pointer(ptr, ec));
}

value*
value::set_at_pointer(
    string_view sv,
    value_ref ref,
    system::error_code& ec,
    set_pointer_options const& opts )
{
    value* result = detail::walk_pointer(
        *this,
        sv,
        ec,
        []( object& obj, detail::pointer_token token)
        {
            if( !obj.empty() )
            {
                key_value_pair* kv = detail::find_in_object( obj, token ).first;
                if( kv )
                    return &kv->value();
            }

            string key( token.begin(), token.end(), obj.storage() );
            return &obj.emplace( std::move(key), nullptr ).first->value();
        },
        [ &opts ]( array& arr, std::size_t index, system::error_code& ec ) -> value*
        {
            if( ec == error::past_the_end )
                index = arr.size();
            else if( ec.failed() )
                return nullptr;

            if( index >= arr.size() )
            {
                std::size_t const n = index - arr.size();
                if( n >= opts.max_created_elements )
                    return nullptr;

                arr.resize( arr.size() + n + 1 );
            }

            ec.clear();
            return arr.data() + index;
        },
        [ &opts ]( value& jv, string_view segment )
        {
            if( jv.is_null() || opts.replace_any_scalar )
            {
                if( opts.create_arrays )
                {
                    system::error_code ec;
                    detail::parse_number_token( segment, ec );
                    if( !ec.failed() || ec == error::past_the_end )
                    {
                        jv = array( jv.storage() );
                        return true;
                    }
                }

                if( opts.create_objects )
                {
                    jv = object( jv.storage() );
                    return true;
                }
            }

            return false;
        });

    if( result )
        *result = ref.make_value( storage() );
    return result;
}

value*
value::set_at_pointer(
    string_view sv,
    value_ref ref,
    std::error_code& ec,
    set_pointer_options const& opts )
{
    system::error_code jec;
    value* result = set_at_pointer( sv, ref, jec, opts );
    ec = jec;
    return result;
}

value&
value::set_at_pointer(
    string_view sv, value_ref ref, set_pointer_options const& opts )
{
    system::error_code ec;
    value* result = set_at_pointer( sv, ref, ec, opts );
    if( !result )
        detail::throw_system_error( ec );
    return *result;
}

} // namespace json
} // namespace boost

#endif // BOOST_JSON_IMPL_POINTER_IPP
