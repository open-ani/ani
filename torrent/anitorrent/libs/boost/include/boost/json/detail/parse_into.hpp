//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2020 Krystian Stasiowski (sdkrystian@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_DETAIL_PARSE_INTO_HPP
#define BOOST_JSON_DETAIL_PARSE_INTO_HPP

#include <boost/json/detail/config.hpp>

#include <boost/json/error.hpp>
#include <boost/json/conversion.hpp>
#include <boost/describe/enum_from_string.hpp>

#include <vector>

/*
 * This file contains the majority of parse_into functionality, specifically
 * the implementation of dedicated handlers for different generic categories of
 * types.
 *
 * At the core of parse_into is the specialisation basic_parser<
 * detail::into_handler<T> >. detail::into_handler<T> is a handler for
 * basic_parser. It directly handles events on_comment_part and on_comment (by
 * ignoring them), on_document_begin (by enabling the nested dedicated
 * handler), and on_document_end (by disabling the nested handler).
 *
 * Every other event is handled by the nested handler, which has the type
 * get_handler< T, into_handler<T> >. The second parameter is the parent
 * handler (in this case, it's the top handler, into_handler<T>). The type is
 * actually an alias to class template converting_handler, which has a separate
 * specialisation for every conversion category from the list of generic
 * conversion categories (e.g. sequence_conversion_tag, tuple_conversion_tag,
 * etc.) Instantiations of the template store a pointer to the parent handler
 * and a pointer to the value T.
 *
 * The nested handler handles specific parser events by setting error_code to
 * an appropriate value, if it receives an event it isn't supposed to handle
 * (e.g. a number handler getting an on_string event), and also updates the
 * value when appropriate. Note that they never need to handle on_comment_part,
 * on_comment, on_document_begin, and on_document_end events, as those are
 * always handled by the top handler into_handler<T>.
 *
 * When the nested handler receives an event that completes the current value,
 * it is supposed to call its parent's signal_value member function. This is
 * necessary for correct handling of composite types (e.g. sequences).
 *
 * Finally, nested handlers should always call parent's signal_end member
 * function if they don't handle on_array_end themselves. This is necessary
 * to correctly handle nested composites (e.g. sequences inside sequences).
 * signal_end can return false and set error state when the containing parser
 * requires more elements.
 *
 * converting_handler instantiations for composite categories of types have
 * their own nested handlers, to which they themselves delegate events. For
 * complex types you will get a tree of handlers with into_handler<T> as the
 * root and handlers for scalars as leaves.
 *
 * To reiterate, only into_handler has to handle on_comment_part, on_comment,
 * on_document_begin, and on_document_end; only handlers for composites and
 * into_handler has to provide signal_value and signal_end; all handlers
 * except for into_handler have to call their parent's signal_end from
 * their on_array_begin, if they don't handle it themselves; once a handler
 * receives an event that finishes its current value, it should call its
 * parent's signal_value.
 */

namespace boost {
namespace json {
namespace detail {

template< class Impl, class T, class Parent >
class converting_handler;

// get_handler
template< class V, class P >
using get_handler = converting_handler< generic_conversion_category<V>, V, P >;

template<error E> class handler_error_base
{
public:

    handler_error_base() = default;

    handler_error_base( handler_error_base const& ) = delete;
    handler_error_base& operator=( handler_error_base const& ) = delete;

public:

    bool on_object_begin( system::error_code& ec ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_array_begin( system::error_code& ec ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_array_end( system::error_code& ec ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_string_part( system::error_code& ec, string_view ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_string( system::error_code& ec, string_view ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_number_part( system::error_code& ec ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_int64( system::error_code& ec, std::int64_t ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_uint64( system::error_code& ec, std::uint64_t ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_double( system::error_code& ec, double ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_bool( system::error_code& ec, bool ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_null( system::error_code& ec ) { BOOST_JSON_FAIL( ec, E ); return false; }

    // LCOV_EXCL_START
    // parses that can't handle this would fail at on_object_begin
    bool on_object_end( system::error_code& ) { BOOST_ASSERT( false ); return false; }
    bool on_key_part( system::error_code& ec, string_view ) { BOOST_JSON_FAIL( ec, E ); return false; }
    bool on_key( system::error_code& ec, string_view ) { BOOST_JSON_FAIL( ec, E ); return false; }
    // LCOV_EXCL_START
};

template< class P, error E >
class scalar_handler
    : public handler_error_base<E>
{
protected:
    P* parent_;

public:
    scalar_handler(scalar_handler const&) = delete;
    scalar_handler& operator=(scalar_handler const&) = delete;

    scalar_handler(P* p): parent_( p )
    {}

    bool on_array_end( system::error_code& ec )
    {
        return parent_->signal_end(ec);
    }
};

template< class D, class V, class P, error E >
class composite_handler
{
protected:
    using inner_handler_type = get_handler<V, D>;

    P* parent_;
#if defined(__GNUC__) && __GNUC__ < 5 && !defined(__clang__)
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif
    V next_value_ = {};
    inner_handler_type inner_;
    bool inner_active_ = false;

public:
    composite_handler( composite_handler const& ) = delete;
    composite_handler& operator=( composite_handler const& ) = delete;

    composite_handler( P* p )
        : parent_(p), inner_( &next_value_, static_cast<D*>(this) )
    {}
#if defined(__GNUC__) && __GNUC__ < 5 && !defined(__clang__)
# pragma GCC diagnostic pop
#endif

    bool signal_end(system::error_code&)
    {
        inner_active_ = false;
        parent_->signal_value();
        return true;
    }

#define BOOST_JSON_INVOKE_INNER(f) \
    if( !inner_active_ ) { \
        BOOST_JSON_FAIL(ec, E); \
        return false; \
    } \
    else \
        return inner_.f

    bool on_object_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_object_begin(ec) );
    }

    bool on_object_end( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_object_end(ec) );
    }

    bool on_array_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_array_begin(ec) );
    }

    bool on_array_end( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_array_end(ec) );
    }

    bool on_key_part( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_key_part(ec, sv) );
    }

    bool on_key( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_key(ec, sv) );
    }

    bool on_string_part( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_string_part(ec, sv) );
    }

    bool on_string( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_string(ec, sv) );
    }

    bool on_number_part( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_number_part(ec) );
    }

