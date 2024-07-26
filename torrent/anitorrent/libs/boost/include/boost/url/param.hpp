//
// Copyright (c) 2019 Vinnie Falco (vinnie.falco@gmail.com)
// Copyright (c) 2022 Alan de Freitas (alandefreitas@gmail.com)
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/url
//

#ifndef BOOST_URL_PARAM_HPP
#define BOOST_URL_PARAM_HPP

#include <boost/url/detail/config.hpp>
#include <boost/url/detail/optional_string.hpp>
#include <boost/url/pct_string_view.hpp>
#include <cstddef>
#include <string>

namespace boost {
namespace urls {

#ifndef BOOST_URL_DOCS
struct param_pct_view;
struct param_view;
#endif

/** The type of no_value
*/
struct no_value_t
{
};

/** Constant indicating no value in a param
*/
constexpr no_value_t no_value{};

//------------------------------------------------

/** A query parameter

    Objects of this type represent a single key
    and value pair in a query string where a key
    is always present and may be empty, while the
    presence of a value is indicated by
    @ref has_value equal to true.
    An empty value is distinct from no value.

    Depending on where the object was obtained,
    the strings may or may not contain percent
    escapes.

    For most usages, key comparisons are
    case-sensitive and duplicate keys in
    a query are possible. However, it is
    the authority that has final control
    over how the query is interpreted.

    @par BNF
    @code
    query-params    = query-param *( "&" query-param )
    query-param     = key [ "=" value ]
    key             = *qpchar
    value           = *( qpchar / "=" )
    @endcode

    @par Specification
    @li <a href="https://en.wikipedia.org/wiki/Query_string"
        >Query string (Wikipedia)</a>

    @see
        @ref param_view,
        @ref param_pct_view.
*/
struct param
{
    /** The key

        For most usages, key comparisons are
        case-sensitive and duplicate keys in
        a query are possible. However, it is
        the authority that has final control
        over how the query is interpreted.
    */
    std::string key;

    /** The value

        The presence of a value is indicated by
        @ref has_value equal to true.
        An empty value is distinct from no value.
    */
    std::string value;

    /** True if a value is present

        The presence of a value is indicated by
        `has_value == true`.
        An empty value is distinct from no value.
    */
    bool has_value = false;

    /** Constructor

        Default constructed query parameters
        have an empty key and no value.

        @par Example
        @code
        param qp;
        @endcode

        @par Postconditions
        @code
        this->key == "" && this->value == "" && this->has_value == false
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    param() = default;

    /** Constructor

        Upon construction, this acquires
        ownership of the members of other
        via move construction. The moved
        from object is as if default
        constructed.

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par other The object to construct from.
    */
    param(param&& other) noexcept
        : key(std::move(other.key))
        , value(std::move(other.value))
        , has_value(other.has_value)
    {
    #ifdef BOOST_URL_COW_STRINGS
        // for copy-on-write std::string
        other.key.clear();
        other.value.clear();
    #endif
        other.has_value = false;
    }

    /** Constructor

        Upon construction, this becomes a copy
        of `other`.

        @par Postconditions
        @code
        this->key == other.key && this->value == other.value && this->has_value == other.has_value
        @endcode

        @par Complexity
        Linear in `other.key.size() + other.value.size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par other The object to construct from.
    */
    param(param const& other) = default;

    /** Assignment

        Upon assignment, this acquires
        ownership of the members of other
        via move assignment. The moved
        from object is as if default
        constructed.

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @par other The object to assign from.
    */
    param&
    operator=(param&& other) noexcept
    {
        key = std::move(other.key);
        value = std::move(other.value);
        has_value = other.has_value;
    #ifdef BOOST_URL_COW_STRINGS
        // for copy-on-write std::string
        other.key.clear();
        other.value.clear();
    #endif
        other.has_value = false;
        return *this;
    }

    /** Assignment

        Upon assignment, this becomes a copy
        of `other`.

        @par Postconditions
        @code
        this->key == other.key && this->value == other.value && this->has_value == other.has_value
        @endcode

        @par Complexity
        Linear in `other.key.size() + other.value.size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @par other The object to assign from.
    */
    param& operator=(
        param const&) = default;

    //--------------------------------------------

