//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/json
//

#ifndef BOOST_JSON_IMPL_OBJECT_HPP
#define BOOST_JSON_IMPL_OBJECT_HPP

#include <boost/json/value.hpp>
#include <iterator>
#include <cmath>
#include <type_traits>
#include <utility>

namespace boost {
namespace json {

namespace detail {

// Objects with size less than or equal
// to this number will use a linear search
// instead of the more expensive hash function.
static
constexpr
std::size_t
small_object_size_ = 18;

BOOST_STATIC_ASSERT(
    small_object_size_ <
    BOOST_JSON_MAX_STRUCTURED_SIZE);

} // detail

//----------------------------------------------------------

struct alignas(key_value_pair)
    object::table
{
    std::uint32_t size = 0;
    std::uint32_t capacity = 0;
    std::uintptr_t salt = 0;

#if defined(_MSC_VER) && BOOST_JSON_ARCH == 32
    // VFALCO If we make key_value_pair smaller,
    //        then we might want to revisit this
    //        padding.
    BOOST_STATIC_ASSERT(
        sizeof(key_value_pair) == 32);
    char pad[4] = {}; // silence warnings
#endif

    constexpr table();

    // returns true if we use a linear
    // search instead of the hash table.
    bool is_small() const noexcept
    {
        return capacity <=
            detail::small_object_size_;
    }

    key_value_pair&
    operator[](
        std::size_t pos) noexcept
    {
        return reinterpret_cast<
            key_value_pair*>(
                this + 1)[pos];
    }

    // VFALCO This is exported for tests
    BOOST_JSON_DECL
    std::size_t
    digest(string_view key) const noexcept;

    inline
    index_t&
    bucket(std::size_t hash) noexcept;

    inline
    index_t&
    bucket(string_view key) noexcept;

    inline
    void
    clear() noexcept;

    static
    inline
    table*
    allocate(
        std::size_t capacity,
        std::uintptr_t salt,
        storage_ptr const& sp);

    static
    void
    deallocate(
        table* p,
        storage_ptr const& sp) noexcept
    {
        if(p->capacity == 0)
            return;
        if(! p->is_small())
            sp->deallocate(p,
                sizeof(table) + p->capacity * (
                    sizeof(key_value_pair) +
                    sizeof(index_t)));
        else
            sp->deallocate(p,
                sizeof(table) + p->capacity *
                    sizeof(key_value_pair));
    }
};

//----------------------------------------------------------

class object::revert_construct
{
    object* obj_;

    BOOST_JSON_DECL
    void
    destroy() noexcept;

public:
    explicit
    revert_construct(
        object& obj) noexcept
        : obj_(&obj)
    {
    }

    ~revert_construct()
    {
        if(! obj_)
            return;
        destroy();
    }

    void
    commit() noexcept
    {
        obj_ = nullptr;
    }
};

//----------------------------------------------------------

class object::revert_insert
{
    object* obj_;
    table* t_ = nullptr;
    std::size_t size_;

    BOOST_JSON_DECL
    void
    destroy() noexcept;

public:
    explicit
    revert_insert(
        object& obj,
        std::size_t capacity)
        : obj_(&obj)
        , size_(obj_->size())
    {
        if( capacity > obj_->capacity() )
            t_ = obj_->reserve_impl(capacity);
    }

    ~revert_insert()
    {
        if(! obj_)
            return;

        destroy();
        if( t_ )
        {
            table::deallocate( obj_->t_, obj_->sp_ );
            obj_->t_ = t_;
        }
        else
        {
            obj_->t_->size = static_cast<index_t>(size_);
        }
    }