    bool on_int64( system::error_code& ec, std::int64_t v )
    {
        BOOST_JSON_INVOKE_INNER( on_int64(ec, v) );
    }

    bool on_uint64( system::error_code& ec, std::uint64_t v )
    {
        BOOST_JSON_INVOKE_INNER( on_uint64(ec, v) );
    }

    bool on_double( system::error_code& ec, double v )
    {
        BOOST_JSON_INVOKE_INNER( on_double(ec, v) );
    }

    bool on_bool( system::error_code& ec, bool v )
    {
        BOOST_JSON_INVOKE_INNER( on_bool(ec, v) );
    }

    bool on_null( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_null(ec) );
    }

#undef BOOST_JSON_INVOKE_INNER
};

// integral handler
template<class V,
typename std::enable_if<std::is_signed<V>::value, int>::type = 0>
bool integral_in_range( std::int64_t v )
{
    return v >= (std::numeric_limits<V>::min)() && v <= (std::numeric_limits<V>::max)();
}

template<class V,
typename std::enable_if<!std::is_signed<V>::value, int>::type = 0>
bool integral_in_range( std::int64_t v )
{
    return v >= 0 && static_cast<std::uint64_t>( v ) <= (std::numeric_limits<V>::max)();
}

template<class V>
bool integral_in_range( std::uint64_t v )
{
    return v <= static_cast<typename std::make_unsigned<V>::type>( (std::numeric_limits<V>::max)() );
}

template< class V, class P >
class converting_handler<integral_conversion_tag, V, P>
    : public scalar_handler<P, error::not_integer>
{
private:
    V* value_;

public:
    converting_handler( V* v, P* p )
        : converting_handler::scalar_handler(p)
        , value_(v)
    {}

    bool on_number_part( system::error_code& )
    {
        return true;
    }

    bool on_int64( system::error_code& ec, std::int64_t v )
    {
        if( !integral_in_range<V>( v ) )
        {
            BOOST_JSON_FAIL( ec, error::not_exact );
            return false;
        }

        *value_ = static_cast<V>( v );

        this->parent_->signal_value();
        return true;
    }

    bool on_uint64( system::error_code& ec, std::uint64_t v )
    {
        if( !integral_in_range<V>( v ) )
        {
            BOOST_JSON_FAIL( ec, error::not_exact );
            return false;
        }

        *value_ = static_cast<V>( v );

        this->parent_->signal_value();
        return true;
    }
};

// floating point handler
template< class V, class P>
class converting_handler<floating_point_conversion_tag, V, P>
    : public scalar_handler<P, error::not_double>
{
private:
    V* value_;

public:
    converting_handler( V* v, P* p )
        : converting_handler::scalar_handler(p)
        , value_(v)
    {}

    bool on_number_part( system::error_code& )
    {
        return true;
    }

    bool on_int64( system::error_code&, std::int64_t v )
    {
        *value_ = static_cast<V>( v );

        this->parent_->signal_value();
        return true;
    }

    bool on_uint64( system::error_code&, std::uint64_t v )
    {
        *value_ = static_cast<V>( v );

        this->parent_->signal_value();
        return true;
    }

    bool on_double( system::error_code&, double v )
    {
        *value_ = static_cast<V>( v );

        this->parent_->signal_value();
        return true;
    }
};

// string handler
template< class V, class P >
class converting_handler<string_like_conversion_tag, V, P>
    : public scalar_handler<P, error::not_string>
{
private:
    V* value_;
    bool cleared_ = false;

public:
    converting_handler( V* v, P* p )
        : converting_handler::scalar_handler(p)
        , value_(v)
    {}

    bool on_string_part( system::error_code&, string_view sv )
    {
        if( !cleared_ )
        {
            cleared_ = true;
            value_->clear();
        }

        value_->append( sv.begin(), sv.end() );
        return true;
    }

    bool on_string( system::error_code&, string_view sv )
    {
        if( !cleared_ )
            value_->clear();
        else
            cleared_ = false;

        value_->append( sv.begin(), sv.end() );

        this->parent_->signal_value();
        return true;
    }
};

// bool handler
template< class V, class P >
class converting_handler<bool_conversion_tag, V, P>
    : public scalar_handler<P, error::not_bool>
{
private:
    V* value_;

public:
    converting_handler( V* v, P* p )
        : converting_handler::scalar_handler(p)
        , value_(v)
    {}

    bool on_bool( system::error_code&, bool v )
    {
        *value_ = v;

        this->parent_->signal_value();
        return true;
    }
};

// null handler
template< class V, class P >
class converting_handler<null_like_conversion_tag, V, P>
    : public scalar_handler<P, error::not_null>
{
private:
    V* value_;

public:
    converting_handler( V* v, P* p )
        : converting_handler::scalar_handler(p)
        , value_(v)
    {}

    bool on_null( system::error_code& )
    {
        *value_ = {};

        this->parent_->signal_value();
        return true;
    }
};

// described enum handler
template< class V, class P >
class converting_handler<described_enum_conversion_tag, V, P>
    : public scalar_handler<P, error::not_string>
{
#ifndef BOOST_DESCRIBE_CXX14

    static_assert(
        sizeof(V) == 0, "Enum support for parse_into requires C++14" );

#else

private:
    V* value_;
    std::string name_;

public:
    converting_handler( V* v, P* p )
        : converting_handler::scalar_handler(p)
        , value_(v)
    {}

    bool on_string_part( system::error_code&, string_view sv )
    {
        name_.append( sv.begin(), sv.end() );
        return true;
    }

    bool on_string( system::error_code& ec, string_view sv )
    {
        string_view name = sv;
        if( !name_.empty() )
        {
            name_.append( sv.begin(), sv.end() );
            name = name_;
        }

        if( !describe::enum_from_string(name, *value_) )
        {
            BOOST_JSON_FAIL(ec, error::unknown_name);
            return false;
        }

        this->parent_->signal_value();
        return true;
    }

#endif // BOOST_DESCRIBE_CXX14
};

template< class V, class P >
class converting_handler<no_conversion_tag, V, P>
{
    static_assert( sizeof(V) == 0, "This type is not supported" );
};

// sequence handler
template< class It >
bool check_inserter( It l, It r )
{
    return l == r;
}

template< class It1, class It2 >
std::true_type check_inserter( It1, It2 )
{
    return {};
}

template<class T>
void
clear_container(
    T&,
    mp11::mp_int<2>)
{
}

template<class T>
void
clear_container(
    T& target,
    mp11::mp_int<1>)
{
    target.clear();
}

template<class T>
void
clear_container(
    T& target,
    mp11::mp_int<0>)
{
    target.clear();
}

template< class V, class P >
class converting_handler<sequence_conversion_tag, V, P>
    : public composite_handler<
        converting_handler<sequence_conversion_tag, V, P>,
        detail::value_type<V>,
        P,
        error::not_array>
{
private:
    V* value_;

    using Inserter = decltype(
        detail::inserter(*value_, inserter_implementation<V>()) );
    Inserter inserter;

public:
    converting_handler( V* v, P* p )
        : converting_handler::composite_handler(p)
        , value_(v)
        , inserter( detail::inserter(*value_, inserter_implementation<V>()) )
    {}

    void signal_value()
    {
        *inserter++ = std::move(this->next_value_);
#if defined(__GNUC__) && __GNUC__ < 5 && !defined(__clang__)
# pragma GCC diagnostic push
# pragma GCC diagnostic ignored "-Wmissing-field-initializers"
#endif
        this->next_value_ = {};
#if defined(__GNUC__) && __GNUC__ < 5 && !defined(__clang__)
# pragma GCC diagnostic pop
#endif
    }

    bool signal_end(system::error_code& ec)
    {
        if( !check_inserter( inserter, value_->end() ) )
        {
            BOOST_JSON_FAIL( ec, error::size_mismatch );
            return false;
        }

        inserter = detail::inserter(*value_, inserter_implementation<V>());

        return converting_handler::composite_handler::signal_end(ec);
    }

    bool on_array_begin( system::error_code& ec )
    {
        if( this->inner_active_ )
            return this->inner_.on_array_begin( ec );

        this->inner_active_ = true;
        clear_container( *value_, inserter_implementation<V>() );
        return true;
    }

    bool on_array_end( system::error_code& ec )
    {
        if( this->inner_active_ )
            return this->inner_.on_array_end( ec );

        return this->parent_->signal_end(ec);
    }
};

// map handler
template< class V, class P >
class converting_handler<map_like_conversion_tag, V, P>
    : public composite_handler<
        converting_handler<map_like_conversion_tag, V, P>,
        detail::mapped_type<V>,
        P,
        error::not_object>
{
private:
    V* value_;
    std::string key_;

public:
    converting_handler( V* v, P* p )
        : converting_handler::composite_handler(p), value_(v)
    {}

    void signal_value()
    {
        value_->emplace( std::move(key_), std::move(this->next_value_) );

        key_ = {};
        this->next_value_ = {};

        this->inner_active_ = false;
    }

    bool on_object_begin( system::error_code& ec )
    {
        if( this->inner_active_ )
            return this->inner_.on_object_begin(ec);

        clear_container( *value_, inserter_implementation<V>() );
        return true;
    }

    bool on_object_end( system::error_code& ec )
    {
        if( this->inner_active_ )
            return this->inner_.on_object_end(ec);

        this->parent_->signal_value();
        return true;
    }

    bool on_array_end( system::error_code& ec )
    {
        if( this->inner_active_ )
            return this->inner_.on_array_end(ec);

        return this->parent_->signal_end(ec);
    }

    bool on_key_part( system::error_code& ec, string_view sv )
    {
        if( this->inner_active_ )
            return this->inner_.on_key_part(ec, sv);

        key_.append( sv.data(), sv.size() );
        return true;
    }

    bool on_key( system::error_code& ec, string_view sv )
    {
        if( this->inner_active_ )
            return this->inner_.on_key(ec, sv);

        key_.append( sv.data(), sv.size() );

        this->inner_active_ = true;
        return true;
    }
};

// tuple handler
template<std::size_t I, class T>
struct handler_tuple_element
{
    template< class... Args >
    handler_tuple_element( Args&& ... args )
        : t_( static_cast<Args&&>(args)... )
    {}