    /** Constructor

        This constructs a parameter with a key
        and value.

        No validation is performed on the strings.
        Ownership of the key and value is acquired
        by making copies.

        @par Example
        @code
        param qp( "key", "value" );
        @endcode

        @code
        param qp( "key", optional<core::string_view>("value") );
        @endcode

        @code
        param qp( "key", boost::none );
        @endcode

        @code
        param qp( "key", nullptr );
        @endcode

        @code
        param qp( "key", no_value );
        @endcode

        @par Postconditions
        @code
        this->key == key && this->value == value && this->has_value == true
        @endcode

        @par Complexity
        Linear in `key.size() + value.size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @tparam OptionalString An optional string
        type, such as `core::string_view`,
        `std::nullptr`, @ref no_value_t, or
        `optional<core::string_view>`.

        @param key, value The key and value to set.
    */
    template <class OptionalString>
    param(
        core::string_view key,
        OptionalString const& value)
        : param(key, detail::get_optional_string(value))
    {
    }

    /** Assignment

        The members of `other` are copied,
        re-using already existing string capacity.

        @par Postconditions
        @code
        this->key == other.key && this->value == other.value && this->has_value == other.has_value
        @endcode

        @par Complexity
        Linear in `other.key.size() + other.value.size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @param other The parameter to copy.
    */
    param&
    operator=(param_view const& other);

    /** Assignment

        The members of `other` are copied,
        re-using already existing string capacity.

        @par Postconditions
        @code
        this->key == other.key && this->value == other.value && this->has_value == other.has_value
        @endcode

        @par Complexity
        Linear in `other.key.size() + other.value.size()`.

        @par Exception Safety
        Calls to allocate may throw.

        @param other The parameter to copy.
    */
    param&
    operator=(param_pct_view const& other);

#ifndef BOOST_URL_DOCS
    // arrow support
    param const*
    operator->() const noexcept
    {
        return this;
    }

    // aggregate construction
    param(
        core::string_view key,
        core::string_view value,
        bool has_value) noexcept
        : key(key)
        , value(has_value
            ? value
            : core::string_view())
        , has_value(has_value)
    {
    }
#endif

private:
    param(
        core::string_view key,
        detail::optional_string const& value)
        : param(key, value.s, value.b)
    {
    }
};

//------------------------------------------------

/** A query parameter

    Objects of this type represent a single key
    and value pair in a query string where a key
    is always present and may be empty, while the
    presence of a value is indicated by
    @ref has_value equal to true.
    An empty value is distinct from no value.

    Depending on where the object was obtained,
    the strings may or may not contain percent
    escapes.

    For most usages, key comparisons are
    case-sensitive and duplicate keys in
    a query are possible. However, it is
    the authority that has final control
    over how the query is interpreted.

    <br>

    Keys and values in this object reference
    external character buffers.
    Ownership of the buffers is not transferred;
    the caller is responsible for ensuring that
    the assigned buffers remain valid until
    they are no longer referenced.

    @par BNF
    @code
    query-params    = query-param *( "&" query-param )
    query-param     = key [ "=" value ]
    key             = *qpchar
    value           = *( qpchar / "=" )
    @endcode

    @par Specification
    @li <a href="https://en.wikipedia.org/wiki/Query_string"
        >Query string (Wikipedia)</a>

    @see
        @ref param,
        @ref param_pct_view.
*/
struct param_view
{
    /** The key

        For most usages, key comparisons are
        case-sensitive and duplicate keys in
        a query are possible. However, it is
        the authority that has final control
        over how the query is interpreted.
    */
    core::string_view key;

    /** The value

        The presence of a value is indicated by
        @ref has_value equal to true.
        An empty value is distinct from no value.
    */
    core::string_view value;

    /** True if a value is present

        The presence of a value is indicated by
        `has_value == true`.
        An empty value is distinct from no value.
    */
    bool has_value = false;

    //--------------------------------------------

    /** Constructor

        Default constructed query parameters
        have an empty key and no value.

        @par Example
        @code
        param_view qp;
        @endcode

        @par Postconditions
        @code
        this->key == "" && this->value == "" && this->has_value == false
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    param_view() = default;

    /** Constructor

        This constructs a parameter with a key
        and value.
        No validation is performed on the strings.
        The new key and value reference
        the same corresponding underlying
        character buffers.
        Ownership of the buffers is not transferred;
        the caller is responsible for ensuring that
        the assigned buffers remain valid until
        they are no longer referenced.

        @par Example
        @code
        param_view qp( "key", "value" );
        @endcode

        @par Postconditions
        @code
        this->key.data() == key.data() && this->value.data() == value.data() && this->has_value == true
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @tparam OptionalString An optional string
        type, such as `core::string_view`,
        `std::nullptr`, @ref no_value_t, or
        `optional<core::string_view>`.

        @param key, value The key and value to set.
    */
    template <class OptionalString>
    param_view(
        core::string_view key,
        OptionalString const& value) noexcept
        : param_view(key, detail::get_optional_string(value))
    {
    }