    void
    commit() noexcept
    {
        BOOST_ASSERT(obj_);
        if( t_ )
            table::deallocate( t_, obj_->sp_ );
        obj_ = nullptr;
    }
};

//----------------------------------------------------------
//
// Iterators
//
//----------------------------------------------------------

auto
object::
begin() noexcept ->
    iterator
{
    return &(*t_)[0];
}

auto
object::
begin() const noexcept ->
    const_iterator
{
    return &(*t_)[0];
}

auto
object::
cbegin() const noexcept ->
    const_iterator
{
    return &(*t_)[0];
}

auto
object::
end() noexcept ->
    iterator
{
    return &(*t_)[t_->size];
}

auto
object::
end() const noexcept ->
    const_iterator
{
    return &(*t_)[t_->size];
}

auto
object::
cend() const noexcept ->
    const_iterator
{
    return &(*t_)[t_->size];
}

auto
object::
rbegin() noexcept ->
    reverse_iterator
{
    return reverse_iterator(end());
}

auto
object::
rbegin() const noexcept ->
    const_reverse_iterator
{
    return const_reverse_iterator(end());
}

auto
object::
crbegin() const noexcept ->
    const_reverse_iterator
{
    return const_reverse_iterator(end());
}

auto
object::
rend() noexcept ->
    reverse_iterator
{
    return reverse_iterator(begin());
}

auto
object::
rend() const noexcept ->
    const_reverse_iterator
{
    return const_reverse_iterator(begin());
}

auto
object::
crend() const noexcept ->
    const_reverse_iterator
{
    return const_reverse_iterator(begin());
}

//----------------------------------------------------------
//
// Capacity
//
//----------------------------------------------------------

bool
object::
empty() const noexcept
{
    return t_->size == 0;
}

auto
object::
size() const noexcept ->
    std::size_t
{
    return t_->size;
}

constexpr
std::size_t
object::
max_size() noexcept
{
    // max_size depends on the address model
    using min = std::integral_constant<std::size_t,
        (std::size_t(-1) - sizeof(table)) /
            (sizeof(key_value_pair) + sizeof(index_t))>;
    return min::value < BOOST_JSON_MAX_STRUCTURED_SIZE ?
        min::value : BOOST_JSON_MAX_STRUCTURED_SIZE;
}

auto
object::
capacity() const noexcept ->
    std::size_t
{
    return t_->capacity;
}

void
object::
reserve(std::size_t new_capacity)
{
    if( new_capacity <= capacity() )
        return;
    table* const old_table = reserve_impl(new_capacity);
    table::deallocate( old_table, sp_ );
}

//----------------------------------------------------------
//
// Lookup
//
//----------------------------------------------------------

auto
object::
at(string_view key) & ->
    value&
{
    auto const& self = *this;
    return const_cast< value& >( self.at(key) );
}

auto
object::
at(string_view key) && ->
    value&&
{
    return std::move( at(key) );
}

auto
object::
at(string_view key) const& ->
    value const&
{
    auto it = find(key);
    if(it == end())
    {
        BOOST_STATIC_CONSTEXPR source_location loc = BOOST_CURRENT_LOCATION;
        detail::throw_system_error( error::out_of_range, &loc );
    }
    return it->value();
}

//----------------------------------------------------------

template<class P, class>
auto
object::
insert(P&& p) ->
    std::pair<iterator, bool>
{
    key_value_pair v(
        std::forward<P>(p), sp_);
    return emplace_impl( v.key(), pilfer(v) );
}

template<class M>
auto
object::
insert_or_assign(
    string_view key, M&& m) ->
        std::pair<iterator, bool>
{
    std::pair<iterator, bool> result = emplace_impl(
        key, key, static_cast<M&&>(m) );
    if( !result.second )
    {
        value(static_cast<M>(m), sp_).swap(
            result.first->value());
    }
    return result;
}

template<class Arg>
auto
object::
emplace(
    string_view key,
    Arg&& arg) ->
        std::pair<iterator, bool>
{
    return emplace_impl( key, key, static_cast<Arg&&>(arg) );
}

//----------------------------------------------------------
//
// (private)
//
//----------------------------------------------------------

template<class InputIt>
void
object::
construct(
    InputIt first,
    InputIt last,
    std::size_t min_capacity,
    std::input_iterator_tag)
{
    reserve(min_capacity);
    revert_construct r(*this);
    while(first != last)
    {
        insert(*first);
        ++first;
    }
    r.commit();
}

template<class InputIt>
void
object::
construct(
    InputIt first,
    InputIt last,
    std::size_t min_capacity,
    std::forward_iterator_tag)
{
    auto n = static_cast<
        std::size_t>(std::distance(
            first, last));
    if( n < min_capacity)
        n = min_capacity;
    reserve(n);
    revert_construct r(*this);
    while(first != last)
    {
        insert(*first);
        ++first;
    }
    r.commit();
}

template<class InputIt>
void
object::
insert(
    InputIt first,
    InputIt last,
    std::input_iterator_tag)
{
    // Since input iterators cannot be rewound,
    // we keep inserted elements on an exception.
    //
    while(first != last)
    {
        insert(*first);
        ++first;
    }
}

template<class InputIt>
void
object::
insert(
    InputIt first,
    InputIt last,
    std::forward_iterator_tag)
{
    auto const n =
        static_cast<std::size_t>(
            std::distance(first, last));
    auto const n0 = size();
    if(n > max_size() - n0)
    {
        BOOST_STATIC_CONSTEXPR source_location loc = BOOST_CURRENT_LOCATION;
        detail::throw_system_error( error::object_too_large, &loc );
    }
    revert_insert r( *this, n0 + n );
    while(first != last)
    {
        insert(*first);
        ++first;
    }
    r.commit();
}

template< class... Args >
std::pair<object::iterator, bool>
object::
emplace_impl( string_view key, Args&& ... args )
{
    std::pair<iterator, std::size_t> search_result(nullptr, 0);
    if( !empty() )
    {
        search_result = detail::find_in_object(*this, key);
        if( search_result.first )
            return { search_result.first, false };
    }

    // we create the new value before reserving, in case it is a reference to
    // a subobject of the current object
    key_value_pair kv( static_cast<Args&&>(args)..., sp_ );
    // the key might get deallocated too
    key = kv.key();

    std::size_t const old_capacity = capacity();
    reserve(size() + 1);
    if( (empty() && capacity() > detail::small_object_size_)
            || (capacity() != old_capacity) )
        search_result.second = detail::digest(
            key.begin(), key.end(), t_->salt);

    BOOST_ASSERT(
        t_->is_small() ||
        (search_result.second ==
            detail::digest(key.begin(), key.end(), t_->salt)) );

    return { insert_impl(pilfer(kv), search_result.second), true };
}

//----------------------------------------------------------

namespace detail {

unchecked_object::
~unchecked_object()
{
    if(! data_)
        return;
    if(sp_.is_not_shared_and_deallocate_is_trivial())
        return;
    value* p = data_;
    while(size_--)
    {
        p[0].~value();
        p[1].~value();
        p += 2;
    }
}

} // detail

} // namespace json
} // namespace boost

#endif