    T t_;
};

template<std::size_t I, class T>
T&
get( handler_tuple_element<I, T>& e )
{
    return e.t_;
}

template<
    class P,
    class LV,
    class S = mp11::make_index_sequence<mp11::mp_size<LV>::value> >
struct handler_tuple;

template< class P, template<class...> class L, class... V, std::size_t... I >
struct handler_tuple< P, L<V...>, mp11::index_sequence<I...> >
    : handler_tuple_element< I, get_handler<V, P> >
    ...
{
    handler_tuple( handler_tuple const& ) = delete;
    handler_tuple& operator=( handler_tuple const& ) = delete;

    template< class Access, class T >
    handler_tuple( Access access, T* pv, P* pp )
        : handler_tuple_element< I, get_handler<V, P> >(
            access( pv, mp11::mp_int<I>() ),
            pp )
        ...
    { }
};

#if defined(BOOST_MSVC) && BOOST_MSVC < 1910

template< class T >
struct tuple_element_list_impl
{
    template< class I >
    using tuple_element_helper = tuple_element_t<I::value, T>;

    using type = mp11::mp_transform<
        tuple_element_helper,
        mp11::mp_iota< std::tuple_size<T> > >;
};
template< class T >
using tuple_element_list = typename tuple_element_list_impl<T>::type;

#else

template< class I, class T >
using tuple_element_helper = tuple_element_t<I::value, T>;
template< class T >
using tuple_element_list = mp11::mp_transform_q<
    mp11::mp_bind_back< tuple_element_helper, T>,
    mp11::mp_iota< std::tuple_size<T> > >;

#endif

template< class Op, class... Args>
struct handler_op_invoker
{
public:
    std::tuple<Args&...> args;