    /** Constructor

        This function constructs a param
        which references the character buffers
        representing the key and value in another
        container.
        Ownership of the buffers is not transferred;
        the caller is responsible for ensuring that
        the assigned buffers remain valid until
        they are no longer referenced.

        @par Example
        @code
        param qp( "key", "value" );
        param_view qpv( qp );
        @endcode

        @par Postconditions
        @code
        this->key == key && this->value == value && this->has_value == other.has_value
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.

        @param other The param to reference
    */
    param_view(
        param const& other) noexcept
        : param_view(
            other.key,
            other.value,
            other.has_value)
    {
    }

    /** Conversion

        This function performs a conversion from
        a reference-like query parameter to one
        retaining ownership of the strings by
        making a copy.
        No validation is performed on the strings.

        @par Complexity
        Linear in `this->key.size() + this->value.size()`.

        @par Exception Safety
        Calls to allocate may throw.
    */
    explicit
    operator
    param()
    {
        return { key, value, has_value };
    }

#ifndef BOOST_URL_DOCS
    // arrow support
    param_view const*
    operator->() const noexcept
    {
        return this;
    }

    // aggregate construction
    param_view(
        core::string_view key_,
        core::string_view value_,
        bool has_value_) noexcept
        : key(key_)
        , value(has_value_
            ? value_
            : core::string_view())
        , has_value(has_value_)
    {
    }
#endif

private:
    param_view(
        core::string_view key,
        detail::optional_string const& value)
        : param_view(key, value.s, value.b)
    {
    }
};

//------------------------------------------------

/** A query parameter

    Objects of this type represent a single key
    and value pair in a query string where a key
    is always present and may be empty, while the
    presence of a value is indicated by
    @ref has_value equal to true.
    An empty value is distinct from no value.

    The strings may have percent escapes, and
    offer an additional invariant: they never
    contain an invalid percent-encoding.

    For most usages, key comparisons are
    case-sensitive and duplicate keys in
    a query are possible. However, it is
    the authority that has final control
    over how the query is interpreted.

    <br>

    Keys and values in this object reference
    external character buffers.
    Ownership of the buffers is not transferred;
    the caller is responsible for ensuring that
    the assigned buffers remain valid until
    they are no longer referenced.

    @par BNF
    @code
    query-params    = query-param *( "&" query-param )
    query-param     = key [ "=" value ]
    key             = *qpchar
    value           = *( qpchar / "=" )
    @endcode

    @par Specification
    @li <a href="https://en.wikipedia.org/wiki/Query_string"
        >Query string (Wikipedia)</a>

    @see
        @ref param,
        @ref param_view.
*/
struct param_pct_view
{
    /** The key

        For most usages, key comparisons are
        case-sensitive and duplicate keys in
        a query are possible. However, it is
        the authority that has final control
        over how the query is interpreted.
    */
    pct_string_view key;

    /** The value

        The presence of a value is indicated by
        @ref has_value equal to true.
        An empty value is distinct from no value.
    */
    pct_string_view value;

    /** True if a value is present

        The presence of a value is indicated by
        `has_value == true`.
        An empty value is distinct from no value.
    */
    bool has_value = false;

    //--------------------------------------------

    /** Constructor

        Default constructed query parameters
        have an empty key and no value.

        @par Example
        @code
        param_pct_view qp;
        @endcode

        @par Postconditions
        @code
        this->key == "" && this->value == "" && this->has_value == false
        @endcode

        @par Complexity
        Constant.

        @par Exception Safety
        Throws nothing.
    */
    param_pct_view() = default;