    template< class Handler >
    bool
    operator()( Handler& handler ) const
    {
        return (*this)( handler, mp11::index_sequence_for<Args...>() );
    }

private:
    template< class Handler, std::size_t... I >
    bool
    operator()( Handler& handler, mp11::index_sequence<I...> ) const
    {
        return Op()( handler, std::get<I>(args)... );
    }
};

template< class Handlers, class F >
struct tuple_handler_op_invoker
{
    Handlers& handlers;
    F fn;

    template< class I >
    bool
    operator()( I ) const
    {
        return fn( get<I::value>(handlers) );
    }
};

struct tuple_accessor
{
    template< class T, class I >
    auto operator()( T* t, I ) const -> tuple_element_t<I::value, T>*
    {
        using std::get;
        return &get<I::value>(*t);
    }
};

template< class T, class P >
class converting_handler<tuple_conversion_tag, T, P>
{

private:
    T* value_;
    P* parent_;

    handler_tuple< converting_handler, tuple_element_list<T> > handlers_;
    int inner_active_ = -1;

public:
    converting_handler( converting_handler const& ) = delete;
    converting_handler& operator=( converting_handler const& ) = delete;

    converting_handler( T* v, P* p )
        : value_(v) , parent_(p) , handlers_(tuple_accessor(), v, this)
    {}

    void signal_value()
    {
        ++inner_active_;
    }

    bool signal_end(system::error_code& ec)
    {
        constexpr int N = std::tuple_size<T>::value;
        if( inner_active_ < N )
        {
            BOOST_JSON_FAIL( ec, error::size_mismatch );
            return true;
        }

        inner_active_ = -1;
        parent_->signal_value();
        return true;
    }

#define BOOST_JSON_HANDLE_EVENT(fn) \
    struct do_ ## fn \
    { \
        template< class H, class... Args > \
        bool operator()( H& h, Args& ... args ) const \
        { \
            return h. fn (args...); \
        } \
    }; \
       \
    template< class... Args > \
    bool fn( system::error_code& ec, Args&& ... args ) \
    { \
        if( inner_active_ < 0 ) \
        { \
            BOOST_JSON_FAIL( ec, error::not_array ); \
            return false; \
        } \
        constexpr int N = std::tuple_size<T>::value; \
        if( inner_active_ >= N ) \
        { \
            BOOST_JSON_FAIL( ec, error::size_mismatch ); \
            return false; \
        } \
        using F = handler_op_invoker< do_ ## fn, system::error_code, Args...>; \
        using H = decltype(handlers_); \
        return mp11::mp_with_index<N>( \
            inner_active_, \
            tuple_handler_op_invoker<H, F>{ \
                handlers_, \
                F{ std::forward_as_tuple(ec, args...) } } ); \
    }

    BOOST_JSON_HANDLE_EVENT( on_object_begin )
    BOOST_JSON_HANDLE_EVENT( on_object_end )

    struct do_on_array_begin
    {
        handler_tuple< converting_handler, tuple_element_list<T> >& handlers;
        system::error_code& ec;

        template< class I >
        bool operator()( I ) const
        {
            return get<I::value>(handlers).on_array_begin(ec);
        }
    };
    bool on_array_begin( system::error_code& ec )
    {
        if( inner_active_ < 0 )
        {
            inner_active_ = 0;
            return true;
        }

        constexpr int N = std::tuple_size<T>::value;

        if( inner_active_ >= N )
        {
            inner_active_ = 0;
            return true;
        }

        return mp11::mp_with_index<N>(
            inner_active_, do_on_array_begin{handlers_, ec} );
    }

    struct do_on_array_end
    {
        handler_tuple< converting_handler, tuple_element_list<T> >& handlers;
        system::error_code& ec;

        template< class I >
        bool operator()( I ) const
        {
            return get<I::value>(handlers).on_array_end(ec);
        }
    };
    bool on_array_end( system::error_code& ec )
    {
        if( inner_active_ < 0 )
            return parent_->signal_end(ec);

        constexpr int N = std::tuple_size<T>::value;

        if( inner_active_ >= N )
            return signal_end(ec);

        return mp11::mp_with_index<N>(
            inner_active_, do_on_array_end{handlers_, ec} );
    }

    BOOST_JSON_HANDLE_EVENT( on_key_part )
    BOOST_JSON_HANDLE_EVENT( on_key )
    BOOST_JSON_HANDLE_EVENT( on_string_part )
    BOOST_JSON_HANDLE_EVENT( on_string )
    BOOST_JSON_HANDLE_EVENT( on_number_part )
    BOOST_JSON_HANDLE_EVENT( on_int64 )
    BOOST_JSON_HANDLE_EVENT( on_uint64 )
    BOOST_JSON_HANDLE_EVENT( on_double )
    BOOST_JSON_HANDLE_EVENT( on_bool )
    BOOST_JSON_HANDLE_EVENT( on_null )

#undef BOOST_JSON_HANDLE_EVENT
};

// described struct handler
#if defined(BOOST_MSVC) && BOOST_MSVC < 1910

template< class T >
struct struct_element_list_impl
{
    template< class D >
    using helper = described_member_t<T, D>;

    using type = mp11::mp_transform< helper, described_members<T> >;
};
template< class T >
using struct_element_list = typename struct_element_list_impl<T>::type;

#else

template< class T >
using struct_element_list = mp11::mp_transform_q<
    mp11::mp_bind_front< described_member_t, T >, described_members<T> >;

#endif

struct struct_accessor
{
    template< class T, class I >
    auto operator()( T* t, I ) const
        -> described_member_t<T, mp11::mp_at< described_members<T>, I> >*
    {
        using Ds = described_members<T>;
        using D = mp11::mp_at<Ds, I>;
        return &(t->*D::pointer);
    }
};

template< class F >
struct struct_key_searcher
{
    F fn;

    template< class D >
    void
    operator()( D ) const
    {
        fn( D::name ) ;
    }
};

template<class V, class P>
class converting_handler<described_class_conversion_tag, V, P>
{
#if !defined(BOOST_DESCRIBE_CXX14)

    static_assert(
        sizeof(V) == 0, "Struct support for parse_into requires C++14" );

#else

private:
    V* value_;
    P* parent_;

    std::string key_;

    using Dm = described_members<V>;

    handler_tuple< converting_handler, struct_element_list<V> > handlers_;
    int inner_active_ = -1;
    std::size_t activated_ = 0;

public:
    converting_handler( converting_handler const& ) = delete;
    converting_handler& operator=( converting_handler const& ) = delete;

    converting_handler( V* v, P* p )
        : value_(v), parent_(p), handlers_(struct_accessor(), v, this)
    {}

    struct is_optional_checker
    {
        template< class I >
        bool operator()( I ) const noexcept
        {
            using L = struct_element_list<V>;
            using T = mp11::mp_at<L, I>;
            return !is_optional_like<T>::value;
        }
    };
    void signal_value()
    {
        BOOST_ASSERT( inner_active_ >= 0 );
        bool required_member = mp11::mp_with_index< mp11::mp_size<Dm> >(
            inner_active_,
            is_optional_checker{});
        if( required_member )
            ++activated_;

        key_ = {};
        inner_active_ = -1;
    }

    bool signal_end(system::error_code&)
    {
        key_ = {};
        inner_active_ = -1;
        parent_->signal_value();
        return true;
    }

#define BOOST_JSON_INVOKE_INNER(fn) \
    if( inner_active_ < 0 ) \
    { \
        BOOST_JSON_FAIL( ec, error::not_object ); \
        return false; \
    } \
    auto f = [&](auto& handler) { return handler.fn ; }; \
    using F = decltype(f); \
    using H = decltype(handlers_); \
    return mp11::mp_with_index< mp11::mp_size<Dm> >( \
            inner_active_, \
            tuple_handler_op_invoker<H, F>{handlers_, f} );

    bool on_object_begin( system::error_code& ec )
    {
        if( inner_active_ < 0 )
            return true;

        BOOST_JSON_INVOKE_INNER( on_object_begin(ec) );
    }