    /** Constructor

        This constructs a parameter with a key
        and value, which may both contain percent
        escapes.
        The new key and value reference
        the same corresponding underlying
        character buffers.
        Ownership of the buffers is not transferred;
        the caller is responsible for ensuring that
        the assigned buffers remain valid until
        they are no longer referenced.

        @par Example
        @code
        param_pct_view qp( "key", "value" );
        @endcode

        @par Postconditions
        @code
        this->key.data() == key.data() && this->value.data() == value.data() && this->has_value == true
        @endcode

        @par Complexity
        Linear in `key.size() + value.size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` or `value` contains an invalid percent-encoding.

        @param key, value The key and value to set.
    */
    param_pct_view(
        pct_string_view key,
        pct_string_view value) noexcept
        : key(key)
        , value(value)
        , has_value(true)
    {
    }

    /** Constructor

        This constructs a parameter with a key
        and optional value, which may both
        contain percent escapes.

        The new key and value reference
        the same corresponding underlying
        character buffers.

        Ownership of the buffers is not transferred;
        the caller is responsible for ensuring that
        the assigned buffers remain valid until
        they are no longer referenced.

        @par Example
        @code
        param_pct_view qp( "key", optional<core::string_view>("value") );
        @endcode

        @par Postconditions
        @code
        this->key.data() == key.data() && this->value->data() == value->data() && this->has_value == true
        @endcode

        @par Complexity
        Linear in `key.size() + value->size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` or `value` contains an invalid percent-encoding.

        @tparam OptionalString An optional
        `core::string_view` type, such as
        `boost::optional<core::string_view>` or
        `std::optional<core::string_view>`.

        @param key, value The key and value to set.
    */
    template <class OptionalString>
    param_pct_view(
        pct_string_view key,
        OptionalString const& value)
        : param_pct_view(key, detail::get_optional_string(value))
    {
    }

    /** Construction

        This converts a param which may
        contain unvalidated percent-escapes into
        a param whose key and value are
        guaranteed to contain strings with no
        invalid percent-escapes, otherwise
        an exception is thrown.

        The new key and value reference
        the same corresponding underlying
        character buffers.
        Ownership of the buffers is not transferred;
        the caller is responsible for ensuring that
        the assigned buffers remain valid until
        they are no longer referenced.

        @par Example
        @code
        param_pct_view qp( param_view( "key", "value" ) );
        @endcode

        @par Complexity
        Linear in `key.size() + value.size()`.

        @par Exception Safety
        Exceptions thrown on invalid input.

        @throw system_error
        `key` or `value` contains an invalid percent escape.

        @param p The param to construct from.
    */
    explicit
    param_pct_view(
        param_view const& p)
        : key(p.key)
        , value(p.has_value
            ? pct_string_view(p.value)
            : pct_string_view())
        , has_value(p.has_value)
    {
    }

    /** Conversion

        This function performs a conversion from
        a reference-like query parameter to one
        retaining ownership of the strings by
        making a copy.

        @par Complexity
        Linear in `this->key.size() + this->value.size()`.

        @par Exception Safety
        Calls to allocate may throw.
    */
    explicit
    operator
    param() const
    {
        return param(
            static_cast<std::string>(key),
            static_cast<std::string>(value),
            has_value);
    }

    operator
    param_view() const noexcept
    {
        return param_view(
            key, value, has_value);
    }

#ifndef BOOST_URL_DOCS
    // arrow support
    param_pct_view const*
    operator->() const noexcept
    {
        return this;
    }

    // aggregate construction
    param_pct_view(
        pct_string_view key,
        pct_string_view value,
        bool has_value) noexcept
        : key(key)
        , value(has_value
            ? value
            : pct_string_view())
        , has_value(has_value)
    {
    }
#endif

private:
    param_pct_view(
        pct_string_view key,
        detail::optional_string const& value)
        : param_pct_view(key, value.s, value.b)
    {
    }
};

//------------------------------------------------

inline
param&
param::
operator=(
    param_view const& other)
{
    // VFALCO operator= assignment
    // causes a loss of original capacity:
    // https://godbolt.org/z/nYef8445K
    //
    // key = other.key;
    // value = other.value;

    // preserve capacity
    key.assign(
        other.key.data(),
        other.key.size());
    value.assign(
        other.value.data(),
        other.value.size());
    has_value = other.has_value;
    return *this;
}

inline
param&
param::
operator=(
    param_pct_view const& other)
{
    // preserve capacity
    key.assign(
        other.key.data(),
        other.key.size());
    value.assign(
        other.value.data(),
        other.value.size());
    has_value = other.has_value;
    return *this;
}

} // urls
} // boost

#endif