    bool on_object_end( system::error_code& ec )
    {
        if( inner_active_ < 0 )
        {
            using L = struct_element_list<V>;
            using C = mp11::mp_count_if<L, is_optional_like>;
            constexpr int N = mp11::mp_size<L>::value - C::value;
            if( activated_ < N )
            {
                BOOST_JSON_FAIL( ec, error::size_mismatch );
                return false;
            }

            parent_->signal_value();
            return true;
        }

        BOOST_JSON_INVOKE_INNER( on_object_end(ec) );
    }

    bool on_array_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_array_begin(ec) );
    }

    bool on_array_end( system::error_code& ec )
    {
        if( inner_active_ < 0 )
            return parent_->signal_end(ec);

        BOOST_JSON_INVOKE_INNER( on_array_end(ec) );
    }

    bool on_key_part( system::error_code& ec, string_view sv )
    {
        if( inner_active_ < 0 )
        {
            key_.append( sv.data(), sv.size() );
            return true;
        }

        BOOST_JSON_INVOKE_INNER( on_key_part(ec, sv) );
    }

    bool on_key( system::error_code& ec, string_view sv )
    {
        if( inner_active_ >= 0 )
        {
            BOOST_JSON_INVOKE_INNER( on_key(ec, sv) );
        }

        string_view key = sv;
        if( !key_.empty() )
        {
            key_.append( sv.data(), sv.size() );
            key = key_;
        }

        int i = 0;

        auto f = [&](char const* name)
        {
            if( key == name )
                inner_active_ = i;
            ++i;
        };

        mp11::mp_for_each<Dm>(
            struct_key_searcher<decltype(f)>{f} );

        if( inner_active_ < 0 )
        {
            BOOST_JSON_FAIL(ec, error::unknown_name);
            return false;
        }

        return true;
    }

    bool on_string_part( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_string_part(ec, sv) );
    }

    bool on_string( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_string(ec, sv) );
    }

    bool on_number_part( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_number_part(ec) );
    }

    bool on_int64( system::error_code& ec, std::int64_t v )
    {
        BOOST_JSON_INVOKE_INNER( on_int64(ec, v) );
    }

    bool on_uint64( system::error_code& ec, std::uint64_t v )
    {
        BOOST_JSON_INVOKE_INNER( on_uint64(ec, v) );
    }

    bool on_double( system::error_code& ec, double v )
    {
        BOOST_JSON_INVOKE_INNER( on_double(ec, v) );
    }

    bool on_bool( system::error_code& ec, bool v )
    {
        BOOST_JSON_INVOKE_INNER( on_bool(ec, v) );
    }

    bool on_null( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_null(ec) );
    }

#undef BOOST_JSON_INVOKE_INNER

#endif
};

// variant handler
struct object_begin_handler_event
{ };

struct object_end_handler_event
{ };

struct array_begin_handler_event
{ };

struct array_end_handler_event
{ };

struct key_handler_event
{
    std::string value;
};

struct string_handler_event
{
    std::string value;
};

struct int64_handler_event
{
    std::int64_t value;
};

struct uint64_handler_event
{
    std::uint64_t value;
};

struct double_handler_event
{
    double value;
};

struct bool_handler_event
{
    bool value;
};

struct null_handler_event
{ };

using parse_event = variant2::variant<
    object_begin_handler_event,
    object_end_handler_event,
    array_begin_handler_event,
    array_end_handler_event,
    key_handler_event,
    string_handler_event,
    int64_handler_event,
    uint64_handler_event,
    double_handler_event,
    bool_handler_event,
    null_handler_event>;

template< class H >
struct event_visitor
{
    H& handler;
    system::error_code& ec;

    bool
    operator()(object_begin_handler_event&) const
    {
        return handler.on_object_begin(ec);
    }

    bool
    operator()(object_end_handler_event&) const
    {
        return handler.on_object_end(ec);
    }

    bool
    operator()(array_begin_handler_event&) const
    {
        return handler.on_array_begin(ec);
    }

    bool
    operator()(array_end_handler_event&) const
    {
        return handler.on_array_end(ec);
    }

    bool
    operator()(key_handler_event& ev) const
    {
        return handler.on_key(ec, ev.value);
    }

    bool
    operator()(string_handler_event& ev) const
    {
        return handler.on_string(ec, ev.value);
    }

    bool
    operator()(int64_handler_event& ev) const
    {
        return handler.on_int64(ec, ev.value);
    }

    bool
    operator()(uint64_handler_event& ev) const
    {
        return handler.on_uint64(ec, ev.value);
    }

    bool
    operator()(double_handler_event& ev) const
    {
        return handler.on_double(ec, ev.value);
    }

    bool
    operator()(bool_handler_event& ev) const
    {
        return handler.on_bool(ec, ev.value);
    }

    bool
    operator()(null_handler_event&) const
    {
        return handler.on_null(ec);
    }
};

// L<T...> -> variant< monostate, get_handler<T, P>... >
template< class P, class L >
using inner_handler_variant = mp11::mp_push_front<
    mp11::mp_transform_q<
        mp11::mp_bind_back<get_handler, P>,
        mp11::mp_apply<variant2::variant, L>>,
    variant2::monostate>;

template< class T, class P >
class converting_handler<variant_conversion_tag, T, P>
{
private:
    using variant_size = mp11::mp_size<T>;

    T* value_;
    P* parent_;

    std::string string_;
    std::vector< parse_event > events_;
    inner_handler_variant<converting_handler, T> inner_;
    int inner_active_ = -1;

public:
    converting_handler( converting_handler const& ) = delete;
    converting_handler& operator=( converting_handler const& ) = delete;

    converting_handler( T* v, P* p )
        : value_( v )
        , parent_( p )
    {}

    void signal_value()
    {
        inner_.template emplace<0>();
        inner_active_ = -1;
        events_.clear();
        parent_->signal_value();
    }

    bool signal_end(system::error_code& ec)
    {
        return parent_->signal_end(ec);
    }

    struct alternative_selector
    {
        converting_handler* self;

        template< class I >
        void
        operator()( I ) const
        {
            using V = mp11::mp_at<T, I>;
            auto& v = self->value_->template emplace<I::value>( V{} );
            self->inner_.template emplace<I::value + 1>(&v, self);
        }
    };
    void
    next_alternative()
    {
        if( ++inner_active_ >= static_cast<int>(variant_size::value) )
            return;

        mp11::mp_with_index< variant_size::value >(
            inner_active_, alternative_selector{this} );
    }

    struct event_processor
    {
        converting_handler* self;
        system::error_code& ec;
        parse_event& event;

        template< class I >
        bool operator()( I ) const
        {
            auto& handler = variant2::get<I::value + 1>(self->inner_);
            using Handler = remove_cvref<decltype(handler)>;
            return variant2::visit(
                event_visitor<Handler>{handler, ec}, event );
        }
    };
    bool process_events(system::error_code& ec)
    {
        constexpr std::size_t N = variant_size::value;

        // should be pointers not iterators, otherwise MSVC crashes
        auto const last = events_.data() + events_.size();
        auto first = last - 1;
        bool ok = false;

        if( inner_active_ < 0 )
            next_alternative();
        do
        {
            if( static_cast<std::size_t>(inner_active_) >= N )
            {
                BOOST_JSON_FAIL( ec, error::exhausted_variants );
                return false;
            }

            for ( ; first != last; ++first )
            {
                ok = mp11::mp_with_index< N >(
                    inner_active_, event_processor{this, ec, *first} );
                if( !ok )
                {
                    first = events_.data();
                    next_alternative();
                    ec.clear();
                    break;
                }
            }
        }
        while( !ok );

        return true;
    }

#define BOOST_JSON_INVOKE_INNER(ev, ec) \
    events_.emplace_back( ev ); \
    return process_events(ec);

    bool on_object_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( object_begin_handler_event{}, ec );
    }

    bool on_object_end( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( object_end_handler_event{}, ec );
    }

    bool on_array_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( array_begin_handler_event{}, ec );
    }

    bool on_array_end( system::error_code& ec )
    {
        if( !inner_active_ )
            return signal_end(ec);

        BOOST_JSON_INVOKE_INNER( array_end_handler_event{}, ec );
    }

    bool on_key_part( system::error_code&, string_view sv )
    {
        string_.append(sv);
        return true;
    }

    bool on_key( system::error_code& ec, string_view sv )
    {
        string_.append(sv);
        BOOST_JSON_INVOKE_INNER( key_handler_event{ std::move(string_) }, ec );
    }

    bool on_string_part( system::error_code&, string_view sv )
    {
        string_.append(sv);
        return true;
    }

    bool on_string( system::error_code& ec, string_view sv )
    {
        string_.append(sv);
        BOOST_JSON_INVOKE_INNER(
            string_handler_event{ std::move(string_) }, ec );
    }

    bool on_number_part( system::error_code& )
    {
        return true;
    }

    bool on_int64( system::error_code& ec, std::int64_t v )
    {
        BOOST_JSON_INVOKE_INNER( int64_handler_event{v}, ec );
    }

    bool on_uint64( system::error_code& ec, std::uint64_t v )
    {
        BOOST_JSON_INVOKE_INNER( uint64_handler_event{v}, ec );
    }

    bool on_double( system::error_code& ec, double v )
    {
        BOOST_JSON_INVOKE_INNER( double_handler_event{v}, ec );
    }

    bool on_bool( system::error_code& ec, bool v )
    {
        BOOST_JSON_INVOKE_INNER( bool_handler_event{v}, ec );
    }

    bool on_null( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( null_handler_event{}, ec );
    }

#undef BOOST_JSON_INVOKE_INNER
};

// optional handler
template<class V, class P>
class converting_handler<optional_conversion_tag, V, P>
{
private:
    using inner_type = value_result_type<V>;
    using inner_handler_type = get_handler<inner_type, converting_handler>;

    V* value_;
    P* parent_;

    inner_type inner_value_ = {};
    inner_handler_type inner_;
    bool inner_active_ = false;

public:
    converting_handler( converting_handler const& ) = delete;
    converting_handler& operator=( converting_handler const& ) = delete;

    converting_handler( V* v, P* p )
        : value_(v), parent_(p), inner_(&inner_value_, this)
    {}

    void signal_value()
    {
        *value_ = std::move(inner_value_);

        inner_active_ = false;
        parent_->signal_value();
    }

    bool signal_end(system::error_code& ec)
    {
        return parent_->signal_end(ec);
    }

#define BOOST_JSON_INVOKE_INNER(fn) \
    if( !inner_active_ ) \
        inner_active_ = true; \
    return inner_.fn;

    bool on_object_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_object_begin(ec) );
    }

    bool on_object_end( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_object_end(ec) );
    }

    bool on_array_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_array_begin(ec) );
    }

    bool on_array_end( system::error_code& ec )
    {
        if( !inner_active_ )
            return signal_end(ec);

        BOOST_JSON_INVOKE_INNER( on_array_end(ec) );
    }

    bool on_key_part( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_key_part(ec, sv) );
    }

    bool on_key( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_key(ec, sv) );
    }

    bool on_string_part( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_string_part(ec, sv) );
    }

    bool on_string( system::error_code& ec, string_view sv )
    {
        BOOST_JSON_INVOKE_INNER( on_string(ec, sv) );
    }

    bool on_number_part( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_number_part(ec) );
    }

    bool on_int64( system::error_code& ec, std::int64_t v )
    {
        BOOST_JSON_INVOKE_INNER( on_int64(ec, v) );
    }

    bool on_uint64( system::error_code& ec, std::uint64_t v )
    {
        BOOST_JSON_INVOKE_INNER( on_uint64(ec, v) );
    }

    bool on_double( system::error_code& ec, double v )
    {
        BOOST_JSON_INVOKE_INNER( on_double(ec, v) );
    }

    bool on_bool( system::error_code& ec, bool v )
    {
        BOOST_JSON_INVOKE_INNER( on_bool(ec, v) );
    }

    bool on_null( system::error_code& ec )
    {
        if( !inner_active_ )
        {
            *value_ = {};

            this->parent_->signal_value();
            return true;
        }
        else
        {
            return inner_.on_null(ec);
        }
    }

#undef BOOST_JSON_INVOKE_INNER
};

// path handler
template< class V, class P >
class converting_handler<path_conversion_tag, V, P>
    : public scalar_handler<P, error::not_string>
{
private:
    V* value_;
    bool cleared_ = false;

public:
    converting_handler( V* v, P* p )
        : converting_handler::scalar_handler(p)
        , value_(v)
    {}

    bool on_string_part( system::error_code&, string_view sv )
    {
        if( !cleared_ )
        {
            cleared_ = true;
            value_->clear();
        }

        value_->concat( sv.begin(), sv.end() );
        return true;
    }

    bool on_string( system::error_code&, string_view sv )
    {
        if( !cleared_ )
            value_->clear();
        else
            cleared_ = false;

        value_->concat( sv.begin(), sv.end() );

        this->parent_->signal_value();
        return true;
    }
};

// into_handler
template< class V >
class into_handler
{
private:

    using inner_handler_type = get_handler<V, into_handler>;

    inner_handler_type inner_;
    bool inner_active_ = true;

public:

    into_handler( into_handler const& ) = delete;
    into_handler& operator=( into_handler const& ) = delete;

public:

    static constexpr std::size_t max_object_size = object::max_size();
    static constexpr std::size_t max_array_size = array::max_size();
    static constexpr std::size_t max_key_size = string::max_size();
    static constexpr std::size_t max_string_size = string::max_size();

public:

    explicit into_handler( V* v ): inner_( v, this )
    {
    }

    void signal_value()
    {
    }

    bool signal_end(system::error_code&)
    {
        return true;
    }

    bool on_document_begin( system::error_code& )
    {
        return true;
    }

    bool on_document_end( system::error_code& )
    {
        inner_active_ = false;
        return true;
    }

#define BOOST_JSON_INVOKE_INNER(f) \
    if( !inner_active_ ) \
    { \
        BOOST_JSON_FAIL( ec, error::extra_data ); \
        return false; \
    } \
    else \
        return inner_.f

    bool on_object_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_object_begin(ec) );
    }

    bool on_object_end( std::size_t, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_object_end(ec) );
    }

    bool on_array_begin( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_array_begin(ec) );
    }

    bool on_array_end( std::size_t, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_array_end(ec) );
    }

    bool on_key_part( string_view sv, std::size_t, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_key_part(ec, sv) );
    }

    bool on_key( string_view sv, std::size_t, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_key(ec, sv) );
    }

    bool on_string_part( string_view sv, std::size_t, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_string_part(ec, sv) );
    }

    bool on_string( string_view sv, std::size_t, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_string(ec, sv) );
    }

    bool on_number_part( string_view, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_number_part(ec) );
    }

    bool on_int64( std::int64_t v, string_view, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_int64(ec, v) );
    }

    bool on_uint64( std::uint64_t v, string_view, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_uint64(ec, v) );
    }

    bool on_double( double v, string_view, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_double(ec, v) );
    }

    bool on_bool( bool v, system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_bool(ec, v) );
    }

    bool on_null( system::error_code& ec )
    {
        BOOST_JSON_INVOKE_INNER( on_null(ec) );
    }

    bool on_comment_part(string_view, system::error_code&)
    {
        return true;
    }

    bool on_comment(string_view, system::error_code&)
    {
        return true;
    }

#undef BOOST_JSON_INVOKE_INNER
};

} // namespace detail
} // namespace boost
} // namespace json

#endif
